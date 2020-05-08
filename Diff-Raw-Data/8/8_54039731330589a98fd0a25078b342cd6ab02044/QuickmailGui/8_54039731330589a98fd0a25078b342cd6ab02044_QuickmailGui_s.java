 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.border.*;
 
 public class QuickmailGui extends JFrame {
 	private JButton fileButton;
 	private JButton editButton;
 	private JButton helpButton;
 	private JButton settingsButton;
 	private JButton newmailButton;
 	private JButton readButton;
 	private JButton replyButton;
 	private JButton forewardButton;
 	private JButton replyallButton;
 	Border blackline;
 
 
 	private JList<String> mailTree;
 	private JList<String> mailList;
 	public boolean debug = false;
 
	public quickmailgui() {
 		super("QuickMailer");
 		setContentPane(createContentPane());
 		addListeners();
 	}
 
 	private DefaultListModel<String> getModel(JList<String> list){
 		return ((DefaultListModel<String>) list.getModel());
 	}
 
 	private void addListeners() {
 		fileButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Test");
 			}
 		});
 
 		editButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Test");
 			}
 		});
 
 		helpButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Test");
 			}
 		});
 
 		settingsButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("Test");
 			}
 		});
 
 
 
 
 
 
 	}
 
 	private JPanel createContentPane() {
 		JPanel content = new JPanel(new BorderLayout(5,5));
 		JPanel content1 = new JPanel(new BorderLayout());
 		JPanel content2 = new JPanel(new BorderLayout());
		mailTree = new JList<>(new DefaultListModel<String>());
 		JScrollPane leftScroll = new JScrollPane(mailTree);
 		leftScroll.setPreferredSize(new Dimension(200, 200)); 
 		content1.add(leftScroll, BorderLayout.WEST);
 
 
 		JPanel contextPanel = new JPanel(new FlowLayout(0));
 		fileButton = new JButton("File");
 		contextPanel.add(fileButton);
 		editButton = new JButton("Edit");
 		contextPanel.add(editButton);
 		settingsButton = new JButton("Settings");
 		contextPanel.add(settingsButton);
 		helpButton = new JButton("Help");
 		contextPanel.add(helpButton);
 
 
 
 		JPanel actionPanel = new JPanel(new FlowLayout(0));
 		newmailButton = new JButton("Compose");
 		actionPanel.add(newmailButton);
 		readButton = new JButton("Read");
 		actionPanel.add(readButton);
 		replyButton = new JButton("Reply");
 		actionPanel.add(replyButton);
 		forewardButton = new JButton("Foreward");
 		actionPanel.add(forewardButton);
 		replyallButton = new JButton("Replyall");
 		actionPanel.add(replyallButton);
 
 
 
 
 		TitledBorder black;
 		blackline = BorderFactory.createLineBorder(Color.black);
 		black = BorderFactory.createTitledBorder(
                 blackline);
 
 
 
 
 
 
 
		mailList = new JList<>(new DefaultListModel<String>());
 		JScrollPane rightScroll = new JScrollPane(mailList);
 		rightScroll.setPreferredSize(new Dimension(200, 200)); 
 		content.add(rightScroll, BorderLayout.CENTER);
 		content.add(actionPanel, BorderLayout.NORTH);
 		content.setBorder(black);
 
 		content1.add(contextPanel, BorderLayout.NORTH);
 		content1.add(content, BorderLayout.CENTER);
 		content2.add(content1,BorderLayout.CENTER);
 		return content2;
 	}	
 }
