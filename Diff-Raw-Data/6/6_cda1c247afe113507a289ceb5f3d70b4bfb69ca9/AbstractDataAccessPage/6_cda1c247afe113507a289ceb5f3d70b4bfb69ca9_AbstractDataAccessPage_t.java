 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.web.data.store;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.model.ResourceModel;
 import org.apache.wicket.validation.IValidator;
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.DataStoreInfo;
 import org.geoserver.catalog.NamespaceInfo;
 import org.geoserver.catalog.ResourcePool;
 import org.geoserver.web.GeoServerSecuredPage;
 import org.geoserver.web.data.store.panel.CheckBoxParamPanel;
 import org.geoserver.web.data.store.panel.NamespacePanel;
 import org.geoserver.web.data.store.panel.PasswordParamPanel;
 import org.geoserver.web.data.store.panel.TextParamPanel;
 import org.geoserver.web.data.store.panel.WorkspacePanel;
 import org.geoserver.web.util.MapModel;
 import org.geoserver.web.wicket.FileExistsValidator;
 import org.geotools.data.DataAccessFactory;
 import org.geotools.data.DataAccessFactory.Param;
 import org.geotools.util.logging.Logging;
 
 /**
  * Abstract base class for adding/editing a {@link DataStoreInfo}, provides the UI components and a
  * template method {@link #onSaveDataStore(Form)} for the subclasses to perform the insertion or
  * update of the object.
  * 
  * @author Gabriel Roldan
  * @see DataAccessNewPage
  * @see DataAccessEditPage
  */
 abstract class AbstractDataAccessPage extends GeoServerSecuredPage {
 
     protected static final Logger LOGGER = Logging.getLogger("org.geoserver.web.data.store");
 
     public AbstractDataAccessPage() {
 
     }
 
     /**
      * 
      * @param info
      * @throws IllegalArgumentException
      */
     protected final void initUI(final DataStoreInfo info) throws IllegalArgumentException {
 
         if (info.getWorkspace() == null) {
             throw new IllegalArgumentException("Workspace not provided");
         }
 
         final Catalog catalog = getCatalog();
         final ResourcePool resourcePool = catalog.getResourcePool();
         DataAccessFactory dsFactory;
         try {
             dsFactory = resourcePool.getDataStoreFactory(info);
         } catch (IOException e) {
             throw new IllegalArgumentException("Can't locate a datastore factory for '"
                     + info.getType() + "'. " + e.getMessage());
         }
         if (dsFactory == null) {
             throw new IllegalArgumentException("Can't locate a datastore factory for '"
                     + info.getType() + "'");
         }
 
         final Map<String, ParamInfo> paramsMetadata = new LinkedHashMap<String, ParamInfo>();
 
         {
             final boolean isNew = null == info.getId();
             final Param[] dsParams = dsFactory.getParametersInfo();
             for (Param p : dsParams) {
                 ParamInfo paramInfo = new ParamInfo(p);
                 paramsMetadata.put(p.key, paramInfo);
 
                 if (isNew) {
                     // set default value
                     Serializable defValue;
                     if ("namespace".equals(paramInfo.getName())) {
                         defValue = catalog.getDefaultNamespace().getURI();
                     } else if (URL.class == paramInfo.getBinding()) {
                         defValue = "file:data/example.extension";
                     } else {
                         defValue = paramInfo.getValue();
                     }
                     info.getConnectionParameters().put(paramInfo.getName(), defValue);
                 }
             }
         }
 
         final IModel model = new CompoundPropertyModel(info);
 
         final Form paramsForm = new Form("dataStoreForm", model);
         add(paramsForm);
 
         paramsForm.add(new Label("storeType", dsFactory.getDisplayName()));
         paramsForm.add(new Label("storeTypeDescription", dsFactory.getDescription()));
 
         final WorkspacePanel workspacePanel;
         {
             final IModel wsModel = new PropertyModel(model, "workspace");
             final IModel wsLabelModel = new ResourceModel("AbstractDataAccessPage.workspace");
             workspacePanel = new WorkspacePanel("workspacePanel", wsModel, wsLabelModel, true);
         }
         paramsForm.add(workspacePanel);
 
         final TextParamPanel dataStoreNamePanel;
 
         dataStoreNamePanel = new TextParamPanel("dataStoreNamePanel", new PropertyModel(model,
                 "name"),
                 new ResourceModel("AbstractDataAccessPage.dataSrcName", "Data Source Name"), true);
         paramsForm.add(dataStoreNamePanel);
 
         paramsForm.add(new TextParamPanel("dataStoreDescriptionPanel", new PropertyModel(model,
                 "description"), new ResourceModel("description", "Description"), false,
                 (IValidator[]) null));
 
         paramsForm.add(new CheckBoxParamPanel("dataStoreEnabledPanel", new PropertyModel(model,
                 "enabled"), new ResourceModel("enabled", "Enabled")));
 
         final List<String> keys = new ArrayList<String>(paramsMetadata.keySet());
         final IModel paramsModel = new PropertyModel(model, "connectionParameters");
 
         ListView paramsList = new ListView("parameters", keys) {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void populateItem(ListItem item) {
                 String paramName = item.getModelObjectAsString();
                 ParamInfo paramMetadata = paramsMetadata.get(paramName);
 
                 Component inputComponent;
                 inputComponent = getInputComponent("parameterPanel", paramsModel, paramMetadata);
 
                 String description = paramMetadata.getTitle();
                 if (description != null) {
                     inputComponent.add(new SimpleAttributeModifier("title", description));
                 }
                 item.add(inputComponent);
             }
         };
         // needed for form components not to loose state
         paramsList.setReuseItems(true);
 
         paramsForm.add(paramsList);
 
         paramsForm.add(new BookmarkablePageLink("cancel", StorePage.class));
 
         paramsForm.add(new AjaxSubmitLink("save", paramsForm) {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void onError(AjaxRequestTarget target, Form form) {
                 super.onError(target, form);
                 target.addComponent(paramsForm);
             }
             
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form form) {
                 try {
                     DataStoreInfo modelObject = (DataStoreInfo) form.getModelObject();
                     onSaveDataStore(modelObject, target);
                 } catch (IllegalArgumentException e) {
                     paramsForm.error(e.getMessage());
                     target.addComponent(paramsForm);
                 }
             }
         });
 
         paramsForm.add(new FeedbackPanel("feedback"));
 
         // validate the selected workspace does not already contain a store with the same name
         final String dataStoreInfoId = info.getId();
         StoreNameValidator storeNameValidator = new StoreNameValidator(workspacePanel
                 .getFormComponent(), dataStoreNamePanel.getFormComponent(), dataStoreInfoId);
         paramsForm.add(storeNameValidator);
     }
 
     /**
      * Call back method called when the save button is hit. Subclasses shall override in order to
      * perform the action over the catalog, whether it is adding a new {@link DataStoreInfo} or
      * saving the edits to an existing one
      * 
      * @param info
      *            the object to save
      * @param requestTarget
      * @throws IllegalArgumentException
      *             with an appropriate message for the user if the operation failed
      */
     protected abstract void onSaveDataStore(final DataStoreInfo info,
             AjaxRequestTarget requestTarget) throws IllegalArgumentException;
 
     /**
      * Creates a form input component for the given datastore param based on its type and metadata
      * properties.
      * 
      * @param paramMetadata
      * @return
      */
     private Panel getInputComponent(final String componentId, final IModel paramsModel,
             final ParamInfo paramMetadata) {
 
         final String paramName = paramMetadata.getName();
         final String paramLabel = paramMetadata.getName();
         final boolean required = paramMetadata.isRequired();
         final Class<?> binding = paramMetadata.getBinding();
 
         Panel parameterPanel;
         if ("namespace".equals(paramName)) {
             IModel namespaceModel = new NamespaceParamModel(paramsModel, paramName);
             IModel paramLabelModel = new ResourceModel(paramLabel, paramLabel);
             parameterPanel = new NamespacePanel(componentId, namespaceModel, paramLabelModel, true);
 
         } else if (Boolean.class == binding) {
             // TODO Add prefix for better i18n?
             parameterPanel = new CheckBoxParamPanel(componentId, new MapModel(paramsModel,
                     paramName), new ResourceModel(paramLabel, paramLabel));
 
         } else if (String.class == binding && paramMetadata.isPassword()) {
             parameterPanel = new PasswordParamPanel(componentId, new MapModel(paramsModel,
                     paramName), new ResourceModel(paramLabel, paramLabel), required);
         } else {
             TextParamPanel tp = new TextParamPanel(componentId,
                     new MapModel(paramsModel, paramName),
                     new ResourceModel(paramLabel, paramLabel), required);
             // if it can be a reference to the local filesystem make sure it's valid
             if (paramName.equalsIgnoreCase("url")) {
                 tp.getFormComponent().add(new FileExistsValidator());
             }
            // make sure the proper value is returned, but don't set it for strings otherwise
            // we incur in a wicket bug (the empty string is not converter back to a null)
            if(binding != null && !String.class.equals(binding))
            	tp.getFormComponent().setType(binding);
             parameterPanel = tp;
         }
         return parameterPanel;
     }
 
     protected void clone(final DataStoreInfo source, DataStoreInfo target) {
         target.setDescription(source.getDescription());
         target.setEnabled(source.isEnabled());
         target.setName(source.getName());
         target.setWorkspace(source.getWorkspace());
         target.setType(source.getType());
 
         target.getConnectionParameters().clear();
         target.getConnectionParameters().putAll(source.getConnectionParameters());
     }
 
     /**
      * Model to wrap and unwrap a {@link NamespaceInfo} to and from a String for the Datastore's
      * "namespace" parameter
      * 
      */
     private final class NamespaceParamModel extends MapModel {
 
         private static final long serialVersionUID = 6767931873085302114L;
 
         private NamespaceParamModel(IModel model, String expression) {
             super(model, expression);
         }
 
         @Override
         public Object getObject() {
             String nsUri = (String) super.getObject();
             NamespaceInfo namespaceInfo = getCatalog().getNamespaceByURI(nsUri);
             return namespaceInfo;
         }
 
         @Override
         public void setObject(Object object) {
             NamespaceInfo namespaceInfo = (NamespaceInfo) object;
             String nsUri = namespaceInfo.getURI();
             super.setObject(nsUri);
         }
     }
 }
