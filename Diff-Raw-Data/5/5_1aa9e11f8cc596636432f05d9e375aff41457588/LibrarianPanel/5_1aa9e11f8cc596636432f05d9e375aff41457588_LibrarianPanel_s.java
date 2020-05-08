 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Connection;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 import javax.swing.table.DefaultTableModel;
 import main.Librarian;
 
 public class LibrarianPanel {
 
 	private JTextField callNumberField;
 	private JTextField isbnField;
 	private JTextField titleField;
 	private JTextField mainAuthorField;
 	private JTextField publisherField;
 	private JTextField yearField;
 	private JPanel mainPanel;
 	
 	private Librarian lib;
 	
 	public LibrarianPanel(Connection con) {
 		lib = new Librarian(con);
 	}
 	
 	public JComponent getLibrarianPanel() {
 
 		mainPanel = new JPanel();
 		mainPanel.setLayout(new GridLayout(0, 2, 10, 10));
 		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10) );
 
 		JButton addBookButton = new JButton("Add Book");
		JButton viewOutButton = new JButton("View Checked Out Items");
 		JButton viewPopularButton = new JButton("View Popular Items");
 
 		addBookButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				openAddBookForm();
 			}
 		});
 
 		viewOutButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				openCheckedOutReport();
 			}
 		});  
 
 		viewPopularButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				openPopularReport();
 			}
 		});  
 
 		mainPanel.add(addBookButton);
 		mainPanel.add(viewOutButton);
 		mainPanel.add(viewPopularButton);
 
 		return mainPanel;
 	}
 	
 	private void openAddBookForm(){
 		// new panel for inputting book info
 		JPanel addBookForm = new JPanel();
 		addBookForm.setLayout(new GridLayout(0, 2, 10, 10));
 		addBookForm.setBorder(new EmptyBorder(10, 10, 10, 10) );
 		
 		JLabel callNumberLabel = new JLabel("Call Number: ");
 		JLabel isbnLabel = new JLabel("ISBN: ");
 		JLabel titleLabel = new JLabel("Title: ");
 		JLabel mainAuthorLabel = new JLabel("Main Author: ");
 		JLabel publisherLabel = new JLabel("Publisher: ");
 		JLabel yearLabel = new JLabel("Year: ");
 
 		// Fields
 		callNumberField = new JTextField();
 		isbnField = new JTextField();
 		titleField = new JTextField();
 		mainAuthorField = new JTextField();
 		publisherField = new JTextField();
 		yearField = new JTextField();
 
 		// Buttons
 		JButton addButton = new JButton("Add");
 		JButton cancelButton = new JButton("Cancel");
 
 		// Add components to panel
 		addBookForm.add(callNumberLabel);
 		addBookForm.add(callNumberField);
 		addBookForm.add(isbnLabel);
 		addBookForm.add(isbnField);
 		addBookForm.add(titleLabel);
 		addBookForm.add(titleField);
 		addBookForm.add(mainAuthorLabel);
 		addBookForm.add(mainAuthorField);
 		addBookForm.add(publisherLabel);
 		addBookForm.add(publisherField);
 		addBookForm.add(yearLabel);
 		addBookForm.add(yearField);
 		addBookForm.add(addButton);
 		addBookForm.add(cancelButton);
 
 		// Window
 		final JFrame frame = new JFrame("Add Book");
 		// Window Properties
 		frame.pack();
 		frame.setVisible(true);
 		frame.setResizable(false);
 		frame.setSize(300, 300);
 
 		//Add content to the window.
 		frame.add(addBookForm, BorderLayout.CENTER);
 
 		// center the frame
 		Dimension d = frame.getToolkit().getScreenSize();
 		Rectangle r = frame.getBounds();
 		frame.setLocation( (d.width - r.width)/2, (d.height - r.height)/2 );
 
 		// Button Listeners
 		addButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				try {
 					String callNumber = callNumberField.getText();
 					int isbn = Integer.parseInt(isbnField.getText());
 					String title = titleField.getText();
 					String mainAuthor = mainAuthorField.getText();
 					String publisher = publisherField.getText();
 					int year = Integer.parseInt(yearField.getText());;
 					
 					if(callNumber.compareTo("") == 0 || title.compareTo("") == 0
 							|| mainAuthor.compareTo("") == 0 || publisher.compareTo("") == 0)
 					{
 						JOptionPane.showMessageDialog(null,
 								"Message: missing information",
 								"Error",
 								JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 					
 					lib.addBook(callNumber, isbn, title, mainAuthor, publisher, year);
 				}
 				catch(NumberFormatException ex) {
 					JOptionPane.showMessageDialog(null,
 							"Message: isbn and year should be numbers",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);	
 					return;
 				}
 				frame.setVisible(false);
 			}
 		});
 
 		cancelButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				frame.setVisible(false);
 			}
 		});
 	}
 	
 	private void openCheckedOutReport(){
 		JPanel viewOutForm = new JPanel();
 		final String[] columnNames = {"Call Number", "Copy #", "Out Date", "Due Date", ""};
 		String[][] data = {};
 		
 		final DefaultTableModel outModel = new DefaultTableModel(data, columnNames);
 		JTable viewOutTable = new JTable(outModel);
 		
 		JScrollPane scrollPane = new JScrollPane(viewOutTable);
 		
 		JLabel subjectsLabel = new JLabel("Subjects: ");
 		final JTextField subjectsField = new JTextField(20);
 		JButton searchButton = new JButton("Search");
 		JPanel subjectsPanel = new JPanel();
 		
 		subjectsPanel.add(subjectsLabel, BorderLayout.LINE_START);
 		subjectsPanel.add(subjectsField, BorderLayout.CENTER);
 		subjectsPanel.add(searchButton, BorderLayout.LINE_END);	
 
 		scrollPane.setPreferredSize(new Dimension(480, 200));
 		viewOutForm.add(scrollPane, BorderLayout.PAGE_START);
 		viewOutForm.add(subjectsPanel, BorderLayout.CENTER);
 
 		final JFrame frame = new JFrame("View Out Items");
 		frame.pack();
 		frame.setVisible(true);
 		frame.setResizable(false);
 		frame.setSize(500, 310);
 		frame.add(viewOutForm, BorderLayout.CENTER);
 
 		Dimension d = frame.getToolkit().getScreenSize();
 		Rectangle r = frame.getBounds();
 		frame.setLocation( (d.width - r.width)/2, (d.height - r.height)/2 );
 
 		searchButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e)
 			{	
 				outModel.setRowCount(0);
 				String subject = subjectsField.getText();
 				ArrayList<String[]> report = lib.getCheckedOutReport(subject);
 				for(int i = 0; i < report.size(); i++)
 					outModel.addRow(report.get(i));
 			}
 		});
 	}
 	
 	private void openPopularReport() {
 		JPanel viewPopForm = new JPanel();
 		final String[] columnNames = {"Call Number", "ISBN", "Title", "Main Author", "Publisher", "Year"};
 		String[][] data = {};
 
 		final DefaultTableModel outModel = new DefaultTableModel(data, columnNames);
 		JTable viewOutTable = new JTable(outModel);
 		
 		JScrollPane scrollPane = new JScrollPane(viewOutTable);
 		
 		JLabel topLabel = new JLabel("Top: ");
 		final JTextField topField = new JTextField(10);
 		JLabel yearLabel = new JLabel("of year: ");
 		final JTextField yearField = new JTextField(10);
 		JPanel topPanel = new JPanel();
 		
 		topPanel.add(topLabel);
 		topPanel.add(topField);
 		topPanel.add(yearLabel);
 		topPanel.add(yearField);
 		
 		JButton searchButton = new JButton("Search");
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.add(searchButton, BorderLayout.LINE_START);		
 
 		scrollPane.setPreferredSize(new Dimension(480, 200));
 		viewPopForm.add(scrollPane, BorderLayout.PAGE_START);
 		viewPopForm.add(topPanel, BorderLayout.CENTER);
 		viewPopForm.add(buttonPanel, BorderLayout.PAGE_END);
 
		final JFrame frame = new JFrame("View Popular Items");
 		frame.pack();
 		frame.setVisible(true);
 		frame.setResizable(false);
 		frame.setSize(500, 275);
 		frame.add(viewPopForm, BorderLayout.CENTER);
 		Dimension d = frame.getToolkit().getScreenSize();
 		Rectangle r = frame.getBounds();
 		frame.setLocation( (d.width - r.width)/2, (d.height - r.height)/2 );
 
 		searchButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {			
 				try {
 					int top = Integer.parseInt(topField.getText());			
 					int year = Integer.parseInt(yearField.getText());
 					outModel.setRowCount(0);
 					ArrayList<String[]> report = lib.getPopularReport(top, year);
 					for(int i = 0; i < report.size(); i++)
 						outModel.addRow(report.get(i));
 				}
 				catch(NumberFormatException numExcept) {
 					JOptionPane.showMessageDialog(null,
 							"Inputs should be numbers",
 							"Error",
 							JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 	}
 }
