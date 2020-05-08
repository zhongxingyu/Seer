 /*
  *
  * Copyright 2007 Luca Molino (luca.molino--AT--assetdata.it)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.romaframework.module.users.view.domain.portal;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.romaframework.aspect.core.annotation.AnnotationConstants;
 import org.romaframework.aspect.persistence.PersistenceAspect;
 import org.romaframework.aspect.persistence.PersistenceConstants;
 import org.romaframework.aspect.persistence.annotation.Persistence;
 import org.romaframework.aspect.view.ViewCallback;
 import org.romaframework.aspect.view.ViewConstants;
 import org.romaframework.aspect.view.annotation.ViewField;
 import org.romaframework.aspect.view.feature.ViewActionFeatures;
 import org.romaframework.aspect.view.feature.ViewFieldFeatures;
 import org.romaframework.aspect.view.portal.PortalPage;
 import org.romaframework.aspect.view.portal.PortalPageContainer;
 import org.romaframework.core.Roma;
 import org.romaframework.core.schema.SchemaClassResolver;
 import org.romaframework.frontend.domain.entity.ComposedEntityInstance;
 import org.romaframework.frontend.domain.message.MessageOk;
 import org.romaframework.module.users.domain.portal.PortalPreferences;
 import org.romaframework.module.users.domain.portal.PortletList;
 
 /**
  * Class that rapresents the portlet configuration page
  * 
  * @author l.molino
  * 
  */
 public class PortletPreferencesConfiguration extends ComposedEntityInstance<PortalPreferences> implements ViewCallback {
 
 	@ViewField(visible = AnnotationConstants.FALSE)
 	protected String							containerSelected;
 
 	@ViewField(visible = AnnotationConstants.FALSE)
 	protected String							selectedPortletSelected;
 
 	@ViewField(visible = AnnotationConstants.FALSE)
 	protected String							availablePortletSelected;
 
 	@ViewField(render = ViewConstants.RENDER_LIST, selectionField = "containerSelected", enabled = AnnotationConstants.FALSE)
 	protected List<String>				containers;
 
 	@ViewField(render = ViewConstants.RENDER_LIST, selectionField = "availablePortletSelected", enabled = AnnotationConstants.FALSE)
 	protected List<String>				availablePortlets;
 
 	@ViewField(render = ViewConstants.RENDER_LIST, selectionField = "selectedPortletSelected", enabled = AnnotationConstants.FALSE)
 	protected List<String>				selectedPortlets;
 
 	@ViewField(visible = AnnotationConstants.FALSE)
 	protected Map<String, String>	classNamesMapper;
 
 	public PortletPreferencesConfiguration(PortalPreferences iEntity) {
 		super(iEntity);
 		availablePortlets = new ArrayList<String>();
 		selectedPortlets = new ArrayList<String>();
 		containers = new ArrayList<String>();
 		classNamesMapper = new HashMap<String, String>();
 	}
 
 	public List<String> getContainers() {
 		return containers;
 	}
 
 	public String getContainerSelected() {
 		return containerSelected;
 	}
 
 	public List<String> getAvailablePortlets() {
 		return availablePortlets;
 	}
 
 	public String getSelectedPortletSelected() {
 		return selectedPortletSelected;
 	}
 
 	public void setContainerSelected(String iContainerSelected) {
 		if (containerSelected != null && containerSelected.equals(iContainerSelected))
 			return;
 		if (iContainerSelected == null || iContainerSelected.equals("")) {
 			Roma.setFeature(this, "availablePortlets", ViewFieldFeatures.VISIBLE, Boolean.FALSE);
 			Roma.setFeature(this, "selectedPortlets", ViewFieldFeatures.VISIBLE, Boolean.FALSE);
 		}
 		containerSelected = iContainerSelected;
 		Roma.setFeature(this, "availablePortlets", ViewFieldFeatures.VISIBLE, Boolean.TRUE);
 		Roma.setFeature(this, "selectedPortlets", ViewFieldFeatures.VISIBLE, Boolean.TRUE);
 		Roma.setFeature(this, "add", ViewActionFeatures.VISIBLE, Boolean.TRUE);
 		Roma.setFeature(this, "remove", ViewActionFeatures.VISIBLE, Boolean.TRUE);
 		Roma.setFeature(this, "addAll", ViewActionFeatures.VISIBLE, Boolean.TRUE);
 		Roma.setFeature(this, "removeAll", ViewActionFeatures.VISIBLE, Boolean.TRUE);
 		availablePortlets.clear();
 		selectedPortlets.clear();
 		availablePortletSelected = null;
 		selectedPortletSelected = null;
 		List<Class<?>> portletSchemas = Roma.component(SchemaClassResolver.class).getLanguageClassByInheritance(PortalPage.class);
 		for (Class<?> schema : portletSchemas) {
 			String classLabel = Roma.i18n().get(schema.getSimpleName() + ".label");
 			if (classLabel == null || classLabel.equals(""))
 				classLabel = schema.getSimpleName();
 			classNamesMapper.put(classLabel, schema.getSimpleName());
 			if (entity.getPortletsInfos().get(classNamesMapper.get(containerSelected)) != null) {
 				List<String> selected = entity.getPortletsInfos().get(classNamesMapper.get(containerSelected)).getPortlets();
 				if (selected.contains(schema.getSimpleName()))
 					selectedPortlets.add(classLabel);
 				else
 					availablePortlets.add(classLabel);
 			} else
 				availablePortlets.add(classLabel);
 		}
 		if (selectedPortlets.size() <= 0 || selectedPortlets.isEmpty()) {
 			Roma.setFeature(this, "removeAll", ViewActionFeatures.VISIBLE, Boolean.FALSE);
 			Roma.setFeature(this, "addAll", ViewActionFeatures.VISIBLE, Boolean.TRUE);
 		}
 		if (availablePortlets.size() <= 0 || availablePortlets.isEmpty()) {
 			Roma.setFeature(this, "addAll", ViewActionFeatures.VISIBLE, Boolean.FALSE);
 			Roma.setFeature(this, "removeAll", ViewActionFeatures.VISIBLE, Boolean.TRUE);
 		}
 		Roma.fieldChanged(this, "availablePortlets");
 		Roma.fieldChanged(this, "selectedPortlets");
 	}
 
 	public void setSelectedPortletSelected(String portletSelected) {
 		if (portletSelected != null) {
 			selectedPortletSelected = portletSelected;
 			setAvailablePortletSelected(null);
 			Roma.setFeature(this, "add", ViewActionFeatures.VISIBLE, Boolean.FALSE);
 			Roma.setFeature(this, "remove", ViewActionFeatures.VISIBLE, Boolean.TRUE);
 			Roma.fieldChanged(this, "availablePortlets");
 		}
 	}
 
 	public String getAvailablePortletSelected() {
 		return availablePortletSelected;
 	}
 
 	public void setAvailablePortletSelected(String availablePortletSelected) {
 		if (availablePortletSelected != null) {
 			this.availablePortletSelected = availablePortletSelected;
 			setSelectedPortletSelected(null);
 			Roma.setFeature(this, "add", ViewActionFeatures.VISIBLE, Boolean.TRUE);
 			Roma.setFeature(this, "remove", ViewActionFeatures.VISIBLE, Boolean.FALSE);
 			Roma.fieldChanged(this, "selectedPortlets");
 		}
 	}
 
 	public void onDispose() {
 	}
 
 	public void onShow() {
 		availablePortlets.clear();
 		containers.clear();
 		List<Class<?>> containerSchemas = Roma.component(SchemaClassResolver.class).getLanguageClassByInheritance(PortalPageContainer.class);
 		for (Class<?> schema : containerSchemas) {
 			String containerName = Roma.i18n().get(schema.getSimpleName() + ".label");
 			if (containerName == null || containerName.equals(""))
 				containerName = schema.getSimpleName();
 			classNamesMapper.put(containerName, schema.getSimpleName());
 			containers.add(containerName);
 		}
 		if (containerSelected == null || containerSelected.equals("")) {
 			Roma.setFeature(this, "availablePortlets", ViewFieldFeatures.VISIBLE, Boolean.FALSE);
 			Roma.setFeature(this, "selectedPortlets", ViewFieldFeatures.VISIBLE, Boolean.FALSE);
 			Roma.setFeature(this, "add", ViewActionFeatures.VISIBLE, Boolean.FALSE);
 			Roma.setFeature(this, "remove", ViewActionFeatures.VISIBLE, Boolean.FALSE);
 			Roma.setFeature(this, "addAll", ViewActionFeatures.VISIBLE, Boolean.FALSE);
 			Roma.setFeature(this, "removeAll", ViewActionFeatures.VISIBLE, Boolean.FALSE);
 		}
 		Roma.fieldChanged(this, "containers");
 	}
 
 	public void addAll() {
 		List<String> availablePortlets = new ArrayList<String>();
 		availablePortlets.addAll(this.availablePortlets);
 		for (String availablePortlet : availablePortlets) {
 			availablePortletSelected = availablePortlet;
 			add();
 		}
 	}
 
 	public void removeAll() {
 		List<String> selectedPortlets = new ArrayList<String>();
 		selectedPortlets.addAll(this.selectedPortlets);
 		for (String selectedPortlet : selectedPortlets) {
 			selectedPortletSelected = selectedPortlet;
 			remove();
 		}
 	}
 
 	public void add() {
 		if (containerSelected != null) {
 			if (availablePortletSelected != null && !availablePortletSelected.equals("")) {
 				if (!entity.getPortletsInfos().containsKey(classNamesMapper.get(containerSelected))) {
 					List<String> portlets = new ArrayList<String>();
 					portlets.add(classNamesMapper.get(selectedPortletSelected));
 					PortletList portletList = new PortletList();
 					portletList.setPortlets(portlets);
 					entity.getPortletsInfos().put(classNamesMapper.get(containerSelected), portletList);
 				} else {
 					if (!entity.getPortletsInfos().get(classNamesMapper.get(containerSelected)).getPortlets().contains(classNamesMapper.get(availablePortletSelected)))
 						entity.getPortletsInfos().get(classNamesMapper.get(containerSelected)).getPortlets().add(classNamesMapper.get(availablePortletSelected));
 				}
 				selectedPortlets.add(availablePortletSelected);
 				availablePortlets.remove(availablePortletSelected);
 				availablePortletSelected = null;
 				selectedPortletSelected = null;
 				if (availablePortlets.size() <= 0 || availablePortlets.isEmpty()) {
 					Roma.setFeature(this, "add", ViewActionFeatures.VISIBLE, Boolean.FALSE);
 					Roma.setFeature(this, "addAll", ViewActionFeatures.VISIBLE, Boolean.FALSE);
 					Roma.setFeature(this, "removeAll", ViewActionFeatures.VISIBLE, Boolean.TRUE);
 				}
 				Roma.fieldChanged(this, "availablePortlets");
 				Roma.fieldChanged(this, "selectedPortlets");
 			} else {
 				showNoPortletSelectedMessage();
 			}
 		} else {
 			showNoContainerSelectedMessage();
 		}
 	}
 
 	public void remove() {
 		if (containerSelected != null) {
 			if (selectedPortletSelected != null && !selectedPortletSelected.equals("")) {
 				while (entity.getPortletsInfos().get(classNamesMapper.get(containerSelected)).getPortlets().contains(classNamesMapper.get(selectedPortletSelected))) {
 					entity.getPortletsInfos().get(classNamesMapper.get(containerSelected)).getPortlets().remove(classNamesMapper.get(selectedPortletSelected));
 				}
 				selectedPortlets.remove(selectedPortletSelected);
 				availablePortlets.add(selectedPortletSelected);
 				availablePortletSelected = null;
 				selectedPortletSelected = null;
 				if (selectedPortlets.size() <= 0 || selectedPortlets.isEmpty()) {
 					Roma.setFeature(this, "remove", ViewActionFeatures.VISIBLE, Boolean.FALSE);
 					Roma.setFeature(this, "removeAll", ViewActionFeatures.VISIBLE, Boolean.FALSE);
 					Roma.setFeature(this, "addAll", ViewActionFeatures.VISIBLE, Boolean.TRUE);
 				}
 				Roma.fieldChanged(this, "availablePortlets");
 				Roma.fieldChanged(this, "selectedPortlets");
 			} else {
 				showNoPortletSelectedMessage();
 			}
 		} else {
 			showNoContainerSelectedMessage();
 		}
 	}
 
 	@Persistence(mode = PersistenceConstants.MODE_ATOMIC)
 	public void save() {
 		PersistenceAspect db = getPersistenceAspect();
 		Set<String> keys = entity.getPortletsInfos().keySet();
 		for (String string : keys) {
 			while (entity.getPortletsInfos().get(string).getPortlets() != null && entity.getPortletsInfos().get(string).getPortlets().contains(null)) {
 				entity.getPortletsInfos().get(string).getPortlets().remove(null);
 			}
 		}
 		db.updateObject(entity);
 		back();
 	}
 
 	public void cancel() {
 		back();
 	}
 
 	protected void back() {
 		Roma.flow().back();
 	}
 
 	private void showNoContainerSelectedMessage() {
 		Roma.flow().popup(new MessageOk("trackingModifyError", "Errore di selezione", null, "$PortletPreferencesConfiguration.noContainerSelected.error"));
 	}
 
 	private void showNoPortletSelectedMessage() {
 		Roma.flow().popup(new MessageOk("trackingModifyError", "Errore di selezione", null, "$PortletPreferencesConfiguration.noPortletSelected.error"));
 	}
 
 	public List<String> getSelectedPortlets() {
 		return selectedPortlets;
 	}
 
 	private PersistenceAspect getPersistenceAspect() {
 		return Roma.context().persistence();
 	}
 
 }
