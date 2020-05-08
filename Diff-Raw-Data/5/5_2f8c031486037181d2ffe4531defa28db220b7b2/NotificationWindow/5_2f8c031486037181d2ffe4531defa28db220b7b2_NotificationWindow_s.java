 package FP;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.EtchedBorder;
 import javax.swing.plaf.basic.BasicInternalFrameTitlePane.CloseAction;
 
 public class NotificationWindow extends JFrame{
 
 	private JLabel notification;
 	private JButton accept, decline;
 	
 	public NotificationWindow(String message)
 	{
 		drawWindow(message);
 	}
 	
 	private void drawWindow(String message)
 	{
 		setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridy = 0;
 		c.anchor = GridBagConstraints.FIRST_LINE_START;
 		c.insets = new Insets(15, 15, 10, 15);
 		notification = new JLabel(message);
 		notification.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
 		add(notification, c);
 		
 		c.gridy = 1;
 		c.anchor = GridBagConstraints.LAST_LINE_END;
 		c.insets = new Insets(10, 15, 5, 15);
 		
		accept = new JButton("Godta");
 		accept.addActionListener(new AcceptButtonAction());
		decline = new JButton("Avsl");
 		decline.addActionListener(new DeclineButtonAction());
 		JPanel pnl = new JPanel();
 		pnl.add(accept);
 		pnl.add(decline);
 		add(pnl, c);
 	}
 	
 	
 	class AcceptButtonAction implements ActionListener{
 		public void actionPerformed(ActionEvent e) {
 			// TODO: message to database
 			dispose();
 		}
 	}
 	
 	class DeclineButtonAction implements ActionListener{
 		public void actionPerformed(ActionEvent e) {
 			// TODO: message to database
 			dispose();
 		}
 	}
 }
