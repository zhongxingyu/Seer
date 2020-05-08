 package kea.kme.pullpit.server.files;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import kea.kme.pullpit.client.objects.PullPitFile;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyService;
 
 public class Upload extends HttpServlet {
 	private static final long serialVersionUID = 8631632854642686229L;
 	BlobstoreService blobstoreService = BlobstoreServiceFactory
 			.getBlobstoreService();
 	Objectify ofy = ObjectifyService.begin();
 	
 	public void doPost(HttpServletRequest req, HttpServletResponse res)
 			throws ServletException, IOException {
 		Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
		List<BlobKey> blobKeys = blobs.get("fileUploader");
 		BlobKey blobKey = blobKeys.get(0);
 		PullPitFile file = new PullPitFile();
 		file.setType(req.getParameter("docType"));
 		file.setDate(Calendar.getInstance().getTime());
 		file.setFileUrl("/upload?blob-key=" + blobKey.getKeyString());
 		ofy.put(file);
 		res.sendRedirect("/upload?id=" + file.id);
 	}
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
 		String id = req.getParameter("id");
 		res.setHeader("Content-Type", "text/html");
 		res.getWriter().println(id);
 	}
 }
