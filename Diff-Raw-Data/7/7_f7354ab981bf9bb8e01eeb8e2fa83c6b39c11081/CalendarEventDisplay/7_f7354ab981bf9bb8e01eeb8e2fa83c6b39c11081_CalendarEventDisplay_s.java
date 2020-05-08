 import java.awt.GridLayout;
 import javax.swing.*;
 
 public class CalendarEventDisplay extends JFrame{
 	
 	public CalendarEventDisplay(int day, int month, int year, CalendarData Data)
 	{
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		this.setTitle("Events");
 		this.setSize(250, 250);
 		this.setVisible(true);
 				
		int count = Data.GetCount(day, month, year);
 		this.setLayout(new GridLayout(count, 1));
 		JButton[] events = new JButton[count];
		Event[] e = Data.GetEvents(day, month, year);
 		for(int i = 0; i < count; i++)
 		{
 			events[i] = new JButton();
 			String label = String.format("<html>%s<br>%s<br>%d:%d<p></html>", e[i].GetName(), e[i].GetLocation(), e[i].GetTimeHour(), e[i].GetTimeMinute());
 			events[i].setText(label);
 			events[i].setHorizontalAlignment(SwingConstants.CENTER); 
 			this.add(events[i]);
 		}
 	}
 }
