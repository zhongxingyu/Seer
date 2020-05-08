 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package evopaint.pixel.rulebased.conditions;
 
 import evopaint.gui.rulesetmanager.util.NamedObjectListCellRenderer;
 import evopaint.pixel.rulebased.targeting.IConditionTarget;
 import evopaint.pixel.rulebased.util.ObjectComparisonOperator;
 import evopaint.pixel.rulebased.Condition;
 import evopaint.pixel.Pixel;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.LinkedHashMap;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 
 /**
  *
  * @author tam
  */
 public class ExistenceCondition extends Condition {
 
     private ObjectComparisonOperator objectComparisonOperator;
 
     public ExistenceCondition(IConditionTarget target, ObjectComparisonOperator objectComparisonOperator) {
         super(target);
         this.objectComparisonOperator = objectComparisonOperator;
     }
 
     public ExistenceCondition() {
         objectComparisonOperator = ObjectComparisonOperator.EQUAL;
     }
 
     public ObjectComparisonOperator getComparisonOperator() {
         return objectComparisonOperator;
     }
 
     public void setComparisonOperator(ObjectComparisonOperator comparisonOperator) {
         this.objectComparisonOperator = comparisonOperator;
     }
 
     public String getName() {
         return "existence";
     }
 
     public boolean isMet(Pixel us, Pixel them) {
        return objectComparisonOperator.compare(them, null);
     }
 
     @Override
     public String toString() {
         if (objectComparisonOperator == ObjectComparisonOperator.NOT_EQUAL) {
             return "does not exist";
         }
         return "exists";
     }
 
     public String toHTML() {
         if (objectComparisonOperator == ObjectComparisonOperator.NOT_EQUAL) {
             return "does not exist";
         }
         return "exists";
     }
 
     public LinkedHashMap<String,JComponent> addParametersGUI(LinkedHashMap<String,JComponent> parametersMap) {
         JComboBox comparisonComboBox = new JComboBox(ObjectComparisonOperator.createComboBoxModel());
         comparisonComboBox.setRenderer(new NamedObjectListCellRenderer());
         comparisonComboBox.setSelectedItem(objectComparisonOperator);
         comparisonComboBox.addActionListener(new ComparisonListener());
         comparisonComboBox.setPreferredSize(new Dimension(80, 25));
         parametersMap.put("Comparison", comparisonComboBox);
 
         return parametersMap;
     }
 
     private class ComparisonListener implements ActionListener {
 
         public void actionPerformed(ActionEvent e) {
             setComparisonOperator((ObjectComparisonOperator) ((JComboBox) (e.getSource())).getSelectedItem());
         }
     }
 }
