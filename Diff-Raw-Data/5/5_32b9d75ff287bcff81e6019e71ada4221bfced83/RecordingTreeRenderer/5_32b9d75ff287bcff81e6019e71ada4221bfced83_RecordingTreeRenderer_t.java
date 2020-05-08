 /*
  * Copyright (c) Henrik Niehaus & Lazy Bones development team
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of the project (Lazy Bones) nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package lazybones.gui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 
 import javax.swing.Icon;
 import javax.swing.JLabel;
 import javax.swing.JTree;
 import javax.swing.JViewport;
 import javax.swing.UIManager;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.TreePath;
 
 import lazybones.LazyBones;
 
 import org.hampelratte.svdrp.responses.highlevel.Recording;
 import org.hampelratte.svdrp.responses.highlevel.TreeNode;
 
 public class RecordingTreeRenderer extends DefaultTreeCellRenderer {
 
     private static final long serialVersionUID = 1L;
 
     int lci = UIManager.getDefaults().getInt("Tree.leftChildIndent");
     int rci = UIManager.getDefaults().getInt("Tree.rightChildIndent");
     int rowHeight = UIManager.getDefaults().getInt("Tree.rowHeight");
 
     private final Icon iconNew;
     private final Icon iconCut;
     private final Icon iconBoth;
 
     protected boolean hasFocus = false;
 
     public RecordingTreeRenderer() {
         iconNew = LazyBones.getInstance().getIcon("lazybones/new.png");
         iconCut = LazyBones.getInstance().getIcon("lazybones/edit-cut.png");
         List<Icon> combined = Arrays.asList(new Icon[] { iconNew, iconCut });
         iconBoth = new CombinedIcon(combined, 2);
     }
 
     @Override
     public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
         this.hasFocus = hasFocus;
         String title = value.toString();
         if (value instanceof TreeNode) {
             title = ((TreeNode) value).getDisplayTitle();
             if (value instanceof Recording) {
                 Recording recording = (Recording) value;
                 DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault());
                 title = "<html>" + df.format(recording.getStartTime().getTime()) + " - <b>" + title + "</b>";
                 if (recording.getShortText() != null && recording.getShortText().trim().length() > 0) {
                     title += " - " + recording.getShortText();
                 }
                 title += "</html>";
             }
         }
         Component renderer = super.getTreeCellRendererComponent(tree, title, sel, expanded, leaf, row, hasFocus);
 
         JLabel _renderer = (JLabel) renderer;
         if (value instanceof Recording) {
             Recording recording = (Recording) value;
             _renderer.setHorizontalTextPosition(JLabel.LEADING);
             if (recording.isNew()) {
                 if (recording.isCut()) {
                     _renderer.setIcon(iconBoth);
                 } else {
                     _renderer.setIcon(iconNew);
                 }
             } else if (recording.isCut()) {
                 _renderer.setIcon(iconCut);
             } else {
                 _renderer.setIcon(null);
             }
         } else {
             _renderer.setHorizontalTextPosition(JLabel.TRAILING);
         }
 
         if (tree.isShowing()) {
             TreePath tp = tree.getPathForRow(row);
             if (tp != null) {
                 int depth = tp.getPathCount();
                 JViewport viewPort = (JViewport) tree.getParent();
                 Dimension size = new Dimension(viewPort.getWidth() - 10 - (depth - 1) * (rci + lci), renderer.getY() * -1);
                 renderer.setPreferredSize(size);
             }
         }
 
         return renderer;
     }
 
     /*
      * Overriden, since the DefaultTreeCellrenderer paints the selection box wrong, if the icons are painted behind the text (JLabel horizontal text position
      * leading)
      */
     @Override
     public void paint(Graphics g) {
         super.hasFocus = false;
 
         if (selected) {
             g.setColor(getBackgroundSelectionColor());
            g.fillRect(0, 0, getWidth(), getHeight());
         }
 
         super.paint(g);
 
         if (this.hasFocus) {
             paintFocus(g, 0, 0, getWidth(), getHeight());
         }
     }
 
     private void paintFocus(Graphics g, int x, int y, int w, int h) {
         Color bsColor = getBorderSelectionColor();
 
         if (bsColor != null && selected) {
             g.setColor(bsColor);
             g.drawRect(x, y, w - 1, h - 1);
         }
     }
 
     private class CombinedIcon implements Icon {
         private List<Icon> icons = new ArrayList<Icon>();
         private int hgap = 2;
 
         public CombinedIcon(List<Icon> icons, int hgap) {
             this.icons = icons;
             this.hgap = hgap;
         }
 
         @Override
         public void paintIcon(Component c, Graphics g, int x, int y) {
             for (int i = 0; i < icons.size(); i++) {
                 Icon icon = icons.get(i);
                 icon.paintIcon(c, g, x + (i * 16) + (i * hgap), y);
             }
         }
 
         @Override
         public int getIconWidth() {
             // int width = 0;
             // for (Icon icon : icons) {
             // width += icon.getIconWidth();
             // }
             // width += (icons.size() - 1) * hgap;
             return icons.size() * 16 + (icons.size() - 1) * hgap;
         }
 
         @Override
         public int getIconHeight() {
             return 16;
         }
     }
 }
