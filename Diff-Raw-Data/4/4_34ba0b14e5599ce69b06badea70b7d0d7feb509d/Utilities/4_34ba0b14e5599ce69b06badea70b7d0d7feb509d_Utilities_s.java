 package controllers;
 
 import static play.libs.Json.toJson;
 
 import java.io.IOException;
 import java.util.List;
 
 import be.objectify.deadbolt.java.actions.SubjectPresent;
 
 import models.User;
 import play.Logger;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Security;
 import play.mvc.Result;
 import pojos.CityBean;
 import pojos.FileBean;
 import pojos.ResponseStatusBean;
 
 import delegates.UtilitiesDelegate;
 import enums.ResponseStatus;
 
 public class Utilities extends Controller {
 
 	public static Form<FileBean> fileForm = Form.form(FileBean.class);;
 
 	public static Result getCities() {
 		List<CityBean> bean = UtilitiesDelegate.getInstance().getCities();
 		return bean != null ? ok(toJson(bean)) : notFound();
 	}
 
 	public static Result getCitiesByCountryName(String country) {
 		List<CityBean> bean = UtilitiesDelegate.getInstance()
 				.getCitiesByCountryName(country);
 		return bean != null ? ok(toJson(bean)) : notFound();
 	}
 
 	public static Result getCitiesByCountryNameAndRegion(String country,
 			String region) {
 		List<CityBean> bean = UtilitiesDelegate.getInstance()
 				.getCitiesByCountryNameAndRegion(country, region);
 		return bean != null ? ok(toJson(bean)) : notFound();
 	}
 
 	public static Result getCityById(Long cityId) {
 		CityBean bean = UtilitiesDelegate.getInstance().getCitiesById(cityId);
 		return bean != null ? ok(toJson(bean)) : notFound();
 	}
 
 	public static Result getNewCities(Long lastCityId) {
 		List<CityBean> bean = UtilitiesDelegate.getInstance().getNewCities(
 				lastCityId);
 		return bean != null ? ok(toJson(bean)) : notFound();
 	}
 
 	public static Result getCitiesByName(String name) {
 		List<CityBean> bean = UtilitiesDelegate.getInstance().getCityByName(
 				name);
 		return bean != null ? ok(toJson(bean)) : notFound();
 	}
 
 	public static Result testUpload() {
 		return ok(views.html.upload.render());
 	}
 
 	// TODO merge file upload with posting of memento
 	@Security.Authenticated(Secured.class)
 	public static Result upload() {
 		Form<FileBean> filledForm = fileForm.bindFromRequest();
 
 		String userEmail = session().get("pa.u.id");
 		User user = User.getByEmail(userEmail);
 
 		if (filledForm.hasErrors()) {
 			ResponseStatusBean response = new ResponseStatusBean();
 			response.setResponseStatus(ResponseStatus.BADREQUEST);
 			response.setStatusMessage("play.authenticate.filledFromHasErrors:"
 					+ filledForm.errorsAsJson());
 			return badRequest(toJson(response));
 		} else {
 			play.mvc.Http.MultipartFormData body = request().body()
 					.asMultipartFormData();
 			play.mvc.Http.MultipartFormData.FilePart file = body
 					.getFile("file") == null ? body.getFile("files[]") : body
 					.getFile("file");
 			FileBean fileBean = filledForm.get() == null ? new FileBean()
 					: filledForm.get();
 
 			if (user != null) {
 				Logger.debug("User " + user.getEmail() + " posted a file");
 				fileBean.setOwner(user.getUserId());
 			} else {
 				// TODO removed once authentication is fully tested, @security
 				// should be enough
 				flash("error", "Missing file");
 				ResponseStatusBean response = new ResponseStatusBean();
 				response.setResponseStatus(ResponseStatus.UNAUTHORIZED);
 				response.setStatusMessage("User is information is null");
 				return unauthorized(toJson(response));
 			}
 
 			if (file != null) {
 				try {
 					fileBean = UtilitiesDelegate.getInstance().saveFile(file,
 							fileBean);
 				} catch (IOException e) {
 					flash("error", "Error saving file in disk");
 					ResponseStatusBean response = new ResponseStatusBean();
 					response.setResponseStatus(ResponseStatus.SERVERERROR);
 					response.setStatusMessage(e.getMessage());
 					return internalServerError(toJson(response));
 				}
 				return ok(toJson(fileBean));
 			} else {
 				flash("error", "Missing file");
 				ResponseStatusBean response = new ResponseStatusBean();
 				response.setResponseStatus(ResponseStatus.BADREQUEST);
 				response.setStatusMessage("File is null");
 				return badRequest(toJson(response));
 			}
 		}
 	}
 
 	@SubjectPresent
 	public static Result getFile(String hashcode) {
 		String userEmail = session().get("pa.u.id");
 		User user = User.getByEmail(userEmail);
 
 		if (user != null) {
 			java.io.File f = UtilitiesDelegate.getInstance().getFile(hashcode,
 					user.getUserId());
 			if (f != null) {
 				return ok(f);
 			} else {
 				// TODO remove once authentication is fully tested, @security
 				// should be enough
 				flash("error", "Missing file");
 				ResponseStatusBean response = new ResponseStatusBean();
 				response.setResponseStatus(ResponseStatus.NOTAVAILABLE);
 				response.setStatusMessage("File does not exist");
 				return notFound(toJson(response));
 			}
 		} else {
 			// TODO remove once authentication is fully tested, @security
 			// should be enough
 			flash("error", "Missing file");
 			ResponseStatusBean response = new ResponseStatusBean();
 			response.setResponseStatus(ResponseStatus.UNAUTHORIZED);
 			response.setStatusMessage("User is information is null");
 			return unauthorized(toJson(response));
 		}
 	}
 
 	@SubjectPresent
 	public static Result getFileBySize(String hashcode, String size) {
 		String userEmail = session().get("pa.u.id");
 		User user = User.getByEmail(userEmail);
 		String upSize = size.toUpperCase();
 
 		if (!upSize.equals("THUMBNAIL") && !upSize.equals("SMALL")
 				&& !upSize.equals("MEDIUM") && !upSize.equals("LARGE")) {
 			flash("error", "Size " + size + " not available");
 			ResponseStatusBean response = new ResponseStatusBean();
 			response.setResponseStatus(ResponseStatus.NOTAVAILABLE);
 			response.setStatusMessage("Size " + size + "not available");
 			return notFound(toJson(response));
 		}
 
 		if (user != null) {
 			java.io.File f = UtilitiesDelegate.getInstance().getFile(hashcode,
 					user.getUserId(), size);
 			if (f != null) {
 				return ok(f);
 			} else {
 				// TODO remove once authentication is fully tested, @security
 				// should be enough
 				flash("error", "Missing file");
 				ResponseStatusBean response = new ResponseStatusBean();
 				response.setResponseStatus(ResponseStatus.NOTAVAILABLE);
 				response.setStatusMessage("Missing file");
 				return notFound(toJson(response));
 			}
 		} else {
 			// TODO remove once authentication is fully tested, @security
 			// should be enough
 			flash("error", "Missing file");
 			ResponseStatusBean response = new ResponseStatusBean();
 			response.setResponseStatus(ResponseStatus.UNAUTHORIZED);
 			response.setStatusMessage("User is information is null");
 			return unauthorized(toJson(response));
 		}
 	}
 
 	public static Result getFileNoLogin(String hashcode) {
 		java.io.File f = UtilitiesDelegate.getInstance().getFileNoLogin(
 				hashcode);
 		if (f != null) {
 			return ok(f);
 		} else {
 			// TODO remove once authentication is fully tested, @security
 			// should be enough
 			flash("error", "Missing file");
 			ResponseStatusBean response = new ResponseStatusBean();
 			response.setResponseStatus(ResponseStatus.NOTAVAILABLE);
 			response.setStatusMessage("File does not exist");
 			return notFound(toJson(response));
 		}
 
 	}
 
 	public static Result getFileBySizeNoLogin(String hashcode, String size) {
 		String upSize = size.toUpperCase();
 
		if (!upSize.equals("THUMBNAIL") && !upSize.equals("MEDIUM")
				&& !upSize.equals("LARGE")) {
 			flash("error", "Size " + size + " not available");
 			ResponseStatusBean response = new ResponseStatusBean();
 			response.setResponseStatus(ResponseStatus.NOTAVAILABLE);
 			response.setStatusMessage("Size " + size + "not available");
 			return notFound(toJson(response));
 		}
 
 		java.io.File f = UtilitiesDelegate.getInstance().getFileNoLogin(
 				hashcode, size);
 		if (f != null) {
 			return ok(f);
 		} else {
 			// TODO remove once authentication is fully tested, @security
 			// should be enough
 			flash("error", "Missing file");
 			ResponseStatusBean response = new ResponseStatusBean();
 			response.setResponseStatus(ResponseStatus.NOTAVAILABLE);
 			response.setStatusMessage("Missing file");
 			return notFound(toJson(response));
 		}
 	}
 
 }
