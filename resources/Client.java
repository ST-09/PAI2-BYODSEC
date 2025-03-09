import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.*;

public class Client {

    private static final int NUM_CLIENTS = 300;
    private static int successfulClients = 0; // Contador de conexiones exitosas

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_CLIENTS);

        for (int i = 0; i < NUM_CLIENTS; i++) {
            int clientId = i + 1;
            executorService.submit(() -> {
                try {
                    runClient(clientId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        // Esperar a que todas las tareas se completen
        executorService.shutdown();
        
        // Imprimir el número total de clientes que se conectaron correctamente
        while (!executorService.isTerminated()) {
            // Esperar a que el pool de hilos termine
        }
        System.out.println("Número total de clientes conectados exitosamente: " + successfulClients);
    }

    private static void runClient(int clientId) throws IOException {
        try {
            // Establecer la conexión SSL con el servidor
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket("localhost", 3343);

            // Establecer los cifrados permitidos
            String[] enabledCipherSuites = {
                "TLS_AES_256_GCM_SHA384",   
                "TLS_AES_128_GCM_SHA256"
            };
            socket.setEnabledCipherSuites(enabledCipherSuites);

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Usar los usuarios definidos en el servidor
            String[] userNames = {"juanperez", "anagomez", "luismartinez"};
            String[] passwords = {"password123", "secret456", "mypassword789"};

            // Seleccionar aleatoriamente uno de los usuarios definidos
            int userIndex = clientId % userNames.length; // Distribuir entre los usuarios existentes
            String userName = userNames[userIndex];
            String password = passwords[userIndex];

            // Enviar el nombre de usuario y contraseña
            output.println(userName);
            output.println(password);
            output.flush();

            // Leer la respuesta de autenticación
            String authResponse = input.readLine();
            System.out.println("Cliente " + clientId + ": " + authResponse);

            if (authResponse.equals("Autenticación exitosa")) {
                // Si la autenticación fue exitosa, enviar un mensaje
                String msg = "Hola";
                output.println(msg);
                output.flush();

                // Leer la respuesta del servidor
                String messageResponse = input.readLine();
                System.out.println("Cliente " + clientId + ": " + messageResponse);

                // Incrementar el contador de clientes conectados exitosamente
                synchronized (Client.class) {
                    successfulClients++;
                }
            }

            output.close();
            input.close();
            socket.close();

        } catch (IOException e) {
            System.err.println("Error en el cliente " + clientId);
            e.printStackTrace();
        }
    }
}
