package com.annual.jeerest.controllers;

import com.annual.jeeshared.entity.User;
import com.annual.jeeshared.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(value = "/rest/v1")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping(value = "/users")
    public List<User> findAll() {
        return userService.findAll();
    }

//    @Override
    public User getById(Long id) {
        return null;
    }

//    @Override
    public User create(User requestBody) {
        return null;
    }

//    @Override
    public User update(Long id, User requestBody) {
        return null;
    }

//    @Override
    public void delete(Long id) {

    }

}
