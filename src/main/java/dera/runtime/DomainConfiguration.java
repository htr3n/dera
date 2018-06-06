package dera.runtime;

import dera.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Properties;

public final class DomainConfiguration {

    private final Logger LOG = LoggerFactory.getLogger(DomainConfiguration.class);

    public static final String DERA_DOMAIN_NAME = "dera.domain.name";
    public static final String DERA_DOMAIN_HOST = "dera.domain.host";
    public static final String DERA_DOMAIN_PORT_HTTP = "dera.domain.port.http";
    public static final String DERA_DOMAIN_PORT_HTTPS = "dera.domain.port.https";
    public static final String DERA_DOMAIN_PORT_LOCAL = "dera.domain.port.local";
    public static final String DERA_DOMAIN_MAX_CHANNEL_SIZE = "dera.domain.maxChannelSize";
    public static final String DERA_DOMAIN_MAX_POOL_SIZE = "dera.domain.maxPoolSize";
    public static final String DERA_DOMAIN_MONITORED = "dera.domain.monitored";
    public static final String DERA_DOMAIN_SECURED = "dera.domain.secured";
    public static final String DERA_DOMAIN_LOGGED = "dera.domain.logged";
    public static final String DERA_DOMAIN_LOG_MONGODB_URI = "dera.domain.log.mongodb.uri";
    public static final String DERA_DOMAIN_LOG_MONGODB_DB = "dera.domain.log.mongodb.db";
    public static final String DERA_DOMAIN_LOG_MONGODB_COLLECTION = "dera.domain.log.mongodb.collection";

    private static final int DEFAULT_HTTP_PORT = 3372;
    private static final int DEFAULT_MAX_CHANNEL_SIZE = 8096;

    private String name = "StandaloneDomain";
    private String host = "localhost";
    private int portHttp = DEFAULT_HTTP_PORT;
    private int portHttps = portHttp + 1;
    private int portLocal = portHttp + 2;
    private String workingDir;
    private int maxChannelSize = DEFAULT_MAX_CHANNEL_SIZE;
    private int maxPoolSize = Runtime.getRuntime().availableProcessors();

    private boolean monitored = false;
    private boolean secured = true;

    private boolean logged = false;
    private String mongoURI = null;
    private String mongoDB = null;
    private String mongoCollection = null;


    public DomainConfiguration() {
        findWorkingDirectory();
        loadDomainConfiguration();
        printDomainConfiguration();
    }

    private void findWorkingDirectory() {
        try {
            CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
            File container = new File(codeSource.getLocation().toURI().getPath());
            if (container.isFile()) {
                workingDir = container.getParentFile().getAbsolutePath();
            } else if (container.isDirectory()) {
                workingDir = container.getAbsolutePath();
            }
        } catch (URISyntaxException e) {
            LOG.warn("Unable to get the working directory: " + e.getMessage());
        }
    }

    private void loadDomainConfiguration() {
        final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("dera.properties");
        if (inputStream != null) {
            try {
                Properties prop = new Properties();
                prop.load(inputStream);
                loadHostingConfiguration(prop);
                loadEngineConfiguration(prop);
                loadDatabaseConfiguration(prop);
            } catch (IOException e) {
                LOG.warn("Unable to load the properties file. Error: " + e.getMessage() + ". Using default values.");
            } catch (IllegalArgumentException e) {
                LOG.warn("The properties file is malformed. Error: " + e.getMessage() + ". Using default values.");
            }
        }
    }

    private void loadDatabaseConfiguration(Properties prop) {
        if (TextUtil.neitherNullNorEmpty(prop.getProperty(DERA_DOMAIN_LOG_MONGODB_URI))) {
            mongoURI = prop.getProperty(DERA_DOMAIN_LOG_MONGODB_URI);
        }
        if (TextUtil.neitherNullNorEmpty(prop.getProperty(DERA_DOMAIN_LOG_MONGODB_COLLECTION))) {
            mongoCollection = prop.getProperty(DERA_DOMAIN_LOG_MONGODB_COLLECTION);
        }
        if (TextUtil.neitherNullNorEmpty(prop.getProperty(DERA_DOMAIN_LOG_MONGODB_DB))) {
            mongoDB = prop.getProperty(DERA_DOMAIN_LOG_MONGODB_DB);
        }
    }

    private void loadEngineConfiguration(Properties prop) {
        try {
            int tmp = Integer.valueOf(prop.getProperty(DERA_DOMAIN_MAX_CHANNEL_SIZE));
            maxChannelSize = tmp;
        } catch (NumberFormatException e) {
        }
        try {
            int tmp = Integer.valueOf(prop.getProperty(DERA_DOMAIN_MAX_POOL_SIZE));
            maxPoolSize = tmp;
        } catch (NumberFormatException e) {
        }
        monitored = Boolean.valueOf(prop.getProperty(DERA_DOMAIN_MONITORED));
        secured = Boolean.valueOf(prop.getProperty(DERA_DOMAIN_SECURED));
        logged = Boolean.valueOf(prop.getProperty(DERA_DOMAIN_LOGGED));

    }

    private void loadHostingConfiguration(Properties prop) {
        if (TextUtil.neitherNullNorEmpty(prop.getProperty(DERA_DOMAIN_HOST))) {
            host = prop.getProperty(DERA_DOMAIN_HOST);
        }
        if (TextUtil.neitherNullNorEmpty(prop.getProperty(DERA_DOMAIN_HOST))) {
            name = prop.getProperty(DERA_DOMAIN_NAME);
        }
        try {
            int tmp = Integer.valueOf(prop.getProperty(DERA_DOMAIN_PORT_HTTP));
            portHttp = tmp;
        } catch (NumberFormatException e) {
        }

        try {
            int tmp = Integer.valueOf(prop.getProperty(DERA_DOMAIN_PORT_HTTPS));
            portHttps = tmp;
        } catch (NumberFormatException e) {
        }
        try {
            int tmp = Integer.valueOf(prop.getProperty(DERA_DOMAIN_PORT_LOCAL));
            portLocal = tmp;
        } catch (NumberFormatException e) {
        }
    }

    public int getPortHttp() {
        return portHttp;
    }

    public boolean isSecured() {
        return secured;
    }

    public int getPortHttps() {
        return portHttps;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public int getMaxChannelSize() {
        return maxChannelSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public boolean isMonitored() {
        return monitored;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public int getPortLocal() {
        return portLocal;
    }

    public boolean isLogged() {
        return logged;
    }

    public String getMongoURI() {
        return mongoURI;
    }

    public String getMongoCollection() {
        return mongoCollection;
    }

    public String getMongoDB() {
        return mongoDB;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPortHttp(int portHttp) {
        this.portHttp = portHttp;
    }

    public void setPortHttps(int portHttps) {
        this.portHttps = portHttps;
    }

    private void printDomainConfiguration() {
        LOG.debug("Domain configuration:\n" +
                "\tHost = {}\n" +
                "\tHTTP port = {}\n" +
                "\tHTTPS port = {}\n" +
                "\tWorking Directory =  {}\n" +
                "\tMax. Channel Size = {}\n" +
                "\tMax. Pool Size = {}\n" +
                "\tMonitored = {}\n" +
                "\tSecured = {}\n" +
                "\tLogged = {}\n",
                new Object[]{
                        getHost(),
                        getPortHttp(),
                        getPortHttps(),
                        getWorkingDir(),
                        getMaxChannelSize(),
                        getMaxPoolSize(),
                        isMonitored() ? "Yes" : "No",
                        isSecured() ? "Yes" : "No",
                        isLogged()  ? "Yes" : "No"
                });
    }

}
