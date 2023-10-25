package com.imatalk.chatservice.service;

import com.imatalk.chatservice.dto.request.LoginRequest;
import com.imatalk.chatservice.dto.request.RegistrationRequest;
import com.imatalk.chatservice.dto.response.CommonResponse;
import com.imatalk.chatservice.dto.response.LoginResponse;
import com.imatalk.chatservice.entity.User;
import com.imatalk.chatservice.exception.ApplicationException;
import com.imatalk.chatservice.relationRepository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.imatalk.chatservice.utils.Utils.generateAvatarUrl;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public ResponseEntity<CommonResponse> register(RegistrationRequest request) {
        checkAndThrowExceptionIfEmailExists(request);

        if (request.getUsername() != null) {
            validateUsernameThrowExceptionIfInvalid(request.getUsername());
        }

        User user = createUserFromRequest(request);
        userRepo.save(user);

        return ResponseEntity.ok(CommonResponse.success("Registration success"));
    }

    private User createUserFromRequest(RegistrationRequest request) {

        String username = request.getUsername();

        return User.builder()
                .email(request.getEmail())
                .id(UUID.randomUUID().toString())
                .displayName(request.getDisplayName())
                .password(passwordEncoder.encode(request.getPassword()))
                .username("@" + username)
                .avatar(generateAvatarUrl(request.getDisplayName()))
                .joinAt(LocalDateTime.now())
                .build();
    }

    private String generateUniqueUsername(RegistrationRequest request) {
        // combine first name and last name and add random 4-digit numbers
        String username = "@"+ request.getDisplayName();
        username = username.replaceAll("\\s+", "_");
        username = username.toLowerCase();
        username = username + "_" + (int) (Math.random() * 10000);

        boolean isUsernameExist = userRepo.existsByUsername(username);

        while (isUsernameExist) {
            username = username + "_" + (int) (Math.random() * 10000);
            isUsernameExist = userRepo.existsByUsername(username);
        }

        return username;

        // check if username already exist
    }

    private void validateUsernameThrowExceptionIfInvalid(String username) {
    //        if (!username.matches(ValidationRegex.USERNAME_REGEX)) {
    //            throw new ApplicationException(ValidationRegex.USERNAME_INVALID_ERROR);
    //        }
        String formatted = "@" + username.toLowerCase();
        boolean isUsernameExist = userRepo.existsByUsername(formatted);

        if (isUsernameExist) {
            throw new ApplicationException("Username already exist");
        }

    }

    private void checkAndThrowExceptionIfEmailExists(RegistrationRequest request) {
        boolean isEmailExist = userRepo.existsByEmail(request.getEmail());

        if (isEmailExist) {
            throw new ApplicationException("Email already exist");
        }
    }



    public ResponseEntity<CommonResponse> login(LoginRequest request) {

        User user = userRepo.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new  ApplicationException("Incorrect credentials"));

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApplicationException("Incorrect credentials");
        }

        String token = jwtService.generateToken(user);
        LoginResponse response = LoginResponse.builder()
                .accessToken(token)
                .build();

        return ResponseEntity.ok(CommonResponse.success("Login success", response));
    }

}
