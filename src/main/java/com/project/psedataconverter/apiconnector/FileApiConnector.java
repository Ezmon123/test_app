package com.project.psedataconverter.apiconnector;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

@Log4j
@Component
@Qualifier("fileApiConnector")
public class FileApiConnector implements ApiConnector{
    @Override
    public List<String> getDataFromUrl(String startDate, String endDate) {
        List<String> content = new LinkedList<>();
        startDate = "C:\\Users\\01105039\\Desktop\\Programowanie\\Web development\\React and Spring\\vehicletogrid_app\\psedataconverter\\src\\test\\java\\com\\project\\psedataconverter\\testdbdata\\" + startDate + ".txt";
        try {
            File file = new File(startDate);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.add(line);
            }
        } catch (FileNotFoundException e) {
            log.error("File not found: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            log.error("IO exception occur: " + e.getMessage());
            e.printStackTrace();
        }
        return content;
    }

    @Override
    public List<String> getDataFromUrl(String day) {
        return this.getDataFromUrl(day, null);
    }
}
