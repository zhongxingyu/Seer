 package com.bluesky.visualprogramming.core;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.bluesky.visualprogramming.core.serialization.dump.SerializationService;
 import com.bluesky.visualprogramming.core.serialization.dump.SerializerType;
 
 public class ObjectRepository {
 
 	static Logger logger = Logger.getLogger(ObjectRepository.class);
 
 	static String DEFAULT_VIEW_POSITION = "DEFAULT_VIEW_POSITION";
 
 	static String DUMP_FILE = "objects";
 	public static String ROOT = "_root";
 	public static String PROTOTYPE_PATH = ROOT + ".core.prototype";
 
 	long objectId;
 	Map<Long, _Object> objects = new HashMap<Long, _Object>();
 	_Object rootObject;
 
 	private List<ObjectRepositoryListener> listeners = new ArrayList<ObjectRepositoryListener>();
 
 	public ObjectRepository() {
 		// start from 0
 		objectId = 0;
 
 		rootObject = createObject(ObjectType.NORMAL, ObjectScope.Persistent);
 		rootObject.setName(ObjectRepository.ROOT);
 	}
 
 	private String generateNewChildName(_Object owner) {
 		String prefix = "unnamed";
 		String name = prefix;
 
 		int i = 0;
 		while (true) {
 			_Object child = owner.getChild(name);
 			if (child == null)
 				break;
 
 			if (i > 10000)
 				throw new RuntimeException("too many 'unnamed' child.");
 
 			i++;
 			name = prefix + i;
 		}
 
 		return name;
 	}
 
 	public _Object createObject(ObjectType type, ObjectScope scope) {
 		if (scope == null)
 			throw new RuntimeException("scope cannot be null.");
 
 		return _createObject(type, scope);
 	}
 
 	/**
 	 * create object without owner. mostly it belongs to runtime execution
 	 * context.
 	 * 
 	 * @param type
 	 * @param scope
 	 * @return
 	 */
 	private _Object _createObject(ObjectType type, ObjectScope scope) {
 
 		_Object newObject = type.create(objectId++);
 		newObject.setScope(scope);
 
 		// set prototype for value objects
 		String prototypeEl = type.getPrototypeEL();
 		if (prototypeEl != null) {
 
 			try {
 				_Object prototype = getObjectByPath(prototypeEl);
 				if (prototype != null)
 					newObject.setPrototype(prototype);
 
 			} catch (InvalidELException e) {
 
 				logger.warn(
 						"the prototype object is not loaded. if it is in loading process, then it is ok."
 								+ prototypeEl, e);
 			}
 		}
 
 		objects.put(newObject.getId(), newObject);
 
 		for (ObjectRepositoryListener l : listeners) {
 			l.afterCreate(newObject);
 		}
 
 		return newObject;
 	}
 
 	public _Object createObject(_Object owner, ObjectType type) {
 		return createObject(owner, generateNewChildName(owner), type, "");
 	}
 
 	public _Object createObject(_Object owner, String name, ObjectType type) {
 		return createObject(owner, name, type, "");
 	}
 
 	/**
 	 * create field for owner object
 	 * 
 	 * @param owner
 	 * @param name
 	 * @param type
 	 * @param value
 	 * @return
 	 */
 	private _Object createObject(_Object owner, String name, ObjectType type,
 			String value) {
 		// scope is null, means it obliges owner's value
 		_Object newObject = _createObject(type, ObjectScope.ExecutionContext);
 
 		newObject.setName(name);
 
 		// String value = type.extractValue(literal);
 		newObject.setValue(value);
 
 		if (owner == null)
 			throw new RuntimeException("owner must not be null");
 
 		owner.setField(name, newObject, true);
 
 		objects.put(newObject.getId(), newObject);
 
 		for (ObjectRepositoryListener l : listeners) {
 			l.afterCreate(newObject);
 		}
 
 		return newObject;
 	}
 
 	/**
 	 * e.g. root.abc.xyz
 	 * 
 	 * @param path
 	 * @return
 	 */
 	public _Object getObjectByPath(String path) {
 		String[] ss = path.split("\\.");
 
 		if (!ss[0].equals(ObjectRepository.ROOT))
 			throw new RuntimeException("the first object must be '"
 					+ ObjectRepository.ROOT + "':" + path);
 
 		int index = path.indexOf('.');
 		if (index < 0)
 			return getRootObject();
 		else {
 			String subPath = path.substring(index + 1);
 
 			return getRootObject().getObjectByPath(subPath);
 		}
 	}
 
 	/**
 	 * copy fields only. procedures are not copied. 'prototype' field point to
 	 * the prototype
 	 * 
 	 * @param src
 	 * @return
 	 */
 	public _Object clone(_Object src) {
 		return new _Object(objectId++, src);
 	}
 
 	public void destroyObject(long id) {
 		_Object obj = objects.get(id);
 		if (obj == null)
 			throw new RuntimeException("delete object, object id not found");
 
 		destroyObject(obj);
 	}
 
 	public void destroyObject(_Object obj) {
 		if (obj.getOwner() != null) {
 			obj.getOwner().removeChild(obj);
 			obj.setOwner(null);
 		}
 
 		objects.remove(obj);
 	}
 
 	public _Object getRootObject() {
 		return rootObject;
 	}
 
 	public void save(String fileName) {
 
 		try {
			Writer w = new OutputStreamWriter(new FileOutputStream(fileName));
 			saveXml(w);
 
		} catch (FileNotFoundException e) {
 
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void saveXml(Writer writer) {
 		SerializationService svc = new SerializationService();
 		try {
 			// update target object ID
 			_Object root = getRootObject();
 
 			svc.serialize(getRootObject(), SerializerType.Xml, true, writer);
 			writer.flush();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void loadXml(String fileName) {
 		SerializationService svc = new SerializationService();
 		try {
			Reader r = new InputStreamReader(new FileInputStream(fileName));
 
 			_Object root = svc.deserialize(r, SerializerType.Xml);
 			rootObject = root;
 
 			registerObject(rootObject);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 
 		afterProcessOfLoadingXml();
 	}
 
 	private void registerObject(_Object obj) {
 		objects.put(obj.getId(), obj);
 		if (obj.hasChildren()) {
 			for (Field f : obj.getFields()) {
 				if (obj.owns(f.target))
 					registerObject(f.target);
 			}
 		}
 	}
 
 	private void afterProcessOfLoadingXml() {
 
 		long maxObjectId = 0;
 
 		for (_Object o : objects.values()) {
 			if (o.getId() > maxObjectId)
 				maxObjectId = o.getId();
 
 			o.recreateFieldIndexes();
 		}
 
 		// reset object id
 		objectId = maxObjectId + 1;
 		logger.info("objects loaded");
 
 		ObjectRepositoryListener migrationListener = new ObjectRepositoryListener() {
 			@Override
 			public void beforeSave(_Object obj) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void beforeDestroy(_Object obj) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void afterLoadFromFile(_Object obj) {
 
 				for (int i = 0; i < obj.getFields().size(); i++) {
 					Field f = obj.getField(i);
 
 					// f.svgScale=0.2f;
 
 				}
 			}
 
 			@Override
 			public void afterCreate(_Object obj) {
 
 			}
 
 			@Override
 			public void afterAllLoaded() {
 				// TODO Auto-generated method stub
 
 			}
 		};
 
 		// this listener is used to modify object data in batch for migration
 		// purpose. enable it only when necessary.
 		// listeners.add(migrationListener);
 
 		// notify
 		for (_Object o : objects.values()) {
 			for (ObjectRepositoryListener l : listeners)
 				l.afterLoadFromFile(o);
 		}
 
 	}
 
 	public Collection<_Object> getAllObjects() {
 		return objects.values();
 	}
 
 	public void addListener(ObjectRepositoryListener listener) {
 		listeners.add(listener);
 	}
 
 	public void removeListener(ObjectRepositoryListener listener) {
 		listeners.remove(listener);
 	}
 }
