package com.example.sonaerukun.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig {

   @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> {
            // CSRFは有効のまま。
        }) 
        .authorizeHttpRequests(auth -> auth
            // 【重要】ここを修正！登録画面 ("/signup") も許可リストに入れる
            .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**").permitAll()
            // それ以外（/index など）はログイン必須
            .anyRequest().authenticated()
        )
        .formLogin(login -> login
            .loginPage("/login")
            .defaultSuccessUrl("/index", true) // 成功したら /index へ
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
        );
        
    return http.build();
}

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
