package com.otus;

import com.otus.context.MiniApplicationContext;
import com.otus.service.GreetingServiceImpl;
import com.otus.service.UserService;

public class Main {
    public static void main(String[] args) {
        var context = new MiniApplicationContext(
                GreetingServiceImpl.class,
                UserService.class
        );

        context.getBeans(UserService.class).greet("Murka");

        System.out.println(context.getBeans(GreetingServiceImpl.class)
                == context.getBeans(GreetingServiceImpl.class));
    }
}