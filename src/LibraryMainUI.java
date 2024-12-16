import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class LibraryMainUI extends JFrame {
    private JButton manageBooksButton;
    private JButton listBooksButton;
    private JButton listAuthorsButton;
    private JButton registerNewBorrowerButton;
    private JButton listBorrowersButton;
    private JButton manageBorrowRecordsButton;
    private JButton searchBorrowerByEmailButton;
    private JDialog bookManagementDialog;
    private JTextField titleField, authorField, genreField;
    private JCheckBox isBorrowedCheckBox;

    //interaction with database
    Connection connection;

    public LibraryMainUI(Connection connection) {
        this.connection = connection; // Assign the passed connection to the class field

        // The rest of your existing constructor code
        setTitle("Library Management System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(7, 1, 10, 10));


        // Initialize buttons and add action listeners
        manageBooksButton = new JButton("Manage Books");
        manageBooksButton.addActionListener(new ManageBooksAction());

        listBooksButton = new JButton("List Books");
        listBooksButton.addActionListener(new ListBooksAction());

        listAuthorsButton = new JButton("List Authors");
        listAuthorsButton.addActionListener(new ListAuthorsAction());

        registerNewBorrowerButton = new JButton("Register New Borrower");
        registerNewBorrowerButton.addActionListener(new RegisterNewBorrowerAction());

        listBorrowersButton = new JButton("List Borrowers");
        listBorrowersButton.addActionListener(new ListBorrowersAction());


        manageBorrowRecordsButton = new JButton("Manage Borrow Records");
        manageBorrowRecordsButton.addActionListener(new ManageBorrowRecordsAction());

        searchBorrowerByEmailButton = new JButton("Search Borrower by Email");
        searchBorrowerByEmailButton.addActionListener(e -> {
            String email = JOptionPane.showInputDialog(this, "Enter the borrower's email:");
            if (email != null && !email.trim().isEmpty()) {
                Integer borrowerId = getBorrowerIdByEmail(email.trim());
                if (borrowerId != null) {
                    JOptionPane.showMessageDialog(this, "Borrower ID: " + borrowerId);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a valid email.");
            }
        });

        // Add buttons to the frame
        add(manageBooksButton);
        add(listBooksButton);
        add(listAuthorsButton);
        add(registerNewBorrowerButton);
        add(listBorrowersButton);
        add(manageBorrowRecordsButton);
        add(searchBorrowerByEmailButton);
    }
    public Integer getBorrowerIdByEmail(String email) {
        try {
            //SQL query to return borrower ID
            PreparedStatement ps = connection.prepareStatement("SELECT id FROM borrowers WHERE email = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                JOptionPane.showMessageDialog(this, "No borrower found with the email: " + email);
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving borrower ID: " + e.getMessage());
            return null;
        }
    }

    // Get all borrowers from the database
    public List<Borrower> getBorrowers() {
        List<Borrower> borrowers = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM borrowers");
            while (rs.next()) {
                borrowers.add(new Borrower(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return borrowers;
    }

    // Get all books from the database
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM books");
            while (rs.next()) {
                books.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getBoolean("is_borrowed")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    // Get all authors from the database
    public List<String> getAuthors() {
        List<String> authors = new ArrayList<>();
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT DISTINCT author FROM books");
            while (rs.next()) {
                authors.add(rs.getString("author"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return authors;
    }
    // Get a specific book by its ID from the database
    public Book getBookById(int id) {
        Book book = null;
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM books WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                book = new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("genre"),
                        rs.getBoolean("is_borrowed")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return book;
    }
    public List<BorrowRecord> getBorrowRecordsByBorrower(int borrowerId) {
        List<BorrowRecord> records = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM borrow_records WHERE borrower_id = ?");
            ps.setInt(1, borrowerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                int bookId = rs.getInt("book_id");
                LocalDate borrowDate = Instant.ofEpochMilli(rs.getLong("borrow_date"))
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                LocalDate returnDate = null;
                if (rs.getObject("return_date") != null) {
                    returnDate = Instant.ofEpochMilli(rs.getLong("return_date"))
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                }

                records.add(new BorrowRecord(id, bookId, borrowerId, borrowDate, returnDate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }


    private class ManageBooksAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Initialize book management dialog
            bookManagementDialog = new JDialog(LibraryMainUI.this, "Manage Books", true);
            bookManagementDialog.setSize(400, 300);
            bookManagementDialog.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Input fields for book details
            titleField = new JTextField(20);
            authorField = new JTextField(20);
            genreField = new JTextField(20);
            isBorrowedCheckBox = new JCheckBox("Is Borrowed");

            // Adding components to the dialog
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            bookManagementDialog.add(new JLabel("Title:"), gbc);
            gbc.gridx = 1;
            bookManagementDialog.add(titleField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            bookManagementDialog.add(new JLabel("Author:"), gbc);
            gbc.gridx = 1;
            bookManagementDialog.add(authorField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 2;
            bookManagementDialog.add(new JLabel("Genre:"), gbc);
            gbc.gridx = 1;
            bookManagementDialog.add(genreField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 3;
            bookManagementDialog.add(isBorrowedCheckBox, gbc);

            // Buttons for adding, updating, and deleting books
            JButton addBookButton = new JButton("Add Book");
            addBookButton.addActionListener(new AddBookAction());
            gbc.gridx = 0;
            gbc.gridy = 4;
            bookManagementDialog.add(addBookButton, gbc);

            JButton updateBookButton = new JButton("Update Book");
            updateBookButton.addActionListener(new UpdateBookAction());
            gbc.gridx = 1;
            bookManagementDialog.add(updateBookButton, gbc);

            JButton deleteBookButton = new JButton("Delete Book");
            deleteBookButton.addActionListener(new DeleteBookAction());
            gbc.gridx = 0;
            gbc.gridy = 5;
            bookManagementDialog.add(deleteBookButton, gbc);

            // Set dialog to be visible
            bookManagementDialog.setLocationRelativeTo(LibraryMainUI.this);
            bookManagementDialog.setVisible(true);
        }
    }

    private class AddBookAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String title = titleField.getText();
            String author = authorField.getText();
            String genre = genreField.getText();
            boolean isBorrowed = isBorrowedCheckBox.isSelected();

            try {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO books (title, author, genre, is_borrowed) VALUES (?, ?, ?, ?)");
                ps.setString(1, title);
                ps.setString(2, author);
                ps.setString(3, genre);
                ps.setBoolean(4, isBorrowed);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(bookManagementDialog, "Book added successfully!");
                bookManagementDialog.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(bookManagementDialog, "Error adding book: " + ex.getMessage());
            }
        }
    }

    private class UpdateBookAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Prompt for the book ID to update
            String input = JOptionPane.showInputDialog(LibraryMainUI.this, "Enter the ID of the book to update:");
            if (input == null || input.trim().isEmpty()) {
                JOptionPane.showMessageDialog(LibraryMainUI.this, "No ID entered!");
                return;
            }

            int bookId;
            try {
                bookId = Integer.parseInt(input.trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(LibraryMainUI.this, "Invalid ID format!");
                return;
            }

            // Fetch the book details from the database
            Book book = getBookById(bookId);
            if (book == null) {
                JOptionPane.showMessageDialog(LibraryMainUI.this, "Book not found!");
                return;
            }

            // Create the update form
            JTextField titleField = new JTextField(book.getTitle());
            JTextField authorField = new JTextField(book.getAuthor());
            JTextField genreField = new JTextField(book.getGenre());
            JCheckBox isBorrowedCheckBox = new JCheckBox("Is Borrowed", book.isBorrowed());

            JPanel updatePanel = new JPanel(new GridLayout(4, 2));
            updatePanel.add(new JLabel("Title:"));
            updatePanel.add(titleField);
            updatePanel.add(new JLabel("Author:"));
            updatePanel.add(authorField);
            updatePanel.add(new JLabel("Genre:"));
            updatePanel.add(genreField);
            updatePanel.add(new JLabel("Is Borrowed:"));
            updatePanel.add(isBorrowedCheckBox);

            // Show the dialog
            int result = JOptionPane.showConfirmDialog(
                    LibraryMainUI.this,
                    updatePanel,
                    "Update Book",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (result == JOptionPane.OK_OPTION) {
                // Retrieve updated values from the form
                String updatedTitle = titleField.getText();
                String updatedAuthor = authorField.getText();
                String updatedGenre = genreField.getText();
                boolean updatedBorrowedStatus = isBorrowedCheckBox.isSelected();

                try {
                    // Update the book details in the database
                    PreparedStatement ps = connection.prepareStatement(
                            "UPDATE books SET title = ?, author = ?, genre = ?, is_borrowed = ? WHERE id = ?"
                    );
                    ps.setString(1, updatedTitle);
                    ps.setString(2, updatedAuthor);
                    ps.setString(3, updatedGenre);
                    ps.setBoolean(4, updatedBorrowedStatus);
                    ps.setInt(5, bookId);

                    int rowsAffected = ps.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(LibraryMainUI.this, "Book updated successfully!");
                    } else {
                        JOptionPane.showMessageDialog(LibraryMainUI.this, "Failed to update the book!");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LibraryMainUI.this, "Error updating book: " + ex.getMessage());
                }
            }
        }
    }
    private class DeleteBookAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String input = JOptionPane.showInputDialog(LibraryMainUI.this, "Enter the ID of the book to delete:");
            if (input == null || input.trim().isEmpty()) {
                JOptionPane.showMessageDialog(LibraryMainUI.this, "No ID entered!");
                return;
            }

            int bookId;
            try {
                bookId = Integer.parseInt(input.trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(LibraryMainUI.this, "Invalid ID format!");
                return;
            }

            try {
                // Begin transaction
                connection.setAutoCommit(false);

                // Delete related borrow records
                PreparedStatement ps = connection.prepareStatement("DELETE FROM borrow_records WHERE book_id = ?");
                ps.setInt(1, bookId);
                ps.executeUpdate();

                // Delete the book
                ps = connection.prepareStatement("DELETE FROM books WHERE id = ?");
                ps.setInt(1, bookId);
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    connection.commit(); // Commit transaction
                    JOptionPane.showMessageDialog(LibraryMainUI.this, "Book and related borrow records deleted successfully!");
                } else {
                    connection.rollback(); // Rollback transaction if book not found
                    JOptionPane.showMessageDialog(LibraryMainUI.this, "Book not found!");
                }
            } catch (SQLException ex) {
                try {
                    connection.rollback(); // Rollback transaction on error
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
                ex.printStackTrace();
                JOptionPane.showMessageDialog(LibraryMainUI.this, "Error deleting book: " + ex.getMessage());
            } finally {
                try {
                    connection.setAutoCommit(true); // Restore auto-commit mode
                } catch (SQLException autoCommitEx) {
                    autoCommitEx.printStackTrace();
                }
            }
        }
    }

    private class ListBooksAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<Book> books = getAllBooks();
            String[] columnNames = {"ID", "Title", "Author", "Genre", "Is Borrowed"};
            Object[][] data = new Object[books.size()][5];

            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                data[i] = new Object[]{book.getId(), book.getTitle(), book.getAuthor(), book.getGenre(), book.isBorrowed()};
            }

            JTable bookTable = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(bookTable);

            // Create a panel for the search functionality
            JPanel searchPanel = new JPanel(new BorderLayout());
            JTextField searchField = new JTextField(20);
            JButton searchButton = new JButton("Find Book ID");

            // Add search field and button to the search panel
            searchPanel.add(new JLabel("Enter Book Name: "), BorderLayout.WEST);
            searchPanel.add(searchField, BorderLayout.CENTER);
            searchPanel.add(searchButton, BorderLayout.EAST);

            // Add an ActionListener to the search button
            searchButton.addActionListener(ev -> {
                String searchText = searchField.getText().trim();
                if (searchText.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter a book name to search.");
                    return;
                }

                // Search for the book in the list
                Book foundBook = null;
                for (Book book : books) {
                    if (book.getTitle().equalsIgnoreCase(searchText)) {
                        foundBook = book;
                        break;
                    }
                }
                if (foundBook != null) {
                    JOptionPane.showMessageDialog(null, "Book found: ID = " + foundBook.getId());
                } else {
                    JOptionPane.showMessageDialog(null, "No book found with the name: " + searchText);
                }
            });
            // Create a frame to display the table and search functionality
            JFrame frame = new JFrame("List of Books");
            frame.setLayout(new BorderLayout());
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.add(searchPanel, BorderLayout.NORTH);
            frame.setSize(600, 400);
            frame.setVisible(true);
        }
    }

    private class ListAuthorsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Get authors from the database
            List<String> authors = getAuthors();

            // Define the columns for the authors table
            String[] columnNames = {"Author ID", "Author Name"};

            // Create a 2D array to hold authors' data
            String[][] data = new String[authors.size()][2];
            for (int i = 0; i < authors.size(); i++) {
                data[i][0] = String.valueOf(i + 1); // Assigning a temporary ID
                data[i][1] = authors.get(i);       // Author name
            }

            // Create a table with the data and column names
            JTable authorsTable = new JTable(data, columnNames);
            authorsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Allow single selection
            JScrollPane scrollPane = new JScrollPane(authorsTable);

            // Show the authors table in a dialog
            JDialog authorsDialog = new JDialog(LibraryMainUI.this, "List of Authors", true);
            authorsDialog.setLayout(new BorderLayout());
            authorsDialog.add(scrollPane, BorderLayout.CENTER);

            // Add action listener to handle row selection
            authorsTable.getSelectionModel().addListSelectionListener(event -> {
                int selectedRow = authorsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String authorName = (String) authorsTable.getValueAt(selectedRow, 1);
                    // Show books of the selected author
                    showBooksByAuthor(authorName);
                }
            });

            authorsDialog.setSize(400, 300);
            authorsDialog.setLocationRelativeTo(LibraryMainUI.this);
            authorsDialog.setVisible(true);
        }

        // Method to show books of a selected author
        private void showBooksByAuthor(String authorName) {
            List<Book> books = getBooksByAuthor(authorName); // Get books for the selected author

            String[] columnNames = {"Book ID", "Title", "Genre", "Is Borrowed"};
            Object[][] data = new Object[books.size()][4];

            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                data[i] = new Object[]{book.getId(), book.getTitle(), book.getGenre(), book.isBorrowed()};
            }

            // Create a table for the books of the selected author
            JTable booksTable = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(booksTable);

            // Show the books in a dialog
            JDialog booksDialog = new JDialog(LibraryMainUI.this, "Books by " + authorName, true);
            booksDialog.setLayout(new BorderLayout());
            booksDialog.add(scrollPane, BorderLayout.CENTER);

            booksDialog.setSize(600, 400);
            booksDialog.setLocationRelativeTo(LibraryMainUI.this);
            booksDialog.setVisible(true);
        }

        // Get books by author from the database
        public List<Book> getBooksByAuthor(String authorName) {
            List<Book> books = new ArrayList<>();
            try {
                PreparedStatement ps = connection.prepareStatement("SELECT * FROM books WHERE author = ?");
                ps.setString(1, authorName);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    books.add(new Book(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("genre"),
                            rs.getBoolean("is_borrowed")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return books;
        }
    }

    private class RegisterNewBorrowerAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextField nameField = new JTextField(20);
            JTextField emailField = new JTextField(20);

            JPanel registerPanel = new JPanel(new GridLayout(2, 2));
            registerPanel.add(new JLabel("Name:"));
            registerPanel.add(nameField);
            registerPanel.add(new JLabel("Email:"));
            registerPanel.add(emailField);

            int confirm = JOptionPane.showConfirmDialog(LibraryMainUI.this, registerPanel, "Register New Borrower", JOptionPane.OK_CANCEL_OPTION);
            if (confirm == JOptionPane.OK_OPTION) {
                String name = nameField.getText();
                String email = emailField.getText();

                try {
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO borrowers (name, email) VALUES (?, ?)");
                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(LibraryMainUI.this, "Borrower registered successfully!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LibraryMainUI.this, "Error registering borrower: " + ex.getMessage());
                }
            }
        }
    }
    private class ListBorrowersAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<Borrower> borrowers = getBorrowers();
            String[] columnNames = {"ID", "Name", "Email"};
            Object[][] data = new Object[borrowers.size()][3];

            for (int i = 0; i < borrowers.size(); i++) {
                Borrower borrower = borrowers.get(i);
                data[i] = new Object[]{borrower.getId(), borrower.getName(), borrower.getEmail()};
            }

            JTable borrowerTable = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(borrowerTable);

            // Button to display borrowed books
            JButton showBorrowedBooksButton = new JButton("Show Borrowed Books");
            showBorrowedBooksButton.addActionListener(ev -> {
                int selectedRow = borrowerTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "Please select a borrower to view their borrowed books.");
                    return;
                }

                int borrowerId = (int) borrowerTable.getValueAt(selectedRow, 0); // Assuming the ID is in column 0
                showBorrowedBooks(borrowerId);
            });

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(showBorrowedBooksButton, BorderLayout.SOUTH);

            JFrame frame = new JFrame("List of Borrowers");
            frame.add(panel);
            frame.setSize(600, 400);
            frame.setVisible(true);
        }
        private void showBorrowedBooks(int borrowerId) {
            List<BorrowRecord> borrowRecords = getBorrowRecordsByBorrower(borrowerId);
            if (borrowRecords.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No books borrowed by this borrower.");
                return;
            }

            String[] columnNames = {"Book ID", "Title", "Author", "Borrow Date", "Return Date"};
            Object[][] data = new Object[borrowRecords.size()][5];

            for (int i = 0; i < borrowRecords.size(); i++) {
                BorrowRecord record = borrowRecords.get(i);
                Book book = getBookById(record.getBookId());
                data[i] = new Object[]{
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        record.getBorrowDate(),
                        record.getReturnDate() != null ? record.getReturnDate().toString() : "Not Returned"
                };
            }

            JTable bookTable = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(bookTable);

            // Add Return Book Button
            JButton returnBookButton = new JButton("Return Book");
            returnBookButton.addActionListener(ev -> {
                int selectedRow = bookTable.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(null, "Please select a book to return.");
                    return;
                }

                int bookId = (int) bookTable.getValueAt(selectedRow, 0);

                // Update the database
                try {
                    // Update return_date in borrow_records
                    PreparedStatement ps = connection.prepareStatement(
                            "UPDATE borrow_records SET return_date = ? WHERE book_id = ? AND borrower_id = ? AND return_date IS NULL"
                    );
                    ps.setDate(1, Date.valueOf(LocalDate.now()));
                    ps.setInt(2, bookId);
                    ps.setInt(3, borrowerId);
                    ps.executeUpdate();

                    // Update is_borrowed in books
                    ps = connection.prepareStatement("UPDATE books SET is_borrowed = 0 WHERE id = ?");
                    ps.setInt(1, bookId);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Book returned successfully!");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error returning book: " + ex.getMessage());
                }
            });

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(returnBookButton, BorderLayout.SOUTH);

            JFrame frame = new JFrame("Borrowed Books");
            frame.add(panel);
            frame.setSize(600, 400);
            frame.setVisible(true);
        }

    }
    private class ManageBorrowRecordsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTextField borrowerIdField = new JTextField(10);
            JTextField bookIdField = new JTextField(10);
            JPanel borrowRecordPanel = new JPanel(new GridLayout(3, 2));
            borrowRecordPanel.add(new JLabel("Borrower ID:"));
            borrowRecordPanel.add(borrowerIdField);
            borrowRecordPanel.add(new JLabel("Book ID:"));
            borrowRecordPanel.add(bookIdField);

            int confirm = JOptionPane.showConfirmDialog(LibraryMainUI.this, borrowRecordPanel, "Manage Borrow Records", JOptionPane.OK_CANCEL_OPTION);
            if (confirm == JOptionPane.OK_OPTION) {
                try {
                    int borrowerId = Integer.parseInt(borrowerIdField.getText().trim());
                    int bookId = Integer.parseInt(bookIdField.getText().trim());

                    // Check if the book is already borrowed
                    Book book = getBookById(bookId);
                    if (book == null) {
                        JOptionPane.showMessageDialog(LibraryMainUI.this, "Book not found!");
                        return;
                    }
                    if (book.isBorrowed()) {
                        JOptionPane.showMessageDialog(LibraryMainUI.this, "Book is already borrowed!");
                        return;
                    }

                    // Add borrow record
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO borrow_records (borrower_id, book_id, borrow_date) VALUES (?, ?, ?)");
                    ps.setInt(1, borrowerId);
                    ps.setInt(2, bookId);
                    ps.setDate(3, Date.valueOf(LocalDate.now()));
                    ps.executeUpdate();

                    // Update book's is_borrowed status
                    ps = connection.prepareStatement("UPDATE books SET is_borrowed = 1 WHERE id = ?");
                    ps.setInt(1, bookId);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(LibraryMainUI.this, "Borrow record added successfully!");
                } catch (NumberFormatException | SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LibraryMainUI.this, "Error adding borrow record: " + ex.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            // Ensure the JDBC driver is loaded
            Class.forName("org.sqlite.JDBC");

            // Connect to the SQLite database
            Connection connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\mixa2\\IdeaProjects\\Project\\src\\database\\library.db");
            SwingUtilities.invokeLater(() -> new LibraryMainUI(connection).setVisible(true));
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to the database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "SQLite JDBC Driver not found. Please add it to the classpath.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}