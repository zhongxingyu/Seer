 package ru.eqlbin.gwt.datepicker.client;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.i18n.client.LocaleInfo;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.i18n.shared.DateTimeFormat;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.datepicker.client.MonthSelector;
 
 /**
  * Extends 
  * {@link com.google.gwt.user.datepicker.client.MonthSelector MonthSelector}
  * from GWT library, which is set as component by default 
  * for {@link com.google.gwt.user.datepicker.client.DatePicker DatePicker}. 
  * 
  * Allows to select the current month of the
  * {@link com.google.gwt.user.datepicker.client.DatePicker DatePicker},
  * using the drop-down list 
  * {@link com.google.gwt.user.client.ui.ListBox ListBox}
  * for setting the month and year.
  * 
  * @author eqlbin
  *
  */
 public class ListBoxMonthSelector extends MonthSelector {
 
     /**
      * Type of the year range for for {@link ListBoxMonthSelector}.<br><br>
      * 
      * <b>Fixed</b>    - fixed range<br>
      * <b>Floating</b> - floating range, which may vary 
      *                   according to the selected year
      */
     public static enum YearsRangeType {Fixed, Floating};
     // current type of the years range
     private YearsRangeType yearsRangeType;
 
     private static final DateTimeFormat monthFormat = 
                              DateTimeFormat.getFormat("yyyy-MMM");
     private static final DateTimeFormat yearFormat = 
                              DateTimeFormat.getFormat("yyyy");
 
     private String[] monthNames; // list of month names
     private String[] years;      // list of years available for selection
 
     private final ListBox yearsBox = new ListBox(false);
     private final ListBox monthsBox = new ListBox(false);
 
     // current shifts for floating range
     private int negativeYearShift = -1, positiveYearShift = -1;
 
     private Grid grid;       
     
     
     @Override
     protected void setup() {
         initYearsBox();
         initMonthsBox();
         initMonthSelector(); 
         setYearsRange(-7, 7, YearsRangeType.Floating);
     }
 
     @Override
     protected void refresh() {
         setListBoxesByModel();
     }
 
     /**
      * Initializes the {@link #yearsBox}
      */
     private void initYearsBox() {
         yearsBox.setVisibleItemCount(1);
         yearsBox.addChangeHandler(new ChangeHandler() {
             @Override
             public void onChange(ChangeEvent event) {
                 if(yearsRangeType == YearsRangeType.Floating) {
                     buildYearsByShifts(Integer.parseInt(getSelectedYear()), 
                                        negativeYearShift, positiveYearShift);
                     updateYearsBox();
                 }
                 setModelByListBoxes();
             }
         });
     }
 
     /**
      * Initializes the {@link #monthsBox}
      */
     private void initMonthsBox() {
         // localized short month names
         monthNames = LocaleInfo.getCurrentLocale().
                         getDateTimeFormatInfo().monthsShortStandalone();
         for (String month : monthNames) {
             monthsBox.addItem(month);
         }
         
         monthsBox.setVisibleItemCount(1);
         monthsBox.addChangeHandler(new ChangeHandler() {
             @Override
             public void onChange(ChangeEvent event) {
                 setModelByListBoxes();
             }
         });
     }
 
     /**
      * Initializes the {@link ListBoxDatePicker}.
      */
     private void initMonthSelector(){
         grid = new Grid(1, 2);
         grid.setWidget(0, 0, yearsBox);
         grid.setWidget(0, 1, monthsBox);
         grid.setStyleName("ListBoxMonthSelector");
     
         initWidget(grid);
     }
         
     /**
      * Sets the range of years available for selection 
      * in the date picker, and its behavior. 
      * 
      * @param first - the value of the first year on the list, 
      *                if the type of the range is fixed, 
      *                or shift to the left of the currently 
      *                selected year if the range is floating;
      *                for a fixed range value must be > 0 and 
      *                <= last, but < 0 if range is floating
      *                
      * @param last -  the value of the last year on the list, 
      *                if the type of the range is fixed,  
      *                or shift to the right of the currently 
      *                selected year if the range is floating;
      *                for a fixed range value must be > 0 and 
      *                >= first, but > 0 if range is floating
      *                 
      * @param type -  range type that defines its behavior
      * 
      * @see YearsRangeType
      */
     protected void setYearsRange(int first, int last, YearsRangeType type){
         yearsRangeType = type;
         
         switch (type) {
         
         case Fixed:
             
             this.negativeYearShift = -1;
             this.positiveYearShift = -1;
             
             buildYears(first, last);
     
             break;
             
         case Floating:
             // various signs for left and right shifts 
             // are needed for additional verification
             if (first >= 0) throw 
                 new IllegalArgumentException("First year shift value must be < 0");
             if (last <= 0) throw 
                 new IllegalArgumentException("Last year shift value must be > 0");
             
             this.negativeYearShift = Math.abs(first);
             this.positiveYearShift = last;
     
             int currentYear = Integer.parseInt(getCurrentModelYear());
             buildYearsByShifts(currentYear, negativeYearShift, positiveYearShift);
     
             break;
         }
     
         updateYearsBox();
         setModelByListBoxes();
     }
 
     /**
      * Rebuilds the component {@link #yearsBox}
      * according with the current value of the 
      * field {@link #years}
      */
     private void updateYearsBox() {
         
         // save the selected year
         String selectedYear = getSelectedYear();
         boolean selectedFound = false;
 
         this.yearsBox.clear();
         for (int i=0; i<this.years.length; i++) {
             String year = this.years[i];
             this.yearsBox.addItem(year);
             
             if(year.equals(selectedYear)) {
                 this.yearsBox.setSelectedIndex(i);
                 selectedFound = true;
             }
         }
         
         if(selectedFound) return;
         
         // select the middle item if the previous selected year is not found in yearsBox
         this.yearsBox.setSelectedIndex(this.yearsBox.getItemCount()/2);
     }
             
     /**         
      * Generates and stores in a field {@link #years}
      * the list of years. Based on shifts relative to the base year.
      * <br><br>
      * For example, to create a list of years from 1990 to 2005, inclusive:
      * <br><br>
      * {@code buildYearsByShifts(2000, 10, 5);}
      * 
      * @param baseYear - base year; must be > 0.
      * 
      * @param negativeShiftYear - shift to the left relative to the base year;
      *                            must be > 0.
      * @param positiveShiftYear - shift to the right relative to the base year; 
      *                            must be > 0.
      */
     private void buildYearsByShifts(int baseYear, 
             int negativeShiftYear, int positiveShiftYear){
         
         if (baseYear < 0 || negativeShiftYear < 0 || positiveShiftYear < 0)
             throw new IllegalArgumentException("All arguments must be positive values");
         
         // cut off the possible negative years, 
         // recalculating the negativeShiftYear
         if(baseYear <= negativeShiftYear)
             negativeShiftYear = negativeShiftYear - (negativeShiftYear - baseYear) - 1;
 
         int yearsCount = positiveShiftYear + negativeShiftYear + 1;
         int firstYear = baseYear - negativeShiftYear;
     
         this.years = new String[yearsCount];
 
         for (int i = firstYear; i < firstYear + yearsCount; i++) {
                 this.years[i - firstYear] = yearToString(i);
         } 
     }
 
     /**
      * Generates and stores in a field {@link #years}
      * a list of years from first to last, inclusive
      * 
      * @param first - first year in the list
      * @param last  - last year in the list
      */
     private void buildYears(int first, int last){
         
         if (last < first) throw 
             new IllegalArgumentException("Last year must be >= first year");
         if (first < 1 || last < 1) throw 
             new IllegalArgumentException("Years must be > 0");
 
         this.years = new String[last - first + 1];
 
         for (int i = first; i <= last; i++) {
                 this.years[i - first] = yearToString(i);
         }
     }
 
     /**
      * Sets the values ​​in the components {@link #yearsBox}
      * and {@link #monthsBox}, which correspond to the current month of the 
      * {@link com.google.gwt.user.datepicker.client.CalendarModel CalendarModel}
      */
    private void setListBoxesByModel() { 

         String currentMonth = monthFormat.
                                 format(getModel().getCurrentMonth());
         
         String[] yearAndMonth = currentMonth.split("-");
     
        
         boolean yearSetted = false;
         boolean monthSetted = false;
         
         for (int i = 0; i < yearsBox.getItemCount(); i++) {
             if (yearsBox.getItemText(i).equals(yearAndMonth[0])) {
                 yearsBox.setSelectedIndex(i);
                 yearSetted = true;
                 break;
             }
         }
     
         for (int i = 0; i < monthsBox.getItemCount(); i++) {
             if (monthsBox.getItemText(i).equalsIgnoreCase(yearAndMonth[1])) {
                 monthsBox.setSelectedIndex(i);
                 monthSetted = true;
                 break;
             }
         }
     
         if(!yearSetted || !monthSetted)
             throw new RuntimeException("Can't set month " + currentMonth + 
                                        " in " + getClass().getName());
     }
     
     /**
      * Sets the current calendar month, based on the values 
      * ​​that are specified in the components {@link #yearsBox}
      * и {@link #monthsBox}
      */
     private void setModelByListBoxes() {
         String year = yearsBox.getItemText(yearsBox.getSelectedIndex());
         String month = monthsBox.getItemText(monthsBox.getSelectedIndex());
     
        getDatePicker().setCurrentMonth(
                 monthFormat.parse(year + "-" + month));
     }
 
     /**
      * Returns current selected year in 
      * the {@link #yearsBox} as String
      * 
      * @return current selected year
      */
     private String getSelectedYear(){
         String selectedYear = null;
         int selectedIndex = yearsBox.getSelectedIndex();
         
         if(selectedIndex >= 0)
             selectedYear = yearsBox.getItemText(selectedIndex);
         
         return selectedYear;
     }
     
     /**
      * Returns current year of the 
      * {@link com.google.gwt.user.datepicker.client.CalendarModel CalendarModel}
      * as String
      * 
      * @return current year of the DatePicker model
      */
     private String getCurrentModelYear(){
         return yearFormat.format(getModel().getCurrentMonth());
     }
     
     
     /**
      * Converts the value at the string so that the number 
      * of characters is always equal to 4. 
      * 
      * For example, the year of 15 will be converted to a string 0015.
      * 
      * @param year - year to translate
      * @return year as String
      */
     private static String yearToString(int year){      
         if(year < 1000) 
             return NumberFormat.getFormat("0000").format(year);
         
         return String.valueOf(year);
     }
 }
