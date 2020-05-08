 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.sql.*;
 
 public class login extends JPanel{
 	
 	//JFrame frame;
 	public static Member currentMember=new Member();
 	JPanel above,center;
 	JLabel incorrect_input;
 	JTextField username;
 	JPasswordField password;
 	JButton submit,reset;
 	String id;
 	
 	private final static login singleton=new login();
 	public static login getSingleton(){
 		return singleton;
 	}
 	
 login(){
 		
 		setLayout(new BorderLayout());
 		above=new JPanel();
 		above.setLayout(new GridBagLayout());
 		GridBagConstraints work=new GridBagConstraints();
 		
 		work.ipady=100;
 		above.add(new JLabel("<html><font size=7 face=nowdance ><u>CLUB MANAGEMENT SOFTWARE</u></font></html>",JLabel.CENTER),work);
 		
 	    center=new JPanel();
 	    center.setLayout(new GridBagLayout());
 	    work.ipadx=100;
 	    work.ipady=0;
 	    
 	    work.gridx=0;
 	    work.gridy=0; 
 	    work.anchor=GridBagConstraints.NORTH;
 	    center.add(new JLabel("<html><font face=nowdance>USERNAME:  </font></html>"),work);
 	    work.gridx=1;
 	    work.gridy=0;
 	    center.add(username=new JTextField(20),work);
 	    work.gridx=0;
 	    work.gridy=1;
 	    center.add(new JLabel("<html><font face=nowdance>PASSWORD:  </font></html>"),work);
 	    work.gridx=1;
 	    work.gridy=1;
 	    center.add(password=new JPasswordField(20),work);
 	    password.setEchoChar('*');
 	    work.gridx=0;
 	    work.gridy=2;
 	    work.gridheight=2;
 	    work.gridwidth=1;
 	    work.ipadx=40;
 	    work.ipady=20;
 	    center.add(submit=new JButton("<html><font color=#5F9EA0 face=nowdance>SUBMIT</font></html>"),work);
 	    work.gridx=1;
 	    work.gridy=2;
 	    work.ipadx=40;
 	    work.ipady=20;
 	    center.add(reset=new JButton("<html><font color=#5F9EA0 face=nowdance>RESET</font></html>"),work);
 	    work.gridx=0;
 	    work.gridy=4;
 	    work.gridheight=2;
 	    work.gridwidth=2;
 	    work.ipadx=200;
 	    work.ipady=50;
 		center.add(incorrect_input=new JLabel("<html><font color=red size=4 face=nowdance>Wrong username or password</font></html>",JLabel.CENTER),work);
 		incorrect_input.setVisible(false);
 		
 		add(center,BorderLayout.CENTER);
 		add(above,BorderLayout.NORTH);
 		
 		
 		submit.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				String pass=String.valueOf(password.getPassword());
 				id=username.getText();
 				String loginQuery="Select * from members where ID='"+id+"';";
 				ResultSet memberDetails;
 				
 				memberDetails=Konnection.getSingleton().query(loginQuery);
 				try{
 					memberDetails.next();
 					if(pass.equals(memberDetails.getString("Password"))){
 						currentMember.MemberId=memberDetails.getInt("ID");
 						currentMember.FirstName=memberDetails.getString("FirstName");
 						currentMember.LastName=memberDetails.getString("LastName");
 						currentMember.Address=memberDetails.getString("Address");
 						currentMember.Position=memberDetails.getString("Position");
 						currentMember.Authority=memberDetails.getInt("Authority");
 						currentMember.JoinDate=memberDetails.getString("JoinDate");
 						mainPage.getSingleton().displayMemberInfo();
 						
 						currentMember.ConsoleOutput();
 						MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"mainPage");
 						Notifications.getSingleton().makePublicNoteGUI();
 						Notifications.getSingleton().makePrivateNoteGUI();
 						incorrect_input.setVisible(false);
 						reset.doClick();
 					}
 					else {
 						incorrect_input.setVisible(true);
 					}
 				}
 				catch(SQLException e){
					incorrect_input.setVisible(true);
 				}
 				
 				
 			}
 		});
 		reset.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 			  password.setText("");
 			  username.setText("");
 			  incorrect_input.setVisible(false);
 			}
 		});
 	}
 
 
 }
