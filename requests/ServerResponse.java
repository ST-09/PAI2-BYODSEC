package requests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ServerResponse {

    private String status;
    private String message;
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public ServerResponse(String operation, String message) {
        this.status = operation;
        this.message = message;
    }

    public ServerResponse(String operation, String message, String userId) {
        this.status = operation;
        this.message = message;
        this.userId = userId;
    }

    private void sendToClient(PrintWriter output) throws IOException {
        output.println(status);
        output.println(message);
        output.println(userId);
        output.flush();
    }

    public static void sendDataToClient(String status, String message, PrintWriter output) throws IOException {
        ServerResponse request = new ServerResponse(status, message);
        request.sendToClient(output);
    }

    public static void sendDataToClient(String status, String message, String userId, PrintWriter output) throws IOException {
        ServerResponse request = new ServerResponse(status, message, userId);
        request.sendToClient(output);
    }

    public static ServerResponse recieveDataFromServer(BufferedReader input) throws IOException {
        String status = input.readLine();
        String message = input.readLine();
        String userId = input.readLine();
        return new ServerResponse(status, message, userId);
    }

}

