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
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.nebula2d.components.Component;
 import com.nebula2d.components.Renderer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * GameObject is the core of Nebula2D's component-based entity system.
  * It has a transform and a list components which describe the entity.
  * @author      Jon Bonazza <jonbonazza@gmail.com>
  */
 public class GameObject extends Group {
     protected Transform transform;
     protected List<Component> components;
     protected Layer layer;
     protected Renderer renderer;
 
     //region constructors
     public GameObject(String name) {
         setName(name);
         this.components = new ArrayList<Component>();
     }
     //endregion
 
     //region accessors
     public Transform getTransform() {
        return transform;
     }
 
     public List<Component> getComponents() {
         return components;
     }
 
     public Layer getLayer() {
         return layer;
     }
 
     public Renderer getRenderer() {
         return renderer;
     }
 
     public void setLayer(Layer layer) {
         this.layer = layer;
         for (Actor actor : getChildren())
             ((GameObject) actor).setLayer(layer);
     }
     //endregion
 
     //region component ops
 
     /**
      * Retrieves the {@link Component} from the GameObject with the given name
      * @param name the name of the Component to retrieve
      * @return the target Component
      */
     public Component getComponent(String name) {
         for (Component c : components) {
             if (c.getName().equals(name))
                 return c;
         }
 
         return null;
     }
 
     /**
      * Adds a Component to the GameObject
      * @param component the Component to add
      */
     public void addComponent(Component component) {
         components.add(component);
         component.setGameObject(this);
         if (component instanceof Renderer)
             renderer = (Renderer) component;
     }
 
     /**
      * Removes a Component from the GameObject
      * @param component the Component to remove
      */
     public void removeComponent(Component component) {
         components.remove(component);
         component.setGameObject(null);
         if (component == renderer)
             renderer = null;
     }
 
     @Override
    public void draw(SpriteBatch batch, float parentAlpha) {
         if (renderer != null)
            renderer.render(batch, Gdx.graphics.getDeltaTime());
 
         drawChildren(batch, parentAlpha);
     }
 
     public void start(Stage stage) {
         this.transform = new Transform(this);
         stage.addActor(this);
         for (Component c : components) {
             c.start();
         }
     }
 
     public void update(float dt) {
         for (Component c : components) {
             if (c.isEnabled())
                 c.update(dt);
         }
     }
     //endregion
 }
