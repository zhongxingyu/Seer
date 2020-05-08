 package org.dejava.component.faces.message;
 
 import java.util.Locale;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.application.FacesMessage.Severity;
 import javax.faces.context.FacesContext;
 
 import org.dejava.component.exception.localized.unchecked.InvalidParameterException;
 import org.dejava.component.faces.message.annotation.MessageType;
 import org.dejava.component.faces.message.constant.ParamKeys;
 import org.dejava.component.i18n.message.exception.MessageNotFoundException;
 import org.dejava.component.i18n.message.handler.ApplicationMessageHandler;
 import org.dejava.component.i18n.message.handler.MessageHandler;
 import org.dejava.component.reflection.AnnotationMirror;
 import org.dejava.component.reflection.ClassMirror;
 import org.dejava.component.validation.method.PreConditions;
 
 /**
  * Java server faces message handler.
  */
 public class FacesMessageHandler implements ApplicationMessageHandler {
 
 	/**
 	 * Generated serial.
 	 */
 	private static final long serialVersionUID = 4989446047854032562L;
 
 	/**
 	 * Decorated message handler.
 	 */
 	private final MessageHandler messageHandler;
 
 	/**
 	 * The faces context to be used.
 	 */
 	private final FacesContext facesContext;
 
 	/**
 	 * @see org.dejava.component.i18n.message.handler.MessageHandler#getMessage(java.lang.Object,
 	 *      java.util.Locale, java.lang.String, java.lang.Object[])
 	 */
 	@Override
 	public String getMessage(final Object type, final Locale locale, final String key,
 			final Object[] parametersValues) throws MessageNotFoundException, InvalidParameterException {
 		return messageHandler.getMessage(type, locale, key, parametersValues);
 	}
 
 	/**
 	 * Gets the message severity for the given type. If no severity is found, the error severity is used.
 	 * 
 	 * @param type
 	 *            Type of the message.
 	 * @return The message severity for the given type.
 	 */
 	private Severity getMessageSeverity(final Object type) {
 		// By default, the message severity is error.
 		Severity severity = FacesMessage.SEVERITY_ERROR;
 		// Gets the class of the message type.
		final ClassMirror<Object> messageTypeClass = new ClassMirror<>(type.getClass());
		//
 		final AnnotationMirror<MessageType> messageTypeInfo = messageTypeClass
 				.getAnnotation(MessageType.class);
 		// If the message type annotation is found.
 		if (messageTypeInfo != null) {
 			// Depending on the annotation severity.
 			switch (messageTypeInfo.getReflectedAnnotation().severity()) {
 			// If the severity is info.
 			case INFO:
 				// Updates the severity to be returned.
 				severity = FacesMessage.SEVERITY_INFO;
 				break;
 			// If the severity is warn.
 			case WARN:
 				// Updates the severity to be returned.
 				severity = FacesMessage.SEVERITY_WARN;
 				break;
 			// If the severity is fatal.
 			case FATAL:
 				// Updates the severity to be returned.
 				severity = FacesMessage.SEVERITY_FATAL;
 				break;
 			// By default, the severity is error.
 			default:
 				// Updates the severity to be returned.
 				severity = FacesMessage.SEVERITY_ERROR;
 				break;
 			}
 		}
 		// Returns the message severity.
 		return severity;
 	}
 
 	/**
 	 * @see org.dejava.component.i18n.message.handler.ApplicationMessageHandler#addMessage(java.lang.Object,
 	 *      java.util.Locale, java.lang.String, java.lang.Object[])
 	 */
 	@Override
 	public String addMessage(final Object type, final Locale locale, final String key,
 			final Object[] parametersValues) throws MessageNotFoundException, InvalidParameterException {
 		// Gets the message.
 		final String message = getMessage(type, locale, key, parametersValues);
 		// Adds the message to the faces context. TODO Think about summary.
 		facesContext.addMessage(null, new FacesMessage(getMessageSeverity(type), message, message));
 		// Returns the message.
 		return message;
 	}
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param messageHandler
 	 *            Message handler to be decorated.
 	 * @param facesContext
 	 *            The faces context to be used.
 	 * 
 	 */
 	public FacesMessageHandler(final MessageHandler messageHandler, final FacesContext facesContext) {
 		// Assert that the given parameters are not null.
 		PreConditions.assertParamNotNull(ParamKeys.MESSAGE_HANDLER, messageHandler);
 		PreConditions.assertParamNotNull(ParamKeys.FACES_CONTEXT, facesContext);
 		// Sets the fields.
 		this.messageHandler = messageHandler;
 		this.facesContext = facesContext;
 	}
 
 }
