 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import controller.GUIController;
 
 public class GUI {
	private GUIController guic;
 	private JTextField filename = new JTextField(), dir = new JTextField();
 	private JFrame frame = new JFrame();
 	private JFileChooser fileBrowser = new JFileChooser();
 
 	public GUI() {
 		frame.setLayout(new BorderLayout());
 		frame.getContentPane().setBackground(Color.WHITE);
 		frame.setSize(400, 480);
 		frame.setVisible(true);
 
 		JButton readButton = new JButton("read file");
 		readButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				int rVal = fileBrowser.showOpenDialog(frame);
 				if (rVal == JFileChooser.APPROVE_OPTION) {
 					filename.setText(fileBrowser.getSelectedFile().getName());
 					dir.setText(fileBrowser.getCurrentDirectory().toString());
					System.out.println(dir.getText()+filename.getText());
 					guic.read(dir.getText()+"\\"+filename.getText());
 				}
 				if (rVal == JFileChooser.CANCEL_OPTION) {
 					filename.setText("You pressed cancel");
 					dir.setText("");
 				}
 			}
 		});
 
 		JButton reportButton = new JButton("generate report");
 		reportButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 			}
 		});
 
 		JPanel buttonPannel = new JPanel();
 		buttonPannel.add(readButton);
 		buttonPannel.add(reportButton);
 
 		frame.add(buttonPannel, BorderLayout.NORTH);
 	}
 }
