package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.entity.Media;
import ru.mishapp.enumiration.MediaStatus;
import ru.mishapp.enumiration.MediaType;
import ru.mishapp.mapper.MediaMapper;
import ru.mishapp.repository.MediaRepository;
import ru.mishapp.services.records.MediaCommand;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final MediaMapper mapper;

    @Transactional(readOnly = true)
    public List<ru.mishapp.dto.MediaDto> getMoviesByAccountId(Long accountId) {
        return mapper.toDtoList(mediaRepository.findAllByAccountIdAndType(accountId, MediaType.MOVIE));
    }

    @Transactional(readOnly = true)
    public List<ru.mishapp.dto.MediaDto> getSeriesAccountId(Long accountId) {
        return mapper.toDtoList(mediaRepository.findAllByAccountIdAndType(accountId, MediaType.SERIES));
    }

    public void deleteMediaItem(Long id) {
        mediaRepository.deleteById(id);
    }

    @Transactional
    public void delete(long accountId, long mediaId) {
        Media existing = mediaRepository.findByIdAndAccountId(mediaId, accountId)
                .orElseThrow(() -> new NoSuchElementException("Медиа не найдено"));
        mediaRepository.deleteById(existing.getId());
    }

    @Transactional(readOnly = true)
    public List<Media> findAll(long accountId, MediaType type) {
        return type == null
                ? mediaRepository.findAllByAccountId(accountId)
                : mediaRepository.findAllByAccountIdAndType(accountId, type);
    }

    @Transactional
    public Media create(long accountId, MediaCommand command) {
        Media media = new Media(
                null,
                command.name().trim(),
                command.status() == null ? MediaStatus.PLAN_TO_WATCH : command.status(),
                command.type(),
                accountId,
                command.rating(),
                command.description()
        );
        return mediaRepository.save(media);
    }

    @Transactional
    public Media update(long accountId, long mediaId, MediaCommand command) {
        Media existing = mediaRepository.findByIdAndAccountId(mediaId, accountId)
                .orElseThrow(() -> new NoSuchElementException("Медиа не найдено"));
        MediaStatus status = command.status() == null ? existing.getStatus() : command.status();
        if (status != existing.getStatus()
                && !existing.getStatus().getAvailableTransitions().contains(status)) {
            throw new IllegalArgumentException("Недопустимый переход статуса медиа");
        }
        Media updated = new Media(
                existing.getId(),
                command.name().trim(),
                status,
                command.type(),
                accountId,
                command.rating(),
                command.description()
        );
        return mediaRepository.save(updated);
    }
}
