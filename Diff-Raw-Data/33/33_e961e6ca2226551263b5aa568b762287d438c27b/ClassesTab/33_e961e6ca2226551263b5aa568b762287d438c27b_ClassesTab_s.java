 package com.xmleditor.gui;
 
 import java.awt.BorderLayout;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JPanel;
 
 import skillsplanner.beans.DFOClass;
 import skillsplanner.resources.ClassManager;
 import skillsplanner.utils.ListUtils;
 
 import com.xmleditor.gui.listeners.Agni;
 public class ClassesTab extends JPanel {
 
 	public ClassesTab(){
 		super();
 		initGUI();
 	}
 	
 	private void initGUI(){
 		this.setLayout(new BorderLayout());
 		
 		//CENTER
 		List<DFOClass> list = ListUtils.getListFromMap(ClassManager.getInstance().getAllClasses());
 		DFOClass[] handler = new DFOClass[list.size()];
 		JList<DFOClass> listpane = new JList<DFOClass>(list.toArray(handler));
 		
 		this.add(listpane,BorderLayout.CENTER);
 				
 		// BOTTOM PANEL
 		JPanel buttonPanel = new JPanel();
 		JButton newButton = new JButton("New Class");
 		Agni agni = new Agni(listpane);
 		newButton.addActionListener(agni);
 		newButton.setActionCommand(Agni.NEW_CLASS);
 		JButton editButton = new JButton("Edit Class");
 		editButton.setActionCommand(Agni.EDIT_CLASS);
 		editButton.addActionListener(agni);
 		
 		buttonPanel.add(newButton);
 		buttonPanel.add(editButton);
 		
 		this.add(buttonPanel,BorderLayout.SOUTH);
 	}
 }
