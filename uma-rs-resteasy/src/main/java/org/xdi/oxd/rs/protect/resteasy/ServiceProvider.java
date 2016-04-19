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
    private UmaConfigurationService configurationService = null;
    private ResourceSetRegistrationService resourceSetRegistrationService = null;

    public ServiceProvider(String umaWellknownEndpoint) {
        this.umaWellknownEndpoint = umaWellknownEndpoint;
    }

    public synchronized UmaConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = UmaClientFactory.instance().createMetaDataConfigurationService(umaWellknownEndpoint);
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
            resourceSetRegistrationService = UmaClientFactory.instance().createResourceSetRegistrationService(umaConfiguration);
        }
        return resourceSetRegistrationService;
    }
}
