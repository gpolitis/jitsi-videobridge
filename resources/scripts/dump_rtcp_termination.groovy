state.videobridge.conferences.each {
    def conference = [:];
    // conference.put("name", it.name);
    def contents = [];
    conference.put("contents", contents);
    it.contents.each {
        def content = [:];
        contents.add(content);

        // content.put("name", it.name);
        def channels = [];
        content.put("channels", channels);
        it.channels.findAll {
            it.hasProperty("stream")
        }.each {
            def channel = [:];
            channels.add(channel);

            channel.put("endpoint", it.endpoint.id);
            if (it.stream.rtcpTransformEngineWrapper.wrapped != null) {
                channel.put("strategy", it.stream.rtcpTransformEngineWrapper.wrapped.class.name);
            } else {
                channel.put("strategy", "null");
            }
        }
    }

    import groovy.json.JsonOutput as JsonOutput
    println JsonOutput.prettyPrint(JsonOutput.toJson(conference)).stripIndent();
}

