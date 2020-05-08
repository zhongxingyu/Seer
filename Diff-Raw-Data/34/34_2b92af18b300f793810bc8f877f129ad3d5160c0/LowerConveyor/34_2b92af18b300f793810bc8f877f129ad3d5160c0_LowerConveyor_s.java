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
     
    public LowerConveyor(){
        //Try catch for errors
        try {
            
            //Setup the Jaguar
            conveyorMotor = new CANJaguar(14);
            
        } catch (CANTimeoutException ex) { //Catch CANTimeout Error
            
            //Print Error
            Debug.fatal(ex, "CAN Timeout in " + this.getClass().getName());
 
        } catch (Exception ex){ //Catch all for errors

            //Print Error
            Debug.fatal(ex, "Unknown error in " + this.getClass().getName());
        }  
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
         conveyorMotor.setX(0);
     }
 }
