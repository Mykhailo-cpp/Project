import java.time.LocalDate;

public class BorrowRecord {
    private int id;
    private int bookId;
    private int borrowerId;
    private LocalDate borrowDate;
    private LocalDate returnDate;

    // Constructor with int for bookId, borrowerId, etc.
    public BorrowRecord(int id, int bookId, int borrowerId, LocalDate borrowDate, LocalDate returnDate) {
        this.id = id;
        this.bookId = bookId;
        this.borrowerId = borrowerId;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
    }

    public int getBookId() {
        return bookId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }
}
