package com.otus.service;

import com.otus.annotation.Component;

@Component
public class GreetingServiceImpl implements GreetingService{

    @Override
    public String sayHello(String name) {
        return "Hello world for " + name;
    }
}
