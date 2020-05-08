 package polyfive.ui.memberpages;
 
 import polyfive.entities.Member;
 import polyfive.ui.adminpages.*;
 import polyfive.ui.images.*;
 import polyfive.ui.master.*;
 import polyfive.ui.publicpages.*;
 
 import java.awt.Cursor;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 
 import java.awt.Font;
 import java.awt.Color;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.Dimension;
 import javax.swing.border.EtchedBorder;
 
 public class PaymentDetails extends MasterPanel {
 	private MainFrame f;
 
 	/**
 	 * Create the panel.
 	 */
 	public PaymentDetails(MainFrame frame) {
 		f = frame;
 		setSize(new Dimension(1366, 768));
 		setLayout(null);
 
 		JButton button = new JButton("");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Member user = new Member();
 				user = f.getSession();
 				if(user.getRank() <= 4 ){
 				MemberCalendar memberCalendar = new MemberCalendar(f);
 				f.getContentPane().removeAll();
 				f.getContentPane().add(memberCalendar);
 				f.repaint();
 				f.revalidate();
 				f.setVisible(true);
 				}
 				else {
 					AdminCalendar adminCalendar = new AdminCalendar(f);
 					f.getContentPane().removeAll();
 					f.getContentPane().add(adminCalendar);
 					f.repaint();
 					f.revalidate();
 					f.setVisible(true);
 				}
 			}
 		});
 		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 		button.setIcon(new ImageIcon(PaymentDetails.class
 				.getResource("/polyFive/ui/images/p5cicon7575.png")));
 		button.setBorder(null);
 		button.setBounds(21, 21, 75, 75);
 		add(button);
 
 		JTextArea txtrPayment = new JTextArea();
 		txtrPayment.setFont(new Font("Tahoma", Font.PLAIN, 19));
 		txtrPayment.setText("Purchase Detail\r\n\r\n\r\nAmount");
 		txtrPayment.setEditable(false);
 		txtrPayment.setBounds(422, 257, 593, 205);
 		add(txtrPayment);
 		
 
 		JButton btnBack = new JButton("Back");
 		btnBack.setBorder(new EtchedBorder(EtchedBorder.LOWERED,
 				Color.DARK_GRAY, null));
 		btnBack.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
				SeatAllocation seatAllocation = new SeatAllocation(f);
 				f.getContentPane().removeAll();
				f.getContentPane().add(seatAllocation);
 				f.repaint();
 				f.revalidate();
 				f.setVisible(true);
 			}
 		});
 		btnBack.setForeground(Color.DARK_GRAY);
 		btnBack.setBackground(new Color(255, 165, 0));
 		btnBack.setFont(new Font("Tahoma", Font.PLAIN, 30));
 		btnBack.setBounds(21, 664, 150, 75);
 		add(btnBack);
 
 		JButton btnNext = new JButton("Next");
 		btnNext.setBorder(new EtchedBorder(EtchedBorder.LOWERED,
 				Color.DARK_GRAY, null));
 		btnNext.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				PaymentPanel paymentPanel = new PaymentPanel(f);
 				f.getContentPane().removeAll();
 				f.getContentPane().add(paymentPanel);
 				f.repaint();
 				f.revalidate();
 				f.setVisible(true);
 			}
 		});
 		btnNext.setForeground(Color.DARK_GRAY);
 		btnNext.setBackground(new Color(255, 165, 0));
 		btnNext.setFont(new Font("Tahoma", Font.PLAIN, 30));
 		btnNext.setBounds(1115, 664, 150, 75);
 		add(btnNext);
 
 		super.setLayout();
 
 	}
 
 }
