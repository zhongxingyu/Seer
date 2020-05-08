 package com.github.ibot02.quaderGui;
 
 import javax.swing.JList;
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 public class SelectionListener implements ListSelectionListener, ChangeListener {
 
 	private JTabbedPane tabs;
 	private int selectedTab;
 	private JList selectedList;
 	private int selectedListIndex;
 
 	public SelectionListener(JTabbedPane tabs){
 		this.tabs=tabs;
 	}
 	
 	@Override
 	public void stateChanged(ChangeEvent e) {
 		selectedTab = tabs.getSelectedIndex();
 	}
 
 	@Override
 	public void valueChanged(ListSelectionEvent e) {
		selectedList = (JList) tabs.getComponentAt(selectedTab);
 		selectedListIndex = selectedList.getSelectedIndex();
 	}
 
 	public int getSelectedTab() {
 		return selectedTab;
 	}
 
 	public int getSelectedListIndex() {
 		return selectedListIndex;
 	}
 
 }
