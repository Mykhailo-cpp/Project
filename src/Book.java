// Book.java
public class Book {
    private int id;
    private String title;
    private String author;
    private String genre;
    private boolean isBorrowed;

    //Constructor
    public Book(int id, String title, String author, String genre, boolean isBorrowed) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.isBorrowed = isBorrowed;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }
    public boolean isBorrowed() {
        return isBorrowed;
    }
}
