 package testspring.testspring;
 
import java.util.Date;
 import java.util.List;
 import javax.sql.DataSource;
 
 import org.springframework.jdbc.core.JdbcTemplate;
 
 public class RuntimeRecordJDBCTemplate implements RuntimeRecordDAO {
 	private DataSource dataSource;
 	private JdbcTemplate jdbcTemplateObject;
 
 	@Override
 	public void setDataSource(DataSource ds) {
 		// TODO Auto-generated method stub
 		this.dataSource = ds;
 		this.jdbcTemplateObject = new JdbcTemplate(ds);
 	}
 
 	@Override
 	public void store(RuntimeRecord record) {
 		// TODO Auto-generated method stub
 		String SQL = "insert into runtime (startTime, endTime, className, methodName) values (?, ?, ?, ?)";
 
 		System.out.println(jdbcTemplateObject.update(SQL, record.getStartTime(),
 				record.getEndTime(), record.getClassName(),
 				record.getMethodName()));
//		System.out.println(jdbcTemplateObject.update(SQL,"2009-09-22 08:08:11","2009-09-22 08:08:20", record.getClassName(),record.getMethodName()));
 		System.out.println("Stored a Record");
 	}
 
 	@Override
 	public RuntimeRecord getRecord(Integer id) {
 		// TODO Auto-generated method stub
 		String SQL = "select * from runtime where id = ?";
 		RuntimeRecord record = jdbcTemplateObject.queryForObject(SQL,
 				new Object[] { id }, new RuntimeRecordMapper());
 		return record;
 	}
 
 	@Override
 	public List<RuntimeRecord> listRuntimeRecords() {
 		// TODO Auto-generated method stub
 		String SQL = "select * from RuntimeRecord";
 	      List <RuntimeRecord> records = jdbcTemplateObject.query(SQL, 
 	                                new RuntimeRecordMapper());
 	      return records;
 	}
 
 	@Override
 	public void delete(Integer id) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
