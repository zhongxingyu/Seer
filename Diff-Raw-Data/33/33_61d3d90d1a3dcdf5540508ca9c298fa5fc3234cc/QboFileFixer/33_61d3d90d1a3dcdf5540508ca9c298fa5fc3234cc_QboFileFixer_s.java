 package stackBuilders;
 
 import stackBuilders.QboFileFilter;
 import stackBuilders.QboFile;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
public class QboFileFixer {
 
	public static void main(String args[]) {
		JFrame frame = new JFrame("QuickBooks HSBC FileFixer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = frame.getContentPane();
 
 		final JLabel headerLabel = new JLabel("Choose QBO File to Fix");
 		headerLabel.setFont(new Font("Serif", Font.BOLD, 36));
 		contentPane.add(headerLabel, BorderLayout.NORTH);
 
 		JFileChooser fileChooser = new JFileChooser(".");
 		fileChooser.setControlButtonsAreShown(true);
 		fileChooser.setFileFilter(new QboFileFilter());
 
 		contentPane.add(fileChooser, BorderLayout.CENTER);
 
 		ActionListener actionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent actionEvent) {
 				JFileChooser theFileChooser = (JFileChooser) actionEvent.getSource();
 
 				String command = actionEvent.getActionCommand();
 
 				if (command.equals(JFileChooser.APPROVE_SELECTION)) {
 					QboFile qboFile = new QboFile(theFileChooser.getSelectedFile());
 					qboFile.fix();
 					theFileChooser.rescanCurrentDirectory();
 				} else if (command.equals(JFileChooser.CANCEL_SELECTION)) {
 					System.exit(0);
 				}
 			}
 		};
 		fileChooser.addActionListener(actionListener);
 
		frame.pack();
		frame.setVisible(true);
 	}	
 }
