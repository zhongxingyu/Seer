 package com.xmleditor.gui;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpringLayout;
 
 import skillsplanner.beans.Skill;
 import skillsplanner.resources.SkillManager;
 import skillsplanner.utils.ListUtils;
 
 import com.xmleditor.gui.dialogs.SkillEditPanel;
 import com.xmleditor.gui.listeners.Rudra;
 import com.xmleditor.gui.listeners.SkillMenuListener;
 
 
 public class SkillsTab extends JPanel implements ActionListener,Observer{
 	JPanel centerPanel = new JPanel(new BorderLayout());
 	SkillEditPanel skillEditPanel = new SkillEditPanel(null);
 
 	public SkillsTab(){
 		super();
 		initGUI();
 		SkillManager.getInstance().addObserver(this);
 	}
 	
 	private void initGUI(){
 		this.removeAll();
 		this.setLayout(new BorderLayout());
 		//CENTER
 //		List<Skill> list = ListUtils.getListFromMap(SkillManager.getInstance().getAllSkills());
 //		Skill[] holder = new Skill[list.size()];
 //		JList<Skill> listpane = new JList<Skill>(list.toArray(holder));
 //		listpane.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 //		JScrollPane scroller = new JScrollPane(listpane);
 //		this.add(scroller,BorderLayout.CENTER);
 		
 		//Whole new design of awesomeness
 		JPanel leftMenu = new JPanel();
 		leftMenu.setLayout(new BoxLayout(leftMenu,BoxLayout.Y_AXIS));
 		JPanel skillList = new JPanel();
 		JScrollPane scrollpanel = new JScrollPane(leftMenu);
 		centerPanel.add(scrollpanel,BorderLayout.WEST);
 		centerPanel.add(skillList,BorderLayout.CENTER);
 		
 		SkillMenuListener skillMenuListener = new SkillMenuListener(this);
 		
 		TreeMap<String,TreeSet<Skill>> skillMap = new TreeMap<String,TreeSet<Skill>>();
 		
 		//add skills to the skill menu 
 		for(Skill sk : SkillManager.getInstance().getAllSkills().values()){
			if(!skillMap.containsKey(sk.getTree())){
				skillMap.put(sk.getTree(), new TreeSet<Skill>());
 			}
			skillMap.get(sk.getTree()).add(sk);
 		}
 		
 		//make the skill menu
 		for(String header : skillMap.keySet()){
 			JPanel contents = new JPanel();
 			contents.setLayout(new BoxLayout(contents,BoxLayout.Y_AXIS));
 			for(Skill sk : skillMap.get(header)){
 				contents.add(new SkillPanel(skillMenuListener,sk));
 			}
 			HideableJPanel panel = new HideableJPanel(header,contents);
 			leftMenu.add(panel);
 		}
 		
 		//the glue to hold it all together. Fills vertical space to keep everything from expanding
 		leftMenu.add(Box.createVerticalGlue());
 		leftMenu.add(Box.createVerticalStrut(400));
 		
 		this.add(centerPanel,BorderLayout.CENTER);
 		
 		// BOTTOM PANEL
 		JPanel buttonPanel = new JPanel();
 		JButton newButton = new JButton("New Skill");
 		Rudra rudra = new Rudra();
 		newButton.addActionListener(rudra);
 		newButton.setActionCommand(Rudra.NEW_SKILL);
 		JButton editButton = new JButton("Edit Class");
 		editButton.setActionCommand(Rudra.EDIT_SKILL);
 		editButton.addActionListener(rudra);
 		
 		buttonPanel.add(newButton);
 		buttonPanel.add(editButton);
 		
 		centerPanel.add(skillEditPanel,BorderLayout.CENTER);
 		this.add(buttonPanel,BorderLayout.SOUTH);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Sets the rightpane to edit the skill
 	 * @param skill
 	 */
 	public void selectSkill(Skill skill) {
 		// TODO Auto-generated method stub
 		centerPanel.remove(skillEditPanel);
 		skillEditPanel = new SkillEditPanel(skill);
 		centerPanel.add(skillEditPanel,BorderLayout.CENTER);
 		centerPanel.revalidate();
 		centerPanel.updateUI();
 		this.validate();
 		this.repaint();
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		System.out.println("Reupdate GUI");
 		initGUI();
 		this.revalidate();
 	}
 }
