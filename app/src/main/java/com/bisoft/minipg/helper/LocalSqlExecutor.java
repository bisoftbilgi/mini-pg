package com.bisoft.minipg.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.bisoft.minipg.PgVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LocalSqlExecutor {

    private static final String    LOCAL_IP = "127.0.0.1";
    private PgVersion pgVersion;



    public List<String> retrieveLocalSqlResult(String sqlString, String localPort, String localUser,
                                               String localPassword) {


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
                log.trace(line);
                cellValues.add(line);
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());

        }

        return cellValues;
    }

    public void executeLocalSql(String sqlString, String localPort, String localUser, String localPassword) {
        log.trace("sql executing:" + sqlString);

        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:" + localPort + "/postgres",
                localUser, localPassword)) {
            Statement stmt = conn.createStatement();

            Class.forName("org.postgresql.Driver");

            stmt.executeUpdate(sqlString);

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());

        }

    }

    public void tryExecuteLocalSql(String sqlString, String localPort, String localUser, String localPassword)
            throws Exception {
        log.trace("sql executing:" + sqlString);

        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:" + localPort + "/postgres",
                localUser, localPassword);
        Statement stmt = conn.createStatement();

        Class.forName("org.postgresql.Driver");

        stmt.execute(sqlString);


    }

    public PgVersion getPgVersion(String localPort, String localUser, String localPassword) {

        if (this.pgVersion != null) {
            return pgVersion;
        }

        String versionText = "";
        // default
        PgVersion result = PgVersion.V10X;
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:" + localPort + "/postgres",
                localUser, localPassword)) {
            Statement stmt = conn.createStatement();

            Class.forName("org.postgresql.Driver");

            ResultSet resultSet = stmt.executeQuery("SELECT version()");

            if (resultSet.next()) {
                versionText = resultSet.getString(1);
                log.trace(versionText);

            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());

        }
        if (versionText.startsWith("PostgreSQL 9.")) {
            result = PgVersion.V9X;
        } else if (versionText.startsWith("PostgreSQL 10.")) {
            result = PgVersion.V10X;
        } else if (versionText.startsWith("PostgreSQL 11.")) {
            result = PgVersion.V11X;
        } else if (versionText.startsWith("PostgreSQL 12.")) {
            result = PgVersion.V12X;
        }else if (versionText.startsWith("PostgreSQL 13.")) {
            result = PgVersion.V13X;
        }else if (versionText.startsWith("PostgreSQL 14.")) {
            result = PgVersion.V14X;
        }
        return result;
    }

}
