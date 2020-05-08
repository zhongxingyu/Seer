 package es.eucm.eadventure.editor.gui.treepanel.nodes.scene;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.controllers.DataControl;
 import es.eucm.eadventure.editor.control.controllers.scene.ElementReferenceDataControl;
 import es.eucm.eadventure.editor.control.controllers.scene.ReferencesListDataControl;
 import es.eucm.eadventure.editor.gui.elementpanels.scene.ReferencesListPanel;
 import es.eucm.eadventure.editor.gui.treepanel.nodes.TreeNode;
 
 public class ReferencesListTreeNode extends TreeNode implements ReferenceListener{
 
 	/**
 	 * Contained micro-controller.
 	 */
 	private ReferencesListDataControl referencesDataControl;
 	
 	/**
 	 * The icon for this node class.
 	 */
 	private static Icon icon;
 	
 	private TreeNode lastNode;
 
 	/**
 	 * Loads the icon of the node class.
 	 */
 	public static void loadIcon( ) {
 		icon = new ImageIcon( "img/icons/references.png" );
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param parent
 	 *            Parent node
 	 * @param dataControl
 	 *            List of item references
 	 */
 	public ReferencesListTreeNode( TreeNode parent, ReferencesListDataControl referencesDataControl) {
 		super( parent );
 		lastNode = null;
 		this.referencesDataControl = referencesDataControl;
 		this.referencesDataControl.setAddNewReferenceListener(this);
 		for( ElementReferenceDataControl itemReferenceDataControl : referencesDataControl.getItemReferences( ) )
 			children.add( new ItemReferenceTreeNode( this, itemReferenceDataControl ) );
 		for( ElementReferenceDataControl atrezzoReferenceDataControl : referencesDataControl.getAtrezzoReferences() )
 			children.add( new AtrezzoReferenceTreeNode( this, atrezzoReferenceDataControl ) );
 		for( ElementReferenceDataControl npcReferenceDataControl : referencesDataControl.getNPCReferences( ) )
 			children.add( new NPCReferenceTreeNode( this, npcReferenceDataControl ) );
 
 	}
 
 	public void addChild( int type ) {
 		// If the element can be added, and the addition was successful
 		if( getDataControl( ).canAddElement( type ) && getDataControl( ).addElement( type ) ) {
 			if (lastNode != null)
 				this.setSelectedChild(lastNode);
 				
 		}
 	}
 	
 	@Override
 	public TreeNode checkForNewChild( int type ) {
 		TreeNode addedTreeNode = null;
 
 		// If an item reference was added
 		if( type == Controller.ITEM_REFERENCE ) {
 			// Add the last item reference of the list
 			addedTreeNode = new ItemReferenceTreeNode( this, referencesDataControl.getLastElementContainer().getErdc());
 			children.add( addedTreeNode );
 
 			// Spread the owner panel to the children
 			spreadOwnerPanel( );
 		}
 
 		if( type == Controller.ATREZZO_REFERENCE ) {
 			// Add the last item reference of the list
 			addedTreeNode = new AtrezzoReferenceTreeNode( this, referencesDataControl.getLastElementContainer().getErdc());
 			children.add( addedTreeNode );
 
 			// Spread the owner panel to the children
 			spreadOwnerPanel( );
 		}
 
 		if( type == Controller.NPC_REFERENCE ) {
 			// Add the last item reference of the list
 			addedTreeNode = new NPCReferenceTreeNode( this, referencesDataControl.getLastElementContainer().getErdc() );
 			children.add( addedTreeNode );
 
 			// Spread the owner panel to the children
 			spreadOwnerPanel( );
 		}
 
 		// Return the node created
 		return addedTreeNode;
 	}
 
 	@Override
 	public void checkForDeletedReferences( ) {
 		// If some item reference is missing, delete it
 		// TODO rewrite to take into account Atrezzo and NPC
 		
 		int i = 0;
 		while( i < children.size( ) ) {
 			if( !referencesDataControl.containsDataControl((ElementReferenceDataControl) children.get( i ).getDataControl( )) )
 				children.remove( i );
 			else
 				i++;
 		}
 	}
 
 	@Override
 	protected int getNodeType( ) {
 		return Controller.ITEM_REFERENCES_LIST;
 	}
 
 	@Override
 	public DataControl getDataControl( ) {
 		return referencesDataControl;
 	}
 
 	@Override
 	public Icon getIcon( ) {
 		return icon;
 	}
 
 	@Override
 	public String getToolTipText( ) {
 		return null;
 	}
 
 	@Override
 	public JComponent getEditPanel( ) {
 		return new ReferencesListPanel( referencesDataControl );
 	}
 
	@Override
 	public void addNewNodeElement(int type) {
 		lastNode = addChildOnlyInTree(type);
 	}
 
	@Override
 	public void deleteNodeElement() {
 		deleteChildOnlyInTree();
 		
 	}
 }
