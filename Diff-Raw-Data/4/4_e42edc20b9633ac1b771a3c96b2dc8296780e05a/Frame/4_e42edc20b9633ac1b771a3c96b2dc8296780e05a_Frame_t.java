 package info.aki017.OpenRitsPen;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public class Frame {
 	private JFrame mainFrame;
 	private Drawspace drawspace;
 	private Protractor protractor;
 	private OpenRitsPen openRitsPen;
 
 	public Frame(OpenRitsPen openRitsPen) {
 		this.openRitsPen = openRitsPen;
 		mainFrame = new JFrame();
 		// ウインドウ閉じたら終わる
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		mainFrame.setTitle("Welcome to OpenRitsPen");
 
 		this.drawspace = new Drawspace(openRitsPen);
 
 		JButton clearButton = new JButton("clear");
 		clearButton.addActionListener(new ButtonListener());
 
 		JPanel leftPanel = new JPanel();
 		//縦に並べる
 		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
 
 		JPanel drawPanel = new JPanel();
 
 		JButton undoButton = new JButton(" ← ");
 		undoButton.addActionListener(new UndoButtonListener());
 
 		JButton redoButton = new JButton(" → ");
 		redoButton.addActionListener(new RedoButtonListener());
 
 		protractor = new Protractor(openRitsPen);
 
 		drawPanel.add(this.drawspace);
 
 		leftPanel.add(Box.createRigidArea(new Dimension(1, 220)));
 		leftPanel.add(clearButton, "Center");
 		leftPanel.add(Box.createRigidArea(new Dimension(1, 30)));
 		leftPanel.add(redoButton, "Center");
 		leftPanel.add(Box.createRigidArea(new Dimension(1, 30)));
 		leftPanel.add(undoButton, "Center");
 
 		leftPanel.add(Box.createRigidArea(new Dimension(1, 30)));
 		leftPanel.add(protractor, "Center");
 
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 		mainFrame.setIconImage(toolkit.getImage("images/picture.gif"));
 
 		mainFrame.getContentPane().add(leftPanel, "Center");
 		mainFrame.getContentPane().add(drawPanel, "East");
		mainFrame.setLocation(20, 20);
		mainFrame.getContentPane().setPreferredSize(new Dimension(800, 700));
		mainFrame.pack();
 		mainFrame.setVisible(true);
 
 		System.out.println("Frame Created");
 	}
 
 	public void repaint(){
 		drawspace.repaint();
 		protractor.repaint();
 	}
 	class ButtonListener implements ActionListener {
 		ButtonListener() {
 		}
 
 		public void actionPerformed(ActionEvent event) {
 			openRitsPen.reset();
 		}
 	}
 
 	class RedoButtonListener implements ActionListener {
 		RedoButtonListener() {
 		}
 
 		public void actionPerformed(ActionEvent event) {
 			openRitsPen.redo();
 		}
 	}
 
 	class UndoButtonListener implements ActionListener {
 		UndoButtonListener() {
 		}
 
 		public void actionPerformed(ActionEvent event) {
 			openRitsPen.undo();
 		}
 	}
 }
