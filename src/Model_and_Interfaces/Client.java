package Model_and_Interfaces;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Date;

public class Client extends User<Client> implements FlightOperations, BookingOperations {
    private Date dateOfBirth;
    private String gender;
    private float balance;
    private Scanner scanner;
    private Connection connection;

    public Client(int id, String name, String password, Date dateOfBirth, String gender, float balance, Scanner scanner, Connection connection) {
        super(id, name, password, name + "@client.com", "client");
        this.connection = connection;
        this.scanner = scanner;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.balance = balance;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public int getAge() {
        // Calculate age from date of birth
        if (dateOfBirth == null) return 0;
        long ageInMillis = new Date().getTime() - dateOfBirth.getTime();
        return (int) (ageInMillis / (1000L * 60 * 60 * 24 * 365));
    }

    public String getGender() {
        return gender;
    }

    public float getBalance() {
        return balance;
    }

    @Override
    public void menu(Client client) throws Exception {
        System.out.println("Welcome Client:-> " + client.name);

        while (true) {
            System.out.println("1.  View Bookings");
            System.out.println("2.  View Flights");
            System.out.println("3.  Book Flight");
            System.out.println("4.  My profile");
            System.out.println("5.  Change password");
            System.out.println("6. Log out");
            System.out.print("Enter: ");

            int option = scanner.nextInt();
            switch (option) {
                case 1:
                    viewBookings(connection, client);
                    break;
                case 2:
                    viewFlights(connection);
                    break;
                case 3:
                    scanner.nextLine();
                    System.out.print("Enter seat type (business/economy): ");
                    String seatType = scanner.nextLine();
                    System.out.print("Enter source: ");
                    String source = scanner.nextLine();
                    System.out.print("Enter destination: ");
                    String destination = scanner.nextLine();
                    bookFlight(connection, scanner, seatType, source, destination, balance, id);
                    break;
                case 4:
                    showProfile();
                    break;
                case 5:
                    scanner.nextLine();
                    System.out.print("Enter current password: ");
                    String currentPassword = scanner.nextLine();

                    System.out.print("Enter new password: ");
                    String newPassword = scanner.nextLine();

                    System.out.print("Confirm new password: ");
                    String confirmPassword = scanner.nextLine();

                    changePassword(currentPassword, newPassword, confirmPassword);
                    break;
                case 6:
                    System.out.println("Logging out");
                    return;
            }
        }
    }

    public void showProfile() {
        try {
            String query = "SELECT * FROM client WHERE id = " + this.id;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String gender = resultSet.getString("gender");
                Date dateOfBirth = resultSet.getDate("date_of_birth");
                System.out.println("Name: " + name + ", Email: " + email + ", Gender: " + gender + ", DOB: " + dateOfBirth);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        ArrayList<Client> clients = getClients(connection);

        for (Client client : clients) {
            if (client.getId() == this.id) {
                if (!client.getPassword().equals(currentPassword)) {
                    System.out.println("Current password is incorrect.");
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    System.out.println("New password and confirmation do not match.");
                    return;
                }

                try {
                    String updateQuery = "UPDATE client SET password = ? WHERE id = ?";
                    PreparedStatement pstmt = connection.prepareStatement(updateQuery);
                    pstmt.setString(1, newPassword);
                    pstmt.setInt(2, this.id);
                    int rowsUpdated = pstmt.executeUpdate();

                    if (rowsUpdated > 0) {
                        System.out.println("Password updated successfully.");
                        this.password = newPassword;
                    } else {
                        System.out.println("Failed to update password.");
                    }
                } catch (SQLException e) {
                    System.out.println("Database error: " + e.getMessage());
                }

                return;
            }
        }

        System.out.println("Client not found.");
    }

    static ArrayList<Client> getClients(Connection connection) {
        ArrayList<Client> clients = new ArrayList<>();
        try {
            String query = "SELECT * FROM client";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            Scanner sc = new Scanner(System.in);
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                Date dateOfBirth = resultSet.getDate("date_of_birth");
                String gender = resultSet.getString("gender");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                float balance = resultSet.getFloat("balance");

                clients.add(new Client(id, name, password, dateOfBirth, gender, balance, sc, connection));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return clients;
    }
}