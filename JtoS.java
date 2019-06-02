import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;

class JtoS {

	public static void main(String[] args) throws ParseException, InterruptedException {

		try {
			while (true) {
				System.out.println(new Date());
				Thread.sleep(120 * 1000);
				String output;
				Map<String, String> hmap = new HashMap<>();
				List<String> outputlist = new ArrayList<>();
				Connection connection = null;
				URL url = new URL("https://ctob-pre.vodafone.com/version");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				// conn.setConnectTimeout(10 * 1000);
				conn.setRequestProperty("Accept", "application/json");

				if (conn.getResponseCode() != 200) {

					try {
						connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:ERDB", "CTOB",
								"CTOB");
						System.out.println("Connection Established");
					} catch (SQLException e) {
						System.out.println("Connection Failed! Check output console");
						return;
					}

					if (connection != null) {
						Statement stmt = connection.createStatement();
						String script = "INSERT INTO CTOB_PI (ID, VERSION, MANIFEST_NAME, BUILD_DATE, ENVIRONMENT, STATUS, TIMESTAMP)  \r\n"
								+ "(Select ID.NEXTVAL,VERSION, MANIFEST_NAME, BUILD_DATE,ENVIRONMENT,'FAIL',SYSDATE \r\n"
								+ "From CTOB_PI where ID IN (SELECT MAX(ID) FROM CTOB_PI) )";
						System.out.println(script);
						stmt.executeQuery(script);
					}

					conn.disconnect();
					throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
				}

				else {

					BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
					while ((output = br.readLine()) != null) {
						outputlist.add(output);
					}

					for (String str : outputlist) {
						String sp[] = str.split(":");
						hmap.put(sp[0].trim(), sp[1].trim());
					}

					try {
						Class.forName("oracle.jdbc.driver.OracleDriver");
					} catch (ClassNotFoundException e) {
						System.out.println("Where is your Oracle JDBC Driver?");
						return;
					}

					System.out.println("Oracle JDBC Driver Registered!");

					try {
						connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:ERDB", "CTOB",
								"CTOB");
						System.out.println("Connection Established");
					} catch (SQLException e) {
						System.out.println("Connection Failed! Check output console");
						return;
					}

					if (connection != null) {
						Statement stmt = connection.createStatement();
						String script = "INSERT INTO CTOB_PI (ID, VERSION, MANIFEST_NAME, BUILD_DATE, ENVIRONMENT, STATUS, TIMESTAMP)"
								+ " VALUES (ID.NEXTVAL, '" + hmap.get("Version") + "', '" + hmap.get("Manifest name")
								+ "', '" + hmap.get("Build date/time") + "', 'PRE', 'SUCCESS', SYSDATE)";
						System.out.println(script);
						stmt.executeQuery(script);
					}

					conn.disconnect();

				}
			}

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
