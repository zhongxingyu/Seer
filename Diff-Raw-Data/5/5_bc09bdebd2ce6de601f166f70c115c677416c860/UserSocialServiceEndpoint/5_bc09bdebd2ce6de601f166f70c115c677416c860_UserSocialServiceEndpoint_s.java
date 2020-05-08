 package com.sound.service.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.validation.constraints.NotNull;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import com.sound.exception.SoundException;
 import com.sound.exception.UserException;
 import com.sound.model.Group;
 import com.sound.model.User;
 import com.sound.service.user.itf.UserSocialService;
 
 @Component
 @Path("/userActivity")
 public class UserSocialServiceEndpoint {
 
 	Logger logger = Logger.getLogger(UserSocialServiceEndpoint.class);
 
 	@Autowired
 	UserSocialService userSocialService;
 
 	@PUT
 	@Path("/{fromUserAlias}/follow/{toUserAlias}")
 	public Response follow(
 			@NotNull @PathParam("fromUserAlias") String fromUserAlias,
 			@NotNull @PathParam("toUserAlias") String toUserAlias) {
 		try {
 			userSocialService.follow(fromUserAlias, toUserAlias);
 		} catch (UserException e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(e.getMessage()).build();
 		} catch (Exception e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(("Failed to follow user " + toUserAlias)).build();
 		}
 		return Response.status(Status.OK).entity("true").build();
 	}
 
 	@DELETE
 	@Path("/{fromUserAlias}/follow/{toUserAlias}")
 	public Response unfollow(
 			@NotNull @PathParam("fromUserAlias") String fromUserAlias,
 			@NotNull @PathParam("toUserAlias") String toUserAlias) {
 		try {
 			userSocialService.unfollow(fromUserAlias, toUserAlias);
 		} catch (UserException e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(e.getMessage()).build();
 		} catch (Exception e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(("Failed to follow user " + toUserAlias)).build();
 		}
 		return Response.status(Status.OK).entity("true").build();
 	}
 
 	@PUT
 	@Path("/{userAlias}/group/{groupName}")
 	public Response createGroup(
 			@NotNull @PathParam("userAlias") String userAlias,
 			@NotNull @PathParam("groupName") String groupName,
 			@NotNull @FormParam("description") String description) {
 		try {
 			userSocialService.createGroup(userAlias, groupName, description);
 		} catch (UserException e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(e.getMessage()).build();
 		} catch (Exception e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(("Failed to create group " + groupName)).build();
 		}
 		return Response.status(Status.OK).entity("true").build();
 	}
 
 	@DELETE
 	@Path("/{userAlias}/group/{groupName}")
 	public Response dismissGroup(
 			@NotNull @PathParam("userAlias") String userAlias,
 			@NotNull @PathParam("groupName") String groupName) {
 		try {
 			userSocialService.dismissGroup(userAlias, groupName);
 		} catch (UserException e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(e.getMessage()).build();
 		} catch (Exception e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(("Failed to delete group " + groupName)).build();
 		}
 		return Response.status(Status.OK).entity("true").build();
 	}
 
 	@PUT
 	@Path("/{userAlias}/joinGroup/{groupName}")
 	public Response joinGroup(
 			@NotNull @PathParam("userAlias") String userAlias,
 			@NotNull @PathParam("groupName") String groupName) {
 		try {
 			userSocialService.joinGroup(userAlias, groupName);
 		} catch (UserException e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(e.getMessage()).build();
 		} catch (Exception e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(("Failed to join group " + groupName)).build();
 		}
 		return Response.status(Status.OK).entity("true").build();
 	}
 
 	@DELETE
 	@Path("/{userAlias}/joinGroup/{groupName}")
 	public Response leaveGroup(
 			@NotNull @PathParam("userAlias") String userAlias,
 			@NotNull @PathParam("groupName") String groupName) {
 		try {
 			userSocialService.leaveGroup(userAlias, groupName);
 		} catch (UserException e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(e.getMessage()).build();
 		} catch (Exception e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(("Failed to leave group " + groupName)).build();
 		}
 		return Response.status(Status.OK).entity("true").build();
 	}
 
 	@PUT
 	@Path("/{ownerAlias}/{groupName}/promoteAdmin/{adminAlias}")
 	public Response promoteGroupAdmin(
 			@NotNull @PathParam("userAlias") String ownerAlias,
 			@NotNull @PathParam("userAlias") String adminAlias,
 			@NotNull @PathParam("groupName") String groupName) {
 		try {
 			userSocialService.promoteGroupAdmin(ownerAlias, adminAlias,
 					groupName);
 		} catch (UserException e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(e.getMessage()).build();
 		} catch (Exception e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(("Failed to promote group admin" + groupName))
 					.build();
 		}
 		return Response.status(Status.OK).entity("true").build();
 	}
 
 	@DELETE
 	@Path("/{ownerAlias}/{groupName}/promoteAdmin/{adminAlias}")
 	public Response demoteGroupAdmin(
 			@NotNull @PathParam("userAlias") String ownerAlias,
 			@NotNull @PathParam("userAlias") String adminAlias,
 			@NotNull @PathParam("groupName") String groupName) {
 		try {
 			userSocialService.promoteGroupAdmin(ownerAlias, adminAlias,
 					groupName);
 		} catch (UserException e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(e.getMessage()).build();
 		} catch (Exception e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(("Failed to demote group admin" + groupName))
 					.build();
 		}
 		return Response.status(Status.OK).entity("true").build();
 	}
 
 	@POST
 	@Path("/recommand/users")
 	public Response getRecommandedUsersByTags(
 			@NotNull @FormParam("tags") List<String> tags,
 			@NotNull @FormParam("pageNum") Integer pageNum,
 			@NotNull @FormParam("pageSize") Integer pageSize) {
 		List<User> users = new ArrayList<User>();
 		try {
 			users.addAll(userSocialService.recommandUsersByTags(tags, pageNum, pageSize));
 		} catch (UserException e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(e.getMessage()).build();
 		} catch (SoundException e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(e.getMessage()).build();
 		}
 		return Response.status(Status.OK).entity(users).build();
 	}
 
 	@POST
 	@Path("/recommand/groups")
 	public Response getRecommandedGroupsByTags(
 			@NotNull @FormParam("tags") List<String> tags,
 			@NotNull @FormParam("pageNum") Integer pageNum,
 			@NotNull @FormParam("pageSize") Integer pageSize) {
 		List<Group> groups = new ArrayList<Group>();
 		try {
 			groups.addAll(userSocialService.recommandGroupsByTags(tags, pageNum, pageSize));
 		} catch (UserException e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(e.getMessage()).build();
 		} catch (SoundException e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(e.getMessage()).build();
 		}
 		return Response.status(Status.OK).entity(groups).build();
 	}
 
 	@GET
	@Path("/followed/{userAlias}")
 	public Response getFollowedUsers(
 			@NotNull @PathParam("userAlias") String userAlias, 
 			@NotNull @FormParam("pageNum") Integer pageNum,
 			@NotNull @FormParam("pageSize") Integer pageSize) {
 		List<User> users = new ArrayList<User>();
 		try {
 			users.addAll(userSocialService.getFollowedUsers(userAlias, pageNum, pageSize));
 		} catch (UserException e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(e.getMessage()).build();
 		}
 		return Response.status(Status.OK).entity(users).build();
 	}
 
 	@GET
	@Path("/following/{userAlias}")
 	public Response getFollowingUsers(
 			@NotNull @PathParam("userAlias") String userAlias, 
 			@NotNull @FormParam("pageNum") Integer pageNum,
 			@NotNull @FormParam("pageSize") Integer pageSize) {
 		List<User> users = new ArrayList<User>();
 		try {
 			users.addAll(userSocialService.getFollowingUsers(userAlias, pageNum, pageSize));
 		} catch (UserException e) {
 			logger.error(e);
 			return Response.status(Status.INTERNAL_SERVER_ERROR)
 					.entity(e.getMessage()).build();
 		}
 		return Response.status(Status.OK).entity(users).build();
 	}
 }
