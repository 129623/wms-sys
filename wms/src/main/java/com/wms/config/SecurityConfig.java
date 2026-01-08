package com.wms.config;

import com.wms.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // 开启方法级安全注解支持 (@PreAuthorize)
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // 禁用 CSRF (通常用于 API 服务)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login", "/api/unauth", "/error").permitAll() // 开放接口
                        // 允许所有已认证用户访问基础数据和业务接口
                        .requestMatchers("/api/product/**", "/api/category/**", "/api/unit/**",
                                "/api/customer/**", "/api/warehouse/**", "/api/zone/**",
                                "/api/rack/**", "/api/location/**",
                                "/api/inbound/**", "/api/outbound/**", "/api/inventory/**",
                                "/api/upload/**", "/api/dashboard/**", "/api/base/**",
                                "/api/storage-type/**", "/api/product-label/**")
                        .authenticated()
                        // 系统管理需要特定权限
                        .requestMatchers("/api/user/**", "/api/role/**", "/api/menu/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                        .anyRequest().authenticated() // 其他接口需认证
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(401);
                            response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper()
                                    .writeValueAsString(com.wms.common.Result.unauthorized()));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(403);
                            response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper()
                                    .writeValueAsString(com.wms.common.Result.forbidden()));
                        }))
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessUrl("/api/unauth") // 退出后跳转
                );

        return http.build();
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
