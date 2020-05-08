 /**
  * 
  */
 package net.frontlinesms.ui.handler;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.apache.log4j.Logger;
 
 import thinlet.Thinlet;
 
 import net.frontlinesms.data.domain.Group;
 import net.frontlinesms.ui.FrontlineUI;
 import net.frontlinesms.ui.Icon;
 
 /**
  * @author aga
  *
  */
 public class GroupSelecter extends BasePanelHandler {
 	private Logger LOG = Logger.getLogger(this.getClass());
 
 	private static final String XML_LAYOUT_GROUP_PANEL = "/ui/core/util/pnGroupSelecter.xml";
 	private static final String COMPONENT_GROUP_TREE = "trGroups";
 	
 	private GroupSelecterOwner owner;
 	
 	private boolean allowMultipleSelections;
 
 	private Group[] rootGroups;
 
 //> CONSTRUCTORS
 	public GroupSelecter(FrontlineUI ui, GroupSelecterOwner owner) {
 		super(ui);
 		this.owner = owner;
 		allowMultipleSelections = owner instanceof MultiGroupSelecterOwner;
 	}
 
 	/** Initialise the selecter. */
 	public void init(Group...rootGroups) {
 		super.loadPanel(XML_LAYOUT_GROUP_PANEL);
 		
 		// TODO update selection of group tree appropriate to allowMultipleSelections
 		
 		// add nodes for group tree
 		this.rootGroups = rootGroups;
 	}
 
 	/** Refresh the list of groups */
 	public void refresh() {
		Object groupTree = getGroupTreeComponent();
		ui.removeAll(groupTree);
 		for(Group rootGroup : rootGroups) {
			ui.add(groupTree, createNode(rootGroup, true));
 		}
 	}
 	
 //> ACCESSORS
 	private void setAllowMultipleSelections(boolean allowMultipleSelections) {
 		this.allowMultipleSelections = allowMultipleSelections;
 		// TODO set selection option of group tree appropriately
 		throw new IllegalStateException("NYI");
 	}
 	
 	/** @return a single group selected in the tree */
 	public Group getSelectedGroup() {
 		assert(!this.allowMultipleSelections) : "Cannot get a single selection if multiple groups are selectable.";
 		return ui.getAttachedObject(ui.getSelectedItem(this.getGroupTreeComponent()), Group.class);
 	}
 	
 	/** @return groups selected in the tree */
 	public Collection<Group> getSelectedGroups() {
 		assert(this.allowMultipleSelections) : "Cannot get multiple groups if multiple selection is not enabled.";
 		Object[] selectedItems = ui.getSelectedItems(this.getGroupTreeComponent());
 		HashSet<Group> groups = new HashSet<Group>();
 		for(Object selected : selectedItems) {
 			groups.add(ui.getAttachedObject(selected, Group.class));
 		}
 		return groups;
 	}
 	
 	/** FIXME this is only here for debug purposes. */
 	@Override
 	public Object getPanelComponent() {
 		return super.getPanelComponent();
 	}
 	
 	/** Adds a new group to the tree */
 	public void addGroup(Group group) {
 		Object groupTree = this.getGroupTreeComponent();
 		Object parentGroupNode = getNodeForGroup(groupTree, group.getParent());
 		ui.add(parentGroupNode, createNode(group, true));
 	}
 	
 //> UI EVENT METHODS
 	public void selectionChanged() {
 		if(owner instanceof SingleGroupSelecterOwner) {
 			((SingleGroupSelecterOwner) owner).groupSelectionChanged(getSelectedGroup());
 		} else {
 			((MultiGroupSelecterOwner) owner).groupSelectionChanged(getSelectedGroups());
 		}
 	}
 
 //> UI HELPER METHODS
 	/**
 	 * Gets the node we are currently displaying for a group.
 	 * @param component
 	 * @param group
 	 * @return
 	 */
 	private Object getNodeForGroup(Object component, Group group) {
 		Object ret = null;
 		for (Object o : this.ui.getItems(component)) {
 			Group g = ui.getAttachedObject(o, Group.class);
 			if (g.equals(group)) {
 				ret = o;
 				break;
 			} else {
 				ret = getNodeForGroup(o, group);
 				if (ret != null) break;
 			}
 		}
 		return ret;
 	}
 	
	/** @return the group tree TODO cache this - it's not going to change, and we use it a lot */
 	private Object getGroupTreeComponent() {
 		return super.find(COMPONENT_GROUP_TREE);
 	}
 	
 	/**
 	 * Creates a node for the supplied group, creating nodes for its sub-groups and contacts as well.
 	 * 
 	 * @param group The group to be put into a node.
 	 * @param showContactsNumber set <code>true</code> to show the number of contacts per group in the node's text or <code>false</code> otherwise
 	 *   TODO removing this argument, and treating it as always <code>false</code> speeds up the contact tab a lot
 	 * @return
 	 */
 	private Object createNode(Group group, boolean showContactsNumber) {
 		LOG.trace("ENTER");
 		
 		LOG.debug("Group [" + group.getName() + "]");
 		
 		String toSet = group.getName();
 		if (showContactsNumber) {
 			toSet += " (" + group.getAllMembers().size() + ")";
 		}
 		
 		Object node = ui.createNode(toSet, group);
 
 		if(ui.getBoolean(node, Thinlet.EXPANDED) && group.hasDescendants()) {
 			ui.setIcon(node, Icon.FOLDER_OPEN);
 		} else {
 			ui.setIcon(node, Icon.FOLDER_CLOSED);
 		}
 		
 		// Add subgroup components to this node
 		for (Group subGroup : group.getDirectSubGroups()) {
 			Object groupNode = createNode(subGroup, showContactsNumber);
 			ui.add(node, groupNode);
 		}
 		LOG.trace("EXIT");
 		return node;
 	}
 }
