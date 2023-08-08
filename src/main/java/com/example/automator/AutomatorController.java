package com.example.automator;

import java.util.ArrayList;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.automator.helper.ABMRunner;
import com.example.automator.helper.CSVReader;
import com.example.automator.helper.ModelResults;
import com.example.automator.helper.Parameters;

@RestController
public class AutomatorController {
    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @GetMapping("/results")
    public Object modelResults() {
         try {
            //get csv from folder and parse it as Parameters object
            Parameters parameters = new CSVReader().parseCSV();

            //run netlog model and receive results
            ArrayList<Double> results = ABMRunner.runABM(parameters);

            // create ModelResults from results
            ModelResults modelResults = new ModelResults();
            modelResults.setAwareFarmers(results.get(0));
            modelResults.setAdopters(results.get(1));

            return modelResults;
         } catch (Exception e) {
             System.out.println(e);
         }

        return null;
    }
}
