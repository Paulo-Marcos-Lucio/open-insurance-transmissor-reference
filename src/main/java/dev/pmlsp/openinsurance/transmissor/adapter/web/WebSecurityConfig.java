package dev.pmlsp.openinsurance.transmissor.adapter.web;

import dev.pmlsp.openinsurance.transmissor.infrastructure.security.dpop.AccessTokenIntrospector;
import dev.pmlsp.openinsurance.transmissor.infrastructure.security.dpop.DPoPValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Security configuration with two profiles:
 * <ul>
 *   <li><strong>default</strong> (any profile that's NOT <code>fapi</code>): permitAll for v0.1.0
 *       behaviour — keeps existing IT and demo flows untouched.</li>
 *   <li><strong>fapi</strong>: Insurance Policies endpoints under
 *       <code>/open-insurance/insurance-policies/**</code> require a DPoP-bound access
 *       token (RFC 9449). Mock auth endpoints stay open so the FAPI E2E IT can issue
 *       the bound token in-process without a real authorization server.</li>
 * </ul>
 */
@Configuration
public class WebSecurityConfig {

    @Bean
    @Profile("!fapi")
    @Order(1)
    public SecurityFilterChain mockFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @Profile("fapi")
    @Order(1)
    public SecurityFilterChain fapiInsuranceChain(HttpSecurity http,
                                                  AccessTokenIntrospector introspector,
                                                  DPoPValidator dpopValidator) throws Exception {
        http
                .securityMatcher(new AntPathRequestMatcher("/open-insurance/insurance-policies/**"))
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .addFilterBefore(new DPoPAuthenticationFilter(introspector, dpopValidator),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Profile("fapi")
    @Order(2)
    public SecurityFilterChain fapiOpenChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
