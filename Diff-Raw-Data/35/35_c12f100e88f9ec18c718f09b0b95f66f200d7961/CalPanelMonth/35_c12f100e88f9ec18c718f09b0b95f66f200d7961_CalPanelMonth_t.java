 package at.ac.tuwien.sepm.ui.calender.cal;
 
 import at.ac.tuwien.sepm.service.CalService;
 import at.ac.tuwien.sepm.service.DateService;
 import at.ac.tuwien.sepm.service.LVAService;
 import at.ac.tuwien.sepm.service.ServiceException;
 //import at.ac.tuwien.sepm.ui.StandardInsidePanel;
 import at.ac.tuwien.sepm.ui.UI;
 import net.miginfocom.swing.MigLayout;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeFieldType;
 import org.springframework.beans.factory.annotation.Autowired;
 
 
 import java.util.Locale;
 
 /**
  * @author Markus MUTH
  */
 
 @UI
 public class CalPanelMonth extends CalAbstractView implements CalendarInterface {
     private MigLayout layout;
     private int preMonthDays;
     private static final Logger logger = Logger.getLogger(CalPanelMonth.class);
 
 
     private Logger log = LogManager.getLogger(this.getClass().getSimpleName());
 
     @Autowired
     public CalPanelMonth(CalService service,LVAService lvaService, DateService dateService) {
         super(5,false,service,lvaService, dateService);
         super.firstDay = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), 1, 0, 0, 0, 0);
         layout = new MigLayout("", "1[]1[]1[]1", "1[]");
         loadFonts();
         setSize((int)whiteSpaceCalendar.getWidth(),(int)whiteSpaceCalendar.getHeight());
         setLocation(CalStartCoordinateOfWhiteSpace);
         this.setLayout(layout);
         this.setVisible(true);
 
         /*
         initPanel();
         try {
             setDates();
         } catch (ServiceException e) {
             e.printStackTrace();
         }
 
         this.repaint();
         this.revalidate();
         */
     }
 
     public void init() {
         initPanel();
         try {
             setDates();
         } catch (ServiceException e) {
             e.printStackTrace();
         }
 
         this.repaint();
         this.revalidate();
     }
 
     public void initPanel () {
         maxDateLabels = 5;
         initWeekNames((int)whiteSpaceCalendar.getWidth(), (int)whiteSpaceCalendar.getHeight());
         initDayPanels();
         setDays();
     }
 
     protected void setDays() {
         int actStart, actStop, preStart, preStop, postStart, postStop, preMonthYear, preMonthMonth, postMonthYear, postMonthMonth;
 
         if(firstDay.getMonthOfYear()==1) {
             preMonthMonth = 12;
             preMonthYear = firstDay.getYear()-1;
             postMonthMonth = 2;
             postMonthYear = firstDay.getYear();
         } else if (firstDay.getMonthOfYear()==12) {
             preMonthMonth = 11;
             preMonthYear = firstDay.getYear();
             postMonthMonth = 1;
             postMonthYear = firstDay.getYear()+1;
         } else {
             preMonthMonth = firstDay.getMonthOfYear()-1;
             preMonthYear = firstDay.getYear();
             postMonthMonth = firstDay.getMonthOfYear()+1;
             postMonthYear = firstDay.getYear();
         }
 
         String actFirstDayName = firstDay.toString("E", Locale.US);
 
         preMonthDays = 0;
         if(actFirstDayName.toUpperCase().equals("TUE")) {
             preMonthDays = 1;
         } else if (actFirstDayName.toUpperCase().equals("WED")) {
             preMonthDays = 2;
         } else if (actFirstDayName.toUpperCase().equals("THU")) {
             preMonthDays = 3;
         } else if (actFirstDayName.toUpperCase().equals("FRI")) {
             preMonthDays = 4;
         } else if (actFirstDayName.toUpperCase().equals("SAT")) {
             preMonthDays = 5;
         } else if (actFirstDayName.toUpperCase().equals("SUN")) {
             preMonthDays = 6;
         }
 
         actStart = 1;
         actStop = firstDay.dayOfMonth().withMaximumValue().get(DateTimeFieldType.dayOfMonth());
         if(actStop + preMonthDays > AMOUNT_DAYS_SHOWN) {
             actStop = AMOUNT_DAYS_SHOWN - preMonthDays;
         }
 
         preStart = (new DateTime(preMonthYear, preMonthMonth, 1, 0, 0, 0)).dayOfMonth().withMaximumValue().get(DateTimeFieldType.dayOfMonth())-preMonthDays+1;
         preStop = (new DateTime(preMonthYear, preMonthMonth, 1, 0, 0, 0)).dayOfMonth().withMaximumValue().get(DateTimeFieldType.dayOfMonth());
 
         postStart = 0;
         postStop = 0;
         if((actStop + preMonthDays) < AMOUNT_DAYS_SHOWN) {
             postStart = (new DateTime(postMonthYear, postMonthMonth, 1, 0, 0, 0)).dayOfMonth().withMinimumValue().get(DateTimeFieldType.dayOfMonth());
             postStop = AMOUNT_DAYS_SHOWN - actStop - preMonthDays;
         }
 
         int a = 0;
         for(int i=preStart; i<=preStop; i++) {
             days[a].setDate(new DateTime(preMonthYear, preMonthMonth, i, 0, 0, 0, 0));
             days[a].setBackground(COLOR_OF_NOT_CURRENT_MONTH);
            days[a].setCurrent(false);
             a++;
         }
 
         for(int i=actStart; i<=actStop; i++) {
             days[a].setDate(new DateTime(firstDay.getYear(), firstDay.getMonthOfYear(), i, 0, 0, 0, 0));
             days[a].setBackground(COLOR_OF_CURRENT_MONTH);
            days[a].setCurrent(true);
             a++;
         }
 
         if((actStop + preMonthDays) < AMOUNT_DAYS_SHOWN) {
             for(int i=postStart; i<=postStop; i++) {
                 days[a].setDate(new DateTime(postMonthYear, postMonthMonth, i, 0, 0, 0, 0));
                 days[a].setBackground(COLOR_OF_NOT_CURRENT_MONTH);
                days[a].setCurrent(false);
                 a++;
             }
         }
     }
 
     @Override
     public void refresh() {
         try {
             setDates();
         } catch (ServiceException e) {
             logger.error(e);
         }
     }
 
     @Override
     public void goToDay(DateTime date) {
         // TODO
     }
 
     public void semester() {
         //todo für die jcombobox in der man das semester auswählen kann in calendarpanel
     }
 
     public void next() throws ServiceException {
         if (firstDay.getMonthOfYear()==12) {
             this.firstDay = new DateTime(firstDay.getYear()+1, 1, 1, 0, 0, 0, 0);
         } else {
             this.firstDay = new DateTime(firstDay.getYear(), firstDay.getMonthOfYear()+1, 1, 0, 0, 0, 0);
         }
         setDays();
         setDates();
         repaint();
         revalidate();
     }
 
     public void last() throws ServiceException {
         if(firstDay.getMonthOfYear()==1) {
             this.firstDay = new DateTime(firstDay.getYear()-1, 12, 1, 0, 0, 0, 0);
         } else {
             this.firstDay = new DateTime(firstDay.getYear(), firstDay.getMonthOfYear()-1, 1, 0, 0, 0, 0);
         }
         setDays();
         setDates();
         repaint();
         revalidate();
     }
 
     @Override
     public String getTimeIntervalInfo() {
         return firstDay.monthOfYear().getAsText(Locale.GERMANY) + " " + firstDay.getYear();
     }
 }
