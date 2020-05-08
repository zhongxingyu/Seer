 package com.canoo.ulc.detachabletabbedpane.server;
 
 import com.ulcjava.base.application.ULCComponent;
 import com.ulcjava.base.application.ULCFrame;
 import com.ulcjava.base.application.ULCMenuItem;
 import com.ulcjava.base.application.ULCPopupMenu;
 import com.ulcjava.base.application.ULCWindow;
 import com.ulcjava.base.application.UlcUtilities;
 import com.ulcjava.base.application.event.ActionEvent;
 import com.ulcjava.base.application.event.IActionListener;
 import com.ulcjava.base.application.event.IWindowListener;
 import com.ulcjava.base.application.event.WindowEvent;
 import com.ulcjava.base.application.util.Dimension;
 import com.ulcjava.base.application.util.ULCIcon;
 import com.ulcjava.base.server.ULCSession;
 
 import java.util.ArrayList;
 import java.util.EventListener;
 import java.util.Iterator;
 
 /**
  * Class that offers undocking functionality on top of a <code>ULCCloseableTabbedPane</code> A <code>ULCDetachableTabbedPane</code> might be
  * used as an ordinary <code>ULCCloseableTabbedPane</code> but tabs might be detached and appear in an own window. There might be different
  * detached windows, at most as many as there are tabs in the initial <code>ULCDetachableTabbedPane</code>. Tabs can be moved via drag and
  * drop between the windows that were derived from the same <code>ULCDetachableTabbedPane</code>. In this way, the tabs can still be
  * searched and accessed via the original <code>ULCDetachableTabbedPane</code>.
  * <p/>
  *
  * @author Alexandra Teynor
  * @author <a href="mailto:Alexandra.Teynor@canoo.com">Alexandra.Teynor@canoo.com</a>
  * @version 1.0, &nbsp; 02-OCT-2009
  */
 public class ULCDetachableTabbedPane extends ULCCloseableTabbedPane {
 
     /**
      * List with frames for the detached panes
      */
     private ArrayList dependentFrames = new ArrayList();
 
     /**
      * In case this is a dependent frame, this field holds the parent Otherwise it is null.
      */
     private ULCDetachableTabbedPane parentDTabbedPane;
 
     /**
      * Determine whether this TabbedPane is dependent (has a parent)
      */
     public boolean isDependent() {
         if (parentDTabbedPane == null)
             return false;
         else
             return true;
     }
 
     /**
      * Upload the state to the UI
      */
     protected void uploadStateUI() {
         super.uploadStateUI();
 
         int id;
         if (isDependent()) {
             id = parentDTabbedPane.getId();
         } else {
             id = getId();
         }
         setStateUI("groupId", null, id);
     }
 
     /**
      * Create a ULCDetachableTabbedPane. By default, detaching is enabled and we have a top level ULCDetachableTabbedPane
      */
 
     public ULCDetachableTabbedPane() {
         // default
         this(true);
     }
 
     /**
      * Create a ULCDetachableTabbedPane.
      *
      * @param detachingEnabled determines whether tabs might be detached
      */
     public ULCDetachableTabbedPane(boolean detachingEnabled) {
 
         super();
 
         // create a context menu for this tabbed pane that allows
         // for undocking, if desired
         if (detachingEnabled) {
 
             ULCPopupMenu componentPopupMenu = new ULCPopupMenu("undock");
             ULCMenuItem menuItemUnDock = new ULCMenuItem("Undock");
             menuItemUnDock.addActionListener(new IActionListener() {
                 public void actionPerformed(ActionEvent event) {
                     int idx = getSelectedIndex();
                     undock(idx);
                 }
             });
 
             componentPopupMenu.add(menuItemUnDock);
             setComponentPopupMenu(componentPopupMenu);
         }
     }
 
     /**
      * Sets the parent frame for this one, i.e the tabs that can be imported were initially in this parent tabbed pane.
      *
      * @param tp the parent tabbed pane
      */
     private void setParentTabbedPane(ULCDetachableTabbedPane tp) {
         parentDTabbedPane = tp;
     }
 
     /**
      * Returns the parent frame for this one,
      *
      * @return tp the parent tabbed pane, null if it is a top level tabbed pane
      */
     private ULCDetachableTabbedPane getParentTabbedPane() {
         return parentDTabbedPane;
     }
 
     /**
      * Ensure that there are no empty dependant tabbed panes belonging to this group of tabbed panes
      */
     public void cleanUpFrames() {
 
         if (isDependent()) {
             parentDTabbedPane.cleanUpFrames();
         } else {
             Iterator fIter = dependentFrames.iterator();
 
             int i = 0;
             while (fIter.hasNext()) {
 
                 ULCFrame cFrame = (ULCFrame) fIter.next();
                 ULCCloseableTabbedPane tp = (ULCCloseableTabbedPane) cFrame.getContentPane().getComponents()[0];
 
                 if (tp.getTabCount() == 0) {
                     // close this frame
                     cFrame.dispose();
                     fIter.remove();
                 }
             }
         }
     }
 
     /**
      * Detach a tab containing this a specific component. If the component can not be found, nothing happens, otherwise a Dialog with a new
      * TabbedPane is opened an the Tab containing the component transferred to the new TabbedPane. Existing TabListeners are also
      * transferred, and the deraultClosingBehaviour of the Tab is maintained.
      *
      * @param component the component on the tab to be detached
      */
     public void undock(ULCComponent component) {
         int tIdx = -1;
         // search the index of this tab
         for (int i = 0; i < getTabCount(); ++i) {
             if (component.equals(getComponentAt(i))) {
                 tIdx = i;
             }
         }
         if (tIdx != -1)
             undock(tIdx);
     }
 
     /**
      * Detach the tab with this window. If the index is invalid, nothing happens, otherwise a Dialog with a new TabbedPane is opened an the
      * Tab with this index is transferred to the new TabbedPane. Existing TabListeners are also transferred, and the deraultClosingBehaviour
      * of the Tab is maintained.
      *
      * @param tabIndex the index of the tab to be detached
      */
     public void undock(int tabIndex) {
 
        if (tabIndex < 0 || tabIndex >= getTabCount()) {
             return;
        }
 
         // create a new frame with a tabbed pane
         ULCWindow window = UlcUtilities.getWindowAncestor(this);
         final ULCFrame detachedFrame = new ULCFrame();
         final ULCDetachableTabbedPane newPane = new ULCDetachableTabbedPane(false);
 
         if (window instanceof ULCFrame) {
             detachedFrame.setIconImage(((ULCFrame) window).getIconImage());
         }
 
         newPane.setParentTabbedPane(this);
         detachedFrame.add(newPane);
 
         // add a window listener, in case the frame is closed
         detachedFrame.addWindowListener(new IWindowListener() {
             public void windowClosing(WindowEvent event) {
                 // close all tabs contained in this frame
                 ULCFrame frame = (ULCFrame) event.getSource();
                 ULCCloseableTabbedPane tp = (ULCCloseableTabbedPane) frame.getContentPane().getComponents()[0];
                 tp.removeAll();
                 cleanUpFrames();
             }
         });
 
         // Tansfer the event listeners registerd to this tabbedPane
         EventListener[] el = getListeners(TabEvent.EVENT_CATEGORY);
         for (int i = 0; i < el.length; ++i) {
             newPane.addListener(TabEvent.EVENT_CATEGORY, el[i]);
         }
 
         detachedFrame.setTitle(getTitleAt(tabIndex));
         int closingBehaviour = getDefaultCloseTabOperation(tabIndex);
 
         // transfer the tab
         newPane.addTab(getTitleAt(tabIndex), getIconAt(tabIndex), getComponentAt(tabIndex), getToolTipTextAt(tabIndex));
 
         // use the closing behavior that was originally set for this pane
         newPane.setDefaultCloseTabOperation(newPane.getTabCount() - 1, closingBehaviour);
 
         // register the frame, so that it can still be accessed
         dependentFrames.add(detachedFrame);
 
         // make frame visible
         detachedFrame.setSize(getNewFrameDimension(0.8, 0.6));
         detachedFrame.setLocationRelativeTo(this);
         detachedFrame.setVisible(true);
     }
 
     private Dimension getNewFrameDimension(double widthScale, double heightScale) {
         ULCWindow window = UlcUtilities.getWindowAncestor(this);
         Dimension dimension;
         if (window != null) {
             int width = (int) ((double) window.getSize().getWidth() * widthScale);
             int height = (int) ((double) window.getSize().getHeight() * heightScale);
             dimension = new Dimension(width, height);
         } else {
             dimension = new Dimension(800, 600);
         }
         return dimension;
     }
 
     /**
      * Import a tab with index <code>tabIndex</code> from the tabbed pane with ULC-ID <code>objectId</code> into this tabbed pane
      *
      * @param objectId the ULC-ID of the tabbedPane, from where the tab should be imported
      * @param tabIdx   the index of the tab to be detached
      */
     public void importTab(int objectId, int tabIdx) {
 
         // Get object from id
         ULCDetachableTabbedPane tp = (ULCDetachableTabbedPane) ULCSession.currentSession().getRegistry().find(objectId);
         int closingBehaviour = tp.getDefaultCloseTabOperation(tabIdx);
 
         // import the tab:
         addTab(tp.getTitleAt(tabIdx), tp.getIconAt(tabIdx), tp.getComponentAt(tabIdx), tp.getToolTipTextAt(tabIdx));
         setDefaultCloseTabOperation(getTabCount() - 1, closingBehaviour);
 
         // look if there are empty dependent frames
         cleanUpFrames();
     }
 
     public int getNumDependentFrames() {
         return dependentFrames.size();
     }
 
 
     /**
      * Returns the frame number currently containing this component. The original frame is frame no. 0, the dependent fame nums start with 1
      * If the component was not found, return -1
      *
      * @param component the component to be searched for
      * @return the ULCFrame in which this component currently is, if not found null is returned
      */
     public int findFrameID(ULCComponent component) {
 
         // look if the component is contained in this frame
         for (int i = 0; i < getTabCount(); ++i) {
             if (getComponentAt(i) == component)
                 return 0;
         }
 
         // look if it is in an dependent frame
         for (int i = 0; i < dependentFrames.size(); ++i) {
 
             ULCCloseableTabbedPane tp = getDependantTabbedPane(i);
 
             // look in all tabs in this tabbed pane
             for (int j = 0; j < tp.getComponentCount(); ++j) {
                 if (component == tp.getComponentAt(j)) {
                     return i + 1;
                 }
             }
         }
 
         // else no frame found containing this component
         return -1;
     }
 
     /**
      * Returns the frame number currently containing this component. The original frame is frame no. 0, the dependent fame nums start with 1
      * If the component was not found, return -1
      *
      * @param title the tab title to be searched for
      * @return the ULCFrame in which this component currently is, if not found null is returned
      */
     public int findFrameID(String title) {
 
         // look if the component is contained in this frame
         for (int i = 0; i < getTabCount(); ++i) {
             if (title.equals(getTitleAt(i)))
                 return 0;
         }
 
         // look if it is in an dependent frame
         for (int i = 0; i < dependentFrames.size(); ++i) {
 
             ULCCloseableTabbedPane tp = getDependantTabbedPane(i);
             // look in all tabs in this tabbed pane
             for (int j = 0; j < tp.getComponentCount(); ++j) {
                 if (title.equals(tp.getTitleAt(j))) {
                     return i + 1;
                 }
             }
         }
 
         // else no frame found containing this component
         return -1;
     }
 
 
     private ULCCloseableTabbedPane getDependantTabbedPane(int i) {
         if (i >= 0 && i < dependentFrames.size()) {
             ULCFrame dialog = (ULCFrame) dependentFrames.get(i);
             ULCCloseableTabbedPane tp = (ULCCloseableTabbedPane) dialog.getContentPane().getComponents()[0];
             return tp;
         } else {
             throw new IndexOutOfBoundsException("Frame with index " + i + " does not exist.");
         }
     }
 
     /**
      * Sets the title of a dependent frame
      *
      * @param frameNum number of the dependent frame, staring with 1
      * @param title    the title
      */
     public void setFrameTitle(int frameNum, String title) {
         if (frameNum > 0 && frameNum < dependentFrames.size()) {
             ULCFrame dialog = (ULCFrame) dependentFrames.get(frameNum - 1);
             dialog.setTitle(title);
         }
     }
 
     /**
      * Get the frame title of a dependent frame
      *
      * @param frameNum number of the demendant frame, staring with 1
      * @return the title of the frame, null if an invalid fame num was specified
      */
     public String getFrameTitle(int frameNum) {
         if (frameNum > 0 && frameNum < dependentFrames.size()) {
             ULCFrame dialog = (ULCFrame) dependentFrames.get(frameNum - 1);
             return dialog.getTitle();
         } else
             return null;
     }
 
     /**
      * Sets the tab tile for a tab containing a specific component
      *
      * @param component
      * @param title     the new title of the frame
      */
     public void setTabTitle(ULCComponent component, String title) {
 
         // look if the component is contained in this frame
         for (int i = 0; i < getTabCount(); ++i) {
             if (getComponentAt(i) == component) {
                 setTitleAt(i, title);
                 return;
             }
         }
 
         // look if it is in an dependent frame
         for (int i = 0; i < dependentFrames.size(); ++i) {
 
             ULCCloseableTabbedPane tp = getDependantTabbedPane(i);
             // look in all tabs in this tabbed pane
             for (int j = 0; j < tp.getComponentCount(); ++j) {
                 if (component == tp.getComponentAt(j)) {
                     tp.setTitleAt(j, title);
                     return;
                 }
             }
         }
     }
 
     /**
      * Method for inquiring whether a specific component is either contained in the main tabbed pane or in dependant tabbed panes
      *
      * @param component
      */
     public boolean anyTabContains(ULCComponent component) {
 
         // look if the component is contained in this frame
         for (int i = 0; i < getTabCount(); ++i) {
             if (getComponentAt(i) == component) {
                 return true;
             }
         }
 
         // look if it is in an dependent frame
         for (int i = 0; i < dependentFrames.size(); ++i) {
 
             ULCCloseableTabbedPane tp = getDependantTabbedPane(i);
             // look in all tabs in this tabbed pane
             for (int j = 0; j < tp.getComponentCount(); ++j) {
                 if (component == tp.getComponentAt(j)) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     /**
      * Method for inquiring whether a tab with a specific title is either contained in the main tabbed pane or in dependant tabbed panes
      *
      * @param title the tab title to be searched for
      */
     public boolean anyTabContains(String title) {
 
         // look if the component is contained in this frame
         for (int i = 0; i < getTabCount(); ++i) {
             if (title.equals(getTitleAt(i))) {
                 return true;
             }
         }
 
         // look if it is in an dependent frame
         for (int i = 0; i < dependentFrames.size(); ++i) {
 
             ULCCloseableTabbedPane tp = getDependantTabbedPane(i);
             // look in all tabs in this tabbed pane
             for (int j = 0; j < tp.getComponentCount(); ++j) {
                 if (title.equals(tp.getTitleAt(j))) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
 
     /**
      * Sets the focus to a tab containing a specific component
      *
      * @param component
      */
     public void setTabFocus(ULCComponent component) {
 
         // look if the component is contained in this frame
         for (int i = 0; i < getTabCount(); ++i) {
             if (getComponentAt(i) == component) {
                 setSelectedComponent(component);
                 return;
             }
         }
 
         // look if it is in a dependent frame
         for (int i = 0; i < dependentFrames.size(); ++i) {
 
             ULCCloseableTabbedPane tp = getDependantTabbedPane(i);
             // look in all tabs in this tabbed pane
             for (int j = 0; j < tp.getComponentCount(); ++j) {
                 if (component == tp.getComponentAt(j)) {
                     ULCFrame dialog = (ULCFrame) dependentFrames.get(i);
                     dialog.toFront();
                     dialog.requestFocus();
                     tp.setSelectedComponent(component);
                     return;
                 }
             }
         }
     }
 
     /**
      * Sets the focus to a tab containing a specific title If multiple tabs have the same title the first one gets the focus.
      *
      * @param title
      */
     public void setTabFocus(String title) {
 
         // look if the component is contained in this frame
         for (int i = 0; i < getTabCount(); ++i) {
             if (title.equals(getTitleAt(i))) {
                 setSelectedIndex(i);
                 return;
             }
         }
 
         // look if it is in a dependent frame
         for (int i = 0; i < dependentFrames.size(); ++i) {
 
             ULCCloseableTabbedPane tp = getDependantTabbedPane(i);
             // look in all tabs in this tabbed pane
             for (int j = 0; j < tp.getComponentCount(); ++j) {
                 if (title.equals(tp.getTitleAt(j))) {
                     ULCFrame dialog = (ULCFrame) dependentFrames.get(i);
                     dialog.toFront();
                     dialog.requestFocus();
                     tp.setSelectedIndex(j);
                     return;
                 }
             }
         }
     }
 
 
     /**
      * Add a tab at the last position in this tabbed pane.
      *
      * @param title     Title of the tab
      * @param component The component in the content parent of the tab
      * @param detached  determines whether this tab is added at the tab or as a separate window
      */
     public void addTab(String title, ULCComponent component, boolean detached) {
         addTab(title, null, component, null, detached);
     }
 
     public void addTab(String title, ULCComponent component, boolean detached, int closingDefaultBehaviour) {
         addTab(title, null, component, null, detached, closingDefaultBehaviour);
     }
 
 
     /**
      * Add a tab at the last position in this tabbed pane.
      *
      * @param title     Title of the tab
      * @param icon      Icon that is displayed before the name
      * @param component The component in the content parent of the tab
      * @param detached  determines whether this tab is added at the tab or as a separate window
      */
     public void addTab(String title, ULCIcon icon, ULCComponent component, boolean detached) {
         addTab(title, icon, component, null, detached);
     }
 
     /**
      * Add a tab at the last position in this tabbed pane.
      *
      * @param title                   Title of the tab
      * @param icon                    Icon that is displayed before the name
      * @param component               The component in the content parent of the tab
      * @param detached                determines whether this tab is added at the tab or as a separate window
      * @param closingDefaultBehaviour determines the behavior of the tab when closing it. Default is
      *                                ULCCloseableTabbedPane.FIRE_EVENT_ON_CLOSING_TAB
      */
     public void addTab(String title, ULCIcon icon, ULCComponent component, boolean detached, int closingDefaultBehaviour) {
         addTab(title, icon, component, null, detached, closingDefaultBehaviour);
     }
 
     /**
      * Add a tab at the last position in this tabbed pane.
      *
      * @param title     Title of the tab
      * @param icon      Icon that is displayed before the name
      * @param component The component in the content parent of the tab
      * @param detached  determines whether this tab is added at the tab or as a separate window
      */
     public void addTab(String title, ULCIcon icon, ULCComponent component, String tip, boolean detached) {
         addTab(title, icon, component, null, detached, FIRE_EVENT_ON_CLOSING_TAB);
     }
 
     /**
      * Add a tab at the last position in this tabbed pane.
      *
      * @param title                   Title of the tab
      * @param icon                    Icon that is displayed before the name
      * @param component               The component in the content parent of the tab
      * @param detached                determines whether this tab is added at the tab or as a separate window
      * @param closingDefaultBehaviour determines the behavior of the tab when closing it. Default is
      *                                ULCCloseableTabbedPane.FIRE_EVENT_ON_CLOSING_TAB
      */
     public void addTab(String title, ULCIcon icon, ULCComponent component, String tip, boolean detached, int closingDefaultBehaviour) {
 
         addTab(title, icon, component, tip);
         setDefaultCloseTabOperation(getTabCount() - 1, closingDefaultBehaviour);
 
         if (!detached)
             undock(getTabCount() - 1);
     }
 
     protected String typeString() {
 
         return "com.canoo.ulc.detachabletabbedpane.client.UIDetachableTabbedPane";
     }
 
 }
