package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.handler.MinipgUserService;
import com.bisoft.minipg.service.pgwireprotocol.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.MD5Digest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
@Component
@Scope("singleton")
public class Md5Authenticator {

    private final MinipgUserService minipgUserService;

    public boolean authenticate(byte[] hashBytes, byte[] salt) {
        boolean result = minipgUserService.isUserExists(hashBytes, salt);
        return result;
    }
}
//        byte[] password = minipgPassword.getBytes(StandardCharsets.UTF_8);
//        byte[] user = minipgUsername.getBytes(StandardCharsets.UTF_8);
//        byte[] md5Bytes = MD5Digest.encode(user, password, salt);
//
//        if (Arrays.equals(md5Bytes, hashBytes)) {
//            return true;
//        } else {
//            //send unauthorizedpacket
//            return false;
//        }
