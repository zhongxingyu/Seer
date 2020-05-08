 import java.util.*;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 
 public class Play extends JFrame implements ActionListener, MouseMotionListener, MouseListener{
 
 	Board playBoard;
 
 	String difficulty;
 	
 	boolean gameOver = false;
 
 	public static void main(String[] args) {
 
		Play game = new Play("hard");
 
 		game.playBoard.initializeBoard();
 		
 		JFrame newGameWindow = new JFrame("MINESWEEPER: CHOOSE A DIFFICULTY");
 
 		//JFrame window = new JFrame(""+game.getDifficulty());
 		game.setTitle(""+game.getDifficulty()+"     Flag Count: "+game.playBoard.getFlagCount());
 		game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Display d = new Display(game.playBoard);
 		game.add(d);
 		game.setSize(30*game.playBoard.getWidth()+5,30*game.playBoard.getHeight()+25);
 		game.setVisible(true);
 		game.setResizable(false);
 
 		game.addMouseListener(game);
 
 	}
 
 	public Play(String difficulty){
 
 		this.difficulty = difficulty;
 
 		if(difficulty.contains("easy")){
 			playBoard = new Board(8,8);
 		}
 
 		if(difficulty.contains("medium")){
 			playBoard = new Board(16,16);
 		}
 
 		if(difficulty.contains("hard")){
 			playBoard = new Board(30,16);
 		}
 
 	}
 
 	public String getDifficulty(){
 
 		return difficulty;
 	}
 
 	public boolean gameOver(){
 
 		if(playBoard.getWin() || playBoard.getLose())
 
 			gameOver = true;
 
 		return gameOver;
 	}
 
 	public static class Display extends JPanel{
 
 		Board displayBoard;
 
 		public Display(Board array){
 
 			displayBoard = array;
 
 		}
 
 		public void paintComponent(Graphics g){
 
 			super.paintComponent(g);
 			this.setBackground(Color.WHITE);
 
 			displayBoard.paintBoard(g);
 
 			//			g.setColor(Color.BLUE);
 			//			g.fillRect(25, 25, 100, 30);
 			//			
 			//			g.setColor(new Color(190,81,215)); //red, green, blue nothing more than 255
 			//			g.fillRect(25, 65, 100, 30);
 			//			
 			//			g.setColor(Color.RED);
 			//			g.drawString("MINESWEEPER", 100, 200); // String, x, y
 
 		}
 
 	}
 
 	public void mouseClicked(MouseEvent e) {
 		
 		
 		//TODO
 		//after game over left click and new game starts
 		
 		//TODO 
 		//make window popup asking if they want an easy, medium, or hard game --- closes --- opens game
 		
 		if(!gameOver()){
 
 		int x = e.getX()/30;
 		int y = (e.getY()-30)/30;
 
 		if(SwingUtilities.isLeftMouseButton(e)){
 
 			playBoard.open(x, y);
 			
 			if(gameOver())
 				gameOverTitle();
 		}
 		else if(SwingUtilities.isRightMouseButton(e)){
 
 			playBoard.markFlagged(x, y);
 			updateFlagTitle();
 		}
 
		repaint();
 	}
 	}
 
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void mouseDragged(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void updateFlagTitle(){
 
 		setTitle(""+getDifficulty()+"     Flag Count: "+playBoard.getFlagCount());
 
 	}
 
 	public void gameOverTitle(){
 
 		if(playBoard.getWin())
 		
 			setTitle("GAME OVER! YOU WIN!");
 		
 		else
 			setTitle("GAME OVER! YOU LOSE");
 
 	}
 
 }
 
 
