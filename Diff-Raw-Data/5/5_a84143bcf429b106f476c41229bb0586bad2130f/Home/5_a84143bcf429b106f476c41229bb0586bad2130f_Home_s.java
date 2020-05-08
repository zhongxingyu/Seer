 package org.robminfor.engine.entities;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 
 public class Home extends AbstractEntity implements IStorage  {
 	
 	private Collection<AbstractFacility> facilities = new ArrayList<AbstractFacility>(); 
 	
 	private Collection<AbstractEntity> content = new ArrayList<AbstractEntity>();
 	
 	public Home() {
 		super();
 	}
 
 	@Override
 	public boolean isSolid() {
 		return true;
 	}
 	
 	public void addFacility(AbstractFacility facility) {
 		if (!facilities.contains(facility)) {
 			facilities.add(facility);
 		}
 	}
 	
 	public Collection<AbstractFacility> getFacilities() {
 		return Collections.unmodifiableCollection(facilities);
 	}
 
 	@Override
 	public void addEntity(AbstractEntity entity) {
		if (!content.contains(entity)) {
			content.add(entity);
		}
 	}
 
 	@Override
 	public void removeEntity(AbstractEntity entity) {
 		if (!content.contains(entity)) {
 			throw new IllegalArgumentException("Entitiy not in storage");
 		}
 		content.remove(entity);
 	}
 
 	@Override
 	public boolean containsEntity(AbstractEntity thing) {
 		return content.contains(thing);
 	}
 
 	@Override
 	public Collection<AbstractEntity> getContent() {
 		return Collections.unmodifiableCollection(content);
 	}
 
 }
