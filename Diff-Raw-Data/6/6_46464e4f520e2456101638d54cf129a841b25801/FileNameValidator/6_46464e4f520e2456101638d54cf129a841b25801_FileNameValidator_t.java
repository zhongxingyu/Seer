 package de.ptb.epics.eve.editor.views.motoraxisview.filecomposite;
 
 import java.io.File;
 
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.databinding.validation.ValidationStatus;
 import org.eclipse.core.runtime.IStatus;
 
 /**
  * @author Marcus Michalsky
  * @since 1.7
  */
 public class FileNameValidator implements IValidator {
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public IStatus validate(Object value) {
 		String filename = String.valueOf(value);
 		File file = new File(filename);
 		if (filename.isEmpty()) {
 			return ValidationStatus.error("Providing a file name is mandatory!");
		} else if (!file.exists()) {
			return ValidationStatus.warning("File does not exist!");
 		}
 		return ValidationStatus.ok();
 	}
 }
