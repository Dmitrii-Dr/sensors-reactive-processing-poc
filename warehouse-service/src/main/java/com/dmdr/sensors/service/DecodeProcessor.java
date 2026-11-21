package com.dmdr.sensors.service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DecodeProcessor extends AbstractMessageProcessor {
    @Override
    protected void doProcess() {
        log.info("DecodeProcessor: processing");
    }
}
