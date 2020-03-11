package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.util.MD5Digest;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class Md5Authenticator {

    @Value("${application.minipg.UserName}")
    private String localBioxyUserName;
    @Value("${application.minipg.pwd}")
    private String localBioxyPassword;

    public boolean authenticate(byte[] hashBytes, byte[] salt) {

        byte[] password = localBioxyPassword.getBytes(StandardCharsets.UTF_8);
        byte[] user     = localBioxyUserName.getBytes(StandardCharsets.UTF_8);
        byte[] md5Bytes = MD5Digest.encode(user, password, salt);
        return Arrays.equals(md5Bytes, hashBytes);
    }

}
