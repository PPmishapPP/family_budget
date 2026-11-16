package ru.mishapp.repository;

import org.springframework.data.repository.CrudRepository;
import ru.mishapp.entity.Media;
import ru.mishapp.enumiration.MediaType;

import java.util.List;
import java.util.Optional;

public interface MediaRepository extends CrudRepository<Media, Long> {

    List<Media> findAllByAccountIdAndType(Long account, MediaType type);

    Optional<Media> findByIdAndAccountId(Long id, Long accountId);
}
