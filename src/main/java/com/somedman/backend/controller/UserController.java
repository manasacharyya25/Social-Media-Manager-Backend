package com.somedman.backend.controller;

import com.somedman.backend.entities.User;
import com.somedman.backend.entities.UserSetting;
import com.somedman.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @CrossOrigin(origins = "*")
    public int login(@RequestBody User newUser) {
        return this.userService.loginUser(newUser);
    }

    @GetMapping("/settings/{userId}")
    @CrossOrigin(origins = "*")
    public Optional<UserSetting> getUserSettings(@PathVariable("userId") int userId) {
        return this.userService.getUserSettings(userId);
    }

    @PostMapping("/settings")
    @CrossOrigin(origins="*")
    public void setUserSettings(@RequestBody
        UserSetting userSettings) {
        this.userService.saveUserSettings(userSettings);
    }
}
