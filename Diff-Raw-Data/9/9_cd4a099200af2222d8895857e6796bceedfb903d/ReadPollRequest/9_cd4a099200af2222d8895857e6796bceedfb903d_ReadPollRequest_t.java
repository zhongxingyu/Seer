 package api.requests;
 
 import api.entities.PollJSON;
 import api.responses.ReadPollResponse;
 import api.responses.Response;
 
 public class ReadPollRequest extends Request {
 	public static final Class EXPECTED_RESPONSE = ReadPollResponse.class;
	public String poll_token;
	public ReadPollRequest (String s) {
		this.poll_token = s;
 	}
 
 	@Override
 	public String getURL() {
		return "/poll/" + poll_token;
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
