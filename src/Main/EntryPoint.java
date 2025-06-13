package Main;

import Exceptions.UserAlreadyExistsException;
import Model_and_Interfaces.User;
import Exceptions.UserNotFoundException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class EntryPoint implements ConnectionDB {
    private Connection connection;
    private Scanner scanner;
    @Override
    public void Entry(String url, String username, String password) throws SQLException {
        this.connection = DriverManager.getConnection(url, username, password);
    }
    public void startApplication() throws SQLException {
        // First try with custom credentials, fall back to interface defaults if needed
        try {
            Entry(url, username, password); // Try with instance variables (from interface)
        } catch (SQLException e) {
            // Fall back to interface constants if instance connection fails
            Entry(ConnectionDB.url, ConnectionDB.username, ConnectionDB.password);
        }

        scanner = new Scanner(System.in);
        runMainLoop();
        cleanup();
    }

    private void runMainLoop() {
        while (true) {
            System.out.print("\nDo you want to signup, signin, or exit? (signup/signin/exit): ");
            String choice = scanner.nextLine().trim().toLowerCase();

            if (choice.equals("exit")) {
                System.out.println("Exiting program. Goodbye!");
                break;
            }

            try {
                switch (choice) {
                    case "signup":
                        handleSignup();
                        break;
                    case "signin":
                        handleSignin();
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter 'signup', 'signin', or 'exit'.");
                }
            } catch (UserNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("An unexpected error occurred: ");
                e.printStackTrace();
            }
        }
    }

    private void handleSignup() throws SQLException, UserNotFoundException {
        System.out.print("Enter usertype (admin/client): ");
        String usertype = scanner.nextLine();

        System.out.print("Enter name: ");
        String name = scanner.nextLine();

        System.out.print("Enter password: ");
        String signupPassword = scanner.nextLine();

        String signupEmail = usertype.equals("client")
                ? name + "@client.com"
                : name + "@ats.com";

        try {
            User.signup(connection, usertype, name, signupPassword, signupEmail);
        } catch (UserAlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Signup successful!");
    }

    private void handleSignin() throws SQLException, UserNotFoundException {
        System.out.print("Enter email: ");
        String signinEmail = scanner.nextLine();

        System.out.print("Enter password: ");
        String signinPassword = scanner.nextLine();

        User user = null;
        try {
            User.signin(connection, signinEmail, signinPassword);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Welcome, " + user.getName() + "! You are logged in as " + user.getRole());
    }

    private void cleanup() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            if (scanner != null) {
                scanner.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
}