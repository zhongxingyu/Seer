 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.web.data.store;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.model.Model;
 import org.geoserver.catalog.WorkspaceInfo;
 import org.geoserver.web.GeoServerSecuredPage;
 import org.geoserver.web.data.NewDataPage;
 import org.geoserver.web.data.workspace.WorkspaceChoiceRenderer;
 import org.geoserver.web.data.workspace.WorkspacesModel;
 import org.geoserver.web.wicket.MenuDropDownChoice;
 
 /**
  * Page listing all the available stores. Follows the usual filter/sort/page
  * approach, provides ways to bulk delete stores and to add new ones
  */
 @SuppressWarnings("serial")
 public class StorePage extends GeoServerSecuredPage {
     StoreProvider provider = new StoreProvider();
 
     StorePanel table;
 
     public StorePage() {
         table = new StorePanel( "table", provider );
         table.setOutputMarkupId(true);
         add(table);
 
         // the workspaces drop down
         add(workspacesDropDown());
     }
 
     private DropDownChoice workspacesDropDown() {
         final DropDownChoice workspaces;
         workspaces = new MenuDropDownChoice("wsDropDown", new Model(null), new WorkspacesModel(), new WorkspaceChoiceRenderer()) {
 
             @Override
             protected void onChoice(AjaxRequestTarget target) {
                 if(getModelObject() != null) {
                     WorkspaceInfo ws = (WorkspaceInfo) getModelObject();
                     String wsId = ws.getId();
					setResponsePage(new NewDataPage(wsId));
                 }
             }
             
         };
         return workspaces;
     }
 }
