 /*
  *  PathFind -- a Diamond system for pathology
  *
  *  Copyright (c) 2008-2009 Carnegie Mellon University
  *  All rights reserved.
  *
  *  PathFind is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, version 2.
  *
  *  PathFind is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with PathFind. If not, see <http://www.gnu.org/licenses/>.
  *
  *  Linking PathFind statically or dynamically with other modules is
  *  making a combined work based on PathFind. Thus, the terms and
  *  conditions of the GNU General Public License cover the whole
  *  combination.
  *
  *  In addition, as a special exception, the copyright holders of
  *  PathFind give you permission to combine PathFind with free software
  *  programs or libraries that are released under the GNU LGPL or the
  *  Eclipse Public License 1.0. You may copy and distribute such a system
  *  following the terms of the GNU GPL for PathFind and the licenses of
  *  the other code concerned, provided that you include the source code of
  *  that other code when and as the GNU GPL requires distribution of source
  *  code.
  *
  *  Note that people who make modified versions of PathFind are not
  *  obligated to grant this special exception for their modified versions;
  *  it is their choice whether to do so. The GNU General Public License
  *  gives permission to release a modified version without this exception;
  *  this exception also makes it possible to release a modified version
  *  which carries forward this exception.
  */
 
 package edu.cmu.cs.diamond.pathfind;
 
 import java.awt.Component;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 
 import javax.swing.*;
 
 import edu.cmu.cs.openslide.OpenSlide;
import edu.cmu.cs.openslide.gui.Annotation;
 import edu.cmu.cs.openslide.gui.OpenSlideView;
 
 public class SavedSelectionCellRenderer extends DefaultListCellRenderer {
 
     final private static int THUMBNAIL_SIZE = 200;
 
     final private OpenSlide ws;
 
     public SavedSelectionCellRenderer(OpenSlideView wv) {
         ws = wv.getOpenSlide();
     }
 
     @Override
     public Component getListCellRendererComponent(JList list, Object value,
             int index, boolean isSelected, boolean cellHasFocus) {
         DefaultListCellRenderer c = (DefaultListCellRenderer) super
                 .getListCellRendererComponent(list, value, index, isSelected,
                         cellHasFocus);
 
        Annotation ann = (Annotation) value;
        BufferedImage thumb = drawThumbnail(ws, ann.getShape(), THUMBNAIL_SIZE);
 
        c.setText(ann.getText());
         c.setIcon(new ImageIcon(thumb));
 
         c.setHorizontalAlignment(SwingConstants.CENTER);
         c.setVerticalAlignment(SwingConstants.CENTER);
 
         c.setHorizontalTextPosition(SwingConstants.CENTER);
         c.setVerticalTextPosition(SwingConstants.BOTTOM);
 
         c.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
 
         return c;
     }
 
     private static BufferedImage drawThumbnail(OpenSlide slide,
             Shape selection, int maxSize) {
         Rectangle2D bb = selection.getBounds2D();
 
         double downsample = Math.max(bb.getWidth(), bb.getHeight()) / maxSize;
 
         if (downsample < 1.0) {
             downsample = 1.0;
         }
 
         BufferedImage thumb = slide.createThumbnailImage((int) bb.getX(),
                 (int) bb.getY(), (int) bb.getWidth(), (int) bb.getHeight(),
                 maxSize);
         Graphics2D g = thumb.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
         OpenSlideView.paintSelection(g, selection,
                 (int) (-bb.getX() / downsample),
                 (int) (-bb.getY() / downsample), downsample);
         g.dispose();
 
         return thumb;
     }
 }
