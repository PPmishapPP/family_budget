package ru.mishapp.listeners;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.mishapp.KafkaTopicConfig;
import ru.mishapp.dto.KafkaMessage;
import ru.mishapp.dto.PeriodicChangeRuleDTO;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.services.PeriodicChangeRuleService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodicChangeRuleListener {
    
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<Long, KafkaMessage> kafkaTemplate;
    private final PeriodicChangeRuleService periodicChangeRuleService;
    
    @KafkaListener(topics = "#{@KafkaTopicConfig.PERIODIC_CHANGE_RULE_CREATE_TOPIC}", groupId = "backConsumeGroup")
    @SneakyThrows
    public void listenCreateAccount(ConsumerRecord<Long, KafkaMessage> rec) {
        KafkaMessage request = rec.value();
        
        PeriodicChangeRule rule = periodicChangeRuleService.create(
            objectMapper.readValue(request.value(), PeriodicChangeRuleDTO.class),
            request.chatId()
        );
        
        KafkaMessage response = new KafkaMessage(request.chatId(), "Создано правило - " + rule.toTelegram());
        log.info(response.toString());
        kafkaTemplate.send(KafkaTopicConfig.ACCOUNT_RESPONSE_TOPIC, response);
    }
}
