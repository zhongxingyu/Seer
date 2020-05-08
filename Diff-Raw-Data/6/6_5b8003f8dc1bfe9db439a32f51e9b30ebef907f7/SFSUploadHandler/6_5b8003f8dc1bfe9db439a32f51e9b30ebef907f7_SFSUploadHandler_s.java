 package il.technion.ewolf.server.handlers;
 
 import il.technion.ewolf.ewolf.WolfPack;
 import il.technion.ewolf.ewolf.WolfPackLeader;
 import il.technion.ewolf.server.jsonDataHandlers.EWolfResponse;
 import il.technion.ewolf.socialfs.Profile;
 import il.technion.ewolf.socialfs.SFSFile;
 import il.technion.ewolf.socialfs.SocialFS;
 import il.technion.ewolf.stash.Group;
 import il.technion.ewolf.stash.exception.GroupNotFoundException;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.http.Consts;
 import org.apache.http.HttpEntityEnclosingRequest;
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
 import org.apache.http.util.EntityUtils;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.inject.Inject;
 
 import static il.technion.ewolf.server.jsonDataHandlers.EWolfResponse.*;
 
 public class SFSUploadHandler implements HttpRequestHandler {
 	private static final int FILENAME_LENGTH = 10;
 	private final SocialFS socialFS;
 	private final WolfPackLeader socialGroupsManager;
 
 	@Inject
 	public SFSUploadHandler(SocialFS socialFS, WolfPackLeader socialGroupsManager) {
 		this.socialFS = socialFS;
 		this.socialGroupsManager = socialGroupsManager;
 	}
 
 	class SFSUploadHandlerResponse extends EWolfResponse {
 		String path;
 		public SFSUploadHandlerResponse(String result, String path) {
 			super(result);
 			this.path = path;
 		}
 	}
 
 	@Override
 	public void handle(HttpRequest req, HttpResponse res,
 			HttpContext context) throws HttpException, IOException {
 		//TODO move adding server header to response intercepter
 		res.addHeader(HTTP.SERVER_HEADER, "e-WolfNode");
 		
 		String uri = req.getRequestLine().getUri();
 
 		Profile profile = socialFS.getCredentials().getProfile();
 		String wolfpackName = null;
 		String ext = null;
 		String resFileName;
 		try {
 			List<NameValuePair> parameters = 
 					URLEncodedUtils.parse(new URI(uri).getQuery(),Consts.UTF_8);
 			for (NameValuePair v : parameters) {
 				String name = v.getName();
 
 				if (name.equals("fileName")) {
 					String fileName = v.getValue();
					String[] splitedFileName = fileName.split(".");
					ext = splitedFileName[splitedFileName.length];
 				}
 				if (name.equals("wolfpackName")) {
 					wolfpackName = v.getValue();
 				}
 			}
 			if (ext == null || wolfpackName == null) {
 				setResponse(res, null, RES_BAD_REQUEST);
 				return;
 			}
 
 			String fileData = EntityUtils.toString(((HttpEntityEnclosingRequest)req).getEntity());
 
 			SFSFile sharedFolder = profile.getRootFile().getSubFile("sharedFolder");
 			//create unique file name
 			while (true) {
 				resFileName = RandomStringUtils.randomAlphanumeric(FILENAME_LENGTH) + "." + ext;
 				try {
 					sharedFolder.getSubFile(resFileName);
 				} catch (FileNotFoundException e) {
 					break;
 				}
 			}
 
 			SFSFile file = socialFS.getSFSFileFactory().createNewFile()
 					.setName(resFileName)
 					.setData(fileData);
 			WolfPack sharedSocialGroup = socialGroupsManager.findSocialGroup(wolfpackName);
 			if (sharedSocialGroup == null) {
 				setResponse(res, null, RES_BAD_REQUEST);
 				return;
 			}
 			Group group = sharedSocialGroup.getGroup();
 			sharedFolder.append(file, group);
 			System.out.println(sharedFolder.getSubFile(resFileName).getData());
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 			setResponse(res, null, RES_BAD_REQUEST);
 			return;
 		} catch (GroupNotFoundException e) {
 			e.printStackTrace();
 			setResponse(res, null, RES_INTERNAL_SERVER_ERROR);
 			return;
 		}
 
 		String path = "/sfs?userID=" + profile.getUserId().toString() + "&fileName=" + resFileName;
 		setResponse(res, path, RES_SUCCESS);
 	}
 
 	private void setResponse(HttpResponse res, String path, String result) {
 		SFSUploadHandlerResponse resObj = new SFSUploadHandlerResponse(result, path);
 		Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
 		String json = gson.toJson(resObj);
 		res.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
 	}
 }
