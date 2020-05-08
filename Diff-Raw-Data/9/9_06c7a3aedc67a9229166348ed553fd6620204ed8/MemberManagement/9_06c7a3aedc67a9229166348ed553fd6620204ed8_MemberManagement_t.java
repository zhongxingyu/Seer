 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 
 public class MemberManagement extends JPanel{
 	
 	JLabel details;
 	JPanel center,mainTablePanel,addrm,modify,tableresult;
 	
 	JLabel FirstLabel,LastLabel,yearLabel,joinLabel,positionLabel,filterLabel,filter2Label,InvalidMessage,addressLabel,ValidMessage;
 	JTextField FirstText,LastText,yearText,joinText,positionText,addressText;
 	TextField filterText,filter2Text;
 	JButton backButton,displaylist,backButton2,backButton3,backButton5,addrmButton,filterButton,commitButton;
 	JButton backButton4,deleteMember;
 	JLabel delMessage,baddelMessage,values,tick,id;
 	JPanel main_Table_below;
 	JPanel addrm_above,modifyPanel,modifyAbove,modifyBelow;
 	JTable table;
 	JComboBox positionCombo;
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
 		
 		
 		//back button which gets you back to the main page, this button is on top of the page
 		backButton=new JButton("Back to mainpage");
 		add(backButton,BorderLayout.NORTH);
 		backButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"mainPage");
 			}
 		});
 		
 		//Main page of the Member Management panel buttons+Icons !
 				center=new JPanel();
 				center.setLayout(new GridLayout(0,3));
 				
 				
 				center.add(displaylist=new JButton("Display the member list",new ImageIcon("Bag-icon.png")));
 				displaylist.setHorizontalTextPosition(SwingConstants.CENTER);
 				displaylist.setVerticalTextPosition(SwingConstants.BOTTOM);
 
 				center.add(addrmButton=new JButton("Add a Member",new ImageIcon("Backup-icon.png")));
 				addrmButton.setHorizontalTextPosition(SwingConstants.CENTER);
 				addrmButton.setVerticalTextPosition(SwingConstants.BOTTOM);
 				
 				center.add(filterButton=new JButton("Remove a Member",new ImageIcon("Recycle-Bin-Full-2-icon.png")));
 				filterButton.setHorizontalTextPosition(SwingConstants.CENTER);
 				filterButton.setVerticalTextPosition(SwingConstants.BOTTOM);
 				
 			    add(center,BorderLayout.CENTER);
 			    //Render the Panel controlling the deletion of members
 				
 				modifyPanel=new JPanel();
 			    modifyPanel.setLayout(new BorderLayout());
 			    MainFrame.getSingleton().mainPanel.add(modifyPanel,"delete");
 			    
 			    GridBagConstraints constraints2=new GridBagConstraints();
 			    modifyPanel.add(backButton5=new JButton("Back"),BorderLayout.SOUTH);
 				modifyAbove=new JPanel();
 				modifyAbove.setLayout(new GridBagLayout());
 				constraints2.gridy=0;
 				modifyAbove.add(filter2Label=new JLabel("Enter ID: "),constraints2);
 				modifyAbove.add(filter2Text=new TextField(20),constraints2);
 				constraints2.gridy=1;
 				constraints2.gridwidth=2;
 				modifyAbove.add(deleteMember=new JButton("Delete Member"),constraints2);
 				constraints2.gridy=2;
 				modifyAbove.add(delMessage=new JLabel("Delete Successful !"),constraints2);
 				delMessage.setVisible(false);
 				constraints2.gridy=3;
 				modifyAbove.add(baddelMessage=new JLabel("Delete Unsuccessful !"),constraints2);
 				baddelMessage.setVisible(false);
 				modifyPanel.add(modifyAbove,BorderLayout.CENTER);
 				
 				deleteMember.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent ae){
 						String val=new String();
 						Konnection test= Konnection.getSingleton();
 						int r1=test.update("Delete from members where ID='"+filter2Text.getText()+"';");
 						if(r1>0){
 							baddelMessage.setVisible(false);
 							delMessage.setVisible(true);
 							((AbstractTableModel)tableModel).fireTableDataChanged();
 						}else{
 							delMessage.setVisible(false);
 							baddelMessage.setVisible(true);
 						}
 						modifyAbove.validate();
 						
 					}
 				});
 				
 				filterButton.addActionListener(new ActionListener(){
 					public void actionPerformed(ActionEvent ae){
 						if(login.currentMember.Authority>2){
 							MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"ERROR");
 						}
 						else{
 							delMessage.setVisible(false);
 							baddelMessage.setVisible(false);
 							MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"delete");
 						}
 					}
 					
 				});	    
 		//Table Creation and SQL queries for the same 
 		int i=0;
 		Object [][] display=new Object[130][10];
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
 		
 		//'table' is instantiated here
 		table=new JTable(display,colHeads);
 		tablepane=new JScrollPane(table,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		
 		mainTablePanel=new JPanel();
 		
 		mainTablePanel.setLayout(new BorderLayout());
 		mainTablePanel.add(tablepane,BorderLayout.NORTH);
 		
 		main_Table_below=new JPanel();
 		
 		//Filter Box and Label for name Filtering
 		main_Table_below.add(filterButton=new JButton("Filter Name: "));
 		main_Table_below.add(filterText=new TextField(20));
 		
 		values=new JLabel("");
 		main_Table_below.add(values);
 	
 		
 	    filterText.addTextListener(new TextListener(){
 			public void textValueChanged(TextEvent te){
 				filter=filterText.getText();
 				((AbstractTableModel)tableModel).fireTableDataChanged();
 				
 			}
 		});
 		//for List Selection adding listener 
 	    table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
 	    	public void valueChanged(ListSelectionEvent e){
 	    		
 	 
 	    		int [] list=table.getSelectedRows();
 	    		String lists="";
 	    		for(int x:list){
 	    			lists=lists+" "+table.getValueAt(x,0);
 	    		}
 	    		
 	    		
 	    		values.setText("The selected member ids are :"+lists);
 	    		main_Table_below.validate();
 	    	}
 	    });
 	    
 	    //using tableModel(abstract class) which controls the data in a table to fire data changes 
 	    tableModel =table.getModel();
 		tableModel.addTableModelListener(new TableModelListener(){
 			public void tableChanged(TableModelEvent a){
 				
 					int i=0;
 					JTable result;
 					
 					//DefaultTableModel model=new DefaultTableModel();
 					
 					Object [][] display1=new Object[30][10];
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
 						
 						mainTablePanel.remove(tablepane);
 						table=new JTable(display1,colHeads1);
 						table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
 						    	public void valueChanged(ListSelectionEvent e){
 						    		
 						 
 						    		int [] list=table.getSelectedRows();
 						    		String lists="";
 						    		for(int x:list){
 						    			lists=lists+" "+table.getValueAt(x,0);
 						    		}
 						    		
 						    		
 						    		values.setText("The selected member ids are :"+lists);
 						    		main_Table_below.validate();
 						    	}
 						    });
 						tablepane=new JScrollPane(table,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 						mainTablePanel.add(tablepane,BorderLayout.NORTH);
 						mainTablePanel.validate();
 					}
 					catch(SQLException e){
 						e.printStackTrace();
 					}
 					
 					
 					
 				}
 		
 		});
 		
 		//adding table panel to main set of panels
 		mainTablePanel.add(main_Table_below,BorderLayout.CENTER);
 		MainFrame.getSingleton().mainPanel.add(mainTablePanel,"tablepane");
 		mainTablePanel.add(backButton2=new JButton("Back"),BorderLayout.SOUTH);
 		
 		
 		
 	    
 	   
 		
 		//Rendering the Add a Member panel 
 		addrm=new JPanel();
 		addrm.setLayout(new BorderLayout());
 		addrm_above=new JPanel();
 		//The form for adding members
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
 	    constraints.gridx=2;
 		constraints.gridy=4;
 	    addrm_above.add(positionText=new JTextField(20),constraints);
 	    constraints.gridx=1;
 	    constraints.gridy=4;
 	    positionText.setVisible(false);
 	    addrm_above.add(positionCombo=new JComboBox(),constraints);
 	    constraints.gridx=0;
 	    constraints.gridy=5;
 	    addrm_above.add(addressLabel=new JLabel("Address: "),constraints);
 	    constraints.gridx=1;
 	    constraints.gridy=5;
 	    addrm_above.add(addressText=new JTextField(20),constraints);
 	    constraints.gridx=0;
 	    constraints.gridy=6;
 	    
 	    positionCombo.addItem("Choose position");
 	    positionCombo.addItem("Board Member");
 	    positionCombo.addItem("Management Committee");
 	    positionCombo.addItem("Member");
 	    
 	    positionCombo.addItemListener(new ItemListener(){
 	    	public void itemStateChanged(ItemEvent ie){
 	    		if(((String)positionCombo.getSelectedItem()).compareToIgnoreCase("Board Member")==0){
 	    			positionText.setVisible(true);
 	    			addrm_above.validate();
 	    		}
 	    		else{
 	    			positionText.setVisible(false);
 	    			addrm_above.validate();
 	    		}
 	    	}
 	    });
 	    constraints.gridheight=2;
 	    constraints.gridwidth=2;
 	    addrm_above.add(commitButton=new JButton("Commit to Database"),constraints);
 	    addrm.add(backButton3=new JButton("Back"),BorderLayout.SOUTH);
 	    constraints.gridx=0;
 		constraints.gridy=8;
 		
 		constraints.gridheight=2;
 		constraints.gridwidth=2;
 		addrm_above.add(InvalidMessage=new JLabel("Invalid Entries"),constraints);
 		InvalidMessage.setVisible(false);
 		constraints.gridy=10;
 		
 		
 		ValidMessage=new JLabel(new ImageIcon());
 		addrm_above.add(ValidMessage,constraints);
 	    addrm.add(addrm_above,BorderLayout.CENTER);
 	    ValidMessage.setVisible(false);
 	    
 	    MainFrame.getSingleton().mainPanel.add(addrm,"add");
 	    
 	    
 	    commitButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 								if(FirstText.getText().isEmpty()||LastText.getText().isEmpty()||joinText.getText().isEmpty()){
 									InvalidMessage.setVisible(true);
 									System.out.println("Invalid Entries !!");
 									addrm_above.validate();
 								}
 								else{
 									InvalidMessage.setVisible(false);
 					
 									int auth=0;
 									String pos=new String();
 									Konnection test= Konnection.getSingleton();
 									if(((String)positionCombo.getSelectedItem()).compareToIgnoreCase("Board Member")==0){
 										auth=2;
 										pos=positionText.getText();
 									}
 									else if(((String)positionCombo.getSelectedItem()).compareToIgnoreCase("Management Committee")==0){
 										auth=3;
 										pos="Management Committee";
 									}
 									else if(((String)positionCombo.getSelectedItem()).compareToIgnoreCase("Member")==0){
 										auth=4;
 										pos="Member";
 									}
 									
									String testQuery="Insert into members values(null"+",'"+FirstText.getText()+"','"+LastText.getText()+"','"+joinText.getText()+"','"+pos+"',LOWER('"+(FirstText.getText()).charAt(0)+(LastText.getText()).charAt(0)+"'),'"+addressText.getText()+"','"+auth+"');";
 									int r=test.update(testQuery);
 									if(r>0){
 										
 											String val=new String();
 											ResultSet rs=test.query("Select max(ID) as ID from members;");
 											try{
 												rs.next();
 											val=rs.getString("ID");
 											System.out.println(val);
 											}catch(Exception q){
 												q.printStackTrace();
 											}
 											
 										
 										
 										ValidMessage.setVisible(true);
 										id=new JLabel("<html><b>Your ID is "+val+"</b></html>");
 										tick=new JLabel(new ImageIcon("Finished-icon.png"));
 										constraints.gridx=0;
 										constraints.gridy=12;
 										constraints.gridheight=1;
 										constraints.gridwidth=1;
 										addrm_above.add(id,constraints);
 										constraints.gridx=1;
 										constraints.gridy=12;
 										addrm_above.add(tick,constraints);
 										addrm_above.validate();
 										((AbstractTableModel)tableModel).fireTableDataChanged();
 										
 									}
 									else{
 									InvalidMessage.setVisible(true);
 									System.out.println("Invalid Entries !!");
 									addrm_above.validate();
 									
 									}
 								}
 			}
 		});
 			
 	    displaylist.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				values.setText("");
 				main_Table_below.validate();
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"tablepane");
 			}
 		});
 			
 		backButton2.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				table.setVisible(true);
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"membermanagement");
 			
 			
 			}
 			
 		});
 		backButton5.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				table.setVisible(true);
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"membermanagement");
 			
 			
 			}
 			
 		});
 	
 		backButton3.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"membermanagement");
 			}
 			
 		});
 		
 		addrmButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent ae){
 				if(login.currentMember.Authority>3){
 					MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"ERROR");
 				}
 				else{
 					positionText.setVisible(false);
 					if(id!=null||tick!=null){
 						id.setVisible(false);
 						tick.setVisible(false);
 					}
 				MainFrame.getSingleton().lay.show(MainFrame.getSingleton().mainPanel,"add");
 				}
 			}
 			
 		});
 		
 		
 		
 	
 	}
 }
