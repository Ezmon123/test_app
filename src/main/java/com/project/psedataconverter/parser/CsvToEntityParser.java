package com.project.psedataconverter.parser;


import com.project.psedataconverter.model.DemandForPower;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Log4j
@Component
public class CsvToEntityParser {
    public List<DemandForPower> getDemandForPowerAsList(List<String> dataFromUrl) {
        dataFromUrl.remove(0);
        for (String line : dataFromUrl) {
            List<String> valuesInLine = Arrays.asList(line.split(";"));
        }
        return dataFromUrl.stream()
                .map(line -> {
                    return Arrays.asList(line.split(";"));
                })
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
    }

    public Date parseToData(String date, String hour) {
        if (hour.length() == 1) {
            hour = "0" + hour;
        }
        Date fullDate = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
            fullDate = dateFormat.parse(date + hour);
        } catch (ParseException e) {
            log.error("Can not parse data! " + e.getMessage());
        }
        return fullDate;
    }
}
