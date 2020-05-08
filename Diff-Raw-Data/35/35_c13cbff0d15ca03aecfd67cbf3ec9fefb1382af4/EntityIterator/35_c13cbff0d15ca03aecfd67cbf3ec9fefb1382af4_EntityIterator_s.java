 package engine.entity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 public class EntityIterator implements Iterator<Entity> {
 	private int index;
 	private HashMap<Object, Entity> entity_list;
 	private ArrayList<Object> key_set;
 	
 	public EntityIterator() {
 		index = 0;
 		entity_list = new HashMap<Object, Entity>();
 	}
 	
 	public EntityIterator(HashMap<Object, Entity> ent_list){
 		index = 0;
 		entity_list = ent_list;
 		key_set = new ArrayList<Object>();			
 		key_set.addAll(entity_list.keySet());
 	}
 	
 	@Override
 	public boolean hasNext() {
		return index < key_set.size()-1;
 	}
 
 	@Override
 	public Entity next() {
		index++;
 		Object ret_key = key_set.get(index);
 		return entity_list.get(ret_key);
 	}
 
 	@Override
 	public void remove() {
 		Object del_key = key_set.get(index - 1);
		entity_list.get(del_key);
 		index--;
 	}
 
 }
