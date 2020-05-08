 /**
  * @file Trigger.java
  * @author William Lam
  * @date Jan 6, 2012
  * @description 
  * 		Trigger.java is an enumeration of the different triggers
  */
 
 package CardAssociation;
 
 public enum Trigger {
	ALL(""), NONE("None"), SOUL("Soul+1"), DUALSOUL("Soul+2"), SOULWIND("Return"), SOULFLAME(
			"Shot"), GATE("Comeback"), TREASURE("Treasure"), GOLDBAG("Pool"), BOOK("Draw");
 
 	String s;
 
 	Trigger(String trigger) {
 		s = trigger;
 	}
 
 	public String toString() {
 		return s;
 	}
 
 	public static Trigger convertString(String s) {
 		if (s.equals("0")) {
 			return NONE;
 		} else if (s.equals("1")) {
 			return SOUL;
 		} else if (s.equals("2")) {
 			return DUALSOUL;
 		} else if (s.equals("1W")) {
 			return SOULWIND;
 		} else if (s.equals("1F")) {
 			return SOULFLAME;
 		} else if (s.equals("D")) {
			return GATE;
 		} else if (s.equals("G")) {
 			return TREASURE;
 		} else if (s.equals("S")) {
 			return GOLDBAG;
 		} else if (s.equals("B")) {
 			return BOOK;
 		} else {
 			return null;
 		}
 	}
 	
 	public int getNumericCode() {
     	switch(this) {
 	    	case ALL:
 	    		return 0;
 	    	case NONE:
 	    		return 1;
 	    	case SOUL:
 	    		return 2;
 	    	case DUALSOUL:
 	    		return 3;
 	    	case SOULWIND:
 	    		return 4;
 	    	case SOULFLAME:
 	    		return 5;
	    	case GATE:
 	    		return 6;
 	    	case TREASURE:
 	    		return 7;
 	    	case GOLDBAG:
 	    		return 8;
 	    	case BOOK:
 	    		return 9;
 	    	default:
     			return 255;
     	}
     }
 }
