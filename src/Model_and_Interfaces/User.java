package Model_and_Interfaces;
import Exceptions.UserNotFoundException;
import Exceptions.UserAlreadyExistsException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public abstract class User<T extends User> {
    protected int id;
    protected String name;
    protected String password;
    protected String email;

    // Only used to distinguish user type in code, not stored in DB
    protected String role;

    public User(int id, String name, String password, String email, String role) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public abstract void menu(T user) throws Exception;

    /**
     * Check if admin credentials are valid
     */
    public static boolean isAdminAvailable(Connection connection, String email, String password) {
        String sql = "SELECT COUNT(*) FROM admin WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // true if count > 0
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if client credentials are valid
     */
    public static boolean isClientAvailable(Connection connection, String email, String password) {
        String sql = "SELECT COUNT(*) FROM client WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0; // true if count > 0
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if email already exists in either admin or client table
     */
    public static boolean isEmailTaken(Connection connection, String email) {
        // Check admin table
        String adminSql = "SELECT COUNT(*) FROM admin WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(adminSql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Check client table
        String clientSql = "SELECT COUNT(*) FROM client WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(clientSql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Fetch admin object from database
     */
    public static Admin getAdmin(Connection connection, String email, String password) {
        String sql = "SELECT * FROM admin WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Create a Scanner for potential input
                Scanner scanner = new Scanner(System.in);

                int id = rs.getInt("id");
                String name = rs.getString("name");
                float profit = rs.getFloat("profit");
                String companyname = rs.getString("companyname");

                return new Admin(
                        id,
                        name,
                        password,
                        "admin", // Role is used internally but not stored in DB
                        connection,
                        scanner,
                        profit,
                        companyname
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetch client object from database
     */
    public static Client getClient(Connection connection, String email, String password) {
        String sql = "SELECT * FROM client WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Create a Scanner for potential input
                Scanner scanner = new Scanner(System.in);

                return new Client(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getFloat("balance"),
                        scanner,
                        connection
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Authenticate user and return the appropriate user object
     * @return User object (either Admin or Client) based on successful authentication
     * @throws UserNotFoundException if no matching user found
     */
    public static User getSignedUser(Connection connection, String email, String password) throws UserNotFoundException {
        // Try to get an admin with these credentials
        Admin admin = getAdmin(connection, email, password);
        if (admin != null) {
            return admin;
        }

        // If not admin, try to get a client
        Client client = getClient(connection, email, password);
        if (client != null) {
            return client;
        }

        // No valid user found
        throw new UserNotFoundException("Invalid Credentials");
    }

    /**
     * Sign in user and display appropriate menu
     */
    public static void signin(Connection connection, String email, String password) throws Exception {
        User user = User.getSignedUser(connection, email, password);

        if (user instanceof Admin) {
            Admin adminUser = (Admin) user;
            adminUser.menu(adminUser); // Call the menu method
            // Handle admin-specific functionality
        } else if (user instanceof Client) {
            Client clientUser = (Client) user;
            clientUser.menu(clientUser); // Call the menu method
            // Handle client-specific functionality
        }
    }

    /**
     * Sign up a new user (either Admin or Client)
     * Dispatches to the appropriate signup method based on userType
     *
     * @param connection Database connection
     * @param userType Type of user to create ("admin" or "client")
     * @param name User's name
     * @param password User's password
     * @param email User's email
     * @throws UserAlreadyExistsException If email is already in use
     * @throws SQLException If database operation fails
     */
    public static void signup(Connection connection, String userType, String name, String password, String email)
            throws UserAlreadyExistsException, SQLException {
        // Check if email is already taken
        if (isEmailTaken(connection, email)) {
            throw new UserAlreadyExistsException("Email already in use: " + email);
        }

        if ("admin".equalsIgnoreCase(userType)) {
            // For admin, prompt for company name

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter company name: ");
            String companyName = scanner.nextLine();

            signupAdmin(connection, name, password, email, companyName);
        } else if ("client".equalsIgnoreCase(userType)) {
            // For client, prompt for required information
            Scanner scanner = new Scanner(System.in);

//            System.out.print("Enter admin ID: ");
//            int adminId = scanner.nextInt();
//            scanner.nextLine(); // consume newline

            System.out.print("Enter age: ");
            int age = scanner.nextInt();
            scanner.nextLine(); // consume newline

            System.out.print("Enter gender (Male/Female/Other): ");
            String gender = scanner.nextLine();

            signupClient(connection, name, password, email, age, gender);
        } else {
            throw new IllegalArgumentException("Invalid user type: " + userType);
        }
    }

    /**
     * Sign up a new admin with all required fields
     *
     * @param connection Database connection
     * @param name Admin's name
     * @param password Admin's password
     * @param email Admin's email
     * @param companyName Admin's company name
     * @throws UserAlreadyExistsException If email is already in use
     * @throws SQLException If database operation fails
     */
    public static void signupAdmin(Connection connection, String name, String password, String email,
                                   String companyName)
            throws UserAlreadyExistsException, SQLException {
        // Check if email is already taken
        if (isEmailTaken(connection, email)) {
            throw new UserAlreadyExistsException("Email already in use: " + email);
        }

        // Default profit starts at 0
        double profit = 0.0;

        String sql = "INSERT INTO admin (name, password, email, companyname, profit) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, companyName);
            stmt.setDouble(5, profit);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected <= 0) {
                throw new SQLException("Failed to insert new admin");
            }

            System.out.println("Admin account created successfully!");
        }
    }

    /**
     * Sign up a new client with all required fields
     *
     * @param connection Database connection
     * @param name Client's name
     * @param password Client's password
     * @param email Client's email
     * @param age Client's age
     * @param gender Client's gender (Male/Female/Other)
     * @throws UserAlreadyExistsException If email is already in use
     * @throws SQLException If database operation fails
     */
    public static void signupClient(Connection connection, String name, String password, String email, int age, String gender)
            throws UserAlreadyExistsException, SQLException {
        // Check if email is already taken
        if (isEmailTaken(connection, email)) {
            throw new UserAlreadyExistsException("Email already in use: " + email);
        }

        // Default balance starts at 0
        double balance = 10000.0;

        // Validate gender input
        if (!gender.equals("Male") && !gender.equals("Female") && !gender.equals("Other")) {
            throw new IllegalArgumentException("Gender must be 'Male', 'Female', or 'Other'");
        }

        String sql = "INSERT INTO client (name, password, email, age, gender, balance) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setInt(4, age);
            stmt.setString(5, gender);
            stmt.setDouble(6, balance);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected <= 0) {
                throw new SQLException("Failed to insert new client");
            }

            System.out.println("Client account created successfully!");
        }
    }

    public int getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}