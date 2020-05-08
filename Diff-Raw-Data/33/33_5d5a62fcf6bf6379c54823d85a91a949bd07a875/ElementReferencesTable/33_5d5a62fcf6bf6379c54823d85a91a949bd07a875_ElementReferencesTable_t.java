 package es.eucm.eadventure.editor.gui.elementpanels.general;
 
 import java.awt.Component;
 import java.util.List;
 
 import javax.swing.JLabel;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableCellRenderer;
 
 import es.eucm.eadventure.common.gui.TextConstants;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.controllers.scene.ElementContainer;
 import es.eucm.eadventure.editor.control.controllers.scene.ElementReferenceDataControl;
 import es.eucm.eadventure.editor.control.controllers.scene.ReferencesListDataControl;
 import es.eucm.eadventure.editor.gui.elementpanels.book.IconTextPanel;
 import es.eucm.eadventure.editor.gui.otherpanels.ElementReferenceSelectionListener;
 
 
 public class ElementReferencesTable extends JTable implements ElementReferenceSelectionListener{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private ReferencesListDataControl dataControl;
 	
 	public ElementReferencesTable (ReferencesListDataControl dControl){
 		super();
 		this.setModel( new ElementsTableModel() );
 		this.getColumnModel( ).setColumnSelectionAllowed( false );
 		this.setDragEnabled( false );
 		this.getColumnModel().getColumn(0).setMaxWidth( 55 );
 		this.getColumnModel().getColumn(0).setPreferredWidth(50);
 		this.getColumnModel().getColumn(1).setCellRenderer(
 				new ElementsReferencesTableCellRenderer());
 		this.getSelectionModel( ).setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
 		this.dataControl = dControl;
 		this.setSize(200, 150);
 	}
 	
 	
 	private class ElementsTableModel extends AbstractTableModel{
 
 		public int getColumnCount( ) {
 			return 2;
 		}
 
 		public int getRowCount( ) {
 			return dataControl.getAllReferencesDataControl().size();
 		}
 
 		public Object getValueAt( int rowIndex, int columnIndex ) {
 			if (columnIndex ==1 ){
 				List<ElementContainer> references = dataControl.getAllReferencesDataControl();
 				return references.get( rowIndex );
 			} else {
 				return Integer.toString( rowIndex );
 			}
 			
 		}
 		
 		@Override
 		public String getColumnName(int columnIndex) {
 			if (columnIndex==1)
 				return TextConstants.getText( "ElementList.Title" );
 			else
 				return TextConstants.getText( "ElementList.Layer" );
 		}
 	}
 	
 	private class ElementsReferencesTableCellRenderer extends DefaultTableCellRenderer {
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
 		 *      java.lang.Object, boolean, boolean, int, int)
 		 */
 		@Override
 		public Component getTableCellRendererComponent(JTable table,
 				Object value, boolean isSelected, boolean hasFocus, int row,
 				int column) {
 			
 			if (value instanceof ElementContainer){
 				
 				String elementName;
 				if (((ElementContainer)value).isPlayer()){
 					elementName = "player";
 					return new IconTextPanel("img/icons/npc.png", elementName,isSelected);
 				}
 				else {
 					elementName = ((ElementContainer)value).getErdc().getElementId();
 					if (((ElementContainer)value).getErdc().getType( ) == Controller.ITEM_REFERENCE){
 						return new IconTextPanel("img/icons/item.png", elementName, isSelected);				
 					} else if (((ElementContainer)value).getErdc().getType( )  == Controller.ATREZZO_REFERENCE){
 						return new IconTextPanel("img/icons/Atrezzo-1.png", elementName,isSelected);
 					} else if (((ElementContainer)value).getErdc().getType( )  == Controller.NPC_REFERENCE){
 						return new IconTextPanel("img/icons/npc.png", elementName,isSelected);
 					} else 
 					return null;
 				}
 			} else {
 				return new JLabel(value.toString( ));
 			}
 		}
 
 	}
 

 	public void elementReferenceSelected(int layer) {
 		if (layer != -1)
 			this.changeSelection(layer, 1, false, false);
 		else
 			this.clearSelection();
 	}
 
 }
