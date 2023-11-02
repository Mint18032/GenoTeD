package io.testrest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Configuration {

    private static final String openApiSpecPath = "specifications/swaggers/flickr.com.json"; // path to openapi specification, can be either a link or a file.
    private static final int maxFuzzingTimes = 5; // number of fuzzing times per operation
    private static final int numberOfMutants = 10; // number of mutants for each nominal test
    private static String locale = "en"; // locale used for generating data (See supported locales at https://github.com/DiUS/java-faker/tree/master#supported-locales)
    private String outputPath;
    private String testingSessionName;
    private String odgFileName;
    private String openAPIName;
    private int specVersion;
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
        setSpecVersion(2);

        try {
            File inputFile = new File("src/main/resources/" + openApiSpecPath);
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String currentLine;

            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.contains("openapi:")) {
                    String ver = currentLine.substring(currentLine.lastIndexOf("openapi:") + 8, currentLine.lastIndexOf("openapi:") + 11).trim();
                    setSpecVersion(ver.startsWith("3") ? 3 : ver.startsWith("2") ? 2 : 4);
                    break;
                } else if (currentLine.contains("swagger")) {
                    setSpecVersion(2);
                    break;
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("Unable to specify specification version\n" + e.getMessage());
            setSpecVersion(4);
        }
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

    public int getNumberOfMutants() {
        return numberOfMutants;
    }

    public static void setLocale(String locale) {
        Configuration.locale = locale;
    }

    public static String getLocale() {
        return locale;
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

    public int getSpecVersion() {
        return specVersion;
    }

    public void setSpecVersion(int specVersion) {
        this.specVersion = specVersion;
    }
}
