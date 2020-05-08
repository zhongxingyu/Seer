 package tira;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public abstract class AProgram {
     
     protected JSONObject system = new JSONObject();
     
     public abstract JSONObject getProgramRecord();
     
     public abstract JSONObject getDefaultConfig();
     
     public abstract String getInfo();
     
     public abstract String[] createRuns(JSONObject runConfig);
     
     public abstract JSONArray readRuns(JSONObject runConfig);
     
     public abstract void updateRun(String runId, JSONObject update);
     
     public abstract void deleteRuns(JSONObject runConfig);
     
     public abstract JSONObject takeRun(String runId);
     
     public void execute(String runId) throws JSONException, IOException, InterruptedException
     {
         JSONObject run = takeRun(runId);
         if(run==null){return;}
         String cmd = Util.substitute(run.getString(Util.MAIN), run);
         cmd = Util.substitute(cmd, system);
         System.out.println("Execute: "+cmd);
         String runDir = system.getString(Util.DATA)+"/"+run.getString(Util.ID);
         //write script to working dir.
         File scriptFile = writeCommand(cmd, runDir);
         //start script.
         JSONObject files = new JSONObject();
         files.put("Data Directory", (new File(runDir)).getAbsolutePath());
         if(system.has(Util.NODE))
         {   files.put("Data Directory", files.getString("Data Directory")
                 .replace(system.getString(Util.DATAROOT),system.getString(Util.NODE)+"data")
                 .replace("\\","/"));
         }
         updateRun(runId, files); files = new JSONObject();
         int exitCode = call(scriptFile,runId);
         if(exitCode!=0) {updateRun(runId, files.put(Util.STATE, Util.ERROR));}
         else {updateRun(runId, files.put(Util.STATE, Util.DONE));}
     }
     
     private File  writeCommand(String cmd, String dir) throws FileNotFoundException {
         File cmdDir = new File(dir); cmdDir.mkdirs();
         File scriptFile = new File(cmdDir,"run.bat");
         PrintWriter pw = new PrintWriter(scriptFile);
         pw.print(cmd); pw.close();
         scriptFile.setExecutable(true);
         return scriptFile;
     }
     
     private int call(File scriptFile, String runId) throws IOException, InterruptedException, JSONException
     {
         ProcessBuilder pb = new ProcessBuilder(scriptFile.getAbsolutePath());
         File pwd = scriptFile.getParentFile();
         pb.directory(pwd);
         //TODO: write log entries directly to the database via update().
         //logger.write("[START] "+ Util.dateFormat.format(new Date())+"\n");
         final Process p = pb.start();
         
         //read standard out.
         new Thread(new Runnable() {
             public void run() {
                 BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                 String line;
                 try {
                     while((line=br.readLine())!=null)
                     {System.out.println(line);}
                     br.close();
                 }
                 catch (IOException e) {e.printStackTrace();}
             }
         }).start();
         
         //read error out.
         BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
         String line;
         while((line=br.readLine())!=null)
         {
             System.err.println(line);
             line.trim();
             if(line.startsWith("{") && line.endsWith("}"))//assume JSONObject-string.
             {
                 try {updateRun(runId,new JSONObject(line));
                     
                 }catch (JSONException e) {}
             }            
         }
         br.close();
         p.getOutputStream().close();
         return p.waitFor();
     }
      
 
 }
