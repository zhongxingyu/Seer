 package at.ac.tuwien.sepm.ui.calender.cal;
 
 import at.ac.tuwien.sepm.entity.Date;
 import at.ac.tuwien.sepm.entity.DateEntity;
 import at.ac.tuwien.sepm.entity.LvaDate;
 import at.ac.tuwien.sepm.service.CalService;
 import at.ac.tuwien.sepm.service.DateService;
 import at.ac.tuwien.sepm.service.LVAService;
 import at.ac.tuwien.sepm.service.ServiceException;
 import at.ac.tuwien.sepm.ui.StandardInsidePanel;
 //import net.miginfocom.swing.MigLayout;
 import at.ac.tuwien.sepm.ui.UI;
 import org.joda.time.DateTime;
 
 import javax.swing.*;
 import java.awt.*;
 //import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * @author Markus MUTH
  */
 @UI
 public abstract class CalAbstractView extends StandardInsidePanel {
 
     private CalService calService;
     private LVAService lvaService;
     private DateService dateService;
 
     // layout config ------------------------------------------------------------------------------------------------ //
 
     protected Color COLOR_OF_ACTUAL_MONTH = new Color(255, 255, 255);           // color of day panles which represent a date in the actual month
     protected Color COLOR_OF_NOT_ACTUAL_MONTH = new Color(220, 220, 220);       // color of day panles which represent a date in an not actual month
     protected Color dayNameLabelForegroundColor = new Color(0, 0, 0);           // font color of the weekday name jlabels
     protected Color dayNameLabelBackgroundColor = new Color(255, 255, 255);     // background color of the weekday name jlabels
     //protected Color backgroundColor = UIManager.getColor("Panel.background");   // the color of the lines between the day panels
 
     protected Dimension dayPanelDimension;      // is set at begin of init()
     protected Dimension dayNameLabelDimension;  // is set at begin of initWeekNames()
     protected int dayNameLabelHeight = 17;      // height of the weekday name jlabels
 
     //protected MigLayout layout = new MigLayout("", "1[]1[]1[]1", "1[]");    // line width of the lines between the day panels
 
     // -------------------------------------------------------------------------------------------------------------- //
 
     protected int WEEKS;                                                        // the amount of rows in the month view view
     protected int AMOUNT_DAYS_SHOWN;                                            // the amount of days in the month view
     protected DayPanel[] days;                                               // the array of all day panels
     protected JLabel[] dayNames = new JLabel[7];                                // the day labels
     protected DateTime firstDay;                                                // the first day of the actual view
     protected int maxDateLabels;                                                // the amount of days shown of the previous month
     protected boolean showTime = false;
 
     // -------------------------------------------------------------------------------------------------------------- //
 
     public CalAbstractView(int weeks, boolean showTime, CalService calService, LVAService lvaService, DateService dateService) {
         this.calService = calService;
         this.lvaService=lvaService;
         this.dateService=dateService;
         WEEKS = weeks;
         this.showTime=showTime;
         AMOUNT_DAYS_SHOWN = 7 * weeks;
         days = new DayPanel[AMOUNT_DAYS_SHOWN];
     }
 
     protected abstract void initPanel() throws ServiceException;
 
     protected void initWeekNames (int width, int height) {
         dayNameLabelDimension = new Dimension((width-8)/7, dayNameLabelHeight);
         dayPanelDimension = new Dimension((width-8)/7, (height-dayNameLabelHeight-WEEKS-1)/WEEKS);
 
         for(int i=0; i<dayNames.length; i++) {
             dayNames[i] = new JLabel();
             dayNames[i].setMinimumSize(dayNameLabelDimension);
             dayNames[i].setForeground(dayNameLabelForegroundColor);
             dayNames[i].setOpaque(true);
             dayNames[i].setBackground(dayNameLabelBackgroundColor);
             dayNames[i].setVerticalAlignment(JLabel.CENTER);
             dayNames[i].setHorizontalAlignment(JLabel.CENTER);
         }
 
         dayNames[0].setText("Montag");
         dayNames[1].setText("Dienstag");
         dayNames[2].setText("Mittwoch");
         dayNames[3].setText("Donnerstag");
         dayNames[4].setText("Freitag");
         dayNames[5].setText("Samstag");
         dayNames[6].setText("Sonntag");
 
         this.add(dayNames[0]);
         this.add(dayNames[1]);
         this.add(dayNames[2]);
         this.add(dayNames[3]);
         this.add(dayNames[4]);
         this.add(dayNames[5]);
         this.add(dayNames[6], "wrap");
     }
 
     protected void initDayPanels() {
         for(int y=0; y<days.length; y=y+7) {
             for(int x=0; x<7-1; x++) {
                days[x+y] = new DayPanel(maxDateLabels, dateService, showTime);
                 days[x+y].setMinimumSize(dayPanelDimension);
                 this.add(days[x + y]);
             }
            days[y+6] = new DayPanel(maxDateLabels, dateService, showTime);
             days[y+6].setMinimumSize(dayPanelDimension);
             this.add(days[y + 6], "wrap");
         }
     }
 
     /**
      * Display all dates.
      * @throws ServiceException If the dates could not be read.
      */
     public void setDates () throws ServiceException {
         //CalServiceImpl s = new CalServiceImpl();
 
         for (DayPanel day : days) {
             List<LvaDate> l1 = calService.getLVADatesByDateInStudyProgress(day.getDate());
             List<DateEntity> l2 = calService.getAllNotLVADatesAt(day.getDate());
             LinkedList<DateLabel> r = new LinkedList<DateLabel>();
             for (Date d : l1) {
                 r.addLast(new DateLabel(d, showTime));
             }
             for (Date d : l2) {
                 r.addLast(new DateLabel(d, showTime));
             }
             day.setDateLabels(r);
             day.refreshDates();
         }
     }
     /*
     public void addDate(Date date) {
         for(DayPanel c : days) {
             if(c.getDate().getDayOfMonth()==date.getTime().from().getDayOfMonth() &&
                     c.getDate().getMonthOfYear()==date.getTime().from().getMonthOfYear() &&
                     c.getDate().getYear()==date.getTime().from().getYear()) {
                 int i = 0;
                 for(DateLabel l : c.getDates()) {
                     if(date.getTime().before(l.getTime()) || l.getTime().equals(date.getTime())) {
                         c.getDates().add(i, new DateLabel(date));
                     }
                 }
 
             }
         }
     }
     */
 }
