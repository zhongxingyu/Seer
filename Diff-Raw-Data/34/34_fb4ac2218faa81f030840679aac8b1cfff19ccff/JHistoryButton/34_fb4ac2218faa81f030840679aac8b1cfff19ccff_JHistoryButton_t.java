 /*
  * JHistoryButton.java
  * Copyright (C) 2005 by:
  *
  *----------------------------
  * cismet GmbH
  * Goebenstrasse 40
  * 66117 Saarbruecken
  * http://www.cismet.de
  *----------------------------
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *----------------------------
  * Author:
  * thorsten.hell@cismet.de
  *----------------------------
  *
  * Created on 14. Juli 2005, 14:25
  *
  */
 
 package de.cismet.tools.gui.historybutton;
 import de.cismet.tools.gui.JPopupMenuButton;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Vector;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 /**
  * The implementation of the JHistoryButton. See a short 
  * <a href="http://flexo.cismet.de/gadgets/JHistory/">description and demo</a>
  * on the website.
  * <p>
  *
  * <p>
  * License: <a href="http://www.gnu.org/copyleft/lesser.html#TOC1">GNU LESSER GENERAL PUBLIC LICENSE</a>
  * <br><img src="http://opensource.org/trademarks/osi-certified/web/osi-certified-60x50.gif"> <img src="http://opensource.org/trademarks/opensource/web/opensource-55x48.gif">
  * </p>
 
  * @author thorsten.hell@cismet.de
  */
 public class JHistoryButton extends JPopupMenuButton implements ActionListener,HistoryModelListener {
     
     public static final int DIRECTION_FORWARD=1;
     public static final int DIRECTION_BACKWARD=2;
     
     public static final int ICON_SIZE_16=4;
     public static final int ICON_SIZE_22=8;
     public static final int ICON_SIZE_32=16;
     public static final int ICON_SIZE_64=32;
     public static final int ICON_SIZE_128=64;
 
    
    private int maxHistoryMenuLength=10; 
    
     private JPopupMenu popupMenu=new JPopupMenu();
     
     private int direction=1;
     private boolean localEnabled=true;
     private HistoryModel historyModel=null;
     
     public static JHistoryButton getDefaultJHistoryButton(int direction,int iconSize,HistoryModel model) {
         JHistoryButton ret=new JHistoryButton();
         ret.setDirection(direction);
         ret.setHistoryModel(model);
        String ressourcePath="/de/cismet/tools/gui/historybutton/res/";  //NOI18N
        String forward="forward";  //NOI18N
        String back="back";  //NOI18N
         String name;
        String filetype=".png";  //NOI18N
         if (direction==DIRECTION_FORWARD) {
             name=forward;
         }
         else {
             name=back;
         }
         switch(iconSize) {
             case ICON_SIZE_16:
                ret.setIcon(new javax.swing.ImageIcon(ret.getClass().getResource(ressourcePath+name+"16"+filetype)));  //NOI18N
                 break;
             case ICON_SIZE_22:
                ret.setIcon(new javax.swing.ImageIcon(ret.getClass().getResource(ressourcePath+name+"22"+filetype)));  //NOI18N
                 break;
             case ICON_SIZE_32:
                ret.setIcon(new javax.swing.ImageIcon(ret.getClass().getResource(ressourcePath+name+"32"+filetype)));  //NOI18N
                 break;
             case ICON_SIZE_64:
                ret.setIcon(new javax.swing.ImageIcon(ret.getClass().getResource(ressourcePath+name+"64"+filetype)));  //NOI18N
                 break;
             case ICON_SIZE_128:
                ret.setIcon(new javax.swing.ImageIcon(ret.getClass().getResource(ressourcePath+name+"128"+filetype)));  //NOI18N
                 break;
         }
         return ret;
     }
     
     /** Creates a new instance of JHistoryButton */
     public JHistoryButton() {
         super.setEnabled(false);
         this.setPopupMenu(popupMenu);
         this.addActionListener(this);
     }
 
     public HistoryModel getHistoryModel() {
         return historyModel;
     }
 
     public void setHistoryModel(HistoryModel historyModel) {
         this.historyModel = historyModel;
         historyModel.addHistoryModelListener(this);
         setEnabled(localEnabled);
         if (localEnabled&&direction==DIRECTION_FORWARD) {
             forwardStatusChanged();
         }
         else {
             backStatusChanged();
         }
 
     }
 
     public void historyChanged() {
         
         
     }
 
     public void forwardStatusChanged() {
         // you have to check whether the component is enabled
         // if you have to disable the component due to history reasons
         // you have to modify the super component directly
         // otherwise there would be no chance to disable the component permanently
         if (localEnabled&&direction==DIRECTION_FORWARD) {
             if (historyModel.isForwardPossible()) {
                 setEnabled(true);
                 Vector poss=historyModel.getForwardPossibilities();
                 refreshPopup(poss);
             }
             else {
 
                 super.setEnabled(false);
             }
         }
         
     }
 
     public void backStatusChanged() {
         // you have to check whether the component is enabled
         // if you have to disable the component due to history reasons
         // you have to modify the super component directly
         // otherwise there would be no chance to disable the component permanently
         if (localEnabled&&direction==DIRECTION_BACKWARD) {
             if (historyModel.isBackPossible()) {
                 setEnabled(true);
                 Vector poss=historyModel.getBackPossibilities();
                 refreshPopup(poss);
             }
             else {
                 super.setEnabled(false);
                 refreshPopup(null);
                 
             }
         }
     }
     
     private void refreshPopup(Vector possibilities) {
         if (possibilities==null) {
             possibilities=new Vector();
         }
         popupMenu.removeAll();
         
         for (int i=possibilities.size()-1;i>=0;--i) {
             Object o=possibilities.get(i);
             JHistoryMenuItem item = new JHistoryMenuItem(o,possibilities.size()-i);
             popupMenu.add(item);
             item.addActionListener(this);
         }
         if (possibilities.size()>maxHistoryMenuLength) {
             int tooMuch=possibilities.size()-maxHistoryMenuLength;
             for (int i=0;i<tooMuch;++i) {
                 popupMenu.remove(possibilities.size()-1-i);
             }
         }
     }
     
     public void setEnabled(boolean enabled) {
         localEnabled=enabled;
         if (historyModel!=null) {
             super.setEnabled(localEnabled);
         }
         else {
             super.setEnabled(false);
         }
     }
 
     
     public void actionPerformed(java.awt.event.ActionEvent e) {
         if (e!=null&&e.getSource() instanceof JHistoryMenuItem) {
             JHistoryMenuItem source=(JHistoryMenuItem)e.getSource();
             for (int i=0;i<source.getPosition()-1;++i) {
                 if (direction==DIRECTION_BACKWARD) {
                     historyModel.back(false);
                 }
                 else if (direction==DIRECTION_FORWARD) {
                     historyModel.forward(false);
                 }
             }
            fireActionPerformed(new ActionEvent(this,0,"JHistoryButtonMenuActionPerformed"));  //NOI18N
         }
         else if (e!=null&&e.getSource() instanceof JHistoryButton) {//&&e.getActionCommand()!="JHistoryButtonMenuActionPerformed"
             Object o=null;
             if (direction==DIRECTION_BACKWARD) {
                 o=historyModel.back(true);
             }
             else if (direction==DIRECTION_FORWARD) {
                 o=historyModel.forward(true);
             }
         }
     }
     
 
     public int getDirection() {
         return direction;
     }
 
     
     
     /**
      *
      *
      * @beaninfo
      *        bound: true
      *         enum: DIRECTION_FORWARD     JHistoryButton.DIRECTION_FORWARD
      *               DIRECTION_BACKWARD    JHistoryButton.DIRECTION_BACKWARD
       *    attribute: visualUpdate true
      *  description: The alignment of the label's content along the X axis.
      */
      
     public void setDirection(int direction) {
         this.direction = direction;
     }
     
     
     
     public JLabel getJHistoryLabel() {
         return new JHistoryLabel();
     }
 
     public void historyActionPerformed() {
     }
     
     private class JHistoryLabel extends JLabel implements HistoryModelListener {
         String text;
         public JHistoryLabel() {
             historyModel.addHistoryModelListener(this);
         }
         public void historyChanged() {
             text=historyModel.getCurrentElement().toString();
             setText(text);
         }
 
         public void forwardStatusChanged() {
         }
 
         public void backStatusChanged() {
         }
         
          public String getText() {
             if (text!=null&&text.length()>0) {
                 return text;
             }
             else {
                 historyChanged();
                 return text;
             }
          }
 
         public void historyActionPerformed() {
         }
     }
   
     
 }
