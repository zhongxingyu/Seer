 package ui.isometric.builder;
 
 import java.awt.Component;
 import java.awt.Point;
 import java.awt.dnd.DropTarget;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.filechooser.FileFilter;
 
 import data.Database;
 
 import ui.isometric.IsoCanvas;
 import ui.isometric.IsoDataSource;
 import ui.isometric.IsoGameModelDataSource;
 import ui.isometric.abstractions.IsoImage;
 import ui.isometric.builder.things.ThingCreator;
 import ui.isometric.builder.things.ThingCreatorDnD;
 import util.Direction;
 import util.Resources;
 
 import game.*;
 
 /**
  * 
  * The overall class that manages the entire user interface
  * 
  * @author melby
  *
  */
 public class IsoInterfaceWorldBuilder {
 	private JFrame frame;
 	private InspectorPanel inspector;
 	private LibraryFrame library;
 	private String frameName;
 	
 	private IsoCanvas canvas;
 	
 	private GameWorld world;
 	private IsoDataSource dataSource;
 	
 	private static final String EXTENTION = "wblrd";
 	
 	private ThingCreator storedCreator = null;
 	
 	/**
 	 * Create a world builder interface with a given GameWorld and ClientMessageHandler
 	 * @param name
 	 * @param world
 	 * @param logic
 	 */
 	public IsoInterfaceWorldBuilder(String name, final GameWorld world, ClientMessageHandler logic) {
 		this.world = world;
 		this.frameName = name;
 		
 		JMenuBar bar = new JMenuBar();
 		JMenu file = new JMenu("File");
 		JMenuItem save = new JMenuItem("Save");
 		file.add(save);
 		save.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				save();
 			}
 		});
 		JMenuItem load = new JMenuItem("Load");
 		file.add(load);
 		load.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				load();
 			}
 		});
 		bar.add(file);
 		
 		frame = new JFrame(name);
 		frame.setJMenuBar(bar);
 		dataSource = new IsoGameModelDataSource(this.world);
 		canvas = new IsoCanvas(dataSource);
 		canvas.addSelectionCallback(new IsoCanvas.SelectionCallback() {
 			@Override
 			public void selected(final IsoImage i, final Location l, MouseEvent event) {
 				if(event.getButton() == MouseEvent.BUTTON3 || event.isControlDown()) { // Right click
 //					if(i != null) {
 //						JPopupMenu popup = new JPopupMenu();
 //						for(GameThing t : i.gameThing().location().contents()) {
 //							JMenuItem n = new JMenuItem(t.name());
 //							n.setEnabled(false);
 //							popup.add(n);
 //							
 //							List<String> interactions = t.interactions();
 //							
 //							for(String intr : interactions) {									
 //								JMenuItem item = new JMenuItem("   "+intr);
 //								item.setName(t.gid()+"");
 //								item.addActionListener(new ActionListener() {
 //									@Override
 //									public void actionPerformed(ActionEvent e) {
 //										Object s = e.getSource();
 //										
 //										if(s instanceof JMenuItem) {
 //											JMenuItem m = (JMenuItem)s;
 //											for(GameThing t : dataSource.level()) {
 //												if(t instanceof Player) {
 //													world.thingWithGID(Long.parseLong(m.getName())).interact(m.getText().substring(3), (Player)t);
 //													break;
 //												}
 //											}
 //										}
 //									}
 //								});
 //								popup.add(item);
 //							}
 //						}
 //						popup.show(canvas, event.getPoint().x, event.getPoint().y);
 //					}
 					
 					if(storedCreator != null) {
 						canvas.calculateTypesAtAtPoint(event.getPoint());
 						dataSource.level().location(canvas.getCachedSelectedSquarePosition(), Direction.NORTH).put(storedCreator.createThing(world));
 					}
 				}
 				else {
 					if(event.isAltDown()) {
 						if(i != null) {
 							Location loc = i.gameThing().location();
 							if(loc instanceof Level.Location) {
 								((Level.Location)loc).rotate(Direction.EAST).put(i.gameThing());
 							}
 						}
 					}
					else {
						storedCreator = null;
						inspect(l);
					}
 				}
 			}
 		});
 		canvas.setDropTarget(new DropTarget(canvas, new ThingCreatorDnD.ThingDropListener(new ThingCreatorDnD.ThingDropListener.ThingDropListenerAction() {
 			@Override
 			public void thingCreatorDroped(Component onto, Point location, ThingCreator creator) {
 				if(onto instanceof IsoCanvas) {
 					storedCreator = creator;
 					IsoCanvas canvas = (IsoCanvas)onto;
 					canvas.calculateTypesAtAtPoint(location);
 					dataSource.level().location(canvas.getCachedSelectedSquarePosition(), Direction.NORTH).put(creator.createThing(world));
 				}
 			}
 		})));
 		canvas.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent arg0) {}
 
 			@Override
 			public void keyReleased(KeyEvent arg0) {}
 
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 				if(arg0.getKeyChar() == 'r') {
 					canvas.setViewDirection(canvas.viewDirection().compose(Direction.EAST));
 				}
 			}
 		});
 		frame.setSize(300, 300);
 		frame.add(canvas);
 		
 		inspector = new InspectorPanel(this);
 		inspector.getContentPane().setLayout(new BoxLayout(inspector.getContentPane(), BoxLayout.Y_AXIS));
 		inspector.setSize(200, 400);
 		inspector.getContentPane().add(Box.createVerticalGlue());
 		inspector.validate();
 		
 		library = new LibraryFrame(this);
 		library.setSize(200, 400);
 	}
 	
 	/**
 	 * Display this interface
 	 */
 	public void show() {
 		frame.setVisible(true);
 		inspector.setVisible(true);
 		library.setVisible(true);
 	}
 	
 	/**
 	 * Inspect a given location
 	 * @param l
 	 */
 	private void inspect(Location l) {
 		inspector.inspect(l);
 	}
 
 	/**
 	 * The GameWorld that this interface is using
 	 * @return
 	 */
 	public GameWorld gameWorld() {
 		return world;
 	}
 	
 	/**
 	 * Save the world
 	 */
 	public void save() {
 		String file = Database.treeToXML(world.toTree());
 		
 		JFileChooser chooser = new JFileChooser();
 		if(chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
 			File save = chooser.getSelectedFile().getAbsoluteFile();
 			if(!save.getAbsolutePath().endsWith("."+EXTENTION)) {
 				save = new File(save.getAbsolutePath() + "." + EXTENTION);
 			}
 			if(save.exists()) {
 				if(JOptionPane.showConfirmDialog(frame, "This file exists, are you sure you wish to overwrite it?", null, JOptionPane.OK_CANCEL_OPTION, 0) == JOptionPane.CANCEL_OPTION) {
 					return;
 				}
 			}
 			try {
 				BufferedWriter writer = new BufferedWriter(new FileWriter(save));
 				writer.write(file);
 				writer.flush();
 				writer.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Load the world
 	 */
 	public void load() {
 		String loaded = null;
 		
 		JFileChooser chooser = new JFileChooser();
 		chooser.setFileFilter(new FileFilter() {
 			@Override
 			public boolean accept(File arg0) {
 				return arg0.isFile() && arg0.getAbsolutePath().endsWith(EXTENTION);
 			}
 
 			@Override
 			public String getDescription() {
 				return "World Builder File";
 			}
 		});
 		if(chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
 			File load = chooser.getSelectedFile().getAbsoluteFile();
 			try {
 				loaded = Resources.loadTextFile(load.getAbsolutePath());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		else {
 			return;
 		}
 		
 		if(loaded == null) {
 			JOptionPane.showMessageDialog(frame, "Error loading file");
 			return;
 		}
 		
 		world.fromTree(Database.xmlToTree(loaded));
 	}
 	
 	/**
 	 * Get the name of the frame
 	 * @return
 	 */
 	public String frameName() {
 		return frameName;
 	}
 }
