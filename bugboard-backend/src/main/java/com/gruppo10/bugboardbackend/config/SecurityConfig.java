package com.gruppo10.bugboardbackend.config;

import com.gruppo10.bugboardbackend.repository.UserRepository;
import com.gruppo10.bugboardbackend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity // Abilita la configurazione web di Spring Security
@RequiredArgsConstructor // Lombok genera il costruttore per i campi final in automatico
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserRepository userRepository;

    // 1. Algoritmo di hashing BCrypt per cifrare le password degli utenti nel DB
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Spieghiamo a Spring dove trovare gli utenti nel database (usando l'email come username)
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utente non trovato nel database"));
    }

    // 3. Colleghiamo il database (UserDetailsService) e il PasswordEncoder a Spring Security
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Passiamo userDetailsService() direttamente nel costruttore come richiesto dall'IDE
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        // Impostiamo il PasswordEncoder per verificare l'hash della password al login
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // 4. Gestore dell'autenticazione (ci servirà in AuthController per effettuare il login)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    // 5. La catena di filtri: abilitiamo CORS, disabilitiamo CSRF, apriamo /api/auth/** e impostiamo tutto Stateless
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Login accessibile a tutti senza token
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated() // Tutte le altre API richiedono un token JWT valido
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 6. Configurazione CORS per permettere al Front-end (su altra porta) di chiamare le API del Back-end
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}