 /*
  * Hex - a hex viewer and annotator
  * Copyright (C) 2009  Trejkaz, Hex Project
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.trypticon.hex.renderer;
 
 import java.awt.Color;
 import java.awt.Component;
 import javax.swing.JLabel;
 
 import org.trypticon.hex.HexUtils;
 import org.trypticon.hex.HexViewer;
 import org.trypticon.hex.anno.Annotation;
 import org.trypticon.hex.anno.AnnotationCollection;
 
 /**
  * Default cell renderer implementation, using a Swing label as the component.
  */
 public class DefaultCellRenderer extends JLabel implements CellRenderer {
     private static final Color transparent = new Color(0, 0, 0, 0);
 
     // TODO: These should be based on what kind of annotation is being coloured,
     //       and there should be a policy to choose which to use.
     private static final Color annotationBorder = Color.RED;
     private static final Color annotationBackground = new Color(255, 240, 240);
 
     public DefaultCellRenderer() {
         setOpaque(true);
     }
 
     public Component getRendererComponent(HexViewer viewer, boolean selected, boolean onCursorRow, boolean atCursor,
                                           long position, int valueDisplayMode) {
 
         // XXX: I should probably split this logic into different renderers for each column.
         //int charYOffset = (rowHeight - metrics.getAscent()) / 2;
 
         setFont(viewer.getFont());
         setBorder(null);
 
         setHorizontalAlignment(valueDisplayMode == ROW_OFFSET ? RIGHT : CENTER);
 
         Color background = transparent;
         Color foreground;
 
         if (valueDisplayMode == ROW_OFFSET) {
             foreground = viewer.getOffsetForeground();
         } else {
             foreground = viewer.getForeground();
 
             AnnotationCollection annotations = viewer.getAnnotations();
             Annotation annotation = annotations.getAnnotationAt(position);
             if (annotation != null) {
                 long annoStart = annotation.getPosition();
                 long annoEnd = annoStart + annotation.getLength() - 1;
 
                 // TODO: This 16 is technically a magic number, we should pass in the row length.
                 boolean top = position < annoStart + 16;
                boolean right = position == annoEnd;
                 boolean bottom = position > annoEnd - 16;
                boolean left = position == annoStart;
 
                 setBorder(new JointedLineBorder(annotationBorder, top, right, bottom, left));
 
                 background = annotationBackground;
             }
 
             if (selected && viewer.getSelectionBackground() != null) {
                 background = viewer.getSelectionBackground();
             }
             if (selected && viewer.getSelectionForeground() != null) {
                 foreground = viewer.getSelectionForeground();
             }
 
             if (atCursor && viewer.getCursorBackground() != null) {
                 background = viewer.getCursorBackground();
             }
             if (atCursor && viewer.getCursorForeground() != null) {
                 foreground = viewer.getCursorForeground();
             }
         }
 
         setBackground(background);
         setForeground(foreground);
 
         // XXX: This is redundant if rendering the address column.
         byte b = viewer.getBinary().read(position);
         String str;
 
         switch (valueDisplayMode) {
             case ROW_OFFSET:
                 str = String.format("%08x", position);
                 break;
             case HEX:
                 str = HexUtils.toHex(b);
                 break;
             case ASCII:
                 str = HexUtils.toAscii(b);
                 break;
             default:
                 throw new IllegalStateException("Unimplemented display mode: " + valueDisplayMode);
         }
 
         setText(str);
 
         return this;
     }
 }
