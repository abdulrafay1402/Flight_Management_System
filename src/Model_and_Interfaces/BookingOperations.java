package Model_and_Interfaces;

import java.sql.*;
import java.util.Scanner;

public interface BookingOperations {
    default void bookFlight(Connection connection, Scanner scanner, String type, String reqSource, String reqDestination,float balance,int iD) {
        // Using PreparedStatement for security and performance
        String query = "SELECT f.*, p.business_seats, p.economy_seats, p.admin_id, " +
                "(SELECT COUNT(*) FROM booking b WHERE b.flight_id = f.id AND b.isreserved = 1 AND b.seat_type = ?) as booked_seats " +
                "FROM flight f " +
                "JOIN plane p ON f.plane_id = p.id " +
                "WHERE f.source = ? AND f.destination = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            // Set parameters safely to prevent SQL injection
            preparedStatement.setString(1, type.toLowerCase());
            preparedStatement.setString(2, reqSource);
            preparedStatement.setString(3, reqDestination);

            // Execute the query
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                boolean flightsFound = false;
                System.out.println("Available flights from " + reqSource + " to " + reqDestination + ":");
                System.out.println("----------------------------------------------------------");

                // Process the ResultSet and display flights
                while (resultSet.next()) {
                    flightsFound = true;
                    int id = resultSet.getInt("id");
                    int planeId = resultSet.getInt("plane_id");
                    int adminId = resultSet.getInt("admin_id");
                    String source = resultSet.getString("source");
                    String destination = resultSet.getString("destination");
                    Timestamp arrivalTime = resultSet.getTimestamp("arrival_time");
                    Timestamp reportingTime = resultSet.getTimestamp("reporting_time");
                    float expense = resultSet.getFloat("expense");
                    int totalSeats = type.equalsIgnoreCase("business") ?
                            resultSet.getInt("business_seats") :
                            resultSet.getInt("economy_seats");
                    int bookedSeats = resultSet.getInt("booked_seats");
                    int availableSeats = totalSeats - bookedSeats;

                    // Display flight information with available seats info
                    System.out.printf("Flight ID: %-5d | Plane: %-5d | Route: %-10s to %-10s | Arrival: %-19s | Check-in: %-19s | Price: $%.2f | %s seats available: %d%n",
                            id, planeId, source, destination, arrivalTime, reportingTime, expense, type, availableSeats);
                }

                if (!flightsFound) {
                    System.out.println("No flights available for the selected route.");
                    return;
                } else {
                    System.out.println("----------------------------------------------------------");
                    System.out.print("Please enter the flight ID to book (or 0 to cancel): ");
                    int selectedFlightId = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    if (selectedFlightId == 0) {
                        System.out.println("Booking cancelled.");
                        return;
                    }

                    // Book the selected flight
                    bookSelectedFlight(connection,scanner,selectedFlightId, type,balance,iD);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error searching for flights: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }
    default void bookSelectedFlight(Connection connection,Scanner scanner, int flightId, String seatType,float balance,int id) {
        // First verify the flight exists and has available seats
        String checkQuery = "SELECT f.*, f.expense, p.business_seats, p.economy_seats, p.admin_id, " +
                "(SELECT COUNT(*) FROM booking b WHERE b.flight_id = f.id AND b.isreserved = 1 AND b.seat_type = ?) as booked_seats " +
                "FROM flight f " +
                "JOIN plane p ON f.plane_id = p.id " +
                "WHERE f.id = ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setString(1, seatType.toLowerCase());
            checkStmt.setInt(2, flightId);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    float expense = rs.getFloat("expense");
                    int adminId = rs.getInt("admin_id");
                    int totalSeats = seatType.equalsIgnoreCase("business") ?
                            rs.getInt("business_seats") : rs.getInt("economy_seats");
                    int bookedSeats = rs.getInt("booked_seats");

                    // Check if seats are available
                    if (bookedSeats >= totalSeats) {
                        System.out.println("Sorry, no " + seatType + " seats available on this flight.");
                        return;
                    }

                    // Calculate ticket price based on seat type
                    float ticketPrice = expense;
                    if (seatType.equalsIgnoreCase("business")) {
                        ticketPrice *= 1.5; // Business class costs more
                    }

                    // Check if client has sufficient balance
                    if (balance < ticketPrice) {
                        System.out.println("Insufficient balance. Your balance: $" + balance +
                                ", Ticket price: $" + ticketPrice);
                        return;
                    }

                    // All checks passed, proceed with booking
                    connection.setAutoCommit(false); // Start transaction

                    try {
                        // 1. Insert booking record
                        String bookingQuery = "INSERT INTO booking (flight_id, client_id, ispaid, isreserved, fees, seat_type) VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement bookStmt = connection.prepareStatement(bookingQuery)) {
                            bookStmt.setInt(1, flightId);
                            bookStmt.setInt(2, id);
                            bookStmt.setBoolean(3, true); // ispaid
                            bookStmt.setBoolean(4, true); // isreserved
                            bookStmt.setFloat(5, ticketPrice);
                            bookStmt.setString(6, seatType.toLowerCase());
                            bookStmt.executeUpdate();
                        }

                        // 2. Deduct balance from client
                        String updateClientQuery = "UPDATE client SET balance = balance - ? WHERE id = ?";
                        try (PreparedStatement updateClientStmt = connection.prepareStatement(updateClientQuery)) {
                            updateClientStmt.setFloat(1, ticketPrice);
                            updateClientStmt.setInt(2, id);
                            updateClientStmt.executeUpdate();
                        }

                        // 3. Add to admin's profit
                        String updateAdminQuery = "UPDATE admin SET profit = profit + ? WHERE id = ?";
                        try (PreparedStatement updateAdminStmt = connection.prepareStatement(updateAdminQuery)) {
                            updateAdminStmt.setFloat(1, ticketPrice);
                            updateAdminStmt.setInt(2, adminId);
                            updateAdminStmt.executeUpdate();
                        }

                        // 4. Update local balance
                        balance -= ticketPrice;

                        connection.commit(); // Commit transaction
                        System.out.println("Flight booked successfully!");
                        System.out.println("Ticket Details:");
                        System.out.println("Flight ID: " + flightId);
                        System.out.println("Seat Type: " + seatType);
                        System.out.println("Amount Paid: $" + ticketPrice);
                        System.out.println("Remaining Balance: $" + balance);

                    } catch (SQLException e) {
                        connection.rollback(); // Rollback in case of error
                        System.out.println("Error during booking: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        connection.setAutoCommit(true); // Reset auto-commit
                    }
                } else {
                    System.out.println("Flight not found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking flight availability: " + e.getMessage());
            e.printStackTrace();
        }
    }
    default void viewAllBookings(Connection connection) {
        try{
            String query = "SELECT * FROM booking";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            //Process the ResultSet and display planes
            while (resultSet.next())
            {
                int id = resultSet.getInt("id");
                int flight_id = resultSet.getInt("flight_id");
                int client_id = resultSet.getInt("client_id");
                boolean ispaid = resultSet.getBoolean("ispaid");
                boolean isreserved = resultSet.getBoolean("isreserved");
                float fees = resultSet.getFloat("fees");
                System.out.println("ID: " + id + ", Flight ID: " + flight_id + ", Client ID: " + client_id + ", Is Paid: " + ispaid + ", Is Reserved: " + isreserved + ", Fees: $" + fees);
            }
        }catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }
    default void viewBookings(Connection connection,Client client){
        try{
            String query = "SELECT * FROM booking where client_id="+client.id;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            //Process the ResultSet and display planes
            while (resultSet.next())
            {
                int id = resultSet.getInt("id");
                int flight_id = resultSet.getInt("flight_id");
                int client_id = resultSet.getInt("client_id");
                boolean ispaid = resultSet.getBoolean("ispaid");
                boolean isreserved = resultSet.getBoolean("isreserved");
                float fees = resultSet.getFloat("fees");
                System.out.println("ID: " + id + ", Flight ID: " + flight_id + ", Client ID: " + client_id + ", Is Paid: " + ispaid + ", Is Reserved: " + isreserved + ", Fees: $" + fees);
            }
        }catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

}
