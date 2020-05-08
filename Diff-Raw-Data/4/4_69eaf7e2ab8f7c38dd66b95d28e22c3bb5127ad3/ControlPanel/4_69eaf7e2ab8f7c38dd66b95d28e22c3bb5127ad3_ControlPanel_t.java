 /**
  * 
  */
 package ch.eiafr.mmmm.gui;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import javax.swing.JButton;
 import java.awt.GridLayout;
 
 /**
  * @author yannickjemmely
  *
  */
 public class ControlPanel extends JPanel {
 
 	
 	private static final String name = "Control";
 	
 	private JPanel wiiHandPanel = new JPanel();
 	private JPanel wiiHeadPanel = new JPanel();
 	private JPanel kinectPanel = new JPanel();
 	
 	
 	/**
 	 * Create the panel.
 	 */
 	public ControlPanel() {
 		setName(name);
 		setLayout(new BorderLayout(0, 0));
 		
 		buildWiiHandPanel();
 		buildWiiHeadPanel();
 		
 		JPanel kinectPanel = new JPanel();
 		add(kinectPanel, BorderLayout.SOUTH);
 		kinectPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		
 		JButton headButton = new JButton("Head");
 		kinectPanel.add(headButton);
 		
 		JButton mineButton = new JButton("Mine");
 		kinectPanel.add(mineButton);
 		
 		JButton startButton = new JButton("start");
 		kinectPanel.add(startButton);
 		
 		JButton stopButton = new JButton("stop");
 		kinectPanel.add(stopButton);
 		
 		JPanel kinectNumberPanel = new JPanel();
 		kinectPanel.add(kinectNumberPanel);
 		kinectNumberPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		JButton oneButton = new JButton("one");
 		kinectNumberPanel.add(oneButton);
 		kinectNumberPanel.setBorder(BorderFactory.createTitledBorder("Voice"));
 	}
 	
 	private void buildWiiHandPanel(){
 		add(wiiHandPanel, BorderLayout.NORTH);
 		wiiHandPanel.setLayout(new GridLayout(1, 0, 0, 0));
 	}
 	
 	private void buildWiiHeadPanel(){
 		add(wiiHeadPanel, BorderLayout.WEST);
 		wiiHeadPanel.setLayout(new GridLayout(1, 0, 0, 0));
 	}
 	
	private void buildKinectPanel(){
		
	}
 	
 
 }
