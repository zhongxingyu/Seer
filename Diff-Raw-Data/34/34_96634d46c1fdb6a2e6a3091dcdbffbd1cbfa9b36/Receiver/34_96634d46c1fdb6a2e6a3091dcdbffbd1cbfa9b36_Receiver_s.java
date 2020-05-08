 package com.formation.spring.commands;
 
 import com.formation.spring.beans.Book;
 import com.formation.spring.exceptions.LibrarySecurityException;
 import com.formation.spring.services.LibraryService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 /**
  * Created with IntelliJ IDEA.
  * User: notdryft
  * Date: 5/13/13
  * Time: 11:23 PM
  */
 @Component
 public class Receiver {
 
     private static String lineSeparator = System.getProperty("line.separator");
 
     @Autowired
     private LibraryService libraryService;
 
     public Receiver() {
         // Do nothing.
     }
 
     public void showMenu() {
         StringBuilder sb = new StringBuilder();
 
         sb.append("Book store, make your choice!").append(lineSeparator);
         sb.append("-----------------------------").append(lineSeparator);
         sb.append("- books: list all books").append(lineSeparator);
         sb.append("- quit: exit the application").append(lineSeparator);
 
         System.out.print(sb.toString());
     }
 
     public void showBooks() {
         StringBuilder sb = new StringBuilder();
 
         sb.append("Book list, make your choice!").append(lineSeparator);
         sb.append("----------------------------").append(lineSeparator);
         for (Book book : libraryService.findAll()) {
             sb.append(book.getId()).append(" : ").append(book.getName());
 
             if (book.isBorrowed()) {
                 sb.append(" (borrowed)");
             }
 
             sb.append(lineSeparator);
         }
 
         System.out.print(sb.toString());
     }
 
     public void showBook(int id) {
         StringBuilder sb = new StringBuilder();
 
         sb.append("You choose ").append(libraryService.findById(id)).append(lineSeparator);
 
         System.out.println(sb.toString());
     }
 
     public void borrowBook(int id) throws LibrarySecurityException {
         StringBuilder sb = new StringBuilder();
 
         Book book = libraryService.findById(id);
        sb.append("Borrowing book ").append(book.toString());

         book.setBorrowed(true);
 
         System.out.println(sb.toString());
     }
 
     public void lendBook(int id) {
         StringBuilder sb = new StringBuilder();
 
         Book book = libraryService.findById(id);
        sb.append("Lending book ").append(book.toString());

         book.setBorrowed(false);
 
         System.out.println(sb.toString());
     }
 
     public void deleteBook(int id) {
         StringBuilder sb = new StringBuilder();
 
         Book book = libraryService.findById(id);
         sb.append("Deleting book ").append(book);
 
         libraryService.delete(book);
 
         System.out.println(sb.toString());
     }
 
     public void exit() {
        System.out.println("Exiting...");
         System.exit(0);
     }
 }
