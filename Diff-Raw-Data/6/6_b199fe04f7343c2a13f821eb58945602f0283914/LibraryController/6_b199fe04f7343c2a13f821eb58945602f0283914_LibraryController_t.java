 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.web;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import no.hials.muldvarpweb.domain.Course;
 import no.hials.muldvarpweb.domain.LibraryItem;
 import no.hials.muldvarpweb.service.LibraryService;
 
 /**
  *
  * @author Nospherus
  */
 
 @Named
 @SessionScoped
 public class LibraryController implements Serializable {
     @Inject 
     LibraryService service;
     LibraryItem newLibraryItem;
     LibraryItem selected;
     List<LibraryItem> libraryList;
 
     public LibraryItem getLibraryItem() {
         if(newLibraryItem == null)
             newLibraryItem = new LibraryItem();
         
         return newLibraryItem;
     }
 
     public void setLibraryItem(LibraryItem newLibraryItem) {
         this.newLibraryItem = newLibraryItem;
     }
     
     public void addLibraryItem(){
         service.addLibraryItem(newLibraryItem);
         clearItem();
     }
     
     public void makeTestData(){
         service.makeTestData();
     }
     
     public void clearItem(){
         newLibraryItem = null;
     }
     
     public List<LibraryItem> getLibraryItems(){
         return service.getLibrary();
     }
     
     public void setLibraryItems(List<LibraryItem> libraryList){
         this.libraryList = libraryList;
     }
     
      public void setSelected(LibraryItem selected) {
         if(selected == null) {
             selected = getLibraryItem();
         }
         this.selected = selected;
     }
      
     public void deleteLibraryItem(LibraryItem lI){
        if(lI != null) {
            service.removeLibraryItem(lI);
         }
     }
      
      public LibraryItem getSelected(){
          return selected;
      }
      
 }
