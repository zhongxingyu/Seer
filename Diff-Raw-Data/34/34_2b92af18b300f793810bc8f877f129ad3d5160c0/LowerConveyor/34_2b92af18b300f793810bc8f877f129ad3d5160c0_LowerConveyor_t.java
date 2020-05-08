 package org.inceptus.chassis;
 
 import edu.wpi.first.wpilibj.CANJaguar;
 import edu.wpi.first.wpilibj.can.CANTimeoutException;
 import org.inceptus.debug.Debug;
 
 /**
  *
  * @author innoying
  */
 public class LowerConveyor {
     
     private CANJaguar conveyorMotor;
     
    public LowerConveyor() throws CANTimeoutException{
 
        //Setup the Jaguar
        conveyorMotor = new CANJaguar(14);
           
     }
     
     public void moveUp() throws CANTimeoutException{
        
         //Go up at not full power to break conveyor
         conveyorMotor.setX(.5);
        
     }
     
     public void moveDown() throws CANTimeoutException{
        
         //May be jamed
         conveyorMotor.setX(-1);
        
     }
     
     public void stop() throws CANTimeoutException{
        
        //Stop
         conveyorMotor.setX(0);
        
     }
 }
