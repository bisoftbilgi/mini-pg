package com.bisoft.minipg.service.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LocalSqlExecutor {

    private static final String LOCAL_IP = "127.0.0.1";

    public List<String> retrieveLocalSqlResult(String sqlString, String localPort, String localUser,
                                               String localPassword) {

        System.out.println("sql executing:" + sqlString);
        log.trace("sql executing:" + sqlString);

        List<String> cellValues = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:" + localPort + "/postgres",
            localUser, localPassword)) {
            Statement stmt = conn.createStatement();

            Class.forName("org.postgresql.Driver");

            ResultSet result = stmt.executeQuery(sqlString);
            String    line;
            while (result.next()) {
                line = result.getString(1);
                System.out.println(line);
                log.trace(line);
                cellValues.add(line);
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);

        }

        return cellValues;
    }

    public void executeLocalSql(String sqlString, String localPort, String localUser, String localPassword) {

        System.out.println("sql executing:" + sqlString);
        log.trace("sql executing:" + sqlString);

        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:" + localPort + "/postgres",
            localUser, localPassword)) {
            Statement stmt = conn.createStatement();

            Class.forName("org.postgresql.Driver");

            stmt.executeUpdate(sqlString);

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

    }

//    public List<String> executeLocalSqlReturnList(String sqlString, String localPort, String localUser, String localPassword) {
//
//        System.out.println("sql executing:" + sqlString);
//        log.trace("sql executing:" + sqlString);
//        List<String> result = new ArrayList<>();
//
//        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:" + localPort + "/postgres",
//            localUser, localPassword)) {
//            Statement stmt = conn.createStatement();
//
//            Class.forName("org.postgresql.Driver");
//            ResultSet rs = stmt.executeQuery(sqlString);
//            while (rs.next()) {
//                result.add(rs.getString(0));
//            }
//
//        } catch (Exception e) {
//            System.err.println(e.getClass().getName() + ": " + e.getMessage());
//            System.exit(0);
//        }
//
//        return result;
//    }

}
