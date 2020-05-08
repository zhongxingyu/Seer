/* $Id: RecordingListCellRenderer.java,v 1.3 2007-04-10 19:44:25 hampelratte Exp $
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
 package lazybones.gui.utils;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.text.DateFormat;
 import java.util.Locale;
 
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListCellRenderer;
 import javax.swing.UIManager;
 
 import lazybones.LazyBones;
 
 import org.hampelratte.svdrp.responses.highlevel.Recording;
 
 public class RecordingListCellRenderer extends JPanel implements ListCellRenderer {
     
     private JLabel date = new JLabel();
     private JLabel newRec = new JLabel();
     private JLabel time = new JLabel();
     private JLabel title = new JLabel();
     
     private Color background = Color.WHITE;
     private Color altBackground = new Color(250, 250, 220);
     
     public RecordingListCellRenderer() {
         initGUI();
     }
 
     private void initGUI() {
         // set foreground color
         time.setForeground(Color.BLACK);
         title.setForeground(Color.BLACK);
         newRec.setForeground(Color.BLACK);
         date.setForeground(Color.BLACK);
         
         Font bold = time.getFont().deriveFont(Font.BOLD);
         time.setFont(bold);
         title.setFont(bold);
         
         setLayout(new GridBagLayout());
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.insets = new Insets(5,5,5,5);
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.anchor = GridBagConstraints.NORTHWEST;
         
         gbc.gridx = 0;
         gbc.gridy = 0;
         add(date, gbc);
         gbc.gridx = 1;
         gbc.gridy = 0;
         add(newRec, gbc);
         gbc.gridx = 0;
         gbc.gridy = 1;
         add(time, gbc);
         gbc.gridx = 1;
         gbc.gridy = 1;
         gbc.weightx = 1.0;
         add(title, gbc);
     }
 
     public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
         if(isSelected) {
             setBackground(UIManager.getColor("List.selectionBackground"));
         } else {
             setBackground(index % 2 == 0 ? background : altBackground);
         }
         
         if(value instanceof Recording) {
             Recording recording = (Recording)value;
             DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());
             DateFormat tf = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());
             
             date.setText(df.format(recording.getStartTime().getTime()));
             time.setText(tf.format(recording.getStartTime().getTime()));
             title.setText(recording.getTitle());
             
             if(recording.isNew()) {
                 newRec.setIcon(LazyBones.getInstance().getIcon("lazybones/new.png"));
            } else {
                newRec.setIcon(null);
             }
             
             return this;
         } else {
             return new JLabel(value.toString());
         }
     }
 }
