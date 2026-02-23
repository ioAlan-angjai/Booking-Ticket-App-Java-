import java.awt.*;
import java.awt.print.*;
import java.io.*;
import java.sql.*;
import javax.swing.*;

public class InvoiceFrame extends JFrame {
    private String username;
    private JTextArea txt;
    private double total = 0.0;
    
    // Warna Dark Mode (Konsisten)
    private final Color BG = new Color(20, 24, 28);
    private final Color CARD = new Color(34, 40, 46);
    private final Color ACCENT = new Color(0, 150, 255); // Biru
    private final Color TEXT_WHITE = Color.WHITE;

    public InvoiceFrame(String username) {
        this.username = username;
        setTitle("Invoice - " + username);
        setSize(700,600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        txt = new JTextArea();
        txt.setEditable(false);
        txt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        // Atur warna dark mode
        txt.setBackground(CARD);
        txt.setForeground(TEXT_WHITE);
        
        JScrollPane scrollPane = new JScrollPane(txt);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(BG);
        
        JButton saveTxt = new JButton("Save TXT");
        JButton saveHtml = new JButton("Save HTML");
        JButton print = new JButton("Print");
        
        JButton cancel = new JButton("Batalkan Checkout");
        cancel.setBackground(new Color(255, 60, 60)); 
        cancel.setForeground(TEXT_WHITE);
        cancel.setFocusPainted(false);
        
        // Atur warna tombol lain
        saveTxt.setBackground(CARD); saveTxt.setForeground(TEXT_WHITE); saveTxt.setFocusPainted(false);
        saveHtml.setBackground(CARD); saveHtml.setForeground(TEXT_WHITE); saveHtml.setFocusPainted(false);
        print.setBackground(ACCENT); print.setForeground(Color.BLACK); print.setFocusPainted(false);
        
        bottom.add(cancel); 
        bottom.add(saveTxt); 
        bottom.add(saveHtml); 
        bottom.add(print);
        add(bottom, BorderLayout.SOUTH);

        loadInvoiceContent();

        saveTxt.addActionListener(e -> saveAsTxt());
        saveHtml.addActionListener(e -> saveAsHtml());
        print.addActionListener(e -> doPrint());
        
        cancel.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Checkout dibatalkan. Silakan tutup frame ini.", "Pembatalan", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });
    }

    private void loadInvoiceContent() {
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("                  INVOICE TioLoka                   \n");
        sb.append("==================================================\n");
        sb.append(String.format("Pelanggan: %s\n", username));
        
        sb.append(String.format("Tanggal: %tF %tT\n", new java.util.Date(), new java.util.Date()));
        sb.append("--------------------------------------------------\n");
        sb.append(String.format("%-10s %-5s %-15s %-8s %-12s\n", "TIPE", "ID", "OPERATOR", "QTY", "SUBTOTAL"));
        sb.append("--------------------------------------------------\n");

        String sqlCart = "SELECT PRODUCT_TYPE, ITEM_ID, QUANTITY FROM CART WHERE USERNAME = ?";
        
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement stCart = conn.prepareStatement(sqlCart)) {

            stCart.setString(1, username);
            ResultSet rsCart = stCart.executeQuery();

            while (rsCart.next()) {
                String type = rsCart.getString("PRODUCT_TYPE");
                int id = rsCart.getInt("ITEM_ID");
                int qty = rsCart.getInt("QUANTITY");

                String table = type;
                String idCol = table + "_ID";
                String sqlDetail = "SELECT OPERATOR, PRICE FROM " + table + " WHERE " + idCol + " = ?";
                
                String op = "N/A";
                double price = 0.0;

                try (PreparedStatement stDetail = conn.prepareStatement(sqlDetail)) {
                    stDetail.setInt(1, id);
                    ResultSet rsDetail = stDetail.executeQuery();

                    if (rsDetail.next()) {
                        op = rsDetail.getString("OPERATOR");
                        price = rsDetail.getDouble("PRICE");
                    }
                }

                double sub = price * qty;
                total += sub;
                
                sb.append(String.format("%-10s %-5d %-15s %-8d %-12s\n", 
                    type, id, op, qty, String.format("%,.2f", sub)));
            }

            sb.append("--------------------------------------------------\n");
            sb.append(String.format("TOTAL:                                  Rp %,.2f\n", total));
            sb.append("==================================================\n");

        } catch (SQLException ex) {
            sb.append("ERROR: Gagal memuat detail keranjang: " + ex.getMessage() + "\n");
        }
        
        txt.setText(sb.toString());
    }
    
    private void saveAsTxt() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Invoice_" + username + ".txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(fc.getSelectedFile())) {
                fw.write(txt.getText());
                JOptionPane.showMessageDialog(this, "Berhasil disimpan sebagai TXT.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Save error: " + ex.getMessage());
            }
        }
    }

    private void saveAsHtml() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("Invoice_" + username + ".html"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String html = "<html><head><meta charset='utf-8'><title>Invoice</title></head><body><pre>" + txt.getText() + "</pre></body></html>";
            try (FileWriter fw = new FileWriter(fc.getSelectedFile())) {
                fw.write(html);
                JOptionPane.showMessageDialog(this, "Berhasil disimpan sebagai HTML.");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Save error: " + ex.getMessage());
            }
        }
    }

    private void doPrint() {
        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setJobName("Invoice Print - TioLoka");

        // --- KOREKSI TERAKHIR FUNGSI PRINT ---
        pj.setPrintable((graphics, pf, pageIndex) -> {
            // Gunakan metode print() tanpa argumen yang didefinisikan melempar PrinterException
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            
            // Set font untuk mencetak
            txt.setFont(new Font("Monospaced", Font.PLAIN, 10)); 

            // Kita harus mengimplementasikan logika menggambar sendiri ke Graphics
            // atau menggunakan metode print yang melempar exception.
            // Di sini kita akan menggunakan Printable dari JTextComponent
            Printable printable = txt.getPrintable(null, null); 
            
            // Panggil metode print pada Printable yang sudah kita dapatkan.
            // Metode ini melempar PrinterException
            return printable.print(graphics, pf, pageIndex);
        });
        // ------------------------------------

        // Tangkap PrinterException untuk dialog print
        if (pj.printDialog()) {
            try {
                pj.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage(), "Error Mencetak", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}