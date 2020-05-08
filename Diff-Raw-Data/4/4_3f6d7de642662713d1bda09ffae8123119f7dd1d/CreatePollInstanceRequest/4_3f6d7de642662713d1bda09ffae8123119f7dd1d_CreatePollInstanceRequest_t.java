 package api.requests;
 
import models.PollInstance;
 import api.entities.PollInstanceJSON;
 
 /*
  * { "pollinstance": [ ""
  */
 
 public class CreatePollInstanceRequest {
 	public static final Class EXECT_RESPONSE = CreatePollInstanceResponse.class;
 	public PollInstanceJSON pollInstance;
	public CreatePollInstanceRequest(PollInstance p) {
 		this.pollInstance = p;
 	}
 	
 }
