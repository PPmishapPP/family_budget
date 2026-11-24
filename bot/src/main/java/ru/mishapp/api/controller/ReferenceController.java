package ru.mishapp.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mishapp.api.ApiPaths;
import ru.mishapp.api.dto.MediaStatusDto;
import ru.mishapp.api.dto.MediaTypeDto;
import ru.mishapp.api.dto.PaymentTypeDto;
import ru.mishapp.api.mapper.ReferenceApiMapper;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.V1 + "/reference")
@RequiredArgsConstructor
public class ReferenceController {

    private final ReferenceApiMapper mapper;

    @GetMapping("/payment-types")
    public List<PaymentTypeDto> paymentTypes() {
        return mapper.paymentTypes();
    }

    @GetMapping("/media-types")
    public List<MediaTypeDto> mediaTypes() {
        return mapper.mediaTypes();
    }

    @GetMapping("/media-statuses")
    public List<MediaStatusDto> mediaStatuses() {
        return mapper.mediaStatuses();
    }
}
