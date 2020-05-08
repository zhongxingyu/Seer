 import lejos.nxt.*
 
 public class RobotTest {
 
 	private byte currentMotorIndex = 0;
 
 	public static void main(String[] args) {
 		printMotor();
 		while (true) {
 			if (Button.ESCAPE.isDown()) {
 				break;
 			} else if (Button.RIGHT.isDown()) {
 				Motor.getInstance(this.currentMotorIndex).forward();
 			} else if (Button.LEFT.isDown()) {
 				Motor.getInstance(this.currentMotorIndex).backward();
 			} else if (Button.ENTER.isDown()) {
 				if (Motor.getInstance(this.currentMotorIndex).isMoving()) {
 					Motor.getInstance(this.currentMotorIndex).stop();
 				} else {
 					this.currentMotorIndex = ++this.currentMotorIndex%3;
 					printMotor();
 				}
				
				while (Button.ENTER.isDown()) {
					Button.waitForAnyEvent(1000);
				}
 			}
 		}
 	}
 
 	private static void printMotor() {
 		if (this.currentMotorIndex==0) {
 			System.out.println("A");
 		} else if (this.currentMotorIndex==1) {
 			System.out.println("B");
 		} else if (this.currentMotorIndex==2) {
 			System.out.println("C");
 		} else {
 			System.out.println("INVALID MOTOR INDEX");
 		}
 	}
 }
