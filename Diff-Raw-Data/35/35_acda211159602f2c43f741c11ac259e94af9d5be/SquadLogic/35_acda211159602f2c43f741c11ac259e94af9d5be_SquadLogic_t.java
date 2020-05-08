 
 
 
 package overwatch.controllers;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import overwatch.core.Gui;
 import overwatch.db.Database;
 import overwatch.db.DatabaseException;
 import overwatch.db.EnhancedPreparedStatement;
 import overwatch.db.EnhancedResultSet;
 import overwatch.db.Personnel;
 import overwatch.db.Squads;
 import overwatch.db.Supplies;
 import overwatch.db.Vehicles;
 import overwatch.gui.NameRefPairList;
 import overwatch.gui.PersonnelPicker;
 import overwatch.gui.PickListener;
 import overwatch.gui.SquadSupplyPicker;
 import overwatch.gui.SquadTroopPicker;
 import overwatch.gui.SquadVehiclePicker;
 import overwatch.gui.tabs.SquadTab;
 
 
 
 
 
 /**
  * Implements squad tab logic
  * 
  * @author  John Murphy
  * @author  Lee Coakley
  * @version 4
  */
 
 
 
 
 
 public class SquadLogic extends TabController<SquadTab>
 {
 
 	public SquadLogic( SquadTab tab ) {
 		super( tab );
 	}
 	
 	
 	public void respondToTabSelect(){
 		populateSquadsList();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////	
 	
 	private void doCreate()
 	{
 		Integer squadNo = Squads.create();
 		populateSquadsList();
 		populateFieldsAndPanels(squadNo);
 	}
 	
 	
 	
 	
 	
 	private void doSave( Integer squadNo )
 	{
 		String  squadName 	  = tab.name.     field.getText();
 		String  commanderName = tab.commander.field.getText();
 		Integer commanderNo	  = Personnel.getNumber( commanderName );
 		ArrayList<Integer> troops 	= tab.assignTroops.getItems();
 		ArrayList<Integer> vehicles = tab.assignVehicles.getItems();
 		ArrayList<Integer> supplies = tab.assignSupplies.getItems();
 		boolean didSave;
 		
 					
 		if ( ! Squads.exists(squadNo)) {
 			showDeletedError( "squad" );
 			populateSquadsList();
 			return;
 		}
 		
 		EnhancedPreparedStatement eps = new EnhancedPreparedStatement(
 				"UPDATE Squads "          				+
 				"SET name 		= <<squadName>>," 		+
 				"    commander 	= <<commanderNo>> "  	+
 				"WHERE squadNo 	= <<squadNo>>;"
 			);
 		
 		try {
 			eps.set( "squadName", squadName );
 			eps.set( "commanderNo", commanderNo );
 			eps.set( "squadNo", squadNo );
 			eps.update();
 			didSave = (0 != eps.update());
 		}
 		finally {
 			eps.close();
 		}
 		
 		
 		Squads.saveSquadDetails(squadNo, troops, vehicles, supplies, didSave);
 		
 		
 		populateSquadsList();
 		tab.setSelectedItem(squadNo);
 	}
 	
 	
 	
 	
 	
 	private void delete( Integer squadNo )
 	{
 		int mods = Squads.deleteSquad(squadNo);		
 		
 		if(mods <= 0) {
 			showDeletedError("squad");
 		}
 		
 		populateSquadsList();
 	}
 	
 	
 	
 	
 	
 	private void populateSquadsList(){
 		populateFieldsAndPanels(null);
 		
 		tab.setSearchableItems(
 		Database.queryKeyNamePairs("Squads", "squadNo", "name", Integer[].class)
 		);
 	}
 	
 	
 	
 	
 	
 	private void populateAssignPanels( int squadNo ) {
 		try {		
 			tab.assignTroops  .setListItems( Squads.getTroops  ( squadNo ));
 			tab.assignVehicles.setListItems( Squads.getVehicles( squadNo ));
 			tab.assignSupplies.setListItems( Squads.getSupplies( squadNo ));
			setAssignPanelButtonsToEnabled();
			}
 		catch (DatabaseException ex) {
 			showDeletedError("Squads");
 		}
 	}
 	
 	
 	
 	
 	
 	private void populateFieldsAndPanels(Integer squadNo)
 	{
 		if(squadNo == null) {
 			tab.setEnableFieldsAndButtons( false );
 			tab.clearFields();
			setAssignPanelButtonsToDisabled();			
 			return;
 		}
 		
 		
 		tab.setEnableFieldsAndButtons( true );
 		
 		EnhancedResultSet ers = Database.query(
 			"SELECT squadNo,  " +
 			"		name,     " +
 			"       commander " +
 		    "FROM Squads " +
 		    "WHERE squadNo  = " + squadNo  + ";"
 		);
 		
 		
 		if (ers.isEmpty()) {
 			showDeletedError( "squad" );
 			return;
 		}
 		
 		
 		Integer commander = ers.getElemAs( "commander", Integer.class );
 		
 		String commanderName = "";
 		if (commander != null) {
 			commanderName = Squads.getCommander(commander);
 		}
 		
 		tab.number   .field.setText( "" + ers.getElemAs( "squadNo",    Integer.class ));
 		tab.name     .field.setText(      ers.getElemAs( "name",       String .class ));
 		tab.commander.field.setText(      commanderName );
 		
 		//Populate the subpanels
 		populateAssignPanels(squadNo);
 	}
 	
 	
 	
 	
 	
 	protected void attachEvents(){
 		setUpButtonActions();
 		setupListSelectActions();
 		setupPickActions();
 		setupTroopAssignActions();
 		setupVehicleAssignActions();
 		setupSupplyAssignActions();
 		setupTabChangeActions();
 	}
 	
 	
 	
 	
 	
 	private void setUpButtonActions(){
 		
 		tab.addNewListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				doCreate();
 			}
 		});
 		
 		
 		tab.addSaveListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				doSave( tab.getSelectedItem() );
 			}
 		});
 		
 		
 		tab.addDeleteListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				delete( tab.getSelectedItem() );
 			}
 		});
 	}
 	
 	
 	
 	
	private void setAssignPanelButtonsToDisabled()
	{
		tab.assignTroops.setButtonsToDisable();
		tab.assignVehicles.setButtonsToDisable();
		tab.assignSupplies.setButtonsToDisable();
	}
	
	
	
	
	private void setAssignPanelButtonsToEnabled()
	{
		tab.assignTroops.setButtonsToEnabled();
		tab.assignVehicles.setButtonsToEnabled();
		tab.assignSupplies.setButtonsToEnabled();	
	}
	
	
	
	
 	
 	private void setupListSelectActions()
 	{
 		tab.addSearchPanelListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 				populateFieldsAndPanels(tab.getSelectedItem());
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void setupTabChangeActions() {
		setAssignPanelButtonsToDisabled();
 		Gui.getCurrentInstance().addTabSelectNotify(this);
 	}
 	
 	
 	
 	
 	
 	private void setupPickActions()
 	{
 		final PickListener<Integer> pickListener = new PickListener<Integer>() {
 			public void onPick( Integer picked ) {
 				if (picked != null)
 					tab.commander.field.setText( Personnel.getLoginName(picked) );
 			}
 		};
 		
 		
 		tab.commander.button.addActionListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				new PersonnelPicker( tab.commander.button, pickListener );
 			}
 		});
 		
 	}
 	
 	
 	
 	
 	
 	private void setupTroopAssignActions()
 	{
 		final PickListener<Integer> pickListener = new PickListener<Integer>() {
 			public void onPick( Integer picked ) {
 				if (picked != null)
 					tab.assignTroops.addItem(picked,Personnel.getLoginName(picked)) ;		
 			}
 		};
 		
 		
 		
 		tab.assignTroops.addAddButtonListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				NameRefPairList<Integer> nrp 	= Squads.getTroopsNotInSquads();
 				ArrayList<Integer> listOfTroops = tab.assignTroops.getItems();	
 				
 				SquadTroopPicker p = new SquadTroopPicker( tab.assignTroops.getAddButton(), pickListener, nrp );	
 				
 				p.removeItems(listOfTroops.toArray(new Integer[listOfTroops.size()])); 
 				p.setVisible(true);				
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void setupVehicleAssignActions()
 	{
 		final PickListener<Integer> vehiclePick = new PickListener<Integer>() {
 			public void onPick( Integer picked ) {
 				if (picked != null)
 					tab.assignVehicles.addItem( picked, Vehicles.getVehicleType(picked) );
 			}
 		};
 		
 		
 		tab.assignVehicles.addAddButtonListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				NameRefPairList<Integer> nrp 		= Vehicles.getAllVehiclesNotInSquads();
 				ArrayList<Integer> listOfVehicles 	= tab.assignVehicles.getItems();
 				
 				SquadVehiclePicker vp = new SquadVehiclePicker( tab.assignVehicles.getAddButton(), vehiclePick, nrp );		
 				
 				vp.removeItems(listOfVehicles.toArray(new Integer[listOfVehicles.size()])); 
 				
 				vp.setVisible(true);
 			}
 		});
 	}
 	
 	
 	
 	
 	private void setupSupplyAssignActions()
 	{
 		final PickListener<Integer> supplyPickListener = new PickListener<Integer>() {
 			public void onPick( Integer picked ) {
 				if (picked != null)
 					tab.assignSupplies.addItem( picked, Supplies.getSupplyName(picked) );
 			}
 		};
 		
 		
 		tab.assignSupplies.addAddButtonListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				NameRefPairList<Integer> nrp 		= Supplies.getAllSupplys();
 				ArrayList<Integer> listOfSupplies	= tab.assignSupplies.getItems();
 				
 				SquadSupplyPicker ss = new SquadSupplyPicker( tab.assignSupplies.getAddButton(), supplyPickListener, nrp );
 				
 				ss.removeItems(listOfSupplies.toArray(new Integer[listOfSupplies.size()]));
 				
 				ss.setVisible(true);				
 			}
 		});
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
