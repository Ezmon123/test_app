package com.project.psedataconverter.parser;

import com.project.psedataconverter.model.DemandForPower;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(JUnitParamsRunner.class)
public class CsvToEntityParserTest {
    private CsvToEntityParser csvToEntityParser = new CsvToEntityParser();

    @Test
    @Parameters(method = "parametersShouldReturnListOFDemandForPower")
    public void shouldReturnListOFDemandForPower(List<String> dataFromUrl, String[] firstLine, String[] secondLine) {
        //given
        List<DemandForPower> expected = new ArrayList<>();
        expected.add(getDemandForPower(firstLine));
        expected.add(getDemandForPower(secondLine));

        //when
        List<DemandForPower> actual = csvToEntityParser.getDemandForPowerAsList(dataFromUrl);
        //then
        Assert.assertArrayEquals(expected.toArray(), actual.toArray());

    }

    private Object[] parametersShouldReturnListOFDemandForPower() {
        return new Object[]{
                new Object[]{
                        new ArrayList<>(Arrays.asList(
                                "Data;Godz.;Dobowa prognoza zapotrzebowania KSE;Rzeczywiste zapotrzebowanie KSE",
                                "20190322;1;17800;17656,638",
                                "20190322;2;17250;17124,188"
                        )),
                        new String[]{"2019032201", "17800", "17656.638"},
                        new String[]{"2019032202", "17250", "17124.188"},
                },
                new Object[]{
                        new ArrayList<>(Arrays.asList(
                                "Data;Godz.;Dobowa prognoza zapotrzebowania KSE;Rzeczywiste zapotrzebowanie KSE",
                                "20190322;1;17800;17656,638",
                                "20190322;2;17150;-"
                        )),
                        new String[]{"2019032201", "17800", "17656.638"},
                        new String[]{"2019032202", "17150", "-"},
                }
        };
    }

    private DemandForPower getDemandForPower(String[] lineValues) {
        DemandForPower demandForPower = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
            Date date = dateFormat.parse(lineValues[0]);
            Double forecastOfPowerDemand = Double.valueOf(lineValues[1]);
            Double actualPowerDemand = null;
            if (!lineValues[2].equals("-")) {
                actualPowerDemand = Double.valueOf(lineValues[2]);
            }
            demandForPower = new DemandForPower(date, forecastOfPowerDemand, actualPowerDemand);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return demandForPower;
    }
}