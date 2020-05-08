 //Copyright (C) 2011 Tomáš Vejpustek
 //Full copyright notice found in src/LICENSE.
 package exceptions;
 
 import java.util.ResourceBundle;
 
 /**
  * Exception with localized message. Each exception class has its own bundle <code>exceptions.msg</code> appended by class's name.
  * 
  * @author Tomáš Vejpustek
  *
  */
 @SuppressWarnings("serial")
 public abstract class LocalizedException extends Exception {
 	private String type;
 
 	/**
 	 * Specifies key of localized message.
 	 * 
 	 * @param type Key of localized message
 	 * @param message Internal error message
 	 */
 	public LocalizedException(String type, String message) {
 		super(message);
 		this.type = type;
 	}
 	
 	/**
 	 * Specifies key of localized message and cause of this exception.
 	 * 
 	 * @param type Key of localized message
 	 * @param message Internal error message
 	 * @param cause Cause of this exception
 	 */
 	public LocalizedException(String type, String message, Throwable cause) {
 		super(message,cause);
		this.type = type;
 	}
 	
 	/**
 	 * @return Resource bundle appropriate for the exception class.
 	 */
 	protected ResourceBundle getBundle() {
 		return ResourceBundle.getBundle("exceptions.msg" + getClass().getSimpleName());
 	}
 	
 	@Override
 	public String getLocalizedMessage() {
 		StringBuilder out = new StringBuilder(getBundle().getString(type));
 		if (getCause() != null) {
 			out.append('\n');
 			out.append(getCause().getLocalizedMessage());
 		}
 		return out.toString();
 	}
 }
