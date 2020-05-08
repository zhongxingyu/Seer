 /* 
 * Copyright 2013 Fredy Wijaya
  *
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package org.fredy.jcalendar;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
  
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
  
 public class JCalendar {
     private static Calendar currentTime = Calendar.getInstance();
     private static Label dateLabel;
     private static Table calendarTable;
      
     private static void updateDate(Calendar calendar) {
        dateLabel.setText(new SimpleDateFormat("MMM yyyy").format(calendar.getTime()));
     }
      
     private static void createNavigation(final Shell shell, final Calendar calendar) {
         Composite composite = new Composite(shell, SWT.BORDER);
         composite.setLayout(new GridLayout(3, true));
         composite.setLayoutData(
             new GridData(GridData.FILL, GridData.FILL, true, true));
          
         Button leftArrowButton = new Button(composite, SWT.PUSH);
         leftArrowButton.setText("<");
         leftArrowButton.setLayoutData(
             new GridData(GridData.FILL, GridData.FILL, true, true));
         leftArrowButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseDown(MouseEvent e) {
                 calendar.add(Calendar.MONTH, -1);
                 updateDate(calendar);
                 updateCalendar(shell, calendarTable, calendar);
                 shell.pack();
             }
         });
          
         dateLabel = new Label(composite, SWT.CENTER);
         dateLabel.setLayoutData(
             new GridData(GridData.FILL, GridData.FILL, true, true));
         updateDate(calendar);
          
         Button rightArrowButton = new Button(composite, SWT.PUSH);
         rightArrowButton.setText(">");
         rightArrowButton.setLayoutData(
             new GridData(GridData.FILL, GridData.FILL, true, true));
         rightArrowButton.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseDown(MouseEvent e) {
                 calendar.add(Calendar.MONTH, 1);
                 updateDate(calendar);
                 updateCalendar(shell, calendarTable, calendar);
                 shell.pack();
             }
         });
     }
      
     private static void addRows(Shell shell, Calendar calendar) {
         int currentDayOfMonth = currentTime.get(Calendar.DAY_OF_MONTH);
         int currentYear = currentTime.get(Calendar.YEAR);
         int currentMonth = currentTime.get(Calendar.MONDAY);
          
         calendar.set(Calendar.DAY_OF_MONTH, 1);
         int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
         int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
         int year = calendar.get(Calendar.YEAR);
         int month = calendar.get(Calendar.MONTH);
          
         TableItem item = new TableItem(calendarTable, SWT.NONE);
         for (int i = 0; i < dayOfWeek-1; i++) {
             item.setText(i, "  ");
         }
         int value = 1;
         for (int i = 0; i < 7-dayOfWeek+1; i++) {
             String day = Integer.toString(value);
             if (value < 10) {
                 day = " " + value;
             }
             item.setText(i+dayOfWeek-1, day);
             value++;
         }
          
         while (value <= daysInMonth) {
             item = new TableItem(calendarTable, SWT.NONE);
             for (int j = 0; j < 7; j++) {
                 if (value <= daysInMonth) {
                     if (value == currentDayOfMonth
                         && currentYear == year && currentMonth == month) {
                         Color blue = new Color(shell.getDisplay(), 0, 0, 255);
                         item.setForeground(j, blue);
                         blue.dispose();
                     }
                     String day = Integer.toString(value);
                     if (value < 10) {
                         day = " " + value;
                     }
                     item.setText(j, day);
                 } else {
                     item.setText(j, "  ");
                 }
                 value++;
             }
         }
     }
      
     private static void updateCalendar(Shell shell, Table table, Calendar calendar) {
         table.removeAll();
         addRows(shell, calendar);
     }
      
     private static void createCalendar(Shell shell, Calendar calendar) {
         calendarTable = new Table(shell, SWT.BORDER);
         calendarTable.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
         calendarTable.setLinesVisible(true);
         calendarTable.setHeaderVisible(true);
  
         String[] titles = {
             "S", "M", "T", "W", "T", "F", "S"
         };
         for (int i = 0; i < titles.length; i++) {
             TableColumn column = new TableColumn(calendarTable, SWT.NONE);
             column.setResizable(false);
             column.setText(titles[i]);
         }
          
         addRows(shell, calendar);
          
         for (int i = 0; i < titles.length; i++) {
             calendarTable.getColumn(i).pack();
         }
     }
      
     public static void main(String[] args) {
         Display display = new Display();
         Shell shell = new Shell(display,
             SWT.TITLE | SWT.CLOSE | SWT.BORDER & (~SWT.RESIZE));
         shell.setText("Calendar");
         shell.setLayout(new GridLayout());
          
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(currentTime.getTime());
         createNavigation(shell, calendar);
         createCalendar(shell, calendar);
          
         shell.pack();
         shell.open();
         while (!shell.isDisposed()) {
             if (!display.readAndDispatch()) {
                 display.sleep();
             }
         }
         display.dispose();
     }
 }
