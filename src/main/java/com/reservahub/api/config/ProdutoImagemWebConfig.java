package com.reservahub.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class ProdutoImagemWebConfig implements WebMvcConfigurer {

    private final String diretorioUpload;

    public ProdutoImagemWebConfig(
            @Value("${app.upload.dir:uploads}") String diretorioUpload
    ) {
        this.diretorioUpload = diretorioUpload;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String localImagens = Path.of(diretorioUpload)
                .toAbsolutePath()
                .normalize()
                .resolve("products")
                .normalize()
                .toUri()
                .toString();

        if (!localImagens.endsWith("/")) {
            localImagens += "/";
        }

        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations(localImagens)
                .setCachePeriod(3600);
    }
}