 /**
  * Functions.java
  * Part of Javascool proglet "game"
  * File creation : 20110704 at revision 48 by gmatheron (INRIA)
  * Last modification 20110704 by gmatheron (INRIA)
  * 
  * gmatheron : guillaume.matheron.06@gmail.com
  */
 package org.javascool.proglets.game;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.lang.reflect.InvocationTargetException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.javascool.tools.Macros;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /* To use these event listeners, use this syntax : 
  * void toto(MouseState s) {
  *     ...
  * }
  * void main() {
  *     onClick("toto");
  * }
  */
 /**
  * 
  * @author gmatheron
  */
 public class Functions {
 
     private static Functions m_singleton;
 
     /**
      * @return the singleton
      */
     public static Functions getSingleton() {
         return m_singleton;
     }
     
     private boolean m_mouseDown[] = {false, false, false};
     private int m_mouseWheelPosition = 0;
     /* These arrays are designed to store the functions the user assigned a listener
      * A convenience type is used : CallbackFunction
      */
     private java.util.ArrayList<String> m_onClick;
     private java.util.ArrayList<String> m_onMouseEntered;
     private java.util.ArrayList<String> m_onMouseExited;
     private java.util.ArrayList<String> m_onMousePressed;
     private java.util.ArrayList<String> m_onMouseReleased;
     private java.util.ArrayList<String> m_onMouseDown;
     private java.util.ArrayList<String> m_onMouseUp;
     private java.util.ArrayList<String> m_onMouseMoved;
     private java.util.ArrayList<String> m_onMouseDragged;
     private java.util.ArrayList<String> m_onMouseWheelUp;
     private java.util.ArrayList<String> m_onMouseWheelDown;
     private java.util.ArrayList<String> m_onMouseWheelMoved;
     private java.util.ArrayList<String> m_onFrame;
     private java.awt.event.MouseListener m_mouseListener;
     private java.awt.event.MouseMotionListener m_mouseMotionListener;
     private java.awt.event.MouseWheelListener m_mouseWheelListener;
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onClick(String s) {
         m_singleton.m_onClick.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseEntered(String s) {
         m_singleton.m_onMouseEntered.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseExited(String s) {
         m_singleton.m_onMouseExited.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMousePressed(String s) {
         m_singleton.m_onMousePressed.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseReleased(String s) {
         m_singleton.m_onMouseReleased.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseDown(String s) {
         m_singleton.m_onMouseDown.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseUp(String s) {
         m_singleton.m_onMouseUp.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseMoved(String s) {
         m_singleton.m_onMouseMoved.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseDragged(String s) {
         m_singleton.m_onMouseDragged.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseWheelUp(String s) {
         m_singleton.m_onMouseWheelUp.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseWheelDown(String s) {
         m_singleton.m_onMouseWheelDown.add(s);
     }
 
     /**
      * Used to create a listener that will callback the specified function
      * with one MouseState argument
      * @param s The function to callback
      */
     public static void onMouseWheelMoved(String s) {
         m_singleton.m_onMouseWheelMoved.add(s);
     }
     
     /**
      * Used to create a listener that will callback the specified function
      * @param s The function to callback
      */
     @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
     public static void onFrame(String s) {
         m_singleton.m_onFrame.add(s);
     }
 
     /**
      * Calls the specified end-user-defined method, passing as a parameter the specified state.
      * If the function is undefined with one argument, it will be called with no arguments.
      * @param method The end-user-defined method to call
      * @param s The state to pass
      */
     private static void call(String method, State s) {
         try {
             for (int i=0; i<Macros.getProgram().getClass().getMethods().length; i++) {
                 java.lang.reflect.Method m=Macros.getProgram().getClass().getMethods()[i];
                 if (m.getName().equals(method)) {
                     int params=m.getParameterTypes().length;
                     if (params==0) {
                         m.invoke(Macros.getProgram());
                     }
                     else if (params==1) {
                         if (m.getParameterTypes()[0].getSuperclass().getSimpleName().equals("State")) {
                             m.invoke(Macros.getProgram(), m.getParameterTypes()[0].cast(s));
                         }
                         else {
                             org.javascool.JvsMain.reportBug(new NoSuchMethodException("The specified method "+method+" takes one parameter but it must extend State to be valid"));
                         }
                     }
                     else {
                         org.javascool.JvsMain.reportBug(new NoSuchMethodException("The specified method "+method+" must take zero or one parameter"));
                     }
                 }
             }
             //TODO tell if method not found
         } catch (IllegalAccessException ex) {
             Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalArgumentException ex) {
             Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InvocationTargetException ex) {
             Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SecurityException ex) {
             Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     /**
      * Calls the specified end-user-defined method
      * @param method The end-user-defined method to call
      */
     private static void call(String method) {
         try {
             for (int i=0; i<Macros.getProgram().getClass().getMethods().length; i++) {
                 java.lang.reflect.Method m=Macros.getProgram().getClass().getMethods()[i];
                 if (m.getName().equals(method)) {
                     int params=m.getParameterTypes().length;
                     if (params==0) {
                         m.invoke(Macros.getProgram());
                     }
                     else if (params==1) {
                         org.javascool.JvsMain.reportBug(new NoSuchMethodException("The specified method "+method+" cannot take a parameter with this listener"));
                     }
                     else {
                         org.javascool.JvsMain.reportBug(new NoSuchMethodException("The specified method "+method+" must take zero parameters"));
                     }
                 }
             }
             //TODO tell if method not found
         } catch (IllegalAccessException ex) {
             Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalArgumentException ex) {
             Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InvocationTargetException ex) {
             Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SecurityException ex) {
             Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /** //TODO
      * This method should be called after the program finishes running
      * It stops the timer and should delete the listeners (//TODO)
      */
     public static void stop() {
         /*
         m_clock.exitClean();
         Macros.getProgletPanel().removeMouseListener(m_singleton.m_mouseListener);
         Macros.getProgletPanel().removeMouseMotionListener(m_singleton.m_mouseMotionListener);
         Macros.getProgletPanel().removeMouseWheelListener(m_singleton.m_mouseWheelListener);
         (Panel)(Macros.getProgletPanel()).stop();*/
     }
     
     private static Clock m_clock;
 
     /**
      * This method is called during init
      * It creates the listeners, the clock and the singleton
      */
     @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "CollectionWithoutInitialCapacity"})
     public static void start() {
         /* A singleton is used to be able to define non-static classes inside Functions
          * The functions the end-user can call are all static, but they refer
          * to non-static attributes using this singleton static attribute
          */
         m_singleton = new Functions();
 
         /* These arrays store the listeners that should be called when an event occurs
          */
         m_singleton.m_onClick = new java.util.ArrayList<String>();
         m_singleton.m_onMouseDown = new java.util.ArrayList<String>();
         m_singleton.m_onMouseDragged = new java.util.ArrayList<String>();
         m_singleton.m_onMouseEntered = new java.util.ArrayList<String>();
         m_singleton.m_onMouseExited = new java.util.ArrayList<String>();
         m_singleton.m_onMouseMoved = new java.util.ArrayList<String>();
         m_singleton.m_onMousePressed = new java.util.ArrayList<String>();
         m_singleton.m_onMouseReleased = new java.util.ArrayList<String>();
         m_singleton.m_onMouseUp = new java.util.ArrayList<String>();
         m_singleton.m_onFrame = new java.util.ArrayList<String>();
         m_singleton.m_onMouseWheelDown = new java.util.ArrayList<String>();
         m_singleton.m_onMouseWheelUp = new java.util.ArrayList<String>();
         m_singleton.m_onMouseWheelMoved = new java.util.ArrayList<String>();
 
         /* The clock object will 'tick' each 1/30s. it will then call the callback
          * functions for onMouseDown, onMouseUp, etc if needed
          */
         m_clock = new Clock();
         m_clock.setFps(30);
         (new Thread(m_clock)).start();
 
         /* Define a few anonymous classes that will define the proglet's behavior
          * when an event is performed. Uually the proglet will call all the callback
          * functions for this event.
          * Possible optimization : only register a listener if an event is defined
          */
         {
             /********* START ANONYMOUS CLASSES ***************/
             m_singleton.m_mouseListener = new java.awt.event.MouseListener() {
 
                 @Override
                 public void mouseClicked(java.awt.event.MouseEvent evt) {
                     callback(getSingleton().m_onClick, (new MouseState(evt)));
                 }
 
                 @Override
                 public void mouseEntered(java.awt.event.MouseEvent evt) {
                     callback(getSingleton().m_onMouseEntered, (new MouseState(evt)));
                 }
 
                 @Override
                 public void mouseExited(java.awt.event.MouseEvent evt) {
                     callback(getSingleton().m_onMouseExited, (new MouseState(evt)));
                 }
 
                 @Override
                 public void mousePressed(java.awt.event.MouseEvent evt) {
                     callback(getSingleton().m_onMousePressed, (new MouseState(evt)));
                     if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                         m_singleton.m_mouseDown[0] = true;
                     } else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON2) {
                         m_singleton.m_mouseDown[1] = true;
                     } else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                         m_singleton.m_mouseDown[2] = true;
                     }
                 }
 
                 @Override
                 public void mouseReleased(java.awt.event.MouseEvent evt) {
                     callback(getSingleton().m_onMouseReleased, (new MouseState(evt)));
                     if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                         m_singleton.m_mouseDown[0] = false;
                     } else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON2) {
                         m_singleton.m_mouseDown[1] = false;
                     } else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
                         m_singleton.m_mouseDown[2] = false;
                     }
                 }
             };
             Macros.getProgletPanel().addMouseListener(m_singleton.m_mouseListener);
 
             m_singleton.m_mouseMotionListener = new java.awt.event.MouseMotionListener() {
                 @Override
                 public void mouseDragged(MouseEvent e) {
                     m_singleton.m_mousePosRelativeToPanelX=e.getX();
                     m_singleton.m_mousePosRelativeToPanelY=e.getY();
                     callback(getSingleton().m_onMouseDragged, (new MouseState(e)));
                     callback(getSingleton().m_onMouseMoved, (new MouseState(e)));
                 }
 
                 @Override
                 public void mouseMoved(MouseEvent e) {
                     m_singleton.m_mousePosRelativeToPanelX=e.getX();
                     m_singleton.m_mousePosRelativeToPanelY=e.getY();
                     callback(getSingleton().m_onMouseMoved, (new MouseState(e)));
                 }
             };
             Macros.getProgletPanel().addMouseMotionListener(m_singleton.m_mouseMotionListener);
 
             m_singleton.m_mouseWheelListener = new java.awt.event.MouseWheelListener() {
                 @Override
                 public void mouseWheelMoved(MouseWheelEvent e) {
                     int copy = getSingleton().m_mouseWheelPosition;
                     m_singleton.m_mouseWheelPosition += e.getWheelRotation();
                     if (copy > getSingleton().m_mouseWheelPosition) {
                         callback(getSingleton().m_onMouseWheelDown, (new MouseWheelState(e,getSingleton().m_mouseWheelPosition)));
                     } else {
                         callback(getSingleton().m_onMouseWheelUp, (new MouseWheelState(e,getSingleton().m_mouseWheelPosition)));
                     }
                     callback(getSingleton().m_onMouseWheelMoved, (new MouseWheelState(e,getSingleton().m_mouseWheelPosition)));
                 }
             };
             Macros.getProgletPanel().addMouseWheelListener(m_singleton.m_mouseWheelListener);
         }
         /********* END ANONYMOUS CLASSES ***************/
     }
 
     private static void callback(java.util.ArrayList<String> functions, State s) {
         for (int i = 0; i < functions.size(); i++) {
             call(functions.get(i), s);
         }
     }
     
     private static void callback(java.util.ArrayList<String> functions) {
         for (int i=0; i<functions.size(); i++) {
             call(functions.get(i));
         }
     }
 
     /**
      * TODO
      */
     @SuppressWarnings("PublicInnerClass")
     public static class Clock implements Runnable {
 
         private boolean m_exit = false;
         private int m_fps=30;
 
         Clock() {
         }
         
         @Override
         public void run() {
             tick();
         }
 
         public void setFps(int fps) {
             m_fps = fps;
         }
         
         @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
         private void tick() {
             for (int j = 0; j < 3; j++) {
                 if (getSingleton().m_mouseDown[j]) {
                     for (int i = 0; i < getSingleton().m_onMouseDown.size(); i++) {
                         call(getSingleton().m_onMouseDown.get(i), new MouseState());
                     }
                 } else {
                     for (int i = 0; i < getSingleton().m_onMouseUp.size(); i++) {
                         call(getSingleton().m_onMouseUp.get(i), new MouseState());
                     }
                 }
             }
             callback(getSingleton().m_onFrame);
             
             Macros.getProgletPanel().repaint();
             
             Timer t=new Timer();
             t.schedule(new TimerTask() {
                 @Override
                 public void run() {
                     tick();
                 }
             },(1000/m_fps));
         }
         
         public void exitClean() {
             m_exit = true;
         }
     }
     
     @SuppressWarnings("PublicField")
     public float m_mousePosRelativeToPanelX;
     @SuppressWarnings("PublicField")
     public float m_mousePosRelativeToPanelY;
     private static final Logger LOG = Logger.getLogger(Functions.class.getName());
 }
 
 /**
  * Superclass of all the states.
  * 
  * If an abstract (State) class can't be declared into a non-abstract class (Functions)
  * maybe we could switch it to an interface or leave it non-abstract (ugly)
  * 
  * PLEASE NOTE THAT ALL THE SUBCLASSES OF STATE MUST BE FINAL : ALL THE STATES MUST DIRECTLY
  * EXTEND THIS CLASS. See 'call' for details
  */
@SuppressWarnings({"MarkerInterface", "MultipleTopLevelClassesInFile"})
interface State {
 }
