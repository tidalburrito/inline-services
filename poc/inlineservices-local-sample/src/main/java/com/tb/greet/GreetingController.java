package com.tb.greet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/greeting")
public class GreetingController {

    @Autowired
    Greeter greeter;

    @GetMapping("/greet")
    public String greet(String name){
        System.out.println("In Greeting Controller");
        String greeting= greeter.greet(name);
        System.out.println("Exiting Greeting Controller");
        return greeting;
    }
}
