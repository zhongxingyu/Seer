 package com.google.gwt.ddmvc.model.update;
 
 /**
  * Update to initialize or reset a model's associated value 
  * @author Kevin Dolan
  */
 public class SetValue extends ModelUpdate {
 	
 	/**
 	 * The default SetValue field, used for comparison
 	 */
 	public static final SetValue DEFAULT = new SetValue(null, null);
 	
 	private Object data;
 	
 	/**
 	 * @param target
 	 * @param data
 	 */
 	public SetValue(String target, Object data) {
 		super(target);
		this.data = data;
 	}
 
 	@Override
 	protected Object performUpdate(Object value) {
 		return data;
 	}
 }
