 import lejos.nxt.I2CPort;
 import lejos.nxt.I2CSensor;
 
 public class Multiplexor extends I2CSensor{
 	private static byte direction;
 	private static byte speed;
 
 	public Multiplexor(I2CPort port){
 		super(port);
 		setAddress(0x5A);
 	}
 
 	public static void main(String[] args){	
 	}
 
 	public void setMotors(int directionIndex, int speedIndex, int wheelIndex){
 		// sets up possible values 
 		if(directionIndex == -1){
 			direction  = (byte)2;
 		}else if(directionIndex == 0){
 			direction = (byte)0;
 		} else if(speedIndex == 1){
 			direction = (byte)1;
 		}
 			
 		if(speedIndex == 0){
 			speed = (byte)0;
 		}else if(speedIndex == 1){
			speed = (byte)90;
        }else if(speedIndex == 2){
			speed = (byte)180;
		} else if(speedIndex == 3){
 			speed = (byte)255;
 		}
 
 		switch (wheelIndex){
 		// left wheel
 		case 0:		 
 			sendData((byte)0x03,direction); 
 			sendData((byte)0x04,speed);
 			break;
 		// right wheel
 		case 1:
 			sendData((byte)0x01,direction); 
 			sendData((byte)0x02,speed);
 			break;
 		}
 	}
 }
