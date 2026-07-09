package com.ltqtest.springbootquickstart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Value("${file.upload.base-path}")
    private String uploadBasePath;
    
    @Value("${file.upload.avatar-path}")
    private String avatarPath;
    
    @Value("${file.upload.access-base-url}")
    private String accessBaseUrl;
    
    @Value("${file.upload.product-img-path}")
    private String productImgPath;
    
  
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/" + avatarPath + "**")
                .addResourceLocations("file:" + uploadBasePath + avatarPath);
        registry.addResourceHandler("/productImages/**")
                .addResourceLocations("file:" + uploadBasePath + productImgPath);
    }
}
