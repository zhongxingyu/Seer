 package org.pathwayeditor.visualeditor.dataviewer;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableColumnModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 
 public class DataViewPanel extends JPanel {
 	private static final long serialVersionUID = 3602462091855803178L;
 
 	private BorderLayout borderLayout1 = new BorderLayout();
 	private DataViewTableModel tableModel;
 	private JScrollPane dataViewScrollPane = new JScrollPane();
 	private TableColumnModel tableColumnModel;
 	private JTable dataViewTable;
 	private javax.swing.JPanel actionPanel = new JPanel();
 	private transient List<DataViewListener> dataViewListeners = new LinkedList<DataViewListener>();
 	private ListSelectionModel selectionModel;
 	private JButton prevButton;
 	private JButton nextButton;
 
 	public DataViewPanel(IRowDefn rowDefn) {
 		tableModel = new DataViewTableModel(rowDefn);
 		tableColumnModel = new DefaultTableColumnModel();
 		dataViewTable = new JTable(tableModel, tableColumnModel);
 		dataViewTable.setAutoscrolls(true);
 		selectionModel = dataViewTable.getSelectionModel();
 		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		setColumnDefs();
 		this.setLayout(borderLayout1);
 		dataViewScrollPane.getViewport().add(dataViewTable);
 		this.add(dataViewScrollPane, java.awt.BorderLayout.CENTER);
 		this.add(actionPanel, java.awt.BorderLayout.SOUTH);
 		setupActionPanel();
 		dataViewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 	}
 
 	private void setupActionPanel() {
 		final JCheckBox irrelevantCheckBox = addCheckBox("Irrelevant");
 		final JCheckBox centralCheckBox = addCheckBox("Central OK");
 		final JCheckBox sateliteCheckBox = addCheckBox("Satelite OK");
 		irrelevantCheckBox.addChangeListener(new ChangeListener(){
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				if(!irrelevantCheckBox.isSelected()){
 					centralCheckBox.setSelected(false);
 					sateliteCheckBox.setSelected(false);
 				}
 			}
 			
 		});
 		centralCheckBox.addChangeListener(new ChangeListener(){
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				if(centralCheckBox.isSelected()){
 					irrelevantCheckBox.setSelected(true);
 				}
 			}
 		});
 		sateliteCheckBox.addChangeListener(new ChangeListener(){
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				if(sateliteCheckBox.isSelected()){
 					irrelevantCheckBox.setSelected(true);
 				}
 			}
 		});
 		prevButton = new JButton("Previous");
 		this.actionPanel.add(prevButton);
 		prevButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int currSelectionIdx = getCurrentSelection()-1;
 				selectionModel.setSelectionInterval(currSelectionIdx, currSelectionIdx);
 			}
 		});
 		nextButton = new JButton("Next");
 		this.actionPanel.add(nextButton);
 		nextButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int currSelectionIdx = getCurrentSelection()+1;
 				selectionModel.setSelectionInterval(currSelectionIdx, currSelectionIdx);
 			}
 		});
 		this.selectionModel.addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				checkNavigatorButtonEnablement();
 			}
 		});
 	}
 
 	private void checkNavigatorButtonEnablement(){
 		int currSelectionIdx = getCurrentSelection();
 		prevButton.setEnabled(currSelectionIdx > 0);
 		nextButton.setEnabled(currSelectionIdx < tableModel.getRowCount()-1);
 	}
 	
 	private JCheckBox addCheckBox(String labelText) {
 		JLabel label = new JLabel(labelText);
 		JCheckBox checkBox = new JCheckBox();
 		checkBox.setSelected(false);
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new GridLayout(1, 2));
 		buttonPanel.add(label);
 		buttonPanel.add(checkBox);
 		this.actionPanel.add(buttonPanel);
 		return checkBox;
 	}
 
 	private void setColumnDefs() {
 		IRowDefn rowDefn = this.tableModel.getRowDefn();
 		for (int i = 0; i < rowDefn.getNumColumns(); i++) {
 			TableColumn colDefn = new TableColumn();
 			colDefn.setHeaderValue(i);
 			colDefn.setHeaderValue(rowDefn.getColumnHeader(i));
 			colDefn.setResizable(rowDefn.isColumnResizable(i));
 			colDefn.setPreferredWidth(rowDefn.getPreferredWidth(i));
 			if (rowDefn.getCustomRenderer(i) != null) {
 				colDefn.setCellRenderer(rowDefn.getCustomRenderer(i));
 			}
 			if (rowDefn.getCellEditor(i) != null) {
 				colDefn.setCellEditor(rowDefn.getCellEditor(i));
 			}
 			colDefn.setModelIndex(i);
 			tableColumnModel.addColumn(colDefn);
 		}
 	}
 
 	public void addDataViewListener(DataViewListener l) {
 		this.dataViewListeners.add(l);
 	}
 
 	public void removeDataViewListener(DataViewListener l) {
 		this.dataViewListeners.remove(l);
 	}
 
 	protected void fireRowInserted(DataViewEvent event) {
 		for (DataViewListener listener : dataViewListeners) {
 			listener.rowInserted(event);
 		}
 	}
 
 	protected void fireRowDeleted(DataViewEvent event) {
 		for (DataViewListener listener : dataViewListeners) {
 			listener.rowDeleted(event);
 		}
 	}
 
 	protected void fireViewSaved(DataViewEvent event) {
 		for (DataViewListener listener : dataViewListeners) {
 			listener.viewSaved(event);
 		}
 	}
 
 	protected void fireViewReset(DataViewEvent event) {
 		for (DataViewListener listener : dataViewListeners) {
 			listener.viewReset(event);
 		}
 	}
 
 	public DataViewTableModel getTableModel() {
 		return this.tableModel;
 	}
 
 	private int getCurrentSelection(){
 		return selectionModel.getAnchorSelectionIndex();
 	}
 	
 	public void resetSelection() {
 		selectionModel.setSelectionInterval(0, 0);
 	}
 }
