package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;

import client.HashUtil;
import requests.ClientRequest;
import requests.ServerResponse;
import requests.User;

import javax.net.ssl.*;

public class Server {

    /**
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException,
            InterruptedException {

        // Base de datos
        Database database = new Database();
        SSLServerSocket serverSocket;

        // Socket con SSL
        // SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        // SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(3343);

        // Socket sin SSL
        // ServerSocket serverSocket = new ServerSocket(3343);

        // Obtener las rutas del keystore y truststore desde las propiedades de sistema
        String keystorePath = System.getProperty("javax.net.ssl.keyStore");
        String keystorePassword = System.getProperty("javax.net.ssl.keyStorePassword");

        // Verificar si las propiedades fueron cargadas correctamente
        if (keystorePath == null || keystorePassword == null) {
            System.out.println("Se requiere el keystore, truststore y sus contraseñas como argumentos de la JVM.");
            return;
        }

        try {
            // Cargar el keystore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (FileInputStream keyStoreFile = new FileInputStream(keystorePath)) {
                keyStore.load(keyStoreFile, keystorePassword.toCharArray());
            }

            // Crear TrustManagerFactory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            if (keyStore != null) {
                trustManagerFactory.init(keyStore);
            } else {
                trustManagerFactory.init(keyStore); // Si no se ha pasado truststore, usar el keystore como truststore
            }

            // Crear KeyManagerFactory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

            // Crear SSLContext con TLS 1.3
            SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            // Crear SSLServerSocketFactory
            SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
            serverSocket = (SSLServerSocket) factory.createServerSocket(3343);
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        while (true) {

            // wait for client connection and check login information
            try {
                System.err.println("Waiting for connection...");

                // Cone

                // Socket socket = serverSocket.accept();

                SSLSocket socket = (SSLSocket) serverSocket.accept();
                System.out.println("Conexión establecida con " + socket.getInetAddress());
                // Habilitar solo TLS_AES_128_GCM_SHA256
                String[] enabledCipherSuites = new String[] {
                        "TLS_AES_128_GCM_SHA256"
                };
                socket.setEnabledCipherSuites(enabledCipherSuites);

                // open BufferedReader for reading data from client
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				// read operation from client
                ClientRequest request = ClientRequest.recieveDataFromServer(input);

                // open PrintWriter for writing data to client
                PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

                String[] tokens = request.getMessage().split(",");

                String username = tokens[0];
                String password = tokens[1];

                if (tokens.length != 2) {
                    ServerResponse.sendDataToClient("error", "Falta el usuario o contraseña", output);
                    output.close();
                    input.close();
                    socket.close();
                    continue;
                }

                // Comprobar si el usuario existe

                User user = database.getUserByUsername(tokens[0]);

				if(request.getOperation().equals("login")) {

                    if(user == null) {
                        ServerResponse.sendDataToClient("error", "El usuario no existe", output);
                        output.close();
                        input.close();
                        socket.close();
                        continue;
                    }

                    boolean loginSucess = database.login(username, password);

                    if(!loginSucess) {
                        ServerResponse.sendDataToClient("error", "La contraseña es incorrecta", output);
                        output.close();
                        input.close();
                        socket.close();
                        continue;
                    }

                    ServerResponse.sendDataToClient("success", "Usuario logueado", String.format("%d", user.getId()),output);
				}
				else if(request.getOperation().equals("register")) {

                    if(user != null) {
                        ServerResponse.sendDataToClient("error", "El usuario ya existe", output);
                        output.close();
                        input.close();
                        socket.close();
                        continue;
                    }

                    // Ingresar usuario en la base de datos
                    boolean success = database.createUser(username, password);

                    if(!success) {
                        ServerResponse.sendDataToClient("error", "Ha ocurrido un error al crear el usuario", output);
                        output.close();
                        input.close();
                        socket.close();
                        continue;
                    }

                    ServerResponse.sendDataToClient("success", "Usuario registrado", output);
				}
				else {
                    ServerResponse.sendDataToClient("error", "Operación invalida", output);
                }

                // read operation from client
                request = ClientRequest.recieveDataFromServer(input);

                if(request.getOperation().equals("logout")){
                    ServerResponse.sendDataToClient("success", "Usuario ha cerrado sesión", output);
                    output.close();
                    input.close();
                    socket.close();
                    continue;
                }
                else if(request.getOperation().equals("message")){
                    if (request.getMessage().length() > 144) {
                        ServerResponse.sendDataToClient("error", "El mensaje supera los 144 caracteres", output);
                        output.close();
                        input.close();
                        socket.close();
                        continue;
                    }
                    database.registryMessage(request.getUserId());
                    ServerResponse.sendDataToClient("success", "Mensaje enviado y recibido correctamente", output);
                }

                output.close();
                input.close();
                socket.close();

            } // end try
            // handle exception communicating with client
            catch (IOException ioException) {
                ioException.printStackTrace();
            }
            catch (Exception e){
                e.printStackTrace();
            }

        } // end while

    }

}
