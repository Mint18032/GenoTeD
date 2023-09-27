package io.testrest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Configuration {

    private static final String openApiSpecPath = "specifications/swaggers/cnab-online.herokuapp.com.json"; // path to openapi specification, can be either a link or a file.
    private final int maxFuzzingTimes = 5; // number of fuzzing times per operation
    private String outputPath;
    private String testingSessionName;
    private String odgFileName;
    private String openAPIName;

    private List<String> qualifiableNames;

    public Configuration() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        LocalDateTime now = LocalDateTime.now();
        testingSessionName = "testing-session-" + dtf.format(now);
        outputPath = System.getProperty("user.dir") + "/output/";
        odgFileName = "odg.txt";
        qualifiableNames = new ArrayList<>();
        qualifiableNames.add("id");
        qualifiableNames.add("name");
    }

    public static String getOpenApiSpecPath() {
        return openApiSpecPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public int getMaxFuzzingTimes() {
        return maxFuzzingTimes;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getTestingSessionName() {
        return testingSessionName;
    }

    public void setTestingSessionName(String testingSessionName) {
        this.testingSessionName = testingSessionName;
    }

    public String getOdgFileName() {
        return odgFileName;
    }

    public void setOdgFileName(String odgFileName) {
        this.odgFileName = odgFileName;
    }

    public String getOpenAPIName() {
        return openAPIName;
    }

    public void setOpenAPIName(String openAPIName) {
        this.openAPIName = openAPIName;
        this.outputPath += "/" + openAPIName + "/" + testingSessionName;
    }

    public List<String> getQualifiableNames() {
        return qualifiableNames;
    }

    public void setQualifiableNames(List<String> qualifiableNames) {
        this.qualifiableNames = qualifiableNames;
    }
}
