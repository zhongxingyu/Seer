 package frontend.highscoreDisplay;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import frontend.TimeDisplay;
 
 import backend.highscore.*;
 
 public class HighscoreList extends JFrame {
 	
 	private HighscoreIndex index;
 	private int currDifficulty;
 	private Entry[] entries;
 	
 	public HighscoreList(HighscoreIndex index, int d){
 		setIndex(index);
 		setDifficulty(d);
 		init();
 	}
 	
 	public void setIndex(HighscoreIndex index){
 		this.index = index;
 	}
 	
 	public void setDifficulty(int d){
 		currDifficulty =  d;
 	}
 	
 	private void init(){
 		entries = new Entry[10];
		HighscoreItem[] items = index.getScores(currDifficulty);
 		int i = 0;
 		for(HighscoreItem h : items){
 			entries[i] = new Entry(h);
 		}
 		setLayout(new GridLayout(10,2));//Need separate component?
 	}
 	
 	private class Entry extends JPanel{
 		
 		public static final int TEXT_SIZE = 20;
 		
 		private HighscoreItem h;
 		
 		public Entry(HighscoreItem h){
 			this.h = h;
 			init();
 		}
 		
 		private void init(){
 			
 			this.setLayout(new GridLayout(1,2));
 			
 			JLabel name = new JLabel();
 			JLabel time = new JLabel();
 			
 			name.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TEXT_SIZE));
 			name.setHorizontalAlignment(JLabel.LEFT);
 			time.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TEXT_SIZE));
 			time.setHorizontalAlignment(JLabel.RIGHT);
 			
 			name.setText(h.getName());
 			time.setText(TimeDisplay.tenthsToString(h.getTime()));
 			
 			this.add(name);
 			this.add(time);
 			
 			setVisible(true);
 		}
 		
 	}
 
 }
