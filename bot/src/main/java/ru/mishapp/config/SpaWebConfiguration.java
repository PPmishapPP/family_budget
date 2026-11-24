package ru.mishapp.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Раздаёт собранный React SPA с корня приложения.
 *
 * <p>Существующие файлы отдаются как есть, клиентские маршруты без расширения
 * (например, {@code /payments}) получают {@code index.html}. Служебные префиксы
 * (API, OpenAPI, Actuator, error) не перехватываются.
 */
@Configuration
public class SpaWebConfiguration implements WebMvcConfigurer {

    private static final String SPA_RESOURCE_LOCATION = "classpath:/static/";
    private static final String SPA_INDEX = "/static/index.html";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations(SPA_RESOURCE_LOCATION)
                .resourceChain(true)
                .addResolver(new SpaResourceResolver());
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Отдельный forward: так index.html получает корректный Content-Type.
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    private static final class SpaResourceResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            if (isReserved(resourcePath)) {
                return null;
            }
            if (resourcePath.isEmpty() || "/".equals(resourcePath)) {
                return new ClassPathResource(SPA_INDEX);
            }
            Resource resource = super.getResource(resourcePath, location);
            if (resource != null) {
                return resource;
            }
            if (isSpaRoute(resourcePath)) {
                return new ClassPathResource(SPA_INDEX);
            }
            return null;
        }

        private static boolean isReserved(String resourcePath) {
            String path = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
            return path.startsWith("api/")
                    || path.startsWith("v3/")
                    || path.startsWith("swagger-ui/")
                    || path.startsWith("actuator/")
                    || "error".equals(path);
        }

        private static boolean isSpaRoute(String resourcePath) {
            String lastSegment = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
            return lastSegment.indexOf('.') < 0;
        }
    }
}
