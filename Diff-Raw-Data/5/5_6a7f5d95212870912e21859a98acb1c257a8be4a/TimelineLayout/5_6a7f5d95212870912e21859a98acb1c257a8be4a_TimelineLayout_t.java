/* $Id: TimelineLayout.java,v 1.3 2007-01-05 23:22:43 hampelratte Exp $
  * 
  * Copyright (c) 2005, Henrik Niehaus & Lazy Bones development team
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
 package lazybones.gui.components.timeline;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.LayoutManager2;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import lazybones.Timer;
 
 public class TimelineLayout implements LayoutManager2 {
     
     private int rowHeight = 50;
     
     private int padding = 0;
     
     private ArrayList<Component> components = new ArrayList<Component>();
 
     public TimelineLayout() {}
     
     public TimelineLayout(int rowHeight) {
         this.rowHeight = rowHeight;
     }
     
     public TimelineLayout(int rowHeight, int padding) {
         this(rowHeight);
         this.padding = padding;
     }
     
     public void addLayoutComponent(Component comp, Object constraints) {
         components.add(comp);
     }
 
     public float getLayoutAlignmentX(Container target) {
         return 0;
     }
 
     public float getLayoutAlignmentY(Container target) {
         return 0;
     }
 
     public void invalidateLayout(Container target) {
     }
 
     public Dimension maximumLayoutSize(Container target) {
         return target.getMaximumSize();
     }
 
     public void addLayoutComponent(String name, Component comp) {
         components.add(comp);
     }
 
     public void layoutContainer(Container parent) {
         if(parent.isValid()) {
             return;
         }
         
        int width = parent instanceof TimelineRowHeader ? 0 : parent.getParent().getWidth();
         int height = parent.getHeight();
         
         double pixelsPerMinute = (double)(width-1) / (double)(24 * 60);
         
         int rowCount = 0;
         Map<Integer, Integer> channelRowMap = new HashMap<Integer, Integer>();
         for (Component comp : components) {
             if(comp instanceof TimelineElement) {
                 TimelineElement te = (TimelineElement) comp;
                 Timer timer = te.getTimer();
                 Calendar currentDate = te.getCurrentDate();
                 Calendar nextDate = (Calendar) currentDate.clone();
                 nextDate.add(Calendar.DAY_OF_MONTH, 1);
                 int startMinute = timer.getStartTime().get(Calendar.MINUTE);
                 startMinute += timer.getStartTime().get(Calendar.HOUR_OF_DAY) * 60;
                 int startPos = (int)(startMinute * pixelsPerMinute);
                 int endMinute = timer.getEndTime().get(Calendar.MINUTE);
                 endMinute += timer.getEndTime().get(Calendar.HOUR_OF_DAY) * 60;
                 int endPos = (int)(endMinute * pixelsPerMinute);
                 if(timer.getStartTime().before(currentDate)) {
                     startPos = 0;
                 }
                 if(timer.getEndTime().after(nextDate)) {
                     endPos = width;
                 }
                 int length = endPos - startPos;
                 Integer channelRow = channelRowMap.get(timer.getChannelNumber());
                 int row = rowCount;
                 if(channelRow == null) {
                     channelRowMap.put(timer.getChannelNumber(), rowCount);
                     rowCount++;
                 } else {
                     row = channelRow.intValue();
                 }
                 
                 te.setLocation(startPos, (rowHeight+padding) * row);
                 te.setSize(length, rowHeight);
             } else if (comp instanceof TimelineRowHeaderElement) {
                 comp.setSize(comp.getPreferredSize());
                 comp.setLocation(0, (rowHeight + padding) * rowCount);
                 rowCount++;
                 if(comp.getWidth() > width) {
                     width = comp.getWidth();
                 }
             }
         }
         
         if(components.size() == 0 && parent instanceof TimelineRowHeader) {
             width=0;
         }
         
         parent.setPreferredSize(new Dimension(width, height));
         parent.setSize(width, height);
     }
 
     public Dimension minimumLayoutSize(Container parent) {
         Dimension d = new Dimension();
         d.width = parent.getWidth();
         d.height = 0;
         for (Iterator iter = components.iterator(); iter.hasNext();) {
             Component comp = (Component) iter.next();
             if(comp.getWidth() > d.width) {
                 d.width = comp.getWidth();
             }
             d.height += rowHeight + padding;
         }
         return d;
     }
 
     public Dimension preferredLayoutSize(Container parent) {
         Dimension d = new Dimension();
         d.width = parent.getWidth();
         d.height = 0;
         for (Iterator iter = components.iterator(); iter.hasNext();) {
             Component comp = (Component) iter.next();
             if(comp.getWidth() > d.width) {
                 d.width = comp.getWidth();
             }
             d.height += rowHeight + padding;
         }
         return d;
     }
 
     public void removeLayoutComponent(Component comp) {
         components.remove(comp);
     }
 }
