package io.testrest.core.valueProvider.single;

import com.github.curiousoddman.rgxgen.RgxGen;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class testRegexParameterValueProvider {
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
    public void testdFaker() {
        String pattern = "\\d";
        String str = faker.regexify(pattern);
        System.out.println(str);
        Matcher matcher = Pattern.compile(pattern).matcher(str);
        assertTrue(matcher.find());
    }
}
