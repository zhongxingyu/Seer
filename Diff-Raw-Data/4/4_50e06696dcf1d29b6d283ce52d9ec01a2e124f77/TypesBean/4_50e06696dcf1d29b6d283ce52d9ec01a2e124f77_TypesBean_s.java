 package org.genericsystem.myadmin.beans;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.genericsystem.core.Cache;
 import org.genericsystem.core.Generic;
 import org.genericsystem.core.Structural;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Holder;
 import org.genericsystem.generic.Relation;
 import org.genericsystem.generic.Type;
 import org.genericsystem.myadmin.util.GsMessages;
 import org.genericsystem.myadmin.util.GsRedirect;
 import org.richfaces.event.DropEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Named
 @SessionScoped
 public class TypesBean implements Serializable {
 
 	private static final long serialVersionUID = 8042406937175946234L;
 
 	// TODO clean
 	private static Logger log = LoggerFactory.getLogger(TypesBean.class);
 
 	@Inject
 	private transient Cache cache;
 
 	@Inject
 	private GsMessages messages;
 
 	@Inject
 	private GsRedirect redirect;
 
 	@Inject
 	private GenericTreeBean genericTreeBean;
 
 	private boolean readPhantoms;
 
 	@PostConstruct
 	public void init() {
 		// TODO TEST
 		Type vehicle = cache.newType("Vehicle");
 		Type car = vehicle.newSubType("Car");
 		Type color = cache.newType("Color");
 		Type time = cache.newType("Time");
 		Attribute power = vehicle.setAttribute("power");
 		Relation vehicleColor = vehicle.setRelation("vehicleColor", color);
 		Relation vehicleColorTime = vehicle.setRelation("vehicleColorTime", color, time);
 		Generic myVehicle = vehicle.newInstance("myVehicle");
 		Generic red = color.newInstance("red");
 		Generic yellow = color.newInstance("yellow");
 		vehicle.setValue(power, 1);
 		car.setValue(power, 2);
 		// myVehicle.setValue(power, 136);
 		myVehicle.setLink(vehicleColor, "myVehicleRed", red);
 		myVehicle.bind(vehicleColorTime, red, time.newInstance("myTime"));
 		vehicle.bind(vehicleColor, yellow);
 		car.newInstance("myCar");
 
 		Type human = cache.newType("Human");
 		Generic nicolas = human.newInstance("Nicolas");
 		Generic michael = human.newInstance("Michael");
 		Generic quentin = human.newInstance("Quentin");
 		Relation isTallerOrEqualThan = human.setRelation("isTallerOrEqualThan", human);
 		nicolas.bind(isTallerOrEqualThan, michael);
 		nicolas.bind(isTallerOrEqualThan, nicolas);
 		Relation isBrotherOf = human.setRelation("isBrotherOf", human);
 		isBrotherOf.enableMultiDirectional();
 		// quentin.bind(isBrotherOf, michael);
 		quentin.setLink(isBrotherOf, "link", michael);
 		Relation isBossOf = human.setRelation("isBossOf", human);
 		nicolas.bind(isBossOf, michael);
 
 		michael.getProperties().put("KEY TEST", "VALUE TEST");
 	}
 
 	public boolean isReadPhantoms() {
 		return readPhantoms;
 	}
 
 	public void setReadPhantoms(boolean readPhantoms) {
 		this.readPhantoms = readPhantoms;
 	}
 
 	public void newType(String newValue) {
 		cache.newType(newValue);
 		messages.info("createRootType", newValue);
 	}
 
 	public void newSubType(String newValue) {
 		((Type) genericTreeBean.getSelectedTreeNodeGeneric()).newSubType(newValue);
 		messages.info("createSubType", newValue, genericTreeBean.getSelectedTreeNodeGeneric().getValue());
 	}
 
 	public void setAttribute(String newValue) {
 		((Type) genericTreeBean.getSelectedTreeNodeGeneric()).setAttribute(newValue);
 		messages.info("createRootAttribute", newValue, genericTreeBean.getSelectedTreeNodeGeneric().getValue());
 	}
 
 	public void addProperty(String key, String value) {
 		((Type) genericTreeBean.getSelectedTreeNodeGeneric()).getProperties().put(key, value);
 		messages.info("createRootProperty", key, value);
 	}
 
 	public void newInstance(String newValue) {
 		((Type) genericTreeBean.getSelectedTreeNodeGeneric()).newInstance(newValue);
 		messages.info("createRootInstance", newValue, genericTreeBean.getSelectedTreeNodeGeneric().getValue());
 	}
 
 	public List<Structural> getStructurals() {
 		return genericTreeBean.getSelectedTreeNodeGeneric().getStructurals();
 	}
 
 	public List<Holder> getHolders(Structural structural) {
 		return ((Type) genericTreeBean.getSelectedTreeNodeGeneric()).getHolders(structural.getAttribute(), structural.getPosition(), readPhantoms);
 	}
 
 	// TODO in GS core ?
 	public boolean isPhantom(Holder holder) {
 		return holder.getValue() == null;
 	}
 
 	public void removePhantoms(Attribute attribute) {
 		genericTreeBean.getSelectedTreeNodeGeneric().removePhantoms(attribute);
 		messages.info("phantomsRemoved", attribute);
 	}
 
 	public List<Generic> getOtherTargets(Holder holder) {
 		return genericTreeBean.getSelectedTreeNodeGeneric().getOtherTargets(holder);
 	}
 
 	public void addValue(Attribute attribute, String newValue) {
 		Generic currentInstance = genericTreeBean.getSelectedTreeNodeGeneric();
 		currentInstance.setValue(attribute, newValue);
 		messages.info("addValue", newValue, attribute, currentInstance);
 	}
 
 	public void removeHolder(Holder holder) {
 		genericTreeBean.getSelectedTreeNodeGeneric().removeHolder(holder);
 		messages.info("remove", holder);
 	}
 
 	public void removeAttribute(Attribute attribute) {
 		attribute.remove();
 		messages.info("remove", attribute);
 	}
 
 	// TODO call clearAll...
 	public String delete() {
 		Generic generic = genericTreeBean.getSelectedTreeNodeGeneric();
 		if (isValue(generic)) {
 			genericTreeBean.setSelectedTreeNode(genericTreeBean.getSelectedTreeNode().getParent());
 			removeHolder((Holder) generic);
 		} else {
 			generic.remove();
 			messages.info("deleteFile", generic.getValue());
 			genericTreeBean.setSelectedTreeNode(genericTreeBean.getSelectedTreeNode().getParent());
 		}
 		return "";
 	}
 
 	public void processDrop(DropEvent dropEvent) {
 		Object dragValue = dropEvent.getDragValue();
 		Type type = (Type) genericTreeBean.getSelectedTreeNodeGeneric();
 		Attribute attribute = type.setAttribute("new_attribute");
 		// if (!(dragValue instanceof GenericTreeNode)) {
 		if (dragValue.equals("int"))
 			attribute.setConstraintClass(Integer.class);
 		if (dragValue.equals("long"))
 			attribute.setConstraintClass(Long.class);
 		if (dragValue.equals("float"))
 			attribute.setConstraintClass(Float.class);
 		if (dragValue.equals("double"))
 			attribute.setConstraintClass(Double.class);
 		if (dragValue.equals("boolean"))
 			attribute.setConstraintClass(Boolean.class);
 		if (dragValue.equals("string"))
 			attribute.setConstraintClass(String.class);
 		// }
 		String msg = /* dragValue instanceof GenericTreeNode ? "" + ((GenericTreeNode) dragValue).getGeneric() : */(String) dragValue;
 		messages.info("dropValue", msg);
 	}
 
 	public void processDrop2(DropEvent dropEvent) {
 		Object dragValue = dropEvent.getDragValue();
 		// if (!(dragValue instanceof GenericTreeNode)) {
 		// log.info("Targets for relation cannot be simple type");
 		// return;
 		// }
 
 		Generic target = ((GenericTreeNode) dragValue).getGeneric();
 		if (target.isStructural()) {
 			Object dropValue = dropEvent.getDropValue();
 			Attribute attribute = ((Structural) dropValue).getAttribute();
 			attribute = attribute.addComponent(attribute.getComponentsSize(), target);
 			messages.info("targetRelation", target, attribute);
 		} else if (target.isConcrete()) {
 			Object dropValue = dropEvent.getDropValue();
 			Attribute attribute = ((Structural) dropValue).getAttribute();
 			if (attribute.getClass() == Relation.class) {
 				Generic base = genericTreeBean.getSelectedTreeNodeGeneric();
 				base.bind((Relation) attribute, target);
 				messages.info("targetLink", target, attribute);
 			} else {
 				messages.info("errorTargetLink");
 			}
 		}
 	}
 
 	public List<Entry<Serializable, Serializable>> getProperties() {
 		return (List) genericTreeBean.getSelectedTreeNodeGeneric().getProperties().entrySet();
 	}
 
 	public void removeProperty(Entry<Serializable, Serializable> entry) {
 		genericTreeBean.getSelectedTreeNodeGeneric().getProperties().remove(entry.getKey());
 		messages.info("remove", entry.getKey());
 	}
 
 	public PropertyWrapper getPropertyWrapper(Entry<Serializable, Serializable> entry) {
 		return new PropertyWrapper(entry);
 	}
 
 	public GenericWrapper getGenericWrapper(Generic generic) {
 		return new GenericWrapper(generic);
 	}
 
 	// TODO in GS CORE
 	public boolean isValue(Generic generic) {
 		return generic.isConcrete() && generic.isAttribute();
 	}
 
 	public boolean isSingular(Structural structural) {
 		return structural.getAttribute().isSingularConstraintEnabled();
 	}
 
 	public String getHolderStyle(Holder holder) {
 		return !holder.getBaseComponent().equals(genericTreeBean.getSelectedTreeNodeGeneric()) ? "italic" : (isPhantom(holder) ? "phantom" : "");
 	}
 
 	// TODO no more used
 	// public boolean isBaseComponent(Holder holder) {
 	// return holder.getBaseComponent().equals(genericTreeBean.getSelectedTreeNodeGeneric());
 	// }
 
 	public boolean hasValues(Attribute attribute) {
 		return !genericTreeBean.getSelectedTreeNodeGeneric().getValues(attribute).isEmpty();
 	}
 
 	public class GenericWrapper {
 		private Generic wrappedGeneric;
 
 		public GenericWrapper(Generic wrappedGeneric) {
 			this.wrappedGeneric = wrappedGeneric;
 		}
 
 		public String getValue() {
 			return wrappedGeneric.toString();
 		}
 
 		public void setValue(String newValue) {
 			if (!newValue.equals(wrappedGeneric.toString())) {
 				wrappedGeneric.updateKey(newValue);
 				messages.info("updateValue", wrappedGeneric, newValue);
 			}
 		}
 	}
 
 	// TODO no more used
 	// public class TargetWrapper {
 	// private Generic generic;
 	//
 	// private Holder holder;
 	//
 	// public TargetWrapper(Generic generic, Holder holder) {
 	// this.generic = generic;
 	// this.holder = holder;
 	// }
 	//
 	// public boolean isBaseComponent() {
 	// return holder.getBaseComponent().equals(generic);
 	// }
 	//
 	// public Generic getGeneric() {
 	// return generic;
 	// }
 	//
 	// public void setGeneric(Generic generic) {
 	// this.generic = generic;
 	// }
 	//
 	// public Holder getHolder() {
 	// return holder;
 	// }
 	//
 	// public void setHolder(Holder holder) {
 	// this.holder = holder;
 	// }
 	// }
 
 	public class PropertyWrapper {
 		private Entry<Serializable, Serializable> entry;
 
 		public PropertyWrapper(Entry<Serializable, Serializable> entry) {
 			this.entry = entry;
 		}
 
 		public String getValue() {
 			return (String) entry.getValue();
 		}
 
 		public void setValue(String newValue) {
 			if (!newValue.equals(entry.getValue().toString())) {
 				genericTreeBean.getSelectedTreeNodeGeneric().getProperties().put(entry.getKey(), newValue);
 				messages.info("updateValue", entry.getValue(), newValue);
 			}
 		}
 	}
 }
