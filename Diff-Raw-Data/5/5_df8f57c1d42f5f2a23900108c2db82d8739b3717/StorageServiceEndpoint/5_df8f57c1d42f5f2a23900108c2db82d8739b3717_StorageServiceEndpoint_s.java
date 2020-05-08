 package com.sound.service.impl;
 
 import java.net.URL;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.sound.model.OssAuth;
 import com.sound.model.enums.FileType;
 
 @Component
 @Path("/storage")
 public class StorageServiceEndpoint {
 
 	@Autowired
 	com.sound.service.storage.itf.RemoteStorageService remoteStorageService;
 
 	@GET
 	@Path("/ossauth")
 	public OssAuth getOSSAuth() {
 		OssAuth dto = loadOssAuthDto();
 		return dto;
 	}
 
 	@GET
	@Path("/downloadurl/{file}")
 	public Response getDownloadUrl(@PathParam("file") String file,
 			@PathParam("type") String type) {
 		if (StringUtils.isBlank(file) || StringUtils.isBlank(type)) {
 			return Response.status(Status.BAD_REQUEST).entity(null).build();
 		}
 		URL url = remoteStorageService.generateDownloadUrl(file,
 				FileType.getFileType(type));
 		return Response.status(Status.OK).entity(url.toString()).build();
 	}
 
 	@GET
	@Path("/uploadurl/{file}")
 	public Response getUploadUrl(@PathParam("file") String file,
 			@PathParam("type") String type) {
 		if (StringUtils.isBlank(file) || StringUtils.isBlank(type)) {
 			return Response.status(Status.BAD_REQUEST).entity(null).build();
 		}
 		URL url = remoteStorageService.generateUploadUrl(file,
 				FileType.getFileType(type));
 		return Response.status(Status.OK).entity(url.toString()).build();
 	}
 
 	private OssAuth loadOssAuthDto() {
 		OssAuth dto = new OssAuth();
 		PropertiesConfiguration config = remoteStorageService
 				.getRemoteStorageConfig();
 		dto.setAccessId(config.getString("ACCESS_ID"));
 		dto.setAccessPassword(config.getString("ACCESS_KEY"));
 		dto.setSoundBucket(config.getString("SoundBucket"));
 		dto.setImageBucket(config.getString("ImageBucket"));
 		return dto;
 	}
 
 }
