package org.xdi.oxd.rs.protect.resteasy;

import org.apache.log4j.Logger;
import org.xdi.oxd.rs.protect.Jackson;

import java.io.InputStream;

/**
 * @author Yuriy Zabrovarnyy
 * @version 0.9, 20/04/2016
 */

public class ConfigurationLoader {

    private static final Logger LOG = Logger.getLogger(ConfigurationLoader.class);

    public static Configuration loadFromJson(InputStream inputStream) {
        try {
            try {
                return Jackson.createJsonMapper().readValue(inputStream, Configuration.class);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            return null;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }
}
