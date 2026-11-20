package ru.mishapp.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SpaProbeController.class)
@ImportAutoConfiguration({
        SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class
})
@Import({SpaWebConfiguration.class, SpaSecurityConfiguration.class})
class SpaWebConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesIndexHtmlForAppRoot() throws Exception {
        mockMvc.perform(get("/app/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("spa-index")));
    }

    @Test
    void fallsBackToIndexForClientRoute() throws Exception {
        mockMvc.perform(get("/app/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("spa-index")));
    }

    @Test
    void servesExistingAsset() throws Exception {
        mockMvc.perform(get("/app/assets/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("spa-asset")));
    }

    @Test
    void returnsNotFoundForMissingAsset() throws Exception {
        mockMvc.perform(get("/app/assets/missing.js"))
                .andExpect(status().isNotFound());
    }

    @Test
    void redirectsAppWithoutTrailingSlash() throws Exception {
        mockMvc.perform(get("/app"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/app/"));
    }

    @Test
    void doesNotInterceptApiEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }
}
