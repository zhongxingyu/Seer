 package controllers.admin;
 
 import controllers.CRUD;
 import controllers.Check;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import models.InputFile;
 import models.OutputFile;
 import models.Role;
 import models.Solution;
 import models.Task;
 import models.User;
 import play.db.jpa.JPA;
 import play.mvc.After;
 import play.mvc.Before;
 import play.mvc.Router;
 import play.mvc.With;
 import services.task.ContestantStatistics;
 import services.task.ContestantResult;
 import services.task.SystemReplyStatistics;
 import services.task.SystemReplyResult;
 
 @Check(Role.Check.ADMIN)
 @With({
 	controllers.Secure.class,
 	controllers.admin.with.CRUDSearch.class,
 	controllers.admin.with.Menu.class,
 	controllers.contestant.with.SubmitNotification.class
 })
 public class Tasks extends CRUD {
 
 	/**
 	 * Check for errors and create files before create
 	 * @param inputData
 	 * @param outputData
 	 * @throws IOException 
 	 */
 	@Before(only = "create")
 	public static void beforeCreate(java.io.File inputData, java.io.File outputData) throws IOException {
 		params.put("object.creator.id", User.getLoggedUser().getId().toString());
 		validation.required(inputData);
 
 		if (!validation.hasErrors()) {
 			InputFile inputFile = new InputFile(inputData);
 			inputFile.save();
 			params.put("object.inputData.id", inputFile.getId().toString());
 
 			if (outputData != null) {
 				OutputFile outputFile = new OutputFile(outputData);
 				outputFile.save();
 				params.put("object.outputData.id", outputFile.getId().toString());
 			}
 		}
 	}
 
 	/**
 	 * Update files before save
 	 * @param id
 	 * @param inputData
 	 * @param outputData
 	 * @param removeOutputData
 	 * @throws IOException 
 	 */
 	@Before(only = "save")
 	public static void beforeSave(Long id, java.io.File inputData, java.io.File outputData, boolean removeOutputData) throws IOException {
 		models.Task task = models.Task.findById(id);
 		notFoundIfNull(task);
 
 		if (removeOutputData) {
 			task.deleteOutputData();
 		}
 
 		if (inputData != null) {
 			InputFile inputFile = new InputFile(inputData);
 			inputFile.save();
 			params.put("object.inputData.id", inputFile.getId().toString());
 		}
 		if (outputData != null) {
 			OutputFile outputFile = new OutputFile(outputData);
 			outputFile.save();
 			params.put("object.outputData.id", outputFile.getId().toString());
 		}
 	}
 
 	/**
 	 * Update units
 	 */
 	@Before(only = {"create", "save"})
 	public static void fixUnits() {
 		String[] convert = new String[]{"object.sourceLimit", "object.memoryLimit", "object.outputLimit"};
 		for (String key : convert) {
			try {
				int parameter = Integer.parseInt(params.get(key)) * 1024;
				params.put(key, Integer.toString(parameter));
			} catch(NumberFormatException e) {
			}
 		}
 	}
 
 	/**
 	 * Check for delete constraints
 	 * @param id 
 	 */
 	@Before(only = {"delete"})
 	public static void beforeDelete(Long id) {
 		Task task = Task.findById(id);
 		notFoundIfNull(task);
 		if (!task.competitions.isEmpty()) {
 			flash.error("Can't delete task! Is used in " + task.competitions.size() + " competition" + (task.competitions.size() > 1 ? "s" : "") + ": " + task.competitions.get(0).name + (task.competitions.size() > 1 ? ", ..." : ""));
 			redirect(request.controller + ".show", id);
 		}
 	}
 
 	/**
 	 * Remove unused files
 	 */
 	@After(only = {"save", "delete"})
 	public static void afterSaveAndDelete() {
 		InputFile.deleteUnused();
 		OutputFile.deleteUnused();
 	}
 
 	/**
 	 * Don't save on errors
 	 */
 	@After
 	public static void rollbackOnValidationError() {
 		if (validation.hasErrors()) {
 			JPA.setRollbackOnly();
 		}
 	}
 
 	/**
 	 * Show statistics
 	 * @param id 
 	 */
 	public static void statistics(Long id) {
 		Task task = Task.findById(id);
 		notFoundIfNull(task);
 
 		Collection<SystemReplyResult> systemReplyStatistics = new SystemReplyStatistics().getStatistics(task);
 		Collection<ContestantResult> contestantStatistics = new ContestantStatistics().getStatistics(task, 10);
 
 		render(task, systemReplyStatistics, contestantStatistics);
 	}
 
 	/**
 	 * Redirect to task by given parameters
 	 * @param task
 	 * @param user
 	 * @param time 
 	 */
 	public static void gotoSolution(Long task, Long user, Integer time) {
 		Solution first = Solution.find("task.id = ? AND user.id=? AND timeLength=?", task, user, time).first();
 		notFoundIfNull(first);
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("id", first);
 		redirect(Router.getFullUrl("admin.Solutions.show", map));
 	}
 
 	/**
 	 * Remove all evaluations from single solutions of this task
 	 * @param id 
 	 */
 	public static void unevaluate(Long id) {
 
 		Task task = Task.findById(id);
 		notFoundIfNull(task);
 		List<models.Solution> solutions = models.Solution.find("task=?", task).fetch();
 		for (models.Solution solution : solutions) {
 			solution.unevaluate();
 			solution.save();
 		}
 
 		Map<String, Object> parameters = new HashMap<String, Object>();
 		parameters.put("search_task", task.id);
 		redirect(Router.getFullUrl("admin.Solutions.list", parameters));
 	}
 }
