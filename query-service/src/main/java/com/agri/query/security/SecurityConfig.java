package com.agri.query.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    AntPathRequestMatcher.antMatcher("/api/**")
                )
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    AntPathRequestMatcher.antMatcher("/**")
                )
            )
            .authorizeHttpRequests(auth -> auth
                // Public views and resources
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher("/"),
                    AntPathRequestMatcher.antMatcher("/history"),
                    AntPathRequestMatcher.antMatcher("/login"),
                    AntPathRequestMatcher.antMatcher("/register"),
                    AntPathRequestMatcher.antMatcher("/forgot-password"),
                    AntPathRequestMatcher.antMatcher("/reset-password"),
                    AntPathRequestMatcher.antMatcher("/actuator/health"),
                    AntPathRequestMatcher.antMatcher("/css/**"),
                    AntPathRequestMatcher.antMatcher("/js/**"),
                    AntPathRequestMatcher.antMatcher("/images/**"),
                    AntPathRequestMatcher.antMatcher("/favicon.ico"),
                    AntPathRequestMatcher.antMatcher("/error"),
                    AntPathRequestMatcher.antMatcher("/api/auth/**"),
                    AntPathRequestMatcher.antMatcher(HttpMethod.POST, "/api/query"),
                    AntPathRequestMatcher.antMatcher(HttpMethod.GET, "/api/history")
                ).permitAll()
                // Protected farmer workflows
                .requestMatchers(
                    AntPathRequestMatcher.antMatcher("/dashboard"),
                    AntPathRequestMatcher.antMatcher("/profile"),
                    AntPathRequestMatcher.antMatcher("/profile/**"),
                    AntPathRequestMatcher.antMatcher("/plots"),
                    AntPathRequestMatcher.antMatcher("/plots/**"),
                    AntPathRequestMatcher.antMatcher("/reminders"),
                    AntPathRequestMatcher.antMatcher("/reminders/**"),
                    AntPathRequestMatcher.antMatcher("/plans"),
                    AntPathRequestMatcher.antMatcher("/plans/**"),
                    AntPathRequestMatcher.antMatcher("/schemes"),
                    AntPathRequestMatcher.antMatcher("/schemes/**"),
                    AntPathRequestMatcher.antMatcher("/api/profile/**"),
                    AntPathRequestMatcher.antMatcher("/api/plots/**"),
                    AntPathRequestMatcher.antMatcher("/api/reminders/**"),
                    AntPathRequestMatcher.antMatcher("/api/plans/**"),
                    AntPathRequestMatcher.antMatcher("/api/schemes/**"),
                    AntPathRequestMatcher.antMatcher("/api/me")
                ).authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/", false)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .deleteCookies("KISAN_SESSIONID")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(5)
            )
            .userDetailsService(userDetailsService);

        return http.build();
    }
}
