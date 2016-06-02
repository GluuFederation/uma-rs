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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 19/04/2016
 */

public class ServiceProvider {

    public static final String WELL_KNOWN_UMA_PATH = "/.well-known/uma-configuration";

    private static final Logger LOG = Logger.getLogger(ServiceProvider.class);

    private final String opHost;
    private final ClientExecutor clientExecutor;

    private UmaConfiguration umaConfiguration = null;
    private UmaConfigurationService configurationService = null;
    private ResourceSetRegistrationService resourceSetRegistrationService = null;
    private PermissionRegistrationService permissionRegistrationService;
    private RptStatusService rptStatusService;

    /**
     * @param opHost opHost (example: https://ophost.com)
     */
    public ServiceProvider(String opHost) {
        this(opHost, true);
    }

    public ServiceProvider(String opHost, boolean trustAll) {
        this(opHost, trustAll ? new ApacheHttpClient4Executor(createHttpClientTrustAll()) :
                        new ApacheHttpClient4Executor());
    }

    public ServiceProvider(String opHost, ClientExecutor clientExecutor) {
        this.opHost = opHost;
        this.clientExecutor = clientExecutor;
    }

    public synchronized RptStatusService getRptStatusService() {
        if (rptStatusService == null) {
            rptStatusService = UmaClientFactory.instance().createRptStatusService(umaConfiguration, clientExecutor);
        }
        return rptStatusService;
    }

    public synchronized UmaConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = UmaClientFactory.instance().createMetaDataConfigurationService(opHost + WELL_KNOWN_UMA_PATH, clientExecutor);
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

    public String getOpHost() {
        return opHost;
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
