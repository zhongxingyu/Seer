 import java.sql.SQLException;
 
 /**
  * TODO Put here a description of what this class does.
  * 
  * @author schepedw. Created Apr 16, 2013.
  */
 public class DeleteCommand extends Command {
 
 	private String table;
 	private String delValue;
 	private String columnName;
 	private DatabaseConnector conn;
 
 	public DeleteCommand(String table, String delValue) {
 		this.table = table.substring(0, 1).toUpperCase() + table.substring(1);// Automatically
 																				// capitalize
 																				// whatever's
 																				// passed
 																				// in
 		this.delValue = delValue;
 
 		if (this.table.equals("Divergence"))
 			this.columnName = "DivName";
 		else if (this.table.equals("Annotation"))
 			this.columnName = "AnnoName";
 		else if (this.table.equals("Vcf"))
 			this.columnName = "VcfName";
 		try {
 			this.conn = new DatabaseConnector();
 		} catch (Exception exception) {
 			exception.printStackTrace();
 		}
 	}
 
 	@Override
 	public String execute() {
 		String sql = "DELETE FROM `" + this.table + "` where `"
 				+ this.columnName + "`='" + this.delValue+"';" ;
 		try {
 			this.conn.executeUpdate(sql);
				System.out.printf("Succesfully deleted %s from %s", this.delValue,
 						this.table);
 
				return "Success";
 			
 		} catch (Exception exception) {
 			System.out.println("Exception: sql:= "+sql);
 			exception.printStackTrace();
 			return "Fail!";
 		} 
 		
 	}
 
 	@Override
 	public void pipeOutput() {
 		// TODO Auto-generated method stub.
 
 	}
 
 }
