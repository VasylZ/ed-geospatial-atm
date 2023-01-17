package com.ed.geospatial.core.shared;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public final class ConfUtils {

    private static final String ENV_PROFILE = "ENV_PROFILE";

    private static final String ENVIRONMENT = System.getProperty(ENV_PROFILE, "");

    private ConfUtils() {
        throw new IllegalStateException("Shouldn't be instantiated");
    }

    public static Config load(final String fileName) {
        final Config config = ConfigFactory.load(fileName);
        if (isEmpty(ENVIRONMENT)) {
            return config;
        }
        return config.getConfig(ENVIRONMENT);
    }

    public static String getStrVal(Config config, String param) {
        String val = config.getString(param);

        if (!isEmpty(System.getenv(param))) {
            val = System.getenv(param);
        }
        return val;
    }
}
