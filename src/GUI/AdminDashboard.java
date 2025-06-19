package GUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Vector;

public class AdminDashboard {
    private static final Color BACKGROUND_COLOR = new Color(220, 240, 255); // Light blue
    private static final Color BUTTON_HOVER_COLOR = Color.WHITE;
    private static final Color BUTTON_BACKGROUND_COLOR = new Color(224, 247, 250);
    private static final Color BUTTON_TEXT_COLOR = new Color(70, 130, 180); // Steel Blue
    private static final Font BUTTON_FONT = new Font("Poppins", Font.PLAIN, 18);
    private static final Font TITLE_FONT = new Font("Poppins", Font.BOLD, 24);
    private static final Font LABEL_FONT = new Font("Poppins", Font.PLAIN, 18);

    private static JFrame frame;
    private int adminId;
    private String adminName;
    private float adminProfit;
    private String companyName;

    private Connection connection; // Database connection object
    private JTable planesTable;
    private JTable flightsTable;

    // Buttons for validation state management (declared as fields for easy access)
    private JButton addFlightButton;
    private JButton updateFlightButton;
    private JButton addPlaneButton;


    public AdminDashboard(int adminId, String adminName, float adminProfit, String companyName) {
        this.adminId = adminId;
        this.adminName = adminName;
        this.adminProfit = adminProfit;
        this.companyName = companyName;

        try {
            // Initialize database connection
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ats", "root", "abcd1234");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        initializeDashboard();
    }

    private void initializeDashboard() {
        frame = new JFrame("Admin Dashboard");
        frame.setLayout(new BorderLayout());

        // Add left and right panels
        frame.add(createLeftPanel(), BorderLayout.WEST);
        frame.add(createRightPanel(), BorderLayout.CENTER);

        frame.setSize(840, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(BACKGROUND_COLOR);
        leftPanel.setPreferredSize(new Dimension(250, 600));
        leftPanel.setLayout(new BorderLayout());

        JLabel imageLabel = createImageLabel("Alogo.png");
        leftPanel.add(imageLabel, BorderLayout.NORTH);

        JPanel buttonPanel = createLeftNavigationButtons();
        leftPanel.add(buttonPanel, BorderLayout.CENTER);

        JButton logoutButton = createStyledLogoutButton("Logout", e -> logout());
        logoutButton.setPreferredSize(new Dimension(150, 60));
        logoutButton.setBackground(Color.RED);
        logoutButton.setForeground(Color.WHITE);
        leftPanel.add(logoutButton, BorderLayout.SOUTH);
        return leftPanel;
    }

    private JPanel createCardLayoutPanel() {
        JPanel centerPanel = new JPanel(new CardLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);

        centerPanel.add(createViewPlanesCard(), "PLANES");
        centerPanel.add(createViewBookingsCard(), "BOOKINGS");
        centerPanel.add(createViewFlightsCard(), "FLIGHTS");
        centerPanel.add(createMyDetailsCard(), "DETAILS");
        centerPanel.add(createAddPlaneCard(), "ADD_PLANE");
        centerPanel.add(createAddFlightCard(adminId), "ADD_FLIGHT");
        centerPanel.add(createUpdateFlightCard(adminId), "UPDATE_FLIGHT");
        centerPanel.add(createMyFlightsCard(), "MY_FLIGHTS");

        return centerPanel;
    }

    private JPanel createTopNavigationButtons() {
        JPanel topNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topNavPanel.setBackground(BACKGROUND_COLOR);
        topNavPanel.add(createStyledButton("Add Plane", e -> switchCard("ADD_PLANE")));
        topNavPanel.add(createStyledButton("Add Flight", e -> switchCard("ADD_FLIGHT")));
        topNavPanel.add(createStyledButton("Update Flight", e -> switchCard("UPDATE_FLIGHT")));

        return topNavPanel;
    }

    private JPanel createLeftNavigationButtons() {
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 5, 10));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        // Update the preferred size of the buttons
        Dimension buttonSize = new Dimension(100, 50); // Adjust width and height as needed

        JButton viewPlanesButton = createStyledButton("View Planes", e -> switchCard("PLANES"));
        viewPlanesButton.setPreferredSize(buttonSize);
        buttonPanel.add(viewPlanesButton);

        JButton viewBookingsButton = createStyledButton("View All Bookings", e -> switchCard("BOOKINGS"));
        viewBookingsButton.setPreferredSize(buttonSize);
        buttonPanel.add(viewBookingsButton);

        JButton viewFlightsButton = createStyledButton("View Flights", e -> switchCard("FLIGHTS"));
        viewFlightsButton.setPreferredSize(buttonSize);
        buttonPanel.add(viewFlightsButton);

        JButton viewMyFlightsButton = createStyledButton("View My Flights", e -> switchCard("MY_FLIGHTS"));
        viewMyFlightsButton.setPreferredSize(buttonSize);
        buttonPanel.add(viewMyFlightsButton);

        JButton myDetailsButton = createStyledButton("My Details", e -> switchCard("DETAILS"));
        myDetailsButton.setPreferredSize(buttonSize);
        buttonPanel.add(myDetailsButton);

        return buttonPanel;
    }

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

    private static JLabel createImageLabel(String imagePath) {
        ImageIcon img = new ImageIcon(imagePath);
        Image image = img.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        return new JLabel(new ImageIcon(image), JLabel.CENTER);
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());

        JPanel topNavPanel = createTopNavigationButtons();
        topNavPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 15, 0, Color.white)); // Bottom border
        rightPanel.add(topNavPanel, BorderLayout.NORTH);

        JPanel centerPanel = createCardLayoutPanel();
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.white, 15), // Outer border
                new EmptyBorder(5, 5, 5, 5) // Padding inside the border
        ));
        rightPanel.add(centerPanel, BorderLayout.CENTER);

        return rightPanel;
    }

    private JPanel createAddPlaneCard() {
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(new Color(220, 240, 255));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(250, 250, 255));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 190, 200), 1),
                        "Add New Plane",
                        TitledBorder.CENTER, TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 16), new Color(50, 70, 90)
                )
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Plane Model
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Plane Model:"), gbc);

        gbc.gridx = 1;
        JTextField modelField = new JTextField(15);
        contentPanel.add(modelField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel modelFeedback = new JLabel("");
        modelFeedback.setFont(new Font("Arial", Font.PLAIN, 12));
        contentPanel.add(modelFeedback, gbc);

        gbc.gridy += 1;
        gbc.anchor = GridBagConstraints.WEST;

        // Manufacturer
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(new JLabel("Manufacturer:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        JTextField manufacturerField = new JTextField(15);
        contentPanel.add(manufacturerField, gbc);

        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel manufacturerFeedback = new JLabel("");
        manufacturerFeedback.setFont(new Font("Arial", Font.PLAIN, 12));
        contentPanel.add(manufacturerFeedback, gbc);

        gbc.gridy += 1;
        gbc.anchor = GridBagConstraints.WEST;

        // Business Seats
        gbc.gridx = 0;
        gbc.gridy = 4;
        contentPanel.add(new JLabel("Business Seats:"), gbc);
        gbc.gridx = 1;
        JTextField businessSeatsField = new JTextField(15);
        contentPanel.add(businessSeatsField, gbc);

        gbc.gridy += 1;

        // Economy Seats
        gbc.gridx = 0;
        gbc.gridy = 5;
        contentPanel.add(new JLabel("Economy Seats:"), gbc);
        gbc.gridx = 1;
        JTextField economySeatsField = new JTextField(15);
        contentPanel.add(economySeatsField, gbc);

        // Validation Flags
        boolean[] isValidModel = {false};
        boolean[] isValidManufacturer = {false};
        boolean[] isValidBusinessSeats = {false};
        boolean[] isValidEconomySeats = {false};

        addPlaneButton = createStyledButton("Add Plane", null); // Set action later
        addPlaneButton.setEnabled(false); // Initially disabled

        // Add DocumentListeners
        modelField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String text = modelField.getText();
                boolean matches = text.matches("[A-Z0-9]+"); // At least one character
                if (text.isEmpty()) {
                    modelFeedback.setText(" Please enter plane model. ");
                    modelFeedback.setForeground(Color.RED);
                    isValidModel[0] = false;
                } else if (matches) {
                    modelFeedback.setText("✓ Valid. ");
                    modelFeedback.setForeground(new Color(0, 150, 0)); // Green
                    isValidModel[0] = true;
                } else {
                    modelFeedback.setText("✘ Only uppercase letters and numbers allowed.");
                    modelFeedback.setForeground(Color.RED);
                    isValidModel[0] = false;
                }
                updateAddPlaneButtonState(addPlaneButton, isValidModel[0], isValidManufacturer[0], isValidBusinessSeats[0], isValidEconomySeats[0]);
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) {}
        });

        manufacturerField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String text = manufacturerField.getText();
                boolean matches = text.matches("[A-Z]+");
                if (text.isEmpty()) {
                    manufacturerFeedback.setText(" Please enter manufacturer. ");
                    manufacturerFeedback.setForeground(Color.RED);
                    isValidManufacturer[0] = false;
                } else if (matches) {
                    manufacturerFeedback.setText("✓ Valid. ");
                    manufacturerFeedback.setForeground(new Color(0, 150, 0));
                    isValidManufacturer[0] = true;
                } else {
                    manufacturerFeedback.setText("✘ Only uppercase letters allowed.");
                    manufacturerFeedback.setForeground(Color.RED);
                    isValidManufacturer[0] = false;
                }
                updateAddPlaneButtonState(addPlaneButton, isValidModel[0], isValidManufacturer[0], isValidBusinessSeats[0], isValidEconomySeats[0]);
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) {}
        });

        DocumentListener seatsValidator = new DocumentListener() {
            private void update() {
                try {
                    int business = Integer.parseInt(businessSeatsField.getText().trim());
                    isValidBusinessSeats[0] = business > 0;
                } catch (NumberFormatException e) {
                    isValidBusinessSeats[0] = false;
                }
                try {
                    int economy = Integer.parseInt(economySeatsField.getText().trim());
                    isValidEconomySeats[0] = economy > 0;
                } catch (NumberFormatException e) {
                    isValidEconomySeats[0] = false;
                }
                updateAddPlaneButtonState(addPlaneButton, isValidModel[0], isValidManufacturer[0], isValidBusinessSeats[0], isValidEconomySeats[0]);
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) {}
        };
        businessSeatsField.getDocument().addDocumentListener(seatsValidator);
        economySeatsField.getDocument().addDocumentListener(seatsValidator);

        // Set final action listener after validation setup
        addPlaneButton.addActionListener(e -> {
            String model = modelField.getText().trim();
            String manufacturer = manufacturerField.getText().trim();
            int businessSeats;
            int economySeats;

            try {
                businessSeats = Integer.parseInt(businessSeatsField.getText().trim());
                economySeats = Integer.parseInt(economySeatsField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please enter valid numbers for seats!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (model.isEmpty() || manufacturer.isEmpty() || businessSeats <= 0 || economySeats <= 0) {
                JOptionPane.showMessageDialog(frame, "Please fill out all fields correctly!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String query = "INSERT INTO plane (admin_id, plane_model, manufacturer, business_seats, economy_seats) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, this.adminId);
                stmt.setString(2, model);
                stmt.setString(3, manufacturer);
                stmt.setInt(4, businessSeats);
                stmt.setInt(5, economySeats);

                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(frame, "Plane added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    modelField.setText("");
                    manufacturerField.setText("");
                    businessSeatsField.setText("");
                    economySeatsField.setText("");
                    // Reset validation states after clearing fields
                    isValidModel[0] = false;
                    isValidManufacturer[0] = false;
                    isValidBusinessSeats[0] = false;
                    isValidEconomySeats[0] = false;
                    updateAddPlaneButtonState(addPlaneButton, false, false, false, false);

                    refreshAddFlightPlaneList();
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to add plane.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Add Button
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        // FIX: Use HORIZONTAL fill to match the screenshot and ensure it stretches
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(addPlaneButton, gbc);

        GridBagConstraints outerGbc = new GridBagConstraints();
        outerGbc.gridx = 0;
        outerGbc.gridy = 0;
        outerGbc.weightx = 1.0;
        outerGbc.weighty = 1.0;
        outerGbc.anchor = GridBagConstraints.CENTER;
        outerPanel.add(contentPanel, outerGbc);

        // Initial feedback text
        modelFeedback.setText(" Please enter plane model. ");
        modelFeedback.setForeground(Color.RED);
        manufacturerFeedback.setText(" Please enter manufacturer. ");
        manufacturerFeedback.setForeground(Color.RED);

        return outerPanel;
    }
    private JPanel createAddFlightCard(int adminId) {
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(new Color(220, 240, 255));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(250, 250, 255));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 190, 200), 1),
                        "Add New Flight",
                        TitledBorder.CENTER, TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 16), new Color(50, 70, 90)
                )
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Validation flags for the button
        final boolean[] isValidPlaneSelection = {false};
        final boolean[] isValidSourceDest = {false};
        final boolean[] isValidDate = {false};
        final boolean[] isValidReportingTime = {false};
        final boolean[] isValidArrivalTime = {false};
        final boolean[] isValidExpense = {false};

        // Plane Selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(new JLabel("Select Plane:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> planeComboBox = new JComboBox<>();
        loadAdminPlanes(planeComboBox, adminId);
        contentPanel.add(planeComboBox, gbc);

        // Source Airport
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(new JLabel("Source Airport:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> sourceComboBox = new JComboBox<>();
        loadAirports(sourceComboBox);
        contentPanel.add(sourceComboBox, gbc);

        // Destination Airport
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(new JLabel("Destination Airport:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> destComboBox = new JComboBox<>();
        loadAirports(destComboBox);
        contentPanel.add(destComboBox, gbc);

        // Flight Date (Modified to match update card style)
        gbc.gridx = 0;
        gbc.gridy = 3;
        contentPanel.add(new JLabel("Flight Date:"), gbc);
        gbc.gridx = 1;
        JPanel datePanel = new JPanel(new GridBagLayout());
        datePanel.setOpaque(false);
        GridBagConstraints dateGbc = new GridBagConstraints();
        dateGbc.insets = new Insets(0, 2, 0, 2);
        dateGbc.anchor = GridBagConstraints.WEST;

        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH) + 1;
        int currentDay = cal.get(Calendar.DAY_OF_MONTH);

        JComboBox<Integer> dayComboBox = new JComboBox<>();
        JComboBox<Integer> monthComboBox = new JComboBox<>();
        JComboBox<Integer> yearComboBox = new JComboBox<>();

        for (int i = currentYear; i <= currentYear + 5; i++) {
            yearComboBox.addItem(i);
        }
        for (int i = 1; i <= 12; i++) {
            monthComboBox.addItem(i);
        }
        monthComboBox.setSelectedItem(currentMonth);
        updateDays(yearComboBox, monthComboBox, dayComboBox, currentDay);
        dayComboBox.setSelectedItem(currentDay);

        dateGbc.gridx = 0; dateGbc.gridy = 0; datePanel.add(new JLabel("D:"), dateGbc);
        dateGbc.gridx = 1; datePanel.add(dayComboBox, dateGbc);
        dateGbc.gridx = 2; datePanel.add(new JLabel("M:"), dateGbc);
        dateGbc.gridx = 3; datePanel.add(monthComboBox, dateGbc);
        dateGbc.gridx = 4; datePanel.add(new JLabel("Y:"), dateGbc);
        dateGbc.gridx = 5; datePanel.add(yearComboBox, dateGbc);
        contentPanel.add(datePanel, gbc);

        // Reporting Time
        gbc.gridx = 0;
        gbc.gridy = 4;
        contentPanel.add(new JLabel("Reporting Time:"), gbc);
        gbc.gridx = 1;
        JPanel reportingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JComboBox<Integer> repHour = new JComboBox<>();
        JComboBox<Integer> repMinute = new JComboBox<>();
        JComboBox<Integer> repSecond = new JComboBox<>();

        populateComboBox(repHour, 0, 23);
        populateComboBox(repMinute, 0, 59);
        populateComboBox(repSecond, 0, 59);
        reportingPanel.add(repHour);
        reportingPanel.add(new JLabel(":"));
        reportingPanel.add(repMinute);
        reportingPanel.add(new JLabel(":"));
        reportingPanel.add(repSecond);
        contentPanel.add(reportingPanel, gbc);

        // Arrival Time
        gbc.gridx = 0;
        gbc.gridy = 5;
        contentPanel.add(new JLabel("Arrival Time:"), gbc);
        gbc.gridx = 1;
        JPanel arrivalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JComboBox<Integer> arrHour = new JComboBox<>();
        JComboBox<Integer> arrMinute = new JComboBox<>();
        JComboBox<Integer> arrSecond = new JComboBox<>();

        populateComboBox(arrHour, 0, 23);
        populateComboBox(arrMinute, 0, 59);
        populateComboBox(arrSecond, 0, 59);
        arrivalPanel.add(arrHour);
        arrivalPanel.add(new JLabel(":"));
        arrivalPanel.add(arrMinute);
        arrivalPanel.add(new JLabel(":"));
        arrivalPanel.add(arrSecond);
        contentPanel.add(arrivalPanel, gbc);

        // Expense
        gbc.gridx = 0;
        gbc.gridy = 6;
        contentPanel.add(new JLabel("Expense:"), gbc);
        gbc.gridx = 1;
        JTextField expenseField = new JTextField(15);
        contentPanel.add(expenseField, gbc);

        // Add Flight Button
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Make the button stretch
        addFlightButton = createStyledButton("Add Flight", null);
        addFlightButton.setEnabled(false); // Initially disabled
        contentPanel.add(addFlightButton, gbc);

        // --- Add Listeners for Dynamic Validation ---

        // Listener for plane selection
        planeComboBox.addItemListener(e -> {
            isValidPlaneSelection[0] = e.getItem() != null && !((String) e.getItem()).startsWith("No planes");
            updateAddFlightButtonState(isValidPlaneSelection[0], isValidSourceDest[0], isValidDate[0], isValidReportingTime[0], isValidArrivalTime[0], isValidExpense[0]);
        });

        // Listener for source/dest combo boxes
        ItemListener sourceDestListener = e -> {
            boolean valid = sourceComboBox.getSelectedItem() != null && destComboBox.getSelectedItem() != null && !sourceComboBox.getSelectedItem().equals(destComboBox.getSelectedItem());
            isValidSourceDest[0] = valid;
            updateAddFlightButtonState(isValidPlaneSelection[0], isValidSourceDest[0], isValidDate[0], isValidReportingTime[0], isValidArrivalTime[0], isValidExpense[0]);
        };
        sourceComboBox.addItemListener(sourceDestListener);
        destComboBox.addItemListener(sourceDestListener);

        // Listener for date components
        ActionListener dateComponentListener = e -> {
            Integer selectedDay = (Integer) dayComboBox.getSelectedItem();
            if (selectedDay == null) selectedDay = currentDay;
            updateDays(yearComboBox, monthComboBox, dayComboBox, selectedDay);
            updateHourComboBoxes(yearComboBox, monthComboBox, dayComboBox, repHour, arrHour);

            isValidDate[0] = isDateValid(yearComboBox, monthComboBox, dayComboBox);
            updateAddFlightButtonState(isValidPlaneSelection[0], isValidSourceDest[0], isValidDate[0], isValidReportingTime[0], isValidArrivalTime[0], isValidExpense[0]);
        };
        yearComboBox.addActionListener(dateComponentListener);
        monthComboBox.addActionListener(dateComponentListener);
        dayComboBox.addActionListener(dateComponentListener);

        // Listener for time components
        ActionListener timeComponentListener = e -> {
            isValidReportingTime[0] = isTimeValid(repHour, repMinute, repSecond);
            isValidArrivalTime[0] = isTimeValid(arrHour, arrMinute, arrSecond);

            if(isValidReportingTime[0] && isValidArrivalTime[0] && isValidDate[0]) {
                Timestamp repTs = getDateTimeFromPickers(yearComboBox, monthComboBox, dayComboBox, repHour, repMinute, repSecond);
                Timestamp arrTs = getDateTimeFromPickers(yearComboBox, monthComboBox, dayComboBox, arrHour, arrMinute, arrSecond);
                if (arrTs != null && repTs != null && !arrTs.after(repTs)) {
                    isValidArrivalTime[0] = false; // Invalid arrival time
                }
            }
            updateAddFlightButtonState(isValidPlaneSelection[0], isValidSourceDest[0], isValidDate[0], isValidReportingTime[0], isValidArrivalTime[0], isValidExpense[0]);
        };
        repHour.addActionListener(timeComponentListener);
        repMinute.addActionListener(timeComponentListener);
        repSecond.addActionListener(timeComponentListener);
        arrHour.addActionListener(timeComponentListener);
        arrMinute.addActionListener(timeComponentListener);
        arrSecond.addActionListener(timeComponentListener);

        // DocumentListener for expense field
        expenseField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                try {
                    float expense = Float.parseFloat(expenseField.getText().trim());
                    isValidExpense[0] = expense > 0;
                } catch (NumberFormatException ex) {
                    isValidExpense[0] = false;
                }
                updateAddFlightButtonState(isValidPlaneSelection[0], isValidSourceDest[0], isValidDate[0], isValidReportingTime[0], isValidArrivalTime[0], isValidExpense[0]);
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) {}
        });

        // Set action listener for the button AFTER validation logic is in place
        addFlightButton.addActionListener(e -> {
            // Final validation check before execution
            if (!isValidPlaneSelection[0] || !isValidSourceDest[0] || !isValidDate[0] || !isValidReportingTime[0] || !isValidArrivalTime[0] || !isValidExpense[0]) {
                JOptionPane.showMessageDialog(frame, "Please fill out all fields correctly to add a flight.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String selectedPlane = (String) planeComboBox.getSelectedItem();
                int planeId = Integer.parseInt(selectedPlane.split(" - ")[0]);

                String source = (String) sourceComboBox.getSelectedItem();
                String destination = (String) destComboBox.getSelectedItem();

                Timestamp reportingTimestamp = getDateTimeFromPickers(yearComboBox, monthComboBox, dayComboBox, repHour, repMinute, repSecond);
                Timestamp arrivalTimestamp = getDateTimeFromPickers(yearComboBox, monthComboBox, dayComboBox, arrHour, arrMinute, arrSecond);

                // This check is redundant with listeners but good for final validation
                if (!arrivalTimestamp.after(reportingTimestamp)) {
                    JOptionPane.showMessageDialog(frame, "Arrival time must be after reporting time.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                float expense = Float.parseFloat(expenseField.getText().trim());

                addFlight(connection, planeId, source, destination, arrivalTimestamp, reportingTimestamp, expense);
                JOptionPane.showMessageDialog(frame, "Flight added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                // Reset fields
                expenseField.setText("");
                yearComboBox.setSelectedItem(currentYear);
                monthComboBox.setSelectedItem(currentMonth);
                updateDays(yearComboBox, monthComboBox, dayComboBox, currentDay);
                dayComboBox.setSelectedItem(currentDay);
                repHour.setSelectedIndex(0);
                repMinute.setSelectedIndex(0);
                repSecond.setSelectedIndex(0);
                arrHour.setSelectedIndex(0);
                arrMinute.setSelectedIndex(0);
                arrSecond.setSelectedIndex(0);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error adding flight: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        GridBagConstraints outerGbc = new GridBagConstraints();
        outerGbc.gridx = 0;
        outerGbc.gridy = 0;
        outerGbc.weightx = 1.0;
        outerGbc.weighty = 1.0;
        outerGbc.anchor = GridBagConstraints.CENTER;
        outerPanel.add(contentPanel, outerGbc);

        // Initial validation check after the GUI is built
        SwingUtilities.invokeLater(() -> {
            isValidPlaneSelection[0] = planeComboBox.getSelectedItem() != null && !((String) planeComboBox.getSelectedItem()).startsWith("No planes");
            isValidSourceDest[0] = sourceComboBox.getSelectedItem() != null && destComboBox.getSelectedItem() != null && !sourceComboBox.getSelectedItem().equals(destComboBox.getSelectedItem());
            isValidDate[0] = isDateValid(yearComboBox, monthComboBox, dayComboBox);
            isValidReportingTime[0] = isTimeValid(repHour, repMinute, repSecond);
            isValidArrivalTime[0] = isTimeValid(arrHour, arrMinute, arrSecond);
            try { isValidExpense[0] = Float.parseFloat(expenseField.getText().trim()) > 0; } catch (NumberFormatException ignored) {}
            updateAddFlightButtonState(isValidPlaneSelection[0], isValidSourceDest[0], isValidDate[0], isValidReportingTime[0], isValidArrivalTime[0], isValidExpense[0]);
            updateHourComboBoxes(yearComboBox, monthComboBox, dayComboBox, repHour, arrHour);
        });

        return outerPanel;
    }
    private JPanel createUpdateFlightCard(int adminId) {
        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(new Color(220, 240, 255));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(250, 250, 255));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(180, 190, 200), 1),
                        "Update Flight Details",
                        TitledBorder.CENTER, TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 16), new Color(50, 70, 90)
                )
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Validation flags
        final boolean[] isValidFlightSelected = {false};
        final boolean[] isValidNewValue = {false};

        // --- Section 1: Select Flight ---
        JPanel flightSelectionPanel = new JPanel(new GridBagLayout());
        flightSelectionPanel.setOpaque(false); // Inherit background from contentPanel
        flightSelectionPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "1. Select Flight to Update"));
        GridBagConstraints fspGbc = new GridBagConstraints();
        fspGbc.insets = new Insets(5, 5, 5, 5);
        fspGbc.fill = GridBagConstraints.HORIZONTAL;

        fspGbc.gridx = 0;
        fspGbc.gridy = 0;
        fspGbc.weightx = 0.2;
        flightSelectionPanel.add(new JLabel("Select Flight:"), fspGbc);
        fspGbc.gridx = 1;
        fspGbc.weightx = 0.8;
        JComboBox<String> flightIdComboBox = new JComboBox<>();
        loadAdminFlights(flightIdComboBox, adminId);
        flightSelectionPanel.add(flightIdComboBox, fspGbc);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(flightSelectionPanel, gbc);

        // --- Section 2: Choose Field and New Value ---
        JPanel updateDetailsPanel = new JPanel(new GridBagLayout());
        updateDetailsPanel.setOpaque(false);
        updateDetailsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "2. Specify Update"));
        GridBagConstraints udpGbc = new GridBagConstraints();
        udpGbc.insets = new Insets(5, 5, 5, 5);
        udpGbc.fill = GridBagConstraints.HORIZONTAL;
        udpGbc.anchor = GridBagConstraints.WEST;

        udpGbc.gridx = 0;
        udpGbc.gridy = 0;
        udpGbc.weightx = 0.3; // Adjust weight for label
        updateDetailsPanel.add(new JLabel("Field to Update:"), udpGbc);
        udpGbc.gridx = 1;
        udpGbc.weightx = 0.7;
        String[] updateOptions = {"Source", "Destination", "Departure Time", "Arrival Time", "Plane"};
        JComboBox<String> fieldToUpdateCombo = new JComboBox<>(updateOptions);
        updateDetailsPanel.add(fieldToUpdateCombo, udpGbc);

        udpGbc.gridx = 0;
        udpGbc.gridy = 1;
        updateDetailsPanel.add(new JLabel("New Value:"), udpGbc);
        udpGbc.gridx = 1;
        JPanel newValueInputPanel = new JPanel(new CardLayout());
        newValueInputPanel.setOpaque(false);
        updateDetailsPanel.add(newValueInputPanel, udpGbc);

        // 1. Airport Selection (for Source and Destination)
        JComboBox<String> newAirportComboBox = new JComboBox<>();
        loadAirports(newAirportComboBox);
        newValueInputPanel.add(newAirportComboBox, "Airport");

        // 2. Date/Time Pickers (for Departure and Arrival Time)
        JPanel dateTimePanel = new JPanel(new GridBagLayout());
        dateTimePanel.setOpaque(false);
        GridBagConstraints dtGbc = new GridBagConstraints();
        dtGbc.insets = new Insets(3, 3, 3, 3);
        dtGbc.fill = GridBagConstraints.HORIZONTAL;
        dtGbc.anchor = GridBagConstraints.WEST;

        JComboBox<Integer> dayComboBox = new JComboBox<>();
        JComboBox<Integer> monthComboBox = new JComboBox<>();
        JComboBox<Integer> yearComboBox = new JComboBox<>();
        JComboBox<Integer> hourComboBox = new JComboBox<>();
        JComboBox<Integer> minuteComboBox = new JComboBox<>();
        JComboBox<Integer> secondComboBox = new JComboBox<>();

        dtGbc.gridx = 0; dtGbc.gridy = 0; dtGbc.weightx = 0.1; dateTimePanel.add(new JLabel("D:"), dtGbc);
        dtGbc.gridx = 1; dtGbc.weightx = 0.2; dateTimePanel.add(dayComboBox, dtGbc);
        dtGbc.gridx = 2; dtGbc.weightx = 0.1; dateTimePanel.add(new JLabel("M:"), dtGbc);
        dtGbc.gridx = 3; dtGbc.weightx = 0.2; dateTimePanel.add(monthComboBox, dtGbc);
        dtGbc.gridx = 4; dtGbc.weightx = 0.1; dateTimePanel.add(new JLabel("Y:"), dtGbc);
        dtGbc.gridx = 5; dtGbc.weightx = 0.3; dateTimePanel.add(yearComboBox, dtGbc);

        dtGbc.gridx = 0; dtGbc.gridy = 1; dtGbc.gridwidth = 1; dateTimePanel.add(new JLabel("H:"), dtGbc);
        dtGbc.gridx = 1; dateTimePanel.add(hourComboBox, dtGbc);
        dtGbc.gridx = 2; dateTimePanel.add(new JLabel("M:"), dtGbc);
        dtGbc.gridx = 3; dateTimePanel.add(minuteComboBox, dtGbc);
        dtGbc.gridx = 4; dateTimePanel.add(new JLabel("S:"), dtGbc);
        dtGbc.gridx = 5; dateTimePanel.add(secondComboBox, dtGbc);

        Calendar cal = Calendar.getInstance();
        int currentYr = cal.get(Calendar.YEAR);
        for (int i = currentYr - 3; i <= currentYr + 5; i++) yearComboBox.addItem(i);
        for (int i = 1; i <= 12; i++) monthComboBox.addItem(i);
        for (int i = 0; i < 24; i++) hourComboBox.addItem(i);
        for (int i = 0; i < 60; i++) minuteComboBox.addItem(i);
        for (int i = 0; i < 60; i++) secondComboBox.addItem(i);
        newValueInputPanel.add(dateTimePanel, "DateTime");

        // 3. Plane Selection
        JComboBox<String> newPlaneComboBox = new JComboBox<>();
        loadAdminPlanes(newPlaneComboBox, adminId);
        newValueInputPanel.add(newPlaneComboBox, "Plane");

        // Add the update details panel to the main content panel
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        contentPanel.add(updateDetailsPanel, gbc);

        // --- Logic to switch between input panels and manage button state ---
        Runnable prefillDateTimePanelLogic = () -> {
            String selectedFlightItem = (String) flightIdComboBox.getSelectedItem();
            String selectedField = (String) fieldToUpdateCombo.getSelectedItem();
            int flightId = -1;
            if (selectedFlightItem != null && selectedFlightItem.startsWith("ID: ")) {
                try {
                    flightId = Integer.parseInt(selectedFlightItem.substring(4, selectedFlightItem.indexOf(" |")).trim());
                } catch (Exception ignored) { /* Parsing error, flightId remains -1 */ }
            }

            if (flightId != -1 && ("Departure Time".equals(selectedField) || "Arrival Time".equals(selectedField))) {
                Timestamp[] times = getFlightTimestamps(flightId);
                if (times != null) {
                    Timestamp targetTs = "Departure Time".equals(selectedField) ? times[0] : times[1];
                    if (targetTs != null) {
                        LocalDateTime ldt = targetTs.toLocalDateTime();
                        yearComboBox.setSelectedItem(ldt.getYear());
                        monthComboBox.setSelectedItem(ldt.getMonthValue());
                        updateDays(yearComboBox, monthComboBox, dayComboBox, ldt.getDayOfMonth());
                        dayComboBox.setSelectedItem(ldt.getDayOfMonth());
                        populateComboBox(hourComboBox, 0, 23);
                        hourComboBox.setSelectedItem(ldt.getHour());
                        minuteComboBox.setSelectedItem(ldt.getMinute());
                        secondComboBox.setSelectedItem(ldt.getSecond());
                        updateHourComboBoxes(yearComboBox, monthComboBox, dayComboBox, hourComboBox, hourComboBox);
                    } else {
                        populateDateTimeWithCurrent(yearComboBox, monthComboBox, dayComboBox, hourComboBox, minuteComboBox, secondComboBox);
                    }
                } else {
                    populateDateTimeWithCurrent(yearComboBox, monthComboBox, dayComboBox, hourComboBox, minuteComboBox, secondComboBox);
                }
            } else if ("Departure Time".equals(selectedField) || "Arrival Time".equals(selectedField)) {
                populateDateTimeWithCurrent(yearComboBox, monthComboBox, dayComboBox, hourComboBox, minuteComboBox, secondComboBox);
            }
        };

        // Add listeners to validate the form and enable/disable the button
        Runnable updateValidationState = () -> {
            boolean flightSelected = flightIdComboBox.getSelectedItem() != null && !((String) flightIdComboBox.getSelectedItem()).startsWith("No flights");

            boolean newValueValid = false;
            String selectedField = (String) fieldToUpdateCombo.getSelectedItem();
            if (selectedField != null) {
                switch (selectedField) {
                    case "Source":
                    case "Destination":
                        newValueValid = newAirportComboBox.getSelectedItem() != null;
                        break;
                    case "Departure Time":
                    case "Arrival Time":
                        newValueValid = isDateValid(yearComboBox, monthComboBox, dayComboBox) &&
                                isTimeValid(hourComboBox, minuteComboBox, secondComboBox);
                        break;
                    case "Plane":
                        newValueValid = newPlaneComboBox.getSelectedItem() != null;
                        break;
                }
            }
            isValidFlightSelected[0] = flightSelected;
            isValidNewValue[0] = newValueValid;
            updateUpdateButtonState(isValidFlightSelected[0], isValidNewValue[0]);
        };

        flightIdComboBox.addItemListener(e -> {
            prefillDateTimePanelLogic.run();
            updateValidationState.run();
        });

        CardLayout cardLayout = (CardLayout) newValueInputPanel.getLayout();
        fieldToUpdateCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedField = (String) e.getItem();
                switch (selectedField) {
                    case "Source":
                    case "Destination":
                        cardLayout.show(newValueInputPanel, "Airport");
                        break;
                    case "Departure Time":
                    case "Arrival Time":
                        prefillDateTimePanelLogic.run();
                        cardLayout.show(newValueInputPanel, "DateTime");
                        break;
                    case "Plane":
                        cardLayout.show(newValueInputPanel, "Plane");
                        break;
                }
                updateValidationState.run();
            }
        });

        ActionListener dateTimeChangeListener = evt -> {
            if (yearComboBox.getSelectedItem() != null && monthComboBox.getSelectedItem() != null) {
                updateDays(yearComboBox, monthComboBox, dayComboBox, (Integer) dayComboBox.getSelectedItem());
                updateHourComboBoxes(yearComboBox, monthComboBox, dayComboBox, hourComboBox, hourComboBox);
            }
            updateValidationState.run();
        };
        yearComboBox.addActionListener(dateTimeChangeListener);
        monthComboBox.addActionListener(dateTimeChangeListener);
        dayComboBox.addActionListener(dateTimeChangeListener);
        hourComboBox.addActionListener(dateTimeChangeListener);
        minuteComboBox.addActionListener(dateTimeChangeListener);
        secondComboBox.addActionListener(dateTimeChangeListener);

        newAirportComboBox.addActionListener(e -> updateValidationState.run());
        newPlaneComboBox.addActionListener(e -> updateValidationState.run());

        if (flightIdComboBox.getItemCount() > 0 && !((String) flightIdComboBox.getSelectedItem()).startsWith("No flights")) {
            prefillDateTimePanelLogic.run();
        }
        fieldToUpdateCombo.setSelectedIndex(0);

        // --- Section 3: Update Button ---
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL; // FIX: Make the button stretch
        updateFlightButton = createStyledButton("Execute Update", null);
        updateFlightButton.setEnabled(false); // Initially disabled
        contentPanel.add(updateFlightButton, gbc);

        // Set action listener for the button
        updateFlightButton.addActionListener(e -> {
            if (!isValidFlightSelected[0] || !isValidNewValue[0]) {
                JOptionPane.showMessageDialog(frame, "Please ensure all fields are selected and valid.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedFlightItem = (String) flightIdComboBox.getSelectedItem();
            int flightId = -1;
            try {
                flightId = Integer.parseInt(selectedFlightItem.substring(4, selectedFlightItem.indexOf(" |")).trim());
            } catch (Exception parseEx) {
                JOptionPane.showMessageDialog(frame, "Could not parse flight ID from selection.", "Selection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String fieldToUpdateStr = (String) fieldToUpdateCombo.getSelectedItem();
            Object newValue = null;
            String columnName = "";

            try {
                switch (fieldToUpdateStr) {
                    case "Source":
                        columnName = "source";
                        newValue = newAirportComboBox.getSelectedItem();
                        if (newValue == null || ((String) newValue).isEmpty())
                            throw new IllegalArgumentException("Please select a new source airport.");
                        break;
                    case "Destination":
                        columnName = "destination";
                        newValue = newAirportComboBox.getSelectedItem();
                        if (newValue == null || ((String) newValue).isEmpty())
                            throw new IllegalArgumentException("Please select a new destination airport.");
                        break;
                    case "Departure Time":
                        columnName = "reporting_time";
                        newValue = getDateTimeFromPickers(yearComboBox, monthComboBox, dayComboBox, hourComboBox, minuteComboBox, secondComboBox);
                        break;
                    case "Arrival Time":
                        columnName = "arrival_time";
                        newValue = getDateTimeFromPickers(yearComboBox, monthComboBox, dayComboBox, hourComboBox, minuteComboBox, secondComboBox);
                        break;
                    case "Plane":
                        columnName = "plane_id";
                        String selectedPlaneStr = (String) newPlaneComboBox.getSelectedItem();
                        if (selectedPlaneStr == null || selectedPlaneStr.isEmpty())
                            throw new IllegalArgumentException("Please select a new plane.");
                        newValue = Integer.parseInt(selectedPlaneStr.split(" - ")[0]);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid field selected for update.");
                }

                if (newValue != null && !columnName.isEmpty()) {
                    updateFlight(flightId, columnName, newValue);
                    int currentFlightSelectionIndex = flightIdComboBox.getSelectedIndex();
                    loadAdminFlights(flightIdComboBox, adminId);
                    if(flightIdComboBox.getItemCount() > currentFlightSelectionIndex) flightIdComboBox.setSelectedIndex(currentFlightSelectionIndex);
                }
            } catch (IllegalArgumentException exVal) {
                JOptionPane.showMessageDialog(frame, exVal.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception exGlobal) {
                JOptionPane.showMessageDialog(frame, "Error processing update: " + exGlobal.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                exGlobal.printStackTrace();
            }
        });

        // --- Outer Panel Setup ---
        GridBagConstraints outerGbc = new GridBagConstraints();
        outerGbc.gridx = 0;
        outerGbc.gridy = 0;
        outerGbc.weightx = 1.0;
        outerGbc.weighty = 1.0;
        outerGbc.anchor = GridBagConstraints.CENTER;
        outerPanel.add(contentPanel, outerGbc);

        // Initial validation check
        SwingUtilities.invokeLater(updateValidationState);

        return outerPanel;
    }

    private void updateAddPlaneButtonState(JButton button, boolean... conditions) {
        for (boolean cond : conditions) {
            if (!cond) {
                button.setEnabled(false);
                return;
            }
        }
        button.setEnabled(true);
    }
    private void refreshAddFlightPlaneList() {
        try {
            JPanel rightPanel = (JPanel) frame.getContentPane().getComponent(1); // Get the right panel
            JPanel centerPanel = (JPanel) rightPanel.getComponent(1); // Get the card layout panel
            // This component search is fragile, relies on component order.
            JPanel addFlightCard = (JPanel) centerPanel.getComponent(5);

            // Find the plane JComboBox in the "Add Flight" card
            for (Component comp : addFlightCard.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    for (Component innerComp : panel.getComponents()) {
                        if (innerComp instanceof JComboBox) {
                            JComboBox<String> planeComboBox = (JComboBox<String>) innerComp;
                            // Check if this is the correct combobox by its item count or name
                            // This assumes it's the first JComboBox in that panel.
                            if (planeComboBox.getItemCount() == 0 || planeComboBox.getItemAt(0).contains(" - ")) {
                                loadAdminPlanes(planeComboBox, this.adminId); // Reload the list of planes
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Failed to refresh plane list: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void loadAdminPlanes(JComboBox<String> comboBox, int adminId) {
        comboBox.removeAllItems();
        String sql = "SELECT id, plane_model, manufacturer FROM plane WHERE admin_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, adminId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String model = rs.getString("plane_model"); // Changed from "model" to "plane_model"
                String manufacturer = rs.getString("manufacturer");
                comboBox.addItem(id + " - " + model + " (" + manufacturer + ")");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading planes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Added to help with debugging
        }
    }

    // --- MODIFIED createAddFlightCard ---


    private void updateAddFlightButtonState(boolean isValidPlane, boolean isValidSourceDest, boolean isValidDate,
                                            boolean isValidRepTime, boolean isValidArrTime, boolean isValidExpense) {
        if (addFlightButton != null) {
            boolean allValid = isValidPlane && isValidSourceDest && isValidDate && isValidRepTime && isValidArrTime && isValidExpense;
            addFlightButton.setEnabled(allValid);
        }
    }

    private boolean isDateValid(JComboBox<Integer> yearCombo, JComboBox<Integer> monthCombo, JComboBox<Integer> dayCombo) {
        Integer year = (Integer) yearCombo.getSelectedItem();
        Integer month = (Integer) monthCombo.getSelectedItem();
        Integer day = (Integer) dayCombo.getSelectedItem();

        if (year == null || month == null || day == null) return false;

        try {
            LocalDate selectedDate = LocalDate.of(year, month, day);
            LocalDate today = LocalDate.now();
            return !selectedDate.isBefore(today);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTimeValid(JComboBox<Integer> hourCombo, JComboBox<Integer> minuteCombo, JComboBox<Integer> secondCombo) {
        Integer hour = (Integer) hourCombo.getSelectedItem();
        Integer minute = (Integer) minuteCombo.getSelectedItem();
        Integer second = (Integer) secondCombo.getSelectedItem();

        return hour != null && minute != null && second != null;
    }

    // New helper method to populate JComboBox
    private void populateComboBox(JComboBox<Integer> comboBox, int start, int end) {
        Integer selected = (Integer) comboBox.getSelectedItem();
        comboBox.removeAllItems();
        for (int i = start; i <= end; i++) {
            comboBox.addItem(i);
        }
        if (selected != null && start <= selected && selected <= end) {
            comboBox.setSelectedItem(selected);
        } else if (comboBox.getItemCount() > 0) {
            comboBox.setSelectedIndex(0);
        }
    }
    // FIX: A more robust updateHourComboBoxes method
    private void updateHourComboBoxes(JComboBox<Integer> yearComboBox, JComboBox<Integer> monthComboBox,
                                      JComboBox<Integer> dayComboBox, JComboBox<Integer> repHourComboBox,
                                      JComboBox<Integer> arrHourComboBox) {
        Integer selectedYearObj = (Integer) yearComboBox.getSelectedItem();
        Integer selectedMonthObj = (Integer) monthComboBox.getSelectedItem();
        Integer selectedDayObj = (Integer) dayComboBox.getSelectedItem();

        if (selectedYearObj == null || selectedMonthObj == null || selectedDayObj == null) {
            // Not enough info to update, set to full range
            populateComboBox(repHourComboBox, 0, 23);
            populateComboBox(arrHourComboBox, 0, 23);
            return;
        }

        int selectedYear = selectedYearObj;
        int selectedMonth = selectedMonthObj;
        int selectedDay = selectedDayObj;

        LocalDate selectedDate = LocalDate.of(selectedYear, selectedMonth, selectedDay);
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        int startHour = 0;
        if (selectedDate.isEqual(today)) {
            startHour = now.getHour();
        }

        // Populate reporting hour combo box
        populateComboBox(repHourComboBox, startHour, 23);

        // Populate arrival hour combo box based on reporting hour selection
        populateComboBox(arrHourComboBox, startHour, 23);

        // Add a listener to repHourComboBox to constrain arrHourComboBox
        // Remove existing listeners to avoid duplicates
        for (ActionListener al : repHourComboBox.getActionListeners()) {
            repHourComboBox.removeActionListener(al);
        }
        repHourComboBox.addActionListener(e -> {
            Integer selectedRepH = (Integer) repHourComboBox.getSelectedItem();
            if (selectedRepH != null) {
                Integer selectedArrH = (Integer) arrHourComboBox.getSelectedItem();
                // If the selected arrival hour is less than the new reporting hour,
                // adjust it to be equal to or greater than the reporting hour.
                if (selectedArrH == null || selectedArrH < selectedRepH) {
                    arrHourComboBox.setSelectedItem(selectedRepH);
                }
            }
        });
    }

    private void loadAirports(JComboBox<String> comboBox) {
        comboBox.removeAllItems();
        String sql = "SELECT CONCAT(airport_code, ' - ', airport_name, ' (', city, ', ', country, ')') AS display_text " +
                "FROM airports ORDER BY city, airport_name";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                comboBox.addItem(rs.getString("display_text"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading airports: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Updates the day JComboBox based on the selected year and month.
     */
    private void updateDays(JComboBox<Integer> yearComboBox, JComboBox<Integer> monthComboBox, JComboBox<Integer> dayComboBox, Integer previouslySelectedDay) {
        int year = (Integer) yearComboBox.getSelectedItem();
        int month = (Integer) monthComboBox.getSelectedItem();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1); // Calendar.MONTH is 0-indexed
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        Vector<Integer> days = new Vector<>();
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(i);
        }
        dayComboBox.setModel(new DefaultComboBoxModel<>(days));

        // Try to re-select the previously selected day if it's still valid
        if (previouslySelectedDay != null && previouslySelectedDay <= daysInMonth) {
            dayComboBox.setSelectedItem(previouslySelectedDay);
        } else if (!days.isEmpty()) {
            dayComboBox.setSelectedIndex(0); // Select the first day if previous is invalid
        }
    }

    private void addFlight(Connection connection, int plane_id, String source, String destination,
                           Timestamp arrival_time, Timestamp reporting_time, float expense) {
        try {
            // Check for overlapping flights
            String checkQuery = "SELECT COUNT(*) FROM flight WHERE plane_id = ? AND (" +
                    "(? < arrival_time AND ? > reporting_time) OR " +
                    "(? BETWEEN reporting_time AND arrival_time) OR " +
                    "(? BETWEEN reporting_time AND arrival_time)" +
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
                JOptionPane.showMessageDialog(frame, "Error: Plane is already assigned to an overlapping flight.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insert flight
            String insertQuery = "INSERT INTO flight (plane_id, source, destination, arrival_time, reporting_time, expense) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(insertQuery);
            stmt.setInt(1, plane_id);
            stmt.setString(2, source);
            stmt.setString(3, destination);
            stmt.setTimestamp(4, arrival_time);
            stmt.setTimestamp(5, reporting_time);
            stmt.setFloat(6, expense);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected <= 0) {
                JOptionPane.showMessageDialog(frame, "Error: Flight not added.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Database Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void switchCard(String cardName) {
        JPanel rightPanel = (JPanel) frame.getContentPane().getComponent(1);
        JPanel centerPanel = (JPanel) rightPanel.getComponent(1);

        CardLayout layout = (CardLayout) centerPanel.getLayout();
        layout.show(centerPanel, cardName);
    }

    private void loadAdminFlights(JComboBox<String> flightComboBox, int adminId) {
        // SQL to get flights whose plane_id belongs to a plane associated with the adminId
        // This query assumes a 'plane' table with 'id' and 'admin_id' columns.
        String sql = "SELECT f.id, f.source, f.destination, f.reporting_time " +
                "FROM flight f " +
                "JOIN plane p ON f.plane_id = p.id " +
                "WHERE p.admin_id = ? " +
                "AND f.reporting_time > NOW() " +
                "ORDER BY f.reporting_time ASC";

        flightComboBox.removeAllItems(); // Clear previous items

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, adminId);
            ResultSet rs = pstmt.executeQuery();

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            boolean flightsFound = false;
            while (rs.next()) {
                flightsFound = true;
                int id = rs.getInt("id");
                String source = rs.getString("source");
                String dest = rs.getString("destination");
                Timestamp reportingTimeTs = rs.getTimestamp("reporting_time");

                String reportingDateStr = "N/A";
                if (reportingTimeTs != null) {
                    reportingDateStr = reportingTimeTs.toLocalDateTime().format(dateFormatter);
                }

                // Format for display, ID is at the beginning for easy parsing
                String displayText = String.format("ID: %d | %s -> %s | Dep: %s", id, source, dest, reportingDateStr);
                flightComboBox.addItem(displayText);
            }

            if (!flightsFound) {
                flightComboBox.addItem("No flights found for this admin.");
                flightComboBox.setEnabled(false);
            } else {
                flightComboBox.setEnabled(true);
                if (flightComboBox.getItemCount() > 0) {
                    flightComboBox.setSelectedIndex(0);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading flights: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            flightComboBox.addItem("Error loading flights.");
            flightComboBox.setEnabled(false);
        }
    }

    /**
     * Retrieves the reporting_time (departure) and arrival_time for a specific flight.
     *
     * @return An array [Timestamp reporting_time, Timestamp arrival_time], or null if error/not found.
     */
    private Timestamp[] getFlightTimestamps(int flightId) {
        String sql = "SELECT reporting_time, arrival_time FROM flight WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, flightId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Timestamp reportingTime = rs.getTimestamp("reporting_time");
                Timestamp arrivalTime = rs.getTimestamp("arrival_time");
                return new Timestamp[]{reportingTime, arrivalTime};
            } else {
                // This case should ideally not be reached if flightId comes from a populated list
                JOptionPane.showMessageDialog(frame, "Flight details not found for ID: " + flightId,
                        "Data Not Found", JOptionPane.WARNING_MESSAGE);
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error fetching flight times for ID " + flightId + ": " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // --- MODIFIED createUpdateFlightCard ---


    private void populateDateTimeWithCurrent(JComboBox<Integer> yearCombo, JComboBox<Integer> monthCombo, JComboBox<Integer> dayCombo,
                                             JComboBox<Integer> hourCombo, JComboBox<Integer> minuteCombo, JComboBox<Integer> secondCombo) {
        Calendar nowCal = Calendar.getInstance();
        yearCombo.setSelectedItem(nowCal.get(Calendar.YEAR));
        monthCombo.setSelectedItem(nowCal.get(Calendar.MONTH) + 1);
        updateDays(yearCombo, monthCombo, dayCombo, nowCal.get(Calendar.DAY_OF_MONTH));
        dayCombo.setSelectedItem(nowCal.get(Calendar.DAY_OF_MONTH));
        updateHourComboBoxes(yearCombo, monthCombo, dayCombo, hourCombo, hourCombo);
        if (hourCombo.getItemCount() > 0) {
            int currentHour = nowCal.get(Calendar.HOUR_OF_DAY);
            boolean currentHourFound = false;
            for (int i = 0; i < hourCombo.getItemCount(); i++) {
                if (hourCombo.getItemAt(i) == currentHour) {
                    hourCombo.setSelectedItem(currentHour);
                    currentHourFound = true;
                    break;
                }
            }
            if (!currentHourFound)
                hourCombo.setSelectedIndex(0);
        }
        minuteCombo.setSelectedItem(nowCal.get(Calendar.MINUTE));
        secondCombo.setSelectedItem(nowCal.get(Calendar.SECOND));
    }

    private Timestamp getDateTimeFromPickers(JComboBox<Integer> yearCombo, JComboBox<Integer> monthCombo, JComboBox<Integer> dayCombo,
                                             JComboBox<Integer> hourCombo, JComboBox<Integer> minuteCombo, JComboBox<Integer> secondCombo) {
        Integer year = (Integer) yearCombo.getSelectedItem();
        Integer month = (Integer) monthCombo.getSelectedItem();
        Integer day = (Integer) dayCombo.getSelectedItem();
        Integer hour = (Integer) hourCombo.getSelectedItem();
        Integer minute = (Integer) minuteCombo.getSelectedItem();
        Integer second = (Integer) secondCombo.getSelectedItem();

        if (year == null || month == null || day == null || hour == null || minute == null || second == null) {
            throw new IllegalArgumentException("Please select a complete date and time.");
        }
        boolean hourIsValid = false;
        DefaultComboBoxModel<Integer> hourModel = (DefaultComboBoxModel<Integer>) hourCombo.getModel();
        for (int i = 0; i < hourModel.getSize(); i++) {
            if (hourModel.getElementAt(i).equals(hour)) {
                hourIsValid = true;
                break;
            }
        }
        if (!hourIsValid) {
            throw new IllegalArgumentException("Selected hour (" + hour + ") is not valid for the chosen date. Please re-select.");
        }

        LocalDate selectedDate = LocalDate.of(year, month, day);
        LocalTime selectedTime = LocalTime.of(hour, minute, second);
        return Timestamp.valueOf(selectedDate.atTime(selectedTime));
    }

    private void updateFlight(int flightId, String columnName, Object newValue) {
        if (columnName == null || columnName.trim().isEmpty() || columnName.equalsIgnoreCase("id")) {
            JOptionPane.showMessageDialog(frame, "Invalid field to update: " + columnName, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String query = "UPDATE flight SET " + columnName + " = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            if (newValue instanceof String) {
                preparedStatement.setString(1, (String) newValue);
            } else if (newValue instanceof Timestamp) {
                preparedStatement.setTimestamp(1, (Timestamp) newValue);
            } else if (newValue instanceof Integer) {
                preparedStatement.setInt(1, (Integer) newValue);
            } else if (newValue instanceof Float) {
                preparedStatement.setFloat(1, (Float) newValue);
            } else if (newValue instanceof Double) {
                preparedStatement.setDouble(1, (Double) newValue);
            } else if (newValue == null) {
                JOptionPane.showMessageDialog(frame, "New value cannot be null for update.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                JOptionPane.showMessageDialog(frame, "Unsupported data type for update: " + newValue.getClass().getName(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            preparedStatement.setInt(2, flightId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(frame, "Flight " + flightId + ", field '" + columnName + "' updated successfully!");
            } else {
                JOptionPane.showMessageDialog(frame, "Flight " + flightId + " not found or no changes made.", "Warning", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Failed to update flight: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUpdateButtonState(boolean isFlightSelected, boolean isNewValueValid) {
        if (updateFlightButton != null) {
            updateFlightButton.setEnabled(isFlightSelected && isNewValueValid);
        }
    }

    private String[][] extractData(ResultSet resultSet, int columnCount) throws SQLException {
        if (resultSet == null) {
            return new String[][]{};
        }
        try {
            resultSet.last();
            int rowCount = resultSet.getRow();
            resultSet.beforeFirst();

            String[][] data = new String[rowCount][columnCount];
            int row = 0;

            while (resultSet.next()) {
                for (int col = 0; col < columnCount; col++) {
                    data[row][col] = resultSet.getString(col + 1);
                }
                row++;
            }
            return data;
        } catch (SQLException e) {
            System.err.println("Warning: ResultSet might not be scrollable. Falling back to List-based extraction.");
            resultSet.beforeFirst();

            java.util.List<String[]> dataList = new java.util.ArrayList<>();
            while (resultSet.next()) {
                String[] rowData = new String[columnCount];
                for (int col = 0; col < columnCount; col++) {
                    rowData[col] = resultSet.getString(col + 1);
                }
                dataList.add(rowData);
            }
            return dataList.toArray(new String[0][0]);
        }
    }

    private String[][] fetchPlanes(int adminId) {
        String query = "SELECT id, plane_model, manufacturer, business_seats, economy_seats " +
                "FROM plane WHERE admin_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
        )) {
            preparedStatement.setInt(1, adminId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return extractData(resultSet, 5);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new String[][]{};
        }
    }

    private void refreshPlanesTable() {
        String[] columns = {"Plane ID", "Plane Model", "Manufacturer", "Business Seats", "Economy Seats"};
        String[][] newData = fetchPlanes(this.adminId);
        DefaultTableModel model = new DefaultTableModel(newData, columns);
        planesTable.setModel(model);
        planesTable.repaint();
    }

    private String[][] fetchBookings() {
        String sql = "SELECT b.id, " +
                "c.name AS client_name, " +
                "CONCAT(f.source, ' -> ', f.destination) AS flight_route, " +
                "DATE_FORMAT(f.reporting_time, '%Y-%m-%d %H:%i') AS flight_date, " +
                "b.seat_type, " +
                "b.fees, " +
                "CASE " +
                "   WHEN b.ispaid = 1 AND b.isreserved = 0 THEN 'Paid' " +
                "   WHEN b.isreserved = 1 AND b.ispaid = 0 THEN 'Reserved' " +
                "   WHEN b.ispaid = 1 AND b.isreserved = 1 THEN 'Paid & Reserved' " +
                "   ELSE 'Unknown' " +
                "END AS status " +
                "FROM booking b " +
                "JOIN client c ON b.client_id = c.id " +
                "JOIN flight f ON b.flight_id = f.id " +
                "JOIN plane p ON f.plane_id = p.id " +
                "WHERE p.admin_id = ? " +
                "ORDER BY f.reporting_time DESC";

        try (PreparedStatement statement = connection.prepareStatement(sql,
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY)) {

            statement.setInt(1, this.adminId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return extractData(resultSet, 7);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new String[0][0];
        }
    }

    private String[][] fetchFlights() {
        try (Statement statement = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
        );
             ResultSet resultSet = statement.executeQuery("SELECT id, source, destination, reporting_time, arrival_time FROM flight")) {

            return extractData(resultSet, 5);
        } catch (SQLException e) {
            e.printStackTrace();
            return new String[][]{};
        }
    }

    private void refreshFlightsTable() {
        String[] columns = {"Flight ID", "Source", "Destination", "Reporting Time", "Arrival Time"};
        String[][] newData = fetchFlights();
        DefaultTableModel model = new DefaultTableModel(newData, columns);
        flightsTable.setModel(model);
        flightsTable.repaint();
    }

    private JPanel createViewPlanesCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(220, 240, 255));
        JLabel titleLabel = new JLabel("VIEW PLANES", JLabel.CENTER);
        titleLabel.setFont(new Font("Poppins", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        card.add(titleLabel, BorderLayout.NORTH);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(220, 240, 255));
        JButton refreshButton = createStyledButton("Refresh", e -> refreshPlanesTable());
        buttonPanel.add(refreshButton);
        card.add(buttonPanel, BorderLayout.SOUTH);
        String[] columns = {"Plane ID", "Plane Model", "Manufacturer", "Business Seats", "Economy Seats"};
        String[][] data = fetchPlanes(this.adminId);
        this.planesTable = new JTable(new DefaultTableModel(data, columns));
        this.planesTable.setFillsViewportHeight(true);
        this.planesTable.setFont(new Font("Poppins", Font.PLAIN, 14));
        this.planesTable.getTableHeader().setFont(new Font("Poppins", Font.BOLD, 14));
        this.planesTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(this.planesTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }


    private JPanel createViewBookingsCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(220, 240, 255));
        JLabel titleLabel = new JLabel("VIEW BOOKINGS", JLabel.CENTER);
        titleLabel.setFont(new Font("Poppins", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        card.add(titleLabel, BorderLayout.NORTH);
        String[] columns = {"Booking ID", "Client Name", "Flight Route", "Flight Date", "Status"};
        String[][] data = fetchBookings();
        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setFont(new Font("Poppins", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Poppins", Font.BOLD, 14));
        table.setRowHeight(25);
        table.setAutoCreateRowSorter(true);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));
        card.add(scrollPane, BorderLayout.CENTER);
        JButton refreshButton = createStyledButton("Refresh", e -> {
            String[][] newData = fetchBookings();
            DefaultTableModel newModel = new DefaultTableModel(newData, columns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            table.setModel(newModel);
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(220, 240, 255));
        buttonPanel.add(refreshButton);
        card.add(buttonPanel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createViewFlightsCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(220, 240, 255));
        JLabel titleLabel = new JLabel("VIEW FLIGHTS", JLabel.CENTER);
        titleLabel.setFont(new Font("Poppins", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        card.add(titleLabel, BorderLayout.NORTH);
        String[] columns = {"Flight ID", "Source", "Destination", "Reporting Time", "Arrival Time"};
        String[][] data = fetchFlights();
        this.flightsTable = new JTable(new DefaultTableModel(data, columns));
        this.flightsTable.setFillsViewportHeight(true);
        this.flightsTable.setFont(new Font("Poppins", Font.PLAIN, 14));
        this.flightsTable.getTableHeader().setFont(new Font("Poppins", Font.BOLD, 14));
        this.flightsTable.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(this.flightsTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));
        card.add(scrollPane, BorderLayout.CENTER);
        JButton refreshButton = createStyledButton("Refresh", e -> refreshFlightsTable());
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(220, 240, 255));
        bottomPanel.add(refreshButton);
        card.add(bottomPanel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createMyFlightsCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(220, 240, 255));
        JLabel titleLabel = new JLabel("MY FLIGHTS", JLabel.CENTER);
        titleLabel.setFont(new Font("Poppins", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        card.add(titleLabel, BorderLayout.NORTH);
        String[] columns = {"Flight ID", "Source", "Destination", "Reporting Time", "Arrival Time", "Expense", "Collected Fare"};
        String[][] data = fetchMyFlights(this.adminId);
        JTable table = new JTable(new DefaultTableModel(data, columns));
        table.setFillsViewportHeight(true);
        table.setFont(new Font("Poppins", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Poppins", Font.BOLD, 14));
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY)
        ));
        card.add(scrollPane, BorderLayout.CENTER);
        JButton refreshButton = createStyledButton("Refresh", e -> {
            String[][] updatedData = fetchMyFlights(this.adminId);
            DefaultTableModel model = new DefaultTableModel(updatedData, columns);
            table.setModel(model);
            table.repaint();
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(220, 240, 255));
        buttonPanel.add(refreshButton);
        card.add(buttonPanel, BorderLayout.SOUTH);
        return card;
    }
    private String[][] fetchMyFlights(int adminId) {
        String query = "SELECT f.id, f.source, f.destination, f.reporting_time, f.arrival_time, f.expense, f.collected_fare " +
                "FROM flight f " +
                "JOIN plane p ON f.plane_id = p.id " +
                "WHERE p.admin_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                query, ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY
        )) {
            preparedStatement.setInt(1, adminId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return extractData(resultSet, 7);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new String[][]{};
        }
    }

    private int countAdminFlights(int adminId) {
        int flightCount = 0;
        try {
            String query = "SELECT COUNT(*) FROM flight f " +
                    "JOIN plane p ON f.plane_id = p.id " +
                    "WHERE p.admin_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, adminId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                flightCount = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error counting flights: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return flightCount;
    }

    private int countAdminPlanes(int adminId) {
        int planeCount = 0;
        try {
            String query = "SELECT COUNT(*) FROM plane WHERE admin_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, adminId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                planeCount = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error counting planes: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return planeCount;
    }

    private String formatCurrency(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###.00");
        return formatter.format(amount);
    }

    private JPanel createMyDetailsCard() {
        int flightCount = countAdminFlights(adminId);
        int planeCount = countAdminPlanes(adminId);

        JPanel outerPanel = new JPanel(new GridBagLayout());
        outerPanel.setBackground(new Color(220, 240, 255));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(250, 250, 255));
        Border innerBorder = BorderFactory.createEmptyBorder(20, 20, 20, 20);
        Border lineBorder = BorderFactory.createLineBorder(new Color(180, 190, 200), 1);
        Border titledBorder = BorderFactory.createTitledBorder(
                lineBorder,
                "Admin Dashboard",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16),
                new Color(50, 70, 90));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(innerBorder, titledBorder));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(adminName, 20);
        nameField.setEditable(false); nameField.setBackground(new Color(240, 240, 240));
        contentPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        contentPanel.add(new JLabel("Company:"), gbc);
        gbc.gridx = 1;
        JTextField companyField = new JTextField(companyName, 20);
        companyField.setEditable(false); companyField.setBackground(new Color(240, 240, 240));
        contentPanel.add(companyField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        contentPanel.add(new JLabel("Profit:"), gbc);
        gbc.gridx = 1;
        String formattedProfit = "$" + formatCurrency(Double.parseDouble(String.valueOf(this.adminProfit)));
        JTextField profitField = new JTextField(formattedProfit, 20);
        profitField.setEditable(false); profitField.setBackground(new Color(240, 240, 240));
        contentPanel.add(profitField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        contentPanel.add(new JLabel("Total Flights:"), gbc);
        gbc.gridx = 1;
        JTextField flightsField = new JTextField(String.valueOf(flightCount), 20);
        flightsField.setEditable(false); flightsField.setBackground(new Color(240, 240, 240));
        contentPanel.add(flightsField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        contentPanel.add(new JLabel("Total Planes:"), gbc);
        gbc.gridx = 1;
        JTextField planesField = new JTextField(String.valueOf(planeCount), 20);
        planesField.setEditable(false); planesField.setBackground(new Color(240, 240, 240));
        contentPanel.add(planesField, gbc);

        if (flightCount > 0 && planeCount > 0) {
            gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
            double flightsPerPlane = (double) flightCount / planeCount;
            DecimalFormat df = new DecimalFormat("#.##");
            JLabel statsLabel = new JLabel("Average Flights per Plane: " + df.format(flightsPerPlane));
            statsLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            statsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            contentPanel.add(statsLabel, gbc);
        }

        outerPanel.add(contentPanel, new GridBagConstraints());
        return outerPanel;
    }

    private void addLabelAndValue(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(nameLabel, gbc);

        gbc.gridx = 1;
        JLabel valueLabel = new JLabel(value);
        panel.add(valueLabel, gbc);
    }

    private void logout() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        frame.dispose();
        JOptionPane.showMessageDialog(null, "Logged out successfully!");
        new LoginTry();
    }
}