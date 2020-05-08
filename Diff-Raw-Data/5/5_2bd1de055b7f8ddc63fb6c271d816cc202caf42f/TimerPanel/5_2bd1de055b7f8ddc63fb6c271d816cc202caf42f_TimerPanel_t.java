/* $Id: TimerPanel.java,v 1.5 2008-07-30 12:02:13 hampelratte Exp $
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
 package lazybones.gui.settings;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.*;
 
 import lazybones.ConflictFinder;
 import lazybones.LazyBones;
 import lazybones.TimerManager;
 import lazybones.gui.utils.TitleMapping;
 
 
 /**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 public class TimerPanel implements MouseListener, ActionListener {
     private final String lBefore = LazyBones.getTranslation("before", "Buffer before program");
 
     private final String ttBefore = LazyBones.getTranslation("before.tooltip", "Time buffer before program");
 
     private JSpinner before;
 
     private final String lAfter = LazyBones.getTranslation("after", "Buffer after program");
 
     private final String ttAfter = LazyBones.getTranslation("after.tooltip", "Time buffer after program");
 
     private JSpinner after;
 
     private JLabel labBefore, labAfter;
 
     private JLabel lPrio = new JLabel(LazyBones.getTranslation("priority", "Priority"));
 
     private JSpinner prio;
 
     private JLabel lLifetime = new JLabel(LazyBones.getTranslation("lifetime", "Lifetime"));
 
     private JSpinner lifetime;
 
     private JLabel lNumberOfCards = new JLabel(LazyBones.getTranslation("numberOfCards", "Number of DVB cards"));
 
     private JSpinner numberOfCards;
 
     private String lMappings = LazyBones.getTranslation("mappings", "Title mappings");
 
     private JLabel labMappings;
 
     private JTable mappingTable;
 
     private JScrollPane mappingPane;
 
     private JButton addRow;
 
     private JButton delRow;
     
     private JLabel lDescSource;
     
     private JComboBox cbDescSource;
 
     private JPopupMenu mappingPopup = new JPopupMenu();
 
     public TimerPanel() {
         initComponents();
     }
 
     private void initComponents() {
         int int_before = Integer.parseInt(LazyBones.getProperties().getProperty("timer.before"));
         int int_after = Integer.parseInt(LazyBones.getProperties().getProperty("timer.after"));
         int int_prio = Integer.parseInt(LazyBones.getProperties().getProperty("timer.prio"));
         int int_lifetime = Integer.parseInt(LazyBones.getProperties().getProperty("timer.lifetime"));
         int int_numberOfCards = Integer.parseInt(LazyBones.getProperties().getProperty("numberOfCards"));
         int descSourceTvb = Integer.parseInt(LazyBones.getProperties().getProperty("descSourceTvb"));
         before = new JSpinner();
         before.setValue(new Integer(int_before));
         before.setToolTipText(ttBefore);
         ((JSpinner.DefaultEditor) before.getEditor()).getTextField().setColumns(2);
         labBefore = new JLabel(lBefore);
         labBefore.setToolTipText(ttBefore);
         labBefore.setLabelFor(before);
 
         after = new JSpinner();
         ((JSpinner.DefaultEditor) after.getEditor()).getTextField().setColumns(2);
         after.setToolTipText(ttAfter);
         after.setValue(new Integer(int_after));
         labAfter = new JLabel(lAfter);
         labAfter.setToolTipText(ttAfter);
         labAfter.setLabelFor(after);
 
         prio = new JSpinner();
         ((JSpinner.DefaultEditor) prio.getEditor()).getTextField().setColumns(2);
         prio.setModel(new SpinnerNumberModel(int_prio, 0, 99, 1));
         lifetime = new JSpinner();
         ((JSpinner.DefaultEditor) lifetime.getEditor()).getTextField().setColumns(2);
         lifetime.setModel(new SpinnerNumberModel(int_lifetime, 0, 99, 1));
 
         numberOfCards = new JSpinner();
         ((JSpinner.DefaultEditor) numberOfCards.getEditor()).getTextField().setColumns(2);
         numberOfCards.setModel(new SpinnerNumberModel(int_numberOfCards, 1, 10, 1));
 
         labMappings = new JLabel(lMappings);
         mappingTable = new JTable(TimerManager.getInstance().getTitleMapping());
         mappingPane = new JScrollPane(mappingTable);
         mappingTable.addMouseListener(this);
         mappingPane.addMouseListener(this);
 
         JMenuItem itemAdd = new JMenuItem(LazyBones.getTranslation("add_row", "Add row"));
         itemAdd.setActionCommand("ADD");
         itemAdd.addActionListener(this);
         JMenuItem itemDel = new JMenuItem(LazyBones.getTranslation("del_rows", "Delete selected rows"));
         itemDel.setActionCommand("DEL");
         itemDel.addActionListener(this);
         mappingPopup.add(itemAdd);
         mappingPopup.add(itemDel);
 
         addRow = new JButton(LazyBones.getTranslation("add_row", "Add row"));
         addRow.setActionCommand("ADD");
         addRow.addActionListener(this);
         delRow = new JButton(LazyBones.getTranslation("del_rows", "Delete selected rows"));
         delRow.setActionCommand("DEL");
         delRow.addActionListener(this);
         
         lDescSource = new JLabel(LazyBones.getTranslation("desc_source", "Use description from TV-Browser"));
         cbDescSource = new JComboBox();
         cbDescSource.addItem("VDR");
         cbDescSource.addItem("TV-Browser");
         cbDescSource.addItem(LazyBones.getTranslation("timer_desc_longest", "longest description"));
         cbDescSource.setSelectedIndex(descSourceTvb);
     }
 
     public JPanel getPanel() {
         JPanel panel = new JPanel(new GridBagLayout());
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.anchor = GridBagConstraints.WEST;
 
         // left column of spinners
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.insets = new Insets(15, 15, 5, 5);
         panel.add(labBefore, gbc);
 
         gbc.gridx = 1;
         gbc.gridy = 0;
         gbc.insets = new Insets(15, 5, 5, 5);
         panel.add(before, gbc);
 
         gbc.gridx = 0;
         gbc.gridy = 1;
         gbc.insets = new Insets(5, 15, 5, 5);
         panel.add(labAfter, gbc);
 
         gbc.gridx = 1;
         gbc.gridy = 1;
         gbc.insets = new Insets(5, 5, 5, 5);
         panel.add(after, gbc);
 
         gbc.gridx = 0;
         gbc.gridy = 2;
         gbc.insets = new Insets(15, 15, 5, 5);
         panel.add(lDescSource, gbc);
         
         gbc.gridx = 1;
         gbc.gridy = 2;
         gbc.insets = new Insets(15, 5, 5, 5);
         panel.add(cbDescSource, gbc);
         
         gbc.gridx = 0;
         gbc.gridy = 3;
         gbc.insets = new Insets(5, 15, 30, 5);
         panel.add(lNumberOfCards, gbc);
 
         gbc.gridx = 1;
         gbc.gridy = 3;
         gbc.insets = new Insets(15, 5, 30, 5);
        panel.add(numberOfCards, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 30, 5), 0, 0));
 
         // right column of spinners
         gbc.gridx = 2;
         gbc.gridy = 0;
         gbc.insets = new Insets(15, 50, 5, 5);
         panel.add(lPrio, gbc);
 
         gbc.gridx = 3;
         gbc.gridy = 0;
         gbc.insets = new Insets(15, 5, 5, 5);
         panel.add(prio, gbc);
 
         gbc.gridx = 2;
         gbc.gridy = 1;
         gbc.insets = new Insets(5, 50, 5, 5);
         panel.add(lLifetime, gbc);
 
         gbc.gridx = 3;
         gbc.gridy = 1;
         gbc.insets = new Insets(5, 5, 5, 5);
         panel.add(lifetime, gbc);
 
         // mapping
         gbc.gridx = 0;
         gbc.gridy = 4;
         gbc.insets = new Insets(5, 15, 5, 5);
         panel.add(labMappings, gbc);
 
         gbc.gridx = 0;
         gbc.gridy = 5;
         gbc.gridwidth = 4;
         gbc.gridheight = 2;
         gbc.fill = GridBagConstraints.BOTH;
         gbc.weightx = 1.0;
         gbc.weighty = 1.0;
         gbc.insets = new Insets(0, 15, 15, 5);
         panel.add(mappingPane, gbc);
 
         // buttons
         gbc.gridx = 4;
         gbc.gridy = 5;
         gbc.gridwidth = 1;
         gbc.gridheight = 1;
         gbc.insets = new Insets(0, 5, 5, 15);
         gbc.weightx = 0.0;
         gbc.weighty = 0.0;
         gbc.anchor = GridBagConstraints.NORTHWEST;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         panel.add(addRow, gbc);
 
         gbc.gridx = 4;
         gbc.gridy = 6;
         gbc.insets = new Insets(5, 5, 5, 15);
         panel.add(delRow, gbc);
 
         return panel;
     }
 
     public void saveSettings() {
         LazyBones.getProperties().setProperty("timer.before", before.getValue().toString());
         LazyBones.getProperties().setProperty("timer.after", after.getValue().toString());
         LazyBones.getProperties().setProperty("timer.prio", prio.getValue().toString());
         LazyBones.getProperties().setProperty("timer.lifetime", lifetime.getValue().toString());
         LazyBones.getProperties().setProperty("numberOfCards", numberOfCards.getValue().toString());
         LazyBones.getProperties().setProperty("descSourceTvb", Integer.toString(cbDescSource.getSelectedIndex()));
         
         ConflictFinder.getInstance().findConflicts();
         ConflictFinder.getInstance().handleConflicts();
     }
 
     public void mouseClicked(MouseEvent e) {
     }
 
     public void mouseEntered(MouseEvent e) {
     }
 
     public void mouseExited(MouseEvent e) {
     }
 
     public void mousePressed(MouseEvent e) {
     }
 
     public void mouseReleased(MouseEvent e) {
         if ((e.getSource() == mappingPane || e.getSource() == mappingTable) && e.getButton() == MouseEvent.BUTTON3) {
             mappingPopup.show(e.getComponent(), e.getX(), e.getY());
         }
     }
 
     public void actionPerformed(ActionEvent e) {
         if ("ADD".equals(e.getActionCommand())) {
             TitleMapping mapping = TimerManager.getInstance().getTitleMapping();
             mapping.put("", "");
         } else if ("DEL".equals(e.getActionCommand())) {
             TitleMapping mapping = TimerManager.getInstance().getTitleMapping();
             int[] indices = mappingTable.getSelectedRows();
             for (int i = indices.length - 1; i >= 0; i--) {
                 mapping.removeRow(indices[i]);
             }
         }
     }
 }
