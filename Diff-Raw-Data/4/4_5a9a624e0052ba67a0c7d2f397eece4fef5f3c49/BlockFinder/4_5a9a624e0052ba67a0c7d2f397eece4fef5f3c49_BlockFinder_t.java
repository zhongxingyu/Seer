 package dinaBOT.detection;
 
 import lejos.nxt.*;
 import dinaBOT.mech.MechConstants;
 import dinaBOT.navigation.*;
 import dinaBOT.sensor.*;
 
 /**
  * This class locates blocks, and navigate properly towards them.
  *
  * @author Vinh Phong Buu
 */
 public class BlockFinder implements USSensorListener, MechConstants {
 
 	Odometer odometer;
 	Movement mover;
 
 	//constants have been moved to MechConstants - stepan
 
 	//Fields
 	double angleA;
 	double angleB;
 	int[] low_Readings;
 	int[] high_Readings;
 
 	//True when data sets for low & high are acquired
 	boolean data_acquired = false;
 
 	//Current phase of operation
 	int phase =0;
 
 	int blockDistance_A;
 	int blockDistance_B;
 	int minLow =0;
 	int minHigh=0;
 	int missedAngle;
 	int i = 0;
 	final int size = 5;
 	
 	static final int SECOND_COLUMN_MAX = 75;
 	//Super experimental constant
 
 	/**
 	 * Creates a BlockFinder using a supplied {@link dinaBOT.navigation.ArcOdometer odometer}.
 	 *
 	*/
 	public BlockFinder(Odometer odometer, Movement mover) {
 		this.odometer = odometer;
 		this.mover = mover;
 		USSensor.low_sensor.registerListener(this);
 		USSensor.high_sensor.registerListener(this);
 		low_Readings = new int[]{255,255,255,255,255,255,255,255};
 		high_Readings = new int[]{255,255,255,255,255};
 	}
 
 	/**
 	 *Pivots the robot to perform a {@value #SWEEP_ARC} radians sweep using the ultrasonic sensor
 	 *to detect the nearest block. The robot then moves towards it.
 	 *In case of a false block detection, the robot simply returns to the orientation it was facing as before it
 	 *initiated the sweep.
 	 *
 	 *
 	 *@param blockAngle The orientation of the robot when the block was seen
 	 *during search (in radians).
 	 *
 	*/
 	public boolean sweep(double blockAngle) {
 		blockDistance_A = 255;
 		blockDistance_B = 255;
 		
		//resets high_Readings
		high_Readings = new int[]{255,255,255,255,255};
		
		
 		double initialOrientation = odometer.getPosition()[2];
 		angleA = initialOrientation;
 		angleB = initialOrientation;
 
 		//Turn to the direction where the block was first seen
 		mover.turnTo(blockAngle+SWEEP_ARC/2, SPEED_ROTATE);
 
 		//Clockwise sweep
 		phase = 1;
 		mover.turn(-SWEEP_ARC, SPEED_ROTATE, false);
 
 		//Counter-clockwise sweep
 		phase = 2;
 		mover.turn(SWEEP_ARC, SPEED_ROTATE, false);
 
 		//Duplicate angle if either is missed
 		//But make sure it definitely is a pallet by checking the second data column
 		phase = 0;
 		if (angleA == 0) {
 			missedAngle = 'A';
 			mover.turnTo(angleB, SPEED_ROTATE);
 			phase = 3;
 		} else if (angleB == 0) {
 			missedAngle = 'B';
 			mover.turnTo(angleA, SPEED_ROTATE);
 			phase = 3;
 		}
 
 		//To the bisecting angle !
 		// or back to start in case of FAIL
 		phase = 0;
 		if (Math.abs(blockDistance_A - blockDistance_B) < 5 && blockDistance_A != 255 && blockDistance_B !=255) {
 			mover.turnTo((angleA+angleB)/2, SPEED_ROTATE);
 			mover.goForward((blockDistance_A+blockDistance_B)/2, SPEED_MED);
 			return true;
 		} else {
 			//Fail-safe technique for now.
 			mover.turnTo(initialOrientation, SPEED_ROTATE);
 			return false;
 		}
 
 	}
 
 	public void findEdgeA() {
 
 			if(minLow < US_TRUST_THRESHOLD
 					&& minHigh - minLow > DETECTION_THRESHOLD
 					&& minLow < blockDistance_A
 					&& low_Readings[1] < SECOND_COLUMN_MAX) {
 
 				blockDistance_A = minLow;
 				angleA = odometer.getPosition()[2];
 				Sound.buzz();
 				LCD.drawInt(minLow, 0, 0);
 				LCD.drawInt(minHigh, 0, 2);
 				LCD.drawInt(low_Readings[1], 0, 1);
 			}
 	}
 
 	public void	findEdgeB() {
 
 			if(minLow < US_TRUST_THRESHOLD
 					&& minHigh - minLow > DETECTION_THRESHOLD
 					&& minLow < blockDistance_B
 					&& low_Readings[1] < SECOND_COLUMN_MAX) {
 
 				blockDistance_B = minLow;
 				angleB = odometer.getPosition()[2];
 				Sound.buzz();
 				LCD.drawInt(minLow, 0, 3);
 				LCD.drawInt(minHigh, 0, 5);
 				LCD.drawInt(low_Readings[1], 0, 4);
 			}
 		}
 
 	public static int getFilteredValue(int[] list) {
 		int value = 255;
 		for (int i=0; i<list.length; i++) {
 			value = Math.min(value, list[i]); 
 		}
 		return value;
 	}
 
 	public void newValues(int[] new_values, USSensor sensor) {
 
 			switch (phase) {
 			case 0:
 				//Do nothing
 				break;
 
 			case 1:
 				//Latching A
 				if (sensor == USSensor.low_sensor) {
 					this.low_Readings = new_values;
 					if (data_acquired) {
 						data_acquired = false;
 					}
 				} else if (sensor == USSensor.high_sensor) {
 					this.high_Readings[i%size] = new_values[0];
 					i++;
 					if (!data_acquired) {
 						minLow = low_Readings[0];
 						minHigh = getFilteredValue(high_Readings);
 						findEdgeA();
 						data_acquired = true;
 					}
 				}
 
 				break;
 
 			case 2:
 				//Latching B
 				if (sensor == USSensor.low_sensor) {
 					this.low_Readings = new_values;
 					if (data_acquired) {
 						data_acquired = false;
 					}
 				} else if (sensor == USSensor.high_sensor) {
 					this.high_Readings[i%size] = new_values[0];
 					i++;
 					if (!data_acquired) {
 						minLow = low_Readings[0];
 						minHigh = getFilteredValue(high_Readings);
 						findEdgeB();
 						data_acquired = true;
 					}
 				}
 
 				break;
 
 
 			case 3:
 				//Get missing angle
 				if (sensor == USSensor.low_sensor) {
 					this.low_Readings = new_values;
 					if (data_acquired) {
 						data_acquired = false;
 					}
 				} else if (sensor == USSensor.high_sensor) {
 					this.high_Readings[i%size] = new_values[0];
 					if (!data_acquired) {
 						if (missedAngle == 'A') {
 							findEdgeA();
 						}
 						if (missedAngle == 'B') {
 							findEdgeB();
 						}
 						data_acquired = true;
 					}
 				}
 
 				break;
 			}
 
 	}
 
 }
