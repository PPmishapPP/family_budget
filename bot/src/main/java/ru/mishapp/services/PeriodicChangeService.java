package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.repository.PeriodicChangeRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PeriodicChangeService {
    
    private final PeriodicChangeRepository periodicChangeRepository;
    
    public PeriodicChange create(String name, long chatId) {
        PeriodicChange periodicChange = new PeriodicChange(name, chatId);
        return periodicChangeRepository.save(periodicChange);
    }
    
    public List<PeriodicChange> readAll(long chatId) {
        return periodicChangeRepository.findAllByChatId(chatId);
    }

    public Map<String, PeriodicChange> findAll(long chatId) {
        List<PeriodicChange> periodicChangeList = periodicChangeRepository.findAllByChatId(chatId);
        return periodicChangeList.stream().collect(Collectors.toMap(PeriodicChange::getName, Function.identity()));
    }
}
