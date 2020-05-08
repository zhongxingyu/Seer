 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package net.wazari.dao.entity.facades;
 
 import java.util.List;
 
 /**
  *
  * @author kevinpouget
  */
 public class SubsetOf<T> {
     public static class Bornes {
        public Bornes(int nbElementsPerPage, int firstElement, int currentPage) {
             this.nbElementsPerPage = nbElementsPerPage ;
            this.firstElement = firstElement;
             this.currentPage = currentPage;
             this.nbElements = null ;
         }
         public Bornes(int nbElements) {
             this.nbElementsPerPage = 0 ;
             this.firstElement = 0;
             this.currentPage = 0;
             this.nbElements = nbElements ;
         }
         //number of element per page
         private Integer nbElementsPerPage ;
         //number of elements in total
         private Integer nbElements ;
         //idx of the first element of the current page
         private Integer firstElement ;
         //idx of the current page
         private Integer currentPage ;
         //idx of the last page
         private Integer lastPage ;
 
         public Integer getFirstElement() {
             return firstElement;
         }
 
         public Integer getCurrentPage() {
             return currentPage;
         }
 
         public Integer getLastPage() {
             return lastPage;
         }
 
        public int getNbElement() {
             return nbElements;
         }
 
         private void setNbElement(long nbElements) {
             this.nbElements = (int) nbElements ;
             this.lastPage = (int) Math.ceil(nbElements / (double) nbElementsPerPage) ;
         }
     }
 
     public final Bornes bornes ;
     public final List<T> subset ;
     public int setSize ;
 
     public SubsetOf(Bornes bornes, List<T> subset, Long setSize) {
         this.bornes = bornes;
         this.subset = subset;
         bornes.setNbElement(setSize) ;
     }
 }
