package ru.mishapp.listeners;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.mishapp.KafkaTopicConfig;
import ru.mishapp.dto.KafkaMessage;
import ru.mishapp.entity.Account;
import ru.mishapp.services.AccountService;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountListener {
    
    private final KafkaTemplate<Long, KafkaMessage> kafkaTemplate;
    private final AccountService accountService;
    
    @KafkaListener(topics = "#{@KafkaTopicConfig.ACCOUNT_READ_TOPIC}", groupId = "backConsumeGroup")
    @SneakyThrows
    public void listenReadAccount(ConsumerRecord<Long, KafkaMessage> rec) {
        KafkaMessage request = rec.value();
        String message;
        if (StringUtils.hasText(request.value())) {
            message = accountService.readByName(request.value(), request.chatId()).toTelegram();
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (Account account : accountService.readAll()) {
                stringBuilder.append(account.toTelegram());
                stringBuilder.append("\n");
            }
            message = stringBuilder.isEmpty() ? "Нет аккаунтов" : stringBuilder.toString();
        }
        KafkaMessage response = new KafkaMessage(request.chatId(), message);
        log.info(response.toString());
        kafkaTemplate.send(KafkaTopicConfig.ACCOUNT_RESPONSE_TOPIC, response);
    }
    
    @KafkaListener(topics = "#{@KafkaTopicConfig.ACCOUNT_CREATE_TOPIC}", groupId = "backConsumeGroup")
    @SneakyThrows
    public void listenCreateAccount(ConsumerRecord<Long, KafkaMessage> rec) {
        KafkaMessage request = rec.value();
        Account account = accountService.create(request.value(), request.chatId());
        KafkaMessage response = new KafkaMessage(request.chatId(), "Создан аккаунт - " + account.toTelegram());
        log.info(response.toString());
        kafkaTemplate.send(KafkaTopicConfig.ACCOUNT_RESPONSE_TOPIC, response);
    }
    
}
