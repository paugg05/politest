package com.politest.stats.adapter.in.messaging;

import com.politest.stats.application.port.in.RegisterCompletedTestUseCase;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/**
 * Wire format of the {@code test.completed} event, as published by the web application.
 * This class is the consumer's copy of the contract: unknown fields are ignored so that the
 * producer can add fields without breaking this service.
 */
record TestCompletedMessage(String eventId, String occurredAt, String language, Map<String, Integer> rightPct) {

    static final String TOPIC = "test.completed";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    static TestCompletedMessage fromJson(String payload) {
        return JSON.readValue(payload, TestCompletedMessage.class);
    }

    RegisterCompletedTestUseCase.Command toCommand() {
        return new RegisterCompletedTestUseCase.Command(eventId, occurredAt, language, rightPct);
    }
}
