package com.example.automator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.automator.helper.ABMRunner;
import com.example.automator.helper.CLIRunner;
import com.example.automator.helper.CSVReader;
import com.example.automator.helper.ModelResults;
import com.example.automator.helper.OptimizationResults;
import com.example.automator.helper.OptimizationResultsConverter;
import com.example.automator.helper.XMLUpdater;
import com.example.automator.helper.DataInput;
import com.example.automator.helper.UserInput;
import com.example.automator.helper.OptimizationOutput;
import com.example.automator.helper.Parameters;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AutomatorController { 
    DataInput defaultDataInput = new DataInput();
    DataInput workingDataInput = new DataInput();

    @GetMapping("/")
    @ResponseStatus(HttpStatus.OK)
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @PostMapping("/updateInput")
    @ResponseStatus(HttpStatus.OK)
    public void updateInput(@RequestBody DataInput input) {
        // check if input is valid
        String inputValidation = input.isDataInputValid(input);
        if (inputValidation != "ok") {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, inputValidation); // 400 - bad request
        }
        System.out.println("input is valid");

        //Update Model Parameters
        workingDataInput.setTrainChiefInfluence(input.getTrainChiefInfluence());
        workingDataInput.setNrDefaultFriendsInterVillage(input.getNrDefaultFriendsInterVillage());
        workingDataInput.setStdNrDefaultFriendsInterVillage(input.getStdNrDefaultFriendsInterVillage());
        workingDataInput.setAvgIntraVillageInteractionFrequency(input.getAvgIntraVillageInteractionFrequency());
        workingDataInput.setStdevIntraVillageInteractionFrequency(input.getStdevIntraVillageInteractionFrequency());
        workingDataInput.setAvgInterVillageInteractionFrequency(input.getAvgInterVillageInteractionFrequency());
        workingDataInput.setStdevInterVillageInteractionFrequency(input.getStdevInterVillageInteractionFrequency());
        workingDataInput.setAvgChiefFarmerMeetingFrequency(input.getAvgChiefFarmerMeetingFrequency());
        workingDataInput.setAvgIntraMentionPercentage(input.getAvgIntraMentionPercentage());
        workingDataInput.setStdevIntraMentionPercentage(input.getStdevIntraMentionPercentage());
        workingDataInput.setAvgInterMentionPercentage(input.getAvgInterMentionPercentage());
        workingDataInput.setStdevInterMentionPercentage(input.getStdevInterMentionPercentage());
        workingDataInput.setPercentageNegativeWoM(input.getPercentageNegativeWoM());
        workingDataInput.setBaseAdoptionProbability(input.getBaseAdoptionProbability());

        System.out.println("Updated input");
    }

    @PostMapping("/resetInput")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DataInput> resetInput(){
        DataInput workingDataInput = new DataInput();
        System.out.println("DataInput set to default parameters");
        return ResponseEntity.ok(workingDataInput); // gives back workingDataInput with 200 OK message
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/results")
    @ResponseStatus(HttpStatus.OK)
    public Object modelResults(@RequestBody UserInput userInput) {

        // check if user input is valid
        String inputValidation = userInput.isModelInputValid(userInput);
        if (inputValidation != "ok") {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, inputValidation); // 406 - not acceptable
        }
        System.out.println("input is valid");

         try {
            //run netlogo model and receive results
            ArrayList<String> results = ABMRunner.runABM(workingDataInput, userInput);

            // create ModelResults from results
            ModelResults modelResults = new ModelResults();
            modelResults.saveABMRunnerOutput(results);
            
            // check if model results are valid
            String outputValidation = modelResults.isModelResultsValid(modelResults);
            if (outputValidation != "ok") {
                throw new ResponseStatusException(HttpStatus.CONFLICT, outputValidation); // 409 - conflict
            }
            System.out.println("output is valid");

            return modelResults;
         } catch (Exception e) {
            System.out.println(e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Something went wrong: ", e)); // 400 - bad request
         }
    }

    @PostMapping("/testUpdateXML") 
    public void updateXML(@RequestBody UserInput input) {
        XMLUpdater.updateXML("MaxAdoptersTest.bsearch", workingDataInput, input);
    }
    
    @PostMapping("/optimization") //Optimization
    @ResponseStatus(HttpStatus.OK)
    public OptimizationOutput optimize(@RequestBody UserInput userInput) {

        // check if user input is valid
        String inputValidation = userInput.isOptimizationInputValid(userInput);
        if (inputValidation != "ok") {
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, inputValidation); // 406 - not acceptable
        }
        System.out.println("input is valid");

        try {
            String optimizationType = userInput.getOptimizationType();
            OptimizationOutput results = null;
            
            switch(optimizationType) {
                case "maxAdopters":
                    results = maxAdopters(userInput);
                    System.out.println("maximizing adopters");
                    break;
                case "maxKnowledge":
                    results = maxKnowledge(userInput);
                    System.out.println("maximizing considerers");
                    break;
                case "minCost":
                    results = minCost(userInput);
                    System.out.println("minimizing cost");
                    break;
                case "test":
                    results = testResults(userInput);
                    System.out.println("returning test results");
                    break;
            }

            // check if optimization results are valid
            String outputValidation = results.isOptimizationResultValid(results);
            if (outputValidation != "ok") {
                throw new ResponseStatusException(HttpStatus.CONFLICT, outputValidation); // 409 - conflict
            }
            System.out.println("output is valid");

            //Make Interpreter return results + the nr of ads/trainings and change return type of optimize() to a new type which includes those?
            return results;

        } catch(Exception e) {
            System.out.println(e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Something went wrong: ", e)); // 400 - bad request
        }
    }

    @GetMapping("/testOptimizer") //Starts up optimizer with a test model
    public OptimizationResults testOptimizer() {
        try {
            CLIRunner CLIRunner = new CLIRunner();
            CLIRunner.runCommand("-p optimization-settings-go-here/TestSettings.bsearch -o optimization-results-go-here/Test");
            
            OptimizationResults results = new CSVReader().parseResultsCSV("Test.finalCheckedBests.csv");
            return results;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping("/uploadRawCSV") //uploads CSV, runs it through the data processing script and return parameters
    @ResponseStatus(HttpStatus.OK)
    public Parameters uploadRawCSV(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The file you uploaded is empty.")); // 400 - bad request
        }

        if (!file.getOriginalFilename().endsWith(".csv")) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, String.format("The file you uploaded does not have a .csv ending.")); // 415 - unsupported media type
        }

        try {
            Path filePath = Path.of("CSV-files-go-here/raw-data.csv");

            // check if file already exists and delete if it does
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            // save new file to folder
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("File uploaded successfully");

        } catch (IOException e) {
            System.out.println(e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Something went wrong when trying to store the file: ", e)); // 400 - bad request
        }

        // run python script to process data
        try {
            Runtime.getRuntime().exec("python3 data-processing/process-survey-data.py");
        } catch (Exception e) {
            System.out.println(e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Something went wrong when trying to run the python script: ", e)); // 400 - bad request
        }

        //get csv from folder and parse it as Parameters object
        String CSV_FILE_PATH_DATA = "CSV-files-go-here/data-processed.csv";
        Parameters parameters = new CSVReader().parseDataCSV(CSV_FILE_PATH_DATA);

        return parameters;
    }


    //Helper methods:

    public OptimizationOutput maxAdopters(UserInput userInput) {
        try {
            //Update Budget
            XMLUpdater.updateXML("MaxAdopters.bsearch", workingDataInput, userInput);

            CLIRunner CLIRunner = new CLIRunner();
            CLIRunner.runCommand("-p optimization-settings-go-here/MaxAdopters.bsearch -o optimization-results-go-here/MaxAdopters");

            OptimizationResults OptimizationResults = new CSVReader().parseResultsCSV("MaxAdopters.finalCheckedBests.csv");
            UserInput optimalInput = OptimizationResultsConverter.convertResultsToUserInput(OptimizationResults, userInput);
            
            ArrayList<String> results = ABMRunner.runABM(workingDataInput, optimalInput);
            ModelResults modelResults = new ModelResults();
            modelResults.saveABMRunnerOutput(results);

            OptimizationOutput output = new OptimizationOutput(OptimizationResults, modelResults);
            return output;

        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public OptimizationOutput maxKnowledge(UserInput userInput) {
        try {
            //Update Budget
            XMLUpdater.updateXML("MaxKnowledge.bsearch", workingDataInput, userInput);

            CLIRunner CLIRunner = new CLIRunner();
            CLIRunner.runCommand("-p optimization-settings-go-here/MaxKnowledge.bsearch -o optimization-results-go-here/MaxKnowledge");

            OptimizationResults OptimizationResults = new CSVReader().parseResultsCSV("MaxKnowledge.finalCheckedBests.csv");
            UserInput optimalInput = OptimizationResultsConverter.convertResultsToUserInput(OptimizationResults, userInput);
            
            ArrayList<String> results = ABMRunner.runABM(workingDataInput, optimalInput);
            ModelResults modelResults = new ModelResults();
            modelResults.saveABMRunnerOutput(results);

            OptimizationOutput output = new OptimizationOutput(OptimizationResults, modelResults);
            return output;

        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public OptimizationOutput minCost(UserInput userInput) {
        try {
            //Update Budget
            XMLUpdater.updateXML("MinCostPerAdopter.bsearch", workingDataInput, userInput);

            CLIRunner CLIRunner = new CLIRunner();
            CLIRunner.runCommand("-p optimization-settings-go-here/MinCostPerAdopter.bsearch -o optimization-results-go-here/MinCostPerAdopter");
            
            OptimizationResults OptimizationResults = new CSVReader().parseResultsCSV("MinCostPerAdopter.finalCheckedBests.csv");
            UserInput optimalInput = OptimizationResultsConverter.convertResultsToUserInput(OptimizationResults, userInput);
            
            ArrayList<String> results = ABMRunner.runABM(workingDataInput, optimalInput);
            ModelResults modelResults = new ModelResults();
            modelResults.saveABMRunnerOutput(results);

            OptimizationOutput output = new OptimizationOutput(OptimizationResults, modelResults);
            return output;

        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public OptimizationOutput testResults(UserInput userInput) {
        try {
            OptimizationResults OptimizationResults = new CSVReader().parseResultsCSV("testResults.csv");
            UserInput optimalInput = OptimizationResultsConverter.convertResultsToUserInput(OptimizationResults, userInput);
            
            
            ArrayList<String> results = ABMRunner.runABM(workingDataInput, optimalInput);
            ModelResults modelResults = new ModelResults();
            modelResults.saveABMRunnerOutput(results);

            OptimizationOutput output = new OptimizationOutput(OptimizationResults, modelResults);
            return output;

        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}
