 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.framemanager.windowmenu;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.FrameContainerComparator;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.SwingWindowListener;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.interfaces.SelectionListener;
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.ui.interfaces.Window;
 import com.dmdirc.util.ReturnableThread;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicBoolean;
 import javax.swing.AbstractButton;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JSeparator;
 
 /**
  * Manages the window menu window list.
  */
 public final class WindowMenuFrameManager extends JMenu implements
         SwingWindowListener, ActionListener, SelectionListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
     /** Comparator. */
     private final FrameContainerComparator comparator =
             new FrameContainerComparator();
     /** Non frame container menu count. */
     private final int itemCount;
     /** Menu items closing the active window. */
     private final JMenuItem closeMenuItem;
     /** Seperator. */
     private final JSeparator separator;
     /** Enabled menu items? */
     private final AtomicBoolean enabledMenuItems = new AtomicBoolean(false);
     /** Swing controller. */
     private final SwingController controller;
     /** Window -> menu map. */
     private final Map<FrameContainer, FrameContainerMenu> menus;
     private final Map<FrameContainer, FrameContainerMenuItem> items;
     private final Map<FrameContainer, FrameContainerMenuItem> menuItems;
 
     /**
      * Creates a new instance of WindowMenuFrameManager.
      *
      * @param controller Swing controller
      */
     public WindowMenuFrameManager(final SwingController controller) {
         super();
         this.controller = controller;
 
         menus = Collections.synchronizedMap(
                 new HashMap<FrameContainer, FrameContainerMenu>());
         items = Collections.synchronizedMap(
                 new HashMap<FrameContainer, FrameContainerMenuItem>());
         menuItems = Collections.synchronizedMap(
                 new HashMap<FrameContainer, FrameContainerMenuItem>());
 
         setText("Window");
         setMnemonic('w');
         controller.getWindowFactory().addWindowListener(this);
 
         closeMenuItem = new JMenuItem(IconManager.getIconManager().getIcon(
                 "close"));
         closeMenuItem.setMnemonic('c');
         closeMenuItem.setText("Close");
         closeMenuItem.setActionCommand("Close");
         closeMenuItem.addActionListener(this);
         add(closeMenuItem);
 
         separator = new JPopupMenu.Separator();
         add(separator);
 
         itemCount = getMenuComponentCount();
 
         new WindowMenuScroller(this, controller.getDomain(), itemCount);
     }
 
     /**
      * Checks the number of components in the menu and enables menus items
      * appropriately.
      */
     private void checkMenuItems() {
         enabledMenuItems.set((getMenuComponentCount() > itemCount));
         separator.setVisible(enabledMenuItems.get());
         closeMenuItem.setEnabled(enabledMenuItems.get());
     }
 
     /** {@inheritDoc} */
     @Override
     public void windowAdded(final Window parent, final Window window) {
         if (parent == null) {
             final FrameContainerMenuItem item = UIUtilities.invokeAndWait(
                     new ReturnableThread<FrameContainerMenuItem>() {
 
                         /** {@inheritDoc} */
                         @Override
                         public void run() {
                             setObject(new FrameContainerMenuItem(window.
                                     getContainer(), WindowMenuFrameManager.this));
                         }
                     });
             items.put(window.getContainer(), item);
             final int index = getIndex(window.getContainer(), this);
             window.getContainer().addSelectionListener(this);
             UIUtilities.invokeLater(new Runnable() {
 
                 /** {@inheritDoc} */
                 @Override
                 public void run() {
                     add(item, index);
                 }
             });
         } else {
             final FrameContainerMenuItem item = UIUtilities.invokeAndWait(
                     new ReturnableThread<FrameContainerMenuItem>() {
 
                         /** {@inheritDoc} */
                         @Override
                         public void run() {
                             setObject(new FrameContainerMenuItem(window.
                                     getContainer(),
                                     WindowMenuFrameManager.this));
                         }
                     });
             final JMenu parentMenu;
             if (!menus.containsKey(parent.getContainer())) {
                 final FrameContainerMenu replacement = UIUtilities.invokeAndWait(
                         new ReturnableThread<FrameContainerMenu>() {
 
                             /** {@inheritDoc} */
                             @Override
                             public void run() {
                                 setObject(new FrameContainerMenu(parent.
                                         getContainer(), controller));
                             }
                         });
                 replaceItemWithMenu(getParentMenu(parent.getContainer()),
                         items.get(parent.getContainer()), replacement);
                 parentMenu = replacement;
             } else {
                 parentMenu = menus.get(parent.getContainer());
             }
             items.put(window.getContainer(), item);
             window.getContainer().addSelectionListener(this);
             UIUtilities.invokeLater(new Runnable() {
 
                 /** {@inheritDoc} */
                 @Override
                 public void run() {
                     parentMenu.add(item, getIndex(window.getContainer(),
                             parentMenu));
                 }
             });
         }
        checkMenuItems();
     }
 
     /** {@inheritDoc} */
     @Override
     public void windowDeleted(final Window parent, final Window window) {
         if (parent == null) {
             final AbstractButton item;
             if (items.containsKey(window.getContainer())) {
                 item = items.get(window.getContainer());
                 items.remove(window.getContainer());
             } else if (menus.containsKey(window.getContainer())) {
                 item = menus.get(window.getContainer());
                 menus.remove(window.getContainer());
             } else {
                 item = null;
             }
             UIUtilities.invokeLater(new Runnable() {
 
                 /** {@inheritDoc} */
                 @Override
                 public void run() {
                     remove(item);
                 }
             });
             window.getContainer().removeSelectionListener(this);
         } else {
             if (items.containsKey(window.getContainer())) {
                 final JMenu menu = getParentMenu(window.getContainer());
                 final FrameContainerMenuItem item = items.get(window.
                         getContainer());
                 items.remove(window.getContainer());
                 UIUtilities.invokeAndWait(new Runnable() {
 
                     /** {@inheritDoc} */
                     @Override
                     public void run() {
                         menu.remove(item);
                         if (menu.getMenuComponentCount() == 1) {
                             replaceMenuWithItem(getParentMenu(parent.
                                     getContainer()),
                                     menus.get(parent.getContainer()),
                                     new FrameContainerMenuItem(parent.
                                     getContainer(),
                                     WindowMenuFrameManager.this));
                         }
                     }
                 });
             } else if (menus.containsKey(window.getContainer())) {
                 menus.get(parent.getContainer()).remove(menus.get(window.
                         getContainer()));
                 menus.remove(window.getContainer());
             }
             window.getContainer().removeSelectionListener(this);
         }
        checkMenuItems();
     }
 
     private JMenu getParentMenu(final FrameContainer window) {
         FrameContainer parent = window.getParent();
 
         if (parent == null) {
             return this;
         } else {
             return menus.get(parent);
         }
     }
 
     private void replaceItemWithMenu(final JMenu parentMenu,
             final FrameContainerMenuItem item, final FrameContainerMenu menu) {
         UIUtilities.invokeAndWait(new Runnable() {
 
             @Override
             public void run() {
                 parentMenu.remove(item);
                 parentMenu.add(menu, getIndex(menu.getFrame(), parentMenu));
                 menu.add(item, getIndex(item.getFrame(), menu));
             }
         });
         items.remove(item.getFrame());
         menus.put(menu.getFrame(), menu);
         menuItems.put(item.getFrame(), item);
     }
 
     private void replaceMenuWithItem(final JMenu parentMenu,
             final FrameContainerMenu menu, final FrameContainerMenuItem item) {
         parentMenu.remove(menu);
         parentMenu.add(item, getIndex(item.getFrame(), parentMenu));
         menus.remove(menu.getFrame());
         items.put(item.getFrame(), item);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Action event
      */
     @Override
     public void actionPerformed(final ActionEvent e) {
         if (enabledMenuItems.get()) {
             if (e.getActionCommand().equals("Close")) {
                 controller.getMainFrame().getActiveFrame().close();
             }
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void selectionChanged(final FrameContainer window) {
         final Collection<SelectionListener> values =
                 new ArrayList<SelectionListener>();
         synchronized (menus) {
             synchronized (items) {
                 synchronized (menuItems) {
                     values.addAll(menus.values());
                     values.addAll(items.values());
                     values.addAll(menuItems.values());
                 }
             }
         }
         UIUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 for (SelectionListener menuItem : values) {
                     menuItem.selectionChanged(window);
                 }
             }
         });
     }
 
     /**
      * Tells a parent its child is selected.
      *
      * @param window parent to inform
      */
     protected void parentSelection(final FrameContainer window) {
         final FrameContainerMenu menuItem = menus.get(window);
         if (menuItem != null) {
             menuItem.childSelected();
         }
     }
 
     /**
      * Compares the new child with the existing children or parent to decide
      * where it needs to be inserted.
      *
      * @param newChild new node to be inserted.
      * @param menu Menu for the node to be inserted in
      *
      * @return index where new node is to be inserted.
      */
     private int getIndex(final FrameContainer newChild, final JMenu menu) {
         final int count = menu == this ? itemCount : 0;
         final int menuItemCount = UIUtilities.invokeAndWait(
                 new ReturnableThread<Integer>() {
 
                     /** {@inheritDoc} */
                     @Override
                     public void run() {
                         setObject(menu.getMenuComponentCount());
                     }
                 });
         for (int i = count; i < menuItemCount; i++) {
             final int index = i;
             final Component component = UIUtilities.invokeAndWait(
                     new ReturnableThread<Component>() {
 
                         @Override
                         public void run() {
                             setObject(menu.getMenuComponent(index));
                         }
                     });
             if (!(component instanceof FrameContainerMenuInterface)) {
                 continue;
             }
             final FrameContainer child = ((FrameContainerMenuInterface) component).
                     getFrame();
             if (sortBefore(newChild, child)) {
                 return i;
             } else if (!sortAfter(newChild, child) && IdentityManager.
                     getGlobalConfig().getOptionBool("treeview",
                     "sortwindows") && newChild.toString().compareToIgnoreCase(
                     child.toString()) < 0) {
                 return i;
             }
         }
 
         return UIUtilities.invokeAndWait(new ReturnableThread<Integer>() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 setObject(menu.getMenuComponentCount());
             }
         });
     }
 
     /**
      * Compares the types of the specified nodes' objects to see if the new
      * node should be sorted before the other.
      *
      * @param newChild The new child to be tested
      * @param child The existing child that it's being tested against
      *
      * @return True iff newChild should be sorted before child
      */
     private boolean sortBefore(final FrameContainer newChild,
             final FrameContainer child) {
 
         return comparator.compare(newChild, child) <= -1;
     }
 
     /**
      * Compares the types of the specified nodes' objects to see if the new
      * node should be sorted after the other.
      *
      * @param newChild The new child to be tested
      * @param child The existing child that it's being tested against
      *
      * @return True iff newChild should be sorted before child
      */
     private boolean sortAfter(final FrameContainer newChild,
             final FrameContainer child) {
 
         return comparator.compare(newChild, child) >= 1;
     }
 }
