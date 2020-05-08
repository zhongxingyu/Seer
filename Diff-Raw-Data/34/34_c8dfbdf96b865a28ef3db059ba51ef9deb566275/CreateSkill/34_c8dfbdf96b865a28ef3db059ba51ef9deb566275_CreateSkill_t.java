 /*
  *  CreateSkill.java
  *  A windowed view-controller that creates SkillModel instances for the SkillTree project
  *  
  *  Authors: Santiago Torres Arias & Miguel Angel Kelly Delgado
  *  Date:    apr 26, 2013
  */
 import java.lang.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.*;
 import javax.swing.event.*;
 public class CreateSkill implements ActionListener{
 	private JFrame thePanel;
 	private JLabel name;
 	private JLabel MaxLevel;
 	private JTextField nameField;
 	private JTextField maxLevelField;
 	private JTabbedPane descriptionsPane;
 	private ArrayList<JTextArea> descriptionsFields;
 	private JList Requirements;
 	private JList Milestones;
 	private JList Parents;
 	private JButton okButton;
 	private JButton cancelButton;
 	private JButton addButton;
 	private JButton removeButton;
 	private JTextField milestoneEntry;
 	private JScrollPane milestoneViewport;
 	private JScrollPane requirementViewport;
 	private JScrollPane parentViewport;
 	private DefaultListModel milestoneListModel;
 	private DefaultListModel requirementListModel;
 
 
 	private TreeModel tree;
 
 	public CreateSkill(TreeModel theTreeView){
 		this.tree = theTreeView;
 
 		Box vbox = new Box(BoxLayout.Y_AXIS);
 		Box hbox = new Box(BoxLayout.X_AXIS);
 		Box nameBox = new Box(BoxLayout.X_AXIS);
 		Box MaxLVLBox = new Box(BoxLayout.X_AXIS);
 		Box milestoneReqs = new Box(BoxLayout.X_AXIS);
 		Box desParents = new Box(BoxLayout.Y_AXIS);
 		Box OkCancel = new Box(BoxLayout.X_AXIS);
 		Box addMilestone = new Box(BoxLayout.X_AXIS);
 		Box removeMilestone = new Box(BoxLayout.Y_AXIS);
 		listAddListener milestoneListener;
 
 		thePanel = new JFrame("Create a Skill");
 		this.name = new JLabel("Name: ");
 		this.MaxLevel = new JLabel("Max lvl: ");
 
 		this.nameField = new JTextField(30);
 		this.nameField.setMaximumSize(new Dimension(300,20));
 
 		this.maxLevelField = new JTextField(30);
 		this.maxLevelField.setMaximumSize(new Dimension(300,20));
 
 		this.descriptionsPane = new JTabbedPane();
 		this.descriptionsPane.setMaximumSize(new Dimension(300,300));
 		this.descriptionsFields = new ArrayList<JTextArea>(1);
 		this.maxLevelField.addActionListener(new levelListener(this.descriptionsPane,this.descriptionsFields));
 
 		this.Requirements = new JList();
 		this.Requirements.setFixedCellHeight(20);
 		this.Requirements.setFixedCellWidth(300);
 		this.Requirements.setVisibleRowCount(8);
 		this.requirementViewport = new JScrollPane(this.Requirements);
 		this.requirementListModel = new DefaultListModel();
 		this.Requirements.setModel(this.requirementListModel);
 
 		this.milestoneListModel = new DefaultListModel();
 		this.milestoneEntry = new JTextField(30);
 		this.milestoneEntry.setMaximumSize(new Dimension(300,20));
 		this.addButton = new JButton("add");
 		addMilestone.add(this.milestoneEntry);
 		addMilestone.add(this.addButton);
 		this.Milestones = new JList();
 		this.Milestones.setFixedCellHeight(20);
 		this.Milestones.setFixedCellWidth(300);
 		this.Milestones.setVisibleRowCount(8);
 		this.Milestones.setModel(this.milestoneListModel);
 		this.milestoneViewport = new JScrollPane(this.Milestones);
 		this.removeButton = new JButton("remove");
 		milestoneListener= new listAddListener(this.milestoneEntry,this.Milestones,this.milestoneListModel);
 		this.addButton.addActionListener(milestoneListener);
 		this.removeButton.addActionListener(milestoneListener);
 		removeMilestone.add(addMilestone);
 		removeMilestone.add(this.milestoneViewport);
 		removeMilestone.add(this.removeButton);
 		removeMilestone.add(Box.createVerticalGlue());
 
 		this.Parents = new JList(traverseTreeForNames(theTreeView));
 		this.Parents.addListSelectionListener(new ListListener(this.Requirements,theTreeView));
 		this.Parents.setFixedCellHeight(20);
 		this.Parents.setFixedCellWidth(300);
 		this.okButton = new JButton("Ok");
 		this.okButton.addActionListener(this);
 		
 		this.cancelButton = new JButton("Cancel");
 		this.cancelButton.addActionListener(this);
 		this.parentViewport = new JScrollPane(this.Parents);
 		
 		this.descriptionsFields.add(new JTextArea());
 		this.descriptionsPane.addTab("0",this.descriptionsFields.get(0));
 
 		nameBox.add(name);
 		nameBox.add(nameField);
 		nameBox.add(Box.createHorizontalGlue());
 		
 		MaxLVLBox.add(MaxLevel);
 		MaxLVLBox.add(maxLevelField);
 		MaxLVLBox.add(Box.createHorizontalGlue());
 
 
 		milestoneReqs.add(requirementViewport);
 		//milestoneReqs.add(removeMilestone);
 		milestoneReqs.add(Box.createHorizontalGlue());
 
 		vbox.add(nameBox);
 		vbox.add(MaxLVLBox);
 		vbox.add(milestoneReqs);
 		vbox.add(Box.createVerticalGlue());
 
 		OkCancel.add(cancelButton);
 		OkCancel.add(okButton);
 
 		desParents.add(descriptionsPane);
 		desParents.add(parentViewport);
 		desParents.add(OkCancel);
 		desParents.add(Box.createVerticalGlue());
 
 		thePanel.getContentPane().setLayout(new BoxLayout(thePanel.getContentPane(),BoxLayout.X_AXIS));
 		thePanel.add(vbox);
 		thePanel.add(removeMilestone);
 		thePanel.add(desParents);
 		thePanel.add(Box.createHorizontalGlue());
 		thePanel.getContentPane().setMaximumSize(new Dimension(600,400));
 		thePanel.pack();
 		thePanel.setVisible(true);
 	}
 
 
 	private String[] traverseTreeForNames(TreeModel tree){
 		ArrayList<String> names = new ArrayList<String>(tree.getList().size());
 		SkillModel ref;
 		ArrayList<SkillModel> treeList = tree.getList(); 
 		for(int i=0;i<treeList.size();i++){
 			ref = treeList.get(i);
 			names.add(ref.getName());
 		}
 		return names.<String>toArray(new String[tree.getList().size()]);
 	}
 
 	private String[] traverseTreeForRequirements(TreeModel tree, int[] indexes){
 		//String[] names = traverseTreeForNames(tree);
 		String[] Milestones;
 		SkillModel ref;
 		ArrayList<SkillModel> treeList = tree.getList();
 		ArrayList<String> requirements = new ArrayList<String>();
 		for(int i=0;i<indexes.length;i++){
 			ref = treeList.get(indexes[i]);
 			Milestones = ref.getMilestones();
 			for(int j=0;j<Milestones.length;j++){
 				requirements.add(new String(ref.getName() + " - " + Milestones[j]));
 			}
 		
 		}
 		return requirements.<String>toArray(new String[requirements.size()]);
 	}
 
 	public void actionPerformed(ActionEvent e){
 		SkillModel newModel;
 		int descriptionSize;
 		ArrayList<String> descriptions;
 		ArrayList<SkillModel> parents;
 		ArrayList<SkillModel> treeList;
 		int[] parentIndices,requirementIndices,milestonesIndices;
 		String[] requirements;
 		String[] milestones;
 		if("Ok".equals(e.getActionCommand())){
 			treeList = tree.getList();
 			
 			requirementIndices = this.Requirements.getSelectedIndices();
 			requirements = new String[requirementIndices.length];
 
			//milestonesIndices = this.Milestones.size();
			//milestones = new String[milestonesIndices.length];
			milestones = new String[this.milestoneListModel.size()];
			this.milestoneListModel.copyInto(milestones);
 			descriptionSize = this.descriptionsFields.size();
 			descriptions = new ArrayList<String>(descriptionSize);
 			parentIndices = this.Parents.getSelectedIndices();
 			parents = new ArrayList<SkillModel>(parentIndices.length);
 
 			System.out.println("lol " + requirements.length + "/" + requirementIndices.length + "/" + this.requirementListModel.size()); 
/*
 			for(int i=0;i<milestonesIndices.length;i++){
 				milestones[i] = ((String)this.milestoneListModel.get(milestonesIndices[i]));
 			}
*/		
 			for(int i=0;i<requirementIndices.length;i++){
 				requirements[i] = ((String)this.requirementListModel.get(requirementIndices[i]));
 			}
 
 			for(int i=0;i<parentIndices.length;i++){
 				parents.add(i,treeList.get(parentIndices[i]));
 			}
 
 			for(int i=0;i<descriptionSize;i++){
 				descriptions.add(i,((JTextArea)this.descriptionsFields.get(i)).getText());
 			}
 			
 				
 			newModel = new SkillModel(this.nameField.getText(),descriptions.toArray(new String[descriptions.size()]),parents.toArray(new SkillModel[parents.size()]),null,this.descriptionsPane.getTabCount(),milestones,requirements);
 			tree.addSkill(newModel);
			System.out.println(newModel.toString());
			SkillDetailView aview = new SkillDetailView(newModel);
 		}
 		System.out.println(e.getActionCommand());
 
 	}
 
 	private class ListListener implements ListSelectionListener{
 		private JList listToUpdate;
 		TreeModel localModel;	
 		public ListListener(JList listToUpdate,TreeModel model){
 			super();
 			this.listToUpdate = listToUpdate;
 			this.localModel = model;
 		}
 
 		public void valueChanged(ListSelectionEvent e){
 			DefaultListModel listModel = ((DefaultListModel)this.listToUpdate.getModel());
 			if(e.getValueIsAdjusting()){
 				return; //lets wait for non shitty messages
 			}
 			JList source = ((JList)e.getSource());
 			int[] indexes = source.getSelectedIndices();
 			System.out.println(source.getSelectedIndices().length);
 			String[] Requirements = traverseTreeForRequirements(this.localModel,indexes);
 			listModel.clear(); //empty the list for filling again.
 			for(int i=0;i<Requirements.length;i++){
 				listModel.addElement(Requirements[i]);
 			}
 			//this.listToUpdate.setListData(Requirements);
 			this.listToUpdate.validate();
 		}
 	}
 	
 
 	private class levelListener implements ActionListener{
 		private JTabbedPane descriptionsPane;
 		private int noOfTabs;
 		private ArrayList<JTextArea> descriptionTexts;
 		public levelListener(JTabbedPane descriptionsPane, ArrayList<JTextArea> descriptionTexts){
 			this.descriptionsPane=descriptionsPane;
 			this.descriptionTexts= descriptionTexts;
 			this.noOfTabs=this.descriptionTexts.size();
 		}
 
 		public void actionPerformed(ActionEvent e){
 			this.noOfTabs = Integer.parseInt(e.getActionCommand());
 			if(this.noOfTabs<1){
 				this.noOfTabs=1;
 			}
 			System.out.println(e.getActionCommand());
 			while(this.noOfTabs!=this.descriptionsPane.getTabCount()){
 				System.out.println("Current Tabs: " +this.noOfTabs + "/" + this.descriptionsPane.getTabCount());
 				if(this.noOfTabs>this.descriptionsPane.getTabCount()){
 					descriptionTexts.add(new JTextArea());
 					descriptionsPane.addTab(""+this.descriptionsPane.getTabCount(),descriptionTexts.get(this.descriptionsPane.getTabCount()));
 				}else if(this.noOfTabs<this.descriptionsPane.getTabCount()){
 					descriptionsPane.remove(this.noOfTabs);
 				}
 					
 		
 			}
 		}
 
 	}
 	private class listAddListener implements ActionListener{
 		private JTextField contentsToAdd;
 		private DefaultListModel listModel;
 		private JList addingList;
 		ArrayList<String> tempString;
 		public listAddListener(JTextField fieldToAdd,JList addingList, DefaultListModel listModel){
 			super();
 			this.contentsToAdd=fieldToAdd;
 			this.addingList = addingList;
 			this.listModel = listModel;
 			//this.addingList.setModel(this.listModel);
 		}
 
 		public void actionPerformed(ActionEvent e){
 			int i=0;
 			int[] selectedIndexes;
 			if("add".equals(e.getActionCommand())){
 				if(this.contentsToAdd.getText().length()>0){
 					this.listModel.addElement(this.contentsToAdd.getText());
 				}
 			}else if("remove".equals(e.getActionCommand())){
 				selectedIndexes = this.addingList.getSelectedIndices();
 				for(i=selectedIndexes.length-1;i>=0;i--){
 					this.listModel.remove(selectedIndexes[i]);
 				}
 			}
 
 		}
 
 
 	}
 
 
 
 }
