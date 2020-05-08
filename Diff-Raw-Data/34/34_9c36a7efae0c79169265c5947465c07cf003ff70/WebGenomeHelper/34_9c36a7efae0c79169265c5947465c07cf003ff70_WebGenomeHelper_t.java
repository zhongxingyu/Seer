 package gov.nih.nci.rembrandt.web.helper;
 
 import gov.nih.nci.rembrandt.web.bean.ReportBean;
 import gov.nih.nci.rembrandt.queryservice.resultset.Resultant;
 import gov.nih.nci.rembrandt.queryservice.resultset.ResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.DimensionalViewContainer;
 import gov.nih.nci.rembrandt.queryservice.resultset.copynumber.CopyNumberSingleViewResultsContainer;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ge.StartEndPosition;
 import gov.nih.nci.rembrandt.queryservice.queryprocessing.ge.ChrRegionCriteriaHandler;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.util.PropertyLoader;
 import gov.nih.nci.caintegrator.dto.de.ChromosomeNumberDE;
 import gov.nih.nci.caintegrator.dto.de.CytobandDE;
 import gov.nih.nci.caintegrator.dto.critieria.RegionCriteria;
 import gov.nih.nci.caIntegrator.services.appState.dto.RBTReportStateDTO;
 import gov.nih.nci.caIntegrator.services.appState.ejb.RBTApplicationStateTracker;
 import gov.nih.nci.caIntegrator.services.appState.ejb.RBTApplicationStateTrackerHome;
 import gov.nih.nci.caIntegrator.services.appState.ApplicationStateTrackerHome;
 import gov.nih.nci.caIntegrator.services.appState.ApplicationStateTracker;
 import gov.nih.nci.caIntegrator.services.util.ServiceLocator;
 
 import java.util.*;
 import java.rmi.NoSuchObjectException;
 
 import org.apache.log4j.Logger;
 import org.apache.ojb.broker.PersistenceBroker;
 import org.apache.ojb.broker.PersistenceBrokerFactory;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Ram Bhattaru
  * Date: Nov 10, 2005
  * Time: 4:15:39 PM
 
  */
 public class WebGenomeHelper {
     private static Logger _logger = Logger.getLogger(WebGenomeHelper.class);
     public static String buildURL(ReportBean report, String sessionID) throws Exception {
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
            // 1. convert cytobands in to respective start and end positions
            cytobands = copyNumberContainer.getCytobandNames();
            positions = convertCytobands(cytobands);
                      // 2. format samples in to groups and store under groupName
            reporterNames = copyNumberContainer.getReporterNames();
            groups = copyNumberContainer.getGroupsLabels();
             for (Object group : groups) {
                 String groupName = (String) group;
                 sampleIds = copyNumberContainer.getBiospecimenLabels(groupName);
                 String[] sampleIDArray = new String[sampleIds.size()];
                 sampleIDArray = (String[]) sampleIds.toArray(sampleIDArray);
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
          String hostURL = PropertyLoader.loadProperties(RembrandtConstants.WEB_GENOMEAPP_PROPERTIES).
                          getProperty("webGenome.hostURL");
 
          _logger.debug("Web Genome URL Retrieved: " + hostURL);
 
          // 4. concatenate url & params
          String webGenomeURL = hostURL + "?" + urlParams;
 
          return webGenomeURL;
     }
 
     private static RBTReportStateDTO buildReportStateDTO(CopyNumberSingleViewResultsContainer copyNumberContainer, Map<ChromosomeNumberDE, StartEndPosition> positions, Collection groups,  String sessionID)
     throws Exception {
         List reporterNames =  null;
         Collection sampleIds = null;
 
         RBTReportStateDTO dto = new RBTReportStateDTO();
 
         HashMap groupsWithSamples = new HashMap();
           List cytobands=null;
           if(copyNumberContainer != null)	{
              // 1. convert cytobands in to respective start and end positions
               cytobands = copyNumberContainer.getCytobandNames();
               positions.putAll(convertCytobands(cytobands));
 
               // 2. format samples in to groups and store under groupName
               reporterNames = copyNumberContainer.getReporterNames();
               groups.add(copyNumberContainer.getGroupsLabels());
               for (Iterator groupNamesIterator = groups.iterator(); groupNamesIterator.hasNext();) {
                   String groupName = (String) groupNamesIterator.next();
                   sampleIds = copyNumberContainer.getBiospecimenLabels(groupName);
 
                   String[] sampleIDArray = new String[sampleIds.size()];
                   sampleIDArray = (String[]) sampleIds.toArray(sampleIDArray);
                   groupsWithSamples.put(groupName, sampleIDArray);
               }
           }
 
           dto.setCytobands(cytobands);
           dto.setGroups(groupsWithSamples);
           dto.setSelectedReporerNames(reporterNames);
           dto.setUserID(sessionID);
 
 
         return dto;
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
 
     private static Integer publishState(RBTReportStateDTO dto) throws Exception {
         Integer stateID = null;
         ServiceLocator locator = null;
         try {
            stateID = publishReportState(dto);
         }catch(NoSuchObjectException e) {
            /* means that EJB container must have restared Hence the old reference is stale
               So clear the cahche by simply getting a new instance of ServiceLocator
            */
            ServiceLocator.getInstance().setInstance(null);
            stateID = publishReportState(dto);
         } catch(Throwable t) {
             _logger.error("Error in publishing the RBTApplicationState.  Error:", t);
             throw new Exception("Error in publishing the RBTApplicationState.  Error:" +
                                 t.toString() );
         }
         return stateID;
     }
 
    private static Integer publishReportState(RBTReportStateDTO dto) throws CreateException, Exception {
        ServiceLocator locator;
        Integer stateID;
        locator = ServiceLocator.getInstance();
        Object h = locator.locateHome(null, RBTApplicationStateTrackerHome.JNDI_NAME,
                                        ApplicationStateTrackerHome.class);
        ApplicationStateTrackerHome home = (ApplicationStateTrackerHome)h;
        ApplicationStateTracker  service = home.create();
        stateID = service.publishReportState(dto);
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
 
                 final PersistenceBroker pb = PersistenceBrokerFactory.defaultPersistenceBroker();
                 RegionCriteria regCrit = new RegionCriteria();
                 ChromosomeNumberDE chromosomeDE = new ChromosomeNumberDE(chrNumber);
                 regCrit.setCytoband(new CytobandDE(cytobandWithoutChrNumber));
                 StartEndPosition position = ChrRegionCriteriaHandler.getStartEndPostions(pb, regCrit, chromosomeDE);
                 cytoStartEndPos.put(chromosomeDE, position);
             }
 
             return cytoStartEndPos;
         }
 
 }
