package org.xdi.oxd.rs.protect.resteasy;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 19/04/2016
 */

public class Key {

    private String path;
    private String httpMethod;

    public Key() {
    }

    public Key(String path, String httpMethod) {
        this.path = path;
        this.httpMethod = httpMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key = (Key) o;

        if (httpMethod != null ? !httpMethod.equals(key.httpMethod) : key.httpMethod != null) return false;
        if (path != null ? !path.equals(key.path) : key.path != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (httpMethod != null ? httpMethod.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Key");
        sb.append("{path='").append(path).append('\'');
        sb.append(", httpMethod='").append(httpMethod).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
