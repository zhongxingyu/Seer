 package controllers;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.persistence.Query;
 
 import controllers.deadbolt.Deadbolt;
 import controllers.tabularasa.TableController;
 import models.tm.User;
 import models.tm.approach.Release;
 import models.tm.test.ExecutionStatus;
 import models.tm.test.Instance;
 import models.tm.test.InstanceParam;
 import models.tm.test.Run;
 import models.tm.test.RunParam;
 import models.tm.test.RunStep;
 import models.tm.test.ScriptStep;
 import org.apache.commons.lang.StringUtils;
 import play.db.jpa.GenericModel;
 import play.mvc.With;
 import util.FilterQuery;
 
 /**
  * TODO security
  *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 @With(Deadbolt.class)
 public class Execution extends TMController {
 
     public static final String ACTUAL_RESULT = "actualResult_";
     public static final String STATUS = "status_";
     public static final String PARAM = "param_";
 
     public static void index() {
         List<Release> releases = Release.find("from Release r where r.project = ?", getActiveProject()).fetch();
         List<User> users = User.listByProject(getActiveProject().getId());
         render(releases, users);
     }
 
     public static void allUsers() {
         Lookups.allUsers();
     }
 
     public static void allTags(String q) {
         Lookups.allTags(getActiveProject(), q);
     }
 
 
     public static void instances(String tableId,
                                  Integer iDisplayStart,
                                  Integer iDisplayLength,
                                  String sColumns,
                                  String sEcho,
                                  Long cycle,
                                  Integer status,
                                  String tags,
                                  Long responsible,
                                  Date dateFrom,
                                  Date dateTo) {
 
 
         FilterQuery fq = new FilterQuery(Instance.class);
 
         Map<String, Object> filters = new HashMap<String, Object>();
 
         fq.addFilter("project", "=", getActiveProject());
 
         if (cycle != null) {
             fq.addFilter("testCycle.id", "=", cycle);
         }
         if (status != null) {
             fq.addFilter("status", "=", status);
         }
         if (tags != null && !StringUtils.isEmpty(tags)) {
             fq.addJoin("tags", "o", "t");
             fq.setDistinct(true);
             // Hibernate has a nasty bug that will yield in a ClassCastException when passing a String[], so we need to cast here
             fq.addWhere("t.name in (:tags)", "tags", Arrays.asList(tags.split(",")));
             fq.addAfterWhere("group by o.id having count(t.id) = " + tags.split(",").length);
         }
         if (responsible != null) {
             fq.addFilter("responsible.id", "=", responsible);
         }
         if (dateFrom != null) {
             fq.addWhere("o.plannedExecution >= :dateFrom", "dateFrom", dateFrom);
         }
         if (dateTo != null) {
             fq.addWhere("o.plannedExecution <= :dateTo", "dateTo", dateTo);
         }
 
         Query query = fq.build();
         if (iDisplayStart != null) {
             query.setFirstResult(iDisplayStart);
         }
         if (iDisplayLength != null) {
             query.setMaxResults(iDisplayLength);
         }
         List instances = query.getResultList();
         long totalRecords = instances.size();
 
         TableController.renderJSON(instances, Instance.class, totalRecords, sColumns, sEcho);
     }
 
     public static void runs(String tableId, Long instanceId,
                             Integer iDisplayStart,
                             Integer iDisplayLength,
                             String sColumns,
                             String sEcho,
                             String sSearch) {
 
         Instance instance = Lookups.getInstance(instanceId);
 
         GenericModel.JPAQuery query = null;
         String q = "from Run r where r.instance = ? and r.project = ? and r.temporary = false order by r.executionDate desc";
         if (sSearch != null && sSearch.length() > 0) {
             // TODO implement the search
             query = Run.find(q, instance, TMController.getActiveProject());
         } else {
             query = Run.find(q, instance, TMController.getActiveProject()).from(iDisplayStart == null ? 0 : iDisplayStart);
         }
         List<Run> runs = query.fetch(iDisplayLength == null ? 10 : iDisplayLength);
         long totalRecords = Run.count();
         TableController.renderJSON(runs, Run.class, totalRecords, sColumns, sEcho);
     }
 
     public static void runSteps(String tableId, Long runId,
                                 Integer iDisplayStart,
                                 Integer iDisplayLength,
                                 String sColumns,
                                 String sEcho,
                                 String sSearch) {
         Run run = Lookups.getRun(runId);
         GenericModel.JPAQuery query = null;
         // TODO implement the search
         if (sSearch != null && sSearch.length() > 0) {
             query = RunStep.find("from RunStep s where s.run = ? and s.project = ? order by s.position asc", run, TMController.getActiveProject());
         } else {
             query = RunStep.find("from RunStep s where s.run = ? and s.project = ? order by s.position asc", run, TMController.getActiveProject()).from(iDisplayStart == null ? 0 : iDisplayStart);
         }
         List<RunStep> runSteps = query.fetch(iDisplayLength == null ? 10 : iDisplayLength);
         long totalRecords = runSteps.size();
         TableController.renderJSON(runSteps, RunStep.class, totalRecords, sColumns, sEcho);
     }
 
     public static void createRunDialog(Long instanceId) {
         Instance instance = Lookups.getInstance(instanceId);
         if (instance == null) {
             notFound();
         }
 
         // create the run
         Run run = new Run();
         run.project = instance.project;
         run.instance = instance;
         run.tester = getConnectedUser();
         run.executionDate = new Date();
         run.executionStatus = ExecutionStatus.NOT_RUN;
         // for now this run is temporary, it will be a permanent one when the user actually saves the run.
         run.temporary = true;
         run.create();
 
         // copy the steps
         for (ScriptStep step : instance.script.getSteps()) {
             RunStep runStep = new RunStep();
             runStep.project = instance.project;
             runStep.run = run;
             runStep.executionStatus = ExecutionStatus.NOT_RUN;
 
             runStep.name = step.name;
             runStep.position = step.position;
             runStep.description = step.description;
             runStep.expectedResult = step.expectedResult;
 
             runStep.create();
         }
 
         // copy the parameters
         for (InstanceParam param : instance.getParams()) {
             RunParam runParam = new RunParam();
             runParam.project = instance.project;
             runParam.run = run;
 
             runParam.name = param.scriptParam.name;
             runParam.value = param.value;
 
             runParam.create();
         }
         render("Execution/runExecution.html", run);
     }
 
     public static void updateRunDialog(Long runId) {
         Run run = Lookups.getRun(runId);
         render("Execution/runExecution.html", run);
     }
 
     public static void updateRun(Long runId) {
         Run run = Lookups.getRun(runId);
         if (run == null) {
             notFound();
         }
 
         // Play can bind Lists of entities as well, using as form input name things like step[id].status and then in the action method List<RunStep> step (make sure it's the same name - no plural!)
         // that is, this automatic binding is sort of buggy: it generates a lot of null elements in the list and also does not pre-load the JPA entity, so this is sort of broken
 
         // TODO rewrite this once we have proper binding
         for (String param : params.all().keySet()) {
             String paramValue = params.all().get(param)[0];
             if (param.startsWith(ACTUAL_RESULT)) {
                 String id = param.substring(ACTUAL_RESULT.length());
                 RunStep step = getRunStep(id, run);
                if (paramValue != null && paramValue.length() > 0) {
                    step.actualResult = paramValue;
                    step.save();
                }
             } else if (param.startsWith(STATUS)) {
                 String id = param.substring(STATUS.length());
                 RunStep step = getRunStep(id, run);
                 if (paramValue != null && paramValue.length() > 0) {
                     try {
                         Integer status = Integer.parseInt(paramValue);
                         if (status != null) {
                             step.executionStatus = ExecutionStatus.fromPosition(status);
                             // $%&/()= Play/Hibernate bug!! we shouldn't have to invoke that bloody PreUpdate handler ourselves!
                             // TODO watch play.lighthouseapp.com/projects/57987-play-framework/tickets/731-jpa-preupdate-lifecycle-handler-does-not-work-in-play-controllers
                             step.doSave();
                             step.save();
                         }
                     } catch (NumberFormatException nfe) {
                         // TODO logging
                         nfe.printStackTrace();
                         error();
                     } catch (IllegalArgumentException iae) {
                         // TODO logging
                         iae.printStackTrace();
                         error();
                     }
                 } else {
                     // no status picked
                     step.executionStatus = ExecutionStatus.NOT_RUN;
                     // $%&/()= Play/Hibernate bug!! we shouldn't have to invoke that bloody PreUpdate handler ourselves!
                     // TODO watch play.lighthouseapp.com/projects/57987-play-framework/tickets/731-jpa-preupdate-lifecycle-handler-does-not-work-in-play-controllers
                     step.doSave();
                     step.save();
                 }
             }
         }
 
         // run is not temporary anymore
         run.temporary = false;
 
         // re-compute Run and Instance status
         run.updateStatus();
         run.instance.updateStatus();
 
         ok();
     }
 
     public static void deleteRun(Long runId) {
         Run run = Lookups.getRun(runId);
         if (run == null) {
             notFound();
         } else {
             try {
                 run.delete();
             } catch (Throwable t) {
                 // TODO logging
                 t.printStackTrace();
                 error();
             }
         }
         ok();
     }
 
     public static void updateParameter(Long runId, String id, String value) {
         Run run = Lookups.getRun(runId);
         if (run == null) {
             notFound("Could not find run " + runId);
         }
         if (id.startsWith(PARAM)) {
             try {
                 // param_id_UUID
                 String param = id.substring(PARAM.length());
                 param = param.substring(0, param.indexOf("_"));
                 Long pId = Long.parseLong(param);
                 RunParam runParam = Lookups.getRunParam(pId);
                 if (runParam == null) {
                     notFound("Could not find parameter " + pId);
                 }
                 runParam.value = value;
                 runParam.save();
                 renderText(value);
             } catch (NumberFormatException nfe) {
                 // TODO logging
                 nfe.printStackTrace();
                 error();
             }
         } else {
             error("Wrong parameter id");
         }
     }
 
     private static RunStep getRunStep(String id, Run run) {
         if (id != null && id.length() > 0) {
             RunStep step = null;
             try {
                 Long lid = Long.parseLong(id);
                 step = RunStep.find("from RunStep step where step.id = ? and step.project = ? and step.run = ?", lid, getActiveProject(), run).first();
                 if (step != null) {
                     checkInAccount(step);
                 }
 
 
             } catch (NumberFormatException nfe) {
                 // TODO log this and report (security)
                 nfe.printStackTrace();
             }
             return step;
         }
         return null;
     }
 
 
 }
