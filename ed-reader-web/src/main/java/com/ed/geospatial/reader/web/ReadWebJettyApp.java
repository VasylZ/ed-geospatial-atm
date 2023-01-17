package com.ed.geospatial.reader.web;

import com.ed.geospatial.core.persistence.AtmPersistenceModule;
import com.ed.geospatial.core.shared.ConfUtils;
import com.ed.geospatial.core.versioning.AtmVersioningServiceModule;
import com.ed.geospatial.reader.presentation.AtmPresentationModule;
import com.ed.geospatial.reader.rest.ReadingRestEndpoint;
import com.ed.geospatial.reader.shared.BasicExceptionMapper;
import com.ed.geospatial.reader.shared.JacksonMapperProvider;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class ReadWebJettyApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadWebJettyApp.class);

    public static final String CONFIG_FILE_NAME = "reader-web-host-config.json";

    public static final String HOST_URI = ConfUtils.load(CONFIG_FILE_NAME).getString("host");

    public static Injector GUICE = Guice.createInjector(
            new AtmPersistenceModule(),
            new AtmPresentationModule(),
            new AtmVersioningServiceModule()
    );

    public static Server startServer() throws Exception {
        LOGGER.info("Configure read application.");

        final ResourceConfig config = new ResourceConfig(ReadingRestEndpoint.class);
        config.register(JacksonMapperProvider.class);
        config.register(BasicExceptionMapper.class);

        final Server server = JettyHttpContainerFactory.createServer(URI.create(HOST_URI), config, false);
        LOGGER.info("Start server");
        server.start();
        LOGGER.info("Server up and running");
        return server;
    }

    public static void main(String[] args) throws Exception {
        try {

            final Server server = startServer();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    LOGGER.info("Shutting down the application...");
                    server.stop();
                    LOGGER.info("Done, exit.");
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }));

            // block and wait shut down signal, like CTRL+C
            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }
}