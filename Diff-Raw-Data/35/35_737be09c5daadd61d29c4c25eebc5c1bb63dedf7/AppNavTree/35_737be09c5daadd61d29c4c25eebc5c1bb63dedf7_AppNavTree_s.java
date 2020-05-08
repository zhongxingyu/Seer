 package com.scholastic.sbam.client.uiobjects;
 
import java.io.Serializable;  
  
 import com.extjs.gxt.ui.client.data.BaseTreeModel;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.ModelIconProvider;
 import com.extjs.gxt.ui.client.store.TreeStore;
 import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 import com.scholastic.sbam.client.util.IconSupplier;
 
 public class AppNavTree {
 	
 	public static class SelectionIconProvider implements ModelIconProvider<ModelData> {
 
 		@Override
 		public AbstractImagePrototype getIcon(ModelData model) {
 			if (model.get("iconName") != null)
 				return IconSupplier.getAppNavIcon(model.get("iconName").toString());
 			return null;
 		}
 		
 	}
 	
 	public static class SelectionTreeNode extends BaseTreeModel {
 		private static final long serialVersionUID = 1L;
 
 	 	public SelectionTreeNode() {
 	 	}
 
 	 	public SelectionTreeNode(String name, String iconName) {
 	 		set("name", name);
 	 		set("iconName", iconName);
 	 		set("portlet", AppPortletProvider.AppPortletIds.UNKNOWN_PORTLET);
 	 	}
 
 	 	public SelectionTreeNode(String name, String iconName, AppPortletProvider.AppPortletIds portlet) {
 	 		set("name", name);
 	 		set("iconName", iconName);
 	 		set("portlet", portlet);
 	 	}
 
 	 	public String getName() {
 	 		return (String) get("name");
 	 	}
 
 	 	public String getIconName() {
 	 		return (String) get("iconName");
 	 	}
 	 	
 	 	public AppPortletProvider.AppPortletIds getPortletId() {
 	 		return (AppPortletProvider.AppPortletIds) get("portlet");
 	 	}
 	 	
 	 	public String toString() {
 	 		return getName();
 	 	}
 	}
 
   
	public static class Folder extends BaseTreeModel implements Serializable {  
 		private static final long serialVersionUID = 1L;  
 		private static int ID = 0;  
 		    
 		public Folder() {  
 			set("id", ID++);  
 		}  
 		  
 		public Folder(String name) {  
 			set("id", ID++);  
 			set("name", name);  
 		}  
 		  
 		public Folder(String name, BaseTreeModel[] children) {  
 			this(name);  
 			for (int i = 0; i < children.length; i++) {  
 				add(children[i]);  
 			}  
 		}  
 		  
 		public Integer getId() {  
 			return (Integer) get("id");  
 		}  
 		  
 		public String getName() {  
 			return (String) get("name");  
 		}
 		
 		public void add(BaseTreeModel [] children) { 
 			for (int i = 0; i < children.length; i++) {  
 				add(children[i]);  
 			}  
 		}
 		  
 		public String toString() {  
 			return getName();  
 		}  
 	}
 	
 	
 	public static Folder getTreeModel() {
 		
 	  Folder searches = new Folder("Searches");
 	  
 	  Folder custSrch = new Folder("Customers");
 	  custSrch.add(new SelectionTreeNode("Full Institution List", IconSupplier.getInstitutionIconName(), AppPortletProvider.AppPortletIds.FULL_INSTITUTION_SEARCH));
 	  custSrch.add(new SelectionTreeNode("Customer List", IconSupplier.getInstitutionIconName()));
 	  custSrch.add(new SelectionTreeNode("Site List", IconSupplier.getSiteIconName()));
 	  custSrch.add(new SelectionTreeNode("Contacts", IconSupplier.getContactsIconName()));
 	  
 	  Folder agreeSrch = new Folder("Agreements");
 	  agreeSrch.add(new SelectionTreeNode("Agreement", IconSupplier.getAgreementIconName()));
 	  agreeSrch.add(new SelectionTreeNode("Product Terms", IconSupplier.getProductIconName()));
 	  agreeSrch.add(new SelectionTreeNode("Sites", IconSupplier.getSiteIconName()));
 	  agreeSrch.add(new SelectionTreeNode("Methods", IconSupplier.getAccessMethodIconName()));
 	  agreeSrch.add(new SelectionTreeNode("Notes", IconSupplier.getNoteIconName()));
 	  
 	  searches.add(custSrch);
 	  searches.add(agreeSrch);
 	  
 	  Folder agreements = new Folder("Agreements");
 	  agreements.add(new SelectionTreeNode("Create New", IconSupplier.getNewIconName()));
 	  agreements.add(new SelectionTreeNode("Agreement", IconSupplier.getAgreementIconName()));
 	  agreements.add(new SelectionTreeNode("Product Terms", IconSupplier.getProductIconName()));
 	  agreements.add(new SelectionTreeNode("Sites", IconSupplier.getSiteIconName()));
 	  agreements.add(new SelectionTreeNode("Contacts", IconSupplier.getContactsIconName()));
 	  agreements.add(new SelectionTreeNode("Methods", IconSupplier.getAccessMethodIconName()));
 
 	  Folder root = new Folder("root");
 	  root.add(searches);
 	  root.add(agreements);
 
 	  return root;
 	}
 	
 	public static TreePanel<ModelData> getTreePanel() {
 		Folder model = getTreeModel();  
 		
 		TreeStore<ModelData> store = new TreeStore<ModelData>();  
 		store.add(model.getChildren(), true); 
 		
 		final TreePanel<ModelData> tree = new TreePanel<ModelData>(store);
 		tree.setDisplayProperty("name");  
 		tree.setWidth(250);
 		tree.setIconProvider(new SelectionIconProvider());
 		tree.setAutoExpand(true);
 		
 		return tree;
 	}
 }
