 package edu.teco.dnd.blocks;
 
 import java.io.Serializable;
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

 /**
  * An output of a {@link FunctionBlock}.
  * 
  * @param <T>
  *            the type of data this Output outputs
  */
 public class Output<T extends Serializable> implements Serializable {
	private static final Logger LOGGER = LogManager.getLogger(Output.class);
 	/**
 	 * Used for serialization.
 	 */
 	private static final long serialVersionUID = 8652366978996728530L;
 
 	private OutputTarget<? super T> target = null;
 
 	public void setValue(final T value) {
		if (target == null) {
			LOGGER.warn("called setValue before any targets were set up.");
		} else {
			target.setValue(value);
		}
 	}
 
 	public synchronized boolean setTarget(final OutputTarget<? super T> target) {
 		if (this.target != null) {
 			return false;
 		}
 		this.target = target;
 		return true;
 	}
 }
