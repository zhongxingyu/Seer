 /**
  * 
  */
 package controllers.classgroups;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import models.EMessages;
 import models.classgroups.ClassGroupContainer;
 import models.data.Link;
 import models.user.AuthenticationManager;
 
 import play.cache.Cache;
 import play.mvc.Http.MultipartFormData;
 import play.mvc.Http.MultipartFormData.FilePart;
 import play.mvc.Result;
 import views.html.classes.uploadclass;
 import views.html.commons.noaccess;
 import controllers.EController;
 import controllers.util.XLSXImporter;
 
 /**
  * @author Jens N. Rammant
  * TODO emessages & decent errors & comments
  */
 public class ClassGroupIOController extends EController {
 
 	/**
 	 * 
 	 * @return the Upload page
 	 */
 	public static Result upload(){
 		return uploadExisting(null);
 	}
 	
 	/**
 	 * Parses the data and shows it
 	 * @return a page with the parsed data
 	 */
 	public static Result post(){
 		return postExisting(null);
 	}
 	
 	/**
 	 * Saves the data
 	 * @param id of the class
 	 * @return page showing the classes or class (dependent on whether there's a class ID)
 	 */
 	public static Result save(String id){
 		return saveExisting(null,id);
 	}
 	
 	/**
 	 * 
 	 * @param id of the Class. Null if it should not be added to existing classes
 	 * @return upload page
 	 */
 	public static Result uploadExisting(Integer id){
 		List<Link> bc = ClassGroupController.getBreadcrumbs();
 		bc.add(new Link("Upload","/classes/upload"));
 		if(!ClassGroupController.isAuthorized())return ok(noaccess.render(bc));		
 		return ok(uploadclass.render(bc,null,null,id));
 	}
 	
 	/**
 	 * 
 	 * @param classID
 	 *            id of the Class. Null if it should not be added to existing
 	 *            classes
 	 * @return a page showing the parsed data
 	 */
 	public static Result postExisting(Integer classID) {
 		List<Link> bc = ClassGroupController.getBreadcrumbs();
 		bc.add(new Link("Upload", "/classes/upload"));
 		// Check if authorized
 		if (!ClassGroupController.isAuthorized())
 			return ok(noaccess.render(bc));
 
 		// retrieve the posted file
 		MultipartFormData body = request().body().asMultipartFormData();
 		FilePart xlsx = body.getFile("xlsx");
 		File file = null;
 		try {
 			//Parse the excel data to a list
 			file = xlsx.getFile();
 			List<List<String>> list = XLSXImporter.read(file);
 			file.delete();
 			List<ClassGroupContainer> cg;
 			//parse data to ClassGroupContainer
 			if (classID == null) {
 				cg = ClassGroupIO.listToClassGroup(list);
 			} else {
 				ClassGroupContainer cgc = ClassGroupIO.listToClassGroup(list,
 						classID);
 				cg = new ArrayList<ClassGroupContainer>();
 				cg.add(cgc);
 			}
 			//Create a key to save the parsed data in the cache
 			String id = AuthenticationManager.getInstance().getUser().getID()
 					+ "-";
 			Random r = new Random();
 			id = id + Integer.toString(r.nextInt(10000));
 			//Save in the cache, expire after 3 hours
 			Cache.set(id, cg,10800);
 			//Return the parsed data on a page
 			return ok(uploadclass.render(bc, cg, id, classID));
 		} catch (Exception e) {
 			//Make sure the file is deleted
 			try{
 				file.delete();
 			}catch(Exception ee){}
 			//Something went wrong with the parsing, show page with error
 			flash("error", EMessages.get("classes.import.error.postfail"));
 			if (classID == null) {
 				return redirect(controllers.classgroups.routes.ClassGroupController
 						.viewClasses(0, "name", "asc", ""));
 			} else {
 				return redirect(controllers.classgroups.routes.ClassPupilController
 						.viewClass(classID, 0, "name", "asc", ""));
 			}
 		}
 	}
 	
 	/**
 	 * Save the posted data
 	 * @param id of the class. Null if it should not be added to existing class
 	 * @param dataid id under which the data is saved in the cache
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static Result saveExisting(Integer id, String dataid){
 		List<Link> bc = ClassGroupController.getBreadcrumbs();
 		bc.add(new Link("Upload","/classes/upload"));	
 		//Check if authorized
 		if(!ClassGroupController.isAuthorized())return ok(noaccess.render(bc));			
 		List<ClassGroupContainer> cgc = null;
 		//Try retrieving the data from the cache
 		try{
 			cgc = (List<ClassGroupContainer>) Cache.get(dataid);
 			cgc.getClass(); //Throws exception when null;
 		}catch(Exception e){
 			//Something went wrong with the retrieval of the data. Could be because it expired
 			flash("error", EMessages.get("classes.import.error.savefail"));
 		}
 		ClassGroupContainer.save(cgc);
 		
 		if(id==null){
 			return redirect(controllers.classgroups.routes.ClassGroupController.viewClasses(0, "name", "asc", ""));
 		}else{
 			return redirect(controllers.classgroups.routes.ClassPupilController.viewClass(id, 0, "name", "asc", ""));
 		}
 	}
 }
