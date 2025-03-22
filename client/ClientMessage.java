package client;

import requests.ClientRequest;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
public class ClientMessage {

    private JFrame mainFrame;
    private JTextField inputField;
    private JButton sendMsgButton;
    private JButton signOutButton;

    public ClientMessage(PrintWriter serverOutput, String userIdentifier) {
        mainFrame = new JFrame("Enviar Mensaje");
        mainFrame.setSize(350, 200);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(null);

        // Centrar la ventana
        mainFrame.setLocationRelativeTo(null);

        inputField = new JTextField();
        inputField.setBounds(30, 50, 280, 25);
        mainFrame.add(inputField);

        // Botón para enviar mensaje
        sendMsgButton = new JButton("Enviar mensaje");
        sendMsgButton.setBounds(30, 90, 280, 30);
        mainFrame.add(sendMsgButton);

        // Botón para cerrar sesión
        signOutButton = new JButton("Cerrar sesión");
        signOutButton.setBounds(30, 130, 280, 30);
        mainFrame.add(signOutButton);

        // Acción para enviar mensaje
        sendMsgButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String msg = inputField.getText();

                if (msg.length() > 144) {
                    JOptionPane.showMessageDialog(mainFrame, "El mensaje no puede exceder los 144 caracteres.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (msg.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(mainFrame, "El mensaje no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    ClientRequest.sendDataToServer("sendMessage", msg, userIdentifier, serverOutput);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                inputField.setText(""); // Limpiar campo de texto después de enviar
            }
        });

        // Acción para cerrar sesión
        signOutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    ClientRequest.sendDataToServer("logout", "logout", serverOutput);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(mainFrame, "Sesión cerrada.");
                mainFrame.dispose();
            }
        });

        mainFrame.setVisible(true);
    }
}
