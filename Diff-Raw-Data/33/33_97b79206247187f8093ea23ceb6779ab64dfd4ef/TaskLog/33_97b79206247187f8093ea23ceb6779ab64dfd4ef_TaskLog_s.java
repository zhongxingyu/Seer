 package com.baoxue.action;
 
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import com.baoxue.common.ActionBase;
 import com.baoxue.db.TDoTaskLog;
 
 public class TaskLog extends ActionBase {
 
 	List<TDoTaskLog> taskLogs;
 	String taskID;
 
 	public List<TDoTaskLog> getTaskLogs() {
 		return taskLogs;
 	}
 
 	public void setTaskLogs(List<TDoTaskLog> taskLogs) {
 		this.taskLogs = taskLogs;
 	}
 
 	public String getTaskID() {
 		return taskID;
 	}
 
 	public void setTaskID(String taskID) {
 		this.taskID = taskID;
 	}
 
 	@Override
 	public String execute() throws Exception {
 		query();
 		return SUCCESS;
 	}
 
 	private void query() {
 		Session session = getDBSession();
 		try {
 			String hql = "from TDoTaskLog l where l.CTaskId=:taskId";
 			Query query = session.createQuery(hql);
 			query.setString("taskId", taskID);
 			taskLogs = query.list();
 		} finally {
 			session.close();
 		}
 	}
 
 	public String clear() {
 		Session session = getDBSession();
 		Transaction tx = session.beginTransaction();
 		try {
 			String sql = "delete from T_DO_TASK_LOG where C_TASK_ID=:taskId";
 			SQLQuery query = session.createSQLQuery(sql);
 			query.setString("taskId", taskID);
 			query.executeUpdate();
 			tx.commit();
 		} catch (Exception ex) {
 			tx.rollback();
 		} finally {
 			session.close();
 		}
 
 		query();
 		return SUCCESS;
 	}
 
 }
