 /*****************************************************************************/
 /** This activity is to allow the user to preview the hours on the calendar **/
 /** and other incomplete details which will be determined as time passes.   **/
 /**                                                                         **/
 /** 03/16/2013 NPK Began programming with main procedures which determine   **/
 /**                calendar square data, and draw it with (currently) made  **/
 /**                up data. Formatting is not yet in place, but is          **/
 /**                somewhat customizable.                                   **/
 /** 03/20/2013 NPK Now determines first day of month correctly, and uses    **/
 /**                leave hours of calendar square class.                    **/
 /** 03/24/2013 NPK Calendar now sized dynamically based upon screen size.   **/
 /**                                                                         **/
 /**                                                                         **/
 /**                                                                         **/
 /**                                                                         **/
 /**                                                                         **/
 /*****************************************************************************/
 
 
package com.example.grantmobile;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
import android.content.Intent;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.Rect;
 import android.util.DisplayMetrics;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 
 public class CalendarActivity extends Activity {
 
 	// Constants
 	
 	// Days in a week
 	private static final int DAYSINAWEEK = 7;
 	// Weeks to draw
 	private static final int WEEKSTODRAW = 7;
 	
 	// Variables
 	
 	// Display metrics analyzer
 	DisplayMetrics dm;
 	// The view where all is drawn on the screen
 	DrawView drawView;			
 	// The paint brush
 	Paint paint = new Paint();
 	// A placeholder rectangle for drawing
 	Rect curRect;
 	
 	// Calendar settings
 	
 	// Month number for calendar
 	int monthNumber = 0;
 	// Month name for calendar
 	String monthName = "";
 	// Days in month for calendar
 	int daysInMonth = 0;
 	// First day of month number for week before
 	int firstDay = 0;
 	// Year for calendar
 	int year = 0;																
 	// Grant title
 	String grant = "";			
 	// Message for top of the calendar
 	String headerMessage = "";
 	// Message for bottom of the calendar
 	String footerMessage = "";
 	// Screen background color
 	int screenBackgroundColor = Color.BLUE;
 	// Header background color
 	int headerBackgroundColor = Color.BLACK;
 	// Header foreground color
 	int headerForegroundColor = Color.WHITE;										 
 	// Daily background color odd
 	int dailyBackgroundColorOdd = Color.WHITE;
 	// Daily background color even
 	int dailyBackgroundColorEven = Color.LTGRAY;	
 	// Daily foreground color
 	int dailyForegroundColor = Color.RED;
 	// Footer background color
 	int footerBackgroundColor = Color.BLACK;
 	// Footer foreground color 
 	int footerForegroundColor = Color.WHITE;
 	// Total grant hours for a month
 	int monthTotalGrantHours = 0;
 	// Total non-grant hours for a month
 	int monthTotalNonGrantHours = 0;
 	// Total leave hours for a month
 	int monthTotalLeaveHours = 0;
 	// Total hours for a month
 	int monthTotalHours = 0;
 	// Calendar square titles
 	ArrayList<String> squareTitles = new ArrayList<String>();
 	// Calendar square size width
 	int calendarSquareSizeW;
 	// Calendar square size height
 	int calendarSquareSizeH;
 	// Calendar initial horizontal margin
 	int calendarMarginX = 10;
 	// Calendar initial vertical margin
 	int calendarMarginY = 10;
 	// Calendar square array list
 	ArrayList<CalendarSquare> calendar = new ArrayList<CalendarSquare>();
 	
 	/**
 	 * This procedure initializes the layout.
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		// Initialize display metrics
 		dm = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(dm);
 		
 		// Initialize rectangle
 		curRect = new Rect();
 		
 		// Initialize header message
 		initHeaderMessage();
 		
 		// Initialize calendar
 		initCalendar();
 		
 		// Initialize drawing view
 		drawView = new DrawView(this);
 		drawView.setBackgroundColor(screenBackgroundColor);
 		
 		// Create
 		super.onCreate(savedInstanceState);
         setContentView(drawView);
 		
 	}
 
 	/**
 	 * This procedure initializes the options menu.
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	/**
 	 * This class is the DrawView class for drawing the calendar.
 	 */
 	class DrawView extends View
 	{
 		
 		/**
 		 * This constructor initializes a draw view with a context.
 		 */
 		public DrawView(Context context)
 		{
 			
 			super(context);
 		}
 
 		/**
 		 * This procedure draws the screen with the current details.
 		 */
 		@Override
 		protected void onDraw (Canvas canvas)
 		{
 			// Variables
 			
 			// The width for the header and footer
 			int headerAndFooterWidth;
 			// Footer Y position
 			int footerY;
 			// Current calendar square index
 			int currentCalendarSquareIndex = 0;
 			// The current display for the current calendar square
 			String thisDisplay = "";
 			// Odd or even flag
 			Boolean oddFlag = true;
 			
 			// Calculate header and footer widths
 			headerAndFooterWidth = calendarSquareSizeW * (DAYSINAWEEK + 1);
 			
 			// Draw header
 			paint.setColor(headerBackgroundColor);
 			paint.setStyle(Style.FILL_AND_STROKE);
 			curRect.set(
 				calendarMarginX,
 				calendarMarginY,
 				calendarMarginX + headerAndFooterWidth,
 				calendarMarginY + calendarSquareSizeH
 			);
 			canvas.drawRect(curRect, paint);
 			paint.setColor(headerForegroundColor);
 			canvas.drawText(headerMessage, calendarMarginX + 10,
 					calendarMarginY + 10, paint);
 			
 			// Draw inner calendar squares
 			paint.setStyle(Style.FILL_AND_STROKE);
 			
 			// Loop of using calendar square index
 			for (
 				currentCalendarSquareIndex = 0;
 				currentCalendarSquareIndex < calendar.size();
 				currentCalendarSquareIndex = currentCalendarSquareIndex + 1
 				) {
 			
 				// Determine rectangle to draw
 				curRect.set(
 						calendar.get(currentCalendarSquareIndex).positionX,
 						calendar.get(currentCalendarSquareIndex).positionY,
 						calendar.get(currentCalendarSquareIndex).positionX +
 							calendar.get(currentCalendarSquareIndex).sizeW,
 						calendar.get(currentCalendarSquareIndex).positionY +
 							calendar.get(currentCalendarSquareIndex).sizeH
 					);
 				
 				// Determine color
 				if (oddFlag) {
 					paint.setColor(dailyBackgroundColorOdd);
 				}
 				else
 				{
 					paint.setColor(dailyBackgroundColorEven);
 				}// end if
 				oddFlag = !oddFlag;
 				
 				// Draw rectangle determined
 				canvas.drawRect(curRect, paint);
 				
 				// Determine display
 				thisDisplay =
 					calendar.get(currentCalendarSquareIndex).displayString;
 				
 				// Show display
 				paint.setColor(dailyForegroundColor);
 				canvas.drawText(thisDisplay,
 						calendar.get(currentCalendarSquareIndex).positionX
 							+ 10,
 						calendar.get(currentCalendarSquareIndex).positionY
 							+ 10,
 						paint);
 
 			}// end for
 			
 			// Determine footer positions
 			footerY = 
 				calendarMarginY + (calendarSquareSizeH * (WEEKSTODRAW + 1));
 			
 			// Draw footer
 			paint.setColor(footerBackgroundColor);
 			paint.setStyle(Style.FILL_AND_STROKE);
 			curRect.set(
 				calendarMarginX,
 				footerY,
 				calendarMarginX + headerAndFooterWidth,
 				footerY + calendarSquareSizeH
 			);
 			canvas.drawRect(curRect, paint);
 			
 			// Draw footer text
 			paint.setColor(footerForegroundColor);
 			canvas.drawText(footerMessage, calendarMarginX + 10,
 				footerY + 10, paint);
 			
 		}
 		
 		
 	}
 	
 	/**
 	 * This procedure handles touch events. Currently, in the event of a
 	 * square's touching, it displays the daily number of that square.
 	 */
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		
 		// Variables
 		
 		// The touching X coordinate
 		int x = (int)event.getX();
 		// The touching Y coordinate
 	    int y = (int)event.getY();
 		
 	    // Check for any calendar square touches, and toast daily
 	    // number of those touched
 	    for (int i = 0; i < calendar.size(); i = i + 1)
 	    {
 	    	if ((x >= calendar.get(i).positionX) &&
 	    		(y >= calendar.get(i).positionY) &&
 	    		(x <= (calendar.get(i).positionX) + calendar.get(i).sizeW) &&
 	    		(y <= (calendar.get(i).positionY) + calendar.get(i).sizeH)) {
 	    		
 	    		//Toast.makeText(this, "D" + calendar.get(i).dailyNumber,
 	    		//	Toast.LENGTH_LONG).show();
 	    		
 	    	}// end if   	
 	    	
 	    }// end for
 	    
 		return false;
 	}
 
 	/**
 	 * This procedure initializes the header message.
 	 */
 	private void initHeaderMessage()
 	{	
 		// Variables
 		
 		// Long date message
 		String longDate = "";
 		
 		// Load month, year, and grant name
 		// (Using sample data)
 		monthNumber = 2;
 		year = 2013;
 		grant = "GRANT-101-101-101";
 		
 		// Get the month's details
 		getMonthDetails();
 	
 		// Determine long date
 		longDate = monthName + ", " + year;
 				
 		// Determine header message
 		headerMessage = grant.trim() + " : " + longDate;
 		
 	}
 	
 	/**
 	 * This procedure initializes the calendar.
 	 */
 	private void initCalendar()
 	{
 		// Variables
 
 		// Width of the screen
 		int screenWidth;
 		// Height of the screen
 		int screenHeight;
 		// Width of the calendar
 		int calendarWidth;
 		// Height of the calendar
 		int calendarHeight;
 		// Current X position
 		int currentX;
 		// Current Y position
 		int currentY;
 		// Daily number for this calendar square
 		int thisDailyNumber = -1;
 		// Initial display for a calendar square
 		String initialDisplay = "";
 		// Days started to being counted flag
 		Boolean daysStarted = false;
 		// Days done being counted flag
 		Boolean daysEnded = false;
 		
 		// Initialize calendar square size
 		
 		// Determine screen width and height
 		screenWidth = dm.widthPixels;
 		screenHeight = dm.heightPixels;
 		
 		// Determine calendar height by subtracting margins
 		calendarWidth = screenWidth - (calendarMarginX * 2);
 		calendarHeight = screenHeight - (calendarMarginY * 2);
 		
 		// Determine individual square sizes
 		calendarSquareSizeW = calendarWidth / (DAYSINAWEEK + 1);
 			// plus one from weekly totals 
 		calendarSquareSizeH = calendarHeight / (WEEKSTODRAW + 4);
 			// plus four from header, footer, and two for extra compensation
 		
 				
 		// Initialize calendar square titles
 		Collections.addAll(squareTitles,
 			"Su","Mo","Tu","We","Th","Fr","Sa","To");
 		
 		// Initialize daily number
 		thisDailyNumber = 0;
 		
 		// Loops of initializing calendar squares:
 		
 		// Vertical
 		for (int yy = 0; yy < WEEKSTODRAW; yy++)
 		{
 			// Calculate current Y coordinate
 			currentY = calendarMarginY + (calendarSquareSizeH * (yy + 1)); 
 			
 			// Horizontal
 			for (int xx = 0; xx <= 7; xx++)
 			{
 				// Calculate current X coordinate
 				currentX = calendarMarginX + (calendarSquareSizeW * xx);
 
 				// For first row, set display to title
 				if (yy == 0)
 				{
 					
 					initialDisplay = squareTitles.get(xx);
 					
 				}
 				else
 				{
 				// For additional rows, determine daily number
 					
 					// Initialize display to set to dash
 					initialDisplay = "-";
 					
 					// If day counting hasn't started, check if first day
 					if (!daysStarted)
 					{
 						if (xx == firstDay)
 						{
 							daysStarted = true;
 							thisDailyNumber = 0;
 						}
 						
 					}// end if
 					
 					// After days have started, but not yet ended
 					if ((daysStarted) && (!daysEnded))
 					{
 						
 						// If not the location for the weekly totals
 						if ((xx != DAYSINAWEEK) && (!daysEnded))
 						{							
 							
 							// Increase daily number							
 							thisDailyNumber = thisDailyNumber + 1;
 							
 							// Set initial display to daily number
 							initialDisplay = String.valueOf(thisDailyNumber);
 							
 						} else
 						{
 							// If location for daily totals, start with N/A
 							initialDisplay = "N/A";
 							
 						}// end if
 						
 						// If past the end of the month, use zero as daily
 						// number, and set display to dash
 						if ((thisDailyNumber > daysInMonth) && (!daysEnded)) {
 							daysEnded = true;
 							thisDailyNumber = 0;
 							initialDisplay = "-";
 						}// end if
 
 					}// end if
 					
 				}// end if
 				
 				// If days have ended, check for daily total square
 				if ((daysEnded) && (xx == DAYSINAWEEK))
 				{
 					initialDisplay = "N/A";
 				
 				}// end if
 				
 				// Initialize this calendar square
 				calendar.add(
 					new CalendarSquare(
 						currentX, currentY,
 						calendarSquareSizeW, calendarSquareSizeH,
 						thisDailyNumber, initialDisplay)
 				);
 				
 			}// end for
 			
 		}// end for
 		
 	}
 	
 	/**
 	 * This procedure returns the month name of the current month number.
 	 */
 	private void getMonthDetails()
 	{
 		// Variables
 		
 		// Calendar object for day of week testing
 		Calendar cal;
 		
 		// Determine month name and number of days in the month
 		switch (monthNumber)
 		{
 			case	1	:
 				monthName = "January";
 				daysInMonth = 31;
 				break;
 			case	2	:
 				monthName = "February";
 				daysInMonth = 28;
 				
 				// Leap year check
 				if (((year % 4) == 0) && (year % 100 != 0))
 				{
 					daysInMonth = 29;
 				}// end if
 				
 				break;
 			case	3	:
 				monthName = "March";
 				daysInMonth = 31;
 				break;
 			case	4	:
 				monthName = "April";
 				daysInMonth = 31;
 				break;
 			case	5	:
 				monthName = "May";
 				daysInMonth = 31;
 				break;
 			case	6	:
 				monthName = "June";
 				daysInMonth = 31;
 				break;
 			case	7	:
 				monthName = "July";
 				daysInMonth = 31;
 				break;
 			case	8	:
 				monthName = "August";
 				daysInMonth = 31;
 				break;
 			case	9	:
 				monthName = "September";
 				daysInMonth = 31;
 				break;
 			case	10	:
 				monthName = "October";
 				daysInMonth = 31;
 				break;
 			case	11	:
 				monthName = "November";
 				daysInMonth = 31;
 				break;
 			case	12	:
 				monthName = "December";
 				daysInMonth = 31;
 				break;
 			default		:
 				monthName = "";
 				daysInMonth = 0;
 				firstDay = 8;
 				break;
 		}// end switch
 		
 		// Determine what day of the week the first of the month is
 		
 		// First check if valid month
 		if (monthName != "")
 		{
 			cal = Calendar.getInstance();
 			cal.set(year, monthNumber - 1, 1);
 			firstDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
 			
 		}// end if
 		
 	}
 	
 	/**
 	 * This procedure loads the calendar with the data for hours, and totals.
 	 */
 	private void loadCalendarData() {
 	
 		// Variables
 		
 		// Total grant hours for a day
 		int dayTotalGrantHours = 0;
 		// Total non-grant hours for a day
 		int dayTotalNonGrantHours = 0;
 		// Total leave hours for a day or week
 		int dayTotalLeaveHours = 0;
 		// Total hours for a day or week
 		int dayTotalHours = 0;
 		// Total grant hours for a week
 		int currentTotalGrantHours = 0;
 		// Total non-grant hours for a week
 		int currentTotalNonGrantHours = 0;
 		// Total leave hours for a week
 		int currentTotalLeaveHours = 0;
 		// Total hours for a day or week
 		int currentTotalHours = 0;
 		
 		// (Currently made-up by using daily number)
 		// Retrieve data for hours
 		for (int i = 0; i < calendar.size(); i++)
 		{
 			calendar.get(i).grantHours = calendar.get(i).dailyNumber;
 			calendar.get(i).nonGrantHours = calendar.get(i).dailyNumber;
 			calendar.get(i).leave = calendar.get(i).dailyNumber;
 			
 		}// end for
 		
 		// Determine weekly and monthly totals and note information
 		for (int i = 0; i < calendar.size(); i++)
 		{
 			
 			// If not the first week test
 			if (i >= (DAYSINAWEEK + 1)) {
 			
 				// End of week test
 				// If end of week, determine weekly totals, and set it
 				if ((i % (DAYSINAWEEK + 1)) == DAYSINAWEEK) {
 				
 					// Set display information as
 					// total hours concatenated string
 					calendar.get(i).displayString =
 						String.valueOf(currentTotalGrantHours) + "|" +
 						String.valueOf(currentTotalNonGrantHours) + "|" +
 						String.valueOf(currentTotalLeaveHours) + "|" +
 						String.valueOf(currentTotalHours);
 					
 					// Reset weekly totals
 					currentTotalGrantHours = 0;
 					currentTotalNonGrantHours = 0;
 					currentTotalLeaveHours = 0;
 					currentTotalHours = 0;
 					
 				}
 				else if ((calendar.get(i).dailyNumber > daysInMonth)
 						|| (calendar.get(i).dailyNumber < 1))
 				{
 					// Daily number out of range test. If so, set display string to dash
 					calendar.get(i).displayString = "-";
 					
 				}
 				else
 				{
 					// If not end of week and within range, determine
 					// daily information
 					dayTotalGrantHours = calendar.get(i).grantHours;
 					dayTotalNonGrantHours = calendar.get(i).nonGrantHours;
 					dayTotalLeaveHours = calendar.get(i).leave;
 					dayTotalHours = calendar.get(i).totalHours();
 					
 					// Add daily information to weekly totals
 					currentTotalGrantHours += dayTotalGrantHours;
 					currentTotalNonGrantHours += dayTotalNonGrantHours;
 					currentTotalLeaveHours += dayTotalLeaveHours;
 					currentTotalHours += dayTotalHours;
 					
 					// Increase totals for monthly total
 					monthTotalGrantHours += dayTotalGrantHours;
 					monthTotalNonGrantHours += dayTotalNonGrantHours;
 					monthTotalLeaveHours += dayTotalLeaveHours;
 					monthTotalHours += dayTotalHours;
 					
 					// Set display information as
 					// total hours concatenated string
 					calendar.get(i).displayString =
 						String.valueOf(dayTotalGrantHours) + "|" +
 						String.valueOf(dayTotalNonGrantHours) + "|" +
 						String.valueOf(dayTotalLeaveHours) + "|" +
 						String.valueOf(dayTotalHours);
 				
 				}// end if
 				
 			}// end if
 			
 		}// end for
 		
 		// Determine footer message from monthly totals
 		footerMessage = "Total Hours This Month: " +
 			String.valueOf(monthTotalGrantHours) + "|" +
 			String.valueOf(monthTotalNonGrantHours) + "|" +
 			String.valueOf(monthTotalLeaveHours) + "|" +
 			String.valueOf(monthTotalHours);
 		
 		// Refresh screen
 		drawView.invalidate();
 	}
 	
 	/**
 	 * This procedure handles all of the options menu selection events.
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	
 		// Variables
 		
 		// The id of the menu item chosen
 		int itemId = 0;	
 		
 		// Determine id of item chosen, and respond accordingly
 		itemId = item.getItemId();
 		
 		switch (itemId) {
 			case (R.id.mnuLoad)	:
 			loadCalendarData();
 			break;
 		case (R.id.mnuDetail) :
 			Intent intent = new Intent(this, DetailViewActivity.class);
 			startActivity(intent);
 			break;
 		case (R.id.mnuDialog) :
 			//show dialog box
 			SubmitDialog dialog = new SubmitDialog();
 			dialog.setUserID(732);
 			dialog.show(getFragmentManager(), "");
 			break;
 		}// end switch
 		
 		return true;
 		
 		} 
 	
 }
