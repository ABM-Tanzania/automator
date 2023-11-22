package com.example.automator.helper;

import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;


import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CSVReader {

    private static final String CSV_FILE_PATH_RESULTS = "optimization-results-go-here/";
    
    public Parameters parseDataCSV(String CSV_FILE_PATH_DATA) {
        try (
            Reader reader = Files.newBufferedReader(Paths.get(CSV_FILE_PATH_DATA));
        ) {

            // check CSV

            // create parameter instance from CSV
            CsvToBean<Parameters> csvToBean = new CsvToBeanBuilder(reader)
                .withType(Parameters.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

            Iterator<Parameters> parameterIterator = csvToBean.iterator();

            while (parameterIterator.hasNext()) {
                Parameters parameters = parameterIterator.next();
                return parameters;
            }
        } catch(Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public OptimizationResults parseResultsCSV(String fileName) {
        try (
            Reader reader = Files.newBufferedReader(Paths.get(CSV_FILE_PATH_RESULTS + fileName));
        ) {
            CsvToBean<OptimizationResults> csvToBean = new CsvToBeanBuilder(reader)
            .withType(OptimizationResults.class)
            .withIgnoreLeadingWhiteSpace(true)
            .build();

            Iterator<OptimizationResults> resultsIterator = csvToBean.iterator();

            while (resultsIterator.hasNext()) {
                OptimizationResults results = resultsIterator.next();
                return results;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public String checkDataCSVHeader(String path) {
        try {
            // get header from csv
            com.opencsv.CSVReader testreader = new CSVReaderBuilder(new FileReader(path)).build();
            List<String[]> rows = testreader.readAll();
            String[] header = rows.get(0);

            // required headers (from python script)
            List<String> allowedHeaders = Arrays.asList(
                "PART 2: STORAGE AND POST-HARVEST/Have you received training on hermetic bag?",
                "PART 7: SOCIAL CAPITAL AND NETWORKING/What is your/partner's role in the group/institution?",
                "Enumerator: Select Farmer Group type",
                "PART 2: STORAGE AND POST-HARVEST/What kind of storage methods do you use?/Hermetic bag",
                "PART 7: SOCIAL CAPITAL AND NETWORKING/Think about the last 10 discussions you had with contacts in your village. In how many discussions were hermetic storage bags mentioned?",
                "PART 7: SOCIAL CAPITAL AND NETWORKING/Think about the last 10 discussions you had with contacts outside of your village. In how many discussions were hermetic storage bags mentioned?",
                "PART 7: SOCIAL CAPITAL AND NETWORKING/Think about the people you know in your village. In a typical week, how many times do you communicate with each person you know? The way of communication (in-person or phone) does not matter.", "PART 7: SOCIAL CAPITAL AND NETWORKING/Now think about people you know that don’t live in your village. In a typical week, how many people outside your village do you communicate with? Include all relatives, friends, traders, extension officers and other people. The way of communication (in-person or phone) does not matter."
            );

            // check that all required headers are present in csv
            for(String element : allowedHeaders) {
                if(Arrays.asList(header).contains(element) == false) {
                    return "%s is a required column name".formatted(element);
                }
            }

        } catch (Exception e) {
            return e.getMessage();
        }
        return "ok";
    }
}
