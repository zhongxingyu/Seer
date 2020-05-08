 package com.ritchey.attendance.client.activity;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.place.shared.Place;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 import com.google.gwt.view.client.AsyncDataProvider;
 import com.google.gwt.view.client.HasData;
 import com.google.gwt.view.client.Range;
 import com.google.gwt.view.client.RangeChangeEvent;
 import com.ritchey.attendance.client.AttendanceServiceAsync;
 import com.ritchey.attendance.client.ClientFactory;
 import com.ritchey.attendance.client.event.StudentUpdateFinishEvent;
 import com.ritchey.attendance.client.event.StudentUpdateFinishEventHandler;
 import com.ritchey.attendance.client.place.AttendanceCommentPlace;
 import com.ritchey.attendance.client.place.StudentPlace;
 import com.ritchey.attendance.client.ui.StudentView;
 import com.ritchey.attendance.domain.powercampus.Attendance;
 import com.ritchey.attendance.domain.powercampus.Course;
 import com.ritchey.attendance.domain.powercampus.GradeValues;
 import com.ritchey.attendance.domain.powercampus.ListCount;
 import com.ritchey.attendance.domain.powercampus.People;
 import com.ritchey.attendance.domain.progress.Progress;
 import com.ritchey.attendance.shared.Log;
 
 public class StudentActivity extends BasePresenter implements StudentView.Presenter {
 	
 	public enum ButtonCodes {
 		PRESENT, ABSENT, TARDY
 	}
 
 	private ClientFactory clientFactory;
 	private StudentView display;
 	
 	private final AttendanceServiceAsync rpcService;
 	private final EventBus eventBus;
 
 	Date today = new Date();
 	DateTimeFormat dateFormat = DateTimeFormat.getFormat("yyyy-MM-dd"); 
 	private boolean unsetOnly = false;
 	final private AsyncDataProvider<People> provider;
 	
 	Course course;
 	final private AsyncDataProvider<People> providerProgress;
 	final Map counts = new HashMap();
 	
 	
 	public StudentActivity(ClientFactory clientFactory) {
 		this.clientFactory = clientFactory;	
 		this.display = clientFactory.getStudentView();
 		this.eventBus = clientFactory.getEventBus();
 		this.rpcService = clientFactory.getRpcService();
 		unsetOnly = false;
 		
 		
 		provider = new AsyncDataProvider<People>() {
 			@Override
 			protected void onRangeChanged(HasData<People> display) {
 				getStudentList(display, this);
 			}
 		};
 		
 		providerProgress = new AsyncDataProvider<People>() {
 			@Override
 			protected void onRangeChanged(HasData<People> display) {
 				getStudentProgressList(display, this);
 			}
 		};
 		
 		bind();
 	}
 
 	
 	@Override
 	public void start(AcceptsOneWidget panel, EventBus eventBus) {
 		waiting = null;
 		additionalUpdate = null;
 		Course course = ((StudentPlace)clientFactory.getPlaceController().getWhere()).getCourse();
 		getGradeValues(course);
 		this.display.setCourse(course);
 		
 		Log.debug("START");
 		panel.setWidget(display.asWidget());
 		display.setListener(this);
 	}
 	
 	public void bind() {
 		Log.debug("Start bind");
 
 		provider.addDataDisplay(display.getTblStudentList());
 		addHandler(eventBus.addHandler(StudentUpdateFinishEvent.TYPE,
 				new StudentUpdateFinishEventHandler() {
 					@Override
 					public void onRun(StudentUpdateFinishEvent event) {
 						provider.updateRowData(event.getIndex(), event.getPeople());
 					}
 				}));
 		
 		providerProgress.addDataDisplay(display.getTblProgressList());
 		addHandler(eventBus.addHandler(StudentUpdateFinishEvent.TYPE,
 				new StudentUpdateFinishEventHandler() {
 					@Override
 					public void onRun(StudentUpdateFinishEvent event) {
 						providerProgress.updateRowData(event.getIndex(), event.getPeople());
 					}
 				}));
 		
 		display.getTblProgressList().addRangeChangeHandler(new RangeChangeEvent.Handler() {
 
 			@Override
 			public void onRangeChange(RangeChangeEvent event) {
 				counts.put("progressRecount", false);
 			}
 		});
 
 		
 		ClickHandler handler = new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				Log.debug("RUN classlist FIRE");
 				StudentPlace place = ((StudentPlace)clientFactory.getPlaceController().getWhere());
 	
 				goTo(place.getParent());
 			}
 		};
 		addHandler(display.getBtnClassList().addClickHandler(handler));
 
 		
 		ClickHandler handlerPresent = new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				Log.debug("RUN AllPresent FIRE");
 				if (unsetOnly) {
 					unsetOnly = false;
 				}
 				HasData<People> hasData = (HasData<People>)display.getTblStudentList();
 				allPresent(hasData, provider);
 			}
 		};
 		addHandler(display.getBtnAllPresent().addClickHandler(handlerPresent));
 		
 		ClickHandler handlerUnset = new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				Log.debug("RUN unset FIRE");
 				unsetOnly = true;
 				HasData<People> hasData = (HasData<People>)display.getTblStudentList();
 				//hasData.setVisibleRange(0, 20);
 				getStudentList(hasData, provider);
 			}
 		};
 		addHandler(display.getBtnUnset().addClickHandler(handlerUnset));
 		
 		ClickHandler handlerQuery = new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				Log.debug("RUN requery FIRE");
 				unsetOnly = false;
 
 				getStudentList((HasData<People>)display.getTblStudentList(), provider);
 			}
 		};
 		addHandler(display.getBtnRequery().addClickHandler(handlerQuery));
 		
 		SelectionHandler handlerTab = new SelectionHandler() {
 
 			@Override
 			public void onSelection(SelectionEvent event) {
 				display.setTab();
 			}
 		};
 		addHandler(display.getTabBar().addSelectionHandler(handlerTab));
 		
 		ClickHandler athleteQuery = new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				getStudentProgressList((HasData<People>)display.getTblProgressList(), providerProgress);
 			}
 		};
 		addHandler(display.getChkAth().addClickHandler(athleteQuery));
 		//Course course = ((StudentPlace)clientFactory.getPlaceController().getWhere()).getCourse();
 		//display.setCourse(course);
 		
 		//display.getLblMeetingInfo().setText("For class " + course.getEvent() + " " + course.getSubtype() + " " + course.getSection() + " on " + dateFormat.format(today));
 	}
 
 	private void allPresent(final HasData<People> display,
 			final AsyncDataProvider<People> dp) {
 
 		final int start = display.getVisibleRange().getStart();
 		int length = display.getVisibleRange().getLength();
 		Log.debug("start = " + start);
 		AsyncCallback<Integer> callback = new AsyncCallback<Integer>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.debug("FAIL Range " + caught.getMessage());
 				Window.alert(caught.getMessage());
 				caught.printStackTrace();
 			}
 
 			@Override
 			public void onSuccess(Integer result) {
 				Log.debug("Updated " + result + " records");
 				getStudentList(display, dp);
 			}
 
 		};
 		Log.debug("Course = " + course);
 		rpcService.insertPresent(course, callback);
 	}
 
 	public void updateDatabase(final People person, Course course, String buttonCode, final int index, final CellTable<People> source) {
 		try {
 			Log.debug("Update database  person = " + person);
 			//Log.debug("UPdate database enter  person = " + person);
 			if (person.getAttendance() == null) {
 				person.setAttendance(new Attendance(person, course));
 			}
 			//Log.debug("UPdate database enter 2");
 			AsyncCallback<String> callback = new AsyncCallback<String>() {
 
 				@Override
 				public void onFailure(Throwable caught) {
 					Window.alert(caught.getMessage());
 					Log.debug("Update database failed");
 				}
 
 				@Override
 				public void onSuccess(String result) {
 					if (result == null) {
 						Window.alert("Nothing updated!");
 					}
 					else {
 						Log.debug("UPdate database SUCCESS");
 						List<People> people = new ArrayList<People>();
 						people.add(person);
 						person.getAttendance().setAttendanceStatusDescription(result);
 						person.getAttendance().setCreateDate(new Date());
 						provider.updateRowData(index, people);
 					}
 				}
 
 			};
 			//Log.debug("UPdate database enter 3");
 			rpcService.updateDatabase(person, buttonCode, callback);
 			//Log.debug("UPdate database enter 4");
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void getStudentList(HasData<People> display,
 			final AsyncDataProvider<People> dp) {
 		
 		final Range range = display.getVisibleRange();
 		display.setVisibleRangeAndClearData(range, false);
 		
 		String countName = getCountName("studentList", false, unsetOnly);
 		final Integer count = (countName != null)?(Integer) counts.get(countName):null;
 
 		////Log.debug("length = " + length);
 		Log.debug("start = " + range.getStart());
 		AsyncCallback<ListCount<People>> callback = new AsyncCallback<ListCount<People>>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.debug("FAIL Range " + caught.getMessage());
 				Window.alert(caught.getMessage());
 				caught.printStackTrace();
 			}
 
 			@Override
 			public void onSuccess(ListCount<People> result) {
 				if (count == null) {
 					Log.debug("Progress UPDATE ROW COUNT = " + result.getCount());
 					dp.updateRowCount(result.getCount(), true);
 				}
 				else {
 					dp.updateRowCount(count, true);
 				}
 				//Log.debug("count = " + result.getCount());
				Log.debug("result data = " + result.getData());
 				dp.updateRowData(range.getStart(), result.getData());
 				Log.debug("SUCCESS DONE");
 			}
 		};
 		
 		course = ((StudentPlace)clientFactory.getPlaceController().getWhere()).getCourse();
 		this.display.setCourse(course);
 		
 		rpcService.getPeopleForCourseList(course, range.getStart(), range.getLength(), count==null, unsetOnly, false,  
 				sessionId, courseId, callback);
 	}
 	
 	Integer sessionId;
 	Integer courseId;
 	
 	private void getStudentProgressList(HasData<People> display,
 			final AsyncDataProvider<People> dp) {
 		
 		final Range range = display.getVisibleRange();
 		display.setVisibleRangeAndClearData(range, false);
 		
 		final boolean athleteOnly = this.display.getChkAth().getValue();
 		String countName = getCountName("studentProgressList", athleteOnly, false);
 		final Integer count = (countName != null)?(Integer) counts.get(countName):null;
 
 		////Log.debug("length = " + length);
 		Log.debug("start = " + range.getStart());
 		AsyncCallback<ListCount<People>> callback = new AsyncCallback<ListCount<People>>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.debug("Progress FAIL Range " + caught.getMessage());
 				Window.alert(caught.getMessage());
 				caught.printStackTrace();
 			}
 
 			@Override
 			public void onSuccess(ListCount<People> result) {
				if (result == null) {
					Window.alert("no results available");
					dp.updateRowCount(0, true);
					return;
				}
				if (result.getData() == null) {
					Window.alert((String)result.getEnv().get("message"));
					dp.updateRowCount(0, true);
					return;
				}
 				if (count == null) {
 					Log.debug("Progress UPDATE ROW COUNT = " + result.getCount());
 					dp.updateRowCount(result.getCount(), true);
 				}
 				else {
 					dp.updateRowCount(count, true);
 				}
 				Map env = result.getEnv();
 				if (env != null) {
 					courseId = (Integer) env.get("courseId");
 					sessionId = (Integer) env.get("sessionId");
 				}
 				else {
 					courseId = null;
 					sessionId = null;
 				}
 
 				//Log.debug("count = " + result.getCount());
 				dp.updateRowData(range.getStart(), result.getData());
 				for (People x:result.getData()) {
 					Log.debug("x = " + x);
 				}
 				Log.debug("SUCCESS DONE");
 			}
 		};
 		
 		course = ((StudentPlace)clientFactory.getPlaceController().getWhere()).getCourse();
 		this.display.setCourse(course);
 		
 		rpcService.getPeopleForCourseList(course, range.getStart(), range.getLength(), count==null, unsetOnly, athleteOnly, sessionId, courseId,  
 				callback);
 	}
 	
 	public String getCountName(String baseName, boolean athleteOnly, boolean unsetOnly) {
 		if (unsetOnly) 
 			return null;
 		String countName = baseName;
 		if (athleteOnly) {
 			countName = countName + "Ath";
 		}
 		return countName;
 	}
 	
 	public void goToCommentPlace(AttendanceCommentPlace place) {
 		place.setParent(clientFactory.getPlaceController().getWhere());
 		goTo(place);
 	}
 
 	@Override
 	public void goTo(Place place) {
 		clientFactory.getPlaceController().goTo(place);
 	}
 	
 	public void getGradeValues(Course course) {
 		AsyncCallback<List<GradeValues>> callback = new AsyncCallback<List<GradeValues>>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.debug("Progress FAIL Range " + caught.getMessage());
 				Window.alert(caught.getMessage());
 				caught.printStackTrace();
 			}
 
 			@Override
 			public void onSuccess(List<GradeValues> result) {
 				display.setGrades(result);
 			}
 		};
 		rpcService.getGrades(course, callback);
 	}
 
 
 	Set waiting;
 	Map additionalUpdate;
 	@Override
 	public void updateProgress(final Progress progress) {
 		AsyncCallback<Integer> callback = new AsyncCallback<Integer>() {
 			@Override
 			public void onFailure(Throwable caught) {
 				Log.debug("Progress FAIL Range " + caught.getMessage());
 				Window.alert(caught.getMessage());
 				caught.printStackTrace();
 			}
 
 			@Override
 			public void onSuccess(Integer result) {
 				if (progress.getId() == null) {
 					Log.debug("progress updated ID is " + progress);
 					progress.setId(result);
 					waiting.remove(progress.getPerson());
 					if (additionalUpdate != null) {
 						Progress p = (Progress) additionalUpdate.get(progress.getPerson());
 						if (p != null) {
 							additionalUpdate.remove(progress.getPerson());
 							updateProgress(p);
 						}
 					}
 				}
 					
 			}
 		};
 		Log.debug("updateProgress waiting = " + waiting);
 		if (waiting == null) {
 			Log.debug("updateProgress create waiting ");
 			waiting = new HashSet();
 		}
 		if (waiting.contains(progress.getPerson())) {
 			if (additionalUpdate == null) {
 				additionalUpdate = new HashMap();
 			}
 			additionalUpdate.put(progress.getPerson(), progress);
 			return;
 		}
 		if (progress.getId() == null) {
 			waiting.add(progress.getPerson());
 		}
 		rpcService.updateProgress(progress, callback);
 	}
 
 
 	@Override
 	public Integer getCourseId() {
 		return courseId;
 	}
 
 }
