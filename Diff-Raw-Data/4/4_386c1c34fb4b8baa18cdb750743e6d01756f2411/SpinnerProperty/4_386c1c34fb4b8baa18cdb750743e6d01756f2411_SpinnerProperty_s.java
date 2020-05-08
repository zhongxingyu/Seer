 package phyml.gui.control;
 
 import com.google.common.collect.ImmutableSet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import phyml.gui.model.AbstractProperty;
 import phyml.gui.model.Node;
 
 import javax.swing.*;
 import java.util.Set;
 
 /**
  * Project: grisu
  * <p/>
  * Written by: Markus Binsteiner
  * Date: 4/10/13
  * Time: 3:25 PM
  */
 public class SpinnerProperty extends AbstractProperty {
 
     public static final String OPTION_MIN = "min";
     public static final String OPTION_MAX = "max";
     public static final String OPTION_STEP = "step";
 
      public static final Set<String> OPTION_KEYS = ImmutableSet.<String>of(OPTION_MIN, OPTION_MAX, OPTION_STEP);
 
     private static final Logger myLogger = LoggerFactory.getLogger(SpinnerProperty.class);
 
     private SpinnerModel model;
 
     private JSpinner spinner;
 
     private String currentText = "";
 
     public SpinnerProperty(Node parent, String id, String group) {
         this(parent, id, id, group);
     }
 
     public SpinnerProperty(Node parent, String id, String label, String group) {
         super(parent, label, group);
     }
     public SpinnerProperty(Node parent, String id) {
         this(parent, id, null);
     }
 
     @Override
     public JComponent getComponent() {
         return getSpinner();
     }
 
     @Override
     protected void reInitialize() {
 
 
         int min = Integer.parseInt(getOption(OPTION_MIN));
         int max = Integer.parseInt(getOption(OPTION_MAX));
         int step = Integer.parseInt(getOption(OPTION_STEP));
 
         int v = 0;
         try {
             v = (Integer)getSpinner().getValue();
             if ( v < min || v > max ) {
                 v = min;
             }
         } catch (Exception e) {
             myLogger.debug("Can't set spinner value: {}", e.getLocalizedMessage());
         }
 
         model = new SpinnerNumberModel(0, min, max, step);
 
         getSpinner().setModel(model);
     }
 
     @Override
     protected void lockUI(boolean lock) {
         getSpinner().setEnabled(!lock);
     }
 
     public JSpinner getSpinner() {
         if (spinner == null) {
            spinner = new JSpinner();
         }
         return spinner;
     }
 
     @Override
     public void setValue(String value) {
 
         int v = Integer.parseInt(value);
 
         String old = getSpinner().getValue().toString();
 
         getSpinner().getModel().setValue(v);
         valueChanged(old, value);
     }
 
     @Override
     public Set<String> getOptionKeys() {
         return OPTION_KEYS;
     }
 
 
 
 }
