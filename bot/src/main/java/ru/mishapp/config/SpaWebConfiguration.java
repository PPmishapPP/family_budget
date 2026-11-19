package ru.mishapp.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class SpaWebConfiguration implements WebMvcConfigurer {

    private static final String SPA_RESOURCE_LOCATION = "classpath:/static/app/";
    private static final String SPA_INDEX = "/static/app/index.html";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/app/", "/app/**")
                .addResourceLocations(SPA_RESOURCE_LOCATION)
                .resourceChain(true)
                .addResolver(new SpaResourceResolver());
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/app", "/app/");
    }

    private static final class SpaResourceResolver extends PathResourceResolver {

        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
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

        private static boolean isSpaRoute(String resourcePath) {
            String lastSegment = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
            return lastSegment.indexOf('.') < 0;
        }
    }
}
