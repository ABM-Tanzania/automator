package com.example.automator;

import java.util.ArrayList;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.automator.helper.ABMRunner;
import com.example.automator.helper.CSVReader;
import com.example.automator.helper.ModelInput;
import com.example.automator.helper.ModelResults;
import com.example.automator.helper.Parameters;

@RestController
public class AutomatorController {
    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @PostMapping("/results")
    public Object modelResults(@RequestBody ModelInput modelInput) {
         try {
            //get csv from folder and parse it as Parameters object
            Parameters parameters = new CSVReader().parseCSV();

            //run netlog model and receive results
            ArrayList<String> results = ABMRunner.runABM(parameters, modelInput);

            // create ModelResults from results
            ModelResults modelResults = new ModelResults();
            modelResults.setAwareFarmers(results.get(0));
            modelResults.setAdopters(results.get(1));
            modelResults.setAwareFarmersPerTick(results.get(2));
            modelResults.setAdoptersPerTick(results.get(3));

            return modelResults;
         } catch (Exception e) {
             System.out.println(e);
         }

        return null;
    }
}
