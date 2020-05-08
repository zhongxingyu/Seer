 package org.Zeitline;
 
 import org.Zeitline.Event.AbstractTimeEvent;
 import org.Zeitline.Event.AtomicEvent;
 import org.Zeitline.Event.ComplexEvent;
 import org.Zeitline.Event.L2TEvent;
 import org.Zeitline.Event.Mask.AtomicEventMask;
 import org.Zeitline.Event.Mask.ComplexEventMask;
 import org.Zeitline.Event.Mask.L2TEventMask;
 import org.Zeitline.GUI.Action.PasteAction;
 import org.Zeitline.GUI.EventTree.EventTree;
 import org.Zeitline.GUI.EventTree.EventTreeModel;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.TreePath;
 import java.awt.*;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 public class TimelineView extends JPanel implements TreeSelectionListener,
         FocusListener,
         ChangeListener,
         MouseListener,
         TreeModelListener {
 
     protected final int DIRECTION_FORWARD = 1;
     protected final int DIRECTION_BACKWARD = -1;
 
     private static boolean complexMaskChangeCancelled = false;
 
     private final TreeSelectionListener app;
     protected ComplexEventMask complexMask;
     protected L2TEventMask l2tMask;
     protected AtomicEventMask atomicMask;
     protected JSplitPane treePane;
     protected JTabbedPane leftTrees, rightTrees;
     protected EventTree orphanTree;
     protected EventTree currentTree;
     protected TreePath currentSelection;
     protected int currentFindTab = -1;
     protected EventTree currentFindTree = null;
     protected FindEntries currentFindPosition;
     protected FindEntries validFindPosition;
     protected EventTreeModel currentFindModel;
     protected boolean findLeft = true;
     protected boolean splitView;
     protected Action moveLeftAction, moveRightAction,
             filterAction, removeAction, saveAction, pasteAction,
             cutAction, clearAction, findAction;
     protected boolean isOrphanVisible;
     protected List<EventTree> timelines;
 
     public TimelineView(TreeSelectionListener app, Action left, Action right,
                         Action filter, Action remove, Action save,
                         Action paste, Action cut, Action clear,
                         Action find,
                         ComplexEventMask cem, AtomicEventMask aem, L2TEventMask lem) {
         this.app = app;
         complexMask = cem;
         atomicMask = aem;
         l2tMask = lem;
 
         timelines = new ArrayList<EventTree>();
 
         leftTrees = new JTabbedPane(JTabbedPane.TOP,
                 JTabbedPane.SCROLL_TAB_LAYOUT);
         rightTrees = new JTabbedPane(JTabbedPane.TOP,
                 JTabbedPane.SCROLL_TAB_LAYOUT);
         leftTrees.addFocusListener(this);
         leftTrees.addChangeListener(this);
         leftTrees.addMouseListener(this);
         rightTrees.addFocusListener(this);
         rightTrees.addChangeListener(this);
         rightTrees.addMouseListener(this);
         treePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
         orphanTree = new EventTree(new ComplexEvent("Orphans", ""));
         orphanTree.addTreeSelectionListener(this);
         orphanTree.addTreeSelectionListener(app);
         isOrphanVisible = false;
         treePane.setRightComponent(rightTrees);
         treePane.setOneTouchExpandable(true);
         leftTrees.setMinimumSize(new Dimension(0, 0));
         rightTrees.setMinimumSize(new Dimension(0, 0));
         treePane.setResizeWeight(0.5);
 
         splitView = false;
         this.setLayout(new BorderLayout());
         this.add(leftTrees);
 
         moveLeftAction = left;
         moveRightAction = right;
         filterAction = filter;
         removeAction = remove;
         saveAction = save;
         pasteAction = paste;
         cutAction = cut;
         clearAction = clear;
         findAction = find;
 
         if (moveLeftAction != null)
             moveLeftAction.setEnabled(false);
         if (moveRightAction != null)
             moveRightAction.setEnabled(false);
 
     }
 
     public void showSplit() {
 
         this.remove(leftTrees);
         treePane.setLeftComponent(leftTrees);
         treePane.setRightComponent(rightTrees);
         this.add(treePane);
         treePane.revalidate();
 
         splitView = true;
 
     }
 
     public void hideSplit() {
 
         treePane.remove(leftTrees);
         this.remove(treePane);
         this.add(leftTrees);
         this.revalidate();
         splitView = false;
 
     }
 
     public void addTree(EventTree t, TreeSelectionListener app) {
         addTreeToTab(rightTrees, t, app);
     } // addTree
 
     protected void addTreeToTab(JTabbedPane target, EventTree t, TreeSelectionListener app) {
 
         if (target == leftTrees)
             timelines.add(leftTrees.getTabCount(), t);
         else
             timelines.add(t);
 
         target.setSelectedComponent(target.add(t.toString(),
                 new TreeDisplay(t)));
 
         t.getModel().addTreeModelListener(this);
         t.addTreeSelectionListener(this);
         if (app != null)
             t.addTreeSelectionListener(app);
         t.addFocusListener(this);
         t.requestFocusInWindow();
 
         if (leftTrees.getTabCount() == 0) {
             this.remove(leftTrees);
             makeRightLeft();
         } else if (target == rightTrees) {
             if (!splitView)
                 showSplit();
             moveLeftAction.setEnabled(true);
             moveRightAction.setEnabled(true);
         }
 
 
         // TODO: check which one we need here
         target.validate();
         target.revalidate();
 
         this.revalidate();
 
     }
 
     protected void removeTab(JTabbedPane pane, int index) {
 
         if (pane.getTabCount() <= index)
             return;
 
         EventTree tree = ((TreeDisplay) pane.getComponentAt(index)).getTree();
 
         tree.removeFocusListener(this);
         if (pane == leftTrees)
             timelines.remove(index);
         else
             timelines.remove(index + leftTrees.getTabCount());
 
         pane.removeTabAt(index);
 
         if (pane.getTabCount() == 0) {
             if (pane == rightTrees)
                 hideSplit();
             else {
                 if (splitView) {
                     treePane.remove(leftTrees);
                     this.remove(treePane);
                     makeRightLeft();
                 } else
                     this.revalidate();
             }
             splitView = false;
             leftTrees.requestFocus();
             moveLeftAction.setEnabled(false);
             if (leftTrees.getTabCount() < 2)
                 moveRightAction.setEnabled(false);
         } else
             pane.requestFocus();
 
         this.revalidate();
 
     }
 
     protected void makeRightLeft() {
 
         JTabbedPane tmp = leftTrees;
         leftTrees = rightTrees;
         rightTrees = tmp;
         this.add(leftTrees);
         this.revalidate();
 
     }
 
     public EventTree getCurrentTree() {
         return currentTree;
     }
 
     public void moveRight() {
 
         if (leftTrees.getTabCount() < 1)
             return;
 
         addTreeToTab(rightTrees,
                 ((TreeDisplay) leftTrees.getSelectedComponent()).getTree(), null);
         removeTab(leftTrees, leftTrees.getSelectedIndex());
 
     }
 
     public void moveLeft() {
 
         if (!splitView)
             return;
 
         addTreeToTab(leftTrees,
                 ((TreeDisplay) rightTrees.getSelectedComponent()).getTree(), null);
         removeTab(rightTrees, rightTrees.getSelectedIndex());
 
     }
 
     public void redraw() {
 
         int i;
         EventTree t;
 
         for (i = 0; i < timelines.size(); i++) {
             t = timelines.get(i);
             ((EventTreeModel) t.getModel()).refresh();
         }
 
     }
 
     public void deleteTree(EventTree toDelete) {
 
         int i;
 
         hideEventMask();
 
         for (i = 0; i < timelines.size(); i++) {
             if ((timelines.get(i)).equals(toDelete)) {
                 int divider = leftTrees.getTabCount();
                 if (i < divider)
                     removeTab(leftTrees, i);
                 else
                     removeTab(rightTrees, i - divider);
                 if (leftTrees.getTabCount() == 0) {
                     removeAction.setEnabled(false);
                     filterAction.setEnabled(false);
                     findAction.setEnabled(false);
                 }
                 return;
             }
         }
 
 
     }
 
     public void clearSelections() {
 
         int i;
 
         for (i = 0; i < timelines.size(); i++)
             (timelines.get(i)).clearSelection();
 
         if (!isOrphanVisible)
             orphanTree.clearSelection();
 
     }
 
     public void removeSelected(EventTree source) {
 
         EventTreeModel orphan_model = (EventTreeModel) orphanTree.getModel();
         ComplexEvent target = (ComplexEvent) orphan_model.getRoot();
         source.moveSelected(target, orphan_model);
 
     }
 
     public void showOrphan() {
 
         if (isOrphanVisible)
             return;
 
         addTree(orphanTree, null);
         isOrphanVisible = true;
 
     }
 
     public void hideOrphan() {
 
         if (!isOrphanVisible)
             return;
 
         deleteTree(orphanTree);
         isOrphanVisible = false;
 
     }
 
     public void toggleOrphanVisible() {
 
         if (isOrphanVisible)
             hideOrphan();
         else
             showOrphan();
 
     }
 
     public EventTree getOrphanTree() {
         return orphanTree;
     }
 
     // TreeSelectionListener interface
     public void valueChanged(TreeSelectionEvent e) {
 
         if (checkComplexMask(e.getOldLeadSelectionPath(), currentTree, (EventTree) e.getSource()))
             return;
 
         EventTree oldCurrent = currentTree;
         currentTree = (EventTree) e.getSource();
 
         if ((oldCurrent != null) && (oldCurrent != currentTree)) {
             oldCurrent.getDisplay().hideBorder();
             currentTree.getDisplay().showBorder();
         }
 
         currentSelection = e.getNewLeadSelectionPath();
 
         if (currentSelection == null) {
             hideEventMask();
             return;
         }
 
         AbstractTimeEvent event = (AbstractTimeEvent) currentSelection.getLastPathComponent();
         displayEvent(event);
 
     }
 
     private boolean checkComplexMask(TreePath oldSelection, EventTree oldTree, EventTree newTree) {
 
 /*	    
     if (complexMaskChangeCancelled) {
         complexMaskChangeCancelled = false;
         return true;
     }
 */
 
         if (complexMask.isVisible() && complexMask.isModified()) {
 
             TreePath newSelection = newTree.getSelectionPath();
 /*	    System.out.println("Current selection: " + currentSelection);
 	    System.out.println("Old Path: " + oldSelection);
 	    System.out.println("Old Tree Selection: " + oldTree.getSelectionPath());
 	    System.out.println("New Tree Selection: " + newSelection);
 	    System.out.println("Current Tree: " + currentTree);
 */
             if ((newSelection == null) || ((currentSelection != null) && currentSelection.equals(newSelection))) {
                 return true;
             }
 
             int choice = complexMask.checkUpdate();
 
             if (choice == JOptionPane.CANCEL_OPTION) {
 
 //			complexMaskChangeCancelled = true;
 
                 if (oldTree.equals(newTree)) {
                     oldTree.setSelectionPath(oldSelection);
 //				complexMaskChangeCancelled = false;
                 } else {
                     currentTree = oldTree;
                     newTree.clearSelection();
 //				oldTree.setSelectionPath(oldSelection);
                 }
 
                 return true;
             }
 
             return false;
 
         }
 
         return false;
 
     }
 
     private void displayEvent(AbstractTimeEvent event) {
 
         if (event instanceof L2TEvent) {
             complexMask.setVisible(false);
             L2TEvent le = (L2TEvent) event;
             l2tMask.set(le);
             l2tMask.setVisible(true);
             pasteAction.setEnabled(false);
         } else if (event instanceof AtomicEvent) {
             complexMask.setVisible(false);
             AtomicEvent ae = (AtomicEvent) event;
             atomicMask.set(ae);
             atomicMask.setVisible(true);
             pasteAction.setEnabled(false);
         } else {
             atomicMask.setVisible(false);
             ComplexEvent ce = (ComplexEvent) event;
             complexMask.set(ce, currentTree.getModel());
             complexMask.setVisible(true);
             pasteAction.setEnabled(((PasteAction) pasteAction).pastePossible());
         }
 
     }
 
     private void hideEventMask() {
         l2tMask.setVisible(false);
         atomicMask.setVisible(false);
         complexMask.setVisible(false);
     } // hideEventMask
 
 
     /* Methods that implement FocusListener interface */
 
     public void focusGained(FocusEvent e) {
 //	    System.out.println("Focus event: " + e);
         setCurrentTree(e.getSource());
     } // focusGained
 
     public void focusLost(FocusEvent e) {
     }
 
     /* Method that implements ChangeListener interface */
 
     public void stateChanged(ChangeEvent e) {
 //	    System.out.println("Change event: " + e);
         setCurrentTree(e.getSource());
     } // stateChanged
 
     protected void setCurrentTree(Object obj) {
 
         EventTree oldCurrent = currentTree;
         EventTree newTree;
 
         if (obj instanceof EventTree) {
             newTree = (EventTree) obj;
         } else if (obj instanceof JTabbedPane) {
             TreeDisplay td = (TreeDisplay) ((JTabbedPane) obj).getSelectedComponent();
             if (td != null)
                 newTree = td.getTree();
             else
                 return;
         } else
             return;
         if ((oldCurrent != null) && checkComplexMask(oldCurrent.getSelectionPath(), oldCurrent, newTree)) {
 //		currentTree = oldCurrent;
             return;
         }
 
         currentTree = newTree;
         currentSelection = currentTree.getLeadSelectionPath();
 
         if ((currentTree.getSelectionCount() == 0) || (currentSelection == null)) {
             cutAction.setEnabled(false);
             clearAction.setEnabled(false);
             hideEventMask();
         } else {
             // TODO: when drag&drop for tabs is implemented, this if-statement
             //       may be removed and the actions always be enabled
             if (!currentTree.isPathSelected(new TreePath(new Object[]{currentTree.getModel().getRoot()}))) {
                 cutAction.setEnabled(true);
                 clearAction.setEnabled(true);
             }
             displayEvent((AbstractTimeEvent) currentSelection.getLastPathComponent());
         }
 
         if ((oldCurrent != null) && (oldCurrent != currentTree))
             oldCurrent.getDisplay().hideBorder();
 
         currentTree.getDisplay().showBorder();
         if (((ComplexEvent) currentTree.getModel().getRoot()).countChildren()
                 == 0) {
             removeAction.setEnabled(true);
             filterAction.setEnabled(false);
             findAction.setEnabled(false);
         } else {
             removeAction.setEnabled(false);
             filterAction.setEnabled(true);
             findAction.setEnabled(true);
         }
         if (currentTree == orphanTree)
             removeAction.setEnabled(false);
     } // setCurrentTree
 
     public boolean isOrphan(EventTree t) {
         return (t == orphanTree);
     } // isOrphan
 
     public void saveEventTrees(ObjectOutputStream out_stream) {
 
         // make sure the orphan tree isn't null
         if (orphanTree != null) {
             // save the ComplexEvents from the orphan tree
             orphanTree.saveComplexEvents(out_stream);
         } else {
             try {
                 out_stream.writeObject(null);
             } catch (IOException io_excep) {
                 System.err.println("ERROR: IOException while writing null orphan tree\n\t"
                         + io_excep.toString());
             }
         }
 
         // write the number of trees in the left pane
 
         int count = leftTrees.getTabCount();
 
         try {
             out_stream.writeInt(count);
         } catch (IOException io_excep) {
             System.err.println("ERROR: IOException while printing number of trees in left pane \n\t"
                     + io_excep.toString());
         }
 
         // write the trees in the left pane
         for (int i = 0; i < count; i++) {
             ((TreeDisplay) leftTrees.getComponentAt(i)).getTree().saveComplexEvents(out_stream);
         }
 
         // write the number of trees in the right pane
 
         count = rightTrees.getTabCount();
 
         try {
             out_stream.writeInt(count);
         } catch (IOException io_excep) {
             System.err.println("ERROR: IOException while printing number of trees in right pane \n\t"
                     + io_excep.toString());
         }
 
         // write the threes in the right pane
         for (int i = 0; i < count; i++) {
             ((TreeDisplay) rightTrees.getComponentAt(i)).getTree().saveComplexEvents(out_stream);
         }
 
     } // saveEventTrees
 
     public void loadFromFile(ObjectInputStream in_stream) {
 
         try {
             orphanTree = new EventTree((ComplexEvent) in_stream.readObject());
             orphanTree.addTreeSelectionListener(app);
 
             int left_count = in_stream.readInt();
             leftTrees.removeAll();
             for (int i = 0; i < left_count; i++) {
                 addTreeToTab(leftTrees, new EventTree((ComplexEvent) in_stream.readObject()), app);
             }
 
             int right_count = in_stream.readInt();
             if (right_count == 0) hideSplit();
             rightTrees.removeAll();
             for (int i = 0; i < right_count; i++) {
                 addTreeToTab(rightTrees, new EventTree((ComplexEvent) in_stream.readObject()), app);
             }
         } catch (IOException io_excep) {
             System.err.println("ERROR: IOException while loading TimelineView from ObjectInputStream\n\t"
                     + io_excep.toString());
         } catch (ClassNotFoundException cnf_excep) {
             System.err.println("ERROR: ClassNotFoundException while loading TimelineView from ObjectInputStream\n\t"
                     + cnf_excep.toString());
         }
 
     }
 
     // TreeModelListener interface //
 
     public void treeNodesChanged(TreeModelEvent e) {
         saveAction.setEnabled(true);
     } // treeNodesChanged
 
     public void treeNodesInserted(TreeModelEvent e) {
         saveAction.setEnabled(true);
     } // treeNodesInserted
 
     public void treeNodesRemoved(TreeModelEvent e) {
         saveAction.setEnabled(true);
     } // treeNodesRemoved
 
     public void treeStructureChanged(TreeModelEvent e) {
         saveAction.setEnabled(true);
     } // treeStructureChanged
 
     public void mouseClicked(MouseEvent e) {
        JTabbedPane pane = (JTabbedPane) e.getSource();
        TreeDisplay selected = (TreeDisplay) pane.getSelectedComponent();
        
        if (selected != null)
            setCurrentTree(selected.getTree());
     }
 
     public void mouseEntered(MouseEvent e) {
     }
 
     public void mouseExited(MouseEvent e) {
     }
 
     public void mousePressed(MouseEvent e) {
     }
 
     public void mouseReleased(MouseEvent e) {
     }
 
 
     protected EventTree getNextTree(EventTree current) {
 
         int index;
 
         if (current == null) {
             if (timelines.size() == 0)
                 return orphanTree;
             else
                 return timelines.get(0);
         }
 
         if ((index = timelines.indexOf(current)) == -1)
             return null;
 
         if (index + 2 > timelines.size())
             return null;
 
         return timelines.get(index + 1);
 
     } // getNextTree
 
     protected EventTree getPreviousTree(EventTree current) {
 
         int index;
 
         if (current == null) {
             if (timelines.size() == 0)
                 return orphanTree;
             else
                 return timelines.get(timelines.size() - 1);
         }
 
         if ((index = timelines.indexOf(current)) == -1)
             return null;
 
         if (index == 0)
             return null;
 
         return timelines.get(index - 1);
 
     } // getPreviousTree
 
     public void displayTree(EventTree t) {
 
         int i;
 
         t.requestFocusInWindow();
 
         if (t.equals(orphanTree) && !isOrphanVisible) {
             showOrphan();
             return;
         }
 
         for (i = 0; i < timelines.size(); i++) {
             if ((timelines.get(i)).equals(t)) {
                 int divider = leftTrees.getTabCount();
                 if (i < divider)
                     leftTrees.setSelectedIndex(i);
                 else
                     rightTrees.setSelectedIndex(i - divider);
 
                 return;
             }
         }
 
     } // displayTree
 
     public void initFindEvent(EventTree startTree) {
 
         TreePath tp = null;
 
         if (startTree != null)
             tp = startTree.getLeadSelectionPath();
 
         if (tp != null) {
             currentFindTree = startTree;
             currentFindModel = (EventTreeModel) currentFindTree.getModel();
             currentFindPosition = new FindEntries();
             ComplexEvent parent = (ComplexEvent) tp.getPathComponent(0);
             for (int i = 1; i < tp.getPathCount(); i++) {
 
 
                 AbstractTimeEvent current = (AbstractTimeEvent) tp.getPathComponent(i);
                 int position = currentFindModel.getIndexOfChild(parent, current);
                 currentFindPosition.push(new EntryItem(parent, position));
                 if (current instanceof ComplexEvent)
                     parent = (ComplexEvent) current;
                 else
                     break;
 
             }
         } else {
 
             currentFindTree = getNextTree(null);
             currentFindPosition = new FindEntries();
             currentFindPosition.push(new EntryItem(((ComplexEvent) currentFindTree.getModel().getRoot()), -1));
         }
 
         currentFindModel = (EventTreeModel) currentFindTree.getModel();
         validFindPosition = currentFindPosition.copy();
 
     } // initFindEvent
 
     public boolean findNextEvent(Query q, boolean wrapAround) {
         return findEvent(q, wrapAround, DIRECTION_FORWARD);
     } // findNextEvent
 
     public boolean findPreviousEvent(Query q, boolean wrapAround) {
         return findEvent(q, wrapAround, DIRECTION_BACKWARD);
     } // findPreviousEvent
 
     protected boolean findEvent(Query q, boolean wrapAround, int direction) {
 
         int run = 1;
         currentFindPosition = validFindPosition.copy();
         EventTree oldTree = currentFindTree;
 
         while (true) {
 
             TreePath match = findNextEventInCurrent(q, direction);
             if (match != null) {
                 oldTree.clearSelection();
                 displayTree(currentFindTree);
                 currentFindTree.setSelectionPath(match);
                 currentFindTree.centerEvent((AbstractTimeEvent) match.getLastPathComponent());
                 validFindPosition = currentFindPosition.copy();
                 return true;
             }
 
             if (direction == DIRECTION_FORWARD)
                 currentFindTree = getNextTree(currentFindTree);
             else
                 currentFindTree = getPreviousTree(currentFindTree);
 
             if (currentFindTree == null) {
                 if (wrapAround) {
                     if (run > 1) {
                         currentFindTree = oldTree;
                         currentFindPosition = validFindPosition.copy();
                         return false;
                     } else {
                         if (direction == DIRECTION_FORWARD)
                             currentFindTree = getNextTree(null);
                         else
                             currentFindTree = getPreviousTree(null);
 
                         run = run + 1;
                     }
                 } else {
                     currentFindTree = oldTree;
                     currentFindPosition = validFindPosition.copy();
                     return false;
                 }
             }
             currentFindModel = (EventTreeModel) currentFindTree.getModel();
             currentFindPosition = new FindEntries();
             ComplexEvent root = (ComplexEvent) currentFindTree.getModel().getRoot();
             if (direction == DIRECTION_FORWARD)
                 currentFindPosition.push(new EntryItem(root, -1));
             else
                 currentFindPosition.push(new EntryItem(root, currentFindTree.getModel().getChildCount(root)));
 
         }
 
     } // findEvent
 
     protected TreePath findNextEventInCurrent(Query q, int direction) {
 
         int current, max;
 
         while (true) {
 
             ComplexEvent parent = currentFindPosition.getCurrentEvent();
             max = currentFindModel.getChildCount(parent);
 
             current = currentFindPosition.getCurrentIndex() + direction;
             while (true) {
                 if (direction == DIRECTION_FORWARD) {
                     if (current >= max)
                         break;
                 } else {
                     if (current < 0)
                         break;
                 }
 
                 AbstractTimeEvent te = (AbstractTimeEvent) currentFindModel.getChild(parent, current);
                 if ((q == null) || (q.matches(te))) {
                     currentFindPosition.setCurrentIndex(current);
                     TreePath parentPath = currentFindPosition.getPath();
                     if (te instanceof ComplexEvent)
                         currentFindPosition.push(new EntryItem((ComplexEvent) te, -1));
                     return parentPath.pathByAddingChild(te);
                 }
                 if (te instanceof ComplexEvent) {
                     if (direction == DIRECTION_FORWARD)
                         currentFindPosition.push(new EntryItem((ComplexEvent) te, -1));
                     else
                         currentFindPosition.push(new EntryItem((ComplexEvent) te, currentFindModel.getChildCount(te)));
                 }
 
                 current = current + direction;
 
             }
             currentFindPosition.pop();
             if (currentFindPosition.isEmpty())
                 return null;
         }
 
 
     } // findNextEventInCurrent
 
     protected class EntryItem {
 
         protected ComplexEvent parent;
         protected int index;
 
         EntryItem(ComplexEvent p, int i) {
             parent = p;
             index = i;
         }
 
         public ComplexEvent getParent() {
             return parent;
         }
 
         public int getIndex() {
             return index;
         }
 
         public void setIndex(int newIndex) {
             index = newIndex;
         }
 
 
     }
 
     protected class FindEntries {
 
         protected Stack<EntryItem> entries;
 
         FindEntries() {
             entries = new Stack<EntryItem>();
         }
 
         public int getCurrentIndex() {
             if (entries.empty())
                 return -1;
             EntryItem ei = entries.peek();
             return ei.getIndex();
         }
 
         public ComplexEvent getCurrentEvent() {
             if (entries.empty())
                 return null;
             EntryItem ei = entries.peek();
             return ei.getParent();
         }
 
         public void setCurrentIndex(int newIndex) {
             if (entries.empty())
                 return;
             EntryItem ei = entries.peek();
             ei.setIndex(newIndex);
         }
 
         public void push(EntryItem ei) {
             entries.push(ei);
         }
 
         public EntryItem pop() {
             return entries.pop();
         }
 
         public boolean isEmpty() {
             return entries.empty();
         }
 
         public FindEntries copy() {
 
             FindEntries ret = new FindEntries();
 
             for (int i = 0; i < entries.size(); i++) {
                 EntryItem item = entries.elementAt(i);
                 ret.push(new EntryItem(item.getParent(), item.getIndex()));
             }
 
             return ret;
 
         }
 
         public TreePath getPath() {
             if (entries.empty())
                 return null;
 
             List<ComplexEvent> temp = new ArrayList<ComplexEvent>();
 
             for (int i = 0; i < entries.size(); i++)
                 temp.add((entries.elementAt(i)).getParent());
 
             return new TreePath(temp.toArray());
 
         }
     }
 
 
 }
