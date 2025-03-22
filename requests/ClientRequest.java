package requests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ClientRequest {

    private String action;
    private String content;
    private String clientId;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAction() {
        return action;
    }

    public String getContent() {
        return content;
    }

    public RequestHandler(String action, String content) {
        this.action = action;
        this.content = content;
        this.clientId = "defaultClientId";
    }

    public RequestHandler(String action, String content, String clientId) {
        this.action = action;
        this.content = content;
        this.clientId = clientId;
    }

    private void transmitToServer(PrintWriter output) throws IOException {
        output.println(action);
        output.println(content);
        output.println(clientId);
        output.flush();
    }

    public static void sendRequestToServer(String action, String content, PrintWriter output) throws IOException {
        RequestHandler request = new RequestHandler(action, content);
        request.transmitToServer(output);
    }

    public static void sendRequestToServer(String action, String content, String clientId, PrintWriter output) throws IOException {
        RequestHandler request = new RequestHandler(action, content, clientId);
        request.transmitToServer(output);
    }

    public static RequestHandler receiveRequestFromServer(BufferedReader input) throws IOException {
        String action = input.readLine();
        String content = input.readLine();
        String clientId = input.readLine();
        return new RequestHandler(action, content, clientId);
    }
}
