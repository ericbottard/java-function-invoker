package io.projectriff.invoker.converters;

import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.ByteArrayMessageConverter;
import org.springframework.util.MimeType;

public class NegotiatingByteArrayMessageConverter extends ByteArrayMessageConverter {

    public NegotiatingByteArrayMessageConverter() {
        setStrictContentTypeMatch(true);
    }

    /**
     * Override of super implementation that doesn't bother with no supported mime-type (won't happen in our case)
     * and honors wildcard type requests.
     */
    @Override
    protected boolean supportsMimeType(MessageHeaders headers) {
        MimeType mimeType = getMimeType(headers);
        if (mimeType == null) {
            return !isStrictContentTypeMatch();
        }
        for (MimeType current : getSupportedMimeTypes()) {
            if (mimeType.includes(current)) {
                return true;
            }
        }
        return false;
    }


}
