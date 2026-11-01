package org.example.bookingservice.controller;

import org.example.bookingservice.entity.User;
import org.example.bookingservice.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserAdminController {

    private final UserRepository userRepository;

    public UserAdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    public User createUser(@RequestBody Map<String, String> body) {
        User user = new User();
        user.setUsername(body.get("username"));
        user.setPassword(body.get("password"));
        user.setRole(body.getOrDefault("role", "USER"));
        return userRepository.save(user);
    }

    @PatchMapping
    public User updateUser(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (body.containsKey("password")) user.setPassword(body.get("password").toString());
        if (body.containsKey("role")) user.setRole(body.get("role").toString());
        return userRepository.save(user);
    }

    @DeleteMapping
    public void deleteUser(@RequestParam Long id) {
        userRepository.deleteById(id);
    }
}