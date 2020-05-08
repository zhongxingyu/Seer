 package emulator.cpu;
 
 /**
  * Write a description of class PartCommunication here.
  * 
  * @author Jacob Weiss
  * @version 0.0.1
  */
 public abstract class PinConnector<E>
 {
    protected Pin<Boolean> clock = new Pin(false);
     private boolean previousState = false;
    private Pin<E> dataIn = new Pin(false);
    private Pin<E> dataOut = new Pin(false);
     
     public Pin<Boolean> clockPin()
     {
         return this.clock;
     }
     public Pin<E> dataInPin()
     {
         return this.dataIn;
     }
     public Pin<E> dataOutPin()
     {
         return this.dataOut;
     }
     
     protected boolean hasNewData()
     {        
         if (!this.previousState && this.clock.getValue())
         {
             this.previousState = this.clock.getValue();
             return true;
         }
         else
         {
             this.previousState = this.clock.getValue();
             return false;
         }
     }
     protected E getData()
     {
         return this.dataIn.getValue();
     }
     protected void setData(E data)
     {
         this.dataOut.setValue(data);
     }
     
 }
