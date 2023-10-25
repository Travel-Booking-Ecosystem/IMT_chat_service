package com.imatalk.chatservice.relationRepository;

import com.imatalk.chatservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findAllByIdIn(List<String> memberIds);

    List<User> findAllByUsernameStartsWithIgnoreCase(String keyword);

    List<User> findAllByDisplayNameStartsWithIgnoreCase(String keyword);
}
