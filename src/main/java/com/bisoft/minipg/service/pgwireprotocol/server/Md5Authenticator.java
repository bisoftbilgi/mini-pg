package com.bisoft.minipg.service.pgwireprotocol.server;

import com.bisoft.minipg.service.handler.MinipgUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class Md5Authenticator {

    @Autowired
    private MinipgUserService minipgUserService;

    public boolean authenticate(byte[] hashBytes, byte[] salt) {
        boolean result = minipgUserService.isUserExists(hashBytes, salt);
        return result;
    }
}
