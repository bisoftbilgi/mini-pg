package com.bisoft.minipg.service.util;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocalSqlExecutorTest {
    LocalSqlExecutor sut;
    
    @BeforeEach
    void init() {
        
        this.sut = new LocalSqlExecutor();
    }
    
    public void executeLocalSqlTest() {
        
        sut.executeLocalSql("checkpoint", "5432", "postgres", "080419");
        
    }
    
    public void retrieveLocalSqlTest() {
        
        List<String> result = sut.retrieveLocalSqlResult("select version()", "5432", "postgres", "080419");
        for (String string : result) {
            System.out.println(string);
        }
    }
    
}