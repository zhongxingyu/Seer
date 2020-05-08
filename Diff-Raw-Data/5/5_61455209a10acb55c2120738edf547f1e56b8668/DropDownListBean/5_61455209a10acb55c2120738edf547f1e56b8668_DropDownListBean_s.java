 /*
  * @author <a href="oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.provider.ui.beans;
 
 import org.gridlab.gridsphere.provider.ui.model.SelectList;
 
 import java.util.Iterator;
 import java.util.Vector;
 import java.util.ArrayList;
 
 
 public class DropDownListBean extends BaseListBean implements DropDownList {
 
     SelectList list = new SelectList();
 
     protected int size = 1;
     protected boolean multiple = false;
 
     public DropDownListBean(String name) {
         super();
         this.name = name;
     }
 
     /**
      * Adds an entry to the dropdownlist.
      * @param name name of the entry
      * @param value value of the entry
      */
     public void add(String name, String value) {
         ListBoxItemBean item = new ListBoxItemBean();
         item.setName(name);
         item.setValue(value);
         list.addElement(item);
     }
 
     /**
      * Adds a selectable item to the list
      * @param item selectable item to be added
      */
     public void add(Selectable item) {
         list.addElement(item);
     }
 
     public String toString() {
         String result = "<select name='"+getTagName()+name+"' size='"+size+"'";
         if (multiple) {
             result = result + " multiple='multiple'" ;
         }
         result = result +">";
         Iterator it = list.iterator();
         while (it.hasNext()) {
             Selectable item = (Selectable)it.next();
             result = result + item.toString();
         }
         result = result +"</select>";
         return result;
     }
 
     public void update(String[] values) {
         //@todo FIXME NPE
         try {
            if (!multiple) {
                 list.unselectAll();
            }
             for (int i=0;i<values.length;i++) {
                 list.setSelected(values[i], true);
             }
         } catch (NullPointerException e) {
             // ok was empty, nothing selected
         }
     }
 
     /**
      * Returns the selected values of the list.
      * @return selected values oof the list
      */
     public ArrayList getSelectedValues() {
         ArrayList result = new ArrayList();
         Iterator it = list.iterator();
         while (it.hasNext()) {
             Selectable item = (Selectable)it.next();
             if (item.isSelected()) {
                 result.add(item.getValue());
             }
         }
         return result;
     }
 
     /**
      * Returns the selected items of the list
      * @return  the selected item of the list
      */
     public ArrayList getSelectedItems() {
         ArrayList result = new ArrayList();
         Iterator it = list.iterator();
         while (it.hasNext()) {
             Selectable item = (Selectable)it.next();
             if (item.isSelected()) {
                 result.add(item);
             }
         }
         return result;
     }
 
 }
