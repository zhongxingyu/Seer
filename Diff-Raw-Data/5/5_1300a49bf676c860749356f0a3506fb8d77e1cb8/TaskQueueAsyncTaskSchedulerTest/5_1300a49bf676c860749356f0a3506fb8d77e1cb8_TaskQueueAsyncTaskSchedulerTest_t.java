 package com.clouway.asynctaskscheduler.gae;
 
 import com.clouway.asynctaskscheduler.common.ActionEvent;
 import com.clouway.asynctaskscheduler.common.CustomTaskQueueAsyncTask;
 import com.clouway.asynctaskscheduler.common.DefaultActionEvent;
 import com.clouway.asynctaskscheduler.common.DefaultTaskQueueAsyncTask;
 import com.clouway.asynctaskscheduler.common.TaskQueueParamParser;
import com.clouway.asynctaskscheduler.spi.AsyncTask;
import com.clouway.asynctaskscheduler.spi.AsyncTaskOptions;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.dev.LocalTaskQueue;
 import com.google.appengine.api.taskqueue.dev.QueueStateInfo;
 import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
 import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
 import com.google.gson.Gson;
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Map;
 
 import static com.clouway.asynctaskscheduler.spi.AsyncTaskOptions.task;
 import static junit.framework.Assert.assertEquals;
 
 /**
  * @author Mihail Lesikov (mlesikov@gmail.com)
  */
 public class TaskQueueAsyncTaskSchedulerTest {
   private LocalServiceTestHelper helper;
 
   @Inject
   private TaskQueueAsyncTaskScheduler taskScheduler;
 
   @Inject
   private Gson gson;
 
   @Before
   public void setUp() {
     LocalTaskQueueTestConfig localTaskQueueTestConfig = new LocalTaskQueueTestConfig();
     localTaskQueueTestConfig.setQueueXmlPath("src/test/java/queue.xml");
     helper = new LocalServiceTestHelper(localTaskQueueTestConfig);
 
     helper.setUp();
     Injector injector = Guice.createInjector(new BackgroundTasksModule());
     injector.injectMembers(this);
   }
 
   @After
   public void tearDown() {
     helper.tearDown();
   }
 
   @Test
   public void shouldAddTaskToTheDefaultTaskQueue() throws Exception {
     taskScheduler.add(AsyncTaskOptions.task(DefaultTaskQueueAsyncTask.class)).now();
     // give the task time to execute if tasks are actually enabled (which they
     // aren't, but that's part of the test)
     Thread.sleep(1000);
 
     QueueStateInfo qsi = getQueueStateInfo(QueueFactory.getDefaultQueue().getQueueName());
     assertEquals(1, qsi.getTaskInfo().size());
 
     assertTaskQueueName(qsi.getTaskInfo().get(0).getBody(), DefaultTaskQueueAsyncTask.class);
   }
 
   @Test
   public void shouldAddTaskToTheDefaultTaskQueueWithTheGivenParams() throws Exception {
     String paramName = "paramName";
     String paramValue = "paramValue";
 
     taskScheduler.add(AsyncTaskOptions.task(DefaultTaskQueueAsyncTask.class)
             .param(paramName, paramValue))
             .now();
     // give the task time to execute if tasks are actually enabled (which they
     // aren't, but that's part of the test)
     Thread.sleep(1000);
 
     QueueStateInfo qsi = getQueueStateInfo(QueueFactory.getDefaultQueue().getQueueName());
 
     assertParams(qsi.getTaskInfo().get(0).getBody(), paramName, paramValue);
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void shouldNotAddTaskTaskQueueWhenTaskOptionsForEventIsProvidedAndParamsAreAdded() throws Exception {
     String paramName = "paramName";
     String paramValue = "paramValue";
 
     taskScheduler.add(AsyncTaskOptions.event(new ActionEvent())
             .param(paramName, paramValue))
             .now();
   }
 
   @Test
   public void shouldAddTaskToDefaultTaskQueueWhenTaskOptionsForEventIsProvided() throws Exception {
     ActionEvent event = new ActionEvent("test message");
     taskScheduler.add(AsyncTaskOptions.event(event))
             .now();
 
     QueueStateInfo qsi = getQueueStateInfo(QueueFactory.getDefaultQueue().getQueueName());
     assertParams(qsi.getTaskInfo().get(0).getBody(), TaskQueueAsyncTaskScheduler.EVENT, event.getClass().getName());
     assertParams(qsi.getTaskInfo().get(0).getBody(), TaskQueueAsyncTaskScheduler.EVENT_AS_JSON, gson.toJson(event));
   }
   @Test
   public void shouldAddTaskToCustomTaskQueueWhenTaskOptionsForEventIsProvided() throws Exception {
     DefaultActionEvent event = new DefaultActionEvent("test message");
     taskScheduler.add(AsyncTaskOptions.event(event))
             .now();
 
     QueueStateInfo qsi = getQueueStateInfo(DefaultActionEvent.CUSTOM_TASK_QUEUE_NAME);
     assertParams(qsi.getTaskInfo().get(0).getBody(), TaskQueueAsyncTaskScheduler.EVENT, event.getClass().getName());
     assertParams(qsi.getTaskInfo().get(0).getBody(), TaskQueueAsyncTaskScheduler.EVENT_AS_JSON, gson.toJson(event));
   }
 
   @Test
   public void shouldAddTaskInToDifferentTaskQueue() throws Exception {
     taskScheduler.add(task(DefaultTaskQueueAsyncTask.class))
             .add(task(CustomTaskQueueAsyncTask.class))
             .now();
 
     QueueStateInfo defaultQueueStateInfo = getQueueStateInfo(QueueFactory.getDefaultQueue().getQueueName());
     assertEquals(1, defaultQueueStateInfo.getTaskInfo().size());
     assertTaskQueueName(defaultQueueStateInfo.getTaskInfo().get(0).getBody(), DefaultTaskQueueAsyncTask.class);
 
     QueueStateInfo customQueueStateInfo = getQueueStateInfo(CustomTaskQueueAsyncTask.CUSTOM_TASK_QUEUE_NAME);
     assertEquals(1, customQueueStateInfo.getTaskInfo().size());
     assertTaskQueueName(customQueueStateInfo.getTaskInfo().get(0).getBody(), CustomTaskQueueAsyncTask.class);
 
   }
 
 
   private void assertParams(String taskQueueBody, String paramName, String paramValue) throws UnsupportedEncodingException {
     Map<String, String> params = TaskQueueParamParser.parse(taskQueueBody);
     assertEquals(params.get(paramName), paramValue);
   }
 
   private QueueStateInfo getQueueStateInfo(String queueName) {
     LocalTaskQueue ltq = LocalTaskQueueTestConfig.getLocalTaskQueue();
     return ltq.getQueueStateInfo().get(queueName);
   }
 
   private void assertTaskQueueName(String taskQueueBody, Class<? extends AsyncTask> asyncTaskClass) throws UnsupportedEncodingException {
     Map<String, String> params = TaskQueueParamParser.parse(taskQueueBody);
     assertEquals(params.get(TaskQueueAsyncTaskScheduler.TASK_QUEUE), asyncTaskClass.getName());
   }
 }
