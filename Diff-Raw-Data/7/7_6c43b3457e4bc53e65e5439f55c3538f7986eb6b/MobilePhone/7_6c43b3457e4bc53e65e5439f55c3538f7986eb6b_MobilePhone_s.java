 package civchat.model;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class MobilePhone 
 {
 	private int id;
 	private int networkId;
 	private String owner;
 	
 	private Set<DirtyMobilePhoneReason> dirty = new HashSet<DirtyMobilePhoneReason>();
 	public enum DirtyMobilePhoneReason
 	{
 		NETWORK,
 		OWNER
 	}
 	
 	public MobilePhone(int networkId, String owner)
 	{
 		this.networkId = networkId;
 		this.owner     = owner;
 	}
 	
 	public MobilePhone(int id, int networkId, String owner)
 	{
 		this.id        = id;
 		this.networkId = networkId;
 		this.owner     = owner;
 	}
 
 	public int getNetworkId() {
 		return networkId;
 	}
 
 	public void setNetworkId(int networkId) {
 		this.networkId = networkId;
 	}
 
 	public String getOwner() {
 		return owner;
 	}
 
 	public void setOwner(String owner) {
 		this.owner = owner;
 	}
 
 	public int getId() {
 		return id;
 	}
 	
 	public boolean isDirty(DirtyMobilePhoneReason type)
 	{
 		return dirty.contains(type);
 	}
 	
 	public boolean isDirty()
 	{
 		return !dirty.isEmpty();
 	}
 	
 	public void clearDirty()
 	{
 		dirty.clear();
 	}
 
 	@Override
 	public String toString() {
 		return "MobilePhone [id=" + id + ", networkId=" + networkId
 				+ ", owner=" + owner + ", dirty=" + dirty + "]";
 	}
 }
