package org.xdi.oxd.rs.protect.resteasy;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.xdi.oxauth.model.uma.RptIntrospectionResponse;
import org.xdi.util.StringHelper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.List;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 18/04/2016
 */

@Provider
@ServerInterceptor
public class RptPreProcessInterceptor implements PreProcessInterceptor {

    private static final Logger LOG = Logger.getLogger(RptPreProcessInterceptor.class);

    public static final String RPT_STATUS_ATTR_NAME = "rptStatus";

    private final ResourceRegistrar resourceRegistrar;
    private final PatProvider patProvider;

    public RptPreProcessInterceptor(ResourceRegistrar resourceRegistrar) {
        this.resourceRegistrar = resourceRegistrar;
        this.patProvider = resourceRegistrar.getPatProvider();
    }

    @Override
    public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException {
        try {
            final HttpHeaders httpHeaders = request.getHttpHeaders();
            if (httpHeaders != null) {
                final List<String> authHeaders = httpHeaders.getRequestHeader("Authorization");
                final List<String> asHeaders = httpHeaders.getRequestHeader("AsHost");
                if (authHeaders != null && !authHeaders.isEmpty()) {
                    final String authorization = authHeaders.get(0);
                    final String rpt = getRptFromAuthorization(authorization);
                    if (!Strings.isNullOrEmpty(rpt)) {
                        LOG.debug("RPT present in request");
                        final RptIntrospectionResponse status = requestRptStatus(rpt);
                        if (status != null && status.getActive()) {
                            request.setAttribute(RPT_STATUS_ATTR_NAME, status);
                            return null;
                        }
                    } else if (asHeaders != null && !asHeaders.isEmpty()) {
                        final String asHost = asHeaders.get(0);
                        // todo : register ticket
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return (ServerResponse) Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        // If the client's request at the protected resource has no RPT,
        // or has an invalid RPT or insufficient authorization data associated with the RPT as determined through
        // RPT status checking (see Section 3.4), then assuming the resource server chooses to respond to the client,
        // it MUST use the protection API's permission registration endpoint to register a requested permission with
        // the corresponding authorization server.
        LOG.debug("Client does not present valid RPT. Registering permission ticket ...");

        return (ServerResponse) registerTicketResponse();
    }

    public static String getRptFromAuthorization(String authorizationHeader) {
        if (StringHelper.isNotEmpty(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring("Bearer ".length());
        }
        return null;
    }

    public RptIntrospectionResponse requestRptStatus(String p_rpt) {
        if (StringUtils.isNotBlank(p_rpt)) {

            LOG.debug("Request RPT status...");
//            final RptStatusService rptStatusService = UmaClientFactory.instance().createRptStatusService(umaConfiguration);
//            final RptIntrospectionResponse status = rptStatusService.requestRptStatus("Bearer " + pat, p_rpt, "");
//            if (status != null) {
//                LOG.debug("RPT status: " + Jackson.asJsonSilently(status));
//                return status;
//            } else {
//                LOG.debug("Unable to retrieve RPT status from AM.");
//            }
        }
        return null;
    }

    public Response registerTicketResponse() {
        return Response.status(Response.Status.UNAUTHORIZED) // todo ticket registration here!!!
                .build();
    }
}

