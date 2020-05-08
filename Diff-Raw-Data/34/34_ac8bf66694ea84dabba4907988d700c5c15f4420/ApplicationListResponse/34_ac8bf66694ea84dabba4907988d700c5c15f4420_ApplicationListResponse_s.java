 package edu.teco.dnd.module.messages.infoReq;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.UUID;
 
 import edu.teco.dnd.network.messages.Response;
 
 /**
  * Contains the UUIDs and names of all running Applications.
  * 
  * @author Philipp Adolf
  */
 public class ApplicationListResponse extends Response {
 
 	public static String MESSAGE_TYPE = "application list";
 
 	/**
 	 * The UUIDs of all running applications mapped to their names.
 	 */
 	private final Map<UUID, String> applicationNames;
 	/**
 	 * The UUIDs of all running applications mapped to the UUID of the blocks.
 	 */
 	private final Map<UUID, Collection<UUID>> applicationBlocks;
 
 	/**
 	 * The UUID of the module this Response was generated on.
 	 */
 	private final UUID moduleUUID;
 
 	/**
 	 * Creates a new ApplicationListResponse.
 	 * 
 	 * @param applicationNames
 	 *            the UUIDs of the running Applications mapped to their names
 	 */
 	public ApplicationListResponse(final UUID moduleUUID, final Map<UUID, String> applicationNames,
 			Map<UUID, Collection<UUID>> applicationBlocks) {
 		this.moduleUUID = moduleUUID;
 		this.applicationNames = Collections.unmodifiableMap(new HashMap<UUID, String>(applicationNames));
 		Map<UUID, Collection<UUID>> blocks = new HashMap<UUID, Collection<UUID>>();
 		for (final Entry<UUID, Collection<UUID>> entry : applicationBlocks.entrySet()) {
 			blocks.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<UUID>(entry.getValue())));
 		}
 		this.applicationBlocks = Collections.unmodifiableMap(blocks);
 	}
 
 	/**
 	 * Constructor used by gson.
 	 */
	// FIXME: add adapter that keeps the maps unmodifiable
 	@SuppressWarnings("unused")
 	private ApplicationListResponse() {
 		this.moduleUUID = null;
 		this.applicationNames = null;
 		this.applicationBlocks = null;
 	}
 
 	/**
 	 * Returns the UUID of the module this Response was generated on.
 	 * 
 	 * @return the UUID of the module this Response was generated on
 	 */
 	public UUID getModuleUUID() {
 		return this.moduleUUID;
 	}
 
 	/**
 	 * Returns the UUIDs of all running Applications mapped to their name
 	 * 
 	 * @return the UUIDs of all running Applications mapped to their name
 	 */
 	public Map<UUID, String> getApplicationNames() {
 		return this.applicationNames;
 	}
 
 	/**
 	 * Returns the UUIDs of all running Applications mapped to their running Map<BlockUUID,FunctionBlock>
 	 * 
 	 * @return the UUIDs of all running Applications mapped to their running Map<BlockUUID,FunctionBlock>
 	 */
 	public Map<UUID, Collection<UUID>> getApplicationBlocks() {
 		return this.applicationBlocks;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + ((applicationNames == null) ? 0 : applicationNames.hashCode());
 		result = prime * result + ((moduleUUID == null) ? 0 : moduleUUID.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (!super.equals(obj)) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		ApplicationListResponse other = (ApplicationListResponse) obj;
 		if (applicationNames == null) {
 			if (other.applicationNames != null) {
 				return false;
 			}
 		} else if (!applicationNames.equals(other.applicationNames)) {
 			return false;
 		}
 		if (moduleUUID == null) {
 			if (other.moduleUUID != null) {
 				return false;
 			}
 		} else if (!moduleUUID.equals(other.moduleUUID)) {
 			return false;
 		}
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "ApplicationListResponse [applications=" + applicationNames + ", moduleUUID=" + moduleUUID
 				+ ", getSourceUUID()=" + getSourceUUID() + "]";
 	}
 }
