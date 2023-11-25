package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.entity.NurseVisit;
import ru.mishapp.repository.NurseRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NurseService {

    private final NurseRepository nurseRepository;

    public void fixedStartNurseVisit(LocalDate visitDay, LocalTime startVisit, Long chatId) {
        NurseVisit nurseVisit = new NurseVisit(visitDay, chatId, startVisit);
        nurseRepository.save(nurseVisit);
    }

    public String endNurseVisit(LocalDate visitDay, LocalTime endVisit, Long chatId) {
        Optional<NurseVisit> nurseVisitByVisitDay = nurseRepository.findNurseVisitByVisitDay(visitDay, chatId);
        if (nurseVisitByVisitDay.isEmpty()) {
            throw new IllegalArgumentException("Не найдена запись посещения няни");
        }
        return processVisitTime(nurseVisitByVisitDay.get(), endVisit);
    }

    private String processVisitTime(NurseVisit nurseVisit, LocalTime endVisit) {
        nurseVisit.setVisitEnd(endVisit);
        nurseRepository.save(nurseVisit);
        return "Время прибывания у няни ";
    }
}
