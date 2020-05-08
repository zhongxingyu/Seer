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
import org.apache.http.entity.SerializableEntity;
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
					URLEncodedUtils.parse(new URI(uri).getQuery(),Consts.UTF_8);
 			String fileName = null;
 			String userID = null;
			String ext = null;
 			for (NameValuePair p : parameters) {
 				String name = p.getName();
 
 				if (name.equals("fileName")) {
 					fileName = p.getValue();
					String[] splitedFileName = fileName.split("\\.");
					ext = splitedFileName[splitedFileName.length-1];
 				}
 				if (name.equals("userID")) {
 					userID = p.getValue();
 				}
 			}
			if (userID == null || fileName == null || ext == null) {
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
			res.addHeader(HTTP.CONTENT_TYPE, ServerResources.getFileTypeMap().getContentType(fileName));
			//TODO work for utf-8?
			res.setEntity(new SerializableEntity(sharedFolder.getSubFile(fileName).getData(), false));
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 }
