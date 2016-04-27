package org.xdi.oxd.rs.protect.resteasy;

import org.apache.log4j.Logger;
import org.xdi.oxauth.client.uma.PermissionRegistrationService;
import org.xdi.oxauth.client.uma.ResourceSetRegistrationService;
import org.xdi.oxauth.client.uma.RptStatusService;
import org.xdi.oxauth.client.uma.UmaClientFactory;
import org.xdi.oxauth.client.uma.UmaConfigurationService;
import org.xdi.oxauth.model.uma.UmaConfiguration;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 19/04/2016
 */

public class ServiceProvider {

    private static final Logger LOG = Logger.getLogger(ServiceProvider.class);

    private final Configuration configuration;
    private UmaConfiguration umaConfiguration = null;
    private UmaConfigurationService configurationService = null;
    private ResourceSetRegistrationService resourceSetRegistrationService = null;
    private PermissionRegistrationService permissionRegistrationService;
    private RptStatusService rptStatusService;

    public ServiceProvider(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public synchronized RptStatusService getRptStatusService() {
        if (rptStatusService == null) {
            rptStatusService = UmaClientFactory.instance().createRptStatusService(umaConfiguration);
        }
        return rptStatusService;
    }

    public synchronized UmaConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = UmaClientFactory.instance().createMetaDataConfigurationService(configuration.getUmaWellknownEndpoint());
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

    public synchronized PermissionRegistrationService getPermissionRegistrationService() {
        if (permissionRegistrationService == null) {
            permissionRegistrationService = UmaClientFactory.instance().createResourceSetPermissionRegistrationService(umaConfiguration);
        }
        return permissionRegistrationService;
    }


    public String getAmHost() {
        try {
            return new URI(getConfiguration().getUmaWellknownEndpoint()).getHost();
        } catch (URISyntaxException e) {
            LOG.error("Failed to parse well-known endpoint from configuration: " + getConfiguration().getUmaWellknownEndpoint(),e);
        }
        return "";
    }
}
