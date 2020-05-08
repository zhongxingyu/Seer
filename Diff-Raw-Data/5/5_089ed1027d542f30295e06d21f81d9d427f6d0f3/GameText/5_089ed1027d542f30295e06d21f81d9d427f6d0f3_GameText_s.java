 /**
  * <e-Adventure> is an <e-UCM> research project. <e-UCM>, Department of Software
  * Engineering and Artificial Intelligence. Faculty of Informatics, Complutense
  * University of Madrid (Spain).
  * 
  * @author Del Blanco, A., Marchiori, E., Torrente, F.J. (alphabetical order) *
  * @author Lpez Maas, E., Prez Padilla, F., Sollet, E., Torijano, B. (former
  *         developers by alphabetical order)
  * @author Moreno-Ger, P. & Fernndez-Manjn, B. (directors)
  * @year 2009 Web-site: http://e-adventure.e-ucm.es
  */
 
 /*
  * Copyright (C) 2004-2009 <e-UCM> research group
  * 
  * This file is part of <e-Adventure> project, an educational game & game-like
  * simulation authoring tool, available at http://e-adventure.e-ucm.es.
  * 
  * <e-Adventure> is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * <e-Adventure> is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * <e-Adventure>; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 package es.eucm.eadventure.engine.core.data;
 
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.engine.core.control.Game;
 
 /**
  * This class holds the texts of the game, such as standard player responses,
  * options text, and so on
  */
 public class GameText {
 
     public static boolean showCommentaries( ) {
 
         return Game.getInstance( ).getGameDescriptor( ).isCommentaries( );
     }
 
     /**
      * This method reloads the strings according to the TextConstants. (Required
      * to change dynamically the language of the engine when launched from the
      * editor
      */
     public static void reloadStrings( ) {
 
         TEXT_PLEASE_WAIT = TC.get( "GameText.PleaseWait" );
 
         TEXT_LOADING_DATA = TC.get( "GameText.LoadingData" );
 
         TEXT_LOADING_XML = TC.get( "GameText.LoadingXML" );
 
         TEXT_LOADING_FINISHED = TC.get( "GameText.LoadingFinished" );
 
         TEXT_GO = TC.get( "GameText.Go" );
 
         TEXT_LOOK = TC.get( "GameText.Look" );
 
         TEXT_EXAMINE = TC.get( "GameText.Examine" );
 
         TEXT_GRAB = TC.get( "GameText.Examine" );
 
         TEXT_TALK = TC.get( "GameText.Talk" );
 
         TEXT_GIVE = TC.get( "GameText.Give" );
 
         TEXT_USE = TC.get( "GameText.Use" );
 
         TEXT_AT = TC.get( "GameText.At" );
 
         TEXT_TO = TC.get( "GameText.To" );
 
         TEXT_WITH = TC.get( "GameText.With" );
 
         TEXT_BACK_TO_GAME = TC.get( "GameText.BackToGame" );
 
         TEXT_SAVE_LOAD = TC.get( "GameText.Save/Load" );
 
         TEXT_CONFIGURATION = TC.get( "GameText.Configuration" );
 
         TEXT_GENERATE_REPORT = TC.get( "GameText.GenerateReport" );
 
         TEXT_EXIT_GAME = TC.get( "GameText.ExitGame" );
 
         TEXT_SAVE = TC.get( "GameText.Save" );
 
         TEXT_LOAD = TC.get( "GameText.Load" );
 
         TEXT_EMPTY = TC.get( "GameText.Empty" );
 
         TEXT_MUSIC = TC.get( "GameText.Music" );
 
         TEXT_EFFECTS = TC.get( "GameText.Effects" );
 
         TEXT_ON = TC.get( "GameText.On" );
 
         TEXT_OFF = TC.get( "GameText.Off" );
 
         TEXT_TEXT_SPEED = TC.get( "GameText.TextSpeed" );
 
         TEXT_SLOW = TC.get( "GameText.Slow" );
 
         TEXT_NORMAL = TC.get( "GameText.Normal" );
 
         TEXT_FAST = TC.get( "GameText.Fast" );
 
         TEXT_BACK = TC.get( "GameText.Back" );
 
         TEXT_TALK_OBJECT = new String[] { TC.get( "GameText.TextTalkObject1" ), TC.get( "GameText.TextTalkObject2" ) };
 
         TEXT_TALK_CANNOT = new String[] { TC.get( "GameText.TextTalkCannot1" ), TC.get( "GameText.TextTalkCannot2" ) };
 
         TEXT_GIVE_NPC = new String[] { TC.get( "GameText.TextGiveNPC1" ), TC.get( "GameText.TextGiveNPC2" ) };
 
         TEXT_GIVE_OBJECT_NOT_INVENTORY = new String[] { TC.get( "GameText.TextGiveObjectNotInventory1" ), TC.get( "GameText.TextGiveObjectNotInventory2" ) };
 
         TEXT_GIVE_CANNOT = new String[] { TC.get( "GameText.TextGiveCannot1" ), TC.get( "GameText.TextGiveCannot2" ) };
 
         TEXT_GRAB_NPC = new String[] { TC.get( "GameText.TextGrabNPC1" ), TC.get( "GameText.TextGrabNPC2" ) };
 
         TEXT_GRAB_OBJECT_INVENTORY = new String[] { TC.get( "GameText.TextGrabObjectInventory1" ), TC.get( "GameText.TextGrabObjectInventory2" ) };
 
         TEXT_GRAB_CANNOT = new String[] { TC.get( "GameText.TextGiveCannot1" ), TC.get( "GameText.TextGiveCannot2" ) };
 
         TEXT_USE_NPC = new String[] { TC.get( "GameText.TextUseNPC1" ), TC.get( "GameText.TextUseNPC2" ) };
 
         TEXT_USE_CANNOT = new String[] { TC.get( "GameText.TextUseCannot1" ), TC.get( "GameText.TextUseCannot2" ) };
 
         TEXT_CUSTOM_CANNOT = new String[] { TC.get( "GameText.TextCustomCannot1" ), TC.get( "GameText.TextCustomCannot2" ) };
 
     }
 
     /**
      * Loading text for "Please wait"
      */
     public static String TEXT_PLEASE_WAIT = TC.get( "GameText.PleaseWait" );
 
     /**
      * Loading text for "Loading data"
      */
     public static String TEXT_LOADING_DATA = TC.get( "GameText.LoadingData" );
 
     /**
      * Loading text for "Loading XML"
      */
     public static String TEXT_LOADING_XML = TC.get( "GameText.LoadingXML" );
 
     /**
      * Loading text for "Loading finished"
      */
     public static String TEXT_LOADING_FINISHED = TC.get( "GameText.LoadingFinished" );
 
     //********************************************************************************//
 
     // Text for the actions
 
     /**
      * Action text for "Go"
      */
     public static String TEXT_GO = TC.get( "GameText.Go" );
 
     /**
      * Action text for "Look"
      */
     public static String TEXT_LOOK = TC.get( "GameText.Look" );
 
     /**
      * Action text for "Examine"
      */
     public static String TEXT_EXAMINE = TC.get( "GameText.Examine" );
 
     /**
      * Action text for "Grab"
      */
    public static String TEXT_GRAB = TC.get( "GameText.Examine" );
 
     /**
      * Action text for "Talk"
      */
     public static String TEXT_TALK = TC.get( "GameText.Talk" );
 
     /**
      * Action text for "Give"
      */
     public static String TEXT_GIVE = TC.get( "GameText.Give" );
 
     /**
      * Action text for "Use"
      */
     public static String TEXT_USE = TC.get( "GameText.Use" );
 
     /**
      * Text for "at" (Look at)
      */
     public static String TEXT_AT = TC.get( "GameText.At" );
 
     /**
      * Text for "to" (Go to, Give to, Talk to)
      */
     public static String TEXT_TO = TC.get( "GameText.To" );
 
     /**
      * Text for "with" (Use with)
      */
     public static String TEXT_WITH = TC.get( "GameText.With" );
 
     //********************************************************************************//
 
     // Text for the options
 
     /**
      * Text for the "Back to game" option
      */
     public static String TEXT_BACK_TO_GAME = TC.get( "GameText.BackToGame" );
 
     /**
      * Text for the "Save/Load" option
      */
     public static String TEXT_SAVE_LOAD = TC.get( "GameText.Save/Load" );
 
     /**
      * Text for the "Configuration" option
      */
     public static String TEXT_CONFIGURATION = TC.get( "GameText.Configuration" );
 
     /**
      * Text for the "Generate report" option
      */
     public static String TEXT_GENERATE_REPORT = TC.get( "GameText.GenerateReport" );
 
     /**
      * Text for the "Exit game" option
      */
     public static String TEXT_EXIT_GAME = TC.get( "GameText.ExitGame" );
 
     /**
      * Text for the "Save" option
      */
     public static String TEXT_SAVE = TC.get( "GameText.Save" );
 
     /**
      * Text for the "Load" option
      */
     public static String TEXT_LOAD = TC.get( "GameText.Load" );
 
     /**
      * Text for the "Empty" label in the savegames
      */
     public static String TEXT_EMPTY = TC.get( "GameText.Empty" );
 
     /**
      * Text for the "Music" section of the configuration
      */
     public static String TEXT_MUSIC = TC.get( "GameText.Music" );
 
     /**
      * Text for the "FunctionalEffects" section of the configuration
      */
     public static String TEXT_EFFECTS = TC.get( "GameText.Effects" );
 
     /**
      * Text for the "On" value of the configuration
      */
     public static String TEXT_ON = TC.get( "GameText.On" );
 
     /**
      * Text for the "Off" value of the configuration
      */
     public static String TEXT_OFF = TC.get( "GameText.Off" );
 
     /**
      * Text for the "Text speed" section of the configuration
      */
     public static String TEXT_TEXT_SPEED = TC.get( "GameText.TextSpeed" );
 
     /**
      * Text for the "Slow" speed of the dialogues
      */
     public static String TEXT_SLOW = TC.get( "GameText.Slow" );
 
     /**
      * Text for the "Normal" speed of the dialogues
      */
     public static String TEXT_NORMAL = TC.get( "GameText.Normal" );
 
     /**
      * Text for the "Fast" speed of the dialogues
      */
     public static String TEXT_FAST = TC.get( "GameText.Fast" );
 
     /**
      * Text for the "Back" option
      */
     public static String TEXT_BACK = TC.get( "GameText.Back" );
 
     //********************************************************************************//
 
     // Text for the player to speak
 
     /**
      * Text to display when the character tries to speak with an item
      */
     private static String[] TEXT_TALK_OBJECT = { TC.get( "GameText.TextTalkObject1" ), TC.get( "GameText.TextTalkObject2" ) };
 
     /**
      * Text to display when the character can't talk
      */
     private static String[] TEXT_TALK_CANNOT = { TC.get( "GameText.TextTalkCannot1" ), TC.get( "GameText.TextTalkCannot2" ) };
 
     /**
      * Text to display when the character tries to give another character
      */
     private static String[] TEXT_GIVE_NPC = { TC.get( "GameText.TextGiveNPC1" ), TC.get( "GameText.TextGiveNPC2" ) };
 
     /**
      * Text to display when the character tries to give an item that's not in
      * the inventory
      */
     private static String[] TEXT_GIVE_OBJECT_NOT_INVENTORY = { TC.get( "GameText.TextGiveObjectNotInventory1" ), TC.get( "GameText.TextGiveObjectNotInventory2" ) };
 
     /**
      * Text to display when the character can't give an item
      */
     private static String[] TEXT_GIVE_CANNOT = { TC.get( "GameText.TextGiveCannot1" ), TC.get( "GameText.TextGiveCannot2" ) };
 
     /**
      * Text to display when the character tries to grab another character
      */
     private static String[] TEXT_GRAB_NPC = { TC.get( "GameText.TextGrabNPC1" ), TC.get( "GameText.TextGrabNPC2" ) };
 
     /**
      * Text to display when the character tries to grab an item which is already
      * in the inventory
      */
     private static String[] TEXT_GRAB_OBJECT_INVENTORY = { TC.get( "GameText.TextGrabObjectInventory1" ), TC.get( "GameText.TextGrabObjectInventory2" ) };
 
     /**
      * Text to display when the character can't grab an item
      */
    private static String[] TEXT_GRAB_CANNOT = { TC.get( "GameText.TextGiveCannot1" ), TC.get( "GameText.TextGiveCannot2" ) };
 
     /**
      * Text to display when the character tries to use another character
      */
     private static String[] TEXT_USE_NPC = { TC.get( "GameText.TextUseNPC1" ), TC.get( "GameText.TextUseNPC2" ) };
 
     /**
      * Text to display when the character can't use an item
      */
     private static String[] TEXT_USE_CANNOT = { TC.get( "GameText.TextUseCannot1" ), TC.get( "GameText.TextUseCannot2" ) };
 
     private static String[] TEXT_CUSTOM_CANNOT = { TC.get( "GameText.TextCustomCannot1" ), TC.get( "GameText.TextCustomCannot2" ) };
 
     //********************************************************************************//
 
     /**
      * Private constructor (Static class)
      */
     private GameText( ) {
 
     }
 
     /**
      * Returns a string used when the character tries to speak with an item
      * 
      * @return Random string amongst the present
      */
     public static String getTextTalkObject( ) {
 
         if( showCommentaries( ) )
             return TEXT_TALK_OBJECT[(int) ( TEXT_TALK_OBJECT.length * Math.random( ) )];
         else
             return null;
     }
 
     /**
      * Returns a string used when the character can't talk
      * 
      * @return Random string amongst the present
      */
     public static String getTextTalkCannot( ) {
 
         if( showCommentaries( ) )
             return TEXT_TALK_CANNOT[(int) ( TEXT_TALK_CANNOT.length * Math.random( ) )];
         else
             return null;
     }
 
     /**
      * Returns a string used when the character tries to give another character
      * 
      * @return Random string amongst the present
      */
     public static String getTextGiveNPC( ) {
 
         if( showCommentaries( ) )
             return TEXT_GIVE_NPC[(int) ( TEXT_GIVE_NPC.length * Math.random( ) )];
         else
             return null;
     }
 
     /**
      * Returns a string used when the character tries to give an item that's not
      * in the inventory
      * 
      * @return Random string amongst the present
      */
     public static String getTextGiveObjectNotInventory( ) {
 
         if( showCommentaries( ) )
             return TEXT_GIVE_OBJECT_NOT_INVENTORY[(int) ( TEXT_GIVE_OBJECT_NOT_INVENTORY.length * Math.random( ) )];
         else
             return null;
     }
 
     /**
      * Returns a string used when the character can't give an item
      * 
      * @return Random string amongst the present
      */
     public static String getTextGiveCannot( ) {
 
         if( showCommentaries( ) )
             return TEXT_GIVE_CANNOT[(int) ( TEXT_GIVE_CANNOT.length * Math.random( ) )];
         else
             return null;
     }
 
     /**
      * Returns a string used when the character tries to grab another character
      * 
      * @return Random string amongst the present
      */
     public static String getTextGrabNPC( ) {
 
         if( showCommentaries( ) )
             return TEXT_GRAB_NPC[(int) ( TEXT_GRAB_NPC.length * Math.random( ) )];
         else
             return null;
     }
 
     /**
      * Returns a string used when the character tries to grab an item which is
      * already in the inventory
      * 
      * @return Random string amongst the present
      */
     public static String getTextGrabObjectInventory( ) {
 
         if( showCommentaries( ) )
             return TEXT_GRAB_OBJECT_INVENTORY[(int) ( TEXT_GRAB_OBJECT_INVENTORY.length * Math.random( ) )];
         else
             return null;
     }
 
     /**
      * Returns a string used when the character can't grab an item
      * 
      * @return Random string amongst the present
      */
     public static String getTextGrabCannot( ) {
 
         if( showCommentaries( ) )
             return TEXT_GRAB_CANNOT[(int) ( TEXT_GRAB_CANNOT.length * Math.random( ) )];
         else
             return null;
     }
 
     /**
      * Returns a string used when the character tries to use another character
      * 
      * @return Random string amongst the present
      */
     public static String getTextUseNPC( ) {
 
         if( showCommentaries( ) )
             return TEXT_USE_NPC[(int) ( TEXT_USE_NPC.length * Math.random( ) )];
         else
             return null;
     }
 
     /**
      * Returns a string used when the character can't use an item
      * 
      * @return Random string amongst the present
      */
     public static String getTextUseCannot( ) {
 
         if( showCommentaries( ) )
             return TEXT_USE_CANNOT[(int) ( TEXT_USE_CANNOT.length * Math.random( ) )];
         else
             return null;
     }
 
     public static String getTextCustomCannot( ) {
 
         if( showCommentaries( ) )
             return TEXT_CUSTOM_CANNOT[(int) ( TEXT_USE_CANNOT.length * Math.random( ) )];
         else
             return null;
     }
 }
