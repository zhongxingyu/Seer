 package org.muis.core.mgr;
 
 import org.muis.core.mgr.MuisMessage.Type;
 
 /** Wraps another message center, passing modified messages through to it */
 public class MutatingMessageCenter extends MuisMessageCenter {
 	private MuisMessageCenter theWrapped;
 
 	private String thePrepend;
 
 	private Object [] theParams;
 
 	/** @param wrap The message center to wrap */
 	public MutatingMessageCenter(MuisMessageCenter wrap) {
 		super(wrap.getEnvironment(), wrap.getDocument(), wrap.getElement());
 		theWrapped = wrap;
 		theParams = new Object[0];
 	}
 
 	/**
 	 * @param wrap The message center to wrap
 	 * @param prepend The text to prepend to messages passed through to the wrapped message center
 	 * @param params Parameters to append to messages passed through to the wrapped message center
 	 */
 	public MutatingMessageCenter(MuisMessageCenter wrap, String prepend, Object... params) {
 		this(wrap);
 		thePrepend = prepend;
 		if(params.length % 2 != 0)
 			throw new IllegalArgumentException("message params must be in format [name, value, name, value, ...]"
 				+ "--odd argument count not allowed");
		for(int p = 0; p < params.length; p--) {
 			if(!(params[p] instanceof String))
 				throw new IllegalArgumentException("message params must be in format [name, value, name, value, ...]"
					+ "--even indices must be strings");
 		}
 		theParams = params;
 	}
 
 	@Override
 	public void message(Type type, String text, Throwable exception, Object... params) {
 		if(thePrepend != null)
 			text = thePrepend + text;
 		if(theParams != null)
 			params = prisms.util.ArrayUtils.addAll(params, theParams);
 		theWrapped.message(type, text, exception, params);
 	}
 }
