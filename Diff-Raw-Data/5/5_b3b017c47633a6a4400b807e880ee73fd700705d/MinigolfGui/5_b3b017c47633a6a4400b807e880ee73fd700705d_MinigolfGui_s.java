 package minigolf;
 
 import java.awt.*;
 import java.util.TimerTask;
 
 import javax.swing.*;
 //import org.jdesktop.swingx.*;
 
 import minigolf.MinigolfGame.MGTimerTask;
 
 public class MinigolfGui {
 	public MinigolfGui() {
 		initComponents();
 	}
 
 	private void initComponents() {
 		// JFormDesigner - Component initialization - DO NOT MODIFY
 		// //GEN-BEGIN:initComponents
 		MGframe = new JFrame();
 		pnlCtrl = new JPanel();
 		button1 = new JButton();
 		button2 = new JButton();
 		label1 = new JLabel();
 		textField1 = new JTextField();
 		button3 = new JButton();
 		pnlMain = new JPanel();
 		pnlStatus = new JPanel();
 		label2 = new JLabel();
 		label3 = new JLabel();
 		label4 = new JLabel();
 		label5 = new JLabel();
 		Game = new MinigolfGame();
 		cl = new Clock(50, 50);
 
 		// ======== MGframe ========
 		{
 			MGframe.setBackground(new Color(255, 102, 255));
 			MGframe.setTitle("SUPER MINIGOLF!!!!1111!!!!!11!1                                1337");
 			Container MGframeContentPane = MGframe.getContentPane();
 			MGframeContentPane.setLayout(new BorderLayout());
 
 			// ======== pnlCtrl ========
 			{
 				pnlCtrl.setBackground(new Color(102, 255, 102));
 				pnlCtrl.setPreferredSize(new Dimension(100, 40));
 
 				// JFormDesigner evaluation mark
 				pnlCtrl.setBorder(new javax.swing.border.CompoundBorder(
 						new javax.swing.border.TitledBorder(
 								new javax.swing.border.EmptyBorder(0, 0, 0, 0),
 								"Minigolf FSTC SS 12-13 INFO II",
 								javax.swing.border.TitledBorder.CENTER,
 								javax.swing.border.TitledBorder.BOTTOM,
 								new java.awt.Font("Dialog", java.awt.Font.BOLD,
 										12), java.awt.Color.red), pnlCtrl
 								.getBorder()));
 				pnlCtrl.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
 					public void propertyChange(java.beans.PropertyChangeEvent e) {
 						if ("border".equals(e.getPropertyName()))
 							throw new RuntimeException();
 					}
 				});
 
 				pnlCtrl.setLayout(null);
 
 				// ---- button1 ----
 				button1.setText("Start");
 				pnlCtrl.add(button1);
 				button1.setBounds(5, 5, 80, 30);
 
 				// ---- button2 ----
 				button2.setText("Stop");
 				pnlCtrl.add(button2);
 				button2.setBounds(95, 5, 80, 30);
 
 				// ---- label1 ----
 				label1.setText(" Name:");
 				pnlCtrl.add(label1);
 				label1.setBounds(565, 5, 40, 29);
 
 				// ---- textField1 ----
 				textField1.setPreferredSize(new Dimension(55, 23));
 				pnlCtrl.add(textField1);
 				textField1.setBounds(615, 5, 90, 30);
 
 				// ---- button3 ----
 				button3.setText("Submit");
 				button3.setPreferredSize(new Dimension(55, 23));
 				pnlCtrl.add(button3);
 				button3.setBounds(715, 5, 80, 30);
 
 				{ // compute preferred size
 					Dimension preferredSize = new Dimension();
 					for (int i = 0; i < pnlCtrl.getComponentCount(); i++) {
 						Rectangle bounds = pnlCtrl.getComponent(i).getBounds();
 						preferredSize.width = Math.max(bounds.x + bounds.width,
 								preferredSize.width);
 						preferredSize.height = Math.max(bounds.y
 								+ bounds.height, preferredSize.height);
 					}
 					Insets insets = pnlCtrl.getInsets();
 					preferredSize.width += insets.right;
 					preferredSize.height += insets.bottom;
 					pnlCtrl.setMinimumSize(preferredSize);
 					pnlCtrl.setPreferredSize(preferredSize);
 				}
 			}
 			MGframeContentPane.add(pnlCtrl, BorderLayout.SOUTH);
 
 			// ======== pnlMain ========
 			{
 				pnlMain.setBackground(new Color(102, 51, 255));
 				pnlMain.setPreferredSize(new Dimension(800, 600));
 				pnlMain.setMaximumSize(new Dimension(800, 600));
 				pnlMain.setMinimumSize(new Dimension(800, 600));
 				pnlMain.setLayout(new BoxLayout(pnlMain, BoxLayout.X_AXIS));
 
 				pnlMain.add(Game);
 
 			}
 
 			MGframeContentPane.add(pnlMain, BorderLayout.CENTER);
 
 			// ======== pnlStatus ========
 			{
 				pnlStatus.setPreferredSize(new Dimension(100, 40));
 				pnlStatus.setLayout(null);
 
 				// ---- label2 ----
 				label2.setText("Time passed:");
 				label2.setHorizontalAlignment(SwingConstants.CENTER);
 				label2.setFont(new Font("Tahoma", Font.BOLD, 14));
 				pnlStatus.add(label2);
 				label2.setBounds(0, 0, label2.getPreferredSize().width, 40);
 
 				// ---- label3 ----
 				label3.setText("Ball Shots:");
 				label3.setHorizontalAlignment(SwingConstants.CENTER);
 				label3.setFont(new Font("Tahoma", Font.BOLD, 14));
 				pnlStatus.add(label3);
 				label3.setBounds(525, 0, 75, 40);
 
 				// ---- label4 ----
 				label4.setText(cl.Time + cl.dspHours + ":" + cl.dspMinutes
 						+ ":" + cl.dspSeconds);
 				label4.setHorizontalAlignment(SwingConstants.CENTER);
 				label4.setFont(new Font("Tahoma", Font.PLAIN, 14));
 				pnlStatus.add(label4);
 				label4.setBounds(100, 0, 80, 40);
 
 				// ---- label5 ----
 				label5.setText(" " + Game.clicks);
 				label5.setHorizontalAlignment(SwingConstants.CENTER);
 				label5.setFont(new Font("Tahoma", Font.PLAIN, 14));
 				pnlStatus.add(label5);
 				label5.setBounds(600, 0, 75, 40);
 				
 				//Updating labels
 			
 
 				{ // compute preferred size
 					Dimension preferredSize = new Dimension();
 					for (int i = 0; i < pnlStatus.getComponentCount(); i++) {
 						Rectangle bounds = pnlStatus.getComponent(i)
 								.getBounds();
 						preferredSize.width = Math.max(bounds.x + bounds.width,
 								preferredSize.width);
 						preferredSize.height = Math.max(bounds.y
 								+ bounds.height, preferredSize.height);
 					}
 					Insets insets = pnlStatus.getInsets();
 					preferredSize.width += insets.right;
 					preferredSize.height += insets.bottom;
 					pnlStatus.setMinimumSize(preferredSize);
 					pnlStatus.setPreferredSize(preferredSize);
 				}
 			}
 			MGframeContentPane.add(pnlStatus, BorderLayout.NORTH);
 			MGframe.pack();
 			MGframe.setLocationRelativeTo(MGframe.getOwner());
 
 		}
 		// JFormDesigner - End of component initialization
 		// //GEN-END:initComponents
 	}
 
 	// JFormDesigner - Variables declaration - DO NOT MODIFY
 	// //GEN-BEGIN:variables
 	private JFrame MGframe;
 	private JPanel pnlCtrl;
 	private JButton button1;
 	private JButton button2;
 	private JLabel label1;
 	private JTextField textField1;
 	private JButton button3;
 	private JPanel pnlMain;
 	private JPanel pnlStatus;
 	private JLabel label2;
 	private JLabel label3;
 	private JLabel label4;
 	private JLabel label5;
 	public MinigolfGame Game;
 	Clock cl;
 	Graphics g;
	
 	private TimerTask tt = new TimerTask() {
 
 		@Override
 		public void run() {
 		if(Game.hasBall==false){
		label4.setText(cl.Time + cl.dspHours + ":" + cl.dspMinutes + ":" + cl.dspSeconds);}
		else{
 		label5.setText(" " + Game.clicks);}
 		}
 
 		};
 
 	// String time = cl.toStringdsp(1, 1,g);
 	// JFormDesigner - End of variables declaration //GEN-END:variables
 	public static void main(String arg[]) {
 		MinigolfGui panel = new MinigolfGui();
 
 		panel.MGframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		panel.MGframe.setVisible(true);
 		java.util.Timer vgTimer = new java.util.Timer();
 		vgTimer.schedule(panel.Game.mgTask, 0, 30);
 		vgTimer.schedule(panel.tt, 2000l,1l);
 		panel.MGframe.pack();
 
 	}
 }
