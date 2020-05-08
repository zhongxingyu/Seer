 /*
  * This file is part of OpenBASE.
  *
  * Copyright © 2012, Kyle Repinski
  * OpenBASE is licensed under the GNU Lesser General Public License.
  *
  * OpenBASE is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * OpenBASE is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package mwisbest.openbase;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 import mwisbest.openbase.event.Event;
 import mwisbest.openbase.event.EventManager;
 import mwisbest.openbase.event.gui.ButtonClickEvent;
 import mwisbest.openbase.event.input.KeyboardEvent;
 import mwisbest.openbase.event.input.MouseEvent;
 import mwisbest.openbase.gui.Button;
 import mwisbest.openbase.gui.RenderPriority;
 import mwisbest.openbase.gui.Widget;
 import mwisbest.openbase.gui.WidgetType;
 import mwisbest.openbase.input.KeyboardKey;
 import mwisbest.openbase.input.KeyboardKeyState;
 import mwisbest.openbase.input.MouseButton;
 import mwisbest.openbase.input.MouseButtonState;
 import mwisbest.openbase.resource.ResourceManager;
 
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.openal.SoundStore;
 
 /**
  * Common code that OpenBASE and OpenBASEApplet share.
  * 
  * This was put in a separate class to make it easier to
  * manage both Application and Applet code.
  */
 public class Common
 {
 	/**
 	 * Common code for rendering the Widgets to the screen.
 	 * After this, customRender() is used from OpenBASE or
 	 * OpenBASEApplet (whatever is calling this method).
 	 */
 	protected static void render()
 	{
 		GL11.glClear( GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );
 		GL11.glMatrixMode( GL11.GL_MODELVIEW );
 		GL11.glLoadIdentity();
 		Map<String, Widget> theWidgets = ResourceManager.getWidgets();
 		for( RenderPriority priority : RenderPriority.values() )
 		{
 			for( Entry<String, Widget> widget : theWidgets.entrySet() )
 			{
 				if( widget.getValue().getRenderPriority() == priority && widget.getValue().getVisible() ) widget.getValue().render();
 			}
 		}
 	}
 	
 	/**
 	 * Common code for the audio system.
 	 * After this, customAudio() is used from OpenBASE or
 	 * OpenBASEApplet (whatever is calling this method).
 	 */
 	protected static void audio()
 	{
 		SoundStore.get().poll( 0 );
 	}
 	
 	/**
 	 * Common code for input. This notifies Mouse and Keyboard events.
 	 * After this, customInput() is used from OpenBASE or
 	 * OpenBASEApplet (whatever is calling this method).
 	 */
 	protected static void input()
 	{
 		while( Mouse.next() )
 		{
			Event mouseEvent = new MouseEvent( MouseButton.getButton( Mouse.getEventButton() ), MouseButtonState.getButtonState( Mouse.getEventButtonState() ), Mouse.getEventX(), Display.getHeight() - Mouse.getEventY(), Mouse.getEventDWheel() );
 			EventManager.callEvent( mouseEvent );
 			if( MouseButton.getButton( Mouse.getEventButton() ) == MouseButton.BUTTON_LEFT && MouseButtonState.getButtonState( Mouse.getEventButtonState() ) == MouseButtonState.PRESSED )
 			{
 				for( Entry<String, Widget> widget : ResourceManager.getWidgets().entrySet() )
 				{
 					if( widget.getValue().getWidgetType() == WidgetType.BUTTON && widget.getValue().getVisible() )
 					{
 						Button theWidget = (Button)widget.getValue();
						if( theWidget.isInside( Mouse.getEventX(), Display.getHeight() - Mouse.getEventY() ) )
 						{
 							Event buttonClickEvent = new ButtonClickEvent( theWidget );
 							EventManager.callEvent( buttonClickEvent );
 						}
 					}
 				}
 			}
 		}
 		while( Keyboard.next() )
 		{
 			Event keyboardEvent = new KeyboardEvent( KeyboardKey.getKey( Keyboard.getEventKey() ), KeyboardKeyState.getKeyState( Keyboard.getEventKeyState() ), Keyboard.isRepeatEvent() );
 			EventManager.callEvent( keyboardEvent );
 		}
 	}
 }
