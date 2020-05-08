 package com.operativus.senacrs.audit.graph.edges.webdriver;
 
 import com.operativus.senacrs.audit.exceptions.ExceptionMessagesKeyEnum;
 import com.operativus.senacrs.audit.graph.nodes.Node;
 import com.operativus.senacrs.audit.graph.nodes.webdriver.WebDriverNode;
 import com.operativus.senacrs.audit.properties.PropertyKey;
 import com.operativus.senacrs.audit.properties.messages.MessagesCentral;
 
 @SuppressWarnings("serial")
 public class IllegalSourceNodeClassException
 		extends IllegalArgumentException {
 
 	public IllegalSourceNodeClassException(final Node source) {
 
 		super(getMessage(source));
 	}
 
 	private static String getMessage(final Node source) {
 
 		String result = null;
 		PropertyKey key = null;
 
 		key = ExceptionMessagesKeyEnum.ILLEGAL_NODE_CLASS;
 		result = MessagesCentral.getMessage(key, source.getClass(), WebDriverNode.class);
 
 		return result;
 	}
 
 	public IllegalSourceNodeClassException(final Throwable cause, final Node source) {
 
 		super(getMessage(source), cause);
 	}
 }
