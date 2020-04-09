package com.bisoft.minipg.service.subservice;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;

//@Service
//@ConfigurationProperties(prefix="server")
//bu şekilde tanimlama yapilabilinir
//@Value("${server.serverList}") gerek kalmadan sadece 
//String serverList; bu sekilde tanimlanabilir
public class ConfigurationService {
    
    @Value("${pg.postgres_path}")
    public String            postgresPath;
    public static Properties properties = new Properties();
    
    // public void initProps() {
    // Properties prop = new Properties();
    // prop.load("app.properties");
    //
    // System.out.prinltn(prop.getProperty("APPNAME"));
    // }
    
    public static String GetValue(String key) {
        
        try {
            properties.load(new FileInputStream("application.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(key);
    }
    
    public static String getValueFromFile(String key, String fileName) {
        
        try {
            properties.load(new FileInputStream("classpath:"+fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(key);
    }
}
