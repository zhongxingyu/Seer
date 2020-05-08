 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package javaapplication;
 
 import java.util.Scanner;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.Persistence;
 
 /**
  *
  * @author alper
  */
 public class JavaApplication {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         // TODO code application logic here
        
         // entity manager and a transaction
         EntityManagerFactory emf = Persistence.createEntityManagerFactory("JavaApplicationPU");
         EntityManager em = emf.createEntityManager();
         EntityTransaction tx = em.getTransaction();
         
       
         
         Operations op = new Operations(em, tx);
         
         while(true){
            System.out.println("1. Find  Book");
             System.out.println("2. Create a new Book");
            System.out.println("3. Remove a Book");
             System.out.println("Select Operation:");
             
             Scanner general = new Scanner(System.in);
             String selection = general.nextLine();
             int selec_int = Integer.parseInt(selection);
             
             if(selec_int == 1)  {
                 Book b = new Book();
                 System.out.println("Enter the name of the book:");
                 String book_name = general.nextLine();
                 b = em.find(Book.class, book_name);
                 System.out.println(b.toString());
             }else if (selec_int == 2) {
                 System.out.println("Enter the name of the book:");
                 String book_name = general.nextLine();
                 System.out.println("Enter the name of the author:");
                 String author = general.nextLine();
                 op.createBook(author.toString(), book_name.toString());
                 System.out.println("New book added to store!");
             }else if (selec_int == 3) {
                 System.out.println("Enter the name of the book:");
                 String book_name = general.nextLine();
                 op.removeBook(book_name);
             }else {
                 System.out.println("Select A Valid Operation");
             }
                     
         }
     }
 }
 
 
 class Operations {
     protected EntityManager em;
     protected EntityTransaction tx;
     public Operations(EntityManager em, EntityTransaction tx) {
         this.em = em;
         this.tx = tx;
     }
     
     public void createBook(String author, String book_name){
         Book b = new Book(book_name, author);
         tx.begin();
         em.persist(b);
         tx.commit();
         em.close();
     }
     
     public void removeBook(String book_name){
         Book b = em.find(Book.class, book_name);
         if(b != null) {
             tx.begin();
             em.remove(b);
             tx.commit();
             em.close();
             System.out.println("Entity has been deleted!");
         } else {
             System.out.println("Nothing found!");
         }
     }
 }
