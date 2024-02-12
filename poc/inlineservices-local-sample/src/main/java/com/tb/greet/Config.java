package com.tb.greet;

import com.tb.fw.InlineProxifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class Config {

    @Autowired
    InlineProxifier inlineProxifier;

    @Bean
    @Primary
    TimeTeller getTimeTeller(){
        return inlineProxifier.getProxied(TimeTeller.class);
    }

    @Bean
    @Primary
    Greeter getGreeter(){
        return inlineProxifier.getProxied(Greeter.class);
    }

}
