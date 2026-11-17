package ru.mishapp.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mishapp.entity.Media;
import ru.mishapp.enumiration.MediaStatus;
import ru.mishapp.enumiration.MediaType;
import ru.mishapp.mapper.MediaMapper;
import ru.mishapp.repository.MediaRepository;
import ru.mishapp.services.records.MediaCommand;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private MediaMapper mapper;

    @InjectMocks
    private MediaService mediaService;

    @Test
    void shouldCreateMediaWithPlanToWatchStatus() {
        when(mediaRepository.save(any(Media.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Media result = mediaService.create(10L, new MediaCommand(
                "  Фильм  ", MediaType.MOVIE, null, 8, "Хороший"));

        assertEquals("Фильм", result.getName());
        assertEquals(MediaStatus.PLAN_TO_WATCH, result.getStatus());
        assertEquals(MediaType.MOVIE, result.getType());
        assertEquals(8, result.getRating());
        assertEquals("Хороший", result.getDescription());
        assertEquals(10L, result.getAccountId());
    }

    @Test
    void shouldUpdateMediaWhenStatusTransitionIsAllowed() {
        Media existing = media(8L, MediaStatus.PLAN_TO_WATCH);
        when(mediaRepository.findByIdAndAccountId(8L, 10L)).thenReturn(Optional.of(existing));
        when(mediaRepository.save(any(Media.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Media result = mediaService.update(10L, 8L, new MediaCommand(
                "Фильм", MediaType.MOVIE, MediaStatus.IN_PROGRESS, 9, "Смотрю"));

        assertEquals(MediaStatus.IN_PROGRESS, result.getStatus());
        assertEquals(9, result.getRating());
    }

    @Test
    void shouldRejectForbiddenStatusTransition() {
        Media existing = media(8L, MediaStatus.WATCHED);
        when(mediaRepository.findByIdAndAccountId(8L, 10L)).thenReturn(Optional.of(existing));

        MediaCommand command = new MediaCommand(
                "Фильм", MediaType.MOVIE, MediaStatus.PLAN_TO_WATCH, 9, null);

        assertThrows(IllegalArgumentException.class,
                () -> mediaService.update(10L, 8L, command));
    }

    @Test
    void shouldListAllMediaWhenTypeIsNotSpecified() {
        when(mediaRepository.findAllByAccountId(10L))
                .thenReturn(List.of(media(8L, MediaStatus.WATCHED)));

        List<Media> result = mediaService.findAll(10L, null);

        assertEquals(1, result.size());
        assertEquals(MediaStatus.WATCHED, result.getFirst().getStatus());
    }

    private Media media(Long id, MediaStatus status) {
        return new Media(id, "Фильм", status, MediaType.MOVIE, 10L, 8, "Хороший");
    }
}
