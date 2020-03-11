package io.projectriff.invoker.converters;

import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeType;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class NegotiatingStringMessageConverter implements MessageConverter {

    private final Charset defaultCharset;

    private final MimeType supportedMimeType;

    public NegotiatingStringMessageConverter() {
        this(StandardCharsets.UTF_8);
    }

    public NegotiatingStringMessageConverter(Charset defaultCharset) {
        this.defaultCharset = defaultCharset;
        this.supportedMimeType = new MimeType("text", "plain");
    }

    @Override
    public String fromMessage(@NonNull Message<?> message, @NonNull Class<?> type) {
        if (type != String.class) {
            return null;
        }
        String rawContentType = message.getHeaders().get(MessageHeaders.CONTENT_TYPE, String.class);
        if (rawContentType == null) {
            return null; // TODO: throw?
        }
        MimeType contentType = MimeType.valueOf(rawContentType);
        if (!supportedMimeType.equalsTypeAndSubtype(contentType)) {
            return null;
        }
        Charset messageCharset = contentType.getCharset();
        Charset charset = messageCharset == null ? defaultCharset : messageCharset;
        return new String((byte[]) message.getPayload(), charset);
    }

    @Override
    public Message<?> toMessage(@NonNull Object object, MessageHeaders messageHeaders) {
        if (!(object instanceof String)) {
            return null;
        }
        if (!supportedMimeType.isCompatibleWith(acceptableMimeType(messageHeaders))) {
            return null;
        }

        // TODO: set charset??
        MessageBuilder<Object> builder = MessageBuilder.withPayload(object);
        if (messageHeaders != null) {
            builder = builder.copyHeaders(messageHeaders);
        }
        return builder
                .setHeader(MessageHeaders.CONTENT_TYPE, this.supportedMimeType.toString())
                .build();
    }

    private MimeType acceptableMimeType(MessageHeaders messageHeaders) {
        String defaultMimeType = "*/*";
        if (messageHeaders == null) {
            return MimeType.valueOf(defaultMimeType);
        }
        return MimeType.valueOf((String) messageHeaders.getOrDefault("Accept", defaultMimeType));
    }
}
