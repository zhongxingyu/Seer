 package net.infinitycoding.carsim.modules;
 
 import java.util.HashMap;
 
 import javax.swing.ImageIcon;
 
 import net.infinitycoding.carsim.CarSim;
 
 public class Level
 {
 	HashMap<Integer, Street> streets = new HashMap<Integer, Street>();
 	
 	public int carCount  = 0;
 	
 	public int MAX_CARS = 0;
 	public float CAR_RATIO = 0F;
 	public int streetcount = 0;
 	
 	public ImageIcon streetPic;
 	
 	public Level(Street[] streets, int maxCars, float carRatio, String imgName)
 	{
 		int streetNumIndex = 0;
 		streetcount = streets.length;
 		
		this.MAX_CARS = maxCars;
		this.CAR_RATIO = carRatio;
		
 		for (Street elem : streets)
 		{
 			this.streets.put(streetNumIndex, elem);
 			streetNumIndex++;
 		}
 		this.streetPic = new ImageIcon(CarSim.class.getResource("res/" + imgName));
 	}
 }
