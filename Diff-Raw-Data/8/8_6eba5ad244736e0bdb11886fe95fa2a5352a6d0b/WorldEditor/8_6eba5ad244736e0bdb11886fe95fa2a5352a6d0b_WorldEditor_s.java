 /*
 * Copyright (c) 2008, Sun Microsystems, Inc.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *     * Neither the name of Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 
 package com.sun.darkstar.example.tool;
 
 import com.jme.app.BaseSimpleGame;
 import com.jme.bounding.BoundingBox;
 import com.jme.image.Image;
 import com.jme.image.Texture;
 import com.jme.input.FirstPersonHandler;
 import com.jme.input.InputHandler;
 import com.jme.input.InputSystem;
 import com.jme.input.KeyBindingManager;
 import com.jme.input.KeyInput;
 import com.jme.input.MouseInput;
 import com.jme.light.DirectionalLight;
 import com.jme.math.FastMath;
 import com.jme.math.Vector3f;
 import com.jme.renderer.Camera;
 import com.jme.renderer.ColorRGBA;
 import com.jme.renderer.Renderer;
 import com.jme.renderer.pass.RenderPass;
 import com.jme.scene.Node;
 import com.jme.scene.PassNode;
 import com.jme.scene.PassNodeState;
 import com.jme.scene.Spatial;
 import com.jme.scene.Text;
 import com.jme.scene.Spatial.CullHint;
 import com.jme.scene.shape.Box;
 import com.jme.scene.shape.Quad;
 import com.jme.scene.shape.Sphere;
 import com.jme.scene.state.BlendState;
 import com.jme.scene.state.CullState;
 import com.jme.scene.state.LightState;
 import com.jme.scene.state.RenderState;
 import com.jme.scene.state.TextureState;
 import com.jme.scene.state.WireframeState;
 import com.jme.system.DisplaySystem;
 import com.jme.system.canvas.JMECanvas;
 import com.jme.system.canvas.SimplePassCanvasImpl;
 import com.jme.util.CloneImportExport;
 import com.jme.util.Debug;
 import com.jme.util.GameTaskQueue;
 import com.jme.util.GameTaskQueueManager;
 import com.jme.util.TextureManager;
 import com.jme.util.Timer;
 import com.jme.util.export.binary.BinaryExporter;
 import com.jme.util.geom.Debugger;
 import com.jme.util.lwjgl.LWJGLTextureUpdater;
 import com.jme.util.stat.StatCollector;
 import com.jme.util.stat.StatType;
 import com.jme.util.stat.graph.DefColorFadeController;
 import com.jme.util.stat.graph.GraphFactory;
 import com.jme.util.stat.graph.LineGrapher;
 import com.jme.util.stat.graph.TabledLabelGrapher;
 import com.jmex.awt.input.AWTKeyInput;
 import com.jmex.awt.input.AWTMouseInput;
 import com.jmex.awt.lwjgl.LWJGLAWTCanvasConstructor;
 import com.jmex.terrain.TerrainPage;
 import com.jmex.terrain.util.ImageBasedHeightMap;
 import com.jmex.terrain.util.ProceduralSplatTextureGenerator;
 import com.worldwizards.saddl.SADDL;
 import com.worldwizards.saddl.Tuple;
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTree;
 import javax.swing.SpinnerModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.LineBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import com.sun.darkstar.example.snowman.game.entity.util.EntityManager;
 import com.sun.darkstar.example.snowman.game.entity.view.util.ViewManager;
 import com.sun.darkstar.example.snowman.common.entity.EditableEntity;
 import com.sun.darkstar.example.snowman.common.entity.enumn.EEntity;
 import com.sun.darkstar.example.snowman.common.entity.terrain.TerrainEntity;
 import com.sun.darkstar.example.snowman.common.entity.view.EditableView;
 import com.sun.darkstar.example.snowman.common.entity.view.terrain.TerrainCluster;
 import com.sun.darkstar.example.snowman.common.entity.view.terrain.TerrainView;
 import com.sun.darkstar.example.snowman.common.entity.view.terrain.enumn.ESculpt;
 import com.sun.darkstar.example.snowman.common.util.enumn.EWorld;
 import com.sun.darkstar.example.snowman.common.world.EditableWorld;
 import com.sun.darkstar.example.snowman.common.world.World;
 import com.sun.darkstar.example.snowman.data.enumn.EDataType;
 
 /**
  * This is the main class for the World Editor gui It uses Swing and JME
  * together. It also uses a few utility classes I wrote to make building it up a
  * bit easier
  * 
  * @author Jeffrey Kesselman
  */
 public class WorldEditor extends JFrame {
 
 	/**
 	 * This is the tabbed panel that contains the console output box
 	 */
 	JTabbedPane consolePane;
 	/**
 	 * This is the tabbed panel that scene graoh display and the assets list
 	 */
 	JTabbedPane projectPane;
 	/**
 	 * This is the viewPanel that contains the JME Canvas
 	 */
 	JPanel viewPanel;
 	/**
 	 * This is the pannel that contaisn the object attributes editor
 	 */
 	JPanel objectAttr;
 	/**
 	 * This is the pannel that contaisn the tool attributes editor
 	 */
 	JPanel toolAttr;
 	/**
 	 * These are the actual atribute editors. They are specialized JTables that
 	 * accept and edit a Map.
 	 */
 	AttributeEditor objectAttrTable;
 	AttributeEditor toolAttrTable;
 	/**
 	 * This is the wrapper for the Scene Graph display that allows it to scroll.
 	 */
 	JPanel treeScrollPane;
 	/**
 	 * This is the JTree that actually displays the Scene Graph within the scene
 	 * graph display panel.
 	 */
 	JTree sceneTree;
 	/**
 	 * This is the model that interpets the scene graph for the JTree
 	 */
 	JMonkeyTreeModel treeModel;
 
 	/**
 	 * This is the lit of textures
 	 * 
 	 */
 	PopUpList textures;
 	/**
 	 * This is the actual text area that displays the console output
 	 */
 	JTextArea consoleOutput;
 
 	// JME objects
 	/**
 	 * This is an AWT canvas that JME will use to paint the 3D output onto
 	 */
 	private Canvas canvas;
 	/**
 	 * This is the root of the current JME scene graph
 	 */
 	Node rootNode;
 	/**
 	 * This is the actual "game object" for JME. The one in this file at the
 	 * moment is just a skeleton that should be replaced with the real JME logic
 	 */
 	WorldEditorCanvasImpl impl;
 	/**
 	 * The DisplaySystem is a fundemental JME interface to the actual display
 	 * hardware.
 	 */
 	DisplaySystem display;
 
 	/**
 	 * This is the current working world object
 	 * 
 	 */
 	EditableWorld world;
 
 	/**
 	 * Enum for button bar
 	 */
 	enum ModeEnum {
 		Select, Move, Raise, Lower, Smooth, Paint, Erase
 	}
 
 	private ModeEnum currentMode;
 
 	private Brush brush;
 
 	private WorldEditorMouseListener mouseListener;
 
 	private boolean pressed;
 
 	private BlendState blend;
 
 	private TerrainView terrainView;
 
 	private TextureLayer selectedLayer;
 	
 	private Callable exportAction;
 	
 	private ExportDialog dlg;
 
 	private LinkedList<TextureLayer> layers;
 	
 	/**
 	 * Holds brush properties
 	 */
 	private Map<String,String> brushProperties = new HashMap<String,String>();
 
 	/**
 	 * This is the constructor for the world editor. To start it all running
 	 * you instance the WorldEditor using this contructor and call start() on
 	 * the returned instance.
 	 */
 	public WorldEditor() {
 		this.layers = new LinkedList<TextureLayer>();
 		this.setMinimumSize(new Dimension(1024, 768));
 		Container c = getContentPane();
 		c.setLayout(new BorderLayout());
 		try {
 			List<Tuple> ui = SADDL.parse(new InputStreamReader(getClass()
 					.getResourceAsStream("res/menubar.sdl")));
 			for (Tuple tuple : ui) {
 				if (tuple.getKey().equalsIgnoreCase("MenuBar")) {
 					setJMenuBar(new JKMenuBar((List<Tuple>) tuple.getValue()));
 				}
 			}
 			// for the moment lets do it by hand, soemday well make it data
 			// driven
 			EnumButtonBar bar = new EnumButtonBar<ModeEnum>(ModeEnum.values());
 			bar.addListener(new EnumButtonBarListener() {
 
 				@Override
 				public void enumSet(Enum actualEnum) {
 					currentMode = (ModeEnum)actualEnum;
 					if(currentMode == ModeEnum.Raise || currentMode == ModeEnum.Lower || currentMode == ModeEnum.Smooth
 							|| currentMode == ModeEnum.Paint || currentMode == ModeEnum.Erase) {
 						if(world == null || terrainView == null) return;
 						if(brush.getParent() == null) world.attachChild(brush);
 					} else {
 						if(world == null || terrainView == null) return;
 						world.detachChild(brush);
 					}
 					if(currentMode == ModeEnum.Raise || currentMode == ModeEnum.Lower || currentMode == ModeEnum.Smooth) {
 						brush.setColor(ColorRGBA.red);
 					} else if(currentMode == ModeEnum.Paint || currentMode == ModeEnum.Erase) {
 						brush.setColor(ColorRGBA.blue);
 					}
 					mouseListener.setMode(currentMode);
 					brush.updateRenderState();
 				}
 			});
 			c.add(bar, BorderLayout.NORTH);
 			viewPanel = new JPanel();
 			viewPanel.setName("View");
 			viewPanel.setLayout(new BorderLayout());
 			viewPanel.setBorder(new TitledBorder(new LineBorder(Color.BLACK),
 			"View"));
 			c.add(viewPanel, BorderLayout.CENTER);
 			consolePane = new JTabbedPane();
 			consolePane.setPreferredSize(new Dimension(800, 100));
 			consolePane.setName("Console Pane");
 			JPanel consolePanel = new JPanel();
 			consolePanel.setLayout(new BorderLayout());
 			consoleOutput = new JTextArea();
 			consolePanel.add(consoleOutput, BorderLayout.CENTER);
 			consolePane.addTab("Console", new JScrollPane(consolePanel));
 			c.add(consolePane, BorderLayout.SOUTH);
 			projectPane = new JTabbedPane();
 			treeScrollPane = new JPanel();
 			treeScrollPane.setLayout(new BorderLayout());
 			projectPane.addTab("Scene Graph", treeScrollPane);
 			JPanel texturePanel = new JPanel();
 			textures = new PopUpList(new DefaultListModel());
 			textures.addListSelectionListener(new ListSelectionListener(){
 
 				@Override
 				public void valueChanged(ListSelectionEvent e) {
 					selectedLayer = (TextureLayer)textures.getSelectedValue();
 				}});
 
 			texturePanel.add(new JScrollPane(textures));
 			// add menu items to texture popup menu
 			JMenuItem deleteTexture = new JMenuItem("Delete");
 			deleteTexture.addActionListener(new ActionListener(){
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					TextureManager.releaseTexture(selectedLayer.getAlpha());
 					TextureManager.releaseTexture(selectedLayer.getColor());
 					terrainView.detachPass(selectedLayer.getPass());
 					terrainView.updateRenderState();
 					DefaultListModel model = (DefaultListModel) textures.getModel();
 					model.remove(textures.getSelectedIndex());
 					textures.repaint();
 				}});
 			textures.addToPopup(deleteTexture);
 			projectPane.addTab("Texture Layers",textures);
 			projectPane.setPreferredSize(new Dimension(200, 400));
 			c.add(projectPane, BorderLayout.WEST);
 			JPanel eastPanel = new JPanel();
 			eastPanel.setLayout(new GridLayout(2, 1));
 			objectAttr = new JPanel();
 			objectAttr.setName("Object Attributes");
 			objectAttr.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "Object Attributes"));
 			objectAttr.setLayout(new BorderLayout());
 			objectAttrTable = new AttributeEditor();
 			objectAttr.add(new JScrollPane(objectAttrTable),
 					BorderLayout.CENTER);
 			eastPanel.add(objectAttr);
 			toolAttr = new JPanel();
 			toolAttr.setBorder(new TitledBorder(new LineBorder(Color.BLACK),
 			"Tool Attributes"));
 			toolAttr.setLayout(new BorderLayout());
 			toolAttrTable = new AttributeEditor();
 			toolAttrTable.addListener(new AttributeEditorListener(){
 				@Override
 				public void attributeChanged(String key, String value) {
 					if(key.equalsIgnoreCase("radius")) {
 						brush.setRadius(Float.valueOf(value));
 					} else if(key.equalsIgnoreCase("intensity")) {
 						brush.setIntensity(Float.valueOf(value));
 					}
 				}});
 			toolAttr.add(new JScrollPane(toolAttrTable), BorderLayout.CENTER);
 			eastPanel.add(toolAttr);
 			eastPanel.setPreferredSize(new Dimension(200, 400));
 			c.add(eastPanel, BorderLayout.EAST);
 			setDefaultCloseOperation(this.DISPOSE_ON_CLOSE);
 			impl = new WorldEditorCanvasImpl(800, 600);
 			set3DCanvas(impl);
 			initProperties();
 			((JKMenuBar) getJMenuBar())
 			.addListener(new WorldEditorMenuListener() {
 
 				@Override
 				public void doAttachTo() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doCreateDirectionalLight() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doCreateLineParticle() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doCreatePointLight() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doCreateProjectedWater() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doCreateQuadParticle() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doCreateQuadWater() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doCreateSpotLight() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doCreateTextureLayer() {
 					if(world == null || terrainView == null) return;
 					JFileChooser chooser = new JFileChooser();
 					FileNameExtensionFilter filter = new FileNameExtensionFilter(
 							"Color Map Images", "jpg","bmp","tga");
 					chooser.setFileFilter(filter);
 					int returnVal = chooser.showOpenDialog(WorldEditor.this);
 					if (returnVal != JFileChooser.APPROVE_OPTION){
 						return;
 					}
 					File colorMap = chooser.getSelectedFile();
 					filter = new FileNameExtensionFilter(
 							"AlphaMapImage", "tga", "png");
 					chooser.setFileFilter(filter);
 					returnVal = chooser.showOpenDialog(WorldEditor.this);
 					if (returnVal != JFileChooser.APPROVE_OPTION){
 						return;
 					}
 					File alphaMap = chooser.getSelectedFile();
 					DefaultListModel mdl = (DefaultListModel) textures.getModel();
 					float xBound = ((BoundingBox)terrainView.getTerrainCluster().getWorldBound()).xExtent;
 					float zBound = ((BoundingBox)terrainView.getTerrainCluster().getWorldBound()).zExtent;
 					TextureLayer layer = new TextureLayer(colorMap,alphaMap, xBound, zBound);
 					layers.add(layer);
 					mdl.add(mdl.size(),layer);
 					terrainView.attachPass(layer.createPass(blend));
 					terrainView.updateRenderState();
 				}
 
 				@Override
 				public void doCreateWorld() {
 					String[] names = new String[EWorld.values().length];
 					for (int i = 0; i < names.length; i++) {
 						names[i] = EWorld.values()[i].name();
 					}
 					String selected = (String) JOptionPane
 					.showInputDialog(WorldEditor.this,
 							"What kind of world",
 							"Create World",
 							JOptionPane.PLAIN_MESSAGE, null,
 							names, names[0]);
 					if (selected == null)
 						return;
 					EWorld selectedEworld = EWorld.valueOf(selected);
 					world = new EditableWorld(selectedEworld);
 					treeModel.addChild(impl.getRootNode(), world);
 					WorldEditor.this.repaint();
 				}
 
 				@Override
 				public void doDettachFromParent() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doExit() {
 					dispose();
 				}
 
 				@Override
 				public void doExportWorld() {
 					dlg = new ExportDialog(WorldEditor.this);
 					if (dlg.showDialog()){
 						exportAction = new Callable() {
 							@Override
 							public Object call() throws Exception {
 									if(dlg.hasFile()) {
 										CloneImportExport cloner = new CloneImportExport();
 										cloner.saveClone(world);
 										EditableWorld cloneWorld = (EditableWorld)cloner.loadClone();
 										World node = (World)cloneWorld.constructFinal();
 										if(!dlg.exportTextures()) {
 											this.stripTexure(node);
 										} else {
 											while(!layers.isEmpty()) {
 												this.exportAlpha(layers.pop());
 											}
 										}
 										try {
 											BinaryExporter.getInstance().save(node, dlg.getFile());
 										} catch (IOException e) {
 											e.printStackTrace();
 										}
 									}
 								return null;
 							}
 							
 							private void stripTexure(Node node) {
 								node.clearRenderState(RenderState.RS_TEXTURE);
 								for(int i = 0; i < node.getQuantity(); i++) {
 									Spatial child = node.getChild(i);
 									if(child instanceof PassNode) ((PassNode)child).clearAll();
 									else if(child instanceof Node) this.stripTexure((Node)child);
 									else child.clearRenderState(RenderState.RS_TEXTURE);
 								}
 							}
 							
 							private void exportAlpha(TextureLayer layer) {
 								String raw = dlg.getFile().getPath();
 								String path = raw.substring(0, raw.lastIndexOf("\\")+1) + layer.getAlphaName() + EDataType.Texture.getExtension();
 								File file = new File(path);
 								try {file.createNewFile();} catch (IOException e) {e.printStackTrace();}
 								try {
 									BinaryExporter.getInstance().save(layer.getAlpha().getImage(), file);
 								} catch (IOException e) {
 									e.printStackTrace();
 								}
 							}
 						};
 					}
 				}
 
 				@Override
 				public void doExportSelected() {
 					ExportDialog dlg = new ExportDialog(WorldEditor.this);
 					if (dlg.showDialog()){
 						String exportName = dlg.getName();
 						boolean exportTextures = dlg.exportTextures();
 					}
 
 				}
 
 				@Override
 				public void doHelp() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doImportModel() {
 					// TODO
 				}
 
 				@Override
 				public void doModelPerspective() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doMove() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doNew() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doOpen() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doRotateX() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doRotateY() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doRotateZ() {
 					// TODO Auto-generated method stub
 
 				}
 
 				@Override
 				public void doWorldPerspective() {
 					// TODO Auto-generated method stub
 
 				}
 			});
 
 			pack();
 			setSize(800, 600);
 			setVisible(true);
 			// Note, this requires youi to refresh the canvas whenever a change
 			// is
 			// made to re-render it. If you want automatic refresh agt the cost
 			// of
 			// some performance, uncomment this line...
 			// /startAutoRefresh(canvas);
 		} catch (IOException ex) {
 			Logger.getLogger(WorldEditor.class.getName()).log(Level.SEVERE,
 					null, ex);
 		}
 	}
 
 	private void initProperties() {
 		brushProperties.put("Radius","5");
 		brushProperties.put("Intensity", "0.1");
 		toolAttrTable.setAttributes(brushProperties);
 
 	}
 
 	/**
 	 * This is the metohd that actually installs the canvas implementation into
 	 * the interfsce
 	 * 
 	 * @param impl
 	 *            An implementation of the JME SimpleCanvasImpl class
 	 */
 	public void set3DCanvas(final SimplePassCanvasImpl impl) {
 		// -------------GL STUFF------------------
 
 		// make the canvas:
 		display = DisplaySystem.getDisplaySystem("lwjgl");
 		display.setMinDepthBits(24);
 		display.setMinStencilBits(8);
 		display.setMinAlphaBits(8);
 		display.setMinSamples(2);
 		display.registerCanvasConstructor("AWT", LWJGLAWTCanvasConstructor.class);
 		final Canvas comp = (Canvas)display.createCanvas(1024, 768);
 		canvas = comp;
 
 		// add a listener... if window is resized, we can do something about it.
 		comp.addComponentListener(new ComponentAdapter() {
 
 			@Override
 			public void componentResized(ComponentEvent ce) {
 				impl.resizeCanvas(comp.getWidth(), comp.getHeight());
 			}
 		});
 		KeyInput.setProvider(KeyInput.INPUT_AWT);
 		KeyListener kl = (KeyListener) KeyInput.get();
 		comp.addKeyListener(kl);
 		AWTMouseInput.setup(comp, false);
 
 		// Important! Here is where we add the guts to the panel:
 
 		JMECanvas jmeCanvas = ((JMECanvas) comp);
 		jmeCanvas.setImplementor(impl);
 		jmeCanvas.setUpdateInput(true);
 
 		viewPanel.add(comp, BorderLayout.CENTER);
 	}
 
 	/**
 	 * This method shoudl be used to set the root of the scene graph. In
 	 * addition to setting it for the JME display it also updates the scene
 	 * graph tree display in the Project panel
 	 * 
 	 * @param root
 	 *            The root of the scene graph
 	 */
 	public void setCurrentSceneGraphTree(Node root) {
 		treeModel = new JMonkeyTreeModel(root);
 		final PopUpTree tree = new PopUpTree(treeModel);
 		// final JTree tree = new JTree(new DefaultMutableTreeNode());
 		tree.setEditable(true);
 		tree.getSelectionModel().setSelectionMode(
 				TreeSelectionModel.SINGLE_TREE_SELECTION);
 		tree.setShowsRootHandles(true);
 		JMenuItem addTerrain = new JMenuItem("Add Enitity");
 		tree.addToPopup(addTerrain);
 		addTerrain.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				doAddEntity(tree.getSelectionPath());
 			}
 		});
 
 		JMenuItem delete = new JMenuItem("Remove Entity");
 		delete.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				deleteNode(tree.getSelectionPath());
 			}
 		});
 		tree.addToPopup(delete);
 		tree.setOpaque(true);
 
 		treeScrollPane.removeAll();
 		treeScrollPane.add(new JScrollPane(tree), BorderLayout.CENTER);
 		repaint();
 	}
 
 	/**
 	 * This method is called by the add terrain selection on the pop up menu.
 	 * 
 	 * @param path
 	 *            The path on the tree to the node that was selected for action
 	 */
 	public void doAddEntity(TreePath path) {
 		if(world == null) return;
 		Node node = (Node) path.getLastPathComponent();
 		String[] names = new String[EEntity.values().length];
 		for (int i = 0; i < names.length; i++) {
 			names[i] = EEntity.values()[i].name();
 		}
 		String selected = (String) JOptionPane.showInputDialog(
 				WorldEditor.this, "What kind of entity", "Create Entity",
 				JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
 		EEntity selectedEntity = EEntity.valueOf(selected);
 		if (selectedEntity == null)	return;
 		EditableEntity entity = (EditableEntity) EntityManager.getInstance().createEntity(selectedEntity);
 		if (selectedEntity == EEntity.Terrain) {
 			TerrainDialog dialog = new TerrainDialog(this);
 			if (dialog.wasCanceled()) {
 				return;
 			}
 			Dimension d = dialog.getTerrainSize();
 			int tris = dialog.getTrisPerMesh();
 			((TerrainEntity) entity).setWidth((int) d.getWidth());
 			((TerrainEntity) entity).setDepth((int) d.getHeight());
 			((TerrainEntity) entity).setTrianglesPerMesh(tris);
 		}
 		EditableView view = (EditableView) ViewManager.getInstance().createView(entity);
 		if (selectedEntity == EEntity.Terrain) {
 			this.terrainView = (TerrainView)view;
 			this.terrainView.getTerrainCluster().setDetailTexture(1, 1);
 		}
 		this.world.attachView(view);
 		treeModel.addChild(node, view);
 		repaint();
 	}
 
 	/**
 	 * This method is called by the add placeable selection on the pop up menu.
 	 * 
 	 * @param path
 	 *            The path on the tree to the node that was selected for action
 	 */
 	public void doAddPlaceable(TreePath path) {
 
 		Node node = (Node) path.getLastPathComponent();
 		// load and add palceable to tree here
 		repaint();
 	}
 
 	/**
 	 * This method is called by the delete selection on the popup menu
 	 * 
 	 * @param path
 	 *            The path on the tree to the node that was selected for action
 	 */
 	public void deleteNode(TreePath path) {
 		Spatial node = (Spatial) path.getLastPathComponent();
 		treeModel.deleteNode(node);
 		repaint();
 	}
 
 	/**
 	 * This method puts text out toi the console window. It does not append
 	 * newlines so any text that ends a line should have "\n" append to it by
 	 * the caller.
 	 * 
 	 * @param text
 	 *            The text to append in the console window.
 	 */
 	public void outputToConsole(String text) {
 		consoleOutput.append(text);
 	}
 
 	/**
 	 * This is the "game guts" implementation for the JME Canvas. As is it is a
 	 * skeleton that should have terrain edior logic added to it.
 	 */
 	class WorldEditorCanvasImpl extends SimplePassCanvasImpl {
 
 		private final Logger logger = Logger
 		.getLogger(WorldEditorCanvasImpl.class.getName());
 		/**
 		 * Handles our mouse/keyboard input.
 		 */
 		protected InputHandler input;
 
 		/**
 		 * True if the renderer should display the depth buffer.
 		 */
 		protected boolean showDepth = false;
 
 		/**
 		 * True if the renderer should display bounds.
 		 */
 		protected boolean showBounds = false;
 
 		/**
 		 * True if the renderer should display normals.
 		 */
 		protected boolean showNormals = false;
 
 		/**
 		 * True if the we should show the stats graphs.
 		 */
 		protected boolean showGraphs = false;
 
 		/**
 		 * A wirestate to turn on and off for the rootNode
 		 */
 		protected WireframeState wireState;
 
 		/**
 		 * A lightstate to turn on and off for the rootNode
 		 */
 		protected LightState lightState;
 
 		/**
 		 * boolean for toggling the simpleUpdate and geometric update parts of
 		 * the game loop on and off.
 		 */
 		protected boolean pause;
 
 		private Quad lineGraph, labGraph;
 
 		public WorldEditorCanvasImpl(int width, int height) {
 			super(width, height);
 			System.setProperty("jme.stats", "set");
 		}
 
 		@Override
 		public void simpleSetup() {
 			setCurrentSceneGraphTree(getRootNode());
 			/** Create a basic input controller. */
 			FirstPersonHandler firstPersonHandler = new FirstPersonHandler(cam,
 					50, 1);
 			firstPersonHandler.getMouseLookHandler().getMouseLook()
 			.setButtonPressRequired(true);
 			firstPersonHandler.getMouseLookHandler().setActionSpeed(0.3f);
 			input = firstPersonHandler;
 
 			/** Get a high resolution timer for FPS updates. */
 			timer = Timer.getTimer();
 
 			/** Sets the title of our display. */
 			String className = getClass().getName();
 			if (className.lastIndexOf('.') > 0)
 				className = className.substring(className.lastIndexOf('.') + 1);
 			display.setTitle(className);
 
 			/** Assign key P to action "toggle_pause". */
 			KeyBindingManager.getKeyBindingManager().set("toggle_pause",
 					KeyInput.KEY_P);
 			/** Assign key ADD to action "step". */
 			KeyBindingManager.getKeyBindingManager().set("step",
 					KeyInput.KEY_ADD);
 			/** Assign key T to action "toggle_wire". */
 			KeyBindingManager.getKeyBindingManager().set("toggle_wire",
 					KeyInput.KEY_T);
 			/** Assign key L to action "toggle_lights". */
 			KeyBindingManager.getKeyBindingManager().set("toggle_lights",
 					KeyInput.KEY_L);
 			/** Assign key B to action "toggle_bounds". */
 			KeyBindingManager.getKeyBindingManager().set("toggle_bounds",
 					KeyInput.KEY_B);
 			/** Assign key N to action "toggle_normals". */
 			KeyBindingManager.getKeyBindingManager().set("toggle_normals",
 					KeyInput.KEY_N);
 			/** Assign key C to action "camera_out". */
 			KeyBindingManager.getKeyBindingManager().set("camera_out",
 					KeyInput.KEY_C);
 			/** Assign key R to action "mem_report". */
 			KeyBindingManager.getKeyBindingManager().set("mem_report",
 					KeyInput.KEY_R);
 			KeyBindingManager.getKeyBindingManager().set("screen_shot",
 					KeyInput.KEY_F1);
 			KeyBindingManager.getKeyBindingManager().set("toggle_stats",
 					KeyInput.KEY_F4);
 
 			wireState = display.getRenderer().createWireframeState();
 			wireState.setEnabled(false);
 			rootNode.setRenderState(wireState);
 
 			/** Attach the light to a lightState and the lightState to rootNode. */
 			DirectionalLight light1 = new DirectionalLight();
 			light1.setDiffuse(ColorRGBA.white);
 			light1.setEnabled(true);
 			light1.setDirection(new Vector3f(0, -1, 0));
 			DirectionalLight light2 = new DirectionalLight();
 			light2.setDiffuse(ColorRGBA.blue);
 			light2.setEnabled(true);
 			light2.setDirection(Vector3f.UNIT_X);
 			lightState = display.getRenderer().createLightState();
 			lightState.setEnabled(true);
 			lightState.attach(light1);
 			lightState.attach(light2);
 			rootNode.setRenderState(lightState);
 
 			RenderPass rootPass = new RenderPass();
 			rootPass.add(this.rootNode);
 			this.manager.add(rootPass);
 
 			brush = new Brush(128, 5, ColorRGBA.red);
 
 			mouseListener = new WorldEditorMouseListener(WorldEditor.this);
 			canvas.addMouseMotionListener(mouseListener);
 			canvas.addMouseListener(mouseListener);
 			canvas.addMouseWheelListener(mouseListener);
 			this.buildBlendState();
 		}
 
 		private void buildBlendState() {
 			blend = display.getRenderer().createBlendState();
 			blend.setBlendEnabled(true);
 			blend.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
 			blend.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
 			blend.setTestEnabled(true);
 			blend.setTestFunction(BlendState.TestFunction.GreaterThan);
 			blend.setEnabled(true);
 		}
 
 		@Override
 		public void simpleUpdate() {
 			if(exportAction != null) {
 				try {
 					exportAction.call();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				exportAction = null;
 			}
 			/** Check for key/mouse updates. */
 			input.update(tpf);
 
 			/** update stats, if enabled. */
 			if (Debug.stats) {
 				StatCollector.update();
 			}
 
 			// Execute updateQueue item
 			GameTaskQueueManager.getManager().getQueue(GameTaskQueue.UPDATE).execute();
 
 			/** If toggle_pause is a valid command (via key p), change pause. */
 			if (KeyBindingManager.getKeyBindingManager().isValidCommand(
 					"toggle_pause", false)) {
 				pause = !pause;
 			}
 
 			/**
 			 * If step is a valid command (via key ADD), update scenegraph one
 			 * unit.
 			 */
 			if (KeyBindingManager.getKeyBindingManager().isValidCommand("step",
 					true)) {
 				simpleUpdate();
 				rootNode.updateGeometricState(tpf, true);
 			}
 
 			/** If toggle_wire is a valid command (via key T), change wirestates. */
 			if (KeyBindingManager.getKeyBindingManager().isValidCommand(
 					"toggle_wire", false)) {
 				wireState.setEnabled(!wireState.isEnabled());
 				rootNode.updateRenderState();
 			}
 			/**
 			 * If toggle_lights is a valid command (via key L), change
 			 * lightstate.
 			 */
 			if (KeyBindingManager.getKeyBindingManager().isValidCommand(
 					"toggle_lights", false)) {
 				lightState.setEnabled(!lightState.isEnabled());
 				rootNode.updateRenderState();
 			}
 			/** If toggle_bounds is a valid command (via key B), change bounds. */
 			if (KeyBindingManager.getKeyBindingManager().isValidCommand(
 					"toggle_bounds", false)) {
 				showBounds = !showBounds;
 			}
 
 			/** If toggle_depth is a valid command (via key F3), change depth. */
 			if (KeyBindingManager.getKeyBindingManager().isValidCommand(
 					"toggle_depth", false)) {
 				showDepth = !showDepth;
 			}
 
 			if (Debug.stats) {
 				/** handle toggle_stats command (key F4) */
 				if (KeyBindingManager.getKeyBindingManager().isValidCommand(
 						"toggle_stats", false)) {
 					showGraphs = !showGraphs;
 					Debug.updateGraphs = showGraphs;
 					labGraph.clearControllers();
 					lineGraph.clearControllers();
 					labGraph.addController(new DefColorFadeController(labGraph,
 							showGraphs ? .6f : 0f, showGraphs ? .5f : -.5f));
 					lineGraph.addController(new DefColorFadeController(
 							lineGraph, showGraphs ? .6f : 0f, showGraphs ? .5f
 									: -.5f));
 				}
 			}
 
 			if (KeyBindingManager.getKeyBindingManager().isValidCommand(
 					"toggle_normals", false)) {
 				showNormals = !showNormals;
 			}
 			/**
 			 * If camera_out is a valid command (via key C), show camera
 			 * location.
 			 */
 			if (KeyBindingManager.getKeyBindingManager().isValidCommand(
 					"camera_out", false)) {
 				logger.info("Camera at: "
 						+ display.getRenderer().getCamera().getLocation());
 			}
 
 			if (KeyBindingManager.getKeyBindingManager().isValidCommand(
 					"screen_shot", false)) {
 				display.getRenderer().takeScreenShot("SimpleGameScreenShot");
 			}
 
 			if (KeyBindingManager.getKeyBindingManager().isValidCommand(
 					"mem_report", false)) {
 				long totMem = Runtime.getRuntime().totalMemory();
 				long freeMem = Runtime.getRuntime().freeMemory();
 				long maxMem = Runtime.getRuntime().maxMemory();
 
 				logger.info("|*|*|  Memory Stats  |*|*|");
 				logger.info("Total memory: " + (totMem >> 10) + " kb");
 				logger.info("Free memory: " + (freeMem >> 10) + " kb");
 				logger.info("Max memory: " + (maxMem >> 10) + " kb");
 			}
 			this.updateSculpting();
 			this.updateTexturing();
 		}
 
 		private void updateSculpting() {
 			if(pressed) {
 				if(currentMode == ModeEnum.Raise || currentMode == ModeEnum.Lower || currentMode == ModeEnum.Smooth) {
 					ESculpt enumn = ESculpt.valueOf(currentMode.toString());
 					terrainView.getTerrainCluster().sculptCluster(enumn, brush.getWorldBound().getCenter(),
 							brush.getWorldTranslation(), brush.getRadius(), brush.getIntensity());
 				}
 			}
 		}
 
 		private void updateTexturing() {
			if(pressed && selectedLayer != null) {
 				switch(currentMode) {
 				case Paint: selectedLayer.modifyAlpha(mouseListener.getIntersection(), brush.getRadius(), brush.getIntensity()); break;
 				case Erase: selectedLayer.modifyAlpha(mouseListener.getIntersection(), brush.getRadius(), -brush.getIntensity()); break;
 				}
 			}
 		}
 
 		@Override
 		public void simpleRender() {
 			doDebug(display.getRenderer());
 		}
 
 		protected void doDebug(Renderer r) {
 			/**
 			 * If showing bounds, draw rootNode's bounds, and the bounds of all
 			 * its children.
 			 */
 			if (showBounds) {
 				Debugger.drawBounds(rootNode, r, true);
 			}
 
 			if (showNormals) {
 				Debugger.drawNormals(rootNode, r);
 				Debugger.drawTangents(rootNode, r);
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		}
 		new WorldEditor();
 	}
 
 	/**
 	 * This method is used to set a JME canvas update thread. If this is not
 	 * called then the cancas will only update when it is repainted by Swing.
 	 * 
 	 * @param canvas
 	 *            The canvas to update on a tick
 	 */
 	private void startAutoRefresh(final Canvas canvas) {
 		new Thread() {
 
 			{
 				setDaemon(true);
 			}
 
 			public void run() {
 				while (true) {
 					canvas.repaint();
 					try {
 						Thread.sleep(1000 / 30); // 30 fps
 					} catch (InterruptedException ex) {
 						Logger.getLogger(WorldEditor.class.getName()).log(
 								Level.SEVERE, null, ex);
 					}
 				}
 			};
 		}.start();
 	}
 
 	public void setPressed(boolean pressed) {
 		this.pressed = pressed;
 	}
 
 	public Brush getBrush() {
 		return this.brush;
 	}
 
 	public EditableWorld getWorld() {
 		return this.world;
 	}
 
 	public TerrainCluster getTerrain() {
 		return this.terrainView.getTerrainCluster();
 	}
 
 	public Canvas getCanvas() {
 		return this.canvas;
 	}
 
 	public boolean isPressed() {
 		return this.pressed;
 	}
 }
