package com.karakoc.sofra.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailService customUserDetailService;

    @Bean
    public SecurityFilterChain applicationSecurity(
            HttpSecurity http
    ) throws Exception {

        http.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        http
                .cors(cors -> {

                    CorsConfiguration config =
                            new CorsConfiguration();

                    config.setAllowedOriginPatterns(
                            List.of(
                                    "http://localhost:5173",
                                    "http://localhost:5173",
                                    "chrome-extension://*"
                            )
                    );

                    config.setAllowedMethods(
                            List.of(
                                    "GET",
                                    "POST",
                                    "PUT",
                                    "DELETE",
                                    "OPTIONS"
                            )
                    );

                    config.setAllowedHeaders(
                            List.of("*")
                    );

                    config.setAllowCredentials(true);

                    config.setMaxAge(3600L);

                    cors.configurationSource(
                            request -> config
                    );
                })

                .csrf(csrf ->
                        csrf.disable()
                )

                .sessionManagement(management ->
                        management.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .formLogin(formLogin ->
                        formLogin.disable()
                )

                .authorizeHttpRequests(registry -> registry

                        .requestMatchers(
                                "/swagger-ui/**"
                        ).permitAll()

                        .requestMatchers(
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers(
                                "/hello/**"
                        ).permitAll()

                        /*
                         * ONLY login/register should be public.
                         */
                        .requestMatchers(
                                "/accounts/login",
                                "/accounts/register"
                        ).permitAll()

                        .requestMatchers(
                                "/admin/**"
                        ).hasRole("ADMIN")

                        /*
                         * Everything else requires JWT.
                         */
                        .anyRequest()
                        .authenticated()
                );

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http
    ) throws Exception {

        return http
                .getSharedObject(
                        AuthenticationManagerBuilder.class
                )
                .userDetailsService(
                        customUserDetailService
                )
                .passwordEncoder(
                        passwordEncoder()
                )
                .and()
                .build();
    }
}