 /*
  * Copyright 2011 Eike Kettner
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.eknet.swing.uploadfield;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JList;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.border.Border;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.jetbrains.annotations.Nullable;
 
 /**
  * Displays a list of images, which can be
  * <ul>
  *   <li>{@link Icon}s,</li>
  *   <li>{@link URL}s to images or</li>
  *   <li>{@link DefaultUploadValue}s</li>
  * </ul>
  * 
  * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
  * @since 01.10.11 20:44
  */
 public class IconsList extends JList {
   private static final Logger log = LoggerFactory.getLogger(IconsList.class);
 
   private Dimension previewSize;
   
   public IconsList() {
 
     setPreviewSize(new Dimension(25, 25));
     setModel(new DefaultListModel());
     setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     setLayoutOrientation(JList.HORIZONTAL_WRAP);
     setVisibleRowCount(-1);
     setCellRenderer(new DefaultListCellRenderer() {
       @Override
       public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
 
         setHorizontalAlignment(SwingConstants.CENTER);
         setVerticalAlignment(SwingConstants.CENTER);
         setPreferredSize(getPreviewSize());
 
         super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
         setText(null);
         if (value instanceof URL) {
           setToolTipText(Utils.lastUrlPart((URL) value));
           try {
             BufferedImage img = ImageIO.read((URL) value);
            setIcon(new ImageIcon(Scales.scaleIfNecessary(img, previewSize.width, previewSize.height)));
           } catch (IOException e) {
             log.error("Cannot scale image: " + value, e);
           }
         } else if (value instanceof Icon) {
           setIcon((Icon) value);
         } else if (value instanceof UploadValue) {
           UploadValue iv = (UploadValue) value;
           setIcon(iv.getIcon());
           setToolTipText(iv.getName());
         } else {
           setIcon(null);
         }
 
         Border border = getBorder();
         setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), border));
         return this;
       }
     });
   }
 
   
   public void setIconElements(@Nullable Iterable<?> icons) {
     DefaultListModel model = new DefaultListModel();
     if (icons != null) {
       for (Object icon : icons) {
         model.addElement(icon);
       }
     }
     setModel(model);
   }
 
   public Dimension getPreviewSize() {
     return previewSize;
   }
 
   public void setPreviewSize(Dimension previewSize) {
     this.previewSize = previewSize;
     Dimension np = new Dimension(previewSize);
     np.width += 5;
     np.height += 5;
     setFixedCellHeight(np.height);
     setFixedCellWidth(np.width);
   }
 
   public void addElement(Object urlOrIconOrImageValue) {
     if (urlOrIconOrImageValue != null) {
       ((DefaultListModel) getModel()).addElement(urlOrIconOrImageValue);
     }
   }
 
   public void removeElement(Object urlOrIconOrImageValue) {
     if (urlOrIconOrImageValue != null) {
       ((DefaultListModel) getModel()).removeElement(urlOrIconOrImageValue);
     }
   }
 
 }
