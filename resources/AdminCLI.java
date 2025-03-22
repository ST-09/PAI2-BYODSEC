import java.util.Scanner;

public class AdminCLI {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Introduce nuevo nombre de usuario: ");
        String username = sc.nextLine();
        System.out.print("Introduce contraseña: ");
        String password = sc.nextLine();

        Server.registrarUsuario(username, password);
        System.out.println("Usuario registrado correctamente.");
    }
}

