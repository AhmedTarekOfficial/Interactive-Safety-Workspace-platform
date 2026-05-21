package com.saftyhub.project1;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.util.Map;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Serve /videos/** from classpath:/static/videos/ with correct MIME types.
     * This prevents the browser from triggering a file-download dialog for .mp4 files.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("classpath:/static/videos/")
                .resourceChain(false);
    }

    /**
     * Map .mp4 extension → video/mp4 so Spring never sends
     * application/octet-stream for video files.
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .mediaType("mp4",  MediaType.parseMediaType("video/mp4"))
            .mediaType("webm", MediaType.parseMediaType("video/webm"))
            .mediaType("ogg",  MediaType.parseMediaType("video/ogg"));
    }
}
