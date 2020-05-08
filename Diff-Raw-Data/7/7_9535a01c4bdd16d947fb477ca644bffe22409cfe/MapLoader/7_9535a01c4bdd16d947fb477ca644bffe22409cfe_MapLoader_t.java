 package nl.sest.gamejam.controller;
 
 import nl.sest.gamejam.model.impl.*;
 
 public class MapLoader {
 
 	public MapLoader() {
 
 	}
 
 	/**
 	 * TODO connect to xml or something
 	 *
 	 * @param model
 	 */
 	public void loadMap(Model model) {
 
 		float gs = 8; // grid square size in meters
 		model.setWorldDimension(20 * gs, 15 * gs);
 		
		// Buildings
 		model.addObstacle(new Obstacle(2 * gs, 2 * gs, "Bank", "N"));
 		model.addObstacle(new Obstacle(9 * gs, 2 * gs, "Bank", "N"));
 		model.addObstacle(new Obstacle(14 * gs, 2 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(19 * gs, 3 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(12 * gs, 4 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(17 * gs, 4 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(5 * gs, 5 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(8 * gs, 5 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(11 * gs, 6 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(15 * gs, 6 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(18 * gs, 6 * gs, "Bank", "N"));
 		model.addObstacle(new Obstacle(2 * gs, 7 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(6 * gs, 7 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(8 * gs, 7 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(10 * gs, 8 * gs, "Bank", "N"));
 		model.addObstacle(new Obstacle(13 * gs, 8 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(16 * gs, 9 * gs, "Bank", "N"));
 		model.addObstacle(new Obstacle(2 * gs, 11 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(8 * gs, 11 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(10 * gs, 11 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(6 * gs, 12 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(12 * gs, 12 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(17 * gs, 12 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(2 * gs, 13 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(9 * gs, 13 * gs, "Bank", "N"));
 		model.addObstacle(new Obstacle(19 * gs, 13 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(5 * gs, 14 * gs, "Normal", "N"));
 		model.addObstacle(new Obstacle(13 * gs, 14 * gs, "Normal", "N"));
 
 		// Trains
 		model.addTrainDestination(new TrainDestination(20 * gs, -3 * gs));
 		model.addTrainDestination(new TrainDestination(-3 * gs, 4 * gs));
 		model.addTrainDestination(new TrainDestination(0 * gs, 18 * gs));
 
 		// POIs
 		model.addPointOfInterest(new PointOfInterest(6 * gs, 2 * gs, 0, 5000));
 		model.addPointOfInterest(new PointOfInterest(6 * gs, 9 * gs, 0, 5000));
 		model.addPointOfInterest(new PointOfInterest(7 * gs, 14 * gs, 0, 5000));
 		model.addPointOfInterest(new PointOfInterest(19 * gs, 10 * gs, 0, 5000));
 		model.addPointOfInterest(new PointOfInterest(12 * gs, 6 * gs, 0, 5000));
 		
 		
 		// Valuable
 		model.addValuable(new Valuable(10.5f * gs, 3.5f * gs, 1000));
 		model.addValuable(new Valuable(19f * gs, 5f * gs, 1000));
 		model.addValuable(new Valuable(7f * gs, 6f * gs, 1000));
 		model.addValuable(new Valuable(17f * gs, 6.5f * gs, 1000));
 		model.addValuable(new Valuable(2f * gs, 7.5f * gs, 1000));
 		model.addValuable(new Valuable(9f * gs, 7.5f * gs, 1000));
 		model.addValuable(new Valuable(18f * gs, 10f * gs, 1000));
 		model.addValuable(new Valuable(8f * gs, 11.5f * gs, 1000));
 		model.addValuable(new Valuable(10.5f * gs, 13f * gs, 1000));
 	}
 
 }
