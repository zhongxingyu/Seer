 package editor.window;
 
 import java.util.ArrayList;
 
 import editor.action_listener.ActionEvent;
 import editor.action_listener.ActionListener;
 
 import de.matthiasmann.twl.Color;
 import de.matthiasmann.twl.DialogLayout;
 import de.matthiasmann.twl.Event;
 import de.matthiasmann.twl.DialogLayout.Group;
 import editor.Block;
 import editor.CubicGrid;
 import engine.render.Model;
 import engine.window.components.Window;
 
 public class GridWindow extends Window implements ActionListener {
 	private CubicGrid<Block<Integer>> grid;
 	private Color current_color = new Color((byte) 0xFF, (byte) 0xFF,
 		(byte) 0xFF, (byte) 0xFF);
 	private DialogLayout layout;
 
 	private ArrayList<ActionListener> action_listeners;
 
 	public GridWindow(Integer grid_size, Model base_model) {
 		this.setSize(400, 400);
 
 		setTitle("Grid View");
 
 		this.grid = new CubicGrid<Block<Integer>>(base_model);
 		setGridSize(grid_size);
 
 		action_listeners = new ArrayList<ActionListener>();
 
 		this.addCellListener(this);
 
 		// Create the layout and button instances
 		layout = new DialogLayout();
 
 		this.add(layout);
 		
 		if (grid_size > 0)	
 			loadLayer(0);
 	}
 
 	public void setGridSize(int grid_size) {
 		grid.setSize(grid_size);
 		for (int z = 0; z < grid_size; z++) {
 			for (int y = 0; y < grid_size; y++) {
 				for (int x = 0; x < grid_size; x++) {
 					 Block<Integer> block = new Block<Integer>();
 					 block.setTheme("block");
 					this.grid.set(x, y, z,block);
 				}
 			}
 		}
 	}
 	
 	public void setCurrentColor(Color color) {
 		current_color = color;
 	}
 
 	public CubicGrid<Block<Integer>> getGrid() {
 		return grid;
 	}
 
 	public void loadLayer(Integer layer) {
 		layout.removeAllChildren();
 
 		// !!!EXAMPLE OF DIALOG LAYOUT!!!//
 		// Sequential groups are like a Swing boxlayout and just lists from top
 		// to bottom
 		// Parallel groups align each start and size and can be cascaded
 		//
 		// Group for holding the Horizontal alignment of the buttons
 		Group h_grid = layout.createParallelGroup();
 		// Group for holding the Vertical alignment of the buttons
 		Group v_grid = layout.createParallelGroup();
 		// Generic row up buttons
 		Group row = layout.createSequentialGroup();
 
 		// Create the horizontal rows
 		for (int i = 0; i < grid.getSize(); i++) {
 			row = layout.createSequentialGroup();
 			for (int j = 0; j < grid.getSize(); j++) {
 				// TODO: *un*fix the z axis
 				row.addWidget(grid.get(j, i, layer));
 			}
 			h_grid.addGroup(row);
 		}
 
 		// Create vertical rows
 		for (int i = 0; i < grid.getSize(); i++) {
 			row = layout.createSequentialGroup();
 			for (int j = 0; j < grid.getSize(); j++) {
 				// TODO: *un*fix the z axis
 				row.addWidget(grid.get(i, j, layer));
 			}
 			v_grid.addGroup(row);
 		}
 
 		// All Dialog layout groups must have both a HorizontalGroup and
 		// VerticalGroup
 		// Otherwise "incomplete" exception is thrown and layout is not applied
 		layout.setHorizontalGroup(h_grid);
 		layout.setVerticalGroup(v_grid);
 	}
 
 	public void addCellListener(ActionListener listener) {
 		for (int i = 0; i < grid.getSize(); i++) {
 			for (int j = 0; j < grid.getSize(); j++) {
 				for (int k = 0; k < grid.getSize(); k++) {
 					grid.get(i, j, k).addActionListener(listener);
 				}
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if( ((Block<Integer>) e.getSource()).getMouseButton() == 0 ) {
 			((Block<Integer>) e.getSource()).setColor(current_color);
 			fireActionEvent();
 		} else if( ((Block<Integer>) e.getSource()).getMouseButton() == 1 ) {
 			((Block<Integer>) e.getSource()).setColor(null);
 			fireActionEvent();
 		} else if( ((Block<Integer>) e.getSource()).getMouseWheel() == 1) {
 			fireActionEvent("mouseup");
 		} else if( ((Block<Integer>) e.getSource()).getMouseWheel() == -1) {
 			fireActionEvent("mousedown");
 		} else {
 			System.out.println(
 				"GridWindow doesn't handle button: " +
 						((Block<Integer>) e.getSource()).getMouseButton() +
 				", yet. Fix it dummy!");
 		}
 	}
 	
 	 @Override
   	 protected boolean handleEvent(Event evt) {
 		 super.handleEvent(evt);
 		 switch (evt.getType()) {
 	        case MOUSE_WHEEL:
 	            //mouse_wheel = evt.getMouseWheelDelta();
 	        	if(evt.getMouseWheelDelta() > 0)
 	        		fireActionEvent("mouseup");
 	        	else if(evt.getMouseWheelDelta() < 0)
 	        		fireActionEvent("mousedown");
 	        /*
 	        default:
 	        	System.out.println(
 	        		"Unhandled Event:" + 
 					evt.getType() + 
 					evt.getType().name()
 				);
 			*/
         }
         
         // eat all mouse events
         return evt.isMouseEvent();
     }
 
 	public void fireActionEvent() {
 		for (ActionListener al : action_listeners) {
 			al.actionPerformed(new ActionEvent(this));
 		}
 	}
 	
 	public void fireActionEvent(String event) {
 		for (ActionListener al : action_listeners) {
 			al.actionPerformed(new ActionEvent(this, event));
 		}
 	}
 
 	public void addActionListener(ActionListener al) {
 		action_listeners.add(al);
 	}
 }
