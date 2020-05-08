 /*
  * Copyright (c) 2010 The Jackson Laboratory
  * 
  * This is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this software.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.jax.drakegenetics.gwtclientapp.client;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.jax.drakegenetics.shareddata.client.LibraryNode;
 
 import com.extjs.gxt.ui.client.data.BaseTreeModel;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.TreeModel;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.TreePanelEvent;
 import com.extjs.gxt.ui.client.store.TreeStore;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.HorizontalPanel;
 import com.extjs.gxt.ui.client.widget.VerticalPanel;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 
 /**
  * @author <A HREF="mailto:dave.walton@jax.org">Dave Walton</A>
  */
 public class LaboratoryPanel  implements DrakeReceiver {
 
     private Label failMessage = null;
     private Folder root = null;
     private HorizontalPanel laboratoryPanel = new HorizontalPanel();
     private ContentPanel treePanel = new ContentPanel();
     private TreeStore<ModelData> store = new TreeStore<ModelData>();
     private TreePanel<ModelData> tree = new TreePanel<ModelData>(store);
     private final VerticalPanel workPanel = new VerticalPanel();
     private ContentPanel formPanel = new ContentPanel();
     private ContentPanel detailPanel = new ContentPanel();
     private final LaboratoryForm laboratoryForm; 
 
     public LaboratoryPanel(HorizontalPanel lp,
             DrakeGeneticsServiceAsync drakeGeneticsService) {
         this.laboratoryPanel = lp;
         final DrakeDetailPanel drakeDetail = new DrakeDetailPanel(detailPanel,
                 (DrakeReceiver)this, drakeGeneticsService);
         laboratoryForm = new LaboratoryForm(formPanel, 
                 drakeDetail, drakeGeneticsService);
 
         treePanel.setHeaderVisible(true);
         treePanel.setLayout(new FitLayout());
         treePanel.setHeading("Drakes");
 
         tree.setDisplayProperty("name");
         tree.setWidth(150);
         //tree.setHeight(450);
         tree.setHeight(670);
         tree.addListener(Events.OnClick,
                 new Listener<TreePanelEvent<ModelData>>() {
 
                     public void handleEvent(TreePanelEvent<ModelData> be) {
                         ModelData item = be.getItem();
                         if ("org.jax.drakegenetics.gwtclientapp.client.Drake".equals(item.getClass().getName())) {
                             Drake drake = (Drake)item;
                             //  Send drake to parent component of form Panel 
                             //  and to detail Panel
                             laboratoryForm.sendDrake(drake);
                             drakeDetail.sendDrake(drake);
                             
                         }
                     }
                 });
          
         treePanel.add(tree);
 
         laboratoryPanel.add(treePanel);
 
         workPanel.add(formPanel);
         
         workPanel.add(detailPanel);
         
         laboratoryPanel.add(workPanel);
         
         DrakeSetGenerator dg = new DrakeSetGenerator();
         Folder model = dg.getTreeModel(drakeGeneticsService);
         store.add(model.getChildren(), true);
 
     }
     
     public void sendDrake(Drake d) {
         Folder females;
         Folder males;
         
        Drake nuDrake = new Drake(d.getName(), d.getDiploidgenome(), 
                d.getPhenome(), new Image(d.getSmallimage().getUrl()), 
                 new Image(d.getLargeimage().getUrl()));
 
         if ( ((Folder)store.getChild(0)).getName().equals("Females")) {
             females = (Folder)store.getChild(0);
             males = (Folder)store.getChild(1);
         } else {
             females = (Folder)store.getChild(1);
             males = (Folder)store.getChild(0);
         }
         store.setMonitorChanges(true);
         
         if (d.getGender().equals("F")) {
             store.add(females,nuDrake, false);
         } else {
             store.add(males,nuDrake, false);
         }
         
     }
 
     public void refreshTabs() {
         laboratoryForm.refreshTabs();
     }    
 }
