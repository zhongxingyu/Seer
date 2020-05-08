 package at.fhv.audioracer.communication.player.message;
 
 public class UpdateVelocityMessage extends PlayerMessage {
 	
 	public float speed;
 	public float direction;
 	public int seqNr;
 	private static int _seqNr;
 	
 	public UpdateVelocityMessage() {
 		super(MessageId.UPDATE_VELOCITY);
		if (++_seqNr < 0) {
			_seqNr = 0;
		}
		seqNr = _seqNr;
 	}
 	
 }
