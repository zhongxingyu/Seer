 /**AUTHOR: Laurence Toal
   *CLASS: CS 230 Data Structures
   *Final Project
   *LAST MODIFIED: 14 December 2011
   *CLASS DESCRIPTION: Creates, organizes, and searches hashmaps of Books and Patrons
 **/
 
 import java.util.*;
 import java.io.*;
import java.awt.Container;
import java.awt.event.*;
import javax.swing.JOptionPane;
 
 public class Library {
     //instance variables
     private String libraryName;
     private String initialBookList;
     private String initialPatronList;
     private List<Book> books;
     private List<Patron> patrons;
     private Map<String, List<Book>> titleIndex;
     private Map<String, List<Book>> authorIndex;
     private Map<String, List<Book>> barcodeIndex;
     private Map<String, List<Patron>> patronNameIndex;
     private Map<String, List<Patron>> patronCardIndex;
    private Book b;
     private static Library INSTANCE;
     
     /**Constructor method to create a new library
     **/
     private Library(String libraryName, String initialBookList, String initialPatronList) {
         this.libraryName = libraryName;
         this.books = new ArrayList<Book>(); 
         this.initialBookList = initialBookList;
         this.patrons = new ArrayList<Patron>();
         this.initialPatronList = initialPatronList;
         this.titleIndex = new HashMap<String, List<Book>>();
         this.authorIndex = new HashMap<String, List<Book>>();
         this.barcodeIndex = new HashMap<String, List<Book>>();
         this.patronNameIndex = new HashMap<String, List<Patron>>();
         this.patronCardIndex = new HashMap<String, List<Patron>>();
 
         try {
             makeArrayListOfBooks(initialBookList);
         } catch (IOException e) {
             System.out.println("No such file");
         }
         try {
             makeArrayListOfPatrons(initialPatronList);
         } catch (IOException e) {
             System.out.println("No such file");
         } 
     }  
     
     /**Gets the library - for use outside the class
       *
       *@return the library
     **/
     public static Library getLibrary() {
         if (INSTANCE == null) {
             INSTANCE = new Library("Burbank", "books.txt", "patrons.txt");
         }
         return INSTANCE;
     }
 
 
     /**Adds Books to the necessary hashmaps
       *
       *@param b the Book to add
     **/
     public void addBook(Book b) {
         books.add(b);
         
         String searchTitle = b.getTitle().toLowerCase().replaceAll(" ", "");
         if (!titleIndex.containsKey(searchTitle)) {
             titleIndex.put(searchTitle, new ArrayList<Book>());
         }
         titleIndex.get(searchTitle).add(b);
         
         String authorNames = b.getAuthor().toLowerCase().replaceAll(" and ", "").trim(); //for handling multiple-author books
         String[] authorWords = authorNames.split("\\s+");
         for (String term: authorWords) {
             if (!authorIndex.containsKey(term)) {
                 authorIndex.put(term, new ArrayList<Book>());
             } else {
                 authorIndex.get(term).add(b);
             }
         } 
 
         String searchBarcode = b.getBarcodeNumber();
         if (!barcodeIndex.containsKey(searchBarcode)) { 
              barcodeIndex.put(searchBarcode, new ArrayList<Book>());
         }
         barcodeIndex.get(searchBarcode).add(b);
     }
     
     /**Adds patrons to the appropriate HashMaps
       * 
       *@param p the Patron to add 
     **/
     public void addPatron(Patron p) {
         patrons.add(p);
         
         String searchName = p.getName().toLowerCase();
         String[] nameWords = searchName.split("\\s+");
         for (String term : nameWords) {
             if (!patronNameIndex.containsKey(term)) {
                 patronNameIndex.put(term, new ArrayList<Patron>());
             }
                 patronNameIndex.get(term).add(p);
         }
 
         String searchCardNumber = p.getCardNumber();
         if (!patronCardIndex.containsKey(searchCardNumber)) {
             patronCardIndex.put(searchCardNumber, new ArrayList<Patron>());
         }
         patronCardIndex.get(searchCardNumber).add(p);
        
     }
     
     
     /**Fills the ArrayList books with Books created
       *by reading in information from a file
       * 
       *@param filename the file where the information is
     **/
     public void makeArrayListOfBooks(String filename) throws IOException { 
         Scanner scan = new Scanner(new File(filename));
         while (scan.hasNextLine()) {
             String[] tokens = scan.nextLine().split("\\s*,\\s*");
             Book book = new Book(tokens[0], tokens[1], tokens[2], tokens[3], tokens[4], tokens[5], tokens[6]);
             addBook(book);
         }
         scan.close();
     }
     
     /**Fills the ArrayList patrons with Patrons created
       *by reading in information from a file
       * 
       *@param filename the file where the information is
     **/
     public void makeArrayListOfPatrons(String filename) throws IOException { 
         Scanner scan = new Scanner(new File(filename));
         while (scan.hasNextLine()) {
             String[] tokens = scan.nextLine().split("\\s*,\\s*");
             Patron patron = new Patron(tokens[0], tokens[1]);
             addPatron(patron); 
         }
         scan.close();
     }
     
     
     /**Displays the result of a search for a book by a title
       *
       *@param title the title to search for
     **/
     public List<Book> findBooksByTitle(String title) {
         title = title.toLowerCase().replaceAll(" ", "");
         if (titleIndex.containsKey(title)) { //we've found a match
             return titleIndex.get(title);
         } else { //we don't have a book with that title, return empty list
             return new ArrayList<Book>();
         }
     }
     
     /**Displays the result of a search for a book by an author
       *
       *@param author the author to search for
     **/
     public List<Book> findBooksByAuthor(String author) {
         author = author.toLowerCase();
         String[] authorWords = author.split("\\P{L}+"); //tokenize
         for (String term: authorWords) {
             if (authorIndex.containsKey(term)) {
                 return authorIndex.get(term);
             }
         } 
         //otherwise there's no match                                                   
         return new ArrayList<Book>();
     }
    
     /**Displays the result of a search for a book by a barcode number
       *
       *@param barcode the barcode number to search for
     **/
     public List<Book> findBooksByBarcode(String barcode) {
         if (barcodeIndex.containsKey(barcode)) { //we've found a match
             return barcodeIndex.get(barcode);
         } else { //we don't have a book with that barcode, return empty list
             return new ArrayList<Book>();
         }
     }
 
     /**Displays the result of a search for a patron by library card number
       *
       *@param cardNumber the library card number to search for
     **/
     public List<Patron> findPatronsByCardNumber(String cardNumber) {
         if (patronCardIndex.containsKey(cardNumber)) { //we've found a match
             return patronCardIndex.get(cardNumber);
         } else { //we don't have a book with that barcode, return empty list
             return new ArrayList<Patron>();
         }   
     }
 
     /**Displays the result of a search for a patron by name
       *
       *@param name the patron name to search for
     **/
     public List<Patron> findPatronsByName(String name) {
         name = name.toLowerCase();
         String[] nameWords = name.split("\\P{L}+"); //tokenize to be able to match just part of a name
         for (String term : nameWords) {
             if (patronNameIndex.containsKey(term)) {
                 return patronNameIndex.get(term);
             }
         }  
         //otherwise there's no match
         return new ArrayList<Patron>();   
     }   
 
     /**Checks a book out to a particular patron
       *
       *@param patronToGetBook the patron to check the book out to
     **/
     public void checkOutBook(String patronToGetBook) {
         if (patronToGetBook.matches("\\d+")) {  //a library card number was entered
             //....
         } else if (patronToGetBook.matches("[a-zA-Z]+( [a-zA-Z]+)*")) {  //a name was entered
             //....
         } else {
             JOptionPane.showMessageDialog(null, "Not a valid input");
             patronToGetBook = JOptionPane.showInputDialog(null, "Please enter the name or card number of the patron \n" + "to check " + b.getTitle() + " out to"); //get patron to check the book out to
             checkOutBook(patronToGetBook);
         }
     }
  
 
 }
