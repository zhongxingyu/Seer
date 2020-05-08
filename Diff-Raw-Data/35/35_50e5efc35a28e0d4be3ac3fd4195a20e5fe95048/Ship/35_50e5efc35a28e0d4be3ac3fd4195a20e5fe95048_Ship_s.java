 package edu.gatech.statusquo.spacetrader.model;
 
import edu.gatech.statusquo.spacetrader.model.Good.GoodType;
 
 public class Ship {
 	public Good[] cargoBay;
 	ShipType shipType;
 	final int fuelCapacity;
 	static int fuelLevel;
 
 	public void setFuelLevel(int fuelLevel) {
 		Ship.fuelLevel = fuelLevel;
 	}
 	public enum ShipType {
     	FLEA {
 	        public String toString() {
 	            return "FLEA";
 	        }
     	}, GNAT {
 	        public String toString() {
 	            return "GNAT";
 	        }
     	}, FIREFLY {
 	        public String toString() {
 	            return "FIREFLY";
 	        }
     	}, MOSQUITO {
 	        public String toString() {
 	            return "MOSQUITO";
 	        }
     	}, BUMBLEBEE {
 	        public String toString() {
 	            return "BUMBLEBEE";
 	        }
     	}, BEETLE {
 	        public String toString() {
 	            return "BEETLE";
 	        }
     	}, HORNET {
 	        public String toString() {
 	            return "HORNET";
 	        }
     	}, GRASSHOPPER {
 	        public String toString() {
 	            return "GRASSHOPPER";
 	        }
     	}, TERMITE {
 	        public String toString() {
 	            return "TERMITE";
 	        }
     	}, WASP {
 	        public String toString() {
 	            return "WASP";
 	        }
     	}
     }
     
     /**
      * 
      * @param st
      */
     public Ship (ShipType st) {
     	this.shipType = st;
     	
     	switch(st) {
 	    	case FLEA:
 	    		cargoBay = new Good[10];
 	    		fuelCapacity = 10;
 	    		fuelLevel = 10;
 	    		break;
 	    	case GNAT:
 	    		cargoBay = new Good[15];
 	    		fuelCapacity = 14;
 	    		fuelLevel = 14;
 	    		break;
 	    	case FIREFLY:
 	    		cargoBay = new Good[20];
 	    		fuelCapacity = 17;
 	    		fuelLevel = 17;
 	    		break;
 	    	case MOSQUITO:
 	    		cargoBay = new Good[15];
 	    		fuelCapacity = 13;
 	    		fuelLevel = 13;
 	    		break;
 	    	case BUMBLEBEE:
 	    		cargoBay = new Good[25];
 	    		fuelCapacity = 15;
 	    		fuelLevel = 15;
 	    		break;
 	    	case BEETLE:
 	    		cargoBay = new Good[25];
 	    		fuelCapacity = 16;
 	    		fuelLevel = 16;
 	    		break;
 	    	case HORNET:
 	    		cargoBay = new Good[30];
 	    		fuelCapacity = 17;
 	    		fuelLevel = 17;
 	    		break;
 	    	case GRASSHOPPER:
 	    		cargoBay = new Good[35];
 	    		fuelCapacity = 19;
 	    		fuelLevel = 19;
 	    		break;
 	    	case TERMITE: 
 	    		cargoBay = new Good[40];
 	    		fuelCapacity = 22;
 	    		fuelLevel = 22;
 	    		break;
 	    	case WASP:
 	    		cargoBay = new Good[50];
 	    		fuelCapacity = 25;
 	    		fuelLevel = 25;
 	    		break;
 	    	default:
 	    		//GIVE ERROR
 	    		cargoBay = new Good[0];
 	    		fuelCapacity = 0;
 	    		fuelLevel = 0;
 	    		break;
 	    		
     	}
     }
     
     /**
      * 
      * @return fuelLevel
      */
     
     public int getFuelLevel() {
     	return fuelLevel;
     }
     
     /**
      * 
      * @return cargoBay
      */
     public Good[] getCargoBay() {
     	return cargoBay;
     }
 	
     /**
      * 
      * @param gt
      * @return count
      */
 	public int countGoodType(GoodType gt) {
 		int count = 0;
 		for (Good g: cargoBay) {
 			if (g.getGoodType() == gt) count++;
 		}
 		return count;
 	}
 	
 	/**
 	 * 
 	 * @return count
 	 */
 	public int countCargoLeft() {
 		int count = 0;
 		for (int i = 0; i < cargoBay.length; i++) {
 			if (cargoBay[i] == null) count++;
 		}
 		return count;
 	}
 	
 	/**
 	 * 
 	 * @param gt
 	 * @return count
 	 */
 	public int countCargo(GoodType gt) {
 		int count = 0;
 		for (int i = 0; i < cargoBay.length; i++) {
 			if (cargoBay[i] != null) {
 				if (cargoBay[i].getGoodType() == gt) count++;
 			}
 		}
 		return count;
 	}
 	
 	/**
 	 * 
 	 * @param amt
 	 * @param gt
 	 */
 	public void addCargo(int amt, GoodType gt) {
 		while (amt > 0) {
 			for (int i = 0; i < cargoBay.length; i++) {
 				if (cargoBay[i] == null) {
 					cargoBay[i] = new Good(gt);
 					break;
 				}
 			}
 			amt--;
 		}
 	}
 	/**
 	 * 
 	 * @param amt
 	 * @param gt
 	 */
 	public void removeCargo(int amt, GoodType gt) {
 		while (amt > 0) {
 			for (int i = 0; i < cargoBay.length; i++) {
 				if (cargoBay[i] != null) {
 					if (cargoBay[i].getGoodType() == gt) {
 						cargoBay[i] = null;
 						break;
 					}
 				}
 			}
 			amt--;
 		}
 	}
 	
 	/**
 	 * Adds amt to current fuelLevel
 	 * @param amt
 	 */
 	public void addFuel(int amt) {
 		fuelLevel = fuelLevel + amt;
 	}
 	/**
 	 * Subtracts amt from current fuelLevel
 	 * @param amt
 	 */
 	public void subFuel(int amt){
 	    fuelLevel = fuelLevel - amt;
 	}
 	
 	/**
 	 * @return fuelCapacity
 	 */
     public int getFuelCapacity() {
         return fuelCapacity;
     }
     
     public String toString() {
     	return shipType + "," + Integer.toString(fuelLevel) + ",";
     }
     
     public String toStringCargoBay() {
     	String out = "";
     	for (Good g: cargoBay) {
     		out = out + g + ",";
     	}
     	return out;
     }
    /**
     * 
     * @param shipName
     * @return ShipType
     */
     public static ShipType getShipType(String shipName) {
     	switch(shipName){
     	case "FLEA":
     		return ShipType.FLEA;
     	case "GNAT":
     		return ShipType.GNAT;
     	case "FIREFLY":
     		return ShipType.FIREFLY;
     	case "MOSQUITO":
     		return ShipType.MOSQUITO;
     	case "BUMBLEBEE":
     		return ShipType.BUMBLEBEE;
     	case "BEETLE":
     		return ShipType.BEETLE;
     	case "HORNET":
     		return ShipType.HORNET;
     	case "GRASSHOPPER":
     		return ShipType.GRASSHOPPER;
     	case "TERMITE":
     		return ShipType.TERMITE;
     	case "WASP":
     		return ShipType.WASP;
        	}
     	return ShipType.FLEA;
     }
 }
 
