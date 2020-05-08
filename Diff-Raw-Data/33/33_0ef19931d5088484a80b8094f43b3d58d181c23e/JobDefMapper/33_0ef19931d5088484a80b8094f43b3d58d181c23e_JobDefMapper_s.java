 package com.gushuley.utils.scheduler.dom;
 
 import java.sql.*;
 
 import com.gushuley.utils.orm.ORMError;
 import com.gushuley.utils.orm.ORMException;
 import com.gushuley.utils.orm.sql.*;
 
 
 public class JobDefMapper 
 extends AbstractSqlMapper2<JobDef, String, SchedulerContext> 
 {
 	@Override
 	protected JobDef createInstance(String key, ResultSet rs)
 			throws SQLException {
 		return new JobDef(key);
 	}
 
 	@Override
 	public String createKey(ResultSet rs) throws SQLException {
 		return rs.getString("sch_id");
 	}
 
 	@Override
 	protected String getConnectionKey() {
 		return ((SchedulerContext)getContext()).getScheduleDbJdni();
 	}
 
 	@Override
 	protected GetQueryCallback<JobDef> getDeleteQueryCB() {
 		return new UnimplementedQueryCallback<JobDef>();
 	}
 
 	@Override
 	protected GetQueryCallback<JobDef> getInsertQueryCB() {
 		return new UnimplementedQueryCallback<JobDef>();
 	}
 
 	@Override
 	protected String getSelectAllSql() {
		return "SELECT sch_id, sch_sch, sch_hour, sch_min, SCH_JOB_CLASS, SCH_DAY_MASK, SCH_props, sch_type " + ctx.getDbScheme() + "FROM gu_schedules_v WHERE sch_sch = ? ";
 	}
 
 	@Override
 	protected void setSelectAllStatementParams(PreparedStatement stm)
 			throws ORMException, SQLException {
 		stm.setString(1, ctx.getScheduler());
 	}
 
 	@Override
 	protected String getSelectSql() {
		return "SELECT sch_id, sch_sch, sch_hour, sch_min, SCH_JOB_CLASS, SCH_DAY_MASK, SCH_props, sch_type " + ctx.getDbScheme() + "FROM gu_schedules_v WHERE sch_id = ?";
 	}
 
 	@Override
 	protected void setSelectStatementParams(PreparedStatement stm, String id)
 			throws SQLException {
 		stm.setString(1, id);
 	}
 
 	@Override
 	protected GetQueryCallback<JobDef> getUpdateQueryCB() {
 		return new UnimplementedQueryCallback<JobDef>();
 	}
 
 	@Override
 	protected void loadInstance(JobDef obj, ResultSet rs)
 			throws SQLException, ORMException {
 		obj.setStartHour(rs.getInt("sch_hour"));
 		obj.setStartMinute(rs.getInt("sch_min"));
 		obj.setClassName(rs.getString("SCH_JOB_CLASS"));
 		obj.setProperties(rs.getString("SCH_props"));
 		obj.setDayMask(rs.getInt("SCH_DAY_MASK"));
 		obj.setType(JobDef.Type.parse(rs.getByte("sch_type")));
 	}
 	
 	public String createKey() throws ORMException {
 		throw new ORMError("Unimplemented method");
 	}
 
 }
