import java.awt.*;
import javax.swing.*;

public class AdminDashboardFrame extends JFrame {
    
    private final Color BG = new Color(20,24,28);
    private final Color ACCENT = new Color(0,150,255);
    
    public AdminDashboardFrame() {
        setTitle("Admin Dashboard - Traveloka");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Pilih Menu Administrasi (CRUD)", SwingConstants.CENTER);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(header, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        menuPanel.setBackground(BG);

        JButton btnFlight = createMenuButton("Manage Pesawat", ACCENT);
        JButton btnTrain = createMenuButton("Manage Kereta", ACCENT);
        JButton btnShip = createMenuButton("Manage Kapal", ACCENT);
        
        // Panggil Admin Frame yang sesuai
        btnFlight.addActionListener(e -> new AdminFlightFrame().setVisible(true));
        btnTrain.addActionListener(e -> new AdminTrainFrame().setVisible(true)); 
        btnShip.addActionListener(e -> new AdminShipFrame().setVisible(true)); 
        
        menuPanel.add(btnFlight);
        menuPanel.add(btnTrain);
        menuPanel.add(btnShip);
        
        add(menuPanel, BorderLayout.CENTER);
        
        // Logout button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setBackground(BG);
        southPanel.add(logoutBtn);
        add(southPanel, BorderLayout.SOUTH);
    }
    
    private JButton createMenuButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(150, 50));
        btn.setFocusPainted(false);
        return btn;
    }
}