 /*
  * Copyright (c) 2006-2014 DMDirc Developers
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
 
 package com.dmdirc.addons.ui_swing.components.statusbar;
 
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.ui.StatusMessage;
 import com.dmdirc.util.collections.RollingList;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.Window;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Previous status bar messages popup.
  */
 class MessagePopup extends StatusbarTogglePanel<JLabel> {
 
     /** A version number for this class. */
     private static final long serialVersionUID = 2;
     /** Parent window. */
     private final Window parentWindow;
     /** List of historical messages. */
     private final RollingList<StatusMessage> messages;
     /** Parent panel. */
     private final JPanel parent;
     /** Icon manager to retrieve icons from. */
     private final IconManager iconManager;
 
     /**
      * Creates a new message history popup.
      *
      * @param parent       Parent to size against
      * @param parentWindow Parent window
      * @param iconManager  Icon manager to retrieve icons from
      */
     public MessagePopup(final JPanel parent, final Window parentWindow,
             final IconManager iconManager) {
         super(new JLabel("^"),
                 new SidelessEtchedBorder(SidelessEtchedBorder.Side.LEFT),
                 new SidelessEtchedBorder(SidelessEtchedBorder.Side.TOP));
         this.parentWindow = parentWindow;
         this.parent = parent;
         this.iconManager = iconManager;
         messages = new RollingList<>(5);
     }
 
     /* {@inheritDoc} */
     @Override
     protected StatusbarPopupWindow getWindow() {
         return new MessageHistoryPanel(this);
     }
 
     /* {@inheritDoc} */
     @Override
     public void mouseEntered(final MouseEvent e) {
         super.mouseEntered(e);
         if (!isDialogOpen()) {
             setBorder(nonSelectedBorder);
             setBackground(UIManager.getColor("ToolTip.background"));
             setForeground(UIManager.getColor("ToolTip.foreground"));
         }
     }
 
     /* {@inheritDoc} */
     @Override
     public void mouseExited(final MouseEvent e) {
         super.mouseExited(e);
         if (!isDialogOpen()) {
             setBorder(new SidelessEtchedBorder(SidelessEtchedBorder.Side.LEFT));
             setBackground(null);
             setForeground(null);
         }
     }
 
     /**
      * Adds a message to this history window.
      *
      * @param message to add
      */
     public void addMessage(final StatusMessage message) {
        synchronized (messages) {
             messages.add(message);
         }
     }
 
     /* {@inheritDoc} */
     @Override
     protected Color getPopupBackground() {
         return parent.getBackground();
     }
 
     /* {@inheritDoc} */
     @Override
     protected Color getPopupForeground() {
         return super.getForeground();
     }
 
     /** Message history status bar popup window. */
     private class MessageHistoryPanel extends StatusbarPopupWindow {
 
         /** A version number for this class. */
         private static final long serialVersionUID = 2;
 
         /**
          * Creates a new message history window.
          *
          * @param window Parent window
          */
         public MessageHistoryPanel(final JPanel parent) {
             super(parent, parentWindow);
         }
 
         /* {@inheritDoc} */
         @Override
         protected void initPanel(final JPanel panel) {
             panel.setLayout(new MigLayout("ins 0 0 0 rel, fill, wmin "
                     + (parent.getWidth() - 5) + ", wmax "
                     + (parent.getWidth() - 5)));
             panel.setBackground(parent.getBackground());
             panel.setForeground(parent.getForeground());
             panel.setBorder(new GappedEtchedBorder(this));
         }
 
         /* {@inheritDoc} */
         @Override
         protected void initContent(final JPanel panel) {
             if (messages.isEmpty()) {
                 panel.add(new JLabel("No previous messages."), "grow, push");
                 return;
             }
 
             for (final StatusMessage message : messages.getList()) {
                 panel.add(new JLabel(message.getMessage(), message.getIconType()
                         == null ? null : iconManager.getIcon(message.getIconType()),
                         SwingConstants.LEFT), "grow, push, wrap");
             }
         }
 
         /* {@inheritDoc} */
         @Override
         protected Point getPopupLocation() {
             final Point point = parent.getLocationOnScreen();
             point.y = point.y - getHeight() + 2;
             return point;
         }
 
     }
 
 }
