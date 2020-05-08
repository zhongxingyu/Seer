 package edu.ucla.loni.client;
 
 import edu.ucla.loni.shared.*;
 import gwtupload.client.IUploadStatus.Status;
 import gwtupload.client.IUploader;
 import gwtupload.client.IUploader.UploadedInfo;
 import gwtupload.client.MultiUploader;
 import gwtupload.client.SingleUploader;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.http.client.URL;
 
 import com.google.gwt.regexp.shared.MatchResult;
 import com.google.gwt.regexp.shared.RegExp;
 import com.google.gwt.regexp.shared.SplitResult;
 
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import com.smartgwt.client.data.Record;
 import com.smartgwt.client.types.Alignment;
 import com.smartgwt.client.types.KeyNames;
 import com.smartgwt.client.types.ListGridComponent;
 import com.smartgwt.client.types.SelectionAppearance;
 import com.smartgwt.client.types.VerticalAlignment;
 import com.smartgwt.client.util.SC;
 
 import com.smartgwt.client.widgets.events.ClickEvent;  
 import com.smartgwt.client.widgets.events.ClickHandler;
 import com.smartgwt.client.widgets.events.ResizedEvent;
 import com.smartgwt.client.widgets.events.ResizedHandler;
 import com.smartgwt.client.widgets.Button;
 import com.smartgwt.client.widgets.Label;
 import com.smartgwt.client.widgets.form.DynamicForm;
 import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
 import com.smartgwt.client.widgets.form.fields.FormItem;
 import com.smartgwt.client.widgets.form.fields.StaticTextItem;
 import com.smartgwt.client.widgets.form.fields.TextAreaItem;
 import com.smartgwt.client.widgets.form.fields.TextItem;
 import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
 import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
 import com.smartgwt.client.widgets.form.fields.events.KeyUpEvent;
 import com.smartgwt.client.widgets.form.fields.events.KeyUpHandler;
 import com.smartgwt.client.widgets.grid.ListGrid;
 import com.smartgwt.client.widgets.grid.ListGridField;
 import com.smartgwt.client.widgets.grid.ListGridRecord;
 import com.smartgwt.client.widgets.grid.events.RecordDoubleClickEvent;
 import com.smartgwt.client.widgets.grid.events.RecordDoubleClickHandler;
 import com.smartgwt.client.widgets.grid.events.SelectionUpdatedEvent;
 import com.smartgwt.client.widgets.grid.events.SelectionUpdatedHandler;
 import com.smartgwt.client.widgets.layout.HLayout;
 import com.smartgwt.client.widgets.layout.VLayout;
 import com.smartgwt.client.widgets.menu.Menu;
 import com.smartgwt.client.widgets.menu.MenuItem;
 import com.smartgwt.client.widgets.menu.MenuItemSeparator;
 import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
 import com.smartgwt.client.widgets.toolbar.ToolStrip;
 import com.smartgwt.client.widgets.toolbar.ToolStripButton;
 import com.smartgwt.client.widgets.tree.Tree;
 import com.smartgwt.client.widgets.tree.TreeGrid;
 import com.smartgwt.client.widgets.tree.TreeNode;
 import com.smartgwt.client.widgets.tree.events.NodeContextClickEvent;
 import com.smartgwt.client.widgets.tree.events.NodeContextClickHandler;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class ServerLibraryManager implements EntryPoint {
 	////////////////////////////////////////////////////////////
 	// Private Variables
 	////////////////////////////////////////////////////////////
 	
 	/**
 	 *   Remote Server Proxy to talk to the server-side code
 	 */
 	private FileServiceAsync fileServer = GWT.create(FileService.class);
 	
 	/**
 	 *   Default Root Directory
 	 */
 	private String rootDirectoryDefault = "/usr/share/tomcat6/CraniumLibrary";
 	
 	/**
 	 *   Current Root Directory
 	 */
 	private String rootDirectory = rootDirectoryDefault;
 	
 	/**
 	 *   Workarea
 	 *   <p>
 	 *   Updated by a lot of functions
 	 */
 	private final VLayout workarea = new VLayout();
 	
 	/**
 	 *   Last pipefile that was viewed
 	 */
 	private Pipefile lastPipefile = null;
 	
 	/**
 	 *   Button that allows you to jump back to the last pipefile you viewed
 	 */
 	private final ToolStripButton backToLastPipefile = new ToolStripButton();
 	
 	/**
 	 *   Message
 	 *   <p>
 	 *   Updated with the last message
 	 */
 	private final Label message = new Label();
 	
 	/**
 	 *   Package Tree
 	 *   <p>
 	 *   Set in: treeRefresh
 	 *   <br>
 	 *   Used in: onModuleLoad
 	 */
 	private final Tree fullTree = new Tree();
 	
 	/**
 	 *   Results Tree
 	 *   <p>
 	 *   Set in: treeResults
 	 *   <br>
 	 *   Used in: onModuleLoad
 	 */
 	private final Tree resultsTree = new Tree();
 	
 	/**
 	 *   Tree Grid
 	 *   <p>
 	 *   Set in: onModuleLoad
 	 *   <br>
 	 *   Used in: selectionClickHandler, contextClickHandler, updateFullTree, updateResultsTree
 	 */
 	private final TreeGrid treeGrid = new TreeGrid();
 	
 	/**
 	 *   True if packages is the highest level folder in fullTree, 
 	 *   False if module type is the highest level folder in fullTree
 	 *   <p>
 	 *   Set in: onModuleLoad
 	 *   <br>
 	 *   Used in: selectionClickHandler, contextClickHandler, updateFullTree, updateResultsTree
 	 */
 	private boolean viewByPackage = true;
 
 	/**
 	 *  String abosolutePath => Pipefile pipe
 	 *  <p>
 	 *  Set in: treeRefresh
 	 *  <br>
 	 *  Used in: viewFile, editFile
 	 */
 	private final LinkedHashMap<String, Pipefile> pipes = new LinkedHashMap<String, Pipefile>();
 	
 	/**
 	 *  String key => TreeNode node, 
 	 *  each tree node is given a key which is either the path from the root to that node (used for folders)
 	 *  <p>
 	 *  Set in: treeRefresh
 	 *  <br>
 	 *  Used in: viewFile, editFile
 	 */
 	private final LinkedHashMap<String, TreeNode> nodes = new LinkedHashMap<String, TreeNode>();
 	
 	/**
 	 *   String groupName => Group g
 	 *   <p>
 	 *   Set in viewGroups
 	 *   <br>
 	 *   Used in editGroup
 	 */
 	private final LinkedHashMap<String, Group> groups = new LinkedHashMap<String, Group>();
 	
 	/**
 	 *   Pipefiles that are currently selected
 	 *   <p>
 	 *   Set in: selectionClickHandler
 	 *   <br>
 	 *   Used in: fileOperations
 	 */
 	private Pipefile[] selected = null;
 	
 	/**
 	 *  Pipefiles that have been selected to be copied
 	 *  <p>
 	 *  Set in: contextClickHandler
 	 *  <br>
 	 *  Used in: contextClickHandler (a later event)
 	 */
 	private Pipefile[] toCopy = null;
 	
 	/**
 	 *  Pipefiles that have been selected to be moved
 	 *  <p>
 	 *  Set in: contextClickHandler
 	 *  <br>
 	 *  Used in: contextClickHandler (a later event)
 	 */
 	private Pipefile[] toMove = null;
 	
 	/**
 	 *   Set in: treeRefresh 
 	 *   <br>
 	 *   Used in: fileOperations
 	 */
 	private String[] packages = null;
 	
 	/**
 	 * Returns all pipefiles related with the given record, 
 	 * Gets the pipefiles from the  
 	 */
 	private ArrayList<Pipefile> getPipefiles(ListGridRecord[] records, Tree t){
 		ArrayList<Pipefile> ret = new ArrayList<Pipefile>();
 		
 		for (ListGridRecord r : records){
 			String absPath = r.getAttribute("absolutePath");
 			if (absPath != null && !absPath.equals("")){
 				Pipefile p = pipes.get(absPath);
 				ret.add(p);
 			}
 			else {
 				String key = r.getAttribute("key");
 				TreeNode node = nodes.get(key);
 				TreeNode[] children = t.getChildren(node);
 				ret.addAll(getPipefiles(children, t));
 			}
 		}
 		
 		return ret;
 	}
 	
 	private void updateSelected(){
 		ListGridRecord[] selectedRecords = treeGrid.getSelectedRecords();
 		Tree tree = treeGrid.getTree();
 		
 		ArrayList<Pipefile> selectedList = getPipefiles(selectedRecords, tree);
 		selected = new Pipefile[selectedList.size()];
 		selected = selectedList.toArray(selected);
 		
 		int numSelected = selected.length;
 		
 		if (numSelected == 0){
 			basicInstructions();
 		}
 		else if (numSelected == 1){
 			viewFile(selected[0]);
 		} else {
 			fileOperations(selected);
 		}
 	}
 	
 	/**
 	 *   Handler for when the selection in the treeGrid is updated
 	 */
 	private SelectionUpdatedHandler selectedPipefilesHandler = new SelectionUpdatedHandler() {
 		public void onSelectionUpdated(SelectionUpdatedEvent event){
 			updateSelected();
 		}
 	};
 	
 	private NodeContextClickHandler contextClickHandler = new NodeContextClickHandler() {
 		public void onNodeContextClick(NodeContextClickEvent event){
 			TreeGrid grid = event.getViewer();
 			final Tree tree = grid.getData();
 			final TreeNode clicked = event.getNode();
 			
 			updateSelected();
 			
 			Menu contextMenu = new Menu();
 			
 			if (tree.isFolder(clicked)){
 				if (clicked.getAttributeAsBoolean("moveHere") == true){
 					MenuItem paste = new MenuItem("Paste");
 					paste.setEnabled(toCopy != null || toMove != null);
 					contextMenu.addItem(paste);
 					
 					paste.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){
 						public void onClick(MenuItemClickEvent event){
 							boolean copy = (toCopy != null);
 							Pipefile[] toPaste = copy ? toCopy : toMove;
 							
 							if (copy){
 								copyFiles(toPaste, clicked.getName());
 							} else {
 								moveFiles(toPaste, clicked.getName());
 							}
 						}
 					});
 					
 					contextMenu.addItem(new MenuItemSeparator());
 				}
 			}
 				
 			MenuItem copy = new MenuItem("Copy");
 			copy.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){
 				public void onClick(MenuItemClickEvent event){												
 					toCopy = selected;
 					toMove = null;
 				}
 			});
 			contextMenu.addItem(copy);
 			
 			MenuItem move = new MenuItem("Move");
 			move.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){
 				public void onClick(MenuItemClickEvent event){
 					toCopy = null;
 					toMove = selected;
 				}
 			});
 			contextMenu.addItem(move);
 					
 			MenuItem download = new MenuItem("Download");
 			download.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){
 				public void onClick(MenuItemClickEvent event){
 					downloadFiles(selected);
 				}
 			});
 			contextMenu.addItem(download);
 			
 			MenuItem remove = new MenuItem("Remove");
 			remove.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){
 				public void onClick(MenuItemClickEvent event){
 					removeFiles(selected);
 				}
 			});
 			contextMenu.addItem(remove);	
 			
 			grid.setContextMenu(contextMenu);
 		}
 	};
 
 
 	////////////////////////////////////////////////////////////
 	// On Module Load
 	////////////////////////////////////////////////////////////
 	
 	/**
 	 * Entry point method (basically main function)
 	 */
 	public void onModuleLoad() {		
 		// Header -- Title
 		Label title = new Label ();
 		title.setWidth100();
 		title.setAlign(Alignment.CENTER);
 		title.setHeight(50);
 		title.setContents("Loni Pipeline Server Library Manager");
 		title.setStyleName("title");
 		
 		// Header -- ToolStrip		
 		ToolStripButton home = new ToolStripButton("Home");
 		home.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				basicInstructions();
 			}
 		});
 		
 		ToolStripButton importButton = new ToolStripButton("Import");
 		importButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				importForm();
 			}
 		});
 		
 		ToolStripButton groupsButton = new ToolStripButton("Manage Groups");
 		groupsButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				viewGroups();
 			}
 		});
 		
 		backToLastPipefile.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				viewFile(lastPipefile);
 			}
 		});
 		
 		message.setStyleName("success");
 		message.setWidth100();
 		
 		// Header -- Button Row
 	    ToolStrip mainToolStrip = new ToolStrip(); 
 	    mainToolStrip.addSpacer(1);
 	    mainToolStrip.addButton(home);
 	    mainToolStrip.addSeparator();
 	    mainToolStrip.addButton(importButton);
 	    mainToolStrip.addSeparator();
 	    mainToolStrip.addButton(groupsButton);
 	    mainToolStrip.addSeparator();
 	    mainToolStrip.addButton(backToLastPipefile);
 	    mainToolStrip.addFill();
 	    mainToolStrip.addMember(message);
 	    
 	    backToLastPipefile.hide();
 	    
 		// Header
 		VLayout header = new VLayout();
 		header.setHeight(75);
 		header.addMember(title);
 		header.addMember(mainToolStrip);
 		
 		// Workarea
 		workarea.setWidth100();
 		workarea.setHeight100();
 		workarea.setPadding(10);
 	    
 		basicInstructions();
 	    
 		// Left -- Root Directory -- Label
 		final Label rootCurrent = new Label(rootDirectory);
 		rootCurrent.setHeight(40);
 		rootCurrent.setAlign(Alignment.CENTER);
 		rootCurrent.setValign(VerticalAlignment.TOP);
 		rootCurrent.setStyleName("root-directory");
 		
 		// Left -- Root Directory -- Form
 		final TextItem rootNew = new TextItem();
 		rootNew.setDefaultValue(rootDirectory);
 		rootNew.setShowTitle(false);
 		rootNew.setWidth(280);
 		
 		final DynamicForm rootForm = new DynamicForm();
 		rootForm.setHeight(40);
 		rootForm.setAlign(Alignment.CENTER);
 		rootForm.setFields(rootNew);
 		
 	    // Left -- Search Form
 	    final TextItem query = new TextItem();
 	    query.setHint("Search");
 	    query.setShowTitle(false);
 	    query.setShowHintInField(true);
 	    query.setWidth(280);
 	    query.addChangedHandler(new ChangedHandler(){
 	    	public void onChanged(ChangedEvent event){
 	    		String q = query.getValueAsString();
 	    		if (q != null && q.length() >= 2){
 	    			treeGrid.setData(resultsTree);
 	    			updateResultsTree(q);
 	    		} else {
 	    			treeGrid.setData(fullTree);
 	    		}
 	    	}
 	    });
 
 	    DynamicForm searchForm = new DynamicForm();
 	    searchForm.setFields(new FormItem[] {query});
 	    searchForm.setWidth100();
 	    
 	    // Left -- Tool Strip
 	    final ToolStripButton byPackage = new ToolStripButton("View By Package");
 	    byPackage.setDisabled(true);
 	    
 	    final ToolStripButton byType = new ToolStripButton("View By Module Type");
 	    byType.setDisabled(false);
 	    
 	    byPackage.addClickHandler(new ClickHandler() {
 	    	public void onClick (ClickEvent event){	    		
 	    		byPackage.setDisabled(true);
 	    		byType.setDisabled(false);
 	    		
 	    		viewByPackage = true;
 	    		sortFullTree();
 	    	}
 	    });
 	    
 	    byType.addClickHandler(new ClickHandler() {
 	    	public void onClick (ClickEvent event){
 	    		byPackage.setDisabled(false);
 	    		byType.setDisabled(true);
 	    		
 	    		viewByPackage = false;
 	    		sortFullTree();
 	    	}
 	    });
 	    
 	    ToolStrip toolStrip = new ToolStrip(); 
 	    toolStrip.addSpacer(1);
 	    toolStrip.addButton(byPackage);
 	    toolStrip.addButton(byType);
 	    
 		// Left -- Tree Grid
 		TreeNode fullTreeRoot = new TreeNode();
 		fullTree.setRoot(fullTreeRoot);
 		fullTree.setShowRoot(false);
 		
 		TreeNode resultsTreeRoot = new TreeNode();
 	    resultsTree.setRoot(resultsTreeRoot);
 	    resultsTree.setShowRoot(false);
 		
 	    treeGrid.setData(fullTree);
 	    treeGrid.setShowConnectors(true);
 	    treeGrid.setShowRollOver(false);
 	    treeGrid.addSelectionUpdatedHandler(selectedPipefilesHandler);
 	    treeGrid.addNodeContextClickHandler(contextClickHandler);
 	    
 	    // Left 	    
 	    final VLayout left = new VLayout();
 	    left.setShowResizeBar(true);
 	    left.setCanDragResize(true);  
 	    left.setResizeFrom("L", "R"); 
 	    left.setWidth(300);
 	    left.setMinWidth(300);
 	    left.setMaxWidth(600);
 	    left.setAlign(Alignment.CENTER);
 	    left.setPadding(10);
 	    left.setBackgroundColor("#F5DEB3");
 	    
 	    left.addMember(rootCurrent);
 	    left.addMember(searchForm);
 	    left.addMember(toolStrip);
 	    left.addMember(treeGrid);
 	    
 	    left.addResizedHandler(new ResizedHandler() {
 	    	public void onResized (ResizedEvent event){
 	    		query.setWidth(left.getWidth() - 20);
 	    		rootNew.setWidth(left.getWidth() - 20);
 	    	}
 	    });
 	    
 	    rootCurrent.addClickHandler(new ClickHandler() {
 	    	public void onClick(ClickEvent event){
 	    		rootNew.setValue(rootDirectory);
 	    		
 	    		left.removeMember(rootCurrent);
 	    		left.addMember(rootForm, 0);
 	    		
 	    		rootForm.focusInItem(rootNew);
 	    	}
 	    });
 	    
 		rootNew.addKeyUpHandler(new KeyUpHandler() {  
             public void onKeyUp(KeyUpEvent event) {
             	String pressed = event.getKeyName();
             	if (pressed.equals(KeyNames.ENTER)){
             		// Determine if the tree needs to be updated, set the rot directory
 	            	String newRoot = rootNew.getValueAsString();
 	            	boolean updateTree = rootDirectory.equals(newRoot) == false;
 	            	rootDirectory = newRoot;
             		
             		// Update the view
 	            	rootCurrent.setContents(rootDirectory);
 	            	
 	            	left.removeMember(rootForm);
             		left.addMember(rootCurrent, 0);
         	        
         	        // Update the tree if need be
         	        if (updateTree){
         	        	updateFullTree();
         	        }
 	            }
             }  
         });
 	    
 	    
 	    // Main
 		HLayout main = new HLayout();
 		main.setWidth100();
 		
 		main.addMember(left);
 		main.addMember(workarea);
 		
 	    VLayout layout = new VLayout();
 	    layout.setHeight100();
 	    layout.setWidth100();
 	    layout.addMember(header);
 	    layout.addMember(main);
 	    layout.draw();
 
 	    // Tree Initialization
 	    updateFullTree();
 	    
 	    // Group Initialization
 	    getGroups(false);
 	}
 	
 	////////////////////////////////////////////////////////////
 	// Private Function - Calls to Server
 	////////////////////////////////////////////////////////////
 	
 	/**
 	 * Makes the RPC to getFiles
 	 */
 	private void getFiles(){
 		pipes.clear();
 		
 		fileServer.getFiles(rootDirectory, new AsyncCallback<Pipefile[]>() {
 		        public void onFailure(Throwable caught) {
 		        	error("Failed to retrieve files: " + caught.getMessage());
 		        }
 
 		        public void onSuccess(Pipefile[] result) {
 		        	if (result != null) {
 		        		
 		        		LinkedHashSet<String> packageNames = new LinkedHashSet<String>();
 		        		for (Pipefile p : result){
 			        		pipes.put(p.absolutePath, p);
 			        		packageNames.add(p.packageName);
 			        	}
 		        		
 		        		packages = new String[packageNames.size()];
 		        		packages = packageNames.toArray(packages);
 		        	}
 		        	
 		        	sortFullTree();
 		        }
 		    }
         );
 	}
 	
 	/**
 	 * Makes the RPC to getSearchResults
 	 */
 	private void getSearchResults(String query){
 		fileServer.getSearchResults(rootDirectory, query, new AsyncCallback<Pipefile[]>() {
 	        public void onFailure(Throwable caught) {
 	        	error("Failed to retrieve search results: "+ caught.getMessage());
 	        }
 
 	        public void onSuccess(Pipefile[] result) {
 	        	// Clear the ResultsTree
 	        	resultsTree.removeList(resultsTree.getDescendants());
 	        	
 	        	if (result != null){
 	        		for (Pipefile p : result){
 	        			if (pipes.containsKey(p.absolutePath) == false){
 	        				pipes.put(p.absolutePath, p);
 	        			}
 		        		
 		        		TreeNode pipe = new TreeNode(p.name);
 		        		pipe.setAttribute("absolutePath", p.absolutePath);
 		        		pipe.setAttribute("viewable", true);
 		        		
 		        		resultsTree.add(pipe, resultsTree.getRoot());
 	        		}
 	        	}
 	        }
 	    });
 	}
 	
 	/**
 	 * Makes the RPC to updateFile
 	 */
 	private void updateFile(final Pipefile pipe){
 		fileServer.updateFile(rootDirectory, pipe, new AsyncCallback<Void>() {
         	public void onFailure(Throwable caught) {
 		        error("Failed to update file: " + caught.getMessage());
 		    }
 
 		    public void onSuccess(Void result){
 		    	success("Successfully updated " + pipe.name);
 		    	updateFullTree();
 		    }
 		});
 	}
 	
 	/**
 	 * Makes the RPC to removeFiles
 	 */
 	private void removeFiles(final Pipefile[] selected){
 		fileServer.removeFiles(rootDirectory, selected, new AsyncCallback<Void>() {
         	public void onFailure(Throwable caught) {
 		        error("Failed to remove file(s): " + caught.getMessage());
 		    }
 
 		    public void onSuccess(Void result){
 		    	success("Successfully removed " + selected.length + " file(s).");
 		    	updateFullTree();
 		    	basicInstructions();
 		    }
 		});
 	}
 	
 	/**
 	 * Makes the RPC to copyFiles
 	 */
 	private void copyFiles(final Pipefile[] selected, final String packageName){
 		fileServer.copyFiles(rootDirectory, selected, packageName, new AsyncCallback<Void>() {
         	public void onFailure(Throwable caught) {
 		        error("Failed to copy file(s): " + caught.getMessage());
 		    }
 
 		    public void onSuccess(Void result){
 		    	success("Successfully copied " + selected.length + " file(s) to " + packageName + ".");
 		    	updateFullTree();
 		    	basicInstructions();
 		    }
 		});
 	}
 	
 	/**
 	 * Makes the RPC to moveFiles
 	 */
 	private void moveFiles(final Pipefile[] selected, final String packageName){
 		fileServer.moveFiles(rootDirectory, selected, packageName, new AsyncCallback<Void>() {
 			public void onFailure(Throwable caught) {
 		        error("Failed to move file(s): " + caught.getMessage());
 		    }
 
 		    public void onSuccess(Void result){
 		    	success("Successfully moved " + selected.length + " file(s) to " + packageName + ".");
 		    	updateFullTree(); 
 		    	basicInstructions();
 		    }
 		});
 	}
 	
 	/**
 	 * Makes the RPC to getGroups
 	 */
 	private void getGroups(final boolean view){		
 		groups.clear();
 		
 		fileServer.getGroups(rootDirectory, new AsyncCallback<Group[]>() {
 	        public void onFailure(Throwable caught) {
 	        	error("Failed to retrieve groups: " + caught.getMessage());
 	        }
 
 	        public void onSuccess(Group[] result) {
 	        	if (result != null){
 		        	for(Group g : result){
 		        		groups.put(g.name, g);
 		        	}
 	        	}
 	        	
 	        	if (view){
 	        		viewGroups();
 	        	}
 	        }
 	    });
 	}
 	
 	/**
 	 * Makes the RPC to updateGroup
 	 */
 	private void updateGroup(final Group group){		
 		fileServer.updateGroup(rootDirectory, group, new AsyncCallback<Void>() {
 	        public void onFailure(Throwable caught) {
 	        	error("Failed to update group: " + caught.getMessage());
 	        }
 
 	        public void onSuccess(Void result) {
 	        	getGroups(true);
 	        	success("Successfully updated " + group.name + ".");
 	        }
 	    });
 	}
 	
 	/**
 	 * Makes the RPC to getGroups
 	 */
 	private void removeGroups(final Group[] groups){		
 		fileServer.removeGroups(rootDirectory, groups, new AsyncCallback<Void>(){
 			public void onFailure(Throwable caught) {
 	        	error("Failed to remove groups: "+ caught.getMessage());
 	        }
 
 	        public void onSuccess(Void result) {
 	        	getGroups(true);
 	        	success("Successfully removed " + groups.length + " file(s).");
 	        }
 		});
 	}
 	
 	/**
 	 * Makes the HTTP request to download
 	 */
 	private void downloadFiles(Pipefile[] selected){
 		int length = selected.length;
 		
 		String url = "serverlibrarymanager/download?n=" + length;
 		for(int i = 0; i < length; i++){
 			String filename = selected[i].absolutePath;
 			url += "&filename_" + i + "=" + URL.encode(filename);
 		}
 		
 		Window.open(url, "downloadWindow", "");
 	}
 	
 	/**
 	 * Makes the HTTP request to upload
 	 */
 	private void uploadFiles(){
 		// TODO
 	}
 	
 	////////////////////////////////////////////////////////////
 	// Private Functions - Tree
 	////////////////////////////////////////////////////////////
 	
 	private void sortFullTree(){
 		// Clear the map
 		nodes.clear();
 		
 		// Clear the full tree
 		fullTree.removeList(fullTree.getDescendants());
 		
 		for (Pipefile p : pipes.values()){    		    		
     		String primaryName, secondaryName;	
     		
     		if (viewByPackage) {
     			primaryName = p.packageName;
     			secondaryName = p.type;
     		} 
     		else {
     			primaryName = p.type;
     			secondaryName = p.packageName;
     		}
     		
     		String primaryKey = primaryName;
     		String secondaryKey = primaryName + " > " + secondaryName;
     		
     		
     		// Primary Node
     		TreeNode primaryNode;
     		if (nodes.containsKey(primaryKey)){
     			primaryNode = nodes.get(primaryKey);
     		} else {
     			primaryNode = new TreeNode(primaryName);
     			primaryNode.setAttribute("key", primaryKey);
     			primaryNode.setAttribute("viewable", false);
     			primaryNode.setAttribute("moveHere", viewByPackage);
     			
     			fullTree.add(primaryNode, fullTree.getRoot());
     			
     			nodes.put(primaryKey, primaryNode);
     		}
     		
     		// Secondary Node
     		TreeNode secondaryNode;
     		if (nodes.containsKey(secondaryKey)){
     			secondaryNode = nodes.get(secondaryKey);
     		} else {
     			secondaryNode = new TreeNode(secondaryName);
     			secondaryNode.setAttribute("key", secondaryKey);
     			secondaryNode.setAttribute("viewable", false);
     			secondaryNode.setAttribute("moveHere", !viewByPackage);
     			
     			fullTree.add(secondaryNode, primaryNode);
     			
     			nodes.put(secondaryKey, secondaryNode);
     		}
     		
     		// Pipefile node
     		TreeNode pipeNode = new TreeNode(p.name);
     		pipeNode.setAttribute("key", secondaryKey + " > " + p.name);
     		pipeNode.setAttribute("absolutePath", p.absolutePath);
     		pipeNode.setAttribute("viewable", true);
     		
     		fullTree.add(pipeNode, secondaryNode);
 		}
 	}
 	
 	/**
 	 *  Updates Package Tree and Module Tree based on the rootDirectory
 	 *  Ensure tree is displayed
 	 */
 	private void updateFullTree(){
 		treeGrid.setData(fullTree);
 		getFiles();
 	}
 	
 	/**
 	 *  Updates ResultsTree based on the query
 	 *  Ensure results are displayed
 	 */
 	private void updateResultsTree(final String query){
 		treeGrid.setData(resultsTree);
 		getSearchResults(query);
 	}
 	
 	////////////////////////////////////////////////////////////
 	// Private Functions - Workarea - File Operations
 	////////////////////////////////////////////////////////////
 	
 	/**
 	 *  Adds file operations (download, remove, copy, move) to the workarea 
 	 */
 	private void fileOperationsActions(final Pipefile[] selected){
 		// Title
 		Label workareaTitle = new Label("File Operations");
 		workareaTitle.setHeight(20);
 		workareaTitle.setStyleName("workarea-title");
 		workarea.addMember(workareaTitle);
 		
 		// Actions
 		final ComboBoxItem combo = new ComboBoxItem();
 		combo.setTitle("To Package"); 
 		combo.setValueMap(packages);
 		
 		DynamicForm form = new DynamicForm();
 		form.setItems(combo);		
 		
 		Button remove = new Button("Remove");
 		Button download = new Button("Download");
 		Button copy = new Button("Copy");
 		Button move = new Button("Move");
 		
 		remove.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event){
 				removeFiles(selected);
 			}
 		});
 		
 		download.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event){
 				downloadFiles(selected);
 			}
 		});
 		
 		copy.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event){
 				copyFiles(selected, combo.getDisplayValue());
 			}
 		});
 		
 		move.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event){
 				moveFiles(selected, combo.getDisplayValue());
 			}
 		});
 		
 		HLayout copyMoveButtons = new HLayout(10);
 		copyMoveButtons.setAlign(Alignment.CENTER);
 		copyMoveButtons.addMember(copy);
 		copyMoveButtons.addMember(move);
 		
 		VLayout copyMoveLayout = new VLayout(5);
 		copyMoveLayout.setPadding(5);
 		copyMoveLayout.setWidth(250);
 		copyMoveLayout.setHeight(80);
 		copyMoveLayout.setShowEdges(true);
 		copyMoveLayout.setDefaultLayoutAlign(Alignment.CENTER);
 		copyMoveLayout.addMember(copyMoveButtons);
 		copyMoveLayout.addMember(form);
 		
 		VLayout downloadLayout = new VLayout();
 		downloadLayout.setPadding(5);
 		downloadLayout.setWidth(110);
 		downloadLayout.setHeight(30);
 		downloadLayout.setShowEdges(true);
 		downloadLayout.addMember(download);
 		
 		VLayout removeLayout = new VLayout();
 		removeLayout.setPadding(5);
 		removeLayout.setWidth(110);
 		removeLayout.setHeight(30);
 		removeLayout.setShowEdges(true);
 		removeLayout.addMember(remove);
 		
 		final HLayout actions = new HLayout(10);
 		actions.setHeight(100);
 		actions.addMember(downloadLayout);
 		actions.addMember(removeLayout);
 		actions.addMember(copyMoveLayout);
 		
 		workarea.addMember(actions);
 	}
 	
 	/**
 	 *  Adds a list of the selected files to the workarea
 	 */
 	private void fileOperationsSelectedFiles(final Pipefile[] selected){
 		// Title
 		Label selectedTitle = new Label("Selected Files");
 		selectedTitle.setHeight(20);
 		selectedTitle.setStyleName("workarea-title");
 		workarea.addMember(selectedTitle);
  		
 		// Selected Files
 		ListGrid grid = new ListGrid();
 		grid.setWidth(600);
 		ListGridField nField = new ListGridField("name", "Name");
 		ListGridField pField = new ListGridField("packageName", "Package");
 		ListGridField tField = new ListGridField("type", "Type");
 		grid.setFields(nField, pField, tField);
         
 		ListGridRecord[] records = new ListGridRecord[selected.length];
 		
 		for(int i = 0; i < selected.length; i++){			
 			Pipefile pipe = selected[i];
 			
 			ListGridRecord record = new ListGridRecord();
 			record.setAttribute("name", pipe.name);
 			record.setAttribute("packageName", pipe.packageName);
 			record.setAttribute("type", pipe.type);
 			
 			records[i] = record;
 		}
 		
 		grid.setData(records);
 		
 		workarea.addMember(grid);
 	}
 	
 	/**
 	 *  Sets workarea to file Operations (download, remove, copy, move)
 	 *    and a list of the selected files
 	 */
 	private void fileOperations(final Pipefile[] selected){
 		clearWorkarea();
 		
 		fileOperationsActions(selected);
 		fileOperationsSelectedFiles(selected);
 	}
 	
 	////////////////////////////////////////////////////////////
 	// Private Functions - Workarea - View File
 	////////////////////////////////////////////////////////////
 	
 	private String loopFound = "<b>LOOP IN GROUP DEPENDENCIES</b>";
 	
 	private String groupHint = "Comma separated list<br/>" +
 			"Group syntax = " + GroupSyntax.start + "groupName" + GroupSyntax.end +
 			"<br/>Groups expanded below";
 	
 	private String expandGroupsRecursive(String originalGroup, String list, String base){
 		String ret = "";
 		
 		String[] agents = list.split(",");
 		
 		for(String agent : agents){
 			// Trim whitespace
 			agent = agent.trim();
 			
 			if (GroupSyntax.isGroup(agent)){
 				String group = GroupSyntax.agentToGroup(agent);
 				
 				if (group.equals(originalGroup)){
 					ret += base + loopFound + "<br/>";
 					return ret;
 				}
 				else if (groups.containsKey(group)){
 					Group g = groups.get(group);
 					
 					ret += base + group + " = " + g.users + "<br/>";
 					ret += expandGroupsRecursive(originalGroup, g.users, base + "&nbsp;&nbsp;&nbsp;&nbsp;");
 				} 
 				else {
 					ret += base + group + " is undefined" + "<br/>";;
 				}
 			}
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 *  Updates a static text item with the groups expanded
 	 */
 	private boolean expandGroups(String groupName, String agentList, StaticTextItem info){
 		String msg = expandGroupsRecursive(groupName, agentList, "");
 		
 		info.setValue(msg);
 		
 		return msg.contains(loopFound);
 	}
 	
 	/**
 	 *  Sets the workarea with file operations and a form to edit
 	 */
 	private void viewFile(final Pipefile pipe){
 		clearWorkarea();
 		
 		lastPipefile = pipe;
 		backToLastPipefile.hide();
 		
 		
 		// File Operations
 		fileOperationsActions(new Pipefile[] {pipe});
 		
 		// Title
 		Label editFileTitle = new Label("Edit File");
 		editFileTitle.setHeight(40);
 		editFileTitle.setStyleName("workarea-title");
 		workarea.addMember(editFileTitle);
 		
 		// Edit File
 		final DynamicForm form = new DynamicForm();
 		form.setCanEdit(true);
         form.setPadding(10);
         form.setStyleName("edit-form");
 		
         int width = 400;
         
 		TextItem name = new TextItem();
 		name.setTitle("Name");
 		name.setName("name");
 		name.setWidth(width);
 		
 		TextItem packageName = new TextItem();
 		packageName.setTitle("Package");
 		packageName.setName("package");
 		packageName.setWidth(width);
 		
 		StaticTextItem type = new StaticTextItem();
 		type.setTitle("Type");
 		type.setName("type");
 		
 		TextAreaItem description = new TextAreaItem();
 		description.setTitle("Description");
 		description.setName("description");
 		description.setWidth(width);
 		
 		TextItem tags = new TextItem();
 		tags.setTitle("Tags");
 		tags.setName("tags");
 		tags.setWidth(width);
 		tags.setHint("Comma separated list");
 	
 		TextAreaItem values = new TextAreaItem();
 		values.setTitle("Values");
 		values.setName("values");
 		values.setWidth(width);
 		values.setHint("Newline separated list<br/> Prefix = "); // TODO
 		
 		TextItem formatType = new TextItem();
 		formatType.setTitle("Format Type");
 		formatType.setName("formatType");
 		formatType.setWidth(width);
 		
 		TextItem location = new TextItem();
 		location.setTitle("Location");
 		location.setName("location");
 		location.setWidth(width);
 		location.setHint("Prefix = "); // TODO
 		
 		TextItem uri = new TextItem();
 		uri.setTitle("URI");
 		uri.setName("uri");
 		uri.setWidth(width);
 		
 		final TextAreaItem access = new TextAreaItem();
 		access.setTitle("Access");
 		access.setName("access");
 		access.setWidth(width);
 		access.setHeight(50);
 		access.setHint(groupHint);
 		
 		final StaticTextItem accessInfo = new StaticTextItem();
 		accessInfo.setTitle("Access Groups Expanded");
 		
 		access.addChangedHandler(new ChangedHandler(){
 			public void onChanged(ChangedEvent event){
 				expandGroups("", access.getValueAsString(), accessInfo);
 			}
 		});
 		
 		form.setFields(name, packageName, type, description, tags, location, uri, values, formatType, access, accessInfo);
 		form.setValue("name", pipe.name);
 		form.setValue("package", pipe.packageName);
 		form.setValue("type", pipe.type);
 		form.setValue("description", pipe.description);
 		form.setValue("tags",pipe.tags);
 		form.setValue("access", pipe.access);
 		expandGroups("", access.getValueAsString(), accessInfo);
 		
 		if(pipe.type.equals("Data")){
 			//TODO: fix this stuff!!!!
 			String valString = "";
 			RegExp split = RegExp.compile("\n", "m");
 			SplitResult vals = split.split(pipe.values);
 			RegExp re = RegExp.compile(".*://.*/(.*)");
 			for (int j = 0; j<vals.length(); j++){
 				MatchResult m = re.exec(vals.get(j));
 				if(m!= null){
 					valString += m.getGroup(1) + "\n";
 				}
 			}
 			form.setValue("values", valString);
 			form.setValue("formatType", pipe.formatType);
 		}
 		else{
 			form.hideItem("values");
 			form.hideItem("formatType");
 		}
 		
 		final String loc;
 		RegExp re = RegExp.compile(".*://.*/(.*)");
 		MatchResult m = re.exec(pipe.location);
 		if(m!= null)
 			loc = m.getGroup(1); 
 		else
 			loc = "";
 		if(pipe.type.equals("Modules")){
 			form.setValue("location", loc);
 		}
 		else
 			form.hideItem("location");
 		
 		if(pipe.type.equals("Modules") || pipe.type.equals("Groups"))
 			form.setValue("uri", pipe.uri);
 		else
 			form.hideItem("uri");
 		
 		// Update Button
 		Button update = new Button("Update");
 		update.addClickHandler( new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				RegExp re = RegExp.compile("(.*://.*/)(.*)");
 				pipe.name = form.getValueAsString("name");
 				pipe.packageName = form.getValueAsString("package");
 				pipe.type = form.getValueAsString("type");
 				pipe.description = form.getValueAsString("description");
 				pipe.tags = form.getValueAsString("tags");
 				pipe.access = form.getValueAsString("access");
 				if(pipe.type.equals("Data")){
 					String valString = "";
 					RegExp split = RegExp.compile("\n", "m");
 					SplitResult vals = split.split(form.getValueAsString("values"));
 					MatchResult m = re.exec(pipe.values);
 					if(m !=null)
 						for (int j = 0; j<vals.length(); j++){
 							if(vals.get(j).length()==0)
 								continue;
 							valString += m.getGroup(1) + vals.get(j) +  "\n";
 						}
 					pipe.values = valString;
 					pipe.formatType = form.getValueAsString("formatType");
 				}
 				if(pipe.type.equals("Modules")){
 					pipe.location = loc + form.getValueAsString("location");
 				}
 				if(pipe.type.equals("Modules") || pipe.type.equals("Groups"))
 					pipe.uri = form.getValueAsString("uri");
 				
 				
 				updateFile(pipe);
 		    }
 		});
 		
 		Button cancel = new Button("Cancel");
 		cancel.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent event){
 				basicInstructions();
 			}
 		});
 		
 		HLayout buttonRow = new HLayout(10);
 		buttonRow.addMember(update);
 		buttonRow.addMember(cancel);
 		
 		workarea.addMember(form);
 		workarea.addMember(buttonRow);
 	}
 	
 	////////////////////////////////////////////////////////////
 	// Private Functions - Workarea - Groups
 	////////////////////////////////////////////////////////////
 	
 	/**
 	 *  Sets workarea to a list of the groups 
 	 */
 	private void viewGroups(){
 		clearWorkarea();
 		
 		int width = 600;
 		
 		// Title
 		Label title = new Label("Manage Groups");
 		title.setHeight(30);
 		title.setStyleName("workarea-title");
 		
 		// Description
 		Label description = new Label(
 			"Double click a group to edit it " +
 			"or mark the checkboxes of groups you want to delete then click \"Remove Selected Groups\"<br/>" +
 			"Groups without a checkbox are in use and cannot be deleted. More info can be found in the edit screen."
 		);
 		description.setHeight(50);
 		description.setStyleName("workarea-description");
 		
 		// Group Grid
 		ListGridField nField = new ListGridField("name", "Name");  
         ListGridField uField = new ListGridField("users", "Users");
         
 		final ListGrid grid = new ListGrid();
 		grid.setFields(nField, uField);
 		grid.setWidth(width);
 		grid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
 		
         Collection<Group> groupsCollection = groups.values();
         
 		ListGridRecord[] records = new ListGridRecord[groupsCollection.size()];
 
 		int i = 0;
 		for(Group group : groupsCollection){
 			ListGridRecord record = new ListGridRecord();
 			record.setAttribute("name", group.name);
 			record.setAttribute("users", group.users);
 			
 			if (group.canRemove == false){
 				record.setAttribute("canSelect", false);
 			}
 			
 			records[i++] = record;
 		}
 		
 		grid.setData(records);
 		
 		grid.addRecordDoubleClickHandler(new RecordDoubleClickHandler(){
 			public void onRecordDoubleClick(RecordDoubleClickEvent event){
 				Record r = event.getRecord();
 				
 				String groupName = r.getAttribute("name");
 				Group g = groups.get(groupName);
 				
 				editGroup(g);
 			}
 		});
 		
 		
 		// ToolStrip
 		Label total = new Label(records.length + " groups");
 		
 		ToolStripButton newGroup = new ToolStripButton();
 		newGroup.setIcon("[SKIN]/actions/add.png");  
 		newGroup.setPrompt("Add New Group"); 
 		newGroup.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent event){
 				Group newGroup = new Group();
 				editGroup(newGroup);
 			}
 		});
 
 		ToolStripButton removeGroups = new ToolStripButton();
 		removeGroups.setIcon("[SKIN]/actions/remove.png");  
 		removeGroups.setPrompt("Remove Selected Groups"); 
 		removeGroups.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent event){
 				ListGridRecord[] selected = grid.getSelectedRecords();
 				
 				if (selected != null && selected.length > 0){
 					Group[] toRemove = new Group[selected.length];
 					
 					int i = 0;
 					for(ListGridRecord r : selected){
 						String name = r.getAttribute("name");
 						toRemove[i++] = groups.get(name);
 					}
 					
 					removeGroups(toRemove);
 				}
 			}
 		});
 		
 		ToolStrip bottom = new ToolStrip();
 		bottom.setWidth100();
 		bottom.addMember(total);
 		bottom.addFill();
 		bottom.addButton(newGroup);
 		bottom.addButton(removeGroups);
 		
 		grid.setGridComponents(new Object[] {  
 				ListGridComponent.HEADER,   
                 ListGridComponent.BODY,   
                 bottom  
         });
 		
 		workarea.addMember(title);
 		workarea.addMember(description);
 		workarea.addMember(grid);
 	}
 
 	/**
 	 *  Sets workarea to a form to edit a group 
 	 */
 	private void editGroup(final Group g){
 		clearWorkarea();
 		
 		final boolean newGroup = (g.groupId == -1);
 		int width = 400;
 		
 		// Title
 		Label title = new Label(newGroup ? "New Group" : "Edit Group");
 		title.setHeight(30);
 		title.setStyleName("workarea-title");
 		
 		// Form for editing
 		final TextItem name = new TextItem("Name");
 		name.setValue(g.name);
 		name.setWidth(width);
 		
 		final TextAreaItem agents = new TextAreaItem("Agents");
 		agents.setValue(g.users);
 		agents.setWidth(width);
 		agents.setHeight(50);
 		agents.setHint(groupHint);
 		
 		final StaticTextItem agentsInfo = new StaticTextItem();
 		agentsInfo.setTitle("Agent Groups Expanded");
 		
 		
 		agents.addChangedHandler(new ChangedHandler(){
 			public void onChanged(ChangedEvent event){
 				expandGroups(name.getValueAsString(), agents.getValueAsString(), agentsInfo);
 			}
 		});
 		
 		DynamicForm form = new DynamicForm();
 		form.setFields(name, agents, agentsInfo);
 		form.setPadding(10);
 		expandGroups(name.getValueAsString(), agents.getValueAsString(), agentsInfo);
 		
 		Button update = new Button("Update");
 		update.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent event){				
 				g.name = name.getValueAsString();
 				g.users = agents.getValueAsString();
 				boolean loop = expandGroups(g.name, g.users, agentsInfo);
 				
 				if (g.name == null || g.name.equals("")){
 					SC.say("Name cannot be blank"); 
 				}
 				else if (g.users == null || g.users.equals("")){
 					SC.say("Users cannot be blank"); 
 				}
 				else if (newGroup && groups.containsKey(g.name)){
 					SC.say("Name (" + g.name + ") is already in use. Please choose another name."); 
 				}
 				else if (loop){
 					SC.say("Loop in group dependencies. Please remove this loop."); 
 				}
 				else {
 					updateGroup(g);
 				}
 			}
 		});
 		
 		Button cancel = new Button("Cancel");
 		cancel.addClickHandler(new ClickHandler(){
 			public void onClick(ClickEvent event){
 				viewGroups();
 			}
 		});
 		
 		HLayout buttonRow = new HLayout(10);
 		buttonRow.addMember(update);
 		buttonRow.addMember(cancel);
 		
 		// TODO, button to view all files that use this group
 		
 		workarea.addMember(title);
 		workarea.addMember(form);
 		workarea.addMember(buttonRow);
 	}
 	
 	////////////////////////////////////////////////////////////
 	// Private Functions - Workarea - Import
 	////////////////////////////////////////////////////////////
 	
 	  private IUploader.OnFinishUploaderHandler onFinishUploaderHandler = new IUploader.OnFinishUploaderHandler() {
 		  public void onFinish(IUploader uploader) {
 			  if (uploader.getStatus() == Status.SUCCESS) {
 				  UploadedInfo info = uploader.getServerInfo();
 				  success("Successfully Uploaded " + info.name);
 			  }
 		  }
 	  };
 	
 	/**
 	 *  Sets workarea to an import form
 	 */
 	private void importForm(){
 		// TODO
 		// Allow user to select files/folders
 		//   Checkbox for recursive
 		// Allow user to supply a url
 		// Import button
 		//   On click, import the files, go back to basic instructions
 		clearWorkarea();
 		
 		/*SingleUploader uploader = new SingleUploader();
 		uploader.addOnFinishUploadHandler(onFinishUploaderHandler);
 		uploader.setServletPath(uploader.getServletPath() + "?root=" + URL.encode(rootDirectory));
 		
 		workarea.addMember(uploader);*/
 		final DynamicForm uploadForm = new DynamicForm();
 		uploadForm.setSize("54px", "147px");
 		uploadForm.setEncoding(Encoding.MULTIPART);			//need to remain to work corectly
 		final UploadItem fileItem = new UploadItem("image");		//need to remain to work corectly
 		uploadForm.setAction(GWT.getModuleBaseURL()+"upload");		//need to remain to work corectly
 		IButton uploadButton = new IButton("Attachment");
 	        uploadButton.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler()
 	        {
 	            @Override
 	            public void onClick(
 	                    com.smartgwt.client.widgets.events.ClickEvent event) {
 	                uploadForm.submitForm();				//need to remain to work corectly
 	            }
 	        });
 		uploadForm.setItems(fileItem);					//need to remain to work corectly
 		workarea.setMembers(uploadForm, uploadButton);
 	}
 	
 	////////////////////////////////////////////////////////////
 	// Private Functions - Workarea - Home
 	////////////////////////////////////////////////////////////
 	
 	/**
 	 *  Sets workarea to the basic instructions
 	 */
 	private void basicInstructions(){
 		clearWorkarea();
 		
 		Label root = new Label("This is your root directory. Click on it to edit.");
 		root.setIcon("leftarrow.jpg");
 		root.setIconWidth(30);
 		root.setHeight(40);
 		root.setStyleName("workarea-description");
 		root.setValign(VerticalAlignment.TOP);
 		
 		Label search = new Label("Use this to search for files. Search must be at least 2 letters.");
 		search.setIcon("leftarrow.jpg");
 		search.setIconWidth(30);
 		search.setHeight(30);
 		search.setStyleName("workarea-description");
 		search.setValign(VerticalAlignment.TOP);
 		
 		Label view = new Label("Use these buttons to reorder the hierarchy");
 		view.setIcon("leftarrow.jpg");
 		view.setIconWidth(30);
 		view.setHeight(50);
 		view.setStyleName("workarea-description");
 		view.setValign(VerticalAlignment.TOP);
 		
 		workarea.addMember(root);
 		workarea.addMember(search);
 		workarea.addMember(view);
 		
 		Label files = new Label(
 			"Select any file to view and edit its properties and perform file operations<br/>" +
 			"You can also select multiple files at once and perform the same operation on all of them"
 		);
 		files.setHeight(60);
 		files.setStyleName("workarea-description");
 		files.setValign(VerticalAlignment.TOP);
 		
 		workarea.addMember(files);
 	}
 	
 	////////////////////////////////////////////////////////////
 	// Private Functions - Workarea - Messages
 	////////////////////////////////////////////////////////////
 	
 	/**
 	 *  Prints an error message to the top of workarea
 	 */
 	private void error(String message){
 		SC.say(message);
 	}
 	
 	/**
 	 *  Prints a success message to the top of workarea
 	 */
 	private void success(String msg){
 		message.setContents(msg);
 	}
 	
 	////////////////////////////////////////////////////////////
 	// Private Functions - Workarea - Clear
 	////////////////////////////////////////////////////////////
 	
 	/**
 	 *  Clears the workarea
 	 */
 	private void clearWorkarea(){
 		if (lastPipefile != null){
 			if (pipes.containsValue(lastPipefile)){
 				backToLastPipefile.setTitle("Back to " + lastPipefile.name);
 				backToLastPipefile.show();
 			} else {
 				lastPipefile = null;
 			}
 		}
 		
 		workarea.removeMembers(workarea.getMembers());
 	}
 }
