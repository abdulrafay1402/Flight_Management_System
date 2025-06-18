package GUI;
import Exceptions.UserAlreadyExistsException;
import Model_and_Interfaces.User;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

    // --- Uppercase Document Filter ---
    private static class UppercaseDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (string != null) {
                super.insertString(fb, offset, string.toUpperCase(Locale.ROOT), attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (text != null) {
                super.replace(fb, offset, length, text.toUpperCase(Locale.ROOT), attrs);
            }
        }
    }

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

        JPanel roleSelectionPanel = createRoleSelectionPanel();
        JPanel adminSignUpPanel = createAdminSignUpPanel();
        JPanel clientSignUpPanel = createClientSignUpPanel();

        centerPanel.add(roleSelectionPanel, "ROLE");
        centerPanel.add(adminSignUpPanel, "ADMIN");
        centerPanel.add(clientSignUpPanel, "CLIENT");

        rightPanel.add(centerPanel);

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

        // --- Name Field (Uppercase Forced) ---
        JTextField nameField = new JTextField();
        ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new UppercaseDocumentFilter());
        addFormField(panel, gbc, "Name", nameField);

        JPasswordField passwordField = new JPasswordField();
        addFormField(panel, gbc, "Password", passwordField);

        // --- Company Name Field (Uppercase Forced) ---
        JTextField companyField = new JTextField();
        ((AbstractDocument) companyField.getDocument()).setDocumentFilter(new UppercaseDocumentFilter());
        addFormField(panel, gbc, "Company Name", companyField);

        gbc.insets = new Insets(30, 100, 10, 100);

        JButton signUpButton = createStyledButton("Sign Up", e -> {
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
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 100, 10, 100);

        // --- Title ---
        JLabel titleLabel = new JLabel("Client Sign Up");
        titleLabel.setFont(new Font("Poppins", Font.BOLD, 32));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 100, 25, 100); // More space below title
        panel.add(titleLabel, gbc);

        // --- Form Fields ---
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST; // Align all labels and content to the left

        // --- Name Field ---
        JLabel nameLabel = new JLabel("Name");
        nameLabel.setFont(new Font("Poppins", Font.PLAIN, 16));
        gbc.insets = new Insets(10, 100, 0, 100);
        panel.add(nameLabel, gbc);
        gbc.gridy++;

        JTextField nameField = new JTextField();
        ((AbstractDocument) nameField.getDocument()).setDocumentFilter(new UppercaseDocumentFilter());
        nameField.setPreferredSize(new Dimension(400, 30));
        nameField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));
        gbc.insets = new Insets(0, 100, 15, 100);
        panel.add(nameField, gbc);
        gbc.gridy++;

        // --- Date of Birth Field ---
        JLabel dobLabel = new JLabel("Date of Birth");
        dobLabel.setFont(new Font("Poppins", Font.PLAIN, 16));
        gbc.insets = new Insets(15, 100, 0, 100);
        panel.add(dobLabel, gbc);
        gbc.gridy++;

        JPanel dobContentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        dobContentPanel.setBackground(Color.WHITE);
        dobContentPanel.setOpaque(false);

        JComboBox<String> dayComboBox = createStyledComboBox(getDays());
        JComboBox<String> monthComboBox = createStyledComboBox(getMonths());
        JComboBox<String> yearComboBox = createStyledComboBox(getYears());

        // Adjust sizes to look good together
        dayComboBox.setPreferredSize(new Dimension(80, 30));
        monthComboBox.setPreferredSize(new Dimension(120, 30));
        yearComboBox.setPreferredSize(new Dimension(90, 30));

        dobContentPanel.add(dayComboBox);
        dobContentPanel.add(monthComboBox);
        dobContentPanel.add(yearComboBox);

        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 100, 15, 100);
        panel.add(dobContentPanel, gbc);
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // --- Gender Field ---
        JLabel genderLabel = new JLabel("Gender");
        genderLabel.setFont(new Font("Poppins", Font.PLAIN, 16));
        gbc.insets = new Insets(15, 100, 0, 100);
        panel.add(genderLabel, gbc);
        gbc.gridy++;

        JPanel genderContentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        genderContentPanel.setBackground(Color.WHITE);
        genderContentPanel.setOpaque(false);

        JRadioButton maleButton = new JRadioButton("Male");
        maleButton.setFont(new Font("Poppins", Font.PLAIN, 14));
        maleButton.setBackground(Color.WHITE);
        maleButton.setOpaque(false);
        maleButton.setFocusPainted(false);

        JRadioButton femaleButton = new JRadioButton("Female");
        femaleButton.setFont(new Font("Poppins", Font.PLAIN, 14));
        femaleButton.setBackground(Color.WHITE);
        femaleButton.setOpaque(false);
        femaleButton.setFocusPainted(false);

        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleButton);
        genderGroup.add(femaleButton);

        genderContentPanel.add(maleButton);
        genderContentPanel.add(femaleButton);

        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 100, 15, 100);
        panel.add(genderContentPanel, gbc);
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // --- Password Field ---
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Poppins", Font.PLAIN, 16));
        gbc.insets = new Insets(10, 100, 0, 100);
        panel.add(passwordLabel, gbc);
        gbc.gridy++;

        JPasswordField passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(400, 30));
        passwordField.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.BLACK));
        gbc.insets = new Insets(0, 100, 25, 100);
        panel.add(passwordField, gbc);
        gbc.gridy++;

        // --- Buttons ---
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE; // Buttons should not stretch

        // --- Sign Up Button ---
        gbc.insets = new Insets(10, 100, 10, 100);
        JButton signUpButton = createStyledButton("Sign Up", e -> {
            // Action listener logic remains the same
            String name = nameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String day = (String) dayComboBox.getSelectedItem();
            String month = (String) monthComboBox.getSelectedItem();
            String year = (String) yearComboBox.getSelectedItem();
            String gender = null;

            if (maleButton.isSelected()) {
                gender = "Male";
            } else if (femaleButton.isSelected()) {
                gender = "Female";
            }

            if (name.isEmpty() || password.isEmpty() || day == null || month == null || year == null || gender == null) {
                JOptionPane.showMessageDialog(frame, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String dobStr = String.format("%s-%02d-%02d", year, getMonthNumber(month), Integer.parseInt(day));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                Date dateOfBirth = sdf.parse(dobStr);

                int age = calculateAge(dateOfBirth);
                if (age < 0 || age > 110) {
                    JOptionPane.showMessageDialog(frame, "Age must be between 0 and 110 years.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String email = name.toLowerCase().replaceAll(" ", ".") + "@client.com";
                User.signupClient(connection, name, password, email, dateOfBirth, gender);
                JOptionPane.showMessageDialog(frame,
                        "Client Sign-Up Successful!\nYour email is: " + email,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid date. Please check the day, month, and year.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (UserAlreadyExistsException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(signUpButton, gbc);
        gbc.gridy++;

        // --- Back to Role Selection Link ---
        JPanel backPanel = createBackPanel("Back to Role Selection", () -> cardLayout.show(centerPanel, "ROLE"));
        gbc.insets = new Insets(10, 100, 10, 100);
        panel.add(backPanel, gbc);

        return panel;
    }

    // ===================================================================================
    //  HELPER METHODS
    // ===================================================================================

    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Poppins", Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        // We don't set a custom border here to keep the native look of the combo box arrow
        return comboBox;
    }

    private String[] getDays() {
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) {
            days[i] = String.valueOf(i + 1);
        }
        return days;
    }

    private String[] getMonths() {
        return new String[]{
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
    }

    private String[] getYears() {
        // Go from 1920 up to the current year
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[currentYear - 1920 + 1];
        for (int i = 0; i < years.length; i++) {
            years[i] = String.valueOf(1920 + i);
        }
        return years;
    }

    private int getMonthNumber(String monthName) {
        String[] months = getMonths();
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(monthName)) {
                return i + 1; // Return 1-12
            }
        }
        return 1; // Default to January
    }

    private int calculateAge(Date dateOfBirth) {
        if (dateOfBirth == null) return 0;
        Calendar dob = Calendar.getInstance();
        dob.setTime(dateOfBirth);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field) {
        // This is primarily for the Admin panel now
        gbc.insets = new Insets(15, 100, 5, 100);
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
        final int width = frame.getWidth();
        final int height = frame.getHeight();

        JPanel animContainer = new JPanel(null);
        animContainer.setSize(width, height);
        animContainer.setBackground(Color.WHITE);

        JPanel leftCopy = new JPanel();
        leftCopy.setBounds(panelWidth, 0, panelWidth, height);
        leftCopy.setBackground(new Color(220, 240, 255));
        leftCopy.setLayout(new BoxLayout(leftCopy, BoxLayout.Y_AXIS));

        JLabel planeLabelCopy = new JLabel(planeIcon);
        planeLabelCopy.setAlignmentX(Component.CENTER_ALIGNMENT);
        leftCopy.add(Box.createVerticalGlue());
        leftCopy.add(planeLabelCopy);
        leftCopy.add(Box.createVerticalGlue());

        JPanel rightCopy = new JPanel();
        rightCopy.setBounds(0, 0, panelWidth, height);
        rightCopy.setBackground(Color.WHITE);

        animContainer.add(leftCopy);
        animContainer.add(rightCopy);

        frame.getContentPane().removeAll();
        frame.add(animContainer);
        frame.revalidate();
        frame.repaint();

        Timer timer = new Timer(16, null);
        final int[] step = {0};
        final int totalSteps = 30;

        timer.addActionListener(e -> {
            step[0]++;
            float progress = (float) step[0] / totalSteps;
            float smoothProgress = (float) (1 - Math.cos(progress * Math.PI)) / 2;

            int newLeftX = (int) (panelWidth * smoothProgress);
            leftCopy.setBounds(newLeftX, 0, panelWidth, height);

            if (step[0] >= totalSteps) {
                timer.stop();
                loginTry.showLoginPanel();
            }
            frame.repaint();
        });
        timer.start();
    }
}