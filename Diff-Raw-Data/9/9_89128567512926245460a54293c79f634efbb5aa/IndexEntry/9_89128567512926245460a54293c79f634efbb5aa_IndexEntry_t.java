 package org.kuali.rice.krtrain.book;
 
 import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;
 
 /**
  * Index Entry Entity
  *
  * @author KRAD Training
  */
 public class IndexEntry extends PersistableBusinessObjectBase {
     private static final long serialVersionUID = 6574987035630737396L;
 
     private String term;
     private String pageNumbers;
 
     public IndexEntry() {
         super();
     }
 
     public IndexEntry(String term, String pageNumbers) {
         super();
 
         this.term = term;
         this.pageNumbers = pageNumbers;
     }
 
     public String getTerm() {
         return term;
     }
 
     public void setTerm(String term) {
         this.term = term;
     }
 
     public String getPageNumbers() {
         return pageNumbers;
     }
 
     public void setPageNumbers(String pageNumbers) {
         this.pageNumbers = pageNumbers;
     }
 }
