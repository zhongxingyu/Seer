 package com.operativus.senacrs.audit.graph.nodes.webdriver;
 
 import com.operativus.senacrs.audit.exceptions.ExceptionMessagesKeyEnum;
 import com.operativus.senacrs.audit.exceptions.RuntimeExceptionFactory;
 import com.operativus.senacrs.audit.properties.PropertyKey;
 import com.operativus.senacrs.audit.properties.messages.MessagesCentral;
 
 @SuppressWarnings("serial")
 public class IllegalNodeTypeException
 		extends IllegalArgumentException {
 
	public IllegalNodeTypeException(final WebDriverNodeTypeEnum type) {
 
 		super(getMessage(type));
 	}
 
	private static String getMessage(final WebDriverNodeTypeEnum type) {
 
 		String result = null;
 		PropertyKey key = null;
 
 		if (type == null) {
 			throw RuntimeExceptionFactory.getNullArgumentException("source");
 		}
 		key = ExceptionMessagesKeyEnum.ILLEGAL_NODE_TYPE;
 		result = MessagesCentral.getMessage(key, type);
 
 		return result;
 	}
 
 	public IllegalNodeTypeException(final Throwable cause, final WebDriverNodeTypeEnum type) {
 
 		super(getMessage(type), cause);
 	}
 }
