package org.xdi.oxd.rs.protect.resteasy;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.xdi.oxauth.client.TokenClient;
import org.xdi.oxauth.client.TokenResponse;
import org.xdi.oxauth.model.uma.UmaConfiguration;
import org.xdi.oxauth.model.uma.UmaScopeType;
import org.xdi.oxauth.model.uma.wrapper.Token;
import org.xdi.oxauth.model.util.Util;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 02/06/2016
 */

public class ObtainPatProvider implements PatProvider {

    private static final Logger LOG = Logger.getLogger(PatProvider.class);

    private final ServiceProvider serviceProvider;

    private Token patToken;

    public ObtainPatProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public synchronized String getPatToken() {
        if (patToken == null) {
            obtainPat();
        }
        Preconditions.checkNotNull(patToken);
        return patToken.getAccessToken();
    }

    public synchronized void clearPat() {
        patToken = null;
        LOG.trace("Cleared PAT.");
    }

    public String renewPat() {
        clearPat();
        return getPatToken();
    }

    private void obtainPat() {
        try {
            UmaConfiguration umaConfiguration = serviceProvider.getUmaConfiguration();
            Configuration configuration = serviceProvider.getConfiguration();

            patToken = requestPat(umaConfiguration.getTokenEndpoint(), configuration.getUmaPatClientId(), configuration.getUmaPatClientSecret());
            LOG.trace("New PAT obtained.");
        } catch (Exception e) {
            LOG.error("Failed to obtain PAT. " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public Token requestPat(final String tokenUrl, final String umaClientId, final String umaClientSecret, String... scopeArray) throws Exception {

        String scope = UmaScopeType.PROTECTION.getValue();
        if (scopeArray != null && scopeArray.length > 0) {
            for (String s : scopeArray) {
                scope = scope + " " + s;
            }
        }

        TokenClient tokenClient = new TokenClient(tokenUrl);
        tokenClient.setExecutor(serviceProvider.getClientExecutor());
        TokenResponse response = tokenClient.execClientCredentialsGrant(scope, umaClientId, umaClientSecret);

        if (response.getStatus() == 200) {
            final String patToken = response.getAccessToken();
            final Integer expiresIn = response.getExpiresIn();
            if (Util.allNotBlank(patToken)) {
                return new Token(null, null, patToken, UmaScopeType.PROTECTION.getValue(), expiresIn);
            }
        }

        return null;
    }
}
