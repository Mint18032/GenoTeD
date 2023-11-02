package io.testrest.helper;

import org.junit.jupiter.api.RepeatedTest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestExtendedRandom {
    private final ExtendedRandom extendedRandom;

    TestExtendedRandom() {
        extendedRandom = new ExtendedRandom();
    }

    @RepeatedTest(3)
    public void testIBAN() {
        String str = extendedRandom.nextIBAN();
        System.out.println(str);
        Matcher matcher = Pattern.compile("[A-Z]{2}[\\p{Alnum}\\s]{13,30}").matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testEmail() {
        String str = extendedRandom.nextEmail();
        System.out.println(str);
        Matcher matcher = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$").matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testUUID() {
        String str = extendedRandom.nextUUID();
        System.out.println(str);
        Matcher matcher = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$").matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testDomain() {
        String str = extendedRandom.nextDomain();
        System.out.println(str);
        Matcher matcher = Pattern.compile("[a-z]+\\.\\w{2,4}").matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testIpV4Address() {
        String str = extendedRandom.nextIPV4();
        System.out.println(str);
        assertEquals(4, str.split("\\.").length);
        final String[] octets = str.split("\\.");
        for (String octet : octets) {
            assertTrue(parseInt(octet) >= 0 && parseInt(octet) <= 255);
        }
    }

    @RepeatedTest(3)
    public void testIpV6Address() {
        String str = extendedRandom.nextIPV6();
        System.out.println(str);
        assertEquals(8, str.split(":").length);
        Matcher matcher = Pattern.compile("[0-9a-fA-F]{1,4}(:([0-9a-fA-F]{1,4})){1,7}").matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testCountryCode2() {
        String str = extendedRandom.nextCountryCode(2);
        System.out.println(str);
        Matcher matcher = Pattern.compile("([a-z]{2})").matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testCountryCode3() {
        String str = extendedRandom.nextCountryCode(3);
        System.out.println(str);
        Matcher matcher = Pattern.compile("([a-z]{3})").matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testCurrency() {
        String str = extendedRandom.nextCurrency();
        System.out.println(str);
        Matcher matcher = Pattern.compile("[A-Z]{3}").matcher(str);
        assertTrue(matcher.find());
    }

    @RepeatedTest(3)
    public void testPhoneNumber() {
        String str = extendedRandom.nextPhoneNumber();
        System.out.println(str);
        Matcher matcher = Pattern.compile("\\(?\\d+\\)?([- .]x?\\d+){1,5}").matcher(str);
        assertTrue(matcher.find());
    }
}
