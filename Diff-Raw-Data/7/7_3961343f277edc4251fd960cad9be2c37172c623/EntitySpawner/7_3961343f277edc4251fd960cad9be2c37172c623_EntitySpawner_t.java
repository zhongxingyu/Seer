 /**
  * 
  */
 package com.punchline.javalib.entities.components.generic;
 
 import java.util.ArrayList;
 
 import com.punchline.javalib.entities.Component;
 import com.punchline.javalib.entities.ComponentManager;
 
 /**
  * An EntitySpawner component for spawning entities.
  * @author William
  * @created Jul 27, 2013
  */
 public class EntitySpawner implements Component {
 	/**
 	 * The delay/rate at which the template is instantiated.
 	 */
 	private float spawnDelay;
 	
 	/**
 	 * The elapsed time during which the system has processed spawnDelay.
 	 */
 	private float elapsed;
 	
 	/**
 	 * Whether or not the template is a group template.
 	 */
 	private boolean group;
 	
 	/**
 	 * The entitiy template to instantiate.
 	 */
 	private String spawnTemplate;
 	
 	/**
 	 * The arguments for the entity template.
 	 */
 	private ArrayList<Object> args;
 	
 	/**
 	 * Constructs the specific entity spawner with a template and delay
 	 * @param spawnTemplate The entity template to spawn.
 	 * @param group Whether or not the template is a group template.
 	 * @param spawnDelay The delay/rate at which the template is instantiated.
 	 * @param args The arguments for the template.
 	 */
 	public EntitySpawner(String spawnTemplate, boolean group, float spawnDelay, Object... args) {
		this.args = new ArrayList<Object>();
 		for(Object arg : args)
 			this.args.add(arg);
 		this.group = group;
 		this.spawnTemplate = spawnTemplate;
 		this.spawnDelay = spawnDelay;
 		this.elapsed = 0f;
 	}
 
 	/**
 	 * Checks if the EntitySpawner allows a spawn.
 	 * @param delta The world-delta time in seconds.
 	 * @return Whether or not the template should be spawned.
 	 */
 	public boolean spawn(float delta){
 		elapsed += delta;
 		if(elapsed > spawnDelay)
 		{
 			elapsed = 0f;
 			return true;
 		}
 		return false;
 	}
 	
 	
 	
 	
 	//GETTERS/SETTERS
 	/**
 	 * Gets the args for the spawnTemplate
 	 * @return The args for the spawnTemplate.
 	 */
 	public ArrayList<Object> getArgs(){
 		return args;
 	}
 	
 	/**
 	 * Gets the spawnTemplate string.
 	 * @return The spawnTemplate string.
 	 */
 	public String getSpawnTemplate(){
 		return spawnTemplate;
 	}
 	
 	/**
 	 * Sets a new spawnTemplate.
 	 * @param spawnTemplate The spawnTemplate string.
 	 * @param group Whether or not the template is a group template.
 	 * @param args The arguments for the template.
 	 */
 	public void setSpawnTemplate(String spawnTemplate, boolean group, Object... args){
 		this.spawnTemplate = spawnTemplate;
 		this.group = group;
 		this.args.clear();
 		for(Object arg : args)
 			this.args.add(arg);
 	}
 	
 	/**
 	 * Gets whether or not the spawnTemplate is a group template.
 	 * @return Whether or not the spawnTemplate is a group template.
 	 */
 	public boolean isGroupTemplate(){
 		return group;
 	}
 	
 	/**
 	 * Gets the spawn delay.
 	 * @return The spawnDelay.
 	 */
 	public float getSpawnDelay(){
 		return spawnDelay;
 	}
 	
 	/**
 	 * Sets the spawn delay.
 	 * @param delay The new spawnDelay.
 	 */
 	public void setSpawnDelay(float delay){
 		spawnDelay = delay;
 	}
 	
 	/** {@inheritDoc}
 	 * @see com.punchline.javalib.entities.Component#onAdd(com.punchline.javalib.entities.ComponentManager)
 	 */
 	@Override
 	public void onAdd(ComponentManager container) {
 	}
 
 	/** {@inheritDoc}
 	 * @see com.punchline.javalib.entities.Component#onRemove(com.punchline.javalib.entities.ComponentManager)
 	 */
 	@Override
 	public void onRemove(ComponentManager container) {
 	}
 
 }
