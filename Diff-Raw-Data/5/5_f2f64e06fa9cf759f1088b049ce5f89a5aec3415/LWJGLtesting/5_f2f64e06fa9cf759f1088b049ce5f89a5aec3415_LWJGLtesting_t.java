 package Java;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import static org.lwjgl.opengl.GL11.*;
 
 public class LWJGLtesting extends Graphics{
 	public static void main(String[] argv) {
 		LWJGLtesting displayExample = new LWJGLtesting();
 		displayExample.start();
 	}
 	public void start() {
 		try {
 			setDisplayMode(800,600,true);
 			Display.create();
 			glMatrixMode(GL_PROJECTION);
 			glLoadIdentity();
 			glOrtho(0, 800, 0, 600, 1, -1);
 			glMatrixMode(GL_MODELVIEW);
 			boolean flashToggle=false;
 			int count=10;
 			long lastFPS=getTime(), fps=0;
 			Display.setFullscreen(true);
 			while (!Display.isCloseRequested()){
 				if(getTime() - lastFPS > 1000) {
 			        Display.setTitle("FPS: " + fps); 
 			        fps = 0; //reset the FPS counter
 			        lastFPS += 1000; //add one second
 			    }
 			    fps++;
			    if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
 			    	Display.destroy();
			    	break;
			    }
 			    if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
 			    	Display.setFullscreen(!Display.isFullscreen());
 			    if(Keyboard.next() && count>10){
 					flashToggle=!flashToggle;
 					count=0;
 				}
 //				boolean pressed=Keyboard.getEventKey()==Keyboard.KEY_A;
 //				System.out.println(pressed);
 			    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 			    if(flashToggle)
 			    	setColor(Math.random(),Math.random(),Math.random());
 			    else
 			    	setColor(0.5f,0.5f,1.0f);
 			    makeSquare(100,100,200);
 				Display.update();
 				if(count>1000)
 					count=100;
 				count++;
 				Display.sync(60);
 			}
 			Display.destroy();
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 }
