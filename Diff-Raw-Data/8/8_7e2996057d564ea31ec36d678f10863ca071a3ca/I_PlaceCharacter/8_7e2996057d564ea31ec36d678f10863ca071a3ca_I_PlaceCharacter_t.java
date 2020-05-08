 package DD.CombatSystem.Interpreter.System;
 
 import org.newdawn.slick.SlickException;
 import DD.Character.DDCharacter;
 import DD.CombatSystem.Interpreter.CombatInterpreter;
 import DD.MapTool.CharacterObjects;
 import DD.Message.CombatMessage;
 import DD.Message.CombatValidationMessage;
 
 /*****************************************************************************************************
  * I_PlaceCharacter is the interpreter for the PlaceCharacter ability. It will create a new character
  * from the characterSheet provided, give it the provided characterID, and place it on the map using
  * the provided coordinates.
  * 
  * @author Carlos Vasquez
  ******************************************************************************************************/
 
 public class I_PlaceCharacter extends CombatInterpreter
 {
 	/************************************ Class Constants *************************************/
 	private static int I = 0;
 	public static final int CHARACTER_ID = I++;		/* Characters ID */
 	public static final int POS_X = I++;			/* X coordinate */
 	public static final int POS_Y = I++;			/* Y Coordinate */
 	public static final int RESET = I++;			/* reset or not to reset character */
 	public static final int BODY_SIZE = I;
 	
 	/************************************ Class Methods *************************************/
 	@Override
 	public CombatValidationMessage validate(CombatMessage cm) 
 	{
 		// TODO Auto-generated method stub
 		CombatValidationMessage returner = new CombatValidationMessage(true, null); // TODO: Check for validity
 		return returner;
 	} /* end validate method */
 
 	@Override
 	public void interpret(CombatMessage cm) 
 	{
 		DDCharacter newCharacter = new DDCharacter(cm.getBody()[CHARACTER_ID]);
 		newCharacter.setCharacterID(cm.getBody()[CHARACTER_ID]);
 		newCharacter.setCharacterSheet(cm.getCharacterData());
 		
 		/* Tell the CombatSystem abut the new character */
 		cs.addCharacter(newCharacter);
 		
 		/* Add the character to the next turn */
 		cs.addToOrder(cm.getBody()[CHARACTER_ID], cs.getTurn() + 1);
 		
 		/* Give to the player that owns the character */
 		if(cs.getNetID() == cm.getCharacterData().getNetID()) ab.addCharacter(cm.getBody()[CHARACTER_ID]);

 		/* Check for reset */
 		if(cm.getBody()[RESET] == 1) newCharacter.resetCharacter();
 		/* now place character on map */
 		try 
 		{
 			cs.getMap().place
 			(
 				cm.getBody()[POS_X], 
 				cm.getBody()[POS_Y], 
 				new CharacterObjects 
 				(
 					cm.getCharacterData().getName(), 
 					cm.getCharacterData().getImage(), 
 					cm.getBody()[POS_X], 
 					cm.getBody()[POS_Y], 
 					cs.getMap(), 
 					newCharacter
 				)
 			);
 		} /* end try */ 
 		catch (SlickException e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} /* end catch */
 		
 	} /* end interpret method */
 
 } /* end I_PlaceCharacter class */
