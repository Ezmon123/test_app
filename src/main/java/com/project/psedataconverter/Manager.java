package com.project.psedataconverter;


import com.project.psedataconverter.apiconnector.PseApiConnector;
import com.project.psedataconverter.model.DemandForPower;
import com.project.psedataconverter.parser.CsvToEntityParser;
import com.project.psedataconverter.service.DemandForPowerService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j
@Component
public class Manager {

    private PseApiConnector pseApiConnector;
    private CsvToEntityParser csvToEntityParser;
    private DemandForPowerService demandForPowerService;

    @Autowired
    public Manager(PseApiConnector pseApiConnector, CsvToEntityParser csvToEntityParser, DemandForPowerService demandForPowerService) {
        this.pseApiConnector = pseApiConnector;
        this.csvToEntityParser = csvToEntityParser;
        this.demandForPowerService = demandForPowerService;
    }

    public void startTask() {
        List<String> dataFromUrl = pseApiConnector.getDataFromUrl("some data", "some data");
        List<DemandForPower> demandForPowers = null;
        if (!dataFromUrl.isEmpty()) {
            demandForPowers = csvToEntityParser.getDemandForPowerAsList(dataFromUrl);
        }
        if (demandForPowers != null) {
            demandForPowers
                    .forEach(demandForPower -> {
                        demandForPowerService.saveDemandForPowerInDb(demandForPower);
                    });
        }
    }
}