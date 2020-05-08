 package net.gerritk.fdsim.entities.vehicles;
 
 import net.gerritk.fdsim.Playground;
 import net.gerritk.fdsim.entities.SquadVehicle;
 import net.gerritk.fdsim.lights.Bluelight;
 import net.gerritk.fdsim.lights.Light;
 import net.gerritk.fdsim.resource.SimImage;
 
 public class MTF extends SquadVehicle {	
 	public MTF(String name, int x, int y,Playground playground) {
 		super(name, x, y, SimImage.VEH_MTF, playground);
 		
 		Light l;
 		
		l = new Light(4, -8, SimImage.LIGHT_1, Light.HEADLIGHT, this);
 		addLight(l);
 		
		l = new Light(33, -8, SimImage.LIGHT_1, Light.HEADLIGHT, this);
 		addLight(l);
 		
 		// Bluelight
 		Bluelight bluelight;
 		
 		bluelight = new Bluelight(6, 21, SimImage.LIGHT_BLUE_1, 10, 50, 3, this);
 		addLight(bluelight);
 		
 		bluelight = new Bluelight(31, 21, SimImage.LIGHT_BLUE_1, 8, 51, 1, this);
 		addLight(bluelight);
 		
 		bluelight = new Bluelight(8, 56, SimImage.LIGHT_BLUE_1, 9, 34, 2, this);
 		addLight(bluelight);
 	}
 }
