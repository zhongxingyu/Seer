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
 import org.netvogue.server.neo4japi.domain.Photo;
 import org.netvogue.server.neo4japi.domain.User;
 import org.netvogue.server.neo4japi.service.GalleryService;
 import org.netvogue.server.neo4japi.service.UserService;
 import org.netvogue.server.webmvc.domain.Galleries;
 import org.netvogue.server.webmvc.domain.Gallery;
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
 public class GalleryController {
 
 	@Autowired NetvogueUserDetailsService userDetailsService;
 	@Autowired UserService 			userService;
 	@Autowired GalleryService 		galleryService;
 	@Autowired ConversionService	conversionService;
 
 	@Autowired
 	private UploadManager uploadManager;
 		
	@RequestMapping(value={"getgalleries", "getgallaries/{profileid}"}, method=RequestMethod.GET)
 	public @ResponseBody Galleries GetGalleries(@ModelAttribute("profileid") String profileid, 
 												@RequestParam("galleryname") String galleryname) {
 		System.out.println("Get Galleries: " + galleryname);
 		Galleries galleries = new Galleries();
 		User user;
 		if(!profileid.isEmpty()) {
 			user = userService.getUserByUsername(profileid);
 			if(user == null) {
 				return galleries;
 			}
 		} else {
 			 user = userDetailsService.getUserFromSession();
 		}
 		
 		galleries.setName(user.getName());
 		galleries.setIsbrand(USER_TYPE.BRAND == user.getUserType()?true:false);
 		galleries.setProfilepic(conversionService.convert(user.getProfilePicLink(), ImageURLsResponse.class));
 		Set<Gallery> galleriesTemp = new LinkedHashSet<Gallery>();
 		Iterable<org.netvogue.server.neo4japi.domain.Gallery> dbGalleries;
 		if(galleryname.isEmpty()) {
 			dbGalleries = userService.GetGalleries(user);
 		} else {
 			dbGalleries = userService.searchGalleryByName(user, galleryname);
 		}
 		if(null == dbGalleries) {
 			return galleries;
 		}
 		Iterator<org.netvogue.server.neo4japi.domain.Gallery> first = dbGalleries.iterator();
 		while ( first.hasNext() ){
 			org.netvogue.server.neo4japi.domain.Gallery dbGallery = first.next() ;
 			System.out.println("Gallery name: " + dbGallery.getGalleryname());
 			galleriesTemp.add(conversionService.convert(dbGallery, Gallery.class));
 		}
 		galleries.setGalleries(galleriesTemp);
 		
 		
 		return galleries;
 	}
 	
	@RequestMapping(value={"gallery/getphotos", "gallery/getphotos/profileid"}, method=RequestMethod.GET)
 	public @ResponseBody Photos GetPhotos(@ModelAttribute("profileid") String profileid, 
 										  @RequestParam("galleryid") String galleryid,
 										  @RequestParam("photoname") String photoname
 											 ) {
 		System.out.println("Get Galleries: " + photoname);
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
 		org.netvogue.server.neo4japi.domain.Gallery gTemp = galleryService.GetGallery(galleryid);
 		if(null == gTemp) {
 			return photos;
 		}
 		photos.setGalleryname(gTemp.getGalleryname());
 		Set<PhotoWeb> photosTemp = new LinkedHashSet<PhotoWeb>();
 		Iterable<org.netvogue.server.neo4japi.domain.Photo> dbPhotos;
 		if(photoname.isEmpty()) {
 			dbPhotos = galleryService.GetPhotos(galleryid);
 		} else {
 			dbPhotos = galleryService.searchPhotoByName(galleryid, photoname);
 		}
 		if(null == dbPhotos) {
 			return photos;
 		}
 		Iterator<org.netvogue.server.neo4japi.domain.Photo> first = dbPhotos.iterator();
 		while ( first.hasNext() ){
 			org.netvogue.server.neo4japi.domain.Photo dbPhoto = first.next() ;
 			photosTemp.add(conversionService.convert(dbPhoto, PhotoWeb.class));
 		}
 		photos.setPhotos(photosTemp);
 			
 		return photos;
 	}
 	
 	@RequestMapping(value="gallery/create", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse CreateGallery(@RequestBody String galleryname) {
 		System.out.println("Create Gallery");
 		StringBuffer error = new StringBuffer();
 		User loggedinUser = userDetailsService.getUserFromSession();
 		org.netvogue.server.neo4japi.domain.Gallery newGallery = new org.netvogue.server.neo4japi.domain.Gallery(galleryname, loggedinUser);
 		
 		JsonResponse response = new JsonResponse();
 		
 		if(ResultStatus.SUCCESS == galleryService.SaveGallery(newGallery, error)) {  
 			response.setStatus(true);
 			response.setIdcreated(newGallery.getGalleryid());
 		}
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 	
 	@RequestMapping(value="gallery/edit", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse EditGallery(@RequestBody JsonRequest request) {
 		System.out.println("Edit Gallery Name");
 		StringBuffer error = new StringBuffer();
 		JsonResponse response = new JsonResponse();
 		
 		if(null == request.getId() || request.getId().isEmpty()) {
 			response.setError("galleryid is empty");
 		} else if(null == request.getValue() || request.getValue().isEmpty()) {
 			response.setError("newgalleryname is empty");
 		} else {
 			if(ResultStatus.SUCCESS == galleryService.editGalleryName(request.getId(), request.getValue(), error))   
 				response.setStatus(true);
 			else
 				response.setError(error.toString());
 		}
 		return response;
 	}
 	
 	@RequestMapping(value="gallery/setprofilepic", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse EditPhotoInfo(@RequestBody JsonRequest profilepic) {
 		System.out.println("Set Profile pic for gallery:" + profilepic.getId());
 		StringBuffer error = new StringBuffer();
 		JsonResponse response = new JsonResponse();
 		if(profilepic.getId().isEmpty() || profilepic.getValue().isEmpty()) {
 			response.setError("galleryid or profile pic is empty");
 			return response;
 		}
 		if(ResultStatus.SUCCESS == galleryService.setProfilepic(profilepic.getId(), profilepic.getValue(), error)) 
 			response.setStatus(true);
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 	
 	@RequestMapping(value="gallery/delete", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse DeleteGallery(@RequestBody String galleryid) {
 		System.out.println("Delete Gallery:"+ galleryid);
 		StringBuffer error = new StringBuffer();
 		JsonResponse response = new JsonResponse();
 		
 		if(null == galleryid || galleryid.isEmpty()) {
 			response.setError("Galleryid is empty");
 			return response;
 		}
 		if(ResultStatus.SUCCESS == galleryService.deleteGallery(galleryid, error)) {  
 			response.setStatus(true);
 		}
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 	
 	@RequestMapping(value="gallery/addphotos", method=RequestMethod.POST)
 	public @ResponseBody UploadedFileResponse AddPhotostoGallery(Model model, 
 			@RequestParam("files[]") List<MultipartFile> fileuploads, @RequestParam("galleryid") String galleryId) {
 		System.out.println("Add photos: Gallery Id:" + galleryId + "No:of Photos:" + fileuploads.size());
 		UploadedFileResponse response = new UploadedFileResponse();
 		
 		if(galleryId.isEmpty()) {
 			response.setError("Gallery Id is empty");
 			return response;
 		}
 		org.netvogue.server.neo4japi.domain.Gallery gallery = galleryService.GetGallery(galleryId);
 		if(null == gallery) {
 			response.setError("No Gallery is present with this id");
 			return response;
 		}
 		
 		List<PhotoWeb> JSONFileData= new ArrayList<PhotoWeb>();
 		
 		for ( MultipartFile fileupload : fileuploads ) {
 			System.out.println("Came here" + fileupload.getOriginalFilename());
 			Map<String, Object> uploadMap  = uploadManager.processUpload(fileupload, ImageType.GALLERY);
 			Photo newPhoto = new Photo((String)uploadMap.get(UploadManager.FILE_ID));
 			gallery.addPhotos(newPhoto);
 			JSONFileData.add(conversionService.convert(newPhoto, PhotoWeb.class));
 		}
 		StringBuffer error = new StringBuffer();
 		if(ResultStatus.SUCCESS == galleryService.SaveGallery(gallery, error)) {  
 			response.setStatus(true);
 			response.setFilesuploaded(JSONFileData);
 		}
 		else
 			response.setError(error.toString());
 		return response;
 	}
 	
 	@RequestMapping(value="gallery/editphotoinfo", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse setCoverPic(@RequestBody PhotoInfoJsonRequest photoInfo) {
 		System.out.println("Edit Photo Info:" + photoInfo.toString());
 		StringBuffer error = new StringBuffer();
 		JsonResponse response = new JsonResponse();
 		String photoId = photoInfo.getPhotoid();
 		if(null == photoId || photoId.isEmpty()) {
 			response.setError("photoid is empty");
 			return response;
 		}
 		if(ResultStatus.SUCCESS == galleryService.editPhotoInfo(photoInfo.getPhotoid(), photoInfo.getPhotoname(), 
 													photoInfo.getSeasonname(), error))
 			response.setStatus(true);
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 	
 	@RequestMapping(value="gallery/editphotoname", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse EditPhotoName(@RequestBody JsonRequest request) {
 		System.out.println("Edit Photo Name");
 		StringBuffer error = new StringBuffer();
 		
 		JsonResponse response = new JsonResponse();
 		
 		if(ResultStatus.SUCCESS == galleryService.editPhotoName(request.getId(), request.getValue(), error))   
 			response.setStatus(true);
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 	
 	@RequestMapping(value="gallery/editphotoseasonname", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse EditPhotoSeasonName(@RequestParam("photoname") String photoname,
 														  @RequestParam("seasonname") String seasonname, 
 														  @RequestParam("photoid") String photoid) {
 		System.out.println("Edit Photo Name");
 		StringBuffer error = new StringBuffer();
 		
 		JsonResponse response = new JsonResponse();
 		
 		if(ResultStatus.SUCCESS == galleryService.editPhotoInfo(photoid, photoname, seasonname, error))   
 			response.setStatus(true);
 		else
 			response.setError(error.toString());
 		
 		return response;
 	}
 
 	@RequestMapping(value="gallery/deletephoto", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse DeletePhoto(@RequestBody String photoid) {
 		System.out.println("Delete Photo:" + photoid);
 		StringBuffer error = new StringBuffer();
 		
 		JsonResponse response = new JsonResponse();
 		if(!photoid.isEmpty()) {
 			if(ResultStatus.SUCCESS == galleryService.deletePhoto(photoid, error)) {  
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
