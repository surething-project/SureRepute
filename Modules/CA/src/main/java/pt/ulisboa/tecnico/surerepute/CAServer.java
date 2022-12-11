package pt.ulisboa.tecnico.surerepute;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import pt.ulisboa.tecnico.surerepute.ca.CAController;
import pt.ulisboa.tecnico.surerepute.ca.CAService;
import pt.ulisboa.tecnico.surerepute.providers.ProtocolBufferMessageBodyProvider;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.logging.LogManager;

public class CAServer {
  public static final int VERSION = 1;
  public static final Properties props = new Properties();
  public static final String APPLICATION_PROTOBUF = "application/x-protobuf";
  private static final Logger logger = LoggerFactory.getLogger(CAServer.class.getName());

  public static void main(String[] args) throws IOException {
    props.load(CAServer.class.getResourceAsStream("/application.properties"));

    setupLoggerBridge();
    final HttpServer server =
        startServer(URI.create(String.format("%s/v%d", System.getenv("CA_URL"), VERSION)));
    Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    logger.info("CA server has started! To stop it, press CTRL+C.");
    try {
      Thread.currentThread().join();
    } catch (InterruptedException e) {
      logger.error("Exception: ", e);
    }
  }

  private static void setupLoggerBridge() {
    LogManager.getLogManager().reset();
    SLF4JBridgeHandler.install();
  }

  private static ResourceConfig getResourceConfig() {
    ResourceConfig config = new ResourceConfig();
    config.registerClasses(CAController.class, ProtocolBufferMessageBodyProvider.class);

    config.register(
        new AbstractBinder() {
          @Override
          protected void configure() {
            bindAsContract(CAService.class);
          }
        });
    return config;
  }

  private static HttpServer startServer(URI uri) {
    return GrizzlyHttpServerFactory.createHttpServer(
        uri,
        getResourceConfig(),
        true,
        new SSLEngineConfigurator(SecurityManager.getInstance().getSSLContext())
            .setClientMode(false)
            .setNeedClientAuth(false));
  }
}
