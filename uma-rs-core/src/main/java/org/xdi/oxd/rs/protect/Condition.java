package org.xdi.oxd.rs.protect;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 18/04/2016
 */

public class Condition {

    @JsonProperty(value = "httpMethods")
    List<String> httpMethods;
    @JsonProperty(value = "scopes")
    List<String> scopes;
    @JsonProperty(value = "ticketScopes")
    List<String> ticketScopes;

    public List<String> getTicketScopes() {
        return ticketScopes;
    }

    public void setTicketScopes(List<String> ticketScopes) {
        this.ticketScopes = ticketScopes;
    }

    public List<String> getHttpMethods() {
        return httpMethods;
    }

    public void setHttpMethods(List<String> httpMethods) {
        this.httpMethods = httpMethods;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Condition");
        sb.append("{httpMethods=").append(httpMethods);
        sb.append(", scopes=").append(scopes);
        sb.append(", ticketScopes=").append(ticketScopes);
        sb.append('}');
        return sb.toString();
    }
}
