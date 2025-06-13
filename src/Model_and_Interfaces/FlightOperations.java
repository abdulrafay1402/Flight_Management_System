package Model_and_Interfaces;

import Exceptions.InvalidTimeException;
import Exceptions.ValueLessThanZeroException;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Scanner;

public interface FlightOperations {
    default  boolean flightExists(Connection connection, int planeId, String source, String destination, Timestamp arrivalTime, Timestamp reportingTime, double expense) {
        String sql = "SELECT COUNT(*) FROM flight WHERE plane_id = ? AND source = ? AND destination = ? " +
                "AND arrival_time = ? AND reporting_time = ? AND expense = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, planeId);
            stmt.setString(2, source);
            stmt.setString(3, destination);
            stmt.setTimestamp(4, arrivalTime);
            stmt.setTimestamp(5, reportingTime);
            stmt.setDouble(6, expense);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
    default  boolean flightExists(Connection connection, int flightId) {
        String sql = "SELECT COUNT(*) FROM flight WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, flightId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // true if count > 0
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
    default void addFlight(Connection connection,int plane_id, String source, String destination, Timestamp arrival_time, Timestamp reporting_time, float expense) {
        try {
            // ✅ Validate time range
            if (!arrival_time.after(reporting_time)) {
                System.out.println("Error: Arrival time must be after reporting time.");
                return;
            }

            // ✅ Check for overlapping flights
            String checkQuery = "SELECT COUNT(*) FROM flight WHERE plane_id = ? AND (" +
                    "? < arrival_time AND ? > reporting_time OR " +  // New completely covers existing
                    "? BETWEEN reporting_time AND arrival_time OR " + // New reporting_time overlaps
                    "? BETWEEN reporting_time AND arrival_time" +     // New arrival_time overlaps
                    ")";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setInt(1, plane_id);
            checkStmt.setTimestamp(2, reporting_time);
            checkStmt.setTimestamp(3, arrival_time);
            checkStmt.setTimestamp(4, reporting_time);
            checkStmt.setTimestamp(5, arrival_time);

            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int conflictCount = rs.getInt(1);

            if (conflictCount > 0) {
                System.out.println("Error: Plane is already assigned to an overlapping flight.");
                return;
            }

            // ✅ Insert flight
            String insertQuery = "INSERT INTO flight (plane_id, source, destination, arrival_time, reporting_time, expense) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(insertQuery);
            stmt.setInt(1, plane_id);
            stmt.setString(2, source);
            stmt.setString(3, destination);
            stmt.setTimestamp(4, arrival_time);
            stmt.setTimestamp(5, reporting_time);
            stmt.setFloat(6, expense);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Flight added successfully!");
            } else {
                System.out.println("Error: Flight not added.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    default boolean flightBelongsToAdmin(Connection connection, int flightId, int adminId) {
        String sql = "SELECT COUNT(*) FROM flight f JOIN plane p ON f.plane_id = p.id " +
                "WHERE f.id = ? AND p.admin_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, flightId);
            stmt.setInt(2, adminId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // true if count > 0 (flight belongs to this admin)
            }
        } catch (SQLException e) {
            System.out.println("Error checking flight ownership: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    default String updateFlight(Connection connection,int flightId, String field, Scanner scanner) throws Exception{
        /// //////////////////////////////////////////////////////////    UPDATE WALE
        try {
            // Validate field name to avoid SQL injection
            if (!field.matches("source|destination|arrival_time|reporting_time|expense")) {
                return "Invalid field name.";
            }

            // Get current plane_id, arrival_time, and reporting_time for comparison
            String selectQuery = "SELECT plane_id, arrival_time, reporting_time FROM flight WHERE id = ?";
            PreparedStatement selectStmt = connection.prepareStatement(selectQuery);
            selectStmt.setInt(1, flightId);
            ResultSet rs = selectStmt.executeQuery();

            if (!rs.next()) return "Flight not found.";

            int planeId = rs.getInt("plane_id");
            Timestamp originalArrival = rs.getTimestamp("arrival_time");
            Timestamp originalReporting = rs.getTimestamp("reporting_time");

            // New values to update
            Timestamp newArrival = originalArrival;
            Timestamp newReporting = originalReporting;

            String query = "UPDATE flight SET " + field + " = ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);

            switch (field) {
                case "expense":
                    System.out.print("Enter new expense: ");
                    float expense = Float.parseFloat(scanner.nextLine());
                    if(expense<0)
                    {
                        throw new ValueLessThanZeroException("Expense cannot be less than zero");
                    }
                    stmt.setFloat(1, expense);
                    break;

                case "arrival_time":
                case "reporting_time":
                    System.out.println("Enter new " + field.replace("_", " ") + ":");
                    System.out.print("  Hour (0–23): ");
                    int hour = Integer.parseInt(scanner.nextLine());
                    System.out.print("  Minute (0–59): ");
                    int minute = Integer.parseInt(scanner.nextLine());
                    System.out.print("  Second (0–59): ");
                    int second = Integer.parseInt(scanner.nextLine());
                    if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
                        throw new InvalidTimeException("Invalid Time");
                    }

                    LocalDate today = LocalDate.now(); // Assuming same day for simplicity
                    LocalTime newTime = LocalTime.of(hour, minute, second);
                    Timestamp newTimestamp = Timestamp.valueOf(LocalDateTime.of(today, newTime));

                    if (field.equals("arrival_time")) {
                        newArrival = newTimestamp;
                    } else {
                        newReporting = newTimestamp;
                    }

                    // Validate time order
                    if (!newArrival.after(newReporting)) {
                        return "Error: Arrival time must be after reporting time.";
                    }

                    // Check for time overlaps
                    String overlapQuery = "SELECT id FROM flight WHERE plane_id = ? AND id != ? AND NOT (arrival_time <= ? OR reporting_time >= ?)";
                    PreparedStatement overlapStmt = connection.prepareStatement(overlapQuery);
                    overlapStmt.setInt(1, planeId);
                    overlapStmt.setInt(2, flightId);
                    overlapStmt.setTimestamp(3, newReporting);
                    overlapStmt.setTimestamp(4, newArrival);

                    ResultSet overlapRs = overlapStmt.executeQuery();
                    if (overlapRs.next()) {
                        return "Error: Another flight with the same plane overlaps in time.";
                    }

                    stmt.setTimestamp(1, newTimestamp);
                    break;

                default:
                    System.out.print("Enter new value for " + field + ": ");
                    String newValue = scanner.nextLine();
                    stmt.setString(1, newValue);
                    break;
            }

            stmt.setInt(2, flightId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0 ? "Flight updated successfully." : "No flight found with the given ID.";

        } catch (SQLException e) {
            return "SQL Error: " + e.getMessage();
        } catch (Exception e) {
            return "Input Error: " + e.getMessage();
        }
    }
    default void viewFlights(Connection connection) {
        try{
            String query = "SELECT * FROM flight";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            //Process the ResultSet and display planes
            while (resultSet.next())
            {
                int id = resultSet.getInt("id");
                int plane_id = resultSet.getInt("plane_id");
                String source = resultSet.getString("source");
                String destination = resultSet.getString("destination");
                Time arrival_time = resultSet.getTime("arrival_time");
                Time reporting_time = resultSet.getTime("reporting_time");
                float expense = resultSet.getFloat("expense");
                System.out.println("ID: " + id + ", Plane ID: " + plane_id + ", Source: " + source + ", Destination: " + destination + ", Arrival Time: " + arrival_time + ", Reporting Time: " + reporting_time + ", Expense: $" + expense);

            }
        }catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }

}
