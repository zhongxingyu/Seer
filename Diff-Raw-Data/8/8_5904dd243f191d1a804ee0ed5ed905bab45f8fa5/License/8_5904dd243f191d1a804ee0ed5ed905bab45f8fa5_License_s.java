 /*
  * Collab desktop - Software for shared drawing via internet in real-time
  * Copyright (C) 2012 Martin Indra <aktive@seznam.cz>
  *
  * This file is part of Collab desktop.
  *
  * Collab desktop is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Collab desktop is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Collab desktop.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package cz.mgn.collabdesktop.menu.frames.about;
 
 import cz.mgn.collabdesktop.menu.MenuFrame;
 import cz.mgn.collabdesktop.utils.Utils;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JEditorPane;
 import javax.swing.JScrollPane;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 /**
  *
  * @author Martin Indra <aktive@seznam.cz>
  */
 public class License extends MenuFrame {
 
     public License() {
         super();
     }
 
     @Override
     protected String getSectionName() {
         return "license";
     }
 
     @Override
     protected void initComponents() {
         setMinimumSize(new Dimension(400, 400));
         setPreferredSize(new Dimension(400, 450));
         setSize(getPreferredSize());
         setLayout(new BorderLayout(0, 10));
 
         try {
            JEditorPane licence = new JEditorPane(License.class.getResource("/resources/other/gplv3.html"));
             licence.addHyperlinkListener(new HyperlinkListener() {
 
                 @Override
                 public void hyperlinkUpdate(HyperlinkEvent e) {
                     if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                         Utils.browse(e.getURL().toString());
                     }
                 }
             });
             licence.setEditable(false);
             JScrollPane licenceScrollPane = new JScrollPane(licence);
             add(licenceScrollPane, BorderLayout.CENTER);
         } catch (IOException ex) {
             Logger.getLogger(License.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 }
