package securityTests;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class ConnectionTest {

    private static final String SERVER_HOST = "localhost"; // Cambiar por la IP o el servidor que desees probar
    private static final int SERVER_PORT = 3343;
    private static final int NUM_CLIENTS = 150;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        testSSL();
        // testNoSSL();
    }

    private static void testNoSSL() throws InterruptedException, ExecutionException  {
        /// Medir el tiempo de conexión sin SSL
        long startTimeNoSSL = System.currentTimeMillis();
        testConnection(false);
        long endTimeNoSSL = System.currentTimeMillis();
        System.out.println("Tiempo de conexión sin SSL: " + (endTimeNoSSL - startTimeNoSSL) + "ms");
    }

    private static void testSSL() throws InterruptedException, ExecutionException  {
        // Medir el tiempo de conexión con SSL
        long startTimeSSL = System.currentTimeMillis();
        testConnection(true);
        long endTimeSSL = System.currentTimeMillis();
        System.out.println("Tiempo de conexión con SSL: " + (endTimeSSL - startTimeSSL) + "ms");
    }

    private static void testConnection(boolean useSSL) {
        long totalTime = 0;

        // Realizar la conexión de forma secuencial
        for (int i = 0; i < NUM_CLIENTS; i++) {
            System.out.println("Cliente:" + i);
            long start = System.currentTimeMillis();
            try {
                if (useSSL) {
                    // Conexión SSL
                    // Intentamos conectarnos al servidor en el puerto 3374
                    // Obtener las rutas del keystore y truststore desde las propiedades de sistema
                    String truststorePath = System.getProperty("javax.net.ssl.trustStore");
                    String truststorePassword = System.getProperty("javax.net.ssl.trustStorePassword");

                    if (truststorePath == null || truststorePassword == null) {
                        System.out.println("Se requiere el keystore y la contraseña como argumentos de la JVM.");
                    }

                    // Cargar el keystore
                    KeyStore keyStore = KeyStore.getInstance("JKS");
                    try (FileInputStream keyStoreFile = new FileInputStream(truststorePath)) {
                        keyStore.load(keyStoreFile, truststorePassword.toCharArray());
                    }

                    // Crear KeyManagerFactory
                    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    keyManagerFactory.init(keyStore, truststorePassword.toCharArray());

                    // Si se ha proporcionado un truststore, cargarlo (opcional)
                    KeyStore trustStore = null;
                    if (truststorePath != null && truststorePassword != null) {
                        trustStore = KeyStore.getInstance("JKS");
                        try (FileInputStream trustStoreFile = new FileInputStream(truststorePath)) {
                            trustStore.load(trustStoreFile, truststorePassword.toCharArray());
                        }
                    }

                    // Crear TrustManagerFactory
                    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    if (trustStore != null) {
                        trustManagerFactory.init(trustStore);
                    } else {
                        trustManagerFactory.init(keyStore); // Si no se ha pasado truststore, usar el keystore como truststore
                    }

                    // Crear SSLContext con TLS 1.3
                    SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
                    sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

                    // Crear el socket SSL
                    SSLSocketFactory socketFactory = sslContext.getSocketFactory();
                    SSLSocket socket = (SSLSocket) socketFactory.createSocket("127.0.0.1", 3343);

                    // Habilitar solo TLS_AES_128_GCM_SHA256
                    String[] enabledCipherSuites = new String[] {
                            "TLS_AES_128_GCM_SHA256"
                    };
                    socket.setEnabledCipherSuites(enabledCipherSuites);

                    // Iniciar el handshake SSL
                    socket.startHandshake();
                    socket.close();
                } else {
                    // Conexión sin SSL
                    try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
                        // Conexión exitosa
                        socket.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            totalTime += (System.currentTimeMillis() - start);
        }

        System.out.println("Tiempo promedio de conexión (" + (useSSL ? "SSL" : "No SSL") + "): " + (totalTime / NUM_CLIENTS) + "ms");
    }
}
