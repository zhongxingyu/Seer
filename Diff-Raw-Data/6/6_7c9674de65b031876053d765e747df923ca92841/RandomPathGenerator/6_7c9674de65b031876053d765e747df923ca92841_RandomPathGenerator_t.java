 package pl.edu.agh.student.pathfinding;
 
 import java.awt.Point;
import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 import pl.edu.agh.student.pathfinding.map.IMap;
 
 public class RandomPathGenerator implements IPathGenerator{
 	
 	private IMap map;
 	
 	private boolean[][] visitationTable;
 	
 	public RandomPathGenerator(IMap map){
 		this.map = map;
 		visitationTable = new boolean[map.getWidth()][map.getHeight()];
 		for(boolean[] verse : visitationTable){
 			for(boolean visitation : verse){
 				visitation = false;
 			}
 		}
 	}
 
 	@Override
 	public Solution getSolution() {
		for (boolean[] line : visitationTable) {
            Arrays.fill(line, false);
        }
		
 		Solution newSolution = new Solution(map);
 		Point current = map.getStartingPoint();
 		newSolution.addStep(current);
 		visitationTable[current.x][current.y] = true;
 		while(!current.equals(map.getFinishPoint())){
 			if(!canGoAnywhere(current)){
 				newSolution.removeStep(current);
 				current = newSolution.getLast();
 				if(current == null) break;
 			} else {
 				current = nextStep(current);
 				newSolution.addStep(current);
 			}
 		}
 	//	print(newSolution);
 		
 		// calculate f
 		newSolution.f();
 		return newSolution;
 	}
 	
 	private Point nextStep(Point currentStep){
 		Point nextStep = null;
 		Random rand = new Random();
 		boolean isValid = false;
 		while(!isValid){
 			int whereToGo = rand.nextInt(8);
 			switch(whereToGo){
 				case 0:
 					nextStep = new Point(currentStep.x - 1, currentStep.y - 1);
 					break;
 				case 1:
 					nextStep = new Point(currentStep.x - 1, currentStep.y);
 					break;
 				case 2:
 					nextStep = new Point(currentStep.x - 1, currentStep.y + 1);
 					break;
 				case 3:
 					nextStep = new Point(currentStep.x, currentStep.y - 1);
 					break;
 				case 4:
 					nextStep = new Point(currentStep.x, currentStep.y + 1);
 					break;
 				case 5:
 					nextStep = new Point(currentStep.x + 1, currentStep.y - 1);
 					break;
 				case 6:
 					nextStep = new Point(currentStep.x + 1, currentStep.y);
 					break;
 				case 7:
 					nextStep = new Point(currentStep.x + 1, currentStep.y + 1);
 					break;
 				default:
 					throw new IllegalStateException("Random step generator has done some crazy shit");
 			}
 			if(nextStep.x >= 0 && nextStep.x < map.getWidth() && nextStep.y >= 0 && nextStep.y < map.getHeight() && visitationTable[nextStep.x][nextStep.y] == false && map.isAccessible(nextStep)){
 				isValid = true;
 				visitationTable[nextStep.x][nextStep.y] = true;
 			}
 		}
 		
 		return nextStep;
 	}
 	
 	private boolean canGoAnywhere (Point currentPoint){
 		return !((currentPoint.x > 0 && currentPoint.y > 0 ? (visitationTable[currentPoint.x - 1][currentPoint.y - 1] || !map.isAccessible(new Point(currentPoint.x - 1, currentPoint.y - 1))) : true) &&
 				 (currentPoint.x > 0 ? (visitationTable[currentPoint.x - 1][currentPoint.y] || !map.isAccessible(new Point(currentPoint.x - 1, currentPoint.y))) : true) &&
 				 (currentPoint.x > 0 && currentPoint.y < map.getHeight()-1 ? (visitationTable[currentPoint.x - 1][currentPoint.y + 1] || !map.isAccessible(new Point(currentPoint.x - 1, currentPoint.y + 1))) : true) &&
 				 (currentPoint.y > 0 ? (visitationTable[currentPoint.x][currentPoint.y - 1] || !map.isAccessible(new Point(currentPoint.x, currentPoint.y - 1))) : true) &&
 				 (currentPoint.y < map.getHeight()-1 ? (visitationTable[currentPoint.x][currentPoint.y + 1] || !map.isAccessible(new Point(currentPoint.x, currentPoint.y + 1))) : true) &&
 				 (currentPoint.x < map.getWidth()-1 && currentPoint.y > 0 ? (visitationTable[currentPoint.x + 1][currentPoint.y - 1] || !map.isAccessible(new Point(currentPoint.x + 1, currentPoint.y - 1))) : true) &&
 				 (currentPoint.x < map.getWidth()-1 ? (visitationTable[currentPoint.x + 1][currentPoint.y] || !map.isAccessible(new Point(currentPoint.x + 1, currentPoint.y))) : true) &&
 				 (currentPoint.x < map.getWidth()-1 && currentPoint.y < map.getHeight()-1 ? (visitationTable[currentPoint.x + 1][currentPoint.y + 1] || !map.isAccessible(new Point(currentPoint.x + 1, currentPoint.y + 1))) : true)
 				);
 	}
 	
 	private void print(Solution newSolution){
 		List<Point> lista = newSolution.getSteps();
 		char[][] tablica = new char[map.getWidth()][map.getHeight()];
 		for(char[] wiersz : tablica){
 			for(char znak : wiersz){
 				znak = '*';
 			}
 		}
 		for(Point p : lista){
 			tablica[p.x][p.y]= 'x'; 
 		}
 		for(char[] wiersz : tablica){
 			for(char znak : wiersz){
 				System.out.print(znak);
 			}
 			System.out.println();
 		}
 	}
 
 }
