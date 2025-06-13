package GUI;

import Main.ConnectionDB;
import Model_and_Interfaces.Admin;
import Model_and_Interfaces.Client;
import Model_and_Interfaces.User;
import Exceptions.UserNotFoundException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LoginTry implements ConnectionDB {
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private ImageIcon logoIcon;
    private ImageIcon planeIcon;
    private JPanel animationPanel;
    private Connection connection;

    public LoginTry() {
        this(null); // Call the main constructor with null connection
    }

    // Main constructor that can be called from elsewhere
    public LoginTry(Connection existingConnection) {
        try {
            frame = new JFrame("ATS Login");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setLayout(new BorderLayout());

            // Load images
            logoIcon = new ImageIcon("ATS_Logo-removebg-preview.png");
            planeIcon = new ImageIcon("10118076_4359602-removebg-preview.png");

            // Use existing connection if provided, otherwise create new one
            if (existingConnection != null) {
                connection = existingConnection;
            } else {
                // Initialize Database Connection using interface constants
                Entry(url, username, password);
            }

            createLoginPanel();
            frame.setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    @Override
    public void Entry(String url, String username, String password) throws SQLException {
        this.connection = DriverManager.getConnection(url, username, password);
    }

    public void createLoginPanel() {
        mainPanel = new JPanel(new BorderLayout());
        frame.getContentPane().removeAll();
        frame.add(mainPanel);
        animationPanel = new JPanel();
        animationPanel.setLayout(new OverlayLayout(animationPanel));
        mainPanel.add(animationPanel, BorderLayout.CENTER);

        leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setOpaque(true);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 100, 10, 100);

        JLabel welcomeLabel = new JLabel("Welcome to ATS");
        welcomeLabel.setFont(new Font("Poppins", Font.BOLD, 32));
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font("Poppins", Font.PLAIN, 16));
        JTextField emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(400, 30));
        emailField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Poppins", Font.PLAIN, 16));

        JPasswordField passField = new JPasswordField();
        passField.setPreferredSize(new Dimension(400, 30));
        passField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));

        JButton loginButton = createStyledButton("Login", e -> {
            // Sign In Logic
            String email = emailField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Email and Password cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (ControllerPanel.isAdminLogin(email, password)) {
                System.out.println("DEBUG: Admin credentials verified successfully");
                frame.dispose();
                System.out.println("DEBUG: Creating ControllerPanel...");
                ControllerPanel controller = new ControllerPanel(connection);
                System.out.println("DEBUG: Showing ControllerPanel...");
                controller.show();
                System.out.println("DEBUG: ControllerPanel should be visible now");
                return;
            }

            try {
                User user = User.getSignedUser(connection, email, password);
                JOptionPane.showMessageDialog(frame, user.getName() + " successfully logged in!", "Success", JOptionPane.INFORMATION_MESSAGE);

                // Check the role and redirect
                String role = user.getRole();
                if ("admin".equalsIgnoreCase(role)) {
                    Admin admin = (Admin) user;
                    frame.dispose();
                    new AdminDashboard(admin.getId(), admin.getName(), admin.getProfit(), admin.getCompanyname());
                } else if ("client".equalsIgnoreCase(role)) {
                    Client client = (Client) user;
                    frame.dispose();
                    new ClientDashboard(client.getId(), client.getName(), client.getEmail(),client.getAge(),client.getGender());
                } else {
                    JOptionPane.showMessageDialog(frame, "Unknown role. Please contact support.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (UserNotFoundException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel signUpPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        signUpPanel.setBackground(Color.WHITE);
        JLabel notMemberLabel = new JLabel("Not a member yet?");
        notMemberLabel.setFont(new Font("Poppins", Font.PLAIN, 14));
        JLabel signUpLabel = new JLabel("Sign Up");
        signUpLabel.setFont(new Font("Poppins", Font.BOLD, 14));
        signUpLabel.setForeground(new Color(0, 150, 136));
        signUpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUpLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                animateToSignUp();
            }
        });
        signUpPanel.add(notMemberLabel);
        signUpPanel.add(signUpLabel);
        leftPanel.add(welcomeLabel, gbc);
        gbc.insets = new Insets(30, 100, 10, 100);
        leftPanel.add(emailLabel, gbc);
        leftPanel.add(emailField, gbc);
        leftPanel.add(passLabel, gbc);
        leftPanel.add(passField, gbc);
        gbc.insets = new Insets(30, 100, 10, 100);
        leftPanel.add(loginButton, gbc);
        leftPanel.add(new JSeparator(), gbc);
        gbc.insets = new Insets(10, 100, 10, 100);
        leftPanel.add(signUpPanel, gbc);

        rightPanel = new JPanel();
        rightPanel.setBackground(new Color(220, 240, 255));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel planeLabel = new JLabel(planeIcon);
        planeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(logoLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 50)));
        rightPanel.add(planeLabel);
        JPanel contentPanel = new JPanel(new GridLayout(1, 2));
        contentPanel.add(leftPanel);
        contentPanel.add(rightPanel);
        animationPanel.add(contentPanel);
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
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                    g2.setColor(new Color(224, 247, 250));
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 50, 50);
                } else {
                    g2.setColor(new Color(224, 247, 250));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);
                }

                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setFont(new Font("Poppins", Font.BOLD, 16));
        button.setForeground(new Color(70, 130, 180));
        button.setPreferredSize(new Dimension(200, 50));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.addActionListener(action);
        return button;
    }

    private void animateToSignUp() {
        final int width = frame.getWidth();
        final int height = frame.getHeight();
        final int panelWidth = width / 2;

        JPanel animContainer = new JPanel(null);
        animContainer.setSize(width, height);
        animContainer.setBackground(Color.WHITE);
        JPanel leftCopy = new JPanel();
        leftCopy.setBounds(0, 0, panelWidth, height);
        leftCopy.setBackground(Color.WHITE);

        JPanel rightCopy = new JPanel();
        rightCopy.setBounds(panelWidth, 0, panelWidth, height);
        rightCopy.setBackground(new Color(220, 240, 255));
        rightCopy.setLayout(new BoxLayout(rightCopy, BoxLayout.Y_AXIS));

        JLabel logoLabelCopy = new JLabel(logoIcon);
        logoLabelCopy.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel planeLabelCopy = new JLabel(planeIcon);
        planeLabelCopy.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightCopy.add(Box.createVerticalGlue());
        rightCopy.add(logoLabelCopy);
        rightCopy.add(Box.createRigidArea(new Dimension(0, 50)));
        rightCopy.add(planeLabelCopy);
        rightCopy.add(Box.createVerticalGlue());
        animContainer.add(leftCopy);
        animContainer.add(rightCopy);

        final SignUpPanel signUpPanel = new SignUpPanel(this, frame, logoIcon, planeIcon, connection);
        frame.getContentPane().removeAll();
        frame.add(animContainer);
        frame.add(signUpPanel);

        Timer timer = new Timer(16, null);
        final int[] step = {0};
        final int totalSteps = 30;

        timer.addActionListener(e -> {
            step[0]++;
            float progress = (float) step[0] / totalSteps;
            float smoothProgress = (float) (1 - Math.cos(progress * Math.PI)) / 2;
            int newRightX = (int) (panelWidth - (panelWidth * smoothProgress));
            rightCopy.setBounds(newRightX, 0, panelWidth, height);

            if (step[0] >= totalSteps) {
                timer.stop();
                frame.getContentPane().removeAll();
                frame.add(signUpPanel);
                signUpPanel.setVisible(true);
                signUpPanel.initializeComponents();
                frame.revalidate();
                frame.repaint();
            }

            frame.repaint();
        });

        timer.start();
    }
    public void showLoginPanel() {
        frame.getContentPane().removeAll();
        createLoginPanel();
        frame.revalidate();
        frame.repaint();
    }

    public void animateBackToLogin(SignUpPanel sourcePanel) {
        // Remove the signup panel
        frame.remove(sourcePanel);

        // Create animation container
        final int width = frame.getWidth();
        final int height = frame.getHeight();
        final int panelWidth = width / 2;

        JPanel animContainer = new JPanel(null);
        animContainer.setSize(width, height);
        animContainer.setBackground(Color.WHITE);

        // Create left panel (with logo)
        JPanel leftPanel = new JPanel();
        leftPanel.setBounds(0, 0, panelWidth, height);
        leftPanel.setBackground(new Color(220, 240, 255));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(logoLabel);
        leftPanel.add(Box.createVerticalGlue());

        // Create right panel (empty white panel)
        JPanel rightPanel = new JPanel();
        rightPanel.setBounds(panelWidth, 0, panelWidth, height);
        rightPanel.setBackground(Color.WHITE);

        animContainer.add(leftPanel);
        animContainer.add(rightPanel);

        frame.getContentPane().removeAll();
        frame.add(animContainer);
        frame.revalidate();
        frame.repaint();

        // Animate the transition
        Timer timer = new Timer(16, null);
        final int[] step = {0};
        final int totalSteps = 30;

        timer.addActionListener(e -> {
            step[0]++;
            float progress = (float) step[0] / totalSteps;
            float smoothProgress = (float) (1 - Math.cos(progress * Math.PI)) / 2;

            // Animate right panel moving left
            int newRightX = (int) (panelWidth - (panelWidth * smoothProgress));
            rightPanel.setBounds(newRightX, 0, panelWidth, height);

            if (step[0] >= totalSteps) {
                timer.stop();
                showLoginPanel();
            }
            frame.repaint();
        });
        timer.start();
    }
}