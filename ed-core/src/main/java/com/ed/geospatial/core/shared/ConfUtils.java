package com.ed.geospatial.core.shared;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public final class ConfUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfUtils.class);

    private static final String ENV_PROFILE = "ENV_PROFILE";

    private static final String ENVIRONMENT;

    static {
        String env = System.getProperty(ENV_PROFILE, "");
        if (isEmpty(env)) {
            env = System.getenv(ENV_PROFILE);
        }
        ENVIRONMENT = env;
        LOGGER.info("Initialize environment: [{}]", ENVIRONMENT);
    }

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
