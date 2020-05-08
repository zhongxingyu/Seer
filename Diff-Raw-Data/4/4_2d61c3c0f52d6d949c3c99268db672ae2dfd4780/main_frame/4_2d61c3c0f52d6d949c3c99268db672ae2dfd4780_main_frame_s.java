 package hugogui;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import java.awt.ScrollPane;
 import java.awt.Button;
 import java.awt.Panel;
 import java.awt.Label;
 import java.awt.FlowLayout;
 import javax.swing.JSplitPane;
 
 public class main_frame extends JFrame {
 
 	private JPanel contentPane;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					main_frame frame = new main_frame();
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
 	public main_frame() {
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 450, 300);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		setContentPane(contentPane);
 		
 		ScrollPane homepane = new ScrollPane();
 		contentPane.add(homepane, BorderLayout.SOUTH);
 		
 		ScrollPane scrollPane_1 = new ScrollPane();
 		contentPane.add(scrollPane_1, BorderLayout.NORTH);
 		
 		ScrollPane scrollPane_2 = new ScrollPane();
 		contentPane.add(scrollPane_2, BorderLayout.CENTER);
 		
 		Panel gamestats = new Panel();
 		contentPane.add(gamestats, BorderLayout.EAST);
 		gamestats.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		
 		Button newgame = new Button("New Game");
 		gamestats.add(newgame);
 		
 		Label punktestand = new Label("5000:5000");
 		gamestats.add(punktestand);
 		
 		JSplitPane splitPane = new JSplitPane();
 		contentPane.add(splitPane, BorderLayout.WEST);
 		
 		JPanel panel = new JPanel();
 		splitPane.setLeftComponent(panel);
 		
 		JPanel panel_1 = new JPanel();
 		splitPane.setRightComponent(panel_1);
 	}
 
 }
