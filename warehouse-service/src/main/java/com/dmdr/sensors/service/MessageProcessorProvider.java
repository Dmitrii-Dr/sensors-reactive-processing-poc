package com.dmdr.sensors.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageProcessorProvider {

    private static final MessageProcessor CHAIN_HEAD = buildChain(List.of(
            new DecodeProcessor(),
            new ValidationProcessor()
    ));

    public static MessageProcessor getChain() {
        return CHAIN_HEAD;
    }

    private static MessageProcessor buildChain(List<MessageProcessor> processors) {
        MessageProcessor current = processors.get(0);
        for (int i = 1; i < processors.size(); i++) {
            current = current.setNext(processors.get(i));
        }
        return processors.get(0);
    }
}
