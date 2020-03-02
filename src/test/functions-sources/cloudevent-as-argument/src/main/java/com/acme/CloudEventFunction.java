package com.acme;

import io.cloudevents.v1.CloudEventBuilder;
import io.cloudevents.v1.CloudEventImpl;

import java.net.URI;
import java.util.function.Function;

public class CloudEventFunction implements Function<CloudEventImpl<String>, CloudEventImpl<Integer>> {

    @Override
    public CloudEventImpl<Integer> apply(CloudEventImpl<String> stringCloudEvent) {
        System.out.println(stringCloudEvent);
        return CloudEventBuilder.<Integer>builder()
                .withId("the-id")
                .withData(42)
                .withDataContentType("application/json")
                .withSource(URI.create("the-source"))
                .withSubject("the-subject")
                .build();
    }
}
