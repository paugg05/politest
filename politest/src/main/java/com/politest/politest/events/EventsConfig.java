package com.politest.politest.events;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Declares the topics this module publishes to; Spring creates them on startup if they are missing. */
@Configuration
class EventsConfig {

    @Bean
    NewTopic testCompletedTopic() {
        return TopicBuilder.name(TestEvents.TEST_COMPLETED_TOPIC).partitions(3).replicas(1).build();
    }
}
