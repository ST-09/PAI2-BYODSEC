package utils;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.apache.commons.lang3.RandomStringUtils;  

public class claveCifrado {

    // Método para generar una clave de cifrado AES y guardarla en un archivo
    public static void generateAndSaveKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128); // Tamaño de la clave (en bits)
        SecretKey secretKey = keyGenerator.generateKey();
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("encryption_key.key"))) {
            oos.writeObject(secretKey);
        }
    }

    // Cargar la clave de cifrado desde el archivo
    public static SecretKey loadEncryptionKey() throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("encryption_key.key"))) {
            return (SecretKey) ois.readObject();
        }
    }

    // Método para cifrar el texto con la clave proporcionada
    public static String encrypt(String plainText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // Método para desencriptar el texto con la clave proporcionada
    public static String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes);
    }

    // Método para generar una contraseña aleatoria
    public static String generateRandomPassword(int length) {
        return RandomStringUtils.randomAlphanumeric(length);  // Genera una contraseña aleatoria
    }

    // Método para insertar un nuevo usuario con la contraseña cifrada en la base de datos
    public static void insertUser(String username) throws Exception {
        String randomPassword = generateRandomPassword(10);  // Generar una contraseña aleatoria de 10 caracteres
        SecretKey secretKey = loadEncryptionKey();  // Cargar la clave de cifrado

        // Cifrar la contraseña antes de guardarla
        String encryptedPassword = encrypt(randomPassword, secretKey);

        // Conectar a la base de datos SQLite y guardar el usuario y la contraseña cifrada
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:database.db")) {
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, encryptedPassword);
                stmt.executeUpdate();
            }
        }

        // Guardar la contraseña generada y cifrada en un archivo
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("user_passwords.txt", true))) {
            writer.write("Username: " + username + ", Password: " + randomPassword + "\n");
        }
    }

    // Método para autenticar usuario usando la contraseña cifrada
    public static boolean authenticateUser(String username, String password) throws Exception {
        SecretKey secretKey = loadEncryptionKey();  // Cargar la clave de cifrado

        // Conectar a la base de datos SQLite y recuperar la contraseña cifrada
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:database.db")) {
            String sql = "SELECT password FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String encryptedPassword = rs.getString("password");
                        // Desencriptar la contraseña almacenada y compararla con la ingresada
                        String decryptedPassword = decrypt(encryptedPassword, secretKey);
                        return password.equals(decryptedPassword);
                    }
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        try {
            // Generar y guardar la clave de cifrado (solo una vez)
            generateAndSaveKey();

            // Insertar usuarios con contraseñas aleatorias
            insertUser("user1");
            insertUser("user2");

            // Intentar autenticar al usuario
            String username = "user1";
            String password = "password123"; // Cambia esto por la contraseña generada
            if (authenticateUser(username, password)) {
                System.out.println("Autenticación exitosa.");
            } else {
                System.out.println("Autenticación fallida.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
