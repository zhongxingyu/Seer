 package api.requests;
 
 import api.entities.PollInstanceJSON;
 
 /*
  * { "pollinstance": [ ""
  */
 
 public class CreatePollInstanceRequest {
 	public static final Class EXECT_RESPONSE = CreatePollInstanceResponse.class;
 	public PollInstanceJSON pollInstance;
	public CreatePollInstanceRequest(pollInstance p) {
 		this.pollInstance = p;
 	}
 	
 }
