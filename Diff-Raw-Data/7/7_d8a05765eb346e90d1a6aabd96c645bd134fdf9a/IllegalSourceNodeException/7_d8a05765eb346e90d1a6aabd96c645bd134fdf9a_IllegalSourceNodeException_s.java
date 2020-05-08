 package com.operativus.senacrs.audit.graph.edges;
 
 import java.util.Arrays;
 
 import com.operativus.senacrs.audit.exceptions.ExceptionMessagesKeyEnum;
 import com.operativus.senacrs.audit.exceptions.RuntimeExceptionFactory;
 import com.operativus.senacrs.audit.graph.nodes.Node;
 import com.operativus.senacrs.audit.properties.PropertyKey;
 import com.operativus.senacrs.audit.properties.messages.MessagesCentral;
 
@SuppressWarnings("serial")
 public class IllegalSourceNodeException
 		extends IllegalArgumentException {
 
 	public IllegalSourceNodeException(final Node source, final Node... valid) {
 
 		super(getMessage(source, valid));
 	}
 
 	private static String getMessage(final Node source, final Node... valid) {
 
 		String result = null;
 		PropertyKey key = null;
 
 		if (source == null) {
 			throw RuntimeExceptionFactory.getNullArgumentException("source");
 		}
 		key = ExceptionMessagesKeyEnum.ILLEGAL_NODE_START;
 		result = MessagesCentral.getMessage(key, source, Arrays.toString(valid));
 
 		return result;
 	}
 
 	public IllegalSourceNodeException(final Throwable cause, final Node source, final Node... valid) {
 
 		super(getMessage(source, valid), cause);
 	}
 }
