 package eu.stratuslab.storage.persistence;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.MapKey;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToMany;
 import javax.persistence.Query;
 
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 import org.simpleframework.xml.ElementMap;
 
 import eu.stratuslab.storage.disk.utils.MiscUtils;
 
 @SuppressWarnings("serial")
 @Entity
 @NamedQueries({
 	@NamedQuery(name = "allInstances", query = "SELECT NEW eu.stratuslab.storage.persistence.InstanceView(i.vmId, i.owner) FROM Instance i ORDER BY i.vmId DESC"),
 	@NamedQuery(name = "allInstancesByUser", query = "SELECT NEW eu.stratuslab.storage.persistence.InstanceView(i.vmId, i.owner) FROM Instance i WHERE i.owner = :user ORDER BY i.vmId DESC")})
 public class Instance implements Serializable {
 
 	private static final String DEVICE_PREFIX = "vd";
 
 	public static Instance load(String vmId) {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		Instance disk = em.find(Instance.class, vmId);
 		em.close();
 		return disk;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<InstanceView> listAll() {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		Query q = em.createNamedQuery("allInstances");
 		List<InstanceView> list = q.getResultList();
 		em.close();
 		return list;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<InstanceView> listAllByUser(String user) {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		Query q = em.createNamedQuery("allInstancesByUser");
 		q.setParameter("user", user);
 		List<InstanceView> list = q.getResultList();
 		em.close();
 		return list;
 	}
 
 	public Instance store() {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		EntityTransaction transaction = em.getTransaction();
 		transaction.begin();
 		Instance obj = em.merge(this);
 		transaction.commit();
 		em.close();
 		return obj;
 	}
 
 	public void remove() {
 		remove(vmId);
 	}
 
 	public static void remove(String vmId) {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		EntityTransaction transaction = em.getTransaction();
 		transaction.begin();
 		Instance fromDb = em.find(Instance.class, vmId);
 		if (fromDb != null) {
 			em.remove(fromDb);
 		}
 		transaction.commit();
 		em.close();
 	}
 
 	public Instance() {
 	}
 
 	public Instance(String vmId, String owner) {
 		this.vmId = vmId;
 		this.owner = owner;
 	}
 
 	@Id
 	private String vmId;
 
 	private String node; // host
 
 	private String timestamp = MiscUtils.getTimestamp();
 	
 	private String owner;
 
 	@MapKey(name = "id")
 	@OneToMany(mappedBy = "instance", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
 	@ElementMap(name = "mounts", required = false, data = true, valueType = Mount.class)
 	private Map<String, Mount> mounts = new HashMap<String, Mount>(); // key
 																		// uuid
 
 	public Map<String, Mount> getMounts() {
 		return mounts;
 	}
 
 	public String getOwner() {
 		return owner;
 	}
 
 	public String getVmId() {
 		return vmId;
 	}
 
 	public String nextDiskTarget(String vmId) {
 
 		String nextTarget = null;
 
		for (char driveLetter = 'z'; driveLetter >= 'a'; driveLetter--) {
 			String device = DEVICE_PREFIX + driveLetter;
 			if (containsDevice(device)) {
 				continue;
 			}
 			nextTarget = device;
 			break;
 		}
 
 		if (nextTarget == null) {
 			throw new ResourceException(Status.CLIENT_ERROR_CONFLICT,
 					"no free devices available");
 		}
 
 		return nextTarget;
 	}
 
 	private boolean containsDevice(String device) {
 		for (String mount : mounts.keySet()) {
 			if (device.equals(mounts.get(mount).getDevice())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void setTimestamp(String timestamp) {
 		this.timestamp = timestamp;
 	}
 
 	public String getTimestamp() {
 		return timestamp;
 	}
 
 	public void setNode(String node) {
 		this.node = node;
 	}
 
 	public String getNode() {
 		return node;
 	}
 
 }
