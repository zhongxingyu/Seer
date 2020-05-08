 package org.iucn.sis.server.api.locking;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 import org.iucn.sis.server.api.persistance.SISPersistentManager;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.Lock;
 import org.iucn.sis.shared.api.models.User;
 
 public class HibernateLockRepository extends LockRepository {
 	
 	private final SISPersistentManager manager;
 
 	public HibernateLockRepository() {
		manager = SISPersistentManager.instance();
 	}
 
 	@SuppressWarnings("unchecked")
 	public void clearGroup(String id) throws LockException {
 		Session session = manager.openSession();
 		session.beginTransaction();
 		
 		Criteria criteria = session.createCriteria(Lock.class);
 		criteria = criteria.add(Restrictions.eq("group", id));
 		
 		List<Lock> results;
 		try {
 			results = criteria.list();
 		} catch (HibernateException e) {
 			Debug.println(e);
 			session.close();
 			throw new LockException(e);
 		}
 		
 		if (results != null && !results.isEmpty()) {
 			for (Lock lock : results) {
 				try {
 					manager.deleteObject(session, lock);
 				} catch (PersistentException e) {
 					session.getTransaction().rollback();
 					session.close();
 					throw new LockException(e);
 				}
 			}
 		}
 		
 		session.getTransaction().commit();
 		session.close();
 	}
 
 	@Override
 	public LockInfo getLockedAssessment(Integer lockID) {
 		Session session = manager.openSession();
 		Criteria criteria = session.createCriteria(Lock.class);
 		criteria = criteria.add(Restrictions.eq("lockid", lockID));
 		
 		Lock lock;
 		try {
 			lock = (Lock)criteria.list().get(0);
 		} catch (NullPointerException e) {
 			Debug.println(e);
 			session.close();
 			return null;
 		} catch (IndexOutOfBoundsException e) {
 			Debug.println(e);
 			session.close();
 			return null;
 		} catch (ClassCastException e) {
 			Debug.println(e);
 			session.close();
 			return null;
 		}
 		
 		LockInfo info = new LockInfo(lock.getLockID(), lock.getUser().getId(), 
 				LockType.fromString(lock.getType()), lock.getGroup(), lock.getDate(), this);
 		
 		session.close();
 		
 		return info;
 	}
 
 	@Override
 	public boolean isAssessmentPersistentLocked(Integer id) {
 		Session session = manager.openSession();
 		boolean result = isAssessmentPersistentLocked(session, id);
 		session.close();
 		return result;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public boolean isAssessmentPersistentLocked(Session session, Integer id) {
 		Criteria criteria = session.createCriteria(Lock.class);
 		criteria = criteria.add(Restrictions.eq("lockid", id));
 		
 		List list = criteria.list();
 		
 		return list != null && !list.isEmpty();
 	}
 
 	@Override
 	public Map<String, List<Integer>> listGroups() {
 		Session session = manager.openSession();
 		List<Lock> list;
 		try {
 			list = manager.listObjects(Lock.class, session);
 		} catch (PersistentException e) {
 			Debug.println(e);
 			session.close();
 			return new HashMap<String, List<Integer>>();
 		}
 		
 		final Map<String, List<Integer>> map = 
 			new ConcurrentHashMap<String, List<Integer>>();
 		
 		for (Lock lock : list) {
 			final String groupID = lock.getGroup();
 			
 			List<Integer> l = new ArrayList<Integer>();
 			l.add(lock.getId());
 			
 			map.put(groupID, l);
 		}
 		
 		session.close();
 		
 		return map;
 	}
 
 	@Override
 	public List<LockInfo> listLocks() {
 		final List<LockInfo> list = new ArrayList<LockRepository.LockInfo>();
 		final Session session = manager.openSession();
 		final List<Lock> locks;
 		try {
 			locks = manager.listObjects(Lock.class, session);
 		} catch (PersistentException e) {
 			Debug.println(e);
 			session.close();
 			return new ArrayList<LockRepository.LockInfo>();
 		}
 		
 		for (Lock lock : locks)
 			list.add(new LockInfo(lock.getLockID(), 
 					lock.getUser().getId(), 
 					LockType.fromString(lock.getType()), 
 					lock.getGroup(), lock.getDate(), this));
 		
 		session.close();
 		
 		return list;
 	}
 
 	@Override
 	public LockInfo lockAssessment(Integer id, User owner, LockType lockType) {
 		return lockAssessment(id, owner, lockType, null);
 	}
 
 	@Override
 	public LockInfo lockAssessment(Integer id, User owner, LockType lockType, String groupID) {
 		if (isAssessmentPersistentLocked(id))
 			return getLockedAssessment(id);
 		
 		Lock lock = new Lock();
 		lock.setLockID(id);
 		lock.setUser(owner);
 		lock.setDate(new Date());
 		lock.setType(lockType.toString());
 		lock.setGroup(groupID);
 		
 		Session session = manager.openSession();
 		session.beginTransaction();
 		
 		try {
 			manager.saveObject(session, lock);
 		} catch (PersistentException e) {
 			Debug.println(e);
 			session.getTransaction().rollback();
 			session.close();
 			return null;
 		}
 		
 		LockInfo result = new LockInfo(lock.getLockID(), lock.getUser().getId(), 
 				LockType.fromString(lock.getType()), groupID, this);
 		
 		Debug.println("Created Hibernate lock ({0}): {1}", lock.getId(), result);
 		
 		session.getTransaction().commit();
 		session.close();
 		
 		Debug.println(listLocks());
 		
 		return result;
 	}
 
 	@Override
 	public void removeLockByID(Integer id) {
 		Session session = manager.openSession();
 		session.beginTransaction();
 		
 		Criteria criteria = session.createCriteria(Lock.class);
 		criteria = criteria.add(Restrictions.eq("lockid", id));
 		
 		Lock lock;
 		try {
 			lock = (Lock)criteria.list().get(0);
 		} catch (NullPointerException e) {
 			Debug.println(e);
 			session.close();
 			return;
 		} catch (IndexOutOfBoundsException e) {
 			Debug.println(e);
 			session.close();
 			return;
 		} catch (ClassCastException e) {
 			Debug.println(e);
 			session.close();
 			return;
 		}
 		
 		try {
 			manager.deleteObject(session, lock);
 		} catch (PersistentException e) {
 			session.getTransaction().rollback();
 			Debug.println(e);
 		} finally {
 			session.getTransaction().commit();
 			session.close();
 		}
 	}
 }
