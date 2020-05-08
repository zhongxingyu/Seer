 package edu.nrao.dss.client.util.dssgwtcal;
 
 import edu.nrao.dss.client.util.dssgwtcal.util.AppointmentUtil;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.i18n.client.NumberFormat;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 
 
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.FocusPanel;
 import com.google.gwt.user.client.ui.UIObject;
 import com.google.gwt.user.client.ui.Widget;
 
 // This subclass of the CalendarView is what brings all the DayView* classes
 // together to actually implement a calendar table with hours as rows and 
 // days as columns.
 
 public class DayView extends CalendarView {
 
     private static final String GWT_CALENDAR_STYLE = "gwt-cal";
     
     // for the header (ex: | 2011 | Mon, May 2 | Tue, May 3 |)
     private DayViewHeader dayViewHeader = null;
     
     // this actually creates the table with hours as rows and days as columns.
     private DayViewBody dayViewBody = null;
     
     // the way in which we show 'stuff' (Appointment objects) in this calendar view
     // is coded in this class.
     private DayViewLayoutStrategy layoutStrategy = null;
     
     private boolean lastWasKeyDown = false;
     
     // This is used to capture mouse and key events, which we use to select/delete/etc. an
     // Appointment displayed in the calendar
     private FocusPanel focusPanel = new FocusPanel();
     
     private float[] scores;
     private String timezone;
     
     public DayView() {
         this(CalendarSettings.DEFAULT_SETTINGS);
     }
 
     public DayView(CalendarSettings settings) {
 
         super(settings);
 
         // Just instantiate these classes - the actual display logic get's 
         // called in doLayout
         this.dayViewBody = new DayViewBody(this);
         this.dayViewHeader = new DayViewHeader(this);
         this.layoutStrategy = new DayViewLayoutStrategy(this);
 
         this.setStyleName(GWT_CALENDAR_STYLE);
 
         rootPanel.add(focusPanel);
 
         rootPanel.add(dayViewHeader);
         rootPanel.add(dayViewBody);
 
         // Why are we setting these attributes of the focusPanel?
         //border: 0px none ; width: 0px; height: 0px; position: absolute; top: -5px; left: -5px;
         DOM.setStyleAttribute(focusPanel.getElement(), "position", "absolute");
         DOM.setStyleAttribute(focusPanel.getElement(), "top", "-10");
         DOM.setStyleAttribute(focusPanel.getElement(), "left", "-10");
         DOM.setStyleAttribute(focusPanel.getElement(), "height", "0px");
         DOM.setStyleAttribute(focusPanel.getElement(), "width", "0px");
         
         // Here we capture some keyboard events
         // Q: why does capturing keyboard vs. mouse events look so different?
         focusPanel.addKeyPressHandler(new KeyPressHandler() {
 
             public void onKeyPress(KeyPressEvent event) {
 
                 if (!lastWasKeyDown) {
                     keyboardNavigation(event.getNativeEvent().getKeyCode());
                 }
                 lastWasKeyDown = false;
             }
         });
         focusPanel.addKeyUpHandler(new KeyUpHandler() {
 
             public void onKeyUp(KeyUpEvent event) {
 
                 lastWasKeyDown = false;
             }
         });
         focusPanel.addKeyDownHandler(new KeyDownHandler() {
 
             public void onKeyDown(KeyDownEvent event) {
                 keyboardNavigation(event.getNativeEvent().getKeyCode());
                 lastWasKeyDown = true;
             }
         });
 
 
         doLayout();
     }
 
     // Here we actually make the calls to the classes which causes stuff to get
     // laid out and displayed properly.
     @Override
     public void doLayout() {
 
         if (layoutSuspended) {
             layoutPending = true;
             return;
         }
 
         dayViewHeader.setDays((Date) getDate().clone(), getDays());
         dayViewHeader.setYear((Date) getDate().clone());
         dayViewBody.setDays((Date) getDate().clone(), getDays());
         dayViewBody.getTimeline().prepare();
 
         // we don't want to re-sort a sorted array
         // so we only sort if some action has triggered a new sort
         if (sortPending) {
             Collections.sort(appointments);
             sortPending = false;
         }
 
         Date tmpDate = (Date) getDate().clone();
 
         // add the appointments day by day
         for (int i = 0; i < getDays(); i++) {
 
         	// get the appointments to display for the current day
             ArrayList<AppointmentInterface> filteredList =
                     AppointmentUtil.filterListByDate(appointments, tmpDate);
 
             // perform layout
             // Q:why are we passing in AppointmentInterface's, not just Appointments?
             // Q why are we returning adapters, only to get the appointment
             // again in the next line?
             ArrayList<AppointmentAdapter> appointmentAdapters =
                     layoutStrategy.doLayout(filteredList, i, getDays());
 
             // add all appointments to the grid
             for (AppointmentAdapter appt : appointmentAdapters) {
                 this.dayViewBody.getGrid().grid.add((Widget) appt.getAppointment());
             }
 
             // if scores are present, 
             // there should be a score for each 15-min interval in our calendar
             int expNumScores = getDays() * 24 * 4; 
             if (scores != null && scores.length == expNumScores) {
             	addScoreLabels(scores, i, getDays(), tmpDate);
             }
 
             // add the 'Start' or 'End' labels?
             addSchedulingRange(i, getDays(), tmpDate);
        	
             // increment the day's date
             tmpDate.setDate(tmpDate.getDate() + 1);
         }
     }
 
     private int estHourToTimezone(int estHour) {
     	// TODO: take day-light savings into account
     	int offset = 4;
     	if (timezone == "UTC") {
     		return estHour + offset;
     	} else {
     		return estHour;
     	}
     }
     
     // The Scheduling Range needs to be demarcated with 'Start' and 'End' labels
     private void addSchedulingRange(int dayIndex, int numDays, Date date) {
     	
     	float thisLeft;
         String desc;
         String color;
         int startHour;
         int offset = 10;
     	
     	if ((dayIndex != 0) && (dayIndex != (numDays - 1))) {
     		return;  // no label needed unless its first day or last day
     	} else {
     		if (dayIndex == 0) {
     			// first day
     			desc = "Start";
     			color = "green";
     			thisLeft = (100.0f/numDays) - offset;
     			// simply start at beginning of calendar, regardless of timezone
     			startHour = 0; //estHourToTimezone(8);
     		} else {
     			// last day
     			desc = "End";
     			color = "red";
     			thisLeft = ((numDays*100.0f)/numDays) - offset;
     			// this *does* depend on a certain time
     	        startHour = estHourToTimezone(8);
     		}
     	}
 
         float widths = 32.3f; // constant width 
         float quarterHeight = this.getSettings().getPixelsPerInterval(); //30.0f; // height of one quarter == (2 px/min)(15 min)
         float qTop = 0.0f;
         Date start = date;
         Date end;
         int numQtrs = 24 * 4;  // each hour has 4 15-min quarters
     	
         qTop = (startHour*4) * quarterHeight;
         start = new Date(start.getTime() + (1000 * 60 * 15 * startHour));
         end = new Date(start.getTime() + (1000 * 60 * 14));
         
         // stuff gets added (like scores) via our Label class
     	Label lb = new Label();
     	lb.setDescription(desc);
     	lb.setTitle("");
     	lb.setStart(start);
     	lb.setEnd(end);
     	lb.setLeft(thisLeft);
     	lb.setWidth(widths);
     	lb.setHeight(quarterHeight);
     	lb.setTop(qTop);
     	
     	DOM.setStyleAttribute(lb.getElement(), "color", color);
     	
     	// add it to the calendar!
     	dayViewBody.addLabel(lb);
     }
     
     // adds the scores for this day to each quarter of the calendar's day
     private void addScoreLabels(float scores[], int dayIndex, int numDays, Date date) {
 
     	// compute where horizontally this line of scores should be printed
         float lefts[] = new float[numDays];
         for (int i = 0; i < numDays; i++) {
         	lefts[i] = i*(100.0f/numDays);
         }
         float thisLeft = lefts[dayIndex]; 
         
         float widths = 6f; // constant width 
         float quarterHeight = this.getSettings().getPixelsPerInterval(); //30.0f; // height of one quarter == (2 px/min)(15 min)
         float qTop = 0.0f;
         Date start = date;
         Date end;
         int numQtrs = 24 * 4;  // each hour has 4 15-min quarters
         String desc;
         int scoreOffset = dayIndex * numQtrs;
         
         for (int q = 0; q < numQtrs; q++) {
         	
         	// figure out where this score goes
         	qTop = (q*quarterHeight);
         	start = new Date(start.getTime() + (1000 * 60 * 15 * q));
         	end = new Date(start.getTime() + (1000 * 60 * 14));
         	float scoreValue = scores[q + scoreOffset];
         	desc = NumberFormat.getFormat("#0.00").format((double) scoreValue);
         	
         	// here's the object that will hold our score
         	Label score = new Label();
         	score.setDescription(desc);
         	score.setTitle("");
         	score.setStart(start);
         	score.setEnd(end);
         	score.setLeft(thisLeft);
         	score.setWidth(widths);
         	score.setHeight(quarterHeight);
         	score.setTop(qTop);      
     	    DOM.setStyleAttribute(score.getElement(), "size", "1");
         	
         	// add it to the calendar!
         	this.dayViewBody.getGrid().grid.add((Widget) score); 
         	
         	if (scoreValue == 0.0) {
         	    DOM.setStyleAttribute(score.getElement(), "color", "FF0000");
         	}
         }        
     }
     
     public void scrollToHour(int hour) {
         dayViewBody.getScrollPanel().setScrollPosition(hour *
                 getSettings().getIntervalsPerHour() * getSettings().getPixelsPerInterval());
     }
 
     @Override
     public void setHeight(String height) {
         super.setHeight(height);
         dayViewBody.setHeight(height);
     }
 
     @Override
     public void setSize(String width, String height) {
 
         super.setSize(width, height);
         dayViewBody.setHeight(getOffsetHeight() - 2 - dayViewHeader.getOffsetHeight() + "px");
     }
 
     // if the mouse clicks on an appointment, set this appointment as the 'selected'
     // one, and possibly fire off other events.
     @Override
     @SuppressWarnings("fallthrough")
     public void onBrowserEvent(Event event) {
         int eventType = DOM.eventGetType(event);
 
         switch (eventType) {
             case Event.ONMOUSEDOWN:
                  {
                     if (DOM.eventGetCurrentTarget(event) == getElement()) {
                         Element elem = DOM.eventGetTarget(event);
                         Appointment appt =
                                 AppointmentUtil.checkAppointmentElementClicked(elem, appointments);
                         if (appt != null) {
                         	// the selected appointment has been found - but should we fire events?
                             setValue(appt);
                         }
                         // Q: what does this do?
                         focusPanel.setFocus(true);
                         // Q: and what do these do?
                         DOM.eventCancelBubble(event, true);
                         DOM.eventPreventDefault(event);
 
                         //break;
                         return;
                     }
             } 
         } 
         
         super.onBrowserEvent(event);
     }
 
     // here we do some basic appointment naviation with the arrow keys,
     // and enable the Delete key - all based on the given key ID
     private void keyboardNavigation(int key) {
         switch (key) {
             case KeyCodes.KEY_DELETE: {
                 removeAppointment((Appointment) getSelectedAppointment());
                 break;
             }
             case KeyCodes.KEY_LEFT:
             case KeyCodes.KEY_UP: {
                 selectPreviousAppointment();
                 dayViewBody.getScrollPanel().ensureVisible((UIObject) getSelectedAppointment());
                 break;
             }
             case KeyCodes.KEY_RIGHT:
             case KeyCodes.KEY_DOWN: {
                 selectNextAppointment();
                 dayViewBody.getScrollPanel().ensureVisible((UIObject) getSelectedAppointment());
                 break;
             }
         }
     }
 
     // called by the focus panel - how to react to certain keys pressed
     private void keyboardNavigation(Event event) {
         //only proceed if an appointment is selected
         if (getSelectedAppointment() == null) {
             return;
         }
         //get the key
         int key = DOM.eventGetKeyCode(event);
         keyboardNavigation(key);
     }
 
 	public void setScores(float[] scores) {
 		this.scores = scores;
 	}
 
 	public float[] getScores() {
 		return scores;
 	}
 
 	public void clearScores() {
 		scores = null;
 	}
 
 	public void setTimezone(String timezone) {
 		this.timezone = timezone;
 	}
 
 	public String getTimezone() {
 		return timezone;
 	}
 }
