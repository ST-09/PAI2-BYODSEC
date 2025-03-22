import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBSetup {
    public static void main(String[] args) {
        String url = "jdbc:sqlite:../securityTeam09BBDD.db";
        String sql = "CREATE TABLE IF NOT EXISTS usuarios (" +
        "username TEXT PRIMARY KEY, " +
        "password TEXT NOT NULL, " +
        "salt TEXT NOT NULL" +
        ");";

        try {
            Class.forName("org.sqlite.JDBC");

            try (Connection conn = DriverManager.getConnection(url);
                 Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                System.out.println("Tabla 'usuarios' verificada/creada correctamente.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
