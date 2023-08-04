package ru.mishapp.handlers;


import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import ru.mishapp.Constants;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.dto.PeriodicChangeRuleDTO;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.services.PeriodicChangeRuleService;

@TelegramHandler(value = "правила", description = "Добавить правило автоматического изменения счетов")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class PeriodicChangeRuleHandler {
    
    private final PeriodicChangeRuleService periodicChangeRuleService;
    
    @TelegramCommand("создать")
    public String create(@TelegramParam("правило") String name,
                         @TelegramParam("изменение") String pcName,
                         @TelegramParam("счёт") String taName,
                         @TelegramParam("сумма") String sum,
                         @TelegramParam("тип") String type,
                         @TelegramParam("пропусков") String pass,
                         @TelegramParam("начало") String startDay,
                         Long chatId
    ) {
        try {
            int intSum = Integer.parseInt(sum);
            int intPass = Integer.parseInt(pass);
            LocalDate localDate = LocalDate.parse(startDay, Constants.DAY);
            PeriodicChangeRuleDTO ruleDTO = new PeriodicChangeRuleDTO(
                name, pcName, taName, null, intSum, type, intPass, localDate
            );
            
            PeriodicChangeRule rule = periodicChangeRuleService.create(ruleDTO, chatId);
            
            return "Создано правило - " + rule.toTelegram();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
