 
 package song.chandler;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 
 import sun.jvm.hotspot.memory.StringTable;
 import sun.jvm.hotspot.memory.SystemDictionary;
 import sun.jvm.hotspot.oops.Instance;
 import sun.jvm.hotspot.oops.InstanceKlass;
 import sun.jvm.hotspot.oops.Klass;
 import sun.jvm.hotspot.oops.Oop;
 import sun.jvm.hotspot.oops.OopField;
 import sun.jvm.hotspot.oops.TypeArray;
 import sun.jvm.hotspot.runtime.VM;
 import sun.jvm.hotspot.tools.Tool;
 
 /**
  * thanks for puneet<br>
  * base on https://github.com/puneetlakhina/javautils/blob/master/com/blogspot/sahyog/PrintStringTable.java if this can't work. update the
  * sa-jdi.jar in lib, which you can find in $java_home$/lib. <br>
  * Please use the lib which is the same as your java version, current version is <br>
  * java version "1.7.0_09" <br>
  * Java(TM) SE Runtime Environment (build 1.7.0_09-b05) <br>
  * Java HotSpot(TM) Client VM (build 23.5-b02, mixed mode, sharing) <br>
  * 
  * @author chandler.song
  */
 public class PrintPermGen extends Tool {
 
     public static String LineSperator = System.getProperty("line.separator");
 
     private static Logger logger = Logger.getLogger(PrintPermGen.class);
 
     private StringPrinter stringPrinter;
 
     private ObjectPrinter objectPrinter;
 
     private File summaryFile;
 
     private File reportFolder;
 
     public PrintPermGen() throws IOException {
         // create report folder path and summaryFile
         File rootReportFolder = new File("report");
 
         if (!rootReportFolder.exists()) {
             rootReportFolder.mkdirs();
         }
 
         String reportFolderPath = "report/" + (rootReportFolder.listFiles().length + 1);
 
         reportFolder = new File(reportFolderPath);
         reportFolder.mkdirs();
         logger.info("report folder:" + reportFolder.getAbsolutePath());
         summaryFile = new File(reportFolder, "Summary.txt");
         summaryFile.createNewFile();
 
     }
 
     public static void main(String args[]) throws Exception {
         if (args.length == 0 || args.length > 1) {
 
             try {
                 // for debug convenient,if you run in IDE, just comment the exception, don't need to modify others
                 args = new String[] {
                     "2460"
                 };
                // throw new Exception();
             } catch (Exception e) {
                 System.err
                         .println("Usage: java com.blogspot.sahyog.PrintStringTable <PID of the JVM whose string table you want to print>");
                 System.exit(1);
             }
 
         }
 
         ;
         PrintPermGen pst = new PrintPermGen();
         pst.start(args);
 
         StringPrinter sp = pst.getStringPrinter();
         ObjectPrinter op = pst.getObjectPrinter();
         sp.print();
         op.print();
         logger.info("analysis finished");
         pst.stop();
 
     }
 
     public void run() {
 
         try {
             SystemDictionary dict = VM.getVM().getSystemDictionary();
             if (objectPrinter == null) {
                 objectPrinter = new ObjectPrinter(this.reportFolder, this.summaryFile);
             }
 
             dict.classesDo(objectPrinter);
 
             StringTable table = VM.getVM().getStringTable();
 
             if (stringPrinter == null) {
                 stringPrinter = new StringPrinter(this.reportFolder, this.summaryFile);
             }
 
             table.stringsDo(stringPrinter);
         } catch (Exception e) {
             logger.error("error happen", e);
         }
     }
 
     public StringPrinter getStringPrinter() {
         return stringPrinter;
     }
 
     public ObjectPrinter getObjectPrinter() {
         return objectPrinter;
     }
 
 }
 
 class StringPrinter implements StringTable.StringVisitor {
     private OopField stringValueField;
 
     private int totalCount;
 
     private long totalsize;
 
     private File stringFile;
 
     private File detailFile;
 
     private File summaryFile;
 
     public StringPrinter(File reportFolder, File summaryFile) throws IOException {
         // VM vm = VM.getVM();
         // SystemDictionary sysDict = vm.getSystemDictionary();
         InstanceKlass strKlass = SystemDictionary.getStringKlass();
         stringValueField = (OopField)strKlass.findField("value", "[C");
 
         // create file
         this.summaryFile = summaryFile;
         detailFile = new File(reportFolder, "StringDetails.txt");
         detailFile.createNewFile();
         stringFile = new File(reportFolder, "Stringlist.txt");
         stringFile.createNewFile();
     }
 
     public void visit(Instance instance) {
         TypeArray charArray = ((TypeArray)stringValueField.getValue(instance));
         StringBuilder sb = new StringBuilder();
         for (long i = 0; i < charArray.getLength(); i++) {
             sb.append(charArray.getCharAt(i));
         }
 
         long stringSize = this.stringSize(instance);
 
         totalCount++;
         totalsize = totalsize + stringSize;
 
         String content = sb.toString().replaceAll("\r\n", "").replaceAll("\r", "");
         try {
             FileUtils.write(stringFile, content + PrintPermGen.LineSperator, true);
             FileUtils.write(detailFile, stringSize + "," + content + PrintPermGen.LineSperator, true);
         } catch (IOException e) {
             e.printStackTrace();
         }
         /**
          * logger.info("size, " + stringSize + ",Content, " + sb.toString().replaceAll("\r\n", "").replaceAll("\r", "").replaceAll("\n", "")
          * + ",Address, " + instance.getHandle());
          */
     }
 
     private long stringSize(Instance instance) {
         // We include String content in size calculation.
         return instance.getObjectSize() + stringValueField.getValue(instance).getObjectSize();
     }
 
     public void print() throws IOException {
         FileUtils.write(summaryFile, totalCount + " intern Strings occupying " + totalsize + " bytes."
                 + PrintPermGen.LineSperator, true);
     }
 }
 
 class ObjectPrinter implements SystemDictionary.ClassAndLoaderVisitor {
 
     private File summaryFile;
 
     private File detailFile;
 
     private Long classCount = 0L;
 
     private Map<String, Integer> loaders = new HashMap<String, Integer>();
 
     public ObjectPrinter(File reportFolder, File summaryFile) throws IOException {
 
         this.summaryFile = summaryFile;
 
         this.detailFile = new File(reportFolder, "object.txt");
         detailFile.createNewFile();
 
     }
 
     public void visit(Klass k, Oop loader) {
         if (!(k instanceof InstanceKlass)) {
             return;
         }
 
         String loaderName = "boots";
         if (loader != null) {
             loaderName = loader.getKlass().getName().asString();
         }
 
         if (!loaders.keySet().contains(loaderName)) {
             loaders.put(loaderName, 1);
         } else {
             int classes = loaders.get(loaderName);
             loaders.put(loaderName, ++classes);
         }
 
         try {
            FileUtils.write(detailFile, k.getName().asString() + "," + loaderName + PrintPermGen.LineSperator, true);
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         classCount++;
 
     }
 
     public void print() throws IOException {
         FileUtils.write(summaryFile, classCount + " classes loaded by " + loaders.size() + " loaders."
                 + PrintPermGen.LineSperator, true);
 
         Iterator<Entry<String, Integer>> iter = loaders.entrySet().iterator();
 
         while (iter.hasNext()) {
             Entry<String, Integer> e = iter.next();
             FileUtils.write(summaryFile, "loader:" + e.getKey() + ",load Class:" + e.getValue()
                     + PrintPermGen.LineSperator, true);
         }
 
     }
 
 }
