 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  */
 
 package org.jenkinsci.plugins.darcs;
 
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Launcher.ProcStarter;
import hudson.model.TaskListener;
 import hudson.util.ArgumentListBuilder;
 
 import java.io.ByteArrayOutputStream;
 import java.util.Map;
 
 /**
  *
  * @author Sven Strittmatter <ich@weltraumschaf.de>
  */
 public class DarcsCmd {
 
     private final Launcher            launcher;
     private final String              darcsExe;
     private final Map<String, String> envs;
 
     public DarcsCmd(Launcher launcher, Map<String, String> envs) {
         this(launcher, envs, "darcs");
     }
 
     public DarcsCmd(Launcher launcher, Map<String, String> envs, String darcsExe) {
         this.envs     = envs;
         this.launcher = launcher;
         this.darcsExe = darcsExe;
     }
 
     public ProcStarter createProc(ArgumentListBuilder args) {
         ProcStarter proc = launcher.launch();
        proc.cmds(args);
         proc.envs(envs);
         
         return proc;
     }
 
     public ByteArrayOutputStream changes(FilePath repo, boolean xmlOutput, boolean summary, int last) throws DarcsCmdException {
         ArgumentListBuilder args = new ArgumentListBuilder();
         args.add(darcsExe)
             .add("changes")
             .add("--repodir=" + repo.toString());
 
         if (xmlOutput) {
             args.add("--xml-output");
         }
 
         if (summary) {
             args.add("--summary");
         }
 
         if (0 < last) {
             args.add(String.format("--last=%1", last));
         }
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ProcStarter proc = createProc(args);
         proc.stdout(baos);
 
         try {
             int ret = proc.join();
             
             if (0 != ret) {
                 throw new DarcsCmdException("can not do darcs changes in repo " + repo);
             }
         } catch (Exception $e) {
             throw new DarcsCmdException("can not do darcs changes in repo " + repo, $e);
         }
                 
         return baos;
     }
 
     public ByteArrayOutputStream changes(FilePath repo) throws DarcsCmdException {
         return changes(repo, true, true, 0);
     }
 
     public ByteArrayOutputStream changes(FilePath repo, int last) throws DarcsCmdException {
         return changes(repo, true, true, last);
     }
 
     public int countChanges(FilePath repo) throws DarcsCmdException {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         
         try {
             ArgumentListBuilder args = new ArgumentListBuilder();
             args.add(darcsExe)
                 .add("changes")
                 .add("--repodir=" + repo.toString())
                 .add("--count");
             ProcStarter proc = createProc(args);
             proc.stdout(baos);
             int ret = proc.join();
 
             if (0 != ret) {
                 throw new DarcsCmdException("can not do darcs changes in repo " + repo);
             }
         } catch (Exception $e) {
             throw new DarcsCmdException("can not do darcs changes in repo " + repo, $e);
         }
 
         return Integer.parseInt(baos.toString().trim());
     }
     
     public void pull(FilePath repo, String from) throws DarcsCmdException {
         ArgumentListBuilder args = new ArgumentListBuilder();
         args.add(darcsExe)
             .add("pull")
             .add(from)
             .add("--repodir=" + repo.toString())
             .add("--all");
         // todo use stdout as output buffer
         try {
             ProcStarter proc = createProc(args);
             int ret = proc.join();
 
             if (0 != ret) {
                 throw new DarcsCmdException("can not do darcs changes in repo " + repo);
             }
         } catch (Exception $e) {
             throw new DarcsCmdException("can not do darcs changes in repo " + repo, $e);
         }
     }
 
     public void get(String repo, String from) throws DarcsCmdException {
         ArgumentListBuilder args = new ArgumentListBuilder();
         args.add(darcsExe)
             .add("get")
             .add(from);
         // todo use stdout as output buffer
         try {
             ProcStarter proc = createProc(args);
            proc.pwd(repo)
                .stdout(this.launcher.getListener());
             int ret = proc.join();
 
             if (0 != ret) {
                 throw new DarcsCmdException("Getting repo with args " + args.toStringWithQuote() + " returne " + ret);
             }
         } catch (Exception $e) {
             throw new DarcsCmdException("Can not get repo with args: " + args.toStringWithQuote(), $e);
         }
     }
 }
