package com.example.simplejavaapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the application.
 * *** Important note for trainee:
 *     For this simple application, we are allowing all requests to the root endpoint ("/") and the health endpoint ("/actuator/health").
 *     In a real-world application, you would implement proper authentication and authorization.
 *     For example, you might use JWT, OAuth2, or basic authentication with a database of users.
 *
 * Author: Omar Alsarabi
 * Date: January 14, 2026
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/actuator/health").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
