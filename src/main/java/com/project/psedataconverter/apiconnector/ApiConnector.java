package com.project.psedataconverter.apiconnector;

import java.util.List;

public interface ApiConnector {
    List<String> getDataFromUrl(String startDate, String endDate);

    List<String> getDataFromUrl(String day);
}
