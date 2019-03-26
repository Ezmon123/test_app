package com.project.psedataconverter;


import com.project.psedataconverter.apiconnector.ApiConnector;
import com.project.psedataconverter.model.DemandForPower;
import com.project.psedataconverter.parser.CsvToEntityParser;
import com.project.psedataconverter.parser.DateParser;
import com.project.psedataconverter.service.DemandForPowerService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Log4j
@Component
public class Manager {

    private ApiConnector apiConnector;
    private CsvToEntityParser csvToEntityParser;
    private DemandForPowerService demandForPowerService;
    private DateParser dateParser;

    @Autowired
    public Manager(@Qualifier("fileApiConnector") ApiConnector apiConnector, CsvToEntityParser csvToEntityParser,
                   DemandForPowerService demandForPowerService, DateParser dateParser) {
        this.apiConnector = apiConnector;
        this.csvToEntityParser = csvToEntityParser;
        this.demandForPowerService = demandForPowerService;
        this.dateParser = dateParser;
    }

    public void startTask() {
        String fileName = "ZAP_KSE_20190322to20190324_20190324202504";
        String firstDate = getDateOfLastActualPowerDemandMeasurement();
        List<String> dataFromUrl = apiConnector.getDataFromUrl(fileName, "some data");
        List<DemandForPower> demandForPowers = null;
        if (!dataFromUrl.isEmpty()) {
            demandForPowers = csvToEntityParser.getDemandForPowerAsList(dataFromUrl);
        }
        if (demandForPowers != null) {
            demandForPowers
                    .forEach(demandForPower -> demandForPowerService.saveDemandForPowerInDb(demandForPower));
        }
        tryToUpdateRecordsWithNullValues();
        String s = "s";
        log.info("c");
    }

    public String getDateOfLastActualPowerDemandMeasurement() {
        List<DemandForPower> allWhereActualPowerIsNull = demandForPowerService.findAllWhereActualPowerIsNull();
        DemandForPower demandForPower = null;
        if (allWhereActualPowerIsNull == null || allWhereActualPowerIsNull.isEmpty()) {
            demandForPower = demandForPowerService.getLastRow();
        } else {
            demandForPower = allWhereActualPowerIsNull.get(0);
        }
        return null;
    }

    public void tryToUpdateRecordsWithNullValues() {
        List<DemandForPower> allWhereActualPowerIsNullDb = demandForPowerService.findAllWhereActualPowerIsNull();
        if (!allWhereActualPowerIsNullDb.isEmpty()) {
            Date timeOfFirstNullMeasurement = allWhereActualPowerIsNullDb.get(0).getDateOfMeasurement();
            String unixDateOfMeasurement = dateParser.convertDateToUnix(timeOfFirstNullMeasurement);
            List<String> dataFromUrl = apiConnector.getDataFromUrl(unixDateOfMeasurement);
            List<DemandForPower> demandForPowersInMeasurementDayUrl = csvToEntityParser.getDemandForPowerAsList(dataFromUrl);
            removeDataFromUrlThatAlreadyExistInDatabase(timeOfFirstNullMeasurement, demandForPowersInMeasurementDayUrl);
            updateRecords(allWhereActualPowerIsNullDb, demandForPowersInMeasurementDayUrl);
            log.info("");
        }else{
            DemandForPower lastDemandForPowerEntity = demandForPowerService.getLastRow();
            Date dateOfMeasurement = lastDemandForPowerEntity.getDateOfMeasurement();
            String dateOfMeasurementUnix = dateParser.convertDateToUnix(dateOfMeasurement);
            Date todayDate = new Date();
            String todayDateUnix = dateParser.convertDateToUnix(todayDate);
            List<String> dataFromUrl = apiConnector.getDataFromUrl(dateOfMeasurementUnix, todayDateUnix);
            List<DemandForPower> demandForPowersInMeasurementDayUrl = csvToEntityParser.getDemandForPowerAsList(dataFromUrl);
            removeDataFromUrlThatAlreadyExistInDatabase(dateOfMeasurement, demandForPowersInMeasurementDayUrl);
            demandForPowersInMeasurementDayUrl.forEach(demandForPower -> demandForPowerService.saveDemandForPowerInDb(demandForPower));
        }
    }

    private List<DemandForPower> removeDataFromUrlThatAlreadyExistInDatabase(Date timeOfMeasurementDb, List<DemandForPower> demandForPowersInMeasurementDayUrl) {
        Iterator<DemandForPower> iterator = demandForPowersInMeasurementDayUrl.listIterator();
        while (iterator.hasNext()) {
            Date measurementTime = iterator.next().getDateOfMeasurement();
            if (measurementTime.before(timeOfMeasurementDb)) {
                iterator.remove();
            }
        }
        log.info(demandForPowersInMeasurementDayUrl.toString());
        return demandForPowersInMeasurementDayUrl;
    }

    public void updateRecords(List<DemandForPower> allWhereActualPowerIsNullDb, List<DemandForPower> demandForPowersInMeasurementDay) {
        for (DemandForPower demandForPowerFromUrl : demandForPowersInMeasurementDay) {
            Date timeOfDemandForPowerFromUrl = demandForPowerFromUrl.getDateOfMeasurement();
            for (DemandForPower demandForPowerFromDb : allWhereActualPowerIsNullDb) {
                Date timeOfDemandForPowerFromDb = demandForPowerFromDb.getDateOfMeasurement();
                if (timeOfDemandForPowerFromDb.getTime() == timeOfDemandForPowerFromUrl.getTime()) {
                    demandForPowerFromDb.setActualPowerDemand(demandForPowerFromUrl.getActualPowerDemand());
                    demandForPowerFromDb.setForecastOfPowerDemand(demandForPowerFromUrl.getForecastOfPowerDemand());
                    demandForPowerService.saveDemandForPowerInDb(demandForPowerFromDb);
                }
            }
        }
    }
}