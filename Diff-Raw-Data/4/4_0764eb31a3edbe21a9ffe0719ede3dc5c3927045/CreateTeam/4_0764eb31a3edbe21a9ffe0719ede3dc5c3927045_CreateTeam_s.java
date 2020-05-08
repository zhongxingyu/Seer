 package com.project5.gui;
 
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import java.awt.BorderLayout;
 import javax.swing.JButton;
 import java.awt.FlowLayout;
 import javax.swing.JLabel;
 import javax.swing.ImageIcon;
 import java.awt.Font;
 import javax.swing.JTextField;
 import java.awt.GridLayout;
 import javax.swing.JRadioButton;
 import javax.swing.ButtonGroup;
 import javax.swing.border.TitledBorder;
 
 import com.project5.dataElements.CurlerPosition;
 import com.project5.dataManager.CurlerTeam;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 public class CreateTeam {
 
 	private JFrame window;
 	private static JTextField txtCountry;
 	private JTextField txtFirstName;
 	private JTextField txtLastName;
 	private final ButtonGroup buttonGroup = new ButtonGroup();
 	private CurlerPosition currentPosition;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					CreateTeam window = new CreateTeam();
 					window.window.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public CreateTeam() {
 		initialize();
 		CurlerTeam.createTeam(txtCountry.getText());
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		window = new JFrame("Create Curlers teams");
 		BorderLayout borderLayout = (BorderLayout) window.getContentPane().getLayout();
 		borderLayout.setVgap(5);
 		window.setBounds(100, 100, 450, 600);
 		window.setResizable(false);
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		JPanel bottomPanel = new JPanel();
 		FlowLayout flowLayout = (FlowLayout) bottomPanel.getLayout();
 		flowLayout.setVgap(10);
 		window.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
 		
 		//adding buttons to the bottom panel
 		JButton btnAddPlayer = new JButton("Add Player");
 		btnAddPlayer.setActionCommand("addPlayer");
 		bottomPanel.add(btnAddPlayer);
 		
 		JButton btnPrintTeam = new JButton("Print Team");
 		btnPrintTeam.setActionCommand("printTeam");
 		bottomPanel.add(btnPrintTeam);
 		
 		JButton btnNewTeam = new JButton("New Team");
 		btnNewTeam.setActionCommand("newTeam");
 		bottomPanel.add(btnNewTeam);
 		
 		JButton btnExit = new JButton("Exit");
 		btnExit.setActionCommand("exit");
 		bottomPanel.add(btnExit);
 		
 		//top panel
 		JPanel topPanel = new JPanel();
 		FlowLayout flowLayout_2 = (FlowLayout) topPanel.getLayout();
 		flowLayout_2.setHgap(25);
 		flowLayout_2.setVgap(10);
 		window.getContentPane().add(topPanel, BorderLayout.NORTH);
 		
 		JLabel lblNewLabel = new JLabel("");
 		lblNewLabel.setIcon(new ImageIcon("D:/workspace/juno/icodereal/Assignment5Olympics/images/sochi2014small.JPG"));
 		topPanel.add(lblNewLabel);
 		
 		JLabel lblTitle = new JLabel("Curler Teams");
 		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 16));
 		topPanel.add(lblTitle);
 		
 		//Center Panel
 		JPanel centerPanel = new JPanel();
 		window.getContentPane().add(centerPanel, BorderLayout.CENTER);
 		centerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 80));
 		
 		JPanel panTextFields = new JPanel();
 		centerPanel.add(panTextFields);
 		panTextFields.setLayout(new GridLayout(0, 2, 0, 20));
 		
 		JLabel lblCountry = new JLabel("Country");
 		lblCountry.setFont(new Font("Tahoma", Font.BOLD, 12));
 		panTextFields.add(lblCountry);
 		
 		txtCountry = new JTextField(JOptionPane.showInputDialog(txtCountry,"Please enter \nTeam Country name","Teacher's Name", JOptionPane.PLAIN_MESSAGE));
 		panTextFields.add(txtCountry);
 		txtCountry.setColumns(15);
 		txtCountry.setEditable(false);
 		
 		JLabel lblFirstName = new JLabel("First Name");
 		lblFirstName.setFont(new Font("Tahoma", Font.BOLD, 12));
 		panTextFields.add(lblFirstName);
 		
 		txtFirstName = new JTextField();
 		panTextFields.add(txtFirstName);
 		txtFirstName.setColumns(15);
 		
 		JLabel lblLastName = new JLabel("Last Name");
 		lblLastName.setFont(new Font("Tahoma", Font.BOLD, 12));
 		panTextFields.add(lblLastName);
 		
 		txtLastName = new JTextField();
 		txtLastName.setColumns(15);
 		panTextFields.add(txtLastName);
 		
 		JPanel panRadioBtns = new JPanel();
 		FlowLayout flowLayout_1 = (FlowLayout) panRadioBtns.getLayout();
 		flowLayout_1.setHgap(25);
 		centerPanel.add(panRadioBtns);
 		
 		//creating Position Btns
 		JPanel groupPosition = new JPanel();
 		groupPosition.setBorder(new TitledBorder(null, "Position", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panRadioBtns.add(groupPosition);
 		
 		JRadioButton rdbtnThrower = new JRadioButton("Thrower");
 		rdbtnThrower.setActionCommand("thrower");
 		groupPosition.add(rdbtnThrower);
 		buttonGroup.add(rdbtnThrower);
 		
 		JRadioButton rdbtnSweeper = new JRadioButton("Sweeper");
 		rdbtnSweeper.setActionCommand("sweeper");
 		groupPosition.add(rdbtnSweeper);
 		buttonGroup.add(rdbtnSweeper);
 		
 		JRadioButton rdbtnSkip = new JRadioButton("Skip");
 		rdbtnSkip.setActionCommand("skip");
 		groupPosition.add(rdbtnSkip);
 		buttonGroup.add(rdbtnSkip);
 		
 		ButtonListener buttonAction = new ButtonListener();
 		btnAddPlayer.addActionListener(buttonAction);
 		btnNewTeam.addActionListener(buttonAction);
 		btnPrintTeam.addActionListener(buttonAction);
 		btnExit.addActionListener(buttonAction);
 		
 		RadioButtonListener radioBtnAction = new RadioButtonListener();
 		rdbtnThrower.addActionListener(radioBtnAction);
 		rdbtnSweeper.addActionListener(radioBtnAction);
 		rdbtnSkip.addActionListener(radioBtnAction);
 		
 	}
 	
 	private class ButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			switch (e.getActionCommand()) {
 			case "addPlayer":
 				String message = CurlerTeam.addPlayer(txtFirstName.getText(), txtLastName.getText(), currentPosition);
 				if (message != null) 
					JOptionPane.showInternalMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
				
 				//System.out.println(CurlerTeam.getNumPlayers());
 				break;
 			case "newTeam":
 				txtCountry.setText(JOptionPane.showInputDialog(txtCountry,"Please enter \nTeam Country name", "Country's name", JOptionPane.PLAIN_MESSAGE));
 				CurlerTeam.createTeam(txtCountry.getText());
 				break;
 			case "printTeam":
 				JOptionPane.showMessageDialog(null, CurlerTeam.printTeam() , CurlerTeam.getTeamName(), JOptionPane.PLAIN_MESSAGE);
 				
 				/*CurlerTeam print = new CurlerTeam(txtCountry.getText());
 				print.printTeam();*/
 				break;
 			case "exit":
 				window.dispose();
 				break;
 			}
 		}
 		
 	}
 	
 	private class RadioButtonListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			switch(e.getActionCommand()) {
 			case "thrower": 
 				currentPosition = CurlerPosition.THROWER;
 				break;
 			case "sweeper":
 				currentPosition = CurlerPosition.SWEEPER;
 				break;
 			case "skip":
 				currentPosition = CurlerPosition.SKIP;
 				break;
 			}
 		}
 		
 	}
 	
 }
