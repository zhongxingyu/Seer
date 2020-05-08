 package ussr.aGui.tabs.view;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 
 import javax.swing.AbstractButton;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JRadioButton;
 
 import ussr.aGui.MainFrame;
 import ussr.aGui.MainFrameInter;
 import ussr.aGui.tabs.controller.ConstructionTabController;
 import ussr.builder.SupportedModularRobots;
 import ussr.physics.jme.JMESimulation;
 
 /**
  * Defines visual appearance of the tab called "1 Step: Construct Robot".  
  * @author Konstantinas
  */
 public class ConstructionTab extends Tabs {
 
 	/**
 	 * The constants of grid bag layout used during design of the tab.
 	 */
 	private GridBagConstraints gridBagConstraints = new GridBagConstraints();
 
 	/**
 	 * Defines visual appearance of the tab called "1 Step: Construct Robot".
 	 * @param firstTabbedPane,
 	 * @param tabTitle, the title of the tab
 	 * @param jmeSimulation, the physical simulation.
 	 * @param imageIconDirectory,
 	 */
 	public ConstructionTab(boolean firstTabbedPane,String tabTitle,JMESimulation jmeSimulation,String imageIconDirectory){
 		
 		this.firstTabbedPane = firstTabbedPane;
 		this.tabTitle = tabTitle;
 		this.jmeSimulation = jmeSimulation;
 		this.imageIconDirectory = imageIconDirectory; 
 		
 		/*instantiate new panel, which will be the container for all components situated in the tab*/		
 		this.jComponent = new javax.swing.JPanel(new GridBagLayout());		
 		initComponents();
 	}
 
 
 
 	/**
 	 * Initializes the visual appearance of all components in the tab.
 	 * Follows Strategy  pattern.
 	 */
 	public void initComponents(){
         
 		/*Instantiation of components*/
 		jToolBar1 = new javax.swing.JToolBar();
 		
 		button10 =   new javax.swing.JButton();
 		button11 =   new javax.swing.JButton();	
 		button12 =   new javax.swing.JButton();
 		
		jLabel10002 = new javax.swing.JLabel();		
	
 		
 		jLabel10002.setText("Shortcut:");
 		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 0;
 		jComponent.add(jLabel10002,gridBagConstraints);
 
 		final ButtonGroup buttonGroup = new ButtonGroup() ;
 
 		radionButtonATRON =  new JRadioButton();
 		radionButtonATRON.setText("ATRON");		
 		radionButtonATRON.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				ConstructionTabController.jButtonGroupActionPerformed(radionButtonATRON,jmeSimulation);
 			}
 		});
 		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.gridx = 1;
 		gridBagConstraints.gridy = 1;
 		//gridBagConstraints.ipady = 10; 
 		jComponent.add(radionButtonATRON,gridBagConstraints);
 		buttonGroup.add(radionButtonATRON);
 
 		radionButtonODIN =  new JRadioButton();
 		radionButtonODIN.setText("Odin");
 		radionButtonODIN.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				ConstructionTabController.jButtonGroupActionPerformed(radionButtonODIN,jmeSimulation);
 			}
 		});
 		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.gridx = 2;
 		gridBagConstraints.gridy = 1;
 		//gridBagConstraints.ipady = 10; 
 		jComponent.add(radionButtonODIN,gridBagConstraints);
 		buttonGroup.add(radionButtonODIN);
 
 
 		radioButtonMTRAN =  new JRadioButton();
 		radioButtonMTRAN.setText("MTran");
 		radioButtonMTRAN.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				ConstructionTabController.jButtonGroupActionPerformed(radioButtonMTRAN,jmeSimulation);
 			}
 		});
 		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.gridx = 3;
 		gridBagConstraints.gridy = 1;
 		//gridBagConstraints.ipady = 10; 
 		jComponent.add(radioButtonMTRAN,gridBagConstraints);
 		buttonGroup.add(radioButtonMTRAN);
 
 		radionButtonCKBOTSTANDARD =  new JRadioButton();
 		radionButtonCKBOTSTANDARD.setText("CKBotStandard");
 		radionButtonCKBOTSTANDARD.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				ConstructionTabController.jButtonGroupActionPerformed(radionButtonCKBOTSTANDARD,jmeSimulation);
 			}
 		});
 		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.gridx = 4;
 		gridBagConstraints.gridy = 1;
 		//gridBagConstraints.ipady = 40; 
 		jComponent.add(radionButtonCKBOTSTANDARD,gridBagConstraints);
 		buttonGroup.add(radionButtonCKBOTSTANDARD);		
 
		jLabel1000 = new javax.swing.JLabel();
		javax.swing.ImageIcon informationIcon =  new javax.swing.ImageIcon(DIRECTORY_ICONS + INFORMATION);
		jLabel1000.setIcon(informationIcon);
 		jLabel1000.setText("Next select connectors (black and white geometric shapes).");
 		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.gridx = 1;
 		gridBagConstraints.gridy = 2;
 		gridBagConstraints.gridwidth = 4;
 		gridBagConstraints.insets = new Insets(5,0,0,0);  //top padding
 		//gridBagConstraints.weighty = 0.5;   //request any extra vertical space
 		jComponent.add(jLabel1000,gridBagConstraints);
 		jLabel1000.setVisible(false);		
 
 
 		jLabel10001 = new javax.swing.JLabel();		
 		jLabel10001.setText("When done with constructing, go to the next tab.");
 		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.gridx = 1;
 		gridBagConstraints.gridy = 3;
 		gridBagConstraints.gridwidth = 4;
		gridBagConstraints.insets = new Insets(0,30,0,0);  //left padding
 		jComponent.add(jLabel10001,gridBagConstraints);
 		jLabel10001.setVisible(false);
 		
 		
 		jLabel10003 = new javax.swing.JLabel();		
 		jLabel10003.setText("Additional:");
 		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 4;
		gridBagConstraints.insets = new Insets(0,0,0,30);  //bring padding back
 		jComponent.add(jLabel10003,gridBagConstraints);
    
 		jLabel10004 = new javax.swing.JLabel();		
 		jLabel10004.setText("Generic functionality:");
 		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.gridx = 1;
 		gridBagConstraints.gridy = 5;
 		jComponent.add(jLabel10004,gridBagConstraints);		
 		
 		jToolBar1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
 		jToolBar1.setFloatable(false);//user can not make the tool bar to float
 		jToolBar1.setRollover(true);// the buttons inside are roll over
 		jToolBar1.setToolTipText("Generic tools");
 		jToolBar1.setPreferredSize(new Dimension(100,40));
 		
 		gridBagConstraints.fill = GridBagConstraints.LINE_END;
 		gridBagConstraints.gridx = 3;		
 		gridBagConstraints.gridy = 5;
 		//jComponent.add(button11,gridBagConstraints);	
 			
 		button11.setToolTipText("Move module");
 		button11.setIcon(new javax.swing.ImageIcon(DIRECTORY_ICONS + MOVE_MODULE));
 		button11.setDisabledIcon(new javax.swing.ImageIcon(MainFrameInter.DIRECTORY_ICONS + MainFrameInter.NO_ENTRANCE));
 		button11.setFocusable(false); 
 		button11.setEnabled(false);
 		button11.setPreferredSize(new java.awt.Dimension(30, 30));	
 		button11.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				ConstructionTabController.jButton11ActionPerformed(jmeSimulation);
 			}
 		});
 		jToolBar1.add(button11);
 				
 		button10.setToolTipText("Delete module");
 		button10.setIcon(new javax.swing.ImageIcon(DIRECTORY_ICONS + DELETE_MODULE));
 		button10.setDisabledIcon(new javax.swing.ImageIcon(MainFrameInter.DIRECTORY_ICONS + MainFrameInter.NO_ENTRANCE));		
 		button10.setFocusable(false);
 		button10.setEnabled(false);
 		button10.setPreferredSize(new java.awt.Dimension(30, 30));
 		button10.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				ConstructionTabController.jButton10ActionPerformed(jmeSimulation);
 			}
 		});
 		//gridBagConstraints.fill = GridBagConstraints.LINE_END;
 		//gridBagConstraints.gridx = 3;
 		//gridBagConstraints.gridwidth = 1; 
 		//gridBagConstraints.gridy = 5;
 		jToolBar1.add(button10);		
 		
 		button12.setToolTipText("Colour connectors");
 		button12.setIcon(new javax.swing.ImageIcon(DIRECTORY_ICONS + COLOUR_CONNECTORS));
 		button12.setDisabledIcon(new javax.swing.ImageIcon(MainFrameInter.DIRECTORY_ICONS + MainFrameInter.NO_ENTRANCE));		
 		button12.setFocusable(false);
 		button12.setEnabled(false);
 		button12.setPreferredSize(new java.awt.Dimension(30, 30));
 		button12.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				ConstructionTabController.jButton12ActionPerformed(jmeSimulation);
 			}
 		});
 		//gridBagConstraints.fill = GridBagConstraints.LINE_END;
 		//gridBagConstraints.gridx = 4;
 		//gridBagConstraints.gridwidth = 1; 
 		//gridBagConstraints.gridy = 5;
 		//jComponent.add(button12,gridBagConstraints);
 		jToolBar1.add(button12);
 		jComponent.add(jToolBar1,gridBagConstraints);
 		
 		
 			
 		
 
 		
 		
 		
 		
 		
 		
 		/*button10 =   new javax.swing.JButton();
 		button10.setIcon(new javax.swing.ImageIcon(DIRECTORY_ICONS + DELETE_MODULE));
 		button10.setToolTipText("Delete module");
 		button10.setFocusable(false);    
 		button10.setPreferredSize(new java.awt.Dimension(30, 30));   
 		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.gridx = 2;
 		gridBagConstraints.gridy = 5;
 		jComponent.add(button10,gridBagConstraints);*/
 
 		//javax.swing.AbstractButton button =  new JButton();
 
 
 
 		//button.setText("ODIN");
 
 		//buttonGroup.add(button);
 		//buttonGroup.add(button1);
 
 		//jComponent.add(buttonGroup);
 
 		/*Instantiation of tab components
 		jComboBox2 = new javax.swing.JComboBox();		
 		jComboBox3 = new javax.swing.JComboBox();		
 		jComboBox1000 = new javax.swing.JComboBox();
 
 		jLabel1000 = new javax.swing.JLabel();	
 
 		Definition of visual appearance for each instantiated component
 		jLabel1000.setText("1) Choose modular robot:");
 		Add your component into panel.
 		jComponent.add(jLabel1000);
 
 		jComboBox1000.setToolTipText("Supported modular robots");
 		jComboBox1000.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ATRON", "ODIN", "MTRAN", "CKBOT" }));
 		jComboBox1000.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				ConstructionTabController.jComboBox1ActionPerformed(jComboBox1000,jmeSimulation);
 			}
 		});
 		jComponent.add(jComboBox1000);
 
 		javax.swing.JLabel jLabel999 = new javax.swing.JLabel();
 		jLabel999.setText(" and locate it in the simulation environment;");
 		jComponent.add(jLabel999);
 
 		javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
 		jLabel3.setText("2) Choose module rotation (in the one of the following three components)");
 		jComponent.add(jLabel3);       
 
 		jComboBox2.setToolTipText("Standard rotations of modules");
 		jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
 		jComboBox2.setEnabled(false);
 		jComboBox2.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				ConstructionTabController.jComboBox2ActionPerformed(jmeSimulation);
 			}
 		});
 		jComponent.add(jComboBox2);
 
 		javax.swing.JButton jButton5 = new javax.swing.JButton();
 		jButton5.setToolTipText("Rotate opposite");        
 		jButton5.setText("Opposite");        
 		jButton5.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				ConstructionTabController.jButton5ActionPerformed(jmeSimulation);
 			}
 		});
 		jComponent.add(jButton5);
 
 		javax.swing.JButton jButton6 = new javax.swing.JButton();
 		jButton6.setToolTipText("Variation of module rotations");
 		jButton6.setText("Variation");
 		jButton6.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				ConstructionTabController.jButton6ActionPerformed(jmeSimulation);
 			}
 		});
 		jComponent.add(jButton6);
 
 		javax.swing.JLabel jLabel998 = new javax.swing.JLabel();
 		jLabel998.setText(" and select the module in the simulation environment to apply it to;");
 		jComponent.add(jLabel998);
 
 		javax.swing.JLabel jLabel997 = new javax.swing.JLabel();
 		jLabel997.setText(" 3)Choose one of the following three construction tools:");
 		jComponent.add(jLabel997);
 
 		javax.swing.JButton jButton7 = new javax.swing.JButton();
 		jButton7.setToolTipText("Variation of module rotations");
 		jButton7.setText("On Selected Connector");
 		jButton7.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				ConstructionTabController.jButton7ActionPerformed(jmeSimulation);
 			}
 		});
 		jComponent.add(jButton7);
 
 		javax.swing.JLabel jLabel996 = new javax.swing.JLabel();
 		jLabel996.setText("here select connector on the module(white or black geometric shapes) ");
 		jComponent.add(jLabel996);
 
 
 		jComboBox3.setToolTipText("Numbers of connectors on the module");
 		jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "" }));
 		jComboBox3.setEnabled(false);
 		jComboBox3.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				//ConstructionTabController.jComboBox3ActionPerformed(jComboBox1000,jmeSimulation);
 			}
 		});
 		jComponent.add(jComboBox3);*/
 
 
 
 	}
 	/*MORE WORK HERE*/
 	public static void adjustToSelectedModularRobot(SupportedModularRobots supportedModularRobot){
 		if (supportedModularRobot.toString().equalsIgnoreCase("ATRON")){
 			radionButtonATRON.setSelected(true);
 			//ADD MORE HERE AND MAYBE ELIMINATE ABOVE LINE
 		}else if (supportedModularRobot.toString().equalsIgnoreCase("MTRAN")){
 			radioButtonMTRAN.setSelected(true);
 			//ADD MORE HERE AND MAYBE ELIMINATE ABOVE LINE
 		}else if (supportedModularRobot.toString().equalsIgnoreCase("ODIN")){
 			radionButtonODIN.setSelected(true);
 			//ADD MORE HERE AND MAYBE ELIMINATE ABOVE LINE
 		}else if (supportedModularRobot.toString().equalsIgnoreCase("CKBOTSTANDARD")){
 			radionButtonCKBOTSTANDARD.setSelected(true);	
 			//ADD MORE HERE AND MAYBE ELIMINATE ABOVE LINE
 		}
 	}
 	/*Getters*/
 
 	public static javax.swing.JComboBox getJComboBox2() {
 		return jComboBox2;
 	}
 
 	public static javax.swing.JComboBox getJComboBox3() {
 		return jComboBox3;
 	}
 
 	public static javax.swing.AbstractButton getButton1() {
 		return radionButtonATRON;
 	}
 	
 	public static javax.swing.JLabel getJLabel1000() {
 		return jLabel1000;
 	}
 	
 	public static javax.swing.JLabel getJLabel10001() {
 		return jLabel10001;
 	}
 	
 	public static javax.swing.JButton getButton10() {
 		return button10;
 	}
 	
 	public static javax.swing.JButton getButton11() {
 		return button11;
 	}
 
 	public static javax.swing.JButton getButton12() {
 		return button12;
 	}
 
 	/*Declaration of tab components*/
 	private static javax.swing.JComboBox jComboBox2;	
 	private static javax.swing.JComboBox jComboBox3;
 	private static javax.swing.JLabel jLabel1000;
 	
 
 	private static javax.swing.JLabel jLabel10001;
 	private static javax.swing.JLabel jLabel10002;
 	private static javax.swing.JLabel jLabel10003;
 	private static javax.swing.JLabel jLabel10004;
 
 	private static javax.swing.JComboBox jComboBox1000;
 
 	private static  javax.swing.AbstractButton radionButtonATRON;
 	private static javax.swing.AbstractButton  radioButtonMTRAN;
 	private static javax.swing.AbstractButton radionButtonODIN;
 	private static javax.swing.AbstractButton radionButtonCKBOTSTANDARD;
 	
 	
 	private  static javax.swing.JButton button10;
 	private  static javax.swing.JButton button11;
 	private  static javax.swing.JButton button12;
 	
 	private javax.swing.JToolBar jToolBar1;
 
 	
 
 	
 
 
 
 
 }
