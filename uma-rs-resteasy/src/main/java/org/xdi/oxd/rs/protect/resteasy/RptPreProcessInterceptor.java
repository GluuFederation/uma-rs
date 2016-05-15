package org.xdi.oxd.rs.protect.resteasy;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.xdi.oxauth.model.uma.PermissionTicket;
import org.xdi.oxauth.model.uma.RptIntrospectionResponse;
import org.xdi.oxauth.model.uma.UmaPermission;
import org.xdi.oxd.rs.protect.Jackson;
import org.xdi.util.StringHelper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 18/04/2016
 */

public class RptPreProcessInterceptor implements PreProcessInterceptor {

    private static final Logger LOG = Logger.getLogger(RptPreProcessInterceptor.class);

    private final ResourceRegistrar resourceRegistrar;
    private final PatProvider patProvider;
    private final ServiceProvider serviceProvider;

    public RptPreProcessInterceptor(ResourceRegistrar resourceRegistrar) {
        Preconditions.checkNotNull(resourceRegistrar, "Resource registrar is null.");
        Preconditions.checkNotNull(resourceRegistrar.getPatProvider(), "PAT Provider is null.");
        Preconditions.checkNotNull(resourceRegistrar.getServiceProvider(), "Service Provider is null.");

        this.resourceRegistrar = resourceRegistrar;
        this.patProvider = resourceRegistrar.getPatProvider();
        this.serviceProvider = resourceRegistrar.getServiceProvider();
    }

    @Override
    public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException {

        String path = getPath(request);
        String httpMethod = request.getHttpMethod();

        Key key = resourceRegistrar.getKey(path, httpMethod);
        if (key == null) {
            LOG.debug("Resource is not protected with UMA, path:" + path + ", httpMethod: " + httpMethod);
            return null;
        }

        try {
            String rpt = getRpt(request.getHttpHeaders());

            if (!Strings.isNullOrEmpty(rpt)) {
                LOG.debug("RPT present in request");
                final RptIntrospectionResponse status = requestRptStatus(rpt);
                if (hasPermission(status, key, httpMethod)) {
                    LOG.debug("RPT has enough permissions, access GRANTED. Path: " + path + ", httpMethod:" + httpMethod);
                    return null;
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return (ServerResponse) Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }

        // If the client's request at the protected resource has no RPT,
        // or has an invalid RPT or insufficient authorization data associated with the RPT as determined through
        // RPT status checking (see Section 3.4), then assuming the resource server chooses to respond to the client,
        // it MUST use the protection API's permission registration endpoint to register a requested permission with
        // the corresponding authorization server.
        LOG.debug("Client does not present valid RPT. Registering permission ticket ...");

        return (ServerResponse) registerTicketResponse(path, httpMethod);
    }

    private boolean hasPermission(RptIntrospectionResponse status, Key key, String httpMethod) {
        if (status != null && status.getActive()) {
            String resourceSetId = resourceRegistrar.getResourceSetId(key);
            if (Strings.isNullOrEmpty(resourceSetId)) {
                LOG.error("Resource has key but is not registered on AS. Key: " + key);
                return false;
            }

            for (UmaPermission permission : status.getPermissions()) {
                if (permission.getResourceSetId().equals(resourceSetId) &&
                        resourceRegistrar.getProtector().hasAccess(key.getPath(), httpMethod, permission.getScopes())) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getPath(HttpRequest request) {
        if (request.getUri() != null && request.getUri().getAbsolutePath() != null) {
            return request.getUri().getAbsolutePath().getPath();
        }
        return null;
    }

    public static String getRptFromAuthorization(String authorizationHeader) {
        if (StringHelper.isNotEmpty(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring("Bearer ".length());
        }
        return null;
    }

    public static String getRpt(HttpHeaders httpHeaders) {
        if (httpHeaders != null) {
            final List<String> authHeaders = httpHeaders.getRequestHeader("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty()) {
                final String authorization = authHeaders.get(0);
                return getRptFromAuthorization(authorization);
            }
        }
        return "";
    }

    public RptIntrospectionResponse requestRptStatus(String rpt) {
        if (StringUtils.isNotBlank(rpt)) {

            LOG.debug("Request RPT status...");

            final RptIntrospectionResponse status = serviceProvider.getRptStatusService().requestRptStatus("Bearer " + patProvider.getPatToken(), rpt, "");
            if (status != null) {
                LOG.debug("RPT status: " + Jackson.asJsonSilently(status));
                return status;
            } else {
                LOG.debug("Unable to retrieve RPT status from AM.");
            }
        }
        return null;
    }

    public Response registerTicketResponse(String path, String httpMethod) {

        Key key = resourceRegistrar.getKey(path, httpMethod);
        if (key == null) {
            LOG.error("Resource is not registered. Path: " + path + ", httpMethod: " + httpMethod + ". Please register it via uma-rs configuration.");
            LOG.error("Skip protection !!!");
            return null;
        }

        String resourceSetId = resourceRegistrar.getResourceSetId(key);

        try {
            UmaPermission permission = new UmaPermission();
            permission.setResourceSetId(resourceSetId);
            permission.setScopes(resourceRegistrar.getRsResource(key).scopes(httpMethod));

            PermissionTicket ticket = resourceRegistrar.getServiceProvider().getPermissionRegistrationService().registerResourceSetPermission(
                    "Bearer " + patProvider.getPatToken(), serviceProvider.getAmHost(), permission);
            if (ticket != null) {
                return Response.status(Response.Status.FORBIDDEN)
                        .header("WWW-Authenticate", "UMA realm=\"rs\"," +
                                "as_uri=\"https://" + serviceProvider.getAmHost() + "\"," +
                                "error=\"insufficient_scope\"," +
                                "ticket=\"" + ticket.getTicket() + "\"")
                        .entity(ticket)
                        .build();
            } else {
                LOG.error("Failed to register permission ticket. Response is null.");
            }
        } catch (Exception e) {
            LOG.error("Failed to register permission.", e);
        }
        return Response.status(Response.Status.FORBIDDEN)
                .header("Warning:", "UMA Authorization Server Unreachable")
                .build();
    }
}

