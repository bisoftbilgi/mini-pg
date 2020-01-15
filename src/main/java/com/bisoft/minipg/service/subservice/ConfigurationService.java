package com.bisoft.minipg.service.subservice;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;

//@Service
//@ConfigurationProperties(prefix="server")
// please use as the following example!!!
// this service is closed to use!!!
// declare and use like this:
// @Value("${server.serverList}")
// String serverList;
@Deprecated
public class ConfigurationService {

    @Value("${pg.postgres_path}")
    public        String     postgresPath;
    public static Properties properties = new Properties();

    @Deprecated
    public static String GetValue(String key) {

        try {
            properties.load(new FileInputStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(key);
    }

}
