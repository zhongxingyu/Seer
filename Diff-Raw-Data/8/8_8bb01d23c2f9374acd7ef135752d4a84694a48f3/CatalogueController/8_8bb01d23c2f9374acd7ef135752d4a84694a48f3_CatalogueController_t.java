 package controllers;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import model.Book;
 import model.Catalogue;
 import dbprocess.DatabaseProcess;
 import exceptions.CatalogueException;
 
 public class CatalogueController {
     private Catalogue cat;
 
     public CatalogueController() {
         cat = new Catalogue();
     }
 
     public ArrayList<Book> getCatalogue() throws SQLException {
         DatabaseProcess process = new DatabaseProcess();
         cat = new Catalogue(process.getBooksBy(DatabaseProcess.CATALOGUE, ""));
         return cat;
     }
 
     public void addBookToCatalogue(Book book) throws CatalogueException, SQLException {
         DatabaseProcess db = new DatabaseProcess();
         db.addBookToCatalogue(book);
         cat.add(book);
     }
 
     /**
      * Searches by all non-null fields of a given book.
      * @param book The Book object to use for searching
      * @return All books that match the criteria of the input book
      * @throws SQLException
      */
     public ArrayList<Book> searchByBook(Book book) throws SQLException {
         DatabaseProcess db = new DatabaseProcess();
 
         // Initialize the ArrayList<Book> to return
         ArrayList<Book> bookList = new ArrayList<Book>();
 
         // Use the database process getBooksBy() method to get books by all applicable fields
 
         // Check ISBN
         if (book.getBookISBN() >= 0) {
             Book bookByISBN = db.getBookByIsbn(book.getBookISBN());
             if (bookByISBN != null) {
                 if (!bookList.contains(bookByISBN)) {
                     bookList.add(bookByISBN);
                 }
             }
             else {
                 System.out.println("Book not found by ISBN");
             }
         }
 
         // Check Title
         if (book.getBookTitle() != null) {
             ArrayList<Book> booksByTitle = db.getBooksBy(DatabaseProcess.TITLE, book.getBookTitle());
             if (booksByTitle != null) {
                 for (Book b : booksByTitle) {
                     if (!bookList.contains(b)) {
                         bookList.add(b);
                     }
                 }
             }
             else {
                 System.out.println("Book not found by Title");
             }
         }
 
         // Check Author
         if (book.getBookAuthor() != null) {
             ArrayList<Book> booksByAuthor = db.getBooksBy(DatabaseProcess.AUTHOR, book.getBookAuthor());
             if (booksByAuthor != null) {
                 for (Book b : booksByAuthor) {
                     if (!bookList.contains(b)) {
                         bookList.add(b);
                     }
                 }
             }
             else {
                 System.out.println("Book not found by Author");
             }
         }
 
         // Check keywords
         if (book.getBookDescription() != null) {
         	ArrayList<Book> booksByDescription = db.getBooksBy(DatabaseProcess.DESCRIPTION, book.getBookDescription());
         	if (booksByDescription != null) {
         		for (Book b : booksByDescription) {
         			if (!bookList.contains(b)) {
        				bookList.add(b);
         			}
         		}
         	}
         	else {
         		System.out.println("Book not found by Description");
         	}
         }
 
         System.out.println("Books returned: ");
         for (Book b: bookList)
         {
             System.out.println(b.title);
         }
         return bookList;
     }
 
     public void removeBookFromCatalogue(Book book) throws CatalogueException, SQLException {
         DatabaseProcess db = new DatabaseProcess();
         db.removeBookFromCatalogue(book.ISBN);
         cat.remove(book);
     }
 
 }
