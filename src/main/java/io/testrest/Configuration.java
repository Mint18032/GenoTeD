package io.testrest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Configuration {

    private String outputPath;
    private String testingSessionName;
    private String odgFileName;
    private String openAPIName;

    public Configuration() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        LocalDateTime now = LocalDateTime.now();
        testingSessionName = "testing-session-" + dtf.format(now);
        outputPath = System.getProperty("user.dir") + "/output/";
        odgFileName = "odg.txt";
    }

    public String getOutputPath() {
        return outputPath;
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
        this.outputPath += "/" + openAPIName + "/";
    }
}
