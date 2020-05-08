 /*
  *  Copyright (C) 2010 Markus Echterhoff <tam@edu.uni-klu.ac.at>
  * 
  *  This file is part of EvoPaint.
  * 
  *  EvoPaint is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with EvoPaint.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package evopaint.gui.rulesetmanager;
 
 import evopaint.gui.rulesetmanager.util.JRangeSlider;
 import evopaint.pixel.rulebased.targeting.IActionTarget;
 import evopaint.pixel.rulebased.targeting.IConditionTarget;
 import evopaint.pixel.rulebased.targeting.QuantifiedConditionTarget;
 import evopaint.pixel.rulebased.targeting.QuantifiedTarget;
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  *
  * @author Markus Echterhoff <tam@edu.uni-klu.ac.at>
  */
 public class JQuantifiedTarget extends JPanel {
     private JRangeSlider jRangeSlider;
     private JLabel lowValueLabel;
     private JLabel highValueLabel;
     private JMultiTarget jTarget;
 
     public JQuantifiedTarget(QuantifiedTarget target) {
         JPanel rangePanel = new JPanel();
         rangePanel.setLayout(new BorderLayout());
 
         JPanel lowAlignmentPanel = new JPanel();
         lowAlignmentPanel.setLayout(new BorderLayout());
         JPanel lowPanel = new JPanel();
         JLabel lowTextLabel = new JLabel("Min:");
         lowPanel.add(lowTextLabel);
         lowValueLabel = new JLabel(Integer.toString(target.getMin()));
         lowPanel.add(lowValueLabel);
         lowAlignmentPanel.add(lowPanel, BorderLayout.SOUTH);
         rangePanel.add(lowAlignmentPanel, BorderLayout.WEST);
 
        jRangeSlider = new JRangeSlider(target.getMin(), target.getMax(),
                 target.getMin(), target.getMax(), JRangeSlider.VERTICAL,
                 JRangeSlider.RIGHTLEFT_BOTTOMTOP);
         lowValueLabel.setText(Integer.toString(jRangeSlider.getLowValue()));
         jRangeSlider.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 lowValueLabel.setText(Integer.toString(jRangeSlider.getLowValue()));
                 highValueLabel.setText(Integer.toString(jRangeSlider.getHighValue()));
             }
         });
         
         rangePanel.add(jRangeSlider, BorderLayout.CENTER);
 
         JPanel highPanel = new JPanel();
         JLabel highTextLabel = new JLabel("Max:");
         highPanel.add(highTextLabel);
         highValueLabel = new JLabel(Integer.toString(target.getMax()));
         highValueLabel.setText(Integer.toString(jRangeSlider.getHighValue()));
         highPanel.add(highValueLabel);
         rangePanel.add(highPanel, BorderLayout.EAST);
 
         add(rangePanel);
 
         JLabel ofLabel = new JLabel("in");
         add(ofLabel);
         
         jTarget = new JMultiTarget(target, new TargetClickListener());
         add(jTarget);
     }
 
     public IConditionTarget createQuantifiedConditionTarget() {
         IConditionTarget target = new QuantifiedConditionTarget(
                 jTarget.getDirections(), jRangeSlider.getLowValue(),
                 jRangeSlider.getHighValue());
         return target;
     }
 
     public IActionTarget createQuantifiedActionTarget() {
         assert (false);
         return null;
     }
 
     private class TargetClickListener implements ActionListener {
 
         public void actionPerformed(ActionEvent e) {
             int currentMax = (Integer)(jRangeSlider.getMaximum());
             int newMax = ((JToggleButton)e.getSource()).isSelected() ? currentMax + 1 : currentMax - 1;
             jRangeSlider.setMaximum(newMax);
             if (jRangeSlider.getHighValue() == currentMax) {
                 jRangeSlider.setHighValue(newMax);
             }
             if (jRangeSlider.getLowValue() == currentMax) {
                 jRangeSlider.setLowValue(newMax);
             }
             jRangeSlider.revalidate();
         }
     }
 
 }
