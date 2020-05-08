 package gui.common;
 
 import common.Result;
 /**
  * IModel defines functionality that is supported by all models in the program.
  */
 public interface IModel {
 
 	public boolean isSaved();
 
 	public boolean isValid();
 	/**
 	 * Validates if the model is in a savable state
 	 */
 	Result validate();
 
 	/**
 	 * If the Model has been validated then it must be saved.
 	 * This should always return a good result as you should validate before you save.
 	 *
 	 */
 	Result save();
 
}

