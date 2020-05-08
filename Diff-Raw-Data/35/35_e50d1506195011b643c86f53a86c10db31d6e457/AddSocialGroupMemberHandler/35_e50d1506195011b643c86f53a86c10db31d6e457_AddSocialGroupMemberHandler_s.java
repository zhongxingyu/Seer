 package il.technion.ewolf.server;
 
 import il.technion.ewolf.WolfPack;
 import il.technion.ewolf.WolfPackLeader;
 import il.technion.ewolf.socialfs.Profile;
 import il.technion.ewolf.socialfs.SocialFS;
 import il.technion.ewolf.socialfs.UserID;
 import il.technion.ewolf.socialfs.UserIDFactory;
 import il.technion.ewolf.socialfs.exception.ProfileNotFoundException;
 import il.technion.ewolf.stash.exception.GroupNotFoundException;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.http.HttpEntityEnclosingRequest;
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.entity.FileEntity;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.protocol.HttpRequestHandler;
 import org.apache.http.util.EntityUtils;
 
 import com.google.inject.Inject;
 
 public class AddSocialGroupMemberHandler implements HttpRequestHandler {
 	private final WolfPackLeader socialGroupsManager;
 	private final SocialFS socialFS;
 	private final UserIDFactory userIDFactory;
 	
 	@Inject
 	public AddSocialGroupMemberHandler(SocialFS socialFS, WolfPackLeader socialGroupsManager,
 			UserIDFactory userIDFactory) {
 		this.socialGroupsManager = socialGroupsManager;
 		this.socialFS = socialFS;
 		this.userIDFactory = userIDFactory;
 	}
 
	//XXX req of type POST with "/addSocialGroupMember/{groupName}" URI and body containing user ID
 	@Override
 	public void handle(HttpRequest req, HttpResponse res,
 			HttpContext context) throws HttpException, IOException {
 		//TODO move adding headers to response intercepter
 		res.addHeader("Server", "e-WolfNode");
 		res.addHeader("Content-Type", "application/json");
 		
		//get user ID from body
		String strUid = EntityUtils.toString(((HttpEntityEnclosingRequest)req).getEntity());
		UserID uid = userIDFactory.getFromBase64(strUid);
 
 		Profile profile = null;
 		try {
 			profile = socialFS.findProfile(uid);			
 		} catch (ProfileNotFoundException e) {
 			// TODO Auto-generated catch block
 			System.out.println("Profile with UserID " + uid + "not found");
 			res.setStatusCode(HttpStatus.SC_NOT_FOUND);
 			res.setEntity(new FileEntity(new File("404.html"),"text/html"));
 			return;
 		}
 		
 		//get social group name from URI
 		String reqURI = req.getRequestLine().getUri();
 		String[] splitedURI = reqURI.split("/");
 		String groupName = splitedURI[splitedURI.length-1];
 		
 		WolfPack socialGroup = socialGroupsManager.findSocialGroup(groupName);
 		if (socialGroup == null) {
 			res.setStatusCode(HttpStatus.SC_BAD_REQUEST);
 			return;
 		}
 		
 		try {
 			socialGroup.addMember(profile);
 		} catch (GroupNotFoundException e) {
 			System.out.println("Social group" + groupName + "not found");
 			res.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return;
 		}
 		
 		//TODO what should be in response?
 		res.setStatusCode(HttpStatus.SC_SEE_OTHER);
 		//FIXME where to redirect?
 		res.setHeader("Location", "/viewSocialGroupMembers/" + groupName);
 	}
 
 }
