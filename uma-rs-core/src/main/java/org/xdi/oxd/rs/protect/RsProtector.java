package org.xdi.oxd.rs.protect;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 24/12/2015
 */

public class RsProtector {

    private Map<String, RsResource> resourceMap = Maps.newHashMap();

    public RsProtector(List<RsResource> resourceList) {
        Preconditions.checkNotNull(resourceList);

        for (RsResource resource : resourceList) {
            resourceMap.put(resource.getPath(), resource);
        }
    }

    public static RsProtector instance(InputStream inputStream) throws IOException {
        try {
            final RsResourceList resourceList = Jackson.createJsonMapper().readValue(inputStream, RsResourceList.class);
            return new RsProtector(resourceList.getResources());
        } finally {
            Closeables.closeQuietly(inputStream);
        }
    }

    public boolean hasAccess(String path, String httpMethod, String... presentScope) {
        Preconditions.checkNotNull(presentScope);

        return hasAccess(path, httpMethod, Arrays.asList(presentScope));
    }

    public boolean hasAccess(String path, String httpMethod, List<String> presentScopes) {
        Preconditions.checkNotNull(path);
        Preconditions.checkNotNull(presentScopes);
        Preconditions.checkNotNull(httpMethod);

        Preconditions.checkState(!presentScopes.isEmpty(), "Scopes can't be empty.");

        final RsResource rsResource = resourceMap.get(path);
        if (rsResource != null) {
            final List<String> requiredScopes = rsResource.scopes(httpMethod);
            if (requiredScopes != null) {
                return requiredScopes.containsAll(presentScopes);
            }
        }
        return false;
    }

}