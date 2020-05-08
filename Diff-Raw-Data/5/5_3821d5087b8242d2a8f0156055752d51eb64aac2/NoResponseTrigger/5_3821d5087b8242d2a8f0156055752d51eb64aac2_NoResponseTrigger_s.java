 
 package axirassa.trigger;
 
 import lombok.Getter;
 
import org.apache.commons.httpclient.NoHttpResponseException;
 
 public class NoResponseTrigger extends AbstractTrigger {
 	@Getter
 	private NoHttpResponseException cause;

 
 	public NoResponseTrigger (NoHttpResponseException cause) {
 		this.cause = cause;
 	}
 }
