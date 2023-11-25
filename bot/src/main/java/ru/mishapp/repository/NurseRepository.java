package ru.mishapp.repository;

import org.springframework.data.repository.CrudRepository;
import ru.mishapp.entity.NurseVisit;

import java.time.LocalDate;
import java.util.Optional;

public interface NurseRepository extends CrudRepository<NurseVisit, Long> {

    Optional<NurseVisit> findNurseVisitByVisitDay(LocalDate visitDay, Long chatId);
}
