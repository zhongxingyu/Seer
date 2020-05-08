 package team01;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @SuppressWarnings("serial")
 public class GUI extends JFrame {
 	
 	private List<Pair> gridLines = new ArrayList<Pair>();
 
 	private final Game game;
 	
 	private int width;
 	private int height;
 
 	private int spacing;
 	private int radius;
 	private int diameter;
 
 	private int minX;
 	private int minY;
 	private int maxX;
 	private int maxY;
 
 	private int winWidth;
 	private int winHeight;
 	
 	private boolean gameInProgress;
 	private DrawCanvas canvas;
 	
 	
 	
 	public GUI(int width, int height, boolean aiPlayer, int player) {
 		//gameInProgress = false;
 		setBackground(new Color(47, 79, 79));
 		
 		
 		//create menu
 		JMenuBar b;
 		Menu menu = new Menu();
 		b = menu.get_bar();
 		setJMenuBar(b);
 		b.setVisible(true);
 		
//		width = menu.get_col_size();
//		height = menu.get_row_size();
 		aiPlayer = menu.get_aiPlayer();
 		gameInProgress = menu.get_gameStart();
 		game = new Game(width, height, aiPlayer, player);
		this.width = game.getBoard().getWidth();
		this.height = game.getBoard().getHeight();
 			
 		
 		spacing = 80;				// 80 px between board positions
 		radius = spacing / 2 - 8;	// 8 px between game pieces
 		diameter = 2 * radius;
 		
 		minX = spacing;
 		minY = spacing;
 		maxX = width * spacing;
 		maxY = height * spacing;
 		
 		winWidth = maxX + spacing;
 		winHeight = maxY + spacing;
 		
 		initializeGridLines();
 		
 		canvas = new DrawCanvas();
 		canvas.setPreferredSize(new Dimension(winWidth, winHeight));
 		
 		canvas.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (game.isTie() || game.whiteWins() || game.blackWins())
 				{
 					game.reset();
 					setTitle("Fanorona");
 					repaint();
 					return;
 				}
 				
 				int mouseX = e.getX();
 				int mouseY = e.getY();
 
 				int x = (mouseX + radius) / spacing;
 				int y = game.getBoard().getHeight() - ((mouseY + radius) / spacing) + 1;
 
 				Point point = new Point(x, y);
 				if (game.getBoard().isValidPoint(point))
 				{
 					if (! game.update(point))
 					{
 						System.out.println("!!! INVALID MOVE: " + point);
 						System.out.println();
 					}
 				}
 				
 				if (game.isTie())
 				{
 					System.out.println("Tie");
 					setTitle("Tie");
 				}
 				else if (game.whiteWins())
 				{
 					System.out.println("White wins");
 					setTitle("White wins");
 				}
 				else if (game.blackWins())
 				{
 					System.out.println("Black wins");
 					setTitle("Black wins");
 				}
 				
 				repaint();
 			}
 		});
 		
 		Container cp = getContentPane();
 		cp.setLayout(new BorderLayout());
 		cp.add(canvas, BorderLayout.CENTER);
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		pack();
 		setTitle("Fanorona");
 		setVisible(true);
 	}
 	
 	class DrawCanvas extends JPanel {
 		@Override
 		public void paintComponent(Graphics g) {
 			//if(gameInProgress) {
 				super.paintChildren(g);
 				drawGridLines(g);
 				drawGamePieces(g); 
 			//} else {
 			//	drawStartScreen(g);
 			//}
 		}
 	}
 	
 	/*
 	class DrawStartScreen extends JPanel {
 		@Override
 		public void paintComponent(Graphics g) {
 			super.paintChildren(g);
 			drawGridLines(g);
 			drawGamePieces(g);
 		}
 	}
 	*/
 	/*
 	private void changePanel(JPanel panel) {
 		getContentPane().removeAll();
 	    getContentPane().add(panel, BorderLayout.CENTER);
 	    getContentPane().doLayout();
 	    update(getGraphics());
 	}
 	*/
 	private void drawStartScreen(Graphics g) {
 		String s1 = "Welcome to Fanorona";
 		String s2 = "Please use the dropdown menu above to select preferences, then start a new game.";
 		g.setColor(Color.white);
 		g.drawString(s1, 50, winHeight/2);
 		g.drawString(s2, 50, winHeight/2+50);
 		
 	}
 	private void drawGamePieces(Graphics g)
 	{
 		Board board = game.getBoard();
 
 		Graphics2D g2d = (Graphics2D)g;
 		g2d.setStroke(new BasicStroke(2));
 		
 		for (Point point : board)
 		{
 			int type = board.getPoint(point);
 			int x = point.getX() * spacing;
 			int y = winHeight - (point.getY() * spacing);
 
 			x -= radius;
 			y -= radius;
 			
 			switch (type)
 			{
 				case Board.WHITE:
 					g2d.setColor(new Color(240, 255, 255));
 					g2d.fillOval(x, y, diameter, diameter);
 					break;
 				case Board.BLACK:
 					g2d.setColor(Color.BLACK);
 					g2d.fillOval(x, y, diameter, diameter);
 					break;
 				case Board.WHITE_GRAY:
 				case Board.BLACK_GRAY:
 					g2d.setColor(Color.GRAY);
 					g2d.fillOval(x, y, diameter, diameter);
 					break;
 			}
 
 			if (game.getClickable().contains(point))
 			{
 				g.setColor(Color.MAGENTA);
 				g.drawOval(x, y, diameter, diameter);
 			}
 		}
 	}
 
 	private void drawGridLines(Graphics g)
 	{
 //		g.setColor(new Color(221, 218, 236));
 //		g.fillRect(0, 0, winWidth, winHeight);
 		g.setColor(new Color(250, 158, 114));
 		for (Pair pair : gridLines)
 		{
 			Point src = pair.getSrc();
 			Point dest = pair.getDest();
 			g.drawLine(src.getX(), src.getY(), dest.getX(), dest.getY());
 		}
 	}
 
 	private void initializeGridLines()
 	{
 		// horizontal lines
 		for (int i = 1; i <= height; i++)
 		{
 			int y = i * spacing;
 
 			gridLines.add(new Pair(minX, y, maxX, y));
 		}
 
 		// vertical lines
 		for (int i = 1; i <= width; i++)
 		{
 			int x = i * spacing;
 
 			gridLines.add(new Pair(x, minY, x, maxY));
 		}
 
 		// diagonal lines (upper left to lower right)
 		for (int i = 1; i < width; i += 2)
 		{
 			int x1 = i * spacing;
 
 			int x2 = x1;
 			int y2 = minY;
 
 			while (x2 < maxX && y2 < maxY)
 			{
 				x2 += spacing;
 				y2 += spacing;
 			}
 
 			gridLines.add(new Pair(x1, minY, x2, y2));
 		}
 		for (int i = 3; i < height; i += 2)
 		{
 			int y1 = i * spacing;
 
 			int x2 = minX;
 			int y2 = y1;
 
 			while (x2 < maxX && y2 < maxY)
 			{
 				x2 += spacing;
 				y2 += spacing;
 			}
 
 			gridLines.add(new Pair(minX, y1, x2, y2));
 		}
 
 		// diagonal lines (upper right to lower left)
 		for (int i = 3; i < width; i += 2)
 		{
 			int x1 = i * spacing;
 
 			int x2 = x1;
 			int y2 = minY;
 
 			while (x2 > minX && y2 < maxY)
 			{
 				x2 -= spacing;
 				y2 += spacing;
 			}
 
 			gridLines.add(new Pair(x1, minY, x2, y2));
 		}
 
 		for (int i = 1; i < height; i += 2)
 		{
 			int y1 = i * spacing;
 
 			int x2 = maxX;
 			int y2 = y1;
 
 			while (x2 > minX && y2 < maxY)
 			{
 				x2 -= spacing;
 				y2 += spacing;
 			}
 
 			gridLines.add(new Pair(maxX, y1, x2, y2));
 		}
 	}
 	
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run()
 			{
 				int width = 9;
 				int height = 5;
 				boolean aiPlayer = true;
 				int player = Board.WHITE;
 				new GUI(width, height, aiPlayer, player);
 			}
 		});
 	}
 }
