 package edu.nrao.dss.client;
 
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 
 public class RcvrScheduleGrid extends FlexTable {
 	
 	private String[] maintenanceDays;
 	private boolean showMaintenance = false;
 	private String styleBase = "gwt-RcvrSchdGrid-";
 
     private static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat("MM/dd/yyyy");
     private static final DateTimeFormat DATE_FORMAT_MAINT = DateTimeFormat.getFormat("yyyy-MM-dd"); 	
     
 	public RcvrScheduleGrid() {
 		initLayout();
 	}	
 	
 	private void initLayout() {
 
 	    addStyleName("cw-FlexTable");
 	    setCellSpacing(2);
 	    setCellPadding(1);	
 	    
 	}
 	
 	public void loadSchedule(int rows, int cols, String[] headers, String[][] schedule) {
 
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
 		if (showMaintenance) {
 		    loadAllDates(rows, cols, schedule);
 		} else {
 			loadOnlyRcvrDates(rows, cols, schedule);
 		}
 
 	}
 	
     private void loadOnlyRcvrDates(int rows, int cols, String[][] schedule) {
 		for (int row = 0; row < rows; row++) {
 			for (int col = 0; col < cols; col++) {
 				//GWT.log("["+Integer.toString(row) + "][" + Integer.toString(col)+"]: "+schedule[row][col], null);
 				String scheduleValue = schedule[row][col];
 				boolean on = (scheduleValue.compareTo("T") == 0) ? true : false;
 				String styleName = on ? "on" : "off"; 
     			if (col != 0) {
 	    			getCellFormatter().setStyleName(row + 1, col, styleBase + styleName);
 				} else {
 					getCellFormatter().setStyleName(row + 1, col, styleBase + "header");
 				}
 				String value = (col == 0) ? scheduleValue : "";
 			    setHTML(row + 1, col, value);
 			}
 		}
 	}
 
     private void loadAllDates(int rows, int cols, String[][] schedule) {
     	String start, end;
     	String [] daysBetween;
     	int gridRow = 1;
 		for (int row = 0; row < rows; row++) {
 			// insert the recevier changes
 			for (int col = 0; col < cols; col++) {
 				//GWT.log("["+Integer.toString(row) + "][" + Integer.toString(col)+"]: "+schedule[row][col], null);
 				String scheduleValue = schedule[row][col];
 				boolean on = (scheduleValue.compareTo("T") == 0) ? true : false;
 				String styleName = on ? "on" : "off"; 
     			if (col != 0) {
 	    			getCellFormatter().setStyleName(gridRow, col, styleBase + styleName);
 				} else {
 					getCellFormatter().setStyleName(gridRow, col, styleBase + "header");
 				}
 				String value = (col == 0) ? scheduleValue : "";
 			    setHTML(gridRow, col, value);
 			}
 			// we've inserted one row of rcvr changes
 			gridRow++;
 			// if this isn't the last row, how many maintenance days to insert?
 			if (row < rows - 1) {
 				start = schedule[row][0];
 				end   = schedule[row+1][0];
 				daysBetween = getMaintenanceDaysBetween(start, end);
 			} else {
 				daysBetween = null;
 			}
 			// insert the maintenance days
 			if (daysBetween != null) {
 			    for (int mRow = 0; mRow < daysBetween.length; mRow++) {
 			    	setHTML(gridRow, 0, daysBetween[mRow]);
 			    	gridRow++;
 			    }
 			}
 			
 		}
     }
     
     private String[] getMaintenanceDaysBetween(String startStr, String endStr) {
     	
     	Date start = DATE_FORMAT.parse(startStr);
     	Date end   = DATE_FORMAT.parse(endStr);
     	ArrayList<Date> mdays = new ArrayList<Date>();
     	
     	GWT.log("start: "+startStr, null);
     	for (int i = 0; i < maintenanceDays.length; i++) {
     		// is this day in our range?
     		GWT.log(maintenanceDays[i], null);
     		Date mday = DATE_FORMAT_MAINT.parse(maintenanceDays[i]);
     		if (mday.after(start) && mday.before(end)) {
     		    mdays.add(mday);	
     		}
     	}
     	String[] days = new String[mdays.size()];
     	for (int i = 0; i < mdays.size(); i++) {
     		days[i] = DATE_FORMAT.format(mdays.get(i));
     	}
     	return days;
     }
     
 	private void clearAll() {
		for (int i = 0; i < getRowCount(); i++) {
			removeRow(i);
		}
 	}
 	
 	private boolean isMaintenanceDay(String day) {
 	    return false; //TODO:	
 	}
 	
 	public void setMaintenanceDays(String[] maintenanceDays) {
 		this.maintenanceDays = maintenanceDays;
 	}	
 	
 	public void setShowMaintenance(boolean show) {
 		showMaintenance = show;
 	}
 }
