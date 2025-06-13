package GUI;
import Exceptions.UserAlreadyExistsException;
import Model_and_Interfaces.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
public class SignUpPanel extends JPanel {
    private final LoginTry loginTry;
    private final JFrame frame;
    private final ImageIcon logoIcon;
    private final ImageIcon planeIcon;
    private JPanel mainPanel;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private CardLayout cardLayout;
    private int panelWidth;
    private JPanel centerPanel;
    private final Connection connection;

    public SignUpPanel(LoginTry loginTry, JFrame frame, ImageIcon logoIcon, ImageIcon planeIcon, Connection connection) {
        this.loginTry = loginTry;
        this.frame = frame;
        this.logoIcon = logoIcon;
        this.planeIcon = planeIcon;
        this.connection = connection;
        setLayout(new BorderLayout());
        panelWidth = frame.getWidth() / 2;
        setVisible(false);
    }

    public void initializeComponents() {
        removeAll();

        mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        createSwappedUI();
        setVisible(true);
        revalidate();
        repaint();
    }

    private void createSwappedUI() {
        leftPanel = new JPanel();
        leftPanel.setBackground(new Color(220, 240, 255));
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        leftPanel.setPreferredSize(new Dimension(panelWidth, frame.getHeight()));
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel planeLabel = new JLabel(planeIcon);
        planeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(logoLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 50)));
        leftPanel.add(planeLabel);
        leftPanel.add(Box.createVerticalGlue());
        rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setPreferredSize(new Dimension(panelWidth, frame.getHeight()));
        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.setOpaque(false);

        // Role selection panel
        JPanel roleSelectionPanel = createRoleSelectionPanel();
        JPanel adminSignUpPanel = createAdminSignUpPanel();
        JPanel clientSignUpPanel = createClientSignUpPanel();

        centerPanel.add(roleSelectionPanel, "ROLE");
        centerPanel.add(adminSignUpPanel, "ADMIN");
        centerPanel.add(clientSignUpPanel, "CLIENT");

        rightPanel.add(centerPanel);

        // Add panels to main panel in swapped order
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.EAST);
    }

    private JPanel createRoleSelectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 100, 10, 100);

        JLabel selectRoleLabel = new JLabel("Select Your Role");
        selectRoleLabel.setFont(new Font("Poppins", Font.BOLD, 32));
        panel.add(selectRoleLabel, gbc);

        gbc.insets = new Insets(30, 100, 10, 100);

        JButton adminButton = createStyledButton("Admin", e -> cardLayout.show(centerPanel, "ADMIN"));
        panel.add(adminButton, gbc);

        JButton clientButton = createStyledButton("Client", e -> cardLayout.show(centerPanel, "CLIENT"));
        panel.add(clientButton, gbc);

        // Back to login link
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        backPanel.setBackground(Color.WHITE);
        backPanel.setOpaque(false);
        JLabel backLabel = new JLabel("Back to Login");
        backLabel.setFont(new Font("Poppins", Font.BOLD, 14));
        backLabel.setForeground(new Color(0, 150, 136));
        backLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                animateBackToLogin();
            }
        });
        backPanel.add(backLabel);

        gbc.insets = new Insets(30, 100, 10, 100);
        panel.add(backPanel, gbc);

        return panel;
    }

    private JPanel createAdminSignUpPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 100, 10, 100);
        JLabel titleLabel = new JLabel("Admin Sign Up");
        titleLabel.setFont(new Font("Poppins", Font.BOLD, 32));
        panel.add(titleLabel, gbc);

        gbc.insets = new Insets(15, 100, 5, 100);

        JTextField nameField = new JTextField();
        addFormField(panel, gbc, "Name", nameField);

        JPasswordField passwordField = new JPasswordField();
        addFormField(panel, gbc, "Password", passwordField);

        JTextField companyField = new JTextField();
        addFormField(panel, gbc, "Company Name", companyField);

        gbc.insets = new Insets(30, 100, 10, 100);

        JButton signUpButton = createStyledButton("Sign Up", e -> {
            // Admin Sign-Up Logic
            String name = nameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String companyName = companyField.getText().trim();
            String email = name + "@ats.com";

            if (name.isEmpty() || password.isEmpty() || companyName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                User.signupAdmin(connection, name, password, email, companyName);
                JOptionPane.showMessageDialog(frame,
                        "ADMIN Sign-Up Successful!\nYour email is: " + email,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (UserAlreadyExistsException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(signUpButton, gbc);

        // Back to role selection
        JPanel backPanel = createBackPanel("Back to Role Selection", () -> cardLayout.show(centerPanel, "ROLE"));
        gbc.insets = new Insets(10, 100, 10, 100);
        panel.add(backPanel, gbc);

        return panel;
    }

    private JPanel createClientSignUpPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 100, 10, 100);

        JLabel titleLabel = new JLabel("Client Sign Up");
        titleLabel.setFont(new Font("Poppins", Font.BOLD, 32));
        panel.add(titleLabel, gbc);

        gbc.insets = new Insets(15, 100, 5, 100);

        JTextField nameField = new JTextField();
        addFormField(panel, gbc, "Name", nameField);

        JTextField ageField = new JTextField();
        addFormField(panel, gbc, "Age", ageField);

        // Gender selection using radio buttons (Male, Female only)
        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setFont(new Font("Poppins", Font.PLAIN, 16));
        panel.add(genderLabel, gbc);

        JRadioButton maleButton = new JRadioButton("Male");
        maleButton.setFont(new Font("Poppins", Font.PLAIN, 14));
        maleButton.setBackground(Color.WHITE);

        JRadioButton femaleButton = new JRadioButton("Female");
        femaleButton.setFont(new Font("Poppins", Font.PLAIN, 14));
        femaleButton.setBackground(Color.WHITE);

        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleButton);
        genderGroup.add(femaleButton);

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        genderPanel.setBackground(Color.WHITE);
        genderPanel.add(maleButton);
        genderPanel.add(femaleButton);
        panel.add(genderPanel, gbc);

        JPasswordField passwordField = new JPasswordField();
        addFormField(panel, gbc, "Password", passwordField);

        gbc.insets = new Insets(30, 100, 10, 100);

        JButton signUpButton = createStyledButton("Sign Up", e -> {
            // Client Sign-Up Logic
            String name = nameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String ageText = ageField.getText().trim();
            String gender = null;

            if (maleButton.isSelected()) {
                gender = "Male";
            } else if (femaleButton.isSelected()) {
                gender = "Female";
            }

            if (name.isEmpty() || password.isEmpty() || ageText.isEmpty() || gender == null) {
                JOptionPane.showMessageDialog(frame, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int age = Integer.parseInt(ageText);
                String email = name.toLowerCase().replaceAll(" ", ".") + "@client.com"; // Generate email
                User.signupClient(connection, name, password, email, age, gender);
                JOptionPane.showMessageDialog(frame,
                        "Client Sign-Up Successful!\nYour email is: " + email,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Age must be a valid number!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (UserAlreadyExistsException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(signUpButton, gbc);

        // Back to role selection
        JPanel backPanel = createBackPanel("Back to Role Selection", () -> cardLayout.show(centerPanel, "ROLE"));
        gbc.insets = new Insets(10, 100, 10, 100);
        panel.add(backPanel, gbc);

        return panel;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Poppins", Font.PLAIN, 16));
        panel.add(label, gbc);

        field.setPreferredSize(new Dimension(400, 30));
        field.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));
        panel.add(field, gbc);
    }

    private JPanel createBackPanel(String text, Runnable action) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panel.setBackground(Color.WHITE);
        panel.setOpaque(false);
        JLabel backLabel = new JLabel(text);
        backLabel.setFont(new Font("Poppins", Font.BOLD, 14));
        backLabel.setForeground(new Color(0, 150, 136));
        backLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });
        panel.add(backLabel);
        return panel;
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

    private void animateBackToLogin() {
        // Create animation container
        final int width = frame.getWidth();
        final int height = frame.getHeight();

        JPanel animContainer = new JPanel(null);
        animContainer.setSize(width, height);
        animContainer.setBackground(Color.WHITE);

        // Create left panel copy (with plane icon)
        JPanel leftCopy = new JPanel();
        leftCopy.setBounds(panelWidth, 0, panelWidth, height);
        leftCopy.setBackground(new Color(220, 240, 255));
        leftCopy.setLayout(new BoxLayout(leftCopy, BoxLayout.Y_AXIS));

        JLabel planeLabelCopy = new JLabel(planeIcon);
        planeLabelCopy.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftCopy.add(Box.createVerticalGlue());
        leftCopy.add(planeLabelCopy);
        leftCopy.add(Box.createVerticalGlue());

        // Create right panel copy (empty white panel)
        JPanel rightCopy = new JPanel();
        rightCopy.setBounds(0, 0, panelWidth, height);
        rightCopy.setBackground(Color.WHITE);

        animContainer.add(leftCopy);
        animContainer.add(rightCopy);

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

            // Animate left panel moving right
            int newLeftX = (int) (panelWidth * smoothProgress);
            leftCopy.setBounds(newLeftX, 0, panelWidth, height);

            if (step[0] >= totalSteps) {
                timer.stop();
                loginTry.showLoginPanel(); // Call back to LoginTry to show login panel
            }
            frame.repaint();
        });
        timer.start();
    }
}
