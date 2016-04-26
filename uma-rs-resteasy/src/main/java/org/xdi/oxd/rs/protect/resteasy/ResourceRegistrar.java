package org.xdi.oxd.rs.protect.resteasy;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.xdi.oxauth.model.uma.ResourceSet;
import org.xdi.oxauth.model.uma.ResourceSetResponse;
import org.xdi.oxd.rs.protect.Condition;
import org.xdi.oxd.rs.protect.RsResource;

import java.util.List;
import java.util.Map;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 19/04/2016
 */

public class ResourceRegistrar {

    private static final Logger LOG = Logger.getLogger(ResourceRegistrar.class);

    private final Map<Key, RsResource> resourceMap = Maps.newHashMap();
    private final Map<Key, String> idMap = Maps.newHashMap();

    private final PatProvider patProvider;
    private final ServiceProvider serviceProvider;

    public ResourceRegistrar(PatProvider patProvider) {
        this.patProvider = patProvider;
        this.serviceProvider = patProvider.getServiceProvider();
    }

    public void registerOnAuthorizationServer(List<RsResource> resources) {
        Preconditions.checkNotNull(resources);

        for (RsResource resource : resources) {
            register(resource);
        }
    }

    public Key getKey(String path, String httpMethod) {
        if (Strings.isNullOrEmpty(path) || Strings.isNullOrEmpty(httpMethod)) {
            return null;
        }

        String id = idMap.get(new Key(path, Lists.newArrayList(httpMethod)));
        if (id != null) {
            return new Key(path, Lists.newArrayList(httpMethod));
        }

        for (Key key : idMap.keySet()) {
            if (key.getPath().equalsIgnoreCase(path) && key.getHttpMethods().contains(httpMethod)) {
                return key;
            }
        }
        return null;
    }

    public String getResourceSetId(Key key) {
        return key != null ? idMap.get(key) : null;
    }

    public String getResourceSetId(String path, String httpMethod) {
        return getResourceSetId(getKey(path, httpMethod));
    }

    private void register(RsResource resource) {
        try {
            for (Condition condition : resource.getConditions()) {
                Key key = new Key(resource.getPath(), condition.getHttpMethods());

                ResourceSet resourceSet = new ResourceSet();
                resourceSet.setName(key.getResourceName());
                resourceSet.setScopes(condition.getScopes());

                ResourceSetResponse resourceSetResponse = serviceProvider.getResourceSetRegistrationService().addResourceSet("Bearer " + patProvider.getPatToken(), resourceSet);

                Preconditions.checkNotNull(resourceSetResponse.getId(), "Resource set ID can not be null.");

                resourceMap.put(key, resource);
                idMap.put(key, resourceSetResponse.getId());
            }

        } catch (ClientResponseFailure ex) {
            LOG.error(ex.getMessage(), ex);
            throw ex;
        }

    }

    public PatProvider getPatProvider() {
        return patProvider;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }
}
