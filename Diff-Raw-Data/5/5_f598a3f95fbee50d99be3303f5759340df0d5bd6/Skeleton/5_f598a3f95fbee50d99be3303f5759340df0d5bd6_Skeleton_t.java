 ////////// idea - multiple windows - one for options another for the grid itself???
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import java.awt.Rectangle;
 import javax.swing.Timer;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.*;
 import java.awt.Color;
 
 class Surface extends JPanel {
 	
 	int gridWidth;
 	int gridHeight;
 	int gridCellSize = 5;
 	
 	int[][] grid;
 	
 	public Surface (int[][] ingrid){
 		grid = ingrid;
 	}
 	
     private void doDrawing(Graphics g) {
         Graphics2D g2d = (Graphics2D) g;
         drawGrid(g2d);
         colorGrid(g2d);
     }
     
 	public void setGrid (int[][] inGrid){
 		grid = inGrid;
 		gridWidth = grid.length;
 		gridHeight = grid[0].length;
 		//System.out.println(gridWidth+" "+gridHeight);
 	};
 	
     private void drawGrid(Graphics2D g2d){
     	for(int i = 0; i < gridWidth; i++){
     		g2d.drawLine(i*gridCellSize,  0, i*gridCellSize, gridCellSize*gridHeight);
     	}
     	for(int i = 0; i < gridHeight; i++){
     		g2d.drawLine(0, i*gridCellSize, gridCellSize*gridWidth, i*gridCellSize);   			
     	}
     }
     
     private void colorGrid (Graphics2D g2d){
     	for (int i = 0; i<grid.length; i++){
 			for(int j = 0; j<grid[i].length; j++){
 				if(grid[i][j] == 1){
 					int xpos = i*gridCellSize;
 					int ypos = j*gridCellSize;
 					g2d.fill(new Rectangle(xpos, ypos, gridCellSize, gridCellSize));
 				}
 			}
 		}
     }
     
 
     @Override
 
 
     public void paintComponent(Graphics g) {
 
         super.paintComponent(g);
         doDrawing(g);
     }
 }
 
 
 /*class GameOfLife extends CellularAutomata {
 	
 	int[][] currentGrid;
 	int width;
 	int height;
 	Surface surface;
 	
 	public int getNeighbour (int x, int y, int ind){
 		//System.out.println(x+"  "+y+"  "+ind);
 		if(x >= width || x <= 0 || y >= height || y <= 0){
 			return 0;
 		}
 	
 		try {
 			if(ind <= 2){
 				return currentGrid[x-1+ind][y-1];
 			}
 			if(ind == 3){
 				return currentGrid[x-1][y];
 			}
 			if(ind == 4){
 				return currentGrid[x+1][y];
 			}
 			if(ind <= 7){
 				return currentGrid[x-6+ind][y+1];
 			}
 		} catch (Exception e){
 			/*System.out.println("butts");
 			System.out.println(x+"  "+y+"  "+ind);
 			
 			
 			
 			
 			
 
 
 			
 									FIX THIS FIRST THING DICKHEAD
 			
 			
 			
 			
 			
 			
 			
 			
 			return 0;
 		}
 			
 		System.out.println("ERR: Get neighbo;ur function called out of bounds index");
 		return -1;
 	}
 	
 	public void setSurface(Surface inSurface){
 		surface = inSurface;
 	}
 	
 	
 	public GameOfLife (int inwidth, int inheight){
 		width = inwidth;
 		height = inheight;
 		currentGrid = new int[width][height];
 		for(int i = 0; i < width; i++){
 			for(int j = 0; j<height; j++){
 				
 				if(Math.random() < 0.3f){
 					currentGrid[i][j] = 1;
 				}else{
 					currentGrid[i][j] = 0;
 				}
 				
 				//System.out.println(i+"  "+j+"  "+currentGrid[i][j]);
 			}
 		}
 		
 		System.out.println(this.getCell(10, 10));
 		
 		///
 				
 	}
 	
 	public int[][] getGrid (){
 		return currentGrid;
 	}
 	
 	public int[][] newGeneration (int[][] grid){
 		int[][] newGrid = new int[width][height];
 		for(int x = 0; x < width; x++){
 			for(int y = 0; y<height; y++){
 				
 				int neighbourCount = 0;
 				for(int i = 0; i<=7; i++){
 					neighbourCount += getNeighbour(x, y, i);
 				}
 				
 				if(currentGrid[x][y] == 1){
 					if(neighbourCount < 2 || neighbourCount > 3){
 						newGrid[x][y] = 0;
 					}else {
 						newGrid[x][y] = 1;
 					}
 				}
 				
 				else{
 					if(neighbourCount == 3){
 						newGrid[x][y] = 1;
 					}
 				}
 			}
 		}
 		currentGrid = newGrid;
 		return newGrid;
 	}
 	
 	public int rules (int x, int y){
 		return 5;
 	}
 	
 }*/
 
 
 class GOLabs extends CellularAutomata {
 	public int getNeighbour (int x, int y, int ind){
 		if(x >= width || x <= 0 || y >= height || y <= 0){
 			return 0;
 		}
 		try {
 			if(ind <= 2){
 				return currentGrid[x-1+ind][y-1];
 			}
 			if(ind == 3){
 				return currentGrid[x-1][y];
 			}
 			if(ind == 4){
 				return currentGrid[x+1][y];
 			}
 			if(ind <= 7){
 				return currentGrid[x-6+ind][y+1];
 			}
 		} catch (Exception e){
 			/*System.out.println("butts");
 			System.out.println(x+"  "+y+"  "+ind);
 			
 			STILL NEEDS TO BE FIXED
 			
 			*/
 			return 0;
 		}
 		System.out.println("ERR: Get neighbour function called out of bounds index");
 		return -1;
 	}
 	
	public GOLabs (int[][] grid){
		super(grid);
	}
 	
 	public int rules (int x, int y){
 		int neighbourCount = 0;
 		for(int i = 0; i<=7; i++){
 			neighbourCount += getNeighbour(x, y, i);
 		}
 		
 		if(currentGrid[x][y] == 1){
 			if(neighbourCount < 2 || neighbourCount > 3){
 				return 0;
 			}else {
 				return 1;
 			}
 		}
 	
 		else{
 			if(neighbourCount == 3){
 				return 1;
 			}
 		}
		return 0;
 	}
 	
 }
 
 
 public class Skeleton extends JFrame {
 	
 	Surface surface;
 	
 	int width = 200;
 	int height = 150;
 	
 	GameOfLife GOL;
 	
 	
     public Skeleton() {
 		
 		GOL = new GOLabs(makeRandomGrid(width, height));
         initUI();
 		
 		
 		/*JFrame test = new JFrame("Sup");
 		test.setSize(100, 400);
 		test.setVisible(true);
 		test.add(new JButton("HELLO FUCKERS"));*/
 		
 		JFrame toolbox = new Toolbox();
 		
 		runCA();
 	}
 
 	private void runCA(){
 		int delay = 60; //milliseconds
 		ActionListener taskPerformer = new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				GOL.newGeneration(GOL.getGrid());
 				renderGrid(GOL.getGrid());
 			}
 		};
 		new Timer(delay, taskPerformer).start();
 	}
 	
 	private void initUI() {
 
 		setTitle("waddup");
 		
 		surface = new Surface(GOL.getGrid());
 		GOL.setSurface(surface);
 		
 		add(surface);
 		
 		setSize(1000, 800);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLocationRelativeTo(null);
 	}
 	
 	private void renderGrid (int [][] grid){
 		surface.setGrid(grid);
 		surface.repaint();
 	}
 	
 	public static void main(String[] args) {
 
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 Skeleton sk = new Skeleton();
                 sk.setVisible(true);
             }
         });
     }
     
     //
     
     public int[][] makeRandomGrid (int w, int h){
     	rGrid = new int[w][h];
 		for(int i = 0; i < w; i++){
 			for(int j = 0; j<h; j++){
 				
 				if(Math.random() < 0.3f){
 					rGrid[i][j] = 1;
 				}else{
 					rGrid[i][j] = 0;
 				}
 			}
 		}
 		return rGrid;
 	}
     
 }
 
 
 class Toolbox extends JFrame {
 	public Toolbox (){
 		setVisible(true);
 		setSize(100, 400);
 		setLocation(200, 300);
 		
 		ToolboxPanel tbp = new ToolboxPanel();
 		
 		add(tbp);
 	}
 }
 
 class ToolboxPanel extends JPanel implements ActionListener {
 	JButton button;
 	public ToolboxPanel () {
 		button = new JButton(" HEELOOO");
 		add(button);
 		button.setVisible(true);
 		button.addActionListener(this);
 	}
 	
 	
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == button){
 			System.out.println("btttsts");
 		}
 	}
 }
 
 
 
 
 
 
 
 
