import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class LibraryApp extends JFrame {
    
    private JTextField txtAuthors, txtTitle, txtPublisher, txtPages;
    private JTextField txtGenre, txtPrice, txtStorage, txtInventoryNum, txtSearch;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnSearch, btnClear;
    
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:library.db";
    
    public LibraryApp() {
        setTitle("Бібліотека - Облік книг (Варіант 22)");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initDatabase();
        initComponents();
        loadData();
    }
    
    private void initDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            
            String createTable = "CREATE TABLE IF NOT EXISTS Books (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "authors TEXT NOT NULL, " +
                "title TEXT NOT NULL, " +
                "publisher TEXT, " +
                "pages INTEGER, " +
                "genre TEXT, " +
                "price REAL, " +
                "storage TEXT, " +
                "inventory_number TEXT UNIQUE)";
            
            Statement stmt = connection.createStatement();
            stmt.execute(createTable);
            stmt.close();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Помилка створення БД: " + e.getMessage(), 
                "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Інформація про книгу"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        addField(inputPanel, gbc, "Автор(и):", txtAuthors = new JTextField(30), 0);
        addField(inputPanel, gbc, "Назва:", txtTitle = new JTextField(30), 1);
        addField(inputPanel, gbc, "Видавництво:", txtPublisher = new JTextField(30), 2);
        addField(inputPanel, gbc, "Кількість сторінок:", txtPages = new JTextField(30), 3);
        addField(inputPanel, gbc, "Жанр:", txtGenre = new JTextField(30), 4);
        addField(inputPanel, gbc, "Ціна:", txtPrice = new JTextField(30), 5);
        addField(inputPanel, gbc, "Книгосховище:", txtStorage = new JTextField(30), 6);
        addField(inputPanel, gbc, "Обліковий номер:", txtInventoryNum = new JTextField(30), 7);
        
        JPanel buttonPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        btnAdd = new JButton("Додати");
        btnUpdate = new JButton("Оновити");
        btnDelete = new JButton("Видалити");
        btnClear = new JButton("Очистити");
        
        btnAdd.setFont(new Font("Arial", Font.BOLD, 14));
        btnUpdate.setFont(new Font("Arial", Font.BOLD, 14));
        btnDelete.setFont(new Font("Arial", Font.BOLD, 14));
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Пошук:"));
        txtSearch = new JTextField(15);
        btnSearch = new JButton("Знайти");
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        buttonPanel.add(searchPanel);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        String[] columns = {"ID", "Автор(и)", "Назва", "Видавництво", 
                           "Сторінок", "Жанр", "Ціна", "Книгосховище", "Обл.номер"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(table);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        btnAdd.addActionListener(e -> addBook());
        btnUpdate.addActionListener(e -> updateBook());
        btnDelete.addActionListener(e -> deleteBook());
        btnClear.addActionListener(e -> clearFields());
        btnSearch.addActionListener(e -> searchBooks());
        
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFieldsFromTable();
            }
        });
    }
    
    private void addField(JPanel panel, GridBagConstraints gbc, String label, JTextField field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(new JLabel(label), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }
    
    private void loadData() {
        try {
            tableModel.setRowCount(0);
            String query = "SELECT * FROM Books";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("authors"),
                    rs.getString("title"),
                    rs.getString("publisher"),
                    rs.getInt("pages"),
                    rs.getString("genre"),
                    rs.getDouble("price"),
                    rs.getString("storage"),
                    rs.getString("inventory_number")
                };
                tableModel.addRow(row);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Помилка завантаження даних: " + e.getMessage(), 
                "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addBook() {
        if (txtAuthors.getText().trim().isEmpty() || txtTitle.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Заповніть обов'язкові поля: Автор(и) та Назва!", 
                "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            String query = "INSERT INTO Books (authors, title, publisher, pages, genre, price, storage, inventory_number) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, txtAuthors.getText().trim());
            pstmt.setString(2, txtTitle.getText().trim());
            pstmt.setString(3, txtPublisher.getText().trim());
            
            try {
                pstmt.setInt(4, Integer.parseInt(txtPages.getText().trim()));
            } catch (NumberFormatException e) {
                pstmt.setInt(4, 0);
            }
            
            pstmt.setString(5, txtGenre.getText().trim());
            
            try {
                pstmt.setDouble(6, Double.parseDouble(txtPrice.getText().trim()));
            } catch (NumberFormatException e) {
                pstmt.setDouble(6, 0.0);
            }
            
            pstmt.setString(7, txtStorage.getText().trim());
            pstmt.setString(8, txtInventoryNum.getText().trim());
            
            pstmt.executeUpdate();
            pstmt.close();
            
            JOptionPane.showMessageDialog(this, 
                "Книгу додано успішно!", 
                "Успіх", JOptionPane.INFORMATION_MESSAGE);
            
            loadData();
            clearFields();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Помилка додавання: " + e.getMessage(), 
                "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateBook() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Виберіть книгу для оновлення!", 
                "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            
            String query = "UPDATE Books SET authors=?, title=?, publisher=?, pages=?, " +
                          "genre=?, price=?, storage=?, inventory_number=? WHERE id=?";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, txtAuthors.getText().trim());
            pstmt.setString(2, txtTitle.getText().trim());
            pstmt.setString(3, txtPublisher.getText().trim());
            
            try {
                pstmt.setInt(4, Integer.parseInt(txtPages.getText().trim()));
            } catch (NumberFormatException e) {
                pstmt.setInt(4, 0);
            }
            
            pstmt.setString(5, txtGenre.getText().trim());
            
            try {
                pstmt.setDouble(6, Double.parseDouble(txtPrice.getText().trim()));
            } catch (NumberFormatException e) {
                pstmt.setDouble(6, 0.0);
            }
            
            pstmt.setString(7, txtStorage.getText().trim());
            pstmt.setString(8, txtInventoryNum.getText().trim());
            pstmt.setInt(9, id);
            
            pstmt.executeUpdate();
            pstmt.close();
            
            JOptionPane.showMessageDialog(this, 
                "Дані оновлено!", 
                "Успіх", JOptionPane.INFORMATION_MESSAGE);
            
            loadData();
            clearFields();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Помилка оновлення: " + e.getMessage(), 
                "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteBook() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Виберіть книгу для видалення!", 
                "Помилка", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
            "Ви впевнені, що хочете видалити цю книгу?", 
            "Підтвердження", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                int id = (int) tableModel.getValueAt(selectedRow, 0);
                
                String query = "DELETE FROM Books WHERE id=?";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
                pstmt.close();
                
                JOptionPane.showMessageDialog(this, 
                    "Книгу видалено!", 
                    "Успіх", JOptionPane.INFORMATION_MESSAGE);
                
                loadData();
                clearFields();
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Помилка видалення: " + e.getMessage(), 
                    "Помилка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void searchBooks() {
        String searchText = txtSearch.getText().trim();
        
        if (searchText.isEmpty()) {
            loadData();
            return;
        }
        
        try {
            tableModel.setRowCount(0);
            
            String query = "SELECT * FROM Books WHERE " +
                          "authors LIKE ? OR title LIKE ? OR publisher LIKE ? OR " +
                          "genre LIKE ? OR inventory_number LIKE ?";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            String searchPattern = "%" + searchText + "%";
            for (int i = 1; i <= 5; i++) {
                pstmt.setString(i, searchPattern);
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("authors"),
                    rs.getString("title"),
                    rs.getString("publisher"),
                    rs.getInt("pages"),
                    rs.getString("genre"),
                    rs.getDouble("price"),
                    rs.getString("storage"),
                    rs.getString("inventory_number")
                };
                tableModel.addRow(row);
            }
            
            rs.close();
            pstmt.close();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Помилка пошуку: " + e.getMessage(), 
                "Помилка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearFields() {
        txtAuthors.setText("");
        txtTitle.setText("");
        txtPublisher.setText("");
        txtPages.setText("");
        txtGenre.setText("");
        txtPrice.setText("");
        txtStorage.setText("");
        txtInventoryNum.setText("");
        txtSearch.setText("");
        loadData();
    }
    
    private void fillFieldsFromTable() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            txtAuthors.setText(tableModel.getValueAt(selectedRow, 1).toString());
            txtTitle.setText(tableModel.getValueAt(selectedRow, 2).toString());
            txtPublisher.setText(tableModel.getValueAt(selectedRow, 3).toString());
            txtPages.setText(tableModel.getValueAt(selectedRow, 4).toString());
            txtGenre.setText(tableModel.getValueAt(selectedRow, 5).toString());
            txtPrice.setText(tableModel.getValueAt(selectedRow, 6).toString());
            txtStorage.setText(tableModel.getValueAt(selectedRow, 7).toString());
            txtInventoryNum.setText(tableModel.getValueAt(selectedRow, 8).toString());
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LibraryApp().setVisible(true);
        });
    }
}
