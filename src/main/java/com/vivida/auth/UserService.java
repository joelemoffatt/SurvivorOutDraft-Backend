package com.vivida.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(int id) {
        return userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found with id " + id
        ));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found with username " + username
        ));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found with email " + email
        ));
    }

    public void insertUser(User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        user.setPassword(passwordEncoder.encode("123"));

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        userRepository.save(user);
    }

    public void updateUser(User user) {
        if (user.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User id is required for update");
        }

        User existingUser = getUserById(user.getId());

        String nextUsername = user.getUsername();
        if (nextUsername == null || nextUsername.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
        }

        String nextEmail = user.getEmail();
        if (nextEmail == null || nextEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        userRepository.findByUsername(nextUsername)
                .filter(found -> !found.getId().equals(existingUser.getId()))
                .ifPresent(found -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
                });

        userRepository.findByEmail(nextEmail)
                .filter(found -> !found.getId().equals(existingUser.getId()))
                .ifPresent(found -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
                });

        existingUser.setUsername(nextUsername);
        existingUser.setEmail(nextEmail);
        existingUser.setRole(user.getRole() == null ? existingUser.getRole() : user.getRole());
        existingUser.setEnabled(user.getEnabled() == null ? existingUser.getEnabled() : user.getEnabled());

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userRepository.save(existingUser);
    }

    public void deleteUserById(int id) {
        userRepository.deleteById(id);
    }
}
