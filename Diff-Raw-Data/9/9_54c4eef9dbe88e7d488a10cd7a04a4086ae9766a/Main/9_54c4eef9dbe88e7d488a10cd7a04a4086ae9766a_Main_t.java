 package editor;
 
 import javax.vecmath.Vector3f;
 
 import editor.action_listener.ActionEvent;
 import editor.action_listener.ActionListener;
 import editor.window.GridWindow;
 import editor.window.LayerMenu;
 import editor.window.PaletteWindow;
 //import editor.window.ToolBox;
 import engine.Engine;
 import engine.entity.*;
 
 import com.bulletphysics.collision.shapes.BoxShape;
 
 public class Main implements ActionListener {
 	// A filthy hack to get around the combobox sending to events on select
 	private Boolean combobox_hack = true;
 
 	private Engine engine;
 
 	private GridWindow grid_window;
 	private PaletteWindow palette_window;
 	private LayerMenu layer_menu;
 	// private ToolBox tool_box;
 
 	private Entity model;
 	private Camera camera;
 
 	public static void main(String args[]) {
 		Main m = new Main();
 		m.run();
 	}
 
 	public Main() {
 		engine = new Engine();
 
 		int num_layers = 8;
 		grid_window = new GridWindow(num_layers);
 		layer_menu = new LayerMenu();
 		layer_menu.populateLayers(num_layers);
 		palette_window = new PaletteWindow(216);
 		// tool_box = new ToolBox();
 
 		engine.addWindow(grid_window, 400, 400);
 		engine.addWindow(palette_window, 300, 300);
 		engine.addWindow(layer_menu, 200, 30);
 		// engine.addWindow(tool_box, 200, 300);
 
 		model = new Entity(1f, new BoxShape(new Vector3f(1, 1, 1)), true);
 		model.setModel(grid_window.getGrid().getModel("resources/models/misc/box.xgl"));
 		model.setProperty(Entity.NAME, "model");
		model.setPosition(new Vector3f(0,0,5));
 
 		camera = new Camera(1d, new BoxShape(new Vector3f(1, 1, 1)), false, model);
 		camera.setProperty(Entity.NAME, "camera");
 		camera.setPosition(new Vector3f(0,0,0));
 		camera.setDistance(10.0f);
 
 		engine.addEntity(model);
 		engine.addEntity(camera);
 		
 		engine.addKeyMap("keymap.txt");
 	}
 
 	public void run() {
 		grid_window.addActionListener(this);
 		palette_window.addActionListener(this);
 		layer_menu.addActionListener(this);
 		// tool_box.addActionListener(this);
 
 		engine.run();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		if (ae.getSource() == palette_window) {
 			grid_window.setCurrentColor(((PaletteWindow) ae.getSource()).getPrimaryColor());
 		} else if (ae.getSource() == grid_window) {
 			model.setModel(grid_window.getGrid().getModel("resources/models/misc/box.xgl"));
 			engine.updateEntity(model);
 		} else if (ae.getSource() == layer_menu) {
 			if (combobox_hack == true) {
 				grid_window.loadLayer(layer_menu.getSelection());
 				combobox_hack = false;
 			} else if (combobox_hack == false) {
 				combobox_hack = true;
 			}
 		}
 	}
 }
