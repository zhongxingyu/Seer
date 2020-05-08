 package br.com.mibsim.building.basement;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import br.com.etyllica.linear.PointInt2D;
 import br.com.etyllica.util.kdtree.KDTree;
 import br.com.etyllica.util.kdtree.KeyDuplicateException;
 import br.com.etyllica.util.kdtree.KeySizeException;
 import br.com.mibsim.building.Building;
 import br.com.mibsim.model.fountain.Fountain;
 import br.com.mibsim.planning.PlanningAction;
 import br.com.mibsim.planning.PlanningTask;
 
 public class Basement extends Building {
 
 	private int perimeter = 1;
 	
 	private int currentIndex = 0;
 	
 	private boolean[] knownSectors = new boolean[8];
 	
 	private Set<Fountain> fountainsSet = new HashSet<Fountain>();
 	private KDTree<Fountain> fountains = new KDTree<Fountain>(2);
 	
 	public Basement(int x, int y) {
 		super(x, y);
 	}
 	
 	public Basement(int x, int y, String path) {
 		super(x, y, path);
 	}
 
 	public KDTree<Fountain> getFountains() {
 		return fountains;
 	}
 
 	public PlanningTask askForDesignation(PlanningTask report) {
 		
 		verifyReport(report);
 		
 		PointInt2D target = nextTarget(currentIndex);
 		
 		PlanningTask task = new PlanningTask(PlanningAction.EXPLORE, target, currentIndex);
 				
 		currentIndex++;
 		currentIndex %= perimeter * 8;
 		
 		return task;
 	}
 	
 	private void verifyReport(PlanningTask report) {
 		
 		if(report == null) {
 			return;
 		}
 		
 		if(report.isCompleted() && report.getReference() != PlanningTask.EMPTY_REFERENCE) {
 			
 			int sector = report.getReference();
 			
 			knownSectors[sector] = true;
 			
 			boolean allExplored = true;
 			
 			for(int i = 0; i < knownSectors.length; i++) {
 				if(!knownSectors[i]) {
 					allExplored = false;
 				}
 			}
 			
 			if(allExplored) {
 				perimeter++;
 				currentIndex = 0;
 				
 				knownSectors = new boolean[perimeter * 8];
 			}
 		}		
 		
 	}
 	
 	public PointInt2D nextTarget(int index) {
 		
 		final int LINE_WIDTH = perimeter*2+1;
 		
 		final int SECTOR_WIDTH = 64;
 		final int SECTOR_HEIGHT = 64;
 		
		int x = getCenter().getX();
		int y = getCenter().getY();
 		
 		final int C2 = LINE_WIDTH*2-1;//4
 		final int C3 = LINE_WIDTH*3-perimeter*2;//6
 				
 		if(index < LINE_WIDTH) {
 			x += - SECTOR_WIDTH * perimeter + SECTOR_WIDTH * index;
 			y += - SECTOR_HEIGHT * perimeter; 
 		} else if(index < C2) {
 			x += SECTOR_WIDTH * perimeter;
 			y += - SECTOR_HEIGHT * perimeter + SECTOR_HEIGHT * (index-LINE_WIDTH+1);
 		} else if(index < C3) {
 			x += SECTOR_WIDTH * perimeter - SECTOR_WIDTH * (index-C3+3);
 			y += SECTOR_HEIGHT * perimeter; 
 		} else {
 			x += - SECTOR_WIDTH * perimeter;
 			y += - SECTOR_HEIGHT * perimeter + (perimeter*8-index) * SECTOR_HEIGHT;
 		}
 		
 		PointInt2D target = new PointInt2D(x, y);
 				
 		return target;
 	}
 	
 	public PlanningTask reportToBasement(PlanningTask explore) {
 		
 		return new PlanningTask(PlanningAction.REPORT, getCenter(), explore.getReference());
 	}
 
 	public void reportFountain(Fountain found) {
 		if(fountainsSet.contains(found))
 			return;
 		
 		addFountain(found);
 	}
 	
 	private void addFountain(Fountain found) {
 		fountainsSet.add(found);		
 		
 		try {
 			double[] key = new double[2];
 			key[0] = found.getX();
 			key[1] = found.getY();
 			
 			fountains.insert(key, found);
 		} catch (KeySizeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (KeyDuplicateException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public Set<Fountain> getFountainsSet() {
 		return fountainsSet;
 	}
 
 }
