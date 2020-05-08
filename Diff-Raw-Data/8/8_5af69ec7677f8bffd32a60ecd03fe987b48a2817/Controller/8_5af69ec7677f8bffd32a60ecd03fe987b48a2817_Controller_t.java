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
 
  public int getFloor() {
     return _floor;
   }
 
  public String getDoor() {
     return _door;
   }
 
  public ControllerPrx getProxy() {
     return _proxy;
   }
 }
