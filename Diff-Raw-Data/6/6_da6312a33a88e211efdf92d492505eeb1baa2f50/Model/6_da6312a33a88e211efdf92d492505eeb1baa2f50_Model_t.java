 package com.ecolemo.jangorm;
 
 import java.lang.reflect.Field;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Set;
 
 import com.ecolemo.jangorm.manager.ModelManager;
 import com.ecolemo.jangorm.util.DataMap;
 
 
 public class Model extends DataMap {
 
 	public static <T extends Model> QuerySet<T> objects(Class<T> model) {
 		return new QuerySet<T>(model);
 	}
 
 	protected boolean loadedFromStorage = false;
 	public static SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
 	
 	public <T extends Model> T set(String key, Object value) {
 		data.put(key, value);
 		
 		try {
 			Field field = getClass().getDeclaredField(key);
 			field.setAccessible(true);
 			if (field.getType().getName().equals("boolean")) {
 				field.set(this, value != Integer.valueOf(0));
				data.put(key,  value != Integer.valueOf(0));
 			} else if (field.getType().equals(Date.class) && value instanceof String) {
				Date date = dbDateFormat.parse((String)value);
				field.set(this, date);
				data.put(key,  date);
 			} else {
 				field.set(this, value);
 			}
 		} catch (NoSuchFieldException e) {
 		} catch (IllegalArgumentException e) {
 		} catch (IllegalAccessException e) {
 			throw new ModelException(e);
 		} catch (ParseException e) {
 			throw new ModelException(e);
 		}
 		
 		return (T) this;
 	}
 
 	public <F extends Model> ForeignObjectSet<F> foreignObjects(Class<F> model) {
 		return new ForeignObjectSet<F>(this, model);
 	}
 	
 	public Object get(String key) {
 		try {
 			Field field = getClass().getField(key);
 			return field.get(this);
 		} catch (NoSuchFieldException e) {
 			return data.get(key);
 		} catch (IllegalArgumentException e) {
 			throw new ModelException(e);
 		} catch (IllegalAccessException e) {
 			throw new ModelException(e);
 		}
 	}
 	
 	public boolean has(String key) {
 		return data.containsKey(key);
 	}
 	
 	public Set<String> keySet() {
 		return data.keySet();
 	}
 	
 	public void remove(String key) {
 		data.remove(key);
 	}
 
 	public boolean getBoolean(String key) {
 		if (data.get(key) == null) return false;
 		if (data.get(key) instanceof Integer) return (Integer) data.get(key) != 0;
 		return (Boolean) data.get(key);
 	}
 	
 	public int getInt(String key) {
 		return (Integer) data.get(key);
 	}
 	
 	public String getString(String key) {
 		return (String) data.get(key);
 	}
 	
 	@Override
 	public String toString() {
 		return getClass().getSimpleName() + ":" + data.toString();
 	}
 
 	public Model getModel(String key) {
 		return (Model) data.get(key);
 	}
 	
 	public void save() {
 		System.out.println("SAVE: " + existsInStorage() + " => " + this);
 		ModelManager manager = ModelManager.getDefaultModelManager();
 		if (existsInStorage()) {
 			manager.updateModel(this);
 		} else {
 			manager.insertModel(this);
 		}
 	}
 	
 	public void delete() {
 		if (existsInStorage()) {
 			Model.objects(getClass()).filter("id=?", get("id")).delete();
 		}
 	}
 	
 	public boolean existsInStorage() {
 		return data.containsKey("id");
 	}
 	
 	public static class CurrentClassGetter extends SecurityManager {
 	    public Class getCurrentClass() {
 	        return getClassContext()[1]; 
 	    }
 	}	
 }
