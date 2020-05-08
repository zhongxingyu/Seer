 package org.sakaiproject.search.queueing;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.sakaiproject.authz.api.SecurityAdvisor;
 import org.sakaiproject.authz.api.SecurityService;
 import org.sakaiproject.search.indexing.SecurityAdvisorMatcher;
 import org.sakaiproject.search.indexing.Task;
 import org.sakaiproject.search.indexing.TaskHandler;
 import org.sakaiproject.search.indexing.exception.NestedTaskHandlingException;
 import org.sakaiproject.search.indexing.exception.TaskHandlingException;
 import org.sakaiproject.search.indexing.exception.TemporaryTaskHandlingException;
 import org.sakaiproject.thread_local.api.ThreadLocalManager;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 
 /**
  * @author Colin Hebert
  */
 public class WaitingTaskRunnerTest {
     private WaitingTaskRunner waitingTaskRunner;
     @Mock
     private SecurityService mockSecurityService;
     @Mock
     private TaskHandler mockTaskHandler;
     @Mock
     private IndexQueueing mockIndexQueueing;
     @Mock
     private ThreadLocalManager threadLocalManager;
 
     @Before
     public void setUp() throws Exception {
         MockitoAnnotations.initMocks(this);
         waitingTaskRunner = new WaitingTaskRunner() {
         };
         waitingTaskRunner.setIndexQueueing(mockIndexQueueing);
         waitingTaskRunner.setTaskHandler(mockTaskHandler);
         waitingTaskRunner.setSecurityService(mockSecurityService);
         waitingTaskRunner.setThreadLocalManager(threadLocalManager);
     }
 
     /**
      * Attempts to throw one {@link TemporaryTaskHandlingException}.
      * <p>
      * Checks that it adds a new {@link Task} to the queueing system.
      * </p>
      */
     @Test
     public void testTemporaryExceptionQueueNewTask() {
         Task task = mock(Task.class);
         doThrow(new TemporaryTaskHandlingException(task)).when(mockTaskHandler).executeTask(any(Task.class));
 
         waitingTaskRunner.runTask(mock(Task.class));
 
         verify(mockIndexQueueing).addTaskToQueue(task);
     }
 
     /**
      * Attempts to throw one {@link TaskHandlingException}.
      * <p>
      * Checks that it doesn't end up with a new task being added to the queue.
      * </p>
      */
     @Test
     public void testExceptionDontQueueNewTask() {
         doThrow(new TaskHandlingException()).when(mockTaskHandler).executeTask(any(Task.class));
 
         waitingTaskRunner.runTask(mock(Task.class));
 
         verify(mockIndexQueueing, never()).addTaskToQueue(any(Task.class));
     }
 
     /**
      * Attempts to throw multiple {@link TemporaryTaskHandlingException}.
      * <p>
      * Checks that every temporary exception creates a new {@link Task} that is added back to the queue.
      * </p>
      */
     @Test
     public void testNestedExceptionWithTempExceptionQueueNewTask() {
         int numberOfTemporaryExceptions = 12;
         int numberOfExceptions = 9;
         doThrow(createNestedException(numberOfTemporaryExceptions, numberOfExceptions)).when(mockTaskHandler).executeTask(any(Task.class));
 
         waitingTaskRunner.runTask(mock(Task.class));
 
         verify(mockIndexQueueing, times(numberOfTemporaryExceptions)).addTaskToQueue(any(Task.class));
     }
 
     /**
      * Attempts to throw multiple {@link TemporaryTaskHandlingException}.
      * <p>
      * Checks that the WaitingTaskRunner doesn't end up in a deadlock when multiple TemporaryTaskHandlingException
      * are thrown at once.
      * </p>
      *
      * @throws Exception should not happen.
      */
     @Test
     public void testMultipleTemporaryExceptionUnlockOtherThreads() throws Exception {
         int numberOfTemporaryExceptions = 12;
         Task failingTask = mock(Task.class);
         doThrow(createNestedException(numberOfTemporaryExceptions, 0)).when(mockTaskHandler).executeTask(failingTask);
 
         assertTaskExecutedWithin(failingTask, 1000);
         assertTaskExecutedWithin(mock(Task.class), 1000);
     }
 
     /**
      * Attempts to throw a {@link TemporaryTaskHandlingException} to stop the task handling threads.
      * <p>
      * Checks that a temporary exceptions results in the system stopping temporarily every thread.
      * </p>
      *
      * @throws Exception should not happen.
      */
     @Test
     public void testTemporaryExceptionStopsOtherThreads() throws Exception {
         int waitingTime = 4000;
         Task failingTask = mock(Task.class);
         doThrow(new TemporaryTaskHandlingException(mock(Task.class))).when(mockTaskHandler).executeTask(failingTask);
 
         Thread failingTaskThread;
         do {
             failingTaskThread = createSeparateTaskThread(failingTask);
             failingTaskThread.start();
             failingTaskThread.join(waitingTime);
         } while (!failingTaskThread.isAlive());
 
        assertTaskNotExecutedWithin(mock(Task.class), waitingTime/4);
        assertTaskExecutedWithin(mock(Task.class), 2*waitingTime);
     }
 
     /**
      * Attempts to execute one task.
      * <p>
      * Checks that an advisor is set up to give access to every resources.
      * </p>
      */
     @Test
     public void testSecurityAdvisorSet() {
         Task task = mock(Task.class);
         // Advisor matcher that checks an advisor will allow anything.
         SecurityAdvisorMatcher securityAdvisorMatcher = new SecurityAdvisorMatcher();
         securityAdvisorMatcher.addUnlockCheckRandom(SecurityAdvisor.SecurityAdvice.ALLOWED);
 
         waitingTaskRunner.runTask(task);
 
         ArgumentCaptor<SecurityAdvisor> argument = ArgumentCaptor.forClass(SecurityAdvisor.class);
         verify(mockSecurityService).pushAdvisor(argument.capture());
         assertThat(argument.getValue(), securityAdvisorMatcher);
         verify(mockSecurityService).popAdvisor(argument.getValue());
     }
 
     private NestedTaskHandlingException createNestedException(int temporaryExceptionsCount, int exceptionsCount) {
         NestedTaskHandlingException nestedTaskHandlingException = new NestedTaskHandlingException();
         for (int i = 0; i < temporaryExceptionsCount; i++) {
             nestedTaskHandlingException.addTaskHandlingException(new TemporaryTaskHandlingException(mock(Task.class)));
         }
         for (int i = 0; i < exceptionsCount; i++) {
             nestedTaskHandlingException.addTaskHandlingException(new TaskHandlingException());
         }
 
         return nestedTaskHandlingException;
     }
 
     private void assertTaskExecutedWithin(Task task, long millis) throws InterruptedException {
         Thread separateTaskThread = createSeparateTaskThread(task);
         separateTaskThread.start();
         separateTaskThread.join(millis);
         assertFalse(separateTaskThread.isAlive());
     }
 
     private void assertTaskNotExecutedWithin(Task task, long millis) throws InterruptedException {
         Thread separateTaskThread = createSeparateTaskThread(task);
         separateTaskThread.start();
         separateTaskThread.join(millis);
         assertTrue(separateTaskThread.isAlive());
     }
 
     private Thread createSeparateTaskThread(final Task task) {
         return new Thread() {
             @Override
             public void run() {
                 waitingTaskRunner.runTask(task);
             }
         };
     }
 }
