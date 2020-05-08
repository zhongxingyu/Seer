 package gui;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingConstants;
 
 import org.newdawn.slick.SlickException;
 
 import clock.MP3;
 
 public class window {
 
 	private JFrame frmAlarm;
 	private static File song;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					window window = new window();
 					window.frmAlarm.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public window() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frmAlarm = new JFrame();
 		frmAlarm.getContentPane().setForeground(Color.BLACK);
 		frmAlarm.setForeground(Color.WHITE);
 		frmAlarm.setTitle("Alarm");
 		frmAlarm.setBounds(100, 100, 500, 250);
 		frmAlarm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmAlarm.getContentPane().setLayout(null);
 
 		JLabel lblWhatTimeWould = new JLabel("What time would you like your alarm?");
 		lblWhatTimeWould.setToolTipText("");
 		lblWhatTimeWould.setHorizontalAlignment(SwingConstants.CENTER);
 		lblWhatTimeWould.setBounds(46, 23, 300, 14);
 		frmAlarm.getContentPane().add(lblWhatTimeWould);
 
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		btnCancel.setBounds(240, 177, 89, 23);
 		frmAlarm.getContentPane().add(btnCancel);
 
 		JLabel lblWhatColorWould = new JLabel("What color would you like your clock?");
 		lblWhatColorWould.setBounds(46, 100, 300, 14);
 		frmAlarm.getContentPane().add(lblWhatColorWould);
 
 		JComboBox comboBox_1 = new JComboBox();
		comboBox_1.setForeground(Color.BLACK);
 		comboBox_1.setModel(new DefaultComboBoxModel(new String[] {"Orange", "Green", "Blue", "Red", "White", "Pink"}));
 		comboBox_1.setMaximumRowCount(6);
 		comboBox_1.setBounds(350, 99, 89, 23);
 		frmAlarm.getContentPane().add(comboBox_1);
 
 		JLabel lblClicknextTo = new JLabel("Click 'Next' to select your music");
 		lblClicknextTo.setBounds(131, 150, 300, 14);
 		frmAlarm.getContentPane().add(lblClicknextTo);
 
 		JButton btnNext = new JButton("Next");
 		btnNext.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					openDialog();
 				} catch (SlickException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		});
 		btnNext.setBounds(140, 177, 89, 23);
 		frmAlarm.getContentPane().add(btnNext);
 
 		final JSpinner spinner = new JSpinner();
 		spinner.setModel(new SpinnerNumberModel(0, 0, 23, 1));
 		spinner.setBounds(350, 23, 37, 20);
 		frmAlarm.getContentPane().add(spinner);
 
 		final JSpinner spinner_1 = new JSpinner();
 		spinner_1.setModel(new SpinnerNumberModel(0, 0, 59, 1));
 		spinner_1.setBounds(400, 23, 37, 20);
 		frmAlarm.getContentPane().add(spinner_1);
 
 		JLabel lblmilitaryTimePlease = new JLabel("(Military time please)");
 		lblmilitaryTimePlease.setBounds(120, 35, 175, 16);
 		frmAlarm.getContentPane().add(lblmilitaryTimePlease);
 
 		JButton btnNewButton = new JButton("Set");
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String spinNum = spinner.getValue().toString();
 				String spinNum1 = spinner_1.getValue().toString();
 				if(spinNum1.length() == 1)
 				{
 					spinNum1 = "0" + spinNum1;
 				}
 				String alarmTime = spinNum + ":" + spinNum1 + ":00";
 				System.out.println(alarmTime);
 				clock.Clock.setAlarm(alarmTime);
 			}
 		});
 		btnNewButton.setBounds(357, 55, 72, 23);
 		frmAlarm.getContentPane().add(btnNewButton);
 	}
 	private void openDialog() throws SlickException{
 		JFileChooser open = new JFileChooser();
 		int openReturn = open.showOpenDialog(null);
 		if (openReturn == JFileChooser.APPROVE_OPTION) {
 			setSong(open.getSelectedFile());
 			clock.Main.main(null);
 			
 		}
 		if (openReturn != JFileChooser.APPROVE_OPTION) {
 			System.exit(0);
 		}
 	}
 	public static File getSong(){
 		return song;
 	}
 	private static void setSong(File sng){
 		song = sng;
 	}
 }
