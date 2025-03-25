package com.knu.coment.config.auth.dto;

import com.knu.coment.entity.User;
import com.knu.coment.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String githubId) throws UsernameNotFoundException {
        User user = userRepository.findByGithubIdFetchStacks(githubId)
                .orElseThrow(() -> new UsernameNotFoundException("No user with githubId: " + githubId));

        // User 엔티티를 UserDetails로 변환 (CustomUserDetails 등)
        return new CustomUserDetails(user);
    }
}
