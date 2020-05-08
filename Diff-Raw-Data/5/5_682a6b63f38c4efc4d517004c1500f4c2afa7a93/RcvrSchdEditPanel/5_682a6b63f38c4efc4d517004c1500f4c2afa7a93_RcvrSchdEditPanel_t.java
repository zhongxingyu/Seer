 package edu.nrao.dss.client;
 
 import java.util.Date;
 import java.util.HashMap;
 
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.DateField;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.json.client.JSONObject;
 
 public class RcvrSchdEditPanel extends ContentPanel {
 	
 	private DateField day = new DateField();
 	private TextField<String> finalRcvrs = new TextField<String>();
 	private TextField<String> goingUpRcvrs = new TextField<String>();
 	private TextField<String> goingDownRcvrs = new TextField<String>();
 	private Button save = new Button();
 	private TextField<String> warning = new TextField<String>();
 	private TextField<String> info = new TextField<String>();
 	
 	private String[][] diffSchedule;
 	private String[] periods;
 	private String[] maintenanceDays;
 	
 	private ReceiverSchedule parent;
 	
     private static final DateTimeFormat DATE_FORMAT = DateTimeFormat.getFormat("MM/dd/yyyy");
     private static final DateTimeFormat DATE_FORMAT_MAINT = DateTimeFormat.getFormat("yyyy-MM-dd");   
     
 	public RcvrSchdEditPanel() {
 		initLayout();
 		initListeners();
 	}
 	
 	private void initLayout() {
 		setLayout(new FlowLayout());
 		setHeading("Create new or modify exisitng Receiver Change Date");
 		
 		FormPanel fp = new FormPanel();
 		fp.setHeaderVisible(false);
 		
 		day.setFieldLabel("Date");
 		fp.add(day);
 
 		warning.setFieldLabel("Warning");
 		warning.setValue("Date is NOT a Maintenance Day");
 		warning.setStyleAttribute("color", "red");
 		warning.setVisible(false);
 		fp.add(warning);
 
 		info.setFieldLabel("Info");
 		info.setValue("New Receiver Change Date.");
 		info.setStyleAttribute("color", "red");
 		info.setVisible(false);
 		fp.add(info);
 		
 		finalRcvrs.setFieldLabel("Rcvrs available at end of day");
 		fp.add(finalRcvrs);
 		
 		goingUpRcvrs.setFieldLabel("Rcvrs going up");
 		goingUpRcvrs.setAllowBlank(true);
 		fp.add(goingUpRcvrs);
 		
 		goingDownRcvrs.setFieldLabel("Rcvrs going down");
 		goingDownRcvrs.setAllowBlank(true);
 		fp.add(goingDownRcvrs);
 		
 
 		save.setText("Save");
 		fp.add(save);
 		
 
 		
 		add(fp);
 	}
 	
 	private void initListeners() {
 		
 	    day.addListener(Events.Valid, new Listener<BaseEvent>() {
 	    	public void handleEvent(BaseEvent be) {
 	            //startCalendarDay = dt.getValue();
 	    		Date dt = day.getValue();
 	    		setDay(DATE_FORMAT.format(dt));
 ;	    	}
 	    });
 	    
 	    save.addListener(Events.OnClick, new Listener<BaseEvent>() {
 	    	public void handleEvent(BaseEvent be) {
 	    		GWT.log("Save.onClick", null);
 	    		// send date, ups & downs to servers to change schedule!
 	    		changeRcvrSchedule();
 	    	}
 	    });
 	}
 	
 	public void loadSchedule(String[][] diffSchedule) {
 		this.diffSchedule = diffSchedule;
 
 		periods = new String[diffSchedule.length];
 		for (int i = 0; i < diffSchedule.length; i++) {
 			periods[i] = diffSchedule[i][0];
 		}
 		
 		Date dt = day.getValue();
 		if (dt != null) {
 		    setDay(DATE_FORMAT.format(dt));
 		}    
 	}
 	
 	private void setDay(String day) {
 		Date diffDayObj = new Date();
 		Date mDayObj = new Date();
 		Date lastDay = null; //new Date();
 		String diffDay;
 		boolean dayOnChange = false;
 		boolean dayOnMaintenance = false;
 		int lastDayIndex = 0;
 		Date dayObj = new Date();
 		dayObj     = DATE_FORMAT.parse(day);
 		
 		// set the up and down text boxes according to the day
 		for (int i = 0; i < diffSchedule.length; i++) {
 			// first we need to find if the day given is one of the days already in schedule.
 			// so, convert the strings to Date objects so we can compare better
 			diffDay = diffSchedule[i][0];
 			diffDayObj = DATE_FORMAT.parse(diffDay);
 			
 			// save off any date that is equal to or before the given day
 			if (diffDayObj.compareTo(dayObj) <= 0) {
 				lastDay = new Date();
 				lastDay = diffDayObj;
 				lastDayIndex = i;
 				dayOnChange = (dayObj.compareTo(diffDayObj) == 0) ? true : false;
 			}
 		}
 		
 		// does this fall on a maintanence day?
 		int mDayIndex = -1;
 		for (int i = 0; i < maintenanceDays.length; i++) {
 			mDayObj = DATE_FORMAT_MAINT.parse(maintenanceDays[i]);
 			if (dayObj.compareTo(mDayObj) == 0) {
 				mDayIndex = i;
 				dayOnMaintenance = true;
 			}
 		}
 		
 		// tell the user what we know:
 		finalRcvrs.setValue(diffSchedule[lastDayIndex][3]);
 		
 		// if the given day is the day we're changing rcvrs, then tell them more:
 		if (dayOnChange) {
 			goingUpRcvrs.setValue(diffSchedule[lastDayIndex][1]);
 			goingDownRcvrs.setValue(diffSchedule[lastDayIndex][2]);
 			//finalRcvrs.setValue(diffSchedule[lastDayIndex][3]);
 			info.setVisible(false);
 		} else {
 			goingUpRcvrs.setValue("");
 			goingDownRcvrs.setValue("");
 			info.setVisible(true);
 		}
 		if (dayOnMaintenance) {
 			warning.setVisible(false);
 		} else {
 			warning.setVisible(true);
 		}
 	}
 	
 	private void changeRcvrSchedule() {
 		
 		HashMap<String, Object> keys = new HashMap<String, Object>();
 		//keys.put("startdate", DATE_FORMAT.format(day.getValue()));
		// NOTE: even though rcvr changes happen at some time during the day, we still need to specify a datetime,
		// so in the DB, all rcvrs are expected to be up by 16:00 UTC - not that it matters, as the Maint. will
		// probably cover this time
		String startStr = DateTimeFormat.getFormat("yyyy-MM-dd").format(day.getValue()) + " 16:00:00";
 		keys.put("startdate", startStr);
 		
 		// watch for nulls in the rcvr entries
 		String upStr = goingUpRcvrs.getValue();
 		String downStr = goingDownRcvrs.getValue();
 		keys.put("up", upStr == null ? "" : upStr);
 		keys.put("down", downStr == null ? "" : downStr);
 		
 		JSONRequest.post("/receivers/change_schedule", keys  
 			      , new JSONCallbackAdapter() {
 			public void onSuccess(JSONObject json) {
 				
 				GWT.log("rcvr change schedule success", null);
 				GWT.log(json.toString(), null);
 				
 				// reload the calendar
 				parent.updateRcvrSchedule();
 
 			
 			}
 		});
 	}
 	
 	public void setParent(ReceiverSchedule rs) {
 		parent = rs;
 	}
 	
 	public void setMaintenanceDays(String[] maintenanceDays) {
 		this.maintenanceDays = maintenanceDays;
 	}
 
 	public String[] getMaintenanceDays() {
 		return maintenanceDays;
 	}	
 }
