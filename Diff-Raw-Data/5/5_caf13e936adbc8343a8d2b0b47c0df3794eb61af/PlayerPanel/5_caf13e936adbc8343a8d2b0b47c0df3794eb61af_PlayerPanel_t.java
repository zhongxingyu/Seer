 package cha.gui;
 
 import java.awt.Color;
 import java.awt.Font;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import cha.domain.Board;
 import cha.event.Event;
 import cha.event.EventBus;
 import cha.event.IEventHandler;
 
 @SuppressWarnings("serial")
 public class PlayerPanel extends JPanel implements IEventHandler {
 
 	private JLabel player;
 	private JPanel panelColor;
 
 	
 	public PlayerPanel(){
 		EventBus.getInstance().register(this);
 		
 		setBackground(Color.WHITE);
 		panelColor = new JPanel();
 		panelColor.setBorder(BorderFactory.createLineBorder(Color.black));
 		Font activePlayerFont = new Font("Arial", Font.PLAIN, 18);
 		player = new JLabel();
 		player.setFont(activePlayerFont);
 		this.add(player);
 		this.add(panelColor);
 	}
 	
 	@Override
 	public void action(Event e, Object o, Object p) {
 		if(e == Event.NextPlayer){
 			player.setText("Active team: " + Board.getInstance().getActivePiece().getTeam().getName());
 			panelColor.setBackground(Board.getInstance().getActivePiece().getTeam().getColor());
 			
			
//			if (Board.getInstance().getTile(Board.getInstance().getActivePiece().getPosition()).isChallenge()) {
//				new ChallengePanel();
//			}
 			this.repaint();
 		}
 		else if(e == Event.CreateBoard){
 			player.setText("Team: " + Board.getInstance().getActivePiece().getTeam().getName() + "'s turn ");
 			panelColor.setBackground(Board.getInstance().getActivePiece().getTeam().getColor());
 			this.repaint();
 		}
 		
 	}
 	
 
 }
