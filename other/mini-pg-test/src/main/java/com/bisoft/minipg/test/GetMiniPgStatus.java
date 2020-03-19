package com.bisoft.minipg.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class GetMiniPgStatus

{

	public static void main(String args[])

	{

		Connection conn = null;
		Statement stmt = null;
		String sqlString = null;

		try

		{

			Class.forName("org.postgresql.Driver");
//			conn = DriverManager.getConnection("jdbc:postgresql://165.227.19.96:9998/postgres", "postgres",
			conn = DriverManager.getConnection("jdbc:postgresql://138.68.9.190:9998/postgres", "postgres",
//			conn = DriverManager.getConnection("jdbc:postgresql://134.209.96.234:9998/postgres", "postgres",
//			conn = DriverManager.getConnection("jdbc:postgresql://localhost:9998/postgres", "postgres",
					"080419");
			System.out.println("Database Connected ..");
			stmt = conn.createStatement();
//			sqlString = "-- minipg_status";
			sqlString = "-- pg_stop";
//			sqlString = "-- pg_start";
//			
//			sqlString = "-- pg_status";
//			sqlString = "-- pg_promote";
			ResultSet result = stmt.executeQuery(sqlString);
			while (result.next()) {

				System.out.println(result.getString(1));

			}
			stmt.close();
			conn.close();
		}

		catch (Exception e)

		{
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);

		}

	}

}