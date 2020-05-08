 package pt.inevo.nuxeo.youtube.operations;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.nuxeo.ecm.automation.core.Constants;
 import org.nuxeo.ecm.automation.core.annotations.Context;
 import org.nuxeo.ecm.automation.core.annotations.Operation;
 import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
 import org.nuxeo.ecm.automation.core.annotations.Param;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.platform.video.TranscodedVideo;
 import org.nuxeo.ecm.platform.video.VideoDocument;
 import org.nuxeo.runtime.api.Framework;
 
 import com.google.api.services.youtube.model.Video;
 import com.google.api.services.youtube.model.VideoContentDetails;
 import com.google.api.services.youtube.model.VideoSnippet;
 
 import pt.inevo.nuxeo.youtube.YouTubeClient;
 import pt.inevo.nuxeo.youtube.YouTubeService;
 
 
 /**
  * @author nelson.silva
  */
@Operation(id = Upload.ID, category = Constants.CAT_DOCUMENT, label = "UploadToYouTube", description = "This operation will upload given video document to YouTube")
 public class Upload {
 
 	public static final String ID = "UploadToYouTube";
 
 	@Context
 	public CoreSession coreSession;
 
 	@Param(name = "document")
 	public DocumentModel doc;
 
 
 	@Param(name = "oauthProvider")
 	public String serviceName;
 
 	@OperationMethod
 	public void run() throws Exception {
 
 	    YouTubeService youTubeService = Framework.getLocalService(YouTubeService.class);
 
 		YouTubeClient youtube = youTubeService.getYouTubeClient(coreSession.getPrincipal().getName());
 		VideoDocument videoDocument = doc.getAdapter(VideoDocument.class);
 
     	Video youtubeVideo = new Video();
 
     	VideoContentDetails contentDetails = new VideoContentDetails();
 		youtubeVideo.setContentDetails(contentDetails);
 
 		VideoSnippet snippet = new VideoSnippet();
 		snippet.setTitle(doc.getTitle());
 		List<String> tags = new ArrayList<String>();
 		snippet.setTags(tags);
 
 		youtubeVideo.setSnippet(snippet);
 
 		TranscodedVideo transcoded = videoDocument.getTranscodedVideo("video/mp4");
 
 		try {
 			youtube.upload(youtubeVideo, transcoded.getBlob().getStream(), transcoded.getBlob().getMimeType(), transcoded.getBlob().getLength());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 
 }
