 
 /* ===========================================================================
  *  Copyright (c) 2007 Serena Software. All rights reserved.
  *
  *  Use of the Sample Code provided by Serena is governed by the following
  *  terms and conditions. By using the Sample Code, you agree to be bound by
  *  the terms contained herein. If you do not agree to the terms herein, do
  *  not install, copy, or use the Sample Code.
  *
  *  1.  GRANT OF LICENSE.  Subject to the terms and conditions herein, you
  *  shall have the nonexclusive, nontransferable right to use the Sample Code
  *  for the sole purpose of developing applications for use solely with the
  *  Serena software product(s) that you have licensed separately from Serena.
  *  Such applications shall be for your internal use only.  You further agree
  *  that you will not: (a) sell, market, or distribute any copies of the
  *  Sample Code or any derivatives or components thereof; (b) use the Sample
  *  Code or any derivatives thereof for any commercial purpose; or (c) assign
  *  or transfer rights to the Sample Code or any derivatives thereof.
  *
  *  2.  DISCLAIMER OF WARRANTIES.  TO THE MAXIMUM EXTENT PERMITTED BY
  *  APPLICABLE LAW, SERENA PROVIDES THE SAMPLE CODE AS IS AND WITH ALL
  *  FAULTS, AND HEREBY DISCLAIMS ALL WARRANTIES AND CONDITIONS, EITHER
  *  EXPRESSED, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY
  *  IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, OF FITNESS FOR A
  *  PARTICULAR PURPOSE, OF LACK OF VIRUSES, OF RESULTS, AND OF LACK OF
  *  NEGLIGENCE OR LACK OF WORKMANLIKE EFFORT, CONDITION OF TITLE, QUIET
  *  ENJOYMENT, OR NON-INFRINGEMENT.  THE ENTIRE RISK AS TO THE QUALITY OF
  *  OR ARISING OUT OF USE OR PERFORMANCE OF THE SAMPLE CODE, IF ANY,
  *  REMAINS WITH YOU.
  *
  *  3.  EXCLUSION OF DAMAGES.  TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE
  *  LAW, YOU AGREE THAT IN CONSIDERATION FOR RECEIVING THE SAMPLE CODE AT NO
  *  CHARGE TO YOU, SERENA SHALL NOT BE LIABLE FOR ANY DAMAGES WHATSOEVER,
  *  INCLUDING BUT NOT LIMITED TO DIRECT, SPECIAL, INCIDENTAL, INDIRECT, OR
  *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, DAMAGES FOR LOSS OF
  *  PROFITS OR CONFIDENTIAL OR OTHER INFORMATION, FOR BUSINESS INTERRUPTION,
  *  FOR PERSONAL INJURY, FOR LOSS OF PRIVACY, FOR NEGLIGENCE, AND FOR ANY
  *  OTHER LOSS WHATSOEVER) ARISING OUT OF OR IN ANY WAY RELATED TO THE USE
  *  OF OR INABILITY TO USE THE SAMPLE CODE, EVEN IN THE EVENT OF THE FAULT,
  *  TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY, OR BREACH OF CONTRACT,
  *  EVEN IF SERENA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.  THE
  *  FOREGOING LIMITATIONS, EXCLUSIONS AND DISCLAIMERS SHALL APPLY TO THE
  *  MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW.  NOTWITHSTANDING THE ABOVE,
  *  IN NO EVENT SHALL SERENA'S LIABILITY UNDER THIS AGREEMENT OR WITH RESPECT
  *  TO YOUR USE OF THE SAMPLE CODE AND DERIVATIVES THEREOF EXCEED US$10.00.
  *
  *  4.  INDEMNIFICATION. You hereby agree to defend, indemnify and hold
  *  harmless Serena from and against any and all liability, loss or claim
  *  arising from this agreement or from (i) your license of, use of or
  *  reliance upon the Sample Code or any related documentation or materials,
  *  or (ii) your development, use or reliance upon any application or
  *  derivative work created from the Sample Code.
  *
  *  5.  TERMINATION OF THE LICENSE.  This agreement and the underlying
  *  license granted hereby shall terminate if and when your license to the
  *  applicable Serena software product terminates or if you breach any terms
  *  and conditions of this agreement.
  *
  *  6.  CONFIDENTIALITY.  The Sample Code and all information relating to the
  *  Sample Code (collectively "Confidential Information") are the
  *  confidential information of Serena.  You agree to maintain the
  *  Confidential Information in strict confidence for Serena.  You agree not
  *  to disclose or duplicate, nor allow to be disclosed or duplicated, any
  *  Confidential Information, in whole or in part, except as permitted in
  *  this Agreement.  You shall take all reasonable steps necessary to ensure
  *  that the Confidential Information is not made available or disclosed by
  *  you or by your employees to any other person, firm, or corporation.  You
  *  agree that all authorized persons having access to the Confidential
  *  Information shall observe and perform under this nondisclosure covenant.
  *  You agree to immediately notify Serena of any unauthorized access to or
  *  possession of the Confidential Information.
  *
  *  7.  AFFILIATES.  Serena as used herein shall refer to Serena Software,
  *  Inc. and its affiliates.  An entity shall be considered to be an
  *  affiliate of Serena if it is an entity that controls, is controlled by,
  *  or is under common control with Serena.
  *
  *  8.  GENERAL.  Title and full ownership rights to the Sample Code,
  *  including any derivative works shall remain with Serena.  If a court of
  *  competent jurisdiction holds any provision of this agreement illegal or
  *  otherwise unenforceable, that provision shall be severed and the
  *  remainder of the agreement shall remain in full force and effect.
  * ===========================================================================
  */
 
 /**
  ** @brief This experimental plugin extends Hudson support for Dimensions SCM repositories
  **
  ** @author Tim Payne
  **
  **/
 
 package hudson.plugins.dimensionsscm;
 
 // Dimensions imports
 import hudson.plugins.dimensionsscm.DimensionsAPI;
 import hudson.plugins.dimensionsscm.DimensionsSCM;
 import hudson.plugins.dimensionsscm.Logger;
 import com.serena.dmclient.api.DimensionsResult;
 
 // Hudson imports
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Build;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor.FormException;
 import hudson.model.Descriptor;
 import hudson.model.Result;
 import hudson.model.TaskListener;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 import hudson.tasks.Notifier;
 import hudson.tasks.Publisher;
 import hudson.util.FormFieldValidator;
 import hudson.util.FormValidation;
 import hudson.util.VariableResolver;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 // General imports
 import java.io.IOException;
 import java.io.Serializable;
 import javax.servlet.ServletException;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 
 public class DimensionsBuilder extends Builder {
 
     private static DimensionsSCM scm = null;
 
     private String projectArea = null;
     private String projectConfig = null;
     private String projectOptions = null;
     private String projectTargets = null;
     private String projectType = null;
     private String projectStage = null;
 
     private boolean batch = false;
     private boolean buildClean = false;
     private boolean capture = false;
 
     private boolean audit = false;
     private boolean populate = false;
     private boolean touch = false;
 
     /*
      * Gets the audit .
      * @return boolean
      */
     public boolean isProjectAudit() {
         return this.audit;
     }
 
     /*
      * Gets the populate .
      * @return boolean
      */
     public boolean isProjectPopulate() {
         return this.populate;
     }
 
     /*
      * Gets the touch .
      * @return boolean
      */
     public boolean isProjectTouch() {
         return this.touch;
     }
 
     /*
      * Gets the batch .
      * @return boolean
      */
     public boolean isProjectBatch() {
         return this.batch;
     }
 
     /*
      * Gets the buildClean .
      * @return boolean
      */
     public boolean isProjectClean() {
         return this.buildClean;
     }
 
     /*
      * Gets the capture .
      * @return boolean
      */
     public boolean isProjectCapture() {
         return this.capture;
     }
 
     /*
      * Gets the project type .
      * @return String
      */
     public String getProjectType() {
         return this.projectType;
     }
 
     /*
      * Gets the project stage .
      * @return String
      */
     public String getProjectStage() {
         return this.projectStage;
     }
 
     /*
      * Gets the area .
      * @return String
      */
     public String getProjectArea() {
         return this.projectArea;
     }
 
     /*
      * Gets the build config .
      * @return String
      */
     public String getProjectConfig() {
         return this.projectConfig;
     }
 
     /*
      * Gets the build options .
      * @return String
      */
     public String getProjectOptions() {
         return this.projectOptions;
     }
 
 
     /*
      * Gets the build targets .
      * @return String
      */
     public String getProjectTargets() {
         return this.projectTargets;
     }
 
     /**
      * Default constructor.
      */
     public DimensionsBuilder(String area, String buildConfig,
                              String buildOptions, String buildTargets,
                              String buildType, String buildStage,
                              boolean batch, boolean buildClean, boolean capture,
                              boolean audit, boolean populate, boolean touch) {
         this.projectArea = area;
         this.projectConfig = buildConfig;
         this.projectOptions = buildOptions;
         this.projectTargets = buildTargets;
         this.projectType = buildType;
         this.projectStage = buildStage;
         this.batch = batch;
         this.buildClean = buildClean;
         this.capture = capture;
         this.audit = audit;
         this.populate = populate;
         this.touch = touch;
     }
 
 
     public boolean perform(Build build, Launcher launcher, BuildListener listener) {
         Logger.Debug("Invoking perform callout " + this.getClass().getName());
         long key = -1;
         try {
 
             if (!(build.getProject().getScm() instanceof DimensionsSCM)) {
                 listener.fatalError("[DIMENSIONS] This plugin only works with the Dimensions SCM engine.");
                 build.setResult(Result.FAILURE);
                 throw new IOException("[DIMENSIONS] This plugin only works with a Dimensions SCM engine");
             }
 
             if (build.isBuilding()) {
                 if (scm == null)
                     scm = (DimensionsSCM)build.getProject().getScm();
                 Logger.Debug("Dimensions user is "+scm.getJobUserName()+" , Dimensions installation is "+scm.getJobServer());
                 Logger.Debug("Running a project build step...");
                 key = scm.getAPI().login(scm.getJobUserName(),
                                        scm.getJobPasswd(),
                                        scm.getJobDatabase(),
                                        scm.getJobServer());
                 if (key>0)
                 {
                     VariableResolver<String> myResolver = build.getBuildVariableResolver();
                     String requests = myResolver.resolve("DM_TARGET_REQUEST");
 
                     if (requests != null) {
                         requests = requests.replaceAll(" ","");
                         requests = requests.toUpperCase();
                     }
 
                     {
                         String projectXType = projectType;
                         if (projectXType.equals("NONE"))
                             projectXType = null;
 
                         // This will active the build baseline functionality
                         listener.getLogger().println("[DIMENSIONS] Submitting a build job to Dimensions...");
                         listener.getLogger().flush();
                         DimensionsResult res = scm.getAPI().buildProject(key,projectArea,scm.getProject(),
                                                                          batch,buildClean,projectConfig,
                                                                          projectOptions,
                                                                          capture,requests,
                                                                          projectTargets,
                                                                          projectStage, projectXType,
                                                                          audit, populate,
                                                                          touch, build);
                         if (res==null) {
                             listener.getLogger().println("[DIMENSIONS] The project failed to be built in Dimensions");
                             listener.getLogger().flush();
                             build.setResult(Result.FAILURE);
                         }
                         else {
                             listener.getLogger().println("[DIMENSIONS] Build step was successfully run in Dimensions");
                             listener.getLogger().println("[DIMENSIONS] ("+res.getMessage().replaceAll("\n","\n[DIMENSIONS] ")+")");
                             listener.getLogger().flush();
                         }
                     }
                 }
                 else {
                     listener.fatalError("[DIMENSIONS] Login to Dimensions failed.");
                     build.setResult(Result.FAILURE);
                     return false;
                 }
             }
         } catch(Exception e) {
            String errMsg;
            if (e.getMessage() != null || e.getMessage().length() > 0) {
                errMsg = e.getMessage();
            } else {
                errMsg = "Dimensions returned no error";
            }
            listener.fatalError("Unable to tag build in Dimensions - " + errMsg);
             build.setResult(Result.FAILURE);
             return false;
         }
         finally
         {
             if (scm != null)
                 scm.getAPI().logout(key);
         }
         return true;
     }
 
     public Descriptor<Builder> getDescriptor() {
         // see Descriptor javadoc for more about what a descriptor is.
         return DMBLD_DESCRIPTOR;
     }
 
     /**
      * Descriptor should be singleton.
      */
     @Extension
     public static final DescriptorImpl DMBLD_DESCRIPTOR = new DescriptorImpl();
 
     /**
      * Descriptor for {@link DimensionsBuilder}. Used as a singleton.
      * The class is marked as public so that it can be accessed from views.
      *
      */
     public static final class DescriptorImpl extends Descriptor<Builder> {
         /**
          * To persist global configuration information,
          * simply store it in a field and call save().
          *
          * <p>
          * If you don't want fields to be persisted, use <tt>transient</tt>.
          */
 
         public DescriptorImpl() {
             super(DimensionsBuilder.class);
             Logger.Debug("Loading " + this.getClass().getName());
         }
 
         /**
          * Performs on-the-fly validation of the form field 'name'.
          *
          * @param value
          *      This receives the current value of the field.
          */
         public void doCheckName(StaplerRequest req, StaplerResponse rsp, @QueryParameter final String value) throws IOException, ServletException {
             new FormFieldValidator(req,rsp,null) {
                 /**
                  * The real check goes here. In the end, depending on which
                  * method you call, the browser shows text differently.
                  */
                 protected void check() throws IOException, ServletException {
                     ok();
                 }
             }.process();
         }
 
         @Override
         public Builder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             // Get variables and then construct a new object
             Boolean batch = Boolean.valueOf("on".equalsIgnoreCase(req.getParameter("dimensionsbuilder.projectBatch")));
             Boolean buildClean = Boolean.valueOf("on".equalsIgnoreCase(req.getParameter("dimensionsbuilder.projectClean")));
             Boolean capture = Boolean.valueOf("on".equalsIgnoreCase(req.getParameter("dimensionsbuilder.projectCapture")));
 
             Boolean audit = Boolean.valueOf("on".equalsIgnoreCase(req.getParameter("dimensionsbuilder.projectAudit")));
             Boolean populate = Boolean.valueOf("on".equalsIgnoreCase(req.getParameter("dimensionsbuilder.projectPopulate")));
             Boolean touch = Boolean.valueOf("on".equalsIgnoreCase(req.getParameter("dimensionsbuilder.projectTouch")));
 
             String area = req.getParameter("dimensionsbuilder.projectArea");
             String buildConfig = req.getParameter("dimensionsbuilder.projectConfig");
             String buildOptions = req.getParameter("dimensionsbuilder.projectOptions");
             String buildTargets = req.getParameter("dimensionsbuilder.projectTargets");
             String buildType = req.getParameter("dimensionsbuilder.projectType");
             String buildStage = req.getParameter("dimensionsbuilder.projectStage");
 
             if (area != null)
                 area = Util.fixNull(req.getParameter("dimensionsbuilder.projectArea").trim());
             if (buildConfig != null)
                 buildConfig = Util.fixNull(req.getParameter("dimensionsbuilder.projectConfig").trim());
             if (buildOptions != null)
                 buildOptions = Util.fixNull(req.getParameter("dimensionsbuilder.projectOptions").trim());
             if (buildTargets != null)
                 buildTargets = Util.fixNull(req.getParameter("dimensionsbuilder.projectTargets").trim());
             if (buildType != null)
                 buildType = Util.fixNull(req.getParameter("dimensionsbuilder.projectType").trim());
             if (buildStage != null)
                 buildStage = Util.fixNull(req.getParameter("dimensionsbuilder.projectStage").trim());
 
 
             DimensionsBuilder notif = new DimensionsBuilder(
                                                         area,buildConfig,
                                                         buildOptions,buildTargets,buildType,
                                                         buildStage,
                                                         batch,buildClean,capture,
                                                         audit,populate,touch);
 
             return notif;
         }
 
 
         /**
          * This human readable name is used in the configuration screen.
          */
         public String getDisplayName() {
             return "Execute Dimensions Build";
         }
 
         public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
             // to persist global configuration information,
             // set that to properties and call save().
             save();
             return super.configure(req);
         }
     }
 }
 
