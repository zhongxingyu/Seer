 /*
  * The HRT Project.
  * This work is licensed under the 
  * Creative Commons Attribution-NonCommercial 3.0 Unported License. 
  * To view a copy of this license, 
  * visit http://creativecommons.org/licenses/by-nc/3.0/ 
  * or send a letter to 
  * Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
  */
 package org.hrva.capture;
 
 import com.fourspaces.couchdb.Document;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.text.MessageFormat;
 import java.util.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 
 /**
  * Combination "Wrapper" which tails a log, reformats and then pushes.
  *
  * <p>This cycles on a one-minute interval. </p>
  *
  * <p>This is both a main program with a command-line interface, as well as
  * object that can be used to push. </p>
  *
  * <p>Typical use case</p>
  * <code><pre>
  *      File prop_file = new File("hrtail.properties");
  *      Properties config= new Properties();
  *      config.load(new FileInputStream(prop_file));
  *
  *      Capture instance = new Capture(config);
  *      instance.capture( "/path/to/some.log", 60.0 );
  * </pre></code>
  *
  * <p>At the command line, it might look like this.</p>
  * <code><pre>
  * java -cp LogCapture/dist/LogCapture.jar org.hrva.capture.Capture /path/to/some.log
  * </pre></code>
  *
  * <p>This will cycle indefinitely, capturing, reformatting and pushing extracts
  * from the log file. </p>
  *
  * <p>This uses the <tt>hrtail.properties</tt> file.</p> <dl>
  * <dt><tt>capture.extract_filename</tt><dd>The file to which to write log
  * extracts</dd> <dt><tt>capture.csv_filename</tt><dd>The file to which to write
  * reformatted extracts</dd> </dl>
  *
  * @author slott
  */
 public class Capture {
 
     /**
      * Properties for this application.
      */
     Properties global;
     /**
      * Immediate Push option.
      */
     @Option(name = "-1", usage = "One Ping Only, Please.")
     boolean one_time = false;
     /**
      * Verbose debugging.
      */
     @Option(name = "-v", usage = "Vebose logging")
     boolean verbose = false;
     /**
      * Cycle time.
      */
     @Option(name = "-c", usage = "Cyclic Processing Interval, default 60 seconds")
     double cycle_time = 60.0;
     /**
      * Command-line Arguments.
      */
     @Argument
     List<String> arguments = new ArrayList<String>();
     /**
      * The Scheduling timer.
      */
     Timer timer = new Timer();
     /**
      * The scheduled operation.
      */
     Tail_Format_Push worker;
     /**
      * Logger.
      */
     final Log logger = LogFactory.getLog(Capture.class);
 
     /**
      * Command-line program to tail a log and then push file to the HRT couch
      * DB.
      *
      * <p>All this does is read properties and invoke run_main</p>
      *
      * @param args arguments
      */
     public static void main(String[] args) {
         Log log = LogFactory.getLog(Reformat.class);
         File prop_file = new File("hrtail.properties");
         Properties config = new Properties();
         try {
             config.load(new FileInputStream(prop_file));
         } catch (IOException ex) {
             log.warn( "Can't find "+prop_file.getName(), ex );
             try {
                 log.debug(prop_file.getCanonicalPath());
             } catch (IOException ex1) {
             }
         }
         Capture capture = new Capture(config);
         try {
             capture.run_main(args);
         } catch (CmdLineException ex1) {
             log.fatal("Invalid Options", ex1);
         } catch (MalformedURLException ex2) {
             log.fatal("Invalid CouchDB URL", ex2);
         } catch (IOException ex3) {
             log.fatal(ex3);
         }
     }
 
     /**
      * Build the LogTail instance.
      *
      * @param global The hrtail.properties file
      */
     public Capture(Properties global) {
         super();
         this.global = global;
         worker = new Tail_Format_Push();
     }
 
     /**
      * Does the three-stop process of tail, reformat and couch push.
      *
      * <p>Accepts the following options</p> <dl> <dt><tt>-1</tt></dt><dd>Don't
      * cycle; process once only.</dd> <dt><tt>-v</tt></dt><dd>Verbose
      * logging.</dd> </dl> <p>This gets properties from the
      * <tt>hrtail.properties</tt> file.</p>
      *
      *
      * @param args the command line arguments
      * @throws CmdLineException
      * @throws FileNotFoundException
      * @throws IOException
      */
     public void run_main(String[] args) throws CmdLineException, FileNotFoundException, IOException {
         CmdLineParser parser = new CmdLineParser(this);
         parser.parseArgument(args);
 
         if (arguments.size() != 1) {
             throw new CmdLineException("Only one log file can be captured");
         }
         for (String source : arguments) {
             if (one_time) {
                 capture(source, 0.0);
             } else {
                 capture(source, cycle_time);
             }
         }
     }
 
     /**
      * Captures an extract of a log file, reformats it and uploads it. <p>If
      * seconds is zero, this is run once.</p> <p>If seconds is non-zero, this is
      * the delay for repeat scheduling. Since a push takes almost 1 second, the
      * intervals need to be fairly long. </p>
      *
      * @param source Log File to capture, reformat and push.
      * @param seconds Scheduling interval in seconds. 0.0 means one-time-only.
      */
     public void capture(String source, double seconds) {
         worker.setSource_filename(source);
         worker.setExtract_filename(global.getProperty("capture.extract_filename", "hrtrtf.txt"));
         worker.setCsv_filename(global.getProperty("capture.csv_filename", "hrtrtf.csv"));
         if (seconds == 0.0) {
             timer.schedule(worker, 2 * 1000);
         } else {
             long interval = (long) seconds * 1000;
             long current = Calendar.getInstance().get(Calendar.SECOND);
             timer.scheduleAtFixedRate(worker, (60 - current) * 1000, interval);
         }
     }
 
     /**
      * TimerTask used to handle cycling log capture process.
      */
     class Tail_Format_Push extends TimerTask {
 
         Timer timer;
         String source_filename;
         String extract_filename;
         String csv_filename;
 
         public void setCsv_filename(String csv_filename) {
             this.csv_filename = csv_filename;
         }
 
         public void setExtract_filename(String extract_filename) {
             this.extract_filename = extract_filename;
         }
 
         public void setSource_filename(String source_filename) {
             this.source_filename = source_filename;
         }
         LogTail tail = new LogTail(global);
         Reformat reformat = new Reformat(global);
         CouchPush push = new CouchPush(global);
 
         Tail_Format_Push() {
             super();
         }
 
         /**
          * Run the timer task. <p> This performs the standard three-step
          * capture. </p> <ol> <li>LogTail</li> <li>Reformat</li>
          * <li>CouchPush</li> </ol>
          */
         @Override
         public void run() {
             try {
                 String created = tail.tail(source_filename, extract_filename);
 
                 if (created == null) {
                     return;
                 }
 
                 Reader extract = new FileReader(new File(created));
                 File csv_file = new File(csv_filename);
                 reformat.include_header = csv_file.length() == 0;
                Writer wtr = new FileWriter(csv_file, true);
                 try {
                     Object[] details = {extract_filename, csv_filename};
                     logger.info(MessageFormat.format("Reformatting {0} to {1}", details));
 
                     reformat.reformat(extract, wtr);
                 } finally {
                     wtr.close();
                 }
 
                 logger.debug("About to push " + csv_filename);
                 push.open();
                 Document doc = push.push_feed(csv_file);
                 if (doc == null) {
                     logger.error("Couch Push Failed.");
                     cancel();
                 }
             } catch (Exception ex) {
                 logger.fatal("Worker Failed", ex);
                 cancel();
                 throw new Error(ex);
             }
         }
     }
 }
