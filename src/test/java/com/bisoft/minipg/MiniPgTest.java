package com.bisoft.minipg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class MiniPgTest {

    public static void main(String[] args) throws Exception {

        MiniPgTest m = new MiniPgTest();
        m.stopMe();
        m.closeMyConn();
    }

    private Connection conn = null;

    private Connection getConnection() {

        // String url =
        String url = "jdbc:postgresql://192.168.2.20:9998/postgres?user=postgres&password=080419";
//        String url = System.getenv("POSTGRES_JDBC_URL");
        System.out.println("connecting to :" + url);
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (Exception e) {

            e.printStackTrace();
        }
        return conn;
    }

    private void stopMe() throws Exception {

//        String command = "-- pg_remove_sync_slave(server7)";
//      String command = "-- pg_basebackup(192.168.2.90)";
        String command  = "-- pg_status";
        this.conn = getConnection();
        Statement statement = conn.createStatement();
        ResultSet rs        = statement.executeQuery(command);
        while (rs.next()) {
            String cell = rs.getString("result");
            System.out.println(cell);
        }

    }
//    public Connection getConnection(String IP, String port, String username, String dbname, String password)
//            throws Exception {
//
//        connection = null;
//
//        System.out
//                .println("IP::::" + IP + "dbname::::" + dbname + "username::::" + username + "password::::" + password);
//
//        connection = DriverManager.getConnection("jdbc:postgresql://" + IP + ":" + port + "/" + dbname, username,
//                password);
//
//        return connection;
//    }

    public boolean closeMyConn() {

        try {
            conn.close();
            System.out.println("Connection closed");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
//
//    public String testMe() {
//
//        String ip     = "localhost";
//        String passwd = "080419";
//
//        String     statusOfMine = null;
//        Connection conn         = null;
//        try {
////            conn = this.getConnection(ip, "9998", "postgres", "postgres", passwd);
//            conn = this.getConnection();
//            Statement statement = conn.createStatement();
//
//            ResultSet rs = statement.executeQuery("-- pg_start");
//            if (rs.next()) {
//                statusOfMine = statusOfMine + rs.getString("result");
//                System.out.println(statusOfMine + " IP:" + ip);
//            }
//        } catch (Exception ex) {
////                  Logger.getLogger(PostgresService.class.getName()).log(Level.SEVERE, null, ex);
//            System.out.println("ERROR during thestarting of the PG process:{}" + ex.getMessage());
//            return null;
//        } finally {
//            this.closeMyConn();
//        }
//        return statusOfMine;
//    }

}


