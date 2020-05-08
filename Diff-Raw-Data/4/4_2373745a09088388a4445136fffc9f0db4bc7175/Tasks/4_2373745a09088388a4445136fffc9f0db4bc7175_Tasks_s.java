 package controllers;
 
 import java.lang.reflect.Modifier;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TreeMap;
 
 import models.Run;
 import models.State;
 import models.Task;
 import models.TaskOccurrence;
 import models.Value;
 import models.ValueOccurrence;
 import models.ValueOccurrence.OccurrenceType;
 import play.Logger;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import utils.PerfLogger;
 
 import com.avaje.ebean.Ebean;
 import com.avaje.ebean.Query;
 import com.avaje.ebean.RawSql;
 import com.avaje.ebean.RawSqlBuilder;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class Tasks extends Controller {
 
 	private static Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.STATIC).excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
 
 	/**
 	 * Get the gson data for the timeline
 	 * 
 	 * @return
 	 */
 	public static Result getTasks(String pEnvName, String pRunId, String pTaskId) {
 
 		PerfLogger.log("Timeline.getTasks", 1); 
 
 		Map<String, Object> gsonObject = new TreeMap<String, Object>();
 
 		// get the tasks (and the ParentId)
 		// -------------
 		boolean allTasks = false;
 		long parentTaskId = 0L;
 		Task parentTask = null;
 		List<Task> tasks = new ArrayList<Task>();
 		try {
 			if ((pEnvName != null) && (!pEnvName.trim().isEmpty())) {
 				// Get task by env
 				tasks = Task.find.fetch("parent").where().eq("environment", pEnvName).orderBy("path asc").findList();
 			} else if ((pTaskId != null) && (Long.parseLong(pTaskId) != 0)) {
 				// Get task by task
 				parentTask = Task.find.byId(Long.parseLong(pTaskId));
 				if (parentTask != null) {
 					if (parentTask.parent != null) {
 						parentTaskId = parentTask.parent.id;
 					}
					tasks = Task.find.fetch("parent").where().eq("environment", parentTask.environment).startsWith("path", parentTask.path).orderBy("path asc").findList();
 				}
 			}
 		} catch (NumberFormatException e) {
 		}
 		if (tasks.isEmpty()) {
 			tasks = Task.find.fetch("parent").orderBy("path asc").findList();
 			allTasks = true;
 		}
 
 		PerfLogger.log("Timeline.getTasks", 2);
 		// prepare tasks
 		Map<Long, List<Task>> mapParentTask = new HashMap<Long, List<Task>>();
 		for (Task task : tasks) {
 			long parentId = 0;
 			if (task.parent != null) {
 				parentId = task.parent.id;
 			}
 			if (mapParentTask.get(parentId) == null) {
 				mapParentTask.put(parentId, new ArrayList<Task>());
 			}
 			mapParentTask.get(parentId).add(task);
 		}
 
 		PerfLogger.log("Timeline.getTasks", 3);
 		// get the runs
 		// ------------
 		final int NB_MAX_RUN = 3;
 		long theRunId = 0;
 		Run theRun = null;
 		try {
 			theRunId = Long.parseLong(pRunId);
 			theRun = Run.find.byId(theRunId);
 		} catch (NumberFormatException e) {
 		}
 		List<Run> runs = new ArrayList<Run>();
 		if (allTasks) {
 			// if no limit on task
 			PerfLogger.log("Timeline.getTasks", 4);
 			
 			// if no limit on runs
 			if (theRun == null) {
 				runs = Run.find.setMaxRows(NB_MAX_RUN+2).orderBy("startDate desc").orderBy("id desc").findList();
 				Collections.reverse(runs);
 			} else {
 				runs = Run.find.setMaxRows(NB_MAX_RUN+2).where().le("startDate", theRun.startDate).orderBy("startDate desc").orderBy("id desc").findList();
 				Collections.reverse(runs);
 				List<Run> run2 = Run.find.setMaxRows(NB_MAX_RUN+2).where().ge("startDate", theRun.startDate).ne("id", theRun.getId()).orderBy("startDate asc").orderBy("id asc").findList();
 				
 				runs.addAll(run2);
 			}
 			
 			PerfLogger.log("Timeline.getTasks", 5);
 		} else {
 			// if it's limited on task
 			List<Long> tasksIds = new ArrayList<Long>();
 			for (Task task : tasks) {
 				tasksIds.add(task.getId());
 			}
 
 			PerfLogger.log("Timeline.getTasks", 6);
 			// if no limit on runs
 			if (theRun == null) {
 				String sql = " SELECT distinct(Run.id), Run.start_date, Run.end_date, Run.description, Run.state, Run.status FROM Run" + 
 										 " JOIN Task_occurrence ON Task_occurrence.run_id = Run.id " + 
 										 " order by Run.start_date desc, Run.id desc";
 				RawSql rawSql = RawSqlBuilder.parse(sql)
 						.columnMapping("distinct(Run.id)", "id")
 						.columnMapping("Run.start_date", "startDate")
 						.columnMapping("Run.end_date", "endDate")
 						.columnMapping("Run.description", "description")
 						.columnMapping("Run.state", "state")
 						.columnMapping("Run.status", "status")
 						.create();
 				Query<Run> query = Ebean.find(Run.class);
 				query.setRawSql(rawSql).setMaxRows(NB_MAX_RUN+2).where().in("Task_occurrence.Task_id", tasksIds);
 
 				runs = query.findList();
 				Collections.reverse(runs);
 			} else {
 				// just limit on runs
 				String sql = " SELECT distinct(Run.id), Run.start_date, Run.end_date, Run.description, Run.state, Run.status FROM Run" + 
 						 " JOIN Task_occurrence ON Task_occurrence.run_id = Run.id ";
 				RawSql rawSql = RawSqlBuilder.parse(sql)
 						.columnMapping("distinct(Run.id)", "id")
 						.columnMapping("Run.start_date", "startDate")
 						.columnMapping("Run.end_date", "endDate")
 						.columnMapping("Run.description", "description")
 						.columnMapping("Run.state", "state")
 						.columnMapping("Run.status", "status")
 						.create();
 				Query<Run> query = Ebean.find(Run.class);
 				query.setRawSql(rawSql)
 						 .setMaxRows(NB_MAX_RUN+2).where()
 						 .in("Task_occurrence.Task_id", tasksIds)
 						 .le("Run.start_date", theRun.startDate)
 						 .orderBy("Run.start_date desc, Run.id desc");
 
 				runs = query.findList();
 				Collections.reverse(runs);
 
 				query = Ebean.find(Run.class);
 				query.setRawSql(rawSql)
 						 .setMaxRows(NB_MAX_RUN+2).where()
 						 .in("Task_occurrence.Task_id", tasksIds)
 						 .gt("Run.start_date", theRun.startDate)
 						 .orderBy("Run.start_date asc, Run.id asc");
 
 				List<Run> runs1 = query.findList();
 				runs.addAll(runs1);
 
 				PerfLogger.log("Timeline.getTasks", 7);
 			}
 			PerfLogger.log("Timeline.getTasks", 8);
 		}
 		PerfLogger.log("Timeline.getTasks", 9);
 		// limit to the size and find the searched run
 		boolean hasPreviousRun = false;
 		boolean hasNextRun = false;
 		int from = 0;
 		int to = 0;
 		for (Run run : runs) {
 			to++;
 			if (to - from >= NB_MAX_RUN) {
 				from++;
 				hasPreviousRun = true;
 			}
 			if (run.id == theRunId) {
 				break;
 			}
 		}
 		if (to < runs.size()) {
 			to++;
 			if (to < runs.size()) {
 				hasNextRun = true;
 			}
 		} else if (from > 0) {
 			from--;
 			if (from == 0) {
 				hasPreviousRun = false;
 			}
 		}
 		runs = runs.subList(from, Math.min(to, runs.size()));
 		Collections.reverse(runs);
 
 		PerfLogger.log("Timeline.getTasks", 10);
 
 		// get the values for the tasks
 		// ------------------------------
 		List<Value> values = Value.find.where().in("task", tasks).findList();
 		Map<Long, List<Value>> mapParentValue = new HashMap<Long, List<Value>>();
 		for (Value value : values) {
 			long parentId = 0;
 			if (value.task != null) {
 				parentId = value.task.id;
 			}
 			if (mapParentValue.get(parentId) == null) {
 				mapParentValue.put(parentId, new ArrayList<Value>());
 			}
 			mapParentValue.get(parentId).add(value);
 		}
 
 		PerfLogger.log("Timeline.getTasks", 11);
 
 		// Get the occurence for these tasks and these runs
 		// ------------------------------------------------
 		List<TaskOccurrence> taskOccurrences = TaskOccurrence.find.where().in("task", tasks).in("run", runs).findList();
 		Map<Long, Map<Long, TaskOccurrence>> mapTaskRunOccurence = new HashMap<Long, Map<Long, TaskOccurrence>>();
 		for (TaskOccurrence taskOccurrence : taskOccurrences) {
 			long taskId = 0;
 			long runId = 0;
 			if (taskOccurrence.task != null) {
 				taskId = taskOccurrence.task.id;
 			}
 			if (taskOccurrence.run != null) {
 				runId = taskOccurrence.run.id;
 			}
 			if (mapTaskRunOccurence.get(taskId) == null) {
 				mapTaskRunOccurence.put(taskId, new HashMap<Long, TaskOccurrence>());
 			}
 			mapTaskRunOccurence.get(taskId).put(runId, taskOccurrence);
 		}
 
 		PerfLogger.log("Timeline.getTasks", 12);
 
 		// Get the occurence for these values and these runs
 		// ------------------------------------------------
 		List<ValueOccurrence> valueOccurrences = ValueOccurrence.find.where().in("value", values).in("run", runs).findList();
 		Map<Long, Map<Long, ValueOccurrence>> mapValueRunOccurence = new HashMap<Long, Map<Long, ValueOccurrence>>();
 		for (ValueOccurrence valueOccurrence : valueOccurrences) {
 			long valueId = 0;
 			long runId = 0;
 			if (valueOccurrence.value != null) {
 				valueId = valueOccurrence.value.id;
 			}
 			if (valueOccurrence.run != null) {
 				runId = valueOccurrence.run.id;
 			}
 			if (mapValueRunOccurence.get(valueId) == null) {
 				mapValueRunOccurence.put(valueId, new HashMap<Long, ValueOccurrence>());
 			}
 			mapValueRunOccurence.get(valueId).put(runId, valueOccurrence);
 		}
 
 		PerfLogger.log("Timeline.getTasks", 13);
 
 		// Add breadcrumb
 		List<Map<String, String>> breadcrumbLst = new ArrayList<Map<String, String>>();
 		if ((pEnvName != null) && (!pEnvName.trim().isEmpty())) {
 			// add env
 			addBreadCrumb(breadcrumbLst, pEnvName, pEnvName);
 		} else if (parentTask != null) {
 			// add env
 			addBreadCrumb(breadcrumbLst, parentTask.environment, parentTask.environment);
 			// add task parents
 			Query<Task> q = Task.find.where("environment = :eventEnvironment and path = substr(:eventPath, 1, length(path))").orderBy("path");
 			q.setParameter("eventPath", parentTask.path);
 			q.setParameter("eventEnvironment", parentTask.environment);
 			List<Task> parentTasks = q.findList();
 			for (Task t : parentTasks) {
 				addBreadCrumb(breadcrumbLst, t.name, t.getId().toString());
 			}
 
 		}
 		gsonObject.put("breadcrumb", breadcrumbLst);
 
 		// Add runs to gson
 		gsonObject.put("hasPreviousRun", hasPreviousRun);
 		gsonObject.put("hasNextRun", hasNextRun);
 		List<Object> gsonRuns = new ArrayList<Object>();
 		DateFormat df = new SimpleDateFormat("MMM dd - HH:mm:ss", Locale.US);
 		for (Run run : runs) {
 			Map<String, Object> gsonRun = new TreeMap<String, Object>();
 			gsonRun.put("id", run.getId());
 			gsonRun.put("start", df.format(run.startDate));
 			gsonRun.put("end", (run.endDate == null ? "" : df.format(run.endDate)));
 			gsonRun.put("duration", (run.endDate == null ? ValueOccurrence.format(System.currentTimeMillis()-run.startDate.getTime(), OccurrenceType.Duration) : ValueOccurrence.format(run.endDate.getTime()-run.startDate.getTime(), OccurrenceType.Duration)));
 			gsonRun.put("status", run.status);
 			gsonRun.put("state", run.state);
 			gsonRuns.add(gsonRun);
 		}
 		gsonObject.put("runs", gsonRuns);
 
 		PerfLogger.log("Timeline.getTasks", 14);
 
 		// Add tasks to gson
 		gsonObject.put("tasks", addTaskChildren(runs, mapParentTask, mapParentValue, mapTaskRunOccurence, mapValueRunOccurence, parentTaskId));
 
 		PerfLogger.log("Timeline.getTasks", 15);
 
 		return ok(gson.toJson(gsonObject));
 
 	}
 
 	/**
 	 * Add an entry to the breadcrumb
 	 * 
 	 * @param breadcrumbLst
 	 *          le lisdt to modify
 	 * @param label
 	 *          the label
 	 * @param id
 	 *          teh id (taskId or env. name)
 	 */
 
 	private static void addBreadCrumb(List<Map<String, String>> breadcrumbLst, String label, String id) {
 		Map<String, String> gsonEntry = new TreeMap<String, String>();
 		gsonEntry.put("label", label);
 		gsonEntry.put("id", id);
 		breadcrumbLst.add(gsonEntry);
 	}
 
 	/**
 	 * A children to a task
 	 * 
 	 * @param runs
 	 * @param parentTask
 	 * @param parentValue
 	 * @param taskRunOccurence
 	 * @param valueRunOccurence
 	 * @param parentId
 	 * @return
 	 */
 	private static List<Object> addTaskChildren(List<Run> runs, Map<Long, List<Task>> parentTask, Map<Long, List<Value>> parentValue, Map<Long, Map<Long, TaskOccurrence>> taskRunOccurence,
 			Map<Long, Map<Long, ValueOccurrence>> valueRunOccurence, Long parentId) {
 		List<Object> gsonTasks = new ArrayList<Object>();
 
 		// For each children
 		if (parentTask.get(parentId) != null) {
 			Collections.sort(parentTask.get(parentId));
 			for (Task task : parentTask.get(parentId)) {
 				// Add the task to the tree
 				Map<String, Object> gsonTask = new TreeMap<String, Object>();
 				gsonTask.put("id", task.getId());
 				gsonTask.put("name", task.name);
 				gsonTask.put("envname", task.environment);
 				gsonTask.put("order", task.orderT);
 				gsonTask.put("description", task.description);
 
 				// Add task occurrences
 				List<Object> gsonTaskRuns = new ArrayList<Object>();
 				for (Run run : runs) {
 					Map<String, Object> gsonTaskOccurence = new TreeMap<String, Object>();
 					if ((taskRunOccurence.get(task.getId()) == null) || (taskRunOccurence.get(task.getId()).get(run.getId()) == null)) {
 						
 						gsonTaskOccurence.put("status", models.Status.NOT_CONCERNED);
 						gsonTaskOccurence.put("state", State.FINISHED);
 					} else {
 						TaskOccurrence to = taskRunOccurence.get(task.getId()).get(run.getId());
 						gsonTaskOccurence.put("status", to.status);
 						gsonTaskOccurence.put("state", to.state);
 						gsonTaskOccurence.put("duration", (to.endDate == null ? ValueOccurrence.format(System.currentTimeMillis()-to.startDate.getTime(), OccurrenceType.Duration) : ValueOccurrence.format(to.endDate.getTime()-to.startDate.getTime(), OccurrenceType.Duration)));
 					}
 					gsonTaskRuns.add(gsonTaskOccurence);
 				}
 				gsonTask.put("runs", gsonTaskRuns);
 
 				// Add values
 				List<Object> gsonValues = new ArrayList<Object>();
 				if (parentValue.get(task.getId()) != null) {
 					for (Value value : parentValue.get(task.getId())) {
 						Map<String, Object> gsonValue = new TreeMap<String, Object>();
 						gsonValue.put("id", value.getId());
 						gsonValue.put("name", value.name);
 						gsonValue.put("value", value.averageValueStr());
 						gsonValue.put("description", value.description);
 
 						// Add value occurrences
 						List<Object> gsonValueRuns = new ArrayList<Object>();
 						for (Run run : runs) {
 							Map<String, Object> gsonValueOccurence = new TreeMap<String, Object>();
 							if ((valueRunOccurence.get(value.getId()) == null) || (valueRunOccurence.get(value.getId()).get(run.getId()) == null)) {
 								gsonValueOccurence.put("status", models.Status.NOT_CONCERNED);
 								gsonValueOccurence.put("state", State.FINISHED);
 								gsonValueOccurence.put("value", "");
 								gsonValueOccurence.put("id", -1);
 							} else {
 								gsonValueOccurence.put("status", valueRunOccurence.get(value.getId()).get(run.getId()).status);
 								gsonValueOccurence.put("state", valueRunOccurence.get(value.getId()).get(run.getId()).state);
 								gsonValueOccurence.put("id", valueRunOccurence.get(value.getId()).get(run.getId()).getId());
 								try {
 									gsonValueOccurence.put("value", valueRunOccurence.get(value.getId()).get(run.getId()).getResultStr());
 								} catch (ParseException e) {
 									Logger.error("Error : ", e);
 									gsonValueOccurence.put("value", "???");
 								}
 							}
 							gsonValueRuns.add(gsonValueOccurence);
 						}
 						gsonValue.put("runs", gsonValueRuns);
 
 						gsonValues.add(gsonValue);
 					}
 				}
 				gsonTask.put("values", gsonValues);
 
 				gsonTasks.add(gsonTask);
 				gsonTask.put("tasks", addTaskChildren(runs, parentTask, parentValue, taskRunOccurence, valueRunOccurence, task.getId()));
 			}
 		}
 
 		return gsonTasks;
 	}
 
 	public static Result getTask(Long taskId) {
 
 		Task task = Task.find.byId(taskId);
 		task.initBreadCrumb();
 
 		return ok(gson.toJson(task));
 	}
 
 	public static Result getValue(Long valueId) {
 		Value value = Value.find.byId(valueId);
 		value.initBreadCrumb();
 
 		return ok(gson.toJson(value));
 	}
 
 	static Form<Task> theTaskForm = form(Task.class);
 	static Form<Value> theValueForm = form(Value.class);
 
 	/**
 	 * Manage Task change
 	 * 
 	 * @return
 	 */
 	public static Result saveTask() {
 
 		Form<Task> filledForm = theTaskForm.bindFromRequest();
 
 		if (filledForm.hasErrors()) {
 			Logger.warn("" + filledForm.errors());
 			return badRequest(gson.toJson(filledForm.errors()));
 		} else {
 			// Find the Task
 			Task task = Task.find.byId(filledForm.get().id);
 			if (task != null) {
 				task.description = filledForm.get().description;
 				task.orderT = filledForm.get().orderT;
 				task.save();
 			}
 
 			flash("success", "Task saved");
 
 		}
 
 		return ok("");
 
 	}
 	/**
 	 * Manage value change
 	 * 
 	 * @return
 	 */
 	public static Result saveValue() {
 
 		Form<Value> filledForm = theValueForm.bindFromRequest();
 
 		if (filledForm.hasErrors()) {
 			Logger.warn("" + filledForm.errors());
 			return badRequest(gson.toJson(filledForm.errors()));
 		} else {
 			// Find the Value
 			Value value = Value.find.byId(filledForm.get().id);
 			if (value != null) {
 				// Get values
 				value.description = filledForm.get().description;
 				value.statusType = filledForm.get().statusType;
 				value.fixedTarget = filledForm.get().fixedTarget;
 				value.targetPercentWarning = filledForm.get().targetPercentWarning;
 				value.targetPercentError = filledForm.get().targetPercentError;
 				value.save();
 			}
 
 			flash("success", "Task saved");
 
 		}
 
 		return ok("");
 
 	}
 	/**
 	 * Manage value change
 	 * 
 	 * @return
 	 */
 	public static Result deleteRun(Long runId) {
 		// Find the Run
 		Run run = Run.find.byId(runId);
 		if (run != null) {
 			// delete the run itself
 			run.delete();
 		}
 
 		return ok("");
 
 	}
 	/**
 	 * Toggle the the flag "correct" or "rejected" of this value (for this run)
 	 * 
 	 * @param runId
 	 * @param valueId
 	 * @return
 	 */
 	public static Result toggleCorrect(Long voId) {
 
 		ValueOccurrence vo = ValueOccurrence.find.byId(voId);
 		if (vo != null) {
 			vo.isCorrect = !vo.isCorrect;
 			vo.save();
 		}
 
 		return ok("");
 
 	}
 	
 	public static Result mergeRuns(Long runIdSrc, Long runIdTrg) {
 		
 		Run runSrc = Run.find.byId(runIdSrc);
 		if (runSrc == null) {
 			return badRequest("Run not found "+runIdSrc);
 		}
 		Run runTrg = Run.find.byId(runIdTrg);
 		if (runTrg == null) {
 			return badRequest("Run not found "+runIdTrg);
 		}
 		
 		// Some checks
 		if (!runSrc.environment.equals(runTrg.environment)) {
 			return badRequest("Not on the same enviroment ("+runSrc.environment+" and "+runTrg.environment+")");
 		}
 		if (runSrc.description != null) {
 			return badRequest("Source is a real run (description not null)");
 		}
 		if (runTrg.description == null) {
 			return badRequest("Not real target run (description is null)");
 		}
 		
 		// prepare target
 		HashMap<Long, ValueOccurrence> trgValueOccurrence = new HashMap<Long, ValueOccurrence>();
 		for (ValueOccurrence vo : runTrg.valueOccurrences) {
 			trgValueOccurrence.put(vo.value.getId(), vo);
 		}
 		
 		// Merge valueOccurences
 		for (ValueOccurrence sourceVo : runSrc.valueOccurrences) {
 			ValueOccurrence targetVo = trgValueOccurrence.get(sourceVo.value.getId());
 			
 			if (targetVo == null) {
 				sourceVo.run = runTrg;
 				sourceVo.save();
 			} else {
 				if ((targetVo.creationDate == null) || ((sourceVo.creationDate != null) && (sourceVo.creationDate.before(targetVo.creationDate)))) {
 					targetVo.creationDate = sourceVo.creationDate;
 				}
 				if ((targetVo.endDate == null) || ((sourceVo.endDate != null) && (sourceVo.endDate.after(targetVo.endDate)))) {
 					targetVo.endDate = sourceVo.endDate;
 				}
 				if ((targetVo.result == null) || ((sourceVo.result != null) && (sourceVo.result > targetVo.result))) {
 					targetVo.result = sourceVo.result;
 				}
 				targetVo.status = models.Status.getWorst(targetVo.status, sourceVo.status);
 
 				// special for state
 				targetVo.state = sourceVo.state;
 
 				sourceVo.delete();
 				targetVo.save();
 			}
 		}
 
 		// Merge taskOccurences
 		for (TaskOccurrence sourceTo : runSrc.taskOccurences) {
 			TaskOccurrence targetTo = TaskOccurrence.find.where().eq("task", sourceTo.task).eq("run", runTrg).findUnique();
 			
 			if (targetTo == null) {
 				sourceTo.run = runTrg;
 				sourceTo.save();
 			} else {
 				if ((targetTo.startDate == null) || ((sourceTo.startDate != null) && (sourceTo.startDate.before(targetTo.startDate)))) {
 					targetTo.startDate = sourceTo.startDate;
 				}
 				if ((targetTo.endDate == null) || ((sourceTo.endDate != null) && (sourceTo.endDate.after(targetTo.endDate)))) {
 					targetTo.endDate = sourceTo.endDate;
 				}
 				targetTo.status = models.Status.getWorst(targetTo.status, sourceTo.status);
 
 				// special for state
 				targetTo.state = sourceTo.state;
 
 				sourceTo.delete();
 				targetTo.save();
 			}
 		}
 		
 		// merge run
 		if ((runTrg.startDate == null) || ((runSrc.startDate != null) && (runSrc.startDate.before(runTrg.startDate)))) {
 			runTrg.startDate = runSrc.startDate;
 		}
 		if ((runTrg.endDate == null) || ((runSrc.endDate != null) && (runSrc.endDate.after(runTrg.endDate)))) {
 			runTrg.endDate = runSrc.endDate;
 		} else if (runSrc.endDate == null) {
 			runTrg.endDate = null;
 		}
 		runTrg.status = models.Status.getWorst(runTrg.status, runSrc.status);
 
 		// special for state
 		runTrg.state = runSrc.state;
 
 		runSrc.delete();
 		runTrg.save();
 
 		
 		return ok("Merged done");
 	}
 
 }
