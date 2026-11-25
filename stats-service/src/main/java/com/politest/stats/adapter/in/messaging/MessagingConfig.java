package com.politest.stats.adapter.in.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Declares the topic this adapter reads, so the service can start before the producer does. */
@Configuration
class MessagingConfig {

    @Bean
    NewTopic testCompletedTopic() {
        return TopicBuilder.name(TestCompletedMessage.TOPIC).partitions(3).replicas(1).build();
    }
}
