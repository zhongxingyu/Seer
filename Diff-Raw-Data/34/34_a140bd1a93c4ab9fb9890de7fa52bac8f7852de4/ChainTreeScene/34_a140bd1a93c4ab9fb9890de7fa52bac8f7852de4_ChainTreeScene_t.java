 package tool;
 
 import j3dScene.J3DScene;
 
 import java.awt.Color;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.vecmath.Point3d;
 
 import dataStructure.CTLeaf;
 import dataStructure.ChainTree;
 import edu.geom3D.Cylinder;
 import edu.geom3D.Sphere;
 import edu.math.Vector;
 import geom3d.Cylinder3d;
 import geom3d.Sphere3d;
 
 /**
  * A 3D scene for painting multiple ChainTrees
  * 
  * @author hkb
  */
 public class ChainTreeScene {
 	
 	public J3DScene scene = J3DScene.createJ3DSceneInFrame();
 	private Map<ChainTree,GUINode[]> cTrees;
	private long lastRepaintTime;
 	
 	/*
 	 * Colours of the different part of the protein backbone.
 	 */
 	public Color[] colorBackbone = {new Color(0, 180, 180),
 							     	new Color(0, 50, 80),
 							     	new Color(180, 180, 180)};
 
 	public Color colorAlphaHelix = new Color(255, 255, 0);
 	public Color colorBetaSheet = new Color(0, 255, 0);
 	public Color colorHeteroAtom = new Color(255, 255, 255, 50);
 	
 	
 	
 	/**
 	 * Create a new empty scene.
 	 */
 	public ChainTreeScene() {
 		this.scene.setBackgroundColor(Color.WHITE);
 		
		this.lastRepaintTime = System.currentTimeMillis();
		
 		this.cTrees = new HashMap<ChainTree,GUINode[]>();
 	}
 	
 	/**
 	 * Create a new scene with one tree.
 	 * 
 	 * @param cTree The tree to be in the scene.
 	 */
 	public ChainTreeScene(ChainTree cTree) {
 		this();
 		
 		this.add(cTree);
 		
 		this.scene.centerCamera();
 		this.scene.autoZoom();
 	}
 	
 	/**
 	 * Create a new scene with multiple trees.
 	 * 
 	 * @param cTrees The trees to be on the scene.
 	 */
 	public ChainTreeScene(ChainTree[] cTrees) {
 		this();
 		
 		for(ChainTree cTree : cTrees) {
 			this.add(cTree);
 		}
 		
 		this.scene.centerCamera();
 		this.scene.autoZoom();
 	}
 	
 	
 	
 	/**
 	 * Add a chain tree to the scene.
 	 * 
 	 * @param cTree The chain tree to be added.
 	 */
 	public void add(ChainTree cTree) {
 		this.add(cTree, 100);
 	}
 	
 	/**
 	 * Add a chain tree to the scene.
 	 * 
 	 * @param cTree The chain tree to be added.
 	 * @param visibility The overall visibility of the chain tree.
 	 */
 	public void add(ChainTree cTree, int visibility) {
 		
 		if(!this.cTrees.containsKey(cTree)) {
 			List<Point3d> points = cTree.getBackboneAtomPositions();
 			
 			GUINode[] guiNodes = new GUINode[points.size()];
 			
 			for (int i = 0, j = points.size(); i < j; i++) {
 				Point3d current = points.get(i);
 				Point3d next = (i+1 < j) ? points.get(i+1) : null;
 				
 				GUINode node = new GUINode(current, next);
 				guiNodes[i] = node;
 				
 				Color color;
 				
 				// add GUI elements to scene
 				if (cTree.isInAlphaHelix(i)) {
 					color = this.colorAlphaHelix;
 				} else if (cTree.isInBetaSheet(i)) {
 					color = this.colorBetaSheet;
 				} else if (cTree.isHeteroAtomBond(i)) {
 					color = this.colorHeteroAtom;
 				} else {
 					color = this.colorBackbone[i%3];
 				}
 				
 				// draw shapes
 				this.scene.addShape(node.sphere, modifyTransparency(new Color(color.getRed(), color.getGreen(), color.getBlue(), 100), visibility));
 				
 				if (next != null)
 					this.scene.addShape(node.cylinder, modifyTransparency(color, visibility));
 			}
 			
 			// store the chain tree GUI info
 			this.cTrees.put(cTree, guiNodes);
 		}
 	}
 	
 	/**
 	 * Repaints the entire scene.
 	 */
 	public void repaint() {
 		for (ChainTree cTree : this.cTrees.keySet()) {
 			this.repaint(cTree);
 		}
 	}
 	
 	/**
 	 * Repaints only the specified tree.
 	 * 
 	 * @param cTree The tree to repaint.
 	 */
 	public void repaint(ChainTree cTree) {
 		List<Point3d> points = cTree.getBackboneAtomPositions();
 		GUINode[] guiNodes = this.cTrees.get(cTree);
 
 		for (int i = 0, j = points.size(); i < j; i++) {
 			Point3d current = points.get(i);
 			Point3d next =(i+1 < j) ? points.get(i+1) : null;
 			
 			guiNodes[i].update(current, next);
 		}
 		
		if (System.currentTimeMillis() - this.lastRepaintTime > 200) { // max 5 repaints per second
			this.lastRepaintTime = System.currentTimeMillis();
			this.scene.repaint();
		}
 	}
 	
 	/**
 	 * Modifies the transparency of a color.
 	 * @param color The colour to modify.
 	 * @param visibility The transparency.
 	 * @return New color with modified transparency.
 	 */
 	private static Color modifyTransparency(Color color, int visibility) {
 		int alpha = (int) Math.round(color.getAlpha() / 100.0 * visibility);
 		
 		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
 	}
 	
 	/**
 	 * Private class to store information about the graphical representation 
 	 * of an backbone element. 
 	 */
 /*
 	private class GUINode {
 		public Sphere sphere = new Sphere(new Vector(0,0,0), (float) CTLeaf.atomRadius/2);
 		public Cylinder cylinder = new Cylinder(new Vector(0,0,0), new Vector(0,0,0), 0.4f);
 		
 		public GUINode(Point3d current, Point3d next) {
 			this.update(current, next);
 		}
 		
 		public void update(Point3d current, Point3d next) {
 			this.sphere.center = this.pointToVector(next);
 			this.cylinder.p1 = this.pointToVector(current);
 			this.cylinder.p2 = this.pointToVector(next);		
 		}
 		
 		private Vector pointToVector(Point3d point) {
 			return new Vector(point.x, point.y, point.z);
 		}
 	}
 */
 	private class GUINode {
 		public Sphere3d sphere = new Sphere3d(new geom3d.Point3d(0,0,0), CTLeaf.atomRadius/2);
 		public Cylinder3d cylinder = new Cylinder3d(new geom3d.Point3d(0,0,0), new geom3d.Point3d(0,0,0), 0.4f);
 		
 		public GUINode(Point3d current, Point3d next) {
 			this.update(current, next);
 		}
 		
 		public void update(Point3d current, Point3d next) {
 			this.sphere.center = new geom3d.Point3d(current.x, current.y, current.z);
 			
 			if (next != null) {
 				this.cylinder.getSegment().setA(new geom3d.Point3d(current.x, current.y, current.z));
 				this.cylinder.getSegment().setB(new geom3d.Point3d(next.x, next.y, next.z));
 			}
 		}
 		
 		private Vector pointToVector(Point3d point) {
 			return new Vector(point.x, point.y, point.z);
 		}
 	}
 }
