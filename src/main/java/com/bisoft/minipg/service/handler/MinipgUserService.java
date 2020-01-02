package com.bisoft.minipg.service.handler;

import com.bisoft.minipg.service.MiniUser;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.MD5Digest;
import org.springframework.beans.factory.annotation.Value;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
public class MinipgUserService {

    @Value("${minipg.username}")
    private String minipgUsername;
    @Value("${minipg.password}")
    private String minipgPassword;

    private void addMinipgUser(byte[] salt) {

        byte[] passwordByte = minipgPassword.getBytes(StandardCharsets.UTF_8);
        byte[] userByte = minipgUsername.getBytes(StandardCharsets.UTF_8);
        byte[] md5Bytes = MD5Digest.initialDigest(userByte, passwordByte);
        MiniUser user = new MiniUser();
        user.setPassAndUserMd5(md5Bytes);
    }

    public boolean isUserExists(byte[] hashBytes, byte[] salt) {

        boolean result = false;
        byte[] md5Bytes;

        MiniUser miniUser = new MiniUser();
        md5Bytes = MD5Digest.encode(miniUser.getPassAndUserMd5(), salt);

        if (Arrays.equals(md5Bytes, hashBytes)) {
            result = true;
        } else {
            //send unauthorizedpacket
        }
        return result;
    }
}
