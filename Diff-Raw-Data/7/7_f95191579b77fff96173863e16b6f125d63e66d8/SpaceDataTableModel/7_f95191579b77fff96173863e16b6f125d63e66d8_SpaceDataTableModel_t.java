 package at.ac.tuwien.complang.carfactory.ui.tableModels;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.table.AbstractTableModel;
 
 public class SpaceDataTableModel extends AbstractTableModel {
 	private static final String[] SPACE_CONTENT_COLUMNS = { "ID", "PartName",
 			"PID", "Note" };
 	private List<Object[]> data = new ArrayList<Object[]>();
 
 	public SpaceDataTableModel() { }
 	
 	public SpaceDataTableModel(Object[][] data) {
 		if(data == null) return;
 		for (Object[] dates : data) {
 			this.data.add(dates);
 		}
 	}
 
 	public int getColumnCount() {
 		return SPACE_CONTENT_COLUMNS.length;
 	}
 
 	public int getRowCount() {
 		if (data != null)
 			return data.size();
 		else
 			return 0;
 	}
 
 	public String getColumnName(int col) {
 		return SPACE_CONTENT_COLUMNS[col];
 	}
 
 	public synchronized Object getValueAt(int row, int column) {
 		if(data.size() > row && data.get(row).length > column) {
 			return data.get(row)[column];
 		} else {
 			return null;
 		}
 	}
 
 	public Class getColumnClass(int c) {
		Object object = getValueAt(0, c);
		if(object == null) {
			return Object.class;
		} else {
			return getValueAt(0, c).getClass();
		}
 	}
 
 	public synchronized void addRow(Object[] dates) {
 		data.add(dates);
 		int row = data.indexOf(dates);
 		for(int column = 0; column < dates.length; column++) {
 			fireTableCellUpdated(row, column);
 		}
 		fireTableRowsInserted(row, row);
 	}
 
 	public synchronized void deleteRow(Object[] objectData) {
 		for(Object[] object : data) {
 			if(object[0] == objectData[0]) { //compare the unique global space id at position 0 of the data array
 				int index = data.indexOf(object);
 				data.remove(object);
 				fireTableRowsDeleted(index, index);
 				return;
 			}
 		}
 		System.out.println("Could not remove part from space. ID: " + objectData[0]);
 	}
 	
 	public synchronized void updateRow(Object[] objectData) {
 		int index = -1;
 		for(Object[] object : data) {
 			if(object[0] == objectData[0]) {
 				index = data.indexOf(object);
 				break;
 			}
 		}
 		if(index != -1) {
 			data.set(index, objectData);
 			fireTableRowsUpdated(index, index);
 		}
 	}
 }
