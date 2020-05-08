 
 package gui.panels.subcontrolpanels;
 
 import gui.panels.ControlPanel;
 import glassLine.*;
 import glassLine.agents.GlassRobotAgent;
 
 import javax.swing.BoxLayout;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 
 import java.awt.GridBagLayout;
 import javax.swing.JCheckBox;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.*;
 
 import javax.swing.JButton;
 import javax.swing.JScrollPane;
 import glassLine.interfaces.*;
 /**
  * The GlassSelectPanel class contains buttons allowing the user to select what
  * type of glass to produce.
  */
 @SuppressWarnings("serial")
 public class GlassSelectPanel extends JPanel implements ActionListener
 {
 	public GlassRobotAgent gRobot;
 	public int glassCount = 0;
 	/** The ControlPanel this is linked to */
 	private ControlPanel parent;
 
 	/** list of checkboxes for creating glass recipe */
 	List<JCheckBox> recipe;
 	
 	List<JButton> glassList;
 	List<Glass>	glasses;
 	
 	JScrollPane scrollPane;
 	private JPanel view = new JPanel();
 	JButton createGlass;
 	/**
 	 * Creates a new GlassSelect and links it to the control panel
 	 * @param cp
 	 *        the ControlPanel linked to it
 	 */
 	public GlassSelectPanel(ControlPanel cp)
 	{
 		recipe = new ArrayList<JCheckBox>();
 		
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
 		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 		
 		/*
 		 * Checkboxes for each station.
 		 * Selected stations will be added to the Glass's recipe
 		 */
 		JCheckBox chckbxCutter = new JCheckBox("CUTTER");
 		recipe.add(chckbxCutter);
 		GridBagConstraints gbc_chckbxCutter = new GridBagConstraints();
 		gbc_chckbxCutter.insets = new Insets(0, 0, 5, 5);
 		gbc_chckbxCutter.gridx = 1;
 		gbc_chckbxCutter.gridy = 0;
 		add(chckbxCutter, gbc_chckbxCutter);
 		chckbxCutter.setHorizontalAlignment(SwingConstants.LEFT);
 		
 		JCheckBox chckbxBreakout = new JCheckBox("BREAKOUT");
 		recipe.add(chckbxBreakout);
 		GridBagConstraints gbc_chckbxBreakout = new GridBagConstraints();
 		gbc_chckbxBreakout.insets = new Insets(0, 0, 5, 5);
 		gbc_chckbxBreakout.gridx = 2;
 		gbc_chckbxBreakout.gridy = 0;
 		add(chckbxBreakout, gbc_chckbxBreakout);
 		chckbxBreakout.setHorizontalAlignment(SwingConstants.RIGHT);
 		
 		JCheckBox chckbxManualBo = new JCheckBox("MANUAL_BREAKOUT");
 		recipe.add(chckbxManualBo);
 		GridBagConstraints gbc_chckbxManualBo = new GridBagConstraints();
 		gbc_chckbxManualBo.insets = new Insets(0, 0, 5, 5);
 		gbc_chckbxManualBo.gridx = 3;
 		gbc_chckbxManualBo.gridy = 0;
 		add(chckbxManualBo, gbc_chckbxManualBo);
 		chckbxManualBo.setHorizontalAlignment(SwingConstants.RIGHT);
 		
 				
 		JCheckBox chckbxCrossseamer = new JCheckBox("CROSS_SEAMER");
 		recipe.add(chckbxCrossseamer);
 		GridBagConstraints gbc_chckbxCrossseamer = new GridBagConstraints();
 		gbc_chckbxCrossseamer.insets = new Insets(0, 0, 5, 5);
 		gbc_chckbxCrossseamer.gridx = 2;
 		gbc_chckbxCrossseamer.gridy = 1;
 		add(chckbxCrossseamer, gbc_chckbxCrossseamer);
 		chckbxCrossseamer.setHorizontalAlignment(SwingConstants.LEFT);
 
 		
 		JCheckBox chckbxDrill = new JCheckBox("DRILL");
 		recipe.add(chckbxDrill);
 		GridBagConstraints gbc_chckbxDrill = new GridBagConstraints();
 		gbc_chckbxDrill.insets = new Insets(0, 0, 5, 5);
 		gbc_chckbxDrill.gridx = 1;
 		gbc_chckbxDrill.gridy = 1;
 		add(chckbxDrill, gbc_chckbxDrill);
 		chckbxDrill.setHorizontalAlignment(SwingConstants.LEFT);
 		
 		JCheckBox chckbxGrinder = new JCheckBox("GRINDER");
 		recipe.add(chckbxGrinder);
 		GridBagConstraints gbc_chckbxGrinder = new GridBagConstraints();
 		gbc_chckbxGrinder.insets = new Insets(0, 0, 5, 5);
 		gbc_chckbxGrinder.gridx = 3;
 		gbc_chckbxGrinder.gridy = 1;
 		add(chckbxGrinder, gbc_chckbxGrinder);
 		chckbxGrinder.setHorizontalAlignment(SwingConstants.LEFT);
 		
 		JCheckBox chckbxWasher = new JCheckBox("WASHER");
 		recipe.add(chckbxWasher);
 		GridBagConstraints gbc_chckbxWasher = new GridBagConstraints();
 		gbc_chckbxWasher.insets = new Insets(0, 0, 5, 5);
 		gbc_chckbxWasher.gridx = 1;
 		gbc_chckbxWasher.gridy = 2;
 		add(chckbxWasher, gbc_chckbxWasher);
 		chckbxWasher.setHorizontalAlignment(SwingConstants.RIGHT);
 		
 				
 		JCheckBox chckbxUv = new JCheckBox("UV_LAMP");
 		recipe.add(chckbxUv);
 		GridBagConstraints gbc_chckbxUv = new GridBagConstraints();
 		gbc_chckbxUv.insets = new Insets(0, 0, 5, 5);
 		gbc_chckbxUv.gridx = 2;
 		gbc_chckbxUv.gridy = 2;
 		add(chckbxUv, gbc_chckbxUv);
 		chckbxUv.setHorizontalAlignment(SwingConstants.RIGHT);
 
 		
 		JCheckBox chckbxPaint = new JCheckBox("PAINTER");
 		recipe.add(chckbxPaint);
 		GridBagConstraints gbc_chckbxPaint = new GridBagConstraints();
 		gbc_chckbxPaint.insets = new Insets(0, 0, 5, 5);
 		gbc_chckbxPaint.gridx = 3;
 		gbc_chckbxPaint.gridy = 2;
 		add(chckbxPaint, gbc_chckbxPaint);
 		chckbxPaint.setHorizontalAlignment(SwingConstants.LEFT);
 				
 						
 		JCheckBox chckbxBake = new JCheckBox("OVEN");
 		recipe.add(chckbxBake);
 		GridBagConstraints gbc_chckbxBake = new GridBagConstraints();
 		gbc_chckbxBake.insets = new Insets(0, 0, 5, 5);
 		gbc_chckbxBake.gridx = 1;
 		gbc_chckbxBake.gridy = 3;
 		add(chckbxBake, gbc_chckbxBake);
 		chckbxBake.setHorizontalAlignment(SwingConstants.LEFT);
 
 		
 		createGlass = new JButton("Create Glass");
 		GridBagConstraints gbc_createGlass = new GridBagConstraints();
 		gbc_createGlass.insets = new Insets(0, 0, 5, 5);
 		gbc_createGlass.gridx = 3;
 		gbc_createGlass.gridy = 3;
 		add(createGlass, gbc_createGlass);
 		createGlass.addActionListener(this);
 		
 		scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
 				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridx = 1;
 		gbc_scrollPane.gridy = 4;
 
 		view.setLayout(new BoxLayout((Container) view, BoxLayout.Y_AXIS));
 		scrollPane.setViewportView(view);
 		add(scrollPane, gbc_scrollPane);
 		glassList = new ArrayList<JButton>();
 		glasses = new ArrayList<Glass>();
 		
 		parent = cp;
 		
 		for(int i = 0; i < recipe.size(); i++){
 			recipe.get(i).addActionListener(this);
 		}
 	}
 
 	/**
 	 * Returns the parent panel
 	 * @return the parent panel
 	 */
 	public ControlPanel getGuiParent()
 	{
 		return parent;
 	}
 	
 	public void setGlassRobot(GlassRobotAgent gr){
 		gRobot = gr;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		// check which checkboxes are selected when the createGlas button is pressed
 		// the selected stations will be added to the glass' recipe 
 		if(ae.getSource() == createGlass){
 			createGlass(JOptionPane.showInputDialog("Please enter a name for glass:"));
			glassCount++;
 		}
 		else{
 			
 			for(int i=0; i < glassList.size(); i++){
 				JButton temp = glassList.get(i);
 
 				if(ae.getSource() == temp)
 				    parent.getGlassInfoPanel().updateGlassInfo(glasses.get(i));		
 			}  
 		}
 	}
 	private void createGlass(String name){
 		if(name != null){
 			List<String> station = new ArrayList<String>();
 			for(JCheckBox cb:recipe){
 				if(cb.isSelected()){
 					station.add(cb.getText());
 				}
 			}
 			Glass temp = new Glass(glassCount, station);
 			parent.makeGlass(temp);
 			gRobot.addGlass(temp);
 			//add to the list
 			JButton glass = new JButton(name);
 			glass.setBackground(Color.white);
 			Dimension paneSize = this.scrollPane.getSize();
 			Dimension buttonSize = new Dimension(paneSize.width-20, 
 							     (int)(paneSize.height/5));
 			glass.setPreferredSize(buttonSize);
 			glass.setMinimumSize(buttonSize);
 			glass.setMaximumSize(buttonSize);
 			glass.addActionListener(this);
 			
 			this.view.add(glass);
 			this.glasses.add(temp);
 			this.glassList.add(glass);
 			validate();
 		}
 	}
 }
