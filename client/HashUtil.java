package client;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HashUtil {
    // 🔐 Clave secreta para el HMAC
    private static final String SECRET_KEY = "XA7CZyEL70HBx1hSfV8WKh8dai7WSSGr";
    private static final String HMAC_ALGORITHM = "HmacSHA256"; // Algoritmo HMAC a usar
    private static final String FILE_PATH = "server/user_data.txt"; // Nueva ruta del archivo a proteger
    private static final String HMAC_FILE = "server/hmac_check.txt"; // Nuevo archivo donde se guarda el HMAC

    public static String hashPassword(String contraseña) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(contraseña.getBytes(StandardCharsets.UTF_8));

            // Convertimos los bytes a un string hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar el hash SHA-256", e);
        }
    }

    public static String createNonce(){
        return UUID.randomUUID().toString();
    }

    public static String createHmacMessage(String message) {
        try {
            // Crear una clave secreta para HMAC
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), HMAC_ALGORITHM);

            // Inicializar el algoritmo HMAC con la clave secreta
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(secretKeySpec);

            byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            // Convertir a Base64 para facilitar la lectura y almacenamiento
            return Base64.getEncoder().encodeToString(hmacBytes);
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateHMACFile(String filePath, String secretKey) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), HMAC_ALGORITHM);
            mac.init(secretKeySpec);

            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            byte[] hmacBytes = mac.doFinal(fileBytes);

            return Base64.getEncoder().encodeToString(hmacBytes); // Se usa Base64 para facilitar su almacenamiento
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkUsersIntegrity () {

        String oldValue = "";
        try (BufferedReader br = new BufferedReader(new FileReader(HMAC_FILE))) {
            oldValue = br.readLine(); // Lee la primera línea del archivo
        } catch (IOException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
        }

        String actualValue = generateHMACFile(FILE_PATH, SECRET_KEY);

        return oldValue.equals(actualValue);
    }

    public static void saveNewIntegrityValue(){
        String actualValue = generateHMACFile(FILE_PATH, SECRET_KEY);
        try (FileWriter fw = new FileWriter(HMAC_FILE, false)) {
            fw.write(actualValue);
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        saveNewIntegrityValue();
    }
}
