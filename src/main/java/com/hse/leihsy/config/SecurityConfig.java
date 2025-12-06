package com.hse.leihsy.config;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS aktivieren
                .csrf(csrf -> csrf.disable()) // Für REST API
                .authorizeHttpRequests(auth -> auth
                        // Öffentliche Endpoints (z.B. für Frontend Login-Redirect)
                        .requestMatchers("/", "/login", "/error").permitAll()

                        // Actuator Health Endpoints (für Docker Health Checks)
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()

                        // H2 Console (nur Development!)
                        .requestMatchers("/h2-console/**").permitAll()

                        // Swagger UI (optional öffentlich für Development)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Bilder öffentlich zugänglich (für Katalog-Ansicht)
                        .requestMatchers("/api/images/**").permitAll()

                        // Products GET-Endpoints öffentlich (für Katalog)
                        .requestMatchers("GET", "/api/products/**").permitAll()
                        .requestMatchers("GET", "/api/categories/**").permitAll()
                        .requestMatchers("GET", "/api/locations/**").permitAll()

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
                );

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
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));

        // Erlaubte HTTP-Methoden
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Erlaubte Headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Credentials erlauben (für Cookies/Auth-Header)
        configuration.setAllowCredentials(true);

        // Preflight-Request Cache (1 Stunde)
        configuration.setMaxAge(3600L);

        // Welche Headers im Response sichtbar sein dürfen
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

    /**
     * Konvertiert JWT Rollen zu Spring Security Authorities
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // Keycloak speichert Rollen unter "realm_access.roles"
        grantedAuthoritiesConverter.setAuthoritiesClaimName("realm_access.roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}