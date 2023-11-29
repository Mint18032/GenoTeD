package io.testrest.boot;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.testrest.datatype.parameter.ParameterLocation;
import io.testrest.datatype.parameter.ParameterName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

public class AuthenticationInfo {

    private static final Logger logger = Logger.getLogger(AuthenticationInfo.class.getName());

    private String description;
    private String command;
    private ParameterName parameterName;
    private String value;
    private ParameterLocation in;
    private Long duration;
    private Long lastAuthUnixTimeStamp = 0L;

    private final static String ignoredAuthInfoError = "This authentication information will be probably ignored.";

    public AuthenticationInfo(String description, String command) {
        this.description = description;
        this.command = command;
    }

    /**
     * Performs authentication by running the authentication script.
     * @return true if the authentication succeeded, false otherwise.
     */
    @SuppressWarnings("unchecked")
    public boolean authenticate() {

        // Store current time to be used as authentication time if authentication is successful
        long currentUnixTime = getCurrentUnixTimeStamp();

        // Proceed only if the command is not null or too short
        if (command == null || command.trim().length() <= 1) {
            logger.warning("Invalid authentication command specified in the API configuration for '" + description +
                    "'. " + ignoredAuthInfoError);
            return false;
        }

        StringBuilder stringBuilder = new StringBuilder();

        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = stdError.readLine()) != null) {
                logger.warning(s);
            }
            while ((s = stdInput.readLine()) != null) {
                stringBuilder.append(s);
            }
        } catch (IOException e) {
            logger.severe("Could not execute authentication script for '" + description + "'. " + ignoredAuthInfoError);
        }

        LinkedHashMap<String, Object> map = null;

        try {
            map = new Gson().fromJson(stringBuilder.toString(), LinkedHashMap.class);
        } catch (JsonSyntaxException | NullPointerException e) {
            logger.warning("Authorization script must return valid JSON data, in the format specified in the README.md file. " +
                    "Instead, its result was: " + stringBuilder + ". " + ignoredAuthInfoError);
            return false;
        }

        // Check that the parsed JSON map contains all and only the required fields
        if (map.size() < 4 || !map.containsKey("name") || !map.containsKey("value") ||
                !map.containsKey("in") || !map.containsKey("duration")) {

            logger.warning("Authorization script must return a JSON containing all and only the following fields " +
                    "'name', 'value', 'in', 'duration'. Instead, its result was: " + map + ". " + ignoredAuthInfoError);
            return false;
        }

        // Use info to fill instance of the class
        setParameterName(new ParameterName((String) map.get("name")));
        setValue((String) map.get("value"));
        setIn(ParameterLocation.getLocationFromString((String) map.get("in")));
        setDuration(((Double) map.get("duration")).longValue());

        // Finally, set authentication time with the time in which this method was called
        this.lastAuthUnixTimeStamp = currentUnixTime;

        return true;
    }

    /**
     * Checks if the system is authenticated by verifying that all the relevant attributes of the class are set, and
     * the authentication is not expired.
     * @return true if the system is currently authenticated.
     */
    public boolean isAuthenticated() {
        return parameterName != null && value != null && in != null && duration != null &&
                getCurrentUnixTimeStamp() < lastAuthUnixTimeStamp + duration;
    }

    /**
     * Performs authentication if the system is not authenticated.
     * @return true if authentication succeeded, or if authentication was already processed.
     */
    public boolean authenticateIfNot() {
        if (!isAuthenticated()) {
            return authenticate();
        }
        return true;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public ParameterName getParameterName() {
        authenticateIfNot();
        return parameterName;
    }

    public void setParameterName(ParameterName parameterName) {
        this.parameterName = parameterName;
    }

    public String getValue() {
        authenticateIfNot();
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ParameterLocation getIn() {
        authenticateIfNot();
        return in;
    }

    /**
     * @param token Custom token for auth. If null, is ignored.
     * @return auth value.
     */
    public String getAuthValue(String token) {

        authenticateIfNot();

        if (isAuthenticated()) {
            String authToken = value;
            if (token != null) {
                authToken = token;
            }
            switch (in) {
                case HEADER:
                case QUERY:
                    return authToken;
                case COOKIE:
                    logger.warning("Cookie parameters are not supported.");
                    break;

            }
        }

        return null;
    }

    public boolean isAuthParam(String parameterName, ParameterLocation location) {
        return parameterName.equals(this.parameterName.toString()) && location == in;
    }

    public void setIn(ParameterLocation in) {
        this.in = in;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getLastAuthUnixTimeStamp() {
        return lastAuthUnixTimeStamp;
    }

    private long getCurrentUnixTimeStamp() {
        return Instant.now().getEpochSecond();
    }
}
