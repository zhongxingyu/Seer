 
 
 
 package overwatch.controllers;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import overwatch.core.Gui;
 import overwatch.db.Database;
 import overwatch.db.DatabaseException;
 import overwatch.db.DatabaseIntegrityException;
 import overwatch.db.EnhancedResultSet;
 import overwatch.db.Personnel;
 import overwatch.db.Squads;
 import overwatch.db.Supplies;
 import overwatch.db.Vehicles;
 import overwatch.gui.CommanderPicker;
 import overwatch.gui.NameRefPair;
 import overwatch.gui.NameRefPairList;
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
 		
 		Integer[] troops   = tab.assignTroops  .getItemsAsArray( Integer.class );
 		Integer[] vehicles = tab.assignVehicles.getItemsAsArray( Integer.class );
 		Integer[] supplies = tab.assignSupplies.getItemsAsArray( Integer.class );
 							
 		
 		if ( ! Squads.exists(squadNo)) {
 			showDeletedError( "squad" );
 			populateSquadsList();
 			return;
 		}
 		
 		try {
 			Squads.save( squadNo, squadName, commanderNo, troops, vehicles, supplies );
 		}
 		catch (DatabaseIntegrityException ex) {
 			Gui.showError( "Commander Already Assigned", "Commander " + commanderName + " has already been assigned to a squad." );
 		}
 		catch (DatabaseException ex) {
 			showDeletedError( "squad" );
 		}
 		
 		populateSquadsList();
		tab.setSelectedItem( squadNo );
 	}
 	
 	
 	
 	
 	
 	private void doDelete( Integer squadNo )
 	{
 		if ( ! confirmDelete( "squad" ))
 			return;
 		
 		Squads.delete( squadNo );		
 		populateSquadsList();
 	}
 	
 	
 	
 	
 	
 	private void populateSquadsList(){
 		populateFieldsAndPanels(null);
 		
 		tab.setSearchableItems(
 			Database.queryKeyNamePairs( "Squads", "squadNo", "name", Integer[].class )
 		);
 	}
 	
 	
 	
 	
 	
 	private void populateAssignPanels( int squadNo ) {
 		try {		
 			tab.assignTroops  .setListItems( Squads.getTroops  ( squadNo ));
 			tab.assignVehicles.setListItems( Squads.getVehicles( squadNo ));
 			tab.assignSupplies.setListItems( Squads.getSupplies( squadNo ));
 		}
 		catch (DatabaseException ex) {
 			showDeletedError( "squad" );
 		}
 	}
 	
 	
 	
 	
 	
 	private void populateFieldsAndPanels(Integer squadNo)
 	{
 		if(squadNo == null) {
 			tab.setEnableFieldsAndButtons( false );
 			tab.clearFields();
 			tab.assignTroops  .setEnabled( false );
 			tab.assignVehicles.setEnabled( false );
 			tab.assignSupplies.setEnabled( false );
 			tab.setEnableNewButton( true );
 			return;
 		}
 		
 		
 		tab.setEnableFieldsAndButtons( true );
 		tab.assignTroops  .setEnabled( true );
 		tab.assignVehicles.setEnabled( true );
 		tab.assignSupplies.setEnabled( true );
 		
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
 		
 		
 		Integer commanderNo = ers.getElemAs( "commander", Integer.class );
 		
 		String commanderName = "";
 		if (commanderNo != null) {
 			commanderName = Personnel.getLoginName( commanderNo );
 		}
 		
 		tab.number   .field.setText( "" + ers.getElemAs( "squadNo",    Integer.class ));
 		tab.name     .field.setText(      ers.getElemAs( "name",       String .class ));
 		tab.commander.field.setText(      commanderName );
 		
 		//Populate the subpanels
 		populateAssignPanels(squadNo);
 	}
 	
 	
 	
 	
 	
 	protected void attachEvents(){
 		setupButtonActions();
 		setupListSelectActions();
 		setupPickActions();
 		setupTroopAssignActions();
 		setupVehicleAssignActions();
 		setupSupplyAssignActions();
 		setupTabChangeActions();
 	}
 	
 	
 	
 	
 	
 	private void setupButtonActions(){
 		
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
 				doDelete( tab.getSelectedItem() );
 			}
 		});
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
 				ArrayList<NameRefPair<Integer>> nrp = Squads.getCommandersNotInsquads();
 				CommanderPicker pick = new CommanderPicker( tab.commander.button, pickListener, nrp );
 				pick.setVisible(true);
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
 			public void actionPerformed(ActionEvent e)
 			{
 				SquadTroopPicker p = new SquadTroopPicker(
 					tab.assignTroops.getAddButton(),
 					pickListener,
 					Squads.getTroopsNotInSquads()
 				);	
 				
 				p.removeItems( tab.assignTroops.getItemsAsArray(Integer.class) ); 
 				p.setVisible( true );				
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
 			public void actionPerformed(ActionEvent e)
 			{		
 				SquadVehiclePicker vp = new SquadVehiclePicker(
 					tab.assignVehicles.getAddButton(),
 					vehiclePick,
 					Vehicles.getAllVehiclesNotInSquads()
 				);	
 				vp.removeItems( tab.assignVehicles.getItemsAsArray(Integer.class) ); 
 				vp.setVisible( true );
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
 			public void actionPerformed(ActionEvent e)
 			{			
 				SquadSupplyPicker ss = new SquadSupplyPicker(
 					tab.assignSupplies.getAddButton(),
 					supplyPickListener,
 					Supplies.getAll()
 				);
 				ss.removeItems( tab.assignSupplies.getItemsAsArray(Integer.class) );
 				ss.setVisible( true );				
 			}
 		});
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
