package com.imatalk.chatservice.service;


import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.exception.ApplicationException;
import com.imatalk.chatservice.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findById(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
    public void saveAll(Iterable<User> users) {
        userRepo.saveAll(users);
    }

    public void save(User user) {
        userRepo.save(user);
    }


    public boolean existsByEmail(String email) {
        return userRepo.existsByEmail(email);
    }

    public User getUserBYEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ApplicationException("User not found for email: " + email));
    }

    public User getUserByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new ApplicationException("Username not found for username: " + username));
    }

    public User getUserById(String id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ApplicationException("User not found for id: " + id));
    }

    public List<User> findAllByIds(List<String> memberIds) {
        return userRepo.findAllByIdIn(memberIds);
    }
}
