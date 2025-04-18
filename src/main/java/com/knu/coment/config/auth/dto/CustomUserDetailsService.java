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
        User user = userRepository.findByGithubId(githubId)
                .orElseThrow(() -> new UsernameNotFoundException("No user with githubId: " + githubId));

        return new CustomUserDetails(user);
    }
}
