 package de.topicmapslab.tmql4j;
 
 import de.topicmapslab.tmql4j.common.core.runtime.TMQLRuntimeFactory;
 import de.topicmapslab.tmql4j.common.model.query.IQuery;
 import de.topicmapslab.tmql4j.common.model.runtime.ITMQLRuntime;
 import org.tmapi.core.TMAPIException;
 import org.tmapi.core.TopicMap;
 import org.tmapi.core.TopicMapSystem;
 import org.tmapi.core.TopicMapSystemFactory;
 import org.tmapix.io.CTMTopicMapReader;
 import org.tmapix.io.LTMTopicMapReader;
 import org.tmapix.io.TopicMapReader;
 import org.tmapix.io.XTMTopicMapReader;
 
 import java.io.*;
 
 /**
  * User: mhoyer
  * Date: 08.09.2010
  * Time: 12:07:01
  */
 public class QueryConsole {
     private String prefix;
     private TopicMap topicMap;
     private ITMQLRuntime runtime;
     private ResultInterpreter resultInterpreter;
     private TopicMapSystem topicMapSystem;
     private PrintStream output;
 
     public QueryConsole(PrintStream output, File topicMapFile) throws TMAPIException, IOException {
         this.output = output;
         resultInterpreter = new ResultInterpreter(output);
         prefix = topicMapFile.getName();
 
         importTopicMap(topicMapFile);
 
         runtime = TMQLRuntimeFactory.newFactory().newRuntime(topicMapSystem, topicMap);
     }
 
     private void importTopicMap(File topicMapFile) throws TMAPIException, IOException {
         output.print(String.format("Importing %s ...", topicMapFile.getName()));
         String fileName = topicMapFile.getName().toLowerCase();
 
         topicMapSystem = TopicMapSystemFactory.newInstance().newTopicMapSystem();
         topicMap = topicMapSystem.createTopicMap(topicMapFile.toURI().toString());
 
         TopicMapReader tmReader;
         if(fileName.endsWith(".ltm")) {
             tmReader = new LTMTopicMapReader(topicMap, topicMapFile);
         }
         else if(fileName.endsWith(".ctm")) {
             tmReader = new CTMTopicMapReader(topicMap, topicMapFile);
         }
         else tmReader = new XTMTopicMapReader(topicMap, topicMapFile);
 
         tmReader.read();
         output.println("done!\n");
     }
 
     public void open() throws IOException {
         output.println("Enter '?' for help.");
 
         InputStreamReader converter = new InputStreamReader(System.in);
         BufferedReader in = new BufferedReader(converter);
 
         String q = "";
         while(true)
         {
             output.print(String.format("%s %s ", prefix, q.isEmpty() ? ">" : "|"));
             
             String line = in.readLine();
 
             if (q.isEmpty()) {
                 String trimedLine = line.trim();
                 if (trimedLine.matches("(e|exit|q|uit)")) break;
                 if (trimedLine.matches("(\\?|h|help)")) { printCommands(); continue; }
                 if (trimedLine.matches("(s|stats)")) { printStats(); continue; }
             }
 
             q = q.concat(line);
             if (!line.trim().endsWith(";")) continue;
 
            q = q.substring(0, q.lastIndexOf(";"));
            runQuery(q);
 
             q = "";
         }
     }
 
     private void printStats() {
         output.println(String.format("  * Topics: %d\n  * Associations: %d\n", topicMap.getTopics().size(), topicMap.getAssociations().size()));
     }
 
     public void runQuery(String q)
     {
         output.println(String.format("Running query:\n%s", q));
         
         try {
             IQuery query = runtime.run(q);
             resultInterpreter.printResults(query);
         }
         catch (Exception ex)
         {
             output.println(ex.toString());
         }
     }
 
     public void printCommands()
     {
         output.println(String.format("%20s  %s", "h(elp)|?", "Shows this screen"));
         output.println(String.format("%20s  %s", "e(xit)|q(uit)", "Exits the console"));
         output.println(String.format("%20s  %s", "s(tats)", "Shows the statistics for loaded Topic Map"));
 
         output.println(String.format("\n%s",   "An entered query should be finalized with ; to execute it.\n"));
     }
 
 }
