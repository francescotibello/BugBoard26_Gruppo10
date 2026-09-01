package com.gruppo10.bugboardbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.gruppo10.bugboardbackend.repository.UserRepository;
import com.gruppo10.bugboardbackend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity // Abilita la configurazione web di Spring Security
@RequiredArgsConstructor // Lombok genera il costruttore per i campi final in automatico
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Spieghiamo a Spring dove trovare gli utenti (usando la mail)
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato nel database"));
    }

    // Colleghiamo il database e il PasswordEncoder a Spring Security
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Passiamo userDetailsService() direttamente nel costruttore come richiesto dall'IDE
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());

        // Impostiamo il PasswordEncoder
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    // Gestore dell'autenticazione (ci servirà nel controller per il login)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 6. La catena di filtri: disabilitiamo CSRF, apriamo /api/auth/** e impostiamo tutto Stateless
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}