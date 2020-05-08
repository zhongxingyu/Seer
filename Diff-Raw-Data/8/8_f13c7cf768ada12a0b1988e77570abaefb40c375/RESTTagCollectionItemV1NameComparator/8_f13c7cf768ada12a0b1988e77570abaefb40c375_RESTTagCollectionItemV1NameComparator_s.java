 package org.jboss.pressgang.ccms.rest.v1.sort;
 
 import java.util.Comparator;
 
import org.jboss.pressgang.ccms.rest.v1.collections.items.join.RESTTagInCategoryCollectionItemV1;
 
 /**
  * A comparator to sort RESTTagCategoryCollectionItemV1 objects by their sort field.
  * @author Matthew Casperson
  */
public class RESTTagCollectionItemV1NameComparator implements Comparator<RESTTagInCategoryCollectionItemV1> {
 
     @Override
    public int compare(final RESTTagInCategoryCollectionItemV1 arg0, final RESTTagInCategoryCollectionItemV1 arg1) {
         if (arg0 == null && arg1 == null) {
             return 0;
         }
 
         if (arg0 == arg1) {
             return 0;
         }
 
         if (arg0 == null) {
             return -1;
         }
 
         if (arg1 == null) {
             return 1;
         }
 
         if (arg0.getItem() == null && arg1.getItem() == null) {
             return 0;
         }
 
         if (arg0.getItem() == null) {
             return -1;
         }
 
         if (arg1.getItem() == null)
         {
             return 1;
         }
         
         /* Second order sort is by name */            
         if (arg0.getItem().getName() == null && arg1.getItem().getName() == null) {
             return 0;
         }
 
         if (arg0.getItem().getName() == null) {
             return -1;
         }
 
         if (arg1.getItem().getName() == null)
         {
             return 1;
         }
         
         return arg0.getItem().getName().compareTo(arg1.getItem().getName());
     }
 
 }
