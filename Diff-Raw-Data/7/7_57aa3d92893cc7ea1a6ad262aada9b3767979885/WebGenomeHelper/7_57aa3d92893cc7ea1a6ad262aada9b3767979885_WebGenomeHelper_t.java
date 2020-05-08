 package gov.nih.nci.rembrandt.web.helper;
 
 import gov.nih.nci.caIntegrator.services.appState.ApplicationStateTracker;
 import gov.nih.nci.caIntegrator.services.appState.ApplicationStateTrackerHome;
 import gov.nih.nci.caIntegrator.services.appState.dto.RBTReportStateDTO;
 import gov.nih.nci.caIntegrator.services.appState.ejb.RBTApplicationStateTrackerHome;
 import gov.nih.nci.caIntegrator.services.util.ServiceLocator;
 import gov.nih.nci.caintegrator.dto.critieria.RegionCriteria;
 import gov.nih.nci.caintegrator.dto.de.BioSpecimenIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.ChromosomeNumberDE;
 import gov.nih.nci.caintegrator.dto.de.CytobandDE;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ge.ChrRegionCriteriaHandler;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ge.StartEndPosition;
 import gov.nih.nci.rembrandt.queryservice.resultset.DimensionalViewContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.Resultant;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.copynumber.CopyNumberSingleViewResultsContainer;
 import gov.nih.nci.rembrandt.web.bean.ReportBean;
 
 import java.rmi.NoSuchObjectException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.ejb.CreateException;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import org.apache.log4j.Logger;
 import org.apache.ojb.broker.PersistenceBroker;
 import org.apache.ojb.broker.PersistenceBrokerFactory;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Ram Bhattaru
  * Date: Nov 10, 2005
  * Time: 4:15:39 PM
 
  */
 
 
 /**
 * caIntegrator License
 * 
 * Copyright 2001-2005 Science Applications International Corporation ("SAIC"). 
 * The software subject to this notice and license includes both human readable source code form and machine readable, 
 * binary, object code form ("the caIntegrator Software"). The caIntegrator Software was developed in conjunction with 
 * the National Cancer Institute ("NCI") by NCI employees and employees of SAIC. 
 * To the extent government employees are authors, any rights in such works shall be subject to Title 17 of the United States
 * Code, section 105. 
 * This caIntegrator Software License (the "License") is between NCI and You. "You (or "Your") shall mean a person or an 
 * entity, and all other entities that control, are controlled by, or are under common control with the entity. "Control" 
 * for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity,
 *  whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) 
 * beneficial ownership of such entity. 
 * This License is granted provided that You agree to the conditions described below. NCI grants You a non-exclusive, 
 * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and royalty-free right and license in its rights 
 * in the caIntegrator Software to (i) use, install, access, operate, execute, copy, modify, translate, market, publicly 
 * display, publicly perform, and prepare derivative works of the caIntegrator Software; (ii) distribute and have distributed 
 * to and by third parties the caIntegrator Software and any modifications and derivative works thereof; 
 * and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties, including the right to license such 
 * rights to further third parties. For sake of clarity, and not by way of limitation, NCI shall have no right of accounting
 * or right of payment from You or Your sublicensees for the rights granted under this License. This License is granted at no
 * charge to You. 
 * 1. Your redistributions of the source code for the Software must retain the above copyright notice, this list of conditions
 *    and the disclaimer and limitation of liability of Article 6, below. Your redistributions in object code form must reproduce 
 *    the above copyright notice, this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials
 *    provided with the distribution, if any. 
 * 2. Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This 
 *    product includes software developed by SAIC and the National Cancer Institute." If You do not include such end-user 
 *    documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments 
 *    normally appear.
 * 3. You may not use the names "The National Cancer Institute", "NCI" "Science Applications International Corporation" and 
 *    "SAIC" to endorse or promote products derived from this Software. This License does not authorize You to use any 
 *    trademarks, service marks, trade names, logos or product names of either NCI or SAIC, except as required to comply with
 *    the terms of this License. 
 * 4. For sake of clarity, and not by way of limitation, You may incorporate this Software into Your proprietary programs and 
 *    into any third party proprietary programs. However, if You incorporate the Software into third party proprietary 
 *    programs, You agree that You are solely responsible for obtaining any permission from such third parties required to 
 *    incorporate the Software into such third party proprietary programs and for informing Your sublicensees, including 
 *    without limitation Your end-users, of their obligation to secure any required permissions from such third parties 
 *    before incorporating the Software into such third party proprietary software programs. In the event that You fail 
 *    to obtain such permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to 
 *    the extent prohibited by law, resulting from Your failure to obtain such permissions. 
 * 5. For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and 
 *    to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses 
 *    of modifications of the Software, or any derivative works of the Software as a whole, provided Your use, reproduction, 
 *    and distribution of the Work otherwise complies with the conditions stated in this License.
 * 6. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, 
 *    THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. 
 *    IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 *    GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 *    OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
 
 public class WebGenomeHelper {
 
     private static Logger _logger = Logger.getLogger(WebGenomeHelper.class);
 
    public static String buildURL(ReportBean report, String sessionID, String hostURL, List<String> specimenNames) throws Exception {
         Resultant resultant = report.getResultant();
         ResultsContainer  resultsContainer = resultant.getResultsContainer();
 
         CopyNumberSingleViewResultsContainer copyNumberContainer = null;
         Collection groups = null;
         Map<ChromosomeNumberDE, StartEndPosition> positions = null;
         List reporterNames =  null;
         Collection sampleIds = null;
 
         if(resultsContainer instanceof DimensionalViewContainer)	{
             DimensionalViewContainer dimensionalViewContainer = (DimensionalViewContainer) resultsContainer;
             if(dimensionalViewContainer != null)	{
                 copyNumberContainer = dimensionalViewContainer.getCopyNumberSingleViewContainer();
             }
         }
         else if(resultsContainer instanceof CopyNumberSingleViewResultsContainer)	{ //for single
             copyNumberContainer = (CopyNumberSingleViewResultsContainer) resultsContainer;
         }
 
         RBTReportStateDTO dto = new RBTReportStateDTO();
         HashMap groupsWithSamples = new HashMap();
         List cytobands=null;
 
         if(copyNumberContainer != null)	{
 
             // 0. convert cytobands in to respective start and end positions
            cytobands = copyNumberContainer.getCytobandNames();
            positions = convertCytobands(cytobands);
 
            // 1. format samples in to groups and store under groupName
            reporterNames = copyNumberContainer.getReporterNames();
            groups = copyNumberContainer.getGroupsLabels();
             for (Object group : groups) {
                 String groupName = (String) group;
                 sampleIds = copyNumberContainer.getBiospecimenLabels(groupName);
                 List<String> specimenList = new ArrayList<String>();
                 for(Object obj:sampleIds){
                 	BioSpecimenIdentifierDE bioSpecimenIdentifierDE = (BioSpecimenIdentifierDE) obj;
                	if(specimenNames.contains(bioSpecimenIdentifierDE.getSpecimenName())){
                		specimenList.add(bioSpecimenIdentifierDE.getSpecimenName());
                	}
                 }
                 String[] sampleIDArray = specimenList.toArray(new String[specimenList.size()]);
                 groupsWithSamples.put(groupName, sampleIDArray);
             }
         }
         dto.setCytobands(cytobands);
         dto.setGroups(groupsWithSamples);
         dto.setSelectedReporerNames(reporterNames);
         dto.setUserID(sessionID);
 
         // 2. build the URL params to be sent out to WebGenome request
         String urlParams = buildURLParams(groups, positions, dto);
 
         // 3. retrieve the where webGenome app is hosted from property file
         //String hostURL = PropertyLoader.loadProperties(RembrandtConstants.WEB_GENOMEAPP_PROPERTIES).
           //              getProperty("webGenome.hostURL");
 
         _logger.debug("Web Genome URL Retrieved: " + hostURL);
 
         // 4. concatenate url & params
         String webGenomeURL = hostURL + "&" + urlParams;
 
         return webGenomeURL;
     }
 
     private static String buildURLParams(Collection groups,  Map<ChromosomeNumberDE, StartEndPosition> positions, RBTReportStateDTO dto) throws Exception {
 
         String urlParams = "";
 
         // a. append experiment IDS to urlParams
         if (groups != null && groups.size() > 0) {
             String exptIDs = "exptIDs=";
             for (Iterator iterator = groups.iterator(); iterator.hasNext();)
                 exptIDs += (String) iterator.next() + ",";
             // remove the "," at the end
             urlParams = exptIDs.substring(0, exptIDs.length()-1);
         }
 
         // b. append cytoband start and end postions to urlParams
         if (positions != null && positions.size() > 0) {
             String positionsURL = "&intervals=";
             Set<ChromosomeNumberDE> chromosomes = positions.keySet();
             for (Iterator<ChromosomeNumberDE> iterator = chromosomes.iterator(); iterator.hasNext();) {
                 ChromosomeNumberDE chromosome =  iterator.next();
                 StartEndPosition pos = positions.get(chromosome);
                 positionsURL += chromosome.getValueObject() + ":"
                                 + pos.getStartPosition().getValueObject() + "-"
                                 + pos.getEndPosition().getValueObject() + ",";
             }
 
             // append to urlParams after removing last "," at the end
             urlParams += positionsURL.substring(0, positionsURL.length()-1);
         }
 
         // c. publish the application state and append the returned clientID to the urlParams
         urlParams += "&clientID=" + publishState(dto);
         return urlParams;
     }
 
     private static Integer publishState(RBTReportStateDTO dto) throws Exception  {
         Integer stateID = null;
         try {
         	stateID = publishReportState(dto);
         }catch(NoSuchObjectException e) {
             //* means that EJB container must have restared  Clear stale references with new instance */
             ServiceLocator.setInstance(null);
             try {
 				stateID = publishReportState(dto);
 			} catch (Exception e1) {
 				_logger.error(e);
 				throw e;
 			}
         } catch(Throwable t) {
             _logger.error("Error in publishing the RBTApplicationState.  Error:", t);
             throw new Exception("Error in publishing the RBTApplicationState.  Error:" +
                                 t.toString() );
         }
         return stateID;
     }
 
     private static Integer publishReportState(RBTReportStateDTO dto) throws Exception  {
     	Integer stateID = null;
         ServiceLocator locator = ServiceLocator.getInstance();
         Object h;
 		try {
 			h = locator.locateHome(null, RBTApplicationStateTrackerHome.JNDI_NAME,
 			                                ApplicationStateTrackerHome.class);
         ApplicationStateTrackerHome home = (ApplicationStateTrackerHome)h;
         ApplicationStateTracker  service = home.create();
         stateID = service.publishReportState(dto);
 		} catch (NamingException e) {
 			_logger.error(e);
 			throw e;
 		} catch (RemoteException e) {
 			_logger.error(e);
 			throw e;
 		} catch (CreateException e) {
 			_logger.error(e);
 			throw e;
 		} catch (Exception e) {
 			_logger.error(e);
 			throw e;
 		}
         return stateID;
     }
 
     private static Map<ChromosomeNumberDE, StartEndPosition> convertCytobands(List cytobands) throws Exception {
             // for each of these cytobands, get start and end positions
             Map<ChromosomeNumberDE, StartEndPosition> cytoStartEndPos = new HashMap<ChromosomeNumberDE, StartEndPosition>();
 
             for (int i = 0; i < cytobands.size(); i++) {
                 String cytoband =  (String) cytobands.get(i);
 
                 // split chromosome number and rest of cytoband first
                 int index = cytoband.indexOf('p', 0);
                 if (index == -1)
                    index = cytoband.indexOf('q',0);
                 String chrNumber = cytoband.substring(0, index);
                 String cytobandWithoutChrNumber = cytoband.substring(index, cytoband.length());
 
                 // TODO: move this getting pb instance to out side  loop
                 final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
                 RegionCriteria regCrit = new RegionCriteria();
                 ChromosomeNumberDE chromosomeDE = new ChromosomeNumberDE(chrNumber);
                 regCrit.setCytoband(new CytobandDE(cytobandWithoutChrNumber));
                 StartEndPosition position = ChrRegionCriteriaHandler.getStartEndPostions(pb, regCrit, chromosomeDE);
                 cytoStartEndPos.put(chromosomeDE, position);
                 pb.close();
             }
 
             return cytoStartEndPos;
         }
 
 }
