package com.otus.service;

import com.otus.annotation.Autowired;
import com.otus.annotation.Component;

@Component
public class UserService {

    @Autowired
    private GreetingService greetingService;

    public void greet(String name) {
        System.out.println(greetingService.sayHello(name));
    }
}
