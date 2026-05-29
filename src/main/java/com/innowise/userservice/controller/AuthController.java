package com.innowise.userservice.controller;

import com.innowise.userservice.dto.PasswordCheckRequest;
import com.innowise.userservice.dto.UserAuthDTO;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.util.PasswordHasher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user-by-email/{email}")
    public UserAuthDTO getUserByEmail(@PathVariable String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return null;
        }
        User user = userOpt.get();
        return new UserAuthDTO(user.getId(), user.getEmail(), user.getRole().name(), user.isActive());
    }

    @PostMapping("/check-password")
    public boolean checkPassword(@RequestBody PasswordCheckRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        return PasswordHasher.checkPassword(request.getPassword(), user.getPasswordHash());
    }
}