package io.testrest.core.testing;

import com.intuit.karate.Results;
import com.intuit.karate.junit5.Karate;
import io.testrest.Environment;
import net.masterthought.cucumber.Configuration;
import net.masterthought.cucumber.ReportBuilder;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestRunner {
    private String outputPath = Environment.getConfiguration().getOutputPath();

    public TestRunner() {
        System.setProperty("karate.options", "-o \"" + outputPath + "/NominalTests\"" );
    }

    @Karate.Test
    public Results testOperation(String testPath, String operationId) {
        return Karate.run(testPath).outputHtmlReport(false).outputCucumberJson(false).outputJunitXml(false).tags("@" + operationId).parallel(0);
    }

    /**
     * Run all tests.
     * @param paths all nominal and error test paths from all servers.
     */
    @Karate.Test
    public void testAll(List<String> paths) {
        Results results = Karate.run(paths.toArray(new String[0])).outputCucumberJson(true).outputHtmlReport(false).parallel(10); //.reportDir(path.substring(0, path.lastIndexOf("/")))
        generateReport(results.getReportDir());
        Assertions.assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }

    /**
     * Run all tests in a feature file.
     * @param path to the feature.
     */
    @Karate.Test
    public void testAll(String path) {
        Results results = Karate.run(path).outputCucumberJson(true).outputHtmlReport(false).parallel(5);
        generateReport(results.getReportDir());
        Assertions.assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }

    /**
     * Create a Cucumber's visualized report.
     * @param karateOutputPath path to Karate report.
     */
    public void generateReport(String karateOutputPath) {
        Collection<File> jsonFiles = FileUtils.listFiles(new File(karateOutputPath), new String[] {"json"}, true);
        List<String> jsonPaths = new ArrayList<>(jsonFiles.size());
        jsonFiles.forEach(file -> jsonPaths.add(file.getAbsolutePath()));
        Configuration config = new Configuration(new File(outputPath),
                Environment.getConfiguration().getOpenAPIName());
        ReportBuilder reportBuilder = new ReportBuilder(jsonPaths, config);
        reportBuilder.generateReports();
    }

    /**
     * Opens the test report in browser.
     */
    public void showReport() {
        try {
            File htmlFile = new File(outputPath + "/cucumber-html-reports/overview-features.html");

            // Check if Desktop is supported, and file exists
            if (Desktop.isDesktopSupported() && htmlFile.exists()) {
                Desktop.getDesktop().browse(htmlFile.toURI());
            }
        } catch (IOException e) {
            // ignore
        }
    }
}
