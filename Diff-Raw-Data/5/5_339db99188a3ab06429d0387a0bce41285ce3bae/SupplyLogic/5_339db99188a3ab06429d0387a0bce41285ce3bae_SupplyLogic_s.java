 
 
 
 package overwatch.controllers;
 
 import overwatch.core.Gui;
 import overwatch.db.Database;
 import overwatch.db.DatabaseConstraints;
 import overwatch.db.EnhancedResultSet;
 import overwatch.db.Supplies;
 import overwatch.gui.CheckedFieldValidator;
 import overwatch.gui.tabs.SupplyTab;
 import overwatch.util.Validator;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 
 
 
 
 /**
  * Supply tab logic
  * 
  * @author  John Murphy
  * @author  Lee Coakley
  * @version 4
  */
 
 
 
 
 
 public class SupplyLogic extends TabController<SupplyTab>
 {
 	
 	public SupplyLogic( SupplyTab tab ){
 		super( tab );
 	}
 	
 	
 	public void respondToTabSelect() {
 		populateTabList();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	
 	
 	private void doSave()
 	{
 		Integer supplyNo    = tab.getSelectedItem();
 		String  supplyName  = tab.name  .field.getText();
 		Integer supplyCount = tab.amount.field.getTextAsInt();
 		
 		if ( ! Supplies.exists(supplyNo)) {
 			Gui.showError( "Failed to save", "The supply no longer exists." );
 			populateTabList(); // Reload
 			return;
 		}
 		
 		Supplies.save( supplyNo, supplyName, supplyCount );
 		
 		populateTabList();
 		tab.setSelectedItem( supplyNo );
 	}
 	
 	
 	
 	
 	
 	private void createNew()
 	{
 		Integer supplyNo = Supplies.create();
 		
 		populateTabList();
 		tab.setSelectedItem( supplyNo );
 	}
 	
 	
 	
 	
 	
 	private void delete()
 	{
 		Integer supplyNo = tab.getSelectedItem();
 		Supplies.delete( supplyNo );		
 		populateTabList();
 	}
 	
 	
 	
 	
 	
 	private void populateTabList()
 	{
 		populateFields( null );
 		
 		tab.setSearchableItems(
 			Database.queryKeyNamePairs( "Supplies", "supplyNo", "name", Integer[].class )
 		);
 	}
 	
 		
 	
 	
 	
 	private void populateFields(Integer supplyNo)
 	{
 		if(supplyNo == null)
 		{
 			tab.setEnableFieldsAndButtons( false );
 			tab.clearFields();
 			tab.setEnableNewButton( true );
 			return;
 		}
 		else {
 			tab.setEnableFieldsAndButtons( true );
 		}
 		
 		EnhancedResultSet ers = Database.query(
 			"SELECT supplyNo, name, count " +
 		    "FROM Supplies    " +
 		    "WHERE supplyNo = " + supplyNo + ";"
 		);
 		
 		if ( ! ers.isEmpty()) {
 			tab.number.field.setText( "" + ers.getElemAs( "supplyNo", Integer.class ));
 			tab.name  .field.setText(      ers.getElemAs( "name",     String .class ));
 			tab.amount.field.setText( "" + ers.getElemAs( "count",    Integer.class ));
 		} else {
 			showDeletedError( "supply" );
 			populateTabList();
 		}		
 	}
 	
 	
 	
 	
 	
 	protected void attachEvents() {
 		setupButtonActions();
 		setupListSelectActions();
 		setupFieldValidators();
 		setupTabChangeActions();
 	}
 	
 	
 	
 	
 	
 	private void setupTabChangeActions() {
 		Gui.getCurrentInstance().addTabSelectNotify( this );
 	}
 	
 	
 	
 	
 	
 	private void setupButtonActions()
 	{
 		tab.addNewListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				createNew();
 			}
 		});
 	
 		tab.addDeleteListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				delete();				
 			}
 		});
 	
 		tab.addSaveListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				doSave();				
 			}
 		});
 	}
 	
 	
 		
 	
 	
 	private void setupListSelectActions()
 	{
 		tab.addSearchPanelListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 				populateFields(tab.getSelectedItem());
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void setupFieldValidators()
 	{
 		tab.addTypeValidator(new CheckedFieldValidator() {
 			public boolean check(String text) {
 				return DatabaseConstraints.isValidName(text);
 			}
 		});
 		
 		
 		tab.addAmountValidator(new CheckedFieldValidator() {
 			public boolean check(String text) {
 				return Validator.isPositiveInt( text );
 			}
 		});
 	}
 	
 }
