package org.xdi.oxd.rs.protect.resteasy;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.xdi.oxauth.client.uma.PermissionRegistrationService;
import org.xdi.oxauth.client.uma.ResourceSetRegistrationService;
import org.xdi.oxauth.client.uma.RptStatusService;
import org.xdi.oxauth.client.uma.UmaClientFactory;
import org.xdi.oxauth.client.uma.UmaConfigurationService;
import org.xdi.oxauth.model.uma.UmaConfiguration;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 19/04/2016
 */

public class ServiceProvider {

    private static final Logger LOG = Logger.getLogger(ServiceProvider.class);

    private final Configuration configuration;
    private final ClientExecutor clientExecutor;

    private UmaConfiguration umaConfiguration = null;
    private UmaConfigurationService configurationService = null;
    private ResourceSetRegistrationService resourceSetRegistrationService = null;
    private PermissionRegistrationService permissionRegistrationService;
    private RptStatusService rptStatusService;


    public ServiceProvider(Configuration configuration) {
        this(configuration, configuration.isTrustAll() ? new ApacheHttpClient4Executor(createHttpClientTrustAll()) :
                        new ApacheHttpClient4Executor());
    }

    public ServiceProvider(Configuration configuration, ClientExecutor clientExecutor) {
        this.configuration = configuration;
        this.clientExecutor = clientExecutor;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public synchronized RptStatusService getRptStatusService() {
        if (rptStatusService == null) {
            rptStatusService = UmaClientFactory.instance().createRptStatusService(umaConfiguration, clientExecutor);
        }
        return rptStatusService;
    }

    public synchronized UmaConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = UmaClientFactory.instance().createMetaDataConfigurationService(configuration.getUmaWellknownEndpoint(), clientExecutor);
        }
        return configurationService;
    }

    public synchronized UmaConfiguration getUmaConfiguration() {
        if (umaConfiguration == null) {
            umaConfiguration = getConfigurationService().getMetadataConfiguration();
        }
        return umaConfiguration;
    }

    public synchronized ResourceSetRegistrationService getResourceSetRegistrationService() {
        if (resourceSetRegistrationService == null) {
            resourceSetRegistrationService = UmaClientFactory.instance().createResourceSetRegistrationService(getUmaConfiguration(), clientExecutor);
        }
        return resourceSetRegistrationService;
    }

    public synchronized PermissionRegistrationService getPermissionRegistrationService() {
        if (permissionRegistrationService == null) {
            permissionRegistrationService = UmaClientFactory.instance().createResourceSetPermissionRegistrationService(getUmaConfiguration(), clientExecutor);
        }
        return permissionRegistrationService;
    }


    public String getAmHost() {
        try {
            return new URI(getConfiguration().getUmaWellknownEndpoint()).getHost();
        } catch (URISyntaxException e) {
            LOG.error("Failed to parse well-known endpoint from configuration: " + getConfiguration().getUmaWellknownEndpoint(), e);
        }
        return "";
    }

    public ClientExecutor getClientExecutor() {
        return clientExecutor;
    }

    public static HttpClient createHttpClientTrustAll() {
        try {
            SSLSocketFactory sf = new SSLSocketFactory(new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }, new X509HostnameVerifier() {
                @Override
                public void verify(String host, SSLSocket ssl) throws IOException {
                }

                @Override
                public void verify(String host, X509Certificate cert) throws SSLException {
                }

                @Override
                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                }

                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            }
            );

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
            registry.register(new Scheme("https", 443, sf));
            ClientConnectionManager ccm = new SingleClientConnManager(registry);
            return new DefaultHttpClient(ccm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
