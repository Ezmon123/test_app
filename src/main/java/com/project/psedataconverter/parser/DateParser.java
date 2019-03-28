package com.project.psedataconverter.parser;

import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;

@Component
public class DateParser {
    public String convertDateToUnix(Date date){
        long unix = date.getTime();
        return String.valueOf(unix);
    }

    public Date convertUnixToDate(String unixDate) {
        long unix = Long.parseLong(unixDate);
        return new Date(unix);
    }

    public Date addMinutesToDate(Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }
}