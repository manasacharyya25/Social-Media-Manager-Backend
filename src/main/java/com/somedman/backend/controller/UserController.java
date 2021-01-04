package com.somedman.backend.controller;

import com.somedman.backend.entities.User;
import com.somedman.backend.repository.UserRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private UserRepository userRepository;

    UserController(UserRepository userRepository) {
        this.userRepository =  userRepository;
    }

    @PostMapping("/user")
    void SaveUser(@RequestBody User newUser) {
        this.userRepository.save(newUser);
    }

}
