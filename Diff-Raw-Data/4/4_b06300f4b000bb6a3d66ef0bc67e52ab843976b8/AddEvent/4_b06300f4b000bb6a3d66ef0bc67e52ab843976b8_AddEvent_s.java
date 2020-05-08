 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 public class AddEvent extends JFrame {
 	
 	private JLabel title;
 	private JLabel dateL = new JLabel("Date (mm-dd-yyyy)");
 	private JTextField date = new JTextField(15);
 	private JLabel fromL = new JLabel("Starting Time");
 	private JTextField from = new JTextField(15);
 	private JLabel toL = new JLabel("Ending Time");
 	private JTextField to = new JTextField(15);
 	private JLabel descripL = new JLabel("Description");
 	private JTextField descrip = new JTextField(15);
 	private JLabel locationL = new JLabel("Location");
 	private JTextField location = new JTextField(15);
 	private JButton submit = new JButton("Confirm Event");
 	private JFrame thisThing = this;
 	
 	AddEvent(StoreData data){
 		title = new JLabel("Adding Event: " + data.getName());
 		title.setFont(new Font("Serif", Font.PLAIN, 22));
 		final StoreData storeData = data;
 		submit.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e){
 				
 				storeData.setLocation(getLoca());
 				storeData.setDate(getDate());
 				storeData.setSTime(getStartDate());
 				storeData.setETime(getEndDate());
 				storeData.setDescription(getDescription());
 				SendToDB storeDB = new SendToDB();
 				storeDB.runStore(storeData, 0);
 				thisThing.dispose();
 			}
 		});


 		JPanel eventPanel = new JPanel();
 		eventPanel.setLayout(new GridBagLayout());
 		GridBagConstraints s = new GridBagConstraints();
 		s.anchor = GridBagConstraints.NORTH;
 		s.gridx = 0;
 		s.gridy = 0;
 		eventPanel.add(title, s);
 		s.gridx = 0;
 		s.gridy = 1;
 		eventPanel.add(dateL, s);
 		s.gridx = 0;
 		s.gridy = 2;
 		eventPanel.add(date, s);
 		s.gridx = 0;
 		s.gridy = 3;
 		eventPanel.add(fromL, s);
 		s.gridx = 0;
 		s.gridy = 4;
 		eventPanel.add(from, s);
 		s.gridx = 0;
 		s.gridy = 5;
 		eventPanel.add(toL, s);
 		s.gridx = 0;
 		s.gridy = 6;
 		eventPanel.add(to, s);
 		s.gridx = 0;
 		s.gridy = 7;
 		eventPanel.add(descripL, s);
 		s.gridx = 0;
 		s.gridy = 8;
 		eventPanel.add(descrip, s);
 		s.gridx = 0;
 		s.gridy = 9;
 		eventPanel.add(locationL, s);
 		s.gridx = 0;
 		s.gridy = 10;
 		eventPanel.add(location, s);
 		s.gridx = 0;
 		s.gridy = 11;
 		eventPanel.add(submit, s);
 		
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.add(eventPanel);
 		this.setSize(300,400);
 		this.setVisible(true);
 		this.setResizable(false);
 		
 	}
 	
 	public String getDate(){
 		return date.getText();
 	}
 	
 	public String getStartDate(){
 		return from.getText();
 	}
 	
 	public String getEndDate(){
 		return to.getText();
 	}
 	
 	public String getDescription(){
 		return descrip.getText();
 	}
 	
 	public String getLoca(){
 		return location.getText();
 	}
 	
 	void addConfirmListener(ActionListener listenForConfirm){
 		submit.addActionListener(listenForConfirm);
 	}
 	
 }
