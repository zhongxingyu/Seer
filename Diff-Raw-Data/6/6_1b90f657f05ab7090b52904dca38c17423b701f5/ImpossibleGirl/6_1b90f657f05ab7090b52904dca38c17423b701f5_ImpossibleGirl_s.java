 package mosquito.g0;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Line2D.Double;
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import mosquito.sim.Collector;
 import mosquito.sim.Light;
 import mosquito.sim.MoveableLight;
 
 
 
 public class ImpossibleGirl extends mosquito.sim.Player {
 	class Section {
 		  int[] boolCombo;
 		  ArrayList<Integer> xPoints;
 		  ArrayList<Integer> yPoints;
 		  int maxX;
 		  int minX;
 		  int maxY;
 		  int minY;
 		  int midX;
 		  int midY;
 		  
 		  boolean visited = false;
 		  
 		  public Section() {
 			  boolCombo = new int[walls.size()];
 			  xPoints = new ArrayList<Integer>();
 			  yPoints = new ArrayList<Integer>();
 		  }
 		  //boolean through; //describes if there are multiple entrances into the section;
 		  void printDetails() {
 			  log.debug("boolCombo: " + Arrays.toString(boolCombo));
 			  //  log.trace("Area: " + this.area + ", endpoints: " + Arrays.toString(this.endpoints) + ", through? " + through);
 		  }
 		  
 		  void setMidpoints() {
 			  int sumX = 0;
 			  int sumY = 0;
 			  int len = xPoints.size();
 			  for (int i = 0; i < len; i++) {
 				  sumX += xPoints.get(i);
 				  sumY += yPoints.get(i);
 			  }
 			  this.midX = sumX/len;
 			  this.midY = sumY/len;
 		  }
 	}
 
 	private int numLights;
 	private Point2D.Double lastLight;
 	private Logger log = Logger.getLogger(this.getClass()); // for logging
 	
 	@Override
 	public String getName() {
 		return "I section things";
 	}
 	
 	private Set<Light> lights;
 	private Set<Line2D> walls;
 	
 	private Set<MoveableLight> mlights;
 	private AreaMap map;
 	private AStar astar;
 	private int move;
 	
 	private int pointLineRelationships[][][];
 	private int numberOfSections;
 	private ArrayList<Section> sections = new ArrayList<Section>();
 	
 	/*
 	 * This is called when a new game starts. It is passed the set
 	 * of lines that comprise the different walls, as well as the 
 	 * maximum number of lights you are allowed to use.
 	 * 
 	 * The return value is a set of lines that you would like to have drawn on the screen.
 	 * These lines don't actually affect gameplay, they're just there so you can have some
 	 * visual clue as to what's happening in the simulation.
 	 */
 	@Override
 	public ArrayList<Line2D> startNewGame(Set<Line2D> walls, int numLights) {
 		this.numLights = numLights;
 		this.walls = walls;
 		pointLineRelationships = new int[100][100][walls.size()];
 		ArrayList<Line2D> lines = new ArrayList<Line2D>();
 		sectioningAlgorithm();
 		identifySections(pointLineRelationships);
 		for (int i=0; i<numberOfSections; i++) {
 			
 			sections.get(i).setMidpoints();
 			Point2D ul = new Point2D.Double(sections.get(i).midX-1, sections.get(i).midY-1);
 			Point2D ll = new Point2D.Double(sections.get(i).midX-1, sections.get(i).midY+1);
 			Point2D ur = new Point2D.Double(sections.get(i).midX+1, sections.get(i).midY-1);
 			Point2D lr = new Point2D.Double(sections.get(i).midX+1, sections.get(i).midY+1);
 			
 			Line2D c1 = new Line2D.Double(ul, lr);
 			Line2D c2 = new Line2D.Double(ll, ur);
 			
 			
 			lines.add(c1);
 			lines.add(c2);
 			
 			System.err.println("Section " + i + " midpoint: (" + sections.get(i).midX +
 				" , " + sections.get(i).midY + ")");
 		}
 		
 		return lines;
 	}
 
 
 	/*
 	 * This is used to determine the initial placement of the lights.
 	 * It is called after startNewGame.
 	 * The board tells you where the mosquitoes are: board[x][y] tells you the
 	 * number of mosquitoes at coordinate (x, y)
 	 */
 	public Set<Light> getLights(int[][] board) {
 
 		// initializing AStar
 		FHeuristic fh = new FHeuristic();
 		
 		lights = new HashSet<Light>();
 		lastLight = new Point2D.Double(10, 10);
 		mlights = new HashSet<MoveableLight>();
 		for(int a = 0; a<numLights; a++)
 		{
 			AreaMap cleanMap = new AreaMap(100,100);
 		    for(int i = 0; i < board.length; i++) {
 		        for(int j = 0; j < board[0].length; j++) {
 		            for(Line2D wall: walls) {
 		                if(wall.ptSegDist(i, j) < 2.0) {
 		                	cleanMap.getNodes().get(i).get(j).isObstacle = true; // nay on the current node
 		                }
 		            }
 		        }
 		    }
 			astar = new AStar(cleanMap, fh);
 			
 			int midX = 10;
 			int midY = 10;
 			if (a < sections.size()) {
 				midX = sections.get(a).midX;
 				midY = sections.get(a).midY;
 			}
 			
 			MoveableLight l = new MoveableLight(midX, midY, true);
 
 			l.turnOff();
 			lights.add(l);
 			
 			astar.calcShortestPath(midX, midY, 50, 50);
 			
 			log.error("Currently the midX and midY are : " + midX + ", " + midY);
 			
 			l.shortestPath = astar.shortestPath;
 			mlights.add(l);
 		}
 	    
 	    return lights;
 	}
 	
 	/*
 	 * This is called at the beginning of each step (before the mosquitoes have moved)
 	 * If your Set contains additional lights, an error will occur. 
 	 * Also, if a light moves more than one space in any direction, an error will occur.
 	 * The board tells you where the mosquitoes are: board[x][y] tells you the
 	 * number of mosquitoes at coordinate (x, y)
 	 */
 	public Set<Light> updateLights(int[][] board) {
 //		AStar currAStar;
 		
 		for (MoveableLight ml : mlights) {
 //			MoveableLight ml = (MoveableLight)l;
 //			currAStar = ml.astar;
 			Path shortest = ml.shortestPath;
 			if (move >= shortest.getLength())
 				continue;
 				
 			ml.moveTo(shortest.getX(move), shortest.getY(move));
 			
 		}
 		this.move++;
 		return lights;
 	}
 
 	/*
 	 * Currently this is only called once (after getLights), so you cannot
 	 * move the Collector.
 	 */
 	@Override
 	public Collector getCollector() {
 		// this one just places a collector next to the last light that was added
 		Collector c = new Collector(50,50);
 		return c;
 	}
 	
 	public void sectioningAlgorithm() {
 		Line2D[] wallArray;
 		  for (int x=0; x<100; x++) {	//could probably also be x+=2, depending on how well we want our sections to be defined
 		  	for (int y=0; y<100; y++) {
 		  		for (int i = 0; i < walls.size(); i++) {
 		  			wallArray = walls.toArray(new Line2D[walls.size()]);
 		  			pointLineRelationships[x][y][i] = comparePointToLine(x, y, wallArray[i]);	
 		  		}
 		  	}
 		  }
 	}
 	
 	public int comparePointToLine(int x, int y, Line2D line) {
 		boolean lineIsVertical = (line.getX1() == line.getX2());
 		
	  if (lineIsVertical) {
	    //for vertical lines, assume that points to the left are lesser and points to the right are greater
			if (y < line.getY1()) return 0;
 	    else return 1;
 		} else {
 	    //find the slope and intercept of the line given
 	    double slope = (line.getY2()-line.getY1()) / (line.getX2()-line.getX1());
 	    double yIntercept = line.getY1() - (slope*line.getX1());
 	    //identify if point is above or below line
 	    if (y > slope * x + yIntercept) return 1;
 	    else return 0;
 		}
 	}
 	
 	void identifySections(int[][][] pointLineRelationships) {
 		numberOfSections=0;
 		for (int x=0; x<100; x++) {
 			for (int y=0; y<100; y++) {
 				int[] boolCombo = pointLineRelationships[x][y];
 		    boolean hasComboBeenSeen = false;
 		    for (int i=0; i<numberOfSections; i++) {
 		      if (Arrays.equals(boolCombo,sections.get(i).boolCombo)) {
 		        hasComboBeenSeen = true;
 		        sections.get(i).xPoints.add(x);
 		        sections.get(i).yPoints.add(y);
 		        if (x > sections.get(i).maxX)
 		        	sections.get(i).maxX = x;
 		        else if (x < sections.get(i).minX)
 		        	sections.get(i).minX = x;
 		        if (y > sections.get(i).maxY)
 		        	sections.get(i).maxY = y;
 		        else if (y < sections.get(i).minY)
 		        	sections.get(i).minY = y;
 		        break;
 		      } 
 		    } 
 		    if (!hasComboBeenSeen) {
 		        Section newSection = new Section();
 		        newSection.boolCombo=pointLineRelationships[x][y];
 		        newSection.xPoints.add(x);
 		        newSection.yPoints.add(y);
 		        newSection.maxX = x;
 		        newSection.minX = x;
 		        newSection.maxY = y;
 		        newSection.minY = y;
 		        sections.add(newSection);
 		        numberOfSections++;
 		    } 
 		  }
 		}
 	}
 
 }
