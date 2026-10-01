package com.github.chaunguyentruongan.warehouse_cdnsg.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.github.chaunguyentruongan.warehouse_cdnsg.auth.JwtFilter;
import com.github.chaunguyentruongan.warehouse_cdnsg.auth.JwtService;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Thêm cấu hình CORS
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                .requestMatchers("/api/auth/**").permitAll()

                                // User Management
                                .requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("MANAGER", "ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                                .requestMatchers("/api/users/**").hasAnyRole("MANAGER", "ADMIN")

                                // Borrow items: USER can view, MANAGER/ADMIN can edit
                                .requestMatchers(HttpMethod.GET, "/api/borrow-items/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                                .requestMatchers("/api/borrow-items/**").hasAnyRole("MANAGER", "ADMIN")

                                // Borrow return: USER can create ticket and view personal history; all management actions require MANAGER/ADMIN
                                .requestMatchers(HttpMethod.POST, "/api/borrow-return/create").hasAnyRole("USER", "MANAGER", "ADMIN")
                                .requestMatchers("/api/borrow-return/user/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                                .requestMatchers("/api/borrow-return/**").hasAnyRole("MANAGER", "ADMIN")
                                .requestMatchers("/api/borrow-dashboard/**").hasAnyRole("MANAGER", "ADMIN")

                                // Materials & Units
                                .requestMatchers(HttpMethod.GET, "/api/materials/**", "/api/units/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                                .requestMatchers("/api/materials/**", "/api/units/**").hasAnyRole("MANAGER", "ADMIN")

                                // Warehouse Receipts (Import & Export)
                                .requestMatchers("/api/import-receipt/**", "/api/export-receipt/**").hasAnyRole("MANAGER", "ADMIN")

                                // Fire Extinguisher & Locations
                                .requestMatchers("/api/fire-extinguisher/**", "/api/locations/**", "/api/zones/**").hasAnyRole("MANAGER", "ADMIN")

                                // Projector Management
                                .requestMatchers(HttpMethod.GET, "/api/projectors/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                                .requestMatchers("/api/projectors/**", "/api/projector-loans/**", "/api/projector-maintenances/**").hasAnyRole("MANAGER", "ADMIN")

                                // Uniform & Water Management
                                .requestMatchers("/api/uniforms/**", "/api/uniform-imports/**", "/api/uniform-receipts/**").hasAnyRole("MANAGER", "ADMIN")
                                .requestMatchers("/api/water-imports/**", "/api/water-zones/**").hasAnyRole("MANAGER", "ADMIN")

                                // Dashboards & Media
                                .requestMatchers("/api/dashboard/**").hasAnyRole("MANAGER", "ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/document-categories/**", "/api/media/**").hasAnyRole("MANAGER", "ADMIN")
                                .requestMatchers("/api/document-categories/**", "/api/media/**").hasRole("ADMIN")

                                // Inventory: GET for all authenticated, DELETE only ADMIN, write for MANAGER/ADMIN
                                .requestMatchers(HttpMethod.GET, "/api/inventory/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/inventory/**").hasRole("ADMIN")
                                .requestMatchers("/api/inventory/**").hasAnyRole("MANAGER", "ADMIN")

                                .anyRequest().hasRole("ADMIN"));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, authEx) -> {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("Unauthorized");
        }));
        return http.build();

    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList("https://*.vercel.app", "http://localhost:5173"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public JwtFilter jwtFilter(JwtService jwtService) {
        return new JwtFilter(jwtService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}