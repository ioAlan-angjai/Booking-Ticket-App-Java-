import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    // Warna Dark Mode yang Konsisten
    private final Color BG = new Color(20, 24, 28);      
    private final Color PANEL_BG = new Color(30, 36, 42); 
    private final Color ACCENT = new Color(0, 150, 255); // Biru Traveloka
    private final Color TEXT_COLOR = Color.WHITE;        

    public LoginFrame() {
        setTitle("TioLoka - Login");
        setSize(500, 600); 
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        getContentPane().setBackground(BG); 
        setLayout(new GridBagLayout()); 

        JPanel loginPanel = createLoginPanel(); 
        add(loginPanel);
        
        setVisible(true);
    }
    
    // --- METHOD UTAMA: MEMBUAT PANEL LOGIN ---
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 50, 50), 1),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        )); 
        
        // ===============================================
        // 1. LOGO TEKS "TIOLOKA"
        // ===============================================
        JLabel logoLabel = new JLabel("TioLoka", SwingConstants.LEFT);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 36)); // Ukuran besar
        logoLabel.setForeground(ACCENT); // Warna Biru
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(logoLabel);

        panel.add(Box.createVerticalStrut(20)); // Spasi setelah logo
        
        // 2. Judul Selamat Datang
        JLabel welcomeLabel = new JLabel("Selamat Datang!", SwingConstants.LEFT);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(TEXT_COLOR);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(welcomeLabel);
        
        panel.add(Box.createVerticalStrut(5)); 

        // Sub Judul
        JLabel subLabel = new JLabel("Silakan masuk ke akun Anda.", SwingConstants.LEFT);
        subLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subLabel.setForeground(Color.LIGHT_GRAY);
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(subLabel);

        panel.add(Box.createVerticalStrut(30)); 
        
        // 3. Form Input
        
        // Username
        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(TEXT_COLOR);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(userLabel);
        
        usernameField = new JTextField(20);
        usernameField.setMaximumSize(new Dimension(300, 30));
        usernameField.setBackground(new Color(50, 50, 50));
        usernameField.setForeground(TEXT_COLOR);
        usernameField.setCaretColor(TEXT_COLOR);
        usernameField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(usernameField);
        
        panel.add(Box.createVerticalStrut(15)); 
        
        // Password
        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(TEXT_COLOR);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(passLabel);

        passwordField = new JPasswordField(20);
        passwordField.setMaximumSize(new Dimension(300, 30));
        passwordField.setBackground(new Color(50, 50, 50));
        passwordField.setForeground(TEXT_COLOR);
        passwordField.setCaretColor(TEXT_COLOR);
        passwordField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(passwordField);

        panel.add(Box.createVerticalStrut(30)); 

        // 4. Tombol Aksi
        
        JButton loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(ACCENT);
        loginButton.setForeground(Color.BLACK);
        loginButton.setFocusPainted(false);
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> attemptLogin(usernameField.getText(), passwordField.getPassword()));
        loginButton.setMaximumSize(new Dimension(300, 40));
        panel.add(loginButton);
        
        panel.add(Box.createVerticalStrut(15)); 
        
        // Link Daftar
        JButton registerButton = new JButton("Belum punya akun? Daftar Sekarang");
        registerButton.setFont(new Font("Arial", Font.PLAIN, 12));
        registerButton.setBackground(PANEL_BG); 
        registerButton.setForeground(ACCENT);
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerButton.addActionListener(e -> showRegisterDialog());
        registerButton.setMaximumSize(new Dimension(300, 30));
        panel.add(registerButton);

        return panel;
    }
    
    // --- (Sisa method attemptLogin, showRegisterDialog, dll. tetap sama) ---
    // Pastikan Anda memindahkan semua method ini dari code sebelumnya.

    private void attemptLogin(String user, char[] pass) {
        String password = new String(pass);
        if (user.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan username dan password.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT USERNAME, ROLE FROM APP_USER WHERE USERNAME = ? AND PASSWORD = ?";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, user);
            st.setString(2, password);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                String role = rs.getString("ROLE");
                dispose(); 

                if ("ADMIN".equalsIgnoreCase(role)) {
                    // Pastikan Anda memiliki class AdminDashboardFrame
                     new AdminDashboardFrame().setVisible(true); 
                    JOptionPane.showMessageDialog(null, "Login Admin Berhasil");
                } else {
                    // Pastikan Anda memiliki class ProductDisplayFrame
                     new ProductDisplayFrame(user).setVisible(true);
                    JOptionPane.showMessageDialog(null, "Login Pelanggan Berhasil");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Username atau Password salah.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal koneksi: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showRegisterDialog() {
        JDialog d = new JDialog(this, "Daftar Akun Baru", true);
        d.setSize(350, 350);
        d.setLayout(new BorderLayout(10, 10));
        d.getContentPane().setBackground(BG); 
        
        JPanel p = new JPanel(new GridLayout(4, 2, 10, 10));
        p.setBackground(BG); 
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JTextField user = new JTextField();
        JPasswordField pass = new JPasswordField();
        
        name.setBackground(new Color(50, 50, 50)); name.setForeground(TEXT_COLOR);
        email.setBackground(new Color(50, 50, 50)); email.setForeground(TEXT_COLOR);
        user.setBackground(new Color(50, 50, 50)); user.setForeground(TEXT_COLOR);
        pass.setBackground(new Color(50, 50, 50)); pass.setForeground(TEXT_COLOR);
        
        p.add(createLabel("Nama Lengkap:", TEXT_COLOR)); p.add(name);
        p.add(createLabel("Email:", TEXT_COLOR)); p.add(email);
        p.add(createLabel("Username:", TEXT_COLOR)); p.add(user);
        p.add(createLabel("Password:", TEXT_COLOR)); p.add(pass);

        d.add(p, BorderLayout.CENTER);

        JButton submit = new JButton("Daftar");
        submit.setBackground(ACCENT);
        submit.setForeground(Color.BLACK);
        submit.addActionListener(ev -> {
            String n = name.getText().trim();
            String em = email.getText().trim();
            String u = user.getText().trim();
            String pw = new String(pass.getPassword());
            if (n.isEmpty() || em.isEmpty() || u.isEmpty() || pw.isEmpty()) { JOptionPane.showMessageDialog(d, "Isi semua field"); return; }
            if (registerUser(u, pw, n, em)) d.dispose();
        });
        
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonWrapper.setBackground(BG); 
        buttonWrapper.add(submit);
        d.add(buttonWrapper, BorderLayout.SOUTH);
        
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
    
    private JLabel createLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        return label;
    }

    private boolean registerUser(String user, String pass, String name, String email) {
        String sql = "INSERT INTO APP_USER (USERNAME, PASSWORD, NAMA_LENGKAP, EMAIL, ROLE) VALUES (?, ?, ?, ?, 'PELANGGAN')";
        try (Connection conn = Koneksi.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, user); st.setString(2, pass); st.setString(3, name); st.setString(4, email);
            st.executeUpdate();
            JOptionPane.showMessageDialog(this, "Registrasi berhasil. Silakan login.");
            return true;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}