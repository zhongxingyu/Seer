 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Currency;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.StringTokenizer;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 
 import com.dropbox.client2.DropboxAPI;
 import com.dropbox.client2.session.WebAuthSession;
 
 
 public class ExpenseTracker extends JFrame implements ActionListener{
 	
 	private LinkedList<JTextField> lists = new LinkedList<JTextField>();
 	private LinkedList<JTextField> amounts = new LinkedList<JTextField>();
 	private LinkedList<JTextField> dates = new LinkedList<JTextField>();
 	private JButton nameSort = new JButton("Name");
 	private JButton amountSort = new JButton("Amount");
 	private JButton dateSort = new JButton("Date");
 	
 	public ExpenseTracker(String user, DropboxAPI<WebAuthSession> mDBApi, boolean byWeek, boolean byLastTrip)
 	{
 		String title = "iBuy - Expense Tracker";
 		if(byWeek ^ byLastTrip)
 		{
 			if(byWeek)
 				title += " (Week Old Lists)";
 			if(byLastTrip)
 				title += " (All)";
 		}
 		this.setTitle(title);
 		
 		StringTokenizer st = new StringTokenizer(Global.getFile(mDBApi, "/"+user+"/lists.txt"));
 		while(st.hasMoreTokens())
 		{
 			String name = st.nextToken();
 			String amount = st.nextToken();
 			Date last = new Date(Date.parse(Global.readFileName(st.nextToken())));
 			Date current = new Date(System.currentTimeMillis());
 			boolean isBefore = compareDates(last, current);
 			if((isBefore && byWeek) || byLastTrip || (!byLastTrip && !byWeek))
 			{
 				lists.add(new JTextField(Global.readFileName(name)));
 				amounts.add(new JTextField("$" + amount));
 				dates.add(new JTextField((last.getMonth()+1) + "/" + last.getDate() + "/" + (last.getYear()+1900)));
 			}
 		}
 		
 		setLayout(new GridLayout(lists.size() + 2, 3));
 		setSize(500, Math.min((200 + 150*lists.size()), 1000));
 		
 		nameSort.addActionListener(this);
 		amountSort.addActionListener(this);
 		dateSort.addActionListener(this);
 		add(nameSort);
 		add(amountSort);
 		add(dateSort);
 		
 		double total = 0;
 		for(int i = 0; i < lists.size(); i++)
 		{
 			add(lists.get(i));
 			Double d = new Double(amounts.get(i).getText().substring(1));
 			total += d;
 			add(amounts.get(i));
 			add(dates.get(i));
 		}
 		add(new JTextField("Total"));
 		String totalText = new String("" + total);
 		if(totalText.length() > 3)
 			totalText = totalText.substring(0, totalText.indexOf('.') + 3);
 		add(new JTextField("$" + totalText));
 		
 		setContentPane(new JScrollPane(getContentPane()));
 	    setVisible(true);
 	}
 	
 	public boolean compareDates(Date first, Date second)
 	{
 		if(first.getYear() <= second.getYear())
 		{
 			if(first.getMonth() <= second.getMonth())
 			{
 				int diff = second.getMonth()-first.getMonth();
 				if(diff > 0)
 					return true;
 				else if(second.getDate()-first.getDate() >= 7)
 					return true;
 			}
 		}
 		return false;
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == nameSort)
 		{
 			String temp;
 			for(int i = 0; i < lists.size(); i++)
 			{
 				for(int j = 1; j < lists.size()-i; j++)
 				{
 					if(lists.get(j-1).getText().compareTo(lists.get(j).getText()) > 0)
 					{
 						temp = lists.get(j-1).getText();
 						lists.get(j-1).setText(lists.get(j).getText());
 						lists.get(j).setText(temp);
 						
 						temp = amounts.get(j-1).getText();
 						amounts.get(j-1).setText(amounts.get(j).getText());
 						amounts.get(j).setText(temp);
 						
 						temp = dates.get(j-1).getText();
 						dates.get(j-1).setText(dates.get(j).getText());
 						dates.get(j).setText(temp);
 					}
 				}
 			}
 			repaint();
 		}
 		if(e.getSource() == dateSort)
 		{
 			String temp;
 			for(int i = 0; i < lists.size(); i++)
 			{
 				for(int j = 1; j < lists.size()-i; j++)
 				{
 					String first = dates.get(j-1).getText();
 					String second = dates.get(j).getText();
					if(first.substring(first.length()-4).compareTo(second.substring(second.length()-4)) >= 0)
 					{
 						Integer monthOne = new Integer(first.substring(0, first.indexOf('/')));
 						Integer monthTwo = new Integer(second.substring(0, second.indexOf('/')));
						if(monthOne.compareTo(monthTwo) >= 0)
 						{
 							int diff = monthTwo.intValue() - monthOne.intValue();
 							boolean doSwitch = false;
 							Integer dayOne = new Integer(first.substring(first.indexOf('/')+1, first.indexOf('/', first.indexOf('/')+1)));
 							Integer dayTwo = new Integer(second.substring(second.indexOf('/')+1, second.indexOf('/', second.indexOf('/')+1)));
 							if(diff > 0)
 								doSwitch = true;
 							else if(dayOne < dayTwo)
 								doSwitch = true;
 							if(doSwitch)
 							{
 								temp = lists.get(j-1).getText();
 								lists.get(j-1).setText(lists.get(j).getText());
 								lists.get(j).setText(temp);
 								
 								temp = amounts.get(j-1).getText();
 								amounts.get(j-1).setText(amounts.get(j).getText());
 								amounts.get(j).setText(temp);
 								
 								temp = dates.get(j-1).getText();
 								dates.get(j-1).setText(dates.get(j).getText());
 								dates.get(j).setText(temp);
 							}
 						}
 					}
 				}
 			}
 			repaint();
 		}
 		
 		if(e.getSource() == amountSort)
 		{
 			String temp;
 			for(int i = 0; i < lists.size(); i++)
 			{
 				for(int j = 1; j < lists.size()-i; j++)
 				{
 					Double d1 = new Double(amounts.get(j-1).getText().substring(1));
 					Double d2 = new Double(amounts.get(j).getText().substring(1));
 					if(d1.doubleValue() > d2.doubleValue())
 					{
 						temp = lists.get(j-1).getText();
 						lists.get(j-1).setText(lists.get(j).getText());
 						lists.get(j).setText(temp);
 						
 						temp = amounts.get(j-1).getText();
 						amounts.get(j-1).setText(amounts.get(j).getText());
 						amounts.get(j).setText(temp);
 						
 						temp = dates.get(j-1).getText();
 						dates.get(j-1).setText(dates.get(j).getText());
 						dates.get(j).setText(temp);
 					}
 				}
 			}
 			repaint();
 		}
 		
 	}
 }
