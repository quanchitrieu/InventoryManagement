import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class InventoryManagementApp {
    private static final String DB_URL = "jdbc:sqlite:inventory.db";

    private JFrame frame;
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtName, txtQuantity, txtPrice;

    public InventoryManagementApp() {
        frame = new JFrame("Quản Lý Kho Hàng");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Table Model
        model = new DefaultTableModel(new String[]{"ID", "Tên SP", "Số lượng", "Giá"}, 0);
        table = new JTable(model);
        createDatabaseAndTable();
        loadProducts();

        // Panel nhập dữ liệu
        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Tên SP:"));
        txtName = new JTextField();
        panel.add(txtName);

        panel.add(new JLabel("Số lượng:"));
        txtQuantity = new JTextField();
        panel.add(txtQuantity);

        panel.add(new JLabel("Giá:"));
        txtPrice = new JTextField();
        panel.add(txtPrice);

        JButton btnAdd = new JButton("Thêm");
        btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addProduct();
            }
        });
        panel.add(btnAdd);

        JButton btnDelete = new JButton("Xóa");
        btnDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteProduct();
            }
        });
        panel.add(btnDelete);

        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void createDatabaseAndTable() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS products ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT NOT NULL,"
                    + "quantity INTEGER NOT NULL,"
                    + "price REAL NOT NULL"
                    + ")";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadProducts() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("id"), rs.getString("name"), rs.getInt("quantity"), rs.getDouble("price")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addProduct() {
        String name = txtName.getText();
        int quantity = Integer.parseInt(txtQuantity.getText());
        double price = Double.parseDouble(txtPrice.getText());

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO products(name, quantity, price) VALUES (?, ?, ?)") ) {
            stmt.setString(1, name);
            stmt.setInt(2, quantity);
            stmt.setDouble(3, price);
            stmt.executeUpdate();
            loadProducts();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteProduct() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) model.getValueAt(selectedRow, 0);
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
                loadProducts();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InventoryManagementApp::new);
    }
}
