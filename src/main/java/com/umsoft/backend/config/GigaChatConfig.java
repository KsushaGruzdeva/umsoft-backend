package com.umsoft.backend.config;

import chat.giga.client.GigaChatClient;
import chat.giga.client.auth.AuthClient;
import chat.giga.client.auth.AuthClientBuilder;
import chat.giga.model.Scope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GigaChatConfig {

    @Value("${gigachat.api.auth-key:}")
    private String authKey;

    @Value("${gigachat.api.scope:GIGACHAT_API_PERS}")
    private String scope;

    @Bean
    public GigaChatClient gigaChatClient() {
        return GigaChatClient.builder()
                .verifySslCerts(true)
                .authClient(AuthClient.builder()
                        .withOAuth(AuthClientBuilder.OAuthBuilder.builder()
                                .scope(Scope.valueOf(scope))
                                .authKey(authKey)
                                .build())
                        .build())
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}