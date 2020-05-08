 package mainGUI;
 
 import java.awt.BorderLayout;
import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import dataStructures.AbstractDocument;
 
 import multiStakeholderGUI.PaneTurnerMS;
 
 @SuppressWarnings("serial")
 public class StakeholderPane extends UpdatePane {
 
 	private JFrame parentFrame;
 	private PaneTurnerMS paneTurner;
 	private AbstractDocument document;
 	
 	public StakeholderPane(JFrame parentFrame, AbstractDocument document){
 		this.parentFrame = parentFrame;
 		this.document = document;
 		
		setLayout(new BorderLayout());
		this.add(createGUI(), BorderLayout.CENTER);
		
 		setVisible(true);
	
 	}
 	
 	@Override
 	public void update() {
 		// TODO Auto-generated method stub
 
 	}
 
 	private JPanel createGUI(){
 		JPanel panel = new JPanel();
 		panel.setLayout(new BorderLayout());
 		
 		paneTurner = new PaneTurnerMS(parentFrame, document);
 		
 		panel.add(paneTurner, BorderLayout.CENTER);
 		return panel;
 	}
 
 }
