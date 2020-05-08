 package controllers;
 
 import java.lang.reflect.Type;
 import java.util.List;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 import models.account.User;
 import models.tm.Defect;
 import models.tm.Project;
 import models.tm.ProjectCategory;
 import models.tm.ProjectRole;
 import models.tm.Requirement;
 import models.tm.TMUser;
 import models.tm.approach.Release;
 import models.tm.approach.TestCycle;
 import models.tm.test.Instance;
 import models.tm.test.InstanceParam;
 import models.tm.test.Run;
 import models.tm.test.RunParam;
 import models.tm.test.Script;
 import models.tm.test.ScriptStep;
 import models.tm.test.Tag;
 import play.mvc.Util;
 
 /**
  * Lookups for retrieving single entities. We use this class because it does security checks (we check if the retrieved entity belongs
  * to the account of the user), and also this is a way to centralize data
  * access to single entities, which may come in handy in the future.
  *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 public class Lookups extends TMController {
 
     @Util
     public static TMUser getUser(Long userId) {
         if (userId == null) {
             return null;
         }
         TMUser user = TMUser.<TMUser>findById(userId);
         if (user == null) {
             return null;
         }
         checkInAccount(user);
         return user;
     }
 
     @Util
     public static User getAccountUser(Long userId) {
         if (userId == null) {
             return null;
         }
        User user = User.<User>findById(userId);
         if (user == null) {
             return null;
         }
         checkInAccount(user);
         return user;
     }
 
 
     @Util
     public static Requirement getRequirement(Long requirementId) {
         if (requirementId == null) {
             return null;
         }
         Requirement requirement = Requirement.<Requirement>findById(requirementId);
         if (requirement == null) {
             return null;
         }
         checkInAccount(requirement);
         return requirement;
     }
 
     @Util
     public static Script getScript(Long scriptId) {
         if (scriptId == null) {
             return null;
         }
         Script script = Script.<Script>findById(scriptId);
         if (script == null) {
             return null;
         }
         checkInAccount(script);
         return script;
     }
 
     @Util
     public static ScriptStep getScriptStep(Long scriptStepId) {
         if (scriptStepId == null) {
             return null;
         }
         ScriptStep scriptStep = ScriptStep.<ScriptStep>findById(scriptStepId);
         if (scriptStep == null) {
             return null;
         }
         checkInAccount(scriptStep);
         return scriptStep;
     }
 
     @Util
     public static Instance getInstance(Long instanceId) {
         if (instanceId == null) {
             return null;
         }
         Instance instance = Instance.<Instance>findById(instanceId);
         if (instance == null) {
             return null;
         }
         checkInAccount(instance);
         return instance;
     }
 
     @Util
     public static InstanceParam getInstanceParam(Long instanceParamId) {
         if (instanceParamId == null) {
             return null;
         }
         InstanceParam instanceParam = InstanceParam.<InstanceParam>findById(instanceParamId);
         if (instanceParam == null) {
             return null;
         }
         checkInAccount(instanceParam);
         return instanceParam;
     }
 
 
     @Util
     public static Run getRun(Long runId) {
         if (runId == null) {
             return null;
         }
         Run run = Run.<Run>findById(runId);
         if (run == null) {
             return null;
         }
         checkInAccount(run);
         return run;
     }
 
     @Util
     public static Project getProject(Long projectId) {
         Project project = null;
         if (projectId != null) {
             project = Project.<Project>findById(projectId);
         }
         if (project == null) {
             return null;
         }
         checkInAccount(project);
         return project;
     }
 
     @Util
     public static ProjectCategory getProjectCategory(Long projectCategoryId) {
         ProjectCategory projectCategory = null;
         if (projectCategoryId != null) {
            projectCategory = ProjectCategory.<ProjectCategory>findById(projectCategoryId);
         }
         if (projectCategory == null) {
             return null;
         }
         checkInAccount(projectCategory);
         return projectCategory;
     }
 
 
     @Util
     public static RunParam getRunParam(Long runParamId) {
         RunParam runParam = null;
         if (runParamId != null) {
             runParam = RunParam.<RunParam>findById(runParamId);
         }
         if (runParam == null) {
             return null;
         }
         checkInAccount(runParam);
         return runParam;
     }
 
     @Util
     public static ProjectRole getRole(Long roleId) {
         ProjectRole role = null;
         if (roleId != null) {
             role = ProjectRole.<ProjectRole>findById(roleId);
         }
         if (role == null) {
             return null;
         }
         checkInAccount(role);
         return role;
     }
 
     @Util
     public static TestCycle getCycle(Long cycleId) {
         if (cycleId == null) {
             return null;
         }
         TestCycle cycle = TestCycle.<TestCycle>findById(cycleId);
         if (cycle == null) {
             return null;
         }
         checkInAccount(cycle);
         return cycle;
     }
 
     @Util
     public static Release getRelease(Long releaseId) {
         if (releaseId == null) {
             return null;
         }
         Release release = Release.<Release>findById(releaseId);
         if (release == null) {
             return null;
         }
         checkInAccount(release);
         return release;
     }
 
     @Util
     public static Defect getDefect(Long defectId) {
         if (defectId == null) {
             return null;
         }
         Defect defect = Defect.<Defect>findById(defectId);
         if (defect == null) {
             return null;
         }
         checkInAccount(defect);
         return defect;
     }
 
 
     @Util
     public static Tag getTag(Long tagId) {
         if (tagId == null) {
             return null;
         }
         Tag tag = Tag.findById(tagId);
         if (tag == null) {
             return null;
         }
         checkInAccount(tag);
         return tag;
     }
 
 
     @Util
     public static void allUsers() {
         List<TMUser> users = TMUser.listByActiveProject();
         renderJSON(users, userSerializer);
     }
 
     @Util
     public static void tags(List<Tag> tags) {
         renderJSON(tags, tagSerializer);
     }
 
     @Util
     public static void allTags(Long projectId, Tag.TagType type, String term) {
         List<Tag> tags = Tag.find("from Tag t where t.type = ? and t.project.id = ? and lower(t.name) like ?", type, projectId, term + "%").fetch();
         renderJSON(tags, tagSerializer);
     }
 
     // TODO generify these serializers
     private static final UserSerializer userSerializer = new UserSerializer();
 
     private static class UserSerializer implements JsonSerializer<TMUser> {
         public JsonElement serialize(TMUser user, Type type, JsonSerializationContext context) {
             JsonObject object = new JsonObject();
             object.addProperty("id", user.id);
             object.addProperty("label", user.getFullName());
             object.addProperty("value", user.id);
             return object;
         }
     }
 
     // TODO generify by making an interface for the autocompletable entities
     public static final TagSerializer tagSerializer = new TagSerializer();
 
     private static class TagSerializer implements JsonSerializer<Tag> {
         public JsonElement serialize(Tag tag, Type type, JsonSerializationContext context) {
             JsonObject object = new JsonObject();
             object.addProperty("id", tag.id);
             object.addProperty("label", tag.name);
             object.addProperty("value", tag.name);
             object.addProperty("name", tag.name);
             return object;
         }
     }
 
 }
