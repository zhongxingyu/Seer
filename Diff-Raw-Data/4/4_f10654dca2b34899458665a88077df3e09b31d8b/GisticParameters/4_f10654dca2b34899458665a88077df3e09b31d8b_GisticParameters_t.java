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
 package gov.nih.nci.caintegrator2.application.analysis.grid.gistic;
 
 import edu.wustl.icr.asrv1.common.GenomeAnnotationInformation;
 import gov.nih.nci.caintegrator2.common.GenePatternUtil;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.genomic.SampleSet;
 import gov.nih.nci.caintegrator2.external.ServerConnectionProfile;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.cabig.icr.asbp.parameter.Parameter;
 import org.cabig.icr.asbp.parameter.ParameterList;
 
 /**
  * Parameters to run Gistic Gene Pattern grid service.
  */
 public class GisticParameters {
     private static final String REMOVE_X_YES = "Yes";
     private static final String REMOVE_X_NO = "No";
     private static final int REMOVE_X_YES_VALUE = 1;
     private static final int REMOVE_X_NO_VALUE = 0;
    private static final Float DEFAULT_AMPLIFICATIONS = .1f;
    private static final Float DEFAULT_DELETIONS = .1f;
     private static final Float DEFAULT_QV_THRESH = .25f;
     
     private Query clinicalQuery;
     private SampleSet excludeControlSampleSet;
     private File cnvSegmentsToIgnoreFile;
     private ServerConnectionProfile server;
     private GisticRefgeneFileEnum refgeneFile;
     private Float amplificationsThreshold = DEFAULT_AMPLIFICATIONS;
     private Float deletionsThreshold = DEFAULT_DELETIONS;
     private Integer joinSegmentSize = 4;
     private Float qvThresh = DEFAULT_QV_THRESH;
     private String extension = ".gp_gistic";
     private Integer removeX = REMOVE_X_YES_VALUE;
 
 
     /**
      * @return the server
      */
     public ServerConnectionProfile getServer() {
         return server;
     }
 
     /**
      * @param server the server to set
      */
     public void setServer(ServerConnectionProfile server) {
         this.server = server;
     }
 
     /**
      * 
      * @return options list for the "removeX" parameter.
      */
     public Map<Integer, String> getRemoveXOptions() {
         Map<Integer, String> options = new HashMap<Integer, String>();
         options.put(REMOVE_X_YES_VALUE, REMOVE_X_YES);
         options.put(REMOVE_X_NO_VALUE, REMOVE_X_NO);
         return options;
     }
     
     /**
      * Creates the parameter list from the parameters given by user.
      * @return parameters to run grid job.
      */
     public ParameterList createParameterList() {
         ParameterList parameterList = new ParameterList();
         Parameter[] params = new Parameter[6];
         params[0] = GenePatternUtil.createParameter("amplifications.threshold", amplificationsThreshold);
         params[1] = GenePatternUtil.createParameter("deletions.threshold", deletionsThreshold);
         params[2] = GenePatternUtil.createParameter("join.segment.size", joinSegmentSize);
         params[3] = GenePatternUtil.createParameter("qv.thresh", qvThresh);
         params[4] = GenePatternUtil.createParameter("extension", extension);
         params[5] = GenePatternUtil.createParameter("remove.X", removeX);
         
         parameterList.setParameterCollection(params);
         return parameterList;       
     }
     
     /**
      * The genomeBuild information.
      * @return the genomeBuild.
      */
     public GenomeAnnotationInformation createGenomeBuild() {
         GenomeAnnotationInformation genomeBuild = new GenomeAnnotationInformation();
         genomeBuild.setSource("NCBI"); // Unsure what this means.  Got it from the demo client.
         genomeBuild.setBuild(refgeneFile.getValue());
         return genomeBuild;
     }
 
     /**
      * @return the amplificationThreshold
      */
     public Float getAmplificationsThreshold() {
         return amplificationsThreshold;
     }
 
     /**
      * @param amplificationsThreshold the amplificationThreshold to set
      */
     public void setAmplificationsThreshold(Float amplificationsThreshold) {
         this.amplificationsThreshold = amplificationsThreshold;
     }
 
     /**
      * @return the deletionsThreshold
      */
     public Float getDeletionsThreshold() {
         return deletionsThreshold;
     }
 
     /**
      * @param deletionsThreshold the deletionsThreshold to set
      */
     public void setDeletionsThreshold(Float deletionsThreshold) {
         this.deletionsThreshold = deletionsThreshold;
     }
 
     /**
      * @return the joinSegmentSize
      */
     public Integer getJoinSegmentSize() {
         return joinSegmentSize;
     }
 
     /**
      * @param joinSegmentSize the joinSegmentSize to set
      */
     public void setJoinSegmentSize(Integer joinSegmentSize) {
         this.joinSegmentSize = joinSegmentSize;
     }
 
     /**
      * @return the qvThresh
      */
     public Float getQvThresh() {
         return qvThresh;
     }
 
     /**
      * @param qvThresh the qvThresh to set
      */
     public void setQvThresh(Float qvThresh) {
         this.qvThresh = qvThresh;
     }
 
     /**
      * @return the extension
      */
     public String getExtension() {
         return extension;
     }
 
     /**
      * @param extension the extension to set
      */
     public void setExtension(String extension) {
         this.extension = extension;
     }
 
     /**
      * @return the removeX
      */
     public Integer getRemoveX() {
         return removeX;
     }
 
     /**
      * @param removeX the removeX to set
      */
     public void setRemoveX(Integer removeX) {
         this.removeX = removeX;
     }
 
     /**
      * @return the clinicalQuery
      */
     public Query getClinicalQuery() {
         return clinicalQuery;
     }
 
     /**
      * @param clinicalQuery the clinicalQuery to set
      */
     public void setClinicalQuery(Query clinicalQuery) {
         this.clinicalQuery = clinicalQuery;
     }
 
     /**
      * @return the cnvSegmentsToIgnoreFile
      */
     public File getCnvSegmentsToIgnoreFile() {
         return cnvSegmentsToIgnoreFile;
     }
 
     /**
      * @param cnvSegmentsToIgnoreFile the cnvSegmentsToIgnoreFile to set
      */
     public void setCnvSegmentsToIgnoreFile(File cnvSegmentsToIgnoreFile) {
         this.cnvSegmentsToIgnoreFile = cnvSegmentsToIgnoreFile;
     }
 
     /**
      * @return the refgeneFile
      */
     public GisticRefgeneFileEnum getRefgeneFile() {
         return refgeneFile;
     }
 
     /**
      * @param refgeneFile the refgeneFile to set
      */
     public void setRefgeneFile(GisticRefgeneFileEnum refgeneFile) {
         this.refgeneFile = refgeneFile;
     }
 
     /**
      * @return the excludeControlSampleSet
      */
     public SampleSet getExcludeControlSampleSet() {
         return excludeControlSampleSet;
     }
 
     /**
      * @param excludeControlSampleSet the excludeControlSampleSet to set
      */
     public void setExcludeControlSampleSet(SampleSet excludeControlSampleSet) {
         this.excludeControlSampleSet = excludeControlSampleSet;
     }
 }
