 package cz.datalite.zk.components.combo;
 
 import cz.datalite.zk.components.cascade.Cascadable;
 import cz.datalite.zk.components.cascade.CascadableComponent;
 import cz.datalite.zk.components.cascade.CascadableExt;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import org.zkoss.zk.ui.Component;
 
 /**
  * Default implementation of the Combobox controller. This is used when
  * user wants to use default ZK combobox. This implementation is required
  * because of MVC architecture which isn't supported by ZK framework.
  * @param <T> 
  * @author Karel Cemus
  */
 public class DLComboboxDefaultController<T> implements DLComboboxExtController<T> {
 
     /** View - component in the browser */
     protected DLCombobox<T> combobox;
 
     /**
      * Default contructor
      * @param combobox component in the browser - view part in the MVC architecture
      */
     public DLComboboxDefaultController( final DLCombobox<T> combobox ) {
         this.combobox = combobox;
     }
 
     public void onOpen() {
         // it is empty because this controller is only for default ZK implementation
         // so onOpen event it must do nothing
     }
 
     public boolean isInModel( final T entity ) {
         if ( getModel().size() > 50 ) return true;
         else return getModel().contains( entity );
     }
 
     public void add( final T entity ) {
         final List<T> list = getModel();
         list.add( entity );
         combobox.setListModel( list );
     }
 
     @SuppressWarnings( "unchecked" )
     public List<T> getModel() {
         final List<T> list = new LinkedList<T>();
 
         if (combobox.getModel() != null)
         {
             for ( int i = 0; i < combobox.getModel().getSize(); i++ ) {
                 list.add( (T) combobox.getModel().getElementAt( i ) );
             }
         }
         return new ArrayList<T>( list );
     }
 
     public void doAfterCompose( final Component comp ) {
         throw new UnsupportedOperationException( "Not supported yet." );
     }
 
     @SuppressWarnings( "unchecked" )
     public T getSelectedItem() {
         if (combobox.getModel() == null)
         {
             return null;
         }
         return (T) combobox.getModel().getElementAt( combobox.getSelectedIndex() );
     }
 
     public int getSelectedIndex() {
         return combobox.getSelectedIndex();
     }
 
     public void addDefaultParent() {
     }
 
     public void addParent( final Cascadable parent, final String column ) {
         throw new UnsupportedOperationException( "Not supported yet." );
     }
 
     public void fireParentChanges( final Cascadable parent ) {
         throw new UnsupportedOperationException( "Not supported yet." );
     }
 
    @Override
    public void fireCascadeChanges() {
        throw new UnsupportedOperationException( "Not supported yet." );
    }

     public void addFollower( final CascadableExt follower ) {
         throw new UnsupportedOperationException( "Not supported yet." );
     }
 
     public CascadableComponent getCascadableComponent() {
         return combobox;
     }
 }
