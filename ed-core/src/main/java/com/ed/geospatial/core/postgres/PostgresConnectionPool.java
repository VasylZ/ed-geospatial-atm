package com.ed.geospatial.core.postgres;

import com.ed.geospatial.core.shared.ConfUtils;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Singleton
public class PostgresConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresConnectionPool.class);

    public static final String CONFIG_FILE_NAME = "postgres-connection.json";

    private final DataSource dataSource;

    public PostgresConnectionPool() {
        LOGGER.debug("Start Postgres connection init");
        final Config config = ConfUtils.load(CONFIG_FILE_NAME);

        final String host = ConfUtils.getStrVal(config, "dbHost");
        final String db = config.getString("dbName");
        final int port = config.getInt("dbPort");

        if (isEmpty(host)) {
            throw new IllegalArgumentException("host is not configured");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port is not configured");
        }

        final Properties props = new Properties();
        props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
        props.setProperty("dataSource.serverName", host);
        props.setProperty("dataSource.portNumber", String.valueOf(port));
        props.setProperty("dataSource.user", config.getString("dbUser"));
        props.setProperty("dataSource.password", config.getString("dbPassword"));
        props.setProperty("dataSource.databaseName", db);
        props.setProperty("maximumPoolSize", String.valueOf(100));

        HikariConfig hikariConfig = new HikariConfig(props);
        dataSource = new HikariDataSource(hikariConfig);

        LOGGER.debug("Start Postgres connection established on [{}:{}]", host, port);
    }

    public Connection getDataSource() throws SQLException {
        return dataSource.getConnection();
    }
}
