 /*  Copyright (C) 2011  Nicholas Wright
 	
 	part of 'Aid', an imageboard downloader.
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package gui;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import filter.FilterModifiable;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * Displays lists of filtered keywords, and lets the user edit them.
  */
 public class Filterlist extends JFrame implements ActionListener, ListSelectionListener {
 	private static final long serialVersionUID = 1L;
 	private JPanel jContentPane = null;
 	private JList<String> lstFileName = null;
 	private JList<String> lstPostContent = null;
 	private JScrollPane scrlFileName = null;
 	private JScrollPane scrlPostContent = null;
 	private DefaultListModel<String> fileNameModel = null;
 	private DefaultListModel<String> postContentModel = null;
 	private JTextField editBox = null;
 	private JButton btnAddFile = null;
 	private JButton btnRemove = null;
 	private JLabel lblPostCont = null;
 	private JLabel lblFileName = null;
 	private JButton btnAddPost = null;
 	private FilterModifiable filter;
 	
 	private final int ADD_BTN_WIDTH = 55;
 
 	/**
 	 * Creates a new Frame that can be used to manage
 	 * the filters.
 	 * @param filter
 	 */
 	public Filterlist(FilterModifiable filter, DefaultListModel<String> fileNameModel, DefaultListModel<String> postContentModel) {
 		super();
 		this.fileNameModel = fileNameModel;
 		this.postContentModel = postContentModel;
 		this.filter = filter;
 		initialize();
 	}
 
 	/**
 	 * This method initializes this
 	 * 
 	 * @return void
 	 */
 	private void initialize() {
 		this.setSize(400, 300);
 		this.setLocation(600, 100);
 		this.setContentPane(getJContentPane());
 		this.setTitle("FilterList");
 	}
 
 	/**
 	 * This method initializes jContentPane
 	 * 
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getJContentPane() {
 		if (jContentPane == null) {
 			lblFileName = new JLabel();
 			lblFileName.setBounds(15, 6, 151, 20);
 			lblFileName.setText("Filename");
 			lblPostCont = new JLabel();
 			lblPostCont.setBounds(188, 6, 151, 20);
 			lblPostCont.setText("Post content");
 			jContentPane = new JPanel();
 			jContentPane.setLayout(null);
 			jContentPane.add(getLstFileName(), null);
 			jContentPane.add(getLstPostContent(), null);
 			jContentPane.add(getEditBox(), null);
 			jContentPane.add(getBtnAddFile(), null);
 			jContentPane.add(getBtnAddPost(), null);
 			jContentPane.add(getBtnRemove(), null);
 			jContentPane.add(lblPostCont, null);
 			jContentPane.add(lblFileName, null);
 			
 		}
 		return jContentPane;
 	}
 
 	/**
 	 * This method initializes lstFileName	
 	 * 	
 	 * @return java.awt.List	
 	 */
 	private JScrollPane getLstFileName() {
 		if (lstFileName == null) {
 			lstFileName = new JList<>(fileNameModel);
 			lstFileName.setBounds(15, 30, 151, 182);
 			lstFileName.addListSelectionListener(this);
 			scrlFileName = new JScrollPane(lstFileName);
 			scrlFileName.setBounds(15, 30, 151, 182);
 		}
 		return scrlFileName;
 	}
 
 	/**
 	 * This method initializes lstPostContent	
 	 * 	
 	 * @return java.awt.List	
 	 */
 	private JScrollPane getLstPostContent() {
 		if (lstPostContent == null) {
 			lstPostContent = new JList<>(postContentModel);
 			lstPostContent.setBounds(188, 30, 151, 182);
 			lstPostContent.addListSelectionListener(this);
 			scrlPostContent = new JScrollPane(lstPostContent);
 			scrlPostContent.setBounds(188, 30, 151, 182);
 		}
 		return scrlPostContent;
 	}
 
 	/**
 	 * This method initializes editBox	
 	 * 	
 	 * @return java.awt.TextField	
 	 */
 	private JTextField getEditBox() {
 		if (editBox == null) {
 			editBox = new JTextField();
 			editBox.setBounds(16, 222, 151, 28);
 		}
 		return editBox;
 	}
 
 	/**
 	 * This method initializes btnAddFile	
 	 * 	
 	 * @return java.awt.Button	
 	 */
 	private JButton getBtnAddFile() {
 		if (btnAddFile == null) {
 			btnAddFile = new JButton();
 			btnAddFile.setBounds(188, 222, ADD_BTN_WIDTH, 28);
 			btnAddFile.setText("Add");
 			btnAddFile.addActionListener(this);
 		}
 		return btnAddFile;
 	}
 
 	/**
 	 * This method initializes btnRemove	
 	 * 	
 	 * @return java.awt.Button	
 	 */
 	private JButton getBtnRemove() {
 		if (btnRemove == null) {
 			btnRemove = new JButton();
 			btnRemove.setBounds(btnAddPost.getX()+btnAddPost.getWidth()+2, 222, 75, 28);
 			btnRemove.setText("Remove");
 			btnRemove.addActionListener(this);
 		}
 		return btnRemove;
 	}
 
 	/**
 	 * This method initializes btnAddPost	
 	 * 	
 	 * @return java.awt.Button	
 	 */
 	private JButton getBtnAddPost() {
 		if (btnAddPost == null) {
 			btnAddPost = new JButton();
 			btnAddPost.setBounds(btnAddFile.getX()+btnAddFile.getWidth()+2, 222, ADD_BTN_WIDTH, 28);
 			btnAddPost.setText("Add");
 			btnAddPost.addActionListener(this);
 		}
 		return btnAddPost;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == btnAddFile)
 			filter.addFileNameFilterItem(editBox.getText());
 		
 		if(e.getSource() == btnAddPost)
 			filter.addPostContentFilterItem(editBox.getText());
 		
 		if(e.getSource() == btnRemove){
 			filter.removeFileNameFilterItem(editBox.getText());
 			filter.removePostContentFilterItem(editBox.getText());
 		}
 	}
 
 	@Override
 	public void valueChanged(ListSelectionEvent e) {
		if(e.getSource() == lstFileName || lstFileName.getSelectedIndex() != -1)
 			editBox.setText(lstFileName.getModel().getElementAt(lstFileName.getSelectedIndex()));
 		
		if(e.getSource() == lstPostContent || lstPostContent.getSelectedIndex() != -1)
 			editBox.setText(lstPostContent.getModel().getElementAt(lstPostContent.getSelectedIndex()));
 	}
 }
