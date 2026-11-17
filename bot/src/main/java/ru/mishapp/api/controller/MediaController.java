package ru.mishapp.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mishapp.api.ApiPaths;
import ru.mishapp.api.dto.MediaDto;
import ru.mishapp.api.dto.MediaRequest;
import ru.mishapp.api.mapper.MediaApiMapper;
import ru.mishapp.api.security.AccountAccessService;
import ru.mishapp.entity.Media;
import ru.mishapp.enumiration.MediaType;
import ru.mishapp.services.MediaService;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.V1 + "/accounts/{accountId}/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private final AccountAccessService accessService;
    private final MediaApiMapper mapper;

    @GetMapping
    public List<MediaDto> findAll(
            @PathVariable Long accountId,
            @RequestParam(required = false) MediaType type) {
        accessService.requireAccount(accountId);
        return mediaService.findAll(accountId, type).stream()
                .map(mapper::toDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<MediaDto> create(
            @PathVariable Long accountId,
            @Valid @RequestBody MediaRequest request) {
        accessService.requireAccount(accountId);
        Media created = mediaService.create(accountId, mapper.toCommand(request));
        return ResponseEntity.created(ApiPaths.media(accountId, created.getId()))
                .body(mapper.toDto(created));
    }

    @PutMapping("/{mediaId}")
    public MediaDto update(
            @PathVariable Long accountId,
            @PathVariable Long mediaId,
            @Valid @RequestBody MediaRequest request) {
        accessService.requireAccount(accountId);
        return mapper.toDto(mediaService.update(accountId, mediaId, mapper.toCommand(request)));
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long accountId,
            @PathVariable Long mediaId) {
        accessService.requireAccount(accountId);
        mediaService.delete(accountId, mediaId);
        return ResponseEntity.noContent().build();
    }
}
