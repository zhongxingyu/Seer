 package icarus.operatingsoftware;
 
 import icarus.exceptions.AlreadyAtStateException;
 import icarus.exceptions.ComponentFailedException;
 import icarus.exceptions.FixAlreadyUnderwayException;
 import icarus.exceptions.InvalidComponentException;
 import icarus.exceptions.InvalidPumpException;
 import icarus.exceptions.InvalidRodsException;
 import icarus.exceptions.InvalidValveException;
 import icarus.exceptions.NoFixNeededException;
 
 /**
  *
  * @author david
  */
 public interface PlantControl {
 
     /**
      * Checks whether the game has ended - if the temperature in the reactor has exceeded the max
      *
      * @return Whether the game over scenario has been reached
      */
     boolean checkIfGameOver();
 
     /**
      * Close a SteamValve in the system
      *
      * @param valveNum The id of the valve to close
      *
      * @throws InvalidValveException   Thrown when a bad valve ID is specified
      * @throws AlreadyAtStateException Thrown if the specified valve is already closed
      */
     void close(int valveNum) throws InvalidValveException, AlreadyAtStateException;
 
     /**
      * Progresses current repairs in the system
      *
      * @return Whether a repair has finished.
      */
     boolean doFix();
 
     /**
      * Begins a fix on any non-pump fixable component
      *
      * @param component The component to begin the fix upon
      *
      * @throws InvalidComponentException   Thrown when a bad component is specified
      * @throws FixAlreadyUnderwayException Thrown when a fix is already occurring in the system
      * @throws NoFixNeededException        Thrown when attempting to fix a functional component
      */
     void fix(Components component) throws InvalidComponentException, FixAlreadyUnderwayException, NoFixNeededException;
 
     /**
      * Begins fixes for pumps
      *
      * @param component Must be set to WaterPump, used as a check to ensure desired functionality
      * @param pumpNum   The id of the pump to be fixed
      *
      * @throws InvalidComponentException   Thrown when a non-pump component is specified
      * @throws FixAlreadyUnderwayException Thrown when a fix is already occurring in the system
      * @throws NoFixNeededException        Thrown when attempting to fix a functional component
      */
     void fix(Components component, int pumpNum) throws InvalidComponentException, FixAlreadyUnderwayException,
                                                        InvalidPumpException, NoFixNeededException;
 
     /**
      *
      * @return Whether there is currently a fix underway in the system
      */
     boolean fixUnderway();
 
     /**
      * Returns whether a specified component is working correctly
      *
      * @param component The component to inspect
      *
      * @return whether the specified component is functional
      *
      * @throws InvalidComponentException Thrown when a bad component is specified
      */
     boolean functional(Components component);
 
     /**
      * Variation of functional(Components) for pumps, returns the functionality of a specified pump
      *
      * @param component Required to be set to Components.WATERPUMP, specifies the component to inspect
      * @param pumpNum   The id of the pump to inspect
      *
      * @return whether the specified pump is functional
      *
      * @throws InvalidComponentException Thrown when a bad component is specified
      */
     boolean functional(Components component, int pumpNum);
 
     /**
      * Gets the number of timesteps remaining for the current fix to complete. Does not specify what component is being
      * fixed.
      *
      * @return The fix time on a current fix in the system
      */
     int getFixTime();
 
     /**
      * get the name of the player
      *
      * @returns String name
      */
     String getPlayerName();
 
     /**
      * Returns the total power generated
      */
     int getPower();
 
     /**
      * Check if a condenser pump is active
      *
      * @returns whether pump is active
      */
     boolean isCondenserPumpActive();
 
     /**
      * Returns whether a specified component is currently being repaired
      *
      * @param component The component to inspect
      *
      * @return Whether the component is currently being repaired
      */
     boolean isRepairing(Components component);
 
     /**
      * The pump variant of isRepairing(Components). Returns whether a specified pump is currently being repaired.
      *
      * @param component required to be set to Components.WATERPUMP, specifies the component to inspect
      * @param id        The pump ID to inspect
      *
      * @return Whether the specified pump is being repaired
      */
     boolean isRepairing(Components component, int id);
 
     /**
      * Check if a valve is opened
      *
      * @returns whether valve is open
      * @throws InvalidValveException Thrown when bad ID is specified
      */
     boolean isValveOpened(int valveNum);
 
     /**
      * Check if a water pump is active
      *
      * @param pumpNum The id of the pump
      *
      * @returns whether pump of ID is active
      */
     boolean isWaterPumpActive(int pumpNum);
 
     /**
      * Progresses time in the system. All time dependent activities occur such as changes in temperature, pressure,
      * water levels, power output and equiptment failure and fixes.
      */
     void next();
 
     /**
      * Opens a SteamValve in the system
      *
      * @param valveNum The id of the valve to open
      *
      * @throws InvalidValveException   Thrown when a bad valve ID is specified
      * @throws AlreadyAtStateException Thrown if the specified valve is already open
      */
     void open(int valveNum) throws InvalidValveException, AlreadyAtStateException;
 
     /**
      * Returns the pressure of a requested component
      *
      * @param component Value of the enum of which component to retrieve the pressure
      *
      * @return double containing the pressure of the requested component
      *
      * @throws InvalidComponentException Thrown when a non-reactor/-condenser component is specified
      */
     double pressure(Components component);
 
     /**
      * Move the control rods to the specified position
      *
      * @param amount The position to move the control rods to, from 1-100
      *
      * @throws InvalidRodsException     Thrown when amount has an invalid value
      * @throws ComponentFailedException Thrown when the reactor has failed and the control rods cannot be moved
      */
     void movecontrolrods(int amount) throws InvalidRodsException, ComponentFailedException;
 
     /**
      * Returns the rod height
      *
      * @return double containing the rod height
      */
     int rodHeight();
 
     /**
      * sets the name of the player
      *
      * @param String name
      */
     void setPlayerName(String name);
 
     /**
      * Returns the temperature of a requested component
      *
      * @param component Value of the enum of which component to retrieve the temperature
      *
      * @return double containing the temperature of the requested component
      *
      * @throws InvalidComponentException Thrown when a non-reactor/-condenser component is specified
      */
     double temperature(Components component);
 
     /**
      * Turn off a Pump in the system
      *
      * @param pumpNum The id of the pump to turn off
      *
      * @throws InvalidPumpException     Thrown when a bad pump ID is specified
      * @throws AlreadyAtStateException  Thrown if the specified pump is already on
      * @throws ComponentFailedException Thrown if method is called when component is failed.
      */
     void turnOff(int pumpNum) throws InvalidPumpException, AlreadyAtStateException, ComponentFailedException;
 
     /**
      * Turn on a Pump in the system
      *
      * @param pumpNum The id of the pump to turn on
      *
      * @throws InvalidPumpException     Thrown when a bad pump ID is specified
      * @throws AlreadyAtStateException  Thrown if the specified pump is already on
      * @throws ComponentFailedException Thrown if method is called when component is failed.
      */
     void turnOn(int pumpNum) throws InvalidPumpException, AlreadyAtStateException, ComponentFailedException;
 
     /**
      * Returns the water level of a requested component
      *
      * @param component Value of the enum of which component to retrieve the water level
      *
      * @return double containing the water level of the requested component
      *
      * @throws InvalidComponentException Thrown when a non-reactor/-condenser component is specified
      */
     double waterLevel(Components component);
     
    void resetPlant();
 }
