 package it.unipd.dei.webqual.converter.merge;
 
 import com.codahale.metrics.CsvReporter;
 import com.codahale.metrics.MetricRegistry;
 import com.codahale.metrics.Timer;
 import it.unipd.dei.webqual.converter.Utils;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.TimeUnit;
 
 /**
  * This class takes as input a set of adjacency list files and merges them
  * removing duplicates
  */
 public class GraphMerger {
 
   private static final Logger log = LoggerFactory.getLogger(GraphMerger.class);
 
   public static final MetricRegistry metrics = new MetricRegistry();
 
   private static final PairComparator PAIR_COMPARATOR = new PairComparator();
   private static final PairMerger PAIR_MERGER = new PairMerger();
 
   private static void sort(String inPath, String outPath, int idLen) throws IOException {
     log.info("Sorting {}", inPath);
     Timer timer = metrics.timer("file-sorting");
     Timer.Context context = timer.time();
     List<Pair> pairs = new LinkedList<>();
 
     // the `true` parameter is for resetting the first bit of the IDs
     LazyFilePairIterator it = new LazyFilePairIterator(inPath, idLen);
     while(it.hasNext()) {
       pairs.add(it.next());
     }
 
     Collections.sort(pairs);
     writePairs(outPath, pairs);
     long time = context.stop();
     log.info("{} sorted, elapsed time: {} seconds", inPath, time/1000000000);
   }
 
   private static void writeIterator(String outPath, Iterator<Pair> pairs) throws IOException {
     OutputStream out = new BufferedOutputStream(new FileOutputStream(outPath));
     while(pairs.hasNext()) {
       Pair pair = pairs.next();
       out.write(Utils.setHead(pair.head));
       for(byte[] neigh : pair.neighbours) {
         out.write(neigh);
       }
     }
     out.close();
   }
 
   private static void writePairs(String outPath, List<Pair> pairs)
     throws IOException {
     writeIterator(outPath, pairs.iterator());
   }
 
   private static File[] sortFiles(File[] inFiles, int idLen) throws IOException {
     log.info("============= Sorting files ===============");
     Timer timer = metrics.timer("total-sorting");
     Timer.Context context = timer.time();
     File[] sortedFiles = new File[inFiles.length];
     for(int i = 0; i<inFiles.length; i++) {
       sortedFiles[i] = File.createTempFile("graph-merger", "sorting"+inFiles[i].getName());
       sort(inFiles[i].getCanonicalPath(), sortedFiles[i].getCanonicalPath(), idLen);
     }
     long time = context.stop();
     log.info("====== Files sorted, elapsed time: {} seconds", time / 1000000000);
     return sortedFiles;
   }
 
   private static void mergeFiles(File[] sortedFiles, String outputName, int groupBy, int idLen) throws IOException {
     if(groupBy < 2) {
       throw new IllegalArgumentException("groupBy should be >= 2");
     }
     if(sortedFiles.length <= groupBy) {
       mergeFiles(sortedFiles, outputName, idLen);
     }
 
     File[] tmpFiles = new File[sortedFiles.length / groupBy];
 
     for(int i = 0; i < tmpFiles.length; i++) {
       tmpFiles[i] = File.createTempFile("graph-merger", "merging");
 
       File[] group = Arrays.copyOfRange(
         sortedFiles, i*groupBy, Math.min((i + 1)*groupBy, sortedFiles.length));
 
       log.info("Merging {} out of {} files: {}%",
         group.length, sortedFiles.length, (((double) i)/tmpFiles.length)*100);
       mergeFiles(group, tmpFiles[i].getCanonicalPath(), groupBy, idLen);
     }
 
   }
 
   /**
    * Creates a single lazy iterator over the merged files and uses it to create
    * the real merge file.
    * @param sortedFiles
    * @param outputName
    */
   private static void mergeFiles(File[] sortedFiles, String outputName, int idLen) throws IOException {
     Timer timer = metrics.timer("file-merging");
     Timer.Context context = timer.time();
     LazyMergeIterator<Pair>[] iterators = new LazyMergeIterator[sortedFiles.length/2];
     for(int i=0; i<iterators.length; i++) {
       if((2*i+1) < sortedFiles.length) {
         iterators[i] = new LazyMergeIterator<>(
           new LazyFilePairIterator(sortedFiles[2*i].getCanonicalPath(), idLen),
           new LazyFilePairIterator(sortedFiles[2*i+1].getCanonicalPath(), idLen),
           PAIR_COMPARATOR, PAIR_MERGER);
       } else {
         iterators[i] = new LazyMergeIterator<>(
           new LazyFilePairIterator(sortedFiles[2*i].getCanonicalPath(), idLen),
           PAIR_COMPARATOR, PAIR_MERGER);
       }
     }
 
     LazyMergeIterator<Pair> it = LazyMergeIterator.compose(PAIR_COMPARATOR, PAIR_MERGER, iterators);
     writeIterator(outputName, it);
    
     long time = context.stop();
     log.info("{} files merged, elapsed time: {} seconds", sortedFiles.length, time/1000000000);
   }
 
   public static void main(String[] args) throws IOException {
 
     Options opts = new Options();
     CmdLineParser parser = new CmdLineParser(opts);
     try {
       parser.parseArgument(args);
     } catch (CmdLineException e) {
       System.err.println(e.getMessage());
       parser.printUsage(System.err);
       System.exit(1);
     }
 
     File[] inFiles = opts.inputDir.listFiles();
 
     File[] sortedFiles = (opts.noSort)? inFiles : sortFiles(inFiles, opts.idLen);
 
     log.info("============= Merging files ===============");
     Timer timer = metrics.timer("total-merging");
     Timer.Context context = timer.time();
     mergeFiles(sortedFiles, opts.outputName, opts.groupBy, opts.idLen);
     long time = context.stop();
     log.info("====== Files merged, elapsed time: {} seconds", time/1000000000);
 
 
     log.info("{} duplicates have been merged", metrics.counter("duplicates").getCount());
 
     CsvReporter reporter = CsvReporter.forRegistry(metrics)
                                       .formatFor(Locale.ITALY)
                                       .convertRatesTo(TimeUnit.SECONDS)
                                       .convertDurationsTo(TimeUnit.SECONDS)
                                       .build(new File("."));
     reporter.report();
     reporter.close();
   }
 
   public static class Options {
 
     @Option(aliases="--input-dir", name="-i", usage="The input directory", required=true, metaVar="FILE")
     File inputDir;
 
     @Option(aliases="--out", name="-o", usage="The output file", required=true, metaVar="FILE")
     String outputName;
 
     @Option(aliases="--group-by", name="-g", usage="Merge files in chunks of N files", metaVar="N")
     int groupBy = 2;
 
     @Option(aliases="--id-len", name="-l", usage="The length of the IDs in bytes", metaVar="N")
     int idLen = 16;
 
     @Option(name="--no-sort", usage="Skip sorting phase. Input must be already sorted")
     boolean noSort = false;
 
   }
 
 }
