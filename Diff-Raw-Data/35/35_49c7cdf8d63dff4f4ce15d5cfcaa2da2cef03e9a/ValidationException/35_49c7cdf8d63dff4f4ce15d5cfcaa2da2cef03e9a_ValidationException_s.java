 package org.dejava.component.validation.object;
 
 import java.util.Collections;
 import java.util.Set;
 
 import javax.ejb.ApplicationException;
 import javax.validation.ConstraintViolation;
 
 import org.dejava.component.exception.localized.unchecked.AbstractLocalizedRuntimeException;
 import org.dejava.component.i18n.message.handler.ApplicationMessageHandler;
 import org.dejava.component.i18n.message.handler.MessageHandler;
 import org.dejava.component.i18n.message.handler.impl.SimpleMessageHandler;
 
 /**
  * Exception related to constraint violations on a validation.
  */
 @ApplicationException(rollback = true)
 public class ValidationException extends AbstractLocalizedRuntimeException {
 
 	/**
 	 * Generated serial.
 	 */
 	private static final long serialVersionUID = 6656566241726366508L;
 
 	/**
 	 * Violations related to the exception.
 	 */
 	private Set<? extends ConstraintViolation<?>> violations;
 
 	/**
 	 * Gets the violations related to the exception.
 	 * 
 	 * @return The violations related to the exception.
 	 */
 	public Set<? extends ConstraintViolation<?>> getViolations() {
 		return violations;
 	}
 
 	/**
 	 * Sets the violations related to the exception.
 	 * 
 	 * @param violations
 	 *            New violations related to the exception.
 	 */
 	public void setViolations(final Set<? extends ConstraintViolation<?>> violations) {
 		this.violations = violations;
 	}
 
 	/**
 	 * The message handler to be used.
 	 */
 	private MessageHandler messageHandler;
 
 	/**
 	 * Gets the messageHandler.
 	 * 
 	 * @return The messageHandler.
 	 */
 	@Override
 	public MessageHandler getMessageHandler() {
 		// If the message handler is null.
 		if (messageHandler == null) {
 			// Creates a new simple message handler.
 			messageHandler = SimpleMessageHandler.getDefaultMessageHandler();
 		}
 		// Returns the message handler.
 		return messageHandler;
 	}
 
 	/**
 	 * Sets the messageHandler.
 	 * 
 	 * @param messageHandler
 	 *            New messageHandler.
 	 */
 	@Override
 	public void setMessageHandler(final MessageHandler messageHandler) {
 		this.messageHandler = messageHandler;
 	}
 
 	/**
 	 * Basic constructor.
 	 * 
 	 * @param violations
 	 *            Violations related to the exception.
 	 */
 	public ValidationException(final Set<? extends ConstraintViolation<?>> violations) {
 		super();
 		// Sets the violations.
 		this.violations = Collections.unmodifiableSet(violations);
 	}
 
 	/**
 	 * @see org.dejava.component.exception.localized.unchecked.AbstractLocalizedRuntimeException#addLocalizedMessages(org.dejava.component.i18n.message.handler.ApplicationMessageHandler)
 	 */
 	@Override
 	public void addLocalizedMessages(final ApplicationMessageHandler appMessageHandler) {
 		// If there are constraint violations.
 		if (getViolations() != null) {
 			// For each constraint violation.
 			for (final ConstraintViolation<?> currentViolation : getViolations()) {
 				// Adds the message for the current violation.
				new ViolationMessageCommand(currentViolation.getRootBeanClass(),
 						currentViolation.getConstraintDescriptor(), getLocale(),
 						currentViolation.getMessageTemplate()).addMessage(appMessageHandler);
 			}
 		}
 	}
 
 	/**
 	 * Throw a new validation exception for the given violations (if any are given).
 	 * 
 	 * @param violations
 	 *            Violations that have been fired.
 	 * @throws ValidationException
 	 *             If there are any given violations.
 	 */
 	public static void throwViolationExceptions(final Set<? extends ConstraintViolation<?>> violations)
 			throws ValidationException {
 		// If there are any given violations.
 		if ((violations != null) && (!violations.isEmpty())) {
 			// Throws a new validation exception.
 			throw new ValidationException(violations);
 		}
 	}
 
 }
