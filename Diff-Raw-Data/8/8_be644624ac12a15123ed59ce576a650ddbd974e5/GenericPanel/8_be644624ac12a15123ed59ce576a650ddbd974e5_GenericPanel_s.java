 
 
 
 package overwatch.gui;
 
 import overwatch.core.Gui;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import net.miginfocom.swing.MigLayout;
 
 
 
 
 
 /**
  * Generic panel with a left-side search panel and right-side main panel.
  * All tabs based on basic editing of attributes should extend this one. 
  * Templated on the SearchPanel type.
  * 
  * @author  Lee Coakley
 * @version 2
  */
 
 
 
 
 
 public class GenericPanel<T> extends JPanel
 {
 	public static final int defaultFieldWidth = 24;
 	
 	protected final SearchPanel<T> searchPanel;
 	protected final JPanel         mainPanel;
 	protected final JLabel         mainLabel;
 	protected final JPanel         subPanel;
 	
 	
 	
 	
 	
 	/**
 	 * Create a generic panel with a left-side search bar and right-side main area.
 	 * @param searchLabelText
 	 * @param mainLabelText
 	 */
 	public GenericPanel( String searchLabelText )
 	{
		super( new MigLayout( "", "[shrink 100][grow,fill]", "[grow,fill][][grow,fill]" ) );
 		
 		this.searchPanel = new SearchPanel<T>( searchLabelText );
 		this.mainPanel   = new JPanel( new MigLayout("","[][256]") );
 		this.mainLabel   = new JLabel( "" );
 		this.subPanel    = new JPanel( new MigLayout() );
 		
 		mainPanel.add( mainLabel, "cell 1 0, wrap" );
 		
		add( searchPanel, "wmin 96px, wmax 224px, spany 2" );
 		add( mainPanel,   "alignx left, wrap"     );
 		add( subPanel,    "alignx right, skip 1"  );
 		
 		setupAutoLabel();
 		autoUpdateMainLabel();
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Add a component to the right main panel. 
 	 * @param comp
 	 * @param layoutParams MigLayout parameters
 	 * @return comp
 	 */
 	public <C extends Component> C addToMain( C comp, String layoutParams ) {
 		mainPanel.add( comp, layoutParams );
 		return comp;
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Add a component to the sub-panel, under the main panel.
 	 * @param comp
 	 * @param layoutParams MigLayout parameters
 	 * @return comp
 	 */
 	public <C extends Component> C addToSub( C comp, String layoutParams ) {
 		subPanel.add( comp, layoutParams );
 		return comp;
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Add a CheckedField and JLabel, then wrap to the next line.
 	 * @param label
 	 * @return LabelFieldPair
 	 */
 	public LabelFieldPair addLabelledField( String label )
 	{
 		JLabel       l = new JLabel( label );
 		CheckedField f = new CheckedField( defaultFieldWidth );
 		
 		addToMain( l, "alignx right" );
 		addToMain( f, "growx, wrap"  );
 		
 		return new LabelFieldPair( l,f );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Add a CheckedField, JLabel and ellipsis JButton, then wrap to the next line.
 	 * @param label
 	 * @return LabelFieldEllipsisTriplet
 	 */
 	public LabelFieldEllipsisTriplet addLabelledFieldWithEllipsis( String label )
 	{
 		JLabel       l = new JLabel ( label );
 		CheckedField f = new CheckedField( defaultFieldWidth );
 		JButton      b = new JButton( "..." );
 		
 		addToMain( l, "alignx right"     );
 		addToMain( f, "growx, split 2"   );
 		addToMain( b, "wmax 32px, wrap"	 );
 		
 		return new LabelFieldEllipsisTriplet( l,f,b );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Adds New/Save/Delete to the subPanel under the main panel.
 	 * Generally you should use GenericPanelButtoned instead if you want these.
 	 * @return StandardButtonTriplet
 	 */
 	public StandardButtonTriplet addNewSaveDeleteButtons()
 	{
 		JButton a = new JButton( "New"    );
 		JButton s = new JButton( "Save"   );
 		JButton d = new JButton( "Delete" );
 		
 		addToSub( a, "alignx right, split 3" );
 		addToSub( s, "" );
 		addToSub( d, "gap left 32px" );
 		
 		return new StandardButtonTriplet( a,s,d );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Enable or disable all the fields and buttons in the main and sub panels.
 	 * To control input normally, you should use setEditable on the components instead of setEnabled.
 	 * @param enable
 	 */
 	public void setEnableFieldsAndButtons( boolean enable )
 	{
 		for (Component comp: mainPanel.getComponents()) {
 			if (comp instanceof JTextField
 			||  comp instanceof JButton) {
 				comp.setEnabled( enable );
 			}
 		}
 		
 		for (Component comp: subPanel.getComponents()) {
 			if (comp instanceof JTextField
 			||  comp instanceof JButton) {
 				comp.setEnabled( enable );
 			}
 		}
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Clear all the fields in the main panel.
 	 */
 	public void clearFields()
 	{
 		for (Component comp: mainPanel.getComponents()) {
 			if (comp instanceof JTextField) {
 				((JTextField) comp).setText( "" );
 			}
 		}
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Check if all the fields are valid.
 	 * Fields with no validators set up count as valid.
 	 */
 	public boolean areAllFieldsValid()
 	{
 		for (Component comp: mainPanel.getComponents()) {
 			if (comp instanceof CheckedField)
 			if ( ! ((CheckedField) comp).isInputValid()) {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Set the items which will be displayed in the left-hand list.
 	 * @param pairs
 	 * @see Database.queryKeyNamePairs
 	 */
 	public void setSearchableItems( ArrayList<NameRefPair<T>> pairs ) {
 		searchPanel.setSearchableItems( pairs );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Add a listener which fires when the list is selected or deselected.
 	 * @param lis
 	 */
 	public void addSearchPanelListSelectionListener( ListSelectionListener lis ) {
 		searchPanel.addListSelectionListener( lis );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get the selected item in the search list.  If nothing is selected, returns null.
 	 * @return T
 	 */
 	public T getSelectedItem() {
 		return searchPanel.getSelectedItem();
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Set the selected item in the search list.
 	 * Generates events.
 	 * @return T
 	 */
 	public void setSelectedItem( T item ) {
 		searchPanel.setSelectedItem( item );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Check if something is selected.
 	 * @return boolean
 	 */
 	public boolean hasSelectedItem() {
 		return searchPanel.hasSelectedItem();
 	}
 	
 	
 	
 	
 	
 		
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Internals
 	/////////////////////////////////////////////////////////////////////////
 	
 	
 	private void setupAutoLabel()
 	{
 		addSearchPanelListSelectionListener( new ListSelectionListener() {
 			public void valueChanged( ListSelectionEvent e ) {
 				autoUpdateMainLabel();		
 			}
 		});		
 	}
 	
 	
 	
 	
 	
 	protected void autoUpdateMainLabel()
 	{
 		String sel     = searchPanel.getSelectedItemName();
 		String pretext = "Details for: ";
 		String text    = "[None selected]";
 		
 		if (null != sel)
 			text = sel;
 		
 		mainLabel.setText( pretext + text );
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////
 	// Test
 	/////////////////////////////////////////////////////////////////////////
 	
 	public static void main( String[] args )
 	{
 		Gui.setNativeStyle();
 		
 		final GenericPanel<Integer> gp = new GenericPanel<Integer>( "SearchLabel" );
 		gp.addLabelledField( "Name:" );
 		gp.addLabelledField( "Age:"  );
 
 		final LabelFieldEllipsisTriplet lft = gp.addLabelledFieldWithEllipsis( "Rank:" );
 		
 		final StandardButtonTriplet sbt = gp.addNewSaveDeleteButtons();
 		
 		
 		lft.field.addValidator( new CheckedFieldValidator() {
 			public boolean check( String text ) {
 				return text.length() == 4;
 			}
 		});
 		
 		
 		
 		final JFrame jf = new JFrame();
 		jf.add( gp );
 		jf.setSize( new Dimension(640,400) );
 		jf.setVisible( true );
 		
 		
 		
 		lft.button.addActionListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ) {
 				SearchPicker<Integer> pick = new SearchPicker<Integer>( jf, "Title", "Label", new NameRefPairList<Integer>() );
 				
 				pick.addPickListener( new PickListener<Integer>() {
 					public void onPick( Integer picked ) {
 						System.out.println( "Picked: " + picked );
 						
 						if (picked != null) {
 							lft.field.setText( picked.toString() );
 						}
 					}
 				});
 			}
 		});
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
