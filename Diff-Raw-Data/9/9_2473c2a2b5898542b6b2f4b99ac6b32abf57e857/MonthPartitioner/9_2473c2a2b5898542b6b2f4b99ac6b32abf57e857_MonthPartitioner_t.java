 package cloudera.lab;
 
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.mapreduce.Partitioner;
 import org.apache.hadoop.conf.Configurable;
 import org.apache.hadoop.conf.Configuration;
 import java.util.*;
 
 public class MonthPartitioner extends Partitioner<Text, IntWritable> implements Configurable {
 
 	private Configuration configuration;
 	private Map<String, Integer> month2reducer = new HashMap<String, Integer>();
 	
 	@Override
 	public void setConf(Configuration configuration) {
 		this.configuration = configuration;
 		month2reducer.put("Jan", 0);
 	    month2reducer.put("Feb", 1);
 	    month2reducer.put("Mar", 2);
 	    month2reducer.put("Apr", 3);
 	    month2reducer.put("May", 4);
 	    month2reducer.put("Jun", 5);
 	    month2reducer.put("Jul", 6);
 	    month2reducer.put("Aug", 7);
 	    month2reducer.put("Sep", 8);
 	    month2reducer.put("Oct", 9);
 	    month2reducer.put("Nov", 10);
 	    month2reducer.put("Dec", 11);
 	}
 	
 	@Override
 	public Configuration getConf() {
 		return this.configuration;
 	}
 	
 	@Override
 	public int getPartition(Text key, IntWritable value, int numReduceTasks) {
 		String month = key.toString().split("\t")[1];
		return (int)(month2reducer.get(month));
 	}
 }
