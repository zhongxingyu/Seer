 /* 
  * Copyleft (o) 2012 James Baiera
  * All wrongs reserved.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package STEngine;
 
 import java.applet.Applet;
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 
 /**
  * This is a game applet object that houses the game display and 
  * launches the game object in its own thread
  * 
  * @author comet
  *
  */
 public class GameApplet extends Applet{
 	Canvas displayParent;
 	Thread gameThread;
 	Game game;
 	
 	/**
 	 * Once the Canvas is created, its add notify method will call this method
 	 * to start the display and the game loop in another thread.
 	 */
 	public void startLWJGL() {
 		gameThread = new Thread() {
 			public void run() {
 				try {
 					Display.setParent(displayParent);
 				} catch (LWJGLException e) {
 					e.printStackTrace();
 				}
 				// start the game
 				game = new Game(false);
 				game.execute();
 			}
 		};
 		gameThread.start();
 	}
 	
 	/**
 	 * Tell the game loop to stop running, after which the LWJGL Display will
 	 * be destroyed. The main thread will wait for the Display.destroy() to complete
 	 */
 	private void stopLWJGL() {
 		Game.gameRunning = false;
 		try {
 			gameThread.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Nothing here
 	 */
 	public void start() {
 		
 	}
 	
 	/**
 	 * Nothing here either...
 	 */
 	public void stop() {
 		
 	}
 	
 	/**
 	 * Applet method that disconnects the canvas and destroys itself
 	 */
 	public void destroy() {
 		remove(displayParent);
 		super.destroy();
 		System.out.println("Clear up");
 	}
 	
 	/**
 	 * Called at the start of the applet run. Creates a canvas and 
 	 * configures it for use
 	 */
 	public void init() {
 		setLayout(new BorderLayout());
 		try {
 			displayParent = new Canvas() {
 				public void addNotify() {
 					super.addNotify();
 					startLWJGL();
 				}
 				public void removeNotify() {
 					stopLWJGL();
 					super.removeNotify();
 				}
 			};
 			displayParent.setSize(getWidth(), getHeight());
 			add(displayParent);
 			displayParent.setFocusable(true);
 			displayParent.requestFocus();
 			displayParent.setIgnoreRepaint(true);
 			setVisible(true);
 		} catch (Exception e) {
 			System.err.println(e);
 			throw new RuntimeException("Unable to create display");
 		}
 	}
 }
