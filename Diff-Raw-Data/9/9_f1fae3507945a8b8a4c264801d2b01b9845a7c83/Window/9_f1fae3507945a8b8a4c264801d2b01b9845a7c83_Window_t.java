 package engine.window;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.lwjgl.LWJGLException;
 
 import engine.window.tree.Model;
 
 import de.matthiasmann.twl.DesktopArea;
 import de.matthiasmann.twl.Event;
 import de.matthiasmann.twl.GUI;
 import de.matthiasmann.twl.ResizableFrame;
 import engine.input.Input;
 import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
 import de.matthiasmann.twl.theme.ThemeManager;
 
 public class Window extends DesktopArea {
 	private LWJGLRenderer renderer;
 	private GUI gui;
 	private ThemeManager theme;
 	private Input input;
 	private ArrayList<ResizableFrame> windows;
 	private Integer layers;
 	
 	public Window() {
 		super();
 		windowInit(null);
 	}
 	
 	public Window(Model m) {
 		super();
 		windowInit(m);
 	}
 	
 	public void windowInit(Model m){
 		try {
 			renderer = new LWJGLRenderer();
 		} catch (LWJGLException e1) {
 			e1.printStackTrace();
 		}
 		gui = new GUI(this, renderer);
 		try {
 			theme = ThemeManager.createThemeManager(
 					this.getClass().getClassLoader().getResource("resources/themes/default.xml"),
 					renderer);
   			gui.applyTheme(theme);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 				
 		//you have to do a gui update or it won't give you the sizes of the subwindows
 		gui.update();
 		
 		windows = new ArrayList<ResizableFrame>();
 	}
 
 	public void draw() {
 		gui.update();
 	}
 
 	public void destroy() {
 		gui.destroy();
 		theme.destroy();
 	}
 
 	protected boolean handleEvent(Event evt) { 
 		//Our event handling
 	    if(input != null && input.handleEvent(evt)) {
 	       return true; 
 	    }
     
 	    return false; 
 	}
 	
 	public void addWindow(ResizableFrame window, int width, int height) {
 		window.setSize(width, height);
 
 		gui.update();
 
 		if(windows.size() > 0) {	
 			ResizableFrame last_window =  windows.get(windows.size()-1);
 			if(window.getHeight()+last_window.getBottom() < this.getHeight()) {
 				window.setPosition(0, last_window.getBottom());
 			} else if(window.getHeight()+last_window.getBottom() > this.getHeight() && last_window.getX() == 0) {
 				window.setPosition(last_window.getRight(),0);
 			} else {
 				window.setPosition(last_window.getRight(),last_window.getBottom());
 			}
			//System.out.println("NewPos:"+window.getX()+":"+window.getY()+" ###last:"+last_window.getWidth()+":"+last_window.getHeight()+"###");
 		}
 		windows.add(window);
 		ResizableFrame current_window = windows.get(windows.indexOf(window));		
 		add(current_window);
 	}
 
 	public Integer getNumLayers() {
 		return layers;
 	}
 	
 	public void setInput(Input i){
 		input = i;
 	} 
 }
