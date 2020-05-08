 package il.technion.ewolf.server;
 
 import il.technion.ewolf.WolfPackLeader;
 import java.io.IOException;
 
 import org.apache.http.HttpEntityEnclosingRequest;
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.protocol.HttpRequestHandler;
 import org.apache.http.util.EntityUtils;
 
 import com.google.inject.Inject;
 
 public class AddNewSocialGroupHandler  implements HttpRequestHandler{
 	private final WolfPackLeader socialGroupsManager;
 	
 	@Inject
 	public AddNewSocialGroupHandler(WolfPackLeader socialGroupsManager) {
 		this.socialGroupsManager = socialGroupsManager;
 	}
 
	//XXX req of type POST with "/addSocialGroup" URI and body containing groupName
 	@Override
 	public void handle(HttpRequest req, HttpResponse res,
 			HttpContext context) throws HttpException, IOException {
		String groupName = EntityUtils.toString(((HttpEntityEnclosingRequest)req).getEntity());
 		//TODO maybe we should use findOrCreateSocialGroup() method?
 		socialGroupsManager.createSocialGroup(groupName);
 
 		//TODO what should be in response?
 		res.setStatusCode(HttpStatus.SC_SEE_OTHER);
 		//FIXME where to redirect?
 		res.setHeader("Location", "/viewSocialGroups");
 	}
 
 }
