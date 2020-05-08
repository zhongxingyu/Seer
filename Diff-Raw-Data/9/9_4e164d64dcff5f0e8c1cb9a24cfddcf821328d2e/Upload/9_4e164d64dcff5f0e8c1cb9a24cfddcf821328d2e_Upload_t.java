 package keen.server;
 
 import java.util.Map;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import java.util.logging.Logger;
 
 import com.google.appengine.api.datastore.Text;
 import com.google.appengine.api.datastore.Rating;
 
 import java.util.Date;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import keen.shared.*;
 
 public class Upload extends HttpServlet {
 	public static final Logger log = Logger.getLogger(Upload.class.getName());
 	private BlobstoreService blobServ = BlobstoreServiceFactory.getBlobstoreService();
 
 	public final int DATA = 0;
 	public final int ART = 1;
 
 
 	public void doPost(HttpServletRequest req,HttpServletResponse res) throws IOException, ServletException {
 		if ( req.getParameter("content").equals("image")) {
 			uploadImage(req,res);
 
 		} else if ( req.getParameter("content").equals("music")) {
 			uploadMusic(req,res);
 
 		} else if ( req.getParameter("content").equals("video")) {
 			uploadVideo(req,res);
 
 		} else {
			log.info("invalid content type");
 
 		}
 		/*
 		Map<String,BlobKey> blobs = blobServ.getUploadedBlobs(req);
 		BlobKey[] blobKeys = new BlobKey[2];
 		blobKeys[DATA] = blobs.get("myfile");
 		blobKeys[ART]  = blobs.get("art");
 		UserService us = UserServiceFactory.getUserService();
 		User fred = us.getCurrentUser();
 		if (blobKey == null || fred == null) {
 			res.sendRedirect("/");
 			return;
 		} 
 		log.info("User : " + fred);
 		DAO oby = new DAO();
 		Image image = createImage(req,blobKey,fred);
 		oby.ofy().put(image);
 		
 		res.sendRedirect("/serve?blob-key=" + blobKey.getKeyString());
 		*/
 
 	}
 
 	private void uploadVideo(HttpServletRequest req,HttpServletResponse res) throws IOException, ServletException {
 
 	}
 
 	private void uploadMusic(HttpServletRequest req,HttpServletResponse res) throws IOException, ServletException {
 		Map<String,BlobKey> blobs = blobServ.getUploadedBlobs(req);
 		BlobKey[] blobKeys = new BlobKey[2];
		blobKeys[DATA] = blobs.get("myFile");
 		blobKeys[ART]  = blobs.get("art");
 		UserService us = UserServiceFactory.getUserService();
 		User fred = us.getCurrentUser();
 		if (blobKeys[DATA] == null || fred == null) {
 			res.sendRedirect("/");
 			return;
 		}
 		log.info("User : " + fred);
 		DAO oby = new DAO();
 		Music music = createMusic(req,blobKeys,fred);
 		oby.ofy().put(music);
 		
 		res.sendRedirect("/serve?blob-key=" + blobKeys[DATA].getKeyString());
 
 	}
 
 	private void uploadImage(HttpServletRequest req,HttpServletResponse res) throws IOException, ServletException {
 		Map<String,BlobKey> blobs = blobServ.getUploadedBlobs(req);
		BlobKey blobKey = blobs.get("myFile");
 		UserService us = UserServiceFactory.getUserService();
 		User fred = us.getCurrentUser();
		log.info("User : " + fred);
 		if (blobKey == null || fred == null) {
 			res.sendRedirect("/");
 			return;
 		} 
 		log.info("User : " + fred);
 		DAO oby = new DAO();
 		Image image = createImage(req,blobKey,fred);
 		oby.ofy().put(image);
 		
 		res.sendRedirect("/serve?blob-key=" + blobKey.getKeyString());
 
 	}
 
 
 	private String[] parseTags(String stags) {
 		if (stags == null)
 			return null;
 		String[] tags = stags.split(";");
 		return tags;
 	}
 
 	private Rating parseRating(String srating) {
 		if (srating == null)
 			return null;
 		try {
 			int r = Integer.parseInt(srating);
 			return new Rating(r);
 		} catch(NumberFormatException e) {
 			log.info("Invalid rating passed, setting to null");
 		}
 		return null;
 	}
 
 	public int tryParseInt(String srating) {
 		try {
 			int r = Integer.parseInt(srating);
 			if ( r > 0)
 				return r;
 
 		} catch(NumberFormatException e) {
 			log.info("Invalid rating passed, setting to 0");
 		}
 			log.info("invalid length");
 			return 0;
 
 	}
 
 	//Assumed that all paramters are not null
 	public Image createImage(HttpServletRequest req,BlobKey blobKey,User fred) {
 
 		String owner = fred.getUserId();
 
 		String title = req.getParameter("title");
 
 		String artist = req.getParameter("artist");
 		log.info("Artist : " + artist);
 
 		Rating rating = parseRating(req.getParameter("rating"));
 
 		String[] tags = parseTags(req.getParameter("tags"));
 
 		log.info("Tag : " + tags);
 		Text comment = null;
 		if (req.getParameter("comment")!=null) {
 			comment = new Text(req.getParameter("comment"));
 		}
 		log.info("BlobKey : " + blobKey.toString());
 		Image image = new Image(owner,title,artist,blobKey,rating,tags,comment,new Date());
 
 		return image;
 	}
 
 	public Music createMusic(HttpServletRequest req,BlobKey[] blobKey,User fred) {
 
 		String owner = fred.getUserId();
 		
 		int length = tryParseInt(req.getParameter("length"));
 		
 		String songName = req.getParameter("songName");
 
 		String artist = req.getParameter("artist");
 		log.info("Artist : " + artist);
 
 		String genre = req.getParameter("genre");
 		
 		log.info("BlobKey : " + blobKey.toString());
 		
 		Rating rating = parseRating(req.getParameter("rating"));
 
 		String[] tags = parseTags(req.getParameter("tags"));
 
 		log.info("Tag : " + tags);
 		Text comment = null;
 		if (req.getParameter("comment")!=null) {
 			comment = new Text(req.getParameter("comment"));
 		}
 		
 		int trackNum = tryParseInt(req.getParameter("trackNum"));
 		int discNum = tryParseInt(req.getParameter("discNum"));
 		
 		Music music = new Music(owner,length,songName,artist,genre,blobKey[DATA],blobKey[ART],rating,tags,trackNum,discNum,comment);
 
 		return music;
 	}
 
 }
 
