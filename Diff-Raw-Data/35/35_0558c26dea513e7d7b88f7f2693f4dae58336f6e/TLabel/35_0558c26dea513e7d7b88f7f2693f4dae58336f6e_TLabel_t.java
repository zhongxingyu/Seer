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
 import processing.core.PApplet;
 import processing.core.PFont;
 import processing.core.PGraphics;
 
 public class TLabel extends TComponent {
   protected String text = null;
 
   public static final int ALIGN_LEFT = PApplet.LEFT;
   public static final int ALIGN_CENTER = PApplet.CENTER;
   public static final int ALIGN_RIGHT = PApplet.RIGHT;
   public static final int VALIGN_TOP = PApplet.TOP;
   public static final int VALIGN_CENTER = PApplet.CENTER;
   public static final int VALIGN_BOTTOM = PApplet.BOTTOM;
   protected int align = ALIGN_LEFT, valign = VALIGN_CENTER;
 
   public TLabel(TransparentGUI gui) { this(gui, ""); }
   public TLabel(TransparentGUI gui, String s) {
     super(gui); setText(s); setFocusable(false); capturesMouse = false; }
 
   public String getText() { return text; }
   public void setText(String s) { text = s; invalidate(); }
 
   public int getAlignment() { return align; }
   public void setAlignment(int align) { this.align = align; }
   public void setAlignment(int align, int valign) { this.align = align; setVerticalAlignment(valign); }
   public int getVerticalAlignment() { return valign; }
   public void setVerticalAlignment(int valign) { this.valign = valign; }
 
   public TComponent.Dimension getMinimumSize() {
     float maxWidth = 0;
     gui.app.g.textFont(getFont());
     String lines[] = PApplet.split(PApplet.trim(text), '\n');
     for (int i = 0; i < lines.length; i++)
       maxWidth = PApplet.max(maxWidth, gui.app.g.textWidth(lines[i]));
     float height = gui.app.g.textAscent() + gui.app.g.textDescent();
     height = height + (lines.length - 1)*gui.app.g.textLeading;
    return new TComponent.Dimension(maxWidth, PApplet.ceil(height));
   }
 
   public void draw(PGraphics g) {
     super.draw(g);
     g.noStroke();
     g.textFont(getFont());
     g.fill(getForeground());
     /*g.textAlign(align, g.BASELINE);
     float x = bounds.x + padding.left;
     if (align != ALIGN_LEFT) {
       float w = bounds.width - padding.left - padding.right;
       if (align == ALIGN_CENTER) w /= 2;
       x += w;
     }
     float y = bounds.y + bounds.height - padding.bottom - g.textDescent();
     float h = g.textAscent() + g.textDescent();
     if (bounds.height - padding.top - padding.bottom > h)
       y -= (bounds.height - padding.top - padding.bottom - h)/2;*/
     g.rectMode(g.CORNERS);
     g.textAlign(align, valign);
     g.text(text, bounds.x + padding.left, bounds.y + padding.top,
                  bounds.x + bounds.width - padding.right, bounds.y + bounds.height - padding.bottom);
   }
 }
