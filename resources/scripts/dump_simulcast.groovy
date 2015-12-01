state.videobridge.conferences.each {
    def conference = [:];
    conference.put("name", it.name);
    def contents = [];
    conference.put("contents", contents);
    it.contents.each {
        def content = [:];
        contents.add(content);

        content.put("name", it.name);
        def channels = [];
        content.put("channels", channels);
        it.channels.findAll {
            it.hasProperty("transformEngine")
        }.each {
            def channel = [:];
            channels.add(channel);

            channel.put("endpoint", it.endpoint.id);

            // Dump simulcast.
            if (it.transformEngine.hasProperty("simulcastEngine"))
            {
                def simulcastSenders = [];
                channel.put("simulcastSenders", simulcastSenders);
                it.transformEngine.simulcastEngine.simulcastSenderManager.senders.each { itReceiver, itSender ->
                    def sender = [:];
                    simulcastSenders.add(sender);
                    def receiver = [:];
                    sender.put("receiver", receiver);

                    def simulcastStreams = [];
                    receiver.put("simulcast_streams", simulcastStreams);

                    def toSimulcastStream = { itSimulcastStream ->
                        def simulcastStream = [:];
                        simulcastStream.put("primary_ssrc", itSimulcastStream.primarySSRC);
                        simulcastStream.put("rtx_ssrc", itSimulcastStream.rtxSSRC);
                        simulcastStream.put("fec_ssrc", itSimulcastStream.fecSSRC);
                        simulcastStream.put("is_streaming", itSimulcastStream.isStreaming);
                        simulcastStream.put("order", itSimulcastStream.order);
                        return simulcastStream;
                    }

                    itReceiver.simulcastLayers.each {
                        def simulcastStream = toSimulcastStream(it);
                        simulcastStreams.add(simulcastStream);
                    }

                    def sendSimulcastStream = toSimulcastStream(itSender.sendMode.current);
                    sender.put("send_layer", sendSimulcastStream);
                };
            }

            // Dump SSRC rewriting.
            if (it.stream.hasProperty("ssrcRewritingEngine") && it.stream.ssrcRewritingEngine.initialized)
            {
                def group_rewriters = [];
                channel.put("group_rewriters", group_rewriters);
                it.stream.ssrcRewritingEngine.origin2rewriter.values().each {
                    def group_rewriter = [:];
                    group_rewriters.add(group_rewriter);

                    group_rewriter.put("target", it.ssrcTarget);

                    def rewriters = [];
                    group_rewriter.put("rewriters", rewriters);
                    it.rewriters.each {
                        def rewriter = [:];
                        rewriters.add(rewriter);
                        rewriter.put("sourceSSRC", it.sourceSSRC);

                        def intervals = [];
                        rewriter.put("intervals", intervals);
                        it.intervals.values().each {
                            def interval = [:];
                            intervals.add(interval);
                            interval.put("min", it.extendedMinOrig);
                            interval.put("max", it.extendedMaxOrig);
                        }
                    }
                }
            }

            // Dump RTCP termination.
            if (it.stream.hasProperty("rtcpTransformEngineWrapper") && it.stream.rtcpTransformEngineWrapper.wrapped != null) {
                channel.put("strategy", it.stream.rtcpTransformEngineWrapper.wrapped.class.name);
            } else {
                channel.put("strategy", "null");
            }
        }
    }

    import groovy.json.JsonOutput as JsonOutput;
    println JsonOutput.prettyPrint(JsonOutput.toJson(conference)).stripIndent();
}

