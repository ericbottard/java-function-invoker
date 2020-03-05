package com.acme;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Function;

public class MessageFunction implements Function<Message<String>, Message<Integer>> {

    @Override
    public Message<Integer> apply(Message<String> stringMessage) {
        System.out.println("Input = " + stringMessage);
        Message<Integer> result = MessageBuilder.withPayload(stringMessage.getPayload().length()).build();
        System.out.println("Result in function " + result);
        return result;
    }
}
