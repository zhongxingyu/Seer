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
import javax.swing.SwingUtilities;
 import javax.swing.JLabel;
import java.lang.reflect.InvocationTargetException;
 
public class QboFileFixer implements Runnable {
    static JFrame mainFrame;
    static JLabel label;
 
    public void run() {
		mainFrame = new JFrame("QuickBooks HSBC FileFixer");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = mainFrame.getContentPane();
 
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
 
		mainFrame.pack();
		mainFrame.setVisible(true);
    }

	public static void main(String args[]) {
        Runnable app = new QboFileFixer();
        try {
            SwingUtilities.invokeAndWait(app);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
 	}	
 }
