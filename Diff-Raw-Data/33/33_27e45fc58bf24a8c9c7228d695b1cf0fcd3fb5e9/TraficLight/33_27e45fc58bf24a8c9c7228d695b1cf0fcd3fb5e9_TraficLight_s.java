 package frans.traficLight;
 
 import enviroment.Crossing;
 
 public class TraficLight {
 
 	private Crossing crossing;
 	private int countDown = 0;
 	
 	public TraficLight(Crossing crossing) {
 		this.crossing = crossing;
 	}
 
 	public void Execute(){
 		
 		if ((crossing.getEastSensor().isCarDetected()) || (crossing.getWestSensor().isCarDetected()) ){
 			crossing.getEastLight().setGreen();
 			crossing.getWestLight().setGreen();
 			crossing.getNorthLight().setRed();
 			crossing.getSouthLight().setRed();
			countdown++;
 		} else if ((crossing.getNorthSensor().isCarDetected()) || (crossing.getSouthSensor().isCarDetected())){
 			crossing.getEastLight().setRed();
 			crossing.getWestLight().setRed();
 			crossing.getNorthLight().setGreen();
 			crossing.getSouthLight().setGreen();
			countdown++;
		} else if (countdown >= 3) {
			countdown = 0;
 		} else {
 			crossing.getEastLight().setRed();
 			crossing.getWestLight().setRed();
 			crossing.getNorthLight().setRed();
 			crossing.getSouthLight().setRed();
 		}
 	}
 	
 }
