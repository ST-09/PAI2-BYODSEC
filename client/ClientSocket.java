package client;

import requests.ServerResponse;

import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import javax.net.ssl.*;
import javax.swing.JOptionPane;


public class ClientSocket {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
	try {
		// Obtener las rutas del keystore y truststore desde las propiedades de sistema
		String truststorePath = System.getProperty("javax.net.ssl.trustStore");
		String truststorePassword = System.getProperty("javax.net.ssl.trustStorePassword");

		if (truststorePath == null || truststorePassword == null) {
			System.out.println("Se requiere el keystore y la contraseña como argumentos de la JVM.");
			return;
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
		System.out.println("Conexión establecida de forma segura.");

		// Socket con SSL
		// SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		// SSLSocket socket = (SSLSocket) factory.createSocket("127.0.0.1", 3343);

		// Socket sin SSL
		// Socket socket = new Socket("127.0.0.1", 3343);

		// create PrintWriter for sending login to server
		PrintWriter output = new PrintWriter(new OutputStreamWriter(
					socket.getOutputStream()));

		ClientAuthentication loginFrame = new ClientAuthentication(output);


		// create BufferedReader for reading server response
		BufferedReader input = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

		// read response from server
		ServerResponse response = ServerResponse.recieveDataFromServer(input);

		// display response to user

		if(response.getStatus().equals("success")) {
			JOptionPane.showMessageDialog(null, response.getMessage());

		}
		else{
			JOptionPane.showMessageDialog(null, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}

		if(response.getMessage().equals("Usuario logueado")) {
			loginFrame.closeFrame();
			new ClientMessage(output, response.getUserId());
			// read response from server
			response = ServerResponse.recieveDataFromServer(input);


			System.out.println(response.getStatus());

			// display response to user

			if(response.getStatus().equals("success")) {
				JOptionPane.showMessageDialog(null, response.getMessage());

			}
			else{
				JOptionPane.showMessageDialog(null, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}



		// clean up streams and Socket
		//output.close();
		//input.close();
		//socket.close();

	} // end try

	// handle exception communicating with server
	catch (IOException ioException) {
		ioException.printStackTrace();
	}
	catch (Exception e) {
		e.printStackTrace();
	}

	// exit application
	finally {
		System.exit(0);
	}

    }
}
