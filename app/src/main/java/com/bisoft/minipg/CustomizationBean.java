package com.bisoft.minipg;

import com.bisoft.minipg.helper.SymmetricEncryptionUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

@Component
@Data
@Slf4j
@RequiredArgsConstructor
public class CustomizationBean implements
        WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private final SymmetricEncryptionUtil symmetricEncryptionUtil;

    @Value("${minipg.port:9998}")
    private Integer port;

    @Value("${bfm.user-crypted:false}")
    public boolean isEncrypted;

    @Value("${minipg.tls-key-alias:bfm}")
    private String tlsKeyAlias;

    @Value("${minipg.use-tls:false}")
    private boolean useSsl;

    @Value("${minipg.tls-secret:changeit}")
    private String tlsSecret;

    @Value("${minipg.tls-key-store-type:PKCS12}")
    private String tlsKeyStoreType;

    @Value("${minipg.tls-key-store:bfm.p12}")
    private String tlsKeyStore;



    @Override
    public void customize(ConfigurableServletWebServerFactory container) {
        container.setPort(port);

        if(useSsl){
            Ssl ssl = new Ssl();
            ssl.setEnabled(true);
            if(isEncrypted){
              //  log.info(symmetricEncryptionUtil.decrypt(tlsSecret).replace("=",""));
                ssl.setKeyStorePassword(symmetricEncryptionUtil.decrypt(tlsSecret).replace("=",""));
            }else{
                ssl.setKeyStorePassword(tlsSecret);
            }
            ssl.setKeyAlias(tlsKeyAlias);
            ssl.setKeyStoreType(tlsKeyStoreType);
            ssl.setKeyStore(tlsKeyStore);
            ssl.setEnabledProtocols(new String[]{"TLSv1.2"});

            //container.set
            container.setSsl(ssl);
        }
    }
}