 package asrsController;
 
 import order.Location;
 
 public class WarehouseArduino implements Warehouse{
 
 	@Override
 	public void retrieveProduct(Location location, int robotId) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void bringToBinPacker(int robotId) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void moveToStart(int robotId) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
	public Location getStartLocation(int r) {
		return new Location(0,0);
 	}
 
 	@Override
	public Integer getNumberOfRobots() {
		return 1;
 	}
 
 }
