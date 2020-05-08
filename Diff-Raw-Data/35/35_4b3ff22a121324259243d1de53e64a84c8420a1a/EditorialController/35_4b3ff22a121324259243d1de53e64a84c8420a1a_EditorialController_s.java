 package org.netvogue.server.webmvc.controllers;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.netvogue.server.aws.core.ImageType;
 import org.netvogue.server.aws.core.UploadManager;
 import org.netvogue.server.neo4japi.common.ResultStatus;
 import org.netvogue.server.neo4japi.common.USER_TYPE;
 import org.netvogue.server.neo4japi.domain.EditorialPhoto;
 import org.netvogue.server.neo4japi.domain.User;
 import org.netvogue.server.neo4japi.service.EditorialService;
 import org.netvogue.server.neo4japi.service.UserService;
 import org.netvogue.server.webmvc.domain.CampaignJSONRequest;
 import org.netvogue.server.webmvc.domain.Editorial;
 import org.netvogue.server.webmvc.domain.Editorials;
 import org.netvogue.server.webmvc.domain.ImageURLsResponse;
 import org.netvogue.server.webmvc.domain.JsonRequest;
 import org.netvogue.server.webmvc.domain.JsonResponse;
 import org.netvogue.server.webmvc.domain.PhotoInfoJsonRequest;
 import org.netvogue.server.webmvc.domain.PhotoWeb;
 import org.netvogue.server.webmvc.domain.Photos;
 import org.netvogue.server.webmvc.domain.UploadedFileResponse;
 import org.netvogue.server.webmvc.security.NetvogueUserDetailsService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.convert.ConversionService;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 
 @Controller
 public class EditorialController {
 	@Autowired NetvogueUserDetailsService 	userDetailsService;
 	@Autowired UserService 					userService;
 	@Autowired EditorialService				editorialService;
 	@Autowired ConversionService			conversionService;
 
 	@Autowired
 	private UploadManager uploadManager;
 
	@RequestMapping(value={"geteditorials", "geteditorials/profileid"}, method=RequestMethod.GET)
 	public @ResponseBody Editorials GetEditorials(@ModelAttribute("profileid") String profileid, 
 												@RequestParam("galleryname") String galleryname) {
 		System.out.println("Get Editorials: " + galleryname);
 		Editorials campaigns = new Editorials();
 		
 		User user;
 		if(!profileid.isEmpty()) {
 			user = userService.getUserByUsername(profileid);
 			if(user == null) {
 				return campaigns;
 			}
 		} else {
 			 user = userDetailsService.getUserFromSession();
 		}
 		
 		campaigns.setName(user.getName());
 		campaigns.setIsbrand(USER_TYPE.BRAND == user.getUserType()?true:false);
 		campaigns.setProfilepic(conversionService.convert(user.getProfilePicLink(), ImageURLsResponse.class));
 		Set<Editorial> campaignTemp = new LinkedHashSet<Editorial>();
 		Iterable<org.netvogue.server.neo4japi.domain.Editorial> dbCampaigns;
 		if(galleryname.isEmpty()) {
 			dbCampaigns = userService.getEditorials(user);
 		} else {
 			dbCampaigns = userService.searchEditorialByName(user, galleryname);
 		}
 		if(null == dbCampaigns) {
 			return campaigns;
 		}
 		Iterator<org.netvogue.server.neo4japi.domain.Editorial> first = dbCampaigns.iterator();
 		while ( first.hasNext() ){
 			org.netvogue.server.neo4japi.domain.Editorial dbCampaign = first.next() ;
 			System.out.println("Get Editorial" + dbCampaign.getEditorialname());
 			campaignTemp.add(conversionService.convert(dbCampaign, Editorial.class));
 		}
 		campaigns.setGalleries(campaignTemp);
 		
 		return campaigns;
 	}
 	
	@RequestMapping(value={"editorial/getphotos", "editorial/getphotos/profileid"}, method=RequestMethod.GET)
 	public @ResponseBody Photos GetPhotos(@ModelAttribute("profileid") String profileid, 
 										  @RequestParam("galleryid") String galleryid,
 										  @RequestParam("photoname") String photoname
 											 ) {
 		System.out.println("Get editorial Photos: " + photoname);
 		Photos photos = new Photos();
 		if(galleryid.isEmpty()) {
 			return photos;
 		}
 		
 		User user;
 		if(!profileid.isEmpty()) {
 			user = userService.getUserByUsername(profileid);
 			if(user == null) {
 				return photos;
 			}
 		} else {
 			 user = userDetailsService.getUserFromSession();
 		}
 		
 		photos.setName(user.getName());
 		photos.setIsbrand(USER_TYPE.BRAND == user.getUserType()?true:false);
 		photos.setProfilepic(conversionService.convert(user.getProfilePicLink(), ImageURLsResponse.class));
 		photos.setGalleryname(editorialService.getEditorial(galleryid).getEditorialname());
 		Set<PhotoWeb> photosTemp = new LinkedHashSet<PhotoWeb>();
 		Iterable<EditorialPhoto> dbPhotos;
 		if(photoname.isEmpty()) {
 			dbPhotos = editorialService.getPhotos(galleryid);
 		} else {
 			dbPhotos = editorialService.searchPhotoByName(galleryid, photoname);
 		}
 		if(null == dbPhotos) {
 			return photos;
 		}
 		Iterator<EditorialPhoto> first = dbPhotos.iterator();
 		while ( first.hasNext() ){
 			EditorialPhoto dbPhoto = first.next() ;
 			photosTemp.add(conversionService.convert(dbPhoto, PhotoWeb.class));
 		}
 		photos.setPhotos(photosTemp);
 			
 		return photos;
 	}
 	
 	@RequestMapping(value="editorial/create", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse CreateEditorial(@RequestBody CampaignJSONRequest request) {
 		System.out.println("Create Editorial");
 		StringBuffer error = new StringBuffer();
 		User loggedinUser = userDetailsService.getUserFromSession();
 		org.netvogue.server.neo4japi.domain.Editorial newEditorial = 
 				new org.netvogue.server.neo4japi.domain.Editorial(request.getName(), request.getDesc(), loggedinUser);
 		
 		JsonResponse response = new JsonResponse();
 		
 		if(ResultStatus.SUCCESS == editorialService.SaveEditorial(newEditorial, error)) {  
 			response.setStatus(true);
 			response.setIdcreated(newEditorial.getEditorialid());
 		}
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 	
 	@RequestMapping(value="editorial/edit", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse EditEditorial(@RequestBody CampaignJSONRequest request) {
 		System.out.println("Edit Editorial");
 		StringBuffer error = new StringBuffer();
 		JsonResponse response = new JsonResponse();
 		
 		if(null == request.getId() || request.getId().isEmpty()) {
 			response.setError("editorial Id is empty");
 			return response;
 		} else if(null == request.getName() || request.getDesc().isEmpty()) {
 			response.setError("new name or description is empty");
 			return response;
 		}
 		
 		if(ResultStatus.SUCCESS == editorialService.editEditorial(request.getId(), 
 							request.getName(), request.getDesc(), error))   
 			response.setStatus(true);
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 	
 	@RequestMapping(value="editorial/setprofilepic", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse setProfilepic(@RequestBody JsonRequest profilepic) {
 		System.out.println("Set Profile pic for editorial:" + profilepic.getId());
 		StringBuffer error = new StringBuffer();
 		JsonResponse response = new JsonResponse();
 		if(profilepic.getId().isEmpty() || profilepic.getValue().isEmpty()) {
 			response.setError("editorialid or profile pic is empty");
 			return response;
 		}
 		if(ResultStatus.SUCCESS == editorialService.setProfilepic(profilepic.getId(), profilepic.getValue(), error)) 
 			response.setStatus(true);
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 	
 	@RequestMapping(value="editorial/delete", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse DeleteEditorial(@RequestBody String galleryid) {
 		System.out.println("Delete Print Campaign:"+ galleryid);
 		StringBuffer error = new StringBuffer();
 		JsonResponse response = new JsonResponse();
 		
 		if(null == galleryid || galleryid.isEmpty()) {
 			response.setError("Galleryid is empty");
 			return response;
 		}
 		if(ResultStatus.SUCCESS == editorialService.deleteEditorial(galleryid, error)) {  
 			response.setStatus(true);
 		}
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 	
 	@RequestMapping(value="editorial/addphotos", method=RequestMethod.POST)
 	public @ResponseBody UploadedFileResponse AddPhotostoGallery(Model model, 
 			@RequestParam("files[]") List<MultipartFile> fileuploads, @RequestParam("galleryid") String galleryId) {
 		System.out.println("Add photos: Gallery Id:" + galleryId + "No:of Photos:" + fileuploads.size());
 		UploadedFileResponse response = new UploadedFileResponse();
 		
 		if(galleryId.isEmpty()) {
 			response.setError("Gallery Id is empty");
 			return response;
 		}
 		org.netvogue.server.neo4japi.domain.Editorial editorial = editorialService.getEditorial(galleryId);
 		if(null == editorial) {
 			response.setError("No editorial is present with this id");
 			return response;
 		}
 		
 		List<PhotoWeb> JSONFileData= new ArrayList<PhotoWeb>();
 		
 		for ( MultipartFile fileupload : fileuploads ) {
 			System.out.println("Came here" + fileupload.getOriginalFilename());
 			Map<String, Object> uploadMap  = uploadManager.processUpload(fileupload, ImageType.EDITORIAL);
 			EditorialPhoto newPhoto = new EditorialPhoto((String)uploadMap.get(UploadManager.FILE_ID));
 			editorial.addPhotos(newPhoto);
 			
 			JSONFileData.add(conversionService.convert(newPhoto, PhotoWeb.class));
 		}
 		StringBuffer error = new StringBuffer();
 		if(ResultStatus.SUCCESS == editorialService.SaveEditorial(editorial, error)) {  
 			response.setStatus(true);
 			response.setFilesuploaded(JSONFileData);
 		}
 		else
 			response.setError(error.toString());
 		return response;
 	}
 	
 	@RequestMapping(value="editorial/editphotoinfo", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse EditPhotoInfo(@RequestBody PhotoInfoJsonRequest photoInfo) {
 		System.out.println("Edit Photo Info:" + photoInfo.toString());
 		StringBuffer error = new StringBuffer();
 		JsonResponse response = new JsonResponse();
 		String photoId = photoInfo.getPhotoid();
 		if(null == photoId || photoId.isEmpty())
 			response.setError("photoid is empty");
 		if(ResultStatus.SUCCESS == editorialService.editPhotoInfo(photoInfo.getPhotoid(), photoInfo.getPhotoname(), 
 													photoInfo.getSeasonname(), error))
 			response.setStatus(true);
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 	
 	@RequestMapping(value="editorial/editphotoname", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse EditPhotoName(@RequestBody JsonRequest request) {
 		System.out.println("Edit Photo Name");
 		StringBuffer error = new StringBuffer();
 		
 		JsonResponse response = new JsonResponse();
 		
 		if(ResultStatus.SUCCESS == editorialService.editPhotoName(request.getId(), request.getValue(), error))   
 			response.setStatus(true);
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 	
 	@RequestMapping(value="editorial/editphotoseasonname", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse EditPhotoSeasonName(@RequestParam("photoname") String photoname,
 														  @RequestParam("seasonname") String seasonname, 
 														  @RequestParam("photoid") String photoid) {
 		System.out.println("Edit Photo Name");
 		StringBuffer error = new StringBuffer();
 		
 		JsonResponse response = new JsonResponse();
 		
 		if(ResultStatus.SUCCESS == editorialService.editPhotoInfo(photoid, photoname, seasonname, error))   
 			response.setStatus(true);
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 
 	@RequestMapping(value="editorial/deletephoto", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse DeletePhoto(@RequestBody String photoid) {
 		System.out.println("Delete Photo:" + photoid);
 		StringBuffer error = new StringBuffer();
 		
 		JsonResponse response = new JsonResponse();
 		if(!photoid.isEmpty()) {
 			if(ResultStatus.SUCCESS == editorialService.deletePhoto(photoid, error)) {  
 				response.setStatus(true);
 			}
 			else
 				response.setError(error.toString());
 		} else {
 			response.setError("photoid is empty");
 		}
 		
 		return response;
 	}
 }
