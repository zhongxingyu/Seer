 package no.ntnu.fp.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  * Shows a grid representing 7days (cols) and 24 hour (rows) with CalendarEnties
  * placed on the sheet corresponding to their weekday, startTime and duration
  * 
  * @author andrephilipp
  * 
  */
 
 public class WeekSheet extends JPanel {
 
 	final Color GRID_COLOR = Color.LIGHT_GRAY;
 	final Color HOURLABELS_COLOR = Color.LIGHT_GRAY;
 	private ArrayList<JLabel> hours = new ArrayList<JLabel>();
 
 	public ArrayList<CalendarEntryView> events = new ArrayList<CalendarEntryView>();
 	
 	private WeekSheetAdapter adapter;
 
 	private int cellHeight;
 	private int cellWidth;
 	private int hourColWidth;
 	private JPanel weekHeader;
 
 	public WeekSheet(WeekSheetAdapter adapter) {
 		this.adapter = adapter;
 		addHourLabels();
 		addEvents();
 		setPreferredSize(new Dimension(600, 1500));
 	}
 	
 	private void addEvents() {
 		for(CalendarEntryView cev: adapter){
			System.out.println(cev.getModel().getDuration());
 			events.add(cev);
 		}
 	}
 
 	public int getCellHeight() {
 		return cellHeight;
 	}
 	
 	public int getCellWidth() {
 		return cellWidth;
 	}
 	
 	private void addHourLabels() {
 		JLabel hour;
 		for (int i = 0; i < 24; i++) {
 			hour = new JLabel(i + ":00");
 			hour.setForeground(HOURLABELS_COLOR);
 			hour.setBounds(0, i * cellHeight, hour.getPreferredSize().width,
 					hour.getPreferredSize().height);
 			add(hour);
 			hours.add(hour);
 			hourColWidth = hour.getWidth() + 2;
 		}
 	}
 
 	public void paint(Graphics g) {
 		cellHeight = getHeight() / 24;
 		cellWidth = (getWidth() - hourColWidth) / 7;
 
 		paintGrid(g);
 		paintEvents();
 		paintHours();
 
 		super.paintComponents(g);
 	}
 
 	private void paintHours() {
 		int i = 0;
 		for (JLabel hour : hours) {
 			hour.setBounds(0, i * cellHeight, hour.getPreferredSize().width,
 					hour.getPreferredSize().height);
 			i++;
 		}
 	}
 
 	private void paintEvents() {
 		int x, y, width, height;
 		for (CalendarEntryView e : events) {
 			x = hourColWidth + (e.getModel().getDayOfWeek() - 1) * cellWidth;
 			y = (e.getModel().getTimeOfDay() * cellHeight) / 60;
 			width = cellWidth;
 			height = (int) (e.getModel().getDuration() * cellHeight) / 60;
 
 			e.setBounds(x, y, width, height);
 		}
 	}
 
 	/**
 	 * Paints a 24x7 grid representing 24 hours and 7 days
 	 * 
 	 * @param Graphic
 	 *            object to paint to
 	 */
 	private void paintGrid(Graphics g) {
 		g.setColor(GRID_COLOR);
 		for (int i = 0; i < 24; i++) {
 			g.drawLine(0, i * cellHeight, getWidth(), i * cellHeight);
 		}
 
 		for (int i = 0; i < 7; i++) {
 			g.drawLine(i * cellWidth + hourColWidth, 0, i * cellWidth
 					+ hourColWidth, getHeight());
 		}
 	}
 }
