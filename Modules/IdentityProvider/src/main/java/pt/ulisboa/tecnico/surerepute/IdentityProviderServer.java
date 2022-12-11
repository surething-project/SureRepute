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
import pt.ulisboa.tecnico.surerepute.identityprovider.IdentityProviderController;
import pt.ulisboa.tecnico.surerepute.identityprovider.IdentityProviderService;
import pt.ulisboa.tecnico.surerepute.providers.ProtocolBufferMessageBodyProvider;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.LogManager;

public class IdentityProviderServer {
  public static final int VERSION = 1;
  public static final Properties props = new Properties();
  public static final String APPLICATION_PROTOBUF = "application/x-protobuf";
  private static final Logger logger =
      LoggerFactory.getLogger(IdentityProviderServer.class.getName());
  private static HikariDataSource ds;

  public static Connection getDb() throws SQLException {
    return ds.getConnection();
  }

  public static void main(String[] args) throws IOException {
    props.load(IdentityProviderServer.class.getResourceAsStream("/application.properties"));

    setConnectionPool(
        System.getenv("DB_CONNECTION"),
        Integer.parseInt(System.getenv("DB_PORT")),
        System.getenv("DB_NAME"),
        System.getenv("DB_USER"),
        System.getenv("DB_PWD"));

    setupLoggerBridge();
    final HttpServer server =
        startServer(
            URI.create(String.format("%s/v%d", System.getenv("IDENTITY_PROVIDER_URL"), VERSION)));
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  server.shutdown();
                  ds.close();
                }));
    logger.info("Identity provider has started!\nTo stop it, press CTRL+C.");
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

  private static ResourceConfig getResourceConfig() {
    ResourceConfig config = new ResourceConfig();
    config.registerClasses(
        IdentityProviderController.class, ProtocolBufferMessageBodyProvider.class);

    config.register(
        new AbstractBinder() {
          @Override
          protected void configure() {
            bindAsContract(IdentityProviderService.class);
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
            .setNeedClientAuth(true));
  }

  private static void setupLoggerBridge() {
    LogManager.getLogManager().reset();
    SLF4JBridgeHandler.install();
  }
}
