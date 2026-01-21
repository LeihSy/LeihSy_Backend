package com.hse.leihsy.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * CORS Filter, der vor allen anderen Filtern OPTIONS Preflight Requests behandelt
 * und alle erlaubten Methoden (inklusive PATCH) zurückgibt.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PreflightCorsFilter extends OncePerRequestFilter {

    @Value("${cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Preflight OPTIONS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setHeader("Access-Control-Allow-Origin", allowedOrigins);
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");

            // OPTIONS sofort beenden
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Alle anderen Requests weiterleiten
        filterChain.doFilter(request, response);
    }
}