 package controllers;
 
 import static play.libs.Json.toJson;
 import external.Constants;
 import external.InstagramParser;
 import geometry.Geometry;
 import geometry.Point;
 import helpers.FeatureCollection;
 import helpers.TwitterHelper;
 
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 import javax.imageio.ImageIO;
 
 import leodagdag.play2morphia.Blob;
 import leodagdag.play2morphia.MorphiaPlugin;
 import models.Feature;
 import models.HashTagTable;
 import models.Session;
 
 import org.apache.commons.io.IOUtils;
 import org.bson.types.ObjectId;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 import org.imgscalr.Scalr;
 
 import play.mvc.Controller;
 import play.mvc.Http.MultipartFormData.FilePart;
 import play.mvc.Result;
 import play.mvc.Results;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.gridfs.GridFSDBFile;
 import com.mongodb.gridfs.GridFSFile;
 
 /**
  * @author Muhammad Fahied
  */
 
 public class Features extends Controller {
 
 	public static Result createGeoFeature() throws JsonParseException,
 			JsonMappingException, IOException {
 
 		// String low_resolution =
 		// convertToInstagramImage(filePart.getFile(),filePart.getContentType());
 
 		FilePart jsonFilePart = ctx().request().body().asMultipartFormData()
 				.getFile("feature");
 
 		// Convert json file to JsonNode
 		ObjectMapper mapperj = new ObjectMapper();
 		BufferedReader fileReader = new BufferedReader(new FileReader(
 				jsonFilePart.getFile()));
 
 		JsonNode node = mapperj.readTree(fileReader);
 		ObjectMapper mapper = new ObjectMapper();
 
 		JsonNode coordinatesNode = node.findPath("coordinates");
 		TypeReference<Double[]> collectionTypeD = new TypeReference<Double[]>() {
 		};
 		Double[] coordinates = mapper.readValue(coordinatesNode,
 				collectionTypeD);
 		Geometry geometry = new Point(coordinates[0], coordinates[1]);
 
 		Feature geoFeature = new Feature(geometry);
 
 		JsonNode propertiesNode = node.get("properties");
 		TypeReference<HashMap<String, Object>> collectionType = new TypeReference<HashMap<String, Object>>() {
 		};
 		HashMap<String, Object> properties = mapper.readValue(propertiesNode,
 				collectionType);
 
 		String description = (String) properties.get("description");
 
 		// Formulate the label of the POI, using first sentence in the
 		// description
 		String delims = "[.,?!]+";
 		String[] tokens = description.split(delims);
 		String name = tokens[0];
 		properties.put("name", name);
 
 		Set<String> tags = TwitterHelper.searchHashTags(description);
 		properties.put("tags", tags);
 
 		String high_resolution = "";
 		String standard_resolution = "";
 		String thumb_resolution = "";
 		
 		BufferedImage image;
 		File tmpFile;
 
 		// TODO: make a method to save different sizes of the picture
 		// public static savePictureInDifferentSizes (FilePart picture){}
 
 		// Extract BasicImage from Multipart data
 		if (ctx().request().body().asMultipartFormData().getFile("picture") != null) {
 			FilePart filePart = ctx().request().body().asMultipartFormData().getFile("picture");
 			
 			//saves the full-size image
 			high_resolution = saveImageFile(filePart.getFile(), filePart.getContentType());
 			properties.put("high_resolution", Constants.SERVER_NAME_T + "/image/" + high_resolution);
 			
 			//saves the 150px width image
 			image = ImageIO.read(filePart.getFile());
 			image = Scalr.resize(image, 150);
 			tmpFile = new File("tmpPic");
 			ImageIO.write(image, "jpg", tmpFile);
 			
 			thumb_resolution = saveImageFile(tmpFile, filePart.getContentType());
 			properties.put("thumb_resolution", Constants.SERVER_NAME_T + "/image/" + thumb_resolution);
 			
 			//saves the 612px width image
 			image = ImageIO.read(filePart.getFile());
 			image = Scalr.resize(image, 612);
 			tmpFile = new File("tmpPic");
 			ImageIO.write(image, "jpg", tmpFile);
 			
 			standard_resolution = saveImageFile(tmpFile, filePart.getContentType());
 			properties.put("standard_resolution", Constants.SERVER_NAME_T + "/image/" + standard_resolution);
 			
 			
 			// String thumbnail
 			// =convertToInstagramImage(filePart.getFile(),filePart.getContentType());
 			// properties.put("thumbnail", Constants.SERVER_NAME_T + "/image/" +
 			// thumbnail);
 
 		}
 
 		properties.put("source_type", "overlay");
 
 		// HTML Content url for the Feature
 		properties.put("descr_url", Constants.SERVER_NAME_T + "/content/"
 				+ geoFeature.id);
 		properties.put("icon_url", Constants.SERVER_NAME_T + "/assets/img/"
 				+ "overlay.png");
 
 		// add timestamp
 		Date date = new Date();
 		long dateInLong = date.getTime();
 		properties.put("created_time", dateInLong);
 
 		geoFeature.setProperties(properties);
 
 		geoFeature.insert();
 
 		// Add this feature to perticular session
 		if (propertiesNode.get("session_id") != null) {
 			String seesion_id = propertiesNode.get("session_id").asText();
 			Session session = Session.find().byId(seesion_id);
 			if (session != null) {
 				session.features.add(geoFeature);
 			}
 
 		}
 
 		// TODO: move these task to Feature Model
 		// Save feature reference to individual tags
 		HashTagManager.saveFeatureRefInHashTable(tags, geoFeature);
 
 		// Save Feature reference for perticular user
 		// JsonNode user = node.findPath("user");
 		// if (!(user.isNull())) {
 		// Users.saveFeatureRefForUser(user.get("id").toString(),
 		// user.get("full_name").toString(),geoFeature);
 		// }
 
 		return ok(toJson(geoFeature));
 	}
 
 	public static Result updateGeoFeature() throws JsonParseException,
 			JsonMappingException, IOException {
 		FilePart jsonFilePart = ctx().request().body().asMultipartFormData()
 				.getFile("feature");
 		// Convert json file to JsonNode
 		ObjectMapper mapperj = new ObjectMapper();
 		BufferedReader fileReader = new BufferedReader(new FileReader(
 				jsonFilePart.getFile()));
 
 		JsonNode node = mapperj.readTree(fileReader);
 		ObjectMapper mapper = new ObjectMapper();
 
 		String storedFeature_id = node.get("id").asText();
 		Feature storedFeature = Feature.find().byId(storedFeature_id);
 
 		JsonNode coordinatesNode = node.findPath("coordinates");
 		TypeReference<Double[]> collectionTypeD = new TypeReference<Double[]>() {
 		};
 		Double[] coordinates = mapper.readValue(coordinatesNode,
 				collectionTypeD);
 		Geometry geometry = new Point(coordinates[0], coordinates[1]);
 		storedFeature.geometry = geometry;
 
 		JsonNode propertiesNode = node.get("properties");
 		TypeReference<HashMap<String, Object>> collectionType = new TypeReference<HashMap<String, Object>>() {
 		};
 		HashMap<String, Object> properties = mapper.readValue(propertiesNode,
 				collectionType);
 
 		String description = (String) properties.get("description");
 		String oldDescription = (String) storedFeature.properties
 				.get("description");
 		if (!(description.equals(oldDescription))) {
 
 			// Formulate the label of the POI, using first sentence in the
 			// description
 			String delims = "[.,?!]+";
 			String[] tokens = description.split(delims);
 			String name = tokens[0];
 			storedFeature.properties.put("name", name);
 			storedFeature.properties.put("description", description);
 
 			Set<String> tags = TwitterHelper.searchHashTags(description);
 			storedFeature.properties.put("tags", tags);
 
 			// Save feature reference to individual tags
 			HashTagManager.saveFeatureRefInHashTable(tags, storedFeature);
 
 		}
 
 		String standard_resolution = "";
 		// Extract BasicImage from Multipart data
 		if (ctx().request().body().asMultipartFormData().getFile("picture") != null) {
 			FilePart filePart = ctx().request().body().asMultipartFormData()
 					.getFile("picture");
 			// TODO: remove old picture from database
 			standard_resolution = saveImageFile(filePart.getFile(),
 					filePart.getContentType());
 			storedFeature.properties.put("standard_resolution",
 					Constants.SERVER_NAME_T + "/image/" + standard_resolution);
 
 			// String thumbnail
 			// =convertToInstagramImage(filePart.getFile(),filePart.getContentType());
 			// storedFeature.properties.put("thumbnail", Constants.SERVER_NAME_T
 			// + "/image/" + thumbnail);
 		}
 
 		storedFeature.properties.put("source_type", "overlay");
 
 		// HTML Content url for the Feature
 		storedFeature.properties.put("icon_url", Constants.SERVER_NAME_T
 				+ "/assets/img/" + "overlay.png");
 
 		// add timestamp
 		Date date = new Date();
 		long dateInLong = date.getTime();
 		storedFeature.properties.put("created_time", dateInLong);
 
 		storedFeature.update();
 
 		// Add this feature to perticular session
 		if (propertiesNode.get("session_id") != null) {
 			String seesion_id = propertiesNode.get("session_id").asText();
 			Session session = Session.find().byId(seesion_id);
 			if (session != null) {
 				session.features.add(storedFeature);
 			}
 
 		}
 
 		return ok(toJson(storedFeature));
 	}
 
 	public static Result fetchAllGeoFeautres() {
 		List<Feature> featureslList = Feature.find().all();
 		FeatureCollection features = new FeatureCollection(featureslList);
 		return ok(toJson(features));
 	}
 
 	public static Result featureById(String id) {
 		Feature feature = Feature.find().byId(id);
 		if (feature == null) {
 			return ok("POI Not found");
 		}
 		return ok(toJson(feature));
 	}
 
 	public static Result geoMostRecentFeatures(String lng, String lat) {
 
 		Double lngD = Double.valueOf(lng);
 		Double latD = Double.valueOf(lat);
 		// limite to nearest 18
 		List<Feature> features = Feature.find()
 				.order("-properties.created_time").limit(10).asList();
 		List<Feature> instaPOIs;
 		try {
 			instaPOIs = InstagramParser.searchRecentInstaFeatures(lngD, latD);
 			features.addAll(instaPOIs);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		FeatureCollection collection = new FeatureCollection(features);
 
 		return ok(toJson(collection));
 	}
 
 	/*
 	 * To enble geoo spacial indexing
 	 * db.Feature.ensureIndex({"geometry.coordinates":"2d"});
 	 */
 	public static Result geoFeaturesInBoundingBox(String lng1, String lat1,
 			String lng2, String lat2) throws Exception {
 		// Double [][] boundingbox = new
 		// Double[][]{{Double.valueOf(lng1),Double.valueOf(lat1)},{Double.valueOf(lng2),Double.valueOf(lat2)}};
 		Double lng11 = Double.valueOf(lng1);
 		Double lat11 = Double.valueOf(lat1);
 		Double lng22 = Double.valueOf(lng2);
 		Double lat22 = Double.valueOf(lat2);
 		// limite to nearest 18
 		List<Feature> features = Feature.find().disableValidation()
 				.field("geometry.coordinates")
				.within(lng11, lat11, lng22, lat22).limit(18).asList();
 		List<Feature> instaPOIs = InstagramParser.searchInstaPOIsByBBox(lng11,
 				lat11, lng22, lat22);
 		features.addAll(instaPOIs);
 
 		FeatureCollection collection = new FeatureCollection(features);
 
 		return ok(toJson(collection));
 	}
 
 	/**
 	 * This method is used to get the pois from a service and return a GeoJSON
 	 * document with the data retrieved given a longitude, latitude and a radius
 	 * in meters.
 	 * 
 	 * @param id
 	 *            The id of the service
 	 * @param lon
 	 *            The longitude
 	 * @param lat
 	 *            The latitude
 	 * @param distanceInMeters
 	 *            The distance in meters from the lon, lat
 	 * @return The GeoJSON response from the original service response
 	 */
 	public static Result getPOIsInRadius(String lng, String lat,
 			String distanceInMeters) {
 
 		double lngD = Double.parseDouble(lng);
 		double latD = Double.parseDouble(lat);
 		double radiusD = Double.parseDouble(distanceInMeters);
 
 		List<Feature> features = Feature.find().disableValidation()
				.field("geometry.coordinates").near(lngD, latD).limit(10)
 				.asList();
 
 		List<Feature> instaPOIs = InstagramParser.searchInstaByRadius(lngD,
 				latD, radiusD);
 
 		features.addAll(instaPOIs);
 
 		FeatureCollection collection = new FeatureCollection(features);
 
 		return ok(toJson(collection));
 	}
 
 	public static Result deleteGeoFeature(String id, String user_id) {
 		Feature feature = Feature.find().byId(id);
 		if (feature == null) {
 			return status(404, "NOT_FOUND");
 		}
 
 		JsonNode userNode = toJson(feature.properties.get("user"));
 		String real_author_id = userNode.get("id").asText();
 
 		if (user_id.equals(real_author_id)) {
 
 			// remove feature reference from user
 			// User user = User.find().byId(user_id);
 			// user.features.remove(feature);
 
 			// remove feature reference from individual hashtable
 			JsonNode tagsNode = toJson(feature.properties.get("tags"));
 
 			if (tagsNode != null) {
 				ObjectMapper mapper = new ObjectMapper();
 				try {
 					Set<String> tags = mapper.readValue(tagsNode,
 							new TypeReference<Set<String>>() {
 							});
 					for (String hashTag : tags) {
 						// HashTagTable htabel = HashTagTable.byTag(hashTag);
 						HashTagTable htabel = HashTagTable.byTag(hashTag);
 						htabel.features.remove(feature);
 						htabel.update();
 
 					}
 				} catch (JsonParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (JsonMappingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 
 			feature.delete();
 			return status(200, "OK");
 		}
 
 		return status(403, "FORBIDDEN");
 	}
 
 	public static String saveImage(FilePart filePart) {
 
 		if (filePart.getFile() == null)
 			return "";
 
 		Blob imageBlob = new Blob(filePart.getFile(), filePart.getContentType());
 		GridFSFile file = imageBlob.getGridFSFile();
 		file.save();
 		return file.getId().toString();
 	}
 
 	public static String saveImageFile(File file, String content_type) {
 
 		if (file == null) {
 			return "";			
 		}
 
 		Blob imageBlob = new Blob(file, content_type);
 		GridFSFile image = imageBlob.getGridFSFile();
 		image.save();
 		return image.getId().toString();
 	}
 
 	public static Boolean deleteImage(String id) {
 		MorphiaPlugin.gridFs()
 				.remove(new BasicDBObject("id", new ObjectId(id)));
 		return true;
 	}
 
 	public static Result showImage(String id) throws IOException {
 
 		GridFSDBFile file = MorphiaPlugin.gridFs().findOne(new ObjectId(id));
 
 		byte[] bytes = IOUtils.toByteArray(file.getInputStream());
 
 		return Results.ok(bytes).as(file.getContentType());
 
 	}
 
 	// Instagram take only images with resolution 612 x 612
 	// public static String convertToInstagramImage(File file, String
 	// content_type)
 	// throws IOException {
 	//
 	// // Thumbnails.of(new
 	// File("/Users/spider/Desktop/Eve Myles Leather Jacket for 1920 x 1200 widescreen"))
 	// // .size(160, 160)
 	// // .toFile(new File("/Users/spider/Desktop/thumbnail.jpg"));
 	//
 	// BufferedImage src = ImageIO.read(file);
 	// int height = src.getHeight();
 	// int width = src.getWidth();
 	// BufferedImage dest = null;
 	// if (height > width)
 	// {
 	// dest = src.getSubimage(0, 0, width, width);
 	// } else
 	// {
 	// dest = src.getSubimage(0, 0, height, height);
 	// }
 	//
 	// BufferedImage os = null;
 	//
 	// try {
 	// os = Thumbnails.of(dest).size(612, 612).asBufferedImage();
 	// } catch (IOException e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// }
 	//
 	// // File file = instagramBufferedImage.;
 	// ImageIO.write(os, content_type, file);
 	//
 	// Blob imageBlob = new Blob(file, content_type);
 	// GridFSFile image = imageBlob.getGridFSFile();
 	// image.save();
 	// return image.getId().toString();
 	//
 	// }
 
 	/*
 	 * HTTP STatus Codes public static final int OK = 200; public static final
 	 * int CREATED = 201; public static final int ACCEPTED = 202; public static
 	 * final int PARTIAL_INFO = 203; public static final int NO_RESPONSE = 204;
 	 * public static final int MOVED = 301; public static final int FOUND = 302;
 	 * public static final int METHOD = 303; public static final int
 	 * NOT_MODIFIED = 304; public static final int BAD_REQUEST = 400; public
 	 * static final int UNAUTHORIZED = 401; public static final int
 	 * PAYMENT_REQUIERED = 402; public static final int FORBIDDEN = 403; public
 	 * static final int NOT_FOUND = 404; public static final int INTERNAL_ERROR
 	 * = 500; public static final int NOT_IMPLEMENTED = 501; public static final
 	 * int OVERLOADED = 502; public static final int GATEWAY_TIMEOUT = 503;
 	 */
 }
