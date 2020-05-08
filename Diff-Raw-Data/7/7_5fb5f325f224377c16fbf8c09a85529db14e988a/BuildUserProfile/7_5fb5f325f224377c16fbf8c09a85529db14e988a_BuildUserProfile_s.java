 package wikigraph;
 
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Partitioner;
 import org.apache.hadoop.mapreduce.Reducer;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.apache.log4j.Logger;
 
import edu.umd.cloud9.collection.XMLInputFormat;
 import edu.umd.cloud9.io.pair.PairOfStringLong;
 	
 	public class BuildUserProfile extends Configured implements Tool {
 	    private static final Logger LOG = Logger.getLogger(BuildUserProfile.class);
 
 	    /* SignatureeMapper
 	     * 
 	     * Parameters that can be tweaked: NHASH, NHASHOUTPUTBITS, MINLEN
 	     * 
 	     * Pulls out sentences from text input using a regex. 
 	     * Emits one NHASH-length minhash signature per sentence.
 	     * Each hash is NHASHOUTPUTBITS long. (So signature is NHASH*NHASHOUTPUTBITS long.)
 	     * Sentences are shingled by individual words. 
 	     * If sentences are less than MINLEN words, then they are skipped.
 	     * 
 	     * 
 	     * Output values are (offset,nsentence) where offset is the byte offset of the input line in the
 	     * input text and nsentence is the number of the sentence in the line. (starting from 0)
 	     * 
 	     */
 
 		/*
 		 * 
 		 * 
 		 * pp      <namespace key="-2" case="first-letter">Media</namespace>
 <namespace key="-1" case="first-letter">Special</namespace>
 *  <namespace key="0" case="first-letter" />
 *  <namespace key="1" case="first-letter">Talk</namespace>
 *  <namespace key="2" case="first-letter">User</namespace>
 *  <namespace key="3" case="first-letter">User talk</namespace>
 <namespace key="4" case="first-letter">Wikipedia</namespace>
 <namespace key="5" case="first-letter">Wikipedia talk</namespace>
 <namespace key="6" case="first-letter">File</namespace>
 <namespace key="7" case="first-letter">File talk</namespace>
 <namespace key="8" case="first-letter">MediaWiki</namespace>
 <namespace key="9" case="first-letter">MediaWiki talk</namespace>
 <namespace key="10" case="first-letter">Template</namespace>
 <namespace key="11" case="first-letter">Template talk</namespace>
 <namespace key="12" case="first-letter">Help</namespace>
 <namespace key="13" case="first-letter">Help talk</namespace>
 <namespace key="14" case="first-letter">Category</namespace>
 <namespace key="15" case="first-letter">Category talk</namespace>
 <namespace key="100" case="first-letter">Portal</namespace>
 <namespace key="101" case="first-letter">Portal talk</namespace>
 <namespace key="108" case="first-letter">Book</namespace>
 <namespace key="109" case="first-letter">Book talk</namespace>
 <namespace key="446" case="first-letter">Education Program</namespace>
 <namespace key="447" case="first-letter">Education Program talk</namespace>
 <namespace key="710" case="first-letter">TimedText</namespace>
 <namespace key="711" case="first-letter">TimedText talk</namespace>
 <namespace key="828" case="first-letter">Module</namespace>
 
 		 */
 	    
 	    private static class RevisionMapper extends Mapper<LongWritable, Text, PairOfStringLong, RevisionRecord> {
 
 	    	 /*
 	        <revision>
 	        <id>4407235</id>
 	        <parentid>2527990</parentid>
 	        <timestamp>2004-02-25T18:55:21Z</timestamp>
 	        <contributor>
 	          <username>Dori</username>
 	          <id>6878</id>
 	        </contributor>
 	        <minor/>
 	        <comment>restoring blanked content, no reason given</comment>
 	        <text id="4407235" bytes="538" />
 	        <sha1>cgc468uc4empeg5jggze1lsb87vquuc</sha1>
 	        <model>wikitext</model>
 	        <format>text/x-wiki</format>
 	      </revision>
 	    */
 	    	private static String wikistart = "2001-01-01T00:00:00Z";
 	    	private static long wikistartmillis = javax.xml.bind.DatatypeConverter.parseDateTime(wikistart).getTimeInMillis();
 	    	private static long millisinday = 86400000;
 	    	static final Pattern titlePattern = Pattern.compile(".*<title>(.*)<\\/title>.*");
 	    	static final Pattern beginRevisionPattern = Pattern.compile(".*<revision>.*");
 	    	static final Pattern endRevisionPattern = Pattern.compile(".*<\\/revision>.*");
 	    	static final Pattern userNamePattern = Pattern.compile(".*<username>(.*)<\\/username>.*");
 	    	static final Pattern ipPattern = Pattern.compile(".*<ip>(.*)<\\/ip>.*");
 	    	static final Pattern nsPattern = Pattern.compile(".*<ns>(.*)<\\/ns>.*");
 	    	static final Pattern timestampPattern = Pattern.compile(".*<timestamp>(.*)<\\/timestamp>.*");
 	    	static final Pattern bytesPattern = Pattern.compile(".*<text id=\"(.*)\" bytes=\"(.*)\" \\/>.*");
 	    	
 	    	static TreeMap<Long,RevisionRecord> revisionMap = new TreeMap<Long,RevisionRecord>();
 	    	
 	        public void map(LongWritable key, Text p, Context context)
 	                throws IOException, InterruptedException {
 	               
 	               // users & articles with weights?
 	        	   // users to users consecutive edits with weights
 	        	   // some sort of user editing diagram
 	        	   // time to next edit
 	        	   // highly protected/controversial articles
 	            String lines[] = p.toString().split("\n");
 	            revisionMap.clear();
 	            //System.out.println("key = " + key);
 	            Matcher m;
 	            String title = null;
 	            String user = null;
 	            String ip = null;
 	            String ns = null;
 	            String timestamp = null;
 	            String bytes = null;
 	            long lastTime = 0;
 	            //Text userOut = null;
 	            for(String line: lines){
 					//System.out.println("LINE " + line);
 	            	m = titlePattern.matcher(line);
 	            	if((m = endRevisionPattern.matcher(line)).matches()){
 						//System.out.println("\tend revision\n");
 						//System.out.println("user = " + ((user != null)?user:"NULL"));
 						//System.out.println("ip = " + ((ip != null)?ip:"NULL"));
 						//<timestamp>2001-01-21T02:12:21Z</timestamp>
 						//<ns>0</ns>
 
 	            		if(title != null && (user != null || ip != null)
 	            				&& ns != null && timestamp != null && bytes != null){
 		            		String name;
 	            			if(user == null){
 	            				name = ip;
 	            			}else{
 	            				name = user;
 	            			}
 	            			//userOut = new Text();
 	            			//userOut.set(name);
 	            			long time = parseTime(timestamp);
 	            			
 	            			RevisionRecord r = new RevisionRecord(Integer.parseInt(ns),time,0,title,Integer.parseInt(bytes));
 	            			r.setUsername(name);
 	            			//System.out.println("user = " + name);
 	            			//System.out.println("last time = " + lastTime);
 	            			//output.collect(userOut, r);
 	            			revisionMap.put(time, r);
 	            			
 	            			
 						}
 	            		bytes = null;
 	            		user = null;
             			ip = null;
             			timestamp = null;
 	            	}else if((m = titlePattern.matcher(line)).matches()){
 	            		title = m.group(1);
 	            		//System.out.println("\tTITLE " + title);
 	            	}else if((m = userNamePattern.matcher(line)).matches()){
 	            		user = m.group(1);
 	            		//System.out.println("\tUSER " + user);
 	            	}else if((m = ipPattern.matcher(line)).matches()){
 	            		ip = m.group(1);
 	            		//System.out.println("\tIP USER " + ip);
 	            	}else if((m = nsPattern.matcher(line)).matches()){
 	            		ns = m.group(1);
 	            		//System.out.println("\tNS " + ns);
 	            	}else if((m = timestampPattern.matcher(line)).matches()){
 	            		timestamp = m.group(1);
 	            		//System.out.println("\tTIMESTAMP " + timestamp);
 	            	}else if((m = bytesPattern.matcher(line)).matches()){
 	            		bytes = m.group(2);
 	            		//System.out.println("\tBYTES " + bytes);
 	            	}
 	            }
 	            
 	            lastTime = 0;
 	            long time = 0;
 	            for(long t : revisionMap.keySet()){
 	            	PairOfStringLong userDate = new PairOfStringLong();
 	            	RevisionRecord r = revisionMap.get(t);
 	            	time = r.getTime();
 	            	long timediff = time - lastTime;
 	            	String name = r.getUsername();
 	            	r.setTimeToNextEdit(timediff);
 	            	userDate.set(name, time);
 	            	//System.out.println("time = " + time + " last time = " + lastTime + " time diff = " + timediff);
 	            	context.write(userDate, r);
 	            	lastTime = time;
 	            }
 	            
 	        }
 	        
 		    
 		    //<timestamp>2004-02-25T18:55:21Z</timestamp>
 		    //yyyy-MM-dd'T'HH:mm:ssz
 	       public static long parseTime(String timestr) {
 	          
 	          Calendar calendar = javax.xml.bind.DatatypeConverter.parseDateTime(timestr);
 	          long time = (calendar.getTimeInMillis() - wikistartmillis)/millisinday;
 			  return time;
 	          /*
 			  Date t; 
 	          try {
 				t = ft.parse(timestr);
 				return t.getTime();
 			  } catch (java.text.ParseException e) {
 					// TODO Auto-generated catch block
 				e.printStackTrace();
 				return 0;
 			  }	
 			  */ 
 		       
 	       }
 	    }
 
 	    
 	    private static class UserPartitioner extends Partitioner<PairOfStringLong,RevisionRecord>{
 
 			@Override
 			public int getPartition(PairOfStringLong key, RevisionRecord val,int nReducers) {
 				return (key.getLeftElement().hashCode() & Integer.MAX_VALUE)%nReducers;
 			}
 	    	
 	    }
 	    
 	    private static class RevisionReducer extends Reducer<PairOfStringLong, RevisionRecord, Text, UserProfile> {
 			public static TreeMap<Long,Long> dayedits = new TreeMap<Long,Long>();
 			public static TreeMap<Long,Long> dayarticles = new TreeMap<Long,Long>();
 			public static TreeMap<Integer,Long> nscounts = new TreeMap<Integer,Long>();
 			public static long narticles = 0;
 			public static long nedits = 0;
 			public static String lastuser = null;
 			HashSet<String> articleSet = new HashSet<String>();
 			public static long sumTime = 0;
 			public static long sumBytes = 0;
 			
 			@Override
 			public void reduce(PairOfStringLong key, Iterable<RevisionRecord> records, Context context)
 			       throws IOException, InterruptedException {
 				//System.out.println("key = " + key);
 				long day = key.getRightElement();
 				String user = key.getLeftElement();
 				UserProfile profile;
 				Text userOut;
 				
 				if(lastuser == null){
 					lastuser = user;
 				}
 				
 				if(!user.equals(lastuser)){
 					// Only output if user has made more than one edit over more than one day
 					// Should count number of users that don't meet this criteria
 					long span = dayedits.lastKey() - dayedits.firstKey();
 					if(nedits > 1 && span > 1){
 						userOut = new Text();
 						profile = new UserProfile();
 						userOut.set(lastuser);
 						profile.setNArticles(narticles);
 						profile.setNEdits(nedits);
 						profile.setEditMap(dayedits);
 						profile.setArticleMap(dayarticles);
 						profile.setMeanTimeToNextEdit(sumTime/nedits);
 						profile.setMeanEditBytes(sumBytes/nedits);
 						profile.setNamespaceMap(nscounts);
 					//	System.out.println("output = " + userOut + "," + profile);
 						context.write(userOut, profile);
 					}
 					dayedits = new TreeMap<Long,Long>();
 					dayarticles = new TreeMap<Long,Long>();
 					nscounts = new TreeMap<Integer,Long>();
 					nedits = 0;
 					narticles = 0;
 					sumTime = 0;
 					sumBytes = 0;
 				}
 				
 				articleSet.clear();
 				Iterator<RevisionRecord> recordsIt = records.iterator();
 				long dayct = 0;
 				while(recordsIt.hasNext()){
 					RevisionRecord r = recordsIt.next();
 					int ns = r.getNamespace();
 					if(!nscounts.containsKey(ns)) nscounts.put(ns, 0l);
 					nscounts.put(ns, nscounts.get(ns) + 1);
 					//System.out.println("Record = " + r);
 					articleSet.add(r.getArticle());
 					sumTime += r.getTimeToNextEdit();
 					sumBytes += r.getLength();
 					dayct++;
 				}
 				dayarticles.put(day, (long) articleSet.size());
 				dayedits.put(day, dayct);
 				nedits += dayct;
 				narticles += articleSet.size();
 				lastuser = user;
 				
 			}
 			
 			@Override
 			public void cleanup(Context context) throws IOException, InterruptedException{
 				if(narticles != 0 || nedits != 0){
 					UserProfile profile;
 					Text userOut;
 					
 					userOut = new Text();
 					profile = new UserProfile();
 					userOut.set(lastuser);
 					profile.setNArticles(narticles);
 					profile.setNEdits(nedits);
 					profile.setEditMap(dayedits);
 					profile.setArticleMap(dayarticles);
 					profile.setNamespaceMap(nscounts);
 					profile.setMeanTimeToNextEdit(sumTime/nedits);
 					profile.setMeanEditBytes(sumBytes/nedits);
 					context.write(userOut, profile);
 				}
 			}
 	    	
 	    }
 
 
 	 
 	    private static final String INPUT = "input";
 	    private static final String OUTPUT = "output";
 	    	    
 	    @SuppressWarnings("static-access")
 	    @Override
 	    public int run(String[] args) throws Exception {
 	        Options options = new Options();
 	        options.addOption(OptionBuilder.withArgName("path")
 	                .hasArg().withDescription("bz2 input path").create(INPUT));
 	        options.addOption(OptionBuilder.withArgName("path")
 	                .hasArg().withDescription("output path").create(OUTPUT));
 	        
 	        CommandLine cmdline;
 	        CommandLineParser parser = new GnuParser();
 	        try {
 	            cmdline = parser.parse(options, args);
 	        } catch (ParseException exp) {
 	            System.err.println("Error parsing command line: " + exp.getMessage());
 	            return -1;
 	        }
 
 	        if (!cmdline.hasOption(INPUT) || !cmdline.hasOption(OUTPUT)){ 
 	            HelpFormatter formatter = new HelpFormatter();
 	            formatter.setWidth(120);
 	            formatter.printHelp(this.getClass().getName(), options);
 	            ToolRunner.printGenericCommandUsage(System.out);
 	            return -1;
 	        }
 
 	        String inputPath = cmdline.getOptionValue(INPUT);
 	        String outputPath = cmdline.getOptionValue(OUTPUT);
 
 	        
 
 	        LOG.info("Tool name: " + this.getClass().getName());
 	        LOG.info(" - input file: " + inputPath);
 	        LOG.info(" - output file: " + outputPath);
 
 	        Configuration conf = getConf();
 	        // Set heap space - using old API
 	        conf.set("mapred.job.map.memory.mb", "2048");
 	        conf.set("mapred.map.child.java.opts", "-Xmx2048m");
 	        conf.set("mapred.job.reduce.memory.mb", "6144");
 	        conf.set("mapred.reduce.child.java.opts", "-Xmx6144m");
 	        conf.set("xmlinput.start","page");
 	        conf.set("xmlinput.end","page");
 	        //conf.set("mapred.child.java.opts", "-Xmx2048m");
 
 	        Job job = Job.getInstance(conf);
 	        //JobConf conf = new JobConf(getConf(), BuildUserProfile.class);
 	        job.setJobName(String.format("BuildUserProfile[%s: %s, %s: %s]", INPUT, inputPath, OUTPUT, outputPath));
 	               
 
 	        job.setNumReduceTasks(20);
 
 	        job.setMapperClass(RevisionMapper.class);
 	        job.setReducerClass(RevisionReducer.class);
 	        job.setPartitionerClass(UserPartitioner.class);
 	        
 	        //conf.setInputFormat(WikipediaPageInputFormat.class);
 	        job.setInputFormatClass(XMLInputFormat.class);
 	        job.setOutputFormatClass(SequenceFileOutputFormat.class);
 	        //conf.setOutputFormat(TextOutputFormat.class);
 	        
 	        
 	        job.setMapOutputKeyClass(PairOfStringLong.class);
 	        job.setMapOutputValueClass(RevisionRecord.class);
 	        
 	        job.setOutputKeyClass(Text.class);
 	        job.setOutputValueClass(UserProfile.class);
 	        
 	        FileSystem fs = FileSystem.get(conf);        
 	        Path outPath = new Path(outputPath);
 	        
 	        FileInputFormat.setInputPaths(job, new Path(inputPath));
 	        FileOutputFormat.setOutputPath(job, outPath);
 	        
 	        // Delete the output directory if it exists already.
 	        fs.delete(outPath, true);
 
 	        long startTime = System.currentTimeMillis();
 	        job.waitForCompletion(true);
 	        LOG.info("Total Job Finished in " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
 	        
 	        return 0;
 	    }
 
 	    public BuildUserProfile() {}
 
 	    public static void main(String[] args) throws Exception {
 	        ToolRunner.run(new BuildUserProfile(), args);
 	    }
 	}
 
 
