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
 import com.google.inject.name.Named;
 
 public class AddNewSocialGroupHandler  implements HttpRequestHandler{
 	private final WolfPackLeader socialGroupsManager;
 	private final String hostName;
 	private final String port;
 	
 	@Inject
 	public AddNewSocialGroupHandler(WolfPackLeader socialGroupsManager, @Named("server.port") String port,
 			@Named("server.host.name") String hostName) {
 		this.socialGroupsManager = socialGroupsManager;
 		this.port = port;
 		this.hostName = hostName;
 	}
 
 	//XXX req of type POST with "/addSocialGroup" URI and body containing groupName=name
 	@Override
 	public void handle(HttpRequest req, HttpResponse res,
 			HttpContext context) throws HttpException, IOException {
		//TODO maybe we should use findOrCreateSocialGroup() method?
		socialGroupsManager.createSocialGroup(groupName);
 
 		//get group name from the body
 		String dataSet = EntityUtils.toString(((HttpEntityEnclosingRequest)req).getEntity());
 		String groupName = dataSet.substring(dataSet.indexOf("=") + 1).trim();
 
 		res.setStatusCode(HttpStatus.SC_SEE_OTHER);
 		//FIXME where to redirect?
 		//res.setHeader("Location", "/viewSocialGroups");
 		res.setHeader("Location", hostName + ":" + port + "/viewSelfProfile");
 	}
 
 }
