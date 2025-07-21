import java.sql.*;
import java.util.Scanner;

public class LibrarySystem {
    static final String DB_URL = "jdbc:mysql://localhost:3306/LibraryDB";
    static final String USER = "root";
    static final String PASS = "your_password";

    static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    static void borrowBook(int memberId, int bookId) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement checkStock = conn.prepareStatement("SELECT stock FROM Books WHERE book_id = ?");
            checkStock.setInt(1, bookId);
            ResultSet rs = checkStock.executeQuery();
            if (!rs.next() || rs.getInt("stock") <= 0) {
                conn.rollback();
                return;
            }

            PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO Borrowings (member_id, book_id, borrow_date) VALUES (?, ?, CURDATE())");
            insert.setInt(1, memberId);
            insert.setInt(2, bookId);
            insert.executeUpdate();

            PreparedStatement updateStock = conn.prepareStatement("UPDATE Books SET stock = stock - 1 WHERE book_id = ?");
            updateStock.setInt(1, bookId);
            updateStock.executeUpdate();

            conn.commit();
        } catch (Exception e) {}
    }

    static void returnBook(int borrowId) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement getBorrow = conn.prepareStatement("SELECT book_id FROM Borrowings WHERE borrow_id = ?");
            getBorrow.setInt(1, borrowId);
            ResultSet rs = getBorrow.executeQuery();
            if (!rs.next()) {
                conn.rollback();
                return;
            }
            int bookId = rs.getInt("book_id");

            PreparedStatement updateReturn = conn.prepareStatement("UPDATE Borrowings SET return_date = CURDATE() WHERE borrow_id = ?");
            updateReturn.setInt(1, borrowId);
            updateReturn.executeUpdate();

            PreparedStatement updateStock = conn.prepareStatement("UPDATE Books SET stock = stock + 1 WHERE book_id = ?");
            updateStock.setInt(1, bookId);
            updateStock.executeUpdate();

            conn.commit();
        } catch (Exception e) {}
    }

    public static void main(String[] args) {
        borrowBook(1, 1);
        returnBook(1);
    }
}
