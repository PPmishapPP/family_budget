package ru.mishapp.handlers;

import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;

@TelegramHandler("account")
@SuppressWarnings("unused")
public class AccountHandler {

    @TelegramCommand()
    public String readAll() {
        return "У вас очень-очень много денег!";
    }

    @TelegramCommand("read")
    public String readByName(@TelegramParam("name") String name) {
        return "На счету " + name + " у вас очень-очень много денег!";
    }
}
