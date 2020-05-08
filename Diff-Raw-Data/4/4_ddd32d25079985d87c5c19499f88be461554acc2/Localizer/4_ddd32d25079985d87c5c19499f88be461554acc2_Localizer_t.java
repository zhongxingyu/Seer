 package program.localization;
 
 
 public class Localizer {
 	private final double pSense = 0.8;// The probability that the color sensed is correct
 	private final double pMoveExact = 0.7;// The probability that the movement was correctly executed
 	private final double pMoveFail = 0.15;// The probability that the robot did not move
 	private final double pMoveOvershoot = 0.15;// The probability that the robot moved more than predicted
 	private LinearColorMap map;// The map of all possible colors on the route
 	private double[] grid;// The map of probable locations
 	private double step;
 	
 	public Localizer() {
 		
 	}
 	
 	public void reset(LinearColorMap map) {
 		step = 0;
 		this.map = map;
 		grid = new double[map.getLength()];
 		for(int i=0;i<map.getLength();i++) {
 			grid[i] = 1.0 / map.getLength();
 		}
 	}
 	
 	public void sense(Color color) {
 		for(int x=0;x<map.getLength();x++) {
 			if(map.isColorAt(color, x))	grid[x] *= pSense;// pSense is the probability that the sensor is right
 			else grid[x] *= 1 - pSense; // 1-pSense is the probability that the sensor is wrong						
 		}
 		normalize();
 	}
 	
 	public void move(double step) {
 		int delta = (int) step;// The number of grid spaces moved
		step += step % 1;
		
 		// Save the current contents of grid to oldGrid
 		double[] oldGrid = new double[map.getLength()];
 		for(int i=0;i<grid.length;i++) {
 			oldGrid[i] = grid[i];
 		}
 		
 		for(int i=0;i<oldGrid.length;i++) {
 			double p = 0;
 			if(i - delta >= 0) {
 				p += oldGrid[i - delta] * pMoveExact;// 
 			}
 			if(i - delta - 1 >= 0) {
 				p += oldGrid[i - delta - (delta / Math.abs(delta))] * pMoveOvershoot;
 			}
 			p += oldGrid[i] * pMoveFail;
 			grid[i] = p;
 		}
 		normalize();
 	}
 	
 	private void normalize() {
 		double sum = 0;
 		for(int i=0;i<grid.length;i++) {
 			sum += grid[i];
 		}
 		for(int i=0;i<grid.length;i++) {
 			grid[i] /= sum;
 		}
 	}
 	
 
 }
