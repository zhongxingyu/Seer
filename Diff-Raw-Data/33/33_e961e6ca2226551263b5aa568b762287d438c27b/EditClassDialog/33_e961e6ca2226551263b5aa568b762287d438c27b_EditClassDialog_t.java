 package com.xmleditor.gui.dialogs;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.swing.Action;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.ListModel;
 
 import com.xmleditor.io.XMLWriter;
 
 import skillsplanner.beans.DFOClass;
 import skillsplanner.beans.Skill;
 import skillsplanner.io.IOHandler;
 import skillsplanner.resources.SkillManager;
 import skillsplanner.utils.ListUtils;
 import skillsplanner.utils.skills.SkillHandler;
 
 public class EditClassDialog extends JFrame implements ActionListener{
 	private static final String ADD_TO = "ADD";
 	private static final String REMOVE_FROM = "REMOVE";
 	private static final String SAVE = "SAVE";
 	private static final String CANCEL = "CANCEL";
 
 	DFOClass _class;
 	
 	JTextPane name;
 	JTextPane description;
 	JList<Skill> availableSkills;
 	JList<Skill> skills;
 	DefaultListModel<Skill> availableSkillsList;
 	DefaultListModel<Skill> skillsList;
 
 	public EditClassDialog(DFOClass dfoclass) {
 		_class = dfoclass;
 		if(_class == null){
 			JOptionPane.showMessageDialog(null, "Nothing Selected");
 		}
 		else{
 			initGUI();
 			this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 			setSize(800,800);
 			setVisible(true);
 		}
 	}
 	
 	private void initGUI(){
 		JPanel _this = new JPanel();
 		Collection<Skill> skillManagerList = SkillManager.getInstance().getAllSkills().values();
		ListUtils.sortSkills(skillManagerList);
 		Skill[] c = new Skill[skillManagerList.size()];
 		ListUtils.sortSkills(skillManagerList).toArray(c);
 		ArrayList<Skill> visibleSkills = new ArrayList<Skill>();
 		ArrayList<Skill> classSkillList = new ArrayList<Skill>();
 		//Remove skills that aren't for this class
 		for(Skill s : c){
 			if(s.getTreeGrandfather().compareToIgnoreCase(this._class.getBaseClass()) == 0 ||
 					s.getTree().equalsIgnoreCase("general")){
 				if(this._class.getSkills().containsKey(s.getName())){
 					classSkillList.add(s);
 				}
 				else{
 					visibleSkills.add(s);
 				}
 			}
 		}
 		
 		
 		name = new JTextPane();
 		name.setText(_class.getName());
 		
 		description = new JTextPane();
 		description.setText(_class.getDescription());
 		
 		_this.setLayout(new BoxLayout(_this,BoxLayout.Y_AXIS));
 		
 		JPanel namePanel = new JPanel();
 		namePanel.setLayout(new GridLayout(1,0));
 		JLabel nameLabel = new JLabel("Name:");
 		nameLabel.setAlignmentX(RIGHT_ALIGNMENT);
 		
 		namePanel.add(nameLabel);
 		namePanel.add(name);
 		
 		_this.add(namePanel);
 		
 		JPanel descriptionPanel = new JPanel();
 		descriptionPanel.setLayout(new GridLayout(1,0));
 		JLabel descriptionLabel = new JLabel("Description");
 		descriptionLabel.setAlignmentX(RIGHT_ALIGNMENT);
 		
 		descriptionPanel.add(descriptionLabel);
 		descriptionPanel.add(description);
 		
 		_this.add(descriptionPanel);
 		
 		JPanel skillsManagement = new JPanel();
 		
 		availableSkillsList = new DefaultListModel<Skill>();
 		skillsList = new DefaultListModel<Skill>();
 		
 		for(Skill sk : visibleSkills){
 			availableSkillsList.addElement(sk);
 		}
 		
 		for(Skill sk : classSkillList){
 			skillsList.addElement(sk);
 		}
 		
 		
 		availableSkills = new JList<Skill>(availableSkillsList);
 		skills = new JList<Skill>(skillsList);
 		
 		JScrollPane availableWrapper = new JScrollPane(availableSkills);
 		JScrollPane skillWrapper = new JScrollPane(skills);
 		
 		skillsManagement.add(availableWrapper);
 		
 		JPanel buttonSelection = new JPanel();
 		
 		JButton addTo = new JButton("Add >>>");
 		addTo.addActionListener(this);
 		addTo.setActionCommand(ADD_TO);
 		
 		JButton removeFrom = new JButton("<<< Remove");
 		removeFrom.addActionListener(this);
 		removeFrom.setActionCommand(REMOVE_FROM);
 		
 		buttonSelection.add(addTo);
 		buttonSelection.add(removeFrom);
 		
 		skillsManagement.add(buttonSelection);
 		
 		skillsManagement.add(skillWrapper);
 		
 		_this.add(skillsManagement);
 		
 		JButton saveButton = new JButton("Save");
 		saveButton.setActionCommand(SAVE);
 		saveButton.addActionListener(this);
 		JButton cancelButton = new JButton("Cancel");
 		cancelButton.setActionCommand(CANCEL);
 		cancelButton.addActionListener(this);
 		JPanel savePanel = new JPanel();
 		savePanel.add(saveButton);
 		savePanel.add(cancelButton);
 		
 		_this.add(savePanel);
 		
 		this.getContentPane().add(_this);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		if(e.getActionCommand().equals(ADD_TO)){
 			List<Skill> selected = this.availableSkills.getSelectedValuesList();
 			for(Skill sk : selected){
 				this.skillsList.addElement(sk);
 				this.availableSkillsList.removeElement(sk);
 				_class.addSkill(sk);
 			}
 		}
 		else if (e.getActionCommand().equals(REMOVE_FROM)){
 			List<Skill> selected = this.skills.getSelectedValuesList();
 			for(Skill sk : selected){
 				this.skillsList.removeElement(sk);
 				this.availableSkillsList.addElement(sk);
 				_class.removeSkill(sk);
 			}
 		}
 		else if (e.getActionCommand().equals(SAVE)){
 			if(XMLWriter.getInstance().classToXML(_class)){
 				JOptionPane.showMessageDialog(this, "Save Successful");
 			}
 			else{
 				JOptionPane.showMessageDialog(this, "Save Failed");
 			}
 		}
 		else if (e.getActionCommand().equals(CANCEL)){
 			this.dispose();
 		}
 		
 	}
 
 }
