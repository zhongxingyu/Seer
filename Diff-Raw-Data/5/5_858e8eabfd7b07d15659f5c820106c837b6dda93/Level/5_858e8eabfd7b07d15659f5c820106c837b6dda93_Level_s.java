 package mygame;
 
 import chair.input.*;
 import com.jme3.asset.AssetManager;
 import com.jme3.input.InputManager;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 import com.jme3.texture.Texture;
 import java.util.LinkedList;
<<<<<<< HEAD
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.shape.Box;
 
 
=======
 import java.util.Random;
>>>>>>> 5cb45909a9e5b3e5cfc90eb10e68bf6c4f048204
 
 /**
  *
  * @author Charlie
  */
 public class Level {
     
     Node rootNode;
     AssetManager assetManager;
     InputManager inputManager;
     LinkedList<MoveableGameObject> moveableObjects;
     LinkedList<StaticGameObject> staticObjects;
  
     //Remove these from PhysicsSpace at the beginning of an update.
     LinkedList<GameObject> killUs;
     
     public Level(Node root, AssetManager assets, InputManager input){
         //We REALLY should implement a singleton...
         rootNode = root;
         assetManager = assets;
         inputManager = input;
         killUs = new LinkedList<GameObject>();
         moveableObjects = new LinkedList<MoveableGameObject>();
         staticObjects = new LinkedList<StaticGameObject>();
         
         InputListener controllerListener = new XboxInputListener(input);
         for(InputController controller : controllerListener.getInputControllers())
         {
             Random rand = new Random();
             rand.setSeed(System.currentTimeMillis());
             String modelID = OfficeChair.models[rand.nextInt(OfficeChair.models.length)];
             Spatial chairSpatial = assetManager.loadModel(modelID);
             rootNode.attachChild(chairSpatial);
             OfficeChair chair = new OfficeChair(this, new Vector2f(-5f, 0.0f), 0.0f, controller, chairSpatial);
             this.moveableObjects.add(chair);
         }
         
         Vector3f min = new Vector3f(-21.0f, 0.0f, -21.0f);
         Vector3f max = new Vector3f(-20.0f, 6.0f, 21.0f);
         Box b = new Box(min, max);
         Geometry g = new Geometry("Box Left", b);
         Material mat = new Material(assetManager, 
                 "Common/MatDefs/Light/Lighting.j3md");
         mat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/cubicle.jpg"));
         g.setMaterial(mat);
         Wall wall = new Wall(g, -21.0f, -21.0f, 1.0f, 42.0f);
         this.staticObjects.add(wall);
         rootNode.attachChild(g);
         min = new Vector3f(-20.0f, 0.0f, 20.0f);
         max = new Vector3f(20.0f, 6.0f, 21.0f);
         b = new Box(min, max);
         g = (new Geometry("Box Top", b));
         g.setMaterial(mat);
         wall = new Wall(g, -20.0f, 20.0f, 40.0f, 1.0f);
         this.staticObjects.add(wall);
         rootNode.attachChild(g);
         min = new Vector3f(20.0f, 0.0f, -21.0f);
         max = new Vector3f(21.0f, 6.0f, 21.0f);
         b = new Box(min, max);
         g = (new Geometry("Box Right", b));
         g.setMaterial(mat);
         wall = new Wall(g, 20.0f, -21.0f, 1.0f, 42.0f);
         this.staticObjects.add(wall);
         rootNode.attachChild(g);
         min = new Vector3f(-20.0f, 0.0f, -21.0f);
         max = new Vector3f(20.0f, 6.0f, -20.0f);
         b = new Box(min, max);
         g = (new Geometry("Box Bottom", b));
         g.setMaterial(mat);
         wall = new Wall(g, -20.0f, -21.0f, 40.0f, 1.0f);
         this.staticObjects.add(wall);
         rootNode.attachChild(g);
         min = new Vector3f(-21.0f, -1.0f, -21.0f);
         max = new Vector3f(21.0f, 0.0f, 21.0f);
         b = new Box(min, max);
         g = (new Geometry("Box Bottom", b));
         mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/buldwarehouseroof.jpg"));
         g.setMaterial(mat);
         wall = new Wall(g, -21.0f, -21.0f, 0.0f, 0.0f);
         this.staticObjects.add(wall);
         rootNode.attachChild(g);
         
     }
     
     
     /**
      *
      * @param shot
      */
     public void spawnProjectile(Projectile shot){
         //Should have a pool for these. Fuck it, we'll deal with that if we have issues
         Spatial shotSpatial = assetManager.loadModel("Models/marker/marker.j3o");
         shot.setSpatial(shotSpatial);
         moveableObjects.add(shot);
         rootNode.attachChild(shotSpatial);
     }
     
     /**
      *
      * @param requestor
      */
     public void removeSelf(GameObject requestor){
         killUs.add(requestor);
     }
     
     /**
      * Damages all GameActors in bounds except for immune.
      * 
      * @param immune
      * @param bounds
      * @param damage 
      */
     public void damageAllInRect(GameActor immune, RectF bounds, int damage){
         for (MoveableGameObject g : moveableObjects){
             if (g != immune && g.boundingCircle.collidesWithRect(bounds, false)){
                 if (g.type == GameObjectType.ACTOR){
                     ((GameActor) g).takeDamage(damage);
                 }
             }
         }
     }
     
     
     /**
      *
      * @param tpf
      */
     public void update(float tpf){
         for(GameObject g : killUs){
             switch (g.type){
                 case ACTOR: 
                     rootNode.detachChild(g.objectModel);
                     moveableObjects.remove((MoveableGameObject) g);
                     break;
                 case PROJECTILE:
                     moveableObjects.remove((MoveableGameObject) g);
                     break;
                 case OBSTACLE:
                     break;
                 default:
             }
             killUs.remove(g);
         }
         for(MoveableGameObject g: moveableObjects){
             g.update(tpf);
 
             for (MoveableGameObject g2: moveableObjects){
                 if (g != g2){
                     g.boundingCircle.collidesWithCircle(g2.boundingCircle);
                 }
             }
             
             for (StaticGameObject g2: staticObjects){
                 System.out.println(g.boundingCircle.collidesWithRect(g2.boundingRect));
             }
 /*
             OfficeChair a = (OfficeChair)allObjects.get(0);
             OfficeChair b = (OfficeChair)allObjects.get(1);
             if (a.getBoundingCircle().collidesWithCircle(b.getBoundingCircle(), true))
                 System.out.println("Fuck yeah");
 */
         }
     }
     
 }
