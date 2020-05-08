 /*
 *
 *  JMoney - A Personal Finance Manager
 *  Copyright (c) 2004 Nigel Westbury <westbury@users.sourceforge.net>
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
 
 package net.sf.jmoney.views;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Vector;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.part.*;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.jface.action.*;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.*;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.SWT;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 
 import net.sf.jmoney.Constants;
 import net.sf.jmoney.IBookkeepingPageListener;
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.model2.CapitalAccount;
 import net.sf.jmoney.model2.AccountAddedEvent;
 import net.sf.jmoney.model2.AccountDeletedEvent;
 import net.sf.jmoney.model2.CapitalAccountImpl;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.SessionChangeAdapter;
 import net.sf.jmoney.model2.SessionChangeListener;
 import net.sf.jmoney.model2.SessionReplacedEvent;
 
 /**
  * This class implements a workbench view that contains the
  * main navigation tree.
  * <p>
  */
 
 public class NavigationView extends ViewPart {
     public static final String ID_VIEW =
         "net.sf.jmoney.views.NavigationView"; //$NON-NLS-1$
 
 	private TreeViewer viewer;
 	private DrillDownAdapter drillDownAdapter;
 	private Action newAccountAction;
 	private Action deleteAccountAction;
 
 	private AccountsNode accountsRootNode;
 	
 	private Session session;
 	
 	/*
 	 * The content provider class is responsible for
 	 * providing objects to the view. It can wrap
 	 * existing objects in adapters or simply return
 	 * objects as-is. These objects may be sensitive
 	 * to the current input of the view, or ignore
 	 * it and always show the same content 
 	 * (like Task List, for example).
 	 */
 	 
 	
 	
 	
 	
 	class TreeNode implements IAdaptable {
 		private String name;
 		private Image image;
 		private TreeNode parent;
 		private String parentId;
 		protected ArrayList children = null;
 		private Vector pageListeners = new Vector();
 		
 		public TreeNode(String name, Image image, TreeNode parent) {
 			this.name = name;
 			this.image = image;
 			this.parent = parent;
 		}
 		public TreeNode(String name, Image image, String parentId) {
 			this.name = name;
 			this.image = image;
 			this.parentId = parentId;
 		}
 		public String getName() {
 			return name;
 		}
 		public TreeNode getParent() {
 			return parent;
 		}
 		public String toString() {
 			return getName();
 		}
 		public Object getAdapter(Class key) {
 			return null;
 		}
 		public Image getImage() {
 			return image;
 		}
 		public void addChild(Object child) {
 			if (children == null) {
 				children = new ArrayList();
 			}
 			children.add(child);
 		}
 		
 		public void removeChild(Object child) {
 			children.remove(child);
 		}
 		public Object [] getChildren() {
 			if (children == null) {
 				return new Object[0];
 			} else {
 				return children.toArray(new Object[children.size()]);
 			}
 		}
 		public boolean hasChildren() {
 			return children != null && children.size()>0;
 		}
 		/**
 		 * @return
 		 */
 		public Object getParentId() {
 			return parentId;
 		}
 
 		/**
 		 * @param parentNode
 		 */
 		public void setParent(TreeNode parent) {
 			this.parent = parent;
 		}
 
 		/**
 		 * @param pageListener
 		 */
 		public void addPageListener(IBookkeepingPageListener pageListener) {
 			pageListeners.add(pageListener);
 		}
 		/**
 		 * @return An array of objects that implement the IBookkeepingPageListener
 		 * 		interface.  The returned value is never null but the Vector may
 		 * 		be empty if there are no listeners for this node.
 		 */
 		public Vector getPageListeners() {
 			return pageListeners;
 		}
 	}
 
 	// TODO: Should the list of accounts be cached by the TreeNode object?
 	// Or should we change this code and send the request to the datastore each time the tree view requests
 	// a list of accounts or sub-accounts?
 	class AccountsNode extends TreeNode {
 		public AccountsNode(String name, Image image, TreeNode parent) {
 			super(name, image, parent);
 			setSession(JMoneyPlugin.getDefault().getSession());
 		}
 		
 		private void setSession(Session session) {
 			// Initialize with list of top level accounts from the session.
 			if (children == null) {
 				children = new ArrayList();
 			} else {
 				children.clear();
 			}
 			if (session != null) {
 				for (Iterator iter = session.getCapitalAccountIterator(); iter.hasNext(); ) {
 					CapitalAccount account = (CapitalAccount)iter.next();
 					children.add(account);
 				}
 			}
 		}
 	}
 
 	class ViewContentProvider implements IStructuredContentProvider, 
 										   ITreeContentProvider {
 		/**
 		 * In fact the input does not change because we create our own node object
 		 * that acts as the root node.  Certain nodes below the root may get
 		 * their data from the model.  The accountsNode object does this.
 		 */
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			// The input never changes so we don't do anything here.
 		}
 		public void dispose() {
 		}
 		public Object[] getElements(Object parent) {
 			return getChildren(parent);
 		}
 		public Object getParent(Object child) {
 			if (child instanceof TreeNode) {
 				return ((TreeNode)child).getParent();
 			} else if (child instanceof CapitalAccount) {
 				CapitalAccount account = (CapitalAccount)child;
 				if (account.getParent() != null) {
 					return account.getParent();
 				} else {
 					return accountsRootNode;
 				}
 			}
 			return null;
 		}
 		public Object [] getChildren(Object parent) {
 			if (parent instanceof TreeNode) {
 				return ((TreeNode)parent).getChildren();
 			} else if (parent instanceof CapitalAccount) {
 				CapitalAccount account = (CapitalAccount)parent;
 				int count = 0;
				for (Iterator iter = account.getSubAccountIterator(); iter.hasNext(); ) {
 					count++;
 				}
 				Object children[] = new Object[count];
 				int i = 0;
 				for (Iterator iter = account.getSubAccountIterator(); iter.hasNext(); ) {
 					children[i++] = iter.next();
 				}
 				return children;
 			};
 			return new Object[0];
 		}
 		public boolean hasChildren(Object parent) {
 			if (parent instanceof TreeNode) {
 				return ((TreeNode)parent).hasChildren();
 			} else if (parent instanceof CapitalAccount) {
 				CapitalAccount account = (CapitalAccount)parent;
 				return account.getSubAccountIterator().hasNext();
 			}
 			return false;
 		}
 
 	}
 	class ViewLabelProvider extends LabelProvider {
 
 		public String getText(Object obj) {
 			return obj.toString();
 		}
 		public Image getImage(Object obj) {
 			if (obj instanceof TreeNode) {
 				return ((TreeNode)obj).getImage();
 			} else if (obj instanceof CapitalAccount) {
 				return Constants.ACCOUNT_ICON;
 			} else {
 				throw new RuntimeException("");
 			}
 		}
 	}
 	class NameSorter extends ViewerSorter {
 	}
 
 	private SessionChangeListener listener =
 		new SessionChangeAdapter() {
 		public void sessionReplaced( SessionReplacedEvent event ) {
 			NavigationView.this.session = event.getNewSession();
 			if (NavigationView.this.session == null) {
 				// TODO: make folder contents and tree empty
 			} else {
 				accountsRootNode.setSession(event.getNewSession());
 				viewer.refresh(accountsRootNode, false);
 			}
 		}
 		public void accountAdded(AccountAddedEvent event) {
 			// TODO process sub-accounts correctly
 			if (event.getNewAccount() instanceof CapitalAccount) {
 				accountsRootNode.addChild(event.getNewAccount());
 				refreshViewer ();
 			}
 		}
 		public void accountDeleted(AccountDeletedEvent event) {
 			// TODO process sub-accounts correctly
 			if (event.getOldAccount() instanceof CapitalAccount) {
 				accountsRootNode.removeChild(event.getOldAccount());
 				refreshViewer ();
 			}
 		}
 	};
 	
 	/**
 	 * Refresh the viewer Object thread-safe. 
 	 *
 	 */
 	private void refreshViewer () {
         Display.getDefault().syncExec( new Runnable() {
             public void run() {
                 viewer.refresh(accountsRootNode, false);
             }
         });
 	}
 
 private Map idToNodeMap = new HashMap();
 	private Map pageListenerAndNodeIdMap = new HashMap();
 	private Vector accountPageListeners = new Vector();
 
 	private IMemento memento;
 		
 
 	/**
 	 * The constructor.
 	 */
 	public NavigationView() {
 	}
 
     public void init(IViewSite site, IMemento memento) throws PartInitException {
         init(site);
         
     	// Some objects owned by the JMoneyPlugin object need to have
     	// their state preserved across sessions.  However, Eclipse
     	// allows only views to preserve state.
     	// We get around this by making it the responsibility of this
     	// navigation view to create a child memento and pass that
     	// on to the plug-in object so that the plug-in object can
     	// preserve its state.
         if (memento != null) {
 //        	JMoneyPlugin.getDefault().init(memento.getChild("JMoneyPlugin"));
         }
     	
         // init is called before createPartControl,
         // and the objects that need the memento are not
         // created until createPartControl is called so we save
         // the memento now for later use.
         this.memento = memento; 
     }
     
     public void saveState(IMemento memento) {
     	// Some objects owned by the JMoneyPlugin object need to have
     	// their state preserved across sessions.  However, Eclipse
     	// allows only views to preserve state.
     	// We get around this by making it the responsibility of this
     	// navigation view to create a child memento and pass that
     	// on to the plug-in object so that the plug-in object can
     	// preserve its state.
 
     	// Ideally plug-in objects should themselves
     	// be able to save and restore state, because objects affect
     	// many views and so it is not clear which view should save
     	// the object.  Anyway, this is an Eclipse design issue and
     	// does not affect us because we always have this navigation
     	// view.
 //    	JMoneyPlugin.getDefault().saveState(memento.createChild("JMoneyPlugin"));
     	
     	// Give each extension a child memento into which it can
     	// save state.
 		for (Iterator iter = pageListenerAndNodeIdMap.keySet().iterator(); iter.hasNext(); ) {
 			IBookkeepingPageListener pageListener = (IBookkeepingPageListener)iter.next();
             IMemento childMemento = memento.createChild(pageListener.getClass().getName());
             pageListener.saveState(childMemento);
 		}
     }
     
 	/**
 	 * This is a callback that will allow us
 	 * to create the viewer and initialize it.
 	 */
 	public void createPartControl(Composite parent) {
 
 		// Load the extensions
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sf.jmoney.pages");
 		IExtension[] extensions = extensionPoint.getExtensions();
 		for (int i = extensions.length-1; i>=0; i--) {
 			IConfigurationElement[] elements =
 				extensions[i].getConfigurationElements();
 			for (int j = 0; j < elements.length; j++) {
 				if (elements[j].getName().equals("node")) {
 					
 					String label = elements[j].getAttribute("label");
 					String icon = elements[j].getAttribute("icon");
 					String id = elements[j].getAttribute("id");
 					String parentNodeId = elements[j].getAttribute("parent");
 					
 					if (id != null && id.length() != 0) {
 						String fullNodeId = extensions[i].getNamespace() + '.' + id;
 						Image image = null;
 						if (icon != null) {
 							image = JMoneyPlugin.createImage(icon);
 						}
 						
 						TreeNode node = new TreeNode(label, image, parentNodeId);
 						idToNodeMap.put(fullNodeId, node);
 					}
 				}
 				if (elements[j].getName().equals("pages")) {
 					try {
 						Object listener = elements[j].createExecutableExtension("class");
 						if (listener instanceof IBookkeepingPageListener) {
 							IBookkeepingPageListener pageListener = (IBookkeepingPageListener)listener;
 
     	 					IMemento pageMemento = null; 
     	 					if (memento != null) {
     	 						pageMemento = memento.getChild(pageListener.getClass().getName());
     	 					}
     	 					pageListener.init(pageMemento);
 
 							String nodeId = elements[j].getAttribute("node");
 							if (nodeId != null && nodeId.length() != 0) {
 								pageListenerAndNodeIdMap.put(pageListener, nodeId);
 							} else {
 								// No 'node' attribute so see if we have
 								// an 'extendable-property-set' attribute.
 								// (This means the page should be supplied if
 								// the node represents an object that contains
 								// the given property set).
 								String nodeClass = elements[j].getAttribute("extendable-property-set");
 								if (nodeClass != null && nodeClass.equals("net.sf.jmoney.capitalAccount")) {
 									accountPageListeners.add(pageListener);
 								}
 							}
 						}
 					} catch (CoreException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		
 		// Pass two does two things:
 		// 1. Set each node's parent.  If no node exists
 		// with the given parent node id then the node
 		// is placed at the root.
 		// 2. Set the list of page constructors in each node.
 
 		// If a node has no child nodes and no page listeners
 		// then the node is removed.  This allows nodes to be
 		// created by the framework or the more general plug-ins
 		// that have no functionality provided by the plug-in that
 		// created the node but that can be extended by other
 		// plug-ins.  By doing this, rather than expecting plug-ins
 		// to create their own nodes, it is more likely that
 		// different plug-in developers will share nodes, and
 		// thus avoiding hundreds of root nodes in the navigation
 		// tree, each with a single tab view. 
 		
 		TreeNode invisibleRoot = new TreeNode("", null, "");
 
 		for (Iterator iter = idToNodeMap.values().iterator(); iter.hasNext(); ) {
 			TreeNode treeNode = (TreeNode)iter.next();
 			TreeNode parentNode;
 			if (treeNode.getParentId() != null) {
 				// TODO: check if map works like this:
 				parentNode = (TreeNode)idToNodeMap.get(treeNode.getParentId());
 				if (parentNode == null) {
 					parentNode = invisibleRoot;
 				}
 			} else {
 				parentNode = invisibleRoot;
 			}
 			treeNode.setParent(parentNode);
 			parentNode.addChild(treeNode);
 		}	
 		
 		for (Iterator iter = pageListenerAndNodeIdMap.entrySet().iterator(); iter.hasNext(); ) {
 			Map.Entry mapEntry = (Map.Entry)iter.next();
 			String nodeId = (String)mapEntry.getValue();
 			TreeNode node = (TreeNode)idToNodeMap.get(nodeId);
 			if (node != null) {
 				IBookkeepingPageListener pageListener = (IBookkeepingPageListener)mapEntry.getKey();
 				node.addPageListener(pageListener);
 			} else {
 				// No node found with given id, so the
 				// page listener is dropped.
 				// TODO Log missing node.
 			}
 		}
 
 
 		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
 		drillDownAdapter = new DrillDownAdapter(viewer);
 		ViewContentProvider contentProvider = new ViewContentProvider();
 		viewer.setContentProvider(contentProvider);
 		viewer.setLabelProvider(new ViewLabelProvider());
 		viewer.setSorter(new NameSorter());
 
 		AccountsNode accountsObject = new AccountsNode(JMoneyPlugin.getResourceString("NavigationTreeModel.accounts"), Constants.ACCOUNTS_ICON, invisibleRoot);
 
 		invisibleRoot.addChild(accountsObject);
 		
 		// Certain nodes must be saved in this class so that they can be updated.
 		this.accountsRootNode = accountsObject;
 	
 		viewer.setInput(invisibleRoot);
 
 		// Listen for changes to the account list.
 		// (The node containing the list of accounts is currently
 		// hard coded into this view).
 		JMoneyPlugin.getDefault().addSessionChangeListener(listener);
 		
 		viewer.expandAll();
 		makeActions();
 		hookContextMenu();
 		contributeToActionBars();
 		
 		// Listen for changes in the selection and update the 
 		// folder view.
 		// TODO: figure out how to get the folder view dynamically.
 		// The static getter is not such a clean interface.
 		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			   public void selectionChanged(SelectionChangedEvent event) {
 			       // if the selection is empty clear the label
 			       if (event.getSelection().isEmpty()) {
 			           FolderView.getDefault().setSelectedObject(new Vector(), null, null);
 			       } else if (event.getSelection() instanceof IStructuredSelection) {
 			           IStructuredSelection selection = (IStructuredSelection)event.getSelection();
 			           for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
 			           	Object selectedObject = iterator.next();
 			           	Vector pageListeners;
 			           	if (selectedObject instanceof TreeNode) {
 			           		pageListeners = ((TreeNode)selectedObject).getPageListeners();
 			           	} else if (selectedObject instanceof CapitalAccount) {
 			           		pageListeners = accountPageListeners;
 			           	} else {
 			           		pageListeners = new Vector();
 			           	}
 			           	FolderView.getDefault().setSelectedObject(pageListeners, selectedObject, session);
 			           	break;
 			           }
 			       }
 			   }
 			});
 	}
 
 	
 	private void hookContextMenu() {
 		MenuManager menuMgr = new MenuManager("#PopupMenu");
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager manager) {
 				NavigationView.this.fillContextMenu(manager);
 			}
 		});
 		Menu menu = menuMgr.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 		getSite().registerContextMenu(menuMgr, viewer);
 	}
 
 	private void contributeToActionBars() {
 		IActionBars bars = getViewSite().getActionBars();
 		fillLocalPullDown(bars.getMenuManager());
 		fillLocalToolBar(bars.getToolBarManager());
 	}
 
 	private void fillLocalPullDown(IMenuManager manager) {
 		manager.add(newAccountAction);
 		manager.add(new Separator());
 		manager.add(deleteAccountAction);
 	}
 
 	private void fillContextMenu(IMenuManager manager) {
 		manager.add(newAccountAction);
 		manager.add(deleteAccountAction);
 		manager.add(new Separator());
 		drillDownAdapter.addNavigationActions(manager);
 		// Other plug-ins can contribute there actions here
 		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 	}
 	
 	private void fillLocalToolBar(IToolBarManager manager) {
 		manager.add(newAccountAction);
 		manager.add(deleteAccountAction);
 		manager.add(new Separator());
 		drillDownAdapter.addNavigationActions(manager);
 	}
 
 	private void makeActions() {
 		newAccountAction = new Action() {
 			public void run() {
 				Session session = JMoneyPlugin.getDefault().getSession();
 
 				CapitalAccountImpl newAccount = (CapitalAccountImpl)session.createAccount(JMoneyPlugin.getCapitalAccountPropertySet());
		        newAccount.setName(JMoneyPlugin.getResourceString("Account.newAccount"));
		        JMoneyPlugin.getChangeManager().applyChanges("add new account");
		        
 		        // Having added the new account, set it as the selected
 		        // account in the tree viewer.
 		        viewer.setSelection(new StructuredSelection(newAccount), true);
 			}
 		};
 		newAccountAction.setText(JMoneyPlugin.getResourceString("MainFrame.newAccount"));
 		newAccountAction.setToolTipText("Action 2 tooltip");
 		newAccountAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
 				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
 
 		deleteAccountAction = new Action() {
 			public void run() {
 				Session session = JMoneyPlugin.getDefault().getSession();
 				CapitalAccount account = null;
 		           IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
 		           for (Iterator iterator = selection.iterator(); iterator.hasNext();) {
 		               Object selectedObject = iterator.next();
 			           account = (CapitalAccount)selectedObject;
 			           break;
 		           }
 		           if (account != null) {
 		            	session.removeAccount(account);
 		           }
 			}
 		};
 		deleteAccountAction.setText(JMoneyPlugin.getResourceString("MainFrame.deleteAccount"));
 		deleteAccountAction.setToolTipText("Action 1 tooltip");
 		deleteAccountAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
 			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
 	}
 	
 	private void showMessage(String message) {
 		MessageDialog.openInformation(
 			viewer.getControl().getShell(),
 			"Navigation",
 			message);
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 }
