package ru.mishapp;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration("KafkaTopicConfig")
public class KafkaTopicConfig {
    
    public static final String ACCOUNT_READ_TOPIC = "accountRequestTopic";
    public static final String ACCOUNT_CREATE_TOPIC = "accountCreate";
    public static final String ACCOUNT_RESPONSE_TOPIC = "accountResponseTopic";
    public static final String CHANGE_TOPIC = "changeTopic";
    
    public static final String ACCOUNT_HISTORY_READ_TOPIC = "accountHistoryTopic";
    
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;
    
    @Bean
    @ConditionalOnMissingBean(KafkaAdmin.class)
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }
    
    @Bean
    public NewTopic accountRequestTopic() {
        return new NewTopic(ACCOUNT_READ_TOPIC, 1, (short) 1);
    }
    
    @Bean
    public NewTopic accountResponseTopic() {
        return new NewTopic(ACCOUNT_RESPONSE_TOPIC, 1, (short) 1);
    }
    
    @Bean
    public NewTopic accountCreateTopic() {
        return new NewTopic(ACCOUNT_CREATE_TOPIC, 1, (short) 1);
    }
    
    @Bean
    public NewTopic changeTopic() {
        return new NewTopic(CHANGE_TOPIC, 1, (short) 1);
    }
    
    @Bean
    public NewTopic historyTopic() {
        return new NewTopic(ACCOUNT_HISTORY_READ_TOPIC, 1, (short) 1);
    }
}
