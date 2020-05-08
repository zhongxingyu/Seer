 package ca.ualberta.cs.completemytask.saving;
 
 import ca.ualberta.cs.completemytask.CompleteMyTask;
 import ca.ualberta.cs.completemytask.userdata.Comment;
 import ca.ualberta.cs.completemytask.userdata.MyPhoto;
 import ca.ualberta.cs.completemytask.userdata.Task;
 import ca.ualberta.cs.completemytask.userdata.TaskManager;
 import ca.ualberta.cs.completemytask.userdata.User;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 public class LocalSaving {
 	private static final String TAG = "LocalSaving";
 
 	private SQLiteDatabase database;
 	private SQLiteHelper dbHelper;
 
 	public LocalSaving() {
 		dbHelper = new SQLiteHelper(CompleteMyTask.getAppContext());
 	}
 
 	public void open() throws SQLException {
 		database = dbHelper.getWritableDatabase();
 	}
 
 	public void close() {
 		dbHelper.close();
 	}
 
 	public void saveTask(Task task) {
 
 		if (task.isLocal()) {
 			User user = task.getUser();
 	
 			String userName = null;
 			String email = null;
 	
 			if (user != null) {
 				userName = user.getUserName();
 				email = user.getEmail();
 			}
 			
 			// Get values of Task
 			ContentValues values = new ContentValues();
 	
 			values.put(SQLiteHelper.COLUMN_GLOBALID, task.getId());
 			values.put(SQLiteHelper.COLUMN_TASK_NAME, task.getName());
 			values.put(SQLiteHelper.COLUMN_TASK_DESCRIPTION, task.getDescription());
 			values.put(SQLiteHelper.COLUMN_TASK_COMPLETE, (task.isComplete()) ? 1
 					: 0);
 			values.put(SQLiteHelper.COLUMN_TASK_NEEDS_COMMENT,
 					(task.needsComment()) ? 1 : 0);
 			values.put(SQLiteHelper.COLUMN_TASK_NEEDS_PHOTO,
 					(task.needsPhoto()) ? 1 : 0);
 			values.put(SQLiteHelper.COLUMN_TASK_NEEDS_AUDIO,
 					(task.needsAudio()) ? 1 : 0);
 	
 			values.put(SQLiteHelper.COLUMN_USER, userName);
 			values.put(SQLiteHelper.COLUMN_EMAIL, email);
 	
 			if (task.getLocalId() == 0) {
 				// Insert
 				long insertId = database.insert(SQLiteHelper.TABLE_TASKS, null,
 						values);
 				// Set local id
 				task.setLocalId(insertId);
 			} else {
 				String strFilter = SQLiteHelper.COLUMN_ID + "=" + task.getLocalId();
 				database.update(SQLiteHelper.TABLE_TASKS, values, strFilter, null);
 			}
 		}
 
 	}
 
 	public void saveComment(Comment comment) {
 
 		// Get values of Task
 		ContentValues values = new ContentValues();
 
 		User user = comment.getUser();
 
 		String userName = null;
 
 		if (user != null) {
 			userName = user.getUserName();
 		}
 
 		values.put(SQLiteHelper.COLUMN_PARENT_ID, comment.getLocalParentId());
 		values.put(SQLiteHelper.COLUMN_GLOBALID, comment.getId());
 		values.put(SQLiteHelper.COLUMN_PARENT_GLOBALID, comment.getParentId());
 		values.put(SQLiteHelper.COLUMN_COMMENT, comment.getContent());
 		values.put(SQLiteHelper.COLUMN_USER, userName);
 
 		Log.v(TAG, "Saving Comment: " + comment.getContent());
 
 		if (comment.getLocalId() == 0) {
 			// Insert
 			long insertId = database.insert(SQLiteHelper.TABLE_COMMENTS, null,
 					values);
 			// Set local id
 			comment.setLocalId(insertId);
 
 			Log.v(TAG, "Insert");
 		} else {
 			String strFilter = SQLiteHelper.COLUMN_ID + "="
 					+ comment.getLocalId();
 			database.update(SQLiteHelper.TABLE_COMMENTS, values, strFilter,
 					null);
 
 			Log.v(TAG, "Update");
 		}
 
 	}
 
 	public void savePhoto(MyPhoto photo) {
 
 		// Get values of Task
 		ContentValues values = new ContentValues();
 
 		User user = photo.getUser();
 
 		String userName = null;
 
 		if (user != null) {
 			userName = user.getUserName();
 		}
 
 		values.put(SQLiteHelper.COLUMN_PARENT_ID, photo.getLocalParentId());
 		values.put(SQLiteHelper.COLUMN_GLOBALID, photo.getId());
 		values.put(SQLiteHelper.COLUMN_PARENT_GLOBALID, photo.getParentId());
 		values.put(SQLiteHelper.COLUMN_PHOTO, photo.getImageAsString());
 		values.put(SQLiteHelper.COLUMN_USER, userName);
 
 		Log.v(TAG, "Saving photo: " + photo.getImageAsString());
 
 		if (photo.getLocalId() == 0) {
 			// Insert
 			long insertId = database.insert(SQLiteHelper.TABLE_PHOTOS, null,
 					values);
 			// Set local id
 			photo.setLocalId(insertId);
 
			Log.v(TAG, "Insert: " + photo.getLocalId());
 		} else {
 			String strFilter = SQLiteHelper.COLUMN_ID + "="
 					+ photo.getLocalId();
 			database.update(SQLiteHelper.TABLE_PHOTOS, values, strFilter, null);
 
 			Log.v(TAG, "Update");
 		}
 
 	}
 
 	public void deleteTask(Task task) {
 		long id = task.getLocalId();
 		System.out.println("Comment deleted with id: " + id);
 		database.delete(SQLiteHelper.TABLE_TASKS, SQLiteHelper.COLUMN_ID
 				+ " = " + id, null);
 	}
 
 	public void saveAllTasks() {
 		for (Task task : TaskManager.getInstance().getTaskArray()) {
 			if (task.isLocal()) {
 				saveTask(task);
 	
 				for (int i = 0; i < task.getNumberOfComments(); i++) {
 					saveComment(task.getCommentAt(i));
 				}
 	
 				for (int i = 0; i < task.getNumberOfPhotos(); i++) {
 					savePhoto(task.getPhotoAt(i));
 				}
 			}
 		}
 	}
 
 	public void loadAllTasks() {
 		String[] col = { SQLiteHelper.COLUMN_ID, SQLiteHelper.COLUMN_GLOBALID,
 				SQLiteHelper.COLUMN_TASK_NAME,
 				SQLiteHelper.COLUMN_TASK_DESCRIPTION,
 				SQLiteHelper.COLUMN_TASK_COMPLETE,
 				SQLiteHelper.COLUMN_TASK_NEEDS_COMMENT,
 				SQLiteHelper.COLUMN_TASK_NEEDS_PHOTO,
 				SQLiteHelper.COLUMN_TASK_NEEDS_AUDIO, SQLiteHelper.COLUMN_USER,
				SQLiteHelper.COLUMN_EMAIL, SQLiteHelper.COLUMN_TIME };
 
 		String[] col_comments = { SQLiteHelper.COLUMN_ID,
 				SQLiteHelper.COLUMN_PARENT_ID, SQLiteHelper.COLUMN_GLOBALID,
 				SQLiteHelper.COLUMN_PARENT_GLOBALID,
				SQLiteHelper.COLUMN_COMMENT, SQLiteHelper.COLUMN_USER, 
				SQLiteHelper.COLUMN_TIME };
 
 		String[] col_photos = { SQLiteHelper.COLUMN_ID,
 				SQLiteHelper.COLUMN_PARENT_ID, SQLiteHelper.COLUMN_GLOBALID,
 				SQLiteHelper.COLUMN_PARENT_GLOBALID, SQLiteHelper.COLUMN_PHOTO,
				SQLiteHelper.COLUMN_USER, SQLiteHelper.COLUMN_TIME };
 
 		// Get Tasks
 		Cursor cursor = database.query(SQLiteHelper.TABLE_TASKS, col, null,
 				null, null, null, null);
 
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			// Load Task
 			Task task = cursorToTask(cursor);
 			Log.v(TAG, "Loading Task: " + task.getName());
 			TaskManager.getInstance().addTask(task);
 
 			// Get Comments for Task
 			Cursor cursor_comment = database.query(SQLiteHelper.TABLE_COMMENTS,
 					col_comments, SQLiteHelper.COLUMN_PARENT_ID + " = ?",
 					new String[] { String.valueOf(task.getLocalId()) }, null,
					null, SQLiteHelper.COLUMN_TIME +" ASC");
			
 			cursor_comment.moveToFirst();
 			while (!cursor_comment.isAfterLast()) {
 				Comment comment = cursorToComment(cursor_comment);
 				task.addComment(comment);
 				Log.v(TAG, "Loading Comment: " + comment.getContent());
 				cursor_comment.moveToNext();
 			}
 
 			cursor_comment.close();
			
 			// Get Photos for Task
 			Cursor cursor_photo = database.query(SQLiteHelper.TABLE_PHOTOS,
 					col_photos, SQLiteHelper.COLUMN_PARENT_ID + " = ?",
 					new String[] { String.valueOf(task.getLocalId()) }, null,
					null, SQLiteHelper.COLUMN_TIME +" ASC");
			
 			cursor_photo.moveToFirst();
 			while (!cursor_photo.isAfterLast()) {
 				MyPhoto photo = cursorToPhoto(cursor_photo);
 				task.addPhoto(photo);
 				Log.v(TAG, "Loading Photo");
 				cursor_photo.moveToNext();
 			}
 
 			cursor_photo.close();
 
 			cursor.moveToNext();
 		}
 		// Make sure to close the cursor
 		cursor.close();
 	}
 
 	private Task cursorToTask(Cursor cursor) {
 		Task task = new Task();
 
 		task.setLocalId(cursor.getLong(0));
 		task.setId(cursor.getLong(1));
 
 		task.setName(cursor.getString(2));
 		task.setDescription(cursor.getString(3));
 
 		task.setComplete((cursor.getLong(4) == 1));
 		task.setRequirements((cursor.getLong(5) == 1),
 				(cursor.getLong(6) == 1), (cursor.getLong(7) == 1));
 
 		task.setLocal(true);
		task.setPublic(task.getId() != 0);
 
 		User user = new User();
 		user.setUserName(cursor.getString(8));
 		user.setEmail(cursor.getString(9));
 
 		task.setUser(user);
 
 		return task;
 	}
 
 	private Comment cursorToComment(Cursor cursor) {
 		Comment comment = new Comment();
 
 		comment.setLocalId(cursor.getLong(0));
 		comment.setLocalParentId(cursor.getLong(1));
 
 		comment.setId(cursor.getLong(2));
 		comment.setParentId(cursor.getLong(3));
 
 		comment.setContent(cursor.getString(4));
 
 		User user = new User();
 		user.setUserName(cursor.getString(5));
 
 		comment.setUser(user);
 
 		return comment;
 	}
 	
 	private MyPhoto cursorToPhoto(Cursor cursor) {
 		MyPhoto photo = new MyPhoto();
 
 		photo.setLocalId(cursor.getLong(0));
 		photo.setLocalParentId(cursor.getLong(1));
 
 		photo.setId(cursor.getLong(2));
 		photo.setParentId(cursor.getLong(3));
 
 		photo.setImageFromString(cursor.getString(4));
 
 		User user = new User();
 		user.setUserName(cursor.getString(5));
 
 		photo.setUser(user);
 
 		return photo;
 	}
 }
