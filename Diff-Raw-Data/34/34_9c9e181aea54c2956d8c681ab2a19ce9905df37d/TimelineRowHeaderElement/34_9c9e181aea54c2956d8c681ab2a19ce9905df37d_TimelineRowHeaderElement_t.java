/* $Id: TimelineRowHeaderElement.java,v 1.3 2007-01-07 14:46:39 hampelratte Exp $
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
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Insets;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.Border;
 
 import lazybones.ProgramManager;
 import lazybones.Timer;
 import lazybones.VDRChannelList;
 import tvbrowser.ui.programtable.ChannelLabel;
 import devplugin.Channel;
 
 public class TimelineRowHeaderElement extends JPanel {
     
     private Timer timer;
     
     public TimelineRowHeaderElement(Timer timer) {
         this.timer = timer;
         initGUI();
     }
 
     private void initGUI() {
         setBorder(new TimelineRowHeaderElementBorder(TimelineRowHeaderElementBorder.TOP));
         Channel tvbChannel = ProgramManager.getInstance().getChannel(timer);
         if(tvbChannel != null) {
             ChannelLabel label = new ChannelLabel(tvbChannel);
             add(label);
         } else {
             de.hampelratte.svdrp.responses.highlevel.Channel vdrChannel = VDRChannelList.getInstance().getChannelByNumber(timer.getChannelNumber());
            if(vdrChannel != null) {
                JLabel l = new JLabel(vdrChannel.getName());
                l.setFont(l.getFont().deriveFont(Font.BOLD));
                add(l);
            }
         }
     }
     
     public class TimelineRowHeaderElementBorder implements Border {
         public static final int TOP = 1;
         public static final int BOTTOM = 2;
         public static final int LEFT = 4;
         public static final int RIGHT = 8;
         
         private int borders = 0;
         
         public TimelineRowHeaderElementBorder(int borders) {
             this.borders = borders;
         }
         
         public Insets getBorderInsets(Component c) {
             int top = (borders & TOP) == TOP ? 1 : 0;
             int bottom = (borders & BOTTOM) == BOTTOM ? 1 : 0;
             int left = (borders & LEFT) == LEFT ? 1 : 0;
             int right = (borders & RIGHT) == RIGHT ? 1 : 0;
             return new Insets(top, left, bottom, right);
         }
 
         public boolean isBorderOpaque() {
             return true;
         }
 
         public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
             Insets i = getInsets();
             g.setColor(Color.GRAY);
             if (i.top == 1) {
                 g.drawLine(0, 0, width, 0);
             }
             if (i.bottom == 1) {
                 g.drawLine(0, height - 1, width, 0);
             }
             if (i.left == 1) {
                 g.drawLine(0, 0, 0, height - 0);
             }
             if (i.right == 1) {
                 g.drawLine(width - 1, 0, width - 1, height - 0);
             }
         }
     }
 }
