 import java.awt.*;
 import java.util.*;
 
 public class EntityManager {
     
     int newID;//Need clarification as to what this variable is for.
     
     private ArrayList<Entity> ents = new ArrayList<Entity>();
     private ArrayList<Entity> addedEnts = new ArrayList<Entity>();
     private ArrayList<Entity> removedEnts = new ArrayList<Entity>();
     
     EntityManager()
     {
         newID = 0;
     }
     
     public int AddEntity(Entity newEnt)
     {
         newEnt.id = newID;
         newID++;
         
         addedEnts.add(newEnt);
         
         return newEnt.id;
     }
     
     private void UpdateLists()
     {
         ents.addAll(addedEnts);
         addedEnts.clear();
         ents.removeAll(removedEnts);
         removedEnts.clear();
     }
     
     private void RemoveEntity(Entity ent)
     {
         removedEnts.add(ent);
     }
     
     public void update(float delta) //Consider rewriting this segment? Seems weird. Why not use a while?
     {
         for(Iterator<Entity> i = ents.iterator(); i.hasNext(); )
         {
             Entity ent = i.next();
             
             if(ent.IsDying()){
                 RemoveEntity(ent);
             } else {
                 ent.update(delta);
             }
         }
         
         UpdateLists();
     }
     
     public void draw(Graphics g, float interp)
     {
        for(Iterator<Entity> i = ents.iterator(); i.hasNext(); )
         {
            Entity ent = i.next();//This line is throwing exceptions with concurrent modification. Might want to restructure this
             ent.draw(g, interp);
         }
     }
     
     public ArrayList<Entity> GetEntities()
     {
         return new ArrayList<Entity>(ents);
     }
 }
