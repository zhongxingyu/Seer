 package Configurator;
 
 import Util.Messages;
 import Util.Strings;
 
 /*******************************************************************************
 * CLASS: Configurator
  *
  * FUNCTIONS: Toggleable(int msg, boolean state)
  *            void setState(boolean s)
  *            void toggle()
  *
  * DATE: 2013-01-18
  *
  * DESIGNER: Team Cirno
  *
  * PROGRAMMER: Team Cirno
  *
  * NOTES: This Represents a toggleable item
  *******************************************************************************/
 public class Toggleable
 {
 	/**
 	 * The state of the toggleable.
 	 */
 	public boolean state = false;
 
 	/**
 	 * The message in the toggleable.
 	 */
 	public int message = 0;
 
 	/*************************************************************************** 
 	 * FUNCTION: Toggleable
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: Toggleable(int msg, boolean state)
 	 *            int msg - The message to be stored in the object
 	 *            boolean state - the initial state of the object
 	 * 
 	 * NOTES: Constructor for the Settings class
 	 ***************************************************************************/
 	public Toggleable(int msg, boolean state)
 	{
 		this.state = state;
 		message = msg;
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: getToggleableState
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: void setState(boolean s)
 	 *            boolean s - The new state
 	 * 
 	 * RETURNS: void
 	 * 
 	 * NOTES: Sets the state of the object
 	 ***************************************************************************/
 	public void setState(boolean s)
 	{
 		state = s;
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: toggle
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: void toggle()
 	 * 
 	 * RETURNS: void
 	 * 
 	 * NOTES: Toggles the toggle to the opposite of whatever it was currently.
 	 ***************************************************************************/
 	public void toggle()
 	{
 		state = !state;
 	}
 
 	/*************************************************************************** 
 	 * FUNCTION: toString
 	 * 
 	 * DATE: 2013-01-30
 	 * 
 	 * DESIGNER: Team Cirno
 	 * 
 	 * PROGRAMMER: Team Cirno
 	 * 
 	 * INTERFACE: String toString()
 	 * 
 	 * RETURNS: A string formatted to contain the message and toggle state
 	 * 
 	 * NOTES: returns the stored message as well as whether the toggle is on or
 	 *        off.
 	 * 
 	 *        "Toggle " [message] " - ON" OR
 	 *        "Toggle " [message] " - OFF"
 	 ***************************************************************************/
 	public String toString()
 	{
 		String str[] = Strings.getAllMessages();
 		String retVal = str[Messages.TOGGLE] + " " + Strings.getMessage(message) + ": ";
 
 		retVal += ((state)? str[Messages.ON] : str[Messages.OFF]);
 		return retVal;
 	}
 }
