 package lab.meteor.core;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import lab.meteor.core.MCollection.Factory;
 import lab.meteor.core.MDBAdapter.DBInfo;
 import lab.meteor.core.MReference.Multiplicity;
 
 /**
  * The object.
  * @author Qiang
  *
  */
 public class MObject extends MElement implements MNotifiable {
 	
 	private Map<Long, Object> values = null;
 	
 	MElementPointer class_pt = new MElementPointer();
 	
 	public MObject(MClass clazz) throws MException {
 		super(MElementType.Object);
 		
 		if (clazz == null)
 			throw new MException(MException.Reason.NULL_ELEMENT);
 		if (clazz.isDeleted())
 			throw new MException(MException.Reason.ELEMENT_MISSED);
 		
 		this.initialize();
 		this.class_pt.setPointer(clazz);
 
 		MDatabase.getDB().createElement(this);
 	}
 	
 	/**
 	 * Create a "lazy" object element with id.
 	 * @param id ID of element.
 	 */
 	public MObject(long id) {
 		super(id, MElementType.Object);
 	}
 	
 	public MClass getClazz() {
 		if (isDeleted())
 			return null;
 		load();
 		MClass cls = (MClass) class_pt.getElement();
 		if (cls == null)
 			throw new MException(MException.Reason.ELEMENT_MISSED);
 		return cls;
 	}
 	
 	public boolean isInstanceOf(MClass clazz) throws MException {
 		if (isDeleted())
 			return false;
 		load();
 		
 		if (this.class_pt.getID() == clazz.getID())
 			return true;
 		else {
 			MClass cls = (MClass) class_pt.getElement();
 			if (cls == null)
 				throw new MException(MException.Reason.ELEMENT_MISSED);
 			return cls.isKindOf(clazz);
 		}
 	}
 	
 	public Object get(String name) {
 		if (isDeleted())
 			return null;
 		MClass cls = getClazz();
 		MProperty p = cls.getProperty(name);
 		if (p == null)
 			return null;
 		switch (p.getElementType()) {
 		case Attribute:
 			return this.getAttribute((MAttribute) p);
 		case Reference:
 			return this.getReference((MReference) p);
 		default:
 			return null;
 		}
 	}
 	
 	public void set(String name, Object o) {
 		if (isDeleted())
 			return;
 		MClass cls = getClazz();
 		MAttribute p = cls.getAttribute(name);
 		if (p == null)
 			return;
 		this.setAttribute((MAttribute) p, o);
 	}
 	
 	public void set(String name, MObject o) {
 		if (isDeleted())
 			return;
 		MClass cls = getClazz();
 		MProperty p = cls.getProperty(name);
 		if (p == null)
 			return;
 		if (p.getElementType() == MElementType.Reference) {
 			this.setReference(p.name, o);
 		}
 	}
 
 	public Object getAttribute(String name) {
 		if (isDeleted())
 			return null;
 		MClass cls = getClazz();
 		MAttribute atb = cls.getAttribute(name);
 		if (atb == null)
 			throw new MException(MException.Reason.ATTRIBUTE_NOT_FOUND);
 		
 		return this.getAttribute(atb);
 	}
 
 	public MObject getReference(String name) {
 		if (isDeleted())
 			return null;
 		MClass cls = getClazz();
 		MReference ref = cls.getReference(name);
 		if (ref == null)
 			throw new MException(MException.Reason.REFERENCE_NOT_FOUND);
 		
 		if (ref.getMultiplicity() == Multiplicity.Multiple)
 			return null;
 		
 		Object o = this.getReference(ref);
 		return (MObject) o;
 	}
 
 	public MObjectSet getReferences(String name) {
 		if (isDeleted())
 			return null;
 		MClass cls = getClazz();
 		MReference ref = cls.getReference(name);
 		if (ref == null)
 			throw new MException(MException.Reason.REFERENCE_NOT_FOUND);
 		if (ref.getMultiplicity() == Multiplicity.One)
 			return null;
 		
 		Object o = this.getReference(ref);
 		MObjectSet set = (MObjectSet) o;
 		set.check();
 		return set;
 	}
 
 	public void setAttribute(String name, Object obj) {
 		if (isDeleted())
 			return;
 		MClass cls = getClazz();
 		MAttribute atb = cls.getAttribute(name);
 		if (atb == null)
 			throw new MException(MException.Reason.ATTRIBUTE_NOT_FOUND);
 		if (!MUtility.checkInputType(atb.getDataType(), obj))
 			throw new MException(MException.Reason.INVALID_VALUE_TYPE);
 		
 		this.setAttribute(atb, obj);
 	}
 
 	public void setReference(String name, MObject obj) {
 		if (isDeleted())
 			return;
 		if (obj != null && obj.isDeleted())
 			return;
 		MClass cls = getClazz();
 		MReference ref = cls.getReference(name);
 		if (ref == null)
 			throw new MException(MException.Reason.REFERENCE_NOT_FOUND);
 		if (ref.getMultiplicity() == Multiplicity.Multiple)
 			return;
 		if (!obj.isInstanceOf(ref.getOwner()))
 			throw new MException(MException.Reason.INVALID_VALUE_CLASS);
 		
 		oppositeWhenSet(ref, obj);
 		this.setReference(ref, obj);
 	}
 
 	public void addReference(String name, MObject obj) {
 		if (isDeleted())
 			return;
 		if (obj == null || obj.isDeleted())
 			return;
 		MClass cls = getClazz();
 		MReference ref = cls.getReference(name);
 		if (ref == null)
 			throw new MException(MException.Reason.REFERENCE_NOT_FOUND);
 		if (ref.getMultiplicity() == Multiplicity.One)
 			return;
 		if (!obj.isInstanceOf(ref.getReference()))
 			throw new MException(MException.Reason.INVALID_VALUE_CLASS);
 		
 		oppositeWhenAdd(ref, obj);
 		this.addReference(ref, obj);
 	}
 	
 	public void removeReference(String name, MObject obj) {
 		if (isDeleted())
 			return;
 		if (obj == null || obj.isDeleted())
 			return;
 		MClass cls = getClazz();
 		MReference ref = cls.getReference(name);
 		if (ref == null)
 			throw new MException(MException.Reason.REFERENCE_NOT_FOUND);
 		if (ref.getMultiplicity() == Multiplicity.One)
 			return;
 		if (!obj.isInstanceOf(ref.getReference()))
 			throw new MException(MException.Reason.INVALID_VALUE_CLASS);
 		
 		oppositeWhenRemove(ref, obj);
 		this.removeReference(ref, obj);
 	}
 
 	private void oppositeWhenAdd(MReference ref, MObject obj) {
 		load();
 		MReference ref_a = ref;
 		MReference ref_b = ref.getOpposite();
 		
 		// have to handle the opposite references
 		if (ref_b != null) {
 			// B One
 			if (ref_b.getMultiplicity() == Multiplicity.One) {
 				Object o = obj.getReference(ref_b);
 				if (o != null && o instanceof MElementPointer) {
 					MObject old_a = (MObject) ((MElementPointer) o).getElement();
 					// ref_a multiplicity is Multiple
 					// unlinke b and old_a
 					old_a.removeReference(ref_a, obj);
 				}
 				obj.setReference(ref_b, this);
 			} else { // B Multiple
 				obj.addReference(ref_b, this);
 			}
 		}
 	}
 	
 	private void oppositeWhenRemove(MReference ref, MObject obj) {
 		load();
 		MReference ref_b = ref.getOpposite();
 		
 		// have to handle the opposite references
 		if (ref_b != null) {
 			// B One
 			if (ref_b.getMultiplicity() == Multiplicity.One) {
 				obj.setReference(ref_b, null);
 			} else { // B Multiple
 				obj.removeReference(ref_b, this);
 			}
 		}
 	}
 	
 	private void oppositeWhenSet(MReference ref, MObject obj) {
 		load();
 		// one
 		MReference ref_a = ref;
 		// one or multiply
 		MReference ref_b = ref.getOpposite();
 		
 		// have to handle the opposite references
 		if (ref_b != null) {
 			// this old reference, because multiplicity is one, so it must be MObject
 			Object o = this.getReference(ref_a);
 			if (o != null && o instanceof MElementPointer) {
 				MObject old_b = (MObject) ((MElementPointer) o).getElement();
 				// if opposite reference is one.
 				if (ref_b.getMultiplicity() == Multiplicity.One) {
 					// unlink old_b and a
 					old_b.setReference(ref_b, null);
 				} else {
 					// delete a in old_b
 					old_b.removeReference(ref_b, this);
 				}
 			}
 			// obj old references
 			if (obj != null) {
 				// B One
 				if (ref_b.getMultiplicity() == Multiplicity.One) {
 					o = obj.getReference(ref_b);
 					if (o != null && o instanceof MElementPointer) {
 						MObject old_a = (MObject) ((MElementPointer) o).getElement();
 						// ref_a multiplicity is One
 						// unlink new_b and old_a
 						old_a.setReference(ref_a, null);
 					}
 					// link a and new_b
 					obj.setReference(ref_b, this);
 				} else { // B Multiple
 					// add a in new_b
 					obj.addReference(ref_b, this);
 				}
 			}
 		}
 	}
 
 	private Map<Long, Object> getValues() {
 		if (this.values == null)
 			this.values = new TreeMap<Long, Object>();
 		return this.values;
 	}
 	
 	private Object getAttribute(MAttribute atb) {
 		load();
 		Object o = this.getValues().get(atb.id);
 		if (o != null) {
 			if (!MUtility.checkOutputType(atb.getDataType(), o)) {
 				this.setAttribute(atb, null);
 			}
 		}
 		
 		MNativeDataType nType = atb.getDataType().getNativeDataType();
 		switch (nType) {
 		case List:
 			if (o == null) {
 				o = MCollection.createCollection(Factory.List, this, atb);
 				this.getValues().put(atb.id, o);
 			}
 			break;
 		case Set:
 			if (o == null) {
 				o = MCollection.createCollection(Factory.Set, this, atb);
 				this.getValues().put(atb.id, o);
 			}
 			break;
 		case Dictionary:
 			if (o == null) {
 				o = MCollection.createCollection(Factory.Dictionary, this, atb);
 				this.getValues().put(atb.id, o);
 			}
 			break;
 		case Enum:
 			if (o != null) {
 				o = ((MElementPointer) o).getElement();
 			}
 			break;
 		default:
 			break;
 		}
 		return o;
 	}
 	
 	private void setAttribute(MAttribute atb, Object obj) {
 		load();
 		Object o;
 		if (obj == null) {
 			o = obj;
 		} else if (obj instanceof MElement) {
 			o = new MElementPointer((MElement) obj);
 		} else if (obj instanceof MCollection.Factory) {
 			o =  MCollection.createCollection((MCollection.Factory) obj, this, atb);
 		} else {
 			o = obj;
 		}
 		this.getValues().put(atb.id, o);
 		this.setChanged(atb);
 	}
 	
 	private Object getReference(MReference ref) {
 		load();
 		Object value = this.getValues().get(ref.id);
 		if (ref.getMultiplicity() == Multiplicity.Multiple) {
 			if (value == null || !(value instanceof MObjectSet)) {
 				value = new MObjectSet(ref);
 				this.getValues().put(ref.id, value);
 			}
 		} else if (ref.getMultiplicity() == Multiplicity.One) {
 			if (value == null)
 				return null;
 			if (value instanceof MElementPointer) {
 				MObject mo = (MObject) ((MElementPointer) value).getElement();
 				if (mo == null) {
 					this.setReference(ref, null);
 				}
 				value = mo;
 			} else {
 				this.setReference(ref, null);
 				value = null;
 			}
 		}
 		return value;
 	}
 
 	private void addReference(MReference ref, MObject obj) {
 		load();
 		if (ref.getMultiplicity() == Multiplicity.One)
 			return;
 		MObjectSet set = (MObjectSet) this.getReference(ref);
 		set.pointers.add(new MElementPointer(obj));
 		this.setChanged(ref);
 	}
 	
 	private void removeReference(MReference ref, MObject obj) {
 		load();
 		if (ref.getMultiplicity() == Multiplicity.One)
 			return;
 		MObjectSet set = (MObjectSet) this.getReference(ref);
 		set.pointers.remove(new MElementPointer(obj));
 		this.setChanged(ref);
 	}
 	
 	private void setReference(MReference ref, MObject obj) {
 		load();
 		if (ref.getMultiplicity() == Multiplicity.Multiple)
 			return;
 		if (obj == null)
 			this.getValues().remove(ref.id);
 		else
 			this.getValues().put(ref.id, obj);
 		this.setChanged(ref);
 	}
 	
 	long getClazzID() {
 		return this.class_pt.getID();
 	}
 
 	@Override
 	void loadFromDBInfo(DBInfo dbInfo) {
 		boolean changeFlag = false;
 		
 		MDBAdapter.ObjectDBInfo objDBInfo = (MDBAdapter.ObjectDBInfo) dbInfo;
 		if (dbInfo.isFlagged(ATTRIB_FLAG_CLASS))
 			this.class_pt = new MElementPointer(objDBInfo.class_id, MElementType.Class);
 		
 		Iterator<Map.Entry<String, Object>> it = objDBInfo.values.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry<String, Object> entry = it.next();
 			long id = MUtility.parseID(entry.getKey());
 			MElementType type = MDatabase.getDB().getElementType(id);
 			Object value = entry.getValue();
 			
 			// if attribute
 			if (type == MElementType.Attribute) {
 				MAttribute atb = MDatabase.getDB().getAttribute(id);
 				if (!MUtility.checkInputType(atb.getDataType(), value)) {
 					changeFlag = true;
 				}
 				fromDBObject(this, atb, value, id);
 			// if reference
 			} else if (type == MElementType.Reference) {
 				MReference ref = MDatabase.getDB().getReference(id);
 				// multiplicity one
 				if (value instanceof MElementPointer) {
 					if (ref.getMultiplicity() == Multiplicity.One) {
 						this.getValues().put(id, value);
 					} else {
 						changeFlag = true;
 					}
 				// multiplicity multiple
 				} else if (value instanceof MDBAdapter.DataSet) {
 					if (ref.getMultiplicity() == Multiplicity.Multiple) {
 						MObjectSet ps = new MObjectSet(ref);
 						MDBAdapter.DataSet ds = (MDBAdapter.DataSet) value;
 						for (Object o : ds) {
 							ps.pointers.add((MElementPointer) o);
 						}
 						this.getValues().put(id, ps);
 					} else {
 						changeFlag = true;
 					}
 				}
 			} else {
 				getRemovedProperties().add(id);
 				changeFlag = true;
 			}
 		}
 		
 		if (changeFlag)
 			this.setChanged(ATTRIB_FLAG_VALUES);
 	}
 	
 	@Override
 	void saveToDBInfo(DBInfo dbInfo) {
 		MDBAdapter.ObjectDBInfo objDBInfo = (MDBAdapter.ObjectDBInfo) dbInfo;
 		objDBInfo.id = this.id;
 		objDBInfo.class_id = this.class_pt.getID();
 		if (dbInfo.isFlagged(ATTRIB_FLAG_VALUES)) {
 			if (this.values != null) {
 				for (Long id : changedProperties) {
 					Object value = values.get(id);
 					if (value instanceof MObjectSet) {
 						MDBAdapter.DataSet ds = new MDBAdapter.DataSet();
 						for (MElementPointer pt : ((MObjectSet) value).pointers) {
 							ds.add(pt);
 						}
 						objDBInfo.values.put(MUtility.stringID(id), ds);
 					} else {
 						Object o = MCollection.toDBObject(value);
 						objDBInfo.values.put(MUtility.stringID(id), o);
 					}
 				}
 			}
 			if (this.removedProperties != null) {
 				for (Long id : this.removedProperties) {
 					objDBInfo.deleteKeys.put(MUtility.stringID(id), null);
 				}
 			}
 		}
 	}
 
 	private static void fromDBObject(MObject obj, MAttribute atb, Object value, Object key) {
 		if (value instanceof MDBAdapter.DataList) {
 			MList list = new MList(obj, atb);
 			MDBAdapter.DataList dl = (MDBAdapter.DataList) value;
 			for (Object o : dl) {
 				MCollection.fromDBObject(list, o, null);
 			}
 			value = list;
 		} else if (value instanceof MDBAdapter.DataSet) {
 			MSet set = new MSet(obj, atb);
 			MDBAdapter.DataSet ds = (MDBAdapter.DataSet) value;
 			for (Object o : ds) {
 				MCollection.fromDBObject(set, o, null);
 			}
 			value = set;
 		} else if (value instanceof MDBAdapter.DataDict) {
 			MDictionary dict = new MDictionary(obj, atb);
 			MDBAdapter.DataDict dd = (MDBAdapter.DataDict) value;
 			Iterator<Map.Entry<String, Object>> it = dd.entrySet().iterator();
 			while (it.hasNext()) {
 				Map.Entry<String, Object> entry = it.next();
 				String k = entry.getKey();
 				Object o = entry.getValue();
 				MCollection.fromDBObject(dict, o, k);
 			}
 			value = dict;
 		}
 		obj.getValues().put(atb.id, value);
 	}
 
 	public class MObjectSet implements Iterable<MObject> {
 		Set<MElementPointer> pointers = new TreeSet<MElementPointer>();
 		MElementPointer refPointer;
 		
 		private MObjectSet(MReference ref) {
 			refPointer = new MElementPointer(ref);
 		}
 		
 		void changed() {
 			setChanged(refPointer);
 		}
 		
 		@Override
 		public Iterator<MObject> iterator() {
 			return new Itr();
 		}
 		
 		public void add(MObject o) {
 			if (isDeleted())
 				return;
 			if (o == null || o.isDeleted())
 				return;
 			MReference r = (MReference)refPointer.getElement();
 			oppositeWhenAdd(r, o);
 			addReference(r, o);
 		}
 
 		public void remove(MObject o) {
 			if (isDeleted())
 				return;
 			if (o == null || o.isDeleted())
 				return;
 			MReference r = (MReference)refPointer.getElement();
 			oppositeWhenRemove(r, o);
 			removeReference(r, o);
 		}
 		
 		public void clear() {
 			if (isDeleted())
 				return;
 			MReference r = (MReference)refPointer.getElement();
 			for (MElementPointer ep : pointers) {
 				MObject o = (MObject) ep.getElement();
 				if (o != null && !o.isDeleted())
 					oppositeWhenRemove(r, o);
 			}
 			pointers.clear();
 			changed();
 		}
 		
 		public boolean contains(MObject o) {
 			if (isDeleted())
 				return false;
 			return pointers.contains(new MElementPointer(o));
 		}
 		
 		public boolean isEmpty() {
 			if (isDeleted())
 				return true;
 			return pointers.isEmpty();
 		}
 		
 		public int size() {
 			if (isDeleted())
 				return 0;
 			return pointers.size();
 		}
 		
 		public void check() {
 			boolean changeFlag = false;
 			Iterator<MElementPointer> it = pointers.iterator();
 			while (it.hasNext()) {
 				MElementPointer pt = it.next();
 				MObject mo = (MObject) pt.getElement();
 				// remove overdue pointer
 				if (mo == null || mo.isDeleted()) {
 					it.remove();
 					changeFlag = true;
 				}
 			}
 			if (changeFlag)
 				changed();
 		}
 		
 		private class Itr implements Iterator<MObject> {
 
 			final Iterator<MElementPointer> it = MObjectSet.this.pointers.iterator();
 			MElementPointer last;
 			@Override
 			public boolean hasNext() {
 				if (isDeleted())
 					return false;
 				return it.hasNext();
 			}
 
 			@Override
 			public MObject next() {
 				if (isDeleted())
 					return null;
 				last = it.next();
 				return (MObject) last.getElement();
 			}
 
 			@Override
 			public void remove() {
 				if (isDeleted())
 					return;
 				MReference r = (MReference)refPointer.getElement();
 				MObject o = (MObject) last.getElement();
 				oppositeWhenRemove(r, o);
 				it.remove();
 				changed();
 			}
 			
 		}
 	}
 	
 	@Override
 	public String details() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(this.getClazz().toString()).append("(").append(this.id).append(")\n");
 		for (Long id : this.getValues().keySet()) {
 			MElementType type = MDatabase.getDB().getElementType(id);
 			sb.append("  ");
 			if (type == MElementType.Attribute) {
 				MAttribute a = MDatabase.getDB().getAttribute(id);
 				sb.append(a.name).append(" : ");
 				Object v = this.values.get(id);
 				if (a.getDataType().getNativeDataType() == MNativeDataType.Enum) {
 					sb.append(((MElementPointer) v).getElement().toString()).append("\n");
 				} else {
 					sb.append(v.toString()).append("\n");
 				}
 			} else {
 				MReference r = MDatabase.getDB().getReference(id);
 				if (r.getMultiplicity() == Multiplicity.Multiple) {
 					MObjectSet set = (MObjectSet)this.values.get(r.id);
					sb.append("{\n");
 					for (MElementPointer pt : set.pointers) {
 						sb.append("    ").append(pt.getElement().toString()).append("\n");
 					}
 					sb.append("  }\n");
 				} else {
 					MElementPointer pt = (MElementPointer)this.values.get(r.id);
 					sb.append(pt.getElement().toString()).append("\n");
 				}
 			}
 		}
 		return sb.toString();
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append(this.getClazz().toString()).append("(").append(this.id).append(")");
 		return sb.toString();
 	}
 	
 	Set<Long> changedProperties = new TreeSet<Long>();
 	private Set<Long> removedProperties;
 	
 	private Set<Long> getRemovedProperties() {
 		if (removedProperties == null)
 			removedProperties = new TreeSet<Long>();
 		return removedProperties;
 	}
 	
 	public void setChanged(MElementPointer property) {
 		changedProperties.add(property.getID());
 		setChanged(ATTRIB_FLAG_VALUES);
 	}
 	
 	void setChanged(MProperty p) {
 		changedProperties.add(p.id);
 		setChanged(ATTRIB_FLAG_VALUES);
 	}
 	
 	void clearChange() {
 		changedProperties.clear();
 	}
 	
 	@Override
 	public void save(int flag) {
 		super.save(flag);
 		if ((changed_flag & ATTRIB_FLAG_VALUES) == 0)
 			clearChange();
 	}
 	
 	public static final int ATTRIB_FLAG_CLASS = 0x00000001;
 	public static final int ATTRIB_FLAG_VALUES = 0x00000002;
 
 }
