 /*
  * Last modification information:
  * $Revision: 1.6 $
  * $Date: 2007-10-01 16:21:36 $
  * $Author: imoncada $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.data.state;
 
 import java.awt.Color;
 import java.awt.Font;
 
 import javax.swing.DefaultCellEditor;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JTextField;
 import javax.swing.table.TableColumn;
 
 import org.concord.data.ui.DataColumnDescription;
 import org.concord.data.ui.DataTableModel;
 import org.concord.data.ui.DataTablePanel;
 import org.concord.framework.data.stream.DataChannelDescription;
 import org.concord.framework.data.stream.DataStore;
 import org.concord.framework.otrunk.OTControllerService;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.view.AbstractOTJComponentView;
 
 
 /**
  * OTDataTableView
  * Class name and description
  *
  * Date created: Apr 4, 2007
  *
  * @author imoncada<p>
  *
  */
 public class OTDataTableView extends AbstractOTJComponentView
 {
 	protected OTDataTable otTable;
     protected OTControllerService controllerService;
 	
 	/**
 	 * @see org.concord.framework.otrunk.view.OTJComponentView#getComponent(org.concord.framework.otrunk.OTObject)
 	 */
 	public JComponent getComponent(OTObject otObject)
 	{
 		otTable = (OTDataTable)otObject;
     	controllerService = createControllerService(otObject);
 		
 		DataTablePanel table = new DataTablePanel(otTable.getVisibleRows());
 		
 		DataStore dataStore;
 		OTDataStore otDataStore;
 		otDataStore = otTable.getDataStore();
 		dataStore = (DataStore) controllerService.getRealObject(otDataStore);
 		table.getTableModel().addDataStore(dataStore);
 		
 		if (otTable.isResourceSet("showLastRowForAddingNew")){
 			table.getTableModel().setShowLastRowForAddingNew(otTable.getShowLastRowForAddingNew());
 		}
 		
 		updateOTColumns(table.getTableModel(), dataStore, otTable.getColumns());
 		
 		table.useDefaultHeaderRenderer();
 		
 		// Set cell editors so that they only require one click to start editing
 		for (int i = 0; i < table.getTable().getColumnCount(); i++) {
 			JTextField text = new JTextField();
			text.setFont(text.getFont().deriveFont(12f));
 			table.getTable().getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(text));
 			((DefaultCellEditor)table.getTable().getColumnModel().getColumn(i).getCellEditor()).setClickCountToStart(1);
 		}
 		
 		for (int i = 0; i < dataStore.getTotalNumChannels(); i++) {
 			// if the column has a specific set of possible values, render those with a combobox
 			DataChannelDescription dcc = dataStore.getDataChannelDescription(i);
 			if (dcc.getPossibleValues().size() > 0) {
 				TableColumn tCol = table.getTable().getColumnModel().getColumn(i);
 				JComboBox cb = new JComboBox();
 				for (Object o : dcc.getPossibleValues()) {
 					cb.addItem(o);
 				}
 				// if we're loading an OTObjectList, use the CustomDetail*Renderer classes to make them readable
 //				try {
 //					Method m = OTObject.class.getMethod("getName",(Class[]) null);
 //					cb.setRenderer(new CustomDetailListCellRenderer(m));
 //					tCol.setCellRenderer(new CustomDetailTableCellRenderer(m));
 //				} catch (Exception e) {
 //					
 //				}
 				tCol.setCellEditor(new DefaultCellEditor(cb));
 			}
 		}
 		
 		return table;
 	}
 
 	/**
 	 * @param dataStore
 	 * @param columns
 	 */
 	protected void updateOTColumns(DataTableModel tableModel, DataStore dataStore, OTObjectList columns)
 	{
 		for (int i = 0; i < columns.size(); i++){
 			OTDataColumnDescription otColDesc = (OTDataColumnDescription)columns.get(i); 
 			if (otColDesc == null) continue;
 			DataColumnDescription colDesc = tableModel.getDataColumn(dataStore, i);
 			
 			String label = otColDesc.getLabel();
 			if (label != null){
 				colDesc.setLabel(label);
 			}
 			if (otColDesc.isResourceSet("color")){
 				System.out.println("set color to "+otColDesc.getColor());
 				colDesc.setColor(new Color(otColDesc.getColor()));
 			}
 		}
 		tableModel.fireTableStructureChanged();
 	}
 
 	/**
 	 * @see org.concord.framework.otrunk.view.OTJComponentView#viewClosed()
 	 */
 	public void viewClosed()
 	{
 		controllerService.dispose();
 	}
 
 }
