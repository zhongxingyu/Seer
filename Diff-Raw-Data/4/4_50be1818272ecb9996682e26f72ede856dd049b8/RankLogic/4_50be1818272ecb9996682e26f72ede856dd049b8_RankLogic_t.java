 
 
 
 package overwatch.controllers;
 
 import java.awt.event.*;
 import javax.swing.event.*;
 import overwatch.core.Gui;
 import overwatch.db.*;
 import overwatch.gui.CheckedFieldValidator;
 import overwatch.gui.tabs.RankTab;
 import overwatch.util.Validator;
 
 
 
 
 
 /**
  * RankTab logic
  * 
  * @author  John Murphy
  * @author  Lee Coakley
  * @version 7
  */
 
 
 
 
 
 public class RankLogic extends TabController<RankTab>
 {	
 	
 	public RankLogic( RankTab tab ) {
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
 		Integer rankNo = tab.getSelectedItem();
 		
 		if ( ! tab.areAllFieldsValid()) {
 			showFieldValidationError();
 			return;
 		}
 		
 		try {
 			String rankName  = tab.name.         field.getText();
 			String rankLevel = tab.securityLevel.field.getText();
 			
 			if ( ! Ranks.exists(rankNo)) {
 				showDeletedError( "rank" );
 				populateTabList(); // Reload
 				return;
 			}
 			
 			Ranks.save( rankNo, rankName, rankLevel );
 			
 			populateTabList();
 			tab.setSelectedItem(rankNo);
 		}
 		catch (DatabaseIntegrityException ex) {
 			Gui.showError(
 				"Rank Already Exists",
 				"A rank with that name already exists. Choose a different name."
 			);
 		}
 		
 		populateTabList();
 		tab.setSelectedItem(rankNo);
 	}
 	
 	
 	
 	
 	
 	private void doNew()
 	{
 		Integer rankNo = Ranks.create();
 				
 		populateTabList();
 		tab.setSelectedItem( rankNo );
 	}
 	
 	
 	
 	
 	
 	private void doDelete()
 	{
 		if ( ! confirmDelete( "rank" ))
 			return;
 		
 		Integer rankNo = tab.getSelectedItem();
 		Ranks.delete( rankNo );
 		populateTabList();
 	}
 	
 	
 	
 	
 	
 	private void populateTabList()
 	{
 		populateFields( null );
 		tab.setSearchableItems(
 			Database.queryKeyNamePairs( "Ranks", "rankNo", "name", Integer[].class )
 		);
 	}
 	
 	
 	
 	
 	
 	private void populateFields( Integer rankNo )
 	{
 		if (rankNo == null) {
 			tab.setEnableFieldsAndButtons( false );
 			tab.clearFields();
 			tab.setEnableNewButton( true );
 			return;
 		}
 		
 		
 		tab.setEnableFieldsAndButtons( ! Ranks.isHardcoded(rankNo) );
 		tab.setEnableNewButton( true ); // TODO change to use secLevel
 		
 		EnhancedResultSet ers = Database.query(
 		    "SELECT rankNo, name, privilegeLevel " +
 		    "FROM Ranks     " +
 		    "WHERE rankNo = " + rankNo
 		);
 		
 		if (ers.isEmpty()) {
 			showDeletedError( "rank" );
			populateTabList();
 			return;
 		}
 		
 		tab.number       .field.setText("" + ers.getElemAs( "rankNo",         Integer.class ));
 		tab.name         .field.setText(     ers.getElemAs( "name",           String .class ));
 		tab.securityLevel.field.setText("" + ers.getElemAs( "privilegeLevel", Integer.class ));
 	}
 	
 	
 	
 	
 	
 	protected void attachEvents() {
 		setupTabChangeActions();
 		setupButtonActions();
 		setupListSelectActions();
 		setupFieldValidators();
 	}
 	
 	
 	
 	
 	
 	private void setupTabChangeActions() {
 		Gui.getCurrentInstance().addTabSelectNotify(this);
 	}
 	
 	
 	
 	
 	
 	private void setupFieldValidators()
 	{
 		tab.addNameValidator(new CheckedFieldValidator() {
 			public boolean check(String text)
 			{
 				if ( ! DatabaseConstraints.isValidName( text ))
 					return false;
 				
 				boolean edited  = tab.name.field.isModifiedByUser();
 				boolean badEdit = (edited && Database.queryUnique( "Ranks", "name", "'"+text+"'" ));
 				return ! badEdit;
 			}
 		});
 		
 		
 		tab.addPrivilegesValidator(new CheckedFieldValidator() {
 			public boolean check(String text) {
 				return Validator.isInt( text );
 			}
 		} );
 	}
 	
 	
 	
 	
 	
 	private void setupListSelectActions()
 	{
 		tab.addSearchPanelListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 				populateFields(tab.getSelectedItem());
 			}
 		});
 		
 	}
 	
 	
 	
 	
 	
 	private void setupButtonActions()
 	{
 		
 		tab.addNewListener(new ActionListener() {			
 			public void actionPerformed(ActionEvent e) {
 				doNew();				
 			}
 		});	
 	
 		tab.addDeleteListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				doDelete();
 			}
 		});		
 	
 		tab.addSaveListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				doSave();
 			}
 		});
 	}
 
 	
 }
