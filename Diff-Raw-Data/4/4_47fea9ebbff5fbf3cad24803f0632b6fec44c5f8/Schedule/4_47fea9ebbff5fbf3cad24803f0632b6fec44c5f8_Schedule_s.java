 package edu.nrao.dss.client;
 
 
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.data.BaseModelData;
 import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
 import com.extjs.gxt.ui.client.data.ListLoader;
 import com.extjs.gxt.ui.client.data.LoadEvent;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.button.ToolButton;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.LabelField;
 import com.extjs.gxt.ui.client.widget.form.Time;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 
 import edu.nrao.dss.client.util.dssgwtcal.Appointment;
 import edu.nrao.dss.client.util.dssgwtcal.CalendarSettings;
 import edu.nrao.dss.client.util.dssgwtcal.DayView;
 import edu.nrao.dss.client.util.dssgwtcal.Event;
 
 // This class is the new version of the Beta Test's Scheduling Page.
 
 public class Schedule extends ContentPanel {
 	
 	public ScheduleCalendar scheduleExplorer;
 	public VacancyControl vacancyControl;
 	public CalendarControl calendarControl;
 	public ScheduleControl scheduleControl;
 	private NomineePanel nomineePanel;
 	private Reservations reservations;
 	private ContentPanel calendar;
 
 	private DayView dayView;
 	
 	Date startCalendarDay = new Date();
 	Integer numCalendarDays = 3;
 	String timezone = "UTC";
 	String baseUrl = "/periods/" + timezone;
 	
 	Scores scores;
 	
 	Integer numVacancyMinutes = 2;
 	Date startVacancyDate = new Date();
 	Time startVacancyTime = new Time();
 	public Date startVacancyDateTime = new Date();
 	
 	private ArrayList<String> sess_handles = new ArrayList<String>();
 	
 	public Schedule() {
 			super();
 			initLayout();
 			initListeners();
 	}	
 	
 	public String getTimeZone() {
 		return timezone;
 	}
 	
 	protected void initLayout() {
 		
 		
 		setHeaderVisible(true);
 		setLayout(new BorderLayout());
 		
 		setCollapsible(false);
 		setBodyBorder(false);
 		setFrame(false);
 		setHeaderVisible(false);
 		setBodyStyle("backgroundColor: white;");
 		setHeight(920);
 		//setAutoHeight(true);
 		getHeader().addTool(new ToolButton("x-tool-gear"));
 		getHeader().addTool(new ToolButton("x-tool-close"));
 
         // basic layout: controls to the west, calendar in the center		
 		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 760);
 		westData.setMinSize(50);
 		westData.setMaxSize(1000);
 		westData.setMargins(new Margins(5));
 		westData.setSplit(true);
 		westData.setCollapsible(true);
 
 		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER); ;
 		centerData.setMargins(new Margins(5, 0, 5, 0));
 		
 		// now for the child panels:
 		// to the side, control widgets
 		// ======================== Controls ===================================
 		final ContentPanel controlsContainer = new ContentPanel();
 		controlsContainer.setFrame(true);
 		controlsContainer.setBorders(true);
 		controlsContainer.setHeading("Controls");
 		controlsContainer.setScrollMode(Scroll.AUTO);
 
 		calendarControl = new CalendarControl(this);
 		calendarControl.setCollapsible(true);
 		controlsContainer.add(calendarControl);
 		
 		scheduleControl = new ScheduleControl(this);
         scheduleControl.setCollapsible(true);
         controlsContainer.add(scheduleControl);
 		
         scheduleExplorer = new ScheduleCalendar(startCalendarDay, numCalendarDays);
 		scheduleExplorer.addButtonsListener(this);
 		scheduleExplorer.setDefaultDate(startCalendarDay);
 		scheduleExplorer.setCollapsible(true);
 		scheduleExplorer.setAutoHeight(true);
 		controlsContainer.add(scheduleExplorer);
 		
 		vacancyControl = new VacancyControl(this);
         vacancyControl.setCollapsible(true);
         vacancyControl.collapse();
         controlsContainer.add(vacancyControl);
 
         nomineePanel = new NomineePanel(this);
 		nomineePanel.setCollapsible(true);
 		nomineePanel.collapse();
 		controlsContainer.add(nomineePanel);
 		
 		reservations = new Reservations(startCalendarDay, numCalendarDays);
         reservations.setCollapsible(true);
         reservations.collapse();
         controlsContainer.add(reservations);
         
         // in the middle, the calendar
 		// ======================== Calendar ===================================
 		calendar = new ContentPanel(); // TODO extend to bottom of panel
   		calendar.setHeading("Calendar");
   		//calendar.setAutoHeight(true);
 		calendar.setScrollMode(Scroll.AUTOX);
 		calendar.setStyleAttribute("bgcolor", "black");
 		
 		FormPanel fp = new FormPanel();
 		fp.setHeaderVisible(false);
 		fp.setBorders(false);
 		fp.setLayout(new RowLayout(Orientation.HORIZONTAL));
 		fp.setHeight(40);
 		fp.setWidth("100%");
 		fp.setStyleAttribute("background", "#E9EEF6");
 		
 		LabelField pending = new LabelField("Legend");
 		pending.setStyleAttribute("color", "#F2A640");
 		pending.setValue("Pending");
 		fp.add(pending, new RowData(-1, -1, new Margins(0, 10, 0, 10)));
 		
 		LabelField fixed = new LabelField("Legend");
 		fixed.setStyleAttribute("color", "#D96666");
 		fixed.setValue("Fixed");
 		fp.add(fixed, new RowData(-1, -1, new Margins(0, 10, 0, 10)));
 		
 		LabelField open = new LabelField("Legend");
 		open.setStyleAttribute("color", "#668CD9");
 		open.setValue("Open");
 		fp.add(open, new RowData(-1, -1, new Margins(0, 10, 0, 10)));
 		
 		LabelField dwindow = new LabelField("Legend");
 		dwindow.setStyleAttribute("color", "#4CB052");
 		dwindow.setValue("Default Windowed");
 		dwindow.setWidth(120);
 		fp.add(dwindow, new RowData(-1, -1, new Margins(0, 10, 0, 10)));
 		
 		LabelField ndwindow = new LabelField("Legend");
 		ndwindow.setStyleAttribute("color", "#BFBF4D");
 		ndwindow.setValue("Non-Default Windowed");
 		ndwindow.setWidth(150);
 		fp.add(ndwindow, new RowData(-1, -1, new Margins(0, 10, 0, 10)));
 		
 		LabelField elwindow = new LabelField("Legend");
 		elwindow.setStyleAttribute("color", "#8C66D9");
 		elwindow.setValue("Elective");
 		//elwindow.setWidth(150);
 		fp.add(elwindow, new RowData(-1, -1, new Margins(0, 10, 0, 10)));
 		
 		calendar.add(fp);
 		
 		// calendar
 		dayView = new DayView();
 		dayView.setDate(startCalendarDay); //calendar date, not required
 		dayView.setDays((int) numCalendarDays); //number of days displayed at a time, not required
 		dayView.setWidth("100%");
		dayView.setHeight("97%");
 		dayView.setTitle("Schedule Calendar");
 		CalendarSettings settings = new CalendarSettings();
 		// this fixes offset issue with time labels
 		settings.setOffsetHourLabels(false);
 		// 15-min. boundaries!
 		settings.setIntervalsPerHour(4);
 		settings.setEnableDragDrop(true);
 		settings.setPixelsPerInterval(12); // shrink the calendar!
 		dayView.setSettings(settings);
 		// when a period is clicked, a user can insert a different session
 		// but we need all those session names
 		getSessionOptions();
 		dayView.addValueChangeHandler(new ValueChangeHandler<Appointment>(){
 	        public void onValueChange(ValueChangeEvent<Appointment> event) {
 	        	// seed the PeriodDialog w/ details from the period that just got clicked
 	            String periodUrl = "/periods/UTC/" + event.getValue().getEventId();
 	    	    JSONRequest.get(periodUrl, new JSONCallbackAdapter() {
 		            @Override
 		            public void onSuccess(JSONObject json) {
 		            	// JSON period -> JAVA period
 	                 	Period period = Period.parseJSON(json.get("period").isObject());
                         // display info about this period, and give options to change it
 	                 	PeriodSummaryDlg dlg = new PeriodSummaryDlg(period, sess_handles, (Schedule) controlsContainer.getParent());
 		            }
 		    });	            
 	            
 	        }               
 	    });	
 		//dayView.addSelectionHandler(handler); // TODO handle nominee selection in calendar?
 		calendar.add(dayView);
 		
 		// add all the components to this parent panel
 		add(controlsContainer, westData);
 		add(calendar, centerData);
 
 		updateCalendar();
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void initListeners() {
 		nomineePanel.getLoader().addListener(ListLoader.Load, new Listener<LoadEvent>() {
 
 			@Override
 			public void handleEvent(LoadEvent be) {
 				// TODO Auto-generated method stub
 				startVacancyDateTime = startVacancyDate;
 				startVacancyDateTime.setHours(startVacancyTime.getHour());
 				startVacancyDateTime.setMinutes(startVacancyTime.getMinutes());
 				startVacancyDateTime.setSeconds(0);
 				String startStr = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss").format(startVacancyDateTime);
 				int num_nominees = nomineePanel.getNumNominees();
 				nomineePanel.setHeading("Nominee Periods for " + startStr + " " + timezone + ".  " + num_nominees + " nominees found.");
 				if (num_nominees == 0){
 					MessageBox.alert("Attention", "No nominees returned!", null);
 				}
 			}
 			
 		});
 	}
 	
 	public FactorsDlg getFactorsDlg() {
 		return scheduleControl.factorsDlg;
 	}
 	
 	public void updateNominees() {
 		startVacancyDateTime = startVacancyDate;
 		startVacancyDateTime.setHours(startVacancyTime.getHour());
 		startVacancyDateTime.setMinutes(startVacancyTime.getMinutes());
 		startVacancyDateTime.setSeconds(0);
 		String startStr = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss").format(startVacancyDateTime);
 		
 		HashMap<String, Object> keys = new HashMap<String, Object>();
 		keys.put("start", startStr);
 		keys.put("duration", numVacancyMinutes);
 		keys.put("timeBetween", (Boolean) vacancyControl.nomineeOptions.get(0).getValue()); // ignore timeBetween limit?
 		keys.put("minimum", (Boolean) vacancyControl.nomineeOptions.get(1).getValue());     // ignore minimum duration limit?
 		keys.put("blackout", (Boolean) vacancyControl.nomineeOptions.get(2).getValue());    // ignore observer blackout times?
 		keys.put("backup", (Boolean) vacancyControl.nomineeOptions.get(3).getValue());      // use only backup sessions?
 		keys.put("completed", (Boolean) vacancyControl.nomineeOptions.get(4).getValue());   // include completed sessions?
 		keys.put("rfi", (Boolean) vacancyControl.nomineeOptions.get(5).getValue());         // ignore RFI exclusion flag?
 		nomineePanel.updateKeys(keys);
 		nomineePanel.loadData();
 		nomineePanel.expand();
 	}
 	
     public void updateCalendar() {	
     	// construct the url that gets us our periods for the explorer
 		String startStr = DateTimeFormat.getFormat("yyyy-MM-dd").format(startCalendarDay);
 		String url = baseUrl + "?startPeriods=" + startStr + "&daysPeriods=" + Integer.toString(numCalendarDays);
 		
 		// get the period explorer to load these
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
 		DynamicHttpProxy<BasePagingLoadResult<BaseModelData>> proxy = scheduleExplorer.pe.getProxy();
 		proxy.setBuilder(builder);
 		scheduleExplorer.setDefaultDate(startCalendarDay);
 		scheduleExplorer.pe.loadData();
 		
 		// now get the calendar to load these
 		dayView.setDate(startCalendarDay); //calendar date, not required
 		dayView.setDays((int) numCalendarDays);		
 
 		// make the JSON request for the periods so we can make appointments
 		// we need the same url in a different format
 		HashMap<String, Object> keys = new HashMap<String, Object>();
 		keys.put("startPeriods", startStr);
 		keys.put("daysPeriods", Integer.toString(numCalendarDays));
 	    JSONRequest.get(baseUrl, keys, new JSONCallbackAdapter() {
 	            @Override
 	            public void onSuccess(JSONObject json) {
 	            	// JSON periods -> JAVA periods
 	                List<Period> periods = new ArrayList<Period>();
 	                JSONArray ps = json.get("periods").isArray();
 	                for (int i = 0; i < ps.size(); ++i) {
 	                	Period period = Period.parseJSON(ps.get(i).isObject());
 	                	if (period != null){
 	                		// TODO: really we should be using period state to keep these periods out
 	                		if (period.getDuration() > 0) {
                         		periods.add(period);
 	                        }
 	                	}
 	                }
 	                // update the gwt-cal widget
 	                loadAppointments(periods);
 	            }
 	    });
 	    reservations.update(DateTimeFormat.getFormat("MM/dd/yyyy").format(startCalendarDay)
 	    		          , Integer.toString(numCalendarDays));
 	}		
 	    
     // updates the gwt-cal widget w/ given periods
     private void loadAppointments(List<Period> periods) {	
 		dayView.suspendLayout();
 		dayView.clearAppointments();
 		for(Period p : periods) {
                 // TODO: format title & description better			
 			    String title = ""; //Integer.toString(p.getId());
 			    String windowInfo = "";
 			    String session_type = p.getSessionType();
 			    String type = "not windowed!"; // TODO: need better way to indicate period attributes
 			    if (p.isWindowed()) {
 			    	windowInfo = " +" + Integer.toString(p.getWindowDaysAhead()) + "/-" + Integer.toString(p.getWindowDaysAfter());
 			    	type = p.isDefaultPeriod() ? "default period" : "chosen period";
 			    }
 			    String desc = p.getSession() + windowInfo;
 			    Event event = new Event(p.getId(), title, desc, p.getStart(), p.getEnd(), type, session_type, p.getState());
 		        dayView.addAppointments(event.getAppointments());
 		        
 		}
 		
 		//dayView.add
 		dayView.resumeLayout();
 		
 		// clear the header if no scores being displayed
         if (dayView.getScores() == null) {
         	setCalendarHeader("Calendar");
         }
 		
 		// clear out the scores so that next time the calendar is updated,
 		// unless new scores have been provided, the present display of scores
 		// is erased
 		dayView.clearScores();
     }
     
     // gets all the session handles (sess name (proj name)) and holds on to them
     // for use in lists (e.g. PeriodDialog)
     private void getSessionOptions() {
         JSONRequest.get("/sessions/options"
         		     , new HashMap<String, Object>() {{
         		    	    put("mode", "session_handles");
         		     }}
         		   , new JSONCallbackAdapter() {
         			   public void onSuccess(JSONObject json) {
     					JSONArray sessions = json.get("session handles").isArray();
     					for (int i = 0; i < sessions.size(); ++i){
     						sess_handles.add(sessions.get(i).toString().replace('"', ' ').trim());
     					}       
    					
         			   }
         		   }
        );
 
     }   
     
 	public void setCalendarScores(float[] scores) {
 	    dayView.setScores(scores);
 	}
 	
 	public Date getStartCalendarDay() {
 	    return startCalendarDay;	
 	}
 	
 	public int getNumCalendarDays() {
 		return numCalendarDays;
 	}
 	
 	public void setCalendarHeader(String header) {
 		calendar.setHeading(header);
 	}
 	
 	public void setTimezone(String tz) {
 		dayView.setTimezone(tz);
 		this.timezone = tz;
 	}
 
 }	
 	
