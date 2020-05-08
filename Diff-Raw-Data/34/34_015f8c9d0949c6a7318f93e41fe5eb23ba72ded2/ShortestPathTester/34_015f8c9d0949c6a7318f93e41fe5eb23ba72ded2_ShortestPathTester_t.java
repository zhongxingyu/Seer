 package chalmers.dax021308.ecosystem.model.agent;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.util.List;
 
 import chalmers.dax021308.ecosystem.model.agent.AbstractAgent.NodePosition;
 import chalmers.dax021308.ecosystem.model.environment.obstacle.IObstacle;
 import chalmers.dax021308.ecosystem.model.population.IPopulation;
 import chalmers.dax021308.ecosystem.model.util.IShape;
 import chalmers.dax021308.ecosystem.model.util.Position;
 import chalmers.dax021308.ecosystem.model.util.Vector;
 
 public class ShortestPathTester {
 	
 	public ShortestPathTester() {
 		Position start = new Position(1.0, 1.0);
		Position end = new Position(5.0, 5.0);
 		DeerAgent test = new DeerAgent("", new Position(0.0, 0.0), Color.gray, 0, 0, Vector.emptyVector(), 0, 0, 0, true);
 		
 		List<Position> result = test.getShortestPath(start, end);
		System.out.println("Result: " + result);
 		
 	}
 		
 		
 	
 	public static void main(String[] args) {
 		new ShortestPathTester();
 	}
 }
