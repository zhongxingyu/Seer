 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 
 public class MemberManagement extends JPanel{
 	
 	JLabel details;
 	JPanel center,smo,addrm,modify,tableresult;
 	
 	JLabel FirstLabel,LastLabel,yearLabel,joinLabel,positionLabel,filterLabel;
 	JTextField FirstText,LastText,yearText,joinText,positionText;
 	TextField filterText;
 	JButton backButton,displaylist,backButton2,backButton3,addrmButton,filterButton,commitButton;
 	JButton backButton4;
 	JPanel smo_below;
 	JPanel addrm_above;
 	JTable table;
 	JScrollPane tablepane,resultpane;
 	String filter=new String();
 		
 	TableModel tableModel;
 	
 	AbstractTableModel change;
 	GridBagConstraints constraints;
 	
 	private static final MemberManagement singleton=new MemberManagement();
 	public static MemberManagement getSingleton(){
 		return singleton;
 		
 	}
 	
 	MemberManagement(){
 		
 		setLayout(new BorderLayout());
 		constraints=new GridBagConstraints();
 		
 		
 		
 		backButton=new JButton("Back to mainpage");
 		add(backButton,BorderLayout.NORTH);
 		backButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"mainPage");
 			}
 		});
 		
 		
 		int i=0;
 		Object [][] display=new Object[10][10];
 		String[] colHeads={"ID","FirstName","LastName","JoinDate","Position","Address"}; 
 		ResultSet testResult;
 		Konnection test= Konnection.getSingleton();
 		String testQuery="Select * from members;";
 		testResult=test.query(testQuery);
 		try{
 			
 			while(testResult.next()){
 		
 				display[i][0]=testResult.getString("ID");
 				display[i][1]=testResult.getString("FirstName");
 				display[i][2]=testResult.getString("LastName");
 				display[i][3]=testResult.getString("JoinDate");
 				display[i][4]=testResult.getString("Position");
 				display[i][5]=testResult.getString("Address");
 				
 				System.out.println(display[i][0]+" "+display[i][1]+" "+display[i][2]+" "+display[i][3]+" ");
 				i++;
 			}
 		}
 		catch(SQLException e){
 			e.printStackTrace();
 		}
 		
 		table=new JTable(display,colHeads);
 		tablepane=new JScrollPane(table,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		
 		smo=new JPanel();
 		
 		smo.setLayout(new BorderLayout());
 		smo.add(tablepane,BorderLayout.NORTH);
 		
 		smo_below=new JPanel();
 		
 		smo_below.add(filterButton=new JButton("Filter Name: "));
 		smo_below.add(filterText=new TextField(20));
 		
 		
 		tableModel =table.getModel();
 	   
 		filterText.addTextListener(new TextListener(){
 			public void textValueChanged(TextEvent te){
 				filter=filterText.getText();
 				((AbstractTableModel)tableModel).fireTableDataChanged();
 				
 			}
 		});
 		
 		
 		tableModel.addTableModelListener(new TableModelListener(){
 			
 			public void tableChanged(TableModelEvent a){
 				
 					int i=0;
 					JTable result;
 					
 					//DefaultTableModel model=new DefaultTableModel();
 					
 					Object [][] display1=new Object[10][10];
					String[] colHeads1={"ID","FirstName","LastName","JoinDate","Position","Address"}; 
 					ResultSet testResult;
 					Konnection test= Konnection.getSingleton();
 					String testQuery="Select * from members where FirstName like '"+filter+"%';";
 					testResult=test.query(testQuery);
 					try{
 					
 						while(testResult.next()){
 				
 							display1[i][0]=testResult.getString("ID");
 							display1[i][1]=testResult.getString("FirstName");
 							display1[i][2]=testResult.getString("LastName");
 							display1[i][3]=testResult.getString("JoinDate");
 							display1[i][4]=testResult.getString("Position");
 							display1[i][5]=testResult.getString("Address");
 							
 							System.out.println(display1[i][0]+" "+display1[i][1]+" "+display1[i][2]+" "+display1[i][3]+" ");
 						
 							//smo_below.add(new JLabel(display1[i][0]+" "+display1[i][1]+" "+display1[i][2]+" "+display1[i][3]+" "),BorderLayout.CENTER);	
 							
 							i++;
 						}
						
						smo.remove(tablepane);
						table=new JTable(display1,colHeads1);
						tablepane=new JScrollPane(table,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						smo.add(tablepane,BorderLayout.NORTH);
						smo.validate();
 					}
 					catch(SQLException e){
 						e.printStackTrace();
 					}
 					
 					
 					
 				}
 		
 		});
 		
 		smo.add(smo_below,BorderLayout.CENTER);
 		
 		MainFrame.getSingleton().mainPanel.add(smo,"tablepane");
 		
 		smo.add(backButton2=new JButton("Back"),BorderLayout.SOUTH);
 		
 		center=new JPanel();
 		center.setLayout(new GridLayout(0,3));
 		
 		center.add(displaylist=new JButton("display the member list"));
 		center.add(addrmButton=new JButton("Add a Member"));
 		center.add(filterButton=new JButton("Modify/Remove a Member"));
 	    add(center,BorderLayout.CENTER);
 		
 		addrm=new JPanel();
 		addrm.setLayout(new BorderLayout());
 		addrm_above=new JPanel();
 		
 		addrm_above.setLayout(new GridBagLayout());
 		constraints.gridx=0;
 		constraints.gridy=0;
 		addrm_above.add(FirstLabel=new JLabel("First Name:"),constraints);
 		constraints.gridx=1;
 		constraints.gridy=0;
 		addrm_above.add(FirstText=new JTextField(20),constraints);
 		constraints.gridx=0;
 		constraints.gridy=1;
 	    addrm_above.add(LastLabel=new JLabel("Last Name:"),constraints);
 	    constraints.gridx=1;
 		constraints.gridy=1;
 	    addrm_above.add(LastText=new JTextField(20),constraints);
 	    constraints.gridx=0;
 		constraints.gridy=2;
 	    addrm_above.add(yearLabel=new JLabel("Year:"),constraints);
 	    constraints.gridx=1;
 		constraints.gridy=2;
 	    addrm_above.add(yearText=new JTextField(20),constraints);
 	    constraints.gridx=0;
 		constraints.gridy=3;
 	    addrm_above.add(joinLabel=new JLabel("Join date:"),constraints);
 	    constraints.gridx=1;
 		constraints.gridy=3;
 	    addrm_above.add(joinText=new JTextField(20),constraints);
 	    constraints.gridx=0;
 		constraints.gridy=4;
 	    addrm_above.add(positionLabel=new JLabel("Position:"),constraints);
 	    constraints.gridx=1;
 		constraints.gridy=4;
 	    addrm_above.add(positionText=new JTextField(20),constraints);
 	    constraints.gridx=0;
 	    constraints.gridy=5;
 	    
 	    constraints.gridheight=2;
 	    constraints.gridwidth=2;
 	    addrm_above.add(commitButton=new JButton("commit to database"),constraints);
 	    addrm.add(backButton3=new JButton("Back"),BorderLayout.SOUTH);
 	    addrm.add(addrm_above,BorderLayout.CENTER);
 	    
 	    
 	    MainFrame.getSingleton().mainPanel.add(addrm,"add");
 	    
 	    
 	    commitButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 								if(FirstText.getText().isEmpty()||LastText.getText().isEmpty()||yearText.getText().isEmpty()||positionText.getText().isEmpty()||joinText.getText().isEmpty()){
 									constraints.gridx=0;
 									constraints.gridy=6;
 									
 									constraints.gridheight=2;
 									constraints.gridwidth=2;
 									
 									addrm_above.add(new JLabel("Invalid Entries"),constraints);
 								}
 								else{
 									
 									
 									
 								}
 			}
 		});
 			
 	    displaylist.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"tablepane");
 			}
 		});
 			
 		backButton2.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				table.setVisible(true);
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"mainPage");
 			
 			
 			}
 			
 		});
 
 		backButton3.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"mainPage");
 			}
 			
 		});
 		
 		addrmButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"add");
 			}
 			
 		});
 		
 		filterButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"mainPage");
 			}
 			
 		});
 		
 	
 	}
 }
