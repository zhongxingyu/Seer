 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caIntegrator2
  * Software was developed in conjunction with the National Cancer Institute 
  * (NCI) by NCI employees, 5AM Solutions, Inc. (5AM), ScenPro, Inc. (ScenPro)
  * and Science Applications International Corporation (SAIC). To the extent 
  * government employees are authors, any rights in such works shall be subject 
  * to Title 17 of the United States Code, section 105. 
  *
  * This caIntegrator2 Software License (the License) is between NCI and You. You (or 
  * Your) shall mean a person or an entity, and all other entities that control, 
  * are controlled by, or are under common control with the entity. Control for 
  * purposes of this definition means (i) the direct or indirect power to cause 
  * the direction or management of such entity, whether by contract or otherwise,
  * or (ii) ownership of fifty percent (50%) or more of the outstanding shares, 
  * or (iii) beneficial ownership of such entity. 
  *
  * This License is granted provided that You agree to the conditions described 
  * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, 
  * no-charge, irrevocable, transferable and royalty-free right and license in 
  * its rights in the caIntegrator2 Software to (i) use, install, access, operate, 
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caIntegrator2 Software; (ii) distribute and 
  * have distributed to and by third parties the caIntegrator2 Software and any 
  * modifications and derivative works thereof; and (iii) sublicense the 
  * foregoing rights set out in (i) and (ii) to third parties, including the 
  * right to license such rights to further third parties. For sake of clarity, 
  * and not by way of limitation, NCI shall have no right of accounting or right 
  * of payment from You or Your sub-licensees for the rights granted under this 
  * License. This License is granted at no charge to You.
  *
  * Your redistributions of the source code for the Software must retain the 
  * above copyright notice, this list of conditions and the disclaimer and 
  * limitation of liability of Article 6, below. Your redistributions in object 
  * code form must reproduce the above copyright notice, this list of conditions 
  * and the disclaimer of Article 6 in the documentation and/or other materials 
  * provided with the distribution, if any. 
  *
  * Your end-user documentation included with the redistribution, if any, must 
  * include the following acknowledgment: This product includes software 
  * developed by 5AM, ScenPro, SAIC and the National Cancer Institute. If You do 
  * not include such end-user documentation, You shall include this acknowledgment 
  * in the Software itself, wherever such third-party acknowledgments normally 
  * appear.
  *
  * You may not use the names "The National Cancer Institute", "NCI", "ScenPro",
  * "SAIC" or "5AM" to endorse or promote products derived from this Software. 
  * This License does not authorize You to use any trademarks, service marks, 
  * trade names, logos or product names of either NCI, ScenPro, SAID or 5AM, 
  * except as required to comply with the terms of this License. 
  *
  * For sake of clarity, and not by way of limitation, You may incorporate this 
  * Software into Your proprietary programs and into any third party proprietary 
  * programs. However, if You incorporate the Software into third party 
  * proprietary programs, You agree that You are solely responsible for obtaining
  * any permission from such third parties required to incorporate the Software 
  * into such third party proprietary programs and for informing Your a
  * sub-licensees, including without limitation Your end-users, of their 
  * obligation to secure any required permissions from such third parties before 
  * incorporating the Software into such third party proprietary software 
  * programs. In the event that You fail to obtain such permissions, You agree 
  * to indemnify NCI for any claims against NCI by such third parties, except to 
  * the extent prohibited by law, resulting from Your failure to obtain such 
  * permissions. 
  *
  * For sake of clarity, and not by way of limitation, You may add Your own 
  * copyright statement to Your modifications and to the derivative works, and 
  * You may provide additional or different license terms and conditions in Your 
  * sublicenses of modifications of the Software, or any derivative works of the 
  * Software as a whole, provided Your use, reproduction, and distribution of the
  * Work otherwise complies with the conditions stated in this License.
  *
  * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
  * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, 
  * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO 
  * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC., SCENPRO, INC.,
  * SCIENCE APPLICATIONS INTERNATIONAL CORPORATION OR THEIR 
  * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.caintegrator2.web;
 
 import gov.nih.nci.caintegrator2.application.geneexpression.GeneExpressionPlotGroup;
 import gov.nih.nci.caintegrator2.application.kmplot.KMPlot;
 import gov.nih.nci.caintegrator2.application.kmplot.PlotTypeEnum;
 import gov.nih.nci.caintegrator2.application.workspace.WorkspaceService;
 import gov.nih.nci.caintegrator2.domain.application.UserWorkspace;
 import gov.nih.nci.caintegrator2.security.SecurityHelper;
 import gov.nih.nci.caintegrator2.web.action.analysis.KMPlotMapper;
 import gov.nih.nci.caintegrator2.web.action.analysis.geneexpression.GEPlotMapper;
 import gov.nih.nci.logging.api.util.StringUtils;
 
 import java.util.Map;
 
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.util.ValueStack;
 
 /**
  * Stores helper variables to our session.
  */
 public final class SessionHelper {
     private static final String SESSION_HELPER_SESSION_KEY = "sessionHelper"; 
     private static final String DISPLAYABLE_USER_WORKSPACE_SESSION_KEY = "displayableWorkspace";
     private static final String DISPLAYABLE_USER_WORKSPACE_VALUE_STACK_KEY = "displayableWorkspace";
     private static final String KM_PLOT_SESSION_KEY = "kmPlot";
     private static final String GE_PLOT_SESSION_KEY = "gePlot";
     private static final String ANONYMOUS_USER_WORKSPACE_SESSION_KEY = "anonymousUserWorkspace";
     private static final String IS_AUTHORIZED_PAGE = "isAuthorizedPage";
     private Boolean studyManager = null;
     private Boolean platformManager = null;
     private Boolean invalidDataBeingAccessed = false;
     
     private SessionHelper() {
         
     }
     
     /**
      * Singleton method to get the instance off of the sessionMap object or create a new one.
      * @return - CSMUserHelper object.
      */
     public static SessionHelper getInstance() {
         SessionHelper instance = 
             (SessionHelper) getSession().get(SESSION_HELPER_SESSION_KEY);
         if (instance == null) {
             instance = new SessionHelper();
             getSession().put(SESSION_HELPER_SESSION_KEY, instance);
         }
         return instance;
     }
     
     /**
      * Refreshes the objects held by this object so that that are up to date and attached
      * to the current Hibernate session. After calling this method, clients can expect the
      * following objects on the object stack.
      * <ul>
      *  <li><b>displayableWorkspace</b> - The current <code>DisplayableUserWorkspace</code></li>
      *  <li><b>workspace</b> - The current <code>UserWorkspace</code></li>
      *  <li><b>studySubscription</b> - The current <code>StudySubscription</code> if one has been selected</li>
      *  <li><b>study</b> - The current <code>Study</code> if one has been selected</li>
      *  <li><b>query</b> - The current <code>Query</code> if one has been selected or is being defined</li>
      *  <li><b>queryResult</b> - The current <code>QueryResult</code> from the last clinical query executed</li>
      *  <li><b>genomicDataQueryResult</b> - The current <code>GenomicDataQueryResult</code> from the last 
      *  genomic data query executed</li>
      * </ul>
      * 
      * @param workspaceService session used to retrieve workspace.
      * @param isStudyNeedRefresh determines if we need to refresh study on the stack or not.
      */
     public void refresh(WorkspaceService workspaceService, boolean isStudyNeedRefresh) {
         if (isAuthenticated()) {
             getDisplayableUserWorkspace().refresh(workspaceService, isStudyNeedRefresh);
             getValueStack().set(DISPLAYABLE_USER_WORKSPACE_VALUE_STACK_KEY, getDisplayableUserWorkspace());
         }
     }
 
     /**
      * @return the username
      */
     public static String getUsername() {
         return SecurityHelper.getCurrentUsername();
     }
 
     /**
      * @return the authenticated
      */
     public boolean isAuthenticated() {
         return getUsername() != null;
     }
     
     /**
      * 
      * @return if user is anonymously logged in.
      */
     public static boolean isAnonymousUser() {
         return UserWorkspace.ANONYMOUS_USER_NAME.equals(getUsername());
     }
 
     /**
      * @return the displayableUserWorkspace
      */
     public DisplayableUserWorkspace getDisplayableUserWorkspace() {
         DisplayableUserWorkspace displayableUserWorkspace =
             (DisplayableUserWorkspace) getSession().get(DISPLAYABLE_USER_WORKSPACE_SESSION_KEY);
         if (displayableUserWorkspace == null && isAuthenticated()) {
             displayableUserWorkspace = new DisplayableUserWorkspace();
             getSession().put(DISPLAYABLE_USER_WORKSPACE_SESSION_KEY, displayableUserWorkspace);
         }
         return displayableUserWorkspace;
     }
 
     private static Map<String, Object> getSession() {
         return ActionContext.getContext().getSession();
     }
 
     private ValueStack getValueStack() {
         return ActionContext.getContext().getValueStack();
     }
 
     /**
      * @return the studyManager
      */
     public boolean isStudyManager() {
         if (isAnonymousUser()) {
             return false;
         }
         if (studyManager == null && !StringUtils.isBlank(getUsername())) {
             setStudyManager(SecurityHelper.isStudyManager(getUsername()));
         }
         return studyManager;
     }
 
     public void setStudyManager(boolean studyManager) {
         this.studyManager = studyManager;
     }
 
     /**
      * @return the platformManager
      */
     public boolean isPlatformManager() {
         if (isAnonymousUser()) {
             return false;
         }
         if (platformManager == null && !StringUtils.isBlank(getUsername())) {
             setPlatformManager(SecurityHelper.isPlatformManager(getUsername()));
         }
         return platformManager;
     }
 
     /**
      * @param platformManager the platformManager to set
      */
     public void setPlatformManager(Boolean platformManager) {
         this.platformManager = platformManager;
     }
     
     /**
      * 
      *  @param anonymousUserWorkspace anonymous user workspace to put on the session.
      */
     public static void setAnonymousUserWorkspace(UserWorkspace anonymousUserWorkspace) {
         getSession().put(ANONYMOUS_USER_WORKSPACE_SESSION_KEY, anonymousUserWorkspace);
     }
     
     /**
      * 
      * @return anonymous user workspace on the session.
      */
     public static UserWorkspace getAnonymousUserWorkspace() {
         return (UserWorkspace) getSession().get(ANONYMOUS_USER_WORKSPACE_SESSION_KEY);
     }
     
     
     /**
      * Uses a KMPlotMapper object to store multiple KMPlots on the value stack, based on
      * one per plot type.
      * @param plotType the plot type to add the plot to.
      * @param kmPlot the kmPlot to set on the ValueStack.
      */
     public static void setKmPlot(PlotTypeEnum plotType, KMPlot kmPlot) {
         if (getSession().get(KM_PLOT_SESSION_KEY) == null) {
             getSession().put(KM_PLOT_SESSION_KEY, new KMPlotMapper());
         }
         KMPlotMapper map = (KMPlotMapper) getSession().get(KM_PLOT_SESSION_KEY);
         map.getKmPlotMap().put(plotType, kmPlot);
     }
     
     /**
      * 
      * @return the kmPlot on the ValueStack.
      */
     public static KMPlot getAnnotationBasedKmPlot() {
         KMPlotMapper map = (KMPlotMapper) getSession().get(KM_PLOT_SESSION_KEY);
         if (map != null) {
             return map.getAnnotationBasedKmPlot();
         }
         return null;
     }
     
     /**
      * 
      * @return the kmPlot on the ValueStack.
      */
     public static KMPlot getGeneExpressionBasedKmPlot() {
         KMPlotMapper map = (KMPlotMapper) getSession().get(KM_PLOT_SESSION_KEY);
         if (map != null) {
             return map.getGeneExpressionBasedKmPlot();
         }
         return null;
     }
     
     /**
      * 
      * @return the kmPlot on the ValueStack.
      */
     public static KMPlot getQueryBasedKmPlot() {
         KMPlotMapper map = (KMPlotMapper) getSession().get(KM_PLOT_SESSION_KEY);
         if (map != null) {
             return map.getQueryBasedKmPlot();
         }
         return null;
     }
     
     /**
      * Clears all plots off the session.
      */
     public static void clearKmPlots() {
         KMPlotMapper mapper = (KMPlotMapper) getSession().get(KM_PLOT_SESSION_KEY);
         if (mapper != null) {
             mapper.clear();
         }
     }
     
     /**
      * Uses a GEPlotMapper object to store multiple GEPlots on the value stack, based on
      * one per plot type.
      * @param plotType the plot type to add the plot to.
      * @param gePlots the gePlots to set on the ValueStack.
      */
     public static void setGePlots(PlotTypeEnum plotType, GeneExpressionPlotGroup gePlots) {
         if (getSession().get(GE_PLOT_SESSION_KEY) == null) {
             getSession().put(GE_PLOT_SESSION_KEY, new GEPlotMapper());
         }
         GEPlotMapper map = (GEPlotMapper) getSession().get(GE_PLOT_SESSION_KEY);
         map.getGePlotMap().put(plotType, gePlots);
     }
 
     /**
      * 
      * @return the Annotation gePlots on the ValueStack.
      */
     public static GeneExpressionPlotGroup getAnnotationBasedGePlots() {
         GEPlotMapper map = (GEPlotMapper) getSession().get(GE_PLOT_SESSION_KEY);
         if (map != null) {
             return map.getAnnotationBasedGePlot();
         }
         return null;
     }
     
     /**
      * 
      * @return the Genomic Query gePlots on the ValueStack.
      */
     public static GeneExpressionPlotGroup getGenomicQueryBasedGePlots() {
         GEPlotMapper map = (GEPlotMapper) getSession().get(GE_PLOT_SESSION_KEY);
         if (map != null) {
             return map.getGenomicQueryBasedGePlot();
         }
         return null;
     }
     
     /**
      * 
      * @return the Clinical Query gePlots on the ValueStack.
      */
     public static GeneExpressionPlotGroup getClinicalQueryBasedGePlots() {
         GEPlotMapper map = (GEPlotMapper) getSession().get(GE_PLOT_SESSION_KEY);
         if (map != null) {
             return map.getClinicalQueryBasedGePlot();
         }
         return null;
     }
 
     /**
      * Clears all plots off the session.
      */
     public static void clearGePlots() {
         GEPlotMapper mapper = (GEPlotMapper) getSession().get(GE_PLOT_SESSION_KEY);
         if (mapper != null) {
             mapper.clear();
         }
     }
     
     /**
      * Determines if the currently requested page is authorized for user.
      * @return T/F value.
      */
     public Boolean isAuthorizedPage() {
         Boolean isAuthorizedPage = (Boolean) getSession().get(IS_AUTHORIZED_PAGE);
         if (isAuthorizedPage == null) {
             return true;
         }
         return isAuthorizedPage;
     }
     
     /**
      * Sets the flag for the requested page to be either T/F.
      * @param isAuthorized if the user is authorized or not to see page.
      */
     public void setAuthorizedPage(Boolean isAuthorized) {
         getSession().put(IS_AUTHORIZED_PAGE, isAuthorized);
     }
 
     /**
      * @return the invalidDataBeingAccessed
      */
     public Boolean getInvalidDataBeingAccessed() {
         return invalidDataBeingAccessed;
     }
 
     /**
      * @param invalidDataBeingAccessed the invalidDataBeingAccessed to set
      */
     public void setInvalidDataBeingAccessed(Boolean invalidDataBeingAccessed) {
         this.invalidDataBeingAccessed = invalidDataBeingAccessed;
     }
 }
