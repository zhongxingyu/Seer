 package GUI;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class GUI {
 
 	private Window activeWindow = null;
 
 	public GUI() {
 
 	}
 	
 	void setActiveWindow(Window window) {
		if (window == activeWindow) {
			activeWindow = null;  
		} else {
			activeWindow = window;
		}
 	}
 
 	public AbstractWindow getActiveWindow() {
		return activeWindow.getInstance();
 	}
 	
 	public void closeWindow(){
 		setActiveWindow(null);
 	}
 	
 	public boolean anyWindowOpen() {
 		return getActiveWindow() != null;
 	}
 	
 	public void render(Graphics gr) {
 		if(anyWindowOpen()){
 			getActiveWindow().render(gr);
 		}
 	}
 	public void update(GameContainer gc, StateBasedGame sbg, float delta) {
 		if(anyWindowOpen()){
 			getActiveWindow().update(gc, sbg, delta);
 		}
 		Input input = gc.getInput();
 		
 		for(Window w : Window.values()){
 			if(w.isWindowKeyPressed(input)){
 				setActiveWindow(w);
 			}
 		}
 	}
 }
