package ru.mishapp.schedulers;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.IBot;
import ru.mishapp.repository.SubmitMeterReadsRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class SubmitMeterReadsScheduler {

    private final SubmitMeterReadsRepository submitMeterReadsRepository;

    private final IBot iBot;

    public static final LocalTime EIGHT_BM = LocalTime.of(8, 0);
    public static final LocalTime TEN_AM = LocalTime.of(22, 0);

    public static final String DEMAND_TO_SEND_METER_READS = "Необходимо сдать показания счетчиков";

    @Scheduled(cron = "${schedule.submit-meter-reminder}")
    @Transactional
    public void execute() {
        LocalTime timeNow = LocalTime.now();
        if (timeNow.isAfter(EIGHT_BM) && timeNow.isBefore(TEN_AM)) {
            submitMeterReadsRepository.findAll().forEach(meterRead -> {
                LocalDateTime dbMeterReadDatetime = meterRead.getDatetimeOfSubmitMeterReads();
                LocalDateTime localDatetime = LocalDateTime.now();

                if (localDatetime.isAfter(dbMeterReadDatetime)) {
                    iBot.sendMessage(DEMAND_TO_SEND_METER_READS, meterRead.getChatId());
                }
            });
        }
    }
}
