package com.somedman.backend.controller;

import com.somedman.backend.entities.User;
import com.somedman.backend.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users")
public class UserController {
    private UserRepository userRepository;

    UserController(UserRepository userRepository) {
        this.userRepository =  userRepository;
    }

    @PostMapping("/login")
    @CrossOrigin(origins = "*")
    public int login(@RequestBody User newUser) {
        User loggedUser =  this.userRepository.save(newUser);
        return loggedUser.id;
    }

}
