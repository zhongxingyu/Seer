 package niagara.utils;
 
import niagara.exception.*;

 /***
  * 
  * @author rfernand
  * 
  * FeedbackMessage is an object sent contrary to stream direction. 
  * Objects sent as feedback can be control messages or punctuation.
  *
  */
 
 public class FeedbackMessage {
 
 	// Fields
 	private FeedbackType _type;
 	private Punctuation _punctuation;
 	private ControlMessage _controlMessage; 
 
 	/***
 	 * Types of feedback supported
 	 * CONTROL - Control Messages
 	 * ASSUMED - Assumed Punctuation
 	 *
 	 */
 	public enum FeedbackType {CONTROL, ASSUMED};

 	// Properties
 	/***
 	 * @return Feedback type
 	 */
 	public FeedbackType getType() {
 		return _type;
 	}
 	/***
 	 * 
 	 * @return Feedback punctuation object
 	 */
	public Punctuation getPunctuation() throws FeedbackException {

		if (_type == FeedbackType.CONTROL) {
			throw new FeedbackException("Requested punctuation from a Control Message.");
		} else {
			return _punctuation;
		}
 	}
 
 	/***
 	 * 
 	 * @return Control Message object
 	 */
	public ControlMessage getControlMessage() throws FeedbackException{
		if(_type != FeedbackType.CONTROL) {
			throw new FeedbackException("Requested control message from a Feedback Punctuation.");
		} else {
 
			return _controlMessage;
		}
 	}
 
 	// Ctor
 
 	/***
 	 * @param t Feedback type
 	 * @param p Feedback punctuation
 	 */
 	public FeedbackMessage(FeedbackType t, Punctuation p) {
 		_type = t;
 		_punctuation = p;
 	}
 
 	/***
 	 * 
 	 * @param t Feedback type
 	 * @param c Control Message
 	 */
 	public FeedbackMessage(FeedbackType t, ControlMessage c) {
 		_type = t;
 		_controlMessage = c;
 	}
 }
 
 
