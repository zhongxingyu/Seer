 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.uhsarp.billrive.webservices.rest;
 
 import com.uhsarp.billrive.domain.Group;
 import com.uhsarp.billrive.services.GroupService;
 import static com.uhsarp.billrive.webservices.rest.GroupController.isEmpty;
 import java.util.List;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.context.request.WebRequest;
 
 /**
  *
  * @author uhsarp
  */
 @Controller
 public class GroupController extends GenericController {
     
     	@Autowired
 	private GroupService groupService;
 
 //	@Autowired
 //	private View jsonView_i;
         
         
         private static final String DATA_FIELD = "data";
 	private static final String ERROR_FIELD = "error";
         private static final Logger logger_c = Logger.getLogger(GroupController.class);
         
         
    	@RequestMapping(value = "/rest/{userId}/groups/", method = RequestMethod.GET)
 	public @ResponseBody List<Group> getGroups(@PathVariable("userId") int userId) {
 		List<Group> groups = null;
                 System.out.println("In Groups...Calling getGroups");
 		try {
 //			groups = groupService.getGroups(userId);
 		} catch (Exception e) {
 			String sMessage = "Error getting all groups. [%1$s]";
 			return groups;
 		}
 
 		logger_c.debug("Returing Groups: " + groups.toString());
 		return groups;
 	}
         
         
        	@RequestMapping(value = "/rest/{userId}/{userId}/groups/{groupId}", method = RequestMethod.GET)
 	public @ResponseBody Group getGroup(@PathVariable("groupId") String groupId_p,@PathVariable("userId") int userId) {
 		Group group = null;
 
 		
 		if (isEmpty(groupId_p) || groupId_p.length() < 5) {
 			String sMessage = "Error invoking getGroup - Invalid group Id parameter";
 			return group;
 		}
 
 		try {
 //			group = groupService.getGroupById(groupId_p);
 		} catch (Exception e) {
 			String sMessage = "Error invoking getGroup. [%1$s]";
 			return group;
 		}
 
 		logger_c.debug("Returing Group: " + group.toString());
 		return group;
 	}
                 
        @RequestMapping(value = { "/user/{userId}/group" }, method = { RequestMethod.POST })
 	public void addGroup(@RequestBody Group group_p,@PathVariable("userId") int userId,
 			HttpServletResponse httpResponse_p, WebRequest request_p) {
 
 		Group createdGroup=null;
 		logger_c.debug("Creating Group: " + group_p.toString());
 
 		try {
 			createdGroup = groupService.addGroup(group_p);
 		} catch (Exception e) {
 			String sMessage = "Error creating new group. [%1$s]";
 		}
 
 		/* set HTTP response code */
                 if(createdGroup!=null)
 		httpResponse_p.setStatus(HttpStatus.CREATED.value());
                 else
                     httpResponse_p.setStatus(HttpStatus.EXPECTATION_FAILED.value());
 
 		/* set location of created resource */
 		httpResponse_p.setHeader("Location", request_p.getContextPath() + "/user/{userId}/group/" + group_p.getId());
 
 	}
 
 	/**
 	 * Updates group with given group id.
 	 *
 	 * @param group_p
 	 *            the group_p
 	 * @return the model and view
 	 */
 	@RequestMapping(value = { "/user/{userId}/group/{groupId}" }, method = { RequestMethod.PUT })
 	public void editGroup(@RequestBody Group group_p, @PathVariable("groupId") String groupId_p,@PathVariable("userId") int userId,
 								   HttpServletResponse httpResponse_p) {
 
 		logger_c.debug("Updating Group: " + group_p.toString());
 
 		/* validate group Id parameter */
 		if (isEmpty(groupId_p) || groupId_p.length() < 5) {
 			String sMessage = "Error updating group - Invalid group Id parameter";
 		}
 
 		Group mergedGroup = null;
 
 		try {
 			mergedGroup = groupService.editGroup(group_p);
 		} catch (Exception e) {
 			String sMessage = "Error updating group. [%1$s]";
 		}
                 if(mergedGroup!=null)
 		httpResponse_p.setStatus(HttpStatus.OK.value());
                 else
                     httpResponse_p.setStatus(HttpStatus.EXPECTATION_FAILED.value());
 	}
 
 	/**
 	 * Deletes the group with the given group id.
 	 *
 	 * @param groupId_p
 	 *            the group id_p
 	 * @return the model and view
 	 */
 	@RequestMapping(value = "/user/{userId}/group/{groupId}", method = RequestMethod.DELETE)
 	public void deleteGroup(@PathVariable("groupId") String groupId_p,@PathVariable("userId") int userId,
 								   HttpServletResponse httpResponse_p) {
             Boolean deleted=false;
 
 		logger_c.debug("Deleting Group Id: " + groupId_p.toString());
 
 		/* validate group Id parameter */
 		if (isEmpty(groupId_p) || groupId_p.length() < 5) {
 			String sMessage = "Error deleting group - Invalid group Id parameter";
 //			return createErrorResponse(sMessage);
 		}
 
 		try {
 			 deleted = groupService.deleteGroup(Long.parseLong(groupId_p));
 		} catch (Exception e) {
 			String sMessage = "Error invoking getGroups. [%1$s]";
 //			return createErrorResponse(String.format(sMessage, e.toString()));
 		}
                 if(deleted)
 		httpResponse_p.setStatus(HttpStatus.OK.value());
                 else
                     httpResponse_p.setStatus(HttpStatus.EXPECTATION_FAILED.value());
 //		return new ModelAndView(jsonView_i, DATA_FIELD, null);
 	}
                 
                 
                 
         public static boolean isEmpty(String s_p) {
 		return (null == s_p) || s_p.trim().length() == 0;
 	}
         
         	private String createErrorResponse(String sMessage) {
 		return sMessage;
 	}
 }
