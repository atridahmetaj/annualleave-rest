package com.annual.jeerest.controllers;

import com.annual.jeeshared.constants.Constants;
import com.annual.jeeshared.entity.User;
import com.annual.jeeshared.service.UserService;
import com.annual.jeeshared.utils.UserUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import lombok.extern.slf4j.Slf4j;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping()
public class UserController{

    private static final Logger logger = LogManager.getLogger(UserController.class);

    @Autowired
    UserService userService;

    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> findAll() {
        return userService.findAll();
    }

    @GetMapping(value = "/users/{id}")
    public User getById(@PathVariable("id") Long id) {
        return userService.getById(id);
    }

    @PostMapping(value = "/users")
    public ResponseEntity<?> createUser(@RequestBody User user) throws AccessDeniedException {
        User loggedInUser = UserUtils.getLoggedInUser();
        if (!UserUtils.isAdmin(loggedInUser)) {
            throw new AccessDeniedException(Constants.NOT_ALLOWED);
        }

        user.setAdmin(loggedInUser);
        userService.save(user);

        return ResponseEntity.ok(user);
    }

    @PutMapping(value = "/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @RequestBody User user) throws AccessDeniedException {
        user.setId(id);
        User loggedInUser = UserUtils.getLoggedInUser();

        if (UserUtils.isTeamMember(loggedInUser) && !user.equals(loggedInUser)) {
            throw new AccessDeniedException(Constants.NOT_ALLOWED);
        }

        userService.save(user);

        return ResponseEntity.ok(user);
    }

    @DeleteMapping(value = "/users/{id}")
    public ResponseEntity.HeadersBuilder<?> deleteUser(@PathVariable("id") Long id) throws AccessDeniedException {
        if (!UserUtils.isAdmin(UserUtils.getLoggedInUser())) {
            throw new AccessDeniedException(Constants.NOT_ALLOWED);
        }

        userService.deleteById(id);

        return ResponseEntity.noContent();
    }

}
