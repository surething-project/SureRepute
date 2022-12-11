package pt.ulisboa.tecnico.surerepute;

import com.zaxxer.hikari.HikariDataSource;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import pt.ulisboa.tecnico.surerepute.key.PublicKeyController;
import pt.ulisboa.tecnico.surerepute.key.PublicKeyService;
import pt.ulisboa.tecnico.surerepute.providers.ProtocolBufferMessageBodyProvider;
import pt.ulisboa.tecnico.surerepute.reputation.ReputationController;
import pt.ulisboa.tecnico.surerepute.reputation.ReputationService;
import pt.ulisboa.tecnico.surerepute.shared.SharedController;
import pt.ulisboa.tecnico.surerepute.shared.SharedService;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.LogManager;

public class SureReputeServer {
  public static final int VERSION = 1;
  public static final Properties props = new Properties();
  public static final String APPLICATION_PROTOBUF = "application/x-protobuf";
  public static final HashMap<String, String> serverUrls = new HashMap<>();
  private static final Logger logger = LoggerFactory.getLogger(SureReputeServer.class.getName());
  public static String id;
  private static HikariDataSource ds;

  public static Connection getDb() throws SQLException {
    return ds.getConnection();
  }

  public static void main(String[] args) {
    id = System.getenv("ID");
    try {
      props.load(SureReputeServer.class.getResourceAsStream("/application.properties"));
      logger.info("Server with ID: " + id);
      Properties properties = new Properties();
      properties.load(SureReputeServer.class.getResourceAsStream("/url.properties"));
      for (String key : properties.stringPropertyNames()) {
        if (key.equals(id)) continue;
        String value = properties.getProperty(key);
        serverUrls.put(key, value);
      }
    } catch (IOException e) {
      logger.error(e.getMessage());
      return;
    }
    setConnectionPool(
        System.getenv("DB_CONNECTION"),
        Integer.parseInt(System.getenv("DB_PORT")),
        System.getenv("DB_NAME"),
        System.getenv("DB_USER"),
        System.getenv("DB_PWD"));

    setupLoggerBridge();
    final HttpServer clientServer =
        startServer(
            URI.create(String.format("%s/v%d", System.getenv("CLIENT_SERVER_URL"), VERSION)),
            getClientServerResourceConfig(),
            true);
    final HttpServer serverServer =
        startServer(
            URI.create(String.format("%s/v%d", System.getenv("SERVER_SERVER_URL"), VERSION)),
            getServerServerResourceConfig(),
            true);
    final HttpServer ipServer =
        startServer(
            URI.create(String.format("%s/v%d", System.getenv("IP_SERVER_URL"), VERSION)),
            getIPServerResourceConfig(),
            false);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  clientServer.shutdown();
                  serverServer.shutdown();
                  ipServer.shutdown();
                  ds.close();
                }));
    logger.info("SureRepute server has started!\nTo stop it, press CTRL+C.");
    try {
      Thread.currentThread().join();
    } catch (InterruptedException e) {
      logger.error("Exception: ", e);
    }
  }

  private static void setConnectionPool(
      String conn, int port, String name, String user, String pwd) {
    String url = String.format("jdbc:postgresql://%s:%d/%s", conn, port, name);
    ds = new HikariDataSource();
    ds.setJdbcUrl(url);
    ds.setUsername(user);
    ds.setPassword(pwd);
  }

  private static ResourceConfig getClientServerResourceConfig() {
    ResourceConfig config = new ResourceConfig();
    config.registerClasses(ReputationController.class, ProtocolBufferMessageBodyProvider.class);

    config.register(
        new AbstractBinder() {
          @Override
          protected void configure() {
            bindAsContract(ReputationService.class);
          }
        });
    return config;
  }

  private static ResourceConfig getServerServerResourceConfig() {
    ResourceConfig config = new ResourceConfig();
    config.registerClasses(SharedController.class, ProtocolBufferMessageBodyProvider.class);

    config.register(
        new AbstractBinder() {
          @Override
          protected void configure() {
            bindAsContract(SharedService.class);
          }
        });
    return config;
  }

  private static ResourceConfig getIPServerResourceConfig() {
    ResourceConfig config = new ResourceConfig();
    config.registerClasses(PublicKeyController.class, ProtocolBufferMessageBodyProvider.class);

    config.register(
        new AbstractBinder() {
          @Override
          protected void configure() {
            bindAsContract(PublicKeyService.class);
          }
        });
    return config;
  }

  private static HttpServer startServer(URI uri, ResourceConfig resourceConfig, boolean auth) {
    return GrizzlyHttpServerFactory.createHttpServer(
        uri,
        resourceConfig,
        true,
        new SSLEngineConfigurator(SecurityManager.getInstance().getSSLContext())
            .setClientMode(false)
            .setNeedClientAuth(auth));
  }

  private static void setupLoggerBridge() {
    LogManager.getLogManager().reset();
    SLF4JBridgeHandler.install();
  }
}
