import java.awt.*;
import java.sql.*;
import javax.swing.*;

public class DetailFrame extends JFrame {
    private int itemId; 
    private String loggedInUser; 
    private String productType;
    // Hapus fotoPath yang tidak digunakan di sini

    public DetailFrame(String user, int id, String type) {
        this.loggedInUser = user; this.itemId = id; this.productType = type;
        setTitle("Detail " + type); setSize(440,380); setLayout(new BorderLayout()); setResizable(false);
        
        Color BG = new Color(20,24,28); // Ambil warna dari ProductDisplayFrame
        Color ACCENT = new Color(0,150,255);

        JPanel p = new JPanel(); 
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); 
        p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        p.setBackground(BG);
        loadItemDetails(p);

        JButton add = new JButton("Masukkan Keranjang"); 
        add.setBackground(ACCENT); 
        add.setForeground(Color.BLACK);
        add.addActionListener(e -> addToCart());

        add(p, BorderLayout.CENTER);

        JPanel s = new JPanel(new FlowLayout(FlowLayout.RIGHT)); 
        s.setBackground(BG); 
        s.add(add); 
        add(s, BorderLayout.SOUTH);
        setLocationRelativeTo(null);
    }

    private void loadItemDetails(JPanel panel) {
        String idCol = productType + "_ID"; 
        String sql = "SELECT * FROM " + productType + " WHERE " + idCol + " = ?";
        try (Connection conn = Koneksi.getConnection(); 
             PreparedStatement st = conn.prepareStatement(sql)) {
            
            st.setInt(1, itemId); 
            ResultSet rs = st.executeQuery();
            
            if (rs.next()) {
                panel.add(new JLabel(rs.getString("OPERATOR") + " (" + productType + ")"));
                panel.add(new JLabel("Rute: " + rs.getString("ORIGIN") + " -> " + rs.getString("DESTINATION")));
                panel.add(new JLabel("Harga: Rp " + String.format("%,.2f", rs.getDouble("PRICE"))));
                panel.add(new JLabel("Tersedia: " + rs.getInt("SEAT_AVAIL") + " Kursi"));
                
                // Styling text
                for (Component comp : panel.getComponents()) {
                    if (comp instanceof JLabel) {
                        ((JLabel)comp).setForeground(Color.WHITE);
                    }
                }

            } else {
                panel.add(new JLabel("Detail produk tidak ditemukan."));
            }
        } catch (SQLException ex) { 
            JOptionPane.showMessageDialog(this, "Gagal muat detail: " + ex.getMessage());
        }
    }

    private void addToCart() {
        String checkSql = "SELECT QUANTITY FROM CART WHERE USERNAME = ? AND ITEM_ID = ? AND PRODUCT_TYPE = ?";
        String updateSql = "UPDATE CART SET QUANTITY = QUANTITY + 1 WHERE USERNAME = ? AND ITEM_ID = ? AND PRODUCT_TYPE = ?";
        String insertSql = "INSERT INTO CART (CART_ID, USERNAME, ITEM_ID, QUANTITY, PRODUCT_TYPE, ADDED_DATE) VALUES (CART_SEQ.NEXTVAL, ?, ?, 1, ?, SYSDATE)";

        try (Connection conn = Koneksi.getConnection()) {
            // Panggil DBUtils untuk memastikan kolom ada
            DBUtils.ensureProductTypeColumnExists(conn);

            conn.setAutoCommit(false);
            try {
                // 1. Cek apakah item sudah ada
                try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                    check.setString(1, loggedInUser);
                    check.setInt(2, itemId);
                    check.setString(3, productType);
                    ResultSet rs = check.executeQuery();

                    if (rs.next()) {
                        // 2. Jika ada, update quantity
                        try (PreparedStatement up = conn.prepareStatement(updateSql)) {
                            up.setString(1, loggedInUser);
                            up.setInt(2, itemId);
                            up.setString(3, productType);
                            up.executeUpdate();
                        }
                    } else {
                        // 3. Jika baru, insert
                        try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                            ins.setString(1, loggedInUser);
                            ins.setInt(2, itemId);
                            ins.setString(3, productType);
                            ins.executeUpdate();
                        }
                    }
                }
                conn.commit();
                JOptionPane.showMessageDialog(this, "Berhasil ditambahkan ke keranjang");
                dispose();
            } catch (SQLException ex) { 
                conn.rollback(); 
                throw ex; 
            } finally { 
                conn.setAutoCommit(true); 
            }
        } catch (SQLException ex) { 
            JOptionPane.showMessageDialog(this, "Gagal: " + ex.getMessage()); 
        }
    }
}