 package edu.brown.cs32.scheddar.gui;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Line2D;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 import edu.brown.cs32.scheddar.Meeting;
 import edu.brown.cs32.scheddar.ScheddarTime;
 import edu.brown.cs32.scheddar.UsefulMethods;
 
 
 /**
  * @author sdemane
  *
  * Class representing the day view of the scheddar GUI. Should
  * be paired with MeetingPanel in the panel opposite GroupTree.
  * Looks similar to MonthPanel and DayPanel, but different size
  * and function.
  *
  */
 public class DayPane extends ScheddarSubPane {
 	private static final long serialVersionUID = 1L;
 	
 	ScheddarTime time;
 	int day,month,year;
 	Color finalMeetingColor = new Color(210,40,40);
 	Color proposedMeetingColor = new Color(120,160,210);
 	int startHour, endHour, numHours;
 	
 	public DayPane(ScheddarPane s, ScheddarTime st) {
 		super(s);
 		this.time = st;
 		this.day = st.getDay();
 		this.month = st.getMonth();
 		this.year = st.getYear();
 		
 		String startTime = _scheddar.getStartHour();
 		String endTime = _scheddar.getEndHour();
 		
 		startHour = Integer.parseInt(startTime.split(":")[0]);
 		endHour = Integer.parseInt(endTime.split(":")[0]);
 		numHours = endHour - startHour;
 		
 		this.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				Dimension size = getPreferredSize();
 				Point p = e.getPoint();
 				int minutes = (int)((p.getY()/size.height)*numHours*60 + startHour*60);
 				List<Meeting> meetings = _scheddar.dayMeetings(day, month, year);
 				LinkedList<Meeting> clickedMeetings = new LinkedList<Meeting>();
 				for(Meeting meeting: meetings) {
 					for (ScheddarTime t : meeting.getProposedTimes()) {
 						if((t.getStartHour()*60 + t.getStartMinutes())<=minutes && t.getDuration()+t.getStartHour()*60+t.getStartMinutes()>=minutes) {
 							clickedMeetings.add(meeting);
 						}
 					}
 				}
 				if (clickedMeetings.size() == 0) {
 					_scheddarPane._calendar.switchToMeeting(new ScheddarTime(0, 0, 1, time.getDayOfWeek(), day, month, year, false), null);
 				} else if (clickedMeetings.size() == 1) {
 					_scheddarPane._calendar.switchToMeeting(new ScheddarTime(0, 0, 1, time.getDayOfWeek(), day, month, year, false), clickedMeetings.poll());
 				} else {
 					JPopupMenu pickMeeting = new JPopupMenu();
 					for (Meeting m : clickedMeetings) {
 						JMenuItem mItem = new JMenuItem(m.getName());
 						mItem.addActionListener(new ActionListener() {
 							
 							@Override
 							public void actionPerformed(ActionEvent arg0) {
 								Object o = arg0.getSource();
 								JMenuItem menuthing = (JMenuItem) o;
 								_scheddarPane._calendar.switchToMeeting(new ScheddarTime(0, 0, 1, time.getDayOfWeek(), day, month, year, false), _scheddar.getMeetingFromName(menuthing.getText()));
 								
 							}
 						});
 					}
 				}
 			}
 		});
 	}
 	
 	@Override
 	public Dimension getPreferredSize() {
 		Dimension d = _scheddarPane.getPreferredSize();
 		return new Dimension(d.width / 8 - 10, d.height);
 	}
 	
 	private Rectangle getTimeBlock(ScheddarTime st) {
 		Dimension d = getPreferredSize();
 		int x = 0;
 		int startMinutes = (st.getStartHour() - startHour)* 60 + st.getStartMinutes();
 		int y = startMinutes * d.height / (60*numHours);
 		int width = d.width;
 		int height = st.getDuration() * d.height / (60*numHours);
 		
 		return new Rectangle(x, y, width, height);
 	}
 	
 	
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Graphics2D g2 = (Graphics2D) g;
 		if (isFun()) {
 			setOpaque(false);
 			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 0.0f));
 		}
 		
 		Composite originalComposite = g2.getComposite();
 		Composite transparentComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
 		
 		
 		Dimension size = getPreferredSize();		
 		g2.setBackground(Color.white);
 		
 		g2.setStroke(new BasicStroke(1));
 		g2.setPaint(Color.darkGray);
 		g2.draw(new Line2D.Double(0,0,0,size.height));
 		g2.draw(new Line2D.Double(0,0,size.width,0));
 		g2.draw(new Line2D.Double(size.width,size.height,0,size.height));
 		g2.draw(new Line2D.Double(size.width,size.height,size.width,0));
 		
 		g2.setPaint(Color.gray);
 		g2.setFont(new Font("SansSerif",Font.PLAIN,10));
 		
 		for (double i = 1; i < numHours; i += 1.0) {
 			
 			g2.draw(new Line2D.Double(0,size.height/numHours*i,size.width,size.height/numHours*i));
 			
 			g2.drawString((int)(i+startHour)+":00",4,(int)(size.height/numHours*i-3));
 
 		}
 		
 		g2.drawString(time.dateToString(), 10, 15);
 		List<Meeting> meetings = _scheddar.dayMeetings(day, month, year);
 		
 		g2.setStroke(new BasicStroke(2));
 		for (Meeting m : meetings) {
 			if (m.isDecided()) {
 				ScheddarTime t = m.getFinalTime();
 				
 				
 				if ((t.getDay() == this.day && t.getMonth() == this.month && t.getYear() == this.year) || 
 						(t.isRecurring() && t.getDayOfWeek()==UsefulMethods.dayOfTheWeek(day, month, year))) {
 					Rectangle block = getTimeBlock(m.getFinalTime());
 					
 					g2.setPaint(Color.black);
 					g2.draw(block);
 					g2.drawString(m.getName(), 10, 15+block.y);
 					
 					g2.setComposite(transparentComposite);
 					g2.setPaint(finalMeetingColor);
 					g2.fill(block);
 					g2.setComposite(originalComposite);
 					g2.drawString(m.getName(), 10, block.y+15);
 				}
 			} else {
 				List<ScheddarTime> proposedTimes = m.getProposedTimes();
 				HashMap<Integer,Double> indexToScore = m.getIndexToScore();
 				for (ScheddarTime t : proposedTimes) {
 					if ((t.getDay() == this.day && t.getMonth() == this.month && t.getYear() == this.year) ||
 							(t.isRecurring() && t.getDayOfWeek()==UsefulMethods.dayOfTheWeek(day, month, year))) {
 						Rectangle block = getTimeBlock(t);
 						
 						g2.setPaint(Color.black);
 						g2.draw(block);
 						
 						g2.setComposite(transparentComposite);
 						g2.setPaint(proposedMeetingColor);
 						g2.fill(block);
 						g2.setComposite(originalComposite);
 					}
 				}
 			}
 		}
 		
 		
 		
 	}
 }
