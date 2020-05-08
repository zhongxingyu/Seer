 package cz.datalite.zk.components.list.view;
 
 import cz.datalite.helpers.ZKBinderHelper;
 import cz.datalite.zk.components.list.controller.DLListboxComponentController;
 import java.util.List;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.event.SelectEvent;
 import org.zkoss.zul.ListModel;
 import org.zkoss.zul.Listbox;
 import org.zkoss.zul.Listgroup;
 import org.zkoss.zul.Listitem;
 import org.zkoss.zul.ListitemRenderer;
 
 /**
  * This component is the extension for the ZK listbox. This component and
  * other utils allows user to use sorting, paging, filtering, exporting
  * and some other tools. This component supports MVC architecture.
  * @author Jiri Bubnik
  * @author Karel Čemus <cemus@datalite.cz>
  */
 public class DLListbox extends Listbox {
 
     protected DLListboxComponentController controller;
 
     public void onCreate() {
         if ( controller != null ) {
             controller.onCreate();
         }
     }
 
     public void setListModel( final List model ) {
         setModel( new org.zkoss.zkplus.databind.BindingListModelList( model, true ) );
     }
 
     @Override
     public void setModel( final ListModel model ) {

        final Execution exec = Executions.getCurrent();
	exec.removeAttribute("zkoss.Listbox.deferInitModel_"+getUuid()); // TODO problems with ROD - Listbox#2482

         super.setModel( model );
 
         final int cnt = getItemCount();
 
         // Automatical first row selection
         if ( isSelectFirstRow() && cnt > 0 ) {
             ZKBinderHelper.loadComponentAttribute( this, "selectedItem" );
             if ( getSelectedItem() == null ) {
                 // if no  record is selected then select the first
                 // listgroups have to be skipped
                 int index;
                 for ( index = 0; getItemAtIndex( index ) instanceof Listgroup && index < cnt; ++index ) {
                 }
                 if ( index != cnt ) {
                     setSelectedIndex( index, true );
                 }
                 ZKBinderHelper.saveComponentAttribute( this, "selectedItem" );
             }
         }
 //        JB - it mihgt break client code if it doesn't expect null in selectedItem
 //        else if (cnt == 0)
 //        {
 //            // No binding for null from ZK
 //            //ZKBinderHelper.saveComponentAttribute( this, "selectedItem" );
 //        }
     }
 
     /**
      * <p>Sets selected index and optionaly send event onSelect.
      * Acceptable values are -1 for null and from 0 to model.size()</p>
      *
      * <p>This method can be used to reset selected item. For example
      * it can be called from {@link cz.datalite.zk.components.list.DLListboxGeneralController#loadData(java.util.List, int, int, java.util.List) .}
      * and its implementations like {@link cz.datalite.zk.components.list.DLListboxSimpleController#loadData(java.util.List, int, int, java.util.List)}
      * and {@link cz.datalite.zk.components.list.DLListboxCriteriaController#loadData(cz.datalite.dao.DLSearch)}.</p>
      * when m
      * @param index new selected index
      * @param sendOnSelect send onSelect event
      */
     public void setSelectedIndex( final int index, final boolean sendOnSelect ) {
         if ( getSelectedIndex() == index ) {
             return;
         }
         setSelectedIndex( index );
         if ( sendOnSelect ) {
             Events.postEvent( new SelectEvent( Events.ON_SELECT, this, getSelectedItems() ) );
         }
     }
 
     /**
      * Sets selected item by value
      *
      * @param value hodnota itemu, podle ktere se vyhledava
      * @param select if is selected
      */
     public void selectItemByValue( final Object value, final boolean select ) {
         for ( Listitem item : ( List<Listitem> ) getItems() ) {
             if ( item.getValue() == null && value == null
                     || (value != null && value.equals( item.getValue() )) ) {
                 if ( select ) {
                     this.addItemToSelection( item );
                 } else {
                     this.removeItemFromSelection( item );
                 }
             }
         }
 
         // ZK bug - doesn't reload automatically
         if ( "select".equals( getMold() ) ) {
             invalidate();
         }
     }
 
     /**
      * Returns value of the selected item - equivalent to getSelectedItem().getValue()
      * @return value of the selected item or null if no item is selected
      */
     public Object getSelectedValue() {
         if ( getSelectedItem() != null ) {
             return getSelectedItem().getValue();
         } else {
             return null;
         }
     }
 
     /**
      * Selects first for in the list if some exists
      * 
      * @return true if the first is selected, false if list is empty
      */
     public boolean selectFirstRow() {
         if ( getModel().getSize() > 0 ) {
             setSelectedIndex( 0, true );
             return true;
         } else {
             return false;
         }
     }
     /**
      * should be automatically selected first row
      */
     private boolean selectFirstRow;
 
     /**
      * Should be selected first row automatically after insertion new model
      * @return the selectFirstRow
      */
     public boolean isSelectFirstRow() {
         return selectFirstRow;
     }
 
     /**
      * <p>Should be selected first row automatically after insertion new model</p>
      *
      * <p>In the classical M-V-C architecture this component is the View.
      * if user want to select something then he clicks on this component
      * and it emits the event.</p>
      *
      * <p>So if the model and controller thinks that there is selected
      * different value than which is in real then the event should be
      * emitted to update model.</p>
      *
      * <p>If there is no selectedItem and new model is set
      * then the first row is automaticly selected if it is enabled.
      * Because model and ctl expect that here is null which is not
      * then the onSelectEvent is emitted. Doesn't matter who did the
      * change if user or view component itself.</p>
      *
      * @param selectFirstRow the selectFirstRow to set
      */
     public void setSelectFirstRow( final boolean selectFirstRow ) {
         this.selectFirstRow = selectFirstRow;
     }
 
     public void setController( final DLListboxComponentController controller ) {
         assert controller != null : "Controller cannot be null.";
         this.controller = controller;
     }
 
     public DLListboxComponentController getController() {
         return controller;
     }
 
     @SuppressWarnings( "unchecked" )
     public List<DLListheader> getListheaders() {
         return ( List<DLListheader> ) getListhead().getChildren();
     }
 
     @SuppressWarnings( "unchecked" )
     @Override
     public void setItemRenderer( final ListitemRenderer renderer ) {
         super.setItemRenderer( renderer );
         if ( controller != null ) {
             controller.setRendererTemplate( getItemAtIndex( 0 ) );
         }
     }
 
     @Override
     public void onInitRender() {
         super.onInitRender();
 
         // contrller doesn't support multpile select item. Setup selected index only if controller has selected item
         if ( controller != null && controller.getSelectedIndex() != -1 ) {
             setSelectedIndex( controller.getSelectedIndex(), false );
         }
     }
     /** Should the page start with listbox filled with data */
     boolean loadDataOnCreate = true;
 
     /**
      * Should the page start with listbox filled with data .
      * @param set true/false (default is true)
      */
     public void setLoadDataOnCreate( boolean set ) {
         loadDataOnCreate = set;
     }
 
     /**
      * Should the page start with listbox filled with data.
      * @return true/false (default is true)
      */
     public boolean isLoadDataOnCreate() {
         return loadDataOnCreate;
     }
 }
