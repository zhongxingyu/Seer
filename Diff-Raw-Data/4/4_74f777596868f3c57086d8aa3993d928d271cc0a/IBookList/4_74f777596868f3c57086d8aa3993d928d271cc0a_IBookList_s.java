 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package group20.bookexchange.core;
 
 import group20.bookexchange.db.IDAO;
 import java.util.List;
 
 /**
  *
  * @author Daniel
  */
 public interface IBookList extends IDAO<Book, Long> {
     public List<Book> getByTitle(String title);
     public List<Book> getByAuthor(String author);
     public List<Book> getByCourse(String course);
     public List<Book> getByState(Book.BookState bookState);
<<<<<<< HEAD:jpa_bookexchange/src/main/java/group20/bookexchange/core/IBookList.java
     public List<Book> getByUser(User user);
     
=======
>>>>>>> origin/Dev:BookExchange/src/main/java/group20/bookexchange/core/IBookList.java
 }
