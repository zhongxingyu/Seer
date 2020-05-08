 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package builder.smartfrog;
 
 import builder.smartfrog.util.LineFilterOutputStream;
 import hudson.Launcher;
 import hudson.Proc;
 import hudson.model.Action;
 import hudson.model.Build;
 import hudson.model.JDK;
 import hudson.model.LargeText;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintStream;
 import java.util.Map;
 import java.util.Vector;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 /**
  *
  * @author dominik
  */
 public class SmartFrogAction implements Action, Runnable {
 
    private static final String nl = System.getProperty("line.separator");
     
    private String host;
    private State state = State.STARTING;
    private Build<?,?> build;
    
    private transient SmartFrogBuilder builder;
    private transient Proc proc;
    private transient Thread execThread;
    private transient Vector<SmartFrogActionListener> listeners = new Vector<SmartFrogActionListener>();
    private transient Launcher launcher;
    private transient PrintStream log;
    
    public enum State { STARTING, RUNNING, FINISHED, FAILED };
    
    private class SFFilterOutputStream extends LineFilterOutputStream {
 
       private OutputStreamWriter os;
       
       public SFFilterOutputStream(OutputStream out) {
          super(out);
          os = new OutputStreamWriter(out);
       }
       protected void writeLine(String line) {
          
          if (line.startsWith("SmartFrog ready")) setState(State.RUNNING);
          
          int idx = line.indexOf("[TerminateHook]");
          if (idx > -1) {
             String compName = line.substring(line.indexOf('[', idx + 15) + 1);
             compName = compName.substring(0, compName.indexOf(']') );
             if (compName.endsWith(builder.getScriptName())) {
                builder.componentTerminated(! line.contains("ABNORMAL"));
             }
          }
          
          try {
             os.write(line);
             os.write(nl);
             os.flush();
          } catch (IOException ioe) {
             
          }
       }
       
    }
    
    public SmartFrogAction(SmartFrogBuilder builder, String host) {
       this.builder = builder;
       this.host = host;                
    }
    
    public void perform(final Build<?, ?> build, final Launcher launcher) {
       this.build = build;
       this.launcher = launcher;      
       
       String[] cl = builder.buildDaemonCommandLine(host);      
       
       Map<String,String> env = build.getEnvVars();
       //JDK jdk = build.getProject().getJDK();
       //if(jdk !=null) jdk.buildEnvVars(env);
             
       try {         
          log = new PrintStream(new SFFilterOutputStream(new FileOutputStream(getLogFile())));
          proc = launcher.launch(cl, env, log, build.getParent().getWorkspace());
          execThread = new Thread(this, "SFDaemon - " + host);
          execThread.start();
       } catch (IOException ioe) {
          
       }
       
    }
 
    public String getIconFileName() {
       return "/plugin/org.jboss.hudson.smartfrog/icons/smartfrog24.png";
    }
 
    public String getDisplayName() {
       return "sfDaemon - " + host;
    }
 
    public String getUrlName() {
       return "console-" + host;
    }
 
    public Build<?,?> getOwnerBuild() {
        return build;
    }
    
    public boolean isBuilding() {
       return (state != State.FAILED) && (state != State.FINISHED);
    }
 
     public String getHost() {
         return host;
     }
    
     public File getLogFile() {
         return new File(build.getRootDir(), host + ".log");
     }
  
     /**
      * Handles incremental log output.
      */
     public void doProgressiveLog( StaplerRequest req, StaplerResponse rsp) throws IOException {
         new LargeText(getLogFile(),!isBuilding()).doProgressText(req,rsp);
     }
     
     private void setState(State s) {
        if (this.getState() == s) return;
        this.state = s;
        for (SmartFrogActionListener l : listeners) l.stateChanged(this,getState());
     }
     
     public void addStateListener(SmartFrogActionListener l) {
        listeners.add(l);
     }
 
     public void removeStateListener(SmartFrogActionListener l) {
        listeners.remove(l);
     }
 
    public State getState() {
       return state;
    }
 
    public PrintStream getLogAsText() {
        return log;
    }
 
   public void doLogAsPlainText(StaplerRequest req, StaplerResponse res) throws IOException, ServletException {
       StringBuffer contents = new StringBuffer();
 BufferedReader input = null;
      try {
         input =  new BufferedReader(new FileReader(this.getLogFile()));
        String line = null;

        while (( line = input.readLine()) != null){
          contents.append(line);
          contents.append(System.getProperty("line.separator"));
        }

      }
      catch (Exception e) {
          //return e.toString();
      }
      finally {
        //input.close();
      }

  req.setAttribute("output", contents);
 RequestDispatcher rd = req.getView(this, "consoleText.jelly");
 rd.forward(req, res);
  
//return contents.toString();
    }

    public void run() {
 
       // wait for proccess to finish
       try
       {
          proc.join();
       } catch (IOException ex) {
          setState(State.FAILED);
          return;
       } catch (InterruptedException ex) {
          setState(State.FAILED);
          return;
       }
       setState(State.FAILED);
       
    }
    
    public void interrupt() {
       String[] cl = builder.buildStopDaemonCommandLine(host);      
       try {
          launcher.launch(cl, build.getEnvVars(), log, build.getParent().getWorkspace()).join();
          execThread = new Thread(this, "SFDaemon - " + host);
          execThread.start();
       } catch (IOException ioe) {         
       } catch (InterruptedException ioe) {
           
       }
       /*
       try {
          proc.kill();
       } catch (IOException ex) {
       } catch (InterruptedException ex) {
       }
        */
    }
       
    
 }
