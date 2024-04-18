package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.entity.NurseVisit;
import ru.mishapp.repository.NurseRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NurseService {

    private static final int HOUR_PAYOUT = 250;
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
        LocalTime visitStart = nurseVisit.getVisitStart();
        long until = visitStart.until(endVisit, ChronoUnit.SECONDS);
        LocalTime visitingTime = LocalTime.ofSecondOfDay(until);

        return "Время прибывания у няни " +
                visitingTime.getHour() +
                " часов " +
                visitingTime.getMinute() +
                " минут.\n" +
                calculatePayout(visitingTime);
    }

    private String calculatePayout(LocalTime visitingTime) {
        int payout;
        int hours = visitingTime.getHour();
        int minutes = visitingTime.getMinute();
        if (hours > 0 && hours <= 8) {
            payout = calc(hours, minutes, HOUR_PAYOUT);
        } else {
            payout = calc(8, 0, HOUR_PAYOUT);
            payout = payout + calc(hours - 8, minutes, HOUR_PAYOUT * 2);
        }
        return String.format("К оплате %d рублей", payout);
    }

    private int calc(int hour, int minute, int hourPayout) {
        int payout = hourPayout * hour;
        if (minute > 0 && minute <= 30) {
            payout = payout + hourPayout / 2;
        } else if (minute > 30) {
            payout = payout + hourPayout;
        }
        return payout;
    }
}
