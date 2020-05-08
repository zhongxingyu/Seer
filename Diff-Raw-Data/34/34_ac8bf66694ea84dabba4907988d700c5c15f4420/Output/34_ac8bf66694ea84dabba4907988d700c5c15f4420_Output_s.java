 package edu.teco.dnd.blocks;
 
 import java.io.Serializable;
 
 /**
  * An output of a {@link FunctionBlock}.
  * 
  * @param <T>
  *            the type of data this Output outputs
  */
 public class Output<T extends Serializable> implements Serializable {
 	/**
 	 * Used for serialization.
 	 */
 	private static final long serialVersionUID = 8652366978996728530L;
 
 	private OutputTarget<? super T> target = null;
 
 	public void setValue(final T value) {
		target.setValue(value);
 	}
 
 	public synchronized boolean setTarget(final OutputTarget<? super T> target) {
 		if (this.target != null) {
 			return false;
 		}
 		this.target = target;
 		return true;
 	}
 }
