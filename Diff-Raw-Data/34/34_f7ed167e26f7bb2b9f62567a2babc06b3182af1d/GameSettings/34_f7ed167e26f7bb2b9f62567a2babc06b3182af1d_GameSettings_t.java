 package ca.hilikus.jrobocom;
 
 /**
  * All the game settings
  * 
  * @author hilikus
  * 
  */
 public class GameSettings {
 
     /**
      * Timing delays
      * 
      */
     public class Timing {
 	public static final int REMOTE_ACCESS_PENALTY = 6; // durRemoteAcc
 	public static final int LOCAL_READ = 1;
 	public static final int LOCAL_WRITE = 2;
 	public static final int TRANSFER_BASE = 0;
 	public static final int TRANSFER_SINGLE = 0;
 	public static final int CREATION_BASE = 0;
 	public static final int CREATION_PER_BANK = 0;
 	public static final int MOBILITY_PENALTY = 0;
 	public static final int MOBILITY_CONSTANT = 0;
 	public static final int ADVANCED_SET_PENALTY = 0;
 	public static final int SUPER_SET_PENALTY = 0;
 	public static final int MOVE = 0;
 	public static final int BANK_CHANGE = 0;
 	public static final int SCAN_BASE = 0;
 	public static final int SCAN_PER_DIST = 0;
 	public static final int TURN = 0;
     }
 
     public static final int MAX_CREATE_WAIT = 0;
    public static final int MAX_BANKS = 50;
     public static final int MAX_GENERATION = 0;
     public static final int BOARD_SIZE = 0;
     public static final int MAX_AGE = 0;
     public static final int MAX_BOTS = 0;
     public static final int MAX_WORLD_AGE = 0;
 
 
 }
