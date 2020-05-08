 package skillsplanner.gui.custom;
 
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.RenderingHints;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.border.Border;
 
 import skillsplanner.beans.Skill;
 import skillsplanner.resources.ImageManager;
 import skillsplanner.resources.SkillManager;
 import skillsplanner.resources.StaticResources;
 import skillsplanner.utils.skills.SkillHandler;
 import skillsplanner.utils.skills.errors.CurrentRequirementException;
 import skillsplanner.utils.skills.errors.MaxLevelException;
 import skillsplanner.utils.skills.errors.MinLevelException;
 import skillsplanner.utils.skills.errors.RequirementsNotMetException;
 import skillsplanner.utils.skills.errors.SPException;
 
 /**
  * A clickable panel with the option of storing a skill name. Used for Leveling up skills. Registers Right click as level down and left click as level up.
  *
  */
 public class ClickableSkillPanel extends JPanel implements MouseListener{
 	private static final long serialVersionUID = 1L;
 	private final int width = 250;
 	private final int height = 50;
 	Border raised;
 	Border lowered;
 	Border empty;
 	Color currentColor;
 	Skill skill;
 	JLabel levelInfo;
 	private int skillrequired = 0;
 	
 	public ClickableSkillPanel(Skill st){
 		this.setName(st.getName());
 		skill = st;
 		setDimensions();
 	}
 	
 	public ClickableSkillPanel(){
 		setDimensions();
 	}
 	
 	public ClickableSkillPanel(String string) {
 		// TODO Auto-generated constructor stub
 		this.setName(string);
 	}
 
 	/**
 	 * Sets the size for the clickable label as well as some other setup
 	 */
 	private void setDimensions(){
 		
 		this.setLayout(new BorderLayout());
 		this.setMinimumSize(new Dimension(width,height));
 		//this.setPreferredSize(new Dimension(width,height));
 		this.setMaximumSize(new Dimension(Short.MAX_VALUE,height));
 		String tooltip = "<html>Click to level up. Right click to level down. <br />";
 		tooltip += SkillHandler.getRequirementsAsString(this.skill).replaceAll("\\n", "<br />");
 		tooltip += "</html>";
 		this.setToolTipText(tooltip);
 		
 		raised = BorderFactory.createRaisedBevelBorder();
 		lowered = BorderFactory.createLoweredBevelBorder();
 		empty = BorderFactory.createEtchedBorder(Color.white, Color.DARK_GRAY);
 		
 		//this.setBorder(lowered);
 		JPanel centerPanel = new JPanel(new GridLayout(0,1));
 		centerPanel.setOpaque(false);
 		JLabel label = new JLabel(this.getName());
 		label.setHorizontalAlignment(JLabel.CENTER);
 		label.setForeground(Color.WHITE);
 		centerPanel.add(label);
 		
 		levelInfo = new JLabel();
 		levelInfo.setHorizontalAlignment(JLabel.CENTER);
 		levelInfo.setForeground(Color.WHITE);
 		centerPanel.add(levelInfo);
 		
 		this.add(centerPanel,BorderLayout.CENTER);
 		
 		JLabel spCostLabel = new JLabel("SP: "+skill.getSpcost());
 		spCostLabel.setForeground(Color.WHITE);
 		spCostLabel.setPreferredSize(new Dimension((int) (this.getPreferredSize().getWidth()/6),(int) this.getPreferredSize().getHeight()));
 		this.add(spCostLabel,BorderLayout.EAST);
 		
 		try {
			BufferedImage icon = ImageManager.getImage(this.skill.getName());
 			JLabel image = new JLabel(new ImageIcon(icon));
 			//image.setMaximumSize(new Dimension((int)this.getPreferredSize().getWidth()/10,(int)this.getPreferredSize().getHeight()));
 			this.add(image,BorderLayout.WEST);
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		this.setCursor(new Cursor(Cursor.HAND_CURSOR));
 		this.addMouseListener(this);
 		
 		currentColor = new Color(0,0,0,220);
 		//this.setBackground(Color.BLACK);
 		this.setBackground(Color.WHITE);
 		this.setOpaque(false);
 		this.setBorder(empty);
 	}
 	
 	public void setSkillName(String skill){
 		this.setName(skill);
 	}
 	
 	public String getSkillName(){
 		return this.getName();
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		if(arg0.getButton() == MouseEvent.BUTTON1){
 			levelUpSkill();
 		}
 		else if(arg0.getButton() == MouseEvent.BUTTON2){
 			//do nothing
 		}
 		else if(arg0.getButton() == MouseEvent.BUTTON3){
 			levelDownSkill();
 		}
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		currentColor = new Color(0,0,0,255); //full black
 		if(!SkillHandler.requirementsMet(this.skill)){
 			//Notify the brigade captain, I'm going in
 			SkillsPane pane = (SkillsPane) this.getParent();
 			pane.notifyRequiredSkills(this.skill.getSkillRequirements());
 			
 		}
 		this.setBorder(raised);
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		currentColor = new Color(0,0,0,220); //slightly see through black
 		if(!SkillHandler.requirementsMet(this.skill)){
 			//Notify the brigade captain, I'm going in
 			SkillsPane pane = (SkillsPane) this.getParent();
 			pane.notifyRequiredSkillsofLeave(this.skill.getSkillRequirements());
 			
 		}
 		this.setBorder(empty);
 	}
 
 	private void levelUpSkill(){
 		try {
 			SkillHandler.levelUp(this.skill);
 			//updateInformation();
 		} catch (RequirementsNotMetException e) {
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 		} catch (SPException e) {
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 		} catch (MaxLevelException e) {
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 		} 
 	}
 	
 	private void levelDownSkill(){
 		try {
 			SkillHandler.levelDown(this.skill);
 			//updateInformation();
 		} catch (MinLevelException e) {
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 		} catch (CurrentRequirementException e) {
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 		} 
 	}
 	
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		this.setBorder(lowered);
 	}
 
 	private void updateInformation() {
 		// TODO Auto-generated method stub
 		this.levelInfo.setText("Current Level: "+StaticResources.getCharacter().getDFOClass().getSkills().get(skill.getName())+"    Max Level: "+skill.getMaxLevel());
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		this.setBorder(raised);
 		
 	}
 	
 	/**
 	 * Ovverride the paint component to make a rounded look. Adapted from http://www.curious-creature.org/2007/08/01/rounded-corners-and-shadow-for-dialogs-extreme-gui-makeover/
 	 */
 	@Override
 	public void paintComponent(Graphics g){
 		updateInformation();
 		int x = 2;
 		int y = 2;
 		int w = getWidth() - 4;
 		int h = getHeight() - 4;
 		int arc = 8;
 		
 		
 		Graphics2D g2 = (Graphics2D) g.create();
 		
 		Color overlayColor = Color.RED; //red for now
 		Color textColor = Color.black;
 		
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 	            RenderingHints.VALUE_ANTIALIAS_ON);
 
 	    g2.setColor(currentColor);
 	    g2.fillRoundRect(x, y, w, h, arc, arc);
 	    
 	    if(skillrequired != 0){
 	    	g2.setColor(overlayColor);
 			g2.fillRoundRect(x, y, w, h, arc, arc);
 			
 			levelInfo.setText("REQUIRES LEVEL "+skillrequired);
 			
 			//g2.setColor(textColor);
 			//g2.drawString("REQUIRED LEVEL " + skillrequired+ "!", getWidth()/2, getHeight()/2);//draw the warning in the center
 	    }
 	    else{
 	    	levelInfo.setText("Current Level: "+StaticResources.getCharacter().getDFOClass().getSkills().get(skill.getName())+"    Max Level: "+ skill.getMaxLevel());
 	    }
 
 	    g2.dispose();
 
 	}
 	
 	/**
 	 * Override the getPreferredSize method in order to define width and height
 	 */
 	@Override
 	public Dimension getPreferredSize(){
 		Dimension d = new Dimension(width,height);
 		
 		return d;
 	}
 
 	public void skillIsRequired(int level) {
 		// TODO Auto-generated method stub
 		this.skillrequired = level;
 	}
 
 	
 
 }
