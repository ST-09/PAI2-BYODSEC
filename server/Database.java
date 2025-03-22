package server;

import client.HashUtil;
import requests.User;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Database {

    private Connection connection;

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:server/database.db");
            System.out.println("Conectado a la base de datos SQLite.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Obtener usuario por id
    public User getUserById(Integer userId) {

        String sql = "SELECT * FROM users WHERE id = ?";
        User user = null;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Integer id = rs.getInt("id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                password = HashUtil.hashPassword(password);
                int num_mensajes = rs.getInt("num_mensajes");
                String last_message_date = rs.getString("last_message_date");

                user = new User(id, username, password, num_mensajes, last_message_date);

                System.out.println("Username: " + rs.getString("username"));
            } else {
                System.out.println("No se encontró un usuario con ID " + userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public User getUserByUsername(String username) {
        connectToDatabase();

        String sql = "SELECT * FROM users WHERE username = ?";
        User user = null;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Integer id = rs.getInt("id");
                String userUsername = rs.getString("username");
                String password = rs.getString("password");
                password = HashUtil.hashPassword(password);
                int num_mensajes = rs.getInt("num_mensajes");
                String last_message_date = rs.getString("last_message_date");

                user = new User(id, userUsername, password, num_mensajes, last_message_date);
                System.out.println("Usuario encontrado: " + userUsername);
            } else {
                System.out.println("No se encontró un usuario con username " + username);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeDatabaseConnection();
        }
        return user;
    }


    public boolean createUser(String username, String password) {
        connectToDatabase();

        String sql = "INSERT INTO users (username, password, num_mensajes, last_message_date) VALUES (?, ?, ?, ?)";
        boolean result = false;

        String encryptedPassword = HashUtil.hashPassword(password);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, encryptedPassword);  // ⚠️ Aquí deberías usar hashing de contraseñas
            stmt.setInt(3, 0); // Número inicial de mensajes en 0
            stmt.setString(4, null); // Última fecha de mensaje como NULL


            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                result = true;
                System.out.println("Usuario creado correctamente: " + username);
            } else {
                System.out.println("Error al crear el usuario.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            closeDatabaseConnection();
        }
        return result;
    }

    public boolean login(String username, String password) {

        connectToDatabase();

        String encriptedPassword = HashUtil.hashPassword(password);

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        boolean result = false;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, encriptedPassword);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                result = true;
                System.out.println("Usuario autenticado correctamente: " + username);
            } else {
                System.out.println("Error al autenticar el usuario.");
            }

        } catch (SQLException e) {
            e.printStackTrace();

        }
        finally {
            closeDatabaseConnection();
        }
        return result;
    }

    public boolean registryMessage(String userId) {
        connectToDatabase();

        User user = getUserById(Integer.parseInt(userId));
        if(user == null){
            return false;
        }

        String sql = "UPDATE users SET num_mensajes = ?, last_message_date = ? WHERE id = ?";
        boolean result = false;
        Integer id = Integer.parseInt(userId);
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd--MM--yyyy");
        String fechaComoString = fechaActual.format(formatter);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, user.getNumMessages()+1);
            stmt.setString(2, fechaComoString);
            stmt.setInt(3, id);
            int rowsUpdated = stmt.executeUpdate();  // executeUpdate en lugar de executeQuery

            if (rowsUpdated > 0) {
                System.out.println("Actualización exitosa.");
            } else {
                System.out.println("No se actualizó ninguna fila.");
            }

        } catch (SQLException e) {
            e.printStackTrace();

        }
        finally {
            closeDatabaseConnection();
        }
        return result;

    }


    // Cerrar conexión con SQLite
    private void closeDatabaseConnection() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Conexión cerrada con la base de datos.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
