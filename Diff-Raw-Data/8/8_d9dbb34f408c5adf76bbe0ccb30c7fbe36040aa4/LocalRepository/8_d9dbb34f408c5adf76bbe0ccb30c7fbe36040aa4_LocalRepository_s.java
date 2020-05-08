 /*
  * This file is part of TaskMan
  *
  * Copyright (C) 2012 Jed Barlow, Mark Galloway, Taylor Lloyd, Braeden Petruk
  *
  * TaskMan is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TaskMan is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TaskMan.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ca.cmput301.team13.taskman.model;
 
 import java.io.ByteArrayOutputStream;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.ShortBuffer;
 import java.util.ArrayList;
 import java.util.Date;
 
 import ca.cmput301.team13.taskman.model.Requirement.contentType;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import ca.cmput301.team13.taskman.TaskMan;
 
 /**
  * Manages a <a href="http://sqlite.org/">SQLite<a> database stored
  * locally on the phone memory.
  */
 public class LocalRepository {
     //Database connection
     private SQLiteDatabase db = null;
     //Database constants and actions
     private RepoHelper helper;
     //Virtual Repository link
     private VirtualRepository vr;
     //The latest modification date
     private Date newestModification;
 
     /**
      * Creates a new LocalRepository instance.
      * @param context the Context of the Android application
      * @param vr the virtual repository
      */
     public LocalRepository(Context context, VirtualRepository vr) {
         helper = new RepoHelper(context);
         this.vr = vr;
         newestModification = new Date(0); //Initialize to a really old date
     }
 
     void open() throws SQLException {
         db = helper.getWritableDatabase();
     }
 
     /**
      * Opens a test connection.
      */
     public void openTestConnection() {
         db = SQLiteDatabase.create(null);
     }
     
     void close() {
         helper.close();
     }
 
     void assertOpen() {
         if (db == null) throw new RuntimeException("" +
                 "LocalRepository: The repo's DB connection needs to be instantiated " +
                 "before creating a task. Call open() first.");
     }
     
     /**
      * Creates a new Task with a default ID
      * @see createTask(User, int) for full implementation details
      */
     Task createTask(User creator) {
     	return createTask(creator, -1);
     }
 
     /**
      * Creates a new Task, with no title, description, or requirements
      * 		*NOTE: The provided ID is used as this Task's ID. It MUST not conflict with a current ID.
      * @param creator The User that has created the Task
      * @return the Task, with no non-housekeeping values yet set
      */
     Task createTask(User creator, int id) {
         assertOpen();
 
         Task t = null;
         ContentValues values = new ContentValues();
         if(id > 0) {
         	values.put(RepoHelper.ID_COL, id);
         }
         values.put(RepoHelper.WEBID_COL, "");
         values.put(RepoHelper.CREATED_COL, new Date().getTime());
         values.put(RepoHelper.LASTMODIFIED_COL, new Date().getTime());
         values.put(RepoHelper.TITLE_COL, "");
         values.put(RepoHelper.DESC_COL, "");
         values.put(RepoHelper.CREATOR_COL, creator.toString());
         long insertId = db.insert(RepoHelper.TASKS_TBL, null, values);
 
         if(insertId == -1) {
             Log.w("LogStore", "Failed to write LogEntry into database.");
             throw new RuntimeException("Database Write Failure.");
         }
 
         Cursor cursor = db.query(RepoHelper.TASKS_TBL,
                 RepoHelper.TASKS_COLS, RepoHelper.ID_COL + " = " + insertId, null,
                 null, null, null);
 
         if (cursor.moveToFirst()) {
             t = new Task(
                     cursor.getInt(0),//ID
                     new Date(cursor.getLong(4)),//Date Created
                     new Date(cursor.getLong(5)),//Date Last Modified
                     new User(cursor.getString(3)),//Creator
                     cursor.getString(1),//Title
                     cursor.getString(2),//Description
                     new ArrayList<Requirement>(),//Current requirements
                     vr
                     );
             updateNewestModificationDate(t);
             t.setWebID(cursor.getString(6));
         }
         
         cursor.close();
         return t;
     }
     
     Task createTask(Task t) {
     	Task createdTask = createTask(t.getCreator(), t.getId());
     	createdTask.delaySaves(true);
     	createdTask.setWebID(t.getWebID());
     	createdTask.setTitle(t.getTitle());
     	createdTask.setDescription(t.getDescription());
     	createdTask.setLastModifiedDate(t.getLastModifiedDate());
     	updateNewestModificationDate(createdTask);
     	createdTask.reqsLoaded = false;
     	createdTask.reqCount = 0;
     	createdTask.isLocal = t.isLocal;
     	createdTask.delaySaves(false);
     	return createdTask;
     }
     
     /**
      * Creates a new Requirement, with no description or fulfillments. Adds a default ID.
      * @see createRequirement(User, Task, Requirement.contentType, int) for full implementation.
      */
     Requirement createRequirement(User creator, Task task, Requirement.contentType contentType) {
     	return createRequirement(creator, task, contentType, -1);
     }
 
     /**
      * Creates a new Requirement, with no description or fulfillments
      * @param creator The User that has created the Requirement
      * @param task The Task to add the Requirement to
      * @param contentType The desired content type of the requirement
      * @param id	The desired ID for the Requirement. -1 if a default ID should be generated.
      * @return the Requirement, with no non-housekeeping values yet set
      */
     Requirement createRequirement(User creator, Task task, Requirement.contentType contentType, int id) {
         assertOpen();
         ContentValues values = new ContentValues();
         //If a custom ID is supplied, insert it
         if(id > 0) {
         	values.put(RepoHelper.ID_COL, id);
         }
         values.put(RepoHelper.WEBID_COL, "");
         values.put(RepoHelper.CREATED_COL, new Date().getTime());
         values.put(RepoHelper.LASTMODIFIED_COL, new Date().getTime());
         values.put(RepoHelper.TASK_COL, task.getId());
         values.put(RepoHelper.DESC_COL, "");
         values.put(RepoHelper.CONTENTTYPE_COL, contentType.ordinal());
         values.put(RepoHelper.CREATOR_COL, creator.toString());
         long insertId = db.insert(RepoHelper.REQS_TBL, null, values);
 
         if(insertId == -1) {
             Log.w("LogStore", "Failed to write LogEntry into database.");
             throw new RuntimeException("Database Write Failure.");
         }
 
         Cursor cursor = db.query(RepoHelper.REQS_TBL,
                 RepoHelper.REQS_COLS, RepoHelper.ID_COL + " = " + insertId, null,
                 null, null, null);
         cursor.moveToFirst();
 
         Requirement r = new Requirement(
                 cursor.getInt(0),//ID
                 new Date(cursor.getLong(5)),//Date Created
                 new Date(cursor.getLong(6)),//Date Last Modified
                 new User(cursor.getString(4)),//Creator
                 cursor.getString(3),//Description
                 Requirement.contentType.values()[cursor.getInt(2)],//Content Type
                 new ArrayList<Fulfillment>(),//Current requirements
                 vr
                 );
         
         r.setWebID(cursor.getString(7));
         task.addRequirement(r);
         updateNewestModificationDate(r);
 
         cursor.close();
         return r;
     }
     
     /**
      * Creates a new Fulfillment, with no content yet set. An ID will be generated.
      * @see createFulfillment(User, Requirement, int) for full implementation
      */
     Fulfillment createFulfillment(User creator, Requirement req) {
     	return createFulfillment(creator, req, -1);
     }
 
     /**
      * Creates a new Fulfillment, with no content yet set
      * @param creator The User that has created the Fulfillment
      * @param req The Requirement to add the FUlfillment to
      * @param id	The desired ID for the Fulfillment. -1 if a default ID should be generated.
      * @return the Fulfillment, with no content yet attached
      */
     Fulfillment createFulfillment(User creator, Requirement req, int id) {
         assertOpen();
         ContentValues values = new ContentValues();
         //If an ID was supplied, use it
         if(id > 0) {
         	values.put(RepoHelper.ID_COL, id);
         }
         values.put(RepoHelper.WEBID_COL, "");
         values.put(RepoHelper.CREATED_COL, new Date().getTime());
         values.put(RepoHelper.LASTMODIFIED_COL, new Date().getTime());
         values.put(RepoHelper.REQ_COL, req.getId());
         values.put(RepoHelper.CONTENTTYPE_COL, req.getContentType().ordinal());
         values.put(RepoHelper.CREATOR_COL, creator.toString());
         long insertId = db.insert(RepoHelper.FULS_TBL, null, values);
 
         if(insertId == -1) {
             Log.w("LogStore", "Failed to write LogEntry into database.");
             throw new RuntimeException("Database Write Failure.");
         }
 
         Cursor cursor = db.query(RepoHelper.FULS_TBL,
                 RepoHelper.FULS_COLS, RepoHelper.ID_COL + " = " + insertId, null,
                 null, null, null);
         cursor.moveToFirst();
 
         Fulfillment f = new Fulfillment(
                 cursor.getInt(0),//ID
                 new Date(cursor.getLong(4)),//Date Created
                 new Date(cursor.getLong(5)),//Date Last Modified
                 Requirement.contentType.values()[cursor.getInt(6)],//Content Type
                 new User(cursor.getString(3)),//Creator
                 vr
                 );
 
         f.setWebID(cursor.getString(7));
         req.addFulfillment(f);
         updateNewestModificationDate(f);
 
         cursor.close();
         return f;
     }
 
     /**
      * Updates the backing structure to acknowledge any changes that have occurred
      * @param t the Task to update
      */
     void updateTask(Task t) {
     	ArrayList<Requirement> existingRequirements;
     	int numTaskRequirements = t.getRequirementCount();
     	
         assertOpen();
         ContentValues values = new ContentValues();
         values.put(RepoHelper.WEBID_COL, t.getWebID());
         values.put(RepoHelper.CREATED_COL, t.getCreatedDate().getTime());
         values.put(RepoHelper.LASTMODIFIED_COL, t.getLastModifiedDate().getTime());
         values.put(RepoHelper.TITLE_COL, t.getTitle());
         values.put(RepoHelper.DESC_COL, t.getDescription());
         values.put(RepoHelper.CREATOR_COL, t.getCreator().toString());
         
         existingRequirements = loadRequirements(t.getId());
         updateNewestModificationDate(t);
         
         //TODO: Increase efficiency here using HashMaps as Requirement lists?
         for(Requirement r : existingRequirements) {
         	boolean keep = false;
         	for(int i=0; i<numTaskRequirements; i++) {
         		//If the Requirement is still present after editing, keep it
         		if(t.getRequirement(i).getId() == r.getId()) {
         			keep = true;
         		}
         	}
         	//If it's been removed, remove it from the Repo
         	if(!keep) {
         		removeRequirement(r);
         	}
         }
         
         //Only update a Task created by the current User
         String creator = t.getCreator().toString();
         String current = TaskMan.getInstance().getUser().toString();
         if(t.getCreator().equals(TaskMan.getInstance().getUser())) {
         	int updateCount = db.update(RepoHelper.TASKS_TBL, values, RepoHelper.ID_COL + "=" + t.getId(), null);
         	if(updateCount != 1)
         		throw new RuntimeException("Database update failed!");
         } else {
         	throw new RuntimeException("Attempted to update a Task from a foreign User.");
         }
 
     }
 
     /**
      * Update the backing structure to acknowledge any changes that have occurred
      * @param r the Requirement to update
      */
     void updateRequirement(Requirement r) {
     	ArrayList<Fulfillment> existingFulfillments;
     	int numFulfillments = r.getFullfillmentCount();
     	
         assertOpen();
         ContentValues values = new ContentValues();
         values.put(RepoHelper.WEBID_COL, r.getWebID());
         values.put(RepoHelper.CREATED_COL, r.getCreatedDate().getTime());
         values.put(RepoHelper.LASTMODIFIED_COL, r.getLastModifiedDate().getTime());
         values.put(RepoHelper.ID_COL, r.getId());
         values.put(RepoHelper.DESC_COL, r.getDescription());
         values.put(RepoHelper.CONTENTTYPE_COL, r.getContentType().ordinal());
         values.put(RepoHelper.CREATOR_COL, r.getCreator().toString());
         
         existingFulfillments = loadFulfillments(r.getId(), r.getContentType());
         updateNewestModificationDate(r);
         
       //TODO: Increase efficiency here using HashMaps as Requirement lists?
         for(Fulfillment f : existingFulfillments) {
         	boolean keep = false;
         	for(int i=0; i<numFulfillments; i++) {
         		//If the Requirement is still present after editing, keep it
         		if(r.getFulfillment(i).getId() == f.getId()) {
         			keep = true;
         		}
         	}
         	//If it's been removed, remove it from the Repo
         	if(!keep) {
         		removeRequirement(r);
         	}
         }
 
         int updateCount = db.update(RepoHelper.REQS_TBL, values, RepoHelper.ID_COL + "=" + r.getId(), null);
 
         if(updateCount != 1)
             throw new RuntimeException("Database update failed!");
     }
 
     /**
      * Update the backing structure to acknowledge any changes that have occurred
      * @param f the Fulfillment to update
      */
     void updateFulfillment(Fulfillment f) {
         assertOpen();
         ContentValues values = new ContentValues();
         values.put(RepoHelper.WEBID_COL, f.getWebID());
         values.put(RepoHelper.CREATED_COL, f.getCreatedDate().getTime());
         values.put(RepoHelper.LASTMODIFIED_COL, f.getLastModifiedDate().getTime());
         values.put(RepoHelper.ID_COL, f.getId());
 
         //Logic to convert typed Fulfillment content into a blob
         switch(f.getContentType()) {
         case text:
             //Store a byte array for the text
             values.put(RepoHelper.CONTENT_COL, f.getText().getBytes());
             break;
         case audio:
             //Directly convert the short[] to byte[]
             short[] audio = f.getAudio();
             byte[] audioBytes = new byte[audio.length * 2];
             ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(audio);
             values.put(RepoHelper.CONTENT_COL, audioBytes);
             break;
         case video:
             //Directly convert the short[] to byte[]
             short[] video = f.getVideo();
             byte[] videoBytes = new byte[video.length * 2];
             ByteBuffer.wrap(videoBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(video);
             values.put(RepoHelper.CONTENT_COL, videoBytes);
             break;
         case image:
             //Compress and store the image
             ByteArrayOutputStream imageWriter = new ByteArrayOutputStream();
             f.getImage().compress(Bitmap.CompressFormat.JPEG, 70, imageWriter);
             values.put(RepoHelper.CONTENT_COL, imageWriter.toByteArray());
             break;
         }
 
         values.put(RepoHelper.CREATOR_COL, f.getCreator().toString());
         
         updateNewestModificationDate(f);
 
         int updateCount = db.update(RepoHelper.FULS_TBL, values, RepoHelper.ID_COL + "=" + f.getId(), null);
 
         if(updateCount != 1)
             throw new RuntimeException("Database update failed!");
     }
 
     /**
      * Load a list of tasks, restricted by the defined TaskFilter
      * @param	filter		TaskFilter			The filter by which to restrict results
      * @return				ArrayList<Task>		The list of tasks received from the DB
      */
     ArrayList<Task> loadTasks(TaskFilter filter) {
         assertOpen();
         ArrayList<Task> tasks = new ArrayList<Task>();
 
         Cursor cursor = db.query(RepoHelper.TASKS_TBL, RepoHelper.TASKS_COLS, filter.toString(), null, null, null, null);
 
         if (cursor.moveToFirst()) {
             do {
                 Task t = new Task(
                         cursor.getInt(0),//ID
                         new Date(cursor.getLong(4)),//Date Created
                         new Date(cursor.getLong(5)),//Date Last Modified
                         new User(cursor.getString(3)),//Creator
                         cursor.getString(1),//Title
                         cursor.getString(2),//Description
                         loadRequirements(cursor.getInt(0)),//Current requirements
                         vr
                         );
                t.setWebID(cursor.getString(7));
                 tasks.add(t);
                 updateNewestModificationDate(t);
             } while (cursor.moveToNext());
         }
 
         return tasks;
     }
 
     /**
      * Loads a list of Requirements for the specified Task
      * @param	t 	Task 						The Task to find Requirements for
      * @return		ArrayList<Requirement>		The list of Requirements for the specified Task
      */
     ArrayList<Requirement> loadRequirementsForTask(Task t) {
         return loadRequirements(t.getId());
     }
 
     /**
      * Loads a list of Requirements for the specified Task
      * @param	taskId 	integer 					The Task ID to find Requirements for
      * @return			ArrayList<Requirement>		The list of Requirements for the specified Task
      */
     private ArrayList<Requirement> loadRequirements(int taskId) {
         assertOpen();
         ArrayList<Requirement> reqs = new ArrayList<Requirement>();
 
         Cursor cursor = db.query(RepoHelper.REQS_TBL,
                 RepoHelper.REQS_COLS, RepoHelper.TASK_COL + " = " + taskId, null,
                 null, null, null);
 
         //If we have requirements, load them
         if(cursor.moveToFirst()) {
             do {
                 Requirement r = new Requirement(
                         cursor.getInt(0),//ID
                         new Date(cursor.getLong(5)),//Date Created
                         new Date(cursor.getLong(6)),//Date Last Modified
                         new User(cursor.getString(4)),//Creator
                         cursor.getString(3),//Description
                         Requirement.contentType.values()[cursor.getInt(2)],//Content Type
                         getFulfillmentCount(cursor.getInt(0)),//load current fulfillments
                         vr
                         );
                r.setWebID(cursor.getString(7));
                 reqs.add(r);
                 updateNewestModificationDate(r);
             } while (cursor.moveToNext());
         }
 
         return reqs;
     }
 
     /**
      * Loads a list of fulfillments for the specified requirement
      * @param	r 	Requirement 				The requirement to find fulfillments for
      * @return		ArrayList<Fulfillment>		The list of fulfillments for the specified requirement
      */
     ArrayList<Fulfillment> loadFulfillmentsForRequirement(Requirement r) {
         return loadFulfillments(r.getId(), r.getContentType());
     }
 
     /**
      * Loads a list of fulfillments for the specified requirement ID
      * @param	r 	Requirement 				The requirement to find fulfillments for
      * @return		ArrayList<Fulfillment>		The list of fulfillments for the specified requirement
      */
     private ArrayList<Fulfillment> loadFulfillments(int reqId, contentType reqContentType) {
         assertOpen();
         ArrayList<Fulfillment> fulfillments = new ArrayList<Fulfillment>();
 
         Cursor cursor = db.query(RepoHelper.FULS_TBL,
                 RepoHelper.FULS_COLS, RepoHelper.REQ_COL + " = " + reqId, null,
                 null, null, null);
 
         //TODO: Content should be loaded on instantiation, not afterwards.
         // (Afterwards triggers a write-back)
         
         if(cursor.moveToFirst()) {
             do {
                 Fulfillment f = new Fulfillment(
                         cursor.getInt(0),//ID
                         new Date(cursor.getLong(5)),//Date Created
                         new Date(cursor.getLong(6)),//Date Last Modified
                         reqContentType, //Content type
                         new User(cursor.getString(4)),//Creator
                         vr
                         );
                 if(!cursor.isNull(2)) {
                     switch(f.getContentType()) {
                     case text:
                         //COnvert the Byte Array to a String
                         f.setText(new String(cursor.getBlob(2)));
                         break;
                     case audio:
                         //Directly convert the byte[] to short[]
                         byte[] audioBytes = cursor.getBlob(2);
                         ByteBuffer bb = ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN);
                         short[] audio = new short[audioBytes.length/2];
                         for(int i=0;i<audio.length;i++)
                             audio[i]=bb.getShort();
                         f.setAudio(audio);
                         break;
                     case video:
                         //Directly convert the byte[] to short[]
                         byte[] videoBytes = cursor.getBlob(2);
                         ByteBuffer vbb = ByteBuffer.wrap(videoBytes).order(ByteOrder.LITTLE_ENDIAN);
                         short[] video = new short[videoBytes.length/2];
                         for(int i=0;i<video.length;i++)
                             video[i]=vbb.getShort();
                         f.setVideo(video);
                         break;
                     case image:
                         //Convert the byte array to a Bitmap
                         byte[] imgBytes = cursor.getBlob(2);
                         f.setImage(BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length));
                         break;
                     }
                 }
                f.setWebID(cursor.getString(7));
                 fulfillments.add(f);
                 updateNewestModificationDate(f);
             } while (cursor.moveToNext());
         }
 
         return fulfillments;
     }
 
 
     /**
      * Get the Task corresponding to the given Task Id
      * @param taskId the ID
      * @return the Task
      */
     Task getTask(int taskId) {
 
         Cursor cursor = db.query(RepoHelper.TASKS_TBL,
                 RepoHelper.TASKS_COLS, RepoHelper.ID_COL + " = " + taskId, null,
                 null, null, null);
 
         if (cursor.moveToFirst()) {
             Task t = new Task(
                     cursor.getInt(0),//ID
                     new Date(cursor.getLong(4)),//Date Created
                     new Date(cursor.getLong(5)),//Date Last Modified
                     new User(cursor.getString(3)),//Creator
                     cursor.getString(1),//Title
                     cursor.getString(2),//Description
                     loadRequirements(taskId),//Current requirements
                     vr
                     );
             t.setWebID(cursor.getString(6));
             cursor.close();
             return t;
         } else {
             cursor.close();
             return null;
         }
     }
     
     /**
      * Get updated data for the requested Task
      * @param t		The task to get updated data for
      * @return		The updated Task
      */
     Task getTaskUpdate(Task t) {
     	return getTask(t.getId());
     }
     
     /**
      * Get updated data for the requested Requirement
      * @param t		The Requirement to get updated data for
      * @return		The updated Requirement
      */
     Requirement getRequirementUpdate(Requirement r) {
     	return getRequirement(r.getId());
     }
     
     /**
      * Get updated data for the requested Fulfillment
      * @param t		The Fulfillment to get updated data for
      * @return		The updated Fulfillment
      */
     Fulfillment getFulfillmentUpdate(Fulfillment f) {
     	return getFulfillment(f.getId());
     }
 
     /**
      * Get the Requirement corresponding to the given Requirement Id
      * @param requirementId the ID
      * @return the Requirement
      */
     Requirement getRequirement(int requirementId) {
 
         Cursor cursor = db.query(RepoHelper.REQS_TBL,
                 RepoHelper.REQS_COLS, RepoHelper.ID_COL + " = " + requirementId, null,
                 null, null, null);
 
         if (cursor.moveToFirst()) {
             Requirement r = new Requirement(
                     cursor.getInt(0),//ID
                     new Date(cursor.getLong(5)),//Date Created
                     new Date(cursor.getLong(6)),//Date Last Modified
                     new User(cursor.getString(4)),//Creator
                     cursor.getString(3),//Description,
                     Requirement.contentType.values()[cursor.getInt(2)],
                     getFulfillmentCount(requirementId),
                     vr
                     );
             r.setWebID(cursor.getString(7));
             cursor.close();
             return r;
         } else {
             cursor.close();
             return null;
         }
     }
 
     /**
      * Get the number of Fulfillments for the specified Requirement
      * @param requirementId		The ID of the requirement
      * @return					The number of Fulfillments contained by the specified Requirement
      */
     private int getFulfillmentCount(int requirementId) {
         Cursor cursor = db.query(RepoHelper.FULS_TBL,
                 new String[] {"COUNT(*)"}, RepoHelper.REQ_COL + " = " + requirementId, null,
                 null, null, null);
 
         if (cursor.moveToFirst()) {
             int count = cursor.getInt(0);//Count
             cursor.close();
             return count;
         } else {
             cursor.close();
             return 0;
         }
     }
 
     /**
      * Get the Fulfillment corresponding to the given Fulfillment Id
      * @param fulfillmentId the ID
      * @return the Fulfillment
      */
     Fulfillment getFulfillment(int fulfillmentId) {
 
         Cursor cursor = db.query(RepoHelper.FULS_TBL,
                 RepoHelper.FULS_COLS, RepoHelper.ID_COL + " = " + fulfillmentId, null,
                 null, null, null);
 
         if (cursor.moveToFirst()) {
             Fulfillment f = new Fulfillment(
                     cursor.getInt(0),//ID
                     new Date(cursor.getLong(4)),//Date Created
                     new Date(cursor.getLong(5)),//Date Last Modified
                     Requirement.contentType.values()[cursor.getInt(6)],
                     new User(cursor.getString(3)),//Creator
                     vr
                     );
             f.setWebID(cursor.getString(7));
             cursor.close();
             return f;
         } else {
             cursor.close();
             return null;
         }
     }
 
     /**
      * Remove a Specified Task from the backing store. All references to the Task object should be discarded
      * @param t The Task
      */
     void removeTask(Task t) {
         int numRequirements = t.getRequirementCount();
         for(int i=0; i<numRequirements; i++) {
             removeRequirement(t.getRequirement(i));
         }
     	db.delete(RepoHelper.TASKS_TBL, RepoHelper.ID_COL + " = " + t.getId(), null);
     }
 
     /**
      * Remove a Specified Requirement from the backing store. All references to the Requirement object should be discarded
      * @param r The Requirement
      */
     void removeRequirement(Requirement r) {
         int numFulfillments = r.getFullfillmentCount();
         for(int i=0; i<numFulfillments; i++) {
     		removeFulfillment(r.getFulfillment(i));
         }
         db.delete(RepoHelper.REQS_TBL, RepoHelper.ID_COL + " = " + r.getId(), null);
     }
 
     /**
      * Remove a Specified Fulfillment from the backing store. All references to the Fulfillment object should be discarded
      * @param f The Fulfillment
      */
     void removeFulfillment(Fulfillment f) {
         db.delete(RepoHelper.FULS_TBL, RepoHelper.ID_COL + " = " + f.getId(), null);
     }
     
     /**
      * Get the newest modification that has been done in the LocalRepository
      * @return The newest modification date
      */
     Date getNewestModification() {
     	return newestModification;
     }
 
     /**
      * Set the newest modification that has been done in the LocalRepository
      * @param date	The newest modification date
      */
     private void setNewestModification(Date date) {
     	newestModification = date;
     }
     
     /**
      * If the provided BackedObject has a modification date that is newer than the current newestModificationDate
      * the value is updated
      * @param bo	The BackedObject to compare dates against
      */
     private void updateNewestModificationDate(BackedObject bo) {
     	if(bo.getLastModifiedDate().after(getNewestModification())) setNewestModification(bo.getLastModifiedDate());
     }
 
 }
 
 class RepoHelper  extends SQLiteOpenHelper{
     //Tables
     public static final String TASKS_TBL = "tasks";
     public static final String REQS_TBL = "requirements";
     public static final String FULS_TBL = "fulfillments";
     //Columns
     public static final String ID_COL = "id";
     public static final String WEBID_COL = "webID";
     public static final String TITLE_COL = "title";
     public static final String DESC_COL = "description";
     public static final String CREATED_COL = "created";
     public static final String LASTMODIFIED_COL = "lastModified";
     public static final String CREATOR_COL = "creator";
     public static final String TASK_COL = "taskID";
     public static final String REQ_COL = "requirementID";
     public static final String CONTENTTYPE_COL = "contentType";
     public static final String CONTENT_COL = "content";
     //Columns as found in tables
     public static final String[] TASKS_COLS = {ID_COL, TITLE_COL, DESC_COL, CREATOR_COL, CREATED_COL, LASTMODIFIED_COL, WEBID_COL};
     private static final String[] TASKS_COLTYPES = {"integer primary key autoincrement", "text not null", "integer not null", "integer not null", "integer not null", "integer not null", "text not null"};
     public static final String[] REQS_COLS = {ID_COL, TASK_COL, CONTENTTYPE_COL, DESC_COL, CREATOR_COL, CREATED_COL, LASTMODIFIED_COL, WEBID_COL};
     private static final String[] REQS_COLTYPES = {"integer primary key autoincrement", "integer not null", "integer not null", "text not null", "text not null", "integer not null", "integer not null", "text not null"};
     public static final String[] FULS_COLS = {ID_COL, REQ_COL, CONTENT_COL, CREATOR_COL, CREATED_COL, LASTMODIFIED_COL, CONTENTTYPE_COL, WEBID_COL};
     private static final String[] FULS_COLTYPES = {"integer primary key autoincrement", "integer not null", "blob", "text not null", "integer not null", "integer not null", "integer not null", "text not null"};
     //The name of our database, and SQL Schema version
     private static final String DBNAME = "taskman.db";
     private static final int VERSION = 1;
 
     public RepoHelper(Context context) {
         super(context, DBNAME, null, VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase database) {
         //Construct our create table statements
         String tasksCreate = "create table "+TASKS_TBL+" ( ";
         for(int i=0;i<TASKS_COLS.length;i++) {
             tasksCreate += TASKS_COLS[i] + " " + TASKS_COLTYPES[i];
             if(i<TASKS_COLS.length-1)
                 tasksCreate += ", ";
         }
         tasksCreate += ");";
 
         String reqsCreate = "create table "+REQS_TBL+" ( ";
         for(int i=0;i<REQS_COLS.length;i++) {
             reqsCreate += REQS_COLS[i] + " " + REQS_COLTYPES[i];
             if(i<REQS_COLS.length-1)
                 reqsCreate += ", ";
         }
         reqsCreate += ");";
 
         String fulsCreate = "create table "+FULS_TBL+" ( ";
         for(int i=0;i<FULS_COLS.length;i++) {
             fulsCreate += FULS_COLS[i] + " " + FULS_COLTYPES[i];
             if(i<FULS_COLS.length-1)
                 fulsCreate += ", ";
         }
         fulsCreate += ");";
 
         //Actually run the SQL
         database.execSQL(tasksCreate);
         database.execSQL(reqsCreate);
         database.execSQL(fulsCreate);
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.w("SQLite Helper", "Upgrading database from version " + oldVersion + " to "
                 + newVersion + ", wiping Database");
         db.execSQL("DROP TABLE IF EXISTS " + TASKS_TBL);
         db.execSQL("DROP TABLE IF EXISTS " + REQS_TBL);
         db.execSQL("DROP TABLE IF EXISTS " + FULS_TBL);
         onCreate(db);
     }
 
     @Override
     public void onOpen(SQLiteDatabase db) {
         //Enabled Object Persistence
         /*Log.w("SQLite Helper", "Wiping local SQLite DB, preventing persistence");
         db.execSQL("DROP TABLE IF EXISTS " + TASKS_TBL);
         db.execSQL("DROP TABLE IF EXISTS " + REQS_TBL);
         db.execSQL("DROP TABLE IF EXISTS " + FULS_TBL);
         onCreate(db);*/
     }
 }
