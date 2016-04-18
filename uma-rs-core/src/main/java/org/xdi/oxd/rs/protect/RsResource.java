package org.xdi.oxd.rs.protect;

import com.google.common.collect.Maps;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 24/12/2015
 */

public class RsResource implements Serializable {

    @JsonProperty(value = "path")
    String path;
    @JsonProperty(value = "conditions")
    List<Condition> conditions;

    private Map<String, List<String>> httpMethodToScopes = null;

    public String getPath() {
        return path;
    }

    public void setPath(String id) {
        this.path = id;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<String> scopes(String httpMethod) {
        if (httpMethodToScopes == null) {
            initMap();
        }
        return httpMethodToScopes.get(httpMethod);
    }

    private void initMap() {
        httpMethodToScopes = Maps.newHashMap();
        if (conditions != null) {
            for (Condition condition : conditions) {
                if (condition.getHttpMethods() != null) {
                    for (String httpMethod : condition.getHttpMethods()) {
                        httpMethodToScopes.put(httpMethod, condition.getScopes());
                    }
                }
            }
        }
    }
}
