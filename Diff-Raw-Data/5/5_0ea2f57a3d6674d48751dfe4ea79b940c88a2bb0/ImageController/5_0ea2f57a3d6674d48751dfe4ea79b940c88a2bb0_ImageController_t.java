 package ca.awesome;
 
 import java.sql.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Iterator;
 
 import oracle.sql.*;
 import oracle.jdbc.*;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import javax.imageio.ImageIO;
 import java.io.*;
 import org.apache.commons.fileupload.*;
 
 public class ImageController extends Controller {
 	public static final String RECORDID_FIELD = "RECORD_ID";
 	public static final String IMAGEID_FIELD = "IMAGE_ID";
 	public static final String FULL_FIELD = "FULL";
 	public static final String REGULAR_FIELD = "REGULAR";
 	public static final String THUMBNAIL_FIELD = "THUMBNAIL";
 	public static final String IMAGEPATH_FIELD = "IMAGEPATH";
 
 	public int record_id = 0;
 	public int image_id = 0;
 	public BufferedImage full = null;
 	public BufferedImage regular = null;
 	public BufferedImage thumbnail = null;
 
 	public Collection<Record> records = new ArrayList<Record>();
 
 	public ImageController(ServletContext context,
 			HttpServletRequest request, HttpServletResponse response,
 			HttpSession session) {
 		super(context, request, response, session);
 	}
 	
 	// GET displayImage.jsp
 	public void getDisplayImage() {
 		image_id = Integer.parseInt(request.getQueryString());
 	}
 
 	// GET uploadToRecord.jsp
 	public void getUploadToRecord() {
		//records = Record.getAllRecord(getDatabaseConnection(context));
		String radiologist = user.getUserName();
		records = Record.findRecordByRadiologist(
			radiologist, getDatabaseConnection(context));
 	}
 
 	// POST uploadToRecord.jsp
 	public boolean attemptSelectRecord() {
 		try {
 			record_id = Integer.parseInt(request.getParameter(RECORDID_FIELD));
 			if (record_id == 0) {
 				throw new RuntimeException("record_id not parsed");
 			}
 			session.setAttribute("record_id", record_id);
 			return true;
 		} catch (Exception e) {
 			return false;
 		}		
 	}
 
 	// GET uploadImage.jsp
 	public void getUploadImage() {
 		record_id = (Integer)session.getAttribute("record_id"); 		
 	}
 	
 	// POST uploadImage.jsp
 	public boolean attemptUploadImage() {
 		
 		DatabaseConnection connection = getDatabaseConnection();
 		try {
 			connection.setAutoCommit(false);
 			connection.setAllowClose(false);
 		
 			// Parse the HTTP request to get the image stream
 			DiskFileUpload fileup = new DiskFileUpload();
 	    		List FileItems = fileup.parseRequest(request);
 
 			// Process the uploaded items, assuming only 1 image file uploaded
 			Iterator i = FileItems.iterator();
 	    		FileItem item = (FileItem) i.next();
 	    		while (i.hasNext() && item.isFormField()) {
 				item = (FileItem) i.next();
 	    		}
 
 			// Get the image stream
 			InputStream instream = item.getInputStream();
 			full = ImageIO.read(instream);
 			regular = shrink(full, 5);
 	    		thumbnail = shrink(full, 10);
 
 			// to generate a unique image_id using an SQL sequence
 			ResultSet results = null;
 			Statement statement = null;
 			statement = connection.createStatement();
 			results = statement.executeQuery(
 				"SELECT image_id_sequence.nextval from dual"
 			);
 			results.next();
 			image_id = results.getInt(1);
 
 			record_id = (Integer)session.getAttribute("record_id");
 
 			ImageRecord newImage = new ImageRecord(
 				record_id, image_id, full, regular, thumbnail);
 				
 			// insert image
 			newImage.insertImage(connection);
 
 			instream.close();				
 			connection.commit();
 			return true;
 
 		} catch (Exception e) {
 			connection.rollback();
 			//throw new RuntimeException("upload failed", e);
 			return false;
 		} finally {
 			connection.setAllowClose(true);
 			connection.close();
 		}
 	}
 
 	//shrink image by a factor of n, and return the shrinked image
     	public static BufferedImage shrink(BufferedImage image, int n) {
         	int w = image.getWidth() / n;
         	int h = image.getHeight() / n;
 
         	BufferedImage shrunkImage =
         	    new BufferedImage(w, h, image.getType());
         	for (int y=0; y < h; ++y)
         		for (int x=0; x < w; ++x)
                 		shrunkImage.setRGB(x, y, image.getRGB(x*n, y*n));
 
         	return shrunkImage;
     	}
 
 }
