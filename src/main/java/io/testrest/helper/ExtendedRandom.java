package io.testrest.helper;

import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;
import com.google.common.io.Resources;
import io.testrest.Configuration;
import io.testrest.Main;
import io.testrest.core.dictionary.Dictionary;
import io.testrest.core.dictionary.DictionaryEntry;
import org.iban4j.CountryCode;
import org.iban4j.Iban;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * Extension of the java.util.Random class providing primitives for random strings, lengths, and other.
 */
public class ExtendedRandom extends Random {
    FakeValuesService fakeValuesService = new FakeValuesService(
            new Locale(Configuration.getLocale()), new RandomService());
    Faker faker = new Faker(new Locale(Configuration.getLocale()));

    /**
     * Returns a positive integer.
     * @return the positive integer.
     */
    public int nextNonNegativeInt() {
        return Math.abs(nextInt());
    }
    /**
     * Generates a random length (integer) value according to the minimum and maximum values provided. Useful for
     * strings or arrays lengths.
     * @param minLength minimum length, null = 0
     * @param maxLength maximum length, if null no maximum value is used
     * @return an integer value among minLength (or 0 if null) and maxLength, or without bound if maxLength == null
     */
    public int nextLength(Integer minLength, Integer maxLength) {
        if (maxLength != null && maxLength == 0) {
            return 0;
        }
        if (minLength == null || minLength < 0) {
            minLength = 0;
        }

        // If minLength and maxLength are the same, there is no reason to generate a random length.
        if (minLength.equals(maxLength)) {
            return minLength;
        }

        int length = (int) Math.abs(this.nextGaussian() * 10) + minLength;
        if (maxLength != null) {
            if (length > maxLength) {
                length -= length - maxLength + nextInt(maxLength - minLength);
            }
        }
        return length;
    }

    /**
     * Same as nextLength(), but returns lower values.
     * @param minLength minimum length, null = 0
     * @param maxLength maximum length, if null no maximum value is used
     @return an integer value among minLength (or 0 if null) and maxLength, or without bound if maxLength == null
     */
    public int nextShortLength(Integer minLength, Integer maxLength) {
        if (maxLength != null && maxLength == 0) {
            return 0;
        }
        if (minLength == null || minLength < 0) {
            minLength = 0;
        }

        // If minLength and maxLength are the same, there is no reason to generate a random length.
        if (minLength.equals(maxLength)) {
            return minLength;
        }

        if (minLength == 0 && nextInt(0, 100) < 70) {
            return 1;
        }

        int length = (int) Math.abs(this.nextGaussian() * 2) + minLength;
        if (maxLength != null) {
            if (length > maxLength) {
                length -= length - maxLength + nextInt(maxLength - minLength);
            }
        }
        return length;
    }

    public Object elementFromSet(Set<Object> enumSet) {
        int p = nextInt(0, enumSet.size());
        int i = 0;
        for(Object value : enumSet) {
            if (p == i) {
                return value;
            }
            i++;
        }
        return enumSet.stream().findFirst();
    }

    /**
     * Generates a random string in different formats, of a random length.
     * @return the generated string.
     */
    public String nextString() {
        return nextString(nextLength(0, 127));
    }

    /**
     * Generates a random string in different formats, of a given length.
     * @param length the wanted length for the string.
     * @return the generated string.
     */
    public String nextString(int length) {
        int p = nextInt(100);

        if (p < 50) {
            return nextWord(length);
        } else if (p < 60) {
            String dictionaryValue = nextGlobalDictionaryEntry(length);
            if (dictionaryValue == null) {
                return nextWord(length);
            } else {
                return dictionaryValue;
            }
        } else if (p < 70) {
            return nextPhrase(length);
        } else if (p < 72) {
            return nextPhoneNumber();
        } else if (p < 74) {
            return nextEmail();
        } else if (p < 76) {
            return nextDate();
        } else if (p < 78) {
            return nextTime();
        } else if (p < 80) {
            return nextUUID();
        } else if (p < 82) {
            return nextHex(length);
        } else if (p < 84) {
            return nextURI();
        } else if (p < 86) {
            return nextSSN();
        } else if (p < 88) {
            return nextNumeric();
        } else if (p < 90) {
            return nextBase64();
        } else if (p < 92) {
            return nextIBAN();
        } else if (p < 93) {
            return nextIPV4();
        } else if (p < 94) {
            return nextIPV6();
        }
        return nextRandomString(length);
    }

    /**
     * Generates a string of random characters with a random length.
     * @return the generated string.
     */
    public String nextRandomString() {
        return nextRandomString(nextLength(0, 127));
    }

    /**
     * Generates a random string within the given lengths
     * @param length length of the string
     * @return the generated string
     */
    public String nextRandomString(int length) {
        // Characters that are common in strings
        String commonChars = "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";

        // Characters that are special, but not rare
        String specialChars = ".:,;-_#+*'\"?=/\\!$@&%€£<>";

        // Characters that are rare
        String rareChars = "àèìòùéç§°^|";

        // StringBuilder to which random characters are appended
        StringBuilder generatedString = new StringBuilder();

        String sourceString;
        for (int i = 0; i < length; i++) {
            int p = this.nextInt(1000);
            if (p < 955) {
                sourceString = commonChars;
            } else if (p < 992) {
                sourceString = specialChars;
            } else {
                sourceString = rareChars;
            }
            int index = this.nextInt(sourceString.length());

            // Double quotes are escaped to prevent errors in JSON. FIXME: apply escaping only to JSON renderer
            if (sourceString.charAt(index) == '"') {
                generatedString.append("\\\"");
            } else if (sourceString.charAt(index) == '\\') {
                generatedString.append("\\\\");
            } else {
                generatedString.append(sourceString.charAt(index));
            }
        }

        return generatedString.toString();
    }

    /**
     * Returns a random word.
     * @return the word.
     */
    public String nextWord() {
        return nextWord(nextLength(1, 19));
    }

    /**
     * Returns an English word of the given length
     * @param length length of the word (From 1 to 19 both inclusive)
     * @return the word
     */
    public String nextWord(int length) {
        if (length > 19 || length < 1) {
            return nextWord();
        }

        URL url = Resources.getResource("random_word/word" + length + ".txt");
        try {
            String text = Resources.toString(url, StandardCharsets.UTF_8);
            String[] list = text.split("\n");
            return list[nextInt(list.length)].replace("\r", "").replace("\n", "");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public String nextGlobalDictionaryEntry(int length) {
        Dictionary globalDictionary = Main.getEnvironment().getGlobalDictionary();
        Optional<DictionaryEntry> chosenEntry = nextElement(globalDictionary.getEntriesByValueLength(length));
        return chosenEntry.map(dictionaryEntry -> dictionaryEntry.getValue().toString()).orElse(null);
    }

    /**
     * Returns a random date (from 2000/01/01 inclusive to 2020/01/01 inclusive)
     * @return the date
     */
    public String nextDate() { return nextDate(LocalDate.of(2000, 1, 1),
            LocalDate.of(2020, 1, 1)); }

    /**
     * Returns a random date between min and max
     * @param min inclusive min date
     * @param max inclusive max date
     * @return the date
     */
    public String nextDate(LocalDate min, LocalDate max) {
        return nextDateTime(LocalDateTime.of(min, LocalTime.MIN), LocalDateTime.of(max, LocalTime.MIN),
                "yyyy/MM/dd");
    }

    /**
     * Returns a random date and time (from 2000/01/01 inclusive to 2022/01/02 inclusive)
     * @return the date
     */
    public String nextDateTime() {
        if (nextBoolean()) {
            return nextDateTime(LocalDateTime.of(2000, 1, 1, 0, 0, 0),
                    LocalDateTime.now());
        } else {
            // UNIX timestamp
            return String.valueOf(System.currentTimeMillis() + nextLong(-100000L, 100000L));
        }
    }

    /**
     * Returns a random date and time between min and max
     * @param min inclusive min date and time
     * @param max inclusive min date and time
     * @return the date
     */
    public String nextDateTime(LocalDateTime min, LocalDateTime max) {
        return nextDateTime(min, max, "yyyy/MM/dd HH:mm:ss");
    }

    public String nextTime()
    {
        return nextTime(LocalTime.of(0, 0, 0), LocalTime.of(23, 59, 59));
    }

    public String nextTime(LocalTime min, LocalTime max)
    {
        return nextDateTime(LocalDateTime.of(2000, 1, 1, min.getHour(), min.getMinute(), min.getSecond()),
                LocalDateTime.of(2000, 1, 1, max.getHour(), max.getMinute(), max.getSecond()), "HH:mm:ss");
    }

    public String nextYear()
    {
        return nextInt(1999, 2024).toString();
    }

    public String nextTimeDuration()
    {
        return nextTime(LocalTime.of(0, 0, 0), LocalTime.of(0, 1, 0));
    }

    /**
     * Returns a random date between min and max with a specific format
     * @param min inclusive min date and time
     * @param max inclusive min date and time
     * @param format requested string format
     * @return string with the requested format
     */
    private String nextDateTime(LocalDateTime min, LocalDateTime max, String format) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        long seconds = nextLongInclusive(0, SECONDS.between(min, max));
        LocalDateTime randomDate = min.plusSeconds(seconds);
        return randomDate.format(dateTimeFormatter);
    }

    /**
     * Returns a random binary string
     * @return the random binary string
     */
    public String nextBinaryString() {
        return nextBinaryString(nextInt(1, 256));
    }

    /**
     * Returns a random binary string with a specific length.
     * @param length the length of the string.
     * @return the random binary string.
     */
    public String nextBinaryString(int length) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (nextInt(2) == 0) {
                s.append("1");
            }
            else {
                s.append("0");
            }
        }

        return s.toString();
    }

    /**
     * Returns a random base64 string.
     * @return the random string in base64.
     */
    public String nextBase64() {
        byte[] randomBytes = new byte[nextInt(1, 100)];
        nextBytes(randomBytes);
        return Base64.getUrlEncoder().encodeToString(randomBytes);
    }

    /**
     * Returns a UUID string.
     * @return the UUID string.
     */
    public String nextUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Returns a random hex string
     * @return the hex string
     */
    public String nextHex() {
        return nextHex(32);
    }

    /**
     * Returns a random hex string with a specific length
     * @param length the length of the hex string
     * @return the hex string
     */
    public String nextHex(int length) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            sb.append(Integer.toHexString(nextInt()));
        }
        sb.setLength(length);
        return sb.toString();
    }

    /**
     * Return a new random IBAN string
     * @return the IBAN string
     */
    public String nextIBAN() {
        return Iban.random().toFormattedString();
    }

    /**
     * Returns a new random IBAN with the specified string
     * @param country the first two char of the country (example: IT)
     * @return the IBAN string
     */
    public String nextIBAN(CountryCode country) {
        return Iban.random(country).toFormattedString();
    }

    enum TypeSSN {        /**
     * xxx-xx-xxx
     */
    First,        /**
     * xxxxxxxxx
     */
    Second,
        /**
         * xxx-xxxxxx
         */
        Third,
        /**
         * xxxxx-xxxx
         */
        Fourth
    }

    /**
     * Returns a new random Social Security Number
     * @return the SSN string
     */
    public String nextSSN()
    {
        return nextSSN(TypeSSN.values()[nextInt(4)]);
    }

    /**
     * Returns a new random Social Security Number with a specific format
     * @param typeSSN the format of the SSN
     * @return the SSN string
     */
    public String nextSSN(TypeSSN typeSSN)
    {
        switch (typeSSN.ordinal())
        {
            case 1:
                return nextSSNSecondFormat();
            case 2:
                return nextSSNThirdFormat();
            case 3:
                return nextSSNFourthFormat();
            default:
                return nextSSNFirstFormat();
        }
    }

    /**
     * First format of the SSN
     * @return the SSN String
     */
    private String nextSSNFirstFormat() {
        return nextNumeric(3) + "-" + nextNumeric(2) + "-" +
                nextNumeric(3);
    }

    public <E> Optional<E> nextElement(Collection<E> e) {
        if (e.size() == 0) {
            return Optional.empty();
        }
        return e.stream().skip(nextInt(e.size())).findFirst();
    }

    public Double nextDouble(Double min, Double max) {
        min = min != null ? min : -Double.MAX_VALUE;
        max = max != null ? max : Double.MAX_VALUE;
        if (min <= max) {
            if (min >= 0 || max <= 0) {
                return min + nextDouble() * (max - min);
            } else {
                return min + nextDouble() * Math.abs(min) + nextDouble() * Math.abs(max);
            }
        }
        return 0.0;
    }

    public Float nextFloat(Float min, Float max) {
        min = min != null ? min : -Float.MAX_VALUE;
        max = max != null ? max : Float.MAX_VALUE;

        if (min <= max) {
            if (min >= 0 || max <= 0) {
                return min + nextFloat() * (max - min);
            } else {
                return min + nextFloat() * Math.abs(min) + nextFloat() * Math.abs(max);
            }
        }
        return 0.0f;
    }

    /**
     * Second format of the SSN
     * @return the SSN String
     */
    private String nextSSNSecondFormat() {
        return nextNumeric(9);
    }
    /**
     * Third format of the SSN
     * @return the SSN String
     */
    private String nextSSNThirdFormat() {
        return nextNumeric(3) + "-" + nextNumeric(6);
    }

    /**
     * Fourth format of the SSN
     * @return the SSN String
     */
    private String nextSSNFourthFormat() {
        return nextNumeric(5) + "-" + nextNumeric(4);
    }

    public String nextPhrase() {
        return nextPhrase(100);
    }

    public String nextPhrase(int length) {
        StringBuilder s = new StringBuilder();
        while (s.length() < length) {
            s.append(nextWord(nextIntBetweenInclusive(1, Math.min(length - s.length() + 1, 19)))).append(" ");
        }
        s.setLength(length);
        return s.toString();
    }

    public String nextLetterString() {
        return nextLetterString(10);
    }
    public String nextLetterString(int length) {
        String base = "qwertyuiopasdfghjklzxcvbnm";
        StringBuilder s = new StringBuilder();
        while (s.length() < length)
        {
            s.append(base.charAt(nextInt(0, base.length())));
        }
        return s.toString();
    }

    /**
     * Returns a string with base 10 numerical characters
     * @return the numeric string
     */
    public String nextNumeric()
    {
        return nextNumeric(10);
    }

    /**
     * Returns a string with base 10 numerical characters with the specified length
     * @param length the length of the string
     * @return the numeric string
     */
    public String nextNumeric(int length) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            s.append(nextInt(10));
        }
        return s.toString();
    }

    /**
     * Return a random phone number
     * @return the random phone number
     */
    public String nextPhoneNumber()
    {
        return faker.phoneNumber().phoneNumber();
    }

    /**
     * Return a random email (example@site.com)
     * @return the random email
     */
    public String nextEmail()
    {
        return faker.internet().emailAddress();
    }

    public String nextURI() {
        StringBuilder str = new StringBuilder(nextProtocol() + "://" + nextDomain() + "/");

        for (int i = 0; i < nextInt(2); i++) {
            str.append(nextString(10)).append("/");
        }

        return str.toString();
//        return faker.internet().url();
    }

    public String nextIPV4() {
        return nextIntBetweenInclusive(0, 254) + "." +
                nextIntBetweenInclusive(0, 254) + "." +
                nextIntBetweenInclusive(0, 254) + "." +
                nextIntBetweenInclusive(0, 254);
    }
    /**
     * This method generates IPv6 addresses
     */
    public  String nextIPV6() {
        StringJoiner joiner = new StringJoiner(":");

        for (int i = 0; i < generateIPv6Address().length; i++) {
            joiner.add(generateIPv6Address()[i]);
        }

        return joiner.toString();
    }

    private final char[] scope = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private String[] generateIPv6Address(){
        String[] randAddress = {"", "", "", "", "", "", "", ""};

        for (int i = 0; i < randAddress.length; i++) {
            for(int j = 0; j < 4; j++){
                randAddress[i] = randAddress[i] + scope[nextInt(0, 15 + 1)];
            }
        }

        return randAddress;
    }

    public String nextDomain() {
        return faker.internet().domainName();
    }

    public String nextProtocol() {
        return "http";
    }

    /**
     * Returns a random integer between min and max.
     * @param min minimum.
     * @param max maximum.
     * @return the random integer.
     */
    public Integer nextInt(Integer min, Integer max) {
        if (min <= max) {
            if (min >= 0 || max <= 0) {
                return min + (int) (nextDouble() * (max - min));
            } else {
                return min + (int) (nextDouble() * Math.abs(min)) + (int) (nextDouble() * Math.abs(max));
            }
        }
        return 0;
    }

    /**
     * Returns a random integer between start (inclusive) and end (exclusive)
     * @param start from (inclusive)
     * @param end to (inclusive)
     * @return the random integer
     */
    public int nextIntBetweenInclusive(int start, int end) {
        return nextInt(start, end + 1);
    }

    /**
     * Returns a random long between min and max (inclusive).
     * @param min minimum value.
     * @param max maximum value.
     * @return the random long
     */
    public Long nextLong(Long min, Long max) {
        if (min <= max) {
            if (min >= 0 || max <= 0) {
                return min + (long) (nextDouble() * (max - min));
            } else {
                return min + (long) (nextDouble() * Math.abs(min)) + (long) (nextDouble() * Math.abs(max));
            }
        }
        return 0L;
    }

    /**
     * Returns a random long between start (inclusive) and end (exclusive)
     * @param start from (inclusive)
     * @param end to (exclusive)
     * @return the random long
     */
    public long nextLongInclusive(long start, long end) {
        return nextLong(start, end + 1);
    }

    public double nextLatitude() {
        return Double.parseDouble(String.format("%.8f", nextDouble(-90., 90.)));
    }

    public double nextLongitude() {
        return Double.parseDouble(String.format("%.8f", nextDouble(-180., 180.)));
    }

    public String nextLocation() {
        return nextLatitude() + "," + nextLongitude();
    }

    public String nextCountryCode(int alpha) {
        if (alpha == 3) {
            return faker.country().countryCode3();
        }

        return faker.country().countryCode2();
    }

    public String nextCurrency() {
        return faker.currency().code();
    }

    public String[] getAlpha3CountryCodes() {
        return new String[]{"ABW", "AFG", "AGO", "AIA", "ALA", "ALB", "AND", "ARE", "ARG", "ARM", "ASM", "ATA", "ATF", "ATG", "AUS", "AUT", "AZE", "BDI", "BEL", "BEN", "BES", "BFA", "BGD", "BGR", "BHR", "BHS", "BIH", "BLM", "BLR", "BLZ", "BMU", "BOL", "BRA", "BRB", "BRN", "BTN", "BVT", "BWA", "CAF", "CAN", "CCK", "CHE", "CHL", "CHN", "CIV", "CMR", "COD", "COG", "COK", "COL", "COM", "CPV", "CRI", "CUB", "CUW", "CXR", "CYM", "CYP", "CZE", "DEU", "DJI", "DMA", "DNK", "DOM", "DZA", "ECU", "EGY", "ERI", "ESH", "ESP", "EST", "ETH", "FIN", "FJI", "FLK", "FRA", "FRO", "FSM", "GAB", "GBR", "GEO", "GGY", "GHA", "GIB", "GIN", "GLP", "GMB", "GNB", "GNQ", "GRC", "GRD", "GRL", "GTM", "GUF", "GUM", "GUY", "HKG", "HMD", "HND", "HRV", "HTI", "HUN", "IDN", "IMN", "IND", "IOT", "IRL", "IRN", "IRQ", "ISL", "ISR", "ITA", "JAM", "JEY", "JOR", "JPN", "KAZ", "KEN", "KGZ", "KHM", "KIR", "KNA", "KOR", "KWT", "LAO", "LBN", "LBR", "LBY", "LCA", "LIE", "LKA", "LSO", "LTU", "LUX", "LVA", "MAC", "MAF", "MAR", "MCO", "MDA", "MDG", "MDV", "MEX", "MHL", "MKD", "MLI", "MLT", "MMR", "MNE", "MNG", "MNP", "MOZ", "MRT", "MSR", "MTQ", "MUS", "MWI", "MYS", "MYT", "NAM", "NCL", "NER", "NFK", "NGA", "NIC", "NIU", "NLD", "NOR", "NPL", "NRU", "NZL", "OMN", "PAK", "PAN", "PCN", "PER", "PHL", "PLW", "PNG", "POL", "PRI", "PRK", "PRT", "PRY", "PSE", "PYF", "QAT", "REU", "ROU", "RUS", "RWA", "SAU", "SDN", "SEN", "SGP", "SGS", "SHN", "SJM", "SLB", "SLE", "SLV", "SMR", "SOM", "SPM", "SRB", "SSD", "STP", "SUR", "SVK", "SVN", "SWE", "SWZ", "SXM", "SYC", "SYR", "TCA", "TCD", "TGO", "THA", "TJK", "TKL", "TKM", "TLS", "TON", "TTO", "TUN", "TUR", "TUV", "TWN", "TZA", "UGA", "UKR", "UMI", "URY", "USA", "UZB", "VAT", "VCT", "VEN", "VGB", "VIR", "VNM", "VUT", "WLF", "WSM", "YEM", "ZAF", "ZMB", "ZWE"};
    }

    public String[] getAlpha2CountryCodes() {
        return new String[]{"AF", "AX", "AL", "DZ", "AS", "AD", "AO", "AI", "AQ", "AG", "AR", "AM", "AW", "AU", "AT", "AZ", "BS", "BH", "BD", "BB", "BY", "BE", "BZ", "BJ", "BM", "BT", "BO", "BQ", "BA", "BW", "BV", "BR", "IO", "BN", "BG", "BF", "BI", "CV", "KH", "CM", "CA", "KY", "CF", "TD", "CL", "CN", "CX", "CC", "CO", "KM", "CD", "CG", "CK", "CR", "CI", "HR", "CU", "CW", "CY", "CZ", "DK", "DJ", "DM", "DO", "EC", "EG", "SV", "GQ", "ER", "EE", "SZ", "ET", "FK", "FO", "FJ", "FI", "FR", "GF", "PF", "TF", "GA", "GM", "GE", "DE", "GH", "GI", "GR", "GL", "GD", "GP", "GU", "GT", "GG", "GN", "GW", "GY", "HT", "HM", "VA", "HN", "HK", "HU", "IS", "IN", "ID", "IR", "IQ", "IE", "IM", "IL", "IT", "JM", "JP", "JE", "JO", "KZ", "KE", "KI", "KP", "KR", "KW", "KG", "LA", "LV", "LB", "LS", "LR", "LY", "LI", "LT", "LU", "MO", "MK", "MG", "MW", "MY", "MV", "ML", "MT", "MH", "MQ", "MR", "MU", "YT", "MX", "FM", "MD", "MC", "MN", "ME", "MS", "MA", "MZ", "MM", "NA", "NR", "NP", "NL", "NC", "NZ", "NI", "NE", "NG", "NU", "NF", "MP", "NO", "OM", "PK", "PW", "PS", "PA", "PG", "PY", "PE", "PH", "PN", "PL", "PT", "PR", "QA", "RE", "RO", "RU", "RW", "BL", "SH", "KN", "LC", "MF", "PM", "VC", "WS", "SM", "ST", "SA", "SN", "RS", "SC", "SL", "SG", "SX", "SK", "SI", "SB", "SO", "ZA", "GS", "SS", "ES", "LK", "SD", "SR", "SJ", "SE", "CH", "SY", "TW", "TJ", "TZ", "TH", "TL", "TG", "TK", "TO", "TT", "TN", "TR", "TM", "TC", "TV", "UG", "UA", "AE", "GB", "UM", "US", "UY", "UZ", "VU", "VE", "VN", "VG", "VI", "WF", "EH", "YE", "ZM", "ZW"};
    }

    public String[] getCurrencies() {
        return new String[]{"AED", "AFN", "ALL", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN", "BAM", "BBD", "BDT", "BGN", "BHD", "BIF", "BMD", "BND", "BOB", "BRL", "BSD", "BTN", "BWP", "BYN", "BZD", "CAD", "CDF", "CHF", "CLP", "CNY", "COP", "CRC", "CUP", "CVE", "CZK", "DJF", "DKK", "DOP", "DZD", "EGP", "ERN", "ETB", "EUR", "FJD", "FKP", "GBP", "GEL", "GGP", "GHS", "GIP", "GMD", "GNF", "GTQ", "GYD", "HKD", "HNL", "HTG", "HUF", "IDR", "ILS", "IMP", "INR", "IQD", "IRR", "ISK", "JEP", "JMD", "JOD", "JPY", "KES", "KGS", "KHR", "KMF", "KPW", "KRW", "KWD", "KYD", "KZT", "LAK", "LBP", "LKR", "LRD", "LSL", "LYD", "MAD", "MDL", "MGA", "MKD", "MMK", "MNT", "MOP", "MRU", "MUR", "MVR", "MWK", "MXN", "MYR", "MZN", "NAD", "NGN", "NIO", "NOK", "none", "NPR", "NZD", "OMR", "PEN", "PGK", "PHP", "PKR", "PLN", "PYG", "QAR", "RON", "RSD", "RUB", "RWF", "SAR", "SBD", "SCR", "SDG", "SEK", "SGD", "SHP", "SLE", "SOS", "SRD", "SSP", "STN", "SYP", "SZL", "THB", "TJS", "TMT", "TND", "TOP", "TRY", "TTD", "TWD", "TZS", "UAH", "UGX", "USD", "UYU", "UZS", "VES", "VND", "VUV", "WST", "XAF", "XCD", "XDR", "XOF", "XPF", "YER", "ZAR", "ZMW"};
    }

    public String nextName() {
        return faker.artist().name();
    }

}

