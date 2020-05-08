 package edu.nrao.dss.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 
 public class WindowCalTable extends FlexTable {
 	private String styleBase = "gwt-RcvrSchdGrid-";
 
 	public WindowCalTable() {
 		initLayout();
 	}	
 	
 	private void initLayout() {
 
 	    addStyleName("cw-FlexTable");
 	    setCellSpacing(2);
 	    setCellPadding(1);	
 	    
 	}
 	
 	public void loadCalendar(int rows, int cols, String[] headers, String[][] schedule) {
 
 		// start fresh
 		clearAll();
 
 		// first the header
 		for (int col = 0; col < cols; col++) {
 			// TODO: how to set the width of these columns?
 			//getColumnFormatter().setWidth(col, "200px");
 			setHTML(0, col, headers[col]);
 			getCellFormatter().setHorizontalAlignment(0, col, HasHorizontalAlignment.ALIGN_CENTER);
 			getCellFormatter().setStyleName(0, col, styleBase + "header");
 			
 			getCellFormatter().setWidth(0, col, "200");
 			
 		}
 		
 		GWT.log("rows, cols: "+Integer.toString(rows) + " " + Integer.toString(cols), null);
 		
 		// now the data
         loadAllDates(rows, cols, schedule);
 	}
 	
     private void loadAllDates(int rows, int cols, String[][] schedule) {
     	String display;
     	
     	// schedule format:
     	// rows - windows
     	// columns - first = session name, all others are dates
     	// go through the windows first
 		for (int row = 0; row < rows; row++) {
 			// go through the session name, then each day
 			for (int col = 0; col < cols; col++) {
 				//GWT.log("["+Integer.toString(row) + "][" + Integer.toString(col)+"]: "+schedule[row][col], null);
 				// has the format, either "F", or "T;informative_text", where F or T is the boolean for whether the 
 				// day falls in this particular window
 				String scheduleValue = schedule[row][col];
 				String[] parts = scheduleValue.split(";");
 				
 				// extract the flag ("F" or "T") from the entry to see if this date is part of the window 
 				String flag = parts[0];
 			    if (parts.length > 1) {
 			    	display = parts[1];
 			    } else {
 					display = ""; 
 			    }
 			    // convert flag to a boolean
 				boolean on = (flag.compareTo("T") == 0) ? true : false;
 				
 				// set the style to show whether this date is in the current window
 				String styleName = on ? "on" : "off"; 
     			if (col != 0) {
 	    			getCellFormatter().setStyleName(row + 1, col, styleBase + styleName);
 				} else {
 					// first column is the session name
 					getCellFormatter().setStyleName(row + 1, col, styleBase + "header");
 				}
     			
     			// make sure just the session name goes in the first column
 				String value = (col == 0) ? scheduleValue : display;
 			    setHTML(row + 1, col, value);
 			}
 		}
 	}
     
 	private void clearAll() {
		// WTF: I can't believe that this is the only thing that really works - 
		// what a sucky interface!
		int rowCount = getRowCount();
		for (int i = 0; i < rowCount; i++) {
			removeRow(0);
		}		
 	}
 }
