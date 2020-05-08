 package com.crowdplatform.util;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Iterator;
 import java.util.List;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ArrayNode;
 
 import au.com.bytecode.opencsv.CSVWriter;
 
 import com.crowdplatform.model.Execution;
 import com.crowdplatform.model.Field;
 import com.crowdplatform.model.Task;
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.collect.ObjectArrays;
 
 public class FileWriter {
 	
 	public static final int NUM_STATIC_TASK_FIELDS = 1;
 	@VisibleForTesting
 	public static final int NUM_STATIC_EXECUTION_FIELDS = 3;
 	
 	public String writeTasksExecutions(List<Task> tasks, List<Field> taskFields, List<Field> executionFields, 
 			List<Field> userFields, Boolean header) throws IOException {
 		StringWriter writer = new StringWriter();
 		CSVWriter csvWriter = new CSVWriter(writer);
 		
 		if (header) {
 			String[] headers = writeHeaders(taskFields, executionFields, userFields);
 			csvWriter.writeNext(headers);
 		}
 		
 		for (Task task : tasks) {
 			writeTaskExecutions(csvWriter, task, taskFields, executionFields, userFields);
 		}
 
 		String result = writer.toString();
 		
 		csvWriter.close();
 		
 		return result;
 	}
 	
 	@VisibleForTesting
 	public String[] writeHeaders(List<Field> taskFields, List<Field> executionFields, List<Field> userFields) {
 		String[] taskHeaders = new String[NUM_STATIC_TASK_FIELDS + taskFields.size()];
 		taskHeaders[0] = "task_id";
 		for (int i = 0; i < taskFields.size(); ++i) {
 			taskHeaders[NUM_STATIC_TASK_FIELDS + i] = taskFields.get(i).getName();
 		}
 		
 		String[] executionHeaders = new String[NUM_STATIC_EXECUTION_FIELDS + executionFields.size() + userFields.size()];
 		executionHeaders[0] = "execution_id";
 		executionHeaders[1] = "date";
 		executionHeaders[2] = "userId";
 		for (int i = 0; i < executionFields.size(); ++i) {
 			executionHeaders[NUM_STATIC_EXECUTION_FIELDS + i] = executionFields.get(i).getName();
 		}
 		for (int i = 0; i < userFields.size(); ++i) {
 			executionHeaders[NUM_STATIC_EXECUTION_FIELDS + executionFields.size() + i] = userFields.get(i).getName();
 		}
 		
 		return ObjectArrays.concat(taskHeaders, executionHeaders, String.class);
 	}
 	
 	private void writeTaskExecutions(CSVWriter csvWriter, Task task, List<Field> taskFields, List<Field> executionFields, 
 			List<Field> userFields) throws IOException {
 		String[] taskValues = decodeTask(task, taskFields);
 		for (Execution execution : task.getExecutions()) {
 			String[] executionValues = decodeExecution(execution, executionFields, userFields);
 			csvWriter.writeNext(ObjectArrays.concat(taskValues, executionValues, String.class));
 		}
 	}
 	
 	@VisibleForTesting
 	public String[] decodeTask(Task task, List<Field> fields) {
 		String[] values = new String[NUM_STATIC_TASK_FIELDS];
 		values[0] = String.valueOf(task.getId());
 		
 		String[] taskValues = decode(task.getContents(), fields);
 		values = ObjectArrays.concat(values, taskValues, String.class);
 		return values;
 	}
 	
 	@VisibleForTesting
 	public String[] decodeExecution(Execution execution, List<Field> fields, List<Field> userFields) {
 		String[] values = new String[NUM_STATIC_EXECUTION_FIELDS];
 		values[0] = String.valueOf(execution.getId());
 		values[1] = execution.getDate().toString();
 		
 		String[] executionValues = decode(execution.getContents(), fields);
 		values = ObjectArrays.concat(values, executionValues, String.class);
 		
 		if (execution.getProjectUser() != null) {
 			values[2] = String.valueOf(execution.getProjectUser().getId());
 			String[] userValues = decode(execution.getProjectUser().getContents(), userFields);
 			values = ObjectArrays.concat(values,  userValues, String.class);
 		} else {
 			String[] userValues = new String[userFields.size()];
 			values = ObjectArrays.concat(values,  userValues, String.class);
 		}
 		
 		return values;
 	}
 	
 	@VisibleForTesting
 	public String[] decode(String contents, List<Field> fields) {
 		String[] values = new String[fields.size()];
 		try {
 			JsonNode node = new ObjectMapper().readTree(contents);
 			for (int i = 0; i < fields.size(); ++i) {
 				JsonNode fieldNode = node.get(fields.get(i).getName());
 				switch (fields.get(i).getType()) {
 				case STRING:
 					values[i] = fieldNode.getTextValue();
 					break;
 				case INTEGER:
 					values[i] = String.valueOf(fieldNode.getIntValue());
 					break;
 				case DOUBLE:
 					values[i] = String.valueOf(fieldNode.getDoubleValue());
 					break;
 				case MULTIVALUATE_STRING:
 					ArrayNode array = (ArrayNode)fieldNode;
 					Iterator<JsonNode> iterator = array.getElements();
 					values[i] = "";
 					while (iterator.hasNext()) {
 						values[i] += iterator.next().getTextValue() + ",";
 					}
 					break;
 				case BOOL:
 					values[i] = String.valueOf(fieldNode.getBooleanValue());
 					break;
 				}
 			}
 		} catch (JsonProcessingException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return values;
 	}
 }
