 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  *         research group.
  *  
  *   Copyright 2005-2010 <e-UCM> research group.
  * 
  *   You can access a list of all the contributors to <e-Adventure> at:
  *         http://e-adventure.e-ucm.es/contributors
  * 
  *   <e-UCM> is a research group of the Department of Software Engineering
  *         and Artificial Intelligence at the Complutense University of Madrid
  *         (School of Computer Science).
  * 
  *         C Profesor Jose Garcia Santesmases sn,
  *         28040 Madrid (Madrid), Spain.
  * 
  *         For more info please visit:  <http://e-adventure.e-ucm.es> or
  *         <http://www.e-ucm.es>
  * 
  * ****************************************************************************
  * 
  * This file is part of <e-Adventure>, version 1.2.
  * 
  *     <e-Adventure> is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU Lesser General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     <e-Adventure> is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU Lesser General Public License for more details.
  * 
  *     You should have received a copy of the GNU Lesser General Public License
  *     along with <e-Adventure>.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.engine.core.gui.hud.contextualhud;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.font.TextAttribute;
 import java.util.Hashtable;
 
 import es.eucm.eadventure.common.data.adventure.DescriptorData;
 import es.eucm.eadventure.common.data.chapter.Action;
 import es.eucm.eadventure.common.data.chapter.elements.Item;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.engine.core.control.ActionManager;
 import es.eucm.eadventure.engine.core.control.DebugLog;
 import es.eucm.eadventure.engine.core.control.Game;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalActiveArea;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalElement;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalItem;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalNPC;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalPlayer;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalScene;
 import es.eucm.eadventure.engine.core.gui.GUI;
 import es.eucm.eadventure.engine.core.gui.hud.HUD;
 import es.eucm.eadventure.engine.multimedia.MultimediaManager;
 
 public class ContextualHUD extends HUD {
 
     /**
      * Width of the playable area of the screen
      */
     private final int GAME_AREA_WIDTH = 800;
 
     /**
      * Height of the playable area of the screen
      */
     private final int GAME_AREA_HEIGHT = 600;
 
     /**
      * Left most point of the response text block
      */
     private static final int RESPONSE_TEXT_X = 10;
 
     /**
      * Upper most point of the upper response text block
      */
     private static final int UPPER_RESPONSE_TEXT_Y = 10;
 
     /**
      * Upper most point of the bottom response text block
      */
     private static final int BOTTOM_RESPONSE_TEXT_Y = 480;
 
     /**
      * Number of response lines to display
      */
     private static final int RESPONSE_TEXT_NUMBER_LINES = 5;
 
     /**
      * Offset to check if the mouse is in it when the inventory is not shown to
      * show it
      */
     private static final int INVENTORY_OFF_OFFSET = Inventory.INVENTORY_PANEL_HEIGHT * 3 / 4;
 
     /**
      * Offset to check if the mouse is not in it nor in the inventory to hide it
      */
     private static final int INVENTORY_ON_OFFSET = Inventory.INVENTORY_PANEL_HEIGHT * 1 / 4;
 
     /**
      * Action buttons
      */
     private ActionButtons actionButtons;
 
     /**
      * Inventory
      */
     private Inventory inventory;
 
     /**
      * Whether the inventory is shown
      */
     private boolean showInventory;
 
     /**
      * Whether the action buttons are shown
      */
     private boolean showActionButtons;
 
     /**
      * Element to do the actions to
      */
     private FunctionalElement elementAction;
 
     /**
      * Element selected and current cursor
      */
     private FunctionalElement elementInCursor;
 
     /**
      * Cursor for use when it's over a element
      */
     private Cursor cursorOver;
 
     /**
      * Cursor for use when it's over an exit
      */
     private Cursor cursorExit;
 
     /**
      * Cursor for use when it's over am action button
      */
     private Cursor cursorAction;
 
     /**
      * Last mouse event in the hud
      */
     private MouseEvent lastMouseMoved;
 
     private long pressedTime = Long.MAX_VALUE;
 
     private int pressedX;
 
     private int pressedY;
 
     private FunctionalElement pressedElement;
     
     private FunctionalElement draggingElement;
     
     private float originalDragX;
     
     private float originalDragY;
 
     private boolean mouseReleased = false;
 
     private boolean pressed = false;
     
     /**
      * This attribute is used to store the last action made to add the action text in interactions (give to, drag to, etc..)
      */
     private int lastSelectedAction=-1;
     
     /**
      * Function that initializa the HUD class
      */
     @Override
     public void init( ) {
 
         super.init( );
         DebugLog.general( "Using contextual HUD" );
 
         actionButtons = new ActionButtons( false );
         inventory = new Inventory( );
 
         showInventory = false;
         showActionButtons = false;
         elementAction = null;
 
         lastMouseMoved = null;
 
         DescriptorData descriptor = Game.getInstance( ).getGameDescriptor( );
 
         cursorOver = getCursor( descriptor, DescriptorData.CURSOR_OVER, "cursorOver", "gui/cursors/over.png" );
         cursorExit = getCursor( descriptor, DescriptorData.EXIT_CURSOR, "cursorExit", "gui/cursors/exit.png" );
         cursorAction = getCursor( descriptor, DescriptorData.CURSOR_ACTION, "cursorAction", "gui/cursors/action.png" );
 
     }
 
     private Cursor getCursor( DescriptorData descriptor, String cursor, String type, String defaultPath ) {
 
         Cursor temp = null;
         if( descriptor.getCursorPath( cursor ) == null )
             try {
                 temp = Toolkit.getDefaultToolkit( ).createCustomCursor( MultimediaManager.getInstance( ).loadImage( defaultPath, MultimediaManager.IMAGE_MENU ), new Point( 5, 5 ), type );
             }
             catch( Exception e ) {
                 DebugLog.general( "Cound't find default cursor " + cursor );
                 temp = Toolkit.getDefaultToolkit( ).createCustomCursor( MultimediaManager.getInstance( ).loadImage( "gui/cursors/nocursor.png", MultimediaManager.IMAGE_MENU ), new Point( 5, 5 ), type );
             }
         else {
             try {
                 temp = Toolkit.getDefaultToolkit( ).createCustomCursor( MultimediaManager.getInstance( ).loadImageFromZip( descriptor.getCursorPath( cursor ), MultimediaManager.IMAGE_MENU ), new Point( 5, 5 ), type );
             }
             catch( Exception e ) {
                 DebugLog.general( "Cound't find custom cursor " + cursor );
                 temp = Toolkit.getDefaultToolkit( ).createCustomCursor( MultimediaManager.getInstance( ).loadImage( "gui/cursors/nocursor.png", MultimediaManager.IMAGE_MENU ), new Point( 5, 5 ), type );
             }
         }
         return temp;
     }
 
     /*
      * (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.gui.hud.HUD#getGameAreaWidth()
      */
     @Override
     public int getGameAreaWidth( ) {
 
         return GAME_AREA_WIDTH;
     }
 
     /*
      * (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.gui.hud.HUD#getGameAreaHeight()
      */
     @Override
     public int getGameAreaHeight( ) {
 
         return GAME_AREA_HEIGHT;
     }
 
     /*
      * (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.gui.hud.HUD#getResponseTextX()
      */
     @Override
     public int getResponseTextX( ) {
 
         return RESPONSE_TEXT_X;
     }
 
     /*
      * (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.gui.hud.HUD#getResponseTextY()
      */
     @Override
     public int getResponseTextY( ) {
 
         int responseTextY;
         FunctionalPlayer player = Game.getInstance( ).getFunctionalPlayer( );
 
         // Show the response block in the upper or bottom of the screen, depending on the player's position
         if( player.getY( ) - player.getHeight( ) > GUI.WINDOW_HEIGHT / 2 )
             responseTextY = UPPER_RESPONSE_TEXT_Y;
         else
             responseTextY = BOTTOM_RESPONSE_TEXT_Y;
 
         return responseTextY;
     }
 
     /*
      * (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.gui.hud.HUD#getResponseTextNumberLines()
      */
     @Override
     public int getResponseTextNumberLines( ) {
 
         return RESPONSE_TEXT_NUMBER_LINES;
     }
 
     /*
      * (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.gui.hud.HUD#newActionSelected()
      */
     @Override
     public void newActionSelected( ) {
 
     }
 
     /*
      *  (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.gui.hud.HUD#mouseMoved(java.awt.event.MouseEvent)
      */
     @Override
     public boolean mouseMoved( MouseEvent e ) {
         boolean inHud = false;
         if (!pressed)
             inHud =  moved(e);
         if (pressed)
             inHud = dragged(e);
         
         lastMouseMoved = e;
 
         return inHud;
     }
     
     private boolean moved(MouseEvent e) {
         boolean inHud = false;
 
         actionButtons.mouseMoved( null );
         if( showActionButtons ) {
             actionButtons.mouseMoved( e );
             inHud = actionButtons.getButtonOver( ) != null;
         }
         else if( showInventory ) {
             inHud = processInventoryMove(e, inHud);
         } else if( e.getY( ) > GUI.WINDOW_HEIGHT - INVENTORY_OFF_OFFSET && Game.getInstance( ).showBottomInventory( ) ) {
                 inventory.setUpperInventory( false );
                 showInventory = true;
         } else if( e.getY( ) < INVENTORY_OFF_OFFSET && Game.getInstance( ).showTopInventory( ) ) {
                 inventory.setUpperInventory( true );
                 showInventory = true;
         }
 
         return inHud;
     }
     
     private boolean processInventoryMove(MouseEvent e, boolean inHud) {
         if( inventory.isUpperInventory( ) ) {
             if( e.getY( ) < Inventory.UPPER_INVENTORY_PANEL_Y + Inventory.INVENTORY_PANEL_HEIGHT ) {
                 inventory.mouseMoved( e );
                 return true;
             }
             else if( e.getY( ) > Inventory.UPPER_INVENTORY_PANEL_Y + Inventory.INVENTORY_PANEL_HEIGHT + INVENTORY_ON_OFFSET ) 
                 showInventory = false;
         }
         else {
             if( e.getY( ) > Inventory.BOTTOM_INVENTORY_PANEL_Y ) {
                 inventory.mouseMoved( e );
                 return true;
             }
             else if( e.getY( ) < Inventory.BOTTOM_INVENTORY_PANEL_Y - INVENTORY_ON_OFFSET )
                 showInventory = false;
         }
         return inHud;
     }
     
     private boolean dragged(MouseEvent e) {
         if( System.currentTimeMillis( ) - pressedTime >= 0 && System.currentTimeMillis( ) - pressedTime <= 60000 && Math.abs( pressedX - e.getX( ) ) < 10 && Math.abs( pressedY - e.getY( ) ) < 10 )
             return true;
 
         if (pressedElement != null && pressedElement.canBeDragged()) {
             return startDragging(pressedElement);
         } else {
             gui.setDefaultCursor( );
             return dragElement(e);
        }        
     }
     
     public boolean startDragging(FunctionalElement pressedElement2) {
         elementAction = pressedElement2;
         elementInCursor = elementAction;
         game.getActionManager( ).setActionSelected( ActionManager.ACTION_DRAG_TO );
         game.getFunctionalPlayer( ).performActionInElement( elementAction );
         draggingElement = pressedElement2;
         originalDragX = draggingElement.getX( );
         originalDragY = draggingElement.getY( );
         pressedElement = null;
         pressedTime = Long.MAX_VALUE;
         pressed = true;
         return true;
     }
     
     public boolean dragElement(MouseEvent e) {
         FunctionalScene functionalScene = game.getFunctionalScene( );
         if( functionalScene != null ) {
             FunctionalElement elementInside = functionalScene.getElementInside( e.getX( ), e.getY( ), draggingElement );
             game.getActionManager( ).setElementOver( elementInside );
         }
         pressedTime = Long.MAX_VALUE;
         if (draggingElement != null) {
             draggingElement.setX( originalDragX - pressedX + e.getX() );
             draggingElement.setY( originalDragY - pressedY + e.getY( ) );
         }
         return true;
     }
 
     /*
      *  (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.gui.hud.HUD#mouseClicked(java.awt.event.MouseEvent)
      */
     @Override
     public boolean mouseClicked( MouseEvent e ) {
 
         boolean inHud = false;
 
         if( mouseReleased ) {
             mouseReleased = false;
             return true;
         }
         pressedTime = Long.MAX_VALUE;
 
         if( draggingElement != null  && game.getFunctionalPlayer( ).getCurrentAction( ).getType( ) == Action.DRAG_TO) {
             return processDragTo(e);
         }
 
         ActionManager actionManager = game.getActionManager( );
 
         if( !game.isShowActions() &&  e.getButton( ) != MouseEvent.BUTTON1 && e.getButton( ) != MouseEvent.BUTTON3 )
             return false;
 
         boolean button = false;
         if( showActionButtons ) {
             actionButtons.mouseClicked( e );
             if( actionButtons.getButtonPressed( ) != null )
                 button = true;
         }
         
         Game game = Game.getInstance( );
         FunctionalScene functionalScene = game.getFunctionalScene( );
         FunctionalElement elementInside = functionalScene.getElementInside( e.getX( ), e.getY( ), null );

         if( ( !button && (e.getButton( ) == MouseEvent.BUTTON3 || game.isShowActions( )) && elementInCursor == null ) || 
                 ( e.getClickCount( ) == 2 && System.getProperty( "os.name" ).contains( "Windows" ) ) || 
                 ( !button && elementInside == null && elementInCursor != null ) ) {
             System.out.println( "RIGHT CLICK o similar" );
             inHud = processRightClickNoButton( pressedElement, e );
             DebugLog.user( "Mouse click, no action button. " + e.getX( ) + " , " + e.getY( ) );
         }
 
         else if( showActionButtons ) {
             actionButtons.mouseClicked( e );
             if( actionButtons.getButtonPressed( ) != null ) {
                 DebugLog.user( "Mouse click, inside action button: " + actionButtons.getButtonPressed( ).getName( ) );
                 inHud = processButtonPressed( actionManager, e );
                 showActionButtons = false;
                 elementAction = null;
             }
         }
 
         else if( showInventory && ( e.getY( ) > Inventory.BOTTOM_INVENTORY_PANEL_Y || 
                 e.getY( ) < Inventory.UPPER_INVENTORY_PANEL_Y + Inventory.INVENTORY_PANEL_HEIGHT ) ) {
             DebugLog.user( "Mouse click in inventory" );
             inHud = processInventoryClick( actionManager, e );
             showActionButtons = false;
             elementAction = null;
         }
 
         else if( /*actionManager.getElementOver( )*/ pressedElement != null ) {
             DebugLog.user( "Mouse click over element at " + e.getX( ) + " , " + e.getY( ) );
             inHud = processElementClick( actionManager );
             System.out.println( "INHUD = " + inHud );
             showActionButtons = false;
             elementAction = null;
         }
 
         if( !inHud ) {
             showActionButtons = false;
             elementAction = null;
         }
 
         return inHud;
     }
 
     @Override
     public boolean mousePressed( MouseEvent e ) {
         pressed = true;
         
         ActionManager actionManager = game.getActionManager( );
         pressedTime = System.currentTimeMillis( );
         pressedX = e.getX( );
         pressedY = e.getY( );
         pressedElement = actionManager.getElementOver( );
 
         DebugLog.user( "Mouse pressed at " + e.getX( ) + " , " + e.getY( ) );
 
         return true;
     }
     
     private void clearDraggingElement() {
         if(draggingElement != null) {
             if (draggingElement.getElement( ) instanceof Item) {
                 if (((Item) draggingElement.getElement( )).isReturnsWhenDragged( )) {
                     draggingElement.setX( originalDragX );
                     draggingElement.setY( originalDragY );
                 }
             }
         }
         draggingElement = null;
     }
 
     @Override
     public boolean mouseReleased( MouseEvent e ) {
         pressed = false;
         
         mouseReleased = true;
         if( pressedElement == null ) {
             if( draggingElement != null  && game.getFunctionalPlayer( ).getCurrentAction( ).getType( ) == Action.DRAG_TO) {
                 return processDragTo(e);
             }
             mouseReleased = false;
             return true;
         }
 
         pressedTime = System.currentTimeMillis( ) - pressedTime;
         System.out.println("" + pressedTime);
         
         DebugLog.user( "Mouse released after " + pressedTime );
 
         if( pressedTime >= 800 && pressedTime < 60000 ) {
             if( Math.abs( pressedX - e.getX( ) ) < 20 && Math.abs( pressedY - e.getY( ) ) < 20 ) {
                 processRightClickNoButton( pressedElement, e );
                 pressedTime = Long.MAX_VALUE;
                 return true;
             }
         }
         else if( pressedTime >= 0 && pressedTime < 600 ) {
             if( Math.abs( pressedX - e.getX( ) ) < 30 && Math.abs( pressedY - e.getY( ) ) < 30 /*&& pressedX != e.getX( ) && pressedY != e.getY( )*/ ) {
                 mouseReleased = false;
                 pressedTime = Long.MAX_VALUE;
                 if (pressedTime >= 80 && pressedX != e.getX( ) && pressedY != e.getY( )) {
                     System.out.println( "Emulate left click" );
                     MouseEvent d = new MouseEvent( e.getComponent( ), e.getID( ), e.getWhen( ), e.getModifiers( ), pressedX, pressedY, e.getXOnScreen( ), e.getYOnScreen( ), 1, false, MouseEvent.BUTTON1 );
                     return this.mouseClicked( d );
                 }
                 return true;
             }
         }
 
         pressedTime = Long.MAX_VALUE;
 
         return false;
     }
 
     private boolean processDragTo( MouseEvent e ) {
         game.getActionManager( ).setActionSelected( ActionManager.ACTION_DRAG_TO );
         FunctionalScene functionalScene = game.getFunctionalScene( );
         if( functionalScene == null ) {
             clearDraggingElement();
             return false;
         }
         FunctionalElement elementInside = functionalScene.getElementInside( e.getX( ), e.getY( ), draggingElement );
         
         if (elementInside == null)
             game.getFunctionalPlayer( ).cancelActions( );
         else
             game.getFunctionalPlayer( ).performActionInElement( elementInside );
         elementInCursor = null;
         gui.setDefaultCursor( );
         pressedTime = Long.MAX_VALUE;
         clearDraggingElement();
         return true;
     }
 
     @Override
     public boolean mouseDragged( MouseEvent e ) {
         return true;
     }
 
     
     
     @Override
     public boolean keyTyped( KeyEvent e ) {
 
         if( e.getKeyCode( ) == KeyEvent.VK_ESCAPE ) {
             ActionManager manager = Game.getInstance( ).getActionManager( );
             if( !showActionButtons && manager.getElementOver( ) == null && elementInCursor != null ) {
                 elementInCursor = null;
                 gui.setDefaultCursor( );
                 elementAction = null;
                 showActionButtons = false;
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Method called when an element is clicked
      * 
      * @param actionManager
      *            The actionManager of the Game
      * @return Value of inHud
      */
     private boolean processElementClick( ActionManager actionManager ) {
 
         if( elementInCursor != null ) {
             if( game.getFunctionalPlayer( ).getCurrentAction( ).getType( ) == Action.CUSTOM_INTERACT ) {
                 actionManager.setActionSelected( ActionManager.ACTION_CUSTOM_INTERACT );
             }
             else if (game.getFunctionalPlayer( ).getCurrentAction( ).getType( ) == Action.DRAG_TO) {
                 actionManager.setActionSelected( ActionManager.ACTION_DRAG_TO );
             }
             else {
                 if( pressedElement.canPerform( ActionManager.ACTION_GIVE_TO ) ) {
                     actionManager.setActionSelected( ActionManager.ACTION_GIVE );
                     game.getFunctionalPlayer( ).performActionInElement( elementInCursor );
                     actionManager.setActionSelected( ActionManager.ACTION_GIVE_TO );
                 }
                 else {
                     actionManager.setActionSelected( ActionManager.ACTION_USE );
                     game.getFunctionalPlayer( ).performActionInElement( elementInCursor );
                     actionManager.setActionSelected( ActionManager.ACTION_USE_WITH );
                 }
             }
             game.getFunctionalPlayer( ).performActionInElement( pressedElement );
             elementInCursor = null;
             gui.setDefaultCursor( );
         }
         else {
             actionManager.setActionSelected( ActionManager.ACTION_LOOK );
             game.getFunctionalPlayer( ).performActionInElement( pressedElement );
         }
         return true;
     }
 
     /**
      * Method called when the click is inside the inventory
      * 
      * @param actionManager
      *            The actionManager of the Game
      * @param e
      *            The MouseEvent of the click
      * @return Value of inHud
      */
     private boolean processInventoryClick( ActionManager actionManager, MouseEvent e ) {
 
         FunctionalElement element = inventory.mouseClicked( e );
         if( elementInCursor != null ) {
             if( element != null ) {
                 actionManager.setActionSelected( ActionManager.ACTION_USE );
                 game.getFunctionalPlayer( ).performActionInElement( elementInCursor );
                 actionManager.setActionSelected( ActionManager.ACTION_USE_WITH );
                 game.getFunctionalPlayer( ).performActionInElement( element );
                 elementInCursor = null;
                 gui.setDefaultCursor( );
             }
         }
         else if( element != null ) {
             actionManager.setActionSelected( ActionManager.ACTION_LOOK );
             game.getFunctionalPlayer( ).performActionInElement( element );
         }
         return true;
     }
 
     /**
      * Method called when an action button is clicked
      * 
      * @param actionManager
      *            The actionManager of the Game
      * @param e
      *            The MouseEvent of the click
      * @return Value of inHud
      */
     private boolean processButtonPressed( ActionManager actionManager, MouseEvent e ) {
         lastSelectedAction=-1;
         switch( actionButtons.getButtonPressed( ).getType( ) ) {
             case ActionButton.HAND_BUTTON:
                 elementInCursor = null;
                 gui.setDefaultCursor( );
                 if( elementAction.canBeUsedAlone( ) ) {
                     actionManager.setActionSelected( ActionManager.ACTION_USE );
                     game.getFunctionalPlayer( ).performActionInElement( elementAction );
                 }
                 else {
                     if( !elementAction.isInInventory( ) ) {
                         actionManager.setActionSelected( ActionManager.ACTION_GRAB );
                         game.getFunctionalPlayer( ).performActionInElement( elementAction );
                     }
                     else {
                         if (actionButtons.getButtonPressed( ).getName( ).equals( TC.get( "ActionButton.GiveTo" ) ))
                             lastSelectedAction = ActionManager.ACTION_GIVE_TO;
                         else
                             lastSelectedAction = ActionManager.ACTION_USE_WITH;
                         elementInCursor = elementAction;
                         Image image = null;
                         if (( (FunctionalItem) elementInCursor ).getIconImage( ) == null)
                             image = MultimediaManager.getInstance( ).loadImage( "gui/hud/contextual/btnError.png", MultimediaManager.IMAGE_MENU );
                         else 
                             image = ( (FunctionalItem) elementInCursor ).getIconImage( );
                         
                         gui.setCursor( Toolkit.getDefaultToolkit( ).createCustomCursor( image , new Point( 5, 5 ), "elementInCursor" )) ;
                     }
                 }
                 break;
             case ActionButton.EYE_BUTTON:
                 actionManager.setActionSelected( ActionManager.ACTION_EXAMINE );
                 game.getFunctionalPlayer( ).performActionInElement( elementAction );
                 break;
             case ActionButton.MOUTH_BUTTON:
                 actionManager.setActionSelected( ActionManager.ACTION_TALK );
                 game.getFunctionalPlayer( ).performActionInElement( elementAction );
                 lastSelectedAction = ActionManager.ACTION_TALK;
                 break;
             case ActionButton.DRAG_BUTTON:
                 lastSelectedAction = ActionManager.ACTION_DRAG_TO;
                 elementInCursor = elementAction;
                 this.startDragging( elementInCursor );
                 this.draggingElement.setX( pressedX );
                 this.draggingElement.setY( pressedY + this.draggingElement.getHeight( ) * this.draggingElement.getScale( ) / 2);
                 pressedX = (int) (this.originalDragX + this.draggingElement.getWidth( ) * this.draggingElement.getScale( ) / 4 );
                 pressedY = (int) (this.originalDragY - this.draggingElement.getHeight( ) * this.draggingElement.getScale( ) / 2);
                 break;
             case ActionButton.CUSTOM_BUTTON:
                 if( actionButtons.getButtonPressed( ).getCustomAction( ).getType( ) == Action.CUSTOM ) {
                     actionManager.setActionSelected( ActionManager.ACTION_CUSTOM );
                     actionManager.setCustomActionName( actionButtons.getButtonPressed( ).getName( ) );
                     game.getFunctionalPlayer( ).performActionInElement( elementAction );
                     break;
                 }
                 else {
                     lastSelectedAction = ActionManager.ACTION_CUSTOM_INTERACT;
                     elementInCursor = elementAction;
                     Image image = null;
                     if (( (FunctionalItem) elementInCursor ).getIconImage( ) == null)
                         image = MultimediaManager.getInstance( ).loadImage( "gui/hud/contextual/btnError.png", MultimediaManager.IMAGE_MENU );
                     else 
                         image = ( (FunctionalItem) elementInCursor ).getIconImage( );
                     
                     gui.setCursor( Toolkit.getDefaultToolkit( ).createCustomCursor( image, new Point( 5, 5 ), "elementInCursor" ) );
                     actionManager.setActionSelected( ActionManager.ACTION_CUSTOM_INTERACT );
                     actionManager.setCustomActionName( actionButtons.getButtonPressed( ).getName( ) );
                     game.getFunctionalPlayer( ).performActionInElement( elementAction );
                     break;
                 }
         }
         actionManager.setActionSelected( ActionManager.ACTION_GOTO );
         return true;
     }
 
     /**
      * Method called when there is a right click that is in no action button
      * 
      * @param actionManager
      *            The actionManager of the Game
      * @param e
      *            The MouseEvent of the click
      * @return Value of inHud
      */
     private boolean processRightClickNoButton( FunctionalElement elementOver, MouseEvent e ) {
 
         elementInCursor = null;
         gui.setDefaultCursor( );
         System.out.println( ( ( elementOver != null ) ? elementOver.getElement( ).getId( ) : "" ) + " " + e.getID( ) + " " );
         if( elementOver != null ) {
             elementAction = elementOver;
             actionButtons.recreate( e.getX( ), e.getY( ), elementAction );
             showActionButtons = true;
             return true;
         }
         else {
             elementAction = null;
             showActionButtons = false;
             return false;
         }
 
     }
 
     /**
      * Draw the HUD with the action button, action and element selected
      * 
      * @param g
      *            Graphics2D where will be drawn
      */
     @Override
     public void draw( Graphics2D g ) {
 
         // Get the action manager
         ActionManager actionManager = game.getActionManager( );
 
         //draw the inventory
         inventory.draw( g );
 
         //If the action buttons are shown
         if( showActionButtons )
             //draw them
             actionButtons.draw( g );
 
         g.setColor( Color.BLUE );
 
         if( System.currentTimeMillis( ) - pressedTime > 100 ) {
             long time = System.currentTimeMillis( ) - pressedTime;
 
             if( time < 800 ) {
                 Composite alphaComposite = AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.4f );
                 g.setComposite( alphaComposite );
                 int size = 6 + (int) ( 8.0f * time / 800.0f );
                 g.fillArc( pressedX - size, pressedY - size, size * 2, size * 2, 0, -(int) ( 360.0 / 800.0 * time ) );
             }
             else {
                 Composite alphaComposite = AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.4f + 0.5f * (float) Math.pow( 1.0f - ( time % 600 ) / 300.0f, 2 ) );
                 g.setComposite( alphaComposite );
                 g.fillOval( pressedX - 16, pressedY - 16, 32, 32 );
             }
 
             g.setComposite( AlphaComposite.DstAtop );
         }
 
         //set the font and color for the text (tooltip)
         g.setFont( new Font( null, Font.BOLD, 16 ) );
         Hashtable attributes = new Hashtable();
         attributes.put(TextAttribute.WIDTH, TextAttribute.WIDTH_SEMI_EXTENDED);
         g.setFont( g.getFont( ).deriveFont( attributes ) );
         g.setColor( Color.WHITE );
 
         //If there is no element selected
         if( elementInCursor == null ) {
             //If there mouse is over an exit
             if( !actionManager.getExit( ).equals( "" ) ) {
                 //change the cursor for the exit one
                 if( actionManager.getExitCursor( ) != null )
                     gui.setCursor( actionManager.getExitCursor( ) );
                 else
                     gui.setCursor( cursorExit );
                 //If there is a known mouse position to be used for the position of the text
                 if( lastMouseMoved != null )
                     //draw the name of the exit into the mouse in the last mouse position
                     GUI.drawStringOnto( g, new String[] { actionManager.getExit( ) }, lastMouseMoved.getX( ) + 16, lastMouseMoved.getY( ), Color.WHITE, Color.BLACK );
             }
             //esle if the mouse is over an element
             else if( actionManager.getElementOver( ) != null ) {
                 //change the cursor for the over an element one
                 gui.setCursor( cursorOver );
                 //If there is a known mouse position to be used for the position of the text
                 if( lastMouseMoved != null )
                     //draw the name of the element into the mouse in the last mouse position
                     GUI.drawStringOnto( g, new String[] { actionManager.getElementOver( ).getElement( ).getName( ) }, lastMouseMoved.getX( ) + 16, lastMouseMoved.getY( ), Color.WHITE, Color.BLACK );
             }
             //else (the mouse is over and action button or nothing
             else {
                 //If the action buttons are shown and the mouse is over one of them
                 if( showActionButtons && actionButtons.getButtonOver( ) != null )
                     //change the cursor for the action button cursor one
                     gui.setCursor( cursorAction );
                 //else, the mouse is over nothing 
                 else
                     //set the default cursor
                     gui.setDefaultCursor( );
             }
         }
         //else if there is element selected and the mouse is over an element
         else if( actionManager.getElementOver( ) != null ) {
             //If there is a known mouse position to be used for the position of the text
             if( lastMouseMoved != null ){
                 String name = "";
                 boolean specialAction = false;
                 if (lastSelectedAction ==ActionManager.ACTION_CUSTOM_INTERACT){
                     name = actionManager.getCustomActionName( );
                     specialAction = true;
                 }
                 if (lastSelectedAction==ActionManager.ACTION_GIVE_TO){
                     name = TC.get( "ActionButton.GiveTo" );
                     specialAction = true;
                 }
                 if (lastSelectedAction==ActionManager.ACTION_USE_WITH){
                     name = TC.get( "ActionButton.UseWith" );
                     specialAction = true;
                 }
                  
                 if (lastSelectedAction==ActionManager.ACTION_DRAG_TO){
                     name = TC.get( "ActionButton.Drag" );
                     specialAction = true;
                 }
                 if (specialAction){
                     if (actionManager.getElementOver( ).getElement( ).getName( ).equals( "" ))
                         name += " " + processElement();
                     else    
                         name += " " + actionManager.getElementOver( ).getElement( ).getName( );
                 }else
                     name = actionManager.getElementOver( ).getElement( ).getName( );
                 
                 //draw the name of the element into the mouse in the last mouse position
                 GUI.drawStringOnto( g, new String[] { name }, lastMouseMoved.getX( ) + 16, lastMouseMoved.getY( ), Color.WHITE, Color.BLACK );
             
             }
             
             }
 
     }
 
     
     private String processElement(){
         FunctionalElement element = game.getActionManager( ).getElementOver( );
         if (element instanceof FunctionalNPC)
             return TC.get( "DefaultText.NPC" );
         else if (element instanceof FunctionalActiveArea)
             return TC.get( "DefaultText.ActiveArea" );
         else if (element instanceof FunctionalItem)
             return TC.get( "DefaultText.Item" );
         else 
         return "";
     }
     
     /*
      *  (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.gui.hud.HUD#update(long)
      */
     @Override
     public void update( long elapsedTime ) {
 
         //update the inventory
         inventory.update( elapsedTime );
         //If the hud is shown and the inventory must show
         if( showHud && showInventory ) {
             //show the inventory
             inventory.setDY( inventory.getDY( ) - elapsedTime / 10.0 );
         }
         else {
             //else hide it
             inventory.setDY( inventory.getDY( ) + elapsedTime / 10.0 );
         }
         NUPDATES++;
     }
 
     /*
      * (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.gui.hud.HUD#toggleHud(boolean)
      */
     @Override
     public void toggleHud( boolean show ) {
 
         showHud = show;
         elementInCursor = null;
     }
 
     private static int NUPDATES = 0;
 
     @Override
     public void setLastMouseMove(MouseEvent e) {
         this.lastMouseMoved = e;
     }
     
 }
