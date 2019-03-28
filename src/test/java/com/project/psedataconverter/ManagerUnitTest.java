package com.project.psedataconverter;

import com.project.psedataconverter.apiconnector.ApiConnector;
import com.project.psedataconverter.apiconnector.FileApiConnector;
import com.project.psedataconverter.model.DemandForPower;
import com.project.psedataconverter.parser.CsvToEntityParser;
import com.project.psedataconverter.parser.DateParser;
import com.project.psedataconverter.service.DemandForPowerService;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ManagerUnitTest {
    private Manager manager;
    private InMemoryDemandForPowerRepository inMemoryDemandForPowerRepository;

    @Before
    public void setUp() {
        ApiConnector apiConnector = new FileApiConnector();
        CsvToEntityParser csvToEntityParser = new CsvToEntityParser();
        DateParser dateParser = new DateParser();
        this.inMemoryDemandForPowerRepository = new InMemoryDemandForPowerRepository();
        DemandForPowerService demandForPowerService = new DemandForPowerService(inMemoryDemandForPowerRepository);
        this.manager = new Manager(apiConnector, csvToEntityParser, demandForPowerService, dateParser);

        List<String> dataFromUrl = apiConnector.getDataFromUrl(
                "init_data_23_03");
        List<DemandForPower> demandForPowerAsList = csvToEntityParser.getDemandForPowerAsList(dataFromUrl);
        demandForPowerService.saveAllDemandForPowerInDb(demandForPowerAsList);
    }

    @Test
    public void should_insertAndUpdateValuesInDb() {
        //Assert that db is not empty and has init data
        Map<Long, DemandForPower> database = inMemoryDemandForPowerRepository.database;
        assertThat(database.size()).isEqualTo(24);
        assertThat(database.get((long) 24).getActualPowerDemand()).isNotNull();
        assertThat(database.get((long) 24).getForecastOfPowerDemand()).isNotNull();
        //Try to add new data
        manager.startTask();
        //Assert that new data has been added
        assertThat(database.size()).isEqualTo(48);
        assertThat(database.get((long) 48).getForecastOfPowerDemand()).isNotNull();
        assertThat(database.get((long) 48).getActualPowerDemand()).isNull();
        assertThat(database.get((long) 47).getActualPowerDemand()).isNull();
        assertThat(database.get((long) 46).getActualPowerDemand()).isNull();
        assertThat(database.get((long) 45).getActualPowerDemand()).isNotNull();
        //Try to update data with nulls
        manager.startTask();
        //Assert that correct data was updated, 3 record should be updated now
        assertThat(database.size()).isEqualTo(48);
        assertThat(database.get((long) 48).getForecastOfPowerDemand()).isNotNull();
        assertThat(database.get((long) 48).getActualPowerDemand()).isNotNull();
        assertThat(database.get((long) 47).getActualPowerDemand()).isNotNull();
        assertThat(database.get((long) 46).getActualPowerDemand()).isNotNull();
        assertThat(database.get((long) 45).getActualPowerDemand()).isNotNull();
        //Try to add new data
        manager.startTask();
        //Assert that new data has been added
        assertThat(database.size()).isEqualTo(72);
        System.out.println("Db Status: ");
        for (Long id : database.keySet()) {
            System.out.println(database.get(id));
        }
    }
}