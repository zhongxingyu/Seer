 package api.requests;
 
import api.responses.ReadPollByTokenResponse;
 import api.responses.Response;
 
 /**
  * A request for the service: 
  */
 public class ReadPollByTokenRequest extends Request {
 	public String token;
 	public ReadPollByTokenRequest (String token) {
 		this.token = token;
 	}
 
 	@Override
 	public String getURL() {
 		return "/poll/token/" + token;
 	}
 
 	@Override
 	public Class<? extends Response> getExpectedResponseClass() {
		return ReadPollByTokenResponse.class;
 	}
 
 	@Override
 	public Method getHttpMethod() {
 		return Method.GET;
 	}
 }
