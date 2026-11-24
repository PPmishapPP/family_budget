package ru.mishapp.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class SpaProbeController {

    @GetMapping("/api/v1/ping")
    String ping() {
        return "pong";
    }
}
