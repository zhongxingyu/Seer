 import java.awt.BorderLayout;
 import java.awt.GridLayout;

 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import java.util.Calendar;
 
 public class CalendarGUI extends JFrame{
 
 	private int month;
 	private int year;
 	private JPanel calendar;
 	private CalendarData data;
 	
 	public CalendarGUI(CalendarData Data)
 	{
 		data = Data;
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setTitle("Calendar");
 		this.setSize(750, 750);
 		this.setVisible(true);
 		CreateGUI(Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR));
 	}
 
 	public void CreateGUI(int monthCreate, int yearCreate)
 	{
 		month = monthCreate;
 		year = yearCreate;
 		Calendar cal = Calendar.getInstance();
 		cal.set(Calendar.MONTH, monthCreate);
 		cal.set(Calendar.DAY_OF_MONTH, 1);
 		cal.set(Calendar.YEAR, yearCreate);
 		if(calendar != null)
 			this.remove(calendar);
 		
 		calendar = new JPanel();
 		calendar.setLayout(new GridLayout(6,7));
 		JButton[][] days = new JButton[6][7];
 		Listener[][] listener = new Listener[6][7];
 		for(int i = 0; i < 6; i++)
 		{
 			for(int j = 0; j < 7; j++)
 			{
 				days[i][j] = new JButton();
 				calendar.add(days[i][j]);
 				listener[i][j] = new Listener(8, days[i][j], monthCreate, yearCreate, data);
 				days[i][j].addActionListener(listener[i][j]);
 			}
 		}
 		
 		int[] MonthDays = {0,31,28,31,30,31,30,31,31,30,31,30,31};
 		if(monthCreate + 1 == 2 && yearCreate % 4 == 0)
 		{
 			MonthDays[2] = 29;
 		}
 		int monthStart = (Math.abs(cal.get(Calendar.DAY_OF_WEEK) - cal.get(Calendar.DAY_OF_MONTH)) % 7);
 		int dayNum = 1;
 		boolean start = false;
 		for(int r = 0; r < 6; r++)
 		{
 			for(int c = 0; c < 7; c++)
 			{
 				if(r == 0 && start == false)
 				{
 					c = monthStart;
 					start = true;
 				}
 				days[r][c].setText(Integer.toString(dayNum));
 				dayNum++;
 				if(MonthDays[monthCreate + 1] == dayNum - 1)
 					break;
 			}
 			if(MonthDays[monthCreate + 1] == dayNum - 1)
 				break;
 		}
 		
 		JPanel actions = new JPanel();
 		JButton[] buttons = new JButton[7];
 		for(int i = 0; i < 7; i++)
 		{
 			buttons[i] = new JButton();
 			actions.add(buttons[i]);
 		}
 		buttons[0].setText("Prev");
 		buttons[2].setText("Add Event");
 		buttons[3].setText("Edit Event");
 		buttons[4].setText("Delete Event");
 		buttons[6].setText("Next");
 
 		JPanel Name = new JPanel();
 		JLabel[] monthName = new JLabel[2];
 		for(int i = 0; i < 2; i++)
 			monthName[i] = new JLabel();
 		switch(monthCreate)
 		{
 		case 0:
 			monthName[0].setText("January");
 			break;
 		case 1:
 			monthName[0].setText("February");
 			break;
 		case 2:
 			monthName[0].setText("March");
 			break;
 		case 3:
 			monthName[0].setText("April");
 			break;
 		case 4:
 			monthName[0].setText("May");
 			break;
 		case 5:
 			monthName[0].setText("June");
 			break;
 		case 6:
 			monthName[0].setText("July");
 			break;
 		case 7:
 			monthName[0].setText("August");
 			break;
 		case 8:
 			monthName[0].setText("September");
 			break;
 		case 9:
 			monthName[0].setText("October");
 			break;
 		case 10:
 			monthName[0].setText("November");
 			break;
 		case 11:
 			monthName[0].setText("December");
 			break;
 		}
 		monthName[1].setText(Integer.toString(yearCreate));
 		for(int i = 0; i < 2; i++)
 			Name.add(monthName[i]);
 
 		this.add(Name, BorderLayout.NORTH);
 		this.add(actions, BorderLayout.SOUTH);
 		this.add(calendar, BorderLayout.CENTER);
 
 		Listener add = new Listener(1,this,data);
 		Listener edit = new Listener(2,this,data);
 		Listener delete = new Listener(3,this,data);
 		Listener next = new Listener(4,this,data);
 		Listener prev = new Listener(5,this,data);
 
 		buttons[0].addActionListener(prev);
 		buttons[2].addActionListener(add);
 		buttons[3].addActionListener(edit);
 		buttons[4].addActionListener(delete);
 		buttons[6].addActionListener(next);
 		this.setVisible(true);
 	}
 	
 	public int GetMonth()
 	{
 		return month;
 	}
 	
 	public int GetYear()
 	{
 		return year;
 	}
 
 	public static void main(String[] args) {
 		new CalendarGUI(new CalendarData());
 
 	}
 
 }
 
