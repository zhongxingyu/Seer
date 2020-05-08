 package edu.rpi.rocs.client.ui;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import edu.rpi.rocs.client.ImageManager;
 import edu.rpi.rocs.client.objectmodel.Course;
 import edu.rpi.rocs.client.objectmodel.CrossListing;
 import edu.rpi.rocs.client.objectmodel.SchedulerManager;
 import edu.rpi.rocs.client.objectmodel.Section;
 import edu.rpi.rocs.client.objectmodel.Semester;
 import edu.rpi.rocs.client.objectmodel.SemesterManager;
 import edu.rpi.rocs.client.objectmodel.SchedulerManager.RestorationEventHandler;
 import edu.rpi.rocs.client.ui.coursesearch.CourseSearchPanel;
 import edu.rpi.rocs.client.ui.scheduler.SchedulerDisplayPanel;
 import edu.rpi.rocs.client.ui.scheduler.SchedulerFilterDisplayPanel;
 import edu.rpi.rocs.client.ui.semesterselect.SemesterSelectionPanel;
 import edu.rpi.rocs.client.ui.classview.ClassViewPanel;
 
 public class ROCSInterface extends HTMLPanel implements RestorationEventHandler {
 	SemesterSelectionPanel semesterPanel;
 	CourseSearchPanel searchPanel;
 	ClassViewPanel viewPanel;
 	SchedulerFilterDisplayPanel filterPanel;
 	SchedulerDisplayPanel schedulerDisplayPanel;
 	edu.rpi.rocs.client.ui.scheduler.ie.SchedulerDisplayPanel ieDisplayPanel;
 	MultiStackPanel thePanel;
 
 	static ROCSInterface theInstance;
 	
 	public static ROCSInterface getInstance() {
 		if(theInstance==null) theInstance = new ROCSInterface();
 		return theInstance;
 	}
 	
 	private static String createHeaderText(String text, boolean visible) {
 		return "<img src=\"" +
 				ImageManager.getPathForImage((visible? 
 			    ImageManager.Image.ExpandedArrow : 
 			    ImageManager.Image.CollapsedArrow)) + 
 				"\" width=\"12\" height=\"12\" />"+text;
 	}
 	
 	private class UpdateImageHelper implements MultiStackPanel.MultiStackPanelVisibilityChangeHandler {
 		private String m_text="";
 		private int m_i;
 		
 		public UpdateImageHelper(int i, String text) {
 			m_text = text;
 			m_i = i;
 		}
 
 		public void onChange(int index, boolean visible) {
 			if(index==m_i) thePanel.setStackText(index, createHeaderText(m_text, visible), true);
 		}
 	}
 	
 	private class SemesterImageHelper extends UpdateImageHelper {
 
 		public SemesterImageHelper() {
 			super(0, "");
 		}
 		
 		public void onChange(int index, boolean visible) {
 			if(index==0 && SemesterManager.getInstance().getCurrentSemester()!=null)
 				thePanel.setStackText(index, createHeaderText("<span>Semester - "+
 					SemesterManager.getInstance().getCurrentSemester().getSemesterDesc()+"</span><span style=\"float:right;font-weight:normal;\"><i>Last updated:</i> "+SemesterManager.getInstance().getCurrentSemester().getLastChangeTime()+"&nbsp;&nbsp;</span>", visible), true);
 		}
 	}
 	
 	private ROCSInterface() {
 		/*
 		super("<div id=\"rocs_PORTLET_rocs_semester_pane\"></div>"+
 				"<div id=\"rocs_PORTLET_rocs_courses_pane\"></div>" +
 				"<div id=\"rocs_PORTLET_rocs_search_pane\"></div>" +
 				"<div id=\"rocs_PORTLET_rocs_scheduler_pane\"></div>");
 		
 		this.setHeight("600px");
 		
 		semesterPanel = SemesterSelectionPanel.getInstance();
 		searchPanel = CourseSearchPanel.getInstance();
 		schedulerPanel = SchedulerPanel.get();
 		viewPanel = ClassViewPanel.getInstance();
 		
 		this.add(semesterPanel, "rocs_PORTLET_rocs_semester_pane");
 		this.add(searchPanel, "rocs_PORTLET_rocs_search_pane");
 		this.add(schedulerPanel, "rocs_PORTLET_rocs_scheduler_pane");
 		this.add(viewPanel, "rocs_PORTLET_rocs_courses_pane");
 		*/
 		super("<div id=\"rocs_PORTLET_rocs_stackbody\"></div>");
 		thePanel = new MultiStackPanel();
 		
 		SemesterManager.getInstance().addSemesterChangeListener(new SemesterManager.SemesterManagerCallback() {
 			
 			public void semesterLoaded(Semester semester) {
 				thePanel.setStackText(0, createHeaderText("<span>Semester - "+semester.getSemesterDesc()+"</span><span style=\"font-weight: normal; float:right;\"><i>Last updated:</i> "+SemesterManager.getInstance().getCurrentSemester().getLastChangeTime()+"&nbsp;&nbsp;</span>",thePanel.isVisible(0)),true);
 				Log.debug("Semester - "+semester.getSemesterDesc());
 			}
 
 			public void didChangeCourse(Course c) {
 			}
 
 			public void didChangeCrosslisting(CrossListing cl) {
 			}
 
 			public void didChangeSection(Section s) {
 			}
 
 			public void semesterUpdated(Semester semester) {
 				thePanel.setStackText(0, createHeaderText("<span>Semester - "+semester.getSemesterDesc()+"</span><span style=\"font-weight: normal; float:right;\"><i>Last updated:</i> "+SemesterManager.getInstance().getCurrentSemester().getLastChangeTime()+"&nbsp;&nbsp;</span>",thePanel.isVisible(0)),true);
 				Log.debug("Semester - "+semester.getSemesterDesc());
 			}
 		});
 		
 		HTMLPanel temp = new HTMLPanel("<div id=\"rocs_PORTLET_rocs_semester_pane\"></div>");
 		semesterPanel = SemesterSelectionPanel.getInstance();
 		temp.add(semesterPanel, "rocs_PORTLET_rocs_semester_pane");
 		thePanel.add(temp,createHeaderText("<span>Semester - Loading...</span>",false),true);
 		
 		
 		searchPanel = CourseSearchPanel.getInstance();
 		temp = new HTMLPanel("<div id=\"rocs_PORTLET_rocs_search_pane\"></div>");
 		temp.add(searchPanel, "rocs_PORTLET_rocs_search_pane");
 		thePanel.add(temp,createHeaderText("Course Search",true),true);
 		viewPanel = ClassViewPanel.getInstance();
 		temp = new HTMLPanel("<div id=\"rocs_PORTLET_rocs_courses_pane\"></div>");
 		temp.add(viewPanel, "rocs_PORTLET_rocs_courses_pane");
 		thePanel.add(temp,createHeaderText("Selected Courses",false),true);
 		
 		Log.debug("Adding filter panel");
 		filterPanel = SchedulerFilterDisplayPanel.getInstance();
 		temp = new HTMLPanel("<div id=\"rocs_PORTLET_rocs_filters_pane\"></div>");
 		temp.add(filterPanel, "rocs_PORTLET_rocs_filters_pane");
 		thePanel.add(temp,createHeaderText("Schedule Filters",false),true);
 		
 		Log.debug("Adding scheduler panel");
 		if(isMSIE()) {
 			ieDisplayPanel = edu.rpi.rocs.client.ui.scheduler.ie.SchedulerDisplayPanel.getInstance();
 			temp = new HTMLPanel("<div id=\"rocs_PORTLET_rocs_scheduler_pane\"></div>");
 			temp.add(ieDisplayPanel, "rocs_PORTLET_rocs_scheduler_pane");
 			thePanel.add(temp,createHeaderText("Schedules",false),true);
 		}
 		else {
 			schedulerDisplayPanel = SchedulerDisplayPanel.getInstance();
 			temp = new HTMLPanel("<div id=\"rocs_PORTLET_rocs_scheduler_pane\"></div>");
 			temp.add(schedulerDisplayPanel, "rocs_PORTLET_rocs_scheduler_pane");
 			thePanel.add(temp,createHeaderText("Schedules",false),true);
 		}
 		
 		thePanel.showStack(0);   // Shown by default, this hides it
 		thePanel.getWidget(0).setHeight("0px");
 		
 		thePanel.showStack(1);   // Shows the search pane
 		thePanel.getWidget(2).setHeight("0px");
 		thePanel.getWidget(3).setHeight("0px");
 		
 		thePanel.getWidget(4).setHeight("0px");
 		
 		thePanel.addChangeHandler(new SemesterImageHelper());
 		thePanel.addChangeHandler(new UpdateImageHelper(1, "Course Search"));
 		thePanel.addChangeHandler(new UpdateImageHelper(2, "Selected Courses"));
 		thePanel.addChangeHandler(new UpdateImageHelper(3, "Schedule Filters"));
 		
 		thePanel.addChangeHandler(new UpdateImageHelper(4, "Schedules"));
 		
 		this.add(thePanel, "rocs_PORTLET_rocs_stackbody");
 		thePanel.setAnimationTime(300);
 		SchedulerManager.getInstance().addRestorationEventHandler(this);
 	}
 	
 	public boolean isDisplaying(Widget w) {
 		int index=-1;
 		boolean result=false;
 		if(w==SemesterSelectionPanel.getInstance()) index=0;
 		if(w==CourseSearchPanel.getInstance()) index=1;
 		if(w==ClassViewPanel.getInstance()) index=2;
 		if(w==SchedulerFilterDisplayPanel.getInstance()) index=3;
		if(!isMSIE()&&w==SchedulerDisplayPanel.getInstance()) index=4;
		if(isMSIE()&&w==edu.rpi.rocs.client.ui.scheduler.ie.SchedulerDisplayPanel.getInstance()) index=4;
 		if(index>-1) {
 			result = thePanel.isVisible(index);
 		}
 		return result;
 	}
 	
 	public void show(Widget w, boolean visible) {
 		int index=-1;
 		if(w==SemesterSelectionPanel.getInstance()) index=0;
 		if(w==CourseSearchPanel.getInstance()) index=1;
 		if(w==ClassViewPanel.getInstance()) index=2;
 		if(w==SchedulerFilterDisplayPanel.getInstance()) index=3;
		if(!isMSIE()&&w==SchedulerDisplayPanel.getInstance()) index=4;
		if(isMSIE()&&w==edu.rpi.rocs.client.ui.scheduler.ie.SchedulerDisplayPanel.getInstance()) index=4;
 		if(index>-1&&isDisplaying(w)!=visible) {
 			thePanel.showStack(index);
 		}
 	}
 	
 	public static native String getUserAgent()/*-{
 		return navigator.userAgent.toLowerCase();
 	}-*/;
 	
 	public static boolean isMSIE() {
 		return getUserAgent().contains("msie");
 	}
 
 	public void restore() {
 		//show(SemesterSelectionPanel.getInstance(),false);
 		//show(CourseSearchPanel.getInstance(),false);
 		show(ClassViewPanel.getInstance(),true);
 		//show(SchedulerFilterDisplayPanel.getInstance(),false);
 		if(isMSIE()) {
 			show(edu.rpi.rocs.client.ui.scheduler.ie.SchedulerDisplayPanel.getInstance(),true);
 		}
 		else {
 			show(SchedulerDisplayPanel.getInstance(),true);
 		}
 	}
 }
