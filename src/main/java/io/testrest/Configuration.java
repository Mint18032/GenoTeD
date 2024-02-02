package io.testrest;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.testrest.boot.AuthenticationInfo;
import io.testrest.core.valueProvider.FuzzingStrategy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

public class Configuration {

    private static String configPath;
    private final Logger logger = Logger.getLogger(Configuration.class.getName());
    private static String openApiSpecPath; // path to openapi specification, can be either a link or a file.
    private static Double maxFuzzingTimes; // number of fuzzing times per operation
    private static Double numberOfMutants; // number of mutants for each nominal test
    private static FuzzingStrategy fuzzingStrategy; // strategy to choose value for parameters
    private static String locale = "en"; // locale used for generating data (See supported locales at https://github.com/DiUS/java-faker/tree/master#supported-locales)
    private String outputPath;
    private String testingSessionName;
    private String odgFileName;
    private String openAPIName;
    private int specVersion;
    private List<String> qualifiableNames;
    private Map<Object, Object> configMap;
    private AuthenticationInfo authenticationInfo;

    public Configuration(String configPath) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        LocalDateTime now = LocalDateTime.now();
        testingSessionName = "testing-session-" + dtf.format(now);
        outputPath = System.getProperty("user.dir") + "/output/";
        odgFileName = "odg.txt";
        qualifiableNames = new ArrayList<>();
        qualifiableNames.add("id");
        qualifiableNames.add("name");
        Configuration.configPath = configPath;
        parseConfig();

        setSpecVersion(2);

        try {
            File inputFile = new File(openApiSpecPath);
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
            logger.fine("Unable to specify specification version\n" + e.getMessage());
            setSpecVersion(4);
        }
    }

    public static FuzzingStrategy getFuzzingStrategy() {
        return fuzzingStrategy;
    }

    public static void setFuzzingStrategy(FuzzingStrategy fuzzingStrategy) {
        Configuration.fuzzingStrategy = fuzzingStrategy;
    }

    private void parseConfig() {
        StringBuilder fileContent = new StringBuilder();
        try {
            File file = new File(configPath);
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                fileContent.append(sc.nextLine());
            }
        } catch (IOException e) {
            logger.warning("Unable to read test config at " + configPath + '\n' + e.getMessage());
        }

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        configMap = new HashMap<>(gson.fromJson(fileContent.toString(), type));

        if (configMap.containsKey("openApiSpecPath"))
            openApiSpecPath = configMap.get("openApiSpecPath").toString();
        else {
            logger.warning("Unable to specify OpenAPI specification path");
            System.exit(-1);
        }

        if (configMap.containsKey("maxFuzzingTimes")) {
            maxFuzzingTimes = (Double) configMap.get("maxFuzzingTimes");
        } else {
            maxFuzzingTimes = 5.0;
        }

        if (configMap.containsKey("numberOfMutants")) {
            numberOfMutants = (Double) configMap.get("numberOfMutants");
        } else {
            numberOfMutants = 10.;
        }

        if (configMap.containsKey("fuzzingStrategy")) {
            fuzzingStrategy = FuzzingStrategy.getStrategy(configMap.get("fuzzingStrategy").toString());
        } else {
            fuzzingStrategy = FuzzingStrategy.DICTIONARY_FIRST;
        }

        if (configMap.containsKey("authenticationCommand")) {
            if (configMap.get("authenticationCommand") instanceof Map) {
                Map auth_map = (Map) configMap.get("authenticationCommand");
                if (auth_map.containsKey("command"))
                    authenticationInfo = new AuthenticationInfo(auth_map.get("description").toString(), auth_map.get("command").toString());
            }
        }
    }

    public static String getOpenApiSpecPath() {
        return openApiSpecPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public Double getMaxFuzzingTimes() {
        return maxFuzzingTimes;
    }

    public Double getNumberOfMutants() {
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

    public Map<Object, Object> getConfigMap() {
        return configMap;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    public static String getConfigPath() {
        return configPath;
    }

    public static void setConfigPath(String configPath) {
        Configuration.configPath = configPath;
    }
}
