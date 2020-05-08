 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package model;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 /**
  *
  * @author paulHaller
  */
 public class SpielFeld {
 
     private String SPIELER_NAME = "Super Mario";
     private String COMPUTER_NAME = "Super C";
     private int playerPosition;
     private int computerPosition;
     private int playerOldPosition;
     private int computerOldPosition;
     private int playerRoll;
     private boolean playerOil;
     private int computerRoll;
     private boolean computerOil;
     private boolean finished;
    private boolean terminated;
     private int[] OIL_FIELDS = {3, 5};
     private final int NUMBER_OF_FIELDS = 7;
     private int round;
     private long time;
    
     public SpielFeld() 
     {
         time = System.currentTimeMillis();
         round = 0;
         playerPosition = 0;
         computerPosition = 0;
         computerOldPosition = 0;
         playerOldPosition = 0;
         finished = false;
        terminated=false;
         playerOil = false;
         computerOil = false;
         playerRoll = 0;
         computerRoll = 0;
 
     }
 
     public String getFuehrender() {
         if (getPlayerPosition() == getComputerPosition()) {
             return "mehrere";
         } else if (getPlayerPosition() > getComputerPosition()) {
             return getSPIELER_NAME();
         } else {
             return getCOMPUTER_NAME();
         }
     }
 
     /**
 			* @return SPIELER_NAME of leading Player
 			* @return If more then one player is leading the string "mehrere" is returned!
 			*/
     public void move(int player, int computer) {
         if (!isFinished()) {
             this.round++;
             setPlayerRoll(player);
             setComputerRoll(computer);
             setPlayerOil(false);
             setComputerOil(false);
             for (int i : getOIL_FIELDS()) {
                 if (getPlayerPosition() + player == i) {
                    setPlayerOldPosition(getPlayerPosition());
                     setPlayerPosition(0);
                     setPlayerOil(true);
                 }
                 if (getComputerPosition() + computer == i) {
                    setComputerOldPosition(getComputerPosition());
                     setComputerPosition(0);
                     setComputerOil(true);
                 }
             }
             if (!isPlayerOil()) {
                 
                 setPlayerOldPosition(getPlayerPosition());
                 setPlayerPosition(getPlayerPosition() + player);
                 
                 if (getPlayerPosition() >= getNUMBER_OF_FIELDS()) {
 
                     setPlayerPosition(getNUMBER_OF_FIELDS());
                     setFinished(true);
                 }
             }
             if (!isComputerOil() && !isFinished()) {
                 setComputerOldPosition(getComputerPosition());
                 setComputerPosition(getComputerPosition() + computer);
                 if (getComputerPosition() >= getNUMBER_OF_FIELDS()) {
                     
                     setComputerPosition(getNUMBER_OF_FIELDS());
                     setFinished(true);
                 }
             }
         }
 
 
 
     }
 		
     /**
      * @return the SPIELER_NAME
      */
 
     public String getSPIELER_NAME() {
         return SPIELER_NAME;
     }
 
     /**
      * @param SPIELER_NAME the SPIELER_NAME to set
      */
     public void setSPIELER_NAME(String SPIELER_NAME) {
         this.SPIELER_NAME = SPIELER_NAME;
     }
 
     /**
      * @return the COMPUTER_NAME
      */
     public String getCOMPUTER_NAME() {
         return COMPUTER_NAME;
     }
 
     /**
      * @param COMPUTER_NAME the COMPUTER_NAME to set
      */
     public void setCOMPUTER_NAME(String COMPUTER_NAME) {
         this.COMPUTER_NAME = COMPUTER_NAME;
     }
 
     /**
      * @return the playerPosition
      */
     public int getPlayerPosition() {
         return playerPosition;
     }
 
     /**
      * @param playerPosition the playerPosition to set
      */
     public void setPlayerPosition(int playerPosition) {
         this.playerPosition = playerPosition;
     }
 
     /**
      *
     **/
     public void setPlayerOldPosition(int pos) {
         this.playerOldPosition = pos;
     }
 
 
     public int getPlayerOldPosition() {
         return this.playerOldPosition;
     }
 
     /**
      * @return the computerPosition
      */
     public int getComputerPosition() {
         return computerPosition;
     }
 
     public void setComputerOldPosition(int pos) {
         this.computerOldPosition = pos;
     }
 
     
     public int getComputerOldPosition() {
         return this.computerOldPosition;
     }
 
     public String getTime(){
         long newTime = System.currentTimeMillis() - time;
         DateFormat df = new SimpleDateFormat("mm:ss");
         return df.format(newTime);
     }
     
     /**
      * @param computerPosition the computerPosition to set
      */
     public void setComputerPosition(int computerPosition) {
         this.computerPosition = computerPosition;
     }
 
     /**
      * @return the playerRoll
      */
     public int getPlayerRoll() {
         return playerRoll;
     }
 
     /**
      * @param playerRoll the playerRoll to set
      */
     public void setPlayerRoll(int playerRoll) {
         this.playerRoll = playerRoll;
     }
 
     /**
      * @return the playerOil
      */
     public boolean isPlayerOil() {
         return playerOil;
     }
 
     /**
      * @param playerOil the playerOil to set
      */
     public void setPlayerOil(boolean playerOil) {
         this.playerOil = playerOil;
     }
 
     /**
      * @return the computerRoll
      */
     public int getComputerRoll() {
         return computerRoll;
     }
 
     /**
      * @param computerRoll the computerRoll to set
      */
     public void setComputerRoll(int computerRoll) {
         this.computerRoll = computerRoll;
     }
 
     /**
      * @return the computerOil
      */
     public boolean isComputerOil() {
         return computerOil;
     }
 
     /**
      * @param computerOil the computerOil to set
      */
     public void setComputerOil(boolean computerOil) {
         this.computerOil = computerOil;
     }
 
     /**
      * @return the OIL_FIELDS
      */
     public int[] getOIL_FIELDS() {
         return OIL_FIELDS;
     }
 
     /**
      * @param OIL_FIELDS the OIL_FIELDS to set
      */
     public void setOIL_FIELDS(int[] OIL_FIELDS) {
         this.setOIL_FIELDS(OIL_FIELDS);
     }
 
     /**
      * @return the finished
      */
     public boolean isFinished() {
         return finished;
     }
 
     /**
      * @param finished the finished to set
      */
     public void setFinished(boolean finished) {
         this.finished = finished;
     }
    
        /**
     * @return the finished
     */
    public boolean isTerminated() {
        return terminated;
    }

    /**
     * @param finished the finished to set
     */
    public void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }
 
     
 
     /**
      * @return the NUMBER_OF_FIELDS
      */
     public int getNUMBER_OF_FIELDS() {
         return NUMBER_OF_FIELDS;
     }
     
     public boolean isOilField(int fieldNumber){
         for(int i :OIL_FIELDS){
             if(i == fieldNumber){
                 return true;
             }
         }
         return false;
     }
 
     /**
      * @return the round
      */
     public int getRound() {
         return round;
     }
 
     
 }
