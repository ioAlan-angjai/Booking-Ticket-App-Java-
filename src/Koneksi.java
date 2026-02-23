import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Koneksi {
private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
private static final String USER = "hr";
private static final String PASS = "Tioalan22";


static {
try {
Class.forName("oracle.jdbc.driver.OracleDriver");
} catch (ClassNotFoundException ex) {
System.err.println("Oracle driver tidak ditemukan: " + ex.getMessage());
}
}


public static Connection getConnection() throws SQLException {
return DriverManager.getConnection(URL, USER, PASS);
}
}