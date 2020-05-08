 package de.ptb.epics.eve.editor.views.scanmoduleview;
 
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.databinding.validation.ValidationStatus;
 import org.eclipse.core.runtime.IStatus;
 
 /**
  * @author Marcus Michalsky
  * @since 1.8
  */
 public class TriggerDelaySettleTimeValidator implements IValidator {
 
 	private final String message;
 	
 	/**
 	 * @param message the error message
 	 */
 	public TriggerDelaySettleTimeValidator(String message) {
 		this.message = message;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public IStatus validate(Object value) {
 		String val = (String)value;
 		if (val.isEmpty()) {
 			return ValidationStatus.ok();
 		}
 		try {
 			double d = Double.parseDouble(val);
			if (d < 0.0) {
 				return ValidationStatus.error(this.message + 
						" must be non negative!");
 			}
 			return ValidationStatus.ok();
 		} catch (NumberFormatException e) {
 			return ValidationStatus.error("cannot parse double.");
 		}
 	}
 }
