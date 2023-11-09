package io.testrest.core.valueProvider.single;

import com.github.curiousoddman.rgxgen.RgxGen;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestRegexParameterValueProvider {
    Faker faker = new Faker(Locale.US);

    @RepeatedTest(3)
    public void testFaker1() {
        String pattern = "\bcat";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testComplexRegexFaker() {
        String pattern = "^([0-9]+@N[0-9]+)|([0-9a-zA-Z-_]+)$";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testComplexRegex() {
        String pattern = "^([0-9]+@N[0-9]+)|([0-9a-zA-Z-_]+)$";
        RgxGen rgxGen = RgxGen.parse(pattern);
        String str = rgxGen.generate();
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testDFaker() {
        String pattern = "\\D";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testrFaker() {
        String pattern = "\\r";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testtFaker() {
        String pattern = "\\t";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testnFaker() {
        String pattern = "\\n";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testsFaker() {
        String pattern = "\\s";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testSFaker() {
        String pattern = "\\S";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testwFaker() {
        String pattern = "\\w";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testWFaker() {
        String pattern = "\\W";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testQFaker() {
        String pattern = "\\Q";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testEFaker() {
        String pattern = "\\\\E";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testxXXFaker() {
        String pattern = "\\x0A";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testxXXXXFaker() {
        String pattern = "\\x{0041}";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testuFaker() {
        String pattern = "\u0041"; // letter A
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testpFaker() {
        String pattern = "\\p{L}";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testPFaker() {
        String pattern = "\\P{L}";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testRgxGen() {
        String pattern = "[a-zA-Z0-9]";
        String str = RgxGen.parse(pattern).generate();
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }
}
