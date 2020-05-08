 package com.github.pps.repo;
 
 import com.github.pps.dto.Task;
 import org.joda.time.DateTime;
 import org.json.JSONException;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 import java.util.List;
 
 import static java.util.Arrays.asList;
 import static org.joda.time.DateTime.now;
 
 public class TaskRepository {
     private static final DateTime YESTERDAYA = DateTime.now().minusDays(1);
     private static final String MAIN_PERSISTENCE_UNIT = "mainPersistenceUnit";
     private static final String QUERY_PERSISTENCE_UNIT = "queryPersistenceUnit";
     private static TaskRepository taskRepository = new TaskRepository();
     private static final String TASK_STATUS_NEW = "new";
     private static final String TASK_STATUS_RUNNING = "running";
     private static final String TASK_STATUS_NOTHING = "nothing";
     public static final String TASK_STATUS_DONE = "done";
 
     public static TaskRepository getInstance() {
         return taskRepository;
     }
 
     public void createTask(String uids, String token, String currentUid) {
         EntityManager entityManager = getEntityManager(MAIN_PERSISTENCE_UNIT);
         Task task = new Task(currentUid, TASK_STATUS_NEW, now().getMillis(), uids, token);
         entityManager.persist(task);
         entityManagerClose(entityManager);
     }
 
     public List<Task> findTasksBy(String uid) throws JSONException {
         EntityManager entityManager = getEntityManager(QUERY_PERSISTENCE_UNIT);
         Query query = entityManager.createQuery(
                 "select t from " + Task.class.getName() + " t where t.uid = ? order by t.createdAt desc ")
                 .setParameter(1, uid);
         List<Task> tasks = query.getResultList();
         entityManagerClose(entityManager);
 
         return tasks;
     }
 
     public Task findOneNewTask() {
         EntityManager entityManager = getEntityManager(QUERY_PERSISTENCE_UNIT);
         Query query = entityManager.createQuery(
                 "select t from " + Task.class.getName() + " t where t.status = ? order by t.createdAt desc")
                 .setParameter(1, TASK_STATUS_NEW).setMaxResults(1);
        List<Task> tasks = query.getResultList();
         entityManagerClose(entityManager);

        if (tasks == null || tasks.isEmpty()) return null;
        return tasks.get(0);
     }
 
     public void updateTaskRunning(Long taskId) {
         updateTaskStatus(taskId, TASK_STATUS_RUNNING);
     }
 
     public void updateTaskNothing(Long taskId) {
         updateTaskStatus(taskId, TASK_STATUS_NOTHING);
     }
 
     public void updateTaskDone(String url, Long taskId) {
         EntityManager entityManager = getEntityManager(MAIN_PERSISTENCE_UNIT);
 
         System.out.println("taskid:" + taskId);
         Task task = entityManager.find(Task.class, taskId);
         System.out.println("task:" + task);
         task.setStatus(TASK_STATUS_DONE);
         task.setUrl(url);
 
         entityManagerClose(entityManager);
     }
 
     public void deleteTask(Task task) {
         EntityManager entityManager = getEntityManager(MAIN_PERSISTENCE_UNIT);
         entityManager.remove(entityManager.contains(task) ? task : entityManager.merge(task));
         entityManagerClose(entityManager);
     }
 
     public List<Task> queryFinishedTasks() {
         EntityManager entityManager = getEntityManager(QUERY_PERSISTENCE_UNIT);
         Query query = entityManager.createQuery(
                 "select t from " + Task.class.getName() +
                         " t where t.status in :tasks and t.createdAt < :yesterday")
                 .setParameter("tasks", asList(TASK_STATUS_DONE, TASK_STATUS_NOTHING))
                 .setParameter("yesterday", YESTERDAYA.getMillis());
         List<Task> tasks = query.getResultList();
         entityManagerClose(entityManager);
         return tasks;
     }
 
     private void updateTaskStatus(Long taskId, String status) {
         EntityManager entityManager = getEntityManager(MAIN_PERSISTENCE_UNIT);
 
         System.out.println("taskid:" + taskId);
         Task task = entityManager.find(Task.class, taskId);
         System.out.println("task:" + task);
         task.setStatus(status);
 
         entityManagerClose(entityManager);
     }
 
     private EntityManager getEntityManager(String persistenceUnit) {
         EntityManagerFactory factory = Persistence.createEntityManagerFactory(persistenceUnit);
         EntityManager entityManager = factory.createEntityManager();
         entityManager.getTransaction().begin();
         return entityManager;
     }
 
     private void entityManagerClose(EntityManager entityManager) {
         entityManager.getTransaction().commit();
         entityManager.close();
     }
 
 }
