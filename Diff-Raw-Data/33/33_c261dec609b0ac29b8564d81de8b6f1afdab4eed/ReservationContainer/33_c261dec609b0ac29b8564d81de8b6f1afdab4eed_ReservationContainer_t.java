 package main.view;
 
 import java.awt.*;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 
 import main.controller.ReservationController;
 
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.Timer;
 
 public class ReservationContainer extends JPanel
 	
 {
 	private Date startDate;
 	private Date endDate;
 	private JButton deleteButton;
 	private JButton createReservationButton;
 	private ReservationController controller;
 	private JTextField startPeriod;
 	private JTextField endPeriod;
 	private SimpleDateFormat dateFormat;
 	private ReservationTable data;
 	private JScrollPane scrollPane;
 	private JTable table;
 	
 	/**
 	 * Creates a new container
 	 */
 	public ReservationContainer()
 	{	
 		startDate 						= new Date(System.currentTimeMillis());		//day - hours - seconds ect. 
 		endDate 						= new Date(startDate.getTime() + 10L * 1 * 24 * 60 * 60 * 1000);
 		
 		setLayout(new BorderLayout());
 		
 		JPanel centerPanel				= new JPanel();
 		JPanel northPanel 				= new JPanel();
 		JPanel southPanel 				= new JPanel(new GridLayout());
 		JPanel westPanel 				= new JPanel();
 		JPanel eastPanel 				= new JPanel();
 		
 		add(centerPanel, BorderLayout.CENTER);
 		add(northPanel,	BorderLayout.NORTH);
 		add(southPanel,	BorderLayout.SOUTH);
 		add(westPanel,	BorderLayout.WEST);
 		add(eastPanel,	BorderLayout.EAST);
 		
 		dateFormat		= new SimpleDateFormat("dd/MM/yyyy");
 		
 		startPeriod		= new JTextField("Today", 10); //remake
 		endPeriod		= new JTextField("Tomorrow", 10); //remake
 		
 		createReservationButton					= new JButton("Create Reservation");
 		createReservationButton.setPreferredSize( new Dimension(200, 50));
 		deleteButton							= new JButton("Delete");
 		
 		addTable();
 		table							= new JTable(data);
 		scrollPane						= new JScrollPane(table);
 		scrollPane.setPreferredSize		( new Dimension(1200, 600));
 		
 		centerPanel.add(scrollPane);
 		northPanel.add(startPeriod);
 		northPanel.add(endPeriod);
 		southPanel.add(createReservationButton);
 		southPanel.add(deleteButton);
 		
 		setVisible(true);		
 	}
 
 	public void addTable() {
 		data = new ReservationTable(startDate, endDate);
 	}
 	
 	public ReservationTable getData() {
 		return data;
 	}
 
 	/**
 	 * returns the button south button which should open a new JFrame to create a reservation
 	 * @return
 	 */
 	public JButton getCreateReservationButton() {
 		return createReservationButton;
 	}
 	
 	/**
 	 * returns a button from the reservation view
 	 * @return JButton
 	 */
 	public JButton getDeleteButton() {
 		return deleteButton;
 	}
 	
 	/**
 	 * returns the table which shows the reservations with the given period
 	 * @return JTable
 	 */
 	public JTable getTable() {
 		return table;
 	}
 	
 	public String getStartPeriodTextField() {
 		return startPeriod.getText();
 	}
 	
 	public String getEndPeriodTextField() {
 		return endPeriod.getText();
 	}
 }
