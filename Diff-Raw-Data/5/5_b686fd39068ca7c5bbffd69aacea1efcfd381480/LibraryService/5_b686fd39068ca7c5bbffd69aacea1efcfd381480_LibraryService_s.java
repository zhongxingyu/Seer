 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.service;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import no.hials.muldvarpweb.domain.Course;
 import no.hials.muldvarpweb.domain.LibraryItem;
 
 /**
  *
  * @author Nospherus
  */
 @Stateless
 @Path("Library")
 public class LibraryService {
     @PersistenceContext
     EntityManager entityManager;
         
     
     
     
     public ArrayList<LibraryItem> getlibraryTestData() {  
         ArrayList<LibraryItem> libraryList = new ArrayList<LibraryItem>();
         for (int i = 0; i < 10; i++) {
 
             LibraryItem lI = new LibraryItem("A tale of two men", "How to make money", "Carl Brooke", "Austin Maxwell", "15.03.1998", "29.02.2012", "239", "This is a book explaining how a pair of people can make money", "http://unrestrictedstock.com/wp-content/uploads/office-icons-book-free-stock-vector.jpg", "http://whhs.cps-k12.org/library/images/icon_book.gif", "http://www.hials.no/nor/content/download/49530/977307/file/PB01%20ETABLERING%20AV%20NYE%20STUDIETILBUD%20TIL%20OG%20MED%2030%20STUDIEPOENG.pdf");
 
         }
         return libraryList;
     }
         
 //        @GET
 //        @Produces({MediaType.APPLICATION_JSON})
 //        public List<LibraryItem> findLibraryItems() {
 //            ArrayList lA = new ArrayList<LibraryItem>();
 ////            while(true){
 ////            TypedQuery<LibraryItem> l = entityManager.createQuery("SELECT c FROM LibraryItem c", LibraryItem.class);
 ////            LibraryItem lib = l.getSingleResult();
 ////            if(lib==null){break;}
 ////            lA.add(lib);
 ////        }
 //            return getlibraryTestData();   //For testing only
 //        }
         
         @GET
         @Produces({MediaType.APPLICATION_JSON})
         public List<LibraryItem> getLibrary(){
             makeTestData();
             return entityManager.createQuery("SELECT l from LibraryItem l", LibraryItem.class).getResultList();
         }
     
     
     
 //        public LibraryItem getLibraryItem(@PathParam("id") Long id){
 //            TypedQuery<LibraryItem> l = entityManager.createQuery("SELECT c FROM LibraryItem c WHERE c.id = :id", LibraryItem.class);
 //            l.setParameter("id", id);
 //            return l.getSingleResult();
 //        }
         
         
         public void addLibraryItem(LibraryItem lI){
             lI = entityManager.merge(lI);
             entityManager.persist(lI);
         }
         
         public void removeLibraryItem(LibraryItem lI){
             entityManager.remove(lI);
             entityManager.persist(lI);
         }
         
         public void makeTestData(){
             LibraryItem lI = new LibraryItem("A tale of two men", "How to make money", "Carl Brooke", "Austin Maxwell", "15.03.1998", "29.02.2012", "239", "This is a book explaining how a pair of people can make money", "http://unrestrictedstock.com/wp-content/uploads/office-icons-book-free-stock-vector.jpg", "http://whhs.cps-k12.org/library/images/icon_book.gif", "http://www.hials.no/nor/content/download/49530/977307/file/PB01%20ETABLERING%20AV%20NYE%20STUDIETILBUD%20TIL%20OG%20MED%2030%20STUDIEPOENG.pdf");
             LibraryItem lI2 = new LibraryItem("Monster manual", "Defence against the black arts", "Severus Snape", "Harry Potter", "5.08.2008", "20.05.2012", "100", "This book explains how you can kick ass", "http://unrestrictedstock.com/wp-content/uploads/office-icons-book-free-stock-vector.jpg", "http://whhs.cps-k12.org/library/images/icon_book.gif", "http://www.hials.no/nor/content/download/49530/977307/file/PB01%20ETABLERING%20AV%20NYE%20STUDIETILBUD%20TIL%20OG%20MED%2030%20STUDIEPOENG.pdf");
             LibraryItem lI3 = new LibraryItem("Master & Commander", "A wet movie", "Master B", "Commander D", "09.01.1460", "30.12.2011", "1902", "This book is a book about sailboats and domination", "http://unrestrictedstock.com/wp-content/uploads/office-icons-book-free-stock-vector.jpg", "http://whhs.cps-k12.org/library/images/icon_book.gif", "http://www.hials.no/nor/content/download/49530/977307/file/PB01%20ETABLERING%20AV%20NYE%20STUDIETILBUD%20TIL%20OG%20MED%2030%20STUDIEPOENG.pdf");
             addLibraryItem(lI);
             addLibraryItem(lI2);
             addLibraryItem(lI3);
             
         }
 }
