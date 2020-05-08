 package com.googlecode.salix.Salix;
 
 import com.google.gwt.dom.client.Style.Display;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.DoubleClickHandler;
 import com.google.gwt.user.client.ui.*;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 /**
  * Tree item representing a tree node - replacement for default GWT tree item.
  *
  * @author <a href="mailto:peter.manka@siemens-enterprise.com">Peter Manka</a>
  * @author <a href="mailto:richard.richter@siemens-enterprise.com">Richard "Virgo" Richter</a>
  */
 public class TreeItem extends UIObject {
 	public static final int ROOT_LEVEL = -1;
 
 	/**
 	 * Holds the information about the level, on which this node is. -1 is for internal root, that is not counted in.
 	 */
 	private int level = ROOT_LEVEL;
 
 	private boolean state;
 
 	/**
 	 * Content of the node.
 	 */
 	private Widget widget;
 	private TreeItem parent;
 	private ArrayList<TreeItem> children = new ArrayList<TreeItem>();
 
 	/**
 	 * Main panel contains the tree node itself with all its children.
 	 */
 	private FlowPanel mainPanel = new FlowPanel();
 	private FlexTable headerPanel = new FlexTable();
 	private FlowPanel childrenPanel = new FlowPanel();
 	private Widget expandWidget;
 	private Widget collapseWidget;
 	private Tree tree;
 	private ArrayList<ClickHandler> clickHandlers;
 
 	private HTMLPanel connectorPictures = new HTMLPanel("");
 	private Comparator<TreeItem> itemComparator;
 	private boolean selected;
 	
 	private Object userObject = null;
 	private Widget currentButtonWidget = null;
 
 	public TreeItem(String name) {
 		this(new Label(name));
 		widget.getElement().getStyle().setProperty("whiteSpace", "nowrap");
 
 	}
 
 	public TreeItem(Widget widget) {
 		this.widget = widget;
 		headerPanel.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				HTMLTable.Cell cell = headerPanel.getCellForEvent(event);
 				if (cell != null && cell.getCellIndex() != 0) {
 					select();
 					if (clickHandlers != null) {
 						for (ClickHandler clickHandler : clickHandlers) {
 							clickHandler.onClick(event);
 						}
 					}
 				}
 			}
 		});
 	}
 
 	public void addDoubleClickHandler(DoubleClickHandler handler) {
 		headerPanel.addDoubleClickHandler(handler);
 	}
 	
 	/**
 	 * Draws this tree item and its subtree. Called internally only.
 	 */
 	void draw(boolean sortFlag) {
 		if (tree == null) {
 			return; // some other time - after we add this to an actual tree
 		}
 
 		if (sortFlag) {
 			defaultSortOnly();
 		}
 		childrenPanel.clear();
 		for (TreeItem child : children) {
 			childrenPanel.add(child.getMainPanel());
 		}
 
 		if (parent != null) {
 			level = getParent().level + 1;
 			int ofs = level;
 			if (tree.configuration().isShowConnectors()) {
 				connectorPictures.clear();
 				addParentConnectors(this);
 				if (currentButtonWidget != null) {
 					connectorPictures.add(currentButtonWidget);
 					ofs++;
 				}
 				ofs *= tree.configuration().getOffsetPx();
 				connectorPictures.getElement().getStyle().setWidth(ofs, Unit.PX);
 			} else {
 				ofs *= tree.configuration().getOffsetPx();
 				headerPanel.getElement().getStyle().setPropertyPx("paddingLeft", ofs);
 			}
 			connectorPictures.getElement().getStyle().setHeight(tree.configuration().getIconHeight(), Unit.PX);
 		}
 
 		if (level > ROOT_LEVEL) { // not internal root
 			expandWidget = tree.expandWidget(new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					expand();
 				}
 			});
 			collapseWidget = tree.collapseWidget(new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					collapse();
 				}
 			});
 
 			headerPanel.addStyleName(tree.configuration().getClassTreeItem());
 			headerPanel.getColumnFormatter().setWidth(0, "1px");
 			headerPanel.setWidth("100%");
 			headerPanel.setHeight(tree.configuration().getIconHeight() + "px"); // lines without icons should have the same height as those with icons
 			headerPanel.setCellSpacing(0);
 			headerPanel.setCellPadding(0);
 
 			headerPanel.setWidget(0, 0, connectorPictures);
 			headerPanel.setWidget(0, 1, widget);
 			mainPanel.add(headerPanel);
 			mainPanel.add(childrenPanel);
 
 			if (state) {
 				expand();
 			} else {
 				collapse();
 			}
 		}
 		for (TreeItem child : children) {
 			child.draw(sortFlag);
 			childrenPanel.add(child.getMainPanel());
 		}
 	}
 
 	private void addParentConnectors(TreeItem treeItem) {
 		if (treeItem.getParent() == null || treeItem.getParent().getParent() == null) {
 			return;
 		}
 		addParentConnectors(treeItem.getParent());
 		connectorPictures.add(treeItem.getParent().isLastChild() ? createEmptyPanel() : new Image(tree.configuration().getIcons().elbowLine()));
 	}
 
 	private Widget createEmptyPanel() {
 		SimplePanel result = new SimplePanel();
 		result.setWidth("16px"); //width of an icon
 		result.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
 		return result;
 	}
 
 	public boolean isLastChild() {
 		return getParent().getChildren().indexOf(this) == getParent().getChildren().size() - 1;
 	}
 
 	public void select() {
 		if (tree == null) {
 			return;
 		}
 		if (level == ROOT_LEVEL) {
 			tree.setSelectedItem(null);
 			return;
 		}
 		tree.setSelectedItem(TreeItem.this);
 	}
 
 	/**
 	 * Special constructor for root node.
 	 */
 	protected TreeItem() {
 		mainPanel.add(childrenPanel);
 	}
 
 	/**
 	 * Returns level of the item in the tree - 0 for root level (items added directly to a tree).
 	 *
 	 * @return depth level of the item
 	 */
 	public int getLevel() {
 		return level;
 	}
 	
 	private void replaceButtonWidget(Widget oldButtonWidget) {
 		if (oldButtonWidget != null) {
 			connectorPictures.remove(oldButtonWidget);
 		}
 		if (currentButtonWidget != null) {
 			connectorPictures.add(currentButtonWidget);
 		}
 	}
 
 	/**
 	 * Expands this tree item.
 	 */
 	public void expand() {
 		Widget oldButtonWidget = currentButtonWidget;
 		if (children.size() > 0) {
 			if (currentButtonWidget != collapseWidget) {
 				if (tree.configuration().isShowConnectors()) {
 					((Image) collapseWidget).setResource(isLastChild() ? tree.configuration().getIcons().elbowEndCollapse() : tree.configuration().getIcons().elbowCollapse());
 				}
 				currentButtonWidget = collapseWidget;
 				replaceButtonWidget(oldButtonWidget);
 			}
 			childrenPanel.setVisible(true);
 			state = true;
 		} else {
 			if (tree.configuration().isShowConnectors()) {
 				currentButtonWidget = createConnectorPicture();
 				replaceButtonWidget(oldButtonWidget);
 			}
 		}
 	}
 
 	/**
 	 * Collapses this tree item.
 	 */
 	public void collapse() {
 		Widget oldButtonWidget = currentButtonWidget;
 		if (children.size() > 0) {
 			if (currentButtonWidget != expandWidget) {
 				if (tree.configuration().isShowConnectors()) {
 					((Image) expandWidget).setResource(isLastChild() ? tree.configuration().getIcons().elbowEndExpand() : tree.configuration().getIcons().elbowExpand());
 				}
 				currentButtonWidget = expandWidget;
 				replaceButtonWidget(oldButtonWidget);
 			}
 		} else {
 			if (tree.configuration().isShowConnectors()) {
 				currentButtonWidget = createConnectorPicture();
 				replaceButtonWidget(oldButtonWidget);
 			}
 		}
 		childrenPanel.setVisible(false);
 		state = false;
 	}
 
 	private Widget createConnectorPicture() {
 		return new Image(isLastChild() ? tree.configuration().getIcons().elbowEnd() : tree.configuration().getIcons().elbow());
 	}
 
 	/**
 	 * Sets expanded state of this node (false is collapsed). This method is provided for GWT TreeItem "compatibility".
 	 * Calls expand/collapse methods accordingly.
 	 *
 	 * @param expanded node will be expanded if true
 	 */
 	public void setState(boolean expanded) {
 		if (expanded) {
 			expand();
 		} else {
 			collapse();
 		}
 	}
 
 	/**
 	 * Returns true if this tree item is expanded, otherwise returns false.
 	 *
 	 * @return true if tree item is expanded, false if collapsed
 	 */
 	public boolean getState() {
 		return state;
 	}
 
 	/**
 	 * Returns parent item of this tree item.
 	 *
 	 * @return parent tree item
 	 */
 	public TreeItem getParent() {
 		return parent;
 	}
 	
 	public TreeItem getParentItem() {
 		return getParent();
 	}
 
 	void setParent(TreeItem parent) {
 		this.parent = parent;
 	}
 
 	/**
 	 * Returns list of all children tree items.
 	 *
 	 * @return all children tree items
 	 */
 	public ArrayList<TreeItem> getChildren() {
 		return children;
 	}
 
 	/**
 	 * Wraps text string as tree item and adds it as a child of this tree item.
 	 *
 	 * @param text text contained in child tree item
 	 * @return added tree item (child)
 	 */
 	public TreeItem addItem(String text) {
 		TreeItem treeItem = new TreeItem(text);
 		return addItem(treeItem);
 	}
 
 	/**
 	 * Wraps widget as tree item and adds it as a child of this tree item.
 	 *
 	 * @param widget widget contained in child tree item
 	 * @return added tree item (child)
 	 */
 	public TreeItem addItem(Widget widget) {
 		TreeItem treeItem = new TreeItem(widget);
 		return addItem(treeItem);
 	}
 
 	/**
 	 * Adds provided tree item as a child of this tree item.
 	 *
 	 * @param child tree item (child)
 	 * @return the same tree item (child)
 	 */
 	public TreeItem addItem(TreeItem child) {
 		children.add(child);
 		child.setParent(this);
 		child.setTree(tree);
 		sortAndRedraw();
 		return child;
 	}
 
 	private void redraw() {
 		if (tree != null && tree.isDrawn()) {
 			draw(false);
 		}
 	}
 
 	Panel getMainPanel() {
 		return mainPanel;
 	}
 
 	/**
 	 * Returns tree object owning this tree item.
 	 *
 	 * @return tree object owning this tree item
 	 */
 	public Tree getTree() {
 		return tree;
 	}
 
 	void setTree(Tree tree) {
 		this.tree = tree;
 		for (TreeItem child : children) {
 			child.setTree(tree);
 		}
 		redraw();
 	}
 
 	/**
 	 * Adds custom click handler for this tree item.
 	 *
 	 * @param clickHandler click handler for tree item
 	 */
 	public void addClickHandler(ClickHandler clickHandler) {
 		if (clickHandlers == null) {
 			clickHandlers = new ArrayList<ClickHandler>();
 		}
 		clickHandlers.add(clickHandler);
 	}
 
 	/**
 	 * Removes all custom click handlers for this tree item.
 	 */
 	public void clearClickHandlers() {
 		clickHandlers.clear();
 	}
 
 	/**
 	 * Removes this item from the tree. If the item was selected, parent will be selected.
 	 * Internal tree root can not be removed (nothing happens).
 	 */
 	public void remove() {
 		if (parent == null) {
 			return;
 		}
 		parent.getChildren().remove(this);
 		parent.sortAndRedraw();
 		if (selected) {
 			parent.select();
 		}
 	}
 
 	/**
 	 * Removes all children from the item.
 	 */
 	public void removeItems() {
 		boolean selectParent = false;
 		if (children.contains(tree.getSelectedItem())) {
 			selectParent = true;
 		}
 		children.clear();
 		childrenPanel.clear();
 		if (selectParent && parent != null) {
 			parent.select();
 		}
 		sortAndRedraw();
 	}
 	
 	public void removeItem(TreeItem item) {
 		item.remove();
 	}
 
 	/**
 	 * Returns GWT {@link Widget} that is contained in this tree item.
 	 *
 	 * @return content of this tree item
 	 */
 	public Widget getWidget() {
 		return widget;
 	}
 
 	/**
 	 * Sets comparator for children sorting and sorts children immediatelly.
 	 *
 	 * @param itemComparator comparator for children sorting
 	 */
 	public void setComparator(Comparator<TreeItem> itemComparator) {
 		this.itemComparator = itemComparator;
 		sortAndRedraw();
 	}
 
 	/**
 	 * Sorts children of this tree item and redraws the whole subtree. If no comparator
 	 * is found for tree item or for tree (in config) then no sort or redraw is performed.
 	 */
 	public void sortAndRedraw() {
 		defaultSortOnly();
 		redraw();
 	}
 
 	private void defaultSortOnly() {
 		if (itemComparator != null) {
 			sortOnly(itemComparator);
 		} else if (tree != null && tree.configuration().getComparator() != null) {
 			sortOnly(tree.configuration().getComparator());
 		} else {
 			sortOnly(null);
 		}
 	}
 
 	/**
 	 * Orders all children items according to specified comparator. This comparator is not set
 	 * for the tree item - use {@link #setComparator(java.util.Comparator)} for this. Subtree
 	 * is redrawn. If {@code null} comparator is set, no sort is performed, but subtree will be
 	 * redrawn.
 	 *
 	 * @param itemComparator tree item comparator
 	 */
 	public void sortChildren(Comparator<TreeItem> itemComparator) {
 		sortOnly(itemComparator);
 		redraw();
 	}
 
 	private void sortOnly(Comparator<TreeItem> itemComparator) {
 		if (itemComparator != null) {
 			Collections.sort(children, itemComparator);
 		}
 	}
 
 	/**
 	 * Selects or deselects this item.
 	 *
 	 * @param selected <code>true</code> to select the item, <code>false</code> to deselect it
 	 */
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 		if (selected) {
 			headerPanel.addStyleName(tree.configuration().getClassTreeItemSelected());
 		} else {
 			headerPanel.removeStyleName(tree.configuration().getClassTreeItemSelected());
 		}
 	}
 
 	/**
 	 * Determines whether this item is currently selected.
 	 *
 	 * @return <code>true</code> if it is selected
 	 */
 	public boolean isSelected() {
 		return selected;
 	}
 	
 	public TreeItem getChild(int index) {
 		return children.get(index); 
 	}
 	
 	public int getChildCount() {
 		return children.size();
 	}
 	
 	public int getChildIndex(TreeItem treeItem)	{
 		return children.indexOf(treeItem);
 	}
 	
 	public void setUserObject(Object object) {
 		userObject = object;
 	}
 	
 	public Object getUserObject() {
 		return userObject;
 	}
 	
 	public void setText(String text) {
		widget.setTitle(text);
 	}
 }
