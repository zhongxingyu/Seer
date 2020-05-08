 package org.eweb4j.orm.config;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
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
 import org.eweb4j.config.LogFactory;
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
 		super(LogFactory.getMVCLogger(PojoAnnotationConfig.class));
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
 			ORMConfigBeanCache.add(clazz, ormBean);
 		} catch (Error er) {
 			log.warn("the action class new instance failued -> " + clsName + " | " + er.toString());
 			return false;
 		} catch (Exception e) {
 			log.warn("the action class new instance failued -> " + clsName + " | " + e.toString());
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
 			p.setNotNull("true");
 			if (colAnn != null) {
 				// int size = colAnn.length();
 				p.setNotNull(String.valueOf(colAnn.nullable()));
 				p.setUnique(String.valueOf(colAnn.unique()));
 			}
 
 			if (ClassUtil.isPojo(f.getType())) {
 				OneToOne oneAnn = getter.getAnnotation(OneToOne.class);
 				if (oneAnn == null)
 					oneAnn = f.getAnnotation(OneToOne.class);
 
 				if (oneAnn != null) {
 					JoinColumn joinColumn = getter
 							.getAnnotation(JoinColumn.class);
 					if (joinColumn == null)
 						joinColumn = f.getAnnotation(JoinColumn.class);
 
 					if (joinColumn == null) {
 						p.setColumn(f.getName() + "_id");
 					} else {
 						if (joinColumn.name().trim().length() == 0) {
 							String refCol = joinColumn.referencedColumnName();
 							if (refCol == null || refCol.trim().length() == 0)
 								p.setColumn(f.getName() + "_id");
 							else
 								p.setColumn(f.getName() + "_" + refCol);
 						} else
 							p.setColumn(joinColumn.name());
 					}
 					String relProperty = oneAnn.mappedBy();
 					if (relProperty == null || relProperty.trim().length() == 0)
 						relProperty = ORMConfigBeanUtil.getIdField(f.getType());
 
 					p.setRelProperty(relProperty);
 					p.setRelClass(f.getType());
 					p.setType(PropType.ONE_ONE);
 					p.setSize("20");
 				}
 
 				ManyToOne manyOneAnn = getter.getAnnotation(ManyToOne.class);
 				if (manyOneAnn == null)
 					manyOneAnn = f.getAnnotation(ManyToOne.class);
 
 				if (manyOneAnn != null) {
 					ReflectUtil _ru;
 					try {
 						_ru = new ReflectUtil(f.getType());
 
 						for (Field _f : _ru.getFields()) {
 							if (!ClassUtil.isListClass(_f))
 								continue;
 
 							String _name = _f.getName();
 							Method _getter = ru.getGetter(_name);
 							if (getter == null)
 								continue;
 
 							OneToMany oneManyAnn = _getter
 									.getAnnotation(OneToMany.class);
 							if (oneManyAnn == null)
 								oneManyAnn = f.getAnnotation(OneToMany.class);
 
 							if (oneManyAnn == null)
 								continue;
 
 							Class<?> _targetClass = ClassUtil
 									.getGenericType(_f);
 							if (!clazz.getName().equals(_targetClass.getName()))
 								continue;
 
 							String relProperty = oneManyAnn.mappedBy();
 							if (relProperty == null
 									|| relProperty.trim().length() == 0)
 								relProperty = ORMConfigBeanUtil.getIdField(_f
 										.getType());
 
 							p.setRelProperty(relProperty);
 
 							break;
 						}
 					} catch (Exception e) {
 					}
 
 					p.setRelClass(f.getType());
 					p.setType(PropType.MANY_ONE);
 					p.setSize("20");
 					JoinColumn col = getter.getAnnotation(JoinColumn.class);
 					if (col == null)
 						col = f.getAnnotation(JoinColumn.class);
 
 					if (col == null) {
 						p.setColumn(f.getName() + "_id");
 					} else {
 						if (col.name().trim().length() == 0) {
 							String refCol = col.referencedColumnName();
 							if (refCol == null || refCol.trim().length() == 0)
 								p.setColumn(f.getName() + "_id");
 							else
 								p.setColumn(f.getName() + "_" + refCol);
 						} else
 							p.setColumn(col.name());
 					}
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
 		}
 		return clazz;
 	}
 }
