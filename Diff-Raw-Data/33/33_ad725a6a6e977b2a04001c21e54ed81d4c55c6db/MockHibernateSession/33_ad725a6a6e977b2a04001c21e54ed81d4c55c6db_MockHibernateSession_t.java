 /* Copyright (c) 2013, Regents of the University of California.  See License.txt for details */
 
 package org.grayleaves.utility;
 
 
 import java.io.Serializable;
 import java.sql.Connection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.CacheMode;
 import org.hibernate.Criteria;
 import org.hibernate.EntityMode;
 import org.hibernate.Filter;
 import org.hibernate.FlushMode;
 import org.hibernate.HibernateException;
 import org.hibernate.LockMode;
 import org.hibernate.Query;
 import org.hibernate.ReplicationMode;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
//import org.hibernate.LockOptions;
 //import org.hibernate.UnknownProfileException;
 //import org.hibernate.jdbc.Work;
 //import org.hibernate.stat.SessionStatistics;
 
 public class MockHibernateSession implements Session
 {
 	
 	private static final long serialVersionUID = 1L;
 	private List<HibernateRetrievable> list;
 	@SuppressWarnings("rawtypes")
 	private Map map;
 
 	public MockHibernateSession(List<HibernateRetrievable> list)
 	{
 		this.list = list; 
 	}
 
 	@Override
 	public Transaction beginTransaction() throws HibernateException
 	{
 		return null;
 	}
 
 //	@Override
 //	public LockRequest buildLockRequest(LockOptions arg0)
 //	{
 //		return null;
 //	}
 
 	@Override
 	public void cancelQuery() throws HibernateException
 	{
 	}
 
 	@Override
 	public void clear()
 	{
 	}
 
 	@Override
 	public Connection close() throws HibernateException
 	{
 		return null;
 	}
 
 	@Override
 	public Connection connection() throws HibernateException
 	{
 		return null;
 	}
 
 	@Override
 	public boolean contains(Object arg0)
 	{
 		return false;
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Criteria createCriteria(Class arg0)
 	{
 		return null;
 	}
 
 	@Override
 	public Criteria createCriteria(String arg0)
 	{
 		return null;
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Criteria createCriteria(Class arg0, String arg1)
 	{
 		return null;
 	}
 
 	@Override
 	public Criteria createCriteria(String arg0, String arg1)
 	{
 		return null;
 	}
 
 	@Override
 	public Query createFilter(Object arg0, String arg1)
 			throws HibernateException
 	{
 		return null;
 	}
 
 	@Override
 	public Query createQuery(String arg0) throws HibernateException
 	{
 		return null;
 	}
 
 	@Override
 	public SQLQuery createSQLQuery(String arg0) throws HibernateException
 	{
 		return null;
 	}
 
 	@Override
 	public void delete(Object arg0) throws HibernateException
 	{
 	}
 
 //	@Override
 //	public void delete(String arg0, Object arg1) throws HibernateException
 //	{
 //	}
 
 //	@Override
 //	public void disableFetchProfile(String arg0) throws UnknownProfileException
 //	{
 //	}
 
 //	@Override
 //	public void disableFilter(String arg0)
 //	{
 //	}
 
 	@Override
 	public Connection disconnect() throws HibernateException
 	{
 		return null;
 	}
 
 //	@Override
 //	public void doWork(Work arg0) throws HibernateException
 //	{
 //	}
 
 //	@Override
 //	public void enableFetchProfile(String arg0) throws UnknownProfileException
 //	{
 //	}
 
 	@Override
 	public Filter enableFilter(String arg0)
 	{
 		return null;
 	}
 
 	@Override
 	public void evict(Object arg0) throws HibernateException
 	{
 	}
 
 	@Override
 	public void flush() throws HibernateException
 	{
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	public Object get(Class clazz, Serializable id) throws HibernateException
 	{
 		Integer intId = (Integer) id; 
 		if (this.map == null)
 		{
 			map = new HashMap(); 
 			// cast to clazz, return null if fails
 			for (HibernateRetrievable retrievable : this.list)
 			{
 				map.put(retrievable.getId(), retrievable);
 			}
 		}
 		return map.get(intId);
 	}
 
 	@Override
 	public Object get(String arg0, Serializable arg1) throws HibernateException
 	{
 		return null;
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Object get(Class arg0, Serializable arg1, LockMode arg2)
 			throws HibernateException
 	{
 		return null;
 	}
 
 //	@Override
 //	public Object get(Class arg0, Serializable arg1, LockOptions arg2)
 //			throws HibernateException
 //	{
 //		return null;
 //	}
 
 	@Override
 	public Object get(String arg0, Serializable arg1, LockMode arg2)
 			throws HibernateException
 	{
 		return null;
 	}
 
 //	@Override
 //	public Object get(String arg0, Serializable arg1, LockOptions arg2)
 //			throws HibernateException
 //	{
 //		return null;
 //	}
 
 	@Override
 	public CacheMode getCacheMode()
 	{
 		return null;
 	}
 
 	@Override
 	public LockMode getCurrentLockMode(Object arg0) throws HibernateException
 	{
 		return null;
 	}
 
 	@Override
 	public Filter getEnabledFilter(String arg0)
 	{
 		return null;
 	}
 
 	@Override
 	public EntityMode getEntityMode()
 	{
 		return null;
 	}
 
 	@Override
 	public String getEntityName(Object arg0) throws HibernateException
 	{
 		return null;
 	}
 
 	@Override
 	public FlushMode getFlushMode()
 	{
 		return null;
 	}
 
 	@Override
 	public Serializable getIdentifier(Object arg0) throws HibernateException
 	{
 		return null;
 	}
 
 	@Override
 	public Query getNamedQuery(String arg0) throws HibernateException
 	{
 		return null;
 	}
 
 	@Override
 	public Session getSession(EntityMode arg0)
 	{
 		return null;
 	}
 
 	@Override
 	public SessionFactory getSessionFactory()
 	{
 		return null;
 	}
 
 //	@Override
 //	public SessionStatistics getStatistics()
 //	{
 //		return null;
 //	}
 
 //	@Override
 //	public Transaction getTransaction()
 //	{
 //		return null;
 //	}
 
 	@Override
 	public boolean isConnected()
 	{
 		return false;
 	}
 
 //	@Override
 //	public boolean isDefaultReadOnly()
 //	{
 //		return false;
 //	}
 
 	@Override
 	public boolean isDirty() throws HibernateException
 	{
 		return false;
 	}
 
 //	@Override
 //	public boolean isFetchProfileEnabled(String arg0)
 //			throws UnknownProfileException
 //	{
 //		return false;
 //	}
 
 	@Override
 	public boolean isOpen()
 	{
 		return false;
 	}
 
 //	@Override
 //	public boolean isReadOnly(Object arg0)
 //	{
 //		return false;
 //	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Object load(Class arg0, Serializable arg1) throws HibernateException
 	{
 		return null;
 	}
 
 	@Override
 	public Object load(String arg0, Serializable arg1)
 			throws HibernateException
 	{
 		return null;
 	}
 
 	@Override
 	public void load(Object arg0, Serializable arg1) throws HibernateException
 	{
 	}
 
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Object load(Class arg0, Serializable arg1, LockMode arg2)
 			throws HibernateException
 	{
 		return null;
 	}
 
 //	@Override
 //	public Object load(Class arg0, Serializable arg1, LockOptions arg2)
 //			throws HibernateException
 //	{
 //		return null;
 //	}
 
 	@Override
 	public Object load(String arg0, Serializable arg1, LockMode arg2)
 			throws HibernateException
 	{
 		return null;
 	}
 
 //	@Override
 //	public Object load(String arg0, Serializable arg1, LockOptions arg2)
 //			throws HibernateException
 //	{
 //		return null;
 //	}
 
 	@Override
 	public void lock(Object arg0, LockMode arg1) throws HibernateException
 	{
 	}
 
 	@Override
 	public void lock(String arg0, Object arg1, LockMode arg2)
 			throws HibernateException
 	{
 	}
 
 	@Override
 	public Object merge(Object arg0) throws HibernateException
 	{
 		return null;
 	}
 
 	@Override
 	public Object merge(String arg0, Object arg1) throws HibernateException
 	{
 		return null;
 	}
 
 	@Override
 	public void persist(Object arg0) throws HibernateException
 	{
 	}
 
 	@Override
 	public void persist(String arg0, Object arg1) throws HibernateException
 	{
 	}
 
 	@Override
 	public void reconnect() throws HibernateException
 	{
 	}
 
 	@Override
 	public void reconnect(Connection arg0) throws HibernateException
 	{
 	}
 
 	@Override
 	public void refresh(Object arg0) throws HibernateException
 	{
 	}
 
 	@Override
 	public void refresh(Object arg0, LockMode arg1) throws HibernateException
 	{
 	}
 
 //	@Override
 //	public void refresh(Object arg0, LockOptions arg1)
 //			throws HibernateException
 //	{
 //	}
 
 	@Override
 	public void replicate(Object arg0, ReplicationMode arg1)
 			throws HibernateException
 	{
 	}
 
 	@Override
 	public void replicate(String arg0, Object arg1, ReplicationMode arg2)
 			throws HibernateException
 	{
 	}
 
 	@Override
 	public Serializable save(Object arg0) throws HibernateException
 	{
 		return null;
 	}
 
 	@Override
 	public Serializable save(String arg0, Object arg1)
 			throws HibernateException
 	{
 		return null;
 	}
 
 	@Override
 	public void saveOrUpdate(Object arg0) throws HibernateException
 	{
 	}
 
 	@Override
 	public void saveOrUpdate(String arg0, Object arg1)
 			throws HibernateException
 	{
 	}
 
 	@Override
 	public void setCacheMode(CacheMode arg0)
 	{
 	}
 
 //	@Override
 //	public void setDefaultReadOnly(boolean arg0)
 //	{
 //	}
 
 	@Override
 	public void setFlushMode(FlushMode arg0)
 	{
 	}
 
 //	@Override
 //	public void setReadOnly(Object arg0, boolean arg1)
 //	{
 //	}
 
 	@Override
 	public void update(Object arg0) throws HibernateException
 	{
 	}
 
 	@Override
 	public void update(String arg0, Object arg1) throws HibernateException
 	{
 	}
 	@Override
 	public void save(Object arg0, Serializable arg1) throws HibernateException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void save(String arg0, Object arg1, Serializable arg2)
 			throws HibernateException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void update(Object arg0, Serializable arg1)
 			throws HibernateException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void update(String arg0, Object arg1, Serializable arg2)
 			throws HibernateException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void disableFilter(String arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 }
 
