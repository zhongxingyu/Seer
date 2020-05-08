 package gov.nih.ncgc.hadoop;
 
 import chemaxon.formats.MolImporter;
 import chemaxon.license.LicenseManager;
 import chemaxon.license.LicenseProcessingException;
 import chemaxon.struc.Molecule;
 import gov.nih.ncgc.hadoop.io.MoleculePairWritable;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.filecache.DistributedCache;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.*;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Random;
 
 /**
  * Frequency of heavy atoms in a collection of SMILES.
  * <p/>
  * The cheminformatics version of the WordCount example. To run, ensure that jchem.jar
  * is either in the Hadoop <code>lib</code> directory, specified via <code>-libjars</code>
  * or else made available via the distributed cache. To run:
  * <code>
  * hadoop jar atomcount.jar path_to_smiles output_dir
  * </code>
  */
 public class BioIsostere extends Configured implements Tool {
 
     public static class BioisostereMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, MoleculePairWritable> {
 
         private final static IntWritable one = new IntWritable(1);
         private Text smirks = new Text();
         private String[] rndKeys = {"key1", "key2", "key3", "key4"};
         Random rnd;
 
         public void configure(JobConf job) {
 
             try {
                 Path[] licFiles = DistributedCache.getLocalCacheFiles(job);
                 BufferedReader reader = new BufferedReader(new FileReader(licFiles[0].toString()));
                 StringBuilder license = new StringBuilder();
                 String line;
                 while ((line = reader.readLine()) != null) license.append(line);
                 reader.close();
                 LicenseManager.setLicense(license.toString());
                 rnd = new Random();
             } catch (IOException e) {
             } catch (LicenseProcessingException e) {
             }
         }
 
         public void map(LongWritable longWritable, Text value, OutputCollector<Text, MoleculePairWritable> output, Reporter reporter) throws IOException {
             String[] toks = value.toString().split("\t");
             Molecule fragment = MolImporter.importMol(toks[0]);
 
             for (int i = 1; i < toks.length - 1; i++) {
                 Molecule m1 = MolImporter.importMol(toks[i]);
                 for (int j = i + 1; j < toks.length; j++) {
                     Molecule m2 = MolImporter.importMol(toks[j]);
 
                     // compare m1 & m2
                     smirks.set(rndKeys[rnd.nextInt(rndKeys.length)]);
                    MoleculePairWritable out = new MoleculePairWritable(toks[i], toks[j]);
                    output.collect(smirks, out);
                 }
             }
         }
     }
 
     public static class MoleculePairReducer extends MapReduceBase implements Reducer<Text, MoleculePairWritable, Text, IntWritable> {
 
         public void reduce(Text key,
                            Iterator<MoleculePairWritable> values,
                            OutputCollector<Text, IntWritable> output, Reporter reporter)
                 throws IOException {
             int sum = 0;
             while (values.hasNext()) sum++;
             output.collect(key, new IntWritable(sum));
         }
     }
 
     public int run(String[] args) throws Exception {
         JobConf jobConf = new JobConf(getConf(), HeavyAtomCount.class);
         jobConf.setJobName("smartsSearch");
 
         jobConf.setOutputKeyClass(Text.class);
        jobConf.setOutputValueClass(IntWritable.class);
 
         jobConf.setMapperClass(BioisostereMapper.class);
         jobConf.setCombinerClass(MoleculePairReducer.class);
         jobConf.setReducerClass(MoleculePairReducer.class);
 
         jobConf.setInputFormat(TextInputFormat.class);
         jobConf.setOutputFormat(TextOutputFormat.class);
 
         jobConf.setNumMapTasks(5);
 
         if (args.length != 3) {
             System.err.println("Usage: bisos <datafile> <out> <license file>");
             System.exit(2);
         }
 
         FileInputFormat.setInputPaths(jobConf, new Path(args[0]));
         FileOutputFormat.setOutputPath(jobConf, new Path(args[1]));
 
         // make the license file available vis dist cache
         DistributedCache.addCacheFile(new Path(args[2]).toUri(), jobConf);
 
         JobClient.runJob(jobConf);
         return 0;
     }
 
     public static void main(String[] args) throws Exception {
 
         int res = ToolRunner.run(new Configuration(), new BioIsostere(), args);
 
     }
 }
