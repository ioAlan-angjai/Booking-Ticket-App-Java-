import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class AdminFlightFrame extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JTextField tfId, tfOperator, tfOrigin, tfDestination, tfPrice, tfSeat, tfImagePath;
    private JLabel imgPreview;
    private JTextField tfSearch;

    public AdminFlightFrame() {
        setTitle("Admin - Manage Flights");
        setSize(1080, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Search Panel
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

        // Table Setup
        model = new DefaultTableModel(new String[]{"ID","Operator","Origin","Destination","Price","Seat","ImagePath"},0) {
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        table = new JTable(model);
        table.setRowHeight(28);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Form Panel
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        form.setPreferredSize(new Dimension(360, 0));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; form.add(new JLabel("ID:"), g);
        g.gridx=1; tfId = new JTextField(8); form.add(tfId,g);

        g.gridx=0; g.gridy=1; form.add(new JLabel("Operator:"), g);
        g.gridx=1; tfOperator = new JTextField(15); form.add(tfOperator,g);

        g.gridx=0; g.gridy=2; form.add(new JLabel("Origin:"), g);
        g.gridx=1; tfOrigin = new JTextField(10); form.add(tfOrigin,g);

        g.gridx=0; g.gridy=3; form.add(new JLabel("Destination:"), g);
        g.gridx=1; tfDestination = new JTextField(10); form.add(tfDestination,g);

        g.gridx=0; g.gridy=4; form.add(new JLabel("Price:"), g);
        g.gridx=1; tfPrice = new JTextField(12); form.add(tfPrice,g);

        g.gridx=0; g.gridy=5; form.add(new JLabel("Seat Avail:"), g);
        g.gridx=1; tfSeat = new JTextField(8); form.add(tfSeat,g);

        g.gridx=0; g.gridy=6; form.add(new JLabel("Image Path:"), g);
        g.gridx=1; tfImagePath = new JTextField(18);
        tfImagePath.setEditable(false);
        form.add(tfImagePath,g);

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

        // Listeners
        clearForm.addActionListener(e -> clearFormFields());
        browse.addActionListener(e -> handleBrowseAction());
        add.addActionListener(e -> createFlight());
        update.addActionListener(e -> updateFlight());
        delete.addActionListener(e -> deleteFlight());
        searchBtn.addActionListener(e -> loadFlights(tfSearch.getText()));
        refreshBtn.addActionListener(e -> {
             tfSearch.setText("");
             loadFlights(null);
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
                    tfSeat.setText(String.valueOf(model.getValueAt(r,5)));
                    tfImagePath.setText(String.valueOf(model.getValueAt(r,6)));
                    previewImage(tfImagePath.getText());
                }
            }
        });

        loadFlights(null);
    }

    // Helpers
    private void clearFormFields() {
        tfId.setText("");
        tfOperator.setText("");
        tfOrigin.setText("");
        tfDestination.setText("");
        tfPrice.setText("");
        tfSeat.setText("");
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

            if (url != null) ic = new ImageIcon(url);
            else {
                File f = new File(imagePath);
                if (!f.exists()) return;
                ic = new ImageIcon(f.getAbsolutePath());
            }

            imgPreview.setIcon(new ImageIcon(
                    ic.getImage().getScaledInstance(imgPreview.getWidth(), imgPreview.getHeight(), Image.SCALE_SMOOTH)
            ));
        } catch (Exception ignored) {}
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

    // Load Flights (Dengan Search)
    private void loadFlights(String searchKeyword) {
        model.setRowCount(0);
        String sql = "SELECT FLIGHT_ID, OPERATOR, ORIGIN, DESTINATION, PRICE, SEAT_AVAIL, IMAGE_PATH FROM FLIGHT ";

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql += "WHERE UPPER(OPERATOR) LIKE UPPER(?) OR UPPER(ORIGIN) LIKE UPPER(?) OR UPPER(DESTINATION) LIKE UPPER(?) ";
        }

        sql += "ORDER BY FLIGHT_ID";

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
                            rs.getInt("FLIGHT_ID"),
                            rs.getString("OPERATOR"),
                            rs.getString("ORIGIN"),
                            rs.getString("DESTINATION"),
                            rs.getDouble("PRICE"),
                            rs.getInt("SEAT_AVAIL"),
                            rs.getString("IMAGE_PATH")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal load flights: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // CRUD Methods (Tanpa commit!)
    private void createFlight() {
        try {
            int id = Integer.parseInt(tfId.getText().trim());
            String op = tfOperator.getText().trim();
            String or = tfOrigin.getText().trim();
            String dest = tfDestination.getText().trim();
            double price = Double.parseDouble(tfPrice.getText().trim());
            int seat = Integer.parseInt(tfSeat.getText().trim());
            String img = tfImagePath.getText().trim();

            if (op.isEmpty() || or.isEmpty() || dest.isEmpty() || img.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Semua field harus diisi.");
                return;
            }

            String sql = "INSERT INTO FLIGHT VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = Koneksi.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, id);
                ps.setString(2, op);
                ps.setString(3, or);
                ps.setString(4, dest);
                ps.setDouble(5, price);
                ps.setInt(6, seat);
                ps.setString(7, img);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Added.");
                clearFormFields();
                loadFlights(null);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
        }
    }

    private void updateFlight() {
        try {
            int id = Integer.parseInt(tfId.getText().trim());
            String sql = "UPDATE FLIGHT SET OPERATOR=?, ORIGIN=?, DESTINATION=?, PRICE=?, SEAT_AVAIL=?, IMAGE_PATH=? WHERE FLIGHT_ID=?";

            try (Connection conn = Koneksi.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, tfOperator.getText().trim());
                ps.setString(2, tfOrigin.getText().trim());
                ps.setString(3, tfDestination.getText().trim());
                ps.setDouble(4, Double.parseDouble(tfPrice.getText().trim()));
                ps.setInt(5, Integer.parseInt(tfSeat.getText().trim()));
                ps.setString(6, tfImagePath.getText().trim());
                ps.setInt(7, id);

                int rows = ps.executeUpdate();
                if (rows > 0) JOptionPane.showMessageDialog(this, "Updated.");
                else JOptionPane.showMessageDialog(this, "ID tidak ditemukan.");

                clearFormFields();
                loadFlights(null);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
        }
    }

    private void deleteFlight() {
        try {
            int id = Integer.parseInt(tfId.getText().trim());
            if (JOptionPane.showConfirmDialog(this, "Hapus flight ID " + id + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                return;

            String sql = "DELETE FROM FLIGHT WHERE FLIGHT_ID = ?";

            try (Connection conn = Koneksi.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Deleted.");
                clearFormFields();
                loadFlights(null);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "DB Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminFlightFrame().setVisible(true));
    }
}
