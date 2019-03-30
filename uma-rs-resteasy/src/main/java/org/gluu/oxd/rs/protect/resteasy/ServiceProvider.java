package org.gluu.oxd.rs.protect.resteasy;

import org.apache.commons.lang.StringUtils;
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
import org.gluu.oxauth.client.uma.UmaClientFactory;
import org.gluu.oxauth.client.uma.UmaMetadataService;
import org.gluu.oxauth.client.uma.UmaPermissionService;
import org.gluu.oxauth.client.uma.UmaResourceService;
import org.gluu.oxauth.client.uma.UmaRptIntrospectionService;
import org.gluu.oxauth.model.uma.UmaMetadata;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author Yuriy Zabrovarnyy
 */

public class ServiceProvider {

    public static final String WELL_KNOWN_UMA_PATH = "/.well-known/uma2-configuration";

    private static final Logger LOG = Logger.getLogger(ServiceProvider.class);

    private final String opHost;
    private final ClientExecutor clientExecutor;

    private UmaMetadata umaMetadata = null;
    private UmaMetadataService metadataService = null;
    private UmaResourceService resourceService = null;
    private UmaPermissionService permissionService;
    private UmaRptIntrospectionService rptIntrospectionService;

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

    public synchronized UmaRptIntrospectionService getRptIntrospectionService() {
        if (rptIntrospectionService == null) {
            rptIntrospectionService = UmaClientFactory.instance().createRptStatusService(umaMetadata, clientExecutor);
        }
        return rptIntrospectionService;
    }

    public synchronized UmaMetadataService getMetadataService() {
        if (metadataService == null) {
            metadataService = UmaClientFactory.instance().createMetadataService(opHost + WELL_KNOWN_UMA_PATH, clientExecutor);
        }
        return metadataService;
    }

    public synchronized UmaMetadata getUmaMetadata() {
        if (umaMetadata == null) {
            umaMetadata = getMetadataService().getMetadata();
            LOG.trace("UMA discovery:" + umaMetadata);
        }
        return umaMetadata;
    }

    public synchronized UmaResourceService getResourceService() {
        if (resourceService == null) {
            resourceService = UmaClientFactory.instance().createResourceService(getUmaMetadata(), clientExecutor);
        }
        return resourceService;
    }

    public synchronized UmaPermissionService getPermissionService() {
        if (permissionService == null) {
            permissionService = UmaClientFactory.instance().createPermissionService(getUmaMetadata(), clientExecutor);
        }
        return permissionService;
    }

    public String getOpHost() {
        return opHost;
    }

    public String opHostWithoutProtocol() {
        if (StringUtils.contains(opHost, "//")) {
            return StringUtils.substringAfter(opHost, "//");
        }
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
