package com.tb.greet;

import com.tb.fw.annotations.InlineService;
import com.tb.fw.annotations.InlineApi;

import java.util.Date;

@InlineService("TimeTellerWrapper")
public class TimeTeller {

    @InlineApi
    public String getTime(){
        System.out.println("Calling getTime");
        String date= new Date().toString();
        System.out.println ("Exiting getTime");
        return date;
    }

}
