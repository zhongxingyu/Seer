 package taberystwyth.view;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class OverviewFrameMenuListener implements ActionListener {
 	OverviewFrame overviewFrame;
 	
 	public OverviewFrameMenuListener(OverviewFrame of){
 		overviewFrame = of;
 	}
 
	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("New")){
 			overviewFrame.new_();
 		}
 		if(e.getActionCommand().equals("Open")){
 			overviewFrame.open();
 		}
 		
 		if(e.getActionCommand().equals("Save")){
 			overviewFrame.save();
 		}
 		
 		if(e.getActionCommand().equals("Quit")){
 			overviewFrame.quit();
 		}
 		
 		if(e.getActionCommand().equals("Teams")){
 			overviewFrame.insertSpeakers();
 		}
 		
 		if(e.getActionCommand().equals("Judges")){
 			overviewFrame.insertJudges();
 		}
 		
 		if(e.getActionCommand().equals("Locations")){
 			overviewFrame.insertLocations();
 		}
 		
 		if(e.getActionCommand().equals("Draw Round")){
 			overviewFrame.drawRound();
 		}
 		
 		if(e.getActionCommand().equals("View Rounds")){
 			overviewFrame.viewRounds();
 		}
 		
 		if(e.getActionCommand().equals("About")){
 			overviewFrame.about();
 		}
 
 	}
 
 }
