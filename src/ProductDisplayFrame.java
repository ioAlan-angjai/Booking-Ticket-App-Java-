import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

public class ProductDisplayFrame extends JFrame {

    private String loggedInUser;
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardsPanel;
    private Map<String, JPanel> productViewMap = new HashMap<>();
    
    // Search fields for Customer
    private JTextField originFilterField;
    private JTextField destinationFilterField;
    private String currentProductType = "FLIGHT"; // Track current product type in use

    private final Color BG = new Color(20,24,28);
    private final Color CARD = new Color(34,40,46);
    private final Color ACCENT = new Color(0,150,255); // Biru
    private final Color TEXT_WHITE = Color.WHITE;

    public ProductDisplayFrame(String user) {
        this.loggedInUser = user;
        setTitle("TioLoka - Pilih Transportasi"); // Ganti ke TioLoka
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 760);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        JPanel top = createTopBar();
        add(top, BorderLayout.NORTH);
        
        JPanel searchBar = createSearchBar();
        add(searchBar, BorderLayout.SOUTH);

        cardsPanel = new JPanel(cardLayout);
        cardsPanel.setBackground(BG);

        cardsPanel.add(createProductView("FLIGHT"), "PESAWAT");
        cardsPanel.add(createProductView("TRAIN"), "KERETA");
        cardsPanel.add(createProductView("SHIP"), "KAPAL");

        add(cardsPanel, BorderLayout.CENTER);

        cardLayout.show(cardsPanel, "PESAWAT");
        loadProducts(currentProductType, "", ""); // Load initial data without filter

        setLocationRelativeTo(null);
    }
    
    private JPanel createSearchBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bar.setBackground(new Color(30, 36, 42)); // Slightly darker background
        bar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel filterTitle = new JLabel("Pencarian Rute:");
        filterTitle.setForeground(TEXT_WHITE);
        bar.add(filterTitle);
        
        // --- KOREKSI WARNA INPUT FIELD ---
        JLabel asal = new JLabel("Asal:");
        asal.setForeground(TEXT_WHITE);
        bar.add(asal);
        originFilterField = new JTextField(10);
        originFilterField.setBackground(CARD); 
        originFilterField.setForeground(TEXT_WHITE);
        bar.add(originFilterField);

        JLabel tujuan = new JLabel("Tujuan:");
        tujuan.setForeground(TEXT_WHITE);
        bar.add(tujuan);
        destinationFilterField = new JTextField(10);
        destinationFilterField.setBackground(CARD);
        destinationFilterField.setForeground(TEXT_WHITE);
        bar.add(destinationFilterField);
        // ---------------------------------

        JButton searchBtn = new JButton("Cari Rute");
        searchBtn.setBackground(ACCENT);
        searchBtn.setForeground(Color.BLACK);
        searchBtn.setFocusPainted(false);
        searchBtn.addActionListener(e -> {
            String origin = originFilterField.getText().trim();
            String dest = destinationFilterField.getText().trim();
            loadProducts(currentProductType, origin, dest);
        });
        bar.add(searchBtn);
        
        JButton clearBtn = new JButton("Clear");
        clearBtn.setBackground(CARD);
        clearBtn.setForeground(TEXT_WHITE);
        clearBtn.setFocusPainted(false);
        clearBtn.addActionListener(e -> {
            originFilterField.setText("");
            destinationFilterField.setText("");
            loadProducts(currentProductType, "", "");
        });
        bar.add(clearBtn);

        return bar;
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(12,15,18));
        bar.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        // TioLoka dengan warna Biru
        JLabel title = new JLabel("TioLoka - Pilih Transportasi (Pelanggan: " + loggedInUser + ")"); 
        title.setForeground(ACCENT); 
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(bar.getBackground());

        String[] tabs = {"PESAWAT", "KERETA", "KAPAL"};
        for (String t : tabs) {
            JButton b = new JButton(t);
            b.setBackground(CARD);
            b.setForeground(TEXT_WHITE);
            b.setFocusPainted(false);
            b.addActionListener(e -> {
                String key = switch (t) {
                    case "PESAWAT" -> "FLIGHT";
                    case "KERETA" -> "TRAIN";
                    default -> "SHIP";
                };
                currentProductType = key; 
                cardLayout.show(cardsPanel, t);
                
                String origin = originFilterField.getText().trim();
                String dest = destinationFilterField.getText().trim();
                loadProducts(key, origin, dest);
            });
            right.add(b);
        }

        JButton cart = new JButton("Keranjang");
        cart.setBackground(ACCENT);
        cart.setForeground(Color.BLACK);
        // Pastikan CartFrame memiliki constructor (String user)
        cart.addActionListener(e -> new CartFrame(loggedInUser).setVisible(true));
        right.add(cart);
        
        JButton logout = new JButton("Logout");
        logout.setBackground(CARD);
        logout.setForeground(TEXT_WHITE);
        // Asumsi ada class LoginFrame()
        logout.addActionListener(e -> {
            // new LoginFrame().setVisible(true); // Ganti jika Anda punya LoginFrame
            dispose();
        });
        right.add(logout);

        bar.add(title, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private JPanel createProductView(String type) {
        JPanel view = new JPanel(new BorderLayout());
        view.setBackground(BG);

        JPanel products = new JPanel(new GridLayout(0, 3, 18, 18));
        products.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        products.setBackground(BG);

        JScrollPane sp = new JScrollPane(products);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getViewport().setBackground(BG); // Penting untuk konsistensi warna scroll pane

        view.add(sp, BorderLayout.CENTER);

        productViewMap.put(type, products);
        return view;
    }

    private void loadProducts(String type, String originFilter, String destinationFilter) {
        JPanel productsPanel = productViewMap.get(type);
        productsPanel.removeAll();

        String table = type;
        String idCol = table + "_ID";
        
        String selectCols = switch (table) {
            case "FLIGHT" -> idCol + ", OPERATOR, ORIGIN, DESTINATION, PRICE, SEAT_AVAIL, IMAGE_PATH";
            case "TRAIN" -> idCol + ", OPERATOR, ORIGIN, DESTINATION, PRICE, SEAT_AVAIL, IMAGE_PATH";
            case "SHIP" -> idCol + ", OPERATOR, ORIGIN, DESTINATION, PRICE, CABIN_AVAIL, IMAGE_PATH";
            default -> idCol + ", OPERATOR, ORIGIN, DESTINATION, PRICE, IMAGE_PATH";
        };
        
        String sql = "SELECT " + selectCols + " FROM " + table;
        
        StringBuilder whereClause = new StringBuilder();
        int paramCount = 0;
        
        if (originFilter != null && !originFilter.trim().isEmpty()) {
            whereClause.append(" UPPER(ORIGIN) LIKE ? ");
            paramCount++;
        }
        
        if (destinationFilter != null && !destinationFilter.trim().isEmpty()) {
            if (paramCount > 0) whereClause.append(" AND ");
            whereClause.append(" UPPER(DESTINATION) LIKE ? ");
            paramCount++;
        }
        
        if (paramCount > 0) {
            sql += " WHERE " + whereClause.toString();
        }
        
        sql += " ORDER BY " + idCol; 

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            
            if (originFilter != null && !originFilter.trim().isEmpty()) {
                st.setString(paramIndex++, "%" + originFilter.toUpperCase() + "%"); 
            }
            if (destinationFilter != null && !destinationFilter.trim().isEmpty()) {
                st.setString(paramIndex++, "%" + destinationFilter.toUpperCase() + "%");
            }

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(idCol);
                    String op = rs.getString("OPERATOR");
                    String o = rs.getString("ORIGIN");
                    String d = rs.getString("DESTINATION");
                    double p = rs.getDouble("PRICE");
                    String img = rs.getString("IMAGE_PATH");
    
                    productsPanel.add(
                        createProductCard(id, op, o, d, p, img, type)
                    );
                }
            }

            productsPanel.revalidate();
            productsPanel.repaint();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Gagal memuat data: " + ex.getMessage() + "\nSQL: " + sql,
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private JPanel createProductCard(int itemId, String operator, String origin,
                                     String destination, double price,
                                     String imagePath, String type) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60,60,60)),
                BorderFactory.createEmptyBorder(8,8,8,8)
        ));

        JLabel imgLabel = new JLabel();
        imgLabel.setPreferredSize(new Dimension(240, 140));
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imgLabel.setBackground(BG); // Warna background image label
        imgLabel.setOpaque(true);

        try {
            // ... (Kode pemuatan gambar tetap sama)
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                
                URL url = getClass().getResource("/" + imagePath.replace(File.separator, "/"));
                ImageIcon icon = null;

                if (url != null) {
                    icon = new ImageIcon(url);
                } else {
                    File f = new File(imagePath);

                    if (!f.exists()) {
                        String fileName = new File(imagePath).getName();
                        f = new File("images" + File.separator + fileName);
                    }

                    if (f.exists()) {
                        icon = new ImageIcon(f.getAbsolutePath());
                    } else {
                        System.err.println("NOT FOUND (Classpath/Filesystem): " + imagePath);
                        imgLabel.setText("No Image");
                    }
                }

                if (icon != null) {
                    Image scaled = icon.getImage().getScaledInstance(240, 140, Image.SCALE_SMOOTH);
                    imgLabel.setIcon(new ImageIcon(scaled));
                    imgLabel.setText("");
                }

            } else {
                imgLabel.setText("No Image");
                imgLabel.setForeground(Color.GRAY);
            }
        } catch (Exception e) {
            imgLabel.setText("Image Error");
            imgLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
        
        card.add(imgLabel, BorderLayout.NORTH);

        JPanel info = new JPanel(new GridLayout(3, 1));
        info.setBackground(card.getBackground());

        JLabel title = new JLabel(operator + " (" + type + ")");
        title.setForeground(TEXT_WHITE);

        JLabel route = new JLabel(origin + " → " + destination);
        route.setForeground(Color.LIGHT_GRAY);

        JLabel pr = new JLabel("Rp " + String.format("%,.2f", price));
        pr.setForeground(new Color(255,164,0)); // Warna emas/oranye

        info.add(title);
        info.add(route);
        info.add(pr);

        card.add(info, BorderLayout.CENTER);

        // Efek hover untuk dark mode
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                // Asumsi ada class DetailFrame
                new DetailFrame(loggedInUser, itemId, type).setVisible(true);
            }
            @Override public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(44,50,56));
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBackground(CARD);
            }
        });

        return card;
    }
}