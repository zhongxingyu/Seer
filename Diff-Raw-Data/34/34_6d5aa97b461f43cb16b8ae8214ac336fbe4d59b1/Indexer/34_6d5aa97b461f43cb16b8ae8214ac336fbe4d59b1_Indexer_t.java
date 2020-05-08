 package group1.inverted;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.FileInputFormat;
 import org.apache.hadoop.mapred.FileOutputFormat;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.MapReduceBase;
 import org.apache.hadoop.mapred.Mapper;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.Reducer;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 public class Indexer extends Configured implements Tool {
 
 	public static class Map extends MapReduceBase implements
 			Mapper<LongWritable, Text, Text, Posting> {
 
 		private Text urlText = new Text();
 		private Text titleText = new Text();
 
 		public void map(LongWritable key, Text contents,
 				OutputCollector<Text, Posting> output, Reporter reporter)
 				throws IOException {
 
 			// Create the tokenizer
 			Tokenizer t;
 			try {
				t = new Tokenizer(key, contents);
 			} catch (Exception e) {
 				e.printStackTrace();
 				throw new RuntimeException(e);
 			}
 
 			// Create the collection for accumulation of word and word count
 			// associations
 			java.util.Map<String, Integer> index = new java.util.HashMap<String, Integer>();
 
 			// Collect word counts
 			Iterator<String> iter = t.iterator();
 			while (iter.hasNext()) {
 				String word = iter.next();
 				if (index.containsKey(word)) {
 					index.put(word, index.get(word) + 1);
 				} else {
 					index.put(word, 1);
 				}
 			}
 
 			// Emit postings
 			Iterator<String> keys = index.keySet().iterator();
 			while (keys.hasNext()) {
 				String word = keys.next();
 				Posting p = new Posting();
 				p.docid = t.getDocId();
 				p.count = index.get(word);
 				output.collect(new Text(word), p);
 			}
 
 		}
 	}
 
 	/**
 	 * A reducer class that just emits its input.
 	 */
 	public static class Reduce extends MapReduceBase implements
 			Reducer<Text, Posting, Text, Posting> {
 
 		public void reduce(Text key, Iterator<Posting> values,
 				OutputCollector<Text, Posting> output, Reporter reporter)
 				throws IOException {
 
 			// Create a sortable tree map.
 			// java.util.Map<String, Integer> postings = new
 			// java.util.TreeMap<String, Integer>();
 			// while (values.hasNext())
 			// {
 			// Posting p = values.next();
 			// postings.put(p.docid, p.count);
 			// }
 
 			while (values.hasNext()) {
 				output.collect(key, values.next());
 			}
 
 		}
 	}
 
 	public int run(String[] args) throws Exception {
 		JobConf conf = new JobConf(getConf(), Indexer.class);
 		conf.setJobName("multifetch");
 
 		// the keys are urls (strings)
 		conf.setOutputKeyClass(Text.class);
 		// the values are titles (strings)
		conf.setOutputValueClass(Posting.class);
 
 		conf.setMapperClass(Map.class);
 		conf.setCombinerClass(Reduce.class);
 		conf.setReducerClass(Reduce.class);
 
 		// Make sure there are exactly 2 parameters left.
 		/*
 		 * if (other_args.size() != 2) {
 		 * System.out.println("ERROR: Wrong number of parameters: " +
 		 * other_args.size() + " instead of 2."); return printUsage(); }
 		 */
 		// conf.setInputPath(new Path(other_args.get(0)));
 		// conf.setOutputPath(new Path(other_args.get(1)));
 
 		FileInputFormat.setInputPaths(conf, new Path(args[0]));
 		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
 
 		JobClient.runJob(conf);
 		return 0;
 	}
 
 	public static void main(String[] args) throws Exception {
 		String[] newArgs = { args[1], args[2] };
 		int res = ToolRunner.run(new Configuration(), new Indexer(), newArgs);
 		System.exit(res);
 	}
 
 }
