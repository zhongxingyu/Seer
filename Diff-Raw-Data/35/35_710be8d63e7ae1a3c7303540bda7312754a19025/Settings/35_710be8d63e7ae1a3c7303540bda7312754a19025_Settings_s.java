 package views.gui;
 
import java.awt.BorderLayout;
 import java.awt.EventQueue;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ButtonGroup;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.GroupLayout.Alignment;
import com.jgoodies.forms.factories.FormFactory;
 import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.LayoutStyle.ComponentPlacement;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
 import javax.swing.JPasswordField;
import javax.swing.JComboBox;
 import javax.swing.JToggleButton;
 
 public class Settings extends JFrame {
 
 	private JPanel contentPane;
 	private JTextField txtText;
 	private JPasswordField pwdPassword;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Settings frame = new Settings();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public Settings() {
 		setTitle("Settings");
 		setBounds(100, 100, 500, 343);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		
 		JLabel lblSetting = new JLabel("Setting1");
 		
 		JLabel lblSetting_1 = new JLabel("Setting2");
 		
 		JLabel lblSetting_2 = new JLabel("Setting3");
 		
 		JLabel lblSetting_3 = new JLabel("Setting4");
 		
 		JLabel lblSetting_4 = new JLabel("Setting5");
 		
 		JLabel lblSetting_5 = new JLabel("Setting6");
 		
 		JPanel panel = new JPanel();
 		
 		txtText = new JTextField();
 		txtText.setText("text");
 		txtText.setColumns(10);
 		
 		JCheckBox chckbxOption = new JCheckBox("option1");
 		
 		JCheckBox chckbxOption_1 = new JCheckBox("option2");
 		
 		JCheckBox chckbxOption_2 = new JCheckBox("option3");
 		
 		JRadioButton radioButton = new JRadioButton("0");
 		
 		JRadioButton radioButton_1 = new JRadioButton("1");
 		
 		JRadioButton radioButton_2 = new JRadioButton("2");
 		
 		JRadioButton radioButton_3 = new JRadioButton("3");
 		
 		JRadioButton radioButton_4 = new JRadioButton("4");
 		
 		ButtonGroup group = new ButtonGroup();
 		group.add(radioButton);
 		group.add(radioButton_1);
 		group.add(radioButton_2);
 		group.add(radioButton_3);
 		group.add(radioButton_4);
 		
 		pwdPassword = new JPasswordField();
 		pwdPassword.setText("password");
 		
 		String[] options = {"option1", "option2", "option3"};
		JComboBox comboBox = new JComboBox(options);
 		
 		final JToggleButton tglbtnSetting = new JToggleButton("off");
 		tglbtnSetting.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(tglbtnSetting.isSelected())
 					tglbtnSetting.setText("on");
 				else
 					tglbtnSetting.setText("off");
 			}
 		});
 		
 		GroupLayout gl_contentPane = new GroupLayout(contentPane);
 		gl_contentPane.setHorizontalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
 						.addComponent(panel, GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
 						.addGroup(gl_contentPane.createSequentialGroup()
 							.addComponent(lblSetting)
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addComponent(chckbxOption, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addComponent(chckbxOption_1, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
 							.addGap(18)
 							.addComponent(chckbxOption_2))
 						.addGroup(gl_contentPane.createSequentialGroup()
 							.addComponent(lblSetting_1)
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addComponent(txtText, GroupLayout.PREFERRED_SIZE, 108, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_contentPane.createSequentialGroup()
 							.addComponent(lblSetting_3)
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addComponent(pwdPassword, GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE))
 						.addGroup(gl_contentPane.createSequentialGroup()
 							.addComponent(lblSetting_5)
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addComponent(tglbtnSetting))
 						.addGroup(gl_contentPane.createSequentialGroup()
 							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
 								.addGroup(gl_contentPane.createSequentialGroup()
 									.addComponent(lblSetting_4)
 									.addPreferredGap(ComponentPlacement.UNRELATED)
 									.addComponent(comboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 								.addGroup(gl_contentPane.createSequentialGroup()
 									.addComponent(lblSetting_2)
 									.addPreferredGap(ComponentPlacement.UNRELATED)
 									.addComponent(radioButton)
 									.addGap(18)
 									.addComponent(radioButton_1)))
 							.addGap(18)
 							.addComponent(radioButton_2)
 							.addGap(18)
 							.addComponent(radioButton_3)
 							.addGap(18)
 							.addComponent(radioButton_4)))
 					.addContainerGap())
 		);
 		gl_contentPane.setVerticalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblSetting)
 						.addComponent(chckbxOption)
 						.addComponent(chckbxOption_1)
 						.addComponent(chckbxOption_2))
 					.addGap(18)
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
 						.addComponent(lblSetting_1)
 						.addComponent(txtText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addGap(14)
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblSetting_2)
 						.addComponent(radioButton)
 						.addComponent(radioButton_1)
 						.addComponent(radioButton_2)
 						.addComponent(radioButton_3)
 						.addComponent(radioButton_4))
 					.addGap(18)
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblSetting_3)
 						.addComponent(pwdPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addGap(18)
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblSetting_4)
 						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addGap(18)
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblSetting_5)
 						.addComponent(tglbtnSetting))
 					.addPreferredGap(ComponentPlacement.RELATED, 71, Short.MAX_VALUE)
 					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE))
 		);
 		
 		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		
 		JButton btnOK = new JButton("OK");
 		btnOK.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("saving...");
 				dispose();
 			}
 		});
 		panel.add(btnOK);
 		
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				dispose();
 			}
 		});
 		panel.add(btnCancel);
 		
 		contentPane.setLayout(gl_contentPane);
 		setVisible(true);
 	}
 }
