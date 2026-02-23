import java.sql.*;

public class DBUtils {
    /**
     * Ensure CART has column PRODUCT_TYPE. If not, try to add it.
     * Returns true if column exists or was successfully added; false if cannot add.
     */
    public static boolean ensureProductTypeColumnExists(Connection conn) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        boolean exists = false;

        try (ResultSet rs = meta.getColumns(null, null, "CART", "PRODUCT_TYPE")) {
            if (rs.next()) exists = true;
        } catch (SQLException ignored) {
            // fallback scan
            try (ResultSet rs2 = meta.getColumns(null, null, "CART", null)) {
                while (rs2.next()) {
                    String col = rs2.getString("COLUMN_NAME");
                    if ("PRODUCT_TYPE".equalsIgnoreCase(col)) { exists = true; break; }
                }
            }
        }

        if (!exists) {
            String alter = "ALTER TABLE CART ADD PRODUCT_TYPE VARCHAR2(20)";
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(alter);
                return true;
            } catch (SQLException ex) {
                System.err.println("Gagal ALTER TABLE CART: " + ex.getMessage());
                return false;
            }
        }
        return true;
    }
}
