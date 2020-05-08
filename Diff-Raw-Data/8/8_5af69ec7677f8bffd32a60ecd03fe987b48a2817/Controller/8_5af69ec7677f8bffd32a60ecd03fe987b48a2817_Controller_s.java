 import utils.ControllerPrx;
 
 public class Controller {
 
   private ControllerPrx _proxy;
   private String _door;
   private int _floor;
 
   public Controller(int floor, String door, ControllerPrx proxy) {
     _proxy = proxy;
     _door = door;
     _floor = floor;
   }
 
  public int getFloor(void) {
     return _floor;
   }
 
  public String getDoor(void) {
     return _door;
   }
 
  public ControllerPrx getProxy(void) {
     return _proxy;
   }
 }
