package dera.runtime;

import dera.core.LifeCycle;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.LogManager;

public class DomainServer extends LifeCycle {

    private static final Logger LOG = LoggerFactory.getLogger(DomainServer.class);
    public static final String ROOT = "/";
    private static final int SLEEP_TIME_IN_MILLISECONDS = 1000;
    private Server server;
    private final StandaloneExecutionDomain domain;

    public DomainServer(StandaloneExecutionDomain domain) {
        this.domain = domain;
        this.server = new Server();
    }

    @Override
    public void init() {
        try {
            final InputStream is = StandaloneExecutionDomain.class.getClassLoader().getResourceAsStream("logging.properties");
            if (is != null) {
                LogManager.getLogManager().readConfiguration(is);
                is.close();
            } else {
                LOG.warn("Unable to load the logging.properties for the Jersey framework");
            }
        } catch (IOException e) {
        }
        initServer();
    }

    @Override
    public void start() {
        startServer();
        switchState();
    }

    @Override
    public void stop() {
        stopServer();
        switchState();
    }

    private void initServer() {
        initHTTPConnector();
        if (domain.getConfiguration().isSecured()) {
            initHTTPSConnector();
        }
        initWebApp();
    }

    private void initWebApp() {
        URL webDir = DomainServer.class.getClassLoader().getResource("web");
        final WebAppContext webAppContext = new WebAppContext();
        webAppContext.setDescriptor("/WEB-INF/web.xml");
        webAppContext.setResourceBase(webDir.toExternalForm());
        webAppContext.setContextPath(ROOT);
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[]{webAppContext});
        server.setHandler(contexts);
    }

    private void initHTTPConnector() {
        if (server != null) {
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(domain.getConfiguration().getPortHttp());
            server.addConnector(connector);
        }
    }

    private void initHTTPSConnector() {
        if (server != null) {
            try {
                final SslContextFactory sslContextFactory = new SslContextFactory();

                sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
                sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
                sslContextFactory.setExcludeCipherSuites(
                        "SSL_RSA_WITH_DES_CBC_SHA",
                        "SSL_DHE_RSA_WITH_DES_CBC_SHA",
                        "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                        "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                        "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                        "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
                        "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");

                URL keystoreUrl = getClass().getClassLoader().getResource("server-keystore.jks");
                sslContextFactory.setKeyStorePath(keystoreUrl.getFile());
                sslContextFactory.setKeyStorePassword("OBF:1l891kfl1m0n1igh1x0z1x1n1idp1lxj1kct1l51");
                sslContextFactory.setKeyManagerPassword("OBF:1l891kfl1m0n1igh1x0z1x1n1idp1lxj1kct1l51");

                HttpConfiguration httpsConfig = new HttpConfiguration();
                httpsConfig.setSecurePort(domain.getConfiguration().getPortHttps());
                httpsConfig.addCustomizer(new SecureRequestCustomizer());

                ServerConnector sslConnector = new ServerConnector(server,
                        new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                        new HttpConnectionFactory(httpsConfig));

                sslConnector.setPort(domain.getConfiguration().getPortHttps());

                server.addConnector(sslConnector);
            } catch (Exception e) {
                LOG.error("Unable to initialize the HTTPS connector", e);
            }
        }
    }

    private void startServer() {
        if (server != null) {
            domain.getDaemonExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        server.setStopAtShutdown(true);
                        server.start();
                        server.join();
                    } catch (Exception e) {
                        LOG.error("Failed to start the server", e);
                    }
                }
            });
        }
    }

    private void stopServer() {
        if (server != null && server.isStarted()) {
            try {
                server.stop();
                LOG.info("The domain server is stopped");
                switchState();
            } catch (Exception e) {
                LOG.error("Failed to stop the listeners", e);
            }
        }
    }
}


