 /*
  * $Id: LicenseHeader-GPLv2.txt 288 2008-01-29 00:59:35Z andrew $
  * --------------------------------------------------------------------------------------
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.mule.galaxy.web.client.registry;
 
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.ChangeListener;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.mule.galaxy.web.client.Galaxy;
 import org.mule.galaxy.web.client.MenuPanel;
 import org.mule.galaxy.web.client.util.NavigationUtil;
 import org.mule.galaxy.web.client.util.Toolbox;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.WArtifactView;
 
 /**
  * Forms the basis of any pages which do not list artifacts.
  */
 public class RegistryMenuPanel extends MenuPanel {
 
     private final Galaxy galaxy;
     private ListBox viewBox;
     private String selectedViewId;
     private final boolean showBrowse;
     private final boolean showSearch;
     private boolean first = true;
     private FlowPanel recentViewsPanel;
     
     public RegistryMenuPanel(Galaxy galaxy) {
         this(galaxy, true, true);
     }
 
     public RegistryMenuPanel(Galaxy galaxy, 
                              boolean showBrowse, 
                              boolean showSearch) {
         super();
         this.galaxy = galaxy;
         this.showBrowse = showBrowse;
         this.showSearch = showSearch;
     }
     
     public void onShow() {
         createLinks();
         loadViews();
     }
 
     private void createLinks() {
         if (!first) {
             return;
         }
         Toolbox menuLinks = new Toolbox(false);
         
         addTopLinks(menuLinks);
         
         Image addImg = new Image("images/add_obj.gif");
         addImg.addClickListener(NavigationUtil.createNavigatingClickListener("add-artifact"));
         
         Image addWkspcImg = new Image("images/fldr_obj.gif");
         addWkspcImg.addClickListener(NavigationUtil.createNavigatingClickListener("add-workspace"));
 
         menuLinks.add(asHorizontal(addImg, new Label(" "), new Hyperlink("Add Artifact", "add-artifact")));
         
         Hyperlink hl = new Hyperlink("Add Workspace", "add-workspace");
         menuLinks.add(asHorizontal(addWkspcImg, new Label(" "), hl));
         
         addBottomLinks(menuLinks);
         
         if (showBrowse) {
            Image browseImg = new Image("images/fldr_obj.gif");
            browseImg.addClickListener(NavigationUtil.createNavigatingClickListener("add-workspace"));

             hl = new Hyperlink("Browse Workspaces", "browse");
            menuLinks.add(asHorizontal(browseImg, new Label(" "), hl));
         }
 
         if (showSearch) {
             hl = new Hyperlink("Search Workspaces", "search");
             menuLinks.add(hl);
         }
         
         addMenuItem(menuLinks, 0);
         
         Toolbox viewToolbox = new Toolbox(false);
         
         viewToolbox.add(asHorizontal(newLabel("Views ", "toolbox-header"), 
                                      new Hyperlink("New", "view_new"),
                                      new Label("...")));
         
         viewBox = new ListBox();
         viewBox.setStyleName("view-ListBox");
         viewBox.setSize("170", "");
         viewToolbox.add(viewBox);
         viewBox.addChangeListener(new ChangeListener() {
             public void onChange(Widget arg0) {
                 int idx = viewBox.getSelectedIndex();
                 if (idx != -1) {
                     String id = viewBox.getValue(idx);
                     
                     if (id.length() > 0) {
                         History.newItem("view_" + id);
                     }
                 }
             }
         });
         
         addMenuItem(viewToolbox, 1);
         
         recentViewsPanel = new FlowPanel();
         recentViewsPanel.setStyleName("recent-views");
         viewToolbox.add(recentViewsPanel);
         
         first = false;
     }
     
     public void loadViews() {
         loadViews(null, null);
     }
     
     public void loadViews(String viewId, final AsyncCallback callback) {
         this.selectedViewId = viewId;
         galaxy.getRegistryService().getArtifactViews(new AbstractCallback(this) {
 
             public void onSuccess(Object views) {
                 initializeViews((Collection) views, callback);
             }
             
         });
         
         galaxy.getRegistryService().getRecentArtifactViews(new AbstractCallback(this) {
 
             public void onSuccess(Object views) {
                 initializeRecentViews((Collection) views);
             }
             
         });
     }
 
     protected void initializeRecentViews(Collection views) {
         recentViewsPanel.clear();
         for (Iterator itr = views.iterator(); itr.hasNext();) {
             WArtifactView wv = (WArtifactView) itr.next();
             
             recentViewsPanel.add(new Hyperlink(wv.getName(), "view_" + wv.getId()));
         }
     }
 
     protected void initializeViews(Collection views, AsyncCallback callback) {
         viewBox.clear();
         viewBox.addItem("Select...", "");
         for (Iterator itr = views.iterator(); itr.hasNext();) {
             WArtifactView wv = (WArtifactView) itr.next();
             
             viewBox.addItem(wv.getName(), wv.getId());
             
             if (wv.getId().equals(selectedViewId)) {
                 viewBox.setSelectedIndex(viewBox.getItemCount()-1);
 
                 if (callback != null) {
                     callback.onSuccess(wv);
                 }
             }
         }
     }
 
     protected void addTopLinks(Toolbox topMenuLinks) {
         
     }
 
     protected void addBottomLinks(Toolbox topMenuLinks) {
         
     }
 
 }
