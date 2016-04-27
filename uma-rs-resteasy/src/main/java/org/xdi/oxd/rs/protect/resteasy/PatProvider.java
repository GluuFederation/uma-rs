package org.xdi.oxd.rs.protect.resteasy;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import org.xdi.oxauth.client.uma.wrapper.UmaClient;
import org.xdi.oxauth.model.uma.UmaConfiguration;
import org.xdi.oxauth.model.uma.wrapper.Token;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 19/04/2016
 */

public class PatProvider {

    private static final Logger LOG = Logger.getLogger(PatProvider.class);

    private final ServiceProvider serviceProvider;

    private Token patToken;

    public PatProvider(ServiceProvider serviceProvider) {
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
    }

    private void obtainPat() {
        try {
            UmaConfiguration umaConfiguration = serviceProvider.getUmaConfiguration();
            Configuration configuration = serviceProvider.getConfiguration();

            patToken = UmaClient.requestPat(umaConfiguration.getTokenEndpoint(), configuration.getUmaPatClientId(), configuration.getUmaPatClientSecret());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }
}
