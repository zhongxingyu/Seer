 package ch.bbw.gallery.web.services;
 
 import static org.imgscalr.Scalr.crop;
 import static org.imgscalr.Scalr.resize;
 
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 import javax.imageio.ImageIO;
 
 import org.apache.commons.io.IOUtils;
 import org.bson.types.ObjectId;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.imgscalr.Scalr.Method;
 import org.imgscalr.Scalr.Mode;
 
 import ch.bbw.gallery.core.models.Image;
 import ch.bbw.gallery.core.models.ImageSize;
 import ch.bbw.gallery.core.models.ImageType;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.BasicDBObjectBuilder;
 import com.mongodb.DB;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 import com.mongodb.gridfs.GridFS;
 import com.mongodb.gridfs.GridFSDBFile;
 import com.mongodb.gridfs.GridFSInputFile;
 
 /**
  * Exposes methods for loading and manipulating images
  */
 public class ImageService implements IImageService {
 	/**
 	 * Connection to the MongoDB server
 	 */
 	private MongoClient mongoClient;
 
 	/**
 	 * Reference to the MongoDB database
 	 */
 	private DB db;
 	
 	/**
 	 * GridFS instance which holds the image files
 	 */
 	private GridFS gridFS;
 	
 	/**
 	 * GridFS instance which holds the thumbnail files (cache)
 	 */
 	private GridFS thumbsGridFS;
 	
 	/**
 	 * Json object mapper
 	 */
 	@SuppressWarnings("unused")
 	private ObjectMapper mapper;
 
 	/**
 	 * Initialize the ImageService
 	 * 
 	 * @throws UnknownHostException
 	 */
 	public ImageService() throws UnknownHostException {
 		this.mongoClient = new MongoClient();
 		this.db = mongoClient.getDB("gallery");
 		this.gridFS = new GridFS(db);
 		this.thumbsGridFS = new GridFS(db, "thumbs");
 		
 		this.mapper = new ObjectMapper();
 	}
 	
 	/**
 	 * Uploads an image without the meta data
 	 * 
 	 * @param stream Stream of the image
 	 * @param type Type of the image
 	 * @return Newly created image object
 	 */
 	public Image uploadImage(InputStream stream, ImageType type) throws Exception {
 		BasicDBObject dbo = new BasicDBObject();
 		
 		// Upload file
 		GridFSInputFile file = this.gridFS.createFile(stream, true);
 		file.setContentType(type.mimeType());
 		file.setMetaData(dbo);
 		file.save();
 		
 		// Create image model
 		Image image = new Image();
 		image.setId(file.getId().toString());
 		image.setType(type);
 		
 		return image;
 	}
 	
 	/**
 	 * Saves the provided image
 	 * 
 	 * @param image Image object to save
 	 */
 	public void saveImage(Image image) {
 		GridFSDBFile file = this.gridFS.findOne(new ObjectId(image.getId()));
 		
 		DBObject dbo = file.getMetaData();
 		dbo.put("filename", image.getFilename());
 		dbo.put("comment", image.getComment());
 		file.save();
 	}
 	
 	/**
 	 * Publishes the specified image
 	 * 
 	 * @param image Image object to publish
 	 */
 	public void publishImage(Image image) {
 		GridFSDBFile file = this.gridFS.findOne(new ObjectId(image.getId()));
 		
 		DBObject dbo = file.getMetaData();
 		dbo.put("published", true);
 		file.save();
 	}
 	
 	/**
 	 * Writes an image to the provided output stream
 	 * 
 	 * @param id Image identifier
 	 * @param output Output stream
 	 * @throws IOException When the output can't be written
 	 */
 	public void downloadImage(String id, OutputStream output) throws IOException {
 		GridFSDBFile file = this.gridFS.findOne(new ObjectId(id));
 		
 		file.writeTo(output);
 	}
 	
 	/**
 	 * Writes an image with the desired size to the provided output stream
 	 * 
 	 * @param id Image identifier
 	 * @param size Size of the requested image
 	 * @param output Output stream
 	 * @throws IOException When the output can't be written
 	 */
 	public void downloadImage(String id, ImageSize size, OutputStream output) throws IOException {
 		final Image image = this.retrieveImage(id);
 		
 		// Get the cached image if available
 		if (downloadCachedImage(id, size, output)) {
 			return;
 		}
 		
 		final PipedInputStream in = new PipedInputStream();
 		final PipedOutputStream out = new PipedOutputStream(in);
 		
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					downloadImage(image.getId(), out);
 				} catch (IOException e) {
 				}
 				try {
 					out.close();
 				} catch (IOException e) {
 				}
 			}
 		}).start();
 		
 		BufferedImage img = ImageIO.read(in);
 		in.close();
 		
 		img = resizeImage(img, size.getWidth(), size.getHeight());
 		img = cropImage(img, size.getWidth(), size.getHeight());
 		
 		final BufferedImage defImg = img;
 		
 		// Save the new image to the cache
 		final PipedOutputStream pout = new PipedOutputStream();
 		final PipedInputStream pin = new PipedInputStream(pout);
 		
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					ImageIO.write(defImg, image.getType().toString(), pout);
 				} catch (IOException e) {
 				}
 				try {
 					pout.close();
 				} catch (IOException e) {
 				}
 			}
 		}).start();
 		
 		uploadCachedImage(id, size, pin);
 		
 		ImageIO.write(img, image.getType().toString(), output);
 	}
 	
 	/**
 	 * Gets the image object
 	 * 
 	 * @param id Image identifier
 	 * @return Image object
 	 */
 	public Image retrieveImage(String id) {
 		GridFSDBFile file = this.gridFS.findOne(new ObjectId(id));
 		
 		return mapGridFSDBFileToImage(file);
 	}
 	
 	/**
 	 * Returns a list of random images
 	 * 
 	 * @param count Number of images to return
 	 * @return Random images
 	 */
 	public List<Image> randomImages(int count) {
 		List<Image> images = new ArrayList<Image>();
 		DBObject query = new BasicDBObject();
 		
 		// Only published images
		query.put("metadata.published", true);
 		
 		DBCursor cursor = this.gridFS.getFileList(query);
 		
 		if (cursor.count() <= count) {
 			while (cursor.hasNext()) {
 				GridFSDBFile file = (GridFSDBFile) cursor.next();
 				
 				Image image = mapGridFSDBFileToImage(file);
 				images.add(image);
 			}
 				
 		} else {
 			Random r = new Random(new Date().getTime());
 			
 			while (images.size() < count) {
 				cursor.close();
 				cursor = this.gridFS.getFileList();
 				
 				int num = r.nextInt(cursor.count());
 				
 				GridFSDBFile file = (GridFSDBFile) cursor.skip(num).next();
 				
 				Image image = mapGridFSDBFileToImage(file);
 				
 				if (!images.contains(image)) {
 					images.add(image);
 				}
 			}
 		}
 		
 		cursor.close();
 		
 		return images;
 	}
 	
 	/**
 	 * Returns a list of images in the creation order, beginning from the
 	 * provided image
 	 * 
 	 * @param count Number of images to return
 	 * @param startId Optional identifier of the starting image
 	 * @return List of images
 	 */
 	public List<Image> getImages(int count, String startId)
 	{
 		List<Image> images = new ArrayList<Image>();
 		DBObject query = new BasicDBObject();
 		
 		// Only published images
		query.put("metadata.published", true);
 		
 		// Only images starting from the provided one
 		if (startId != null) {
 			GridFSDBFile startFile = this.gridFS.findOne(new ObjectId(startId));
 			Date startDate = startFile.getUploadDate();
 		
 			query.put("uploadDate", new BasicDBObject("$lt", startDate));
 		}
 		
 		DBObject sort = new BasicDBObject("uploadDate", -1); 
 		
 		DBCursor cursor = this.gridFS.getFileList(query).sort(sort);
 		
 		while (cursor.hasNext() && images.size() < count) {
 			GridFSDBFile file = (GridFSDBFile)cursor.next();
 			
 			images.add(mapGridFSDBFileToImage(file));
 		}
 		
 		cursor.close();
 		
 		return images;
 	}
 	
 	/**
 	 * Maps a raw grid fs database file into an image
 	 * 
 	 * @param file Raw grid fs database file
 	 * @return Image
 	 */
 	private Image mapGridFSDBFileToImage(GridFSDBFile file) {
 		BasicDBObject dbo = (BasicDBObject)file.getMetaData();
 		
 		Image image = new Image();
 		image.setId(file.getId().toString());
 		image.setFilename(dbo.getString("filename"));
 		image.setComment(dbo.getString("comment"));
 		image.setType(ImageType.fromMimeType(file.getContentType()));
 		
 		return image;
 	}
 	
 	private boolean downloadCachedImage(String id, ImageSize size, OutputStream output) {
 		DBObject query = BasicDBObjectBuilder
 				.start("metadata.id", new ObjectId(id))
 				.add("metadata.size.width", size.getWidth())
 				.add("metadata.size.height", size.getHeight())
 				.get();
 		
 		GridFSDBFile file = this.thumbsGridFS.findOne(query);
 		
 		if (file == null)
 			return false;
 		
 		try {
 			IOUtils.copy(file.getInputStream(), output);
 			
 			return true;
 		} catch (IOException e) {
 			return false;
 		}
 	}
 	
 	private void uploadCachedImage(String id, ImageSize size, InputStream stream) {
 		DBObject sizeObject = BasicDBObjectBuilder
 				.start("width", size.getWidth())
 				.add("height", size.getHeight())
 				.get();
 		
 		DBObject metadataObject = BasicDBObjectBuilder
 				.start("id", new ObjectId(id))
 				.add("size", sizeObject)
 				.get();
 		
 		GridFSInputFile file = this.thumbsGridFS.createFile(stream, true);
 		file.setMetaData(metadataObject);
 		file.save();
 	}
 	
 	private BufferedImage resizeImage(BufferedImage img, int width, int height) {
 		if (width == 0 && height == 0) {
 			return img;
 		}
 		
 		Mode mode = Mode.AUTOMATIC;
 		
 		if (width == 0) {
 			mode = Mode.FIT_TO_HEIGHT;
 		} else if (height == 0) {
 			mode = Mode.FIT_TO_WIDTH;
 		} else {
 			double ratio = (double)img.getWidth() / img.getHeight();
 			
 			double newWidth = height * ratio;
 			double newHeight = width / ratio;
 			
 			if (newWidth >= width) {
 				mode = Mode.FIT_TO_HEIGHT;
 			} else if (newHeight >= height) {
 				mode = Mode.FIT_TO_WIDTH;
 			}
 		}
 		
 		return resize(img, Method.QUALITY, mode, width, height);
 	}
 	
 	private BufferedImage cropImage(BufferedImage img, int width, int height) {
 		int x = (img.getWidth() - width) / 2;
 		int y = (img.getHeight() - height) / 2;
 		
 		return crop(img, x, y, width, height);
 	}
 }
