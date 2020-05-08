 package niagara.utils;
 
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
	public Punctuation getPunctuation() {
		// TODO: assert this call occurs iff type is feedback
		return _punctuation;
 	}
 
 	/***
 	 * 
 	 * @return Control Message object
 	 */
	public ControlMessage getControlMessage() {
		// TODO: assert this call occurs iff type is control
 
		return _controlMessage;
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
 
 
