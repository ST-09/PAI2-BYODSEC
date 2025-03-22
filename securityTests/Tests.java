package securityTests;

import client.HashUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Tests {

    public static void main(String[] args) {
        test_nonce_used();
        test_changing_message();
    }

    public static void test_nonce_used() {
        String operation = "transfer";
        String message = "123, 123, 123";
        String nonce = "5658f805-d68a-4ed1-9cd0-d7686564794d";

        String hmac = HashUtil.createHmacMessage(nonce+message);
        try{
            Socket socket = new Socket("127.0.0.1", 3343);

            PrintWriter output = new PrintWriter(new OutputStreamWriter(
                    socket.getOutputStream()));
            output.println(operation);
            output.println(message);
            output.println(nonce);
            output.println(hmac);
            output.flush();

            BufferedReader input = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            // read response from server
            String statusCode = input.readLine();
            message = input.readLine();

            assert statusCode.equals("error");
            assert message.equals("El nonce ya ha sido usado");
            System.out.println(message);
            output.close();
            socket.close();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void test_changing_message() {
        String operation = "transfer";
        String message = "123, 123, 123";
        String messageModified = "222, 222, 222";
        String nonce = "5658f805-d68a-4ed1-9cd0-d7686564794d";

        String hmac = HashUtil.createHmacMessage(nonce+message);
        try{
            Socket socket = new Socket("127.0.0.1", 3343);

            PrintWriter output = new PrintWriter(new OutputStreamWriter(
                    socket.getOutputStream()));
            output.println(operation);
            output.println(messageModified);
            output.println(nonce);
            output.println(hmac);
            output.flush();

            BufferedReader input = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            // read response from server
            String statusCode = input.readLine();
            message = input.readLine();

            assert statusCode.equals("error");
            assert message.equals("El mensaje ha perdido la integridad");
            System.out.println(message);
            output.close();
            socket.close();

        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
