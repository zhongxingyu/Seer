 package com.codemonkey.service;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.StopWatch;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Criterion;
 import org.json.JSONObject;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.codemonkey.dao.GenericDao;
 import com.codemonkey.domain.IEntity;
 import com.codemonkey.error.BadObjVersionError;
 import com.codemonkey.error.FieldValidation;
 import com.codemonkey.error.FormFieldValidation;
 import com.codemonkey.error.ValidationError;
 import com.codemonkey.utils.ClassHelper;
 import com.codemonkey.utils.ExtConstant;
 import com.codemonkey.web.converter.CustomConversionService;
 
 @Transactional
 public abstract class GenericServiceImpl<T extends IEntity> extends AbsService implements GenericService<T> {
 
 	private GenericDao<T> dao;
 	
 	private Class<?> type;
 	
 	public GenericServiceImpl(){
 		this.type = ClassHelper.getSuperClassGenricType(getClass());
 	}
 	
 	@Override
 	public abstract T createEntity();
 	
 	@Autowired
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		dao = new GenericDao<T>(sessionFactory , type);
 	}
 	
 	protected GenericDao<T> getDao() {
 		return dao;
 	}
 	
 	public T get(Long id) {
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 		T t = getDao().get(id);
 		stopWatch.stop();
 		getLog().info(stopWatch);
 		return t;
 	}
 
 	public void save(T entity) {
 		
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 		
 		if(entity.isOptimisticLockingFailure()){
			throw new BadObjVersionError(get(entity.getId()).detailJson());
 		}
     	
 		entity.setOriginVersion(null);
     	
 		
 		Set<FieldValidation> set = validate(entity);
 		if(CollectionUtils.isNotEmpty(set)){
 			throw new ValidationError(set);
 		}
 		getDao().save(entity);
 		
 		stopWatch.stop();
 		getLog().info(stopWatch);
 		
 	}
 
 	//implements by subclass if needed
 	//if validation failed , throw ValidationError exception
 	protected Set<FieldValidation> validate(T entity) {
 		Set<FieldValidation> errorSet = new HashSet<FieldValidation>();
 		if(entity == null){
 			return errorSet;
 		}
 		
 		if(StringUtils.isNotBlank(entity.getCode())){
 			long count = getDao().countBy("code" , entity.getCode());
 			if(count > 1){
 				errorSet.add(new FormFieldValidation("code" , "code must be unique"));
 			}
 		}
 		
 		return errorSet;
 	}
 
 	public void delete(Long id) {
 		
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 		
 		getDao().delete(id);
 		
 		stopWatch.stop();
 		getLog().info(stopWatch);
 	
 	}
 
 	public List<T> findAll() {
 
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 		
 		List<T> list = getDao().findAll();
 		
 		stopWatch.stop();
 		getLog().info(stopWatch);
 		
 		return list;
 	}
 	
 	public List<T> findAll(int start , int limit){
 		
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 		
 		List<T> list = getDao().findAll(start , limit);
 		
 		stopWatch.stop();
 		getLog().info(stopWatch);
 		
 		return list;
 	}
 	
 	public List<T> find(Criterion... criterions){
 		
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 		
 		List<T> list = getDao().findByCriteria(criterions);
 		
 		stopWatch.stop();
 		getLog().info(stopWatch);
 		
 		return list;
 	}
 	
 	public List<T> find(int start, int limit, Criterion... criterions) {
 		
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 		
 		List<T> list = getDao().findByCriteria(start , limit , criterions);
 		
 		stopWatch.stop();
 		getLog().info(stopWatch);
 		
 		return list;
 	}
 
 	public T findBy(String query , Object... params){
 		
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 		
 		T t = getDao().findBy(query , params);
 		
 		stopWatch.stop();
 		getLog().info(stopWatch);
 		
 		return t;
 	}
 	
 	public T findBy(String query , String[] joins , Object... params){
 		
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 		
 		T t = getDao().findBy(query , joins , params);
 		
 		stopWatch.stop();
 		getLog().info(stopWatch);
 		
 		return t;
 	}
 	
 	@Override
 	public long countBy(String query, Object... params) {
 		return countBy(query , null , params);
 	}
 
 	@Override
 	public long countBy(String query, String[] joins, Object... params) {
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 		
 		long count = getDao().countBy(query, joins , params);
 		
 		stopWatch.stop();
 		getLog().info(stopWatch);
 		
 		return count;
 	}
 	
 	public List<T> findAllBy(String query , Object... params){
 		
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 		
 		List<T> list = getDao().findAllBy(query , params);
 		
 		stopWatch.stop();
 		getLog().info(stopWatch);
 		
 		return list;
 	}
 	
 	@Override
 	public List<T> findAllBy(String query, String[] joins, Object... params) {
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 		
 		List<T> list = getDao().findAllBy(query , joins , params);
 		
 		stopWatch.stop();
 		getLog().info(stopWatch);
 		
 		return list;
 	}
 	
 	public long count(Criterion... criterions){
 		
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 		
 		long count = getDao().count(criterions);
 		
 		stopWatch.stop();
 		getLog().info(stopWatch);
 		
 		return count;
 	}
 	
 	public T convert(String source) {
 		
 		if(StringUtils.isEmpty(source)){
 			return null;
 		}
 		
 		return getDao().get(Long.valueOf(source));
 	}
 	
 	public List<T> findByQueryInfo(JSONObject queryInfo, Integer start, Integer limit) {
 		
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 		
 		List<T> list = getDao().findByQueryInfo(queryInfo , start , limit);
 		
 		stopWatch.stop();
 		getLog().info(stopWatch);
 		
 		return list;
 		
 	}
 
 	public List<T> findByQueryInfo(JSONObject queryInfo) {
 		
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 
 		List<T> list = getDao().findByQueryInfo(queryInfo);
 		
 		stopWatch.stop();
 		getLog().info(stopWatch);
 		
 		return list;
 		
 	}
 
 	public long countByQueryInfo(JSONObject queryInfo) {
 		
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 
 		long count = getDao().countByQueryInfo(queryInfo);
 		
 		stopWatch.stop();
 		getLog().info(stopWatch);
 		
 		return count;
 		
 	}
 	
 	public T doSave(JSONObject body , CustomConversionService ccService){
 		T t = buildEntity(body , ccService);
 		save(t);
 		return t;
 	}
 	
 	public T buildEntity(JSONObject params , CustomConversionService ccService){
 		T t = null;
 		Long id = extractId(params);
 		
 		if(id == null){
 			t = createEntity();
 			ClassHelper.build(params, t , ccService);
 		}else{
 			t = get(id);
 			if(t != null){
 				ClassHelper.build(params, t , ccService);
 			}
 		}
 		return t;
 	}
 	
 	private Long extractId(JSONObject params) {
 		Long id = null;
 		if(params.has(ExtConstant.ID) && StringUtils.isNotBlank(params.getString(ExtConstant.ID)) && !"null".equals(params.getString(ExtConstant.ID))){
 			id = params.getLong(ExtConstant.ID);
 		}
 		return id;
 	}
 
 }
