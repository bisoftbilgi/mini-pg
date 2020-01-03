package com.bisoft.minipg.service.handler;

import com.bisoft.minipg.service.MiniUser;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.MD5Digest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class MinipgUserService {

    HashMap<String, MiniUser> users = new HashMap<String, MiniUser>();

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
        users.put(user.getUserName(), user);
    }

    public boolean isUserExists(byte[] hashBytes, byte[] salt) {

        boolean result = false;

        if (users.size() < 1) {
            addMinipgUser(salt);
        }

        byte[] md5Bytes;
        for (Map.Entry<String, MiniUser> entry : users.entrySet()) {

            MiniUser miniUser = entry.getValue();
            md5Bytes = MD5Digest.encode(miniUser.getPassAndUserMd5(), salt);
            log.debug("===========");
            log.debug("server:" + new String(md5Bytes));
            log.debug("client:" + new String(hashBytes));
            log.debug("===========");

            if (Arrays.equals(md5Bytes, hashBytes)) {
                result = true;
                break;
            }
        }
        log.debug("result:" + result);
        return result;
        }
}
