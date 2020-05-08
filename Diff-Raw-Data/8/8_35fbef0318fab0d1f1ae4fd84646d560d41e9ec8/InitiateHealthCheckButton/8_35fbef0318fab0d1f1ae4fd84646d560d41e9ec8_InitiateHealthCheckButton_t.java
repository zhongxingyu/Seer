 package dk.frv.eavdam.buttons;
 
 import com.bbn.openmap.Layer;
 import com.bbn.openmap.LayerHandler;
 import com.bbn.openmap.MapBean;
 import com.bbn.openmap.gui.LayersMenu;
 import com.bbn.openmap.gui.OMToolComponent;
 import com.bbn.openmap.gui.OpenMapFrame;
 import com.bbn.openmap.gui.Tool;
 import com.bbn.openmap.proj.Projection;
 import dk.frv.eavdam.app.SidePanel;
 import dk.frv.eavdam.data.AISDatalinkCheckArea;
 import dk.frv.eavdam.data.AISDatalinkCheckIssue;
 import dk.frv.eavdam.data.AISDatalinkCheckResult;
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.data.Options;
 import dk.frv.eavdam.io.AISDatalinkCheckListener;
 import dk.frv.eavdam.io.EmailSender;
 import dk.frv.eavdam.layers.AISDatalinkCheckBandwidthAreasLayer;
 import dk.frv.eavdam.layers.AISDatalinkCheckIssueLayer;
 import dk.frv.eavdam.menus.EavdamMenu;
 import dk.frv.eavdam.menus.IssuesMenuItem;
 import dk.frv.eavdam.menus.OptionsMenuItem;
 import dk.frv.eavdam.utils.DBHandler;
 import dk.frv.eavdam.utils.HealthCheckHandler;
 import dk.frv.eavdam.utils.LinkLabel;
 import dk.frv.eavdam.utils.XMLHandler;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import javax.mail.MessagingException;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JToolBar;
 import net.sf.image4j.codec.ico.ICODecoder;
 
 public class InitiateHealthCheckButton extends OMToolComponent implements ActionListener, Tool, AISDatalinkCheckListener {
 
 	private static final long serialVersionUID = 6706092036947101L;
 	protected JButton initiateHealthCheckButton = null;
 	protected JToolBar jToolBar;
     private OpenMapFrame openMapFrame;
 	private MapBean mapBean;
 	private LayerHandler layerHandler;
 	private LayersMenu layersMenu;
 	private EavdamMenu eavdamMenu;
 	private SidePanel sidePanel;
 	
 	private JDialog dialog;
 	private JDialog waitDialog;
 	private JDialog completedDialog;
 	
 	private JCheckBox rule1CheckBox;
 	private JCheckBox rule2CheckBox;
 	private JCheckBox rule3CheckBox;
 	private JCheckBox rule4CheckBox;
 	private JCheckBox rule5CheckBox;
 	private JCheckBox rule6CheckBox;
 	private JCheckBox rule7CheckBox;
 	private LinkLabel rulesHelpLabel;
 	private JCheckBox includePlannedCheckBox;
 	private JSlider resolutionSlider;
 	
 	private JButton goButton;
 	private JButton cancelButton;
 	private JButton viewIssuesButton;
 	private JButton cancelViewIssuesButton;
 
 	private EAVDAMData data;
 	
 	public InitiateHealthCheckButton() {
 		super();
 		boolean foundIcon = false;
 		try {
 			List<BufferedImage> images = ICODecoder.read(new File("share/data/images/EAVDAM.ico"));
 			for (BufferedImage img : images) {
 				if (img.getWidth() == 32) {
 					initiateHealthCheckButton = new JButton(new ImageIcon(img));
 					foundIcon = true;
 				}
 			}
 		} catch (IOException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}		
 		if (!foundIcon) {
 			initiateHealthCheckButton = new JButton("Health check");
 		}
 
         initiateHealthCheckButton.setBorder(BorderFactory.createEmptyBorder());
 		initiateHealthCheckButton.addActionListener(this);
 		initiateHealthCheckButton.setToolTipText("Initiate AIS VHF datalink health check");
 		add(initiateHealthCheckButton);
 	}
 	
 	 public void actionPerformed(ActionEvent e) {
 	 
 		if (e.getSource() == initiateHealthCheckButton) {
 	    
 			//int response = JOptionPane.showConfirmDialog(openMapFrame, "This will initiate the AIS VHF datalink health check process,\nwhich may take some time. Are you sure you want to do this?", "Confirm action", JOptionPane.YES_NO_OPTION);
          
 			//if (response == JOptionPane.YES_OPTION) {
          
 				dialog = new JDialog(openMapFrame, "AIS VHF Datalink Health Check", false);  // true for modal dialog
 			
 				JPanel panel = new JPanel();
 				panel.setLayout(new GridBagLayout());                  
 			
 				GridBagConstraints c = new GridBagConstraints();
 				c.gridx = 0;
 				c.gridy = 0;                   
 				c.anchor = GridBagConstraints.LINE_START;
 				c.insets = new Insets(5,5,5,5);
 				
 				JPanel rulesPanel = new JPanel(new GridBagLayout());
 				rulesPanel.setPreferredSize(new Dimension(520, 330));
 				rulesPanel.setMaximumSize(new Dimension(520, 330));
 				rulesPanel.setMinimumSize(new Dimension(520, 330));					
 				rulesPanel.setBorder(BorderFactory.createTitledBorder("Which rules and stations to process"));
 
 				rule1CheckBox = new JCheckBox("Rule 1: Conflicting stations");
 				rule1CheckBox.setSelected(true);
 				rulesPanel.add(rule1CheckBox, c);
 				
 				c.gridy = 1;
 				rule2CheckBox = new JCheckBox("Rule 2: Reservation, but no intended use");
 				//rule2CheckBox.setSelected(true);
 				rulesPanel.add(rule2CheckBox, c);				
 
 				c.gridy = 2;
 				rule3CheckBox = new JCheckBox("Rule 3: Intended FATDMA use, but no reservation");
 				//rule3CheckBox.setSelected(true);
 				rulesPanel.add(rule3CheckBox, c);		
 
 				c.gridy = 3;
 				rule4CheckBox = new JCheckBox("Rule 4: Simultaneous use of several frequencies");
 				//rule4CheckBox.setSelected(true);
 				rulesPanel.add(rule4CheckBox, c);		
 
 				c.gridy = 4;
 				rule5CheckBox = new JCheckBox("Rule 5: Slots reserved outside IALA A-124 recommended default FATDMA schemes");
 				//rule5CheckBox.setSelected(true);
 				rulesPanel.add(rule5CheckBox, c);		
 
 				c.gridy = 5;
 				rule6CheckBox = new JCheckBox("Rule 6: Slots reserved outside overall slot pattern for fixed statons (IALA A-124)");
 				//rule6CheckBox.setSelected(true);
 				rulesPanel.add(rule6CheckBox, c);					
 
 				c.gridy = 6;
 				rule7CheckBox = new JCheckBox("Rule 7: Free Bandwith below 50%");
 				//rule7CheckBox.setSelected(true);
 				rulesPanel.add(rule7CheckBox, c);
 				
 				c.gridy = 7;
 				rulesHelpLabel = new LinkLabel("View detailed descriptions of rules");
 				rulesHelpLabel.addActionListener(this);
 				rulesPanel.add(rulesHelpLabel, c);
 				
 				c.gridy = 8;
 				includePlannedCheckBox = new JCheckBox("Process also planned stations");
 				rulesPanel.add(includePlannedCheckBox, c);				
 				
 				c.gridy = 0;
 				panel.add(rulesPanel, c);		
 				
 				JPanel resolutionPanel = new JPanel(new GridBagLayout());
 				resolutionPanel.setBorder(BorderFactory.createTitledBorder("Resolution for area based check in nautical miles"));
 				resolutionPanel.setPreferredSize(new Dimension(520, 100));
 				resolutionPanel.setMaximumSize(new Dimension(520, 100));
 				resolutionPanel.setMinimumSize(new Dimension(520, 100));					
 		
 				resolutionSlider = new JSlider(JSlider.HORIZONTAL, 1, 50, 10);
 				resolutionSlider.setPreferredSize(new Dimension(480, 50));
 				resolutionSlider.setMaximumSize(new Dimension(480, 50));
 				resolutionSlider.setMinimumSize(new Dimension(480, 50));				
                 Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
 				labelTable.put(new Integer(1), new JLabel("0.1"));
 				labelTable.put(new Integer(10), new JLabel("1"));
 				labelTable.put(new Integer(20), new JLabel("2"));
 				labelTable.put(new Integer(30), new JLabel("3"));
 				labelTable.put(new Integer(40), new JLabel("4"));
 				labelTable.put(new Integer(50), new JLabel("5"));				
 				resolutionSlider.setLabelTable( labelTable );
 				resolutionSlider.setMajorTickSpacing(10);
 				resolutionSlider.setMinorTickSpacing(1);
 				resolutionSlider.setPaintTicks(true);
 				resolutionSlider.setPaintLabels(true);
 				resolutionPanel.add(resolutionSlider, c);
 				c.gridx = 1;
 				
 				c.gridx = 0;
 				c.gridy = 1;
 				panel.add(resolutionPanel, c);
 				
 				JPanel buttonPanel = new JPanel(new GridBagLayout());        
 				buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
 				c.gridx = 0;
 				c.gridy = 0;
 				c.gridwidth = 1;
 				goButton = new JButton("Go");
 				goButton.addActionListener(this);
 				buttonPanel.add(goButton, c);
 				c.gridx = 1;
 				cancelButton = new JButton("Cancel");
 				cancelButton.addActionListener(this);
 				buttonPanel.add(cancelButton, c);			
 
 				c.gridx = 0;
 				c.gridy = 2;
 				c.anchor = GridBagConstraints.CENTER;
 				panel.add(buttonPanel, c);			
 				
 				dialog.getContentPane().add(panel);
 			
 				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 				dialog.setBounds((int) screenSize.getWidth()/2 - 560/2, (int) screenSize.getHeight()/2 - 600/2, 600, 560);
 				dialog.setVisible(true);
 
 			 //} else if (response == JOptionPane.NO_OPTION) { 
 				// ignore        
 			 //}
 		
 		} else if (e.getSource() == rulesHelpLabel) {
 		
 			JDialog rulesDialog = new JDialog(openMapFrame, "AIS VHF Datalink Rules", false);
 
 			JEditorPane editorPane = new JEditorPane();
 			editorPane.setEditable(false);
 			URL rulesURL = InitiateHealthCheckButton.class.getClassLoader().getResource("share/data/rules.html");
 			if (rulesURL != null) {
 				try {
 					editorPane.setPage(rulesURL);
 				} catch (IOException ex) {
 					System.err.println(ex.getMessage());
 					ex.printStackTrace();
 				}
 			} else {
 				System.out.println("Did not find rules.html");
 			}
 
 			JScrollPane editorScrollPane = new JScrollPane(editorPane);
 			editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 			editorScrollPane.setPreferredSize(new Dimension(600, 440));
 			editorScrollPane.setMinimumSize(new Dimension(100, 50));			
 			
             rulesDialog.getContentPane().add(editorScrollPane);
                                                                          
             int frameWidth = 640;
             int frameHeight = 480;
             Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
             rulesDialog.setBounds((int) screenSize.getWidth()/2 - frameWidth/2,
                 (int) screenSize.getHeight()/2 - frameHeight/2, frameWidth, frameHeight);
             rulesDialog.setVisible(true);			
 		
 		} else if (e.getSource() == goButton) {
 		
 			Projection projection = mapBean.getProjection();
 			Point2D topLeft = projection.getUpperLeft();
 			Point2D lowerRight = projection.getLowerRight();
 			
 			double topLeftLatitude = topLeft.getY();
 			double topLeftLongitude = topLeft.getX();
 			double lowerRightLatitude = lowerRight.getY();
 			double lowerRightLongitude = lowerRight.getX();
 			
 			data = DBHandler.getData();
 			
 			boolean checkRule1 = false;
 			if (rule1CheckBox.isSelected()) {
 				checkRule1 = true;
 			}
 			boolean checkRule2 = false;
 			if (rule2CheckBox.isSelected()) {
 				checkRule2 = true;
 			}
 			boolean checkRule3 = false;
 			if (rule3CheckBox.isSelected()) {
 				checkRule3 = true;
 			}
 			boolean checkRule4 = false;
 			if (rule4CheckBox.isSelected()) {
 				checkRule4 = true;
 			}
 			boolean checkRule5 = false;
 			if (rule5CheckBox.isSelected()) {
 				checkRule5 = true;
 			}
 			boolean checkRule6 = false;
 			if (rule6CheckBox.isSelected()) {
 				checkRule6 = true;
 			}
 			boolean checkRule7 = false;
 			if (rule7CheckBox.isSelected()) {
 				checkRule7 = true;
 			}
 			boolean includePlanned = false;
 			if (includePlannedCheckBox.isSelected()) {
 				includePlanned = true;
 			}
 			
 			double resolution = (double) resolutionSlider.getValue() / 10;
 			
 			new InitiateHealthCheckThread(data, this, checkRule1, checkRule2, checkRule3, checkRule4, 
 				checkRule5, checkRule6, checkRule7, includePlanned, topLeftLatitude, topLeftLongitude,
 				lowerRightLatitude, lowerRightLongitude, resolution).start();
 			
 			sidePanel.getProgressIndicatorPane().setVisible(true);
 			
 			dialog.dispose();
 			
 		} else if (e.getSource() == cancelButton) {		
 			dialog.dispose();	
 	
 		} else if (e.getSource() == viewIssuesButton) {
 			completedDialog.dispose();
 			new IssuesMenuItem(eavdamMenu).doClick();
 			
 		} else if (e.getSource() == cancelViewIssuesButton) {
 			completedDialog.dispose();
 		}
 	}
 
 	public void progressed(double progress) {
 		if (sidePanel.getProgressBar() != null) {
 			sidePanel.getProgressBar().setValue((int) Math.round(100*progress));
 		}
 	}
 	
 	public void completed(AISDatalinkCheckResult result) {	
 
 		if (data == null) {
 			data = DBHandler.getData();		
 		}
 		
 		data.setAISDatalinkCheckIssues(null);			
 		IssuesMenuItem.issues = null;
 		
 		if (result != null) {
 						
			data.setAISDatalinkCheckIssues(result.getIssues());
			IssuesMenuItem.issues = result.getIssues();
 			
 			// FOR TESTING FOR NOW
			/*
 			if (result.getAreas() != null && !result.getAreas().isEmpty()) {
 				List<AISDatalinkCheckIssue> issues = new ArrayList<AISDatalinkCheckIssue>();
 				for (AISDatalinkCheckArea area : result.getAreas()) {
 					List<AISDatalinkCheckIssue> temp = area.getIssues();
 					if (temp != null) {
 						for (AISDatalinkCheckIssue issue : temp) {
 							if (!issues.contains(issue)) {
 								issues.add(issue);
 							}
 						}
 					}
 				}
 				data.setAISDatalinkCheckIssues(issues);
 				IssuesMenuItem.issues = issues;
 			}
			*/
 		}
 
 		int numberOfOldIssues = 0;		
 		int numberOfNewIssues = 0;
 		if (IssuesMenuItem.issues != null) {
 			for (AISDatalinkCheckIssue issue : IssuesMenuItem.issues) {
 				if (issue.isAcknowledged()) {
 					numberOfOldIssues++;
 				} else {
 					numberOfNewIssues++;
 				}
 			}
 		}
 		
 		DBHandler.saveData(data);		
 				
 		Layer[] layers = layerHandler.getLayers();
 		Layer aisDatalinkCheckIssueLayer = null;
 		for (Layer layer : layers) {
 			if (layer.getClass().getCanonicalName().equals("dk.frv.eavdam.layers.AISDatalinkCheckIssueLayer")) {
 				layerHandler.turnLayerOn(true, layer);
 				aisDatalinkCheckIssueLayer = layer;
 			} else if (layer.getClass().getCanonicalName().equals("dk.frv.eavdam.layers.AISDatalinkCheckBandwidthAreasLayer")) {
 				if (result != null && result.getAreas() != null && !result.getAreas().isEmpty()) {
 					((AISDatalinkCheckBandwidthAreasLayer) layer).setAreas(result.getAreas());				
 					layerHandler.turnLayerOn(true, layer);
 				}
 			}
 		}		
 		if (aisDatalinkCheckIssueLayer != null) {
 			layerHandler.moveLayer(aisDatalinkCheckIssueLayer, 0);
 			aisDatalinkCheckIssueLayer.repaint();
 			aisDatalinkCheckIssueLayer.validate();
 		}
 		layersMenu.setLayers(layers);
 
 		sidePanel.getProgressIndicatorPane().setVisible(false);
 		dialog.dispose();
 		
 		completedDialog = new JDialog(openMapFrame, "AIS VHF datalink health check completed", true);
 		JPanel panel = new JPanel();
 		panel.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 0;
 		c.anchor = GridBagConstraints.LINE_START;
 		c.insets = new Insets(5,5,5,5);
 		JLabel titleLabel = new JLabel("<html><body><p>The AIS VHF datalink health check is now completed. Found " + numberOfNewIssues +
 			" new issues and " + numberOfOldIssues + " acknowledged issues. View issues now or later through the Eavdam menu. The AIS "+
 			"VHF datalink issues and bandwidth areas layers are also activated.<p></body></html>");
 		titleLabel.setPreferredSize(new Dimension(330, 90));
 		titleLabel.setMaximumSize(new Dimension(330, 90));
 		titleLabel.setMinimumSize(new Dimension(330, 90));
 		panel.add(titleLabel, c);
 		c.gridy = 1;
 		JPanel buttonPanel = new JPanel();
 		viewIssuesButton = new JButton("View issues now");
 		viewIssuesButton.addActionListener(this);
 		cancelViewIssuesButton = new JButton("Cancel");
 		cancelViewIssuesButton.addActionListener(this);
 		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		buttonPanel.add(viewIssuesButton);
 		buttonPanel.add(cancelViewIssuesButton);
 		c.anchor = GridBagConstraints.CENTER;
 		panel.add(buttonPanel, c);
 		completedDialog.getContentPane().add(panel);
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		completedDialog.setBounds((int) screenSize.getWidth()/2 - 380/2, (int) screenSize.getHeight()/2 - 200/2, 380, 200);
 		completedDialog.setVisible(true);				
 	}
 	
 	public void findAndInit(Object obj) {
 		if (obj instanceof OpenMapFrame) {
 			this.openMapFrame = (OpenMapFrame) obj;
 		} else if (obj instanceof MapBean) {
 			this.mapBean = (MapBean) obj;
 		} else if (obj instanceof EavdamMenu) {
 			this.eavdamMenu = (EavdamMenu) obj;
         } else if (obj instanceof LayerHandler) {
 			this.layerHandler = (LayerHandler) obj;		
 		} else if (obj instanceof LayersMenu) {
 			this.layersMenu = (LayersMenu) obj;
 		} else if (obj instanceof SidePanel) {
 			this.sidePanel = (SidePanel) obj;
 		}
 	}	    
 
 	public void findAndUndo(Object obj) {}
 
 }
 
 
 class InitiateHealthCheckThread extends Thread {
 	
 	EAVDAMData data;
 	AISDatalinkCheckListener listener;
 	boolean checkRule1;
 	boolean checkRule2;
 	boolean checkRule3;
 	boolean checkRule4;
 	boolean checkRule5;
 	boolean checkRule6;
 	boolean checkRule7;
 	boolean includePlanned;
 	double topLeftLatitude;
 	double topLeftLongitude;
 	double lowerRightLatitude;
 	double lowerRightLongitude;
 	double resolution;
 	
 	InitiateHealthCheckThread(EAVDAMData data, AISDatalinkCheckListener listener, boolean checkRule1, boolean checkRule2,
 			boolean checkRule3, boolean checkRule4, boolean checkRule5, boolean checkRule6, boolean checkRule7, boolean includePlanned,
 			double topLeftLatitude, double topLeftLongitude, double lowerRightLatitude, double lowerRightLongitude, double resolution) {
 		this.data = data;
 		this.listener = listener;
 		this.checkRule1 = checkRule1;
 		this.checkRule2 = checkRule2;
 		this.checkRule3 = checkRule3;
 		this.checkRule4 = checkRule4;
 		this.checkRule5 = checkRule5;
 		this.checkRule6 = checkRule6;
 		this.checkRule7 = checkRule7;
 		this.includePlanned = includePlanned;
 		this.topLeftLatitude = topLeftLatitude;
 		this.topLeftLongitude = topLeftLongitude;
 		this.lowerRightLatitude = lowerRightLatitude;
 		this.lowerRightLongitude = lowerRightLongitude;
 		this.resolution = resolution;
 	}
 	
 	public void run() {
 		HealthCheckHandler hch = new HealthCheckHandler(data);		
 		hch.startAISDatalinkCheck(listener, checkRule1, checkRule2, checkRule3, checkRule4, checkRule5, checkRule6,
 			checkRule7, includePlanned, topLeftLatitude, topLeftLongitude, lowerRightLatitude, lowerRightLongitude, resolution);		
 		
 		// XXX: for testing -->
 		/*
 		for (int i=1; i<=5; i++) {
 			listener.progressed(0.2*i);
 			try {
 				Thread.sleep(1000);							
 			} catch (InterruptedException ex) {}
 		}
 		AISDatalinkCheckResult result = null;
 		*/
 		// XXX: <-- for testing
 		
 		//listener.completed(result);
 	}
 
 }
