 package org.genericsystem.myadmin.beans;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.el.MethodExpression;
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.event.Observes;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.genericsystem.cdi.CacheProvider;
 import org.genericsystem.core.Generic;
 import org.genericsystem.core.Snapshot;
 import org.genericsystem.core.Snapshot.Projector;
 import org.genericsystem.exception.NotRemovableException;
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Holder;
 import org.genericsystem.generic.Type;
 import org.genericsystem.myadmin.beans.MenuBean.MenuEvent;
 import org.genericsystem.myadmin.util.GsMessages;
 import org.jboss.seam.international.status.MessageFactory;
 import org.jboss.seam.international.status.builder.BundleKey;
 import org.richfaces.component.UIMenuGroup;
 import org.richfaces.component.UIMenuItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Named
 @SessionScoped
 public class GenericBean implements Serializable {
 
 	private static final long serialVersionUID = 2108715680116264876L;
 	private static final String MESSAGES_BUNDLE_NAME = "/bundles/messages";
 
 	protected static Logger log = LoggerFactory.getLogger(GenericBean.class);
 	private UIMenuGroup menuGroup;
 
 	/* Injected beans */
 	@Inject transient CacheProvider cacheProvider;
 	@Inject private GsMessages messages;
 	@Inject private GuiGenericsTreeBean genericTreeBean;
 	@Inject MessageFactory factory;
 
 	private List<StructuralWrapper> structuralWrappers = new ArrayList<>();
 
 	/**
 	 * Creates a new type in current cache.
 	 * 
 	 * @param typeName - the name of new type.
 	 */
 	public void newType(String typeName) {
 		cacheProvider.getCurrentCache().newType(typeName);
 		//genericTreeBean.rebuildTree();
 		genericTreeBean.getSelectedTreeNode().updateChildren();
 
 		messages.info("createRootType", typeName);
 	}
 
 	/**
 	 * Creates a new sub type of currently selected type generic.
 	 * 
 	 * @param subTypeName - the name of new sub type.
 	 */
 	public void newSubType(String subTypeName) {
 		((Type) genericTreeBean.getSelectedTreeNode().getGeneric()).newSubType(subTypeName);
 		genericTreeBean.updateTree();
 
		messages.info("createSubType", subTypeName, genericTreeBean.getSelectedTreeNode().getGeneric().getValue());
 	}
 
 	/**
 	 * Creates a new attribute on currently selected type generic.
 	 * 
 	 * @param attributeName - the name of attribute.
 	 */
 	public void setAttribute(String attributeName) {
 		((Type) genericTreeBean.getSelectedTreeNode().getGeneric()).setAttribute(attributeName);
 
 		messages.info("createRootAttribute", attributeName, genericTreeBean.getSelectedTreeNode().getGeneric().getValue());
 	}
 
 	/**
 	 * Creates a new property on currently selected type generic.
 	 * 
 	 * @param key - key of property.
 	 * @param value - value of property.
 	 */
 	public void addProperty(String key, String value) {
 		((Type) genericTreeBean.getSelectedTreeNode().getGeneric()).getPropertiesMap().put(key, value);
 
 		messages.info("createRootProperty", key, value);
 	}
 
 	public void addContraint(String key, String value) {
 		((Type) genericTreeBean.getSelectedTreeNode().getGeneric()).getConstraintsMap().put(key, value);
 
 		messages.info("createRootProperty", key, value);
 	}
 
 	public void addSystemProperty(String key, String value) {
 		((Type) genericTreeBean.getSelectedTreeNode().getGeneric()).getSystemPropertiesMap().put(key, value);
 
 		messages.info("createRootProperty", key, value);
 	}
 
 	public void newInstance(String instanceName) {
 		((Type) genericTreeBean.getSelectedTreeNode().getGeneric()).newInstance(instanceName);
 		genericTreeBean.updateTree();
 
 		messages.info("createRootInstance", instanceName, genericTreeBean.getSelectedTreeNode().getGeneric().getValue());
 	}
 
 	public void addValue(Attribute attribute, String value) {
 		Generic currentInstance = genericTreeBean.getSelectedTreeNode().getGeneric();
 		currentInstance.setValue(attribute, value);
 
 		messages.info("addValue", value, attribute, currentInstance);
 	}
 
 	public void remove(Holder holder) {
 		genericTreeBean.getSelectedTreeNode().getGeneric().removeHolder(holder);
 		genericTreeBean.updateTree();
 
 		messages.info("remove", holder);
 	}
 
 	public String delete() {
 		Generic generic = genericTreeBean.getSelectedTreeNode().getGeneric();
 		genericTreeBean.setSelectedTreeNode(genericTreeBean.getSelectedTreeNode().getParent());
 		generic.remove();
 		genericTreeBean.updateTree();
 
 		messages.info("remove", generic);
 		return "";
 	}
 
 	public void removeProperty(Entry<Serializable, Serializable> entry) {
 		removeEntry(genericTreeBean.getSelectedTreeNode().getGeneric().getPropertiesMap(), entry);
 	}
 
 	public void removeContraint(Entry<Serializable, Serializable> entry) {
 		removeEntry(genericTreeBean.getSelectedTreeNode().getGeneric().getConstraintsMap(), entry);
 
 	}
 
 	public void removeSystemProperty(Entry<Serializable, Serializable> entry) {
 		removeEntry(genericTreeBean.getSelectedTreeNode().getGeneric().getSystemPropertiesMap(), entry);
 	}
 
 	private void removeEntry(Map<Serializable, Serializable> map, Entry<Serializable, Serializable> entry) {
 		try {
 			map.remove(entry.getKey());
 			messages.info("remove", entry.getKey());
 		} catch (NotRemovableException e) {
 			messages.info("cannotremove", e.getMessage());
 		}
 	}
 
 	public Generic getGeneric() {
 		return genericTreeBean.getSelectedTreeNode().getGeneric();
 	}
 
 	public void initMenuGroup(@Observes MenuEvent menuEvent) {
 		menuGroup.getChildren().clear();
 		FacesContext facesContext = FacesContext.getCurrentInstance();
 		int i = 0;
 		for (Generic generic : ((Type) getGeneric()).getAttributes()) {
 			UIMenuItem uiMenuItem = (UIMenuItem) facesContext.getApplication().createComponent(UIMenuItem.COMPONENT_TYPE);
 			uiMenuItem.setLabel(factory.info(new BundleKey(MESSAGES_BUNDLE_NAME, "itmShowValuesOf"), generic.toString()).build().getText());
 			MethodExpression methodExpression = facesContext.getApplication().getExpressionFactory().createMethodExpression(
 					facesContext.getELContext(),
 					"#{guiGenericsTreeBean.changeAttributeSelected(" + i + ")}",
 					void.class,
 					new Class<?>[] { Integer.class });
 			uiMenuItem.setActionExpression(methodExpression);
 			uiMenuItem.setRender("typestree, typestreetitle, editTypesManager");
 			menuGroup.getChildren().add(uiMenuItem);
 			i++;
 		}
 	}
 
 	public List<StructuralWrapper> getStructuralWrappers() {
 		List<StructuralWrapper> list = new ArrayList<>();
 		for (Structural structural : listStructurals((Attribute) genericTreeBean.getSelectedTreeNode().getGeneric())) {
 			list.add(getStructuralWrapper(structural));
 		}
 		structuralWrappers = list;
 		return list;
 	}
 
 	Snapshot<Structural> listStructurals(final Attribute generic) {
 		return generic.getAttributes().project(new Projector<Structural, Attribute>() {
 			@Override
 			public Structural project(Attribute element) {
 				return new StructuralImpl(element, generic.getBasePos(element));
 			}
 		});
 	}
 
 	private StructuralWrapper getStructuralWrapper(Structural structural) {
 		for (StructuralWrapper old : structuralWrappers)
 			if (old.getStructural().equals(structural))
 				return old;
 		return new StructuralWrapper(structural);
 	}
 
 	public class StructuralWrapper {
 		private Structural structural;
 		private boolean readPhantoms;
 
 		public StructuralWrapper(Structural structural) {
 			this.structural = structural;
 		}
 
 		public Structural getStructural() {
 			return structural;
 		}
 
 		public void setStructural(Structural structural) {
 			this.structural = structural;
 		}
 
 		public boolean isReadPhantoms() {
 			return readPhantoms;
 		}
 
 		public void setReadPhantoms(boolean readPhantoms) {
 			this.readPhantoms = readPhantoms;
 		}
 	}
 
 	public List<Holder> getHolders(StructuralWrapper structuralWrapper) {
 		return ((Type) genericTreeBean.getSelectedTreeNode().getGeneric()).getHolders(structuralWrapper.getStructural().getAttribute(), structuralWrapper.getStructural().getPosition(), structuralWrapper.isReadPhantoms());
 	}
 
 	public List<Generic> getOtherTargets(Holder holder) {
 		return genericTreeBean.getSelectedTreeNode().getGeneric().getOtherTargets(holder);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Entry<Serializable, Serializable>> getPropertiesMap() {
 		return (List<Entry<Serializable, Serializable>>) genericTreeBean.getSelectedTreeNode().getGeneric().getPropertiesMap().entrySet();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Entry<Serializable, Serializable>> getContraintsMap() {
 		return (List<Entry<Serializable, Serializable>>) genericTreeBean.getSelectedTreeNode().getGeneric().getConstraintsMap().entrySet();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Entry<Serializable, Serializable>> getSystemPropertiesMap() {
 		return (List<Entry<Serializable, Serializable>>) genericTreeBean.getSelectedTreeNode().getGeneric().getSystemPropertiesMap().entrySet();
 	}
 
 	public boolean isSingular(Structural structural) {
 		return structural.getAttribute().isSingularConstraintEnabled();
 	}
 
 	public String getHolderStyle(Holder holder) {
 		return !holder.getBaseComponent().equals(genericTreeBean.getSelectedTreeNode().getGeneric()) ? "italic" : (isPhantom(holder) ? "phantom" : "");
 	}
 
 	public boolean hasValues(Attribute attribute) {
 		return !genericTreeBean.getSelectedTreeNode().getGeneric().getValues(attribute).isEmpty();
 	}
 
 	public boolean isPhantom(Holder holder) {
 		return holder.getValue() == null;
 	}
 
 	public UIMenuGroup getMenuGroup() {
 		return menuGroup;
 	}
 
 	public void setMenuGroup(UIMenuGroup menuGroup) {
 		this.menuGroup = menuGroup;
 	}
 
 }
