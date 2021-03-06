 package net.sourceforge.seqware.pipeline.workflowV2.engine.oozie.object;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import net.sourceforge.seqware.pipeline.workflowV2.model.AbstractJob;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.jdom.Element;
 import org.jdom.Namespace;
 
 public class OozieJob {
   protected String okTo = "end";
   // private String errorTo; always to fail now
   protected String name;
   protected Collection<String> parentAccessions;
   protected String wfrAccession;
   protected boolean wfrAncesstor;
   protected AbstractJob jobObj;
   protected boolean metadataWriteback;
   protected List<OozieJob> parents;
   protected List<OozieJob> children;
   protected String oozie_working_dir;
   protected List<String> parentAccessionFiles;
   protected boolean useSge;
   protected String seqwareJarPath;
 
   public OozieJob(AbstractJob job, String name, String oozie_working_dir,
                   boolean useSge, File seqwareJar) {
     this.name = name;
     this.jobObj = job;
     this.oozie_working_dir = oozie_working_dir;
     this.parents = new ArrayList<OozieJob>();
     this.children = new ArrayList<OozieJob>();
     this.parentAccessionFiles = new ArrayList<String>();
     this.parentAccessions = new ArrayList<String>();
     this.useSge = useSge;
     if (useSge && seqwareJar == null) {
       throw new IllegalArgumentException(
                                          "seqwareJar must be specified when useSge is true.");
     }
     this.seqwareJarPath = seqwareJar.getAbsolutePath();
   }
 
   public final Element serializeXML() {
     Element element = new Element("action", WorkflowApp.NAMESPACE);
     element.setAttribute("name", this.name);
 
     if (useSge) {
       element.addContent(getSgeElement());
     } else {
       element.addContent(getJavaElement());
     }
 
     // okTo
     Element ok = new Element("ok", WorkflowApp.NAMESPACE);
     ok.setAttribute("to", this.okTo);
     element.addContent(ok);
 
     Element error = new Element("error", WorkflowApp.NAMESPACE);
     error.setAttribute("to", "fail");
     element.addContent(error);
 
     return element;
   }
 
   public String getName() {
     return this.name;
   }
 
   public String getOozieWorkingDir() {
     return oozie_working_dir;
   }
 
   /**
    * Creates and writes a Runner script to the working directory, then returns
    * the XML for an SGE action node that will invoke that script via qsub.
    * 
    * @return the SGE XML node
    */
   private Element getSgeElement() {
     File subdir = new File(oozie_working_dir, SGE_SCRIPTS_SUBDIR);
     if (!subdir.exists())
       subdir.mkdirs();
 
     File scriptFile = new File(subdir, name + ".sh");
     try {
       if (!scriptFile.createNewFile()) {
         throw new RuntimeException("Duplicate job name : " + name);
       }
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
     scriptFile.setReadable(true, false);
     scriptFile.setWritable(true, true);
     scriptFile.setExecutable(true, false);
 
     String scriptContents = createRunnerScript();
 
     FileWriter writer = null;
     try {
       writer = new FileWriter(scriptFile);
       writer.write(scriptContents);
     } catch (IOException e) {
       throw new RuntimeException(e);
     } finally {
       try {
         writer.close();
       } catch (Exception e) {
         // gulp
       }
     }
 
     File optionsFile = new File(subdir, name + ".opts");
     try {
       if (!optionsFile.createNewFile()) {
         throw new RuntimeException("Duplicate job name : " + name);
       }
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
     optionsFile.setReadable(true, false);
     optionsFile.setWritable(true, true);
     optionsFile.setExecutable(false);
 
     String optionsContents = createSgeOptionsArgs(subdir.getAbsolutePath());
 
     try {
       writer = new FileWriter(optionsFile);
       writer.write(optionsContents);
     } catch (IOException e) {
       throw new RuntimeException(e);
     } finally {
       try {
         writer.close();
       } catch (Exception e) {
         // gulp
       }
     }
     
     return createSgeAction(scriptFile.getAbsolutePath(), optionsFile.getAbsolutePath());
   }
 
   protected Element getJavaElement() {
 
     Element javaE = new Element("java", WorkflowApp.NAMESPACE);
     Element jobTracker = new Element("job-tracker", WorkflowApp.NAMESPACE);
     jobTracker.setText("${jobTracker}");
     javaE.addContent(jobTracker);
 
     Element nameNode = new Element("name-node", WorkflowApp.NAMESPACE);
     nameNode.setText("${nameNode}");
     javaE.addContent(nameNode);
 
     Element config = new Element("configuration", WorkflowApp.NAMESPACE);
     Element p0 = new Element("property", WorkflowApp.NAMESPACE);
     config.addContent(p0);
     Element name0 = new Element("name", WorkflowApp.NAMESPACE);
     name0.setText("mapred.job.queue.name");
     p0.addContent(name0);
     Element value0 = new Element("value", WorkflowApp.NAMESPACE);
     value0.setText("${queueName}");
     p0.addContent(value0);
 
     // map memory
     p0 = new Element("property", WorkflowApp.NAMESPACE);
     config.addContent(p0);
     name0 = new Element("name", WorkflowApp.NAMESPACE);
     name0.setText("oozie.launcher.mapred.job.map.memory.mb");
     p0.addContent(name0);
     value0 = new Element("value", WorkflowApp.NAMESPACE);
     value0.setText(this.getJobObject().getMaxMemory());
     p0.addContent(value0);
 
     p0 = new Element("property", WorkflowApp.NAMESPACE);
     config.addContent(p0);
     name0 = new Element("name", WorkflowApp.NAMESPACE);
     name0.setText("oozie.launcher.mapred.job.reduce.memory.mb");
     p0.addContent(name0);
     value0 = new Element("value", WorkflowApp.NAMESPACE);
     value0.setText(this.getJobObject().getMaxMemory());
     p0.addContent(value0);
 
     p0 = new Element("property", WorkflowApp.NAMESPACE);
     config.addContent(p0);
     name0 = new Element("name", WorkflowApp.NAMESPACE);
     name0.setText("oozie.launcher.mapreduce.map.memory.physical.mb");
     p0.addContent(name0);
     value0 = new Element("value", WorkflowApp.NAMESPACE);
     value0.setText(this.getJobObject().getMaxMemory());
     p0.addContent(value0);
 
     p0 = new Element("property", WorkflowApp.NAMESPACE);
     config.addContent(p0);
     name0 = new Element("name", WorkflowApp.NAMESPACE);
     name0.setText("oozie.launcher.mapreduce.reduce.memory.physical.mb");
     p0.addContent(name0);
     value0 = new Element("value", WorkflowApp.NAMESPACE);
     value0.setText(this.getJobObject().getMaxMemory());
     p0.addContent(value0);
 
     // add configuration
     javaE.addContent(config);
 
     Element mainClass = new Element("main-class", WorkflowApp.NAMESPACE);
     mainClass.setText("net.sourceforge.seqware.pipeline.runner.Runner");
     javaE.addContent(mainClass);
 
     this.buildMetadataString(javaE);
 
     Element arg1 = new Element("arg", WorkflowApp.NAMESPACE);
     arg1.setText("--module");
     javaE.addContent(arg1);
 
     Element argModule = new Element("arg", WorkflowApp.NAMESPACE);
     argModule.setText("net.sourceforge.seqware.pipeline.modules.GenericCommandRunner");
     javaE.addContent(argModule);
 
     Element dash = new Element("arg", WorkflowApp.NAMESPACE);
     dash.setText("--");
     javaE.addContent(dash);
 
     Element algo = new Element("arg", WorkflowApp.NAMESPACE);
     algo.setText("--gcr-algorithm");
     javaE.addContent(algo);
 
     Element algoValue = new Element("arg", WorkflowApp.NAMESPACE);
     algoValue.setText(this.jobObj.getAlgo());
     javaE.addContent(algoValue);
 
     Element command = new Element("arg", WorkflowApp.NAMESPACE);
     command.setText("--gcr-command");
     javaE.addContent(command);
 
     // add cd command for every gcr job
     Element cdE = new Element("arg", WorkflowApp.NAMESPACE);
     cdE.setText("cd " + this.oozie_working_dir + "; ");
     javaE.addContent(cdE);
 
     for (String arg : this.jobObj.getCommand().getArguments()) {
       Element cmdArg = new Element("arg", WorkflowApp.NAMESPACE);
       cmdArg.setText(arg);
       javaE.addContent(cmdArg);
     }
 
     return javaE;
   }
 
   public void setParentAccessions(Collection<String> parentAccessions) {
     this.parentAccessions.addAll(parentAccessions);
   }
 
   public boolean hasMetadataWriteback() {
     return metadataWriteback;
   }
 
   public void setMetadataWriteback(boolean metadataWriteback) {
     this.metadataWriteback = metadataWriteback;
   }
 
   public String getWorkflowRunAccession() {
     return wfrAccession;
   }
 
   public void setWorkflowRunAccession(String wfrAccession) {
     this.wfrAccession = wfrAccession;
   }
 
   public boolean isWorkflowRunAncesstor() {
     return wfrAncesstor;
   }
 
   public void setWorkflowRunAncesstor(boolean wfrAncesstor) {
     this.wfrAncesstor = wfrAncesstor;
   }
 
   public void addParent(OozieJob parent) {
     if (!this.parents.contains(parent))
       this.parents.add(parent);
     if (!parent.getChildren().contains(this))
       parent.getChildren().add(this);
   }
 
   public Collection<OozieJob> getParents() {
     return this.parents;
   }
 
   public Collection<OozieJob> getChildren() {
     return this.children;
   }
 
   public void setOkTo(String okTo) {
     this.okTo = okTo;
   }
 
   public String getOkTo() {
     return this.okTo;
   }
 
   public boolean hasFork() {
     return this.getChildren().size() > 1;
   }
 
   public boolean hasJoin() {
     return this.getParents().size() > 1;
   }
 
   public AbstractJob getJobObject() {
     return this.jobObj;
   }
 
   @Override
   public boolean equals(Object obj) {
     if (obj == null || obj instanceof OozieJob == false)
       return false;
     if (obj == this)
       return true;
     OozieJob rhs = (OozieJob) obj;
     return new EqualsBuilder().appendSuper(super.equals(obj)).append(name,
                                                                      rhs.name).isEquals();
   }
 
   @Override
   public int hashCode() {
     return new HashCodeBuilder(17, 37).append(name).toHashCode();
   }
 
   @Override
   public String toString() {
     return this.name;
   }
 
   protected void buildMetadataString(Element javaElement) {
     Element metaArg = new Element("arg", WorkflowApp.NAMESPACE);
     if (this.hasMetadataWriteback()) {
       metaArg.setText("--metadata");
     } else {
       metaArg.setText("--no-metadata");
     }
     javaElement.addContent(metaArg);
 
     if (this.parentAccessions != null && !this.parentAccessions.isEmpty()) {
       for (String pa : this.parentAccessions) {
         Element paArg = new Element("arg", WorkflowApp.NAMESPACE);
         paArg.setText("--metadata-parent-accession");
         javaElement.addContent(paArg);
 
         Element paVal = new Element("arg", WorkflowApp.NAMESPACE);
         paVal.setText(pa);
         javaElement.addContent(paVal);
       }
     }
 
     if (this.wfrAccession != null) {
       Element wfraArg = new Element("arg", WorkflowApp.NAMESPACE);
       Element wfraVal = new Element("arg", WorkflowApp.NAMESPACE);
       if (!this.wfrAncesstor) {
         wfraArg.setText("--metadata-workflow-run-ancestor-accession");
       } else {
         wfraArg.setText("--metadata-workflow-run-accession");
       }
       wfraVal.setText(this.wfrAccession);
       javaElement.addContent(wfraArg);
       javaElement.addContent(wfraVal);
     }
 
     if (!this.parentAccessionFiles.isEmpty()) {
       for (String paf : this.parentAccessionFiles) {
         Element pafArg = new Element("arg", WorkflowApp.NAMESPACE);
         pafArg.setText("--metadata-parent-accession-file");
         Element pafVal = new Element("arg", WorkflowApp.NAMESPACE);
         pafVal.setText(paf);
         javaElement.addContent(pafArg);
         javaElement.addContent(pafVal);
       }
     }
     Element pafArg = new Element("arg", WorkflowApp.NAMESPACE);
     pafArg.setText("--metadata-processing-accession-file");
 
     Element pafVal = new Element("arg", WorkflowApp.NAMESPACE);
     pafVal.setText(this.getAccessionFile());
     javaElement.addContent(pafArg);
     javaElement.addContent(pafVal);
   }
 
   public void addParentAccessionFile(String paf) {
     if (!this.parentAccessionFiles.contains(paf))
       this.parentAccessionFiles.add(paf);
   }
 
   public String getAccessionFile() {
     return this.oozie_working_dir + "/" + this.getName() + "_accession";
   }
 
   /**
    * Returns the metadata arg list for the Runner.
    * 
    * @param job
    *          the job to run
    * @return Runner args
    */
   public static ArrayList<String> metaDataArgs(OozieJob job) {
     return metaDataArgs(job.metadataWriteback, job.getAccessionFile(),
                         job.parentAccessions, job.parentAccessionFiles,
                         job.wfrAccession, job.wfrAncesstor);
   }
 
   /**
    * Returns the metadata arg list for the Runner.
    * 
    * @return Runner args
    */
   public static ArrayList<String> metaDataArgs(boolean metadataWriteback,
                                                String accessionFile,
                                                Collection<String> parentAccessions,
                                                Collection<String> parentAccessionFiles,
                                                String workflowRunAccession,
                                                boolean workflowRunAncestor) {
     ArrayList<String> args = new ArrayList<String>();
 
     if (metadataWriteback) {
       args.add("--metadata");
     } else {
       args.add("--no-metadata");
     }
 
     if (parentAccessions != null) {
       for (String pa : parentAccessions) {
         args.add("--metadata-parent-accession");
         args.add(pa);
       }
     }
 
     if (parentAccessionFiles != null) {
       for (String paf : parentAccessionFiles) {
         args.add("--metadata-parent-accession-file");
         args.add(paf);
       }
     }
 
     if (workflowRunAccession != null) {
       if (workflowRunAncestor) {
         args.add("--metadata-workflow-run-accession");
       } else {
         args.add("--metadata-workflow-run-ancestor-accession");
       }
       args.add(workflowRunAccession);
     }
 
     args.add("--metadata-processing-accession-file");
     args.add(accessionFile);
 
     return args;
   }
 
   /**
    * Returns the GenericCommandRunner module arg list for the Runner.
    * 
    * @param job
    *          the job to run
    * @return Runner args
    */
   public static ArrayList<String> genericRunnerArgs(OozieJob job) {
     return genericRunnerArgs(job.name, job.oozie_working_dir,
                              job.getJobObject().getCommand().getArguments());
   }
 
   /**
    * Returns the GenericCommandRunner module arg list for the Runner.
    * 
    * @param jobName
    *          name of the job
    * @param workingDirectory
    *          working directory of the job
    * @param commands
    *          the list of bash commands
    * @return Runner args
    */
   public static ArrayList<String> genericRunnerArgs(String jobName,
                                                     String workingDirectory,
                                                     List<String> commands) {
     ArrayList<String> args = new ArrayList<String>();
 
     args.add("--module");
     args.add("net.sourceforge.seqware.pipeline.modules.GenericCommandRunner");
 
     args.add("--");
 
     args.add("--gcr-algorithm");
     args.add(jobName);
 
     StringBuilder sb = new StringBuilder();
     sb.append("cd ");
     sb.append(workingDirectory);
     sb.append("; ");
     for (String cmd : commands) {
       sb.append(cmd);
     }
 
     args.add("--gcr-command");
     args.add(sb.toString());
 
     return args;
   }
 
   /**
    * Creates the list of arguments to the Runner. Override to provide an
    * alternative list of values.
    * 
    * @return Runner args
    */
   public ArrayList<String> runnerArgs() {
     ArrayList<String> args = metaDataArgs(this);
     args.addAll(genericRunnerArgs(this));
     return args;
   }
 
   /**
    * Creates the script that will invoke the Runner for this job.
    * 
    * @return the script
    */
   public final String createRunnerScript() {
 
     ArrayList<String> list = new ArrayList<String>();
     list.add("java");
     list.add("-classpath");
     list.add(seqwareJarPath);
     list.add("net.sourceforge.seqware.pipeline.runner.Runner");
 
     list.addAll(runnerArgs());
 
     StringBuilder sb = new StringBuilder("#!/usr/bin/env bash\n\n");
     for (String s : list) {
       sb.append(s);
       sb.append(" ");
     }
 
     return sb.toString();
   }
   
   public final String createSgeOptionsArgs(String genScriptsDir){
     StringBuilder sb = new StringBuilder();
     sb.append("-b y ");
     sb.append(" -e ");
     sb.append(genScriptsDir);
     sb.append(" -o ");
     sb.append(genScriptsDir);
     sb.append(" -N ");
     sb.append(jobObj.getAlgo());
     
     if (StringUtils.isNotBlank(jobObj.getQueue())){
       sb.append(" -q ");
       sb.append(jobObj.getQueue());
     }
 
     // TODO: The following options are system-specific, thus we need a way to configure them
 
     if (StringUtils.isNotBlank(jobObj.getMaxMemory())){
      sb.append(" -l h_vmem=");
       sb.append(jobObj.getMaxMemory());
     }
     
     if (jobObj.getThreads() > 0){
      sb.append(" -l slots=");
       sb.append(jobObj.getThreads());
     }
     
     return sb.toString();
   }
 
   /**
    * The sub-directory (of the working directory) in which generated script
    * files will be placed.
    */
   public static final String SGE_SCRIPTS_SUBDIR = "generated-sge-job-scripts";
 
   public static final Namespace SGE_XMLNS = Namespace.getNamespace("uri:oozie:sge-action:1.0");
 
   /**
    * Creates the sge XML action node.
    * 
    * @param scriptFileName
    *          the name of the file passed to qsub
    * @param workingDirectory
    *          the working directory passed to qsub
    * @return the sge node
    */
   public final Element createSgeAction(String scriptFileName,
                                        String optionsFileName) {
     Element sge = new Element("sge", SGE_XMLNS);
 
     Element script = new Element("script", SGE_XMLNS);
     script.setText(scriptFileName);
     sge.addContent(script);
 
     Element opts = new Element("options-file", SGE_XMLNS);
     opts.setText(optionsFileName);
     sge.addContent(opts);
 
     return sge;
   }
 
 }
