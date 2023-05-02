package ru.mishapp.handlers;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import ru.mishapp.KafkaTopicConfig;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.config.Constans;
import ru.mishapp.dto.KafkaMessage;
import ru.mishapp.dto.PeriodicChangeRuleDTO;
import ru.mishapp.services.KafkaSendService;

import java.time.LocalDate;

@TelegramHandler("rule")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class PeriodicChangeRuleHandler {
    
    private final KafkaSendService kafkaSendService;
    private final ObjectMapper objectMapper;
    
    @TelegramCommand("create_e")
    public String create(@TelegramParam("name") String name,
                         @TelegramParam("pcName") String pcName,
                         @TelegramParam("taName") String taName,
                         @TelegramParam("sum") String sum,
                         @TelegramParam("type") String type,
                         @TelegramParam("pass") String pass,
                         @TelegramParam("startDay") String startDay,
                         Long chatId
    ) {
        try {
            int intSum = Integer.parseInt(sum);
            int intPass = Integer.parseInt(pass);
            LocalDate localDate = LocalDate.parse(startDay, Constans.DAY);
            PeriodicChangeRuleDTO rule = new PeriodicChangeRuleDTO(
                name, pcName, taName, null, intSum, type, intPass, localDate
            );
            
            kafkaSendService.send(
                new KafkaMessage(chatId, objectMapper.writeValueAsString(rule)),
                KafkaTopicConfig.PERIODIC_CHANGE_RULE_CREATE_TOPIC
            );
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        return null;
    }
}
