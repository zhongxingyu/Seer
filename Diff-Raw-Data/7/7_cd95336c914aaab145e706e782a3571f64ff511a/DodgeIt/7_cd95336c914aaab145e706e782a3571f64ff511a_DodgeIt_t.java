 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dodgeit;
 
 //import dodgeit.statics.state;
 import java.util.Stack;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 
 /**
  *
  * @author juho
  */
 public class DodgeIt {
     
     private Stack<LoopTemplate> stateStack = new Stack();
     public GameLoop gameState;
     public MenuLoop menuState;
     //private LoopTemplate currentState;
     
     public DodgeIt() {
         gameState = new GameLoop(this);
         menuState = new MenuLoop(this);
     }
 
     public void startGame() {
 
         try {
             Display.setDisplayMode(new DisplayMode(statics.DISPLAY_WIDTH, statics.DISPLAY_HEIGHT));
             Display.create();
         } catch (LWJGLException e) {
             e.printStackTrace(System.out);
             System.exit(0);
         }
         
         Display.setTitle(statics.TITLE);
         
         GL11.glMatrixMode(GL11.GL_PROJECTION);
         GL11.glLoadIdentity();
         GL11.glOrtho(0, statics.DISPLAY_WIDTH, 0, statics.DISPLAY_HEIGHT, 1, -1);
         GL11.glMatrixMode(GL11.GL_MODELVIEW);
 
         stateStack.push(menuState);
         stackHandler();
     }
 
     private void stackHandler() {
         //System.out.println(statics.state.MENU_STATE);
         while (stateStack.empty() == false) {
             stateStack.pop().run();
         }
 
         Display.destroy();
     }
     public void addToStack(LoopTemplate state){
         stateStack.add(state);
         thisDoesNothing();
     }
     public void thisDoesNothing(){
         System.out.println("State added to stack.");
        //My original modifications are right here:
         int x = 2000000000;
         x += x + x + x * x; //overflows?
         System.out.println(x);
        //And then I'm gonna modify your stuff just for the sake of it:
        for (int i = 0;i < 10;i = i += 2){
            System.out.println(i);
            i *= 2;
        }
     }
 }
