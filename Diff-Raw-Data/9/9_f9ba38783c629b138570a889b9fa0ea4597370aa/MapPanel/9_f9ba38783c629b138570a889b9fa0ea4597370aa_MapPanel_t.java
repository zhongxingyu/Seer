 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 
 public class MapPanel extends JPanel{
 	private Map map=null;
 	private GridLayout grid=null;
 	private boolean random; //implement random maps later
 	private GameMain frame;
 	private static JLabel curPlayerName, currRoundLabel, currMoney;
 	private static JLabel remainTime;
 	private JButton done;
 	private static boolean isDone;
 	private int currRound;
 	protected Player curPlayer;
 	private JPanel entire,sub;
 
 	public MapPanel(boolean random, GameMain frame){
 		this.frame=frame;
 		this.random = random;
 		
 		entire = new JPanel();
 		sub = new JPanel();
 		
 		setVisible(true);
 		map= new Map(false, frame);
 		//System.out.println(this.toString());
 		grid = new GridLayout(Map.NUM_ROW, Map.NUM_COL);
 		setLayout(grid);
 		JButton[][] buttons = map.getMap();
 
 		for(int r=0; r<buttons.length; r++){
 			if(buttons[r]!=null){
 				for(int c=0; c<buttons[r].length; c++){
 					if(buttons[r][c]!=null){
 						System.out.println(buttons[r][c].toString());
 						add(buttons[r][c]);
 					}
 				}
 			}
 		}
 		curPlayerName = new JLabel("Current Player: " );
 		currRoundLabel = new JLabel("Current Round: ");
 		remainTime = new JLabel("Remaining Time: ");
 		currMoney = new JLabel("Money amount: ");
 		sub.add(curPlayerName);
 		sub.add(currRoundLabel);
 		sub.add(remainTime);
 		sub.add(currMoney);
 		done = new JButton("Done");
 		sub.add(done);
		done.addActionListener(new DListener(frame));
 		currRound = 1;
 		entire.add(this);
 		entire.add(sub);
 		this.frame.setContentPane(entire);
 	}
 	
 	public static void setIsDone(boolean isDonex){
 		isDone = isDonex;
 	}
 	
 	public boolean getIsDone(){
 		return isDone;
 	}
 	
 	public void setCurPlayer(Player p){
 		curPlayer = GameMain.getCurrPlayer();
 		
 	}
 	
 	public Player getCurrPlayer(){
 		return curPlayer;
 	}
 	
 	public void nextTurn(){
 		currRound++;
 	}
 	
 	public boolean gameOver(int currTurn){
 		if(currTurn<12)
 			return false;
 		return true;
 	}
 	
 	public String toString(){
 		return "I am a Map Panel";
 	}
 	
 	public int getCurrTurn(){
 		return currRound;
 	}
 	
 	public void setCurrPlayerLabel(String name){
 		curPlayerName.setText("Current Player: " + name);
 	}
 	
 	public void setCurrRound(int round){
 		currRoundLabel.setText("Current Round: " + round);
 	}
 	
 	public void setRemainTime(int time){
 		remainTime.setText("Remaining Time: "+time);
 	}
 	
 	public void setCurrMoney(int money){
 		currMoney.setText("Money Amount: " + money);
 	}
 }
 
 class DListener implements ActionListener{
	GameMain frame;
	public DListener(GameMain frame){
		this.frame = frame;
	}
 	public void actionPerformed(ActionEvent event) {
 		Player p = GameMain.getCurrPlayer();
 		p.setDone(true);
		frame.cancelTimer();
 	}
 }
