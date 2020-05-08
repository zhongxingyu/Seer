 package pt.ist.vaadinframework.data.hints;
 
 import pt.ist.vaadinframework.data.HintedProperty.Hint;
 
 import com.vaadin.ui.AbstractTextField;
 import com.vaadin.ui.Field;
 import com.vaadin.ui.PasswordField;
 
 public class Password implements Hint {
     @Override
     public Field applyHint(Field field) {
 	PasswordField password = new PasswordField();
 	HintTools.copyConfiguration(field, password);
	return field;
     }
 
     @Override
     public boolean appliesTo(Field field) {
 	return field instanceof AbstractTextField;
     }
 }
