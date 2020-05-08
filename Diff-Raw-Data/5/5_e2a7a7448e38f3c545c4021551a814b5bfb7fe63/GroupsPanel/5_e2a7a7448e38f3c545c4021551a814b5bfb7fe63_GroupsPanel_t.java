 /*
     This file is part of SchedVis.
 
     SchedVis is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     SchedVis is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with SchedVis.  If not, see <http://www.gnu.org/licenses/>.
 
  */
 /**
  * 
  */
 package cz.muni.fi.spc.SchedVis.ui;
 
 import java.awt.GridLayout;
 import java.awt.LayoutManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.AbstractMap;
 import java.util.AbstractSet;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import javax.swing.JCheckBox;
 
 import cz.muni.fi.spc.SchedVis.model.entities.MachineGroup;
 import cz.muni.fi.spc.SchedVis.model.models.ScheduleTreeModel;
 import cz.muni.fi.spc.SchedVis.ui.common.JBorderedPanel;
 
 /**
  * @author Lukáš Petrovický <petrovicky@mail.muni.cz>
  * 
  */
 public class GroupsPanel extends JBorderedPanel implements ActionListener {
 
     /**
      * 
      */
     private static final long serialVersionUID = 8820656296600471333L;
 
     private final AbstractMap<Integer, JCheckBox> boxes = new HashMap<Integer, JCheckBox>();
 
     /**
      * @param isDoubleBuffered
      * @param title
      */
     public GroupsPanel(final boolean isDoubleBuffered, final String title) {
 	super(isDoubleBuffered, title);
 	this.specialize();
     }
 
     /**
      * @param layout
      * @param isDoubleBuffered
      * @param title
      */
     public GroupsPanel(final LayoutManager layout,
 	    final boolean isDoubleBuffered, final String title) {
 	super(layout, isDoubleBuffered, title);
 	this.specialize();
     }
 
     /**
      * @param layout
      * @param title
      */
     public GroupsPanel(final LayoutManager layout, final String title) {
 	super(layout, title);
 	this.specialize();
     }
 
     /**
      * @param title
      */
     public GroupsPanel(final String title) {
 	super(title);
 	this.specialize();
     }
 
     public void actionPerformed(final ActionEvent e) {
 	if (this.boxes.containsValue(e.getSource())) {
 	    ScheduleTreeModel.getInstance().regroup(this.getSelectedGroups());
 	}
     }
 
     public AbstractSet<Integer> getSelectedGroups() {
 	final AbstractSet<Integer> selectedBoxes = new HashSet<Integer>();
 	for (final Integer groupId : this.boxes.keySet()) {
 	    if (this.boxes.get(groupId).isSelected()) {
 		selectedBoxes.add(groupId);
 	    }
 	}
 	return selectedBoxes;
     }
 
     private void specialize() {
 	this.setLayout(new GridLayout(0, 2));
	this.update();
     }
 
     public void update() {
 	final AbstractSet<Integer> selectedGroups = this.getSelectedGroups();
 	// assemble new groups
 	this.boxes.clear();
 	for (final MachineGroup item : MachineGroup.getAll()) {
 	    this.boxes.put(item.getId(), new JCheckBox(item.getName()));
 	}
 	this.boxes.put(-1, new JCheckBox("Ungrouped"));
 	// update the widget with new groups
 	this.removeAll();
 	for (final Integer groupId : this.boxes.keySet()) {
 	    final JCheckBox box = this.boxes.get(groupId);
 	    // select those selected before
 	    if (selectedGroups.contains(groupId)) {
 		box.setSelected(true);
 	    }
 	    // add it to the screen
 	    box.addActionListener(this);
 	    this.add(box);
 	}
 	ScheduleTreeModel.getInstance().regroup(this.getSelectedGroups());
     }
 
 }
