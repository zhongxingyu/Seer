 package com.micmiu.framework.web.v1.base.service.impl;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.criterion.Criterion;
 
 import com.micmiu.framework.web.v1.base.dao.BasicDao;
 import com.micmiu.framework.web.v1.base.service.BasicService;
 import com.micmiu.framework.web.v1.base.vo.AbstractPagedQuery;
 
 /**
  * 
  * @author <a href="http://www.micmiu.com">Michael Sun</a>
  */
 public abstract class BasicServiceImpl<E, ID extends Serializable> implements
 		BasicService<E, ID> {
 
 	public abstract BasicDao<E, ID> getBasicDao();
 
 	@Override
 	public ID save(E entity) {
 		return getBasicDao().save(entity);
 
 	}
 
 	@Override
 	public void saveOrUpdate(E entity) {
 		getBasicDao().saveOrUpdate(entity);
 
 	}
 
 	@Override
 	public void delete(E entity) {
 		getBasicDao().delete(entity);
 
 	}
 
 	@Override
 	public void delete(ID id) {
 		getBasicDao().delete(id);
 
 	}
 
 	@Override
 	public void update(E entity) {
 		getBasicDao().update(entity);
 
 	}
 
 	@Override
 	public E findById(ID id) {
 		return getBasicDao().findById(id);
 	}
 
 	@Override
 	public List<E> findAll() {
 		return getBasicDao().findAll();
 	}
 
 	/**
 	 * 分页查询
 	 * 
 	 * @param query
 	 * @return List
 	 */
 	public List<E> pageQuery(AbstractPagedQuery<E> page) {
 		return getBasicDao().pageQuery(page);
 	}
 
 	/**
 	 * 按属性查找对象列表, 匹配方式为相等.
 	 */
 	@Override
 	public List<E> findList(final String propertyName, final Object value) {
 		return getBasicDao().findList(propertyName, value);
 	}
 
 	/**
 	 * 按属性查找唯一对象, 匹配方式为相等.
 	 */
 	@Override
 	public E findUnique(final String propertyName, final Object value) {
 		return getBasicDao().findUnique(propertyName, value);
 	}
 
 	/**
 	 * 按HQL查询对象列表.
 	 * 
 	 * @param params
 	 */
 	@Override
 	public List<E> findList(final String hql, final Object... params) {
 		return getBasicDao().findList(hql, params);
 	}
 
 	/**
 	 * 按HQL查询对象列表.
 	 * 
 	 * @param paramMap
 	 *            命名参数,按名称绑定.
 	 */
 	@Override
 	public List<E> findList(final String hql, final Map<String, ?> paramMap) {
 		return getBasicDao().findList(hql, paramMap);
 	}
 
 	/**
 	 * 按HQL查询唯一对象.
 	 * 
 	 * @param params
 	 */
 	@Override
 	public E findUnique(final String hql, final Object... params) {
 		return getBasicDao().findUnique(hql, params);
 	}
 
 	/**
 	 * 按HQL查询唯一对象.
 	 * 
 	 * @param paramMap
 	 *            命名参数,按名称绑定.
 	 */
 	@Override
 	public E findUnique(final String hql, final Map<String, ?> paramMap) {
 		return getBasicDao().findUnique(hql, paramMap);
 	}
 
 	/**
 	 * 按Criteria查询对象列表.
 	 * 
 	 * @param criterions
 	 */
 	@Override
 	public List<E> findList(final Criterion... criterions) {
 		return getBasicDao().findList(criterions);
 	}
 
 	/**
 	 * 按Criteria查询唯一对象.
 	 * 
 	 * @param criterions
 	 */
 	@Override
 	public E findUnique(final Criterion... criterions) {
 		return getBasicDao().findUnique(criterions);
 	}
 
 	protected <X> List<X> findListObject(String hql, Object... params) {
 		return getBasicDao().findListObject(hql, params);
 	}
 
 	protected <X> List<X> findListObject(String hql, Map<String, ?> paramMap) {
 		return getBasicDao().findListObject(hql, paramMap);
 	}
 
 	protected <X> X findUniqueObject(String hql, Object... params) {
		return getBasicDao().findUniqueObject(hql, params);
 	}
 
 	protected <X> X findUniqueObject(String hql, Map<String, ?> paramMap) {
		return getBasicDao().findUniqueObject(hql, paramMap);
 	}
 }
