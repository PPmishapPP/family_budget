package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.dto.MediaDto;
import ru.mishapp.enumiration.MediaType;
import ru.mishapp.mapper.MediaMapper;
import ru.mishapp.repository.MediaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final MediaMapper mapper;


    @Transactional(readOnly = true)
    public List<MediaDto> getMoviesByAccountId(Long accountId) {
        return mapper.toDtoList(mediaRepository.findAllByAccountIdAndType(accountId, MediaType.MOVIE));
    }

    @Transactional(readOnly = true)
    public List<MediaDto> getSeriesAccountId(Long accountId) {
        return mapper.toDtoList(mediaRepository.findAllByAccountIdAndType(accountId, MediaType.SERIES));
    }

    public void deleteMediaItem(Long id) {
        mediaRepository.deleteById(id);
    }
}
