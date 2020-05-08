 /*
  * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 package com.dmdirc.addons.ui_swing.framemanager.tree;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.addons.ui_swing.components.ImageButton;
 import com.dmdirc.interfaces.FrameInfoListener;
 import com.dmdirc.interfaces.NotificationListener;
 import com.dmdirc.interfaces.SelectionListener;
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.ui.messages.Styliser;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.JTextPane;
 import javax.swing.UIManager;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.StyledDocument;
 
 import net.miginfocom.layout.PlatformDefaults;
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Node label.
  */
 public class NodeLabel extends JPanel implements SelectionListener,
         NotificationListener, FrameInfoListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
     /** The window this node represents in the tree. */
     private final FrameContainer<?> window;
     /** Colour used to show if the mouse is over this node. */
     private boolean rollover;
     /** Colour used to show if this node has an active notification. */
     private Color notificationColour;
     /** Are we the selected window? */
     private boolean selected;
     /** Node icon. */
     private final ImageButton icon = new ImageButton("", IconManager
             .getIconManager().getIcon("icon"));
     /** Text label. */
     private final JTextPane text = new JTextPane(new DefaultStyledDocument());
     /** Current styled text. */
     private String currentText = "";
 
     /**
      * Instantiates a new node label.
      *
      * @param window Window for this node
      */
     public NodeLabel(final FrameContainer<?> window) {
         super();
 
         this.window = window;
 
         init();
     }
 
     /**
      * Initialises the label.
      */
     private void init() {
         if (window == null) {
             return;
         }
 
         icon.setIcon(IconManager.getIconManager().getIcon(window.getIcon()));
         text.setText(window.toString());
         text.setBorder(null);
 
         setLayout(new MigLayout("ins 0"));
         add(icon, "left");
         add(text, "left, grow, pushx");
 
 
         icon.setToolTipText(null);
         text.setToolTipText(null);
         setBorder(BorderFactory.createEmptyBorder(1, 0, 2, 0));
 
         setPreferredSize(new Dimension(100000, getFont().getSize()
                 + (int) PlatformDefaults.getUnitValueX("related").
                 getValue()));
         notificationColour = null;
         selected = false;
     }
 
     /** {@inheritDoc} */
     @Override
     public void selectionChanged(final FrameContainer<?> window) {
         if (equals(window)) {
             selected = true;
         } else {
             selected = false;
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void notificationSet(final FrameContainer<?> window,
             final Color colour) {
         if (equals(window)) {
             notificationColour = colour;
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void notificationCleared(final FrameContainer<?> window) {
         if (equals(window)) {
             notificationColour = null;
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void iconChanged(final FrameContainer<?> window, final String icon) {
         if (equals(window)) {
             this.icon.setIcon(IconManager.getIconManager().getIcon(icon));
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void nameChanged(final FrameContainer<?> window, final String name) {
        if (equals(window)) {
            text.setText(name);
        }
     }
 
     /** {@inheritDoc} */
     @Override
     public void titleChanged(final FrameContainer<?> window,
             final String title) {
         // Do nothing
     }
 
     /**
      * Sets the rollover state for the node.
      *
      * @param rollover rollover state
      */
     public void setRollover(final boolean rollover) {
         this.rollover = rollover;
     }
 
     /**
      * Is this node a rollover node?
      *
      * @return true iff this node is a rollover node
      */
     public boolean isRollover() {
         return rollover;
     }
 
     /**
      * Is this node a selected node?
      *
      * @return true iff this node is a selected node
      */
     public boolean isSelected() {
         return selected;
     }
 
     /**
      * Returns the notification colour for this node.
      *
      * @return notification colour or null if non set
      */
     public Color getNotificationColour() {
         return notificationColour;
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean equals(final Object obj) {
         if (window == null) {
             return false;
         }
 
         return window.equals(obj);
     }
 
     /** {@inheritDoc} */
     @Override
     public int hashCode() {
         if (window == null) {
             return super.hashCode();
         }
 
         return window.hashCode();
     }
 
     /** {@inheritDoc} */
     @Override
     public Font getFont() {
         return UIManager.getFont("TextPane.font");
     }
 
     /**
      * Sets the foreground colour for this label.
      *
      * @param colour New foreground colour
      */
     @Override
     public void setForeground(final Color colour) {
         if (text != null) {
             text.setForeground(colour);
         }
     }
 
     /**
      * Sets the background colour for this label.
      *
      * @param colour New background colour
      */
     @Override
     public void setBackground(final Color colour) {
         if (text != null) {
             text.setBackground(colour);
         }
     }
 
     /**
      * Sets the font for this label.
      *
      * @param font New font
      */
     @Override
     public void setFont(final Font font) {
         if (text != null) {
             text.setFont(font);
         }
     }
 
     /**
      * Sets the opacity of this label.
      *
      * @param opacity Desired opacity
      */
     @Override
     public void setOpaque(final boolean opacity) {
         if (text != null) {
             text.setOpaque(opacity);
         }
     }
 
     /**
      * Sets the text and style in this label.
      *
      * @param styliser Styliser to use to style text
      * @param styledText Styled text string to use
      */
     public void setStyledText(final Styliser styliser,
             final String styledText) {
         if (currentText.equals(styledText)) {
             return;
         }
         text.setText("");
         currentText = styledText;
         styliser.addStyledString((StyledDocument) text.getDocument(),
                 new String[] {styledText, });
     }
 
     /**
      * Sets the styles for the text in this label.
      *
      * @param styliser Styliser to use
      * @param newText Style to set
      */
     public void setTextStyle(final Styliser styliser, final String newText) {
         if (currentText.equals(newText + window.toString())) {
             return;
         }
         text.setText("");
         currentText = newText + window.toString();
         styliser.addStyledString((StyledDocument) text.getDocument(),
                 new String[] {newText, window.toString(), });
     }
 
 }
