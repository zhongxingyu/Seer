 package frontend;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.Timer;
 
 import backend.*;
 import backend.event.GameEndEvent;
 import backend.event.GameEndListener;
 
 public class MineFrame extends JFrame implements FocusListener{
 	
 	private Minesweeper game;
 	private Timer timer;
 	
 	private MineField mF;
 	
 	public MineFrame(){
 		super("Minesweeper");
 		setTitle("Minesweeper");
 		setLayout(new BorderLayout());
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		
 		backend();
 		selector();
 		minefield();
 		
 		addFocusListener(this);
 		
 		add(new TimeDisplay(game), BorderLayout.NORTH);
 		
 		setSize();
 		
 		setVisible(true);
 		
 	}
 
 	private void setSize() {
 		Dimension s = new Dimension(mF.getSize().width, mF.getSize().height + TimeDisplay.TEXT_HEIGHT);
 		setSize(s);
 		setMinimumSize(getSize());
 	}
 	
 	private void backend(){
 		backend(1);
 	}
 
 	private void backend(int d) {
 		setGame(new Minesweeper(d));
 		timer = game.getTimer();
 	}
 
 	private void selector() {
		// TODO make a selection screen
 		
 	}
 
 	private void minefield() {
 		mF = new MineField(game);
 		this.add(mF, BorderLayout.CENTER);
 		repaint();
 	}
 	
 	public Minesweeper getGame(){
 		return game;
 	}
 	
 	public void setGame(Minesweeper m){
 		game = m;
 	}
 	
 	public MineField getField(){
 		return mF;
 	}
 	
 	public void setDifficulty(int d) {
		// TODO bugs with refreshing and generating new displays
 		Minesweeper newGame = new Minesweeper(d);
 		setGame(newGame);
 		remove(mF);
 		minefield();
 		setSize();
 	}
 
 	@Override
 	public void focusGained(FocusEvent e) {
 		System.out.println("Gained focus");
		if(!game.isFinished()) // TODO not working properly in game
 			timer.start();
 	}
 
 	@Override
 	public void focusLost(FocusEvent e) {
 		System.out.println("Lost focus");
 		timer.stop();
 	}
 
 }
