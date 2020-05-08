 package eu.stratuslab.storage.persistence;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.Query;
 
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 
 import eu.stratuslab.storage.disk.utils.MiscUtils;
 
 @SuppressWarnings("serial")
 @Entity
 @NamedQueries({
 	@NamedQuery(name = "allMounts", query = "SELECT m FROM eu.stratuslab.storage.persistence.Mount m ORDER BY m.device DESC"),
 	@NamedQuery(name = "mounts", query = "SELECT m FROM eu.stratuslab.storage.persistence.Mount m WHERE m.uuid = :uuid ORDER BY m.device")})
 public class Mount implements Serializable {
 
 	private static final String DEVICE_PREFIX = "vd";
 
 	public static Mount load(String id) {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		Mount mount = em.find(Mount.class, id);
 		em.close();
 		return mount;
 	}
 
 	public static Mount load(Instance instance, Disk disk) {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		Mount mount = em.find(Mount.class, constructId(instance, disk));
 		em.close();
 		return mount;
 	}
 
 	private static String constructId(Instance instance, Disk disk) {
 		return disk.getUuid() + "_" + instance.getVmId();
 	}
 
 	public Mount store() {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		EntityTransaction transaction = em.getTransaction();
 		transaction.begin();
 		Mount obj = em.merge(this);
 		transaction.commit();
 		em.close();
 		return obj;
 	}
 
 	public void remove() {
 		remove(id);
 	}
 
 	public static void remove(String id) {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		EntityTransaction transaction = em.getTransaction();
 		transaction.begin();
 		Mount fromDb = em.find(Mount.class, id);
 		if (fromDb != null) {
 			em.remove(fromDb);
 		}
 		transaction.commit();
 		em.close();
 	}
 
 	public Mount() {
 	}
 	
 	public Mount(Instance instance, Disk disk) {
 		id = constructId(instance, disk);
 		this.vmId = instance.getVmId();
 		this.instance = instance;
 		uuid = disk.getUuid();
 		this.disk = disk;
 	}
 
 	@Id
 	private String id;
 
 	public String getId() {
 		return id;
 	}
 
 	private String device; // vd<x>
 
 	private String timestamp = MiscUtils.getTimestamp();
 
 	@ManyToOne(fetch=FetchType.LAZY)
 	private Instance instance;
 
 	private String vmId;
 
 	private String uuid; // Disk uuid
 
 	public String getVmId() {
 		return vmId;
 	}
 
 	public String getUuid() {
 		return uuid;
 	}
 
 	public Instance getInstance() {
 		return instance;
 	}
 
 	public void setInstance(Instance instance) {
 		this.instance = instance;
 		this.vmId = instance.getVmId();
 	}
 
 	@ManyToOne(fetch=FetchType.LAZY)
 	private Disk disk;
 
 	public Disk getDisk() {
 		return disk;
 	}
 
 	public String nextDiskTarget(String vmId) {
 
 		String nextTarget = null;
 
		for (char driveLetter = 'a'; driveLetter <= 'z'; driveLetter++) {
 			String device = DEVICE_PREFIX + driveLetter;
 			if (instance.getMounts().containsKey(device)) {
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
 
 	public void setTimestamp(String timestamp) {
 		this.timestamp = timestamp;
 	}
 
 	public String getTimestamp() {
 		return timestamp;
 	}
 
 	public void setDevice(String device) {
 		this.device = device;
 	}
 
 	public String getDevice() {
 		return device;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<Mount> getMounts(String uuid) {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		Query q = em.createNamedQuery("mounts");
 		q.setParameter("uuid", uuid);
 		List<Mount> list = q.getResultList();
 		em.close();
 		return list;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<Mount> getMounts() {
 		EntityManager em = PersistenceUtil.createEntityManager();
 		Query q = em.createNamedQuery("allMounts");
 		List<Mount> list = q.getResultList();
 		em.close();
 		return list;
 	}
 
 }
