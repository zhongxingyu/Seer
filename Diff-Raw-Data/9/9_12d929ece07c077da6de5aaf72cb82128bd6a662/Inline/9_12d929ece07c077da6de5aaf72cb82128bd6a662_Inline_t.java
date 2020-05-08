 package pt.ist.vaadinframework.data.hints;
 
 import pt.ist.vaadinframework.data.HintedProperty.Hint;
 
 import com.vaadin.ui.DateField;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.InlineDateField;
 
 public class Inline implements Hint {
     @Override
     public Field applyHint(Field field) {
 	InlineDateField inlined = new InlineDateField();
 	HintTools.copyConfiguration(field, inlined);
	return inlined;
     }
 
     @Override
     public boolean appliesTo(Field field) {
 	return field instanceof DateField;
     }
 }
