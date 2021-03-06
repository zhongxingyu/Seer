 /*******************************************************************************
  * Copyright (c) 2011 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.orion.server.core.tasks;
 
 import java.net.URI;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.orion.server.core.*;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * Represents a snapshot of the state of a long running task.
  */
 public class TaskInfo {
 	private static final String KEY_PERCENT_COMPLETE = "PercentComplete"; //$NON-NLS-1$
 	private static final String KEY_ID = "Id"; //$NON-NLS-1$
 	private static final String KEY_USER = "User"; //$NON-NLS-1$
 	private static final String KEY_MESSAGE = "Message"; //$NON-NLS-1$
 	private static final String KEY_RUNNING = "Running"; //$NON-NLS-1$
 	private static final String KEY_LOCATION = "Location"; //$NON-NLS-1$
 	private static final String KEY_RESULT = "Result"; //$NON-NLS-1$
 	private static final String KEY_CAN_BE_CANCELED = "CanBeCanceled"; //$NON-NLS-1$
 	private final String id;
 	private final String userId;
 	private String message = ""; //$NON-NLS-1$
 	private int percentComplete = 0;
 	private boolean running = true;
 	private URI resultLocation = null;
 	private IStatus result;
 	private ITaskCanceler taskCanceler;
 
 	/**
 	 * Returns a task object based on its JSON representation. Returns
 	 * null if the given string is not a valid JSON task representation.
 	 */
 	public static TaskInfo fromJSON(String taskString) {
 		return fromJSON(taskString, null);
 	}
 	
 	public static TaskInfo fromJSON(String taskString, ITaskCanceler taskCanceler) {
 		TaskInfo info;
 		try {
 			JSONObject json = new JSONObject(taskString);
 			info = new TaskInfo(json.getString(KEY_USER), json.getString(KEY_ID), taskCanceler);
 			info.setMessage(json.optString(KEY_MESSAGE, "")); //$NON-NLS-1$
 			info.running = json.optBoolean(KEY_RUNNING, true);
 			info.setPercentComplete(json.optInt(KEY_PERCENT_COMPLETE, 0));
 			String location = json.optString(KEY_LOCATION, null);
 			if (location != null)
 				info.resultLocation = URI.create(location);
 			String resultString = json.optString(KEY_RESULT, null);
 			if (resultString != null)
 				info.result = ServerStatus.fromJSON(resultString);
 			return info;
 		} catch (JSONException e) {
 			LogHelper.log(new Status(IStatus.ERROR, ServerConstants.PI_SERVER_CORE, "Invalid task: " + taskString, e)); //$NON-NLS-1$
 			return null;
 		}
 	}
 
 	public TaskInfo(String userId, String id) {
 		this.userId = userId;
 		this.id = id;
 	}
 
 	public TaskInfo(String userId, String id, ITaskCanceler taskCanceler) {
 		this.userId = userId;
 		this.id = id;
 		this.taskCanceler = taskCanceler;
 	}
 
 	/**
 	 * Cancels the task.
 	 * @throws TaskOperationException if task does not support canceling
 	 */
 	public void cancelTask() throws TaskOperationException {
 		if (this.taskCanceler == null) {
 			throw new TaskOperationException("Task does not support canceling.");
 		}
 		taskCanceler.cancelTask();
 	}
 	
 	/**
 	 * Returns information if task canceling is supported.
 	 * @return <code>true</code> if task can be canceled
 	 */
 	public boolean canBeCanceled(){
 		return this.taskCanceler!=null;
 	}
 
 	/**
 	 * Returns a message describing the current progress state of the task, or the
 	 * result if the task is completed.
 	 * @return the message
 	 */
 	public String getMessage() {
 		return message;
 	}
 
 	/**
 	 * @return an integer between 0 and 100 representing the current progress state
 	 */
 	public int getPercentComplete() {
 		return percentComplete;
 	}
 
 	/**
 	 * Returns the location of the resource representing the result of the computation. 
 	 * Returns <code>null</code> if the task has not completed, or if it did not complete successfully
 	 * @return The task result location, or <code>null</code>
 	 */
 	public URI getResultLocation() {
 		return resultLocation;
 	}
 
 	/**
 	 * Returns the status describing the result of the operation, or <code>null</code>
 	 * if the operation has not yet completed.
 	 * @return The result status
 	 */
 	public IStatus getResult() {
 		return result;
 	}
 
 	public String getTaskId() {
 		return id;
 	}
 
 	public String getUserId() {
 		return userId;
 	}
 
 	/**
 	 * Returns whether the task is currently running.
 	 * @return <code>true</code> if the task is currently running, and
 	 * <code>false</code> otherwise.
 	 */
 	public boolean isRunning() {
 		return running;
 	}
 
 	/**
 	 * Sets a message describing the current task state. 
 	 * @param message the message to set
 	 * @return Returns this task
 	 */
 	public TaskInfo setMessage(String message) {
 		this.message = message == null ? "" : message; //$NON-NLS-1$
 		return this;
 	}
 
 	/**
 	 * Sets the percent complete of this task. Values below 0 will be rounded up to zero,
 	 * and values above 100 will be rounded down to 100;
 	 * @param percentComplete an integer between 0 and 100 representing the
 	 * current progress state
 	 * @return Returns this task
 	 */
 	public TaskInfo setPercentComplete(int percentComplete) {
 		if (percentComplete < 0)
 			percentComplete = 0;
 		if (percentComplete > 100)
 			percentComplete = 100;
 		this.percentComplete = percentComplete;
 		return this;
 	}
 
 	/**
 	 * Indicates that this task is completed.
 	 * @param status The result status
 	 * @return Returns this task
 	 */
 	public TaskInfo done(IStatus status) {
 		this.running = false;
 		this.percentComplete = 100;
 		this.result = status;
 		this.message = status.getMessage();
 		return this;
 	}
 
 	/**
 	 * Sets the location of the task result object.
 	 * @param location The location of the result resource for the task, or
 	 * <code>null</code> if not applicable.
 	 * @return Returns this task
 	 */
 	public TaskInfo setResultLocation(String location) {
 		this.resultLocation = URI.create(location);
 		return this;
 	}
 
 	/**
 	 * Sets the location of the task result object.
 	 * @param location The location of the result resource for the task, or
 	 * <code>null</code> if not applicable.
 	 * @return Returns this task
 	 */
 	public TaskInfo setResultLocation(URI location) {
 		this.resultLocation = location;
 		return this;
 	}
 
 	/**
 	 * Returns a JSON representation of this task state.
 	 */
	public JSONObject toLightJSON() {
		JSONObject resultObject = new JSONObject();
		try {
			resultObject.put(KEY_RUNNING, isRunning());
			resultObject.put(KEY_MESSAGE, getMessage());
			resultObject.put(KEY_ID, getTaskId());
			resultObject.put(KEY_USER, getUserId());
			resultObject.put(KEY_PERCENT_COMPLETE, getPercentComplete());
			if(taskCanceler!=null)
				resultObject.put(KEY_CAN_BE_CANCELED, true);
		} catch (JSONException e) {
			//can only happen if key is null
		}
		return resultObject;
	}

	

	/**
	 * Returns a JSON representation of this task state.
	 */
 	public JSONObject toJSON() {
 		JSONObject resultObject = new JSONObject();
 		try {
 			resultObject.put(KEY_RUNNING, isRunning());
 			resultObject.put(KEY_MESSAGE, getMessage());
 			resultObject.put(KEY_ID, getTaskId());
 			resultObject.put(KEY_USER, getUserId());
 			resultObject.put(KEY_PERCENT_COMPLETE, getPercentComplete());
 			if(taskCanceler!=null)
 				resultObject.put(KEY_CAN_BE_CANCELED, true);
 			if (resultLocation != null)
 				resultObject.put(KEY_LOCATION, resultLocation);
 			if (result != null) {
 				resultObject.put(KEY_RESULT, ServerStatus.convert(result).toJSON());
 			}
 		} catch (JSONException e) {
 			//can only happen if key is null
 		}
 		return resultObject;
 	}
 
 	@Override
 	public String toString() {
 		return "TaskInfo" + toJSON(); //$NON-NLS-1$
 	}
 }
