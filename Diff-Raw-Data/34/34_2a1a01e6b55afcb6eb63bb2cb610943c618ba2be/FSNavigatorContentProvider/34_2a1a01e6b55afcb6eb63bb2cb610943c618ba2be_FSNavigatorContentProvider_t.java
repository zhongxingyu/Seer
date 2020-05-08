 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.filesystem.controls;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IFileSystem;
 import org.eclipse.tcf.services.IFileSystem.DirEntry;
 import org.eclipse.tcf.services.IFileSystem.FileSystemException;
 import org.eclipse.tcf.services.IFileSystem.IFileHandle;
 import org.eclipse.tcf.te.tcf.core.Tcf;
 import org.eclipse.tcf.te.tcf.core.interfaces.IChannelManager;
 import org.eclipse.tcf.te.tcf.filesystem.model.FSModel;
 import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
 import org.eclipse.tcf.te.ui.nls.Messages;
 import org.eclipse.tcf.te.ui.trees.TreeContentProvider;
 
 
 /**
  * File system content provider for the common navigator of Target Explorer.
  */
 public class FSNavigatorContentProvider extends TreeContentProvider {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
 	 */
 	@Override
 	public Object getParent(Object element) {
 		if (element instanceof FSTreeNode) {
 			FSTreeNode parent = ((FSTreeNode)element).parent;
 			// If the parent is a root node, return the associated peer node
 			if (parent != null) {
 				if (parent.isSystemRoot()) {
 					return parent.peerNode;
 				}
 				return parent;
 			}
 			return ((FSTreeNode) element).peerNode;
 		}
 		return null;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
 	 */
 	@Override
 	public Object[] getChildren(Object parentElement) {
 		Assert.isNotNull(parentElement);
 
 		Object[] children = NO_ELEMENTS;
 
 		// For the file system, we need the peer node
 		if (parentElement instanceof IPeerModel) {
 			final IPeerModel peerNode = (IPeerModel)parentElement;
 			installPropertyChangeListener(peerNode);
 			// Get the file system model root node, if already stored
 			final FSModel fsModel = FSModel.getFSModel(peerNode);
 			final FSTreeNode root = fsModel.getRoot();
 
 			// If the file system model root node hasn't been created, create
 			// and initialize the root node now.
 			if (root == null) {
 				IPeer peer = peerNode.getPeer();
 				final int[] state = new int[1];
 				Protocol.invokeAndWait(new Runnable() {
 					@Override
 					public void run() {
 						state[0] = peerNode.getIntProperty(IPeerModelProperties.PROP_STATE);
 					}
 				});
 				if (peer != null && IPeerModelProperties.STATE_ERROR != state[0] && IPeerModelProperties.STATE_NOT_REACHABLE != state[0]) {
 					final AtomicReference<FSTreeNode> rootNode = new AtomicReference<FSTreeNode>();
 					// Create the root node and the initial pending node.
 					// This must happen in the TCF dispatch thread.
 					Protocol.invokeAndWait(new Runnable() {
 						@Override
 						public void run() {
 							// The root node
 							FSTreeNode node = new FSTreeNode();
 							node.type = "FSRootNode"; //$NON-NLS-1$
 							node.peerNode = peerNode;
 							node.childrenQueried = false;
 							node.childrenQueryRunning = false;
 							node.name = org.eclipse.tcf.te.tcf.filesystem.nls.Messages.FSTreeNodeContentProvider_rootNode_label;
 
 							fsModel.setRoot(node);
 
 							rootNode.set(node);
 						}
 					});
 
 					children = isRootNodeVisible() ? new Object[] { rootNode.get() } : getChildren(rootNode.get());
 				}
 			} else {
 				children = isRootNodeVisible()  ? new Object[] { root }  : getChildren(root);
 			}
 		} else if (parentElement instanceof FSTreeNode) {
 			final FSTreeNode node = (FSTreeNode)parentElement;
 
 			// Get possible children
 			// This must happen in the TCF dispatch thread.
 			final List<FSTreeNode> candidates = new ArrayList<FSTreeNode>();
 			Protocol.invokeAndWait(new Runnable() {
 				@Override
 				public void run() {
 					candidates.addAll(node.getChildren());
 				}
 			});
 			children = candidates.toArray();
 
 			// No children -> check for "childrenQueried" property. If false, trigger the query.
 			if (children.length == 0 && !node.childrenQueried) {
 
 				if (node.type.endsWith("RootNode")) { //$NON-NLS-1$
 					children = getRootNodeChildren(node);
 				}
 				else if (node.type.endsWith("DirNode")) { //$NON-NLS-1$
 					children = getDirNodeChildren(node);
 				}
 			}
 		}
 
 		return children;
 	}
 
 	/**
 	 * Queries the children for the root node.
 	 *
 	 * @param rootNode The root node. Must not be <code>null</code>.
 	 * @return The children list.
 	 */
 	protected Object[] getRootNodeChildren(final FSTreeNode rootNode) {
 		Assert.isNotNull(rootNode);
 
 		Object[] children = NO_ELEMENTS;
 
 		// Get the peer node from the root node
 		final IPeerModel peerNode = rootNode.peerNode;
 		Assert.isNotNull(peerNode);
 
 		// The child query should not be marked running at this point
 		Assert.isTrue(!rootNode.childrenQueryRunning);
 		rootNode.childrenQueryRunning = true;
 
 		final List<FSTreeNode> candidates = new ArrayList<FSTreeNode>();
 		// Create the initial pending node.
 		// This must happen in the TCF dispatch thread.
 		Protocol.invokeAndWait(new Runnable() {
 			@Override
 			public void run() {
 				// Add a special "Pending..." node
 				FSTreeNode pendingNode = new FSTreeNode();
 				pendingNode.name = Messages.PendingOperation_label;
 				pendingNode.type ="FSPendingNode"; //$NON-NLS-1$
 				pendingNode.parent = rootNode;
 				pendingNode.peerNode = rootNode.peerNode;
 				rootNode.getChildren().add(pendingNode);
 
 				candidates.addAll(rootNode.getChildren());
 			}
 		});
 
 		children = candidates.toArray();
 
 		Tcf.getChannelManager().openChannel(peerNode.getPeer(), false, new IChannelManager.DoneOpenChannel() {
 			@Override
 			public void doneOpenChannel(final Throwable error, final IChannel channel) {
 				Assert.isTrue(Protocol.isDispatchThread());
 
 				if (channel != null) {
 					final IFileSystem service = channel.getRemoteService(IFileSystem.class);
 					if (service != null) {
 
 						Protocol.invokeLater(new Runnable() {
 							@Override
 							public void run() {
 								service.roots(new IFileSystem.DoneRoots() {
 									@Override
 									public void doneRoots(IToken token, FileSystemException error, DirEntry[] entries) {
 										// Close the channel, not needed anymore
 										 Tcf.getChannelManager().closeChannel(channel);
 
 										FSTreeNode rootNode = FSModel.getFSModel(peerNode).getRoot();
 										if (rootNode != null && error == null) {
 
 											for (DirEntry entry : entries) {
 												FSTreeNode node = createNodeFromDirEntry(entry, true);
 												if (node != null) {
 													node.parent = rootNode;
 													node.peerNode = rootNode.peerNode;
 													rootNode.getChildren().add(node);
 												}
 											}
 
 											// Find the pending node and remove it from the child list
 											Iterator<FSTreeNode> iterator = rootNode.getChildren().iterator();
 											while (iterator.hasNext()) {
 												FSTreeNode candidate = iterator.next();
 												if (Messages.PendingOperation_label.equals(candidate.name)) {
 													iterator.remove();
 													break;
 												}
 											}
 
 											// Reset the children query markers
 											rootNode.childrenQueryRunning = false;
 											rootNode.childrenQueried = true;
 											rootNode.firePropertyChange();
 										}
 									}
 								});
 							}
 						});
 						rootNode.firePropertyChange();
 					} else {
 						// The file system service is not available for this peer.
 						// --> Close the just opened channel
 						 Tcf.getChannelManager().closeChannel(channel);
 					}
 				}
 			}
 		});
 
 		return children;
 	}
 
 	/**
 	 * Queries the children for a directory node.
 	 *
 	 * @param node The directory node. Must not be <code>null</code>.
 	 * @return The children list.
 	 */
 	protected Object[] getDirNodeChildren(final FSTreeNode node) {
 		Assert.isNotNull(node);
 
 		Object[] children = NO_ELEMENTS;
 
 		// Get the peer node from the root node
 		final IPeerModel peerNode = node.peerNode;
 		Assert.isNotNull(peerNode);
 
 		// The child query should not be marked running at this point
 		Assert.isTrue(!node.childrenQueryRunning);
 		node.childrenQueryRunning = true;
 
 		final List<FSTreeNode> candidates = new ArrayList<FSTreeNode>();
 		// Create the initial pending node.
 		// This must happen in the TCF dispatch thread.
 		Protocol.invokeAndWait(new Runnable() {
 			@Override
 			public void run() {
 				// Add a special "Pending..." node
 				FSTreeNode pendingNode = new FSTreeNode();
 				pendingNode.name = Messages.PendingOperation_label;
 				pendingNode.type ="FSPendingNode"; //$NON-NLS-1$
 				pendingNode.parent = node;
 				pendingNode.peerNode = node.peerNode;
 				node.getChildren().add(pendingNode);
 
 				candidates.addAll(node.getChildren());
 			}
 		});
 
 		children = candidates.toArray();
 
 		final String absName = getEntryAbsoluteName(node);
 
 		if (absName != null) {
 			// Open a channel to the peer and query the children
 			Tcf.getChannelManager().openChannel(node.peerNode.getPeer(), false, new IChannelManager.DoneOpenChannel() {
 				@Override
 				public void doneOpenChannel(final Throwable error, final IChannel channel) {
 					Assert.isTrue(Protocol.isDispatchThread());
 
 					if (channel != null && channel.getState() == IChannel.STATE_OPEN) {
 						final IFileSystem service = channel.getRemoteService(IFileSystem.class);
 						if (service != null) {
 
 							Protocol.invokeLater(new Runnable() {
 								@Override
 								public void run() {
 									service.opendir(absName, new IFileSystem.DoneOpen() {
 										@Override
 										public void doneOpen(IToken token, FileSystemException error, final IFileHandle handle) {
 											if (error == null) {
 												// Read the directory content until finished
 												readdir(channel, service, handle, node);
 											} else {
 												// In case of an error, we are done here
 												node.childrenQueryRunning = false;
 												node.childrenQueried = true;
 											}
 										}
 									});
 								}
 							});
 						} else {
 							// No file system service available
 							node.childrenQueryRunning = false;
 							node.childrenQueried = true;
 						}
 					} else {
 						// Channel failed to open
 						node.childrenQueryRunning = false;
 						node.childrenQueried = true;
 					}
 				}
 			});
 		} else {
 			// No absolute name
 			node.childrenQueryRunning = false;
 			node.childrenQueried = true;
 		}
 
 		return children;
 	}
 
 	/**
 	 * Reads the content of a directory until the file system service signals EOF.
 	 *
 	 * @param channel The open channel. Must not be <code>null</code>.
 	 * @param service The file system service. Must not be <code>null</code>.
 	 * @param handle The directory handle. Must not be <code>null</code>.
 	 * @param parentNode The parent node receiving the entries. Must not be <code>null</code>.
 	 * @param mode The notification mode to set to the parent node once done.
 	 */
 	protected void readdir(final IChannel channel, final IFileSystem service, final IFileHandle handle, final FSTreeNode parentNode) {
 		Assert.isNotNull(channel);
 		Assert.isNotNull(service);
 		Assert.isNotNull(handle);
 		Assert.isNotNull(parentNode);
 
 		Protocol.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				service.readdir(handle, new IFileSystem.DoneReadDir() {
 
 					@Override
 					public void doneReadDir(IToken token, FileSystemException error, DirEntry[] entries, boolean eof) {
 						// Close the handle and channel if EOF is signaled or an error occurred.
 						if (eof) {
 							service.close(handle, new IFileSystem.DoneClose() {
 								@Override
 								public void doneClose(IToken token, FileSystemException error) {
 									 Tcf.getChannelManager().closeChannel(channel);
 								}
 							});
 						}
 
 						// Process the returned data
 						if (error == null && entries != null && entries.length > 0) {
 							for (DirEntry entry : entries) {
 								FSTreeNode node = createNodeFromDirEntry(entry, false);
 								if (node != null) {
 									node.parent = parentNode;
 									node.peerNode = parentNode.peerNode;
 									parentNode.getChildren().add(node);
 								}
 							}
 						}
 
 						if (eof) {
 							// Find the pending node and remove it from the child list
 							Iterator<FSTreeNode> iterator = parentNode.getChildren().iterator();
 							while (iterator.hasNext()) {
 								FSTreeNode candidate = iterator.next();
 								if (Messages.PendingOperation_label.equals(candidate.name)) {
 									iterator.remove();
 									break;
 								}
 							}
 
 							// Reset the children query markers
 							parentNode.childrenQueryRunning = false;
 							parentNode.childrenQueried = true;
 						} else {
 							// And invoke ourself again
 							readdir(channel, service, handle, parentNode);
 						}
 
 						parentNode.firePropertyChange();
 					}
 				});
 			}
 		});
 	}
 
 
 	/**
 	 * Creates a tree node from the given directory entry.
 	 *
 	 * @param entry The directory entry. Must not be <code>null</code>.
 	 *
 	 * @return The tree node.
 	 */
 	protected FSTreeNode createNodeFromDirEntry(DirEntry entry, boolean entryIsRootNode) {
 		Assert.isNotNull(entry);
 
 		FSTreeNode node = null;
 
 		IFileSystem.FileAttrs attrs = entry.attrs;
 
 		if (attrs == null || attrs.isDirectory()) {
 			node = new FSTreeNode();
 			node.childrenQueried = false;
 			node.childrenQueryRunning = false;
 			node.attr = attrs;
 			node.name = entry.filename;
 			node.type = entryIsRootNode ? "FSRootDirNode" : "FSDirNode"; //$NON-NLS-1$ //$NON-NLS-2$
 		} else if (attrs.isFile()) {
 			node = new FSTreeNode();
 			node.childrenQueried = false;
 			node.childrenQueryRunning = false;
 			node.attr = attrs;
 			node.name = entry.filename;
 			node.type = "FSFileNode"; //$NON-NLS-1$
 		}
 
 		return node;
 	}
 
 	/**
 	 * Returns the absolute name for the given node.
 	 *
 	 * @param node The node. Must not be <code>null</code>.
 	 * @return The absolute name.
 	 */
 	public static String getEntryAbsoluteName(FSTreeNode node) {
 		Assert.isNotNull(node);
 
 		StringBuilder path = new StringBuilder();
 
 		// We have to walk upwards the hierarchy until the root node is found
 		FSTreeNode parent = node.parent;
 		while (parent != null && parent.type != null && parent.type.startsWith("FS")) { //$NON-NLS-1$
 			if ("FSRootNode".equals(parent.type)) { //$NON-NLS-1$
 				// We are done if reaching the root node
 				break;
 			}
 
 			if (path.length() == 0) path.append(parent.name.replaceAll("\\\\", "/")); //$NON-NLS-1$ //$NON-NLS-2$
 			else {
 				String name = parent.name.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
 				if (!name.endsWith("/")) name = name + "/"; //$NON-NLS-1$ //$NON-NLS-2$
 				path.insert(0, name);
 			}
 
 			parent = parent.parent;
 		}
 
 		if (path.length() > 0 && path.charAt(path.length() - 1) != '/') {
 			path.append("/"); //$NON-NLS-1$
 		}
 		path.append(node.name);
 
 		return path.toString();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
 	 */
 	@Override
 	public boolean hasChildren(final Object element) {
 		Assert.isNotNull(element);
 
 		boolean hasChildren = false;
 
 		if (element instanceof FSTreeNode) {
 			final FSTreeNode node = (FSTreeNode)element;
 			if (node.type != null && (node.type.endsWith("DirNode") || node.type.endsWith("RootNode"))) { //$NON-NLS-1$ //$NON-NLS-2$
 				if (!node.childrenQueried || node.childrenQueryRunning) {
 					hasChildren = true;
 				} else if (node.childrenQueried) {
 					hasChildren = super.hasChildren(element);
 				}
 			}
 		}
 		else if (element instanceof IPeerModel) {
 			// Get the root node for this peer model object.
 			// If null, true is returned as it means that the file system
 			// model hasn't been created yet and have to treat is as children
 			// not queried yet.
 			FSTreeNode root = FSModel.getFSModel(((IPeerModel)element)).getRoot();
 			hasChildren = root != null ? hasChildren(root) : true;
 		}
 
 		return hasChildren;
 	}
 
 	/**
 	 * If the root node of the tree is visible.
 	 * 
 	 * @return true if it is visible.
 	 */
 	protected boolean isRootNodeVisible() {
 		return true;
 	}	
 }
