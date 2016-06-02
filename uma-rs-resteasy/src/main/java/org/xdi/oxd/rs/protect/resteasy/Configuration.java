package org.xdi.oxd.rs.protect.resteasy;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 19/04/2016
 */

public class Configuration {

    public static final String WELL_KNOWN_UMA_PATH = "/.well-known/uma-configuration";

    @JsonProperty(value = "well_known_endpoint")
    private String umaWellknownEndpoint;
    @JsonProperty(value = "pat_client_id")
    private String umaPatClientId;
    @JsonProperty(value = "pat_client_secret")
    private String umaPatClientSecret;
    @JsonProperty(value = "trust_all")
    private boolean trustAll;

    public Configuration() {
    }

    public boolean isTrustAll() {
        return trustAll;
    }

    public void setTrustAll(boolean trustAll) {
        this.trustAll = trustAll;
    }

    public String getUmaWellknownEndpoint() {
        return umaWellknownEndpoint;
    }

    public void setUmaWellknownEndpoint(String umaWellknownEndpoint) {
        this.umaWellknownEndpoint = umaWellknownEndpoint;
    }

    public String getUmaPatClientId() {
        return umaPatClientId;
    }

    public void setUmaPatClientId(String umaPatClientId) {
        this.umaPatClientId = umaPatClientId;
    }

    public String getUmaPatClientSecret() {
        return umaPatClientSecret;
    }

    public void setUmaPatClientSecret(String umaPatClientSecret) {
        this.umaPatClientSecret = umaPatClientSecret;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Configuration");
        sb.append("{umaWellKnownEndpoint='").append(umaWellknownEndpoint).append('\'');
        sb.append(", umaPatClientId='").append(umaPatClientId).append('\'');
        sb.append(", umaPatClientSecret='").append(umaPatClientSecret).append('\'');
        sb.append(", trustAll='").append(trustAll).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
