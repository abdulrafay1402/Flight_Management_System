package GUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;

public class ClientDashboard {
    private static final Color BACKGROUND_COLOR = new Color(220, 240, 255); // Light blue
    private static final Color BUTTON_HOVER_COLOR = Color.WHITE;
    private static final Color BUTTON_TEXT_COLOR = new Color(70, 130, 180); // Steel Blue
    private static final Font BUTTON_FONT = new Font("Poppins", Font.PLAIN, 18);
    private static final Font TITLE_FONT = new Font("Poppins", Font.BOLD, 24);
    private static final Font VALUE_FONT = new Font("Poppins", Font.BOLD, 24);
    private static final Font LABEL_FONT = new Font("Poppins", Font.PLAIN, 18);

    private JFrame frame;
    private JPanel centerPanel; // Center panel for cards
    private JButton changePasswordButton;
    private Connection connection;
    private int clientId;
    private String clientName;
    private String clientEmail;
    private int clientAge;
    private String clientGender;
    private JTable bookingsTable;
    private JTable flightsTable;


    // Constructor to initialize the dashboard
    public ClientDashboard(int clientId, String clientName, String clientEmail,int Age,String Gender) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientEmail = clientEmail;
        this.clientAge=Age;
        this.clientGender=Gender;

        try {
            // Initialize database connection
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ats", "root", "abcd1234");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        initializeDashboard();
    }

    // Initialize the dashboard components
    private void initializeDashboard() {
        frame = new JFrame("Client Dashboard");
        frame.setLayout(new BorderLayout());

        // Add left and right panels
        frame.add(createLeftPanel(), BorderLayout.WEST);
        frame.add(createRightPanel(), BorderLayout.CENTER);

        // Set frame properties
        frame.setSize(840, 600); // Set the frame size to accommodate both panels and image
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Create the left panel
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(BACKGROUND_COLOR); // Light Blue background
        leftPanel.setPreferredSize(new Dimension(250, 400));
        leftPanel.setLayout(new BorderLayout());

        // Add image/logo to the top of the left panel
        JLabel imageLabel = createLogo("Clogo.png");
        leftPanel.add(imageLabel, BorderLayout.NORTH);

        // Add navigation buttons to the center of the left panel
        JPanel leftNavPanel = createLeftNavigationButtons();
        leftPanel.add(leftNavPanel, BorderLayout.CENTER);

        // Add logout button at the bottom of the left panel
        JButton logoutButton = createStyledLogoutButton("Logout", e -> logout());
        logoutButton.setPreferredSize(new Dimension(220, 60));
        JPanel logoutPanel = new JPanel();
        logoutPanel.setBackground(BACKGROUND_COLOR);
        logoutPanel.add(logoutButton);
        leftPanel.add(logoutPanel, BorderLayout.SOUTH);

        return leftPanel;
    }

    // Create the left navigation buttons
    private JPanel createLeftNavigationButtons() {
        JPanel leftNavPanel = new JPanel();
        leftNavPanel.setBackground(BACKGROUND_COLOR); // Light blue background
        leftNavPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 15));

        // Create buttons for navigation
        JButton viewBookingsButton = createStyledButton("View My Bookings", e -> switchCard("BOOKINGS"));
        JButton viewFlightsButton = createStyledButton("View Flights", e -> switchCard("FLIGHTS"));
        JButton bookFlightButton = createStyledButton("Book Flight", e -> switchCard("BOOK"));
        JButton profileButton = createStyledButton("My Profile", e -> {
            switchCard("PROFILE");
            if (changePasswordButton != null) {
                changePasswordButton.setVisible(true); // Show the "Change Password" button after viewing the profile
            }
        });

        // Add buttons to the left navigation panel
        leftNavPanel.add(viewBookingsButton);
        leftNavPanel.add(viewFlightsButton);
        leftNavPanel.add(bookFlightButton);
        leftNavPanel.add(profileButton);

        return leftNavPanel;
    }

    // Create the right panel
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);

        // Create the center panel for cards
        centerPanel = new JPanel();
        centerPanel.setBackground(BACKGROUND_COLOR); // Light blue background for consistency
        centerPanel.setLayout(new CardLayout());
        centerPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 40, true));

        // Add cards to the center panel
        centerPanel.add(createBookingsCard(), "BOOKINGS");
        centerPanel.add(createViewFlightsCard(),"FLIGHTS");
        centerPanel.add(createBookSeatCard(this.clientId), "BOOK");
        centerPanel.add(createProfileCard(), "PROFILE");

        rightPanel.add(centerPanel, BorderLayout.CENTER);
        return rightPanel;
    }


    // Switch cards in the center panel
    private void switchCard(String cardName) {
        if (centerPanel != null) {
            CardLayout cardLayout = (CardLayout) centerPanel.getLayout();
            cardLayout.show(centerPanel, cardName);
        } else {
            JOptionPane.showMessageDialog(frame, "Navigation panel is not initialized!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // In createBookingsCard():
    private JPanel createBookingsCard() {
        JPanel bookingsCard = new JPanel(new BorderLayout());
        bookingsCard.setBackground(BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("My Bookings", JLabel.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(BUTTON_TEXT_COLOR);
        bookingsCard.add(titleLabel, BorderLayout.NORTH);

        // Create table to display bookings
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        bookingsTable = new JTable(tableModel);

        // Set table column headers
        tableModel.addColumn("Booking ID");
        tableModel.addColumn("Route");
        tableModel.addColumn("Seat Type");
        tableModel.addColumn("Fees");
        tableModel.addColumn("Status");

        // Initial data load
        refreshBookingsTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        bookingsCard.add(scrollPane, BorderLayout.CENTER);

        // Add refresh button at the bottom
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(BACKGROUND_COLOR);
        JButton refreshButton = createStyledButton("Refresh", e -> refreshBookingsTable(tableModel));
        bottomPanel.add(refreshButton);
        bookingsCard.add(bottomPanel, BorderLayout.SOUTH);

        return bookingsCard;
    }

    private void refreshBookingsTable(DefaultTableModel tableModel) {
        try {
            // Clear existing data first
            tableModel.setRowCount(0);

            String[][] bookings = fetchClientBookings();
            for (String[] booking : bookings) {
                tableModel.addRow(booking);
            }
        } catch (Exception e) {
            showErrorDialog("Failed to refresh bookings: " + e.getMessage());
        }
    }

    private String[][] fetchClientBookings() {
        String query = "SELECT b.id, " +
                "CONCAT(f.source, ' → ', f.destination), " +
                "b.seat_type, b.fees, " +
                "CASE WHEN b.ispaid = 1 THEN 'Paid' ELSE 'Reserved' END " +
                "FROM booking b JOIN flight f ON b.flight_id = f.id " +
                "WHERE b.client_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, clientId);
            return extractData(stmt.executeQuery(), 5);
        } catch (SQLException e) {
            showErrorDialog("Failed to load bookings: " + e.getMessage());
            return new String[0][0];
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JPanel createViewFlightsCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BACKGROUND_COLOR);

        // Title
        JLabel titleLabel = new JLabel("VIEW FLIGHTS", JLabel.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(BUTTON_TEXT_COLOR);
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        card.add(titleLabel, BorderLayout.NORTH);

        // Table setup with proper model
        String[] columns = {"Flight ID", "Route", "Departure", "Arrival"};
        String[][] data = fetchFlights();

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        flightsTable = new JTable(model);
        flightsTable.setFillsViewportHeight(true);
        flightsTable.setFont(LABEL_FONT);
        flightsTable.getTableHeader().setFont(LABEL_FONT.deriveFont(Font.BOLD));
        flightsTable.setRowHeight(25);

        // Enable sorting
        flightsTable.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(flightsTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));

        card.add(scrollPane, BorderLayout.CENTER);

        // Refresh button with proper action
        JButton refreshButton = createStyledButton("Refresh", e -> refreshFlightsTable());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(refreshButton);

        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }
    private String[][] fetchFlights() {
        String query = "SELECT f.id, " +
                "CONCAT(f.source, ' → ', f.destination) as route, " +
                "DATE_FORMAT(f.reporting_time, '%Y-%m-%d %H:%i') as departure, " +
                "DATE_FORMAT(f.arrival_time, '%Y-%m-%d %H:%i') as arrival " +
                "FROM flight f " +
                "WHERE f.reporting_time > CURRENT_TIMESTAMP " +  // Only future flights
                "ORDER BY f.reporting_time";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            return extractData(rs, 4);
        } catch (SQLException e) {
            showErrorDialog("Failed to load flights: " + e.getMessage());
            return new String[0][0];
        }
    }

    private void refreshFlightsTable() {
        try {
            String[][] newData = fetchFlights();
            DefaultTableModel model = (DefaultTableModel) flightsTable.getModel();
            model.setRowCount(0); // Clear existing data

            if (newData.length == 0) {
                model.addRow(new String[]{"No flights available"});
            } else {
                for (String[] row : newData) {
                    model.addRow(row);
                }
            }
        } catch (Exception e) {
            showErrorDialog("Failed to refresh flights: " + e.getMessage());
        }
    }
    // Reusable data extraction (from AdminDashboard)
    private String[][] extractData(ResultSet rs, int cols) throws SQLException {
        List<String[]> rows = new ArrayList<>();
        while (rs.next()) {
            String[] row = new String[cols];
            for (int i = 0; i < cols; i++) {
                row[i] = rs.getString(i + 1);
            }
            rows.add(row);
        }
        return rows.toArray(new String[0][0]);
    }
    // Create a logo for the left panel
    private JLabel createLogo(String imagePath) {
        ImageIcon img = new ImageIcon(imagePath);

        if (img.getImageLoadStatus() != MediaTracker.COMPLETE) {
            JOptionPane.showMessageDialog(null, "Image failed to load: " + imagePath, "Error", JOptionPane.ERROR_MESSAGE);
            return new JLabel("No Image", JLabel.CENTER); // Placeholder text if image fails
        }

        // Scale the image and set it to the label
        Image image = img.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        img = new ImageIcon(image);

        JLabel imageLabel = new JLabel(img);
        imageLabel.setHorizontalAlignment(JLabel.CENTER);

        return imageLabel;
    }

    // Create a styled button
    private static JButton createStyledButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text) {
            private boolean hover = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (hover) {
                    g2.setColor(new Color(220, 240, 255)); // Hover color
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                    g2.setColor(new Color(70, 130, 180)); // Border color for hover
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 50, 50);
                } else {
                    g2.setColor(new Color(70, 130, 180)); // Initial button color
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                }

                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setFont(new Font("Poppins", Font.PLAIN, 18));
        button.setForeground(Color.WHITE); // Text color
        button.setPreferredSize(new Dimension(200, 50));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false); // No focus on button
        button.addActionListener(action);
        return button;
    }

    private static JButton createStyledLogoutButton(String text, ActionListener action) {
        JButton button = new JButton(text) {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (hover) {
                    g2.setColor(new Color(255, 200, 200)); // Light red hover
                } else {
                    g2.setColor(Color.RED);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);

                super.paintComponent(g);
                g2.dispose();
            }
        };

        button.setFont(BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.addActionListener(action);

        return button;
    }

    // Logout method
    private void logout() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        frame.dispose();
        JOptionPane.showMessageDialog(null, "You have been logged out!");
    }

    private JPanel createBookSeatCard(int clientId) {
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(new Color(220, 240, 255));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(250, 250, 255));

        // Fixed border creation without the problematic braces
        Border innerBorder = BorderFactory.createEmptyBorder(20, 20, 20, 20);
        Border lineBorder = BorderFactory.createLineBorder(new Color(180, 190, 200), 1);
        Border titledBorder = BorderFactory.createTitledBorder(
                lineBorder,
                "Book a Seat",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16),
                new Color(50, 70, 90));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(innerBorder, titledBorder));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Flight Selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Select Flight:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> flightComboBox = new JComboBox<>();
        loadAllFlights(flightComboBox);
        contentPanel.add(flightComboBox, gbc);

        // Seat Type Selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(new JLabel("Select Seat Type:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> seatTypeComboBox = new JComboBox<>(new String[]{"Economy", "Business"});
        contentPanel.add(seatTypeComboBox, gbc);

        // Fees Display
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(new JLabel("Fees:"), gbc);
        gbc.gridx = 1;
        JTextField feesField = new JTextField(10);
        feesField.setEditable(false);
        contentPanel.add(feesField, gbc);

        // Update fees when selections change
        ActionListener updateFeesListener = e -> {
            if (flightComboBox.getSelectedItem() != null && seatTypeComboBox.getSelectedItem() != null) {
                String selectedFlight = (String) flightComboBox.getSelectedItem();
                String seatType = (String) seatTypeComboBox.getSelectedItem();
                if (selectedFlight != null && !selectedFlight.equals("No flights available")) {
                    int flightId = Integer.parseInt(selectedFlight.split(" - ")[0]);
                    double fees = calculateFees(flightId, seatType);
                    feesField.setText(String.format("%.2f", fees));
                }
            }
        };
        flightComboBox.addActionListener(updateFeesListener);
        seatTypeComboBox.addActionListener(updateFeesListener);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // Reserve Button
        JButton reserveButton = createStyledButton("Reserve", e -> {
            try {
                String selectedFlight = (String) flightComboBox.getSelectedItem();
                if (selectedFlight == null || selectedFlight.equals("No flights available")) {
                    JOptionPane.showMessageDialog(frame, "Please select a valid flight.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int flightId = Integer.parseInt(selectedFlight.split(" - ")[0]);
                String seatType = (String) seatTypeComboBox.getSelectedItem();
                double fees = calculateFees(flightId, seatType);

                if (reserveSeat(clientId, flightId, fees, seatType)) {
                    JOptionPane.showMessageDialog(frame, "Seat reserved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAllFlights(flightComboBox);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error reserving seat: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Pay Button (with improved error handling)
        JButton payButton = createStyledButton("Pay", e -> {
            try {
                String selectedFlight = (String) flightComboBox.getSelectedItem();
                if (selectedFlight == null || selectedFlight.equals("No flights available")) {
                    JOptionPane.showMessageDialog(frame, "Please select a valid flight.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int flightId = Integer.parseInt(selectedFlight.split(" - ")[0]);
                String seatType = (String) seatTypeComboBox.getSelectedItem();
                double fees = calculateFees(flightId, seatType);

                if (!isSeatAvailable(flightId, seatType)) {
                    JOptionPane.showMessageDialog(frame, "No available " + seatType + " seats", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (processPayment(clientId, flightId, fees, seatType)) {
                    JOptionPane.showMessageDialog(frame, "Payment successful! Seat booked.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAllFlights(flightComboBox);

                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error processing payment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(reserveButton);
        buttonPanel.add(payButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        contentPanel.add(buttonPanel, gbc);

        outerPanel.add(contentPanel, new GridBagConstraints());
        return outerPanel;
    }
    private boolean reserveSeat(int clientId, int flightId, double fees, String seatType) {
        try {
            connection.setAutoCommit(false);
            if (!isFlightInFuture(flightId)) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Cannot book a flight that has already departed!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return false;
            }

            // 1. Check seat availability
            if (!isSeatAvailable(flightId, seatType)) {
                connection.rollback();
                JOptionPane.showMessageDialog(frame, "No available " + seatType + " seats", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            // 2. Decrement available seats
            if (!decrementAvailableSeats(flightId, seatType)) {
                connection.rollback();
                return false;
            }

            // 3. Create reservation (isPaid = false, isReserved = true)
            addBooking(clientId, flightId, fees, seatType, false, true);

            connection.commit();
            refreshFlightsTable();
            return true;
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            JOptionPane.showMessageDialog(frame, "Reservation failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    private boolean isSeatAvailable(int flightId, String seatType) throws SQLException {
        String query = "SELECT " +
                (seatType.equalsIgnoreCase("Business") ? "business_seats" : "economy_seats") +
                " FROM flight f JOIN plane p ON f.plane_id = p.id WHERE f.id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, flightId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int availableSeats = rs.getInt(1);
                return availableSeats > 0;
            }
        }
        return false;
    }

    // Enhanced payment processing with transaction
    private boolean processPayment(int clientId, int flightId, double fees, String seatType) {

        try {
            connection.setAutoCommit(false); // Start transaction
            if (!isFlightInFuture(flightId)) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Cannot book a flight that has already departed!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return false;
            }

            // 1. Deduct from client balance
            if (!deductBalance(clientId, fees)) {
                connection.rollback();
                return false;
            }

            // 2. Add to flight's collected amount
            if (!updateFlightCollectedAmount(flightId, fees)) {
                connection.rollback();
                return false;
            }

            // 3. Decrement available seats
            if (!decrementAvailableSeats(flightId, seatType)) {
                connection.rollback();
                return false;
            }

            // 4. Create the booking
            addBooking(clientId, flightId, fees, seatType, true, false);

            connection.commit(); // Commit transaction
            refreshFlightsTable();
            return true;
        } catch (SQLException ex) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            JOptionPane.showMessageDialog(frame, "Transaction failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean updateFlightCollectedAmount(int flightId, double amount) {
        try {
            // Try with collected_amount first
            try {
                String updateQuery = "UPDATE flight SET collected_amount = IFNULL(collected_amount, 0) + ? WHERE id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                    stmt.setDouble(1, amount);
                    stmt.setInt(2, flightId);
                    return stmt.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                // If collected_amount fails, try collected_fare
                String updateQuery = "UPDATE flight SET collected_fare = IFNULL(collected_fare, 0) + ? WHERE id = ?";
                try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                    stmt.setDouble(1, amount);
                    stmt.setInt(2, flightId);
                    return stmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error updating flight collected amount: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean decrementAvailableSeats(int flightId, String seatType) {
        String column = seatType.equalsIgnoreCase("Business") ? "business_seats" : "economy_seats";
        String query = "UPDATE plane p JOIN flight f ON p.id = f.plane_id " +
                "SET p." + column + " = p." + column + " - 1 " +
                "WHERE f.id = ? AND p." + column + " > 0";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, flightId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error updating seat availability: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Modified addBooking to be part of the transaction
    private void addBooking(int clientId, int flightId, double fees, String seatType, boolean isPaid, boolean isReserved) throws SQLException {
        String bookingQuery = "INSERT INTO booking (flight_id, client_id, ispaid, isreserved, fees, seat_type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement bookingStmt = connection.prepareStatement(bookingQuery)) {
            bookingStmt.setInt(1, flightId);
            bookingStmt.setInt(2, clientId);
            bookingStmt.setBoolean(3, isPaid);
            bookingStmt.setBoolean(4, isReserved);
            bookingStmt.setDouble(5, fees);
            bookingStmt.setString(6, seatType);
            bookingStmt.executeUpdate();
        }
    }

    // Deduct balance from the client's account
    private boolean deductBalance(int clientId, double amount) {
        String query = "UPDATE client SET balance = balance - ? WHERE id = ? AND balance >= ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, clientId);
            stmt.setDouble(3, amount);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                return true;
            } else {
                JOptionPane.showMessageDialog(frame, "Insufficient balance to complete the payment.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error deducting balance: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean isFlightInFuture(int flightId) {
        String query = "SELECT reporting_time FROM flight WHERE id = ? AND reporting_time > NOW()";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, flightId);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if flight is in the future
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    private void loadAllFlights(JComboBox<String> comboBox) {
        comboBox.removeAllItems();
        String query = "SELECT id, source, destination, reporting_time " +
                "FROM flight " +
                "WHERE reporting_time > NOW() " +
                "ORDER BY reporting_time ASC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int flightId = rs.getInt("id");
                String source = rs.getString("source");
                String destination = rs.getString("destination");
                Timestamp reportingTime = rs.getTimestamp("reporting_time");

                String flightDetails = String.format(
                        "%d - %s to %s (Dep: %s)",
                        flightId, source, destination, reportingTime
                );
                comboBox.addItem(flightDetails);
            }

            if (comboBox.getItemCount() == 0) {
                comboBox.addItem("No upcoming flights available");
                comboBox.setEnabled(false);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Error loading flights: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    private double calculateFees(int flightId, String seatType) {
        String query = "SELECT expense, business_seats, economy_seats FROM flight f " +
                "JOIN plane p ON f.plane_id = p.id WHERE f.id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, flightId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double totalExpense = rs.getDouble("expense");
                int businessSeats = rs.getInt("business_seats");
                int economySeats = rs.getInt("economy_seats");
                int totalSeats = businessSeats + economySeats;

                if (totalSeats == 0) {
                    throw new IllegalArgumentException("Flight has no seats defined.");
                }

                // Calculate base fee per seat
                double baseFee = totalExpense / totalSeats;

                if ("Business".equalsIgnoreCase(seatType)) {
                    // Add 10% markup for all seats and additional 20% for business seats
                    return baseFee * 1.1 * 1.2;
                } else {
                    // Economy seats calculation (10% markup)
                    return baseFee * 1.1;
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error calculating fees: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return 0.0;
    }

    // Create the "My Profile" card
    private JPanel createProfileCard() {
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(new Color(220, 240, 255));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(250, 250, 255));

        // Create the same styled border as in book seat card
        Border innerBorder = BorderFactory.createEmptyBorder(20, 20, 20, 20);
        Border lineBorder = BorderFactory.createLineBorder(new Color(180, 190, 200), 1);
        Border titledBorder = BorderFactory.createTitledBorder(
                lineBorder,
                "My Profile",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16),
                new Color(50, 70, 90));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(innerBorder, titledBorder));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Name Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(clientName, 20);
        nameField.setEditable(false);
        nameField.setBackground(new Color(240, 240, 240));
        contentPanel.add(nameField, gbc);

        // Email Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        String email = clientName.toLowerCase().replaceAll(" ", ".") + "@client.com";
        JTextField emailField = new JTextField(email, 20);
        emailField.setEditable(false);
        emailField.setBackground(new Color(240, 240, 240));
        contentPanel.add(emailField, gbc);

        // Age Field
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1;
        JTextField ageField = new JTextField(String.valueOf(clientAge), 20);
        ageField.setEditable(false);
        ageField.setBackground(new Color(240, 240, 240));
        contentPanel.add(ageField, gbc);

        // Gender Field
        gbc.gridx = 0;
        gbc.gridy = 3;
        contentPanel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1;
        JTextField genderField = new JTextField(clientGender, 20);
        genderField.setEditable(false);
        genderField.setBackground(new Color(240, 240, 240));
        contentPanel.add(genderField, gbc);

        // Membership Status (example additional field)
        gbc.gridx = 0;
        gbc.gridy = 4;
        contentPanel.add(new JLabel("Membership:"), gbc);
        gbc.gridx = 1;
        JTextField membershipField = new JTextField("Premium Member", 20); // You can make this dynamic
        membershipField.setEditable(false);
        membershipField.setBackground(new Color(240, 240, 240));
        contentPanel.add(membershipField, gbc);

        outerPanel.add(contentPanel, new GridBagConstraints());
        return outerPanel;
    }
}