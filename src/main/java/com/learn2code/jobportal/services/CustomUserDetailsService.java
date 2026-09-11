package com.learn2code.jobportal.services;

import com.learn2code.jobportal.entity.Users;
import com.learn2code.jobportal.repository.UsersRepository;
import com.learn2code.jobportal.util.CustomUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users users = usersRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Could not find user"));
        return new CustomUserDetails(users);
    }
}
