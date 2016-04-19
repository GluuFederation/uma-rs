package org.xdi.oxd.rs.protect.resteasy;

import org.xdi.oxauth.client.uma.wrapper.UmaClient;
import org.xdi.oxauth.model.uma.wrapper.Token;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 19/04/2016
 */

public class PatProvider {

    private final ServiceProvider serviceProvider;

    private Token patToken;

    public PatProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public synchronized String getPatToken() {
        if (patToken == null) {
            obtainPat();
        }
        return patToken.getAccessToken();
    }

    private void obtainPat() {
        try {
            patToken = UmaClient.requestPat(null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }
}
