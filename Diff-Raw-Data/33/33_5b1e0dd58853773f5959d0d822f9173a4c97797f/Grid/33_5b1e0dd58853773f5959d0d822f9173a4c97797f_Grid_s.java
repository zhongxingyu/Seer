 package backend;
 
 import backend.cell.Cell;
 import backend.element.Candy;
 import backend.element.CandyColor;
 import backend.element.Element;
 import backend.element.FoodType;
 import backend.move.Move;
 import backend.move.MoveMaker;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public abstract class Grid {
 	
 	public static final int SIZE = 9;
 	public static final int DELAY_MS = 50;
 	
 	private Cell[][] g = new Cell[SIZE][SIZE];
 	private Map<Cell, Point> gMap = new HashMap<Cell, Point>();
 	private GameState state;
 	private List<GameListener> listeners = new ArrayList<GameListener>();
 	private MoveMaker moveMaker;
 	private FigureDetector figureDetector;
 	
 	protected abstract GameState newState();
 	protected abstract void fillCells();
 	
 	protected Cell[][] g() {
 		return g;
 	}
 	
 	public void setCell(int i, int j, Cell cell) {
 		Cell old = g[i][j];
 		gMap.remove(old);
 		g[i][j] = cell;
 		gMap.put(g[i][j], new Point(i,j));
 	}
 	
 	protected GameState state(){
 		return state;
 	}
 	
 	
 	public void initialize() {
 		moveMaker = new MoveMaker(this);
 		figureDetector = new FigureDetector(this);
 		fillGrid();
 		fillCells();
 		fallElements();	
 	}	
 
     protected void fillGrid(){
         for (int row = 0; row < SIZE; row++) {
             for (int col = 0; col < SIZE; col++) {
                 g[row][col] = new Cell(this);
                 gMap.put(g[row][col], new Point(row,col));
             }
         }
     }
 
 	public Element get(int i, int j) {
 		return g[i][j].getContent();
 	}
 	
 	public Cell getCell(int i, int j) {
 		return g[i][j];
 	}
 
 	
 	public void fallElements() {
 		int row = SIZE - 1;
 		while (row >= 0) {
 			int col = 0;
 			while (col < SIZE) {
 			    if(g[row][col].fallUpperContent()){
                     row = SIZE;
                     col = -1;
                     break;
                }  // TODO parte de abajo era como estaba originalmente, cambiado para tratar de solucionar bug con Food, de por si fall
                   // fallUpperContent() ya contiene un isEmpty() dentro suyo...
			/*	if (g[row][col].isEmpty() || ) {
					if (g[row][col].fallUpperContent()) {
						row = SIZE;
						col = -1;
						break;
					}
				}*/
 				col++;
			}	
 			row--;
 		}
 	}
 	
 	public void clearContent(int i, int j) {
 		g[i][j].clearContent();
 	}
 	
 	public void setContent(int i, int j, Element e) {
 		g[i][j].setContent(e);
 	}
 	
 	public boolean tryMove(int i1, int j1, int i2, int j2) {
 		Move move = moveMaker.getMove(i1, j1, i2, j2);
 		if(move == null){
 			return false;
 		}
 		swapContent(i1, j1, i2, j2);
 		if (move.isValid()) {
 			move.removeElements();
 			fallElements();
 			return true;
 		} else {
 			swapContent(i1, j1, i2, j2);
 			return false;
 		}
 	}	
 	
 	public Figure tryRemove(Cell cell) {
 		if (gMap.containsKey(cell)) {
 			Point p = gMap.get(cell);
 			Figure f = figureDetector.checkFigure(p.x, p.y);
 			if (f != null) {
 				removeFigure(p.x, p.y, f);
 			}
 			return f;
 		}
 		return null;
 	}
 	
 	private void removeFigure(int i, int j, Figure f) {
 		CandyColor color = ((Candy)get(i, j)).getColor();
 		if (f.hasReplacement()) {
 			setContent(i, j, f.generateReplacement(color));
 		} else {
 			clearContent(i, j);
 		}
 		for (Point p: f.getPoints()) {
 			clearContent(i + p.x, j + p.y);
 		}
 	}
 
 	
 	public void swapContent(int i1, int j1, int i2, int j2) {
 		Element e = g[i1][j1].getContent();
 		g[i1][j1].setContent(g[i2][j2].getContent());
 		g[i2][j2].setContent(e);
 		wasUpdated();
 	}
 	
 	public GameState createState() {
 		this.state = newState();
 		return this.state;
 	}
 	
 	public void addListener(GameListener listener) {
 		listeners.add(listener);
 	}
 
 	
 	public void wasUpdated(){
 		if (listeners.size() > 0) {
 			for (GameListener gl: listeners) {
 				gl.gridUpdated();
 			}
 			delay(DELAY_MS);
 		}
 	}
 	
 	public void cellExplosion(Element e) {
 		for (GameListener gl: listeners) {
 			gl.cellExplosion(e);
 		}
 	}
 	
 	public void delay(int ms){
 		try {
 			  Thread.sleep(ms);
 		} catch (InterruptedException ie) {
 		}
 	}
 	
 	public Map<Cell, Point> getMap(){
 		return this.gMap;
 	}
 	
 	public void displayOnGrid(int i , int j, Element t)/*
 	este metodo me deja porner los elementos en la posicion deseada y se encarga de avisar a las celdas vecinas*/
 	{
 		g()[i][j].setContent(t);
 		g()[i][j].refreshPostitions();
 }
 }
