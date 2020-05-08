 package fellesprosjektet;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.LayoutManager;
 import java.awt.Rectangle;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import model.Day;
 import model.MyCalendar;
 import model.MeetTime;
 import model.Time;
 
 import com.sun.xml.internal.ws.api.server.Container;
 
 public class CalenderView {
 	public JFrame myFrame = new JFrame("Calender");
 	public JLayeredPane lpane = new JLayeredPane();
 	public JPanel myPanel;
 	public JPanel calpanel;
 	public JPanel cal2panel;
 	public UserView uview;
 	public java.awt.Container pane;
 	public GridBagConstraints layout;
 	public model.MyCalendar caldata;
 	public MonthCalender cal;
 	public WeekCalendar wcal;
 	public JButton butt;
 	
 	public CalenderView() {
 		//Look and feel
 		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
 		catch (ClassNotFoundException e) {}
 		catch (InstantiationException e) {}
 		catch (IllegalAccessException e) {}
 		catch (UnsupportedLookAndFeelException e) {}
 		
 //		myFrame = new JFrame();
 		myPanel = new JPanel();
 		
 		//Prepare frame
 		Dimension mysize = new Dimension(1000, 600);
 		myFrame.setSize(mysize); //Set size to 400x400 pixels
 		myFrame.setLayout(new BorderLayout());
 //		lpane.setLayout(new BorderLayout());
 		myFrame.setContentPane(lpane);
 //		myFrame.add(lpane, BorderLayout.CENTER);
 //		pane = myFrame.getContentPane(); //Get content pane
 		myFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Close when X is clicked
 		myFrame.setVisible(true);
 
 		//Create controls
 		butt = new JButton("hei");
 		myPanel = new JPanel();
 		calpanel = new JPanel();
 		cal2panel = new JPanel();
 		layout = new GridBagConstraints();
 		myPanel.setLayout(new GridBagLayout());
 		caldata = new model.MyCalendar();
 		cal = new MonthCalender(lpane, caldata, 0, 30, 1, BorderLayout.WEST);
 		wcal = new WeekCalendar(caldata, 270, 30);
 		MessageBar mbar = new MessageBar(0, 0);
 		EventPanel evpnl = new EventPanel(870, 30);
 
 		uview = new UserView(0, 250);
 		
 		
 		//Add controls to pane
 		myPanel.add(butt);
 		layout.gridx = 0;
 		layout.gridy = 0;
 		lpane.add(wcal, new Integer(2));
 		lpane.add(mbar, new Integer(2));
 		lpane.add(uview, new Integer(2));
		lpane.add(wcal.addStickers(new MeetTime(new Time(10, 0), new Time(11, 0), Day.Sunday, 12, 2012)), new Integer(3));
 		lpane.add(evpnl, new Integer(2));
 //		lpane.add(myPanel);
 //		lpane.add(cal);//, BorderLayout.WEST);
 //		lpane.add(cal2, BorderLayout.EAST);
 
 		myPanel.setBounds(0, 0, 800, 600);
 		
 		
 //		myPanel.add(cal);
 	}
 }
