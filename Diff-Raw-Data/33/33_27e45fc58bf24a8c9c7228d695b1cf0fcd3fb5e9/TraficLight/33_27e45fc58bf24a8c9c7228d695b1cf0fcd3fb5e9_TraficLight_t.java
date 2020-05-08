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
			countDown++;
 		} else if ((crossing.getNorthSensor().isCarDetected()) || (crossing.getSouthSensor().isCarDetected())){
 			crossing.getEastLight().setRed();
 			crossing.getWestLight().setRed();
 			crossing.getNorthLight().setGreen();
 			crossing.getSouthLight().setGreen();
			countDown++;
		} else if (countDown >= 3) {
			countDown = 0;
 		} else {
 			crossing.getEastLight().setRed();
 			crossing.getWestLight().setRed();
 			crossing.getNorthLight().setRed();
 			crossing.getSouthLight().setRed();
 		}
 	}
 	
 }
