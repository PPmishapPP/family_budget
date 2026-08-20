package ru.mishapp.repository;

import org.springframework.data.repository.CrudRepository;
import ru.mishapp.entity.Media;
import ru.mishapp.enumiration.MediaType;

import java.util.List;

public interface MediaRepository extends CrudRepository<Media, Long> {

    List<Media> findAllByAccountIdAndType(Long account, MediaType type);
}
