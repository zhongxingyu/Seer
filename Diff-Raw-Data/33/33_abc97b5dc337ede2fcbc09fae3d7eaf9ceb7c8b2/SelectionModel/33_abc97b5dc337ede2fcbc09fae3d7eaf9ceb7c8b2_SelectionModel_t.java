 package com.google.gwt.sample.contacts.client.common;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class SelectionModel<T>
 {
   final List<T> selectedItems = new ArrayList<T>();
 
   public List<T> getSelectedItems()
   {
     return selectedItems;
   }
 
   public void addSelection( final T item )
   {
     selectedItems.add( item );
   }
 
   public void removeSelection( final T item )
   {
     selectedItems.remove( item );
   }
 
   public boolean isSelected( final T item )
   {
     return selectedItems.contains( item );
   }

   public void clear()
   {
     selectedItems.clear();
   }
 }
