 package fhbrs.ooka.ateam.components;
 
 import java.util.ArrayList;
 
 /**
  * This class Manages the components. Other classes can use it to find running
  * components, find components by name and by class name. It keeps track of all
  * Components in an ArrayList.
  *
  *
  * @author David Goering
  */
 public class ComponentManager {
     private ArrayList<IComponent> components = new ArrayList<IComponent>();
 
     public ComponentManager() {
     }
 
     /**
      * The method findByClassName allows finding a component by its class Name.
      * If no match is found, null is returned.
      *
      * @author David Goering
      */
     public IComponent findByClassName(String ClassName) {
         for (int i = 0; i < components.size(); i++) {
             if (components.get(i).getClass().getName() == ClassName)
                 return components.get(i);
         }
         return null;
     }
 
     /**
      * The method findByName allows finding a component by its name field. The
      * distinction is allowed to allow different names to the class name for
      * displaying its name, If no match is found, null is returned.
      *
      * @author David Goering
      */
     public IComponent findByName(String name) {
         for (int i = 0; i < components.size(); i++) {
             if (components.get(i).getName() == name)
                 return components.get(i);
         }
         return null;
     }
 
     /**
      * The method getRunningComponents searches through all registered
      * Components and returns an ArrayList of all running Components. See
      * ComponentState for Details.
      *
      * @author David Goering
      */
     public ArrayList<IComponent> getRunningComponents() {
         ArrayList<IComponent> running = new ArrayList<IComponent>();
         for (int i = 0; i < components.size(); i++) {
             if (components.get(i).getState() == ComponentState.Running) {
                 running.add(components.get(i));
             }
         }
         return running;
     }
 
     /**
      * The add Method allows filling the ArrayList with registered Components.
      * Should be called by the Main class when a new Component is loaded at
      * runtime.
      *
      * @author David Goering
      */
     public void add(IComponent component) {
         components.add(component);
     }
 
     /**
      * The remove Method allows removing a component from the ArrayList. It
      * should be called by the Main class when an existing Component is deleted.
      *
      * @author David Goering
      */
     public void remove(IComponent component) {
         components.remove(component);
     }
 
     /**
     * The getAllComponents method returns all registered Components
      *
      * @author David Goering
      */
    public ArrayList<IComponent> getAllComponents() {
 	return components;
     }
 
 } 
