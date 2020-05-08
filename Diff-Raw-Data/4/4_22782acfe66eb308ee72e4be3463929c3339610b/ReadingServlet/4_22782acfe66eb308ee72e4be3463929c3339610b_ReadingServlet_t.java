 package com.ptzlabs.wc.servlet;
 
 import static com.googlecode.objectify.ObjectifyService.ofy;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.nio.channels.Channels;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.util.PDFTextStripper;
 
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.files.AppEngineFile;
 import com.google.appengine.api.files.FileReadChannel;
 import com.google.appengine.api.files.FileService;
 import com.google.appengine.api.files.FileServiceFactory;
 import com.google.appengine.api.files.FileWriteChannel;
 import com.googlecode.objectify.Key;
 import com.ptzlabs.wc.Chunk;
 import com.ptzlabs.wc.Reading;
 import com.ptzlabs.wc.User;
 
 public class ReadingServlet extends HttpServlet {
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 
 		if (req.getParameter("mode").equals("new")
 				&& req.getParameter("name") != null
 				&& req.getParameter("location") != null
 				&& req.getParameter("fbid") != null
 				&& req.getParameter("type") != null) {
 
 			Reading reading;
 			if (req.getParameter("dueDate") != null) {
 				reading = new Reading(req.getParameter("name"), new Date(
 						Long.parseLong(req.getParameter("dueDate"))),
 						Long.parseLong(req.getParameter("fbid")));
 			} else {
 				reading = new Reading(req.getParameter("name"),
 						Long.parseLong(req.getParameter("fbid")));
 			}
 
 			ofy().save().entity(reading).now();
 			Key<Reading> readingKey = Key.create(Reading.class, reading.id);
 
 			if (req.getParameter("type").equals("application/pdf")) {
 				PDDocument document = PDDocument.load(req
 						.getParameter("location"));
 				PDFTextStripper stripper = new PDFTextStripper();
 				String text = stripper.getText(document);
 
 				int fromIndex = 0;
 				int endSentence = text.indexOf(". ", fromIndex);
 				int sentence = 0;
 				String data = "";
 				
 				int chunkCounter = 1;
 				while (endSentence != -1) {
					data += text.substring(fromIndex, endSentence);
 					sentence++;
 					if (sentence == 2) {
 						Chunk chunk = new Chunk(chunkCounter, readingKey, data);
 						ofy().save().entity(chunk).now();
 						chunkCounter++;
 						sentence = 0;
 						data = "";
 					}
 					fromIndex = endSentence + 2;
 					endSentence = text.indexOf(". ", fromIndex);
 				}
 				if (sentence != 0) {
 					Chunk chunk = new Chunk(chunkCounter, readingKey, data);
 					ofy().save().entity(chunk).now();
 				}
 				
 				//total chunks = chunkCounter
 				Reading r = ofy().load().key(readingKey).get();
 				r.setTotalChunks(chunkCounter);
 				ofy().save().entity(r).now();
 
 			} else {
 
 				AppEngineFile file = readFileAndStore(req
 						.getParameter("location"));
 
 				FileService fileService = FileServiceFactory.getFileService();
 
 				// Later, read from the file using the file API
 				FileReadChannel readChannel = fileService.openReadChannel(file,
 						false);
 
 				// Again, different standard Java ways of reading from the
 				// channel.
 				BufferedReader reader = new BufferedReader(Channels.newReader(
 						readChannel, "UTF8"));
 
 				String line = reader.readLine();
 				String data = "";
 
 				int sentence = 0;
 
 				int chunkCounter = 1;
 				int fromIndex = 0;
 				while (line != null) {
 					int i = 0;
 					int endSentence = line.indexOf(". ", fromIndex);
 					while (endSentence != -1) {
 						data += line.substring(fromIndex, endSentence);
 						sentence++;
 						if (sentence == 2) {
 							Chunk chunk = new Chunk(chunkCounter, readingKey, data);
 							ofy().save().entity(chunk).now();
 							sentence = 0;
 							data = "";
 							chunkCounter++;
 						}
 						fromIndex = endSentence + 2;
 						endSentence = line.indexOf(". ", fromIndex);
 					}
 					
 					data += line.substring(fromIndex, endSentence);
 					line = reader.readLine();
 				}
 
 				if (sentence != 0) {
 					Chunk chunk = new Chunk(chunkCounter, readingKey, data);
 					ofy().save().entity(chunk).now();
 				}
 				
 				//total chunks = chunkCounter
 				Reading r = ofy().load().key(readingKey).get();
 				r.setTotalChunks(chunkCounter);
 				ofy().save().entity(r).now();
 
 				readChannel.close();
 
 				// remove blob from blobstore
 				BlobKey blobKey = fileService.getBlobKey(file);
 				BlobstoreService blobStoreService = BlobstoreServiceFactory
 						.getBlobstoreService();
 
 				blobStoreService.delete(blobKey);
 			}
 			resp.setContentType("text/plain");
 			resp.getWriter().println("OK");
 		}
 	}
 
 	private static AppEngineFile readFileAndStore(String location) throws IOException {
 		BufferedReader reader;
 		FileService fileService = FileServiceFactory.getFileService();
 		AppEngineFile file = fileService.createNewBlobFile("text/plain");
 		FileWriteChannel writeChannel = fileService.openWriteChannel(file, true);
 
 		reader = new BufferedReader(new InputStreamReader(
 				new URL(location).openStream()));
 
 		String line;
 		while ((line = reader.readLine()) != null) {
 			writeChannel.write(ByteBuffer.wrap(line.getBytes()));
 		}
 		reader.close();
 		writeChannel.closeFinally();
 
 		return file;
 	}
 	
 	public static List<Reading> getReadings(long fbid) {
 		User user = User.getUser(fbid);
 		return ofy().load().type(Reading.class).filter("user", user.id).list();
 	}
 	
 	public static Reading get(long id) {
 		return ofy().load().type(Reading.class).id(id).get();
 	}
 }
