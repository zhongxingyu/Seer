 package org.mule.galaxy.web.client;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Tree;
 import com.google.gwt.user.client.ui.TreeItem;
 import com.google.gwt.user.client.ui.TreeListener;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.mule.galaxy.web.client.util.Toolbox;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.RegistryService;
 import org.mule.galaxy.web.rpc.RegistryServiceAsync;
 import org.mule.galaxy.web.rpc.WArtifactType;
 import org.mule.galaxy.web.rpc.WWorkspace;
 
 public class RegistryPanel extends AbstractMenuPanel {
 
     private Set artifactTypes;
     private Toolbox artifactTypesBox;
     private String workspaceId;
     private Collection workspaces;
     private RegistryServiceAsync service;
     private WorkspacePanel workspacePanel;
     private Toolbox workspaceBox;
     private Tree workspaceTree;
     
     public RegistryPanel(Galaxy galaxy) {
         super(galaxy);
         this.service = (RegistryServiceAsync) GWT.create(RegistryService.class);
         
         ServiceDefTarget target = (ServiceDefTarget) service;
         target.setServiceEntryPoint("/handler/registry.rpc");
         
         workspaceBox = new Toolbox();
         workspaceBox.setTitle("Workspaces");
         Image addImg = new Image("images/add_obj.gif");
         addImg.addClickListener(new ClickListener() {
             public void onClick(Widget w) {
                setMain(new AddArtifactPanel());
             }
             
         });
         workspaceBox.addButton(addImg);
         
        final RegistryPanel registryPanel = this;
         Image addWkspcImg = new Image("images/adddir_wiz.png");
         addWkspcImg.addClickListener(new ClickListener() {
             public void onClick(Widget arg0) {
                 setMain(new EditWorkspacePanel(registryPanel, 
                                                workspaces,
                                                workspaceId));
             }
             
         });
         workspaceBox.addButton(addWkspcImg);
         
         Image editWkspcImg = new Image("images/editor_area.gif");
         editWkspcImg.addClickListener(new ClickListener() {
             public void onClick(Widget w) {
                 TreeItem item = workspaceTree.getSelectedItem();
                 TreeItem parent = item.getParentItem();
                 String parentId = null;
                 if (parent != null) {
                     parentId = (String) parent.getUserObject();
                 }
                 setMain(new EditWorkspacePanel(registryPanel, 
                                                workspaces,
                                                parentId,
                                                workspaceId,
                                                item.getText()));
             }
             
         });
         workspaceBox.addButton(editWkspcImg);
         this.workspaceTree = new Tree();
         workspaceTree.setStyleName("workspaces");
         workspaceBox.add(workspaceTree);
         
         addMenuItem(workspaceBox);
 
         refreshWorkspaces();
         
         workspaceTree.addTreeListener(new TreeListener() {
             public void onTreeItemSelected(TreeItem ti) {
                 setActiveWorkspace((String) ti.getUserObject());
             }
 
             public void onTreeItemStateChanged(TreeItem ti) {
             }
         });
         
         artifactTypesBox = new Toolbox();
         artifactTypesBox.setTitle("Artifact Types");
         addMenuItem(artifactTypesBox);
         
         initArtifactTypes();
         
         workspacePanel = new WorkspacePanel(this);
         setMain(workspacePanel);
     }
 
     public void refreshWorkspaces() {
         if (workspaceTree.getItemCount() > 0) {
             workspaceTree.clear();
         }
         
         final TreeItem treeItem = workspaceTree.addItem("All");
         treeItem.setState(true);
         
         // Load the workspaces into a tree on the left
         service.getWorkspaces(new AbstractCallback(this) {
 
             public void onSuccess(Object o) {
                 workspaces = (Collection) o;
                 
                 initWorkspaces(treeItem, workspaces);
             }
         });
         // this crashes the gwt shell on winblows....
 //        workspaceTree.setSelectedItem(treeItem);
         
     }
 
     public RegistryServiceAsync getRegistryService() {
         return service;
     }
 
     private void initArtifactTypes() {
         artifactTypes = new HashSet();
         
         // Load the workspaces into a tree on the left
         service.getArtifactTypes(new AbstractCallback(this) {
 
             public void onSuccess(Object o) {
                 Collection workspaces = (Collection) o;
                 
                 for (Iterator itr = workspaces.iterator(); itr.hasNext();) {
                     final WArtifactType at = (WArtifactType) itr.next();
                     
                     Hyperlink hl = new Hyperlink(at.getDescription(), at.getId());
                     hl.addClickListener(new ClickListener() {
                         public void onClick(Widget w) {
                             String style = w.getStyleName();
                             if ("unselected-link".equals(style)) {
                                 w.setStyleName("selected-link");
                                 addArtifactTypeFilter(at.getId());
                             } else {
                                 w.setStyleName("unselected-link");
                                 removeArtifactTypeFilter(at.getId());
                             }
                             
                         }
                     });
                     hl.setStyleName("unselected-link");
                     artifactTypesBox.add(hl);
                 }
             }
         });
     }
     
     private void initWorkspaces(TreeItem ti, Collection workspaces) {
         for (Iterator itr = workspaces.iterator(); itr.hasNext();) {
             WWorkspace wi = (WWorkspace) itr.next();
             
             TreeItem treeItem = ti.addItem(wi.getName());
             treeItem.setUserObject(wi.getId());
             
             Collection workspaces2 = wi.getWorkspaces();
             if (workspaces2 != null) {
                 initWorkspaces(treeItem, workspaces2);
             }
         }
     }
     
     public void addArtifactTypeFilter(String id) {
         artifactTypes.add(id);
         setMain(new WorkspacePanel(this));
     }
 
     public void removeArtifactTypeFilter(String id) {
         artifactTypes.remove(id);
         setMain(new WorkspacePanel(this));
     }
 
     public void setActiveWorkspace(String workspaceId) {
         this.workspaceId = workspaceId;
         
         setMain(new WorkspacePanel(this));
     }
 
     public Set getArtifactTypes() {
         return artifactTypes;
     }
 
     public String getWorkspaceId() {
         return workspaceId;
     }
 }
