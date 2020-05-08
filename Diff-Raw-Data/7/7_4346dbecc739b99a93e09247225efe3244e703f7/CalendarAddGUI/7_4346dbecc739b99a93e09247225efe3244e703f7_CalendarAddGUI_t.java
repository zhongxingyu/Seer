 import java.awt.GridLayout;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 
 public class CalendarAddGUI extends JFrame{
 
 	private JComponent[][] inputs;
 	private CalendarData data;
 	
 	public CalendarAddGUI(CalendarData Data)
 	{
 		data = Data;
 		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		this.setTitle("Add Event");
 		this.setSize(250, 250);
 		this.setVisible(true);
 		this.setLayout(new GridLayout(10,2));
 		inputs = new JComponent[10][2];
 		String[] labels = {"Name:","Day:","Month:","Year:","Location:","Time(hour):","Time(minute):","Duration:","Description:"};
 		for(int j = 0; j < 9; j++){
 			for(int k = 0; k <2; k++){
 				if(k == 0){
 					inputs[j][k] = new JLabel(labels[j]);
 				}
 				else{
 					inputs[j][k] = new JTextField(30);
 				}
 				this.add(inputs[j][k]);
 			}
 		}
 		JButton confirmEvent = new JButton("Add");
 		JButton cancelEvent = new JButton("Cancel");
 		this.add(confirmEvent);
 		this.add(cancelEvent);
 
 		Listener add = new Listener(6,this,data);
 		Listener cancel = new Listener(7,this,data);
 
 		confirmEvent.addActionListener(add);
 		cancelEvent.addActionListener(cancel);
 	}
 	
 	public int GetDay()
 	{
 		JTextField j = (JTextField)inputs[1][1];
 		return Integer.parseInt(j.getText());
 	}
 	
 	public int GetMonth()
 	{
 		JTextField j = (JTextField)inputs[2][1];
 		return Integer.parseInt(j.getText());
 	}
 	
 	public int GetYear()
 	{
 		JTextField j = (JTextField)inputs[3][1];
 		return Integer.parseInt(j.getText());
 	}
 	
 	public String GetName()
 	{
 		JTextField j = (JTextField)inputs[0][1];
 		return j.getText();
 	}
 	
 	public String GetDesc()
 	{
 		JTextField j = (JTextField)inputs[8][1];
 		return j.getText();
 	}
 	
 	public String GetLocation()
 	{
 		JTextField j = (JTextField)inputs[4][1];
 		return j.getText();
 	}
 	
 	public int GetTimeMin()
 	{
 		JTextField j = (JTextField)inputs[6][1];
 		return Integer.parseInt(j.getText());
 	}
 	
 	public int GetTimeHour()
 	{
 		JTextField j = (JTextField)inputs[5][1];
 		return Integer.parseInt(j.getText());
 	}
 	
 	public float GetDuration()
 	{
 		JTextField j = (JTextField)inputs[7][1];
 		return Float.parseFloat(j.getText());
 	}
 }
 
