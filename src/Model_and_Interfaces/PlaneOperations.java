package Model_and_Interfaces;

import java.sql.*;

public interface PlaneOperations {
//            +----------------+--------------+------+-----+---------+----------------+
//            | Field          | Type         | Null | Key | Default | Extra          |
//            +----------------+--------------+------+-----+---------+----------------+
//            | id             | int          | NO   | PRI | NULL    | auto_increment |
//            | admin_id       | int          | YES  | MUL | NULL    |                |
//            | plane_model    | varchar(100) | NO   |     | NULL    |                |
//            | manufacturer   | varchar(100) | YES  |     | NULL    |                |
//            | business_seats | int          | YES  |     | NULL    |                |
//            | economy_seats  | int          | YES  |     | NULL    |                |
//            +----------------+--------------+------+-----+---------+----------------+

    default void addPlane(Connection connection,int id,String planeModel, String manufacturer, int businessSeats, int economySeats) {
        try {
            String query = "INSERT INTO plane (admin_id, plane_model, manufacturer, business_seats, economy_seats) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, id);
            stmt.setString(2, planeModel);
            stmt.setString(3, manufacturer);
            stmt.setInt(4, businessSeats);
            stmt.setInt(5, economySeats);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Plane added successfully!");
            } else {
                System.out.println("Error: Plane not added.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    default void viewPlanes(Connection connection) {
        try {
            String query = "SELECT * FROM plane";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            //Process the ResultSet and display planes
            while(resultSet.next())
            {
                int id = resultSet.getInt("id");
                int admin_id = resultSet.getInt("admin_id");
                String plane_model = resultSet.getString("plane_model");
                String manufacturer = resultSet.getString("manufacturer");
                int business_seats = resultSet.getInt("business_seats");
                int economy_seats = resultSet.getInt("economy_seats");
                System.out.println("ID: " + id + ", Admin ID: " + admin_id + ", Model: " + plane_model +
                        ", Manufacturer: " + manufacturer + ", Business Seats: " + business_seats +
                        ", Economy Seats: " + economy_seats);

            }
        }catch (SQLException e)
        {
            System.out.println(e.getMessage());
        }
    }
    default boolean planeExists(Connection connection, String model, String manufacturer) {
        String sql = "SELECT COUNT(*) FROM plane WHERE plane_model = ? AND manufacturer = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, model);
            stmt.setString(2, manufacturer);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // true if count > 0
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    default  boolean planeExists(Connection connection, int planeId) {
        String sql = "SELECT COUNT(*) FROM plane WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, planeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // true if count > 0
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    default boolean planeBelongsToAdmin(Connection connection, int planeId, int adminId) {
        String sql = "SELECT COUNT(*) FROM plane WHERE id = ? AND admin_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, planeId);
            stmt.setInt(2, adminId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // true if count > 0 (plane belongs to this admin)
            }
        } catch (SQLException e) {
            System.out.println("Error checking plane ownership: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

}
