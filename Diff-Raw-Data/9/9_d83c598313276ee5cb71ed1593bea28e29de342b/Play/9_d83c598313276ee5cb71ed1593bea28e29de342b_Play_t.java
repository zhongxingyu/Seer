 import java.util.*;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 
 public class Play extends JFrame implements ActionListener, MouseMotionListener, MouseListener{
 
 	Board playBoard;
 
 	String difficulty;
 	boolean win = false;
 	boolean lose = false;
 	boolean gameOver = false;
 
 
 	public static void main(String[] args) {
 
		Play game = new Play("easy");
 		game.playBoard.initializeBoard();
 
 		//JFrame window = new JFrame(""+game.getDifficulty());
 		game.setTitle(""+game.getDifficulty());
 		game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Display d = new Display(game.playBoard);
 		game.add(d);
 		game.setSize(30*game.playBoard.getWidth()+5,30*game.playBoard.getHeight()+25);
 		game.setVisible(true);
 		game.setResizable(false);
 		
 		game.addMouseListener(game);
 
 //		Scanner scan = new Scanner(System.in);
 //
 //		System.out.println("Please place your x and y coordinates seperated by a space");
 //
 //		int x = scan.nextInt();
 //		int y = scan.nextInt();
 //
 //		if(game.playBoard.isValid(x-1, y-1)){ //and being opened
 //			game.playBoard.setStartXandY(x-1,y-1);
 //			game.playBoard.openBox(x-1, y-1);
 //		}
 //		
 //		d.repaint();
 //
 //		while(!game.gameOver()){
 //
 //			System.out.println("Please place your x and y coordinates seperated by a space");
 //			if(game.playBoard.isValid(x-1, y-1)){
 //
 //				x = scan.nextInt();
 //				y = scan.nextInt();
 //
 //				game.playBoard.openBox(x-1,y-1); //left click -- right click will be markFlagged
 //				d.repaint();
 //			}
 //		}
 //
 //		System.out.println("GAME OVER!");
 //
 //		if(game.gameOverWin())
 //			System.out.print("YOU WIN!");
 //
 //		else
 //			System.out.print("YOU LOSE!");
 
 
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
 
 		if(gameOverWin() || gameOverLose())
 
 			gameOver = true;
 
 		return gameOver;
 	}
 
 	public boolean gameOverWin(){
 		
 		if((playBoard.getOpenedBoxCount() == (playBoard.getTotalBoxes()) - (playBoard.getTotalBombs())))
 			win = true;
 
 		return win;
 	}
 
 	public boolean gameOverLose(){
 
 		if(playBoard.getTouchedBomb())
 			lose = true;
 
 		return lose;
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
 	
 	int x = e.getX()/30;
 	int y = (e.getY()-30)/30;
 	
 	if(SwingUtilities.isLeftMouseButton(e)){
 	
 		playBoard.open(x, y);
 	}
 	else if(SwingUtilities.isRightMouseButton(e)){
 		
 		playBoard.markFlagged(x, y);
 	}
 	
 	repaint();
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
 	
 
 	
 }
 
 
