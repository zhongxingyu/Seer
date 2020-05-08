 package dk.frv.eavdam.menus;
 
 import com.bbn.openmap.gui.OpenMapFrame;
 import dk.frv.eavdam.data.AISSlotMap;
 import dk.frv.eavdam.data.AISStation;
 import dk.frv.eavdam.data.AISTimeslot;
 import dk.frv.eavdam.utils.ImageHandler;
 import dk.frv.eavdam.utils.LinkLabel;
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
 import java.util.List;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 public class SlotMapDialog extends JDialog implements ActionListener {
 
 	private static final long serialVersionUID = 1L;
 
 	private double latitude;
 	private double longitude;
 	private AISSlotMap slotmap;
 	
 	public static int SLOTMAP_WINDOW_WIDTH = 1024;
 	public static int SLOTMAP_WINDOW_HEIGHT = 1000;
 
 	private LinkLabel slots0_250LinkLabel;
 	private LinkLabel slots251_500LinkLabel;
 	private LinkLabel slots501_750LinkLabel;
 	private LinkLabel slots751_1000LinkLabel;
 	private LinkLabel slots1001_1250LinkLabel;
 	private LinkLabel slots1251_1500LinkLabel;
 	private LinkLabel slots1501_1750LinkLabel;
 	private LinkLabel slots1751_2000LinkLabel;
 	private LinkLabel slots2001_2249LinkLabel;
 	
 	public SlotMapDialog(OpenMapFrame openMapFrame, double latitude, double longitude, AISSlotMap slotmap) {
 	
 		super(openMapFrame, "Slotmap for latitude " + String.valueOf((double) (Math.round(latitude*1000))/1000) +
 			", longitude " + String.valueOf(((double) Math.round(longitude*1000))/1000) , true);
 			
 		this.latitude = latitude;
 		this.longitude = longitude;
 		this.slotmap = slotmap;
 
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 		Dimension dimension = toolkit.getScreenSize();
 
 		if (dimension.width-100 < SLOTMAP_WINDOW_WIDTH) {
 			SLOTMAP_WINDOW_WIDTH = dimension.width-100;
 		}
 		if (dimension.width-100 < SlotMapDialog.SLOTMAP_WINDOW_WIDTH) {
 			SlotMapDialog.SLOTMAP_WINDOW_WIDTH = dimension.width-100;
 		}
 		if (dimension.height-100 < SLOTMAP_WINDOW_HEIGHT) {
 			SLOTMAP_WINDOW_HEIGHT = dimension.height-100;
 		}
 		if (dimension.height-100 < SlotMapDialog.SLOTMAP_WINDOW_HEIGHT) {
 			SlotMapDialog.SLOTMAP_WINDOW_HEIGHT = dimension.height-100;
 		}
 
 		JScrollPane scrollPane = getScrollPane(0);
 		scrollPane.setBorder(BorderFactory.createEmptyBorder());
 		
 		JPanel containerPane = new JPanel();
 		containerPane.setBorder(BorderFactory.createEmptyBorder());
 		containerPane.setLayout(new BorderLayout());		 
 		containerPane.add(scrollPane, BorderLayout.NORTH);	
 
 		getContentPane().add(containerPane);	
 	}
 
 	private JScrollPane getScrollPane(int selectedSlotsIndex) {
 
 		JPanel panel = new JPanel();
 		panel.setLayout(new GridBagLayout());                  
 		
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 0;                   
 		c.anchor = GridBagConstraints.LINE_START;
 		c.insets = new Insets(5,5,5,5);
 	
 		if (slotmap == null) {
 		
 			panel.add(new JLabel("<html><body><h1>Slotmap for latitude " + String.valueOf((double) (Math.round(latitude*1000))/1000) +
 				", longitude " + String.valueOf(((double) Math.round(longitude*1000))/1000) + "</h1></body></html>"), c);
 			c.anchor = GridBagConstraints.LINE_START;
 			
 			c.gridy = 1;
 			panel.add(new JLabel("No slots reserved."), c);
 		
 		} else {	
 
 			//c.anchor = GridBagConstraints.CENTER;		
 			c.gridwidth = 9;
 			panel.add(new JLabel("<html><body><h1>Slotmap for latitude " + String.valueOf((double) (Math.round(latitude*1000))/1000) +
 				", longitude " + String.valueOf(((double) Math.round(longitude*1000))/1000) + "</h1></body></html>"), c);
 			c.anchor = GridBagConstraints.LINE_START;
 			
 			JPanel infoPanel = new JPanel();
 			infoPanel.setLayout(new GridBagLayout()); 		
 			c.gridwidth = 1;		
 			infoPanel.add(new JLabel("Total bandwith usage:"), c);
 			c.gridx = 1;		
 			infoPanel.add(new JLabel("AIS1: " + (double) Math.round(10000 * slotmap.getBandwidthReservationA()) / 100 + " %"), c);
 			c.gridx = 2;			
 			infoPanel.add(new JLabel("AIS2: " + (double) Math.round(10000 * slotmap.getBandwidthReservationB()) / 100 + " %"), c);
 			c.gridx = 0;
 			c.gridy = 1;
 			infoPanel.add(new JLabel("Bandwidth in use by local fixed stations:"), c);	
 			c.gridx = 1;
 			infoPanel.add(new JLabel("AIS1: " + (double) Math.round(10000 * slotmap.getBandwidthUsedByLocalA()) / 100 + " %"), c);
 			c.gridx = 2;
 			infoPanel.add(new JLabel("AIS2: " + (double) Math.round(10000 * slotmap.getBandwidthUsedByLocalB()) / 100 + " %"), c);
 			c.gridx = 0;
 			c.gridy = 2;	
 			infoPanel.add(new JLabel("Bandwidth free for selection:"), c);
 			c.gridx = 1;	
 			infoPanel.add(new JLabel("AIS1: " + (double) Math.round(10000 * (1-slotmap.getBandwidthReservationA())) / 100 + " %"), c);
 			c.gridx = 2;	
 			infoPanel.add(new JLabel("AIS2: " + (double) Math.round(10000 * (1-slotmap.getBandwidthReservationB())) / 100 + " %"), c);
 				
 			c.gridwidth = 9;		
 			c.gridx = 0;
 			c.gridy = 1;
 			panel.add(infoPanel, c);
 			
 			c.gridwidth = 1;		
 			c.gridy = 0;
 			JPanel timeslotsChartPanel = new JPanel();
 			timeslotsChartPanel.setLayout(new GridBagLayout()); 
 			List<AISTimeslot> ais1Timeslots = slotmap.getAIS1Timeslots();		
 			boolean[] reservationsForChannelA = new boolean[2250];
 			for (int i=0; i<2250; i++) {
 				reservationsForChannelA[i] = false;
 			}
 			if (ais1Timeslots != null) {
 				for (AISTimeslot timeslot : ais1Timeslots) {
 					if (timeslot.getFree() != null && timeslot.getFree().booleanValue() == false) {
 						reservationsForChannelA[timeslot.getSlotNumber()] = true;
 					}
 				}
 			}
 			Image timeslotImageForChannelA = ImageHandler.getTimeslotImage(SLOTMAP_WINDOW_WIDTH-200, 15, reservationsForChannelA);
 			if (timeslotImageForChannelA != null) {
 				timeslotsChartPanel.add(new JLabel("AIS1:"), c);
 				c.gridx = 1;
 				JLabel timeslotsForChannelALabel = new JLabel();
 				timeslotsForChannelALabel.setIcon(new ImageIcon(timeslotImageForChannelA));
 				timeslotsChartPanel.add(timeslotsForChannelALabel, c);
 			}
 			List<AISTimeslot> ais2Timeslots = slotmap.getAIS2Timeslots();		
 			boolean[] reservationsForChannelB = new boolean[2250];
 			for (int i=0; i<2250; i++) {
 				reservationsForChannelB[i] = false;
 			}
 			if (ais2Timeslots != null) {
 				for (AISTimeslot timeslot : ais2Timeslots) {
 					if (timeslot.getFree() != null && timeslot.getFree().booleanValue() == false) {
 						reservationsForChannelB[timeslot.getSlotNumber()] = true;
 					}
 				}
 			}
 			Image timeslotImageForChannelB = ImageHandler.getTimeslotImage(SLOTMAP_WINDOW_WIDTH-200, 15, reservationsForChannelB);
 			if (timeslotImageForChannelB != null) {
 				c.gridx = 0;
 				c.gridy = 1;
 				timeslotsChartPanel.add(new JLabel("AIS2:"), c);
 				c.gridx = 1;
 				JLabel timeslotsForChannelBLabel = new JLabel();
 				timeslotsForChannelBLabel.setIcon(new ImageIcon(timeslotImageForChannelB));
 				timeslotsChartPanel.add(timeslotsForChannelBLabel, c);
 			}		
 
 			c.gridx = 0;
 			c.gridy = 2;
 			c.gridwidth = 9;
 			panel.add(timeslotsChartPanel, c);
 			
 			JPanel slotsPanel = new JPanel();
 			slotsPanel.setLayout(new GridBagLayout());
 			
 			c.gridwidth = 1;
 			slots0_250LinkLabel = new LinkLabel("Slots 0...250");
 			if (selectedSlotsIndex == 0) {
 				slots0_250LinkLabel.setRedText("Slots 0...250");
 			}
 			slots0_250LinkLabel.addActionListener(this);
 			slotsPanel.add(slots0_250LinkLabel, c);
 			
 			c.gridx = 1;
 			slots251_500LinkLabel = new LinkLabel("Slots 251...500");
 			if (selectedSlotsIndex == 1) {
 				slots251_500LinkLabel.setRedText("Slots 251...500");
 			}
 			slots251_500LinkLabel.addActionListener(this);
 			slotsPanel.add(slots251_500LinkLabel, c);		
 			
 			c.gridx = 2;
 			slots501_750LinkLabel = new LinkLabel("Slots 501...750");
 			if (selectedSlotsIndex == 2) {
 				slots501_750LinkLabel.setRedText("Slots 501...750");
 			}
 			slots501_750LinkLabel.addActionListener(this);
 			slotsPanel.add(slots501_750LinkLabel, c);	
 			
 			c.gridx = 3;
 			slots751_1000LinkLabel = new LinkLabel("Slots 751...1000");
 			if (selectedSlotsIndex == 3) {
 				slots751_1000LinkLabel.setRedText("Slots 751...1000");
 			}
 			slots751_1000LinkLabel.addActionListener(this);
 			slotsPanel.add(slots751_1000LinkLabel, c);	
 			
 			c.gridx = 4;
 			slots1001_1250LinkLabel = new LinkLabel("Slots 1001...1250");
 			if (selectedSlotsIndex == 4) {
 				slots1001_1250LinkLabel.setRedText("Slots 1001...1250");
 			}		
 			slots1001_1250LinkLabel.addActionListener(this);
 			slotsPanel.add(slots1001_1250LinkLabel, c);
 				
 			c.gridx = 5;
 			slots1251_1500LinkLabel = new LinkLabel("Slots 1251...1500");
 			if (selectedSlotsIndex == 5) {
 				slots1251_1500LinkLabel.setRedText("Slots 1251...1500");
 			}		
 			slots1251_1500LinkLabel.addActionListener(this);
 			slotsPanel.add(slots1251_1500LinkLabel, c);
 
 			c.gridx = 6;
 			slots1501_1750LinkLabel = new LinkLabel("Slots 1501...1750");
 			if (selectedSlotsIndex == 6) {
 				slots1501_1750LinkLabel.setRedText("Slots 1501...1750");
 			}		
 			slots1501_1750LinkLabel.addActionListener(this);
 			slotsPanel.add(slots1501_1750LinkLabel, c);
 
 			c.gridx = 7;
 			slots1751_2000LinkLabel = new LinkLabel("Slots 1751...2000");
 			if (selectedSlotsIndex == 7) {
 				slots1751_2000LinkLabel.setRedText("Slots 1751...2000");
 			}		
 			slots1751_2000LinkLabel.addActionListener(this);
 			slotsPanel.add(slots1751_2000LinkLabel, c);
 
 			c.gridx = 8;
 			slots2001_2249LinkLabel = new LinkLabel("Slots 2001...2249");
 			if (selectedSlotsIndex == 8) {
 				slots2001_2249LinkLabel.setRedText("Slots 2001...2249");
 			}		
 			slots2001_2249LinkLabel.addActionListener(this);
 			slotsPanel.add(slots2001_2249LinkLabel, c);		
 
 			c.gridx = 0;
 			c.gridy = 3;		
 			c.gridwidth = 9;
 			panel.add(slotsPanel, c);
 
 			c.gridy = 4;
 			c.gridwidth = 1;
 			c.fill = GridBagConstraints.BOTH;
 			c.insets = new Insets(0,0,0,0);
 			JLabel frequencyLabel = new JLabel(" Frequency:  ");
 			frequencyLabel.setFont(new Font("Arial", Font.BOLD, 14));
 			frequencyLabel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
 			panel.add(frequencyLabel, c);
 			c.gridx = 1;           
 			c.gridwidth = 4;
 			JLabel ais1Label = new JLabel(" AIS1  ");
 			ais1Label.setFont(new Font("Arial", Font.BOLD, 14));
 			ais1Label.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.BLACK));
 			panel.add(ais1Label, c);
 			c.gridx = 5;
 			JLabel ais2Label = new JLabel(" AIS2  ");
 			ais2Label.setFont(new Font("Arial", Font.BOLD, 14));
 			ais2Label.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, Color.BLACK));
 			panel.add(ais2Label, c);				
 			c.gridx = 0;                
 			c.gridy = 5;
 			c.gridwidth = 1;
 			JLabel slotnoLabel = new JLabel(" Slotno.  ");
 			slotnoLabel.setFont(new Font("Arial", Font.BOLD, 14));
 			slotnoLabel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.BLACK));
 			panel.add(slotnoLabel, c);
 			c.gridx = 1;   
 			JLabel ais1FreeLabel = new JLabel(" Free  ");
 			ais1FreeLabel.setFont(new Font("Arial", Font.BOLD, 14));
 			ais1FreeLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 			panel.add(ais1FreeLabel, c);				
 			c.gridx = 2;         
 			JLabel ais1ReservedByLabel = new JLabel(" Reserved by  ");
 			ais1ReservedByLabel.setFont(new Font("Arial", Font.BOLD, 14));
 			ais1ReservedByLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 			panel.add(ais1ReservedByLabel, c);
 			c.gridx = 3;   
 			JLabel ais1UsedByLabel = new JLabel(" Used by  ");
 			ais1UsedByLabel.setFont(new Font("Arial", Font.BOLD, 14));
 			ais1UsedByLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 			panel.add(ais1UsedByLabel, c);	
 			c.gridx = 4;
 			JLabel ais1InterferedByLabel = new JLabel(" Interfered by  ");
 			ais1InterferedByLabel.setFont(new Font("Arial", Font.BOLD, 14));
 			ais1InterferedByLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 			panel.add(ais1InterferedByLabel, c);						
 			c.gridx = 5; 
 			JLabel ais2FreeLabel = new JLabel(" Free  ");
 			ais2FreeLabel.setFont(new Font("Arial", Font.BOLD, 14));
 			ais2FreeLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 			panel.add(ais2FreeLabel, c);				
 			c.gridx = 6;         
 			JLabel ais2ReservedByLabel = new JLabel(" Reserved by  ");
 			ais2ReservedByLabel.setFont(new Font("Arial", Font.BOLD, 14));
 			ais2ReservedByLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 			panel.add(ais2ReservedByLabel, c);
 			c.gridx = 7;   
 			JLabel ais2UsedByLabel = new JLabel(" Used by  ");
 			ais2UsedByLabel.setFont(new Font("Arial", Font.BOLD, 14));
 			ais2UsedByLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 			panel.add(ais2UsedByLabel, c);	
 			c.gridx = 8;
 			JLabel ais2InterferedByLabel = new JLabel(" Interfered by  ");
 			ais2InterferedByLabel.setFont(new Font("Arial", Font.BOLD, 14));
 			ais2InterferedByLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 			panel.add(ais2InterferedByLabel, c);
 			
 			int start = -1;
 			int end = -1;
 			
 			boolean[] ais1Free = new boolean[0];
 			String[] ais1ReservedBy = new String[0];
 			String[] ais1UsedBy = new String[0];
 			String[] ais1InterferedBy = new String[0];
 			boolean[] ais2Free = new boolean[0];
 			String[] ais2ReservedBy = new String[0];
 			String[] ais2UsedBy = new String[0];
 			String[] ais2InterferedBy = new String[0];
 			boolean[] conflicts = new boolean[0];
 			
 			if (selectedSlotsIndex == 0) {
 				ais1Free = new boolean[251];
 				ais1ReservedBy = new String[251];
 				ais1UsedBy = new String[251];
 				ais1InterferedBy = new String[251];
 				ais2Free = new boolean[251];
 				ais2ReservedBy = new String[251];
 				ais2UsedBy = new String[251];
 				ais2InterferedBy = new String[251];	
 				conflicts = new boolean[251];	
 			} else if (selectedSlotsIndex > 0 && selectedSlotsIndex < 8) {
 				ais1Free = new boolean[250];
 				ais1ReservedBy = new String[250];
 				ais1UsedBy = new String[250];
 				ais1InterferedBy = new String[250];
 				ais2Free = new boolean[250];
 				ais2ReservedBy = new String[250];
 				ais2UsedBy = new String[250];
 				ais2InterferedBy = new String[250];
 				conflicts = new boolean[250];			
 			} else if (selectedSlotsIndex == 8) {
 				ais1Free = new boolean[249];
 				ais1ReservedBy = new String[249];
 				ais1UsedBy = new String[249];
 				ais1InterferedBy = new String[249];
 				ais2Free = new boolean[249];
 				ais2ReservedBy = new String[249];
 				ais2UsedBy = new String[249];
 				ais2InterferedBy = new String[249];
 				conflicts = new boolean[249];
 			}
 			
 			if (selectedSlotsIndex == 0) {
 				start = 0;
 				end = 250;
 			} else if (selectedSlotsIndex == 1) {
 				start = 251;
 				end = 500;
 			} else if (selectedSlotsIndex == 2) {
 				start = 501;
 				end = 750;
 			} else if (selectedSlotsIndex == 3) {
 				start = 751;
 				end = 1000;
 			} else if (selectedSlotsIndex == 4) {
 				start = 1001;
 				end = 1250;
 			} else if (selectedSlotsIndex == 5) {
 				start = 1251;
 				end = 1500;
 			} else if (selectedSlotsIndex == 6) {
 				start = 1501;
 				end = 1750;
 			} else if (selectedSlotsIndex == 7) {
 				start = 1751;
 				end = 2000;		
 			} else if (selectedSlotsIndex == 8) {
 				start = 2001;
 				end = 2249;			
 			}
 			
 			ais1Free = fillEmpty(ais1Free, true);
 			ais1ReservedBy = fillEmpty(ais1ReservedBy);
 			ais1UsedBy = fillEmpty(ais1UsedBy);
 			ais1InterferedBy = fillEmpty(ais1InterferedBy);
 			ais2Free = fillEmpty(ais2Free, true);
 			ais2ReservedBy = fillEmpty(ais2ReservedBy);
 			ais2UsedBy = fillEmpty(ais2UsedBy);
 			ais2InterferedBy = fillEmpty(ais2InterferedBy);
 			conflicts = fillEmpty(conflicts, false);
 			
 			if (ais1Timeslots != null) {
 				for (AISTimeslot timeslot : ais1Timeslots) {
 					if (timeslot.getSlotNumber() >= start && timeslot.getSlotNumber() <= end) {
 						if (timeslot.getFree() != null) {
 							ais1Free[timeslot.getSlotNumber()-start] = timeslot.getFree().booleanValue();
 						}		
 						if (timeslot.getReservedBy() != null) {
 							if (timeslot.getReservedBy().size() == 1) {
 								AISStation reservedBy = timeslot.getReservedBy().get(0);
 								ais1ReservedBy[timeslot.getSlotNumber()-start] = "  " + reservedBy.getOrganizationName() + ": " + reservedBy.getStationName() + "  ";
 							} else if (timeslot.getReservedBy().size() > 1) {
 								String html = "<html><body>";
 								for (int i=0; i<timeslot.getReservedBy().size(); i++) {
 									AISStation reservedBy = timeslot.getReservedBy().get(i);
 									html += "&nbsp;&nbsp;" + reservedBy.getOrganizationName() + ": " + reservedBy.getStationName() + "&nbsp;&nbsp;";
 									if (i<timeslot.getReservedBy().size()-1) {
 										html += "<br>";
 									}
 								}
 								html += "</body></html>";
 								ais1ReservedBy[timeslot.getSlotNumber()-start] = html;
 							}
 						}					
 						if (timeslot.getUsedBy() != null) {
 							if (timeslot.getUsedBy().size() == 1) {
 								AISStation usedBy = timeslot.getUsedBy().get(0);
 								ais1UsedBy[timeslot.getSlotNumber()-start] = "  " + usedBy.getOrganizationName() + ": " + usedBy.getStationName() + "  ";						
 							} else if (timeslot.getUsedBy().size() > 1) {
 								String html = "<html><body>";
 								for (int i=0; i<timeslot.getUsedBy().size(); i++) {
 									AISStation usedBy = timeslot.getUsedBy().get(i);
 									html += "&nbsp;&nbsp;" + usedBy.getOrganizationName() + ": " + usedBy.getStationName() + "&nbsp;&nbsp;";
 									if (i<timeslot.getUsedBy().size()-1) {
 										html += "<br>";
 									}
 								}
 								html += "</body></html>";
 								ais1UsedBy[timeslot.getSlotNumber()-start] = html;
 							}
 						}				
 						if (timeslot.getInterferedBy() != null) {
 							if (timeslot.getInterferedBy().size() == 1) {
 								AISStation interferedBy = timeslot.getInterferedBy().get(0);
 								ais1InterferedBy[timeslot.getSlotNumber()-start] = "  " + interferedBy.getOrganizationName() + ": " + interferedBy.getStationName() + "  ";			
 							} else if (timeslot.getInterferedBy().size() > 1) {
 								String html = "<html><body>";
 								for (int i=0; i<timeslot.getInterferedBy().size(); i++) {
 									AISStation interferedBy = timeslot.getInterferedBy().get(i);
 									html += "&nbsp;&nbsp;" + interferedBy.getOrganizationName() + ": " + interferedBy.getStationName() + "&nbsp;&nbsp;";
 									if (i<timeslot.getInterferedBy().size()-1) {
 										html += "<br>";
 									}
 								}
 								html += "</body></html>";
 								ais1InterferedBy[timeslot.getSlotNumber()-start] = html;
 							}
 						}
 						if (timeslot.getPossibleConflicts() != null) {
 							conflicts[timeslot.getSlotNumber()-start] = timeslot.getPossibleConflicts().booleanValue();
 						}							
 					}
 				}
 			}
 
 			if (ais2Timeslots != null) {
 				for (AISTimeslot timeslot : ais2Timeslots) {
 					if (timeslot.getSlotNumber() >= start && timeslot.getSlotNumber() <= end) {
 						if (timeslot.getFree() != null) {
 							ais2Free[timeslot.getSlotNumber()-start] = timeslot.getFree().booleanValue();
 						}
 						if (timeslot.getReservedBy() != null) {
 							if (timeslot.getReservedBy().size() == 1) {
 								AISStation reservedBy = timeslot.getReservedBy().get(0);
 								ais2ReservedBy[timeslot.getSlotNumber()-start] = "  " + reservedBy.getOrganizationName() + ": " + reservedBy.getStationName() + "  ";
 							} else if (timeslot.getReservedBy().size() > 1) {
 								String html = "<html><body>";
 								for (int i=0; i<timeslot.getReservedBy().size(); i++) {
 									AISStation reservedBy = timeslot.getReservedBy().get(i);
 									html += "&nbsp;&nbsp;" + reservedBy.getOrganizationName() + ": " + reservedBy.getStationName() + "&nbsp;&nbsp;";
 									if (i<timeslot.getReservedBy().size()-1) {
 										html += "<br>";
 									}
 								}
 								html += "</body></html>";
 								ais2ReservedBy[timeslot.getSlotNumber()-start] = html;
 							}
 						}
 						if (timeslot.getUsedBy() != null) {
 							if (timeslot.getUsedBy().size() == 1) {
 								AISStation usedBy = timeslot.getUsedBy().get(0);
 								ais2UsedBy[timeslot.getSlotNumber()-start] = "  " + usedBy.getOrganizationName() + ": " + usedBy.getStationName() + "  ";
 							} else if (timeslot.getUsedBy().size() > 1) {
 								String html = "<html><body>";
 								for (int i=0; i<timeslot.getUsedBy().size(); i++) {
 									AISStation usedBy = timeslot.getUsedBy().get(i);
 									html += "&nbsp;&nbsp;" + usedBy.getOrganizationName() + ": " + usedBy.getStationName() + "&nbsp;&nbsp;";
 									if (i<timeslot.getUsedBy().size()-1) {
 										html += "<br>";
 									}
 								}
 								html += "</body></html>";
 								ais2UsedBy[timeslot.getSlotNumber()-start] = html;
 							}
 						}
 						if (timeslot.getInterferedBy() != null) {
 							if (timeslot.getInterferedBy().size() == 1) {
 								AISStation interferedBy = timeslot.getInterferedBy().get(0);
 								ais2InterferedBy[timeslot.getSlotNumber()-start] = "  " + interferedBy.getOrganizationName() + ": " + interferedBy.getStationName() + "  ";
 							} else if (timeslot.getInterferedBy().size() > 1) {
 								String html = "<html><body>";
 								for (int i=0; i<timeslot.getInterferedBy().size(); i++) {
 									AISStation interferedBy = timeslot.getInterferedBy().get(i);
 									html += "&nbsp;&nbsp;" + interferedBy.getOrganizationName() + ": " + interferedBy.getStationName() + "&nbsp;&nbsp;";
 									if (i<timeslot.getInterferedBy().size()-1) {
 										html += "<br>";
 									}
 								}
 								html += "</body></html>";
 								ais2InterferedBy[timeslot.getSlotNumber()-start] = html;
 							}
 						}
						if (timeslot.getPossibleConflicts() != null) {		
 							conflicts[timeslot.getSlotNumber()-start] = timeslot.getPossibleConflicts().booleanValue();
 						}
 					}	
 				}			
 			}
 
 			for (int i=0; i<ais1Free.length; i++) {
 			
 				c.gridy++;
 				c.gridx = 0;
 		
 				JLabel slotnoValueLabel = new JLabel("  " + String.valueOf(start+i) + "  ");
 				slotnoValueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
 				if (conflicts[i] == true) {
 					slotnoValueLabel.setForeground(Color.red);
 					slotnoValueLabel.setText("  " + String.valueOf(start+i) + " (!)  ");
 				}
 				slotnoValueLabel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.BLACK));
 				panel.add(slotnoValueLabel, c);			
 				c.gridx = 1;
 				JLabel ais1FreeValueLabel = new JLabel();
 				if (ais1Free[i] == true) {
 					ais1FreeValueLabel.setText("  Yes  ");
 				} else if (ais1Free[i] == false) {
 					ais1FreeValueLabel.setText("  No  ");
 				}
 				ais1FreeValueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
 				ais1FreeValueLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 				panel.add(ais1FreeValueLabel, c);		
 				c.gridx = 2;
 				JLabel ais1ReservedByValueLabel = new JLabel(ais1ReservedBy[i]);
 				ais1ReservedByValueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
 				ais1ReservedByValueLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 				panel.add(ais1ReservedByValueLabel, c);
 				c.gridx = 3;
 				JLabel ais1UsedByValueLabel = new JLabel(ais1UsedBy[i]);
 				ais1UsedByValueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
 				ais1UsedByValueLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 				panel.add(ais1UsedByValueLabel, c);					
 				c.gridx = 4;
 				JLabel ais1InterferedByValueLabel = new JLabel(ais1InterferedBy[i]);
 				ais1InterferedByValueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
 				ais1InterferedByValueLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 				panel.add(ais1InterferedByValueLabel, c);			
 				c.gridx = 5;
 				JLabel ais2FreeValueLabel = new JLabel("");
 				if (ais2Free[i] == true) {
 					ais2FreeValueLabel.setText("  Yes  ");
 				} else if (ais2Free[i] == false) {
 					ais2FreeValueLabel.setText("  No  ");
 				}
 				ais2FreeValueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
 				ais2FreeValueLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 				panel.add(ais2FreeValueLabel, c);		
 				c.gridx = 6;
 				JLabel ais2ReservedByValueLabel = new JLabel(ais2ReservedBy[i]);
 				ais2ReservedByValueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
 				ais2ReservedByValueLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 				panel.add(ais2ReservedByValueLabel, c);		
 				c.gridx = 7;
 				JLabel ais2UsedByValueLabel = new JLabel(ais2UsedBy[i]);
 				ais2UsedByValueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
 				ais2UsedByValueLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 				panel.add(ais2UsedByValueLabel, c);
 				c.gridx = 8;
 				JLabel ais2InterferedByValueLabel = new JLabel(ais2InterferedBy[i]);
 				ais2InterferedByValueLabel.setFont(new Font("Arial", Font.PLAIN, 12));
 				ais2InterferedByValueLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.BLACK));
 				panel.add(ais2InterferedByValueLabel, c);
 			}			
 		
 		}
 
 		JScrollPane scrollPane = new JScrollPane(panel);
 		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		//scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		if (scrollPane.getViewport().getViewSize().getHeight() > SLOTMAP_WINDOW_HEIGHT-60) {
 			scrollPane.setPreferredSize(new Dimension(IssuesMenuItem.ISSUES_WINDOW_WIDTH, SLOTMAP_WINDOW_HEIGHT-60));
 			scrollPane.setMaximumSize(new Dimension(IssuesMenuItem.ISSUES_WINDOW_WIDTH, SLOTMAP_WINDOW_HEIGHT-60));
 		}
 		scrollPane.validate();
 		
 		return scrollPane;
 	}
 	
     public void actionPerformed(ActionEvent e) {
 	
 		int selectedSlotsIndex = -1;
 	
 		if (e.getSource() == slots0_250LinkLabel) {
 			selectedSlotsIndex = 0;
 		} else if (e.getSource() == slots251_500LinkLabel) {
 			selectedSlotsIndex = 1;
 		} else if (e.getSource() == slots501_750LinkLabel) {
 			selectedSlotsIndex = 2;	
 		} else if (e.getSource() == slots751_1000LinkLabel) {
 			selectedSlotsIndex = 3;
 		} else if (e.getSource() == slots1001_1250LinkLabel) {
 			selectedSlotsIndex = 4;
 		} else if (e.getSource() == slots1251_1500LinkLabel) {
 			selectedSlotsIndex = 5;
 		} else if (e.getSource() == slots1501_1750LinkLabel) {
 			selectedSlotsIndex = 6;
 		} else if (e.getSource() == slots1751_2000LinkLabel) {
 			selectedSlotsIndex = 7;
 		} else if (e.getSource() == slots2001_2249LinkLabel) {
 			selectedSlotsIndex = 8;			
 		}
 		
 		if (selectedSlotsIndex != -1) {
 			JScrollPane scrollPane = getScrollPane(selectedSlotsIndex);
 			setContentPane(scrollPane);
 			validate();
 		}
 
 	}
 
 	private String[] fillEmpty(String[] array) {
 		for (int i=0; i<array.length; i++) {
 			array[i] = "";
 		}
 		return array;
 	}
 	
 	private boolean[] fillEmpty(boolean[] array, boolean defaultValue) {
 		for (int i=0; i<array.length; i++) {
 			array[i] = defaultValue;
 		}
 		return array;
 	}	
 	
 }
