 
 
 
 package overwatch.controllers;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import javax.swing.JPanel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import overwatch.core.Gui;
 import overwatch.db.Database;
 import overwatch.db.DatabaseException;
 import overwatch.db.EnhancedResultSet;
 import overwatch.db.Personnel;
 import overwatch.db.Squads;
 import overwatch.gui.NameRefPairList;
 import overwatch.gui.PersonnelPicker;
 import overwatch.gui.PickListener;
 import overwatch.gui.SquadTroopPicker;
 import overwatch.gui.SquadVehiclePicker;
 import overwatch.gui.tabs.SquadTab;
 
 
 
 
 
 /**
  * Implements squad tab logic
  * 
  * @author  John Murphy
  * @author  Lee Coakley
  * @version 3
  */
 
 
 
 
 
 public class SquadLogic extends TabController
 {
 	private final SquadTab tab;
 	
 	
 	
 	
 	
 	public SquadLogic(SquadTab tab)
 	{
 		this.tab = tab;
 		attachEvents();
 	}
 	
 	
 	
 	
 	
 	public void respondToTabSelect(){
 		populateSquadsList();
 	}
 	
 	
 	
 	
 	
 	public JPanel getTab(){
 		return tab;
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
 		
 					
 		if ( ! Squads.exists(squadNo)) {
 			showDeletedError( "squad" );
 			populateSquadsList();
 			return;
 		}
 		
 		
 		int modRows = Database.update(
 			"UPDATE Squads "          +
 			"SET name           = '"  + squadName  + "'," +
 			"    commander = " 		  + commanderNo + " "  +
 			"WHERE squadNo = " 		  + squadNo + " ;"
 		);
 		
 		if (modRows <= 0) {
 			showDeletedError( "squad" );
 			populateSquadsList();
 			return;
 		}
 		
 		
 		populateSquadsList();
 		tab.setSelectedItem(squadNo);
 	}
 	
 	
 	
 	
 	
 	private void delete( Integer squadNo )
 	{
 		int mods = Database.update(
 			"DELETE          " +
 			"FROM Squads     " +
 			"WHERE squadNo = " + squadNo + ";"
 		);
 		
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
 			commanderName = Database.querySingle( String.class,
 				"select loginName " +
 				"from Personnel   " +
 				"where personNo = " + commander + ";"
 			);
 		}
 		
 		tab.number   .field.setText( "" + ers.getElemAs( "squadNo",    Integer.class ));
 		tab.name     .field.setText(      ers.getElemAs( "name",       String .class ));
 		tab.commander.field.setText(      commanderName );
 		
 		//Populate the subpanels
 		populateAssignPanels(squadNo);
 	}
 	
 	
 	
 	
 	
 	private void attachEvents(){
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
 				new SquadTroopPicker( tab.assignTroops.getAddButton(), pickListener, new NameRefPairList<Integer>() );
 				// TODO make pickable list
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void setupVehicleAssignActions()
 	{
 		final PickListener<Integer> vehiclePick = new PickListener<Integer>() {
 			public void onPick( Integer picked ) {
 				if (picked != null)
 					tab.assignVehicles.addItem( picked, Squads.getAllVehiclesNotInSquads() );
 					// TODO this code doesn't make ANY sense.
 			}
 		};
 		
 		
 		tab.assignVehicles.addAddButtonListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				new SquadVehiclePicker( tab.assignVehicles.getAddButton(), vehiclePick, new NameRefPairList<Integer>() );
 				// TODO make pickable list
 			}
 		});
 	}
 	
 	
 	
 	
 	private void setupSupplyAssignActions()
 	{
 		final PickListener<Integer> supplyPickListener = new PickListener<Integer>() {
 			public void onPick( Integer picked ) {
 				if (picked != null)
 					tab.assignSupplies.addItem( picked, Squads.getAllVehiclesNotInSquads() );
 					// TODO this code doesn't make ANY sense.
 			}
 		};
 		
 		
 		tab.assignSupplies.addAddButtonListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
				new SquadVehiclePicker( tab.assignSupplies.getAddButton(), supplyPickListener, new NameRefPairList<Integer>() );
 				// TODO make pickable list
 			}
 		});
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
