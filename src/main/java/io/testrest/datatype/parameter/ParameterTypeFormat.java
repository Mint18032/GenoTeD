package io.testrest.datatype.parameter;

import java.util.logging.Logger;

public enum ParameterTypeFormat {
    INT8,
    INT16,
    INT32,
    INT64,
    UINT8,
    UINT16,
    UINT32,
    UINT64,
    FLOAT,
    DOUBLE,
    DECIMAL,
    BYTE,
    BINARY,
    DATE,
    DATE_TIME,
    TIME,
    YEAR,
    DURATION,
    PASSWORD,
    HOSTNAME,
    URI,
    UUID,
    IPV4,
    IPV6,
    HASH,
    EMAIL,
    PHONE,
    IBAN,
    SSN,
    FISCAL_CODE,
    LATITUDE,
    LONGITUDE,
    LOCATION,
    COUNTRY_CODE,
    CURRENCY,

    MISSING,    // To codify missing format and increase fault tolerance
    UNKNOWN     // Unknown format. To increase fault tolerance
    ;

    private static final Logger logger = Logger.getLogger(ParameterTypeFormat.class.getName());

    public static ParameterTypeFormat getFormatFromString(String formatName) {
        if (formatName == null) {
            return MISSING;
        }

        switch (formatName.toLowerCase()) {
            case("int8"):
                return INT8;
            case("int16"):
                return INT16;
            case("integer"):
            case("int32"):
                return INT32;
            case ("int64"):
                return INT64;
            case("uint8"):
                return UINT8;
            case("uint16"):
                return UINT16;
            case("uint32"):
                return UINT32;
            case ("uint64"):
                return UINT64;
            case("float"):
                return FLOAT;
            case ("double"):
                return DOUBLE;
            case ("decimal"):
                return DECIMAL;
            case ("byte"):
                return BYTE;
            case ("binary"):
                return BINARY;
            case ("date"):
                return DATE;
            case("datetime"):
            case ("date-time"):
                return DATE_TIME;
            case ("time"):
                return TIME;
            case ("year"):
                return YEAR;
            case ("duration"):
                return DURATION;
            case ("password"):
                return PASSWORD;
            case ("hostname"):
                return HOSTNAME;
            case ("uri"):
                return URI;
            case ("uuid"):
                return UUID;
            case ("ipv4"):
                return IPV4;
            case ("ipv6"):
                return IPV6;
            case ("email"):
                return EMAIL;
            default:
                logger.warning("Unknown type format \"" + formatName + "\".");
                return UNKNOWN;
        }
    }



    public boolean isCompatibleWithType(ParameterType type) {
        if (type == ParameterType.INTEGER) {
            return this == INT8 || this == INT16 || this == INT32 || this == INT64 || this == UINT8 || this == UINT16 ||
                    this == UINT32 || this == UINT64 || this == MISSING || this == UNKNOWN;
        } else if (type == ParameterType.NUMBER) {
            return this == INT8 || this == INT16 || this == INT32 || this == INT64 || this == UINT8 || this == UINT16 ||
                    this == UINT32 || this == UINT64 || this == FLOAT || this == DOUBLE || this == DECIMAL ||
                    this == MISSING || this == UNKNOWN;
        } else if (type == ParameterType.STRING) {
            return !(this == INT8 || this == INT16 || this == INT32 || this == INT64 || this == UINT8 || this == UINT16 ||
                    this == UINT32 || this == UINT64 || this == FLOAT || this == DOUBLE || this == DECIMAL ||
                    this == MISSING || this == UNKNOWN);
        }
        return type == ParameterType.MISSING || type == ParameterType.UNKNOWN;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase().replace("_", "");
    }
}

