 package client.gui.view.calendarWindowHelper;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import client.gui.view.CalendarWindow;
 
 public class DayPanel extends JPanel {
 
 	private final CalendarWindow calendarWindow;
 
 	public DayPanel(JPanel calendarWindow) {
 		super();
 		this.calendarWindow = (CalendarWindow) calendarWindow;
 		this.setBackground(Color.white);
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 	}
 }
