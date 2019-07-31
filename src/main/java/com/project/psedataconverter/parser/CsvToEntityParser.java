package com.project.psedataconverter.parser;


import com.project.psedataconverter.model.DemandForPower;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Log4j
@Component
public class CsvToEntityParser {
    /**
     * I have used filter here because fetched some dump data with 24+ hours(for 11.04.2019).
     * @param dataFromUrl
     * @return
     */
    public List<DemandForPower> getDemandForPowerAsList(List<String> dataFromUrl) {
        dataFromUrl.remove(0);
        List<List<String>> splittedDataFromUrl = new LinkedList<>();
        for (String line :
                dataFromUrl) {
            splittedDataFromUrl.add(Arrays.asList(line.split(";")));
        }
        int previusHour = 0;
//        System.out.println(splittedDataFromUrl.toString());
        Iterator<List<String>> iterator = splittedDataFromUrl.iterator();
        while (iterator.hasNext()) {
            List<String> lineValues = iterator.next();
            int hour = Integer.parseInt(lineValues.get(1));
            if (hour == previusHour) {
                iterator.remove();
            }
            if (hour > 24) {
                iterator.remove();
            }
            previusHour = hour;
        }
//        System.out.println(splittedDataFromUrl.toString());
//        dataFromUrl = dataFromUrl.stream()
//                .map(line -> Arrays.asList(line.split(";")))
//                .collect(Collectors.());

        List<DemandForPower> entitiesFromUrl = splittedDataFromUrl.stream()
                .map(lineValues -> {
                    Date date = parseToData(lineValues.get(0), lineValues.get(1));
                    Double forecastOfPowerDemand = Double.valueOf(lineValues.get(2).replaceAll(",", "."));
                    Double actualPowerDemand = null;
                    if (!lineValues.get(3).equals("-")) {
                        actualPowerDemand = Double.valueOf(lineValues.get(3).replaceAll(",", "."));
                    }
                    return new DemandForPower(date, forecastOfPowerDemand, actualPowerDemand);
                })
                .collect(Collectors.toList());
//        System.out.println(entitiesFromUrl.toString());
        return entitiesFromUrl;
    }

    public Date parseToData(String date, String hour) {
        if (date.equals("20190331")) {
            log.warn("jestem w ifie");
            log.warn(date);
            log.warn(hour);
            System.out.println(date);
        }
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        String minutes = "00";
        if (hour.equals("24")) {
            hour = "23";
            minutes = "59";
        }
        Date fullDate = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
            fullDate = dateFormat.parse(date + hour + minutes);
        } catch (ParseException e) {
            log.error("Can not parse data! " + e.getMessage());
        }
        return fullDate;
    }
}