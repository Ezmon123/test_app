package com.project.psedataconverter;


import com.project.psedataconverter.apiconnector.ApiConnector;
import com.project.psedataconverter.model.DemandForPower;
import com.project.psedataconverter.parser.CsvToEntityParser;
import com.project.psedataconverter.parser.DateParser;
import com.project.psedataconverter.service.DemandForPowerService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    @Scheduled(fixedRate = 900000)
    public void startTask() {
        putTestRecords();
//        putInitDataIfDbIsEmpty();
//        updateRecordsOrAddNewRecords();
    }

    private void putTestRecords(){
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
            Date dateOfMeasurement = dateFormat.parse("201903310100");
            Date dateOfMeasurement1 = dateFormat.parse("201903310200");
            Date dateOfMeasurement2 = dateFormat.parse("201903310300");
            Date dateOfMeasurement3 = dateFormat.parse("201903310400");
            log.info(dateOfMeasurement.toString());
            log.info(dateOfMeasurement1.toString());
            log.info(dateOfMeasurement2.toString());
            log.info(dateOfMeasurement3.toString());
            demandForPowerService.saveDemandForPowerInDb(new DemandForPower(dateOfMeasurement, 17000.0, 2000.0));
            demandForPowerService.saveDemandForPowerInDb(new DemandForPower(dateOfMeasurement1, 17000.0, 2000.0));
            demandForPowerService.saveDemandForPowerInDb(new DemandForPower(dateOfMeasurement2, 17000.0, 2000.0));
            demandForPowerService.saveDemandForPowerInDb(new DemandForPower(dateOfMeasurement3, 17000.0, 2000.0));
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
    }

    private void putInitDataIfDbIsEmpty() {
        if (demandForPowerService.findAll().isEmpty()) {
            log.info("Initializing data in Db... start date is 26 march 11:00");
            //26 march 11:00
            List<String> dataFromUrl = apiConnector.getDataFromUrl("1553594400000");
            List<DemandForPower> demandForPowerAsList = csvToEntityParser.getDemandForPowerAsList(dataFromUrl);
            demandForPowerService.saveAllDemandForPowerInDb(demandForPowerAsList);
        }
    }

    public void updateRecordsOrAddNewRecords() {
        List<DemandForPower> allWhereActualPowerIsNullDb = demandForPowerService.findAllWhereActualPowerIsNull();
        if (!allWhereActualPowerIsNullDb.isEmpty()) {
            log.info("Updating records with with id in range: " +
                    allWhereActualPowerIsNullDb.get(0).getId() + " to " +
                    allWhereActualPowerIsNullDb.get(allWhereActualPowerIsNullDb.size() - 1).getId());
            prepareAndUpdateRecords(allWhereActualPowerIsNullDb);
        } else {
            log.info("Trying to add new records");
            addNewRecords();
        }
    }

    /**
     * time for last measurement in each day is 23:59, i must add two minutes to this time to ensure
     * that application will download data from next day and add new records.
     */
    private void addNewRecords() {
        DemandForPower lastDemandForPowerEntity = demandForPowerService.getLastRow();
        Date dateOfMeasurement = lastDemandForPowerEntity.getDateOfMeasurement();
        dateOfMeasurement = dateParser.addMinutesToDate(dateOfMeasurement, 2);
        String dateOfMeasurementUnix = dateParser.convertDateToUnix(dateOfMeasurement);
        Date realDate = new Date();
        List<String> dataFromUrl;
        if (realDate.getTime() - dateOfMeasurement.getTime() > 8640000) {
            log.info("Last measurement was later than 24 hours ago. Date of last measurement in Db: " + dateOfMeasurement.toString() +
                    " Real date: " + realDate.toString());
            String realDateUnix = dateParser.convertDateToUnix(realDate);
            dataFromUrl = apiConnector.getDataFromUrl(dateOfMeasurementUnix, realDateUnix);
        } else {
            log.info("Last measurement was sooner than 24 hours ago Date of last measurement in Db: " + dateOfMeasurement.toString() +
                    " Real date is: " + realDate.toString());
            dataFromUrl = apiConnector.getDataFromUrl(dateOfMeasurementUnix);
        }
        List<DemandForPower> demandForPowersInMeasurementDayUrl = csvToEntityParser.getDemandForPowerAsList(dataFromUrl);
        removeDataFromUrlThatAlreadyExistInDatabase(dateOfMeasurement, demandForPowersInMeasurementDayUrl, "add");
        if(!demandForPowersInMeasurementDayUrl.isEmpty()) {
            demandForPowersInMeasurementDayUrl.forEach(demandForPower -> demandForPowerService.saveDemandForPowerInDb(demandForPower));
            log.info("Adding " + demandForPowersInMeasurementDayUrl.size() + " record(s) end successful");
        }
    }

    private void prepareAndUpdateRecords(List<DemandForPower> allWhereActualPowerIsNullDb) {
        Date timeOfFirstNullMeasurement = allWhereActualPowerIsNullDb.get(0).getDateOfMeasurement();
        String unixDateOfMeasurement = dateParser.convertDateToUnix(timeOfFirstNullMeasurement);
        List<String> dataFromUrl = apiConnector.getDataFromUrl(unixDateOfMeasurement);
        List<DemandForPower> demandForPowersInMeasurementDayUrl = csvToEntityParser.getDemandForPowerAsList(dataFromUrl);
        removeDataFromUrlThatAlreadyExistInDatabase(timeOfFirstNullMeasurement, demandForPowersInMeasurementDayUrl, "update");
        if (!demandForPowersInMeasurementDayUrl.isEmpty()) {
            updateRecords(allWhereActualPowerIsNullDb, demandForPowersInMeasurementDayUrl);
        }
    }


    private List<DemandForPower> removeDataFromUrlThatAlreadyExistInDatabase(Date timeOfMeasurementDb, List<DemandForPower> demandForPowersInMeasurementDayUrl, String addOrUpdate) {
        Iterator<DemandForPower> iterator = demandForPowersInMeasurementDayUrl.listIterator();
        while (iterator.hasNext()) {
            DemandForPower demandForPowerUrl = iterator.next();
            Date measurementTime = demandForPowerUrl.getDateOfMeasurement();
            Double actualPowerDemand = demandForPowerUrl.getActualPowerDemand();
            if (measurementTime.before(timeOfMeasurementDb)) {
                iterator.remove();
            }
            if (actualPowerDemand == null && addOrUpdate.equals("update")) {
                iterator.remove();
            }
        }
        log.info("After removing data from URL that is existing in Db, size of list contain this data is: " +
                demandForPowersInMeasurementDayUrl.size());
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
                    log.info("Update to record with id: " + demandForPowerFromDb.getId() + " was successful");
                }
            }
        }
    }
}