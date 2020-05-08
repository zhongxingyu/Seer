 package controllers;
 
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.Query;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import controllers.deadbolt.Restrict;
 import controllers.tabularasa.TableController;
 import models.account.Account;
 import models.account.User;
 import models.general.UnitRole;
 import models.tabularasa.TableModel;
 import models.tm.*;
 import models.tm.test.Instance;
 import models.tm.test.Run;
 import models.tm.test.RunStep;
 import models.tm.test.Tag;
 import org.apache.commons.lang.StringUtils;
 import play.mvc.Before;
 import util.FilterQuery;
 import util.Logger;
 
 
 /**
  * @author Nikola Milivojevic
  */
 public class Defects extends TMController {
 
     private static final String[] sortBy = {"id", "name", "tags", "assignedTo", "submittedBy", "status.name", "created"};
 
     @Restrict(UnitRole.DEFECTVIEW)
     public static void index() {
         List<TMUser> users = TMUser.listByProject(getActiveProjectId());
         render(users);
     }
 
     public static void create(Long runId) {
         boolean createDefect = true;
 
         List<RunStep> runSteps = RunStep.find("from RunStep rs where rs.run.id=? and rs.status=3", runId).fetch();
         Instance instance = Run.find("select r.instance from Run r where r.id=?", runId).first();
 
         String defectDescription = "Test instance ran: " + instance.name;
 
         for (RunStep runStep : runSteps) {
             defectDescription = defectDescription + "\\n\\nExpected result: "
                     + runStep.expectedResult + "\\nActual result: " + runStep.actualResult;
         }
 
         render("Defects/index.html", createDefect, runId, defectDescription);
     }
 
     @Restrict(UnitRole.DEFECTVIEW)
     public static void defects(String tableId,
                                Integer iDisplayStart,
                                Integer iDisplayLength,
                                String sColumns,
                                String sEcho,
                                String title,
                                String titleCase,
                                String tags,
                                String status,
                                Long assignedToId,
                                Long submittedById,
                                Date dateFrom,
                                Date dateTo,
                                Integer iSortCol_0,
                                String sSortDir_0) {
 
 
         FilterQuery fq = new FilterQuery(Defect.class);
 
         fq.addFilter("project", "=", getActiveProject());
 
         if (StringUtils.isNotEmpty(title)) {
             StringMatcherType type = null;
             if (titleCase == null) {
                 type = StringMatcherType.CONTAINS;
             } else {
                 type = StringMatcherType.valueOf(titleCase.toUpperCase());
             }
             switch (type) {
                 case EQUALS:
                     fq.addFilter("name", "=", title);
                     break;
                 case CONTAINS:
                     fq.addFilter("name", "like", '%' + title + '%');
                     break;
                 case STARTSWITH:
                     fq.addFilter("name", "like", title + '%');
                     break;
                 case ENDSWITH:
                     fq.addFilter("name", "not like", '%' + title + '%');
                     break;
             }
         }
         if (StringUtils.isNotEmpty(tags)) {
             fq.addJoin("tags", "o", "t");
             fq.setDistinct(true);
             fq.addWhere("t.name in (:tags)", "tags", Arrays.asList(tags.split(",")));
             fq.addAfterWhere("group by o.id having count(t.id) = " + tags.split(",").length);
         }
         if (StringUtils.isNotEmpty(status)) {
             fq.addFilter("status.name", "=", status);
         }
         if (assignedToId != null) {
             fq.addFilter("assignedTo.id", "=", assignedToId);
         }
         if (submittedById != null) {
             fq.addFilter("submittedBy.id", "=", submittedById);
         }
         if (dateFrom != null) {
             fq.addWhere("o.created >= :dateFrom", "dateFrom", dateFrom);
         }
         if (dateTo != null) {
             fq.addWhere("o.created <= :dateTo", "dateTo", dateTo);
         }
 
         if (iSortCol_0 != null && iSortCol_0 > -1)
             fq.addAfterWhere("order by " + sortBy[iSortCol_0] + " " + sSortDir_0);
 
 
         Query query = fq.build();
 
         if (iDisplayStart != null) {
             query.setFirstResult(iDisplayStart);
         }
         if (iDisplayLength != null) {
             query.setMaxResults(iDisplayLength);
         }
 
         try {
             List defects = query.getResultList();
             TableController.renderJSON(defects, Defect.class, Defect.count(), sColumns, sEcho);
         } catch (Throwable t) {
             t.printStackTrace();
             error();
         }
 
     }
 
     @Before
     public static void handleTags() {
         if (request.actionMethod.equals("createDefect") || request.actionMethod.equals("updateDefect")) {
             processTags("defect.tags", Tag.TagType.DEFECT);
         }
     }
 
     @Restrict(UnitRole.DEFECTCREATE)
     public static void createDefect(Defect defect) {
         defect.submittedBy = getConnectedUser();
         defect.account = getConnectedUserAccount();
         defect.project = getActiveProject();
         defect.status = DefectStatus.getDefaultDefectStatus();
        defect.tags = getTags(params.get("defect.tags"), Tag.TagType.DEFECT);
         boolean created = defect.create();
         if (!created) {
             Logger.error(Logger.LogType.DB, "Error while creating defect");
             error("Error saving Defect, please try again");
         }
 
         // linking to test instance
         String runIdParam = params.get("runId");
         if (StringUtils.isNotEmpty(runIdParam)) {
             try {
                 Long runId = Long.valueOf(runIdParam);
                 if (runId != null) {
                     Instance instance = Run.find("select r.instance from Run r where r.id = ?", runId).first();
                     instance.defects.add(defect);
                     try {
                         instance.save();
                     } catch (Throwable t) {
                         Logger.error(Logger.LogType.DB, "Error while updating defect instance during linking to defefct %s", defect.getId());
                         error(String.format("Error linking the newly created defect %s to test instance %s. Please try again.", defect.naturalId, instance.naturalId));
                     }
                 }
             } catch (NumberFormatException nfe) {
                 Logger.error(Logger.LogType.SECURITY, "Invalid value '%s' for runId parameter passed during defect creation", runIdParam);
                 error();
             }
         }
 
         ok();
     }
 
     @Restrict(UnitRole.DEFECTEDIT)
     public static void updateDefect(Defect defect) {
         Defect d = Lookups.getDefect(defect.getId());
         if (d == null) {
             Logger.error(Logger.LogType.TECHNICAL, "Could not find defect with ID %s", defect.getId());
             notFound("Could not find defect " + defect.getId());
         }
         d.name = defect.name;
         d.description = defect.description;
         d.assignedTo = defect.assignedTo;
         d.status = defect.status;
        defect.tags = getTags(params.get("defect.tags"), Tag.TagType.DEFECT);
         try {
             d.save();
         } catch (Throwable t) {
             Logger.error(Logger.LogType.DB, "Error updating defect");
             error("Error while saving defect, please try again.");
         }
         ok();
     }
 
     @Restrict(UnitRole.DEFECTVIEW)
     public static void defectDetails(Long baseObjectId, String[] fields) {
         Defect defect = Lookups.getDefect(baseObjectId);
         if (defect == null) {
             Logger.error(Logger.LogType.TECHNICAL, "Could not find defect with ID %s", defect.id);
             notFound("Could not find defect " + defect.id);
         }
 
         renderFields(defect, fields);
     }
 
     @Restrict(UnitRole.DEFECTDELETE)
     public static void deleteDefect(Long defectId) {
         Defect defect = Lookups.getDefect(defectId);
         if (defect == null) {
             Logger.error(Logger.LogType.TECHNICAL, "Could not find defect for deletion %s", defectId);
             notFound("Could not find defect with ID " + defectId);
         }
         try {
             defect.delete();
         } catch (Throwable t) {
             Logger.error(Logger.LogType.DB, "Error deleting defect %s", defectId);
             error(String.format("Error deleting defect %s, please try again", defect.naturalId));
         }
         ok();
     }
 
     @Restrict(UnitRole.DEFECTVIEW)
     public static void allTags(String q) {
         Lookups.allTags(getActiveProjectId(), Tag.TagType.DEFECT, q);
     }
 
     @Restrict(UnitRole.DEFECTVIEW)
     public static void defectDescription(Long defectId) {
         Defect defect = Lookups.getDefect(defectId);
         if (defect == null) {
             Logger.error(Logger.LogType.TECHNICAL, "Could not find defect with ID %s", defect.id);
             notFound("Could not find defect " + defect.id);
         }
         JsonObject jsonObject = new JsonObject();
         jsonObject.addProperty("defectTitle", defect.name);
         jsonObject.addProperty("defectAssignedTo", defect.assignedTo == null ? "" : defect.assignedTo.toString());
         jsonObject.addProperty("defectSubmittedBy", defect.submittedBy == null ? "" : defect.submittedBy.toString());
         jsonObject.addProperty("defectStatus", defect.status == null ? "" : defect.status.toString());
         jsonObject.addProperty("defectCreated", defect.created == null ? "" : defect.created.toString());
         jsonObject.addProperty("defectTags", defect.tags == null ? "" : defect.tags.toString());
         jsonObject.addProperty("defectDescription", defect.description == null ? "" : defect.description);
         renderJSON(jsonObject.toString());
     }
 
     @Restrict(UnitRole.DEFECTVIEW)
     public static void addComment(DefectComment comment, Long defectId){
         Defect defect = Defect.find("id=?", defectId).first();
         TMUser tmUser = TMUser.find("id=?", getConnectedUserId()).first();
         Project project = Project.find("id=?", getActiveProjectId()).first();
         comment.submittedBy = tmUser;
         comment.defect = defect;
         comment.project = project;
         comment.account = tmUser.account;
         comment.save();
         ok();
     }
 
     @Restrict(UnitRole.DEFECTVIEW)
     public static void getComments(Long defectId){
         List<DefectComment> defectComments = DefectComment.find("defect.id=?", defectId).fetch();
         TMUser tmUser = TMUser.find("id=?", getConnectedUserId()).first();
         JsonArray jsonArray = new JsonArray();
         JsonObject result = new JsonObject();
         for(DefectComment defectComment: defectComments){
             JsonObject comment = new JsonObject();
             comment.addProperty("id", defectComment.id);
             comment.addProperty("comment", defectComment.comment);
             comment.addProperty("submittedBy", defectComment.submittedBy.getFullName());
             SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
             comment.addProperty("submittedOn", sdf.format(defectComment.created));
 
             comment.addProperty("hiddenDivId", "hiddenDiv" + defectComment.id);
             comment.addProperty("visibleDivId", "visibleDiv" + defectComment.id);
             comment.addProperty("visibleActionsId", "visibleActions" + defectComment.id);
             comment.addProperty("hiddenActionsId", "hiddenActions" + defectComment.id);
             comment.addProperty("commentEditInputText", defectComment.comment);
             comment.addProperty("canEdit", defectComment.submittedBy.equals(tmUser));
             
             jsonArray.add(comment);
         }
         result.add("comments", jsonArray);
         renderJSON(result.toString());
     }
 
     @Restrict(UnitRole.DEFECTVIEW)
     public static void editComment(Long commentId, String comment){
         DefectComment defectComment = DefectComment.find("id=? and submittedBy.id=?", commentId, getConnectedUserId()).first();
         if(defectComment==null){
             error("You are not allowed to edit this comment, or comment doesn't exist!");
         }
         defectComment.comment=comment;
         defectComment.save();
         ok();
     }
 
     @Restrict(UnitRole.DEFECTVIEW)
     public static void deleteComment(Long commentId){
         DefectComment defectComment = DefectComment.find("id=? and submittedBy.id=?", commentId, getConnectedUserId()).first();
         if(defectComment==null){
             error("You are not allowed to delete this comment, or comment doesn't exist!");
         }
         defectComment.delete();
         ok();
     }
 }
