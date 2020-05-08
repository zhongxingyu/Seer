 /**
  *
  * @author Ryan
  * 
  * This class makes invisible shapes that represent reasons for the
  * player to put or not put a wind farm at the location.
  */
 package mygame;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.collision.CollisionResults;
 import com.jme3.effect.ParticleEmitter;
 import com.jme3.effect.ParticleMesh;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Ray;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.Camera;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial.CullHint;
 import com.jme3.scene.shape.Box;
 import com.jme3.scene.shape.Sphere;
 import com.jme3.texture.Texture;
 
 public class WindFarmPlacementChecker {
     
     //--------------------------------------------------------------------------
     ConfigReader confReader;
     private Camera gameCamera;
     private AssetManager mainAssetManager;//from mainHandle
     private Node mainRootNode;//from mainHandle
         //Holds all shapes, put into rootNode.
         public Node placementShapesMasterNode;
         //Holds all "indicators" (like smoke) for
         //all shapes, put into rootNode.
         public Node placementIndicatorMasterNode;
             //indevidual nodes for each "indicator",
             //smoke in this case.
             public Node _EffectEmitterIndividualNodes[];
     
     //current level everything is loaded for.
     private short currentLevel;
         //all relavent data for currentLevel.
         private DataPlacementLevelShapes levelDataObj;
         private DebugGlobals debugGlobalsObj;
     
     //--------------------------------------------------------------------------
     //--------------------------------------------------------------------------
     //--------------------------------------------------------------------------
     /**
      * 
      * @param mainHandle handle to main class.
      */
     public WindFarmPlacementChecker(DataWindFarmPlacementChecker dataObj){
         //this.mainHandle = mainHandle;
         mainAssetManager = dataObj.assetMan;
         mainRootNode = dataObj.rootNode;
         debugGlobalsObj = dataObj.debugGlobals;
         gameCamera = dataObj.gameCam;
         confReader = dataObj.configReader;
     }
     
     //--------------------------------------------------------------------------
     /**
      * 
      * @param level The current level to load shapes for.
      * @return 0 = everything is okie dokie, 1 = shit just got real...
      */
     public byte loadShapes(short level){
         //INIT SOME THINGS------------
         placementShapesMasterNode = new Node();
         placementIndicatorMasterNode = new Node();
         
         //Make these invisible shapes by default.
         placementShapesMasterNode.setCullHint(CullHint.Always);
         //Unless we are debugging...
         if(debugGlobalsObj.DEBUG_PLACEMENT_SHAPES_VISIBLE)
             placementShapesMasterNode.setCullHint(CullHint.Never);
         
         //add our node to the main node
         mainRootNode.attachChild(placementShapesMasterNode);
         
         currentLevel = level;
         
         levelDataObj = confReader.getPlacementLevelData(currentLevel);
         Geometry geom;
         
         //-----------------------------
         
         //ADD ALL THE SHAPES IN THIS LEVEL
         for(short i = 0; i < levelDataObj._shapesInThisLevel.length; ++i){
             
             //adding a rectangle
             if(levelDataObj._shapesInThisLevel[i].shapeType == 0){
                 Box mesh = new Box(new Vector3f(levelDataObj._shapesInThisLevel[i].centerX, levelDataObj._shapesInThisLevel[i].height, levelDataObj._shapesInThisLevel[i].centerZ), //LOCATION
                                     levelDataObj._shapesInThisLevel[i].lengthX,   1.0f,   levelDataObj._shapesInThisLevel[i].lengthZ);//size X, Y, Z
                 geom = new Geometry( (levelDataObj._reasons[(levelDataObj._shapesInThisLevel[i].reasonIndex)]), mesh);
                 //geom.setUserData("reason", levelDataObj._reasons[(levelDataObj._shapesInThisLevel[i].reasonIndex)] );
             }//if
             
             //Adding a Circle
             else if(levelDataObj._shapesInThisLevel[i].shapeType == 1){
                 Sphere mesh = new Sphere(16, 16, levelDataObj._shapesInThisLevel[i].diameter, false, false);
                 geom = new Geometry( (levelDataObj._reasons[(levelDataObj._shapesInThisLevel[i].reasonIndex)]), mesh);
                 geom.setLocalTranslation(levelDataObj._shapesInThisLevel[i].centerX, levelDataObj._shapesInThisLevel[i].height, levelDataObj._shapesInThisLevel[i].centerZ);
                 //geom.setUserData("reason", levelDataObj._reasons[(levelDataObj._shapesInThisLevel[i].reasonIndex)] );
             }//eif
             
             
             //More shapes would go here ...
             
             
             //in case stupid happens,
             //and no recognized shape index is present.
             else{
                 return 1;
             }//else
             
             
             //debug, make shape visible
             if(debugGlobalsObj.DEBUG_PLACEMENT_SHAPES_VISIBLE){
                 //default material to make the shape visible
                 Material mat_stl = new Material(mainAssetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                Texture tex_ml = mainAssetManager.loadTexture("Textures/Cockpit.png");
                 mat_stl.setTexture("ColorMap", tex_ml);
                 geom.setMaterial(mat_stl);
             }//if
             
             //attaching created geometry on node
             placementShapesMasterNode.attachChild(geom);
         }//for
         
         return 0;
     }//method
     
     //--------------------------------------------------------------------------
     /**
      * This will make smoke on all of the placement objects,
      * to make it visible to the user.  Re-initializes everything
      * when called so can be used for different levels, just recall.
      * CALL THIS AFTER loadShapes() !!!!!
      */
     public void loadSmokeOnAllObjects(){
         if(debugGlobalsObj.DEBUG_PLACEMENT_SHAPES_SMOKE_VISIBLE){
             _EffectEmitterIndividualNodes = new Node[levelDataObj._shapesInThisLevel.length];
             for(int i = 0; i < levelDataObj._shapesInThisLevel.length; ++i){
                 _EffectEmitterIndividualNodes[i] = new Node();
 
                 ParticleEmitter indicator = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
                 Material material = new Material(mainAssetManager, "Common/MatDefs/Misc/Particle.j3md");
                 material.setTexture("Texture", mainAssetManager.loadTexture("Effects/smoke3.png"));
                 indicator.setMaterial(material);
                 indicator.setImagesX(1); 
                 indicator.setImagesY(1); // 2x2 texture animation
                 indicator.setStartColor(new ColorRGBA(0f, 0f, 1f, 1f));
                 indicator.setEndColor(  new ColorRGBA(.5f, .5f, .7f, .5f));
 
                 indicator.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 200, 0));
                 indicator.setStartSize(30f);
                 indicator.setEndSize(70f);
                 indicator.setGravity(0, 0, 0);
                 indicator.setLowLife(1f);
                 indicator.setHighLife(3f);
                 indicator.getParticleInfluencer().setVelocityVariation(0.3f);
 
                 _EffectEmitterIndividualNodes[i].attachChild(indicator);
                 _EffectEmitterIndividualNodes[i].setLocalTranslation(levelDataObj._shapesInThisLevel[i].centerX, levelDataObj._shapesInThisLevel[i].indicatorHeight, levelDataObj._shapesInThisLevel[i].centerZ);
                 placementIndicatorMasterNode.attachChild(_EffectEmitterIndividualNodes[i]);
 
             }//for
             placementIndicatorMasterNode.setCullHint(CullHint.Never);
             this.mainRootNode.attachChild(placementIndicatorMasterNode);
         }//if
     }//method
     
     //--------------------------------------------------------------------------
     //--------------------------------------------------------------------------
     //--------------------------------------------------------------------------
     /**
      * This method shoots a ray down from the camera and returns all the
      * reasons for no-placement of objects hit by the ray cast.
      * @return All the reason for no-placement of the windfarm at this current location.
      */
     public CollisionResults getPlacementResult(){
         CollisionResults results = new CollisionResults();
         // 2. Aim the ray from cam loc to cam direction.
         Ray ray = new Ray(gameCamera.getLocation(), new Vector3f(0,-1,0));
         // 3. Collect intersections between Ray and Shootables in results list.
         placementShapesMasterNode.collideWith(ray, results);
         return results;
         //return null;
     }
     
     //OLD
     /*
         public String[] getPlacementResult(){
         CollisionResults results = new CollisionResults();
         // 2. Aim the ray from cam loc to cam direction.
         Ray ray = new Ray(mainHandle.getCamera().getLocation(), new Vector3f(0,-1,0));
         // 3. Collect intersections between Ray and Shootables in results list.
         placementShapesMasterNode.collideWith(ray, results);
         // 4. Print the results
         //System.out.println("----- Collisions? " + results.size() + "-----");
         String[] returnMe = new String[results.size()];
         for(short i = 0; i < results.size(); ++i){
             returnMe[i] = results.getCollision(i).getGeometry().getName();
         }//for
         return returnMe;
         //return null;
     }
      */
     
     
     
 }//class
