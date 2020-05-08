 package no.magott.spring.batch.tasklet;
 
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.springframework.batch.core.StepContribution;
 import org.springframework.batch.core.scope.context.ChunkContext;
 import org.springframework.batch.core.step.tasklet.Tasklet;
 import org.springframework.batch.item.ExecutionContext;
 import org.springframework.batch.item.ItemStream;
 import org.springframework.batch.item.ItemStreamException;
 import org.springframework.batch.item.util.ExecutionContextUserSupport;
 import org.springframework.batch.repeat.RepeatStatus;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 public class SqlExecutingTasklet implements Tasklet{
 
 	private final static String EXECUTION_COUNT = "sql.execution.count";
 
 	private JdbcTemplate jdbcTemplate;
 	private ExecutionContextUserSupport ecSupport;
 	private List<String> sqls;
 	private int count = 0;
 	private ExecutionContext executionContext;
 
 	public SqlExecutingTasklet() {
 		ecSupport = new ExecutionContextUserSupport(
 				SqlExecutingTasklet.class.getSimpleName());
 	}
 
 	public RepeatStatus execute(StepContribution contribution,
 			ChunkContext chunkContext) throws Exception {
 		count = getCount();
 		String sql = sqls.get(count);
 		int updateCount = jdbcTemplate.update(sql);
 		contribution.incrementWriteCount(updateCount);
 		incrementCount();
 		return count == sqls.size() ? RepeatStatus.FINISHED
 				: RepeatStatus.CONTINUABLE;
 	}
 
 	public void setDataSource(DataSource dataSource) {
 		jdbcTemplate = new JdbcTemplate(dataSource);
 	}
 
 	public int getCount() {
		return executionContext.containsKey(EXECUTION_COUNT)?executionContext.getInt(ecSupport.getKey(EXECUTION_COUNT)):0;
 	}
 
 	public void incrementCount(){
		executionContext.putInt(ecSupport.getKey(EXECUTION_COUNT), ++count);
 	}
 
 	public void setSqls(List<String> sqls) {
 		this.sqls = sqls;
 	}
 
 	public void setExecutionContext(ExecutionContext executionContext) {
 		this.executionContext = executionContext;
 	}
 
 }
