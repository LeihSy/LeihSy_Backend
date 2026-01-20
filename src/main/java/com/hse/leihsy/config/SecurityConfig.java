package com.hse.leihsy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    private final UserSyncFilter userSyncFilter;

    public SecurityConfig(UserSyncFilter userSyncFilter) {
        this.userSyncFilter = userSyncFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Oeffentliche Endpoints
                        .requestMatchers("/", "/login", "/error").permitAll()

                        // Actuator Health Endpoints (für Docker Health Checks)
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()

                        // H2 Console (nur Development!)
                        .requestMatchers("/h2-console/**").permitAll()

                        // Swagger UI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html/**", "/v3/api-docs/**").permitAll()

                        // Bilder oeffentlich zugaenglich (fuer Katalog-Ansicht)
                        .requestMatchers("/api/images/**").permitAll()

                        // Products GET-Endpoints oeffentlich (fuer Katalog)
                        .requestMatchers("GET", "/api/products/**").permitAll()
                        .requestMatchers("GET", "/api/categories/**").permitAll()
                        .requestMatchers("GET", "/api/locations/**").permitAll()

                        // User-Info Endpoint (fuer Frontend um aktuellen User zu holen)
                        .requestMatchers("GET", "/api/users/me").authenticated()

                        // Abhol-Link Student (Muss öffentlich sein)
                        .requestMatchers("/api/bookings/verify-pickup").permitAll()

                        // Alle anderen API-Endpoints erfordern Authentifizierung
                        .requestMatchers("/api/**").authenticated()

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // UserSyncFilter NACH der JWT-Authentifizierung einfuegen
                .addFilterAfter(userSyncFilter, UsernamePasswordAuthenticationFilter.class);

        // H2 Console Frame-Options deaktivieren (nur Development!)
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    /**
     * CORS Configuration Bean
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Erlaubte Origins (Frontend URL)
        configuration.setAllowedOrigins(Arrays.asList(
                allowedOrigins
        ));

        // Erlaubte HTTP-Methoden
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Erlaubte Headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Credentials erlauben (fuer Cookies/Auth-Header)
        configuration.setAllowCredentials(true);

        // Preflight-Request Cache (1 Stunde)
        configuration.setMaxAge(3600L);

        // Welche Headers im Response sichtbar sein duerfen
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

    /**
     * Konvertiert JWT Rollen zu Spring Security Authorities.
     * Keycloak speichert Rollen unter verschiedenen Pfaden - wir unterstuetzen beide.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return jwtAuthenticationConverter;
    }
}