 package org.eweb4j.orm.config;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.eweb4j.cache.ORMConfigBeanCache;
 import org.eweb4j.config.Log;
 import org.eweb4j.config.ScanPackage;
 import org.eweb4j.orm.PropType;
 import org.eweb4j.orm.annotation.Ignore;
 import org.eweb4j.orm.config.bean.ORMConfigBean;
 import org.eweb4j.orm.config.bean.Property;
 import org.eweb4j.util.ClassUtil;
 import org.eweb4j.util.ReflectUtil;
 
 /**
  * Persistent obj annotation read to cache
  * 
  * @author weiwei
  * 
  */
 public class PojoAnnotationConfig extends ScanPackage {
 
 	public PojoAnnotationConfig() {
 		super();
 	}
 
 	/**
 	 * 
 	 * @param clsName
 	 * @throws Exception
 	 */
 	public boolean handleClass(String clsName) {
 		Class<?> clazz = getClass(clsName);
 
 		if (clazz == null)
 			return false;
 		
 		if (clazz.isInterface())
 			return false;
 		
 		Entity entity = clazz.getAnnotation(Entity.class);
 		if (entity == null && !clsName.endsWith("PO")
 				&& !clsName.endsWith("POJO") && !clsName.endsWith("Entity")
 				&& !clsName.endsWith("Model")) {
 			return false;
 		}
 		Table tableAnn = clazz.getAnnotation(Table.class);
 		String table = tableAnn == null ? "" : tableAnn.name();
 		table = "".equals(table.trim()) ? clazz.getSimpleName()
 				.replace("PO", "").replace("POJO", "").replace("Entity", "")
 				.replace("Model", "") : table;
 		if (table == null || table.trim().length() == 0)
 			return false;
 		
 		try {
 			List<Property> pList = getProperties(clazz, null, false, log);
 			List<Property> superList = new ArrayList<Property>();
 			Class<?> superClazz = clazz.getSuperclass();
 		
 			for (; superClazz != Object.class && superClazz != null; superClazz = superClazz
 					.getSuperclass()) {
 				if (!superClazz.isAnnotationPresent(MappedSuperclass.class))
 					continue;
 				List<Property> list = getProperties(superClazz, pList, true, log);
 				if (list != null)
 					superList.addAll(list);
 			}
 		
 			List<Property> properties = new ArrayList<Property>(superList);
 			properties.addAll(pList);
 	
 			ORMConfigBean ormBean = new ORMConfigBean();
 			ormBean.setClazz(clazz.getName());
 			ormBean.setId(clazz.getSimpleName());
 			ormBean.setTable(table);
 			ormBean.setProperty(properties);
 			ORMConfigBeanCache.add(clazz.getName(), ormBean);
 		} catch (Error er) {
 			log.warn("the entity class new instance failued -> " + clsName + " | " + er.toString());
 			return false;
 		} catch (Exception e) {
 			log.warn("the entity class new instance failued -> " + clsName + " | " + e.toString());
 			return false;
 		}
 		
 		return true;
 	}
 
 	private static boolean hasIdProperty(List<Property> list) {
 		for (Property p : list) {
 			if ("1".equals(p.getAutoIncrement()) && "1".equals(p.getPk())) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	private static List<Property> getProperties(Class<?> clazz, final List<Property> pList, final boolean requireSuper, Log log) throws Exception {
 		List<Property> result = new ArrayList<Property>();
 		ReflectUtil ru;
 		try {
 			ru = new ReflectUtil(clazz);
 			ru.setRequiredSuper(requireSuper);
 		} catch (Error e) {
 			log.warn(e.toString());
 			throw e;
 		} catch (Exception e) {
 			log.warn(e.toString());
 			throw e;
 		}
 
 		for (Field f : ru.getFields()) {
 			if (Collection.class.isAssignableFrom(f.getType()))
 				continue;
 			
 			String name = f.getName();
 			Method getter = ru.getGetter(name);
 			if (getter == null)
 				continue;
 
 			Ignore igAnn = f.getAnnotation(Ignore.class);
 			if (igAnn == null)
 				igAnn = getter.getAnnotation(Ignore.class);
 			
 			if (igAnn != null)
 				continue;
 
 			Transient trans = f.getAnnotation(Transient.class);
 			if (trans == null)
 				trans = getter.getAnnotation(Transient.class);
 			
 			if (trans != null)
 				continue;
 			
 			OneToMany manyAnn = getter.getAnnotation(OneToMany.class);
 			if (manyAnn != null)
 				continue;
 			else {
 				manyAnn = f.getAnnotation(OneToMany.class);
 				if (manyAnn != null)
 					continue;
 			}
 
 			ManyToMany manyManyAnn = getter.getAnnotation(ManyToMany.class);
 			if (manyManyAnn != null)
 				continue;
 			else {
 				manyManyAnn = f.getAnnotation(ManyToMany.class);
 				if (manyManyAnn != null)
 					continue;
 			}
 
 			Property p = new Property();
 
 			if (Long.class.isAssignableFrom(f.getType())
 					|| long.class.isAssignableFrom(f.getType()))
 				p.setSize("20");
 			else if (Integer.class.isAssignableFrom(f.getType())
 					|| int.class.isAssignableFrom(f.getType()))
 				p.setSize("4");
 			else if (String.class.isAssignableFrom(f.getType()))
 				p.setSize("255");
 			else if (Boolean.class.isAssignableFrom(f.getType())
 					|| boolean.class.isAssignableFrom(f.getType()))
 				p.setSize("");
 			else if (Float.class.isAssignableFrom(f.getType())
 					|| float.class.isAssignableFrom(f.getType()))
 				p.setSize("8");
 
 			Id idAnn = getter.getAnnotation(Id.class);
 			if (idAnn == null)
 				idAnn = f.getAnnotation(Id.class);
 
 			if (idAnn != null) {
 				if (pList != null && hasIdProperty(pList))
 					continue;
 
 				p.setAutoIncrement("1");
 				p.setPk("1");
 				p.setSize("20");
 			}
 
 			Column colAnn = getter.getAnnotation(Column.class);
 			if (colAnn == null) {
 				colAnn = f.getAnnotation(Column.class);
 			}
 
 			String column = colAnn == null ? "" : colAnn.name();
 			column = "".equals(column.trim()) ? name : column;
 			p.setName(name);
 			p.setColumn(column);
 			p.setType(f.getType().getName());
 			p.setNotNull("false");
 			if (colAnn != null) {
 				// int size = colAnn.length();
 				p.setNotNull(String.valueOf(!colAnn.nullable()));
 				p.setUnique(String.valueOf(colAnn.unique()));
 			}
 
 			if (ClassUtil.isPojo(f.getType())) {
 				OneToOne oneAnn = getter.getAnnotation(OneToOne.class);
 				if (oneAnn == null)
 					oneAnn = f.getAnnotation(OneToOne.class);
 				
 				ManyToOne manyToOneAnn = null;
 				if (oneAnn == null){
 					manyToOneAnn = getter.getAnnotation(ManyToOne.class);
 					if (manyToOneAnn == null)
 						manyToOneAnn = f.getAnnotation(ManyToOne.class);
 				}
 				
 				if (oneAnn != null || manyToOneAnn != null) {
 					if (oneAnn != null)
 						p.setType(PropType.ONE_ONE);
 					else
 						p.setType(PropType.MANY_ONE);
 					
 					JoinColumn joinColumn = getter.getAnnotation(JoinColumn.class);
 					if (joinColumn == null)
 						joinColumn = f.getAnnotation(JoinColumn.class);
 
 					if (joinColumn != null && joinColumn.name().trim().length() > 0) 
 						p.setColumn(joinColumn.name());
 					 else 
 						p.setColumn(f.getName() + "_id");
 					
					p.setRelProperty(null);
 					String refCol = null;
 					if (joinColumn != null && joinColumn.referencedColumnName().trim().length() > 0){
 						refCol = joinColumn.referencedColumnName();
 						if (refCol != null && refCol.trim().length() > 0){
 							String relField = ORMConfigBeanUtil.getField(f.getType(), refCol);
 							if (relField != null && relField.trim().length() > 0)
 								p.setRelProperty(relField);
 						}
 					}
 					
 					p.setRelClass(f.getType());
 					p.setSize("20");
 				}
 
 			}
 
 			result.add(p);
 		}
 
 		return result;
 	}
 
 	private static Class<?> getClass(String clsName) {
 		Class<?> clazz = null;
 
 		try {
 			clazz = Class.forName(clsName);
 		} catch (Error e) {
 			return null;
 		} catch (Exception e) {
 			return null;
 		} catch (Throwable e) {
 			return null;
 		}
 		
 		return clazz;
 	}
 
 	@Override
 	protected void onOk() throws Exception {
 		for (Iterator<Entry<Object, ORMConfigBean>> it = ORMConfigBeanCache.entrySet().iterator(); it.hasNext(); ){
 			Entry<Object, ORMConfigBean> e = it.next();
 			ORMConfigBean orm = e.getValue();
 			Class<?> clazz = null;
 			ReflectUtil ru = null;
 			for (Property p : orm.getProperty()){
 				String type = p.getType();
 				if (!PropType.ONE_ONE.equals(type) && !PropType.MANY_ONE.equals(type))
 					continue;
 				
				if (p.getRelProperty() != null && p.getRelProperty().trim().length() > 0)
 					continue;
 				
 				if (clazz == null)
 					clazz = Class.forName(orm.getClazz());
 				if (ru == null)
 					ru = new ReflectUtil(clazz);
 				
 				Field f = ru.getField(p.getName());
 				String refCol = ORMConfigBeanUtil.getIdColumn(f.getType());
 				
 				p.setRelProperty(ORMConfigBeanUtil.getField(f.getType(), refCol));
 			}
 		}
 	}
 }
