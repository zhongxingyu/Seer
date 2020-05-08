 package cz.admin24.myachievo.web2.calendar;
 
 import java.text.MessageFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.time.DateUtils;
 import org.apache.commons.lang3.time.FastDateFormat;
 import org.joda.time.DateTime;
 import org.joda.time.Minutes;
 
 import com.google.common.collect.ImmutableList;
 import com.vaadin.navigator.View;
 import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
 import com.vaadin.server.Page;
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Calendar;
 import com.vaadin.ui.CssLayout;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.NativeButton;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.TabSheet.SelectedTabChangeEvent;
 import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
 import com.vaadin.ui.TabSheet.Tab;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.components.calendar.CalendarComponentEvents.BackwardEvent;
 import com.vaadin.ui.components.calendar.CalendarComponentEvents.BackwardHandler;
 import com.vaadin.ui.components.calendar.CalendarComponentEvents.DateClickEvent;
 import com.vaadin.ui.components.calendar.CalendarComponentEvents.DateClickHandler;
 import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventClick;
 import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventClickHandler;
 import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventMoveHandler;
 import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventResize;
 import com.vaadin.ui.components.calendar.CalendarComponentEvents.EventResizeHandler;
 import com.vaadin.ui.components.calendar.CalendarComponentEvents.ForwardEvent;
 import com.vaadin.ui.components.calendar.CalendarComponentEvents.ForwardHandler;
 import com.vaadin.ui.components.calendar.CalendarComponentEvents.MoveEvent;
 import com.vaadin.ui.components.calendar.CalendarComponentEvents.RangeSelectEvent;
 import com.vaadin.ui.components.calendar.CalendarComponentEvents.RangeSelectHandler;
 import com.vaadin.ui.components.calendar.CalendarComponentEvents.WeekClick;
 import com.vaadin.ui.components.calendar.CalendarComponentEvents.WeekClickHandler;
 import com.vaadin.ui.components.calendar.event.CalendarEvent;
 
 import cz.admin24.myachievo.connector.http.dto.PhaseActivity;
 import cz.admin24.myachievo.connector.http.dto.Project;
 import cz.admin24.myachievo.connector.http.dto.ProjectPhase;
 import cz.admin24.myachievo.connector.http.dto.WorkReport;
 import cz.admin24.myachievo.web2.SpringUtils;
 import cz.admin24.myachievo.web2.calendar.CalendarUrl.CalendarViewType;
 import cz.admin24.myachievo.web2.calendar.detail.EventDetailsWindow;
 import cz.admin24.myachievo.web2.service.AchievoConnectorWrapper;
 import cz.admin24.myachievo.web2.service.ProjectsCache;
 import cz.admin24.myachievo.web2.utils.RemainingTime;
 import cz.admin24.myachievo.web2.utils.TimesheetConstants;
 import cz.admin24.myachievo.web2.utils.TimesheetUtils;
 
 //@Component
 //@Scope("prototype")
 //@VaadinView(CalendarView.NAME)
 public class CalendarView extends VerticalLayout implements View {
     private static final ImmutableList<String> HOLIDAY_REGION   = ImmutableList.of("cz");
     private static final long                  serialVersionUID = 1L;
     public static final String                 NAME             = "";
     private final FastDateFormat               dayFormat        = FastDateFormat.getDateInstance(FastDateFormat.SHORT, UI.getCurrent().getLocale());
     private final FastDateFormat               weekFormat       = FastDateFormat.getInstance("w", UI.getCurrent().getLocale());
     private final FastDateFormat               monthFormat      = FastDateFormat.getInstance("MMMM", UI.getCurrent().getLocale());
     private final CssLayout                    dailyTab         = new TypeTab("Daily");
     private final CssLayout                    weeklyTab        = new TypeTab("Weekly");
     private final CssLayout                    monthlyTab       = new TypeTab("Monthly");
     private final CssLayout                    todayTab         = new TypeTab("Today");
     private final CssLayout                    statisticsTab    = new TypeTab("5/40h");
     private final TabSheet                     tabSheet         = new TabSheet(todayTab, dailyTab, weeklyTab, monthlyTab, statisticsTab);
     private final Calendar                     calendar         = new Calendar() {
                                                                     public java.util.List<com.vaadin.ui.components.calendar.event.CalendarEvent> getEvents(Date startDate, Date endDate) {
                                                                         List<CalendarEvent> ret = super.getEvents(startDate, endDate);
                                                                         return ret;
                                                                     };
                                                                 };
     private final HorizontalLayout             buttonsLayout    = new HorizontalLayout();
     private final NativeButton                 nextBtn          = new NativeButton();
     private final NativeButton                 prevBtn          = new NativeButton();
     // private final Button todayBtn = new Button("Today");
     // private final Button dailyBtn = new Button("Daily");
     // private final Button weeklyBtn = new Button("Weekly");
     // private final Button monthlyBtn = new Button("Monthly");
 
     private AchievoConnectorWrapper            achievoConnector = SpringUtils.getBean(AchievoConnectorWrapper.class);
     private ProjectsCache                      projectsCache    = SpringUtils.getBean(ProjectsCache.class);
 
 
     // private Holidays holidays = SpringUtils.getBean(Holidays.class);
 
     // @Autowired
     // private Navigator navigator;
 
     public CalendarView() {
         buildLayout();
         configure();
         css();
         // DaoAuthenticationProvider
     }
 
 
     @Override
     public void attach() {
         super.attach();
         localize();
     }
 
 
     private void configure() {
 
         calendar.setHandler(new EventClickHandler() {
 
             @Override
             public void eventClick(EventClick event) {
                 WorkReportEvent calendarEvent = (WorkReportEvent) event.getCalendarEvent();
                 EventDetailsWindow eventDetailsWindow = new EventDetailsWindow(calendarEvent.getWorkReport()) {
                     @Override
                     protected void onEventChanged() {
                         calendar.markAsDirty();
                     }
                 };
                 CalendarView.this.getUI().addWindow(eventDetailsWindow);
             }
         });
 
         calendar.setHandler(new EventResizeHandler() {
 
             @Override
             public void eventResize(EventResize event) {
                 WorkReportEvent wre = (WorkReportEvent) event.getCalendarEvent();
                 WorkReport r = wre.getWorkReport();
 
                 DateTime start = new DateTime(event.getNewStart());
                 DateTime newEnd = new DateTime(event.getNewEnd());
                 int minutesBetween = Minutes.minutesBetween(start, newEnd).getMinutes();
                 minutesBetween = (int) Math.round(minutesBetween / 15.0) * 15;
                 Integer hours = minutesBetween / 60;
                 Integer minutes = minutesBetween % 60;
 
                 Project project = projectsCache.getProjectByName(r.getProject());
                 ProjectPhase phase = projectsCache.getPhaseByName(r.getPhase(), project);
                 PhaseActivity activity = projectsCache.getActivityByName(r.getActivity(), phase, project);
 
                 achievoConnector.updateRegiteredHours(r.getId(), start.toDate(), hours, minutes, project.getId(), phase.getId(), activity.getId(), r.getRemark());
                 calendar.markAsDirty();
             }
         });
 
         calendar.setHandler(new EventMoveHandler() {
 
             @Override
             public void eventMove(MoveEvent event) {
                 WorkReportEvent wre = (WorkReportEvent) event.getCalendarEvent();
                 WorkReport r = wre.getWorkReport();
 
                 Date newStart = event.getNewStart();
                 if (DateUtils.isSameDay(r.getDate(), newStart)) {
                     // NOP
                     return;
                 }
 
                 Project project = projectsCache.getProjectByName(r.getProject());
                 ProjectPhase phase = projectsCache.getPhaseByName(r.getPhase(), project);
                 PhaseActivity activity = projectsCache.getActivityByName(r.getActivity(), phase, project);
 
                 achievoConnector.updateRegiteredHours(r.getId(), newStart, r.getHours(), r.getMinutes(), project.getId(), phase.getId(), activity.getId(), r.getRemark());
                 calendar.markAsDirty();
             }
         });
 
         calendar.setHandler(new RangeSelectHandler() {
 
             @Override
             public void rangeSelect(RangeSelectEvent event) {
 
                 DateTime start = new DateTime(event.getStart());
                 DateTime newEnd = new DateTime(event.getEnd());
                 int minutesBetween = Minutes.minutesBetween(start, newEnd).getMinutes();
                 minutesBetween = (int) Math.round(minutesBetween / 15.0) * 15;
                 Integer hours = minutesBetween / 60;
                 Integer minutes = minutesBetween % 60;
 
                if (hours > 9) {
                    hours = 9;
                    minutes = 45;
                }

                 WorkReport workReport = new WorkReport(null, start.toDate(), null, null, null, null, hours, minutes);
 
                 EventDetailsWindow eventDetailsWindow = new EventDetailsWindow(workReport) {
                     @Override
                     protected void onEventChanged() {
                         calendar.markAsDirty();
                     }
                 };
                 getUI().addWindow(eventDetailsWindow);
 
             }
         });
 
         calendar.setHandler(new BackwardHandler() {
 
             @Override
             public void backward(BackwardEvent event) {
                 CalendarUrl url = getUrl();
                 url.prev();
                 navigateTo(url);
             }
         });
 
         calendar.setHandler(new ForwardHandler() {
 
             @Override
             public void forward(ForwardEvent event) {
                 CalendarUrl url = getUrl();
                 url.next();
                 navigateTo(url);
             }
         });
 
         calendar.setHandler(new WeekClickHandler() {
 
             @Override
             public void weekClick(WeekClick event) {
                 java.util.Calendar c = java.util.Calendar.getInstance(UI.getCurrent().getLocale());
                 c.set(java.util.Calendar.YEAR, event.getYear());
                 c.set(java.util.Calendar.WEEK_OF_YEAR, event.getWeek());
                 CalendarUrl url = getUrl();
                 url.setDate(c.getTime());
                 url.setType(CalendarViewType.WEEK);
                 navigateTo(url);
             }
         });
 
         calendar.setHandler(new DateClickHandler() {
 
             @Override
             public void dateClick(DateClickEvent event) {
                 CalendarUrl url = getUrl();
                 url.setDate(event.getDate());
                 url.setType(CalendarViewType.DAY);
                 navigateTo(url);
             }
         });
 
         prevBtn.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 CalendarUrl url = getUrl();
                 url.prev();
                 navigateTo(url);
             }
         });
 
         nextBtn.addClickListener(new ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 CalendarUrl url = getUrl();
                 url.next();
                 navigateTo(url);
             }
         });
 
         tabSheet.addSelectedTabChangeListener(new SelectedTabChangeListener() {
 
             @Override
             public void selectedTabChange(SelectedTabChangeEvent event) {
                 com.vaadin.ui.Component tab = tabSheet.getSelectedTab();
                 CalendarUrl url = getUrl();
 
                 if (todayTab == tab) {
                     url.setDate(new Date());
                     // url.setType(CalendarViewType.DAY);
                 }
 
                 if (dailyTab == tab) {
                     url.setType(CalendarViewType.DAY);
                 }
 
                 if (weeklyTab == tab) {
                     url.setType(CalendarViewType.WEEK);
                 }
 
                 if (monthlyTab == tab) {
                     url.setType(CalendarViewType.MONTH);
                 }
 
                 navigateTo(url);
             }
         });
 
         // dailyBtn.addClickListener(new ClickListener() {
         //
         // @Override
         // public void buttonClick(ClickEvent event) {
         // CalendarUrl url = getUrl();
         // url.setType(CalendarViewType.DAY);
         // navigateTo(url);
         // }
         // });
         //
         // weeklyBtn.addClickListener(new ClickListener() {
         //
         // @Override
         // public void buttonClick(ClickEvent event) {
         // CalendarUrl url = getUrl();
         // url.setType(CalendarViewType.WEEK);
         // navigateTo(url);
         // }
         // });
         //
         // monthlyBtn.addClickListener(new ClickListener() {
         //
         // @Override
         // public void buttonClick(ClickEvent event) {
         // CalendarUrl url = getUrl();
         // url.setType(CalendarViewType.MONTH);
         // navigateTo(url);
         // }
         // });
 
         // todayBtn.addClickListener(new ClickListener() {
         //
         // @Override
         // public void buttonClick(ClickEvent event) {
         // CalendarUrl url = getUrl();
         // url.setDate(new Date());
         // navigateTo(url);
         // }
         // });
 
         // calendar.set
     }
 
 
     private void localize() {
         // WebBrowser browser = Page.getCurrent().getWebBrowser();
         // int rawTimezoneOffset = browser.getTimezoneOffset();
         // String[] possibleTimeZones = TimeZone.getAvailableIDs(rawTimezoneOffset);
         // if (!ArrayUtils.isEmpty(possibleTimeZones)) {
         // calendar.setTimeZone(TimeZone.getTimeZone(possibleTimeZones[0]));
         // }
 
     }
 
 
     private void css() {
         setSizeFull();
         // addStyleName("schedule");
 
         setExpandRatio(calendar, 1);
         buttonsLayout.setExpandRatio(tabSheet, 1);
 
         calendar.setSizeFull();
         tabSheet.setWidth("100%");
         buttonsLayout.setWidth("100%");
 
         setMargin(true);
         setSpacing(true);
         buttonsLayout.setSpacing(true);
 
         buttonsLayout.setComponentAlignment(prevBtn, Alignment.MIDDLE_LEFT);
         // buttonsLayout.setComponentAlignment(todayBtn, Alignment.MIDDLE_CENTER);
         // buttonsLayout.setComponentAlignment(dailyBtn, Alignment.MIDDLE_CENTER);
         // buttonsLayout.setComponentAlignment(weeklyBtn, Alignment.MIDDLE_CENTER);
         // buttonsLayout.setComponentAlignment(monthlyBtn, Alignment.MIDDLE_CENTER);
         buttonsLayout.setComponentAlignment(nextBtn, Alignment.MIDDLE_RIGHT);
 
         prevBtn.addStyleName("prev-next-btn");
         nextBtn.addStyleName("prev-next-btn");
         prevBtn.addStyleName("prev-btn");
         nextBtn.addStyleName("next-btn");
 
     }
 
 
     private void buildLayout() {
         // addComponent(tabSheet);
         addComponent(buttonsLayout);
         addComponent(calendar);
 
         buttonsLayout.addComponent(prevBtn);
         buttonsLayout.addComponent(tabSheet);
         buttonsLayout.addComponent(nextBtn);
 
         // buttonsLayout.addComponent(monthlyBtn);
         // buttonsLayout.addComponent(weeklyBtn);
         // buttonsLayout.addComponent(dailyBtn);
         // buttonsLayout.addComponent(todayBtn);
     }
 
 
     @Override
     public void enter(ViewChangeEvent event) {
         calendar.setEventProvider(new AchievoEventProvider(achievoConnector) {
             @Override
             public List<CalendarEvent> getEvents(Date startDate, Date endDate) {
                 List<CalendarEvent> events = super.getEvents(startDate, endDate);
                 onEventsLoaded((List) events, startDate, endDate);
                 return events;
             }
         });
         String parameters = event.getParameters();
         CalendarUrl url = new CalendarUrl(parameters);
         if (StringUtils.isBlank(parameters)) {
             getUI().getNavigator().navigateTo(new CalendarUrl().toFragment());
             return;
         }
 
         calendar.setStartDate(url.getStartDate());
         calendar.setEndDate(url.getEndDate());
 
         switch (url.getType()) {
         case DAY:
             if (DateUtils.isSameDay(url.getStartDate(), new Date())) {
                 tabSheet.setSelectedTab(todayTab);
             } else {
                 tabSheet.setSelectedTab(dailyTab);
             }
             break;
         case MONTH:
             tabSheet.setSelectedTab(monthlyTab);
             break;
         case WEEK:
             tabSheet.setSelectedTab(weeklyTab);
             break;
         }
 
         Date date = url.getDate();
         tabSheet.getTab(dailyTab).setCaption("Daily (" + dayFormat.format(date) + ")");
         tabSheet.getTab(weeklyTab).setCaption("Weekly (" + weekFormat.format(date) + ")");
         tabSheet.getTab(monthlyTab).setCaption("Monthly (" + monthFormat.format(date) + ")");
         refresh();
     }
 
 
     private void onEventsLoaded(List<WorkReportEvent> events, Date startDate, Date endDate) {
         Integer expectedMinutes;
         CalendarUrl url = getUrl();
         switch (url.getType()) {
         case MONTH:
             // trim on single month
             Date date = url.getDate();
             startDate = DateUtils.truncate(date, java.util.Calendar.MONTH);
             endDate = DateUtils.addMilliseconds(DateUtils.addMonths(startDate, 1), -1);
             break;
         case DAY:
         case WEEK:
         default:
             // NOP
             break;
         }
         java.util.Calendar today = java.util.Calendar.getInstance();
         expectedMinutes = 0;
         java.util.Calendar c = java.util.Calendar.getInstance();
         c.setTime(startDate);
 
         // Holidays holidays = new Holidays();
 
         do {
             // don't register time in future
             if (c.after(today)) {
                 break;
             }
             // skip holidays
             // try {
             // if (!holidays.on(c.getTime(), HOLIDAY_REGION, Holidays.NO_OPTION).isEmpty()) {
             // break;
             // }
             // } catch (HolidayException e) {
             // throw new IllegalStateException(e);
             // } catch (UnknownRegionException e) {
             // throw new IllegalStateException(e);
             // }
 
             int dayOfWeek = c.get(java.util.Calendar.DAY_OF_WEEK);
             if (dayOfWeek != java.util.Calendar.SATURDAY && dayOfWeek != java.util.Calendar.SUNDAY) {
                 expectedMinutes += TimesheetConstants.CONTRACT_MINUTES;
             }
             c.add(java.util.Calendar.DAY_OF_YEAR, 1);
         } while (c.getTime().before(endDate));
 
         RemainingTime remainingTime = TimesheetUtils.countRemainingTime(events, expectedMinutes, startDate, endDate);
 
         Tab tab = tabSheet.getTab(statisticsTab);
         tab.setCaption(MessageFormat.format("{0}h {1}min left", remainingTime.getHours(), remainingTime.getMinutes()));
 
         if (remainingTime.isPositive()) {
             tab.setStyleName("not-enough-hours");
         } else {
             tab.setStyleName("enough-hours");
         }
     }
 
 
     private void refresh() {
 
     }
 
 
     private CalendarUrl getUrl() {
         return new CalendarUrl(Page.getCurrent());
     }
 
 
     private void navigateTo(CalendarUrl url) {
         UI.getCurrent().getNavigator().navigateTo(url.toFragment());
     }
 }
