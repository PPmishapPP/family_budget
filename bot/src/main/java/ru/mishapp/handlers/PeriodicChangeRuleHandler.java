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

@TelegramHandler("rule")
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class PeriodicChangeRuleHandler {
    
    private final PeriodicChangeRuleService periodicChangeRuleService;
    
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
