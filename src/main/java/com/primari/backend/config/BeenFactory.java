package com.primari.backend.config;

import com.cloudmersive.client.ConvertDocumentApi;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

@Factory
public class BeenFactory {

    @Bean
    public ConvertDocumentApi convertDocumentApiBeen() {
        return new ConvertDocumentApi();
    }

}
