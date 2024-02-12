package com.tb.greet;

import com.tb.fw.annotations.InlineApi;
import com.tb.fw.annotations.InlineService;
import org.springframework.beans.factory.annotation.Autowired;

@InlineService("GreeterWrapper")
public class Greeter {
    @Autowired
    TimeTeller timeTeller;

    @InlineApi
    public String greet(String name){
        System.out.println("Inside greet");
        String time=timeTeller.getTime();
        String greeting="Hello "+name+" at "+time;
        System.out.println("Exiting greet");
        return greeting;
    }
}
