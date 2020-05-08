 
 
 
 package overwatch.controllers;
 
 import overwatch.core.Gui;
 import overwatch.db.Database;
 import overwatch.db.DatabaseConstraints;
 import overwatch.db.EnhancedResultSet;
 import overwatch.db.Personnel;
 import overwatch.db.Ranks;
 import overwatch.db.Vehicles;
 import overwatch.gui.CheckedFieldValidator;
 import overwatch.gui.PickListener;
 import overwatch.gui.RankPicker;
 import overwatch.gui.tabs.PersonnelTab;
 import overwatch.util.Validator;
 import java.math.BigDecimal;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JPanel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 
 
 
 
 /**
  * Implements the program logic for the personnel tab.
  * Controls saving, loading, security checking etc.
  * 
  * @author  Lee Coakley
  * @version 5
  */
 
 
 
 
 
 public class PersonnelLogic extends TabController
 {
 	private final PersonnelTab tab;
 	
 	
 	
 	
 	
 	/**
 	 * Plug the GUI tab into the controller.
 	 * @param tab
 	 */
 	public PersonnelLogic( PersonnelTab tab )
 	{
 		this.tab = tab;
 		
 		attachEvents();
 	}
 	
 	
 	
 	
 	
 	public JPanel getTab() {
 		return tab;
 	}
 	
 	
 	
 	
 	
 	public void respondToTabSelect() {	
 		populateList();
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	
 	
 	private void doNew()
 	{
 		Integer personNo = Personnel.create();
 		
 		populateList();
 		tab.setSelectedItem( personNo );
 	}
 	
 	
 	
 	
 	
 	private void doSave()
 	{
 		// TODO Personnel save
 		System.out.println( "save" );
 	}
 	
 	
 	
 	
 	
 	private void doDelete()
 	{
 		Integer personNo = tab.getSelectedItem();
 		Personnel.delete( personNo );
 		populateList();
 	}
 	
 	
 	
 	
 	
 	private void respondToRankPicker( Integer rankNo ) {
 		if (rankNo != null)
 			tab.rank.field.setText( Ranks.getName(rankNo) );
 	}
 	
 	
 	
 	
 	
 	private void populateList()
 	{
 		populateFields( null );
 		tab.setSearchableItems(
 			Database.queryKeyNamePairs( "Personnel", "personNo", "loginName", Integer[].class )
 		);
 	}
 	
 	
 	
 	
 	
 	private void populateFields( Integer personNo )
 	{
 		if (personNo == null) {
 			tab.setEnableFieldsAndButtons( false );
 			tab.clearFields();
 			return;
 		}
 		
 		
 		tab.setEnableFieldsAndButtons( true );
 		
 		EnhancedResultSet ers = Database.query(
 			"select r.name as rankName,   " +
 			"		p.name as personName, " +
 			"		personNo,    " +
 			"		age,         " +
 			"		sex,         " +
			"		salary,      " +
 			"       loginName    " +
 			"from Ranks     r,   " +
 			"	  Personnel p    " +
 			"where p.personNo =  " + personNo + " " +
 			"  and p.rankNo   = r.rankNo;"
 		);
 		
 		if (ers.isEmpty()) {
 			showDeletedError( "person" );
 			return;
 		}
		
 		tab.number   .field.setText( "" + ers.getElemAs( "personNo",   Integer   .class ) );
 		tab.name     .field.setText(      ers.getElemAs( "personName", String    .class ) );
 		tab.age      .field.setText( "" + ers.getElemAs( "age",        Integer   .class ) );
 		tab.sex      .field.setText(      ers.getElemAs( "sex",        String    .class ) );
 		tab.salary   .field.setText( "" + ers.getElemAs( "salary",     BigDecimal.class ) );
 		tab.rank     .field.setText(      ers.getElemAs( "rankName",   String    .class ) );
 		tab.loginName.field.setText(      ers.getElemAs( "loginName",  String    .class ) );
 	}
 	
 	
 	
 	
 	
 	private void attachEvents()
 	{
 		setupTabChangeActions();
 		setupSelectActions   ();
 		setupButtonActions   ();
 		setupPickActions     ();
 		setupFieldValidators ();
 	}
 	
 	
 	
 	
 	
 	private void setupTabChangeActions() {
 		Gui.getCurrentInstance().addTabSelectNotify( this );	
 	}
 	
 	
 	
 	
 	
 	private void setupPickActions()
 	{
 		final PickListener<Integer> rankPickListener = new PickListener<Integer>() {
 			public void onPick( Integer picked ) {
 				respondToRankPicker( picked );
 			}
 		};
 		
 		
 		tab.rank.button.addActionListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				new RankPicker( Gui.getCurrentInstance(), rankPickListener );
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void setupSelectActions()
 	{
 		tab.addSearchPanelListSelectionListener( new ListSelectionListener() {
 			public void valueChanged( ListSelectionEvent e ) {
 				populateFields( tab.getSelectedItem() );
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void setupButtonActions()
 	{
 		tab.addNewListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				doNew();
 			}
 		});
 		
 		
 		tab.addSaveListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				doSave();				
 			}
 		});
 		
 		
 		tab.addDeleteListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				doDelete();
 			}
 		});
 	}
 	
 	
 	
 	
 	
 	private void setupFieldValidators()
 	{
 		tab.addNameValidator( new CheckedFieldValidator() {
 			public boolean check( String text ) {
 				return DatabaseConstraints.isValidName( text );
 			}
 		});
 		
 		
 		tab.addAgeValidator( new CheckedFieldValidator() {
 			public boolean check( String text ) {
 				return Validator.isPositiveInt( text );
 			}
 		});
 		
 		
 		tab.addSexValidator( new CheckedFieldValidator() {
 			public boolean check( String text ) {
 				return DatabaseConstraints.isValidSex( text );
 			}
 		});
 		
 		
 		tab.addSalaryValidator( new CheckedFieldValidator() {
 			public boolean check( String text ) {
 				return DatabaseConstraints.isValidSalary( text );
 			}
 		});
 		
 		
 		tab.addRankValidator( new CheckedFieldValidator() {
 			public boolean check( String text ) {
 				return DatabaseConstraints.rankExists( text );
 			}
 		});
 	}
 	
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
