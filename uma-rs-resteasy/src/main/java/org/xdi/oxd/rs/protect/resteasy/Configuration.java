package org.xdi.oxd.rs.protect.resteasy;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 19/04/2016
 */

public class Configuration {

    private String umaWellknownEndpoint;
    private String umaPatClientId;
    private String umaPatClientSecret;

    public Configuration() {
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
}
