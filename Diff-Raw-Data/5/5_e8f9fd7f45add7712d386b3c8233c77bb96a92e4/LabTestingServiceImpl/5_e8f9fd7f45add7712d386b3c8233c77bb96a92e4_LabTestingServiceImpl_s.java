 package org.openmrs.module.jsslab.impl;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.api.APIException;
 import org.openmrs.api.impl.BaseOpenmrsService;
 import org.openmrs.module.jsslab.LabTestingService;
 import org.openmrs.module.jsslab.db.LabInstrument;
 import org.openmrs.module.jsslab.db.LabTestResult;
 import org.openmrs.module.jsslab.db.LabSpecimen;
 import org.openmrs.module.jsslab.db.LabReport;
 import org.openmrs.module.jsslab.db.LabTestRange;
 import org.openmrs.module.jsslab.db.LabTestRangeDAO;
 import org.openmrs.module.jsslab.db.LabTestSpecimen;
 import org.openmrs.module.jsslab.db.LabTestSpecimenDAO;
 import org.openmrs.module.jsslab.db.LabReport;
 import org.openmrs.module.jsslab.db.LabReportDAO;
 import org.openmrs.module.jsslab.db.LabTestResult;
 import org.openmrs.module.jsslab.db.LabTestResultDAO;
 
 
 
 public class LabTestingServiceImpl extends BaseOpenmrsService implements LabTestingService{
 
 	private final Log log = LogFactory.getLog(this.getClass());
 	
 	protected LabTestRangeDAO labTestRangeDAO;
 	
 	protected LabTestSpecimenDAO labTestSpecimenDAO;
 	
 	protected LabReportDAO labReportDAO;
 	
 	protected LabTestResultDAO labTestResultDAO;
 	
 	public void setLabTestRangeDAO(LabTestRangeDAO labTestRangeDAO)
 	{
 		this.labTestRangeDAO=labTestRangeDAO;
 	}
 	
 	public void setLabTestSpecimenDAO(LabTestSpecimenDAO labTestSpecimenDAO)
 	{
 		this.labTestSpecimenDAO=labTestSpecimenDAO;
 	}
 	
 	public void setLabReportDAO(LabReportDAO labReportDAO)
 	{
 		this.labReportDAO=labReportDAO;
 	}
 	
 	public void setLabTestResultDAO(LabTestResultDAO labTestResultDAO)
 	{
 		this.labTestResultDAO=labTestResultDAO;
 	}
 	
 	@Override
 	public LabTestRange getLabTestRange(Integer labTestRangeId) {		
 		return labTestRangeDAO.getLabTestRange(labTestRangeId);
 	}
 
 	@Override
 	public LabTestRange getLabTestRangeByUuid(String uuid) {		
 		return labTestRangeDAO.getLabTestRangeByUuid(uuid);
 	}
 
 
 	@Override
 	public LabTestRange saveLabTestRange(LabTestRange labTestRange)throws APIException {
 		return labTestRangeDAO.saveLabTestRange(labTestRange);
 	}
 
 	@Override
 	public void deleteLabTestRange(LabTestRange labTestRange, String reason)throws APIException {
 			labTestRange.setVoided(true);
 			labTestRange.setDateVoided(new Date());
 			labTestRange.setVoidReason(reason);
 			labTestRangeDAO.saveLabTestRange(labTestRange);
 			return;
 	}
 
 	@Override
 	public void purgeLabTestRange(LabTestRange labTestRange)throws APIException {
 		labTestRangeDAO.deleteLabTestRange(labTestRange);
 	}
 
 	@Override
 	public List<LabTestRange> getAllLabTestRanges(Boolean ifVoided)throws APIException{
 		return labTestRangeDAO.getAllLabTestRanges(ifVoided);
 	}
 
 	@Override
 	public List<LabTestRange> getLabTestRanges(String nameFragment,
 			Boolean includeVoided, Integer start, Integer length) {
 		return labTestRangeDAO.getLabTestRanges(nameFragment,
 				includeVoided, start, length);
 	}
 
 	@Override
 	public Integer getCountOfLabTestRanges(Boolean includeVoided)
 			throws APIException {
 		return labTestRangeDAO.getLabTestRanges("", includeVoided, null, null).size();
 	}
 
 //--------------------------------------------------------
 	
 	@Override
 	public LabTestSpecimen getLabTestSpecimen(Integer labTestSpecimenId) {
 		return labTestSpecimenDAO.getLabTestSpecimen(labTestSpecimenId);
 	}
 
 	@Override
 	public LabTestSpecimen getLabTestSpecimenByUuid(String uuid) {
 		return labTestSpecimenDAO.getLabTestSpecimenByUuid(uuid);
 	}
 
 	@Override
 	public LabTestSpecimen saveLabTestSpecimen(LabTestSpecimen labTestSpecimen)
 			throws APIException {
 		return labTestSpecimenDAO.saveLabTestSpecimen(labTestSpecimen);
 	}
 
 	@Override
 	public void purgeLabTestSpecimen(LabTestSpecimen labTestSpecimen)
 			throws APIException {
 		labTestSpecimenDAO.deleteLabTestSpecimen(labTestSpecimen);
 		
 	}
 
 	@Override
 	public LabTestSpecimen retireLabTestSpecimen(
 			LabTestSpecimen labTestSpecimen, String retireReason)
 			throws APIException {
 		labTestSpecimen.setRetired(true);
 		labTestSpecimen.setDateRetired(new Date());
 		labTestSpecimen.setRetireReason(retireReason);
 		return labTestSpecimenDAO.saveLabTestSpecimen(labTestSpecimen);
 	}
 
 	@Override
 	public List<LabTestSpecimen> getAllLabTestSpecimens(Boolean includeVoided)
 			throws APIException {
 		return labTestSpecimenDAO.getLabTestSpecimens("",includeVoided,null,null);
 	}
 
 	@Override
 	public List<LabTestSpecimen> getAllLabTestSpecimens() throws APIException {
 		return labTestSpecimenDAO.getLabTestSpecimens("",false,null,null);
 	}
 
 	@Override
 	public List<LabTestSpecimen> getLabTestSpecimens(String nameFragment,
 			Boolean includeVoided, Integer start, Integer length) {
 		return labTestSpecimenDAO.getLabTestSpecimens(nameFragment,
 				includeVoided, start, length);
 	}
 
 	@Override
 	public Integer getCountOfLabTestSpecimens(Boolean includeRetired)
 			throws APIException {
 		return labTestSpecimenDAO.getLabTestSpecimens("", includeRetired, null, null).size();
 	}
 	
 	//--------------------------------------------------------
 	
 	@Override
 	public LabReport getLabReport(Integer labReportId) {
 		return labReportDAO.getLabReport(labReportId);
 	}
 
 	@Override
 	public LabReport getLabReportByUuid(String uuid) {
 		return labReportDAO.getLabReportByUuid(uuid);
 	}
 
 	@Override
 	public LabReport saveLabReport(LabReport labReport)
 			throws APIException {
 		return labReportDAO.saveLabReport(labReport);
 	}
 
 	@Override
 	public void purgeLabReport(LabReport labReport)
 			throws APIException {
 		labReportDAO.deleteLabReport(labReport);
 		
 	}
 
 	@Override
 	public LabReport retireLabReport(
 			LabReport labReport, String retireReason)
 			throws APIException {
 		labReport.setRetired(true);
 		labReport.setDateRetired(new Date());
 		labReport.setRetireReason(retireReason);
 		return labReportDAO.saveLabReport(labReport);
 	}
 
 	@Override
	public List<LabReport> getAllLabReports(Boolean includeVoided)
 			throws APIException {
		return labReportDAO.getLabReports("",includeVoided,null,null);
 	}
 
 	@Override
 	public List<LabReport> getAllLabReports() throws APIException {
 		return labReportDAO.getLabReports("",false,null,null);
 	}
 
 	@Override
 	public List<LabReport> getLabReports(String nameFragment,
 			Boolean includeVoided, Integer start, Integer length) {
 		return labReportDAO.getLabReports(nameFragment,
 				includeVoided, start, length);
 	}
 
 	@Override
 	public Integer getCountOfLabReports(Boolean includeRetired)
 			throws APIException {
 		return labReportDAO.getLabReports("", includeRetired, null, null).size();
 	}
 	//------------------------------------------------------------	
 	
 		public LabTestResult getLabTestResult(Integer labTestResult) {
 			//
 			return labTestResultDAO.getLabTestResult(labTestResult);
 		}
 
 		public LabTestResult getLabTestResultByUuid(String uuid) {
 			//
 			return labTestResultDAO.getLabTestResultByUuid(uuid);
 		}
 
 		public LabTestResult saveLabTestResult(LabTestResult labTestResult)
 				throws APIException {
 			//
 			return labTestResultDAO.saveLabTestResult(labTestResult);
 		}
 
 		public LabTestResult voidLabTestResult(LabTestResult labTestResult,
 				String deleteReason) throws APIException {
 			labTestResult.setVoided(true);
 			labTestResult.setDateVoided(new Date());
 			labTestResult.setVoidReason(deleteReason);
 			return labTestResultDAO.saveLabTestResult(labTestResult);
 		}
 
 		public void purgeLabTestResult(LabTestResult labTestResult)
 				throws APIException {
 			//
 			labTestResultDAO.deleteLabTestResult(labTestResult);
 		}
 
 		public List<LabTestResult> getAllLabTestResults(Boolean includeVoided) {
 			return labTestResultDAO.getLabTestResults("", includeVoided, 0, 0);
 		}
 			
 		public List<LabTestResult> getLabTestResults(String displayFragment,
 				Boolean ifVoided, Integer index, Integer length) {
 			return labTestResultDAO.getLabTestResults(displayFragment,
 					ifVoided, index, length);
 		}
 
 		public Integer getCountOfLabTestResult(String search, Boolean ifVoided)
 				throws APIException {
 			return labTestResultDAO.getCountOfLabTestResults(search, ifVoided);
 		}
 
 
 }
