 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package group20.jsf.bb;
 
 import group20.bookexchange.core.Book;
 import group20.bookexchange.core.Book.BookState;
 import group20.bookexchange.core.User;
 import group20.jsf.mb.ExchangeBean;
 import java.io.Serializable;
import java.util.Date;
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 /**
  *
  * @author alexandralazic
  */
 @Named("addBook")
 @RequestScoped
 public class AddBookBB implements Serializable{
     
     private String title;
     private String author;
     private int price;
     private User owner;
     private String course;
     private BookState bookState;
     private ExchangeBean eb;
     
     public String save(){
        Book b = new Book(title, author, price, owner, course, bookState, new Date(1L));
         eb.getBookList().add(b);
         return null; //what to return?
     }
     
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public int getPrice() {
         return price;
     }
 
     public void setPrice(int price) {
         this.price = price;
     }
             
     @Inject
     public void setShop(ExchangeBean s){
         this.eb = s;
     }
 }
