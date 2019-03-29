package com.project.psedataconverter.apiconnector;

import java.util.List;

public interface ApiConnector {
    List<String> getDataFromUrl(String startDateUnix, String endDateUnix);

    List<String> getDataFromUrl(String dayUnix);
}
