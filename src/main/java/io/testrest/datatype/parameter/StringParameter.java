package io.testrest.datatype.parameter;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.testrest.Main;
import io.testrest.datatype.graph.OperationNode;
import io.testrest.helper.ExtendedRandom;
import io.testrest.helper.ObjectHelper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class StringParameter extends ParameterLeaf {

    private Integer maxLength; // MUST be >= 0
    private Integer minLength = 0;

    private String pattern;

    private static final Logger logger = Logger.getLogger(StringParameter.class.getName());

    public StringParameter() {
    }

    public StringParameter(ParameterElement parent, Parameter parameterMap, OperationNode operation, String name) {
        super(parent, parameterMap, operation, name);

        @SuppressWarnings("unchecked")
        Schema sourceMap = parameterMap.getSchema();

        int maxLength = sourceMap.getMaxLength();
        if (maxLength < 0) {
            logger.warning("Max length " + maxLength + " not valid for parameter '" + getName() + "' in operation '" +
                    operation + "'. The value will be ignored.");
        } else {
            this.maxLength = maxLength;
        }

        int minLength = sourceMap.getMinLength();
        if (minLength < 0 || (this.maxLength != null && minLength > this.maxLength)) {
            logger.warning("Min length " + minLength + " not valid for parameter '" + getName() + "' in operation '" +
                    operation + "'. The value will be ignored.");
        } else {
            this.minLength = minLength;
        }

        this.pattern = sourceMap.getPattern();
    }

    public StringParameter(ParameterElement parent, Parameter parameterMap, OperationNode operation) {
        this(parent, parameterMap, operation, null);
    }

    public StringParameter(Parameter other) {
        super(other);

        maxLength = other.getSchema().getMaxLength();
        minLength = other.getSchema().getMinLength();
        pattern = other.getSchema().getPattern();
    }

    public StringParameter(ParameterLeaf other) {
        super(other);
    }

    public StringParameter(Parameter other, OperationNode operation) {
        super(other, operation);

        if (other.getSchema().getMaxLength() != null) {
            maxLength = other.getSchema().getMaxLength();
        }

        minLength = other.getSchema().getMinLength() != null ? other.getSchema().getMinLength() : 0;
        pattern = other.getSchema().getPattern();
    }

    public StringParameter(StringParameter other, OperationNode operation, ParameterElement parent) {
        super(other, operation, parent);

        maxLength = other.maxLength;
        minLength = other.minLength;
        pattern = other.pattern;
    }

    public StringParameter(OperationNode operation, ParameterElement parent, String name) {
        super(operation, parent);

        this.name = new ParameterName(Objects.requireNonNullElse(name, ""));
        this.normalizedName = NormalizedParameterName.computeParameterNormalizedName(this);
        this.type = ParameterType.STRING;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public StringParameter merge(ParameterElement other) {
        if (!(other instanceof StringParameter)) {
            throw new IllegalArgumentException("Cannot merge a " + this.getClass() + " instance with a "
                    + other.getClass() + " one.");
        }

        StringParameter stringParameter = (StringParameter) other;
        StringParameter merged = this;
        merged.maxLength = this.maxLength == null ?
                stringParameter.maxLength : stringParameter.maxLength != null ?
                Math.min(this.maxLength, stringParameter.maxLength) : null;
        merged.minLength = this.minLength == null ?
                stringParameter.minLength : stringParameter.minLength != null ?
                Math.max(this.minLength, stringParameter.minLength) : null;
        // TODO: when adding pattern support, concat the two patterns

        return merged;
    }

    @Override
    public boolean isValueCompliant(Object value) {
        try {
            String stringValue = ObjectHelper.castToString(value);

            // Check if value is in enum set, if enum values are available
            if (getEnumValues().size() == 0 || getEnumValues().contains(stringValue)) {

                // Check if length is compliant with maxLength and minLength, if defined
                if ((maxLength == null || stringValue.length() <= maxLength) && (minLength == null || stringValue.length() >= minLength)) {
                    return true;
                }
            }
        } catch (ClassCastException ignored) {}
        return false;
    }

    @Override
    public boolean isObjectTypeCompliant(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof StringParameter) {
            return true;
        }
        return String.class.isAssignableFrom(o.getClass());
    }

    /**
     * Infers a format from format, type, example and name of the parameter.
     * @return the inferred format.
     */
    public ParameterTypeFormat inferFormat() {

        ExtendedRandom random = Main.getEnvironment().getRandom();

        if (format == null) return ParameterTypeFormat.MISSING;

        switch (format) {
            case BYTE:
                return ParameterTypeFormat.BYTE;
            case BINARY:
                return ParameterTypeFormat.BINARY;
            case DATE:
                return ParameterTypeFormat.DATE;
            case DATE_TIME:
                return ParameterTypeFormat.DATE_TIME;
            case TIME:
                return ParameterTypeFormat.TIME;
            case YEAR:
                return ParameterTypeFormat.YEAR;
            case DURATION:
                return ParameterTypeFormat.DURATION;
            case PASSWORD:
                return ParameterTypeFormat.PASSWORD;
            case HOSTNAME:
                return ParameterTypeFormat.HOSTNAME;
            case URI:
                return ParameterTypeFormat.URI;
            case UUID:
                return ParameterTypeFormat.UUID;
            case IPV4:
                return ParameterTypeFormat.IPV4;
            case IPV6:
                return ParameterTypeFormat.IPV6;
            case HASH:
                return ParameterTypeFormat.HASH;
            case EMAIL:
                return ParameterTypeFormat.EMAIL;
            case PHONE:
                return ParameterTypeFormat.PHONE;
            case IBAN:
                return ParameterTypeFormat.IBAN;
            case FISCAL_CODE:
                return ParameterTypeFormat.FISCAL_CODE;
            case SSN:
                return ParameterTypeFormat.SSN;
            default:
                ParameterTypeFormat inferedValue = inferFormatFromName();
                if (inferedValue.equals(ParameterTypeFormat.MISSING)) {
                    inferedValue = inferFormatFromExample();
                }
                return inferedValue;
        }
    }

    /**
     * Infers a format from the example of the parameter.
     * @return the inferred format.
     */
    public ParameterTypeFormat inferFormatFromExample() {
        if (examples == null) return ParameterTypeFormat.MISSING;

        String email_regex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";
        Pattern mail_pat = Pattern.compile(email_regex);

        for (Object example : examples.toArray()) {
            if (mail_pat.matcher(example.toString()).matches()) {
                return ParameterTypeFormat.EMAIL;
            }

            for (String currency : Main.getEnvironment().getRandom().getCurrencies()) {
                if (currency.equalsIgnoreCase(example.toString())) return ParameterTypeFormat.CURRENCY;
            }

            for (String code : Main.getEnvironment().getRandom().getAlpha2CountryCodes()) {
                if (code.equalsIgnoreCase(example.toString())) return ParameterTypeFormat.COUNTRY_CODE;
            }

            for (String code : Main.getEnvironment().getRandom().getAlpha3CountryCodes()) {
                if (code.equalsIgnoreCase(example.toString())) return ParameterTypeFormat.COUNTRY_CODE;
            }
        }

        return ParameterTypeFormat.MISSING;
    }

    /**
     * Infers a format from the name of the parameter.
     * @return the inferred format.
     */
    public ParameterTypeFormat inferFormatFromName() {
        ExtendedRandom random = Main.getEnvironment().getRandom();

        if (name.contains("email") || name.contains("e-mail")) {
            return ParameterTypeFormat.EMAIL;
        } else if (name.contains("password")) {
            return ParameterTypeFormat.PASSWORD;
        } else if (name.endsWith("time") || name.startsWith("time")) {
            return ParameterTypeFormat.TIME;
        } else if (name.endsWith("year") || name.startsWith("year")) {
            return ParameterTypeFormat.YEAR;
        } else if (name.contains("duration")) {
            return ParameterTypeFormat.DURATION;
        } else if (name.contains("iban")) {
            return ParameterTypeFormat.IBAN;
        } else if ((name.contains("codice") && name.contains("fiscale")) || name.startsWith("cf") ||
                name.endsWith("cf")) {
            return ParameterTypeFormat.FISCAL_CODE;
        } else if ((name.contains("social") && name.contains("security") && name.contains("number")) ||
                name.contains("ssn")) {
            return ParameterTypeFormat.SSN;
        } else if (name.contains("uuid")) {
            return ParameterTypeFormat.UUID;
        } else if (name.contains("phone")) {
            return ParameterTypeFormat.PHONE;
        } else if (name.startsWith("uri") || name.endsWith("uri") ||
                name.startsWith("url") || name.endsWith("url")) {
            return ParameterTypeFormat.URI;
        } else if (name.contains("hostname")) {
            return ParameterTypeFormat.HOSTNAME;
        } else if (name.contains("host")) {
            if (random.nextBoolean()) {
                return ParameterTypeFormat.HOSTNAME;
            }
            return ParameterTypeFormat.IPV4;
        } else if (name.endsWith("ip") || name.startsWith("ip")) {
            if (random.nextInt(10) < 8) {
                return ParameterTypeFormat.IPV4;
            }
            return ParameterTypeFormat.IPV6;
        } else if (name.startsWith("date") || name.endsWith("date")) {
            if (random.nextBoolean()) {
                return ParameterTypeFormat.DATE;
            }
            return ParameterTypeFormat.DATE_TIME;
        } else if (name.endsWith("file")) {
            if (random.nextInt(10) < 8) {
                return ParameterTypeFormat.BINARY;
            }
            return ParameterTypeFormat.BYTE;
        } else if (name.endsWith("time") || name.startsWith("time")) {
            if (random.nextBoolean()) {
                return ParameterTypeFormat.TIME;
            }
            return ParameterTypeFormat.DATE_TIME;
        } else if (name.endsWith("year") || name.startsWith("year")) {
            return ParameterTypeFormat.YEAR;
        } else if (name.contains("sha-1") || name.endsWith("hash") || name.contains("md5") ||
                name.contains("sha-256")) {
            return ParameterTypeFormat.HASH;
        } else if (name.endsWith("location")) {
            return ParameterTypeFormat.LOCATION;
        } else if (name.contains("country") || name.contains("alpha")) {
            return ParameterTypeFormat.COUNTRY_CODE;
        } else if (name.contains("currency")) {
            return ParameterTypeFormat.CURRENCY;
        }
        // TODO: optimize country code, capital, continent. Classify by description

        return ParameterTypeFormat.MISSING;
    }

    @Override
    public StringParameter deepClone() {
        return this;
    }

    @Override
    public StringParameter deepClone(OperationNode operation, ParameterElement parent) {
        return new StringParameter(this, operation, parent);
    }

    /**
     * No arrays are available at this level. No underlying parameters are available in leaves.
     * @return an empty list
     */
    @Override
    public Collection<ArrayParameter> getArrays() {
        return new LinkedList<>();
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getJSONString(Object value) {

        // If leaf is inside an array, don't print the leaf name
        if (this.getParent() instanceof ArrayParameter) {
            return "\"" + value.toString() + "\"";
        } else {
            return getJSONHeading() + "\"" + value.toString() + "\"";
        }
    }
}
