import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.*;
import javax.net.ssl.*;

public class Server {

    private static final String DB_URL = "jdbc:sqlite:database.db"; // Ruta a tu base de datos SQLite

    public static void main(String[] args) {
        try {
                        // Configurar el keystore y el truststore
            System.setProperty("javax.net.ssl.keyStore", "mykeystore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "Password123");
            System.setProperty("javax.net.ssl.trustStore", "mytruststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "Password123");
            DBSetup.main(null); // Verifica o crea tabla al iniciar el servidor
            // Crear la conexión SSL con el servidor
            SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(3343, 1000);  // 1000 es el backlog

            // Establecer las Cipher Suites permitidas
            String[] enabledCipherSuites = {
                "TLS_AES_256_GCM_SHA384",   // Más seguro
                "TLS_AES_128_GCM_SHA256"    // Cifrado estándar
            };
            serverSocket.setEnabledCipherSuites(enabledCipherSuites);

            System.err.println("Waiting for connection...");


            while (true) {
                try (SSLSocket socket = (SSLSocket) serverSocket.accept();
                     BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)) {

                    // Leer nombre de usuario y contraseña enviados por el cliente
                    String userName = input.readLine();
                    String password = input.readLine();

                    // Validar credenciales con SQLite
                    if (autenticarUsuario(userName, password)) {
                        output.println("Autenticación exitosa");
                        System.out.println("Autenticación exitosa para el usuario: " + userName);

                        // Leer mensaje del cliente tras la autenticación
                        String msg = input.readLine();
                        System.out.println("Mensaje recibido: " + msg);

                        // Responder según el mensaje recibido
                        if ("Hola".equalsIgnoreCase(msg)) {
                            output.println("Welcome to the Server");
                        } else {
                            output.println("Incorrect message.");
                        }

                    } else {
                        output.println("Error: Usuario o contraseña incorrecta.");
                        System.out.println("Autenticación fallida para el usuario: " + userName);
                    }

                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    // Método para autenticar usuario desde SQLite
    private static boolean autenticarUsuario(String username, String password) {
        String query = "SELECT password, salt FROM usuarios WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPasswordHash = rs.getString("password");
                String salt = rs.getString("salt");
                // Hashear la contraseña proporcionada con el salt almacenado
                String hashedPassword = hashPassword(password, salt);
                return storedPasswordHash.equals(hashedPassword); // Comparación de los hashes
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Método para generar el hash de una contraseña con un salt
    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt.getBytes()); // Añadir el salt al hash
            byte[] hashedBytes = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método para crear un salt único
    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        StringBuilder saltString = new StringBuilder();
        for (byte b : salt) {
            saltString.append(String.format("%02x", b));
        }
        return saltString.toString();
    }

    // Método para registrar un usuario en la base de datos con el salt y el hash de la contraseña
    public static boolean registrarUsuario(String username, String password) {
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);

        String insertQuery = "INSERT INTO usuarios (username, password, salt) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, salt);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
