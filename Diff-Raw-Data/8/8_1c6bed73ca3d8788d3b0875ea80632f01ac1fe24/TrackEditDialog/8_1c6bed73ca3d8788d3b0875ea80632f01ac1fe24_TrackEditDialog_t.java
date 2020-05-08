 package CTCOffice;
 
 import java.awt.BorderLayout;
 import java.awt.Checkbox;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 
 import TrackController.TrackController;
 import TrackModel.trackBlock;
 
 @SuppressWarnings("serial")
 public class TrackEditDialog extends JFrame implements ActionListener{
 
 	JButton okButton,cancelButton;
 	String label;
 	JTextField entry;
 	Checkbox maintenance;
 	TrackController controller;
 	trackBlock block;
 	TrackEditDialog(trackBlock trackBlock, TrackController trackController){
 		super("Edit Track Dialog");
 		label = "Track";
 		setMinimumSize(new Dimension(250,200));
 		setPreferredSize(this.getMinimumSize());
 		setResizable(false);
 		controller = trackController;
 		block = trackBlock;
 		JPanel master = new JPanel();
 		
 		master.setLayout(new BorderLayout());
 		master.setBorder(new TitledBorder(block.track_line +" " + block.block_number));
 		
 		JPanel fieldPanel = new JPanel();
 		fieldPanel.setLayout(new GridLayout(2,1));
 		JPanel speedLimit = new JPanel();
 		speedLimit.setLayout(new GridLayout(1,2));
 		JLabel label = new JLabel("Speed Limit: ");
 		speedLimit.add(label);
 		entry = new JTextField();
 		entry.setText(new Integer(block.speed_limit).toString());
 		speedLimit.add(entry);
 		maintenance = new Checkbox("Maintenance", block.maintenance);
 		
 		fieldPanel.add(speedLimit);
 		fieldPanel.add(maintenance);
 		
 		master.add(fieldPanel,BorderLayout.CENTER);
 		
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		okButton = new JButton("OK");
 		okButton.addActionListener(this);
 		cancelButton = new JButton("Cancel");
 		cancelButton.addActionListener(this);
 		buttonPanel.add(okButton);
 		buttonPanel.add(cancelButton);
 		
 		master.add(buttonPanel,BorderLayout.PAGE_END);
 		
 		this.add(master);
 		this.setVisible(true);
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource().equals(okButton)){
 				Object[] message = new Object[6];
 				message[0] = label;
 				message[1] = block.track_line.charAt(0)+block.block_number;
				message[2] = "Speed";
 				message[3] = entry.getText();
 				message[4] = "Maintenance";
 				message[5] = maintenance.getState();
 				
 				controller.GetSuggestion(message);
 				this.dispose();
 		}
 		else if(e.getSource().equals(cancelButton)){
 			this.dispose();
 			
 		}
 		
 	}
 
 }
