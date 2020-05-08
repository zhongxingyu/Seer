 package pentagoxl.client;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.*;
 
 import pentagoxl.ProtocolEndpoint;
 
 public class RotateGUI extends JFrame implements ActionListener{
 	
 	private final GUI parent;
 	private JButton[] buttons;
 	
 	private static final int AANTALKNOPPEN = GUI.AANTALHOKKEN * 2; //2 richtingen per hok
 	
 	public RotateGUI(GUI parent) {
 		super("PentagoXL > Rotate");
 		this.parent = parent;
 		buildGUI();
 		this.setVisible(true);
 		
 		addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 e.getWindow().dispose();
             }
         }
     	);
 	}
 
 	private void buildGUI() {
 		
 		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
 		
 		//panel which contains the buttons
 		
 		JPanel buttonspane = new JPanel(new GridLayout(3,3));
 		
 		buttons = new JButton[AANTALKNOPPEN];
 		for (int i = 0; i < GUI.AANTALHOKKEN; i++) {
 			JPanel pane = new JPanel(new GridLayout(1, 2));
 			buttons[2 * i] = new JButton(i + "L");
 			buttons[2 * i].addActionListener(this);
 			pane.add(buttons[2 * i]);
 			
 			buttons[2 * i + 1] = new JButton(i + "R");
 			buttons[2 * i + 1].addActionListener(this);
 			pane.add(buttons[2 * i + 1]);
 			
 			buttonspane.add(pane);
 		}
 		
 		this.getContentPane().add(buttonspane);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		Object source = arg0.getSource();
 		for (int i = 0; i < AANTALKNOPPEN; i ++) {
 			if (source == buttons[i]) {
 				parent.sendRotate(i/2, i % 2 == 0 ? ProtocolEndpoint.DIRECTION_COUNTERCLOCKWISE : ProtocolEndpoint.DIRECTION_CLOCKWISE);
 				this.dispose();
 			}
 		}
 		
 	}
 }
