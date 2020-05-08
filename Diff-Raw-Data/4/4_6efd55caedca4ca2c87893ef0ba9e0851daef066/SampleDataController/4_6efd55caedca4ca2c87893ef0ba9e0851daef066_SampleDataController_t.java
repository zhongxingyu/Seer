 package com.crowdplatform.controller;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.crowdplatform.model.Batch;
 import com.crowdplatform.model.Execution;
 import com.crowdplatform.model.Field;
 import com.crowdplatform.model.Project;
 import com.crowdplatform.model.Task;
 import com.crowdplatform.service.BatchService;
 import com.crowdplatform.service.ExecutionService;
 import com.crowdplatform.service.FieldService;
 import com.crowdplatform.service.ProjectService;
 import com.crowdplatform.service.TaskService;
 import com.google.common.collect.Sets;
 
 @Controller
 @RequestMapping("/sample")
 public class SampleDataController {
 
 	@Autowired
 	private ProjectService projectService;
 	
 	@Autowired
     private BatchService batchService;
 	
 	@Autowired
 	private FieldService fieldService;
 	
 	@Autowired
 	private TaskService taskService;
 	
 	@Autowired
 	private ExecutionService executionService;
 	
 	private Random random = new Random();
 	
 	@RequestMapping("/")
 	public String createSampleData(Map<String, Object> map) {
 		createSampleProject();
 		
 		return "redirect:/projects";
 	}
 	
 	@RequestMapping("/clean")
 	public String cleanSampleData() {
 		List<Project> projects = projectService.listProjects();
 		for (Project project : projects) {
 			project.setBatches(null);
 			project.setInputFields(null);
 			projectService.saveProject(project);
 			
 		}
 		List<Field> fields = fieldService.listFields();
 		for (Field field : fields) {
 			fieldService.removeField(field.getId());
 		}
 		List<Batch> batches = batchService.listBatches();
 		for (Batch batch : batches) {
 			batch.setTasks(null);
 			batchService.saveBatch(batch);
 		}
 		List<Task> tasks = taskService.listTasks();
 		for (Task task : tasks) {
 			task.setExecutions(null);
 			taskService.saveTask(task);
 		}
 		List<Execution> executions = executionService.listExecutions();
 		for (Execution execution : executions) {
 			executionService.removeExecution(execution.getId());
 		}
 		for (Task task : tasks) {
 			taskService.removeTask(task.getId());
 		}
 		for (Batch batch : batches) {
 			batchService.removeBatch(batch.getId());
 		}
 		for (Project project : projects) {
 			projectService.removeProject(project.getId());
 		}
 		return "redirect:/projects";
 	}
 	
 	private static final String[] definitions = {"{\"answers\":[],\"id\":1,\"word\":\"a lot\",\"level\":1,\"type\":\"separation\",\"language\":\"EN\",\"display\":\"alot\"}",
 		"{\"answers\":[\"a\",\"s\",\"i\",\"u\"],\"id\":3,\"word\":\"boasted\",\"level\":1,\"type\":\"substitution\",\"language\":\"EN\",\"display\":\"boested\"}",
 		"{\"answers\":[\"ua\",\"ie\",\"i\",\"ei\"],\"id\":4,\"word\":\"actually\",\"level\":1,\"type\":\"substitution\",\"language\":\"EN\",\"display\":\"act|ai|lly\"}",
 		"{\"answers\":[],\"id\":5,\"word\":\"bacon\",\"level\":1,\"type\":\"omission\",\"language\":\"EN\",\"display\":\"baecon\"}",
 		"{\"answers\":[\"t\",\"e\",\"y\",\"h\"],\"id\":7,\"word\":\"trust\",\"level\":1,\"type\":\"insertion1\",\"language\":\"EN\",\"display\":\"trus_\"}",
 		"{\"answers\":[\"t\"],\"id\":8,\"word\":\"trust\",\"level\":1,\"type\":\"insertion\",\"language\":\"EN\",\"display\":\"trus\"}",
		"{\"answers\":[\"iness\",\"izer\",\"ition\",\"less\"],\"id\":10,\"word\":\"scariness\",\"level\":1,\"type\":\"derivation\",\"language\":\"EN\",\"display\":\"scar\"}",
		"{\"answers\":[],\"id\":16,\"word\":\"jueves\",\"level\":1,\"type\":\"omission\",\"language\":\"ES\",\"display\":\"jruerves\"}"};
 	private static final Batch.State[] states = {Batch.State.RUNNING, Batch.State.PAUSED};
 	
 	private void createSampleProject() {
 		Project project = new Project();
 		project.setName("Awesome project");
 		projectService.addProject(project);
 		
 		Field field0 = new Field();
 		field0.setName("id");
 		field0.setType(Field.Type.INTEGER);
 		fieldService.addField(field0);
 		Field field1 = new Field();
 		field1.setName("type");
 		field1.setType(Field.Type.STRING);
 		fieldService.addField(field1);
 		Field field2 = new Field();
 		field2.setName("level");
 		field2.setType(Field.Type.INTEGER);
 		fieldService.addField(field2);
 		Field field3 = new Field();
 		field3.setName("language");
 		field3.setType(Field.Type.STRING);
 		fieldService.addField(field3);
 		Field field4 = new Field();
 		field4.setName("word");
 		field4.setType(Field.Type.STRING);
 		fieldService.addField(field4);
 		Field field5 = new Field();
 		field5.setName("display");
 		field5.setType(Field.Type.STRING);
 		fieldService.addField(field5);
 		Field field6 = new Field();
 		field6.setName("answers");
 		field6.setType(Field.Type.MULTIVALUATE_STRING);
 		field6.setColumnNames(Sets.newHashSet("correct", "dis_1", "dis_2", "dis_3", "dis_4", "dis_5", "dis_6"));
 		fieldService.addField(field6);
 		project.setInputFields(Sets.newHashSet(field0, field1, field2, field3, field4, field5, field6));
 		
 		Set<Batch> batches = Sets.newHashSet();
 		for (int i = 0; i < 5; ++i)
 			batches.add(createSampleBatch(project));
 		project.setBatches(batches);
 		projectService.saveProject(project);
 	}
 	
 	private Batch createSampleBatch(Project project) {
 		Batch batch = new Batch();
 		
 		batch.setName("Wonderful" + random.nextInt(99));
 		int exPerTask = random.nextInt(10) + 1;
 		batch.setExecutionsPerTask(exPerTask);
 		int stateIndex = random.nextInt(states.length);
 		batch.setState(states[stateIndex]);
 		batch.setProject(project);
 		
 		batchService.addBatch(batch);
 		
 		Random random = new Random();
 		
 		Set<Task> tasks = Sets.newHashSet();
 		int numTasks = random.nextInt(15) + 1;
 		for (int i = 0; i < numTasks; ++i) {
 			Task task = new Task();
 			int numExecutions = random.nextInt(exPerTask);
 			task.setNumExecutions(numExecutions);
 			task.setBatch(batch);
 			int index = random.nextInt(definitions.length);
 			task.setContents(definitions[index]);
 			taskService.addTask(task);
 			
 			Set<Execution> executions = Sets.newHashSet();
 			for (int j = 0; j < numExecutions; ++ j) {
 				Execution execution = new Execution("blabla", task);
 				executionService.addExecution(execution);
 				executions.add(execution);
 			}
 			task.setExecutions(executions);
 			taskService.saveTask(task);
 			tasks.add(task);
 		}
 		batch.setTasks(tasks);
 		batchService.saveBatch(batch);
 		
 		return batch;
 	}
 }
