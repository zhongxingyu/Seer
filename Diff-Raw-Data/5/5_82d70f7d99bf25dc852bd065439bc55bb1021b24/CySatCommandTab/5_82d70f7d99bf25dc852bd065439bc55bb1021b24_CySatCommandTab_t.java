 package gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 public class CySatCommandTab extends JPanel {
 	private static final long serialVersionUID = 0L;
//	private CySatGUI gui;
 	
 	public CySatCommandTab(CySatGUI gui) {
//		this.gui = gui;
 		
 		initComponents();
 	}
 	
 	private void initComponents() {
 		JButton hello = new JButton("Send 'Hello'");
 		hello.setFocusable(false);
 		add(hello);
 		
 		final JTextArea response = new JTextArea(5, 30);
 		response.setText("Testing...\n");
 		hello.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				response.append("Hello, World!\n");
 			}
 		});
 		JScrollPane scrollPane = new JScrollPane(response, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		add(scrollPane);
 	}
 }
