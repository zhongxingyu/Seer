 package com.gushuley.utils.scheduler.dom;
 
 import java.sql.*;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 
 import com.gushuley.utils.Tools;
 import com.gushuley.utils.orm.*;
 import com.gushuley.utils.orm.sql.*;
 
 
 public class JobDoneMapper 
 extends AbstractSqlMapper2<JobDone, Integer, SchedulerContext>
 implements JobDone.Mapper 
 {
 	@Override
 	protected JobDone createInstance(Integer key, ResultSet rs) throws SQLException {
 		return new JobDone(key);
 	}
 
 	@Override
 	public Integer createKey(ResultSet rs) throws SQLException {
 		return rs.getInt(rs.getInt("scd_id"));
 	}
 
 	@Override
 	protected String getConnectionKey() {
 		return ((SchedulerContext)getContext()).getScheduleDbJdni();
 	}
 
 	@Override
 	protected GetQueryCallback<JobDone> getDeleteQueryCB() {
 		return new UnimplementedQueryCallback<JobDone>();
 	}
 
 	@Override
 	protected GetQueryCallback<JobDone> getInsertQueryCB() {
 		return new GetQueryCallback<JobDone>() {
 			public void executeStep(Connection cnn, JobDone obj) throws SQLException {
 			}
 
 			/* begin gu_schedules_pkg.job_done(p_schedule => :p_schedule,
                  p_job_id => :p_job_id, p_date => :p_date, p_id => :p_id); end; */
 			public String getSql() throws ORMException {
 				return "begin " + ctx.getDbScheme() + "gu_schedules_pkg.job_done(?, ?, ?, ?); end;";
 			}
 
 			public void setParams(PreparedStatement stm, JobDone obj) throws SQLException, ORMException {
 				stm.setString(1, ctx.getScheduler());
 				stm.setString(2, obj.getJobId());
 				Tools.setTimestamp(stm, 3, obj.getDate());
 				stm.setInt(4, obj.getKey());
 			}			
 		};
 	}
 
 	@Override
 	protected String getSelectAllSql() {
 		throw new ORMError("Unimplemented method");
 	}
 
 	@Override
 	protected void setSelectAllStatementParams(PreparedStatement stm)
 	throws ORMException {
 		throw new ORMError("Unimplemented method");
 	}
 	
 	@Override
 	protected String getSelectSql() {
 		throw new ORMError("Unimplemented method");
 	}
 
 	@Override
 	protected void setSelectStatementParams(PreparedStatement stm, Integer id) throws SQLException {
 		throw new ORMError("Unimplemented method");	
 	}
 	
 	@Override
 	protected GetQueryCallback<JobDone> getUpdateQueryCB() {
 		return new UnimplementedQueryCallback<JobDone>();
 	}
 
 	@Override
 	protected void loadInstance(JobDone obj, ResultSet rs) throws SQLException,
 			ORMException {
 	}
 
 	public Integer createKey() throws ORMException {
 		return getScalar(new GetScalarCallback<Integer>() {
 			@Override
 			public Integer getNull() {
 				return 1;
 			}
 
 			@Override
 			public Integer getValue(ResultSet rs) throws SQLException {
 				return rs.getInt("max") + 1;
 			}
 
 			@Override
 			public String getSql() throws ORMException {
				return "SELECT MAX(scd_id) max FROM " + ctx.getDbScheme() + "gu_schedules_done_v";
 			}
 
 			@Override
 			public void setParams(PreparedStatement stm) throws SQLException, ORMException { }
 		});
 	}
 
 	@Override
 	public Collection<JobDone> getJobDoneDayAndAfter(Date date, String jobId) throws ORMException {
 		final Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		cal.set(Calendar.MILLISECOND, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		return getCollection(new ExecCallback<PreparedStatement>() {
 			@Override
 			public String getSql() throws ORMException {
 				return "SELECT scd_id, scd_sch, scd_job_id, scd_done_date FROM " + ctx.getDbScheme() + "gu_schedules_done_v WHERE scd_done_date >= ?";
 			}
 
 			@Override
 			public void setParams(PreparedStatement stm) throws SQLException, ORMException {
 				Tools.setTimestamp(stm, 1, cal.getTime());
 			}
 		});
 	}
 
 }
