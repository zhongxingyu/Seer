 package com.yogocodes.httpmonitor.gui.form;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.table.AbstractTableModel;
 
 public class MonitorResultTableModel extends AbstractTableModel {
 
 	private final List<MonitorResult> results; 
 	private String [] header = {"url", "delay"};
 	public MonitorResultTableModel() {
 		results = new ArrayList<MonitorResult>();
 	}
 	
 	public void addResult(MonitorResult result) {
 		results.add(result); 
 	}
 	
 	@Override
 	public String getColumnName(int column) {
 		return header[column];
 	}
 	
 	@Override
 	public int getRowCount() {
 		// TODO Auto-generated method stub
 		return results.size();
 	}
 
 	@Override
 	public int getColumnCount() {
 		// TODO Auto-generated method stub
 		return header.length;
 	}
 
 	@Override
 	public Object getValueAt(int rowIndex, int columnIndex) {
 
 		MonitorResult result = results.get(rowIndex); 
 		
 		switch(columnIndex) {
 		case 0:
 			return result.getUrl();
 		
 		case 1:
 			return result.getTime();
 		}
 		
 		return -1;
 	}
 
 }
