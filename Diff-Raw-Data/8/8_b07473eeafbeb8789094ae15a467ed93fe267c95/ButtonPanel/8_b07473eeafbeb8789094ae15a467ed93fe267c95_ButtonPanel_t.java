 package cha.gui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import cha.controller.ChallengeAccepted;
 import cha.controller.Event;
 import cha.controller.IEventHandler;
 import cha.domain.Bet;
 
 @SuppressWarnings("serial")
 public class ButtonPanel extends JPanel implements IEventHandler, ActionListener {
 	
 	private static final String Bet = null;
 	private JButton startMissionButton;
 	private JButton cancelButton;
 	private JButton yesButton;
 	private JButton noButton;
 	private JButton nextButton;
 	private JButton doneButton;
 	private JLabel timer;
 	
 	public ButtonPanel(){
 		ChallengeAccepted.getInstance().register(this);
 		
 		timer = new JLabel();
 		timer.setFont(new Font("Dialog", Font.BOLD, 20));
 		startMissionButton = new JButton("Start Mission");
 		cancelButton = new JButton("Cancel");
 		yesButton = new JButton("Yes");
 		noButton = new JButton("No");
 		nextButton = new JButton("Next Card");
 		doneButton = new JButton("Done");
 		startMissionButton.addActionListener(this);
 		cancelButton.addActionListener(this);
 		yesButton.addActionListener(this);
 		noButton.addActionListener(this);
 		nextButton.addActionListener(this);
 		doneButton.addActionListener(this);
 		
 		this.add(startMissionButton);
 		this.add(cancelButton);
 		this.add(nextButton);
 		this.add(doneButton);
 		this.add(yesButton);
 		this.add(noButton);
 		this.add(timer);
 		
 		startMissionButton.setVisible(false);
 		cancelButton.setVisible(false);
 		nextButton.setVisible(false);
 		doneButton.setVisible(false);
 		yesButton.setVisible(false);
 		noButton.setVisible(false);
 		timer.setVisible(false);
 		
 		this.setBackground(Color.WHITE);
 	}
 	@Override
 	public void action(Event e, Object o) {
 		if(e == Event.ShowBet){
 			startMissionButton.setVisible(false);
 			cancelButton.setVisible(false);
 		}
 		else if(e == Event.MakeBet){
 			startMissionButton.setVisible(true);
 			cancelButton.setVisible(true);
 		}
 		else if(e == Event.StartMission){
 			startMissionButton.setVisible(false);
 			cancelButton.setVisible(false);
 			nextButton.setVisible(true);
 			doneButton.setVisible(true);
 			timer.setVisible(true);
 		}
 		else if(e == Event.TimeOver){
 			nextButton.setVisible(false);
 			doneButton.setVisible(false);
 			yesButton.setVisible(true);
 			noButton.setVisible(true);
 			timer.setVisible(false);
 		}
 		else if(e == Event.TimeTick){
 			String time = (String)o;
 			timer.setText(time);
 		}
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == startMissionButton){

 			ChallengeAccepted.getInstance().getBoard().startMission();

 			//ChallengeAccepted.getInstance().getBoard().startMission(Bet.getBetValue());

 			ChallengeAccepted.getInstance().publish(Event.StartMission, 
 					ChallengeAccepted.getInstance().getBoard().getMission());
 		}
 		else if(e.getSource() == cancelButton){
 			ChallengeAccepted.getInstance().getBoard().getActivePiece().setBet(0);
 			ChallengeAccepted.getInstance().publish(Event.ShowBet, null);
 		}
 		else if(e.getSource() == doneButton){
 			//TODO
 			ChallengeAccepted.getInstance().getBoard().getMission().timeOver();
 			ChallengeAccepted.getInstance().publish(Event.TimeOver, null);
 		}
 		else if(e.getSource() == yesButton){
 			ChallengeAccepted.getInstance().getBoard().getMission().missionDone(true);
 			ChallengeAccepted.getInstance().publish(Event.MissionSuccess, null);
 		}
 	}
 }
