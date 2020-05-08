 /**
  * 
  */
 package pbs;
 
 /**
  * @author Skylar Hiebert
  *
  */
 import java.util.*;
 import jig.engine.*;
 import jig.engine.physics.BodyLayer;
 import jig.engine.util.Vector2D;
 import quadtree.*;
 
 public class PBSQuadLayer<T extends Entity> implements Iterable<T>, BodyLayer<T>, ViewableLayer {
	private static final int MAX_ENTITIES = 10;
     
     //goal will be to change this to a quad tree
     protected List<T> entities;
     protected QuadTree<T> tree;
 
     public PBSQuadLayer(Vector2D min, Vector2D max){
     	entities = new ArrayList<T>();
     	tree = new QuadTree<T>(MAX_ENTITIES, min, max);
     }
 
     public boolean isActive(){
     	return true;
     }
 
     public void setActivation(boolean a){
     }
 
     public void render(RenderingContext rc){
 		for(T e : entities){
 		    e.render(rc);
 		}
     }
     
     public void updateTreeBounds(Vector2D min, Vector2D max) {
     	tree.min = min;
     	tree.max = max;
     }
 
     public void update(long deltaMs){
 		T e;
 		tree = new QuadTree<T>(tree.maxEntitiesPerNode, tree.min, tree.max);
 		
 		for( int i = size()-1; i >= 0; i--){
 		    e = entities.get(i);
 		    if(e.alive() == false){
 		    	entities.remove(i);
 		    } else {
 		    	e.update(deltaMs);
		    	tree.add(e);
 		    }
 		}
     }
 
     public Iterator<T> iterator(){
     	return entities.iterator();
     }
 
     public void add(T e){
     	entities.add(e);
     }
     
     public int size(){
     	return entities.size();
     }
 
     public void clear(){
     	entities.clear();
     }
     
     @Override
     public T get(int i) {
 	return entities.get(i);
     }
     
     public List<T> get(Vector2D min, Vector2D max) {
 	return tree.getEntities(min, max);
     }
     
 }
