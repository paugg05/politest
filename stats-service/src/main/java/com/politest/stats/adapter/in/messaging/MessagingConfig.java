package com.politest.stats.adapter.in.messaging;

import com.politest.stats.domain.error.DomainValidationException;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

/** Topics and error handling of the messaging adapter. */
@Configuration
class MessagingConfig {

    static final String DEAD_LETTER_TOPIC = TestCompletedMessage.TOPIC + ".DLT";

    private static final int PARTITIONS = 3;

    /** Declares the topic this adapter reads, so the service can start before the producer does. */
    @Bean
    NewTopic testCompletedTopic() {
        return TopicBuilder.name(TestCompletedMessage.TOPIC).partitions(PARTITIONS).replicas(1).build();
    }

    /** Where records that cannot be processed end up. It needs as many partitions as the source topic. */
    @Bean
    NewTopic testCompletedDeadLetterTopic() {
        return TopicBuilder.name(DEAD_LETTER_TOPIC).partitions(PARTITIONS).replicas(1).build();
    }

    /**
     * Failure policy for the listener. Spring Boot adds this bean to the auto-configured listener container.
     *
     * <ul>
     *   <li>Transient failures (for example the database being briefly unavailable) are retried three times,
     *       waiting 0.5 s, 1 s and 2 s.</li>
     *   <li>Permanent failures (an unreadable payload or a value that breaks a domain rule) skip the retries:
     *       trying again would give the same result.</li>
     *   <li>When retries are exhausted, or skipped, the record is published to the dead-letter topic with the
     *       error in its headers, and the consumer moves on, so one bad record never blocks the partition.</li>
     * </ul>
     */
    @Bean
    DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
                (record, exception) -> new TopicPartition(DEAD_LETTER_TOPIC, record.partition()));

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(500L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(5_000L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        handler.addNotRetryableExceptions(MalformedEventException.class, DomainValidationException.class);
        return handler;
    }
}
