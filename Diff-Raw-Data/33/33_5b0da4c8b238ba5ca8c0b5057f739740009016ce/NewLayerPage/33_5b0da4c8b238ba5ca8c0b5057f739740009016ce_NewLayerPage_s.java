 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.web.data.layer;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.panel.Fragment;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.CatalogBuilder;
 import org.geoserver.catalog.CoverageInfo;
 import org.geoserver.catalog.CoverageStoreInfo;
 import org.geoserver.catalog.DataStoreInfo;
 import org.geoserver.catalog.FeatureTypeInfo;
 import org.geoserver.catalog.LayerInfo;
 import org.geoserver.catalog.ResourceInfo;
 import org.geoserver.catalog.StoreInfo;
 import org.geoserver.web.CatalogIconFactory;
 import org.geoserver.web.GeoServerSecuredPage;
 import org.geoserver.web.data.resource.ResourceConfigurationPage;
 import org.geoserver.web.wicket.GeoServerTablePanel;
 import org.geoserver.web.wicket.SimpleAjaxLink;
 import org.geoserver.web.wicket.GeoServerDataProvider.Property;
 
 /**
  * A page listing the resources contained in a store, and whose links will bring
  * the user to a new resource configuration page
  * 
  * @author Andrea Aime - OpenGeo
  */
 @SuppressWarnings("serial")
 public class NewLayerPage extends GeoServerSecuredPage {
 
     String storeId;
     private NewLayerPageProvider provider;
     private GeoServerTablePanel<Resource> layers;
     private WebMarkupContainer layersContainer;
     private WebMarkupContainer selectLayersContainer;
     private WebMarkupContainer selectLayers;
     private Label storeName;
     
     public NewLayerPage() {
         this(null);
     }
 
     public NewLayerPage(String storeId) {
         this.storeId = storeId;
         
         // the store selector, used when no store is initially known
         Form selector = new Form("selector");
         selector.add(storesDropDown());
         selector.setVisible(storeId == null);
         add(selector);
         
         // the layer choosing block 
         // visible when in any  way a store has been chosen
         selectLayersContainer = new WebMarkupContainer("selectLayersContainer");
         selectLayersContainer.setOutputMarkupId(true);
         add(selectLayersContainer);
         selectLayers = new WebMarkupContainer("selectLayers");
         selectLayers.setVisible(storeId != null);
         selectLayersContainer.add(selectLayers);
         
         selectLayers.add(storeName = new Label("storeName", new Model()));
         if(storeId != null) {
             StoreInfo store = getCatalog().getStore(storeId, StoreInfo.class);
             storeName.setModelObject(store.getName());
         }
         
         provider = new NewLayerPageProvider();
         provider.setStoreId(storeId);
         provider.setShowPublished(true);
         layers = new GeoServerTablePanel<Resource>("layers", provider) {
 
             @Override
             protected Component getComponentForProperty(String id,
                     IModel itemModel, Property<Resource> property) {
                 if (property == NewLayerPageProvider.NAME) {
                     return resourceChooserLink(id, itemModel, property);
                 } else if (property == NewLayerPageProvider.PUBLISHED) {
                     final Resource resource = (Resource) itemModel.getObject();
                     final CatalogIconFactory icons = CatalogIconFactory.get();
                     ResourceReference icon = resource.isPublished()? icons.getEnabledIcon() : icons.getDisabledIcon();
                     Fragment f = new Fragment(id, "iconFragment", NewLayerPage.this);
                     f.add(new Image("layerIcon", icon));
                     return f;
                 } else {
                     throw new IllegalArgumentException(
                             "Don't know of property " + property.getName());
                 }
             }
 
         };
         layers.setFilterVisible(false);
         
         selectLayers.add(layers);
     }
     
     private DropDownChoice storesDropDown() {
         final DropDownChoice stores = new DropDownChoice("storesDropDown", new Model(),
                 new StoreListModel());
         stores.setOutputMarkupId(true);
         stores.add(new AjaxFormComponentUpdatingBehavior("onchange") {
             
             @Override
             protected void onUpdate(AjaxRequestTarget target) {
                 if (stores.getModelObject() != null) {
                     String name = stores.getModelObjectAsString();
                     StoreInfo store = getCatalog().getStoreByName(name, StoreInfo.class);
                     provider.setStoreId(store.getId());
                     storeName.setModelObject(store.getName());
                    
                     selectLayers.setVisible(true);
                    target.addComponent(selectLayersContainer);
                 } else {
                     selectLayers.setVisible(false);
                    target.addComponent(selectLayersContainer);
                 }
             }
 
         });
         return stores;
     }
 
     SimpleAjaxLink resourceChooserLink(String id, IModel itemModel,
             Property<Resource> property) {
         return new SimpleAjaxLink(id, itemModel, property
                 .getModel(itemModel)) {
 
             @Override
             protected void onClick(AjaxRequestTarget target) {
                 Resource resource = (Resource) getModelObject();
                 setResponsePage(new ResourceConfigurationPage(
                         buildLayerInfo(resource), true));
             }
 
         };
     }
 
     /**
      * Turns a resource name into a full {@link ResourceInfo}
      * 
      * @param resource
      * @return
      */
     LayerInfo buildLayerInfo(Resource resource) {
         Catalog catalog = getCatalog();
         StoreInfo store = catalog.getStore(getSelectedStoreId(), StoreInfo.class);
 
         // try to build from coverage store or data store
         try {
             CatalogBuilder builder = new CatalogBuilder(catalog);
             builder.setStore(store);
             if (store instanceof CoverageStoreInfo) {
                 CoverageInfo ci = builder.buildCoverage();
                 return builder.buildLayer(ci);
             } else if (store instanceof DataStoreInfo) {
                 FeatureTypeInfo fti = builder.buildFeatureType(resource.getName());
                 return builder.buildLayer(fti);
             }
         } catch (Exception e) {
             throw new RuntimeException(
                     "Error occurred while building the resources for the configuration page",
                     e);
         }
 
         // handle the case in which the store was not found anymore, or was not
         // of the expected type
         if (store == null)
             throw new IllegalArgumentException(
                     "Store is missing from configuration!");
         else
             throw new IllegalArgumentException(
                     "Don't know how to deal with this store " + store);
     }
     
     /**
      * Returns the storeId provided during construction, or the one pointed
      * by the drop down if none was provided during construction
      * @return
      */
     String getSelectedStoreId() {
         // the provider is always up to date 
         return provider.getStoreId();
     }
 
     final class StoreListModel extends LoadableDetachableModel {
       @Override
       protected Object load() {
           List<StoreInfo> stores = getCatalog().getStores(StoreInfo.class);
           List<String> storeNames = new ArrayList<String>();
           for (StoreInfo store : stores) {
              storeNames.add(store.getName());
           }
           Collections.sort(storeNames);
           return storeNames;
       }
   }
 
 }
