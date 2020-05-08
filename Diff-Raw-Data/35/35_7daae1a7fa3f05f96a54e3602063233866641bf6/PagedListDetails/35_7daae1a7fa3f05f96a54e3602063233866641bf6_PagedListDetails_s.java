 /**
  * 
  */
 package net.frontlinesms.ui.handler;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Details of the page of a list we are viewing.
  * @author Alex Anderson alex@frontlinesms.com
  */
 public class PagedListDetails {
 //> INSTANCE PROPERTIES
 	/** Total count of all items to be displayed in this list, over all pages. */
 	private final int totalItemCount;
 	/** Items to display on the current page. */
 	private final Object[] listItems;
 	/** Items in {@link #listItems} which should be selected in the list. */
 	private final Set<Object> selectedItems;
 
 //> CONSTRUCTORS
 	public PagedListDetails(int totalItemCount, Object[] listItems, Object...selectedItems) {
 		this.totalItemCount = totalItemCount;
 		this.listItems = listItems;
 		if(selectedItems.length == 0) {
 			this.selectedItems = null;
 		} else {
 			this.selectedItems = new HashSet<Object>();
 			for(Object selected : selectedItems) {
 				this.selectedItems.add(selected);
 			}
 		}
 	}
 
 //> ACCESSORS
 	public int getTotalItemCount() {
 		return totalItemCount;
 	}
 
 	public Object[] getListItems() {
 		return listItems;
 	}
 
 	public boolean isSelected(Object listItem) {
 		return selectedItems != null && selectedItems.contains(listItem);
 	}
 }
