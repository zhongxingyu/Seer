 package me.aaronwilson.calculator;
 
 import java.awt.Font;
 
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 /**
  * Represents the graphical user interface class to take care of the input and
  * display.
  * 
  * @author Aaron Wilson
  */
 public class GUI extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	public GUI() {
 		setTitle("Calculator");
 		setSize(500, 300);
 		setLocationRelativeTo(null); // Center the GUI
		setResizable(false);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 
 		JPanel panel = new JPanel();
 		panel.setLayout(new GroupLayout(panel));
 		add(panel);
 
 		JTextField output = new JTextField("0");
		output.setBounds(5, 5, 485, 40);
 		output.setFont(new Font(Font.DIALOG_INPUT, Font.PLAIN, 20));
 		output.setHorizontalAlignment(JTextField.RIGHT);
 		output.setCaretPosition(output.getText().length());
 		panel.add(output);
 
 		JButton clearButton = new JButton("AC");
 		clearButton.setBounds(5, 50, 100, 40);
 		panel.add(clearButton);
 	}
 
 	/**
 	 * Open the GUI. This will make the GUI become visible to the user.
 	 */
 	public void open() {
 		setVisible(true);
 	}
 
 }
