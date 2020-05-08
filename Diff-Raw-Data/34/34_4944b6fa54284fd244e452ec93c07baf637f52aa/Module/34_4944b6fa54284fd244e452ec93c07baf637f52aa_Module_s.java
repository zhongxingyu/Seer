 package edu.teco.dnd.module;
 
 import java.util.HashMap;
 import java.util.UUID;
 
 import edu.teco.dnd.module.config.BlockTypeHolder;
 
 /**
  * Represents a Module
  * 
  * @author jung
  * 
  */
 public class Module {
 	private UUID uuid;
 	private String name;
 	private String location;
 	private BlockTypeHolder holder;
 
	// TODO this needs hashcode() equals() toString()

 	/**
 	 * Creates a new representation of a module.
 	 * 
 	 * @param id
 	 *            UUID of the module.
 	 * @param name
 	 *            Name of the module.
 	 * @param holder
 	 *            BlockTypeHolder of the module.
 	 */
 	public Module(UUID id, String name, BlockTypeHolder holder) {
 		this.uuid = id;
 		this.name = name;
 		this.holder = holder;
 	}
 
 	/**
 	 * Returns the BlockTypeHolder of this module.
 	 * 
 	 * @return BlockTypeHolder of this module.
 	 */
 	public BlockTypeHolder getHolder() {
 		return this.holder;
 	}
 
 	public UUID getUUID() {
 		return uuid;
 	}
 
 	/**
 	 * Returns Map from available Types to amount of available slots.
 	 * 
 	 * @return Map from Types to amount of free slots for this type
 	 */
 	public HashMap<String, Integer> getTypes() {
 		return holder.getTypes();
 	}
 
 	public boolean tryAddFunctionBlock(String functType) {
 		if (holder.getTypes().containsKey(functType)) {
 
 		}
 		return false;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getLocation() {
 		return location;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((holder == null) ? 0 : holder.hashCode());
 		result = prime * result + ((location == null) ? 0 : location.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		Module other = (Module) obj;
 		if (holder == null) {
 			if (other.holder != null) {
 				return false;
 			}
 		} else if (!holder.equals(other.holder)) {
 			return false;
 		}
 		if (location == null) {
 			if (other.location != null) {
 				return false;
 			}
 		} else if (!location.equals(other.location)) {
 			return false;
 		}
 		if (name == null) {
 			if (other.name != null) {
 				return false;
 			}
 		} else if (!name.equals(other.name)) {
 			return false;
 		}
 		if (uuid == null) {
 			if (other.uuid != null) {
 				return false;
 			}
 		} else if (!uuid.equals(other.uuid)) {
 			return false;
 		}
 		return true;
 	}
 }
