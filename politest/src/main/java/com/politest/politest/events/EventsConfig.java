package com.politest.politest.events;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Declares the topics this module publishes to and enables the scheduled outbox relay. */
@Configuration
@EnableScheduling
class EventsConfig {

    @Bean
    NewTopic testCompletedTopic() {
        return TopicBuilder.name(TestEvents.TEST_COMPLETED_TOPIC).partitions(3).replicas(1).build();
    }
}
