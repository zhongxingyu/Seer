 package com.ebt.app.ajax.datasource;
 
 import com.ebt.app.ajax.task.Task;
 import org.springframework.dao.DataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: svetoslav
  * Date: 1/24/13
  * Time: 2:42 PM
  * To change this template use File | Settings | File Templates.
  */
 public class TaskDAOImpl implements TaskDAO {
 
     //Constants
     private static final String SQL_INSERT_TASK = "insert into timesheetRecords (task, started, completed, mon, tue, wed, thu, fri, sat, sun) " +
             "values (?,?,?,?,?,?,?,?,?,?)";
    private static final String SQL_SELECT_ALL = "select * from timesheetRecords";
     private static final String SQL_FIND_BY_ID = "select * from timesheetRecords where id=?";
     private static final String SQL_DELETE_BY_ID = "delete from timesheetRecords where id=?";
     private static final String SQL_UPDATE_TASK_BY_ID = "update timesheetRecords set task=?, started=?, completed=?, mon=?, tue=?, wed=?, thu=?, " +
             "fri=?, sat=?, sun=? where id=?";
     private JdbcTemplate jdbcTemplate;
 
 
     public JdbcTemplate getJdbcTemplate() {
         return jdbcTemplate;
     }
 
     public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
         this.jdbcTemplate = jdbcTemplate;
     }
 
 
 
     public void insert(Task task) {
 
         try {
             Object[] args = new Object[]{task.getTaskName(), task.getStarted(), task.getCompleted(),
                             task.getMondayHours(), task.getTuesdayHours(), task.getWednesdayHours(),
                             task.getThursdayHours(), task.getFridayHours(), task.getSaturdayHours(),
                             task.getSundayHours()};
 
             this.getJdbcTemplate().update(SQL_INSERT_TASK, args);
 
             System.out.println("The Task was successfully Created... ");
         } catch (DataAccessException e) {
             e.printStackTrace();
         }
 
     }
 
     public void update(int pk, Task task) {
         Object[] args = new Object[]{task.getTaskName(), task.getStarted(), task.getCompleted(),
                 task.getMondayHours(), task.getTuesdayHours(), task.getWednesdayHours(),
                 task.getThursdayHours(), task.getFridayHours(), task.getSaturdayHours(),
                 task.getSundayHours(),pk};
         try {
             this.getJdbcTemplate().update(SQL_UPDATE_TASK_BY_ID, args);
         } catch (DataAccessException e) {
             e.printStackTrace();
         }
     }
 
     public void delete(int pk) {
         Object[] args = new Object[]{pk};
         try {
             this.getJdbcTemplate().update(SQL_DELETE_BY_ID, args);
             System.out.println("The task with id "+pk+" was deleted.");
         } catch (DataAccessException e) {
             e.printStackTrace();
         }
     }
 
     public ArrayList<Task> tasks() {
         ArrayList<Task> tasksList = null;
         try {
             tasksList = (ArrayList)this.getJdbcTemplate().queryForList(SQL_SELECT_ALL, Task.class);
         } catch (DataAccessException e) {
             e.printStackTrace();
         }
         return tasksList;
     }
 
     public Task findById(int pk) {
         Object[] args = new Object[]{pk};
         Task task = null;
         try {
             task = this.getJdbcTemplate().queryForObject(SQL_FIND_BY_ID, args, Task.class);
         }
         catch (DataAccessException e) {
             e.printStackTrace();
         }
         return task;
     }
 }
