import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CartFrame extends JFrame {
    private String loggedInUser;
    private JTable cartTable;
    private JLabel totalLabel;

    // Warna Dark Mode (Konsisten)
    private final Color BG = new Color(20, 24, 28);
    private final Color CARD = new Color(34, 40, 46);
    private final Color ACCENT = new Color(0, 150, 255); // Biru
    private final Color TEXT_WHITE = Color.WHITE;

    public CartFrame(String user) {
        this.loggedInUser = user;
        setTitle("Keranjang - TioLoka");
        setSize(920, 480); // Ukuran lebih besar untuk menampung tombol tambahan
        setLayout(new BorderLayout());
        
        getContentPane().setBackground(BG);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG);
        add(top, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Tipe","ID","Operator","Rute","Harga","Qty","Subtotal"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        cartTable = new JTable(model);
        
        // Pengaturan warna tabel (Dark Mode)
        cartTable.setBackground(CARD);
        cartTable.setForeground(TEXT_WHITE);
        cartTable.getTableHeader().setBackground(CARD);
        cartTable.getTableHeader().setForeground(ACCENT);
        cartTable.setSelectionBackground(ACCENT.darker()); // Warna saat item dipilih
        cartTable.setSelectionForeground(Color.BLACK);
        
        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10)); // Tambah padding
        scrollPane.getViewport().setBackground(CARD);
        add(scrollPane, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setBackground(BG);
        foot.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        totalLabel = new JLabel("Total: Rp 0.00", SwingConstants.RIGHT);
        totalLabel.setForeground(TEXT_WHITE);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(BG);
        totalPanel.add(totalLabel, BorderLayout.EAST);
        foot.add(totalPanel, BorderLayout.NORTH);

        
        // --- PANEL TOMBOL AKSI ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(BG);
        
        // 1. Tombol HAPUS ITEM (Baru)
        JButton deleteItem = new JButton("Hapus Item Terpilih");
        deleteItem.setBackground(CARD);
        deleteItem.setForeground(TEXT_WHITE);
        deleteItem.setFocusPainted(false);
        deleteItem.addActionListener(e -> deleteSelectedItem());
        actionPanel.add(deleteItem);
        
        // 2. Tombol KOSONGKAN KERANJANG (Baru)
        JButton clearAll = new JButton("Kosongkan Keranjang");
        clearAll.setBackground(new Color(150, 0, 0)); // Merah gelap
        clearAll.setForeground(TEXT_WHITE);
        clearAll.setFocusPainted(false);
        clearAll.addActionListener(e -> clearCartConfirmation());
        actionPanel.add(clearAll);

        // 3. Tombol CHECKOUT (Bayar)
        JButton checkout = new JButton("Bayar (Checkout)");
        checkout.setBackground(ACCENT);
        checkout.setForeground(Color.BLACK);
        checkout.setFocusPainted(false);
        checkout.addActionListener(e -> processCheckout()); 
        actionPanel.add(checkout);
        
        foot.add(actionPanel, BorderLayout.SOUTH);
        add(foot, BorderLayout.SOUTH);

        loadCart();
        setLocationRelativeTo(null);
    }
    
    // FUNGSI UTAMA: Proses Checkout
    private void processCheckout() {
        if (cartTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Keranjang kosong. Tidak bisa checkout.", "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Yakin untuk Checkout dan melanjutkan ke pembayaran?", "Konfirmasi Checkout", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // 1. Tampilkan Invoice
            new InvoiceFrame(loggedInUser).setVisible(true);

            // 2. KOSONGKAN KERANJANG
            clearAllCartData();
            
            // 3. Tutup frame keranjang
            dispose();
        }
    }
    
    // FUNGSI BARU: Hapus Item Terpilih
    private void deleteSelectedItem() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih item yang ingin dihapus dari keranjang.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ambil data (Tipe Produk dan ID Item) dari baris yang dipilih
        String productType = (String) cartTable.getValueAt(selectedRow, 0);
        // ID diambil dari kolom ke-1, tetapi harus dikonversi karena bisa berupa String (jika error)
        Object idObj = cartTable.getValueAt(selectedRow, 1);
        int itemId;
        try {
            itemId = Integer.parseInt(idObj.toString());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID item tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus item terpilih dari keranjang?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM CART WHERE USERNAME = ? AND PRODUCT_TYPE = ? AND ITEM_ID = ?";
            try (Connection conn = Koneksi.getConnection();
                 PreparedStatement st = conn.prepareStatement(sql)) {

                st.setString(1, loggedInUser);
                st.setString(2, productType);
                st.setInt(3, itemId);
                
                int rowsAffected = st.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Item berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                }
                
                // Muat ulang keranjang setelah penghapusan
                loadCart();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Gagal menghapus item: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // FUNGSI BARU: Konfirmasi Kosongkan Keranjang
    private void clearCartConfirmation() {
        if (cartTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Keranjang sudah kosong.", "Info", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus SEMUA item dari keranjang?", "Konfirmasi Kosongkan", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            clearAllCartData();
            loadCart(); // Muat ulang untuk update tampilan
            JOptionPane.showMessageDialog(this, "Semua item berhasil dihapus dari keranjang.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // FUNGSI PENDUKUNG: Hapus semua data CART untuk user ini
    private void clearAllCartData() {
        String sql = "DELETE FROM CART WHERE USERNAME = ?";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, loggedInUser);
            st.executeUpdate();
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus data keranjang: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCart() {
        DefaultTableModel model = (DefaultTableModel) cartTable.getModel();
        model.setRowCount(0);
        double total = 0.0;

        String sqlCart = "SELECT PRODUCT_TYPE, ITEM_ID, QUANTITY FROM CART WHERE USERNAME = ?";
        
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement stCart = conn.prepareStatement(sqlCart)) {

            stCart.setString(1, loggedInUser);
            ResultSet rsCart = stCart.executeQuery();

            while (rsCart.next()) {
                String type = rsCart.getString("PRODUCT_TYPE");
                int id = rsCart.getInt("ITEM_ID");
                int qty = rsCart.getInt("QUANTITY");

                String table = type;
                String idCol = table + "_ID";
                // Asumsi umum: operator, origin, destination, price
                String sqlDetail = "SELECT OPERATOR, ORIGIN, DESTINATION, PRICE FROM " + table + " WHERE " + idCol + " = ?";
                
                String op = "(unknown)";
                String route = "-";
                double price = 0.0;
                double sub = 0.0; // Inisialisasi subtotal

                // --- BLOK TRY-CATCH SPESIFIK UNTUK SETIAP ITEM ---
                // Ini menangkap error jika tabel atau kolom SQL tidak ditemukan
                try (PreparedStatement stDetail = conn.prepareStatement(sqlDetail)) {
                    stDetail.setInt(1, id);
                    ResultSet rsDetail = stDetail.executeQuery();

                    if (rsDetail.next()) {
                        op = rsDetail.getString("OPERATOR");
                        
                        try {
                            // Coba ambil kolom rute (Origin dan Destination)
                            route = rsDetail.getString("ORIGIN") + " → " + rsDetail.getString("DESTINATION");
                        } catch (SQLException columnNotFoundEx) {
                            // Jika kolom ORIGIN atau DESTINATION tidak ada di tabel ini
                            route = "N/A (Non-Rute)";
                            // Jika Anda punya nama produk lain (misal PULSA, ambil kolom lain yang ada)
                        }
                        
                        price = rsDetail.getDouble("PRICE");
                        sub = price * qty;
                        total += sub;
                        
                        model.addRow(new Object[]{
                            type, id, op, route,
                            String.format("%,.2f", price),
                            qty,
                            String.format("%,.2f", sub)
                        });
                    } else {
                        // Item di Cart tapi produk utama sudah dihapus
                         model.addRow(new Object[]{
                            type, id, "(Produk Dihapus)", "-", "0.00", qty, "0.00"
                        });
                    }

                } catch (SQLException ex) {
                    // Jika query ke tabel detail gagal (misal tabel FLIGHT, TRAIN, SHIP tidak ditemukan)
                    model.addRow(new Object[]{
                        type, id, "(Error SQL)", "-", "0.00", qty, "0.00"
                    });
                    System.err.println("Error memuat detail item: " + ex.getMessage());
                }
                // --------------------------------------------------
            }

            totalLabel.setText("Total: Rp " + String.format("%,.2f", total));

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal muat keranjang: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}