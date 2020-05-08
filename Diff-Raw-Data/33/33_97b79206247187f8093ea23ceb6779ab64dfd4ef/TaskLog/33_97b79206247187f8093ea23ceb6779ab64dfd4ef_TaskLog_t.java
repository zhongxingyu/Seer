 package com.baoxue.action;
 
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import com.baoxue.common.ActionBase;
 import com.baoxue.db.TDoTaskLog;
import com.baoxue.db.TTask;
 
 public class TaskLog extends ActionBase {
 
 	List<TDoTaskLog> taskLogs;
 	String taskID;
	String taskName;

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
 
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

			if (taskName == null || "".equals(taskName)) {
				hql = "from TTask t where t.CId=:id";
				query = session.createQuery(hql);
				query.setString("id", taskID);
				List<TTask> tasks = query.list();
				if (tasks != null && tasks.size() > 0) {
					taskName = tasks.get(0).getCName();
				}
			}
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
