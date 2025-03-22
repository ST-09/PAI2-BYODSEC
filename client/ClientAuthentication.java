package client;

import requests.HashUtil;
import requests.ClientRequest;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

public class ClientAuthentication {
    private JFrame frame;
    private JTextField userField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public ClientAuthentication(PrintWriter output) {
        frame = new JFrame("Autenticación de Cliente");
        frame.setSize(300, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);

        // Centrar la ventana
        frame.setLocationRelativeTo(null);

        // Campo de usuario
        JLabel userLabel = new JLabel("Usuario:");
        userLabel.setBounds(50, 20, 80, 25);
        frame.add(userLabel);

        userField = new JTextField();
        userField.setBounds(130, 20, 120, 25);
        frame.add(userField);

        // Campo de contraseña
        JLabel passwordLabel = new JLabel("Contraseña:");
        passwordLabel.setBounds(50, 60, 80, 25);
        frame.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(130, 60, 120, 25);
        frame.add(passwordField);

        // Botón de iniciar sesión
        loginButton = new JButton("Iniciar sesión");
        loginButton.setBounds(50, 100, 200, 30);
        frame.add(loginButton);

        // Botón de registrarse
        registerButton = new JButton("Registrarse");
        registerButton.setBounds(50, 140, 200, 30);
        frame.add(registerButton);

        // Acción del botón de iniciar sesión
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String usuario = userField.getText();
                String contrasena = new String(passwordField.getPassword());
                String message = String.format(usuario + "," + contrasena);
                try {
                    ClientRequest.sendDataToServer("login", message, output);
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Acción del botón de registrarse
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String usuario = userField.getText();
                String contraseña = new String(passwordField.getPassword());
                String message = String.format(usuario + "," + contraseña);
                try {
                    ClientRequest.sendDataToServer("register", message, output);
                }
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.setVisible(true);
    }

    public void closeFrame() {
        frame.dispose(); // Cierra la ventana
    }
}
