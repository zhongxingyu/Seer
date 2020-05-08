 /*
  * Gardenshift Webservices with Resteasy for Jboss
  * 
  * 
  * Author					Comment														Modified
  * 
  * Hilay Khatri				Add new user with email/username validation					06-11-2012
  * 	
  * Hilay Khatri				Insert user details in mongoDB								06-11-2012
  * 
  * Hilay Khatri				Show all users personal information							06-11-2012
  * 
  * Hilay Khatri				Show information of a particular user						06-11-2012
  * 
  * Hilay Khatri				Update personal records 									06-11-2012
  * 
  * Hilay Khatri				Authenticate user											06-11-2012
  *
  */
 
 package mypackage;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.Response;
 
 //import javax.ws.rs.core.Response;
 
 import sun.misc.BASE64Encoder;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 import java.util.Date;
 import java.sql.Timestamp;
 @Path("/")
 public class Gardenshift {
 
 	public DB db;
 	public Mongo mongo;
 
 	public Gardenshift() {
 
 		try {
 
 			mongo = new Mongo("127.3.119.1", 27017);
 		//	mongo = new Mongo("localhost", 27017);
 			db = mongo.getDB("gardenshift");
 
 			db.authenticate("admin", "redhat".toCharArray());
 
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MongoException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	@Path("adduser")
 	@POST
 	public Response adduser(@FormParam("username") String userID,
 			@FormParam("password") String password,
 			@FormParam("email") String emailAdd) {
 
 		/*
 		 * Adds a new username to the database.
 		 */
 
 		Boolean userExists = false; // See if username already exists
 		Boolean emailExists = false; // See if email already exists
 		Boolean validEmail = false; // See if valid email address
 		Boolean validUsername = false; // See if valid email address
 		String user, email; // Stores username and email retrieved from mongoDB
 							// for verification
 
 		String msg = ""; // Error message
 
 		try {
 			
 		//	userClass newUser = new userClass();
 
 			validEmail = isValidEmailAddress(emailAdd);
 
 			if (userID.length() < 6) {
 				msg = "Username should not be less than 6 characters";
 				return Response.status(200).entity(msg).build();
 
 			}
 
 			if (validEmail) {
 
 				DBCollection collection = db.getCollection("users");
 				BasicDBObject document = new BasicDBObject();
 
 				DBCursor cursor = collection.find();
 
 				while (cursor.hasNext()) {
 
 					BasicDBObject obj = (BasicDBObject) cursor.next();
 
 					user = obj.getString("username");
 					email = obj.getString("email");
 
 					if (user.equals(userID)) {
 						userExists = true;
 						msg = "failure-user already exists";
 
 					}
 
 					if (email.equals(emailAdd)) {
 						emailExists = true;
 						msg = "failure-email already exists";
 
 					}
 
 				}
 
 				// If username or email is unique, create a new user
 				if (userExists == false && emailExists == false) {
 
 					msg = "Success-user created";
 					document.put("username", userID);
 					document.put("password",
 							encryptPassword(password, "SHA-1", "UTF-8"));
 					document.put("creation_date", new Date().toString());
 					document.put("email", emailAdd);
 					document.put("name", "");
 					document.put("picture", "http://www.worldbiofuelsmarkets.com/EF/Images/blank_profile_pic.jpg");
 					document.put("zipcode", ""); // HTML5 Geolocation API can
 													// also be used
 
 					BasicDBObject feedback = new BasicDBObject();
 					feedback.put("from", "");
 					feedback.put("text", "");
 					document.put("feedback", feedback);
 					
 					document.put("notifications_read", new ArrayList());
 
                     document.put("notifications_unread", new ArrayList());
                     document.put("bulletin", new ArrayList());
 
 
                     document.put("bulletin_archive", new ArrayList());
 
 					BasicDBObject friends = new BasicDBObject();
 					friends.put("friends_username", "");
 					friends.put("status", "");
					friends.put("friends", friends);
 
 //					BasicDBObject user_crops = new BasicDBObject();
 //					user_crops.put("crop_name", "");
 //					user_crops.put("crop_expected_quantity", "");
 //					user_crops.put("crop_harvest_date", "");
 //					user_crops.put("crop_harvested", "");
 //					user_crops.put("pictures", "");
 //					user_crops.put("videos", "");
 //					user_crops.put("comments", "");
 //					document.put("user_crops", user_crops);
					 document.put("user_crops", new ArrayList());
 					
 					collection.insert(document);
 					
 				}
 			}
 
 			else {
 				msg = "Invalid Email Address";
 				return Response.status(200).entity(msg).build();
 			}
 
 		} catch (UnknownHostException e) {
 			Response.status(500);
 		} catch (MongoException e) {
 			Response.status(500);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			Response.status(500);
 		}
 		return Response.status(200).entity(msg).build();
 
 	}
 
 	/*
 	 * This method encrypts the password using SHA-1 algorithm and UTF-8
 	 * encoding.
 	 */
 	private static String encryptPassword(String plaintext, String algorithm,
 			String encoding) throws Exception {
 
 		MessageDigest msgDigest = null;
 		String hashValue = null;
 
 		try {
 			msgDigest = MessageDigest.getInstance(algorithm);
 			msgDigest.update(plaintext.getBytes(encoding));
 			byte rawByte[] = msgDigest.digest();
 			hashValue = (new BASE64Encoder()).encode(rawByte);
 
 		} catch (NoSuchAlgorithmException e) {
 			System.out.println("No Such Algorithm Exists");
 		} catch (UnsupportedEncodingException e) {
 			System.out.println("The Encoding Is Not Supported");
 		}
 		return hashValue;
 	}
 
 	@Path("authenticate")
 	@POST
 	public Response authenticate(@FormParam("username") String userID,
 			@FormParam("password") String password) {
 
 		/*
 		 * This method authenticates the user
 		 */
 
 		String msg = "false";
 	
 		try {
 
 			DBCollection collection = db.getCollection("users");
 
 			BasicDBObject searchQuery = new BasicDBObject();
 			searchQuery.put("username", userID);
 
 			BasicDBObject keys = new BasicDBObject();
 			keys.put("password", 1);
 
 			DBCursor cursor = collection.find(searchQuery, keys);
 
 			while (cursor.hasNext()) {
 
 				BasicDBObject obj = (BasicDBObject) cursor.next();
 
 				String result = obj.getString("password");
 				
 				System.out.println("function=" + encryptPassword(password, "SHA-1", "UTF-8") + "from database=" + result);
 
 				if (result.equals(encryptPassword(password, "SHA-1", "UTF-8"))) {
 					msg = "true";
 				}
 
 			}
 
 		} catch (UnknownHostException e) {
 			Response.status(500);
 		} catch (MongoException e) {
 			Response.status(500);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			Response.status(500);
 		}
 
 		return Response.status(200).entity(msg).build();
 
 	}
 
 	// @Path("user_info")
 	// @POST
 	// public Response insert(@FormParam("username") String userID,
 	// @FormParam("name") String name, @FormParam("phone") String phone,
 	// @FormParam("address") String address, @FormParam("gender") String gender,
 	// @FormParam("dob") String dob) {
 	//
 	// /*
 	// * Stores user's personal information in the database
 	// */
 	//
 	// try {
 	//
 	//
 	// DBCollection collection = db.getCollection("users");
 	// BasicDBObject document = new BasicDBObject();
 	//
 	// document.put("username", userID);
 	// document.put("name", name);
 	// document.put("address", address); //HTML5 Geolocation API can also be
 	// used
 	// document.put("gender", gender);
 	// document.put("phone", phone);
 	// document.put("dob", dob);
 	//
 	// collection.insert(document);
 	//
 	// } catch (MongoException e) {
 	// Response.status(500);
 	// }
 	//
 	// return Response.status(200).entity("Inserted").build();
 	//
 	//
 	// }
 	//
 
 	@GET
 	@Path("/user_details/{username}")
 	@Produces("application/json")
 	public Response showUserDetails(@PathParam("username") String username) {
 
 		/*
 		 * Displays information for a user
 		 */
 
 		String msg = "";
 
 		try {
 
 			BasicDBObject searchQuery = new BasicDBObject();
 			DBCollection collection = db.getCollection("users");
 
 			searchQuery.put("username",
 					java.util.regex.Pattern.compile(username));
 			DBCursor cursor = collection.find(searchQuery);
 
 			if (cursor.hasNext() == false) {
 				msg = "null";
 				return Response.status(200).entity(msg).build();
 			}
 
 			while (cursor.hasNext()) {
 				msg += cursor.next();
 			}
 
 		} catch (MongoException e) {
 			Response.status(500);
 		}
 
 		return Response.status(200).entity(msg).build();
 
 	}
 	
 	@GET
 	@Path("/user_available/{username}")
 	@Produces("application/json")
 	public Response isUserAvailable(@PathParam("username") String username) {
 
 		/*
 		 * checks if use is available
 		 */
 
 		String msg = "";
 
 		try {
 
 			BasicDBObject searchQuery = new BasicDBObject();
 			DBCollection collection = db.getCollection("users");
 
 			searchQuery.put("username",
 					username);
 			DBCursor cursor = collection.find(searchQuery);
 
 			if (cursor.hasNext() == false) {
 				msg = "null";
 				return Response.status(200).entity(msg).build();
 			}
 
 			while (cursor.hasNext()) {
 				msg += cursor.next();
 			}
 
 		} catch (MongoException e) {
 			Response.status(500);
 		}
 
 		return Response.status(200).entity(msg).build();
 
 	}
 
 	@GET
 	@Path("/user_search/{data}")
 	@Produces("application/json")
 	public Response searchUser(@PathParam("data") String data) {
 
 		/*
 		 * Displays information for a user
 		 */
 
 		String msg = "";
 
 		try {
 
 			DBCollection collection = db.getCollection("users");
 
 			List<BasicDBObject> searchQuery = new ArrayList<BasicDBObject>();
 
 			searchQuery.add(new BasicDBObject("username", data));
 			searchQuery.add(new BasicDBObject("email", data));
 			searchQuery.add(new BasicDBObject("zipcode", data));
 			searchQuery.add(new BasicDBObject("name", data));
 
 			BasicDBObject sQuery = new BasicDBObject();
 			sQuery.put("$or", searchQuery);
 
 			DBCursor cursor = collection.find(sQuery);
 
 			if (cursor.hasNext() == false) {
 				msg = "null";
 				return Response.status(200).entity(msg).build();
 			}
 
 			while (cursor.hasNext()) {
 				msg += cursor.next();
 			}
 
 		} catch (MongoException e) {
 			Response.status(500);
 		}
 
 		return Response.status(200).entity(msg).build();
 
 	}
 
 	@GET
 	@Path("/user_details/all")
 	@Produces("application/json")
 	public Response showAllUserDetails() {
 
 		/*
 		 * Displays information of all users
 		 */
 
 		String msg = "[";
 
 		try {
 
 			DBCollection collection = db.getCollection("users");
 
 			DBCursor cursor = collection.find();
 
 			if (cursor.hasNext() == false) {
 				msg = "null";
 			}
 
 			while (cursor.hasNext()) {
 				msg += cursor.next() + ",";
 			}
 
 		} catch (Exception e) {
 		}
 
 		msg = msg.substring(0, msg.length() - 1);
 		msg += "]";
 
 		return Response.status(200).entity(msg).build();
 
 	}
 
 	@Path("updateuser")
 	@POST
 	public Response update(@FormParam("username") String username,
 			@FormParam("name") String name, 
 			@FormParam("zip") String zip,
 			@FormParam("email") String email) {
 
 		/*
 		 * Stores user's personal information in the database
 		 */
 
 		
 		try {
 			DBCollection collection = db.getCollection("users");
 			BasicDBObject newDocument = new BasicDBObject().append("$set",
 					new BasicDBObject().append("name", name).append("email", email).append("zipcode", zip));
 			
 			System.out.println(newDocument);
 
 			collection.update(
 					new BasicDBObject().append("username", username),
 					newDocument);
 
 			return Response.status(200).entity("success").build();
 
 		} catch (Exception e) {
 			return Response.status(503).entity("failed").build();
 		}
 
 	}
 	
 	@Path("change_picture")
 	@POST
 	public Response change_picture(@FormParam("username") String username,			
 			@FormParam("url") String url)
 			 {
 
 		/*
 		 * Stores user's personal information in the database
 		 */
 
 		
 		try {
 			DBCollection collection = db.getCollection("users");
 			BasicDBObject newDocument = new BasicDBObject().append("$set",
 					new BasicDBObject().append("picture", url));
 			
 			
 			collection.update(
 					new BasicDBObject().append("username", username),
 					newDocument);
 
 			return Response.status(200).entity("success").build();
 
 		} catch (Exception e) {
 			return Response.status(503).entity("failed").build();
 		}
 
 	}
 	
 	@Path("status")
 	@POST
 	public Response updateStatus(@FormParam("username") String username,
 			@FormParam("status_txt") String status_txt )
 			 {
 
 		/*
 		 * Add a new status to user's database
 		 */
 
 		
 		try {
 					DBCollection collection = db.getCollection("users");
 					BasicDBObject update = new BasicDBObject();
 		            update.put("username", username);
 	
 		           
 		            BasicDBObject document = new BasicDBObject();
 	            
 	                document.put("text", status_txt);
 	                document.put("date", new Date().toString());               
 	                
 	                BasicDBObject temp = new BasicDBObject();
 	                temp.put("$push", new BasicDBObject("status", document));
 
 	                collection.update(update, temp, true, true);
 
 	                return Response.status(200).entity("success").build();
 
 		} catch (Exception e) {
 			return Response.status(503).entity("failed").build();
 		}
 
 	}
 	
 	@GET
 	@Path("delete_status/{username}/{date}")
 	@Produces("application/json")
 	public Response delete_userstatus(@PathParam("date") String date, @PathParam("username") String username) {
 	    /*
 	     * This method deletes a particular status entry from user
 	     */
 	    try {
 
 	    	DBCollection collection = db.getCollection("users");
             
             BasicDBObject update = new BasicDBObject();
             update.put("username", username);
 
             // check if the entry is not a duplicate
             BasicDBObject document = new BasicDBObject();
             
                 document.put("date", date);              
                 
                 BasicDBObject temp = new BasicDBObject();
                 temp.put("$pull", new BasicDBObject("status", document));
 
                 collection.update(update, temp, true, true);
                
                 return Response.status(200).entity("success").build();
 
 
 	    } catch (Exception e) {
 	        return Response.status(503).entity("failed").build();
 	    }
 
 	}
 	
 
 	@GET
 	@Path("deleteuser/{username}")
 	@Produces("application/json")
 	public Response deleteUser(@PathParam("username") String username) {
 		/*
 		 * This method deletes a particular crop entry
 		 */
 		try {
 
 			DBCollection collection = db.getCollection("users");
 			BasicDBObject searchquery = new BasicDBObject();
 			searchquery.put("username", username);
 
 			collection.remove(searchquery);
 
 			return Response.status(200).entity("success").build();
 
 		} catch (Exception e) {
 			return Response.status(503).entity("failed").build();
 		}
 
 	}
 
 	// @POST
 	// @Path("/upload")
 	// @Consumes("multipart/form-data")
 	// public Response uploadFile(@MultipartForm FileUploadForm form) {
 	//
 	// String fileName = "/home/hilaykhatri/test211.txt";
 	//
 	// try {
 	// writeFile(form.getData(), fileName);
 	// } catch (IOException e) {
 	// e.printStackTrace();
 	// }
 	//
 	// System.out.println("Done");
 	//
 	// return Response.status(200)
 	// .entity("uploadFile is called, Uploaded file name : " +
 	// fileName).build();
 	//
 	// }
 	//
 	// // save to somewhere
 	// private void writeFile(byte[] content, String filename) throws
 	// IOException {
 	//
 	//
 	//
 	// DBCollection collection = db.getCollection("images");
 	//
 	//
 	//
 	// // create a "photo" namespace
 	// GridFS gfsPhoto = new GridFS(db, "photo");
 	//
 	// // get image file from local drive
 	// GridFSInputFile gfsFile = gfsPhoto.createFile(content);
 	//
 	// // set a new filename for identify purpose
 	// gfsFile.setFilename("hilay");
 	//
 	// // save the image file into mongoDB
 	// gfsFile.save();
 	//
 	// // print the result
 	// DBCursor cursor = gfsPhoto.getFileList();
 	// while (cursor.hasNext()) {
 	// System.out.println(cursor.next());
 	// }
 	//
 	// // get image file by it's filename
 	// GridFSDBFile imageForOutput = gfsPhoto.findOne("hilay");
 	//
 	// // save it into a new image file
 	// imageForOutput.writeTo("/home/hilaykhatri/test1hilay.txt");
 	//
 	// System.out.println("Done");
 	//
 	// }
 	
 	
 	
 	@GET
 	@Path("/userCropSearch/{cropname}")
 	@Produces("application/json")
 	public Response showUserByCrop(@PathParam("cropname") String cropname) {
 
 		/*
 		 * Displays information for a user
 		 */
 
 		String msg = "[";
 
 		try {
 
 			BasicDBObject searchQuery = new BasicDBObject();
 			
 			BasicDBObject keys = new BasicDBObject();
 			
 			DBCollection collection = db.getCollection("users");
 			
 			keys.put("username", 1);
 			keys.put("email", 1);
 			keys.put("zipcode", 1);
 			keys.put("user_crops.crop_name", 1);
 			
 			searchQuery.put("user_crops.crop_name",
 					java.util.regex.Pattern.compile(cropname));
 			DBCursor cursor = collection.find(searchQuery, keys);
 
 			if (cursor.hasNext() == false) {
 				msg = "null";
 				return Response.status(200).entity(msg).build();
 			}
 
 			while (cursor.hasNext()) {
 				msg += cursor.next() + ",";
 			}
 
 		} catch (MongoException e) {
 			Response.status(500);
 		}
 		
 		msg = msg.substring(0, msg.length() - 1);
 		msg += "]";
 
 
 		return Response.status(200).entity(msg).build();
 
 	}
 
 
 	public static boolean isValidEmailAddress(String email) {
 		boolean result = true;
 		try {
 			InternetAddress emailAddr = new InternetAddress(email);
 			emailAddr.validate();
 		} catch (AddressException ex) {
 			result = false;
 		}
 		return result;
 	}
 	
 	// Add a crop grown by user
 	@GET
     @Path("create_usercrop/{username}/{name}/{quantity}/{date}/{comment}")
     @Produces("application/json")
     public Response addusercrop(
             @PathParam("name") String name,
             @PathParam("username") String username,
             @PathParam("quantity") String quantity,
             @PathParam("date") String date,
             @PathParam("comment") String comment) {
 
        
         try {
             DBCollection collection = db.getCollection("users");
             
             BasicDBObject update = new BasicDBObject();
             update.put("username", username);
 
            
             BasicDBObject document = new BasicDBObject();
             
                 document.put("crop_name", name);
                 document.put("crop_expected_quantity", quantity);
                 document.put("crop_harvest_date",date);
                 document.put("comments",comment);
                 
                 BasicDBObject temp = new BasicDBObject();
                 temp.put("$push", new BasicDBObject("user_crops", document));
 
                 collection.update(update, temp, true, true);
 
                 return Response.status(200).entity("success").build();
 
             }
          catch (Exception e) {
             return Response.status(503).entity("failed").build();
         }
     }
 	
 	// Delete a crop grown by user
 	
 	@GET
 	@Path("delete_usercrop/{username}/{crop_name}")
 	@Produces("application/json")
 	public Response delete_usercrop(@PathParam("crop_name") String crop_name, @PathParam("username") String username) {
 	    /*
 	     * This method deletes a particular crop entry
 	     */
 	    try {
 
 	    	DBCollection collection = db.getCollection("users");
             
             BasicDBObject update = new BasicDBObject();
             update.put("username", username);
 
             // check if the entry is not a duplicate
             BasicDBObject document = new BasicDBObject();
             
                 document.put("crop_name", crop_name);              
                 
                 BasicDBObject temp = new BasicDBObject();
                 temp.put("$pull", new BasicDBObject("user_crops", document));
 
                 collection.update(update, temp, true, true);
                
                 return Response.status(200).entity("success").build();
 
 
 	    } catch (Exception e) {
 	        return Response.status(503).entity("failed").build();
 	    }
 
 	}
 	
 	//Update an existing crop grown by user
 	@GET
     @Path("update_usercrop/{username}/{name}/{quantity}/{date}/{comment}")
     @Produces("application/json")
     public  Response  updateusercrop(
             @PathParam("name") String name,
             @PathParam("username") String username,
             @PathParam("quantity") String quantity,
             @PathParam("date") String date,
             @PathParam("comment") String comment) {
 
         try{
         		DBCollection collection = db.getCollection("users");
         		BasicDBObject update = new BasicDBObject();
         		update.put("username", username);
         		BasicDBObject document = new BasicDBObject();
         		document.put("crop_name", name);              
                 BasicDBObject temp = new BasicDBObject();
                 temp.put("$pull", new BasicDBObject("user_crops", document));
                 collection.update(update, temp, true, true);
                 Thread.sleep(3000);
                 
                 DBCollection collection1 = db.getCollection("users");
                 BasicDBObject update1 = new BasicDBObject();
                 update1.put("username", username);
                 BasicDBObject document1 = new BasicDBObject();
                 document1.put("crop_name", name);
                 document1.put("crop_expected_quantity", quantity);
                 document1.put("crop_harvest_date",date);
                 document1.put("comments",comment);
                 BasicDBObject temp1 = new BasicDBObject();
                 temp1.put("$push", new BasicDBObject("user_crops", document1));
                 collection1.update(update1, temp1, true, true);
                 return Response.status(503).entity("success").build();
 
            }   catch(Exception E){
             return Response.status(503).entity("failed").build();
         }
 
     }
 	
 
 	// Crops API===============================================
 
 	
 	@GET
     @Path("/crop_search/{crop_name}")
     @Produces("application/json")
     public Response searchCrop(@PathParam("crop_name") String crop_name) {
 
     /*
      *      Displays information for a user
      */
 
              String msg ="";
 
              try {
 
 
              DBCollection collection = db.getCollection("crop_details");
 
              BasicDBObject sq = new BasicDBObject();
              
 sq.put("crop_name",java.util.regex.Pattern.compile(crop_name));
               DBCursor cursor = collection.find(sq);
 
              if(cursor.hasNext() == false)
              {
              msg = "null";
              return Response.status(200).entity(msg).build();
              }
 
              while (cursor.hasNext()) {
              msg += cursor.next();
              }
 
              } catch (MongoException e) {
                      Response.status(500);
              }
 
              return Response.status(200).entity(msg).build();
 
 
     }
 
 
 	@POST
     @Path("create_crop")
     @Produces("application/json")
     public Response addcrop(
             @FormParam("name") String crop_name,
             @FormParam("description") String description) {
 
         /*
          * Adds a new crop entry to the database.
          */
         try {
             DBCollection collection = db.getCollection("crop_details");
 
             // check if the entry is not a duplicate
             BasicDBObject searchQuery = new BasicDBObject();
             searchQuery.put("crop_name", crop_name);
                         DBCursor cursor = collection.find(searchQuery);
             if (cursor.hasNext()) {
                 return Response
                         .status(403)
                         .entity("entry already exists, choose to update instead")
                         .build();
             } else {
                 BasicDBObject document = new BasicDBObject();
                 document.put("crop_name", crop_name);
                 document.put("crop_description", description);
                 //document.put("image",image);
 
                 collection.insert(document);
 
                 return Response.status(200).entity("success").build();
 
             }
         } catch (Exception e) {
             return Response.status(503).entity("failed").build();
         }
     }
 
 @POST
 @Path("updatecrop")
 @Produces("application/json")
 public Response updatecrop(
         @FormParam("crop_name") String crop_name,
         @FormParam("description") String description) {
     /*
      * This method returns the list of all the crops grown by a
 particular
      * user
      */
     try {
         DBCollection collection = db.getCollection("crop_details");
         BasicDBObject newDocument = new BasicDBObject().append(
                 "$set",
                 new BasicDBObject().append("crop_description",
 description));
 
         collection.update(
                 new BasicDBObject().append("crop_name", crop_name),
                 newDocument);
 
         return Response.status(200).entity("success").build();
 
     } catch (Exception e) {
         return Response.status(503).entity("failed").build();
     }
 
 }
 
 @GET
 @Path("deletecrop/{crop_name}")
 @Produces("application/json")
 public Response deletecrop(@PathParam("crop_name") String crop_name) {
     /*
      * This method deletes a particular crop entry
      */
     try {
 
         DBCollection collection = db.getCollection("crop_details");
         BasicDBObject searchquery = new BasicDBObject();
         searchquery.put("crop_name", crop_name);
 
         collection.remove(searchquery);
 
         return Response.status(200).entity("success").build();
 
     } catch (Exception e) {
         return Response.status(503).entity("failed").build();
     }
 
 }
 @GET
 @Path("crop_details/all")
 @Produces("application/json")
 public Response dashboard() {
     /*
      * This method returns the list of all the crops that are in the
      * database
      */
 	String msg = "[";
 
 	try {
 
 		DBCollection collection = db.getCollection("crop_details");
 
 		DBCursor cursor = collection.find();
 
 		if (cursor.hasNext() == false) {
 			msg = "null";
 		}
 
 		while (cursor.hasNext()) {
 			msg += cursor.next() + ",";
 		}
 
 	} catch (Exception e) {
 	}
 
 	msg = msg.substring(0, msg.length() - 1);
 	msg += "]";
 
 	return Response.status(200).entity(msg).build();
 
 }
 
 
 // Geolocation Based API
 
 
 @GET
 @Path("/search/{zipcode}/{distance}")
 @Produces("application/json")
 public Response search_crop(@PathParam("zipcode") String zipcode, @PathParam("distance") String distance)
 {
 
 /*
 * Displays all the users which are within the given radius
 */
 
 	String URI = "http://api.geonames.org/findNearbyPostalCodesJSON?";
 			
       String RESTCall ="";
       String res ="";
       String result="";
 
       try {
 
 
     	  	 RESTCall = URI + "formatted=true" + "&postalcode=" + zipcode +  "&country=US&" + "radius=" + distance + "&username=gardenshift&" +"style=full";
 	      
     	   	 URL url = new URL(RESTCall);
 	
 	         URLConnection conn = url.openConnection();
 	
 	         BufferedReader in = new BufferedReader(new
 	         InputStreamReader(conn.getInputStream()));
 	        
 	         while ((res = in.readLine()) != null) {
 	
 	         result += res;
 
       }
      
      } catch (IOException e) {
          // TODO Auto-generated catch block
      Response.status(500);
      }
       
      
 
       return Response.status(200).entity(result).build();
 
       }
 
 @GET
 @Path("/search/{zipcode}/{distance}/{cropname}")
 @Produces("application/json")
 public Response search_user_Crop(@PathParam("zipcode") String zipcode, @PathParam("distance") String distance, @PathParam("cropname") String cropname) throws Exception
 {
 
 /*
 * Displays all the users which are within the given radius
 */
 
 		  String URI = "http://api.geonames.org/findNearbyPostalCodesJSON?";
 				
 	      String RESTCall ="";
 	      String res ="";
 	      String result="";
       
     
     
     
 
          RESTCall = URI + "formatted=true" + "&postalcode=" + zipcode +  "&country=US&" + "radius=" + distance + "&username=gardenshift&" +"style=full";
 
          URL url = new URL(RESTCall);
 
          URLConnection conn = url.openConnection();
 
          BufferedReader in = new BufferedReader(new
          InputStreamReader(conn.getInputStream()));
                  
          
        
         
          while ((res = in.readLine()) != null) {
 
          result += res;
 
       }
          
      
          ArrayList<String> zip = new ArrayList();
        
          JsonElement json = new JsonParser().parse(result);
 
          JsonObject obj= json.getAsJsonObject();
          
          JsonArray jarray = obj.getAsJsonArray("postalCodes");
          
          
        
          for( int i=0; i < obj.getAsJsonArray("postalCodes").size(); i++)
          {
         	 
         	 JsonObject jobject = jarray.get(i).getAsJsonObject();
         	 String result1 = jobject.get("postalCode").getAsString();
         	 
         	 zip.add(result1);
         	 
         	    
         	       
          }
          
          
         String msg = "[";
          
         BasicDBObject keys = new BasicDBObject();
 			
 		DBCollection collection = db.getCollection("users");
 		
 		keys.put("username", 1);
 		keys.put("email", 1);
 		keys.put("zipcode", 1);
 		keys.put("user_crops.crop_name", 1);
 		
 		List<BasicDBObject> searchQuery = new ArrayList<BasicDBObject>();
 
 		
 		BasicDBObject filteredZip = new BasicDBObject(); 
         
         filteredZip.put("zipcode", new BasicDBObject("$in", zip) );
          
         searchQuery.add(new BasicDBObject("user_crops.crop_name",java.util.regex.Pattern.compile(cropname)));
  		searchQuery.add(filteredZip);
  		
  		BasicDBObject sQuery = new BasicDBObject();
  		sQuery.put("$and", searchQuery);
          
  		DBCursor cursor =  collection.find(sQuery, keys);
          
          if(cursor.hasNext() == false)
          {
              return Response.status(200).entity("null").build();
          }
          
          while (cursor.hasNext()) {
              msg = msg + cursor.next() + ",";
          }
          
          msg = msg.substring(0, msg.length() - 1);
  		 msg += "]";
 
 
          return Response.status(200).entity(msg).build();
 } 
 
 @Path("send_notification")
 @POST
 	public Response send_notification(@FormParam("username") String username,
 			@FormParam("type") String type, @FormParam("from") String from,
 			@FormParam("text") String text) {
 
 	/*
 	 * sends a notification to the user.
 	 */
 
 	
 	try {
 		
 		DBCollection collection = db.getCollection("users");
         BasicDBObject sendNotif = new BasicDBObject();
         sendNotif.put("username", username);
 
        
         BasicDBObject document = new BasicDBObject();
         
             
             document.put("type", type);
             document.put("from",from);
             document.put("text",text);
             document.put("timestamp",System.currentTimeMillis());
             
             BasicDBObject temp = new BasicDBObject();
             temp.put("$push", new BasicDBObject("notifications_unread", document));
 
             collection.update(sendNotif, temp, true, true);
 
             return Response.status(200).entity("success").build();
 	}catch(Exception e){return Response.status(500).entity("failed").build();}
 
 }
 @Path("update_notification_to_read/{username}/{timestamp}")
 @GET
 	public Response update_notification(@PathParam("username") String username,
 			@PathParam("timestamp") Long timestamp
 			) {
 
 	/*
 	 * sends a notification to the user.
 	 */
 
 	
 	try {
 			
 			BasicDBObject searchQuery = new BasicDBObject();
 			
 			BasicDBObject keys = new BasicDBObject();
 			
 			DBCollection collection = db.getCollection("users");
 		
 			keys.put("notifications_unread", 1);
 			
 			searchQuery.put("username",username);
 			DBCursor cursor = collection.find(searchQuery, keys);
 
 			while (cursor.hasNext()) {
 				
 					BasicDBObject result = (BasicDBObject) cursor.next();
 					int i = result.size();
 				
 					@SuppressWarnings("unchecked")
 					ArrayList<BasicDBObject> notifs =
 					(ArrayList<BasicDBObject>)result.get("notifications_unread"); // * See Note
 					for(BasicDBObject embedded : notifs){
 					Long ts = (Long)embedded.get("timestamp");
 					if(ts.equals(timestamp)){
 					String from = (String)embedded.get("from");
 					String type = (String)embedded.get("type");
 					String text = (String)embedded.get("text");
 					
 					Mongo mongo1 = new Mongo("127.3.119.1", 27017);
 					//Mongo mongo1 = new Mongo("localhost",27017);
 					DB db1 = mongo1.getDB("gardenshift");
 					db1.authenticate("admin", "redhat".toCharArray());
 					DBCollection collection1 = db1.getCollection("users");
 			        BasicDBObject updNotif = new BasicDBObject();
 			        updNotif.put("username", username);
 			        BasicDBObject document1 = new BasicDBObject();
 		            document1.put("type", type);
 		            document1.put("from",from);
 		            document1.put("text",text);
 		            document1.put("timestamp",timestamp);
 		            BasicDBObject temp = new BasicDBObject();
 		            temp.put("$push", new BasicDBObject("notifications_read", document1));
 		            collection.update(updNotif, temp, true, true);
 		            
 //		        	Mongo mongo2 = new Mongo("127.3.119.1", 27017);
 //					//Mongo mongo2 = new Mongo("localhost",27017);
 //					DB db2 = mongo1.getDB("gardenshift");
 //					db2.authenticate("admin", "redhat".toCharArray());
 					DBCollection collection2 = db.getCollection("users");
 					BasicDBObject updateNotif = new BasicDBObject();
 					updateNotif.put("username", username);
 		
 					BasicDBObject document2 = new BasicDBObject();
 		
 					document2.put("timestamp", timestamp);
 		
 					BasicDBObject temp1 = new BasicDBObject();
 							
 				temp1.put("$pull", new BasicDBObject("notifications_unread",	document2));
 					collection2.update(updateNotif, temp1, true, true);
 		            
 		            
 						
 					}
 					
 					}
 			}
 
 		
             return Response.status(200).entity("success").build();
 	}catch(Exception e){return Response.status(500).entity(e).build();}
 
 }
 
 @Path("get_notification_unread/{username}")
 @GET
 	public Response get_notification_unread(@PathParam("username") String username) {
 
 	/*
 	 * gives all the unread notifications of a user.
 	 */
 
 	
 	try {
 		
 		DBCollection collection = db.getCollection("users");
         BasicDBObject getNotif = new BasicDBObject();
         getNotif.put("username", username);
 
         BasicDBObject keys = new BasicDBObject();
         keys.put("notifications_unread" ,1 );
         DBCursor cursor = collection.find( getNotif, keys);
         
         String msg = "";
         while (cursor.hasNext()) {
     			msg += cursor.next() ;
     		}
 
     	return Response.status(200).entity(msg).build();
 	}catch(Exception e){return Response.status(500).entity("failed").build();}
 
 }
 
 
 
 @Path("get_notification_read/{username}")
 @GET
 	public Response get_notification_read(@PathParam("username") String username) {
 
 	/*
 	 * gives all the unread notifications of a user.
 	 */
 
 	
 	try {
 		
 		DBCollection collection = db.getCollection("users");
         BasicDBObject getNotif = new BasicDBObject();
         getNotif.put("username", username);
 
         BasicDBObject keys = new BasicDBObject();
         keys.put("notifications_read" ,1 );
         DBCursor cursor = collection.find( getNotif, keys);
         
         String msg = "";
 
     		while (cursor.hasNext()) {
     			msg += cursor.next() ;
     		}
 
             return Response.status(200).entity(msg).build();
 	}catch(Exception e){return Response.status(500).entity("failed").build();}
 
 }
 @Path("delete_notification_read/{username}/{timestamp}")
 @GET
 	public Response delete_notification_read(@PathParam("username") String username,@PathParam("timestamp") Long timestamp) {
 
 	/*
 	 * deletes a particular read notification of a user.
 	 */
 
 	
 	try {
 		
 		DBCollection collection2 = db.getCollection("users");
 		BasicDBObject updateNotif = new BasicDBObject();
 		updateNotif.put("username", username);
 
 		BasicDBObject document2 = new BasicDBObject();
 
 		document2.put("timestamp", timestamp);
 
 		BasicDBObject temp1 = new BasicDBObject();
 				
 	temp1.put("$pull", new BasicDBObject("notifications_read",	document2));
 		collection2.update(updateNotif, temp1, true, true);
 		return Response.status(200).entity("success").build();
 		
 	}catch(Exception e){return Response.status(500).entity("failed").build();}
 
 }
 @Path("delete_notification_unread/{username}/{timestamp}")
 @GET
 	public Response delete_notification_unread(@PathParam("username") String username,@PathParam("timestamp") Long timestamp) {
 
 	/*
 	 * deletes a particular unread notification of a user.
 	 */
 
 	
 	try {
 		
 		DBCollection collection2 = db.getCollection("users");
 		BasicDBObject updateNotif = new BasicDBObject();
 		updateNotif.put("username", username);
 
 		BasicDBObject document2 = new BasicDBObject();
 
 		document2.put("timestamp", timestamp);
 
 		BasicDBObject temp1 = new BasicDBObject();
 				
 	temp1.put("$pull", new BasicDBObject("notifications_unread",	document2));
 		collection2.update(updateNotif, temp1, true, true);
 		return Response.status(200).entity("success").build();
 		
 	}catch(Exception e){return Response.status(500).entity("failed").build();}
 
 }
 @Path("add_bulletin")
 @POST
 	public Response add_bulletin(@FormParam("username") String username,@FormParam("text") String text) {
 
 	/*
 	 * updates the bulletin.
 	 */
 
 	
 	try {
 		
 		DBCollection collection = db.getCollection("users");
         BasicDBObject sendNotif = new BasicDBObject();
         sendNotif.put("username", username);
 
        
         BasicDBObject document = new BasicDBObject();
         
             
             document.put("text", text);
             
             BasicDBObject temp = new BasicDBObject();
             temp.put("$push", new BasicDBObject("bulletin", document));
 
             collection.update(sendNotif, temp, true, true);
 
             return Response.status(200).entity("success").build();
 	}catch(Exception e){return Response.status(500).entity("failed").build();}
 }
 
 @Path("flush_bulletin/{username}")
 @GET
 	public Response flush_bulletin(@PathParam("username") String username) {
 
 	/*
 	 * updates the bulletin.
 	 */
 
 	
 	try {
 		
 		BasicDBObject searchQuery = new BasicDBObject();
 		
 		BasicDBObject keys = new BasicDBObject();
 		
 		DBCollection collection = db.getCollection("users");
 	
 		keys.put("bulletin", 1);
 		
 		searchQuery.put("username",username);
 		DBCursor cursor = collection.find(searchQuery, keys);
 
 		while (cursor.hasNext()) {
 			
 				BasicDBObject result = (BasicDBObject) cursor.next();
 				int i = result.size();
 			
 				@SuppressWarnings("unchecked")
 				ArrayList<BasicDBObject> bulletins =
 				(ArrayList<BasicDBObject>)result.get("bulletin"); // * See Note
 				for(BasicDBObject embedded : bulletins){
 				
 				String text = (String)embedded.get("text");
 				
 				Mongo mongo1 = new Mongo("127.3.119.1", 27017);
 				//Mongo mongo1 = new Mongo("localhost",27017);
 				DB db1 = mongo1.getDB("gardenshift");
 				db1.authenticate("admin", "redhat".toCharArray());
 				DBCollection collection1 = db1.getCollection("users");
 		        BasicDBObject updNotif = new BasicDBObject();
 		        updNotif.put("username", username);
 		        BasicDBObject document1 = new BasicDBObject();
 	            
 	            document1.put("text",text);
 	            
 	            BasicDBObject temp = new BasicDBObject();
 	            temp.put("$push", new BasicDBObject("bulletin_archive", document1));
 	            collection.update(updNotif, temp, true, true);
 	            
 //	        	Mongo mongo2 = new Mongo("127.3.119.1", 27017);
 //				//Mongo mongo2 = new Mongo("localhost",27017);
 //				DB db2 = mongo1.getDB("gardenshift");
 //				db2.authenticate("admin", "redhat".toCharArray());
 				DBCollection collection2 = db.getCollection("users");
 				BasicDBObject updateNotif = new BasicDBObject();
 				updateNotif.put("username", username);
 	
 				BasicDBObject document2 = new BasicDBObject();
 	
 	
 				BasicDBObject temp1 = new BasicDBObject();
 						
 			temp1.put("$pull", new BasicDBObject("bulletin",document1));
 				collection2.update(updateNotif, temp1, true, true);
 				
 				}
 		}
 		return Response.status(200).entity("success").build();
 	}catch(Exception e){return Response.status(500).entity("failed").build();}}
 
 @Path("get_bulletin/{username}")
 @GET
 	public Response get_bulletin(@PathParam("username") String username) {
 
 	/*
 	 * gives all the bulletin notifications of a user.
 	 */
 
 	
 	try {
 		
 		DBCollection collection = db.getCollection("users");
         BasicDBObject getNotif = new BasicDBObject();
         getNotif.put("username", username);
 
         BasicDBObject keys = new BasicDBObject();
         keys.put("bulletin" ,1 );
         DBCursor cursor = collection.find( getNotif, keys);
         
         String msg = "";
 
     		while (cursor.hasNext()) {
     			msg += cursor.next() ;
     		}
 
             return Response.status(200).entity(msg).build();
 	}catch(Exception e){return Response.status(500).entity("failed").build();}
 
 }
 @Path("get_bulletin_archive/{username}")
 @GET
 	public Response get_bulletin_archive(@PathParam("username") String username) {
 
 	/*
 	 * gives all the bulletin archive notifications of a user.
 	 */
 
 	
 	try {
 		
 		DBCollection collection = db.getCollection("users");
         BasicDBObject getNotif = new BasicDBObject();
         getNotif.put("username", username);
 
         BasicDBObject keys = new BasicDBObject();
         keys.put("bulletin_archive" ,1 );
         DBCursor cursor = collection.find( getNotif, keys);
         
         String msg = "";
 
     		while (cursor.hasNext()) {
     			msg += cursor.next() ;
     		}
 
             return Response.status(200).entity(msg).build();
 	}catch(Exception e){return Response.status(500).entity("failed").build();}
 
 }
 @Path("get_bulletin_count/{username}")
 @GET
 	public Response get_bulletin_count(@PathParam("username") String username) {
 
 	/*
 	 * gives the bulletin  notifications count of a user.
 	 */
 
 	
 	try {
 		
 		DBCollection collection = db.getCollection("users");
         BasicDBObject getNotif = new BasicDBObject();
         getNotif.put("username", username);
 
         BasicDBObject keys = new BasicDBObject();
         keys.put("bulletin" ,1 );
         DBCursor cursor = collection.find( getNotif, keys);
         
         int count=0;
 
     		while (cursor.hasNext()) {
     			count+=1 ;
     		}
 
             return Response.status(200).entity(count).build();
 	}catch(Exception e){return Response.status(500).entity("null").build();}
 
 }
 
 	@Path("add_friends")
 	@POST
 	public Response addfriends(@FormParam("username") String username,
 			@FormParam("friend_name") String friend_name) {
 
 		/*
 		 * Add a new friend request to user's database
 		 */
 
 		try {
 			DBCollection collection = db.getCollection("users");
 			BasicDBObject update = new BasicDBObject();
 			update.put("username", friend_name);
 
 			BasicDBObject document = new BasicDBObject();
 
 			document.put("friends_username", username);
 			document.put("status", "pending");
 
 			BasicDBObject temp = new BasicDBObject();
 			temp.put("$push", new BasicDBObject("friends", document));
 
 			collection.update(update, temp, true, true);
 			
 			
 			BasicDBObject update1 = new BasicDBObject();
 			update1.put("username", username);
 
 			BasicDBObject document1 = new BasicDBObject();
 
 			document1.put("friends_username", friend_name);
 			document1.put("status", "pending");
 
 			BasicDBObject temp1 = new BasicDBObject();
 			temp1.put("$push", new BasicDBObject("friends", document1));
 
 			collection.update(update1, temp1, true, true);
 
 			return Response.status(200).entity("success").build();
 
 		} catch (Exception e) {
 			return Response.status(503).entity("failed").build();
 		}
 
 	}
 
 	@Path("accept_friends")
 	@POST
 	public Response acceptFriends(@FormParam("username") String username,
 			@FormParam("friend_name") String friend_name) {
 
 		/*
 		 * Accepts invitation of a friend and updates the user's database to
 		 * reflect the change in the friends
 		 */
 
 		try {
 			DBCollection collection = db.getCollection("users");
 
 			BasicDBObject update = new BasicDBObject();
 			update.put("username", username);
 
 			BasicDBObject document = new BasicDBObject();
 
 			document.put("friends_username", friend_name);
 
 			BasicDBObject temp = new BasicDBObject();
 			temp.put("$pull", new BasicDBObject("friends", document));
 
 			collection.update(update, temp, true, true);
 
 			BasicDBObject update1 = new BasicDBObject();
 			update1.put("username", username);
 
 			BasicDBObject document1 = new BasicDBObject();
 
 			document1.put("friends_username", friend_name);
 			document1.put("status", "accepted");
 
 			BasicDBObject temp1 = new BasicDBObject();
 			temp1.put("$push", new BasicDBObject("friends", document1));
 
 			collection.update(update1, temp1, true, true);
 
 			BasicDBObject update2 = new BasicDBObject();
 			update2.put("username", friend_name);
 
 			BasicDBObject document2 = new BasicDBObject();
 
 			document2.put("friends_username", username);
 			document2.put("status", "accepted");
 
 			BasicDBObject temp2 = new BasicDBObject();
 			temp2.put("$push", new BasicDBObject("friends", document2));
 
 			collection.update(update2, temp2, true, true);
 
 			return Response.status(200).entity("success").build();
 
 		} catch (Exception e) {
 			return Response.status(503).entity("failed").build();
 		}
 
 	}
 	@Path("add_feedback")
 	@POST
 	public Response addFeedback(@FormParam("from") String from, @FormParam("to") String to,
 			@FormParam("status_txt") String status_txt )
 			 {
 
 		/*
 		 * Add a new status to user's database
 		 */
 
 		
 		try {
 					DBCollection collection = db.getCollection("users");
 					BasicDBObject update = new BasicDBObject();
 		            update.put("username", to);
 	
 		           
 		            BasicDBObject document = new BasicDBObject();
 	            
 	                document.put("text", status_txt);
 	                document.put("from", from);               
 	                
 	                BasicDBObject temp = new BasicDBObject();
 	                temp.put("$push", new BasicDBObject("feedback", document));
 
 	                collection.update(update, temp, true, true);
 
 	                return Response.status(200).entity("success").build();
 
 		} catch (Exception e) {
 			return Response.status(503).entity("failed").build();
 		}
 
 	}
 
 }
