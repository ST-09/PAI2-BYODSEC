package securityTests;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class ServerTest {

    private static final int NUM_CLIENTS = 300;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_CLIENTS);
        int failedConnections = 0;

        // Crear las tareas que se ejecutarán simultáneamente
        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < NUM_CLIENTS; i++) {
            Callable<Boolean> task = () -> {
                try {
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
                    socket.close(); // Cerrar la conexión inmediatamente si es exitosa
                    return true; // Conexión exitosa
                } catch (Exception e) {
                    return false; // Fallo en la conexión
                }
            };

            // Guardamos el Future para poder obtener el resultado después
            futures.add(executorService.submit(task));
        }

        // Esperar a que todas las tareas se completen y contar los fallos
        for (Future<Boolean> future : futures) {
            if (!future.get()) {
                failedConnections++;
            }
        }

        // Cerrar el ExecutorService después de completar las tareas
        executorService.shutdown();

        // Calcular el porcentaje de fallos
        double failurePercentage = (failedConnections / (double) NUM_CLIENTS) * 100;

        // Reportar el porcentaje de fallos
        System.out.println("Porcentaje de fallos: " + failurePercentage + "%");
    }
}
