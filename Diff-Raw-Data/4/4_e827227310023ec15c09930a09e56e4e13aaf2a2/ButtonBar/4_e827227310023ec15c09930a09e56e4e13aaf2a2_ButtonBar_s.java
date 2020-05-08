 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes,
  * Simon Mott
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
 package com.dmdirc.addons.ui_swing.framemanager.buttonbar;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.FrameContainerComparator;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.SwingWindowFactory;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.actions.CloseFrameContainerAction;
 import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.interfaces.FrameInfoListener;
 import com.dmdirc.interfaces.NotificationListener;
 import com.dmdirc.interfaces.SelectionListener;
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.addons.ui_swing.framemanager.FrameManager;
 import com.dmdirc.addons.ui_swing.framemanager.FramemanagerPosition;
 import com.dmdirc.interfaces.ConfigChangeListener;
 import com.dmdirc.ui.WindowManager;
 import com.dmdirc.ui.interfaces.UIController;
 import com.dmdirc.ui.interfaces.Window;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 
 import java.util.HashMap;
 import java.util.Map;
 import javax.swing.JComponent;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 
 import javax.swing.SwingUtilities;
 import net.miginfocom.layout.PlatformDefaults;
 import net.miginfocom.swing.MigLayout;
 
 /**
  * The button bar manager is a grid of buttons that presents a manager similar
  * to that used by mIRC.
  *
  * @author chris
  */
 public final class ButtonBar implements FrameManager, ActionListener,
         ComponentListener, Serializable, NotificationListener,
         SelectionListener, FrameInfoListener, MouseListener, ConfigChangeListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 3;
     /** A map of windows to the buttons we're using for them. */
     private final Map<Window, FrameToggleButton> buttons;
     /** The position of this frame manager. */
     private final FramemanagerPosition position;
     /** The parent for the manager. */
     private JComponent parent;
     /** The scrolling panel for our ButtonBar */
      private final JScrollPane scrollPane;
     /** The panel used for our buttons. */
     private final ButtonPanel buttonPanel;
     /** The currently selected frame. */
     private transient FrameContainer<?> selected;
     /** Selected window. */
     private Window activeWindow;
     /** The default number of buttons per row or column. */
     private int cells = 1;
     /** The default number of buttons to render per {cell,row}. */
     private int maxButtons = Integer.MAX_VALUE;
     /** The default width of buttons. */
     private int buttonWidth = 0;
     /** The default height of buttons. */
     private int buttonHeight = 25;
     /** Sort root windows prefs setting */
     private boolean sortRootWindows = IdentityManager.getGlobalConfig()
             .getOptionBool("ui", "sortrootwindows");
     /** Sort child windows prefs setting */
     private boolean sortChildWindows = IdentityManager.getGlobalConfig()
             .getOptionBool("ui", "sortchildwindows");
     /** UI Window Factory. */
     private SwingWindowFactory windowFactory;
     /** UI Controller. */
     private SwingController controller;
 
     /** Creates a new instance of DummyFrameManager. */
     public ButtonBar() {
         scrollPane = new JScrollPane();
         scrollPane.setBorder(null);
         scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants
                 .HORIZONTAL_SCROLLBAR_NEVER);
         scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants
                 .VERTICAL_SCROLLBAR_AS_NEEDED);
         scrollPane.setMinimumSize(new Dimension(0,buttonHeight
                 + ((int) PlatformDefaults.getUnitValueX("related").getValue()) * 2));
 
         buttons = Collections.synchronizedMap(new HashMap<Window, FrameToggleButton>());
         position = FramemanagerPosition.getPosition(
                 IdentityManager.getGlobalConfig().getOption("ui",
                 "framemanagerPosition"));
 
         IdentityManager.getGlobalConfig().addChangeListener("ui",
                         "sortrootwindows", this);
         IdentityManager.getGlobalConfig().addChangeListener("ui",
                         "sortchildwindows", this);
         if (position.isHorizontal()) {
             buttonPanel = new ButtonPanel(new MigLayout("ins rel, fill, flowx"),
                     this);
         } else {
             buttonPanel = new ButtonPanel(new MigLayout("ins rel, fill, flowy"),
                     this);
         }
         scrollPane.getViewport().addMouseWheelListener(buttonPanel);
         scrollPane.getViewport().add(buttonPanel);
     }
 
     /**
      * Retreives button height.
      *
      * @return Button height
      *
      * @since 0.6.4
      */
     public int getButtonHeight() {
         return buttonHeight;
     }
 
     /**
      * Returns the button object of the current selected window
      *
      * @return Button object for the current selected window
      */
     public FrameToggleButton getSelectedButton() {
         return getButton(selected);
     }
 
     /** {@inheritDoc} */
     @Override
     public void setParent(final JComponent parent) {
         SwingUtilities.invokeLater(new Runnable() {
 
             /** {inheritDoc} */
             @Override
             public void run() {
                 ButtonBar.this.parent = parent;
                 scrollPane.setSize(parent.getWidth(), parent.getHeight());
 
                 parent.setVisible(false);
                 parent.setLayout(new MigLayout("ins 0"));
                 parent.add(scrollPane);
                 parent.addComponentListener(ButtonBar.this);
                 ButtonBar.this.buttonWidth = position.isHorizontal()
                         ? 150 : (parent.getWidth() / cells);
                 initButtons(WindowManager.getRootWindows());
                 if (controller.getMainFrame().getActiveFrame() != null) {
                     selectionChanged(controller.getMainFrame()
                             .getActiveFrame().getContainer());
                 }
                 parent.setVisible(true);
             }
         });
     }
 
     /**
      * Initialises buttons for the currently available windows. This should only
      * be called once when this buttonbar is made active in the client.
      * See {@link #setParent}. This method essentially does nothing if the
      * client is started with the buttonbar enabled.
      *
      * @param windowCollection Collection of windows {@link FrameContainer}
      * @author Simon Mott
      * @since 0.6.4
      */
     private void initButtons(final Collection<FrameContainer<?>> windowCollection) {
         Window window;
         Window parentWindow;
         for (FrameContainer<?> frame : windowCollection) {
             window = windowFactory.getSwingWindow(frame);
             parentWindow = windowFactory.getSwingWindow(frame.getParent());
            windowAdded(parentWindow, window);
 
             if (!frame.getChildren().isEmpty()) {
                 final ArrayList<FrameContainer<?>> childList = new ArrayList
                         <FrameContainer<?>>(frame.getChildren());
                 initButtons(childList);
             }
         }
     }
 
     /**
      * Retreives the button object associated with {@link FrameContainer}.
      *
      * @param frame FrameContainer to find associated button for
      * @return {@link FrameToggleButton} object asociated with this FrameContainer.
      * Returns null if none exist
      */
     public FrameToggleButton getButton(final FrameContainer<?> frame) {
         final Window window = windowFactory.getSwingWindow(frame);
         if (buttons.containsKey(window)) {
             return buttons.get(window);
         }
         return null;
     }
 
     /** {@inheritDoc} */
     @Override
     public void setController(final UIController controller) {
         if (!(controller instanceof SwingController)) {
             throw new IllegalArgumentException("Controller must be an instance" +
                     " of SwingController");
         }
         this.windowFactory = ((SwingController) controller).getWindowFactory();
         this.controller = ((SwingController) controller);
     }
 
     /**
      * Adds buttons for the collection of windows that is passed to it.
      * This method also iterates through any children for each item in the
      * collection.
      *
      * This method has no boundaries as to how many generations it can iterate
      * through.
      *
      * @param windowCollection Collection of windows {@link FrameContainer}
      *
      * @author Simon Mott
      * @since 0.6.4
      */
     private void displayButtons(final Collection<FrameContainer<?>> windowCollection) {
         FrameToggleButton button;
         for (FrameContainer<?> window : windowCollection) {
             button = getButton(window);
             if (button != null) {
                 button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
                 buttonPanel.add(button);
                 if (!window.getChildren().isEmpty()) {
                     final ArrayList<FrameContainer<?>> childList = new ArrayList
                             <FrameContainer<?>>(window.getChildren());
                     if (sortChildWindows) {
                          Collections.sort(childList, new FrameContainerComparator());
                     }
                     displayButtons(childList);
                 }
             }
         }
     }
 
     /**
      * Removes all buttons from the bar and readds them.
      */
     private void relayout() {
         buttonPanel.setVisible(false);
         buttonPanel.removeAll();
 
         final ArrayList<FrameContainer<?>> windowList = new
                 ArrayList<FrameContainer<?>>(WindowManager.getRootWindows());
         if (sortRootWindows) {
             Collections.sort(windowList, new FrameContainerComparator());
         }
 
         displayButtons(windowList);
         buttonPanel.setVisible(true);
     }
 
     /**
      * Adds a button to the button array with the details from the specified
      * container.
      *
      * @param source The Container to get title/icon info from
      */
     private void addButton(final Window source) {
         final FrameToggleButton button = new FrameToggleButton(source.getContainer()
                 .toString(), IconManager.getIconManager().getIcon(
                 source.getContainer().getIcon()), source);
         button.addActionListener(this);
         button.addMouseListener(this);
         button.setHorizontalAlignment(SwingConstants.LEFT);
         button.setMinimumSize(new Dimension(0,buttonHeight));
         button.setMargin(new Insets(0, 0, 0, 0));
         buttons.put(source, button);
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean canPositionVertically() {
         return true;
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean canPositionHorizontally() {
         return true;
     }
 
     /** {@inheritDoc} */
     @Override
     public void windowAdded(final Window parent, final Window window) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 addButton(window);
                 relayout();
                 window.getContainer().addNotificationListener(ButtonBar.this);
                 window.getContainer().addSelectionListener(ButtonBar.this);
                 window.getContainer().addFrameInfoListener(ButtonBar.this);
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void windowDeleted(final Window parent, final Window window) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 window.getContainer().removeNotificationListener(ButtonBar.this);
                 window.getContainer().removeFrameInfoListener(ButtonBar.this);
                 window.getContainer().removeSelectionListener(ButtonBar.this);
                 if (buttons.containsKey(window)) {
                     buttonPanel.setVisible(false);
                     buttonPanel.remove(buttons.get(window));
                     buttons.remove(window);
                     buttonPanel.setVisible(true);
                 }
             }
         });
     }
 
     /**
      * Called when the user clicks on one of the buttons.
      *
      * @param e The action event associated with this action
      */
     @Override
     public void actionPerformed(final ActionEvent e) {
         final FrameToggleButton button = (FrameToggleButton) e.getSource();
         final FrameContainer<?> window =  button.getFrameContainer();
         final TextFrame frame = (TextFrame) button.getWindow();
         if (frame != null && window.equals(activeWindow.getContainer())) {
             button.setSelected(true);
         }
         window.activateFrame();
     }
 
     /**
      * Called when the parent component is resized.
      *
      * @param e A ComponentEvent corresponding to this event.
      */
     @Override
     public void componentResized(final ComponentEvent e) {
         buttonWidth = position.isHorizontal() ? 150 : (parent.getWidth() / cells);
         relayout();
     }
 
     /**
      * Called when the parent component is moved.
      *
      * @param e A ComponentEvent corresponding to this event.
      */
     @Override
     public void componentMoved(final ComponentEvent e) {
         // Do nothing
     }
 
     /**
      * Called when the parent component is made visible.
      *
      * @param e A ComponentEvent corresponding to this event.
      */
     @Override
     public void componentShown(final ComponentEvent e) {
         // Do nothing
     }
 
     /**
      * Called when the parent component is made invisible.
      *
      * @param e A ComponentEvent corresponding to this event.
      */
     @Override
     public void componentHidden(final ComponentEvent e) {
         // Do nothing
     }
 
     /** {@inheritDoc} */
     @Override
     public void notificationSet(final FrameContainer<?> window, final Color colour) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 final FrameToggleButton button = getButton(window);
                 if (button != null) {
                     button.setForeground(colour);
                 }
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void notificationCleared(final FrameContainer<?> window) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 notificationSet(window, window.getNotification());
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void selectionChanged(final FrameContainer<?> window) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 activeWindow = (TextFrame) (getButton(window)).getWindow();
                 FrameToggleButton button;
                 button = getButton(selected);
                 if (selected != null && button != null) {
                     button.setSelected(false);
                 }
 
                 selected = window;
                 button = getButton(window);
                 if (button != null) {
                     scrollPane.getViewport().scrollRectToVisible(button.getBounds());
                     button.setSelected(true);
                 }
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void iconChanged(final FrameContainer<?> window, final String icon) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 final FrameToggleButton button = getButton(window);
                 if (button != null) {
                     button.setIcon(IconManager.getIconManager().getIcon(icon));
                 }
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void nameChanged(final FrameContainer<?> window, final String name) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 final FrameToggleButton button = getButton(window);
                 if (button != null) {
                     button.setText(name);
                 }
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void titleChanged(final FrameContainer<?> window, final String title) {
         // Do nothing
     }
 
     /**
      * Creates and displays a Popup menu for the button that was clicked.
      *
      * @param e MouseEvent for this event
      */
     public void processMouseEvents(MouseEvent e) {
         if (e.isPopupTrigger()) {
             final FrameToggleButton button = (FrameToggleButton) e.getSource();
 
             TextFrame frame = (TextFrame) button.getWindow();
             if (frame == null) {
                 return;
             }
             final JPopupMenu popupMenu = frame.getPopupMenu(null, "");
             frame.addCustomPopupItems(popupMenu);
             popupMenu.add(new JMenuItem(new CloseFrameContainerAction(frame.
                         getContainer())));
             popupMenu.show(button, e.getX(), e.getY());
         }
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e MouseEvent for this event
      */
     @Override
     public void mouseClicked(MouseEvent e) {
         processMouseEvents(e);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e MouseEvent for this event
      */
     @Override
     public void mousePressed(MouseEvent e) {
         processMouseEvents(e);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e MouseEvent for this event
      */
     @Override
     public void mouseReleased(MouseEvent e) {
         processMouseEvents(e);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e MouseEvent for this event
      */
     @Override
     public void mouseEntered(MouseEvent e) {
         //Do nothing
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e MouseEvent for this event
      */
     @Override
     public void mouseExited(MouseEvent e) {
         //Do nothing
     }
 
     /** {@inheritDoc} */
     @Override
     public void configChanged(String domain, String key) {
         if ("sortrootwindows".equals(key)) {
             sortRootWindows = IdentityManager.getGlobalConfig()
                     .getOptionBool("ui", "sortrootwindows");
         } else if ("sortchildwindows".equals(key)) {
             sortChildWindows = IdentityManager.getGlobalConfig()
                     .getOptionBool("ui", "sortrootwindows");
         }
         relayout();
     }
 }
