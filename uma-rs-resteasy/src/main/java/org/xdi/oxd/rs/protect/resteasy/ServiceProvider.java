package org.xdi.oxd.rs.protect.resteasy;

import org.xdi.oxauth.client.uma.ResourceSetRegistrationService;
import org.xdi.oxauth.client.uma.UmaClientFactory;
import org.xdi.oxauth.client.uma.UmaConfigurationService;
import org.xdi.oxauth.model.uma.UmaConfiguration;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 19/04/2016
 */

public class ServiceProvider {

    private final String umaWellknownEndpoint;
    private UmaConfiguration umaConfiguration = null;

    public ServiceProvider(String umaWellknownEndpoint) {
        this.umaWellknownEndpoint = umaWellknownEndpoint;
    }

    private synchronized UmaConfiguration getUmaConfiguration() {
        if (umaConfiguration == null) {
            UmaConfigurationService service = UmaClientFactory.instance().createMetaDataConfigurationService(umaWellknownEndpoint);
            umaConfiguration = service.getMetadataConfiguration();
        }
        return umaConfiguration;
    }

    public ResourceSetRegistrationService getResourceSetRegistrationService() {
        return UmaClientFactory.instance().createResourceSetRegistrationService(umaConfiguration);
    }
}
