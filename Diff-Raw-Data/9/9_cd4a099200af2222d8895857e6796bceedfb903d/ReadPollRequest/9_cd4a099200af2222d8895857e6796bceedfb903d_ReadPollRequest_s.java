 package api.requests;
 
 import api.entities.PollJSON;
 import api.responses.ReadPollResponse;
 import api.responses.Response;
 
 public class ReadPollRequest extends Request {
 	public static final Class EXPECTED_RESPONSE = ReadPollResponse.class;
	public Long poll_id;
	public ReadPollRequest (Long l) {
		this.poll_id = l;
 	}
 
 	@Override
 	public String getURL() {
		return "/poll/" + poll_id;
 	}
 
 	@Override
 	public Class<? extends Response> getExpectedResponseClass() {
 		return ReadPollResponse.class;
 	}
 
 	@Override
 	public Method getHttpMethod() {
 		return Method.GET;
 	}
 }
