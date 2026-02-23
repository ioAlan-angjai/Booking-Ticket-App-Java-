import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class AdminShipFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField tfId, tfOperator, tfOrigin, tfDestination, tfPrice, tfCabin, tfImagePath;
    private JLabel imgPreview;
    private JTextField tfSearch;

    private static final String TABLE_NAME = "SHIP";
    private static final String ID_COL = "SHIP_ID";
    private static final String CABIN_COL = "CABIN_AVAIL"; // Ship uses CABIN_AVAIL

    public AdminShipFrame() {
        setTitle("Admin - Manage Ships");
        setSize(1080, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Search Panel ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        searchPanel.add(new JLabel("Cari (Operator/Asal/Tujuan):"));
        tfSearch = new JTextField(20);
        searchPanel.add(tfSearch);
        JButton searchBtn = new JButton("Cari");
        JButton refreshBtn = new JButton("Refresh");
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);
        add(searchPanel, BorderLayout.NORTH);

        // --- Table Setup ---
        model = new DefaultTableModel(new String[]{"ID","Operator","Origin","Destination","Price","Cabin","ImagePath"},0) {
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        table = new JTable(model);
        table.setRowHeight(28);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // form panel
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        form.setPreferredSize(new Dimension(360, 0));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; form.add(new JLabel("ID:"), g);
        g.gridx=1; tfId = new JTextField(8); form.add(tfId,g);
        tfId.setEditable(true);

        g.gridx=0; g.gridy=1; form.add(new JLabel("Operator:"), g);
        g.gridx=1; tfOperator = new JTextField(15); form.add(tfOperator,g);

        g.gridx=0; g.gridy=2; form.add(new JLabel("Origin:"), g);
        g.gridx=1; tfOrigin = new JTextField(10); form.add(tfOrigin,g);

        g.gridx=0; g.gridy=3; form.add(new JLabel("Destination:"), g);
        g.gridx=1; tfDestination = new JTextField(10); form.add(tfDestination,g);

        g.gridx=0; g.gridy=4; form.add(new JLabel("Price:"), g);
        g.gridx=1; tfPrice = new JTextField(12); form.add(tfPrice,g);

        g.gridx=0; g.gridy=5; form.add(new JLabel("Cabin Avail:"), g);
        g.gridx=1; tfCabin = new JTextField(8); form.add(tfCabin,g);

        g.gridx=0; g.gridy=6; form.add(new JLabel("Image Path:"), g);
        g.gridx=1; tfImagePath = new JTextField(18); tfImagePath.setEditable(false); form.add(tfImagePath,g);
        JButton browse = new JButton("Browse");
        g.gridx=2; form.add(browse,g);

        imgPreview = new JLabel();
        imgPreview.setPreferredSize(new Dimension(260,140));
        imgPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        g.gridx=1; g.gridy=7; g.gridwidth=2; form.add(imgPreview,g);
        g.gridwidth=1;

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Add");
        JButton update = new JButton("Update");
        JButton delete = new JButton("Delete");
        JButton clearForm = new JButton("Clear Form");
        btns.add(add); btns.add(update); btns.add(delete); btns.add(clearForm);
        g.gridx=1; g.gridy=8; form.add(btns,g);

        add(form, BorderLayout.EAST);
        
        // --- Action Listeners ---
        clearForm.addActionListener(e -> clearFormFields());
        browse.addActionListener(e -> handleBrowseAction());
        add.addActionListener(e -> createItem());
        update.addActionListener(e -> updateItem());
        delete.addActionListener(e -> deleteItem());
        searchBtn.addActionListener(e -> loadItems(tfSearch.getText()));
        refreshBtn.addActionListener(e -> {
             tfSearch.setText("");
             loadItems(null);
        });

        table.addMouseListener(new java.awt.event.MouseAdapter(){
            @Override public void mouseClicked(java.awt.event.MouseEvent e){
                int r = table.getSelectedRow();
                if (r >= 0) {
                    tfId.setText(String.valueOf(model.getValueAt(r,0)));
                    tfOperator.setText(String.valueOf(model.getValueAt(r,1)));
                    tfOrigin.setText(String.valueOf(model.getValueAt(r,2)));
                    tfDestination.setText(String.valueOf(model.getValueAt(r,3)));
                    tfPrice.setText(String.valueOf(model.getValueAt(r,4)));
                    tfCabin.setText(String.valueOf(model.getValueAt(r,5)));
                    tfImagePath.setText(String.valueOf(model.getValueAt(r,6)));
                    
                    previewImage(tfImagePath.getText());
                }
            }
        });

        loadItems(null);
    }
    
    // --- Helper Methods ---
    private void clearFormFields() {
        tfId.setText("");
        tfOperator.setText("");
        tfOrigin.setText("");
        tfDestination.setText("");
        tfPrice.setText("");
        tfCabin.setText("");
        tfImagePath.setText("");
        imgPreview.setIcon(null);
        table.clearSelection();
    }
    
    private void previewImage(String imagePath) {
        imgPreview.setIcon(null);
        if (imagePath == null || imagePath.isEmpty()) return;
        
        try {
            URL url = getClass().getResource("/" + imagePath.replace(File.separator, "/"));
            ImageIcon ic;
            if (url != null) {
                ic = new ImageIcon(url);
            } else {
                File f = new File(imagePath);
                if (f.exists()) {
                    ic = new ImageIcon(f.getAbsolutePath());
                } else {
                    return;
                }
            }
            imgPreview.setIcon(new ImageIcon(ic.getImage().getScaledInstance(imgPreview.getWidth(), imgPreview.getHeight(), Image.SCALE_SMOOTH)));
        } catch (Exception ex) {
            imgPreview.setIcon(null);
        }
    }
    
    private void handleBrowseAction() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Image files", "jpg","jpeg","png","jfif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File src = fc.getSelectedFile();
            try {
                String destFolder = "images";
                Path destDir = Paths.get(destFolder);
                if (!Files.exists(destDir)) Files.createDirectories(destDir);

                Path destFile = destDir.resolve(src.getName());
                Files.copy(src.toPath(), destFile, StandardCopyOption.REPLACE_EXISTING);

                String rel = destFolder + "/" + src.getName();
                tfImagePath.setText(rel);

                ImageIcon icon = new ImageIcon(destFile.toAbsolutePath().toString());
                Image scaled = icon.getImage().getScaledInstance(imgPreview.getWidth(), imgPreview.getHeight(), Image.SCALE_SMOOTH);
                imgPreview.setIcon(new ImageIcon(scaled));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Gagal menyalin gambar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- CRUD and Load Methods ---

    private void loadItems(String searchKeyword) {
        model.setRowCount(0);
        String sql = "SELECT " + ID_COL + ", OPERATOR, ORIGIN, DESTINATION, PRICE, " + CABIN_COL + ", IMAGE_PATH FROM " + TABLE_NAME;
        
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql += " WHERE UPPER(OPERATOR) LIKE UPPER(?) OR UPPER(ORIGIN) LIKE UPPER(?) OR UPPER(DESTINATION) LIKE UPPER(?)";
        }
        
        sql += " ORDER BY " + ID_COL;
        
        try (Connection conn = Koneksi.getConnection(); 
             PreparedStatement st = conn.prepareStatement(sql)) {
            
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                String keyword = "%" + searchKeyword.trim() + "%";
                st.setString(1, keyword);
                st.setString(2, keyword);
                st.setString(3, keyword);
            }

            try(ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt(ID_COL),
                        rs.getString("OPERATOR"),
                        rs.getString("ORIGIN"),
                        rs.getString("DESTINATION"),
                        rs.getDouble("PRICE"),
                        rs.getInt(CABIN_COL),
                        rs.getString("IMAGE_PATH")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal load " + TABLE_NAME + ": " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createItem() {
        try {
            int id = Integer.parseInt(tfId.getText().trim());
            String op = tfOperator.getText().trim();
            String or = tfOrigin.getText().trim();
            String dest = tfDestination.getText().trim();
            double price = Double.parseDouble(tfPrice.getText().trim());
            int cabin = Integer.parseInt(tfCabin.getText().trim());
            String img = tfImagePath.getText().trim();
            
            if (op.isEmpty() || or.isEmpty() || dest.isEmpty() || img.isEmpty()) {
                 JOptionPane.showMessageDialog(this, "Semua field harus diisi.", "Input Error", JOptionPane.WARNING_MESSAGE);
                 return;
            }

            String sql = "INSERT INTO " + TABLE_NAME + " (" + ID_COL + ", OPERATOR, ORIGIN, DESTINATION, PRICE, " + CABIN_COL + ", IMAGE_PATH) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = Koneksi.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.setString(2, op);
                ps.setString(3, or);
                ps.setString(4, dest);
                ps.setDouble(5, price);
                ps.setInt(6, cabin);
                ps.setString(7, img.isEmpty() ? null : img);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Added.");
                clearFormFields();
                loadItems(null);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Isi field ID, Price, dan Cabin dengan benar.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateItem() {
        try {
            int id = Integer.parseInt(tfId.getText().trim());
            String op = tfOperator.getText().trim();
            String or = tfOrigin.getText().trim();
            String dest = tfDestination.getText().trim();
            double price = Double.parseDouble(tfPrice.getText().trim());
            int cabin = Integer.parseInt(tfCabin.getText().trim());
            String img = tfImagePath.getText().trim();

            String sql = "UPDATE " + TABLE_NAME + " SET OPERATOR=?, ORIGIN=?, DESTINATION=?, PRICE=?, " + CABIN_COL + "=?, IMAGE_PATH=? WHERE " + ID_COL + "=?";
            try (Connection conn = Koneksi.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, op);
                ps.setString(2, or);
                ps.setString(3, dest);
                ps.setDouble(4, price);
                ps.setInt(5, cabin);
                ps.setString(6, img.isEmpty() ? null : img);
                ps.setInt(7, id);
                int cnt = ps.executeUpdate();
               
                if (cnt > 0) JOptionPane.showMessageDialog(this, "Updated.");
                else JOptionPane.showMessageDialog(this, "ID tidak ditemukan.");
                clearFormFields();
                loadItems(null);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Isi field numeric dengan benar.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteItem() {
        try {
            int id = Integer.parseInt(tfId.getText().trim());
            if (JOptionPane.showConfirmDialog(this, "Hapus ID " + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + ID_COL + " = ?";
            try (Connection conn = Koneksi.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
                
                JOptionPane.showMessageDialog(this, "Deleted.");
                clearFormFields();
                loadItems(null);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Isi ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}