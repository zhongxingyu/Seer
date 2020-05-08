 package dbmigrate.executor;
 
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import dbmigrate.model.operation.ModifyColumnOperationDescriptor;
 import java.sql.Connection;
 
 
 public class ModifyColumnExecutor extends GeneralExecutor<ModifyColumnOperationDescriptor> {
 	public ModifyColumnExecutor(Connection connection) {
 		this.setConnection(connection);
 	}
 
 	public String createSql(ModifyColumnOperationDescriptor operation) {
 		StringBuffer buf = new StringBuffer();
 		buf.append("ALTER TABLE \"").append(operation.getTableName()).append("\" ").append("MODIFY ").append(operation.getColumn().getSqlDescription());
 		return buf.toString();
 	}
 	
 	public void execute(ModifyColumnOperationDescriptor operation) throws SQLException {
        Statement stmt = getConnection().createStatement();
        stmt.executeUpdate(createSql(operation));
 	}
 }
