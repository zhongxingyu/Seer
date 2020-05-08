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
		System.out.println("hasNext");
		return index < key_set.size();
 	}
 
 	@Override
 	public Entity next() {
		System.out.println("next");
 		Object ret_key = key_set.get(index);
		
		System.out.println(ret_key.toString());
		
		index++;
 		return entity_list.get(ret_key);
 	}
 
 	@Override
 	public void remove() {
		/*
		System.out.println("remove");
 		Object del_key = key_set.get(index - 1);
		entity_list.remove(del_key);
		key_set.remove(index - 1);
 		index--;
		*/
 	}
 
 }
