 /**
  * Copyright (c) 2007-2008, JAGaToo Project Group all rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * 
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * 
  * Neither the name of the 'Xith3D Project Group' nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) A
  * RISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE
  */
 package org.jagatoo.test.input;
 
 import javax.media.opengl.GL;
 
 import org.jagatoo.input.InputSystem;
 import org.jagatoo.input.InputSystemException;
 import org.jagatoo.input.actions.InputAction;
 import org.jagatoo.input.actions.InvokableInputAction;
 import org.jagatoo.input.devices.Controller;
 import org.jagatoo.input.devices.InputDevice;
 import org.jagatoo.input.devices.InputDeviceFactory;
 import org.jagatoo.input.devices.components.AnalogDeviceComponent;
 import org.jagatoo.input.devices.components.ControllerAxis;
 import org.jagatoo.input.devices.components.ControllerButton;
 import org.jagatoo.input.devices.components.DeviceComponent;
 import org.jagatoo.input.devices.components.InputState;
 import org.jagatoo.input.devices.components.Key;
 import org.jagatoo.input.devices.components.Keys;
 import org.jagatoo.input.devices.components.MouseButton;
 import org.jagatoo.input.devices.components.MouseButtons;
 import org.jagatoo.input.events.ControllerAxisChangedEvent;
 import org.jagatoo.input.events.ControllerButtonEvent;
 import org.jagatoo.input.events.ControllerButtonPressedEvent;
 import org.jagatoo.input.events.ControllerButtonReleasedEvent;
 import org.jagatoo.input.events.KeyPressedEvent;
 import org.jagatoo.input.events.KeyReleasedEvent;
 import org.jagatoo.input.events.KeyStateEvent;
 import org.jagatoo.input.events.KeyTypedEvent;
 import org.jagatoo.input.events.MouseButtonEvent;
 import org.jagatoo.input.events.MouseButtonPressedEvent;
 import org.jagatoo.input.events.MouseButtonReleasedEvent;
 import org.jagatoo.input.events.MouseMovedEvent;
 import org.jagatoo.input.events.MouseStoppedEvent;
 import org.jagatoo.input.events.MouseWheelEvent;
 import org.jagatoo.input.listeners.InputListener;
 import org.jagatoo.input.managers.InputBindingsAdapter;
 import org.jagatoo.input.managers.InputBindingsManager;
 import org.jagatoo.input.managers.InputHotPlugListener;
 import org.jagatoo.input.managers.InputHotPlugManager;
 import org.jagatoo.input.managers.InputStatesManager;
 import org.jagatoo.input.managers.SimpleInputActionListener;
 import org.jagatoo.input.managers.SimpleInputActionManager;
 import org.jagatoo.input.render.Cursor;
 import org.jagatoo.input.render.InputSourceWindow;
 import org.jagatoo.test.util.JOGLBase;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 /**
  * Insert type comment here.
  * 
  * @author Marvin Froehlich (aka Qudus)
  */
 public class InputTest implements InputListener, InputHotPlugListener
 {
     private static final int DEBUG_MASK_EVENTS = 1;
     private static final int DEBUG_MASK_KEYBOARD_EVENTS = 2;
    private static final int DEBUG_MASK_MOUSE_EVENTS = 3;
    private static final int DEBUG_MASK_CONTROLLER_EVENTS = 4;
     private static final int DEBUG_MASK_TEST_ACTION = 16;
     private static final int DEBUG_MASK_MYBINDING = 32;
     private static final int DEBUG_MASK_MYACTION = 64;
     private static final int DEBUG_MASK_STRING_ACTION = 128;
     
     private static int debugMask = ~0;
     static
     {
         /*
          * Disable particular debugging outputs...
          */
         
         debugMask &= ~DEBUG_MASK_EVENTS;
         debugMask &= ~DEBUG_MASK_KEYBOARD_EVENTS;
         debugMask &= ~DEBUG_MASK_MOUSE_EVENTS;
         debugMask &= ~DEBUG_MASK_CONTROLLER_EVENTS;
         debugMask &= ~DEBUG_MASK_TEST_ACTION;
         debugMask &= ~DEBUG_MASK_MYBINDING;
         debugMask &= ~DEBUG_MASK_MYACTION;
         debugMask &= ~DEBUG_MASK_STRING_ACTION;
     }
     
     private final InputHotPlugManager hotplugManager = new InputHotPlugManager();
     
     private static final boolean isDebugFlagSet( int flag )
     {
         return( ( debugMask & flag ) != 0 );
     }
     
     public void onKeyPressed( KeyPressedEvent e, Key key )
     {
         if ( isDebugFlagSet( DEBUG_MASK_EVENTS ) && isDebugFlagSet( DEBUG_MASK_KEYBOARD_EVENTS ) )
             System.out.println( "key pressed: " + e.getKey() + ", " + e.getModifierMask() );
     }
     
     public void onKeyReleased( KeyReleasedEvent e, Key key )
     {
         if ( isDebugFlagSet( DEBUG_MASK_EVENTS ) && isDebugFlagSet( DEBUG_MASK_KEYBOARD_EVENTS ) )
             System.out.println( "key released: " + e.getKey() + ", " + e.getModifierMask() );
     }
     
     public void onKeyStateChanged( KeyStateEvent e, Key key, boolean state )
     {
         if ( isDebugFlagSet( DEBUG_MASK_EVENTS ) && isDebugFlagSet( DEBUG_MASK_KEYBOARD_EVENTS ) )
             System.out.println( "key state: " + e.getKey() + ", state: " + state );
     }
     
     public void onKeyTyped( KeyTypedEvent e, char keyChar )
     {
         if ( isDebugFlagSet( DEBUG_MASK_EVENTS ) && isDebugFlagSet( DEBUG_MASK_KEYBOARD_EVENTS ) )
             System.out.println( "key typed: " + keyChar + ", " + (int)keyChar + ", " + e.getModifierMask() );
     }
     
     public void onMouseButtonPressed( MouseButtonPressedEvent e, MouseButton button )
     {
         if ( isDebugFlagSet( DEBUG_MASK_EVENTS ) && isDebugFlagSet( DEBUG_MASK_MOUSE_EVENTS ) )
             System.out.println( "Button pressed: " + e.getButton() + ", " + e.getX() + ", " + e.getY() );
     }
     
     public void onMouseButtonReleased( MouseButtonReleasedEvent e, MouseButton button )
     {
         if ( isDebugFlagSet( DEBUG_MASK_EVENTS ) && isDebugFlagSet( DEBUG_MASK_MOUSE_EVENTS ) )
             System.out.println( "Button released: " + e.getButton() + ", " + e.getX() + ", " + e.getY() );
     }
     
     public void onMouseButtonStateChanged( MouseButtonEvent e, MouseButton button, boolean state )
     {
         if ( isDebugFlagSet( DEBUG_MASK_EVENTS ) && isDebugFlagSet( DEBUG_MASK_MOUSE_EVENTS ) )
             System.out.println( "mouse button state: " + e.getButton() + ", state: " + state );
         
         try
         {
             if ( e.getButton() == MouseButtons.RIGHT_BUTTON )
             {
                 e.getMouse().setAbsolute( !state );
             }
         }
         catch ( InputSystemException ex )
         {
             ex.printStackTrace();
         }
     }
     
     @SuppressWarnings("unused")
     private int xxx = 0;
     
     public void onMouseMoved( MouseMovedEvent e, int x, int y, int dx, int dy )
     {
         if ( isDebugFlagSet( DEBUG_MASK_EVENTS ) && isDebugFlagSet( DEBUG_MASK_MOUSE_EVENTS ) )
             System.out.println( "Mouse moved: " + x + ", " + y + ", " + dx + ", " + dy + ", " + e.getMouse().getButtonsState() );
         /*
         xxx += dx;
         System.out.println( xxx );
         */
     }
     
     public void onMouseWheelMoved( MouseWheelEvent e, int wheelDelta )
     {
         if ( isDebugFlagSet( DEBUG_MASK_EVENTS ) && isDebugFlagSet( DEBUG_MASK_MOUSE_EVENTS ) )
             System.out.println( "Wheel moved: " + e.getWheelDelta() + ", " + e.isPageMove() + ", " + e.getMouse().getCurrentX() + ", " + e.getMouse().getCurrentY() + ", " + e.getMouse().getButtonsState() );
     }
     
     public void onMouseStopped( MouseStoppedEvent e, int x, int y )
     {
         if ( isDebugFlagSet( DEBUG_MASK_EVENTS ) && isDebugFlagSet( DEBUG_MASK_MOUSE_EVENTS ) )
             System.out.println( "Mouse stopped: " + x + ", " + y );
     }
     
     public void onControllerAxisChanged( ControllerAxisChangedEvent e, ControllerAxis axis, float axisDelta )
     {
         if ( isDebugFlagSet( DEBUG_MASK_EVENTS ) && isDebugFlagSet( DEBUG_MASK_CONTROLLER_EVENTS ) )
             System.out.println( "axis changed: " + axis.getName() + ", normValue = " + axis.getNormalizedValue() );
             //System.out.println( "axis changed: " + axis + ", normValue = " + axis.getNormalizedValue() + ", delta = " + axisDelta );
     }
     
     public void onControllerButtonPressed( ControllerButtonPressedEvent e, ControllerButton button )
     {
         if ( isDebugFlagSet( DEBUG_MASK_EVENTS ) && isDebugFlagSet( DEBUG_MASK_CONTROLLER_EVENTS ) )
             System.out.println( "controller-button pressed: " + button );
     }
     
     public void onControllerButtonReleased( ControllerButtonReleasedEvent e, ControllerButton button )
     {
         if ( isDebugFlagSet( DEBUG_MASK_EVENTS ) && isDebugFlagSet( DEBUG_MASK_CONTROLLER_EVENTS ) )
             System.out.println( "controller-button released: " + button );
     }
     
     public void onControllerButtonStateChanged( ControllerButtonEvent e, ControllerButton button, boolean state )
     {
         if ( isDebugFlagSet( DEBUG_MASK_EVENTS ) && isDebugFlagSet( DEBUG_MASK_CONTROLLER_EVENTS ) )
             System.out.println( "controller-button state: " + button + ", state: " + state );
     }
     
     public void onInputDevicePluggedIn( InputDevice device )
     {
         System.out.println( "plugged in: " + device );
     }
     
     public void onInputDevicePluggedOut( InputDevice device )
     {
         System.out.println( "plugged out: " + device );
     }
     
     private static enum MyInputBinding implements InputAction
     {
         ACTION0,
         ACTION1,
         ACTION2,
         ACTION3,
         ACTION4,
         ACTION5
     }
     
     private static class TestAction implements InvokableInputAction
     {
         public final int ordinal()
         {
             return( MyInputBinding.values().length );
         }
         
         public String invokeAction( InputDevice device, DeviceComponent comp, int delta, int state, long nanoTime )
         {
             if ( !isDebugFlagSet( DEBUG_MASK_TEST_ACTION ) )
                 return( null );
             
             System.out.println( "TestAction: " + delta + ", " + state );
             
             return( "ok" );
         }
     }
     
     private static enum MyInputAction implements InvokableInputAction
     {
         ACTION0,
         ACTION1,
         ACTION2,
         ACTION3,
         ACTION4,
         ACTION5;
         
         public String invokeAction( InputDevice device, DeviceComponent comp, int delta, int state, long nanoTime ) throws InputSystemException
         {
             if ( isDebugFlagSet( DEBUG_MASK_MYACTION ) )
                 System.out.println( "Invoked action: " + this );
             
             return( "ok" );
         }
     }
     
     private InputStatesManager statesManager = null;
     
     private void setupInputBindings( InputSystem is ) throws Throwable
     {
         InputBindingsManager< InputAction > bindingsManager = new InputBindingsManager< InputAction >( MyInputBinding.values().length + 1 );
         
         bindingsManager.bind( Keys.G, MyInputBinding.ACTION0 );
         bindingsManager.bind( is.getMouse().getButton( 0 ), MyInputBinding.ACTION1 );
         bindingsManager.bind( is.getMouse().getWheel(), MyInputBinding.ACTION2 );
         bindingsManager.bind( is.getMouse().getXAxis(), MyInputBinding.ACTION3 );
         if ( is.getController() != null )
         {
             if ( is.getController().getButtonsCount() > 0 )
                 bindingsManager.bind( is.getController().getButton( 0 ), MyInputBinding.ACTION4 );
             if ( is.getController().getAxesCount() > 3 )
                 bindingsManager.bind( is.getController().getAxis( 3 ), MyInputBinding.ACTION5 );
         }
         bindingsManager.bind( Keys.SPACE, new TestAction() );
         /*
         //bindingsManager.bind( is.getMouse().getWheel().getUp(), MyInputBinding.ACTION0 );
         //bindingsManager.bind( is.getMouse().getWheel().getDown(), MyInputBinding.ACTION1 );
         bindingsManager.bind( MouseWheel.GLOBAL_WHEEL.getUp(), MyInputBinding.ACTION0 );
         bindingsManager.bind( MouseWheel.GLOBAL_WHEEL.getDown(), MyInputBinding.ACTION1 );
         bindingsManager.bind( is.getMouse().getWheel(), MyInputBinding.ACTION2 );
         bindingsManager.bind( MouseWheel.GLOBAL_WHEEL, MyInputBinding.ACTION3 );
         */
         
         statesManager = new InputStatesManager( bindingsManager );
         is.registerInputStatesManager( statesManager );
         
         
         InputBindingsAdapter< MyInputAction > bindingsAdapter = new InputBindingsAdapter< MyInputAction >( MyInputAction.values().length );
         
         bindingsAdapter.bind( MouseButtons.LEFT_BUTTON, MyInputAction.ACTION0 );
         bindingsAdapter.bind( Keys._1, MyInputAction.ACTION1 );
         //bindingsAdapter.bind( MouseWheel.GLOBAL_WHEEL, MyInputAction.ACTION2 );
         bindingsAdapter.bind( MouseButtons.WHEEL_UP, MyInputAction.ACTION3 );
         bindingsAdapter.bind( MouseButtons.WHEEL_DOWN, MyInputAction.ACTION4 );
         if ( is.getController() != null )
         {
             bindingsAdapter.bind( is.getController().getButton( 0 ), MyInputAction.ACTION5 );
         }
         
         is.addInputStateListener( bindingsAdapter );
         
         
         SimpleInputActionManager siam = SimpleInputActionManager.getInstance();
         
         /*
          * We don't need to add it as a listener, since we're using
          * the singleton-instance, which is automatically added/removed.
          */
         //is.addInputStateListener( siam );
         
         siam.bindAction( MouseButtons.LEFT_BUTTON, "My sample String action 0" );
         siam.bindAction( Keys._1, "My sample String action 1" );
         //siam.bindAction( MouseWheel.GLOBAL_WHEEL, "My sample String action 2" );
         siam.bindAction( MouseButtons.WHEEL_UP, "My sample String action 3" );
         siam.bindAction( MouseButtons.WHEEL_DOWN, "My sample String action 4" );
         if ( is.getController() != null )
         {
             siam.bindAction( is.getController().getButton( 0 ), "My sample String action 5" );
         }
         
         siam.addActionListener( new SimpleInputActionListener()
         {
             public void onActionInvoked( Object action, int delta, int state )
             {
                 if ( !isDebugFlagSet( DEBUG_MASK_STRING_ACTION ) )
                     return;
                 
                 if ( delta > 0 )
                     System.out.println( "String action invoked: \"" + action + "\"" );
             }
         } );
     }
     
     private void setupInputSystem( InputSystem is, InputSourceWindow sourceWindow ) throws Throwable
     {
         try
         {
             is.registerNewMouse( sourceWindow );
             is.getMouse().addMouseListener( this );
             //is.getMouse().startMouseStopManager();
             
             MouseButtons.MIDDLE_BUTTON.bindAction( new TestAction() );
             is.getMouse().getWheel().bindAction( new TestAction() );
         }
         catch ( InputSystemException ex )
         {
             ex.printStackTrace();
         }
         
         try
         {
             is.registerNewKeyboard( sourceWindow );
             is.getKeyboard().addKeyboardListener( this );
         }
         catch ( InputSystemException ex )
         {
             ex.printStackTrace();
         }
         
         try
         {
             Controller[] controllers = sourceWindow.getInputDeviceFactory( is ).getControllers( false );
             if ( controllers.length > 0 )
             {
                 is.registerController( controllers[ 0 ] );
                 controllers[ 0 ].addControllerListener( this );
             }
             //org.jagatoo.input.util.ControllerCalibrator.start( controllers[ 0 ] );
         }
         catch ( InputSystemException ex )
         {
             ex.printStackTrace();
         }
         
         System.out.println( ( is.hasKeyboard() ? is.getKeyboard() : "No Keyboard registered." ) );
         System.out.println( ( is.hasMouse() ? is.getMouse() : "No Mouse registered." ) );
         System.out.println( ( is.hasController() ? is.getController() : "No Controller registered." ) );
         
         setupInputBindings( is );
         
         /*
          * Listen for hot-plugged InputDevices.
          */
         hotplugManager.registerDeviceFactory( sourceWindow.getInputDeviceFactory( is ) );
         hotplugManager.addHotPlugListener( this );
         //hotplugManager.start();
     }
     
     private void check( InputAction action, AnalogDeviceComponent comp )
     {
         /*
         if ( !isDebugFlagSet( DEBUG_MASK_MYBINDING ) )
             return;
         */
         
         InputState state = statesManager.getInputState( action );
         
         if ( state.isVolatile() )
         {
             if ( isDebugFlagSet( DEBUG_MASK_MYACTION ) )
                 System.out.println( action + ": " + state + ", " + comp.getIntValue() );
         }
     }
     
     private void iteration( InputSystem is, final long time ) throws Throwable
     {
         statesManager.update( time );
         
         check( MyInputBinding.ACTION0, is.getMouse().getWheel() );
         check( MyInputBinding.ACTION1, is.getMouse().getWheel() );
         check( MyInputBinding.ACTION2, is.getMouse().getWheel() );
         check( MyInputBinding.ACTION3, is.getMouse().getWheel() );
         check( MyInputBinding.ACTION4, is.getMouse().getWheel() );
         check( MyInputBinding.ACTION5, is.getMouse().getWheel() );
         
         //Thread.sleep( 1000L );
     }
     
     
     private static class LWJGLSourceWindow implements InputSourceWindow
     {
         private Cursor cursor = Cursor.DEFAULT_CURSOR;
         private static InputDeviceFactory deviceFactory = null;
         
         public Object getDrawable()
         {
             return( null );
         }
         
         public InputDeviceFactory getInputDeviceFactory( InputSystem inputSystem )
         {
             if ( deviceFactory == null )
             {
                 //deviceFactory = new org.jagatoo.input.impl.lwjgl.LWJGLInputDeviceFactory( this, inputSystem.getEventQueue() );
                 deviceFactory = new org.jagatoo.input.impl.mixed.LWJGLJInputInputDeviceFactory( this, inputSystem.getEventQueue() );
             }
             
             return( deviceFactory );
         }
         
         public boolean receivesInputEvents()
         {
             return( true );
         }
         
         public int getWidth()
         {
             return( org.lwjgl.opengl.Display.getDisplayMode().getWidth() );
         }
         
         public int getHeight()
         {
             return( org.lwjgl.opengl.Display.getDisplayMode().getHeight() );
         }
         
         public void setCursor( Cursor cursor )
         {
             this.cursor = cursor;
         }
         
         public void refreshCursor()
         {
         }
         
         public Cursor getCursor()
         {
             return( cursor );
         }
     };
     
     private static class AWTSourceWindow implements InputSourceWindow
     {
         final java.awt.Component component;
         
         private InputDeviceFactory deviceFactory = null;
         private Cursor cursor = Cursor.DEFAULT_CURSOR;
         
         public java.awt.Component getDrawable()
         {
             return( component );
         }
         
         public InputDeviceFactory getInputDeviceFactory( InputSystem inputSystem )
         {
             if ( deviceFactory == null )
             {
                 deviceFactory = new org.jagatoo.input.impl.awt.AWTInputDeviceFactory( this, inputSystem.getEventQueue() );
                 //deviceFactory = new org.jagatoo.input.impl.mixed.AWTJInputInputDeviceFactory( this, inputSystem.getEventQueue() );
             }
             
             return( deviceFactory );
         }
         
         public boolean receivesInputEvents()
         {
             return( true );
         }
         
         public int getWidth()
         {
             return( component.getWidth() );
         }
         
         public int getHeight()
         {
             return( component.getHeight() );
         }
         
         public void setCursor( Cursor cursor )
         {
             this.cursor = cursor;
         }
         
         public void refreshCursor()
         {
         }
         
         public Cursor getCursor()
         {
             return( cursor );
         }
         
         public AWTSourceWindow( java.awt.Component component )
         {
             this.component = component;
         }
     };
     
     
     private void prepareShutdown( InputSystem is ) throws InputSystemException
     {
         if ( is != null )
             is.destroy();
         
         hotplugManager.stop( true );
     }
     
     @SuppressWarnings("unused")
     private void startLWJGL() throws Throwable
     {
         Display.setDisplayMode( new DisplayMode( 1024, 768 ) );
         Display.create();
         Display.setLocation( 0, 0 );
         
         InputSystem is = null;
         try
         {
             LWJGLSourceWindow sourceWindow = new LWJGLSourceWindow();
             is = InputSystem.getInstance();
             
             setupInputSystem( is, sourceWindow );
             
             while ( !Display.isCloseRequested() )
             {
                 final long time = System.nanoTime();
                 
                 is.update( time );
                 
                 iteration( is, time );
             }
         }
         catch ( InputSystemException e )
         {
             e.printStackTrace();
         }
         finally
         {
             prepareShutdown( is );
             
             Display.destroy();
         }
     }
     
     @SuppressWarnings("unused")
     private void startJInput() throws Throwable
     {
         Display.setDisplayMode( new DisplayMode( 1024, 768 ) );
         Display.create();
         Display.setLocation( 0, 0 );
         
         InputSystem is = null;
         try
         {
             LWJGLSourceWindow sourceWindow = new LWJGLSourceWindow();
             is = InputSystem.getInstance();
             
             setupInputSystem( is, sourceWindow );
             
             while ( !Display.isCloseRequested() )
             {
                 final long time = System.nanoTime();
                 
                 is.update( time );
                 
                 iteration( is, time );
                 
                 Thread.yield();
             }
         }
         catch ( InputSystemException e )
         {
             e.printStackTrace();
         }
         finally
         {
             prepareShutdown( is );
             
             Display.destroy();
         }
     }
     
     @SuppressWarnings("unused")
     private void startAWT() throws Throwable
     {
         final JOGLBase jogl = new JOGLBase( "Test", 1024, 768 )
         {
             @Override
             protected void render( GL gl, int canvasWidth, int canvasHeight, long nanoTime )
             {
                 //System.out.println( "sdfsdfsdf" );
             }
         };
         
         jogl.getFrame().setLocation( 0, 0 );
         
         InputSystem is = null;
         try
         {
             AWTSourceWindow sourceWindow = new AWTSourceWindow( jogl.getCanvas() );
             is = InputSystem.getInstance();
             
             setupInputSystem( is, sourceWindow );
             
             while ( jogl.getFrame().isDisplayable() )
             {
                 final long time = System.nanoTime();
                 
                 is.update( time );
                 
                 iteration( is, time );
                 
                 Thread.yield();
                 //Thread.sleep( 200L );
             }
         }
         catch ( InputSystemException e )
         {
             e.printStackTrace();
         }
         finally
         {
             prepareShutdown( is );
         }
     }
     
     public InputTest() throws Throwable
     {
         startLWJGL();
         //startJInput();
         //startAWT();
     }
     
     public static void main( String[] args ) throws Throwable
     {
         new InputTest();
         
         /*
         for ( KeyID kid : KeyID.values() )
         {
             final Key key = kid.getKey();
             
             System.out.println( kid + ", " + key + ", " + key.getName() + ": " + key.getLocalizedName() );
         }
         */
     }
 }
