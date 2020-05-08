 package views.gui.components;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.GregorianCalendar;
 
 import javax.swing.JComponent;
 
 import models.Event;
 
 public class JEventDisplay extends JComponent {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6850617078923766896L;
 	private static final int eventArcWidth = 10;
 	private static final int eventArcHeight = 10;
 	private static final int eventRectangleXOffset = 5;
 	private int rowCount=14;
 	private int startingHour = 8;
 	String[] daysOfTheWeek = {"Monday", "Tuesday", "roda(via pic)", "Thursday", "Friday", "Saturday", "Sunday"};
 	Iterable<Event> events= null;
 	public Iterable<Event> getEvents() {
 		return events;
 	}
 	public void setEvents(Iterable<Event> events) {
 		this.events = events;
 	}
 	@Override
 	protected void paintComponent(Graphics g) {
 		int colWidth = getWidth()/8;
		int rowHeight = getHeight()/(rowCount+1);
 		drawRowsAndColumns(colWidth, rowHeight, g);
 	}
 	
 	private void drawRowsAndColumns(int colWidth,int rowHeight,Graphics g){
 		drawWeekNameRow(colWidth,rowHeight,g);
 		drawHourColumn(colWidth,rowHeight,g);
 		g.setColor(Color.WHITE);
 		g.fillRect(colWidth,rowHeight,getWidth(),getHeight());
 		drawLines(colWidth,rowHeight,g);
 		drawEvents(colWidth,rowHeight,g);
 		// TODO: tooooooltips or something to show details about event like on pic.
 	}
 	private void drawLines(int colWidth, int rowHeight, Graphics g) {
 		drawVerticalLines(colWidth, rowHeight, g);
 		drawHorizontalLines(colWidth, rowHeight, g);
 	}
 	private void drawVerticalLines(int colWidth, int rowHeight, Graphics g){
 		g.setColor(Color.BLACK);
 		for(int i=1;i<=7;i++){
 			g.drawLine(colWidth*i,0,colWidth*i,getHeight());
 		}
 	}
 	private void drawHorizontalLines(int colWidth, int rowHeight, Graphics g){
 		g.setColor(Color.BLACK);
 		for(int i=1;i<=rowCount;i++){
 			g.drawLine(0,rowHeight*i,getWidth(),rowHeight*i);
 		}
 	}
 	private void drawHourColumn(int colWidth, int rowHeight, Graphics g) {
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillRect(0,rowHeight,colWidth,getHeight());
 		g.setColor(Color.BLACK);
 		for(int i=1;i<=rowCount;i++){
 			int hour = startingHour+i-1;
 			String hourStr = (hour>=10?""+hour:"0"+hour)+":00";
 			g.drawString(hourStr, 
 					(int) (colWidth/2-g.getFontMetrics().getStringBounds(hourStr, g).getWidth()/2),
 					rowHeight*(i+1)-g.getFontMetrics().getHeight()/2);
 		}
 	}
 	private void drawWeekNameRow(int colWidth,int rowHeight,Graphics g){
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillRect(0,0,getWidth(),rowHeight);
 		g.setColor(Color.BLACK);
 		for(int i=1;i<=7;i++){
 			g.drawString(daysOfTheWeek[i-1],
 					colWidth*i+(int) (colWidth/2 - g.getFontMetrics().getStringBounds(daysOfTheWeek[i-1],g).getWidth()/2),
 					rowHeight-g.getFontMetrics().getHeight()/2);
 		}
 	}
 	private void drawEvents(int colWidth,int rowHeight,Graphics g){
 		for(Event event : events){
 			int startingDay = event.getStartTime().get(GregorianCalendar.DAY_OF_WEEK);
 			int endingDay = event.getEndTime().get(GregorianCalendar.DAY_OF_WEEK);
 			int startHour = event.getStartTime().get(GregorianCalendar.HOUR_OF_DAY);
 			int endHour = event.getEndTime().get(GregorianCalendar.HOUR_OF_DAY);
 			int startingMinute = event.getStartTime().get(GregorianCalendar.MINUTE);
 			int endingMinute = event.getEndTime().get(GregorianCalendar.MINUTE);
 			startingDay=(startingDay+5)%7;
 			endingDay=(endingDay+5)%7;
 			Color eventFillColor = Color.RED;
 			Color eventBorderColor = Color.BLACK;
			//System.out.println(""+startingDay+" "+endingDay+"|"+startHour+" "+endHour+"|"+startingMinute+" "+endingMinute);
 			for(int day=startingDay;day<=endingDay;day++){
 				int x = eventRectangleXOffset+colWidth*(day+1);
 				int y = rowHeight*(startHour-startingHour+1)+(int)((float)rowHeight*((float)startingMinute/60.0f));
 				int width = colWidth-eventRectangleXOffset*2;
 				int height = rowHeight*(endHour-startHour)+(int)((float)rowHeight*((float)endingMinute/60.0f));
 				if(day==startingDay&&day==endingDay){
 					drawEventRectangle(x, y, 
 							width, height,
 							eventArcWidth, eventArcHeight, eventFillColor, eventBorderColor, g);
 				}
 				else if(day==startingDay){
 					drawEventRectangle(x, y,
 							width, getHeight()+eventArcHeight*2,
 							eventArcWidth, eventArcHeight, eventFillColor, eventBorderColor, g);
 				}
 				else if(day<endingDay){
 					drawEventRectangle(x, 0,
 							width, getHeight()+eventArcHeight*2,
 							eventArcWidth, eventArcHeight, eventFillColor, eventBorderColor, g);
 				}
 				else if(day==endingDay){
 					drawEventRectangle(x, rowHeight-eventArcHeight,
 							width, height+eventArcHeight,
 							eventArcWidth, eventArcHeight, eventFillColor, eventBorderColor, g);
 				}
 			}
 		}
 		drawWeekNameRow(colWidth, rowHeight, g);
 		g.drawLine(0,rowHeight,getWidth(),rowHeight);
 		drawVerticalLines(colWidth, rowHeight, g);
 	}
 	private void drawEventRectangle(int x,int y,int width,int height, int arcWidth,int arcHeight, Color fill, Color border, Graphics g){
 		g.setColor(fill);
 		g.fillRoundRect(x, y, 
 				width,height,
 				arcWidth, arcHeight);
 		g.setColor(border);
 		g.drawRoundRect(x, y, 
 				width, height,
 				arcWidth, arcHeight);
 	}
 }
