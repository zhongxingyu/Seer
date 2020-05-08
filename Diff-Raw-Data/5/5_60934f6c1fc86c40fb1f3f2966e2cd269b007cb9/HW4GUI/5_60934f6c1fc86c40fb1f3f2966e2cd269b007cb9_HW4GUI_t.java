 package edu.cmu.mse.rui.J2EE.HW4;
 /*
  * 
  * 08-600 
  * Homework #4
  * Rui Li <ruili@andrew.cmu.edu> 
  * September 28, 2012 
  */
  
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JTextField;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 
 import javax.swing.JLabel;
 import javax.swing.JTextArea;
 import java.awt.Font;
 
 public class HW4GUI {
 
 	private ArrayList<HW4Data> operationHistory = new ArrayList<HW4Data>();
 	// increment by 1 once.
 	private static int currentCheckNo = 101;
 	
 
 	private JFrame frame;
 	private JTextField textField;
 	private JTextField textField_1;
 	private JTextField textField_2;
 	private JButton btnCheck;
 	private JTextField textField_3;
 	private JTextArea textArea;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					HW4GUI window = new HW4GUI();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public HW4GUI() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 643, 446);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(null);
 
 		JPanel panel = new JPanel();
 		panel.setBounds(6, 6, 631, 412);
 		frame.getContentPane().add(panel);
 		panel.setLayout(null);
 
 		textField = new JTextField();
 		textField.setBounds(18, 43, 113, 28);
 		panel.add(textField);
 		textField.setText(Util.DateToString(Calendar.getInstance().getTime()));
 
 		textField.setColumns(10);
 
 		textField_1 = new JTextField();
 		textField_1.setColumns(10);
 		textField_1.setBounds(143, 43, 353, 28);
 		panel.add(textField_1);
 
 		textField_2 = new JTextField();
 		textField_2.setColumns(10);
 		textField_2.setBounds(545, 43, 80, 28);
 		panel.add(textField_2);
 
 		JButton btnNewButton = new JButton("Write Check");
 		btnNewButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				processOperation(true);
 			}
 		});
 		btnNewButton.setBounds(210, 83, 117, 29);
 		panel.add(btnNewButton);
 
 		btnCheck = new JButton("Make Deposit");
 		btnCheck.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				processOperation(false);
 
 			}
 		});
 		btnCheck.setBounds(339, 83, 117, 29);
 		panel.add(btnCheck);
 
 		JLabel lblDate = new JLabel("Date");
 		lblDate.setBounds(24, 19, 36, 16);
 		panel.add(lblDate);
 
 		JLabel lblDescription = new JLabel("Description");
 		lblDescription.setBounds(152, 18, 80, 16);
 		panel.add(lblDescription);
 
 		JLabel lblAmount = new JLabel("Amount");
 		lblAmount.setBounds(546, 15, 58, 16);
 		panel.add(lblAmount);
 
 		JLabel label = new JLabel("$");
 		label.setBounds(530, 49, 15, 16);
 		panel.add(label);
 
 		textField_3 = new JTextField();
 		textField_3.setEditable(false);
 		textField_3.setBounds(76, 121, 443, 21);
 		panel.add(textField_3);
 		textField_3.setColumns(10);
 
 		textArea = new JTextArea();
 		textArea.setEditable(false);
 		textArea.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
 		textArea.setBounds(36, 174, 549, 213);
 		panel.add(textArea);
 
 	}
 
 	//represent data in the text area.
 	private void representDataInTextArea(ArrayList<HW4Data> arr) {
 		textArea.setText("");
 		textArea.append(buildHeader());
 		textArea.append("\n");
 		for (HW4Data row : arr) {
 			textArea.append(formatOneOperation(row));
 		}
 	}
 
 	//to format one operation 
 	private String formatOneOperation(HW4Data data) {
 		StringBuffer answer = new StringBuffer();
 
 		padAndAppend(answer, Util.DateToString(data.getCreateDate()), 20);
 		if (data.isCheck())
 			padAndAppend(answer, data.getChectNo() + "", 15);
 		else
 			padAndAppend(answer, "", 15);
 		padAndAppend(answer, data.getDescription(), 30);
 
 		padAndAppend(answer, data.getAmount() + "", 10);
		padAndAppend(answer,"-"+ data.getFee() , 10);
 		padAndAppend(answer, data.getCurrentBalance() + "", 10);
 
 		answer.append('\n');
 		return answer.toString();
 	}
 
 	private void padAndAppend(StringBuffer b, String s, int width) {
 		
 		b.append(s);
 		for (int i = s.length(); i < width; i++) {
 			b.append(' ');
 		}
 	}
 	
 	//to build the header of the operation history
 	private String buildHeader(){
 		StringBuffer header=new StringBuffer();
 		padAndAppend(header, "date", 20);
 		padAndAppend(header, "Check#", 15);
 		padAndAppend(header, "Description", 30);
 		padAndAppend(header, "Amount", 10);
 		padAndAppend(header, "Fee", 10);
 		padAndAppend(header, "Balance", 10);
 		return header.toString();
 		
 	}
 
 	// check the data
 	// create a data event
 	// put in to the arr, sort the arr
 	// represent the array in the table
 	@SuppressWarnings("unchecked")
 	private void processOperation(boolean flag) {
 		String inputDate = textField.getText().trim();
 		String inputDes = textField_1.getText().trim();
 		String inputAmount = textField_2.getText().trim();
 		boolean isCheck = flag;
 		if (Util.checkoutEmptyDescription(inputDes)) {
 			if (Util.checkAmount(inputAmount, isCheck)) {
 				HW4Data e1 = null;
 				e1 = new HW4Data(Util.dateConvert(inputDate), inputDes,
 						Double.parseDouble(inputAmount), currentCheckNo,
 						isCheck);
 				if (isCheck) {
 					currentCheckNo++;
 				}
 
 				operationHistory.add(e1);
 				Collections.sort(operationHistory);
 				Util.calcBlance(operationHistory);
 				representDataInTextArea(operationHistory);
				textField_2.setText("");
				textField_1.setText("");
 
 			} else {
 				textField_3.setText("something wrong with the amount");
 			}
 
 		} else {
 			textField_3
 					.setText("something wrong with the description, forogt it?");
 
 		}
 
 	}
 
 }
