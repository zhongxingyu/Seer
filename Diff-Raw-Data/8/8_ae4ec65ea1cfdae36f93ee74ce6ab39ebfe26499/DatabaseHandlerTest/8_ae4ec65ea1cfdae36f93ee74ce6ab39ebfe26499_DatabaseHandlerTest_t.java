 package de.htwg.android.taskmanager.backend.database;
 
 import de.htwg.android.taskmanager.backend.entity.LocalTask;
 import de.htwg.android.taskmanager.backend.entity.LocalTaskList;
 import android.test.AndroidTestCase;
 import android.test.RenamingDelegatingContext;
 
 public class DatabaseHandlerTest extends AndroidTestCase {
 
 	private DatabaseHandler db;
 
 	public void setUp() {
 		RenamingDelegatingContext context = new RenamingDelegatingContext(
 				getContext(), "test_");
 		db = new DatabaseHandler(context);
 	}
 	
 	
 	public void testAddAndGetLocalTaskList(){
 		LocalTaskList localTaskList = new LocalTaskList();
 		localTaskList.setTitle("TestTaskList");
 		db.addTaskList(localTaskList);
 		assertNotNull(db.getTaskListByInternalId(localTaskList.getInternalId()));
 	}
 	
 	public void testAddAndGetLocalTask(){
 		LocalTaskList localTaskList = new LocalTaskList();
 		localTaskList.setTitle("TestTaskList");
 		LocalTask localTask = new LocalTask();
 		localTask.setTitle("TestTask");
 		db.addTaskList(localTaskList);
 		db.addTask(localTaskList, localTask);
 		assertNotNull(db.getTaskByInternalId(localTask.getInternalId()));
 	}
 	
 	public void testGetNonExistingLocalTaskList(){
 		LocalTaskList localTaskList = new LocalTaskList();
 		localTaskList.setTitle("TestTaskList");
		assertNull(db.getTaskListByInternalId(localTaskList.getInternalId()));
 	}
 	
 	public void testGetNonExistingLocalTask(){
 		LocalTaskList localTaskList = new LocalTaskList();
 		localTaskList.setTitle("TestTaskList");
 		LocalTask localTask = new LocalTask();
 		localTask.setTitle("TestTask");
		assertNull(db.getTaskByInternalId(localTask.getInternalId()));
 	}
 	
 	public void testDeleteAllTasksForTaskList(){
 		LocalTaskList localTaskList = new LocalTaskList();
 		localTaskList.setTitle("TestTaskList");
 		LocalTask localTask1 = new LocalTask();
 		localTask1.setTitle("TestTask1");
 		LocalTask localTask2 = new LocalTask();
 		localTask2.setTitle("TestTask2");
 		
 		db.addTaskList(localTaskList);
 		db.addTask(localTaskList, localTask1);
 		db.addTask(localTaskList, localTask2);
 		
 		
 		assertEquals(2, db.getTaskListByInternalId(localTaskList.getInternalId()).getTaskList().size());
 		
 		db.deleteAllTasksForTaskList(localTaskList.getInternalId());
 		
 		assertEquals(0, db.getTaskListByInternalId(localTaskList.getInternalId()).getTaskList().size());
 		
 	
 	}
 	
 	public void testNotEmptyDeleteAllTasksForTaskList(){
 		LocalTaskList localTaskList = new LocalTaskList();
 		localTaskList.setTitle("TestTaskList");
 		LocalTask localTask1 = new LocalTask();
 		localTask1.setTitle("TestTask1");
 		LocalTask localTask2 = new LocalTask();
 		localTask2.setTitle("TestTask2");
 		
 		db.addTaskList(localTaskList);
 		db.addTask(localTaskList, localTask1);
 		db.addTask(localTaskList, localTask2);
 		
 		
 		assertEquals(2, db.getTaskListByInternalId(localTaskList.getInternalId()).getTaskList().size());
 		
 		db.deleteAllTasksForTaskList(localTaskList.getInternalId());
 		
		assertEquals(0, db.getTaskListByInternalId(localTaskList.getInternalId()).getTaskList().size());
 		
 	
 	}
 	
 	public void testDeleteLocalTaskList(){
 		LocalTaskList localTaskList = new LocalTaskList();
 		localTaskList.setTitle("TestTaskList");
 		db.addTaskList(localTaskList);
 		assertNotNull(db.getTaskListByInternalId(localTaskList.getInternalId()));
 		db.deleteTaskList(localTaskList.getInternalId());
 		assertNull(db.getTaskListByInternalId(localTaskList.getInternalId()));
 	}
 	
 	
 	
 	public void tearDown() throws Exception{
 		db.close();
 		super.tearDown();
 	}
 
 }
