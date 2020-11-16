package com.jmorder.jmoserviceauth.config.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jmorder.jmoserviceauth.config.pubsub.envelop.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.cloud.gcp.pubsub.support.converter.JacksonPubSubMessageConverter;
import org.springframework.cloud.gcp.pubsub.support.converter.PubSubMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;

@Configuration
public class PubsubConfig {
    @Value("${jmo.topics.data-sync}")
    private String DATA_SYNC_TOPIC;
    @Autowired
    private ObjectMapper objectMapper;

    @MessagingGateway(defaultRequestChannel = "pubsubOutputChannel")
    public interface PubsubOutboundGateway {
        void sendUserToPubsub(UserMessage userMessage);
    }

    @Bean
    public PubSubMessageConverter pubSubMessageConverter() {
        return new JacksonPubSubMessageConverter(objectMapper);
    }

    @Bean
    @ServiceActivator(inputChannel = "pubsubOutputChannel")
    public MessageHandler dataSyncMessageSender(PubSubTemplate pubSubTemplate) {
        pubSubTemplate.setMessageConverter(pubSubMessageConverter());
        return new PubSubMessageHandler(pubSubTemplate, DATA_SYNC_TOPIC);
    }
}
