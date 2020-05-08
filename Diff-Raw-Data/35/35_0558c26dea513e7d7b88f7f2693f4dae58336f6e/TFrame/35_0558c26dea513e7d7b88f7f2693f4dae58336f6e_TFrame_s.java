 /*
  * Copyright 2011 Christian Thiemann <christian@spato.net>
  * Developed at Northwestern University <http://rocs.northwestern.edu>
  *
  * This file is part of TransparentGUI, a GUI library for Processing.
  *
  * TransparentGUI is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TransparentGUI is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TransparentGUI.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.cthiemann.tGUI;
 import java.awt.Color;
 import java.awt.event.MouseEvent;
 
 public class TFrame extends TWindow {
   TLabel lblTitle;
   TPanel contentPane;
 
   public TFrame(TransparentGUI gui) { this(gui, null, new TBorderLayout()); }
   public TFrame(TransparentGUI gui, TLayoutManager layout) { this(gui, null, layout); }
   public TFrame(TransparentGUI gui, String title) { this(gui, title, new TBorderLayout()); }
   public TFrame(TransparentGUI gui, String title, TLayoutManager layout) {
     super(gui, new TBorderLayout()); setMovable(true);
    super.add(lblTitle = new TLabel(gui), TBorderLayout.NORTH, -1);
    super.add(contentPane = new TPanel(gui, layout), TBorderLayout.CENTER, -1);
    lblTitle.capturesMouse = contentPane.capturesMouse = false;
     setFocusable(true);
     setTitle(title);
   }
 
   public TPanel getContentPane() { return contentPane; }
 
   public String getTitle() { return lblTitle.isVisible() ? lblTitle.getText() : null; }
   public void setTitle(String title) {
     lblTitle.setText((title != null) ? title : "");
     lblTitle.setVisible(title != null);
   }
 
   public void add(TComponent comp, Object hint, int index) { contentPane.add(comp, hint, index); }
   public void remove(int index) { contentPane.remove(index); }
   public void remove(TComponent comp) { contentPane.remove(comp); }
 
   public void handleMouseEvent(MouseEvent e) {
     super.handleMouseEvent(e);
     if (e.getID() == MouseEvent.MOUSE_PRESSED)
       requestFocus();
   }
 
  public void validate() {
    if (valid) return;
    lblTitle.setMargin(0);
    lblTitle.setPadding(2);
    lblTitle.setAlignment(TLabel.ALIGN_CENTER);
    lblTitle.setBackgroundColor(new Color(225, 225, 225, 225));  // FIXME: should be a style option
    lblTitle.setBorderRadius(getBorderRadiusTopLeft(), getBorderRadiusTopRight(), 0, 0);
    contentPane.setMargin(0);
    contentPane.setPadding(10);
    super.validate();
  }
 }
