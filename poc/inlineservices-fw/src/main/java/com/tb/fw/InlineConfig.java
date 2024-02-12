package com.tb.fw;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("classpath:application.properties")
@PropertySource("classpath:services.properties")
@PropertySource("classpath:server.properties")
public class InlineConfig {
    @Bean
    @Scope("prototype")
    public InlineProxifier getMicroServiceProxifier(){
        return new InlineProxifier();
    }

    @Bean(name="InlineClientCache")
    @Scope("singleton")
    public InlineClientCache getInlineClientMap(){
        return new InlineClientCache();
    };
}
