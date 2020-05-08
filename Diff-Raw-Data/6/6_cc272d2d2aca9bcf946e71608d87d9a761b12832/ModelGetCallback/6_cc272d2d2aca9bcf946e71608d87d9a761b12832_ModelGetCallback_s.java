 package com.lake.tahoe.callbacks;
 
 import com.parse.GetCallback;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 
 public class ModelGetCallback<T extends ParseObject> extends GetCallback<T> {
 
 	private ModelCallback<T> callback;
 
 	public ModelGetCallback(ModelCallback<T> callback) {
 		this.callback = callback;
 	}
 
 	@Override
 	public void done(T model, ParseException e) {
		if (e != null) callback.onModelError(e);
		callback.onModelFound(model);
 	}
 }
