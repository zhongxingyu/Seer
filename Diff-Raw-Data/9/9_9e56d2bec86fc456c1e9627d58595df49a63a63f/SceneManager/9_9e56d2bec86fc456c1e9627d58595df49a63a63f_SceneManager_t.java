 /*
  * Nebula2D is a cross-platform, 2D game engine for PC, Mac, & Linux
  * Copyright (c) 2014 Jon Bonazza
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.nebula2d.scene;
 
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.XmlReader;
 import com.nebula2d.assets.AssetManager;
 import com.nebula2d.components.*;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * SceneManager is a singleton class used to manage the game's various {@link Scene}s.
  *
  * @author Jon Bonazza <jonbonazza@gmail.com>
  */
 public class SceneManager {
 
     private static Scene currentScene;
     private static Map<String, Scene> sceneMap = new HashMap<String, Scene>();
 
     /**
      * Retrieves the singleton instance of SceneManager. SceneManager should
      * only ever have once instance at any given time.
      * @return Singleton SceneManager instance.
      */
 
     public static Scene getCurrentScene() {
         return currentScene;
     }
 
 
     /**
      * Retrieves the {@link Scene} with the given name.
      * @param name The name of the Scene to retrieve.
      * @return The target Scene.
      */
     public static Scene getScene(String name) {
         return sceneMap.get(name);
     }
 
     /**
      * Adds a Scene to the scene manager.
      * @param scene The Scene to add.
      */
     public static void addScene(Scene scene) {
         sceneMap.put(scene.getName(), scene);
     }
 
     /**
      * Changes the current scene, unloading the currently loaded
      * assets and loading in the assets for the new scene.
      * @param name The name of the Scene to load.
      */
     public static void setCurrentScene(String name) {
 
         if (sceneMap.containsKey(name)) {
             if (currentScene != null) {
                 currentScene.finish();
                 AssetManager.unloadAssets(currentScene.getName());
             }
             currentScene = getScene(name);
             AssetManager.loadAssets(name);
             currentScene.start();
         }
     }
 
     public static void start() {
         currentScene.start();
     }
 
     public static void update(float dt) {

        if (currentScene != null)
            currentScene.update(dt);
     }
 
     public static void fixedUpdate() {

        if (currentScene == null)
            return;

         World physicalWorld = currentScene.getPhysicalWorld();
         physicalWorld.step(1/45f, 6, 2);
 
         Array<Body> bodies = new Array<Body>();
         physicalWorld.getBodies(bodies);
 
         for (Body body : bodies) {
             if (body.getType() != BodyDef.BodyType.StaticBody) {
                 GameObject go = (GameObject) body.getUserData();
                 go.setPosition(body.getPosition().x, body.getPosition().y);
                 go.setRotation((float) (body.getAngle() * 180.0f / Math.PI));
             }
         }
     }
 
     public static void loadSceneData(FileHandle scenesFile) throws IOException {
         XmlReader reader = new XmlReader();
         XmlReader.Element root = reader.parse(scenesFile);
         Array<XmlReader.Element> sceneElements = root.getChildrenByName("scene");
         String startScene = root.getAttribute("startScene");
         for (XmlReader.Element sceneElement : sceneElements) {
             String sceneName = sceneElement.getAttribute("name");
             XmlReader.Element gravityElement = sceneElement.getChildByName("gravity");
             float gX = gravityElement != null ? gravityElement.getFloatAttribute("x", 0.0f) : 0.0f;
             float gY = gravityElement != null ? gravityElement.getFloatAttribute("y", 0.0f) : 0.0f;
             boolean sleepPhysics = sceneElement.getBooleanAttribute("sleepPhysics", false);
             Scene scene = new Scene(sceneName, new Vector2(gX, gY), sleepPhysics);
 
             Array<XmlReader.Element> layerElements = sceneElement.getChildrenByName("layer");
             for (int i = 0; i < layerElements.size; ++i) {
                 XmlReader.Element layerElement = layerElements.get(i);
                 String layerName = layerElement.getAttribute("name");
                 Layer layer = new Layer(layerName, i);
                 Array<XmlReader.Element> gameObjectElements = layerElement.getChildrenByName("gameObject");
                 for (XmlReader.Element gameObjectElement : gameObjectElements) {
                     String gameObjectName = gameObjectElement.getAttribute("name");
                     float rotation = gameObjectElement.getFloatAttribute("rot");
                     XmlReader.Element position = gameObjectElement.getChildByName("position");
                     float posX = position.getFloatAttribute("x");
                     float posY = position.getFloatAttribute("y");
                     XmlReader.Element scale = gameObjectElement.getChildByName("scale");
                     float scaleX = scale.getFloatAttribute("x");
                     float scaleY = scale.getFloatAttribute("y");
 
                     GameObject gameObject = new GameObject(gameObjectName);
                     gameObject.setPosition(posX, posY);
                     gameObject.setScale(scaleX, scaleY);
                     gameObject.setRotation(rotation);
 
                     Array<XmlReader.Element> componentElements = gameObjectElement.getChildrenByName("component");
                     for (XmlReader.Element componentElement : componentElements) {
                         String componentName = componentElement.getAttribute("name");
                         String componentType = componentElement.getAttribute("componentType");
                         boolean enabled = componentElement.getBooleanAttribute("enabled");
                         Component component = null;
 
                         if (componentType.equalsIgnoreCase("RENDER")) {
                             String rendererType = componentElement.getAttribute("rendererType");
                             if (rendererType.equalsIgnoreCase("SPRITE_RENDERER")) {
                                 String spritePath = componentElement.getAttribute("sprite");
                                 component = new SpriteRenderer(componentName, spritePath);
                             } else if (rendererType.equalsIgnoreCase("TILE_MAP_RENDERER")) {
                                 String tileSheetPath = componentElement.getAttribute("tileSheet");
                                 component = new TiledMapRenderer(componentName, tileSheetPath);
                             }
                         } else if (componentType.equalsIgnoreCase("MUSIC")) {
                             String trackPath = componentElement.getAttribute("track");
                             component = new MusicSource(componentName, trackPath);
                         } else if (componentType.equalsIgnoreCase("SFX")) {
                             String sfxPath = componentElement.getAttribute("sfx");
                             component = new SoundEffectSource(componentName, sfxPath);
                         } else if (componentType.equalsIgnoreCase("BEHAVE")) {
                             String scriptPath = componentElement.getAttribute("script");
                             component = new Behavior(componentName, scriptPath);
                         } else if (componentType.equalsIgnoreCase("RIGID_BODY")) {
                             boolean isKinematic = componentElement.getBooleanAttribute("isKinematic");
                             boolean fixedRotation = componentElement.getBooleanAttribute("fixedRotation");
                             boolean isBullet = componentElement.getBooleanAttribute("isBullet");
                             float mass = componentElement.getFloatAttribute("mass");
                             XmlReader.Element shapeElement = componentElement.getChildByName("collisionShape");
                             String shapeType = shapeElement.getAttribute("shapeType");
 
                             XmlReader.Element materialElement = shapeElement.getChild(0);
                             float density = materialElement.getFloatAttribute("density");
                             float friction = materialElement.getFloatAttribute("friction");
                             float restitution = materialElement.getFloatAttribute("restitution");
 
                             PhysicsMaterial material = new PhysicsMaterial(density, friction, restitution);
 
                             CollisionShape shape = null;
                             if (shapeType.equalsIgnoreCase("BOX")) {
                                 float w = shapeElement.getFloatAttribute("w");
                                 float h = shapeElement.getFloatAttribute("h");
                                 shape = new BoundingBox(material, w, h);
                             } else if (shapeType.equalsIgnoreCase("CIRCLE")) {
                                 float r = shapeElement.getFloatAttribute("r");
                                 shape = new Circle(material, r);
                             }
 
                             if (shape != null) {
                                 component = new RigidBody(componentName, shape, isKinematic, mass,
                                         fixedRotation, isBullet);
                             }
                         } else if (componentType.equalsIgnoreCase("COLLIDER")) {
                             boolean isSensor = componentElement.getBooleanAttribute("isSensor");
                             XmlReader.Element shapeElement = componentElement.getChildByName("collisionShape");
                             String shapeType = shapeElement.getAttribute("shapeType");
 
                             XmlReader.Element materialElement = shapeElement.getChild(0);
                             float density = materialElement.getFloatAttribute("density");
                             float friction = materialElement.getFloatAttribute("friction");
                             float restitution = materialElement.getFloatAttribute("restitution");
 
                             PhysicsMaterial material = new PhysicsMaterial(density, friction, restitution);
 
                             CollisionShape shape = null;
                             if (shapeType.equalsIgnoreCase("BOX")) {
                                 float w = shapeElement.getFloatAttribute("w");
                                 float h = shapeElement.getFloatAttribute("h");
                                 shape = new BoundingBox(material, w, h);
                             } else if (shapeType.equalsIgnoreCase("CIRCLE")) {
                                 float r = shapeElement.getFloatAttribute("r");
                                 shape = new Circle(material, r);
                             }
 
                             if (shape != null) {
                                 component = new Collider(componentName, shape, isSensor);
                             }
                         }
 
                         if (component != null) {
                             component.setEnabled(enabled);
                             gameObject.addComponent(component);
                         }
                     }
 
                     layer.addGameObject(gameObject);
                 }
 
                 scene.addLayer(layer);
             }
 
             addScene(scene);
         }
 
         setCurrentScene(startScene);
     }
 
     public static void cleanup() {
         for (Scene scene : sceneMap.values()) {
             scene.cleanup();
         }
     }
 }
