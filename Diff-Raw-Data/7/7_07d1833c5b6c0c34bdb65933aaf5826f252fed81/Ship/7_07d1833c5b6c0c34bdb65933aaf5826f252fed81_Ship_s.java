 import java.util.ArrayList;
 
 public class Ship {
 	
     private ShipType currentShip;
 
 	private int flea = 0;
 	private int gnat = 1;
 	private int firefly = 2;
 	private int mosquito = 3;
 	private int bumblebee = 4;
 	private int beetle = 5;
 	private int hornet = 6;
 	private int grasshopper = 7;
 	private int termite = 8;
 	private int wasp = 9;
 		
     public enum ShipType {
         FLEA, GNAT, FIREFLY, MOSQUITO, BUMBLEBEE, BEETLE, HORNET, GRASSHOPPER, TERMITE, WASP
     }
 
	public Ship()
	{
        this.currentShip = FLEA;
 	}

 }
