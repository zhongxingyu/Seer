 package api.requests;
 
 import api.entities.PollInstanceJSON;
 import api.responses.ReadPollInstanceResponse;
 import api.responses.Response;
 
 public class ReadPollInstanceRequest extends Request {
 	public static final Class EXPECTED_RESPONSE = ReadPollInstanceResponse.class;
 	public Long pollInstance_id;
 	public ReadPollInstanceRequest (Long l) {
 		this.pollInstance_id = l;
 	}
 	
 	@Override
 	public String getURL() {
 		return "/pollinstance/" + pollInstance_id;
 	}
 
 	@Override
 	public Class<? extends Response> getExpectedResponseClass() {
 		return ReadPollInstanceResponse.class;
 	}
 
 	@Override
 	public Method getHttpMethod() {
 		return Method.GET;
 	}
 	
 }
