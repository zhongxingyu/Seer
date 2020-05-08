 package com.github.marschall.punch;
 
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.datasource.DataSourceTransactionManager;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.TransactionDefinition;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.DefaultTransactionDefinition;
 
 public class DatabaseTest {
 
   private JdbcTemplate jdbcTemplate;
   private PunchPool pool;
   private EmbeddedDatabase db;
 
   @Before
   public void before() {
     this.db = new EmbeddedDatabaseBuilder()
     .setName("punch-db")
     .setType(EmbeddedDatabaseType.H2)
     .addScript("punch-db.sql")
     .build();
 
     this.jdbcTemplate = new JdbcTemplate(this.db);
     PlatformTransactionManager transactionManager = new DataSourceTransactionManager(this.db);
     this.pool = new PunchPool(new PersistingTaskStateListener(this.jdbcTemplate, transactionManager));
   }
 
   @After
   public void after() throws InterruptedException {
    this.pool.shutdown();
    assertTrue(this.pool.awaitTermination(1, TimeUnit.SECONDS));
     this.db.shutdown();
   }
 
   @Test
   public void testSuccess() {
     this.pool.invoke(JobTrees.buildFileOutRoot());
 
     int totalTaskCount = this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM t_task_state");
     int finishedCount = this.jdbcTemplate.queryForInt("SELECT COUNT(*) FROM t_task_state WHERE task_state = 'FINISHED'");
 
     assertEquals(17, totalTaskCount);
     assertEquals(17, finishedCount);
   }
 
   static class DatabaseRecoveryService implements RecoveryService {
 
     private static final String SELECT_TASK_PATH = "SELECT task_path FROM t_task_state WHERE task_state = 'FINISHED'";
 
     private final JdbcTemplate jdbcTemplate;
 
     private final AtomicReference<Set<TaskPath>> finishedTasks;
 
     public DatabaseRecoveryService(JdbcTemplate jdbcTemplate) {
       this.jdbcTemplate = jdbcTemplate;
       this.finishedTasks = new AtomicReference<>();
     }
 
     private Set<TaskPath> getFinishedTasks() {
       Set<TaskPath> set = this.finishedTasks.get();
       if (set != null) {
         return set;
       }
       List<TaskPath> list = this.jdbcTemplate.query(SELECT_TASK_PATH, TaskPathRowMapper.INSTANCE);
       set = new HashSet<>(list);
       boolean success = this.finishedTasks.compareAndSet(null, set);
       if (success) {
         return set;
       } else {
         return this.finishedTasks.get();
       }
     }
 
     @Override
     public boolean isFinished(TaskPath path) {
       return this.getFinishedTasks().contains(path);
     }
 
   }
 
   static class PersistingTaskStateListener implements TaskStateListener {
 
     private static final String INSERT_TASK_SQL = "INSERT INTO t_task_state(task_path, task_state) VALUES (?, ?)";
     private static final String UPDATE_TASK_SQL = "UPDATE t_task_state SET task_state = ? WHERE task_path = ?";
 
     private final JdbcTemplate jdbcTemplate;
     private final PlatformTransactionManager transactionManager;
     private final ConcurrentMap<TaskPath, TransactionStatus> transactionStates;
     private final TransactionDefinition transactionDefinition;
 
     PersistingTaskStateListener(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
       this.jdbcTemplate = jdbcTemplate;
       this.transactionManager = transactionManager;
       this.transactionStates = new ConcurrentHashMap<>();
       this.transactionDefinition = new DefaultTransactionDefinition();
     }
 
     @Override
     public void taskStarted(TaskPath path) {
       TransactionStatus status = this.transactionManager.getTransaction(this.transactionDefinition);
       this.transactionStates.put(path, status);
       this.jdbcTemplate.update(INSERT_TASK_SQL, path.toString(), "RUNNING");
     }
 
     @Override
     public void taskFinished(TaskPath path) {
       this.jdbcTemplate.update(UPDATE_TASK_SQL, "FINISHED", path.toString());
       TransactionStatus status = this.transactionStates.remove(path);
       this.transactionManager.commit(status);
     }
 
     @Override
     public void taskFailed(TaskPath path) {
       TransactionStatus status = this.transactionStates.remove(path);
       this.transactionManager.rollback(status);
     }
   }
 
   static class TaskState {
     TaskPath taskPath;
     String taskStatus;
 
     TaskState(String taskPath, String taskStatus) {
       this.taskPath = TaskPath.fromString(taskPath);
       this.taskStatus = taskStatus;
     }
   }
 
   static enum TaskPathRowMapper implements RowMapper<TaskPath> {
 
     INSTANCE;
 
     @Override
     public TaskPath mapRow(ResultSet rs, int rowNum) throws SQLException {
       return TaskPath.fromString(rs.getString("TASK_PATH"));
     }
 
   }
 
   static enum TaskStateRowMapper implements RowMapper<TaskState> {
 
     INSTANCE;
 
     @Override
     public TaskState mapRow(ResultSet rs, int rowNum) throws SQLException {
       return new TaskState(rs.getString("TASK_PATH"), rs.getString("TASK_STATE"));
     }
 
   }
 }
