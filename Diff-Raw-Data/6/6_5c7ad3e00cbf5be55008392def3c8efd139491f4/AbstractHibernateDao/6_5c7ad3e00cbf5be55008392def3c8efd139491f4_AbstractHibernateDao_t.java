 package com.xone.model.hibernate.generic;
 
 
 import java.beans.PropertyDescriptor;
 import java.io.Serializable;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.ParameterizedType;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.ibatis.session.SqlSession;
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.LockMode;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.criterion.CriteriaSpecification;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Example.PropertySelector;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projection;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.impl.CriteriaImpl;
 import org.hibernate.metadata.ClassMetadata;
 import org.hibernate.transform.ResultTransformer;
 import org.hibernate.type.Type;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.BeansException;
 import org.springframework.orm.hibernate3.HibernateCallback;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import org.springframework.util.Assert;
 
 import com.xone.model.hibernate.support.Pagination;
 
 @SuppressWarnings("unchecked")
 public class AbstractHibernateDao<T extends Serializable> extends HibernateDaoSupport implements HibernateDao<T> {
 	
 	protected final Logger logger = LoggerFactory.getLogger(getClass());
 	protected Class<T> entityClass;
 	protected SqlSession sqlSession;
 	
 	public AbstractHibernateDao() {
 	}
 
 	public SqlSession getSqlSession() {
 		return sqlSession;
 	}
 
 	public void setSqlSession(SqlSession sqlSession) {
 		this.sqlSession = sqlSession;
 	}
 	
 	public <T> T getMapper(Class<T> type) {
 		return getSqlSession().getMapper(type);
 	}
 
 	/**
 	 * 保存对象
 	 * 
 	 * @param entity
 	 *            实体对象
 	 * @return 实体对象
 	 */
 	public T save(T entity) {
 		Assert.notNull(entity);
 		getHibernateTemplate().save(setDateCreated(entity));
 		return entity;
 	}
 	
 	/**
 	 * 设置更新时间
 	 * @param entity
 	 */
 	protected T setLastUpdated(T entity) {
 		try {
 			Method setLastUpdated = entity.getClass().getMethod("setLastUpdated", Date.class);
 			setLastUpdated.invoke(entity, new Date());
 		} catch (Exception e) {
 			logger.debug(e.getMessage());
 		}
 		return entity;
 	}
 	
 	public T setDateCreated(T entity) {
 		try {
 			Date date = new Date();
 			Method setDateCreated = entity.getClass().getMethod("setDateCreated", Date.class);
 			setDateCreated.invoke(entity, date);
 			Method setLastUpdated = entity.getClass().getMethod("setLastUpdated", Date.class);
 			setLastUpdated.invoke(entity, date);
 		} catch (Exception e) {
 			logger.debug(e.getMessage());
 		}
 		return entity;
 	}
 	
 	/**
 	 * 
 	 * 批量保存对象。
 	 * 
 	 * @param entity
 	 * @return
 	 */
 	public List<T> save(List<T> entities) {
 		Assert.notNull(entities);
 		List<T> result = new ArrayList<T>(entities.size());
 		for (int i = 0; i < entities.size(); i++) {
 			result.add(this.save(entities.get(i)));
			// fix bug:a different object with the same identifier value was already associated with the session
			//if (i % 20 == 0) {
 				getSession(false).flush();
			//}
 		}
 		return result;
 	}
 	/**
 	 * 更新对象
 	 * 
 	 * @param entity
 	 *            实体对象
 	 * @return 实体对象
 	 */
 	public T update(T entity) {
 		Assert.notNull(entity);
 		getHibernateTemplate().update(setLastUpdated(entity));
 		return entity;
 	}
 	/**
 	 * 保存或更新对象
 	 * 
 	 * @param entity
 	 *            实体对象
 	 * @return 实体对象
 	 */
 	public T saveOrUpdate(T entity) {
 		Assert.notNull(entity);
 		getHibernateTemplate().saveOrUpdate(setLastUpdated(entity));
 		return entity;
 	}
 	/**
 	 * 保存或更新对象拷贝
 	 * 
 	 * @param entity
 	 * @return 已更新的持久化对象
 	 */
 	public Object merge(Object entity) {
 		Assert.notNull(entity);
 		return getHibernateTemplate().merge(entity);
 	}
 	/**
 	 * 删除对象
 	 * 
 	 * @param entity
 	 *            实体对象
 	 */
 	public void delete(Object entity) {
 		Assert.notNull(entity);
 		getHibernateTemplate().delete(entity);
 	}
 	
 	/**
 	 * 根据ID删除记录
 	 * 
 	 * @param id
 	 *            记录ID
 	 */
 	public T deleteById(Serializable id) {
 		Assert.notNull(id);
 		T entity = load(id);
 		if (null == entity) {
 			return null;
 		}
 		getHibernateTemplate().delete(entity);
 		return entity;
 	}
 	
 	/**
 	 * 根据ID删除记录
 	 * 
 	 * @param id
 	 *            记录ID
 	 */
 	public T findById(Serializable id) {
 		Assert.notNull(id);
 		try {
 			return load(id);
 		} catch (Exception e) {}
 		return null;
 	}
 	
 	
 	/**
 	 * 
 	 * 根据ID批量删除。
 	 * 
 	 * @param ids
 	 * @return
 	 */
 	@SuppressWarnings("rawtypes")
 	public int deleteById(Serializable[] ids) {
 		Assert.notNull(ids);
 		Class clazz = this.getClass();
 		java.lang.reflect.Type genType = clazz.getGenericSuperclass();
 		java.lang.reflect.Type[] params = ((java.lang.reflect.ParameterizedType) genType)
 				.getActualTypeArguments();
 		Class classT = (Class) params[0];
 		String className = classT.getName();
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("delete from ");
 		buffer.append(className);
 		buffer.append(" dClass where dClass.id in (:id)");
 		Query query = getSession(false).createQuery(buffer.toString());
 		//query.setCacheable(true);
 		query.setParameterList("id", ids);
 		return query.executeUpdate();
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public int deleteByProperties(String propertyName, List<T> propertyList) {
 		Assert.notNull(propertyList);
 		Class clazz = this.getClass();
 		java.lang.reflect.Type genType = clazz.getGenericSuperclass();
 		java.lang.reflect.Type[] params = ((java.lang.reflect.ParameterizedType) genType)
 				.getActualTypeArguments();
 		Class classT = (Class) params[0];
 		String className = classT.getName();
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("delete from ");
 		buffer.append(className);
 		buffer.append(" dClass where dClass.");
 		buffer.append(propertyName);
 		buffer.append(" in (:property)");
 		Query query = getSession(false).createQuery(buffer.toString());
 		//query.setCacheable(true);
 		query.setParameterList("property", propertyList);
 		return query.executeUpdate();
 	}
 	
 	/**
 	 * 通过ID查找对象
 	 * 
 	 * @param id
 	 *            记录的ID
 	 * @param lock
 	 *            是否锁定对象
 	 * @return 实体对象
 	 */
 	public T load(Serializable id) {
 		Assert.notNull(id);
 		return load(id, false);
 	}
 
 	/**
 	 * 通过ID查找对象
 	 * @param id
 	 * 			记录的ID
 	 * @return 	实体对象
 	 */
 	public T get(Serializable id) {
 		Assert.notNull(id);
 		return (T) getHibernateTemplate().get(getPersistentClass(), id);
 	}
 	/**
 	 * 通过ID查找对象,不锁定对象
 	 * 
 	 * @param id
 	 *            记录的ID
 	 * @return 实体对象
 	 */
 	public T load(Serializable id, boolean lock) {
 		Assert.notNull(id);
 		T entity = null;
 		if (lock) {
 			entity = (T) getHibernateTemplate().load(getPersistentClass(), id,
 					LockMode.UPGRADE);
 		} else {
 			entity = (T) getHibernateTemplate().load(getPersistentClass(), id);
 		}
 		return entity;
 	}
 	/**
 	 * 查找所有对象
 	 * 
 	 * @return 对象列表
 	 */
 	public List<T> findAll() {
 		return findByCriteria();
 	}
 
 	/**
 	 * 根据提供的sql预编译语句和参数值查找对象
 	 * @param hql
 	 * 			预编译sql语句
 	 * @param values 
 	 * 			数量可变的参数
 	 * @return 实体对象列表
 	 */
 	@SuppressWarnings("rawtypes")
 	public List find(String hql, Object... values) {
 		return createQuery(hql, values).list();
 	}
 
 	/**
 	 * 根据提供的sql预编译语句和参数值查找对象
 	 * @param hql
 	 * 			预编译sql语句
 	 * @param values 
 	 * 			数量可变的参数
 	 * @return 实体对象
 	 */
 	public Object findUnique(String hql, Object... values) {
 		return createQuery(hql, values).uniqueResult();
 	}
 
 	/**
 	 * 按属性查找对象列表.
 	 * 
 	 * @param property
 	 * @param value
 	 * @return
 	 */
 	public List<T> findByProperty(String property, Object value) {
 		Assert.hasText(property);
 		return createCriteria(Restrictions.eq(property, value)).list();
 	}
 	
 	/**
 	 * 按属性查找对象列表.
 	 * 
 	 * @param property
 	 * @param value
 	 * @return
 	 */
 	public <K> List<T> findInProperty(String property, List<K> value) {
 		Assert.hasText(property);
 		return createCriteria(Restrictions.in(property, value)).list();
 	}
 
 	/**
 	 * 按属性查找唯一对象.
 	 * 
 	 * @param property
 	 * @param value
 	 * @return
 	 */
 	public T findUniqueByProperty(String property, Object value) {
 		Assert.hasText(property);
 		Assert.notNull(value);
 		return (T) createCriteria(Restrictions.eq(property, value))
 				.uniqueResult();
 	}
 	/**
 	 * 按属性查找对象的数量
 	 * 
 	 * @param property
 	 * @param value
 	 * @return
 	 */
 	public int countByProperty(String property, Object value) {
 		Assert.hasText(property);
 		Assert.notNull(value);
 		return ((Number) (createCriteria(Restrictions.eq(property, value))
 				.setProjection(Projections.rowCount()).uniqueResult()))
 				.intValue();
 	}
 
 	/**
 	 * 根据查询函数与参数列表创建Query对象,后续可进行更多处理,辅助函数.
 	 * @param queryString
 	 * @param values
 	 * @return
 	 */
 	public Query createQuery(String queryString, Object... values) {
 		Assert.hasText(queryString);
 		Query queryObject = getSession(false).createQuery(queryString);
 		//queryObject.setCacheable(true);
 		if (values != null) {
 			for (int i = 0; i < values.length; i++) {
 				queryObject.setParameter(i, values[i]);
 			}
 		}
 		return queryObject;
 	}
 
 	/**
 	 * 按Criterion查询对象列表.
 	 * 
 	 * @param criterion
 	 *            数量可变的Criterion.
 	 */
 	public List<T> findByCriteria(Criterion... criterion) {
 		return createCriteria(criterion).list();
 	}
 
 	/**
 	 * 获取分页对象
 	 * @param crit
 	 * @param pageNo
 	 * @param pageSize
 	 * @param projection
 	 * @param orders
 	 * @return
 	 */
 	public Pagination findByCriteria(Criteria crit, int pageNo,
 			int pageSize, Projection projection, Order... orders) {
 		int totalCount = ((Number) crit.setProjection(Projections.rowCount())
 				.uniqueResult()).intValue();
 		Pagination p = new Pagination(pageNo, pageSize, totalCount);
 		if (totalCount < 1) {
 			p.setList(Collections.emptyList());
 			return p;
 		}
 		crit.setProjection(projection);
 		if (projection == null) {
 			crit.setResultTransformer(Criteria.ROOT_ENTITY);
 		}
 		if (orders != null) {
 			for (Order order : orders) {
 				crit.addOrder(order);
 			}
 		}
 		crit.setFirstResult(p.getFirstResult());
 		crit.setMaxResults(p.getPageSize());
 		p.setList(crit.list());
 		return p;
 	}
 
 	/**
 	 * @param hql
 	 *            查询语句
 	 * @param countHql
 	 *            查询总数
 	 * @param pageSize
 	 *            每页大小
 	 * @param startIndex
 	 *            开始索引位置
 	 * @return
 	 */
 	public Pagination findPageByQuery(final String hql, final String countHql,
 			final int pageSize, final int startIndex) {
 		return (Pagination) getHibernateTemplate().execute(
 				new HibernateCallback() {
 					@SuppressWarnings("rawtypes")
 					public Object doInHibernate(Session session)
 							throws HibernateException, SQLException {
 						int totalCount = ((Number) session
 								.createQuery(countHql).iterate().next())
 								.intValue();
 						Query query = session.createQuery(hql);
 						query.setFirstResult(startIndex);
 						query.setMaxResults(pageSize);
 						List items = query.list();
 						Pagination ps = new Pagination(startIndex / pageSize
 								+ 1, pageSize, totalCount, items);
 						return ps;
 					}
 				});
 	}
 
 	/**
 	 * 查询对象
 	 * @param hql 查询语句
 	 * @param pageSize 页大小
 	 * @param startIndex 开始位置
 	 * @return
 	 */
 	@SuppressWarnings("rawtypes")
 	public List findByQuery(final String hql, final int pageSize,
 			final int startIndex) {
 		return (List) getHibernateTemplate().execute(new HibernateCallback() {
 			public Object doInHibernate(Session session)
 					throws HibernateException, SQLException {
 				Query query = session.createQuery(hql);
 				query.setFirstResult(startIndex);
 				query.setMaxResults(pageSize);
 				return query.list();
 			}
 		});
 	}
 
 	/**
 	 * 根据查询条件查询总数
 	 * @param condi
 	 * @return
 	 */
 	public int countByProperty(DetachedCriteria detachCriteria){
 		Criteria criteria=detachCriteria
 		.getExecutableCriteria(getSession(false));
 		criteria.setProjection(Projections.rowCount());
 		return ((Number)criteria.uniqueResult()).intValue();
 	}
 
 	/**
 	 * 刷新对象
 	 * 
 	 * @param entity
 	 */
 	public void refresh(Object entity) {
 		getSession(false).refresh(entity);
 	}
 
 	/** 
 	 * 根据Criterion条件创建Criteria,后续可进行更多处理,辅助函数.
 	 * @param criterions
 	 * @return
 	 */
 	public Criteria createCriteria(Criterion... criterions) {
 		Criteria criteria = getSession(false).createCriteria(
 				getPersistentClass());
 		for (Criterion c : criterions) {
 			criteria.add(c);
 		}
 		//criteria.setCacheable(true);
 		return criteria;
 	}
 
 	/**查询对象列表
 	 * @param criterion
 	 * 			游离态的动态查询对象
 	 * @return
 	 */
 
 	public List<T> findByDetachedCriteria(DetachedCriteria detachCriteria) {
 		return getHibernateTemplate().findByCriteria(detachCriteria);
 //		return detachCriteria.getExecutableCriteria(getSession(false)).list();
 	}
 	
 	public List<T> findListByDetachedCriteria(DetachedCriteria detachCriteria, int fetchSize, int maxResults) {
 		return getHibernateTemplate().findByCriteria(detachCriteria, fetchSize, maxResults);
 		//return detachCriteria.getExecutableCriteria(getSession(false)).setFetchSize(fetchSize).setMaxResults(maxResults).list();
 	}
 
 	/**
 	 * 查询分页对象
 	 * @param criterion
 	 * 			游离态的动态查询对象
 	 * @return
 	 */
 	@SuppressWarnings("rawtypes")
 	public Pagination findByDetachedCriteria(DetachedCriteria detachCriteria,
 			int pageSize, int startIndex) {
 		Session session = getSession(false);
 		Criteria criteria = detachCriteria.getExecutableCriteria(session);
 		CriteriaImpl impl = (CriteriaImpl) criteria;
 		Projection poj = impl.getProjection();
 		ResultTransformer transformer = impl.getResultTransformer();
 		int totalCount = (Integer) criteria.setProjection(
 				Projections.rowCount()).uniqueResult();
 		criteria.setProjection(poj);
 		if (poj == null)
 			criteria.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);
 		if (transformer != null) {
 			criteria.setResultTransformer(transformer);
 		}
 		List items = criteria.setFirstResult((startIndex - 1) * pageSize)
 				.setMaxResults(pageSize).list();
 		Pagination ps = new Pagination(startIndex, pageSize,
 				totalCount, items);
 		return ps;
 	}
 	
 	/**
 	 * 查询分页对象
 	 * @param criterion
 	 * 			游离态的动态查询对象
 	 * @return
 	 */
 	@SuppressWarnings("rawtypes")
 	public Pagination findBySqlMap(String sqlMapName, Map<String, String> params,
 	        int pageSize, int pageNo) {
 		List items = null;
 		Integer totalCount = 0;
 		params.put("limitStart", (pageNo - 1) * pageSize + "");
 		params.put("limitNum", pageSize + "");
 		try {
 			totalCount = (Integer) getSqlSession().selectOne(sqlMapName + "TotalCount", params);//.queryForObject(sqlMapName + "TotalCount", params);
 			if (null == totalCount || totalCount <= 0) {
 				return new Pagination(0, 0, 0, Collections.emptyList());
 			}
 			items = getSqlSession().selectList(sqlMapName, params);//sqlMapClient.queryForList(sqlMapName, params);
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 		}
 
 		if (items == null) {
 			items = Collections.emptyList();
 		}
 
 		Pagination ps = new Pagination(pageNo / pageSize + 1, pageSize,
 				totalCount, items);
 	    return ps;
 	}
 
 	/**
 	 * 根据传入sql和主键id批量操作
 	 */
 	public int batchUpdateDelete(String sql, Serializable[] ids) {
 		Session session=this.getSession(false);
 		Query query = session.createQuery(sql);
 		query.setParameterList("id", ids);
 		int dels = query.executeUpdate();
 		return dels;
 	}
 
 	@SuppressWarnings("rawtypes")
 	public void compSQL(String sql, Map proMap,Map listMap) {
 		Session session=this.getSession(false);
 		Query query = session.createQuery(sql);
 		Set set=proMap.keySet();
 		Iterator it=set.iterator();
 		while(it.hasNext()){
 			Object key=it.next();
 			query.setParameter((String)key, proMap.get(key));
 		}
 		set=listMap.keySet();
 		it=set.iterator();
 		while(it.hasNext()){
 			Object key=it.next();
 			query.setParameterList((String)key, (List)listMap.get(key));
 		}
 		query.executeUpdate();
 	}
 	
 	public Class<T> getPersistentClass() {
 		if (entityClass == null) {
 			entityClass = (Class<T>) ((ParameterizedType) getClass()
 					.getGenericSuperclass()).getActualTypeArguments()[0];
 		}
 		return entityClass;
 	}
 
 	public T createNewEntity() {
 		try {
 			return getPersistentClass().newInstance();
 		} catch (Exception e) {
 			throw new RuntimeException("不能创建实体对象："
 					+ getPersistentClass().getName());
 		}
 	}
 
 	@SuppressWarnings("rawtypes")
 	protected ClassMetadata getClassMetadata(Class clazz) {
 		return (ClassMetadata) getHibernateTemplate().getSessionFactory()
 				.getClassMetadata(clazz);
 	}
 
 	/**
 	 * 不为空的EXAMPLE属性选择方式
 	 * 
 	 */
 //	static final class NotBlankPropertySelector implements PropertySelector {
 //		private static final long serialVersionUID = 1L;
 //
 //		public boolean include(Object object, String property, Type type) {
 //			return object != null
 //					&& !(object instanceof String && StringUtils
 //							.hasText((String) object));
 //		}
 //	}
 
 	/**
 	 * 根据属性删除
 	 * 
 	 * @param propertyName
 	 * @param value
 	 */
 	public int deleteByProperty(String propertyName, Object value) {
 		return deleteByProperty(propertyName, new Object[] { value });
 	}
 
 	/**
 	 * 通过属性删除实体
 	 * 
 	 * @param propertyName
 	 * @param ids
 	 * @return
 	 */
 	@SuppressWarnings("rawtypes")
 	public int deleteByProperty(String propertyName, Object[] ids) {
 		Assert.notNull(ids);
 		Class clazz = this.getClass();
 		java.lang.reflect.Type genType = clazz.getGenericSuperclass();
 		java.lang.reflect.Type[] params = ((java.lang.reflect.ParameterizedType) genType)
 				.getActualTypeArguments();
 		Class classT = (Class) params[0];
 		String className = classT.getName();
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("delete from ");
 		buffer.append(className);
 		if (ids.length == 1)
 			buffer.append(" dClass where dClass." + propertyName + " = :id");
 		else
 			buffer.append(" dClass where dClass." + propertyName + " in (:id)");
 		Query query = getSession(false).createQuery(buffer.toString());
 		//query.setCacheable(true);
 		if (ids.length == 1)
 			query.setParameter("id", ids[0]);
 		else
 			query.setParameterList("id", ids);
 		return query.executeUpdate();
 	}
 	
 	protected String toMyRole(String name) {
 		if (StringUtils.isBlank(name)) {
 			return StringUtils.EMPTY;
 		}
 		String n = name.replaceFirst("set", "");
 		int sz = n.length();
 		if (sz == 0) {
 			return n;
 		}
 		StringBuffer buffer = new StringBuffer();
 		buffer.append(n.charAt(0));
         for (int i = 1; i < sz; i++) {
         	char c = n.charAt(i);
             if (Character.isUpperCase(c) == true) {
             	buffer.append("_");
             }
         	buffer.append(c);
         }
 		return buffer.toString().toUpperCase();
 	}
 	
 	protected void copyPropertiesFromMap(Map<String, Object> source, Object target, DaoMapCopyRoles copyRoles, MyDaoAssignRules assignRules, String[] ignoreProperties)
 			throws BeansException {
 		
 		Assert.notNull(source, "Source must not be null");
 		Assert.notNull(target, "Target must not be null");
 		
 		PropertyDescriptor[] targetPds = org.springframework.beans.BeanUtils.getPropertyDescriptors(target.getClass());
 		List<String> ignoreList = (ignoreProperties != null) ? Arrays.asList(ignoreProperties) : null;
 		
 		for (PropertyDescriptor targetPd : targetPds) {
 			if (targetPd.getWriteMethod() != null &&
 					(ignoreProperties == null || (!ignoreList.contains(targetPd.getName())))) {
 				Method writeMethod = targetPd.getWriteMethod();
 				Class<?>[] parameterTypes = writeMethod.getParameterTypes();
 				if (parameterTypes.length >= 2 || parameterTypes.length <= 0) {
 					continue;
 				}
 				if (null != copyRoles && !copyRoles.myCopyRules(parameterTypes[0], targetPd.getName())) {//如果值为null就不赋值
 					continue;
 				}
 				Object v = source.get(toMyRole(targetPd.getName()));
 				if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
 					writeMethod.setAccessible(true);
 				}
 				Object value = null;
 				if (null != assignRules) {
 					value = assignRules.myAssignRules(parameterTypes[0], v);
 				}
 				try {
 					writeMethod.invoke(target, value);
 				} catch (IllegalArgumentException e) {
 					e.printStackTrace();
 				} catch (IllegalAccessException e) {
 					e.printStackTrace();
 				} catch (InvocationTargetException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public interface DaoMapCopyRoles {
 		public boolean myCopyRules(Class<?> parameterClass, String method);
 	}
 	
 	public interface MyDaoAssignRules {
 		public Object myAssignRules(Class<?> parameterClass, Object value);
 	}
 
 }
