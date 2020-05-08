 package gui;
 
 import java.awt.Dimension;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import model.Scores;
 
 public class Scoreboard extends JPanel implements Observer {
 	
 	public Scoreboard() {
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		setPreferredSize(new Dimension(200, 200));
 		Scores.getInstance().addObserver(this);
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		removeAll();
	
 		Map<String, Integer> map = Scores.getInstance().getScores();
 		for (Entry<String, Integer> entry : map.entrySet()) {
 			String str = entry.getKey() + ": " + entry.getValue() + " points";
 			System.out.println(str);
 			add(new JLabel(str));
 		}
 //		for (String s : map.keySet()) {
 //			String str = s + ": " + map.get(s) + " points";
 //			System.out.println(str);
 //			add(new JLabel(str));
 //		}
 		
		revalidate();
		repaint();
 	}
 	
 
 }
