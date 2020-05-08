 /*
  * Copyright luntsys (c) 2004-2005,
  * Date: 2004-4-26
  * Time: 11:38:40
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: 1.
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 
 package com.luntsys.luntbuild.vcs;
 
 import com.luntsys.luntbuild.ant.Commandline;
 import com.luntsys.luntbuild.ant.cvstask.CVSPass;
 import com.luntsys.luntbuild.ant.cvstask.Cvs;
 import com.luntsys.luntbuild.db.Build;
 import com.luntsys.luntbuild.db.Schedule;
 import com.luntsys.luntbuild.facades.Constants;
 import com.luntsys.luntbuild.facades.lb12.CvsAdaptorFacade;
 import com.luntsys.luntbuild.facades.lb12.CvsModuleFacade;
 import com.luntsys.luntbuild.facades.lb12.ModuleFacade;
 import com.luntsys.luntbuild.facades.lb12.VcsFacade;
 import com.luntsys.luntbuild.utility.*;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.tapestry.form.IPropertySelectionModel;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.taskdefs.FixCRLF;
 import org.apache.tools.ant.types.Environment;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * CVS VCS adaptor implementation.
  * 
  * <p>This adaptor is safe for remote hosts.</p>
  *
  * @author robin shine
  */
 public class CvsAdaptor extends Vcs {
 	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
 	/**
 	 * Keep tracks of version of this class, used when do serialization-deserialization
 	 */
 	static final long serialVersionUID = 1;
 	private static final String passwdFileName = ".cvspass";
 	private String cvsRoot;
 	private String cvsPassword;
 	/**
 	 * Determines if the cvs executable being used is a cygwin one?
 	 */
 	private String cygwinCvs;
 
 	/**
 	 * Determines if the -S option for log command should be disabled
 	 */
 	private String disableSuppressOption;
 
 	/**
 	 * Determines if the history command should be disabled when check revisions
 	 */
 	private String disableHistoryCmd;
 
 	private String cvsDir;
 
 	/** CVS web interface to itegrate with */
 	private String webInterface;
 
 	/** CVS web interface URL */
 	private String webUrl;
 
     /**
      * Constructor, creates a CVS adaptor with default settings.
      */
 	public CvsAdaptor() {
 		setQuietPeriod("60");
 	}
 
 	/**
 	 * Sets the CVS root of the repository.
 	 *
 	 * @param cvsRoot the CVS root, an example can be ":pserver:anoncvs@cvs.pmease.com:/home/cvspublic"
 	 */
 	public void setCvsRoot(String cvsRoot) {
 		this.cvsRoot = cvsRoot;
 	}
 
 	/**
 	 * Gets the CVS root of the repository.
 	 * 
 	 * @return the CVS root
 	 */
 	public String getCvsRoot() {
 		return cvsRoot;
 	}
 
 	/**
 	 * Gets the CVS root of the repository. This method will parse OGNL variables.
 	 * 
 	 * @return the CVS root
 	 */
 	public String getActualCvsRoot() {
 		return OgnlHelper.evaluateScheduleValue(getCvsRoot());
 	}
 
 	/**
 	 * Gets the path to the CVS executable.
 	 * 
 	 * @return the path to the CVS executable
 	 */
 	public String getCvsDir() {
 		return cvsDir;
 	}
 
 	/**
 	 * Sets the path to the CVS executable.
 	 * 
 	 * @param cvsDir the path to the CVS executable
 	 */
 	public void setCvsDir(String cvsDir) {
 		this.cvsDir = cvsDir;
 	}
 
 	/**
 	 * Sets the CVS password for the repository.
 	 *
 	 * @param cvsPassword the password
 	 */
 	public void setCvsPassword(String cvsPassword) {
 		this.cvsPassword = cvsPassword;
 	}
 
 	/**
 	 * Gets the CVS password for the repository.
 	 *
 	 * @return the password
 	 */
 	public String getCvsPassword() {
 		return cvsPassword;
 	}
 
 	/**
 	 * Gets the is cygwin CVS property.
 	 * This property indicates whether or not ("yes" or "no") the CVS executable to use is the cygwin one.
 	 * 
 	 * @return the is cygwin CVS property
 	 */
 	public String getCygwinCvs() {
 		return cygwinCvs;
 	}
 
 	/**
 	 * Checks if the CVS executable is the cygwin one.
 	 * 
 	 * @return <code>true</code> if the CVS executable is the cygwin one
 	 */
 	public boolean isCygwinCvs() {
 		return !Luntbuild.isEmpty(getCygwinCvs()) && getCygwinCvs().equalsIgnoreCase("yes");
 	}
 
 	/**
 	 * Sets the is cygwin CVS property.
 	 * This property indicates whether or not ("yes" or "no") the CVS executable to use is the cygwin one.
 	 * 
 	 * @param cygwinCvs the is cygwin CVS property
 	 */
 	public void setCygwinCvs(String cygwinCvs) {
 		this.cygwinCvs = cygwinCvs;
 	}
 
 	/**
 	 * Gets the disable suppress property.
 	 * This property indicates whether or not ("yes" or "no") to disable -S option for log command.
 	 *
 	 * @return the disable suppress property
 	 */
 	public String getDisableSuppressOption() {
 		return disableSuppressOption;
 	}
 
 	/**
 	 * Sets the disable suppress property.
 	 * This property indicates whether or not ("yes" or "no") to disable -S option for log command.
 	 *
 	 * @param disableSuppressOption the disable suppress property
 	 */
 	public void setDisableSuppressOption(String disableSuppressOption) {
 		this.disableSuppressOption = disableSuppressOption;
 	}
 
 	/**
 	 * Checks if the -S option should be suppressed for the log command.
 	 * 
 	 * @return <code>true</code> if the -S option should be suppressed
 	 */
 	public boolean isDisableSuppressOption() {
 		return !Luntbuild.isEmpty(getDisableSuppressOption()) && getDisableSuppressOption().equalsIgnoreCase("yes");
 	}
 
 	/**
 	 * Gets the disable history command property.
 	 * This property indicates whether or not ("yes" or "no") to disable history command when checking revisions.
 	 *
 	 * @return the disable history command property
 	 */
 	public String getDisableHistoryCmd() {
 		return disableHistoryCmd;
 	}
 
 	/**
 	 * Sets the disable history command property.
 	 * This property indicates whether or not ("yes" or "no") to disable history command when checking revisions.
 	 *
 	 * @param disableHistoryCmd the disable history command property
 	 */
 	public void setDisableHistoryCmd(String disableHistoryCmd) {
 		this.disableHistoryCmd = disableHistoryCmd;
 	}
 
 	/**
 	 * Checks if the history command should be disabled.
 	 * 
 	 * @return <code>true</code> if the history command should be disabled
 	 */
 	public boolean isDisableHistoryCmd() {
 		return !Luntbuild.isEmpty(getDisableHistoryCmd()) && getDisableHistoryCmd().equalsIgnoreCase("yes");
 	}
 
 	/**
 	 * Gets the web interface to integrate with.
 	 * 
 	 * @return the web interface to integrate with
 	 */
 	public String getWebInterface() {
 		return webInterface;
 	}
 
 	/**
 	 * Sets the web interface to integrate with.
 	 * 
 	 * @param webInterface the web interface to integrate with
 	 */
 	public void setWebInterface(String webInterface) {
 		this.webInterface = webInterface;
 	}
 
 	/**
 	 * Gets the web interface URL.
 	 * 
 	 * @return the web interface URL
 	 */
 	public String getWebUrl() {
 		return webUrl;
 	}
 
 	/**
 	 * Sets the web interface URL.
 	 * 
 	 * @param webUrl the web interface URL
 	 */
 	public void setWebUrl(String webUrl) {
 		this.webUrl = webUrl;
 	}
 
     /**
 	 * @inheritDoc
      */
 	public String getDisplayName() {
 		return "Cvs";
 	}
 
     /**
 	 * @inheritDoc
      */
 	public String getIconName() {
 		return "cvs.jpg";
 	}
 
 	/**
 	 * Logs in to the CVS server.
 	 *
 	 * @param antProject the ant project used for logging
 	 * @throws BuildException if unable to read or find the CVS password file
 	 */
 	private void login(Project antProject) {
 		// call ant CVSPass task to login into cvs server
 		antProject.log("Login to cvs...", Project.MSG_INFO);
 
 		com.luntsys.luntbuild.ant.cvstask.CVSPass cvsPassTask = new CVSPass();
 		cvsPassTask.setProject(antProject);
 		cvsPassTask.setCvsroot(getActualCvsRoot());
 		cvsPassTask.setPassword(getCvsPassword());
 		cvsPassTask.setPassfile(new File(Luntbuild.installDir + "/" + passwdFileName));
 		cvsPassTask.setTaskType("CVSPass");
 		cvsPassTask.setTaskName("CVSPass");
 		cvsPassTask.execute();
 
 		// adjust eol of cvs pass file
 		if (isCygwinCvs()) {
 			FixCRLF fixCRLF = new FixCRLF();
 			fixCRLF.setProject(antProject);
 			fixCRLF.setTaskName("fixCRLF");
 			fixCRLF.setTaskType("fixCRLF");
 			fixCRLF.setSrcdir(new File(Luntbuild.installDir));
 			fixCRLF.setIncludes(".cvspass");
 			FixCRLF.CrLf crLf = new FixCRLF.CrLf();
 			crLf.setValue("unix");
 			fixCRLF.setEol(crLf);
 			FixCRLF.AddAsisRemove addAsisRemove = new FixCRLF.AddAsisRemove();
 			addAsisRemove.setValue("remove");
 			fixCRLF.setEof(addAsisRemove);
 			fixCRLF.execute();
 		}
 
 		// cause the ant task CVSPass have a bug in windows system, we should go
 		// longer to pick up the encrypted password and call a external program to save
 		// it to windows registry
 		if (System.getProperty("os.name").startsWith("Windows")) {
 			BufferedReader reader = null;
 			try {
 				reader = new BufferedReader(new FileReader(Luntbuild.installDir + "/" + passwdFileName));
 				String line;
 				while ((line = reader.readLine()) != null) {
 					int index = line.indexOf(' ');
 					String currentCvsRoot;
 					String currentEncryptedPasswd;
 					if (index == -1) {
 						currentCvsRoot = line;
 						currentEncryptedPasswd = "";
 					} else {
 						currentCvsRoot = line.substring(0, index);
 						currentEncryptedPasswd = line.substring(index + 1);
 					}
 					if (currentCvsRoot.equals(getActualCvsRoot())) {
 						String cvsntPasswdExe = new File(Luntbuild.installDir + "/osdependent/cvsnt_passwd").getCanonicalPath();
 						Commandline cmdLine = new Commandline();
 						cmdLine.setExecutable(cvsntPasswdExe);
 						cmdLine.createArgument().setValue(getActualCvsRoot());
 						Commandline.Argument arg = cmdLine.createArgument();
 						arg.setValue(currentEncryptedPasswd.replaceAll("\"", "\\\\\""));
 						arg.setDescriptiveValue("******");
 						new MyExecTask("cvsnt_passwd", antProject, cmdLine, Project.MSG_INFO).execute();
 						return;
 					}
 				}
 			} catch (IOException e) {
 				throw new BuildException("ERROR: " + e.getMessage());
 			} finally {
 				if (reader != null)
 					try {
 						reader.close();
 					} catch (IOException e) {
 						throw new BuildException("ERROR: Failed to close password file: " + e.getMessage());
 					}
 			}
 			// we should not go this far
 			antProject.log("ERROR: Failed to find password for CVSROOT \"" +
 					getActualCvsRoot() + "\" in password file!", Project.MSG_ERR);
 		}
 	}
 
 	/**
 	 * Constructs the executable part of a commandline object.
 	 * 
 	 * @return the commandline object
 	 */
 	protected Commandline buildCvsExecutable() {
 		Commandline cmdLine = new Commandline();
 		if (Luntbuild.isEmpty(getCvsDir()))
 			cmdLine.setExecutable("cvs");
 		else
 			cmdLine.setExecutable(Luntbuild.concatPath(getCvsDir(), "cvs"));
 		return cmdLine;
 	}
 
     /**
      * Validates the properties of this VCS.
      *
      * @throws ValidationException if a property has an invalid value
      */
 	public void validateProperties() {
 		super.validateProperties();
 		if (!Luntbuild.isEmpty(getCygwinCvs())) {
 			if (!getCygwinCvs().equalsIgnoreCase("yes") && !getCygwinCvs().equalsIgnoreCase("no"))
 				throw new ValidationException("Only \"yes\" or \"no\" value can be specified " +
 						"for \"is cygwin cvs\" property!");
 		}
 		if (!Luntbuild.isEmpty(getDisableSuppressOption())) {
 			if (!getDisableSuppressOption().equalsIgnoreCase("yes") && !getDisableSuppressOption().equalsIgnoreCase("no"))
 				throw new ValidationException("Only \"yes\" or \"no\" value can be specified " +
 						"for \"disable -S option for log command\" property!");
 		}
 		if (!Luntbuild.isEmpty(getDisableHistoryCmd())) {
 			if (!getDisableHistoryCmd().equalsIgnoreCase("yes") && !getDisableHistoryCmd().equalsIgnoreCase("no"))
 				throw new ValidationException("Only \"yes\" or \"no\" value can be specified " +
 						"for \"disable history command\" property!");
 		}
 	}
 
 	private String normalizeModulePath(String modulePath) {
 		return Luntbuild.removeTrailingSlash(Luntbuild.removeLeadingSlash(modulePath.replace('\\', '/')));
 	}
 
 	/**
 	 * Checks out the contents from a module.
 	 * 
 	 * @param workingDir the working directory
 	 * @param module the module
 	 * @param antProject the ant project used for logging
 	 */
 	private void retrieveModule(String workingDir, CvsModule module, boolean isClean, Project antProject) {
 		if (isClean)
 			antProject.log("Retrieve source path: " + module.getActualSrcPath(), Project.MSG_INFO);
 		else
 			antProject.log("Update source path: " + module.getActualSrcPath(), Project.MSG_INFO);
 
 		if (isClean)
 			Luntbuild.deleteDir(workingDir + "/" + module.getActualSrcPath());
 
 		// call ant cvs task to retrieve module
 		Cvs cvsTask = new Cvs();
 		cvsTask.setProject(antProject);
 		cvsTask.setCommand("checkout -P");
 		cvsTask.setCvsRoot(getActualCvsRoot());
 		cvsTask.setPackage(normalizeModulePath(module.getActualSrcPath()));
 		cvsTask.setPassfile(new File(Luntbuild.installDir + "/" + passwdFileName));
 		cvsTask.setCvsDir(getCvsDir());
 
 		// label takes precedence of branch if both of them are not empty
 		if (!Luntbuild.isEmpty(module.getLabel()))
 			cvsTask.setTag(module.getActualLabel());
 		else if (!Luntbuild.isEmpty(module.getBranch()))
 			cvsTask.setTag(module.getActualBranch());
 
 		cvsTask.setDest(new File(workingDir));
 		LuntbuildLogger luntBuildLogger = Luntbuild.getLuntBuildLogger(antProject);
 		if (luntBuildLogger == null || luntBuildLogger.getMessageOutputLevel() <= Project.MSG_INFO)
 			cvsTask.setReallyquiet(true);
 		cvsTask.setFailOnError(true);
 		cvsTask.setTaskType("Cvs");
 		cvsTask.setTaskName("Cvs");
 		cvsTask.execute();
 	}
 
 	/**
 	 * Labels the contents of a module.
 	 * 
 	 * @param workingDir the working directory
 	 * @param module the module
 	 * @param label the label
 	 * @param antProject the ant project used for logging
 	 */
 	private void labelModule(String workingDir, CvsModule module, String label, Project antProject) {
 		// call ant cvs task to perform code labeling
 		antProject.log("Label source path: " + module.getActualSrcPath(), Project.MSG_INFO);
 		Cvs cvsTask = new Cvs();
 		cvsTask.setProject(antProject);
 		cvsTask.setCommand("tag " + label);
 		cvsTask.setCvsRoot(getActualCvsRoot());
 		cvsTask.setPassfile(new File(Luntbuild.installDir + "/" + passwdFileName));
 		cvsTask.setCvsDir(getCvsDir());
 
 		cvsTask.setDest(new File(Luntbuild.concatPath(workingDir, module.getActualSrcPath())));
 		LuntbuildLogger luntBuildLogger = Luntbuild.getLuntBuildLogger(antProject);
 		if (luntBuildLogger == null || luntBuildLogger.getMessageOutputLevel() <= Project.MSG_INFO)
 			cvsTask.setReallyquiet(true);
 		cvsTask.setFailOnError(true);
 		cvsTask.setTaskType("Cvs");
 		cvsTask.setTaskName("Cvs");
 		cvsTask.execute();
 	}
 
 	/**
 	 * @inheritDoc
 	 */
 	public void checkoutActually(Build build, Project antProject) {
 		String workingDir = build.getSchedule().getWorkDirRaw();
 		if (getActualCvsRoot().startsWith(":pserver:"))
 			login(antProject);
 
 		// retrieve modules
 		Iterator it = getModules().iterator();
 		while (it.hasNext()) {
 			CvsAdaptor.CvsModule module = (CvsAdaptor.CvsModule) Luntbuild.cloneModule(this, (Vcs.Module) it.next());
 			if (build.isRebuild() && Luntbuild.isEmpty(module.getActualLabel()))
 				module.setLabel(Luntbuild.getLabelByVersion(build.getVersion()));
 			if (build.isRebuild() || build.isCleanBuild())
 				retrieveModule(workingDir, module, true, antProject);
 			else
 				retrieveModule(workingDir, module, false, antProject);
 		}
 	}
 
 	/**
 	 * @inheritDoc
 	 */
 	public void label(Build build, Project antProject) {
 		String workingDir = build.getSchedule().getWorkDirRaw();
 		Iterator it = getModules().iterator();
 		while (it.hasNext()) {
 			CvsModule module = (CvsModule) it.next();
 			if (Luntbuild.isEmpty(module.getActualLabel()))
 				labelModule(workingDir, module, Luntbuild.getLabelByVersion(build.getVersion()), antProject);
 		}
 	}
 
 	/**
 	 * @inheritDoc
 	 * @see CvsModule
 	 */
 	public Module createNewModule() {
 		return new CvsModule();
 	}
 
 	/**
 	 * @inheritDoc
 	 * @see CvsModule
 	 */
     public Module createNewModule(Module module) {
         return new CvsModule((CvsModule)module);
     }
 
 	/**
 	 * Checks if the VCS has had any changes since the specified date.
 	 * Overrides the default implementation in order to speed up quiet detection for the CVS adaptor.
 	 * 
 	 * @inheritDoc
 	 */
 	public boolean isVcsQuietSince(Date sinceDate, Schedule workingSchedule, Project antProject) {
 		String workingDir = workingSchedule.getWorkDirRaw();
 		if (getActualCvsRoot().startsWith(":pserver:"))
 			login(antProject);
 		Environment envs = new Environment();
 		Environment.Variable var = new Environment.Variable();
 		var.setKey("CVS_PASSFILE");
 		try {
 			var.setValue(new File(Luntbuild.installDir + "/" + passwdFileName).getCanonicalPath());
 		} catch (IOException e) {
 			throw new BuildException("Failed to get canonical path for cvs pass file!", e);
 		}
 		envs.addVariable(var);
 		Commandline cmdLine = buildCvsExecutable();
 
 		final SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
         if (!isDisableHistoryCmd()) {
 			final boolean commitsHappened[] = new boolean[1];
 			commitsHappened[0] = true;
 			cmdLine.clearArgs();
 			cmdLine.createArgument().setValue("-d" + getActualCvsRoot());
 			cmdLine.createArgument().setLine("-q history -c -a");
 			cmdLine.createArgument().setValue("-D" + format.format(sinceDate));
 			new MyExecTask("history", antProject, null, cmdLine, envs, null, -1) {
 				public void handleStdout(String line) {
 					if (line.equals("No records selected."))
 						commitsHappened[0] = false;
 				}
 			}.execute();
 			if (!commitsHappened[0])
 				return true;
 			else
 				return false;
 		}
 
 		Iterator it = getModules().iterator();
 		while (it.hasNext()) {
 			CvsModule module = (CvsModule) it.next();
 			if (Luntbuild.isEmpty(module.getLabel())) { // necessary to check revisions
 				// prepare working directory to run log command
 				try {
 					// first try update working directory
 					retrieveModule(workingDir, module, false, antProject);
 				} catch (BuildException e) {
 					retrieveModule(workingDir, module, true, antProject);
 				}
 
 				cmdLine.clearArgs();
 				cmdLine.createArgument().setValue("-d" + getActualCvsRoot());
 				if (isDisableSuppressOption())
 					cmdLine.createArgument().setLine("-q log -N");
 				else
 					cmdLine.createArgument().setLine("-q log -S -N");
 				cmdLine.createArgument().setValue("-d>" + format.format(sinceDate));
 				if (Luntbuild.isEmpty(module.getBranch()))
 					cmdLine.createArgument().setValue("-b");
 				else
 					cmdLine.createArgument().setValue("-r" + module.getActualBranch());
 
 				final Revisions revisions = new Revisions();
 				// initialize blocks to help parse output when -S option is disabled for log command
 				final RevisionBlock block = new RevisionBlock();
 				block.setValid(false);
 				block.setReady(false);
 				new MyExecTask("log", antProject, Luntbuild.concatPath(workingDir, module.getActualSrcPath()),
 						cmdLine, envs, null, -1) {
 					public void handleStdout(String line) {
 						if (isDisableSuppressOption()) {
 							if (line.startsWith("RCS file:")) {
 								if (block.isValid())
 									revisions.setFileModified(true);
 								block.setReady(true);
 								block.setValid(false);
 							} else {
 								if (block.isReady()) {
 									if (line.startsWith("revision "))
 										block.setValid(true);
 								}
 							}
 						} else {
 							if (!revisions.isFileModified() && line.toLowerCase().startsWith("revision"))
 								revisions.setFileModified(true);
 						}
 					}
 				}.execute();
 				if (isDisableSuppressOption()) {
 					if (block.isValid())
 						revisions.setFileModified(true);
 				}
 				if (revisions.isFileModified())
 					return false;
 			}
 		}
 		return true;
 	}
 
     /**
      * @inheritDoc
      */
     public Revisions getRevisionsSince(Date sinceDate, Schedule workingSchedule, Project antProject) {
         String workingDir = workingSchedule.getWorkDirRaw();
         if (getActualCvsRoot().startsWith(":pserver:"))
             login(antProject);
         final Revisions revisions = new Revisions();
         revisions.addLog(this.getClass().getName(), toString());
         revisions.getChangeLogs().add("*************************************************************");
         revisions.getChangeLogs().add(toString());
         revisions.getChangeLogs().add("");
         Environment envs = new Environment();
         Environment.Variable var = new Environment.Variable();
         var.setKey("CVS_PASSFILE");
         try {
             var.setValue(new File(Luntbuild.installDir + "/" + passwdFileName).getCanonicalPath());
         } catch (IOException e) {
             throw new BuildException("Failed to get canonical path for cvs pass file!", e);
         }
         envs.addVariable(var);
         Commandline cmdLine = buildCvsExecutable();
 		final SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
         if (!isDisableHistoryCmd()) {
             antProject.log("History command enabled.");
             final boolean commitsHappened[] = new boolean[1];
             commitsHappened[0] = true;
             cmdLine.createArgument().setValue("-d" + getActualCvsRoot());
             cmdLine.createArgument().setLine("-q history -c -a");
 			cmdLine.createArgument().setValue("-D" + format.format(sinceDate));
             new MyExecTask("history", antProject, null, cmdLine, envs, null, -1) {
                 public void handleStdout(String line) {
                     if (line.equals("No records selected."))
                         commitsHappened[0] = false;
                 }
             }.execute();
             if (!commitsHappened[0]) {
                 antProject.log("Commits not found in repository, no need to run log command.");
                 return revisions;
             }
             antProject.log("Commits found in module history, further check with log command...");
         }
 
         final Pattern pathPattern = Pattern.compile("^RCS file: /[^/]*/(.*),v$");
         final Pattern revisionPattern = Pattern.compile("^revision (.*)$");
         final Pattern authorPattern = Pattern.compile("^date:(.*);.*author:(.*);.*state:.*$");
 
         // initialize blocks to help parse output when -S option is disabled for log command
         final RevisionBlock block = new RevisionBlock();
         block.setValid(false);
         block.setReady(false);
         Iterator it = getModules().iterator();
         while (it.hasNext()) {
             CvsModule module = (CvsModule) it.next();
             if (Luntbuild.isEmpty(module.getLabel())) { // necessary to check revisions
                 antProject.log("Getting revisions for module \"" + module.getActualSrcPath() + "\"...");
 
                 // prepare working directory to run log command
                 antProject.log("Prepares the work directory for running of the log command...");
                 if (workingSchedule.getBuildType() == Constants.BUILD_TYPE_CLEAN) {
                     retrieveModule(workingDir, module, true, antProject);
                 } else {
                     try {
                         // first try update working directory
                         retrieveModule(workingDir, module, false, antProject);
                     } catch (BuildException e) {
                         retrieveModule(workingDir, module, true, antProject);
                     }
                 }
 
                 cmdLine.clearArgs();
                 cmdLine.createArgument().setValue("-d" + getActualCvsRoot());
                 if (isDisableSuppressOption())
                     cmdLine.createArgument().setLine("-q log -N");
                 else
                     cmdLine.createArgument().setLine("-q log -S -N");
 				cmdLine.createArgument().setValue("-d>" + format.format(sinceDate));
                 if (Luntbuild.isEmpty(module.getBranch()))
                     cmdLine.createArgument().setValue("-b");
                 else
                     cmdLine.createArgument().setValue("-r" + module.getActualBranch());
                 antProject.log("Run log command to get revisions for the current module...");
                 new MyExecTask("log", antProject, Luntbuild.concatPath(workingDir, module.getActualSrcPath()),
                         cmdLine, envs, null, -1) {
                     private String revision = "";
                     private String author = "";
                     private Date date = null;
                     private String message = "";
                     private String path = "";
                     private boolean captureMessage = false;
                     private boolean addRevision = false;
                     public void handleStdout(String line) {
                         if (isDisableSuppressOption()) {
                             if (line.startsWith("RCS file:")) {
                                 if (block.isValid()) {
                                     revisions.setFileModified(true);
                                     Iterator itBlockLine = block.getLines().iterator();
                                     while (itBlockLine.hasNext()) {
                                         String blockLine = (String) itBlockLine.next();
                                         revisions.getChangeLogs().add(blockLine);
                                         Matcher pathmatcher = pathPattern.matcher(blockLine);
                                         Matcher revisionmatcher = revisionPattern.matcher(blockLine);
                                         Matcher authormatcher = authorPattern.matcher(blockLine);
                                         if (pathmatcher.find()) {
                                             path = pathmatcher.group(1).trim();
                                             addRevision = false;
                                         } else if (revisionmatcher.find()) {
                                             revision = revisionmatcher.group(1).trim();
                                             addRevision = true;
                                         } else if (authormatcher.find()) {
                                             try {
                                                 date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(authormatcher.group(1).trim());
                                             } catch (Exception e) {
                                                 logger.error("Failed to parse date from CVS log", e);
                                                 date = null;
                                             }
                                             author = authormatcher.group(2).trim();
                                             revisions.getChangeLogins().add(author);
                                             captureMessage = true;
                                         } else if (blockLine.equals("----------------------------") ||
                                                 blockLine.startsWith("=============================")) {
                                             if (addRevision) {
                                                 captureMessage = false;
                                                 revisions.addEntryToLastLog(revision, author, date, message);
                                                 revisions.addPathToLastEntry(path, "", revision);
                                                 message = "";
                                             }
                                         } else if (captureMessage) {
                                             message += blockLine + "\r\n";
                                         }
                                     }
                                 }
                                 block.setReady(true);
                                 block.setValid(false);
                                 block.getLines().clear();
                                 block.getLines().add(line);
                             } else {
                                 if (block.isReady()) {
                                     block.getLines().add(line);
                                     if (line.startsWith("revision "))
                                         block.setValid(true);
                                 }
                             }
                         } else {
                             if (!line.startsWith("? ")) {
                                 revisions.getChangeLogs().add(line);
                                 Matcher pathmatcher = pathPattern.matcher(line);
                                 Matcher revisionmatcher = revisionPattern.matcher(line);
                                 Matcher authormatcher = authorPattern.matcher(line);
                                 if (pathmatcher.find()) {
                                     path = pathmatcher.group(1).trim();
                                     addRevision = false;
                                 } else if (revisionmatcher.find()) {
                                     revision = revisionmatcher.group(1).trim();
                                     addRevision = true;
                                 } else if (authormatcher.find()) {
                                     try {
                                         date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(authormatcher.group(1).trim());
                                     } catch (Exception e) {
                                         logger.error("Failed to parse date from CVS log", e);
                                         date = null;
                                     }
                                     author = authormatcher.group(2).trim();
                                     revisions.getChangeLogins().add(author);
                                     captureMessage = true;
                                 } else if (line.equals("----------------------------") ||
                                         line.startsWith("=============================")) {
                                     if (addRevision) {
                                         captureMessage = false;
                                         revisions.addEntryToLastLog(revision, author, date, message);
                                         revisions.addPathToLastEntry(path, "", revision);
                                         message = "";
                                     }
                                 } else if (captureMessage) {
                                     message += line + "\r\n";
                                 }
                             }
                             if (!revisions.isFileModified() && line.toLowerCase().startsWith("revision"))
                                 revisions.setFileModified(true);
                         }
                     }
                 }.execute();
                 if (isDisableSuppressOption()) {
                     String revision = "";
                     String author = "";
                     Date date = null;
                     String message = "";
                     String path = "";
                     boolean captureMessage = false;
                     boolean addRevision = false;
                     if (block.isValid()) {
                         revisions.setFileModified(true);
                         Iterator itBlockLine = block.getLines().iterator();
                         while (itBlockLine.hasNext()) {
                             String blockLine = (String) itBlockLine.next();
                             revisions.getChangeLogs().add(blockLine);
                             Matcher pathmatcher = pathPattern.matcher(blockLine);
                             Matcher revisionmatcher = revisionPattern.matcher(blockLine);
                             Matcher authormatcher = authorPattern.matcher(blockLine);
                             if (pathmatcher.find()) {
                                 path = pathmatcher.group(1).trim();
                                 addRevision = false;
                             } else if (revisionmatcher.find()) {
                                 revision = revisionmatcher.group(1).trim();
                                 addRevision = true;
                             } else if (authormatcher.find()) {
                                 try {
                                     date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(authormatcher.group(1).trim());
                                 } catch (Exception e) {
                                     throw new BuildException("Failed to parse date from CVS log", e);
                                 }
                                 author = authormatcher.group(2).trim();
                                 revisions.getChangeLogins().add(author);
                                 captureMessage = true;
                             } else if (blockLine.equals("----------------------------") ||
                                     blockLine.startsWith("=============================")) {
                                 if (addRevision) {
                                     captureMessage = false;
                                     revisions.addEntryToLastLog(revision, author, date, message);
                                     revisions.addPathToLastEntry(path, "", revision);
                                     message = "";
                                 }
                             } else if (captureMessage) {
                                 message += blockLine + "\r\n";
                             }
                         }
                     }
                     block.setReady(false);
                     block.setValid(false);
                     block.getLines().clear();
                 }
             }
         }
         return revisions;
     }
 
     /**
      * Creates a link to browse the repository.
      * 
      * @return the link
      */
     public String createLinkForRepository() {
         if (Luntbuild.isEmpty(getWebInterface()) || Luntbuild.isEmpty(getWebUrl()))
             return "&nbsp;";
         return "<a href=\"" + getWebUrl() + "\">browse</a>";
     }
 
     /**
      * Creates a link to the specified file version.
      * 
      * @param path the path to the file
      * @param version the version
      * @return the link
      */
     public String createLinkForFile(String path, String version) {
         if (Luntbuild.isEmpty(getWebInterface()) || Luntbuild.isEmpty(getWebUrl()) ||
                 Luntbuild.isEmpty(path) || Luntbuild.isEmpty(version))
             return path;
         if (getWebInterface().equals("ViewVC"))
            return "<a href=\"" + getWebUrl() + "/" + path + "?rev=" + version + "&view=markup\">" + path + "</a>";
         else if (getWebInterface().equals("Chora"))
            return "<a href=\"" + getWebUrl() + "/co.php?r=" + version + "&f=" + path + "\">" + path + "</a>";
         else if (getWebInterface().equals("wwCVS"))
            return "<a href=\"" + getWebUrl() + "/file.aspx?tag=&project=" + path + "&revision=" + version + "\">" + path + "</a>";
         else
             return path;
     }
 
     /**
      * Creates a link to diff the specified file and version with the previous version.
      * 
      * @param path the path to the file
      * @param version the version
      * @return the link
      */
     public String createLinkForDiff(String path, String version) {
         if (Luntbuild.isEmpty(version))
             return "";
         version = StringUtils.reverse(version);
         Pattern pattern = Pattern.compile("\\d+");
         Matcher matcher = pattern.matcher(version);
         if (matcher.find()) {
             String digits = matcher.group();
             digits = StringUtils.reverse(digits);
             String newDigits = String.valueOf(new Long(digits).longValue() - 1);
             newDigits = StringUtils.reverse(newDigits);
             String prev_ver = matcher.replaceFirst(newDigits);
             version = StringUtils.reverse(version);
             prev_ver = StringUtils.reverse(prev_ver);
             return createLinkForDiff(path, version, prev_ver);
         }
         return "";
     }
 
     /**
      * Creates a link to diff the specified file between the specified versions.
      * 
      * @param path the path to the file
      * @param version the version (right hand side)
      * @param prev_ver the previous version (left hand side)
      * @return the link
      */
     public String createLinkForDiff(String path, String version, String prev_ver) {
         if (Luntbuild.isEmpty(getWebInterface()) || Luntbuild.isEmpty(getWebUrl()) ||
                 Luntbuild.isEmpty(path) || Luntbuild.isEmpty(version) || Luntbuild.isEmpty(prev_ver))
             return "";
         if (getWebInterface().equals("ViewVC"))
            return "(<a href=\"" + getWebUrl() + "/" + path + "?r1=" + prev_ver + "&r2=" + version + "\">diff</a>)";
         else if (getWebInterface().equals("Chora"))
            return "(<a href=\"" + getWebUrl() + "/diff.php?r1=" + prev_ver + "&r2=" + version + "&f=" + path + "\">diff</a>)";
         else if (getWebInterface().equals("wwCVS"))
            return "(<a href=\"" + getWebUrl() + "/diff.aspx?tag=&project=" + path + "&diff_rev_1=" + prev_ver + "&diff_rev_2=" + version + "\">diff</a>)";
         else
             return "";
     }
 
     /**
 	 * @inheritDoc
      */
 	public List getVcsSpecificProperties() {
 		List properties = new ArrayList();
 		properties.add(new DisplayProperty() {
 			public String getDisplayName() {
 				return "Cvs root";
 			}
 
 			public String getDescription() {
 				return "The Cvs root for this project, for example, :pserver:administrator@localhost:d:/cvs_repository. " +
 						"If you are using ssh, the :ext: protocol will need to be specified, " +
 						"and proper environment need to be setup outside of Luntbuild system. " +
 						"Please refer to your Cvs User's Guide for details.";
 			}
 
 			public String getValue() {
 				return getCvsRoot();
 			}
 
 			public String getActualValue() {
 				return getActualCvsRoot();
 			}
 
 			public void setValue(String value) {
 				setCvsRoot(value);
 			}
 		});
 		properties.add(new DisplayProperty() {
 			public String getDisplayName() {
 				return "Cvs password";
 			}
 
 			public String getDescription() {
 				return "The Cvs password for above Cvs root if connecting using pserver protocol.";
 			}
 
 			public boolean isRequired() {
 				return false;
 			}
 
 			public boolean isSecret() {
 				return true;
 			}
 
 			public String getValue() {
 				return getCvsPassword();
 			}
 
 			public void setValue(String value) {
 				setCvsPassword(value);
 			}
 		});
 		DisplayProperty p = new DisplayProperty() {
 			public String getDisplayName() {
 				return "Is cygwin cvs?";
 			}
 
 			public String getDescription() {
 				return "This property indicates whether or not the cvs executable being used is a cygwin one.";
 			}
 
 			public boolean isRequired() {
 				return false;
 			}
 
 			public boolean isSelect() {
 				return true;
 			}
 
 			public String getValue() {
 				return getCygwinCvs();
 			}
 
 			public void setValue(String value) {
 				setCygwinCvs(value);
 			}
 		};
 		// Create selection model
 		IPropertySelectionModel model = new CvsYesNoSelectionModel();
 		// Set selection model
 		p.setSelectionModel(model);
 		// Add property to properties list
 		properties.add(p);
 		p = new DisplayProperty() {
 			public String getDisplayName() {
 				return "Disable \"-S\" option for log command?";
 			}
 
 			public String getDescription() {
 				return "This property indicates whether or not the \"-S\" option for the log command should be disabled." +
 						"The -S option used in the log command can speed up modification detection, however " +
 						"some earlier versions of Cvs do not support this option. In this case you should disable it. ";
 			}
 
 			public boolean isRequired() {
 				return false;
 			}
 
 			public boolean isSelect() {
 				return true;
 			}
 
 			public String getValue() {
 				return getDisableSuppressOption();
 			}
 
 			public void setValue(String value) {
 				setDisableSuppressOption(value);
 			}
 		};
 		// Create selection model
 		model = new CvsYesNoSelectionModel();
 		// Set selection model
 		p.setSelectionModel(model);
 		// Add property to properties list
 		properties.add(p);
 		p = new DisplayProperty() {
 			public String getDisplayName() {
 				return "Disable history command?";
 			}
 
 			public String getDescription() {
 				return "This property indicates whether or not to disable the history command when performing modification detection. " +
 						"Using the history command in conjunction with the log command can speed up modification detection, " +
 						"however some Cvs repositories may not hold history information of commits. In this case you should disable it. ";
 			}
 
 			public boolean isRequired() {
 				return false;
 			}
 
 			public boolean isSelect() {
 				return true;
 			}
 
 			public String getValue() {
 				return getDisableHistoryCmd();
 			}
 
 			public void setValue(String value) {
 				setDisableHistoryCmd(value);
 			}
 		};
 		// Create selection model
 		model = new CvsYesNoSelectionModel();
 		// Set selection model
 		p.setSelectionModel(model);
 		// Add property to properties list
 		properties.add(p);
 		properties.add(new DisplayProperty() {
 			public String getDisplayName() {
 				return "Path for cvs executable";
 			}
 
 			public String getDescription() {
 				return "The directory path, where your cvs executable file resides in. " +
 						"It should be specified here, if it does not exist in the system path.";
 			}
 
 			public boolean isRequired() {
 				return false;
 			}
 
 			public String getValue() {
 				return getCvsDir();
 			}
 
 			public void setValue(String value) {
 				setCvsDir(value);
 			}
 		});
         p = new DisplayProperty() {
             public String getDisplayName() {
                 return "Web interface";
             }
 
             public String getDescription() {
                 return "Set the web interface to integrate with.";
             }
 
             public boolean isRequired() {
                 return false;
             }
 
             public boolean isSelect() {
                 return true;
             }
 
             public String getValue() {
                 return getWebInterface();
             }
 
             public void setValue(String value) {
                 setWebInterface(value);
             }
         };
         // Create selection model
         model = new CvsWebInterfaceSelectionModel();
         // Set selection model
         p.setSelectionModel(model);
         // Add property to properties list
         properties.add(p);
         properties.add(new DisplayProperty() {
             public String getDisplayName() {
                 return "URL to web interface";
             }
 
             public String getDescription() {
                 return "The URL to access the repository in your chosen web interface.";
             }
 
             public boolean isRequired() {
                 return false;
             }
 
             public String getValue() {
                 return getWebUrl();
             }
 
             public void setValue(String value) {
                 setWebUrl(value);
             }
         });
 		return properties;
 	}
 
 	/**
 	 * Selection model used for user interface of <code>CvsAdaptor</code>.
 	 */
 	static class CvsYesNoSelectionModel implements IPropertySelectionModel {
 		String[] values = {"no", "yes"};
 
 		/**
 		 * Gets the number of options.
 		 * 
 		 * @return the number of options
 		 */
 		public int getOptionCount() {
 			return this.values.length;
 		}
 
 		/**
 		 * Gets an option.
 		 * 
 		 * @param index the index of the opiton
 		 * @return the option
 		 */
 		public Object getOption(int index) {
 			return this.values[index];
 		}
 
 		/**
 		 * Gets the display label of an option.
 		 * 
 		 * @param index the index of the opiton
 		 * @return the label
 		 */
 		public String getLabel(int index) {
 			return this.values[index];
 		}
 
 		/**
 		 * Gets the value of an option.
 		 * 
 		 * @param index the index of the opiton
 		 * @return the value
 		 */
 		public String getValue(int index) {
 			return this.values[index];
 		}
 
 		/**
 		 * Gets the option that corresponds to a value.
 		 * 
 		 * @param value the value
 		 * @return the option
 		 */
 		public Object translateValue(String value) {
 			return value;
 		}
 	}
 
 	/**
 	 * Selection model used for user interface of <code>CvsAdaptor</code>.
 	 */
 	static class CvsWebInterfaceSelectionModel implements IPropertySelectionModel {
 		String[] values = {"", "ViewVC", "Chora", "wwCVS"};
 		String[] display_values = {"", "CVSweb / ViewVC (ViewCVS)", "Chora", "wwCVS"};
 
 		/**
 		 * Gets the number of options.
 		 * 
 		 * @return the number of options
 		 */
 		public int getOptionCount() {
 			return this.values.length;
 		}
 
 		/**
 		 * Gets an option.
 		 * 
 		 * @param index the index of the opiton
 		 * @return the option
 		 */
 		public Object getOption(int index) {
 			return this.values[index];
 		}
 
 		/**
 		 * Gets the display label of an option.
 		 * 
 		 * @param index the index of the opiton
 		 * @return the label
 		 */
 		public String getLabel(int index) {
 			return this.display_values[index];
 		}
 
 		/**
 		 * Gets the value of an option.
 		 * 
 		 * @param index the index of the opiton
 		 * @return the value
 		 */
 		public String getValue(int index) {
 			return this.values[index];
 		}
 
 		/**
 		 * Gets the option that corresponds to a value.
 		 * 
 		 * @param value the value
 		 * @return the option
 		 */
 		public Object translateValue(String value) {
 			return value;
 		}
 	}
 
 	/**
 	 * A CVS module definition.
 	 *
 	 * @author robin shine
 	 */
 	public class CvsModule extends Module {
 		/**
 		 * Keep tracks of version of this class, used when do serialization-deserialization
 		 */
 		static final long serialVersionUID = 1;
 
 		private String srcPath;
 		private String branch;
 		private String label;
 
 		/**
 		 * Constructor, creates a blank CVS module.
 		 */
         public CvsModule() {}
 
 		/**
 		 * Copy constructor, creates a CVS module from another CVS module.
 		 * 
 		 * @param module the module to create from
 		 */
         public CvsModule(CvsModule module) {
             this.srcPath = module.srcPath;
             this.branch = module.branch;
             this.label = module.label;
         }
 
 		/**
 		 * Gets the source path of this module for the repository.
 		 * 
 		 * @return the source path of this module
 		 */
 		public String getSrcPath() {
 			return srcPath;
 		}
 
 		/**
 		 * Gets the source path of this module for the repository. This method will parse OGNL variables.
 		 * 
 		 * @return the source path of this module
 		 */
         private String getActualSrcPath() {
 			return OgnlHelper.evaluateScheduleValue(getSrcPath());
 		}
 
 		/**
 		 * Sets the source path of this module for the repository.
 		 * 
 		 * @param srcPath the source path of this module
 		 */
 		public void setSrcPath(String srcPath) {
 			this.srcPath = srcPath;
 		}
 
 		/**
 		 * Gets the branch to retrieve this module from.
 		 * 
 		 * @return the branch
 		 */
 		public String getBranch() {
 			return branch;
 		}
 
 		/**
 		 * Gets the branch to retrieve this module from. This method will parse OGNL variables.
 		 * 
 		 * @return the branch
 		 */
         private String getActualBranch() {
 			return OgnlHelper.evaluateScheduleValue(getBranch());
 		}
 
 		/**
 		 * Sets the branch to retrieve this module from.
 		 * 
 		 * @param branch the branch
 		 */
 		public void setBranch(String branch) {
 			this.branch = branch;
 		}
 
 		/**
 		 * Gets the label to use for this module.
 		 * 
 		 * @return the label
 		 */
 		public String getLabel() {
 			return label;
 		}
 
 		/**
 		 * Gets the label to use for this module. This method will parse OGNL variables.
 		 * 
 		 * @return the label
 		 */
         private String getActualLabel() {
 			return OgnlHelper.evaluateScheduleValue(getLabel());
 		}
 
 		/**
 		 * Sets the label to use for this module.
 		 * 
 		 * @param label the label
 		 */
 		public void setLabel(String label) {
 			this.label = label;
 		}
 
 		/**
 		 * @inheritDoc
 		 */
 		public List getProperties() {
 			List properties = new ArrayList();
 			properties.add(new DisplayProperty() {
 				public String getDisplayName() {
 					return "Source path";
 				}
 
 				public String getDescription() {
 					return "Specify a path to retrieve from the Cvs repository, for example: testcvs/src.";
 				}
 
 				public String getValue() {
 					return getSrcPath();
 				}
 
 				public String getActualValue() {
 					return getActualSrcPath();
 				}
 
 				public void setValue(String value) {
 					setSrcPath(value);
 				}
 			});
 			properties.add(new DisplayProperty() {
 				public String getDisplayName() {
 					return "Branch";
 				}
 
 				public String getDescription() {
 					return "Specify the branch for the above source path. This property " +
 							"is optional. When left empty, main branch is assumed.";
 				}
 
 				public boolean isRequired() {
 					return false;
 				}
 
 				public String getValue() {
 					return getBranch();
 				}
 
 				public String getActualValue() {
 					return getActualBranch();
 				}
 
 				public void setValue(String value) {
 					setBranch(value);
 				}
 			});
 			properties.add(new DisplayProperty() {
 				public String getDisplayName() {
 					return "Label";
 				}
 
 				public String getDescription() {
 					return "Specify the label for the above source path. This property is optional. " +
 							"If specified, it will take preference over branch. When left empty, latest " +
 							"version of the specified branch will be retrieved.";
 				}
 
 				public boolean isRequired() {
 					return false;
 				}
 
 				public String getValue() {
 					return getLabel();
 				}
 
 				public String getActualValue() {
 					return getActualLabel();
 				}
 
 				public void setValue(String value) {
 					setLabel(value);
 				}
 			});
 			return properties;
 		}
 
 	    /**
 		 * @inheritDoc
 	     * @see CvsModuleFacade
 	     */
 		public ModuleFacade getFacade() {
 			CvsModuleFacade facade = new CvsModuleFacade();
 			facade.setBranch(getBranch());
 			facade.setLabel(getLabel());
 			facade.setSrcPath(getSrcPath());
 			return facade;
 		}
 
 	    /**
 		 * @inheritDoc
 	     * @throws RuntimeException if the facade is not an <code>CvsModuleFacade</code>
 	     * @see CvsModuleFacade
 	     */
 		public void setFacade(ModuleFacade facade) {
 			if (facade instanceof CvsModuleFacade) {
 				CvsModuleFacade cvsModuleFacade = (CvsModuleFacade) facade;
 				setBranch(cvsModuleFacade.getBranch());
 				setLabel(cvsModuleFacade.getLabel());
 				setSrcPath(cvsModuleFacade.getSrcPath());
 			} else
 				throw new RuntimeException("Invalid facade class: " + facade.getClass().getName());
 		}
 	}
 
     /**
      * @inheritDoc
      * @see CvsAdaptorFacade
      */
 	public void saveToFacade(VcsFacade facade) {
     	// TODO throw RuntimeException if the facade is not the right class
 		CvsAdaptorFacade cvsFacade = (CvsAdaptorFacade) facade;
 		cvsFacade.setCvsRoot(getCvsRoot());
 		cvsFacade.setCvsPassword(getCvsPassword());
 		cvsFacade.setCygwinCvs(getCygwinCvs());
 		cvsFacade.setDisableHistoryCmd(getDisableHistoryCmd());
 		cvsFacade.setDisableSuppressOption(getDisableSuppressOption());
 		cvsFacade.setCvsDir(getCvsDir());
 		cvsFacade.setWebInterface(getWebInterface());
 		cvsFacade.setWebUrl(getWebUrl());
 	}
 
     /**
      * @inheritDoc
      * @throws RuntimeException if the facade is not an <code>CvsAdaptorFacade</code>
      * @see CvsAdaptorFacade
      */
 	public void loadFromFacade(VcsFacade facade) {
 		if (!(facade instanceof CvsAdaptorFacade))
 			throw new RuntimeException("Invalid facade class: " + facade.getClass().getName());
 		CvsAdaptorFacade cvsFacade = (CvsAdaptorFacade) facade;
 		setCvsRoot(cvsFacade.getCvsRoot());
 		setCvsPassword(cvsFacade.getCvsPassword());
 		setCygwinCvs(cvsFacade.getCygwinCvs());
 		setDisableHistoryCmd(cvsFacade.getDisableHistoryCmd());
 		setDisableSuppressOption(cvsFacade.getDisableSuppressOption());
 		setCvsDir(cvsFacade.getCvsDir());
 		setWebInterface(cvsFacade.getWebInterface());
 		setWebUrl(cvsFacade.getWebUrl());
 	}
 
     /**
      * @inheritDoc
      * @see CvsAdaptorFacade
      */
 	public VcsFacade constructFacade() {
 		return new CvsAdaptorFacade();
 	}
 }
