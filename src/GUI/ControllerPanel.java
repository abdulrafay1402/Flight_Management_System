package GUI;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ControllerPanel {
    private JFrame frame;
    private Connection connection;
    private static final String ADMIN_EMAIL = "./Controller";
    private static final String ADMIN_PASSWORD = "arafay_1402";
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Color scheme
    private static final Color PRIMARY_COLOR = new Color(0, 150, 136); // Teal
    private static final Color SECONDARY_COLOR = new Color(45, 45, 48); // Dark gray
    private static final Color ACCENT_COLOR = new Color(255, 153, 51); // Orange
    private static final Color TEXT_COLOR = new Color(0, 150, 136); // Light text
    private static final Color HEADER_COLOR = new Color(60, 60, 65); // Darker gray
    private static final Color ROW_COLOR_1 = new Color(50, 50, 55); // Dark row
    private static final Color ROW_COLOR_2 = new Color(60, 60, 65); // Darker row
    private static final Color SELECTION_COLOR = new Color(0, 120, 110); // Dark teal
    private static final Color GRID_COLOR = new Color(80, 80, 80); // Grid lines

    public ControllerPanel(Connection connection) {
        this.connection = connection;
        initializePanel();
    }

    private void initializePanel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Table.background", SECONDARY_COLOR);
            UIManager.put("Table.foreground", TEXT_COLOR);
            UIManager.put("Table.gridColor", GRID_COLOR);
            UIManager.put("Table.selectionBackground", SELECTION_COLOR);
            UIManager.put("TableHeader.background", HEADER_COLOR);
            UIManager.put("TableHeader.foreground", TEXT_COLOR);
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame("Database Controller Panel");
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(SECONDARY_COLOR);

        // Main tabbed pane with styled buttons
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP) {
            @Override
            public void updateUI() {
                super.updateUI();
                setBackground(SECONDARY_COLOR);
                setForeground(TEXT_COLOR);
                setFont(new Font("Segoe UI", Font.BOLD, 14));
                setFocusable(false);
            }
        };

        // Add tabs with consistent styling
        addTabWithStyle(tabbedPane, "Admins", createTablePanel("SELECT * FROM admin"));
        addTabWithStyle(tabbedPane, "Clients", createTablePanel("SELECT * FROM client"));
        addTabWithStyle(tabbedPane, "Planes", createTablePanel("SELECT * FROM plane"));
        addTabWithStyle(tabbedPane, "Flights", createFlightPanel());
        addTabWithStyle(tabbedPane, "Bookings", createBookingPanel());

        frame.add(tabbedPane, BorderLayout.CENTER);

        // Add refresh button with consistent styling
        JButton refreshButton = createStyledButton("Refresh All Data", e -> refreshAllTabs(tabbedPane));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(SECONDARY_COLOR);
        buttonPanel.add(refreshButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addTabWithStyle(JTabbedPane tabbedPane, String title, JComponent component) {
        tabbedPane.addTab(title, component);
        int index = tabbedPane.indexOfTab(title);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(TEXT_COLOR);
        tabbedPane.setTabComponentAt(index, label);
    }

    private JButton createStyledButton(String text, java.awt.event.ActionListener action) {
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
                    g2.setColor(ACCENT_COLOR.brighter());
                } else {
                    g2.setColor(ACCENT_COLOR);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(this.getText(), g2);
                int x = (this.getWidth() - (int) r.getWidth()) / 2;
                int y = (this.getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                g2.drawString(this.getText(), x, y);

                g2.dispose();
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(200, 40));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.addActionListener(action);
        return button;
    }

    private JPanel createFlightPanel() {
        JPanel flightPanel = new JPanel(new BorderLayout());
        flightPanel.setBackground(SECONDARY_COLOR);

        // Future flights
        JPanel futurePanel = createTablePanel(
                "SELECT id, plane_id, source, destination, " +
                        "reporting_time, arrival_time, expense, collected_fare " +
                        "FROM flight WHERE reporting_time > NOW() ORDER BY reporting_time ASC",
                "Upcoming Flights"
        );
        futurePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                "Upcoming Flights", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), PRIMARY_COLOR));

        // Past flights
        JPanel pastPanel = createTablePanel(
                "SELECT id, plane_id, source, destination, " +
                        "reporting_time, arrival_time, expense, collected_fare " +
                        "FROM flight WHERE reporting_time <= NOW() ORDER BY reporting_time DESC",
                "Completed Flights"
        );
        pastPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                "Completed Flights", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), PRIMARY_COLOR));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, futurePanel, pastPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        flightPanel.add(splitPane, BorderLayout.CENTER);
        return flightPanel;
    }
    private JPanel createTablePanel(String query) {
        return createTablePanel(query, null);
    }

    private JPanel createTablePanel(String query, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SECONDARY_COLOR);

        if (title != null) {
            panel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                    title, TitledBorder.LEFT, TitledBorder.TOP,
                    new Font("Segoe UI", Font.BOLD, 12), TEXT_COLOR));
        }

        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData metaData = rs.getMetaData();

            int columnCount = metaData.getColumnCount();
            String[] columnNames = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i-1] = metaData.getColumnName(i);
            }

            List<Object[]> data = new ArrayList<>();
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    if (value instanceof Timestamp) {
                        LocalDateTime dateTime = ((Timestamp) value).toLocalDateTime();
                        row[i-1] = dateTime.format(DATE_TIME_FORMATTER);
                    } else {
                        row[i-1] = value;
                    }
                }
                data.add(row);
            }

            DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                @Override
                public Class<?> getColumnClass(int column) {
                    if (getRowCount() > 0 && getValueAt(0, column) != null) {
                        return getValueAt(0, column).getClass();
                    }
                    return Object.class;
                }
            };

            JTable table = new JTable(model) {
                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                    Component c = super.prepareRenderer(renderer, row, column);

                    if (!isRowSelected(row)) {
                        c.setBackground(row % 2 == 0 ? ROW_COLOR_1 : ROW_COLOR_2);
                        c.setForeground(TEXT_COLOR);
                    }

                    String colName = getColumnName(column).toLowerCase();
                    if (colName.contains("status") || colName.contains("paid")) {
                        Object value = getValueAt(row, column);
                        if (value != null) {
                            String valStr = value.toString().toLowerCase();
                            if (valStr.contains("active") || valStr.contains("paid")) {
                                c.setForeground(new Color(100, 255, 100)); // Light green
                            } else if (valStr.contains("cancel") || valStr.contains("pending")) {
                                c.setForeground(new Color(255, 100, 100)); // Light red
                            }
                        }
                    }
                    return c;
                }
            };

            for (Object[] row : data) {
                model.addRow(row);
            }

            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            table.setFillsViewportHeight(true);
            table.setRowHeight(30);
            table.setAutoCreateRowSorter(true);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            table.setGridColor(GRID_COLOR);
            table.setSelectionBackground(SELECTION_COLOR);
            table.setSelectionForeground(Color.WHITE);

            JTableHeader header = table.getTableHeader();
            header.setBackground(HEADER_COLOR);
            header.setForeground(TEXT_COLOR);
            header.setFont(new Font("Segoe UI", Font.BOLD, 12));

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            panel.add(scrollPane, BorderLayout.CENTER);

            JPanel footerPanel = new JPanel(new GridLayout(1, 3, 10, 0));
            footerPanel.setBackground(SECONDARY_COLOR);
            footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            JLabel countLabel = new JLabel("Total Records: " + data.size());
            styleFooterLabel(countLabel);
            footerPanel.add(countLabel);

            if (title != null && title.toLowerCase().contains("flight")) {
                try {
                    Statement dateStmt = connection.createStatement();
                    ResultSet dateRs = dateStmt.executeQuery(
                            query.replace("*", "MIN(reporting_time) as first, MAX(reporting_time) as last")
                    );
                    if (dateRs.next()) {
                        JLabel earliestLabel = new JLabel("Earliest: " + formatTimestamp(dateRs.getTimestamp("first")));
                        styleFooterLabel(earliestLabel);
                        footerPanel.add(earliestLabel);

                        JLabel latestLabel = new JLabel("Latest: " + formatTimestamp(dateRs.getTimestamp("last")));
                        styleFooterLabel(latestLabel);
                        footerPanel.add(latestLabel);
                    }
                } catch (SQLException e) {
                    JLabel errorLabel = new JLabel("Date range unavailable");
                    styleFooterLabel(errorLabel);
                    footerPanel.add(errorLabel);
                }
            }
            panel.add(footerPanel, BorderLayout.SOUTH);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            panel.add(new JLabel("Error loading data"), BorderLayout.CENTER);
        }

        return panel;
    }

    private JPanel createBookingPanel() {
        JPanel bookingPanel = new JPanel(new BorderLayout());
        bookingPanel.setBackground(SECONDARY_COLOR);

        String query = "SELECT " +
                "b.id as booking_id, " +
                "c.name as client_name, " +
                "CONCAT(f.source, ' → ', f.destination) as flight_route, " +
                "f.reporting_time, " +
                "b.seat_type, " +
                "b.fees, " +
                "CASE WHEN b.ispaid THEN 'Paid' ELSE 'Pending' END as payment_status " +
                "FROM booking b " +
                "LEFT JOIN client c ON b.client_id = c.id " +
                "LEFT JOIN flight f ON b.flight_id = f.id " +
                "ORDER BY b.id DESC";

        try {
            JPanel tablePanel = createTablePanel(query, "All Bookings");
            bookingPanel.add(tablePanel, BorderLayout.CENTER);

            // Add summary statistics
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT COUNT(*) as total_bookings, " +
                                 "SUM(fees) as total_revenue, " +
                                 "AVG(fees) as avg_booking " +
                                 "FROM booking")) {

                if (rs.next()) {
                    JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
                    statsPanel.setBackground(new Color(60, 70, 75));
                    statsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

                    JLabel totalLabel = createStatLabel(String.format("Total Bookings: %,d", rs.getInt("total_bookings")));
                    JLabel revenueLabel = createStatLabel(String.format("Total Revenue: $%,.2f",
                            rs.getObject("total_revenue") != null ? rs.getDouble("total_revenue") : 0));
                    JLabel avgLabel = createStatLabel(String.format("Avg. Booking: $%,.2f",
                            rs.getObject("avg_booking") != null ? rs.getDouble("avg_booking") : 0));

                    statsPanel.add(totalLabel);
                    statsPanel.add(revenueLabel);
                    statsPanel.add(avgLabel);

                    bookingPanel.add(statsPanel, BorderLayout.SOUTH);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame,
                    "Error loading booking data: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            bookingPanel.add(new JLabel("Error loading booking data"), BorderLayout.CENTER);
        }

        return bookingPanel;
    }

    private void styleFooterLabel(JLabel label) {
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_COLOR);
    }

    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(PRIMARY_COLOR);
        return label;
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "N/A";
        return timestamp.toLocalDateTime().format(DATE_TIME_FORMATTER);
    }

    private void refreshAllTabs(JTabbedPane tabbedPane) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component comp = tabbedPane.getComponentAt(i);
            if (comp instanceof JPanel) {
                if (i == 3) { // Flights tab
                    tabbedPane.setComponentAt(i, createFlightPanel());
                } else if (i == 4) { // Bookings tab
                    tabbedPane.setComponentAt(i, createBookingPanel());
                } else {
                    String query = getQueryForTab(i);
                    tabbedPane.setComponentAt(i, createTablePanel(query));
                }
            }
        }
    }

    private String getQueryForTab(int tabIndex) {
        switch (tabIndex) {
            case 0: return "SELECT * FROM admin";
            case 1: return "SELECT * FROM client";
            case 2: return "SELECT * FROM plane";
            case 4: return "SELECT * FROM booking";
            default: return "";
        }
    }

    public static boolean isAdminLogin(String email, String password) {
        return ADMIN_EMAIL.equals(email) && ADMIN_PASSWORD.equals(password);
    }

    public void show() {
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.toFront();
    }
}