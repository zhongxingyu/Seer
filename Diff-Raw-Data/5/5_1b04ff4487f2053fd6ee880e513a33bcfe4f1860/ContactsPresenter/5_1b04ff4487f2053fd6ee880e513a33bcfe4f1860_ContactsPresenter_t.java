 package com.google.gwt.sample.contacts.client.presenter;
 
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.sample.contacts.client.ContactsServiceAsync;
 import com.google.gwt.sample.contacts.client.common.ColumnDefinition;
 import com.google.gwt.sample.contacts.client.common.SelectionModel;
 import com.google.gwt.sample.contacts.client.event.AddContactEvent;
 import com.google.gwt.sample.contacts.client.event.EditContactEvent;
 import com.google.gwt.sample.contacts.client.view.ContactsView;
 import com.google.gwt.sample.contacts.shared.ContactDetails;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasWidgets;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ContactsPresenter
   implements Presenter, ContactsView.Presenter<ContactDetails>
 {
   private List<ContactDetails> contactDetails;
   private final ContactsServiceAsync rpcService;
   private final HandlerManager eventBus;
   private final ContactsView<ContactDetails> view;
   private final SelectionModel<ContactDetails> selectionModel;
 
   public ContactsPresenter( final ContactsServiceAsync rpcService,
                             final HandlerManager eventBus, final ContactsView<ContactDetails> view,
                             final List<ColumnDefinition<ContactDetails>> columnDefinitions )
   {
     this.rpcService = rpcService;
     this.eventBus = eventBus;
     this.view = view;
     this.selectionModel = new SelectionModel<ContactDetails>();
     this.view.setPresenter( this );
     this.view.setColumnDefinitions( columnDefinitions );
   }
 
   public void onAddButtonClicked()
   {
     eventBus.fireEvent( new AddContactEvent() );
   }
 
   public void onDeleteButtonClicked()
   {
     deleteSelectedContacts();
   }
 
   public void onItemClicked( final ContactDetails contactDetails )
   {
     eventBus.fireEvent( new EditContactEvent( contactDetails.getId() ) );
   }
 
   public void onItemSelected( final ContactDetails contactDetails )
   {
     if ( selectionModel.isSelected( contactDetails ) )
     {
       selectionModel.removeSelection( contactDetails );
     }
     else
     {
       selectionModel.addSelection( contactDetails );
     }
   }
 
   public void go( final HasWidgets container )
   {
     container.clear();
     container.add( view.asWidget() );
     fetchContactDetails();
   }
 
   public void sortContactDetails()
   {
     // Yes, we could use a more optimized method of sorting, but the
     //  point is to create a test case that helps illustrate the higher
     //  level concepts used when creating MVP-based applications.
     //
     final int size = contactDetails.size();
     for ( int i = 0; i < size; ++i )
     {
       for ( int j = 0; j < size - 1; ++j )
       {
         if ( contactDetails.get( j )
                .getDisplayName()
                .compareToIgnoreCase( contactDetails.get( j + 1 ).getDisplayName() ) >= 0 )
         {
           final ContactDetails tmp = contactDetails.get( j );
           contactDetails.set( j, contactDetails.get( j + 1 ) );
           contactDetails.set( j + 1, tmp );
         }
       }
     }
   }
 
   public void setContactDetails( final List<ContactDetails> contactDetails )
   {
     this.contactDetails = contactDetails;
     sortContactDetails();
   }
 
   public List<ContactDetails> getContactDetails()
   {
     return contactDetails;
   }
 
   private void fetchContactDetails()
   {
     rpcService.getContactDetails( new AsyncCallback<ArrayList<ContactDetails>>()
     {
       public void onSuccess( final ArrayList<ContactDetails> result )
       {
         setContactDetails( result );
         view.setRowData( contactDetails );
       }
 
       public void onFailure( final Throwable caught )
       {
         Window.alert( "Error fetching contact details" );
       }
     } );
   }
 
   private void deleteSelectedContacts()
   {
     final List<ContactDetails> selectedContacts = selectionModel.getSelectedItems();
     final ArrayList<String> ids = new ArrayList<String>();
 
     for ( final ContactDetails selected : selectedContacts )
     {
       ids.add( selected.getId() );
     }
 
    rpcService.deleteContacts( ids, new AsyncCallback<Void>()
     {
      public void onSuccess( final Void result )
       {
         fetchContactDetails();
       }
 
       public void onFailure( final Throwable caught )
       {
         System.out.println( "Error deleting selected contacts" );
       }
     } );
   }
 }
