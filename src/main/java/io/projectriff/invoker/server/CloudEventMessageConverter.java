package io.projectriff.invoker.server;

import io.cloudevents.CloudEvent;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.util.MimeType;

public class CloudEventMessageConverter extends AbstractMessageConverter {

    public CloudEventMessageConverter() {
        super(MimeType.valueOf("text/plain"));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        boolean result = CloudEvent.class.isAssignableFrom(clazz);
        System.out.println("Huh? " + clazz + " " + result);
        return result;
    }


}
