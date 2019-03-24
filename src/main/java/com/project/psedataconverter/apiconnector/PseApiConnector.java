package com.project.psedataconverter.apiconnector;

import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Log4j
@Component
public class PseApiConnector {

    public List<String> getDataFromUrl(String startDate, String endDate) {
        List<String> dataFromUrl = new LinkedList<>();
        try {
//            String urlString = "https://www.pse.pl/dane-systemowe/funkcjonowanie-kse/raporty-dobowe-z-pracy-kse/zapotrzebowanie-mocy-kse?" +
//                    "p_p_id=danekse_WAR_danekserbportlet&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_cacheability=cacheLevelPage&" +
//                    "p_p_col_id=column-2&p_p_col_count=1&_danekse_WAR_danekserbportlet_type=kse&_danekse_WAR_danekserbportlet_target=csv&" +
//                    "_danekse_WAR_danekserbportlet_from=1548975600000&_danekse_WAR_danekserbportlet_to=1551308400000";
            String urlString = "https://www.pse.pl/obszary-dzialalnosci/krajowy-system-elektroenergetyczny/zapotrzebowanie-kse?" +
                    "p_p_id=danekse_WAR_danekserbportlet&p_p_lifecycle=2&p_p_state=normal&p_p_mode=view&p_p_cacheability=cacheLevelPage&" +
                    "p_p_col_id=column-2&p_p_col_count=1&_danekse_WAR_danekserbportlet_type=kse&_danekse_WAR_danekserbportlet_target=csv" +
                    "&_danekse_WAR_danekserbportlet_from=1553209200000&_danekse_WAR_danekserbportlet_to=1553382000000";
            URL url = new URL(urlString);
            BufferedReader csv = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = csv.readLine()) != null) {
                dataFromUrl.add(line);
                log.info(line);
            }

        } catch (MalformedURLException e) {
            log.error("URL is malformed!" + e.getMessage());
        } catch (IOException e) {
            log.error("Open stream exception occurs" + e.getMessage());
        }
        return dataFromUrl;

    }
}
