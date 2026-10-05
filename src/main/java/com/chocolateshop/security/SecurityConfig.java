package com.chocolateshop.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration with database authentication and role-based permissions.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/quick-order", "/cart/**", "/api/**"))
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/",
                    "/shop/**",
                    "/product/**",
                    "/order-track/**",
                    "/order-confirmation/**",
                    "/quick-order",
                    "/cart/**",
                    "/login",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico",
                    "/error"
                ).permitAll()
                // Module-level role restrictions
                .requestMatchers("/users/**").hasRole("ADMIN")
                .requestMatchers("/reports/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/expenses/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/pos/**", "/api/**").hasAnyRole("ADMIN", "MANAGER", "CASHIER")
                .requestMatchers("/sales/**").hasAnyRole("ADMIN", "MANAGER", "CASHIER")
                .requestMatchers("/customers/**").hasAnyRole("ADMIN", "MANAGER", "CASHIER")
                .requestMatchers("/products/**", "/categories/**", "/brands/**").hasAnyRole("ADMIN", "MANAGER", "INVENTORY_MANAGER")
                .requestMatchers("/inventory/**").hasAnyRole("ADMIN", "MANAGER", "INVENTORY_MANAGER")
                .requestMatchers("/purchases/**", "/suppliers/**").hasAnyRole("ADMIN", "MANAGER", "INVENTORY_MANAGER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}
