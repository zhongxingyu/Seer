 package edu.nrao.dss.client;
 
 import java.util.Date;
 import java.util.HashMap;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.DateField;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 
 public class WindowCalendar extends ContentPanel {
 	
 	private WindowCalTable calendar = new WindowCalTable();
     private DateField start = new DateField();
     private SimpleComboBox<String> numDays = new SimpleComboBox<String>();
     private Button update = new Button();
     
     
 	public WindowCalendar() {
 		initLayout();
 		initListeners();
 	}
 	
 	private void initLayout() {	
 	
 		setLayout(new RowLayout(Orientation.VERTICAL));
 	
 		setScrollMode(Scroll.AUTO);
 		setBorders(false);
 		setHeaderVisible(true);
		setHeading("Window Calendar");
 		
 		FormPanel fp = new FormPanel();
 		fp.setHeaderVisible(false);
 
 		// when to start the schedule
 		start.setFieldLabel("Start Date");
 		fp.add(start);
 		
 		// for how long?
 		int maxDays = 30;
 		numDays.setFieldLabel("# Days");
 		for (int i = 2; i < maxDays; i++) {
 			numDays.add(Integer.toString(i));
 		}
 		fp.add(numDays);
 		
 		update.setText("Update");
 		fp.add(update);
 		
 		add(fp);
 		
 	    add(calendar);
     
 	}
 	
 	private void initListeners() {
 		
 	    update.addListener(Events.OnClick, new Listener<BaseEvent>() {
 	    	public void handleEvent(BaseEvent be) {
 	    		// update the calendar using the controls
 	    		getWindows();
 	    		
 	    	}
 	    });
 	   
 	}	
 	
 	public void setupCalendar() {
 		
 		// set default values for controls
 		start.setValue(new Date()); // today
 		numDays.setSimpleValue("1");
 		
 		// update the calendar
 		getWindows();
 	}
 	
 	public void getWindows() {
 	    // update the calendar using the controls
 		// /windows?filterStartDate=2010-02-01&filterDuration=30&sortField=null&sortDir=NONE&offset=0&limit=50
 		HashMap<String, Object> keys = new HashMap<String, Object>();
 		//keys.put("startdate", DATE_FORMAT.format(day.getValue()));
 		String startStr = DateTimeFormat.getFormat("yyyy-MM-dd").format(start.getValue()); //+ " 00:00:00";
 		keys.put("filterStartDate", startStr);
 		keys.put("filterDuration", numDays.getSimpleValue());
 		JSONRequest.get("/windows", keys  
 			      , new JSONCallbackAdapter() {
 			public void onSuccess(JSONObject json) {
 				GWT.log(json.toString(), null);
 				jsonToCalendar(json, start.getValue(), Integer.parseInt(numDays.getSimpleValue()));
 			}
 		});		
 	}
 	
 	private void jsonToCalendar(JSONObject json, Date start, int numDays) {
 		int i;
 		String text, dstartStr, cstartStr;
 		String dstate = "";
 		String cstate = "";
 		Date dstart, cstart;
 		String[] headers = new String[numDays + 1];
 		String[] dateStrs = new String[numDays];
 		Date[] dates = new Date[numDays];
 
 		// construct the column headers for the calendar
 		headers[0] = "Session";
 		String day;
 		for (i = 0; i < numDays; i++) {
 			long dateSecs = start.getTime() + (i * 1000 * 60 * 60 * 24);
 			Date dt = new Date(dateSecs);
 			String dayStr = DateTimeFormat.getFormat("yyyy-MM-dd").format(dt);
 			headers[i+1] = dayStr;
 			// for use later
 			dateStrs[i] = dayStr;
 			dates[i] = dt;
 		}
 
 		JSONArray windows = json.get("windows").isArray();
 		int numWindows = windows.size();		
 		String [][] cal = new String[numWindows][numDays + 1];
 		for (i = 0; i < numWindows; i++) {
 		    JSONObject window = windows.get(i).isObject();
 		    // the first column is the session name (handle)
 		    String handle = window.get("handle").isString().stringValue();
 		    cal[i][0] = handle;
 		    
 		    // when does the window start and stop?
 		    String wstartStr = window.get("start").isString().stringValue();
 		    String wstopStr  = window.get("end").isString().stringValue();
 			Date wstart = DateTimeFormat.getFormat("yyyy-MM-dd").parse(wstartStr);
 			Date wstop  = DateTimeFormat.getFormat("yyyy-MM-dd").parse(wstopStr);
 			
 			// when is the default period?
 			if (window.get("default_date").isNull() == null) {
 		        dstartStr = window.get("default_date").isString().stringValue();
 			    dstart = DateTimeFormat.getFormat("yyyy-MM-dd").parse(dstartStr);
 			    if (window.get("default_state").isNull() == null) {
 			    	dstate = window.get("default_state").isString().stringValue();
 			    }
 			} else {
 				dstartStr = "";
                 dstart = null;
                 dstate = "";
 			}
 				
 			// when is the choosen period?
 			if (window.get("choosen_date").isNull() == null) {
 		        cstartStr = window.get("choosen_date").isString().stringValue();
 			    cstart = DateTimeFormat.getFormat("yyyy-MM-dd").parse(cstartStr);
 			    if (window.get("choosen_state").isNull() == null) {
 			    	cstate = window.get("choosen_state").isString().stringValue();
 			    }
 			} else {
 				cstartStr = "";
 				cstart = null;
 				cstate = "";
 			}
 		    
 		    // the rest are the days - each showing whehter it's part of the window 
 		    for (int j = 0; j < numDays; j++) {
 	    		text = "";
 		    	boolean partOfWindow = isDateInWindow(dates[j], wstart, wstop);
 		    	// for each day, does this window cover it?
 		    	//cal[i][j+1] = partOfWindow ? "T" : "F";
 		    	// if part of the window, might be more info to add, like:
 		    	// * does this window extend off the calendar
 		    	// * what day does the default & choosen fall on?
 		    	if (partOfWindow) {
 		    		// first day in calendar?
 		    		if (j==0) {
 		    		    // if window starts before calendar, tell user when
 		    			if (dates[j].getTime() > wstart.getTime()) {
 		    				text += wstartStr;
 		    			}
 		    		}
 		    		// last day in calendar?
 		    		if (j == numDays - 1) {
 		    		    // if window ends after calendar, tell user when
 		    			if (dates[j].getTime() < wstop.getTime()) {
 		    				text += wstopStr;
 		    			}
 		    		}
 		    		// same day as default period?
 		    		if (dstart != null) {
 			    		if (dstart.equals(dates[j])) {
 			    			text += " default (" + dstate + ")";
 			    		}
 		    		}
 		    		// same day as choosen period?
 		    		if (cstart != null) {
 			    		if (cstart.equals(dates[j])) {
 			    			text += " choosen (" + cstate + ")";
 			    		}
 		    		}
 		    		// insert a separator, if needed
 		    		if (text.compareTo("") != 0) {
 		    			text = "; " + text;
 		    		}
 		    	}
 		    	cal[i][j+1] = partOfWindow ? "T" + text : "F";
 		    	
 		    }
 		}
 	
 		// now display it!
		//calendar = new WindowCalTable();
		//add(calendar);
 		calendar.loadCalendar(numWindows, numDays + 1, headers, cal);
 	}
 
 	private boolean isDateInWindow(Date dt, Date start, Date stop) {
 		return ((dt.getTime() >= start.getTime()) && (dt.getTime() <= stop.getTime()));
 	}
 	
 	public void loadCalendar(int rows, int cols, String[] headers, String[][] cal) {
 		// TODO
 		//String[] headers = new String[] {"h0", "h1"};
 		//String[][] cal = new String[][] {{"r0c0", "r0c1"},{"r1c0", "r1c1"}};
 		calendar.loadCalendar(rows, cols, headers, cal);
 	}
 	
 
 }
