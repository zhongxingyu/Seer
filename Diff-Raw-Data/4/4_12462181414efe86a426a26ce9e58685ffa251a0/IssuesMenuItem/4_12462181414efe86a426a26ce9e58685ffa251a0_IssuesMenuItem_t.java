 package dk.frv.eavdam.menus;
 
 import com.bbn.openmap.gui.OpenMapFrame;
 import dk.frv.eavdam.data.AISDatalinkCheckIssue;
 import dk.frv.eavdam.data.AISDatalinkCheckRule;
 import dk.frv.eavdam.data.AISDatalinkCheckSeverity;
 import dk.frv.eavdam.data.AISFrequency;
 import dk.frv.eavdam.data.AISStation;
 import dk.frv.eavdam.data.AISTimeslot;
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.utils.DBHandler;
 import dk.frv.eavdam.utils.HealthCheckHandler;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 public class IssuesMenuItem extends JMenuItem {
 
     public static final long serialVersionUID = 1L;
 
 	public static int ISSUES_WINDOW_WIDTH = 1024;
 	public static int ISSUES_WINDOW_HEIGHT = 1000;	
 	
 	public static List<AISDatalinkCheckIssue> issues = null;
 			
     public IssuesMenuItem(EavdamMenu eavdamMenu) {        
         super("AIS VHF Datalink Issues");                
         addActionListener(new IssuesMenuItemActionListener(eavdamMenu));
     }
 	
 }
  
 class IssuesMenuItemActionListener implements ActionListener {
 
     private EavdamMenu eavdamMenu;
     private JDialog dialog;
 		
 	private JButton exportToCSVButton;
 	private ExportIssuesToCSVDialog exportIssuesToCSVDialog;	
     
 	public IssuesMenuItemActionListener(EavdamMenu eavdamMenu) {
         super();
         this.eavdamMenu = eavdamMenu;
     }
      
     public void actionPerformed(ActionEvent e) {
         
         if (e.getSource() instanceof IssuesMenuItem) {
 						
 			Toolkit toolkit = Toolkit.getDefaultToolkit();
 			Dimension dimension = toolkit.getScreenSize();
 
 			if (dimension.width-100 < IssuesMenuItem.ISSUES_WINDOW_WIDTH) {
 				IssuesMenuItem.ISSUES_WINDOW_WIDTH = dimension.width-100;
 			}
 			if (dimension.width-100 < IssuesMenuItem.ISSUES_WINDOW_WIDTH) {
 				IssuesMenuItem.ISSUES_WINDOW_WIDTH = dimension.width-100;
 			}
 			if (dimension.height-100 < IssuesMenuItem.ISSUES_WINDOW_HEIGHT) {
 				IssuesMenuItem.ISSUES_WINDOW_HEIGHT = dimension.height-100;
 			}
 			if (dimension.height-100 < IssuesMenuItem.ISSUES_WINDOW_HEIGHT) {
 				IssuesMenuItem.ISSUES_WINDOW_HEIGHT = dimension.height-100;
 			}		
 		
             dialog = new JDialog(eavdamMenu.getOpenMapFrame(), "AIS VHF Datalink Issues", false);
 			
 			JScrollPane scrollPane = getScrollPane();
 			scrollPane.setBorder(BorderFactory.createEmptyBorder());
 			
 			JPanel containerPane = new JPanel();
 			containerPane.setBorder(BorderFactory.createEmptyBorder());
 			containerPane.setLayout(new BorderLayout());		 
 			containerPane.add(scrollPane, BorderLayout.NORTH);			
 
 			exportToCSVButton = new JButton("Export issues");
 			exportToCSVButton.setToolTipText("Exports issues to CSV file format");
 			exportToCSVButton.addActionListener(this);			
 			JPanel buttonPanel = new JPanel();
 			buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
 			buttonPanel.add(exportToCSVButton);
 			dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
 			
 			dialog.getContentPane().add(containerPane, BorderLayout.CENTER);
 
             Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
             dialog.setBounds((int) screenSize.getWidth()/2 - IssuesMenuItem.ISSUES_WINDOW_WIDTH/2,
                 (int) screenSize.getHeight()/2 - IssuesMenuItem.ISSUES_WINDOW_HEIGHT/2,
 				IssuesMenuItem.ISSUES_WINDOW_WIDTH, IssuesMenuItem.ISSUES_WINDOW_HEIGHT);
             dialog.setVisible(true);	
 		
 		} else if (e.getSource() == exportToCSVButton) {
 
             exportIssuesToCSVDialog = new ExportIssuesToCSVDialog(dialog);
 			
      		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
             exportIssuesToCSVDialog.setBounds((int) screenSize.getWidth()/2 - ExportIssuesToCSVDialog.WINDOW_WIDTH/2,
 				(int) screenSize.getHeight()/2 - ExportIssuesToCSVDialog.WINDOW_HEIGHT/2, ExportIssuesToCSVDialog.WINDOW_WIDTH,
 				ExportIssuesToCSVDialog.WINDOW_HEIGHT);
             exportIssuesToCSVDialog.setVisible(true);	
 		}
 		
 	}
 
 	private JScrollPane getScrollPane() {
 
 		if (IssuesMenuItem.issues == null) {
 			EAVDAMData data = DBHandler.getData();
 			IssuesMenuItem.issues = data.getAISDatalinkCheckIssues();
 		}
 		
 		//Put them into the correct order.
 		//TODO Change this as you will; the list should be ordered when it is returned from DBHandler but for some reason it isn't
     	List<AISDatalinkCheckIssue> tempList = new ArrayList<AISDatalinkCheckIssue>();
     	for(AISDatalinkCheckIssue issue : IssuesMenuItem.issues){
     		if(tempList.size() == 0){
     			tempList.add(issue);
     		}else{
     			for(int i = 0 ; i < tempList.size(); ++i){
     				if(!issue.isAcknowledged() && tempList.get(i).isAcknowledged()){
     					tempList.add(i, issue);
     					break;
     				}
     				
     				if(i == tempList.size() - 1){
     					tempList.add(issue);
     					break;
     				}
     			}
     		}
     	}
     	IssuesMenuItem.issues = tempList;
 				
 		// XXX: FOR TESTING
 		/*
 		List<AISStation> testStations = new ArrayList<AISStation>();
 		testStations.add(new AISStation("VTT", "Test station", (double) 60, (double) 20));
 		testStations.add(new AISStation("VTT", "Test station 2", (double) 59, (double) 19));
 		List<AISTimeslot> testTimeslots = new ArrayList<AISTimeslot>();
 		testTimeslots.add(new AISTimeslot(AISFrequency.AIS1, 100, false, null, null, null, null));
 		testTimeslots.add(new AISTimeslot(AISFrequency.AIS2, 101, false, null, null, null, null));			
 		testTimeslots.add(new AISTimeslot(AISFrequency.AIS1, 102, false, null, null, null, null));	
 		testTimeslots.add(new AISTimeslot(AISFrequency.AIS2, 103, false, null, null, null, null));	
 		testTimeslots.add(new AISTimeslot(AISFrequency.AIS1, 104, true, null, null, null, null));	
 		AISDatalinkCheckIssue test = new AISDatalinkCheckIssue(1, AISDatalinkCheckRule.RULE4, AISDatalinkCheckSeverity.SEVERE, testStations, testTimeslots);
 		//test.setAcknowledged(true);
 		if (issues == null || issues.isEmpty()) {
 			issues = new ArrayList<AISDatalinkCheckIssue>();			
 			issues.add(test);
 		}
 		*/
 
 		JPanel panel = new JPanel();
 		panel.setLayout(new GridBagLayout());                  
 		
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 0;    
 		//c.weightx = 0.5;
 		//c.weighty = 0.5;
 		c.anchor = GridBagConstraints.LINE_START;
 		c.insets = new Insets(5,5,5,5);
 		
 		if (IssuesMenuItem.issues == null || IssuesMenuItem.issues.isEmpty()) {
 		
 			panel.add(new JLabel("<html><body><h1>AIS VHF Datalink Issues</h1></body></html>"), c);
 			c.anchor = GridBagConstraints.LINE_START;
 			
 			c.gridy = 1;
 			panel.add(new JLabel("No issues."), c);
 		
 		} else {
 		
 			//c.anchor = GridBagConstraints.CENTER;		
 			c.gridwidth = 7;
 			panel.add(new JLabel("<html><body><h1>AIS VHF Datalink Issues</h1></body></html>"), c);
 			c.anchor = GridBagConstraints.LINE_START;
 			
 			c.gridy = 1;
 			c.insets = new Insets(5,0,15,5);			
 			String numberOfIssuesLabel = "Number of issues: " + IssuesMenuItem.issues.size();
 			if (IssuesMenuItem.issues.size() > 100) {
 				numberOfIssuesLabel += " (displaying 100 first)";
 			}
 			panel.add(new JLabel(numberOfIssuesLabel), c);			
 			
 			c.gridy = 2;
 			c.gridwidth = 1;
 			c.fill = GridBagConstraints.BOTH;
 			c.insets = new Insets(0,0,0,0);
 			JLabel ruleViolatedTitleLabel = new JLabel(" Rule violated  ");
 			ruleViolatedTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
 			ruleViolatedTitleLabel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
 			panel.add(ruleViolatedTitleLabel, c);
 			c.gridx = 1;           		
 			JLabel severityTitleLabel = new JLabel(" Severity  ");
 			severityTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
 			severityTitleLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.BLACK));
 			panel.add(severityTitleLabel, c);
 			c.gridx = 2;
 			JLabel aisStationsInvolvedTitleLabel = new JLabel(" AIS stations involved  ");
 			aisStationsInvolvedTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
 			aisStationsInvolvedTitleLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.BLACK));
 			panel.add(aisStationsInvolvedTitleLabel, c);				
 			c.gridx = 3;
 			JLabel timeslotsInvolvedTitleLabel = new JLabel(" Timeslots involved  ");
 			timeslotsInvolvedTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
 			timeslotsInvolvedTitleLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.BLACK));
 			panel.add(timeslotsInvolvedTitleLabel, c);
 			c.gridx = 4;   
 			JLabel acknowledgedTitleLabel = new JLabel(" Acknowledged  ");
 			acknowledgedTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
 			acknowledgedTitleLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.BLACK));
 			panel.add(acknowledgedTitleLabel, c);				
 			c.gridx = 5;         
 			JLabel acknowledgeButtonTitleLabel = new JLabel("  ");
 			acknowledgeButtonTitleLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.BLACK));
 			panel.add(acknowledgeButtonTitleLabel, c);
 			/*
 			c.gridx = 6;   
 			JLabel deleteTitleLabel = new JLabel("  ");
 			deleteTitleLabel.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.BLACK));
 			panel.add(deleteTitleLabel, c);
 			*/
 			
 			int count = 0;
 			
 			for (AISDatalinkCheckIssue issue : IssuesMenuItem.issues) {
 			
 				if (count == 100) {
 					break;
 				} else {
 					count++;
 				}
 			
 				int id = issue.getId();
 				AISDatalinkCheckRule ruleViolated = issue.getRuleViolated();
 				AISDatalinkCheckSeverity severity = issue.getSeverity();
 				List<AISStation> involvedStations = issue.getInvolvedStations();
 				List<AISTimeslot> involvedTimeslots = issue.getInvolvedTimeslots();
 				boolean acknowledged = issue.isAcknowledged();
 			
 				c.gridy++;
 				c.gridx = 0;
 			
 				String ruleViolatedStr = "";
 				if (ruleViolated != null) {
 					if (ruleViolated == AISDatalinkCheckRule.RULE1) {
 						ruleViolatedStr = "<html><body>&nbsp;&nbsp;Conflicting stations&nbsp;&nbsp;</html></body>";
 					} else if (ruleViolated == AISDatalinkCheckRule.RULE2) {
 						ruleViolatedStr = "<html><body>&nbsp;&nbsp;Reservation, but no intended use&nbsp;&nbsp;</html></body>";
 					} else if (ruleViolated == AISDatalinkCheckRule.RULE3) {
 						ruleViolatedStr = "<html><body>&nbsp;&nbsp;Intended FATDMA use, but no&nbsp;&nbsp;<br>&nbsp;&nbsp;reservation&nbsp;&nbsp;</html></body>";
 					} else if (ruleViolated == AISDatalinkCheckRule.RULE4) {
 						ruleViolatedStr = "<html><body>&nbsp;&nbsp;Simultaneous use of several&nbsp;&nbsp;<br>&nbsp;&nbsp;frequencies&nbsp;&nbsp;</html></body>";
 					} else if (ruleViolated == AISDatalinkCheckRule.RULE5) {
 						ruleViolatedStr = "<html><body>&nbsp;&nbsp;Slots reserved outside IALA A-124&nbsp;&nbsp;<br>&nbsp;&nbsp;recommended default FATDMA schemes&nbsp;&nbsp;</html></body>";
 					} else if (ruleViolated == AISDatalinkCheckRule.RULE6) {
 						ruleViolatedStr = "<html><body>&nbsp;&nbsp;Slots reserved outside overall slot&nbsp;&nbsp;<br>&nbsp;&nbsp;pattern for fixed statons (IALA A-124)&nbsp;&nbsp;</html></body>";
 					} else if (ruleViolated == AISDatalinkCheckRule.RULE7) {					
 						ruleViolatedStr = "<html><body>&nbsp;&nbsp;Free Bandwith below 50%&nbsp;&nbsp;</html></body>";
 					}
 				}
 
 				JLabel ruleViolatedLabel = new JLabel(ruleViolatedStr);
 				ruleViolatedLabel.setFont(new Font("Arial", Font.PLAIN, 12));
 				ruleViolatedLabel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.BLACK));        
 				panel.add(ruleViolatedLabel, c);
 				
 				c.gridx = 1;
 				
 				String severityStr = "";
 				if (severity != null) {
 					if (severity == AISDatalinkCheckSeverity.SEVERE) {
 						severityStr = "  Severe  ";		
 					} else if (severity == AISDatalinkCheckSeverity.MAJOR) {
 						severityStr = "  Major  ";	
 					} else if (severity == AISDatalinkCheckSeverity.MAJOR) {
 						severityStr = "  Minor  ";	
 					}
 				}				
 				
 				JLabel severityLabel = new JLabel(severityStr);
 				severityLabel.setFont(new Font("Arial", Font.PLAIN, 12));
 				severityLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 				panel.add(severityLabel, c);		
 				
 				c.gridx = 2;
 				
 				String involvedStationsStr = "";
 				if (involvedStations != null) {
 					if (involvedStations.size() == 1) {
 						AISStation involvedStation = involvedStations.get(0);
 						involvedStationsStr = "  " + involvedStation.getOrganizationName() + ": " + involvedStation.getStationName() + "  ";
 					} else if (involvedStations.size() > 1) {
 						involvedStationsStr = "<html><body>";
 						for (int i=0; i<involvedStations.size(); i++) {
 							AISStation involvedStation = involvedStations.get(i);
 							involvedStationsStr += "&nbsp;&nbsp;" + involvedStation.getOrganizationName() + ": " + involvedStation.getStationName() + "&nbsp;&nbsp;";
 							if (i<involvedStations.size()-1) {
 								involvedStationsStr += "<br>";
 							}
 						}
 						involvedStationsStr += "</body></html>";
 					}
 				}					
 				
 				JLabel involvedStationsLabel = new JLabel(involvedStationsStr);
 				involvedStationsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
 				involvedStationsLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 				panel.add(involvedStationsLabel, c);
 			
 				c.gridx = 3;
 			
 				String involvedTimeslotsStr = "";
 				if (involvedTimeslots != null) {
 					involvedTimeslotsStr = "<html><body>";
 					for (int i=0; i<involvedTimeslots.size(); i++) {
 						if (i > 0 && i%2 == 0) {
 							involvedTimeslotsStr += "&nbsp;&nbsp;<br>";
 						} else {
 							involvedTimeslotsStr += " ";
 						}
 						AISTimeslot involvedTimeslot = involvedTimeslots.get(i);
 						String frequency = "NULL";
 						if (involvedTimeslot.getFrequency() != null) {
 							if (involvedTimeslot.getFrequency() == AISFrequency.AIS1) {
 								frequency = "AIS1";
 							} else if (involvedTimeslot.getFrequency() == AISFrequency.AIS2) {
 								frequency = "AIS2";
 							}
 						}							
 						if (i == 0) {
 							involvedTimeslotsStr += "&nbsp;&nbsp;{";
 						} else {
 							involvedTimeslotsStr += "&nbsp;&nbsp;";
 						}
 						involvedTimeslotsStr += "(" + frequency + "," + involvedTimeslot.getSlotNumber() + ")";
 
 						if (i<involvedTimeslots.size()-1) {
 							involvedTimeslotsStr += ",";
 						} else {
 							involvedTimeslotsStr += "}&nbsp;&nbsp;";	
 						}
 					}
 					involvedTimeslotsStr += "</body></html>";					
 				}
 				
 				JLabel involvedTimeslotsLabel = new JLabel(involvedTimeslotsStr);
 				involvedTimeslotsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
 				involvedTimeslotsLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 				panel.add(involvedTimeslotsLabel, c);	
 
 				c.gridx = 4;
 			
 				String acknowledgedStr = "";
 				if (acknowledged) {
 					acknowledgedStr = "  Yes  ";
 				} else {
 					acknowledgedStr = "  No  ";
 				}
 				JLabel acknowledgedLabel = new JLabel(acknowledgedStr);
 				acknowledgedLabel.setFont(new Font("Arial", Font.PLAIN, 12));
 				acknowledgedLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 				panel.add(acknowledgedLabel, c);		
 			
 				c.gridx = 5;
 			
 				JButton acknowledgeButton = new JButton("Acknowledge");
 				acknowledgeButton.setPreferredSize(new Dimension(90, 25));
 				acknowledgeButton.setMaximumSize(new Dimension(90, 25));
 				acknowledgeButton.setMinimumSize(new Dimension(90, 25));					
 				acknowledgeButton.setMargin(new Insets(1,1,1,1));  					
 				acknowledgeButton.setFont(new Font("Arial", Font.PLAIN, 12));
 				acknowledgeButton.setFocusPainted(false);
 				acknowledgeButton.setAction(new AcknowledgeAction("Acknowledge", this, dialog, issue));
 				if (acknowledged) {
 					acknowledgeButton.setEnabled(false);
 				}
 				Box verticalBox = Box.createVerticalBox();			
 				verticalBox.add(Box.createVerticalGlue());
 				verticalBox.add(acknowledgeButton);
 				verticalBox.add(Box.createVerticalGlue());
 				JPanel temp = new JPanel(new BorderLayout());
 				temp.add(verticalBox, BorderLayout.CENTER);
 				temp.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK), BorderFactory.createEmptyBorder(5, 5, 5, 5)));	
 				panel.add(temp, c);
 				
 				
 				/*
 				c.gridx = 6;
 			
 				JButton deleteButton = new JButton("Delete");
 				deleteButton.setPreferredSize(new Dimension(60, 25));
 				deleteButton.setMaximumSize(new Dimension(60, 25));
 				deleteButton.setMinimumSize(new Dimension(60, 25));
 				deleteButton.setMargin(new Insets(1,1,1,1));  				
 				deleteButton.setFont(new Font("Arial", Font.PLAIN, 12));							
 				deleteButton.setFocusPainted(false);				
 				deleteButton.setAction(new DeleteAction("Delete", this, dialog, issue));				
 				Box verticalBox2 = Box.createVerticalBox();			
 				verticalBox2.add(Box.createVerticalGlue());
 				verticalBox2.add(deleteButton);
 				verticalBox2.add(Box.createVerticalGlue());
 				JPanel temp2 = new JPanel(new BorderLayout());
 				temp2.add(verticalBox2, BorderLayout.CENTER);
 				temp2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK), BorderFactory.createEmptyBorder(5, 5, 5, 5)));	
 				panel.add(temp2, c)
 				*/
 			}	
 		}			
 		
 		JScrollPane scrollPane = new JScrollPane(panel);
 		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		//scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		if (scrollPane.getViewport().getViewSize().getHeight() > IssuesMenuItem.ISSUES_WINDOW_HEIGHT-90) {
 			scrollPane.setPreferredSize(new Dimension(IssuesMenuItem.ISSUES_WINDOW_WIDTH, IssuesMenuItem.ISSUES_WINDOW_HEIGHT-90));
 			scrollPane.setMaximumSize(new Dimension(IssuesMenuItem.ISSUES_WINDOW_WIDTH, IssuesMenuItem.ISSUES_WINDOW_HEIGHT-90));
 		}
 		scrollPane.validate();
 		
 		return scrollPane;
 	}
 		
 	public void updateScrollPane() {
 		JScrollPane scrollPane = getScrollPane();		
 		scrollPane.setBorder(BorderFactory.createEmptyBorder());
 			
 		JPanel containerPane = new JPanel();
 		containerPane.setBorder(BorderFactory.createEmptyBorder());
 		containerPane.setLayout(new BorderLayout());		 
 		containerPane.add(scrollPane, BorderLayout.NORTH);			
 
 		exportToCSVButton = new JButton("Export issues");
 		exportToCSVButton.setToolTipText("Exports issues to CSV file format");
 		exportToCSVButton.addActionListener(this);
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
 		buttonPanel.add(exportToCSVButton);
 		dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);		
 		
 		dialog.setContentPane(containerPane);
 		dialog.validate();
 	}
 	
 }
 
 
 class AcknowledgeAction extends AbstractAction {
 
     public static final long serialVersionUID = 1L;
 
 	private IssuesMenuItemActionListener issuesMenuItemActionListener;
 	private AISDatalinkCheckIssue issue;
 	private JDialog dialog;
 
     public AcknowledgeAction(String name, IssuesMenuItemActionListener issuesMenuItemActionListener, JDialog dialog, AISDatalinkCheckIssue issue) {
         super(name);
 		this.issuesMenuItemActionListener = issuesMenuItemActionListener;
 		this.issue = issue;
 		this.dialog = dialog;
     }
     public void actionPerformed(ActionEvent e) {
         int response = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to acknowledge this issue?", "Confirm action", JOptionPane.YES_NO_OPTION);	
 		if (response == JOptionPane.YES_OPTION) {
 			List<AISDatalinkCheckIssue> issues = new ArrayList<AISDatalinkCheckIssue>();
 			issues.add(issue);			
 			DBHandler.acknowledgeIssues(issues);
 			//IssuesMenuItem.issues.remove(issue);
 			//issue.setAcknowledged(true);
 			//IssuesMenuItem.issues.add(issue);
 			IssuesMenuItem.issues = null;
 			issuesMenuItemActionListener.updateScrollPane();           
         } else if (response == JOptionPane.NO_OPTION) {}
     }
 }
 
 /*
 class DeleteAction extends AbstractAction {
 
     public static final long serialVersionUID = 1L;
 
 	private IssuesMenuItemActionListener issuesMenuItemActionListener;
 	private AISDatalinkCheckIssue issue;
 	private JDialog dialog;
 
     public DeleteAction(String name, IssuesMenuItemActionListener issuesMenuItemActionListener, JDialog dialog, AISDatalinkCheckIssue issue) {
         super(name);
 		this.issuesMenuItemActionListener = issuesMenuItemActionListener;
 		this.issue = issue;
 		this.dialog = dialog;
     }
     public void actionPerformed(ActionEvent e) {
         int response = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to delete this issue?", "Confirm action", JOptionPane.YES_NO_OPTION);
         if (response == JOptionPane.YES_OPTION) {
 			List<AISDatalinkCheckIssue> issues = new ArrayList<AISDatalinkCheckIssue>();
 			issues.add(issue);			
 			DBHandler.deleteIssues(issues);
 			IssuesMenuItem.issues.remove(issue);
 			issuesMenuItemActionListener.updateScrollPane();           
         } else if (response == JOptionPane.NO_OPTION) {}
     }
 }
 */
