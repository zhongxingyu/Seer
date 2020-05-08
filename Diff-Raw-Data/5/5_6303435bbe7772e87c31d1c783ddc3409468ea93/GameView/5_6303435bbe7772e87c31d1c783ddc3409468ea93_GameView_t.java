 package projectrts.view;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import projectrts.controller.InGameState;
 import projectrts.io.MaterialManager;
 import projectrts.io.TextureManager;
 import projectrts.model.IGame;
 import projectrts.model.entities.IEntity;
 import projectrts.model.world.INode;
 import projectrts.view.controls.MoveControl;
 import projectrts.view.spatials.AbstractSpatial;
 import projectrts.view.spatials.DebugNodeSpatial;
 import projectrts.view.spatials.SpatialFactory;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.material.Material;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 import com.jme3.terrain.geomipmap.TerrainLodControl;
 import com.jme3.terrain.geomipmap.TerrainQuad;
 import com.jme3.terrain.heightmap.AbstractHeightMap;
 import com.jme3.terrain.heightmap.ImageBasedHeightMap;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture.WrapMode;
 
 /**
  * The in-game view, creating and managing the scene.
  * @author Markus Ekstrm
  *
  */
 // TODO Markus: PMD: This class has too many methods, consider refactoring it.
 public class GameView implements PropertyChangeListener{
 	private final SimpleApplication app;
 	private final IGame game;
     private final Node entities = new Node("entities"); // The node for all entities
     private final Node selected = new Node("selected"); // The node for the selected graphics
     private final Node debug = new Node("debug"); // The node for the debugging graphics
     private Node terrainNode = new Node("terrain"); // The node for all terrain
     private Node mouseEffects = new Node("mouseEffects"); // The node for mouseEffects
     // TODO Markus: PMD: Perhaps 'matTerrain' could be replaced by a local variable.
     private Material matTerrain;
     // TODO Markus: PMD: Perhaps 'terrain' could be replaced by a local variable.
     private TerrainQuad terrain;
     private float mod = InGameState.MODEL_TO_WORLD; // The modifier value for converting lengths between model and world.
     
     private boolean debugNodes = false;
     
   //TODO Markus: Add javadoc
 	public GameView(SimpleApplication app, IGame game) {
 		this.app = app;
 		this.game = game;
 		game.getEntityManager().addListener(this);
 	}
 	
 	/**
 	 * Initializes the scene.
 	 * @param entitiesList A list containing the initial entities.
 	 * @param controlList The controls for the initial entities.
 	 */
 	public void initialize() {
 		initializeWorld();
 		initializeDebug();
 		initializeEntities();
 		initializeMouseEffects();
 		this.app.getRootNode().attachChild(selected);
 	}
 
 	/**
 	 * Based on Jmonkey terrain example code
 	 * http://jmonkeyengine.org/wiki/doku.php/jme3:beginner:hello_terrain
 	 */
     private void initializeWorld() {
     	
     	  /** 1. Create terrain material and load four textures into it. */
         matTerrain = MaterialManager.INSTANCE.getMaterial("Terrain");
      
         /** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
         matTerrain.setTexture("Alpha",TextureManager.INSTANCE.getTexture("Alpha"));
      
         /** 1.2) Add GRASS texture into the red layer (Tex1). */
         Texture grass = TextureManager.INSTANCE.getTexture("Grass");
         grass.setWrap(WrapMode.Repeat);
         matTerrain.setTexture("Tex1", grass);
         matTerrain.setFloat("Tex1Scale", 64f);
      
         /** 1.3) Add WATER texture into the green layer (Tex2) */
         Texture water = TextureManager.INSTANCE.getTexture("Water");
         water.setWrap(WrapMode.Repeat);
         matTerrain.setTexture("Tex2", water);
         matTerrain.setFloat("Tex2Scale", 32f);
      
         /** 1.4) Add ROAD texture into the blue layer (Tex3) */
         Texture rock = TextureManager.INSTANCE.getTexture("Rock");
         rock.setWrap(WrapMode.Repeat);
         matTerrain.setTexture("Tex3", rock);
         matTerrain.setFloat("Tex3Scale", 128f);
      
         /** 2. Create the height map */
         Texture heightMapImage = TextureManager.INSTANCE.getTexture("HeightMap");
        AbstractHeightMap heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
         heightmap.load();
      
         /** 3. We have prepared material and heightmap. 
          * Now we create the actual terrain:
          * 3.1) Create a TerrainQuad and name it "my terrain".
          * 3.2) A good value for terrain tiles is 64x64 -- so we supply 64+1=65.
          * 3.3) We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
          * 3.4) As LOD step scale we supply Vector3f(1,1,1).
          * 3.5) We supply the prepared heightmap itself.
          */
         int patchSize = 65;
         terrain = new TerrainQuad("my terrain", patchSize, 513, heightmap.getHeightMap());
      
         /** 4. We give the terrain its material, position & scale it, and attach it. */
         terrain.setMaterial(matTerrain);
         terrain.setLocalTranslation(0, 0, 0);
         terrain.setLocalScale(.02f, .01f, .02f);
         terrainNode.attachChild(terrain);
         app.getRootNode().attachChild(terrainNode);
 
         terrainNode.setLocalTranslation(2, -2, -100);
         terrainNode.rotateUpTo(new Vector3f(0f,0f,1f));
 
      
         /** 5. The LOD (level of detail) depends on were the camera is: */
         TerrainLodControl control = new TerrainLodControl(terrain, app.getCamera());
         terrain.addControl(control);
         
         
     }
 	
 	private void initializeDebug() {
 		if (debugNodes)
 		{
 			integrateNodes(game.getWorld().getNodes());
 		}
 		
 		this.app.getRootNode().attachChild(debug);
 	}
 
 	private void initializeEntities() {
 
     	integrateNewEntities(game.getEntityManager().getAllEntities());
     	
     	//Attach the entities node to the root node, connecting it to the world.
     	this.app.getRootNode().attachChild(entities);
     }
 	
 	private void initializeMouseEffects() {
 
     	//Attach the entities node to the root node, connecting it to the world.
     	this.app.getRootNode().attachChild(mouseEffects);
     }
     
     private void integrateNodes(INode[][] nodes)
     {
     	Box[][] nodeShapes = new Box[nodes.length][];
     	
     	for (int i=0; i<nodes.length; i++)
     	{
     		nodeShapes[i] = new Box[nodes[i].length];
     		for (int j=0; j<nodes[i].length; j++)
     		{
     			nodeShapes[i][j] = new Box(
     					new Vector3f((float)nodes[i][j].getPosition().getX()*mod,
     							-(float)nodes[i][j].getPosition().getY()*mod,
     							1),
     					(0.1f * mod)/2,
     					(0.1f * mod)/2,
     					0);
     			
     			AbstractSpatial nodeSpatial = SpatialFactory.INSTANCE.createNodeSpatial("DebugNodeSpatial",
     					nodes[i][j].getClass().getSimpleName(), nodeShapes[i][j], nodes[i][j]);
     			debug.attachChild(nodeSpatial);
     		}
     	}
 		
 	}
     
     private void integrateNewEntities(List<IEntity> newEntities) {
     	for(int i = 0; i < newEntities.size(); i++) {
     		integrateNewEntity(newEntities.get(i));
     	}
     }
     
     private void integrateNewEntity(IEntity newEntity) {
     	// Create shape.
 		// The location of the entity is initialized to (0, 0, 0) but is then instantly translated to the correct place by moveControl.
 		// Gets the size from the model and converts it to world size.
 		Box shape = new Box(new Vector3f(0, 0, 0),  
 									(newEntity.getSize() * mod)/2, (newEntity.getSize() * mod)/2, 0); 
 		// Create spatial.
 		AbstractSpatial entitySpatial = SpatialFactory.INSTANCE.createEntitySpatial(newEntity.getClass().getSimpleName() + "Spatial",
 				newEntity.getClass().getSimpleName(), shape, newEntity);
 		// Attach spatial to the entities node.
 		entities.attachChild(entitySpatial);
     }
     
     private void removeDeadEntity(IEntity entity) {
     	for(Spatial spatial : entities.getChildren()) {
     		if(spatial.getControl(MoveControl.class).getEntity().equals(entity)) {
 				spatial.removeFromParent();
 			}
 		}
     }
  
     /**
      * Draws the selected graphics for all entities in the passed list.
      * @param selectedEntities A list of selected entities.
      * @param controlList A list of controls for the select-spatials, one for each spatial.
      */
     public void drawSelected(List<IEntity> selectedEntities) {
     	// Remove all previously selected graphics
     	selected.detachAllChildren();
     	
     	for(int i = 0; i < selectedEntities.size(); i++) {
 	    	// Sets the location of the spatial to (0, 0, -1) to make sure it's behind the entities that use (x, y, 0).
 	    	// The control will instantly translate it to the correct location.
 	    	Box circle = new Box(new Vector3f(0, 0, -1), 
 	    			(selectedEntities.get(i).getSize() + 0.3f)/2 * mod, (selectedEntities.get(i).getSize() + 0.3f)/2 * mod, 0);
 	    	AbstractSpatial circleSpatial = SpatialFactory.INSTANCE.createEntitySpatial("SelectSpatial", selectedEntities.get(i).getName(), circle, selectedEntities.get(i));	
 	    	// Attach spatial to the selected node, connecting it to the world.
 	    	selected.attachChild(circleSpatial);
     	}
     }
     
     //TODO Jakob: Add javadoc
     public void drawNodes(List<projectrts.model.world.INode> coveredNodes){
     	   	
     	List<INode> oldNodes = getNodes(mouseEffects.getChildren());
     	
     	for (INode node : coveredNodes){
     		if (!oldNodes.contains(node)){
     			addNodeSpatial(node);
     		}
     	}  	
     	
     	for (INode node : oldNodes){
     		if (!coveredNodes.contains(node)){
     			removeNodeSpatial(node);
     		}
     	}
     	
     	
     }
     
 
 	private List<INode> getNodes(List<Spatial> coveredNodes){
 		List<INode> output = new ArrayList<INode>();
 		DebugNodeSpatial dSpatial;
 		for (Spatial spatial : coveredNodes){
 			if(spatial instanceof DebugNodeSpatial){
 				dSpatial = (DebugNodeSpatial) spatial;				
 				output.add(dSpatial.getNode());
 				
 			}
 			
 		}
 		return output;
 	}
 
 	private void removeNodeSpatial(INode node){	
 		DebugNodeSpatial dSpatial;
 		for(Spatial spatial: mouseEffects.getChildren()){
 			
 			if(spatial instanceof DebugNodeSpatial){
 				dSpatial = (DebugNodeSpatial)spatial;
 				if(node.equals(dSpatial.getNode())){
 					mouseEffects.detachChild(dSpatial);
 				}
 			}
 		}
 	}
 	
 	private void addNodeSpatial(INode node){	
 		Box nodeBox = new Box(new Vector3f((float)node.getPosition().getX()*mod,
 						-(float)node.getPosition().getY()*mod,1),
 				(1f * mod)/2,(1f * mod)/2,0);
 		AbstractSpatial nodeSpatial = 
 				SpatialFactory.INSTANCE.createNodeSpatial("DebugNodeSpatial",
 									node.getClass().getSimpleName(), nodeBox, node);
 				    		mouseEffects.attachChild(nodeSpatial);
 	}
 	
 		
   //TODO Jakob: Add javadoc
     public void clearNodes(){
     	mouseEffects.detachAllChildren();
     }
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if(evt.getPropertyName().equals("entityCreated")) {
 			if(evt.getNewValue() instanceof IEntity) {
 				integrateNewEntity((IEntity)evt.getNewValue());
 			}
 		} else if (evt.getPropertyName().equals("entityRemoved")) {
 			// TODO Markus: PMD: These nested if statements could be combined
 			if(evt.getOldValue() instanceof IEntity) {
 				removeDeadEntity((IEntity)evt.getOldValue());
 				drawSelected(game.getEntityManager().getSelectedEntities());
 			}
 		}	
 	}   
 }
