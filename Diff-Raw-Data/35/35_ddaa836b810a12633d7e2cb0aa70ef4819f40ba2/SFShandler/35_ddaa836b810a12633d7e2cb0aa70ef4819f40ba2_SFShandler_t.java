 package il.technion.ewolf.server.handlers;
 
 import il.technion.ewolf.server.ServerResources;
 import il.technion.ewolf.socialfs.Profile;
 import il.technion.ewolf.socialfs.SFSFile;
 import il.technion.ewolf.socialfs.SocialFS;
 import il.technion.ewolf.socialfs.UserID;
 import il.technion.ewolf.socialfs.UserIDFactory;
 import il.technion.ewolf.socialfs.exception.ProfileNotFoundException;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import org.apache.http.Consts;
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.protocol.HttpRequestHandler;
 
 import com.google.inject.Inject;
 
 public class SFShandler implements HttpRequestHandler {
 	private final SocialFS socialFS;
 	private final UserIDFactory userIDFactory;
 
 	@Inject
 	public SFShandler(SocialFS socialFS, UserIDFactory userIDFactory) {
 		this.socialFS = socialFS;
 		this.userIDFactory = userIDFactory;
 	}
 
 	@Override
 	public void handle(HttpRequest req, HttpResponse res,
 			HttpContext context) throws HttpException, IOException {
 		//TODO move adding server header to response intercepter
 		res.addHeader(HTTP.SERVER_HEADER, "e-WolfNode");
 		
 		String uri = req.getRequestLine().getUri();
 		try {
 			List<NameValuePair> parameters = 
					URLEncodedUtils.parse(new URI(uri).getRawQuery(), Consts.UTF_8);
 			String fileName = null;
 			String userID = null;
 			for (NameValuePair p : parameters) {
 				String name = p.getName();
 
 				if (name.equals("fileName")) {
 					fileName = p.getValue();
 				}
 				if (name.equals("userID")) {
 					userID = p.getValue();
 				}
 			}
			if (userID == null || fileName == null) {
 				//TODO reply bad request
 				return;
 			}
 			Profile profile;
 			try {
 				UserID uid = userIDFactory.getFromBase64(userID);
 				profile = socialFS.findProfile(uid);
 			} catch (ProfileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return;
 			}  catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return;
 			}
 			SFSFile sharedFolder = profile.getRootFile().getSubFile("sharedFolder");
			String strEntity = sharedFolder.getSubFile(fileName).getData().toString();
			String mimeType = ServerResources.getFileTypeMap().getContentType(fileName);
			res.setEntity(new StringEntity(strEntity, ContentType.create(mimeType, Consts.UTF_8)));
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 }
