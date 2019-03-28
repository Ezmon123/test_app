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
    public Manager(@Qualifier("pseApiConnector") ApiConnector apiConnector, CsvToEntityParser csvToEntityParser,
                   DemandForPowerService demandForPowerService, DateParser dateParser) {
        this.apiConnector = apiConnector;
        this.csvToEntityParser = csvToEntityParser;
        this.demandForPowerService = demandForPowerService;
        this.dateParser = dateParser;
    }

    public void startTask() {
        putInitDataIfDbIsEmpty();
        updateRecordsOrAddNewRecords();
    }

    private void putInitDataIfDbIsEmpty() {
        

    }

    public void updateRecordsOrAddNewRecords() {
        List<DemandForPower> allWhereActualPowerIsNullDb = demandForPowerService.findAllWhereActualPowerIsNull();
        if (!allWhereActualPowerIsNullDb.isEmpty()) {
            updateRecords(allWhereActualPowerIsNullDb);
        }else{
            addNewRecords();
        }
    }

    /**
     * time for last measurement in each day is 23:59, i must add two minutes to this time to ensure
     * that application will download data from next day and add new records.
     *
     */
    private void addNewRecords() {
        DemandForPower lastDemandForPowerEntity = demandForPowerService.getLastRow();
        Date dateOfMeasurement = lastDemandForPowerEntity.getDateOfMeasurement();
        dateOfMeasurement = dateParser.addMinutesToDate(dateOfMeasurement, 2);
        String dateOfMeasurementUnix = dateParser.convertDateToUnix(dateOfMeasurement);
        Date realDate = new Date();
        List<String> dataFromUrl;
        if (realDate.getTime() - dateOfMeasurement.getTime() > 8640000) {
            String realDateUnix = dateParser.convertDateToUnix(realDate);
            dataFromUrl = apiConnector.getDataFromUrl(dateOfMeasurementUnix, realDateUnix);
        }else{
            dataFromUrl = apiConnector.getDataFromUrl(dateOfMeasurementUnix);
        }
        List<DemandForPower> demandForPowersInMeasurementDayUrl = csvToEntityParser.getDemandForPowerAsList(dataFromUrl);
        removeDataFromUrlThatAlreadyExistInDatabase(dateOfMeasurement, demandForPowersInMeasurementDayUrl);
        demandForPowersInMeasurementDayUrl.forEach(demandForPower -> demandForPowerService.saveDemandForPowerInDb(demandForPower));
    }

    private void updateRecords(List<DemandForPower> allWhereActualPowerIsNullDb) {
        Date timeOfFirstNullMeasurement = allWhereActualPowerIsNullDb.get(0).getDateOfMeasurement();
        String unixDateOfMeasurement = dateParser.convertDateToUnix(timeOfFirstNullMeasurement);
        List<String> dataFromUrl = apiConnector.getDataFromUrl(unixDateOfMeasurement);
        List<DemandForPower> demandForPowersInMeasurementDayUrl = csvToEntityParser.getDemandForPowerAsList(dataFromUrl);
        removeDataFromUrlThatAlreadyExistInDatabase(timeOfFirstNullMeasurement, demandForPowersInMeasurementDayUrl);
        updateRecords(allWhereActualPowerIsNullDb, demandForPowersInMeasurementDayUrl);
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