 /*
     basiclti - Building Block to provide support for Basic LTI
     Copyright (C) 2013  Stephen P Vickers
 
     This program is free software; you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation; either version 2 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along
     with this program; if not, write to the Free Software Foundation, Inc.,
     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 
     Contact: stephen@spvsoftwareproducts.com
 
     Version history:
       2.0.0 29-Jan-12
       2.0.1 20-May-12
       2.1.0 18-Jun-12  Updated to limit list when content item assigned to groups
       2.2.0  2-Sep-12  Added experimental option to send group membership details
       2.3.0  5-Nov-12  Added support to send group set details
       2.3.1 17-Dec-12
       2.3.2  3-Apr-13
 */
 package org.oscelot.blackboard.basiclti.extensions;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import blackboard.data.course.CourseMembership.Role;
 import blackboard.persist.Id;
 import blackboard.data.user.User;
 import blackboard.data.content.ContentStatus;
 import blackboard.data.course.CourseMembership;
 import blackboard.data.course.Group;
 import blackboard.data.course.GroupMembership;
 import blackboard.persist.content.ContentStatusDbLoader;
 import blackboard.persist.course.CourseMembershipDbLoader;
 import blackboard.persist.course.GroupDbLoader;
 import blackboard.persist.course.GroupMembershipDbLoader;
 import blackboard.persist.user.UserDbLoader;
 import blackboard.persist.PersistenceException;
 import blackboard.platform.user.MyPlacesUtil;
 
 import ca.ubc.ctlt.encryption.Encryption;
 
 import com.spvsoftwareproducts.blackboard.utils.B2Context;
 import org.oscelot.blackboard.basiclti.Tool;
 import org.oscelot.blackboard.basiclti.Utils;
 
 import org.oscelot.blackboard.basiclti.Constants;
 
 
 public class Memberships implements Action {
 
   public Memberships() {
   }
 
   public boolean execute(String actionName, B2Context b2Context, Tool tool, List<String> serviceData,
      Response response) {
 
     boolean ok = actionName.equals(Constants.EXT_MEMBERSHIPS_READ) ||
                  actionName.equals(Constants.EXT_MEMBERSHIP_GROUPS_READ);
 
     if (ok) {
       String codeMinor = null;
       if (!tool.getSendMembershipsService().equals(Constants.DATA_TRUE) ||
           !tool.getSendUserId().equals(Constants.DATA_MANDATORY)) {
         codeMinor = "ext.codeminor.notavailable";
       } else {
         codeMinor = "ext.codeminor.success";
         serviceData.remove(serviceData.size() - 1);  // remove time
         StringBuilder xml = new StringBuilder();
 
         Id contentId = null;
         if (b2Context.getContext().hasContentContext()) {
           contentId = b2Context.getContext().getContent().getId();
         }
         boolean systemRolesOnly = !b2Context.getSetting(Constants.TOOL_PARAMETER_PREFIX + "." + Constants.TOOL_COURSE_ROLES, Constants.DATA_FALSE).equals(Constants.DATA_TRUE);
         boolean includeAll = !tool.getLimitMemberships().equals(Constants.DATA_TRUE);
         boolean includeGroups = actionName.equals(Constants.EXT_MEMBERSHIP_GROUPS_READ) &&
            tool.getGroupMemberships().equals(Constants.DATA_TRUE);
         String groupPrefix = tool.getMembershipsGroupNames();
         if ((groupPrefix.length() > 0) && (groupPrefix.indexOf("*") < 0)) {
           groupPrefix += "*";
         }
         try {
           CourseMembershipDbLoader courseMembershipLoader = CourseMembershipDbLoader.Default.getInstance();
           Map<Id,Group> groups = null;
           Map<Id,List<Id>> groupMembers = null;
           if (includeGroups) {
             groups = new HashMap<Id,Group>();
             GroupDbLoader groupLoader = GroupDbLoader.Default.getInstance();
             List<Group> loadGroups = groupLoader.loadGroupsAndSetsByCourseId(b2Context.getContext().getCourseId());
             for (Group group : loadGroups) {
               String title = group.getTitle();
               if (group.isGroupSet() || (group.getIsAvailable() && ((groupPrefix.length() <= 0) || title.matches(groupPrefix)))) {
                 groups.put(group.getId(), group);
               }
             }
             GroupMembershipDbLoader groupMembershipLoader = GroupMembershipDbLoader.Default.getInstance();
             List<GroupMembership> groupMemberships = groupMembershipLoader.loadByCourseId(b2Context.getContext().getCourseId());
             groupMembers = new HashMap<Id,List<Id>>();
             for (GroupMembership groupMembership : groupMemberships) {
               List<Id> groupIds = null;
               if (!groupMembers.containsKey(groupMembership.getCourseMembershipId())) {
                 groupIds = new ArrayList<Id>();
               } else {
                 groupIds = groupMembers.get(groupMembership.getCourseMembershipId());
               }
               groupIds.add(groupMembership.getGroupId());
               groupMembers.put(groupMembership.getCourseMembershipId(), groupIds);
             }
           }
           List<User> users = UserDbLoader.Default.getInstance().loadByCourseId(b2Context.getContext().getCourseId());
           Map<Id,Boolean> contentStatusMap = null;
           if (contentId != null) {
             contentStatusMap = getContentStatusList(contentId, includeAll);
           }
           xml.append("  <memberships>\n");
           for (User user : users) {
             CourseMembership courseMembership = null;
             Role role = null;
             String roles = "";
             boolean isAvailable = user.getIsAvailable();
             if (isAvailable) {
               courseMembership = courseMembershipLoader.loadByCourseAndUserId(
                  b2Context.getContext().getCourseId(), user.getId());
               isAvailable = courseMembership.getIsAvailable();
               boolean isVisible = true;
               if (isAvailable && (contentStatusMap != null) && contentStatusMap.containsKey(courseMembership.getId())) {
                 isVisible = contentStatusMap.get(courseMembership.getId());
               }
               if (isAvailable) {
                 role = Utils.getRole(courseMembership.getRole(), systemRolesOnly);
                 roles = Utils.getRoles(tool.getRole(role.getIdentifier()),
                    tool.getSendAdministrator().equals(Constants.DATA_TRUE) && user.getSystemRole().equals(User.SystemRole.SYSTEM_ADMIN));
                 isAvailable = ((roles.indexOf(Constants.ROLE_INSTRUCTOR) >= 0) || isVisible);
               }
             }
             if (isAvailable) {
               StringBuilder member = new StringBuilder();
               String userIdType = tool.getUserIdType();
               String userId = null;
               if (userIdType.equals(Constants.DATA_USERNAME)) {
                 userId = user.getUserName();
               } else if (userIdType.equals(Constants.DATA_PRIMARYKEY)) {
                 userId = user.getId().toExternalString();
               } else if (userIdType.equals(Constants.DATA_STUDENTID)) {
                 userId = user.getStudentId();
               } else {
                 userId = user.getBatchUid();
               }
               // encrypt data is option is selected
               String encUserId = userId;
               String encBatchUid = user.getBatchUid();
               String encEmail = user.getEmailAddress();
               String encUserName = user.getGivenName();
               String encFamilyName = user.getFamilyName();
               String encFullname = "";
               if (tool.getSendUsername().equals(Constants.DATA_MANDATORY)) {
                   encFullname = user.getGivenName();
                   if ((user.getMiddleName() != null) && (user.getMiddleName().length() > 0)) {
                 	  encFullname += " " + user.getMiddleName();
                   }
                   encFullname += " " + user.getFamilyName();
               }
               
               if (tool.isEncryptData()) {
             	  Encryption encryptInstance = new Encryption();
             	  encUserId = encryptInstance.encrypt(encUserId);
             	  encBatchUid = encryptInstance.encrypt(encBatchUid);
             	  encUserName = encryptInstance.encrypt(encUserName);
             	  encFamilyName = encryptInstance.encrypt(encFamilyName);
             	  encFullname = encryptInstance.encrypt(encFullname);
             	  String[] encEmailArr = encEmail.split("(?=@)");
            	  encEmail = encryptInstance.encrypt(encEmailArr[0]) + (encEmailArr.length > 1 ? encEmailArr[1] : "");
               }
               member = member.append("      <user_id>").append(encUserId).append("</user_id>\n");
               try {
                 if (MyPlacesUtil.avatarsEnabled() && tool.getDoSendAvatar()) {
                   String image = null;
                   /*if (MyPlacesUtil.displayAvatar(user.getId())) {
                     image = MyPlacesUtil.getAvatarImage(user.getId());
                   }*/
                   if (image != null) {
                     member = member.append("      <user_image>").append(b2Context.getServerUrl()).append(image).append("</user_image>\n");
                   } else {
                     member = member.append("      <user_image></user_image>\n");
                   }
                 }
               } catch (Exception e) {
               }
               if (tool.getDoSendRoles()) {
                 member = member.append("      <roles>").append(roles).append("</roles>\n");
               }
               if (tool.getDoSendUserSourcedid()) {
                 member = member.append("      <person_sourcedid>").append(encBatchUid).append("</person_sourcedid>\n");
               }
               if (tool.getSendEmail().equals(Constants.DATA_MANDATORY)) {
                 member = member.append("      <person_contact_email_primary>").append(encEmail).append("</person_contact_email_primary>\n");
               }
               if (tool.getSendUsername().equals(Constants.DATA_MANDATORY)) {
                 member = member.append("      <person_name_given>").append(encUserName).append("</person_name_given>\n");
                 member = member.append("      <person_name_family>").append(encFamilyName).append("</person_name_family>\n");
                 member = member.append("      <person_name_full>").append(encFullname).append("</person_name_full>\n");
               }
               if (role.equals(Role.STUDENT) && tool.getSendUserId().equals(Constants.DATA_MANDATORY)) {
                 String resultSourcedid = Utils.getServiceId(serviceData, userId, tool.getSendUUID());
                 member = member.append("      <lis_result_sourcedid>").append(resultSourcedid).append("</lis_result_sourcedid>\n");
               }
               if (includeGroups && (groupMembers != null)) {
                 List<Id> groupIds = groupMembers.get(courseMembership.getId());
                 Group group;
                 if (groupIds != null) {
                   member.append("      <groups>\n");
                   for (Id groupId : groupIds) {
                     group = groups.get(groupId);
                     if (group != null) {
                       member.append("        <group>\n");
                       member.append("          <id>").append(groupId.toExternalString()).append("</id>\n");
                       member.append("          <title>").append(group.getTitle()).append("</title>\n");
                       if (group.isInGroupSet()) {
                         group = groups.get(group.getSetId());
                         member.append("          <set>\n");
                         member.append("            <id>").append(group.getId().toExternalString()).append("</id>\n");
                         member.append("            <title>").append(group.getTitle()).append("</title>\n");
                         member.append("          </set>\n");
                       }
                       member.append("        </group>\n");
                     }
                   }
                   member.append("      </groups>\n");
                 }
               }
               if (member.length() > 0) {
                 xml.append("    <member>\n");
                 xml.append(member.toString());
                 xml.append("    </member>\n");
               }
             }
           }
         } catch (PersistenceException e) {
         } catch (Exception e) {
 		}
         xml.append("  </memberships>\n");
 
         response.setData(xml.toString());
         response.setDescription(b2Context.getResourceString("ext.description.memberships"));
 
       }
 
       response.setCodeMinor(b2Context.getResourceString(codeMinor));
 
     }
 
     return ok;
 
   }
 
   private Map<Id,Boolean> getContentStatusList(Id contentId, boolean includeAll) throws PersistenceException {
 
     List<ContentStatus> contentStatusList = ContentStatusDbLoader.Default.getInstance().loadByContentId(contentId);
     Map<Id,Boolean> contentStatusMap = new HashMap<Id,Boolean>();
     for (Iterator<ContentStatus> iter = contentStatusList.iterator(); iter.hasNext();) {
       ContentStatus contentStatus = iter.next();
       contentStatusMap.put(contentStatus.getMembership().getId(),
          contentStatus.getAvailGroup() && (includeAll || contentStatus.getIsVisible()));
     }
 
     return contentStatusMap;
 
   }
 
 }
