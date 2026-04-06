package com.example.sonaerukun.Service;

import com.example.sonaerukun.model.User;
import com.example.sonaerukun.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. DBからユーザーを探す
        User user = userRepository.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + username));

        // 2. Spring Security専用のユーザーオブジェクトに変換して返す
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword()) // ここには暗号化済みのパスワードが入っている必要がある
                .authorities("USER") // 権限を設定（とりあえずUSERでOK）
                .build();
    }
}