 package org.huamuzhen.oa.biz;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.Resource;
 
 import org.huamuzhen.oa.domain.dao.FeedbackDAO;
 import org.huamuzhen.oa.domain.dao.ReportFormDAO;
 import org.huamuzhen.oa.domain.entity.Feedback;
 import org.huamuzhen.oa.domain.entity.OrgUnit;
 import org.huamuzhen.oa.domain.entity.ReportForm;
 import org.huamuzhen.oa.domain.entity.User;
 import org.huamuzhen.oa.domain.enumeration.Privilege;
 import org.huamuzhen.oa.domain.enumeration.ReportFormStatus;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 public class FeedbackManager extends BaseManager<Feedback, String> {
 	
 	@Resource
 	private ReportFormDAO reportFormDAO;
 	
 	@Resource
 	private FeedbackDAO feedbackDAO;
 	
 	@Resource
 	public void setDao(FeedbackDAO dao) {
 		super.setDao(dao);
 	}
 
 	@Transactional
 	public Feedback add(String reportFormId, String content, String signature,
 			String orgUnitId, String owner, User currentUser, boolean agree, String currentReceiverId, String leader2Id) {
 		Feedback feedback = new Feedback();
 		feedback.setContent(content);
 		feedback.setSignature(signature);
 		feedback.setFeedBackTime(new Timestamp(System.currentTimeMillis()));
 		feedback.setReportFormId(reportFormId);
 		feedback.setAgree(agree);
 		ReportForm reportForm = reportFormDAO.findOne(reportFormId);
 		
 		if(currentUser.getPrivilege() == Privilege.DEPARTMENT && null != orgUnitId){
 			feedback.setOwner(currentUser.getOrgUnit().getName());
 			feedback.setResponseOrgUnitId(currentUser.getOrgUnit().getId());
 			Feedback savedFeedback= this.saveAndFlush(feedback);;
 			if(!agree){
 				reportForm.setStatus(ReportFormStatus.DENIED);
 			}else{
 				if(checkIfAllRequiredOrgUnitsSendPositiveFeedback(reportForm)){
 					reportForm.setStatus(ReportFormStatus.GOT_REPLY_FROM_UNITS);
 				}
 			}
 			reportFormDAO.save(reportForm);
 			return savedFeedback;
 			
 		}else if(currentUser.getPrivilege() == Privilege.LEADER1 && null != currentReceiverId && null != leader2Id){
 			feedback.setOwner(Privilege.LEADER1.toString());
 			Feedback savedFeedback = this.save(feedback);
 			if(!agree){
 				reportForm.setStatus(ReportFormStatus.DENIED);
 			}else{
 				reportForm.setCurrentReceiverId(leader2Id);
 				reportForm.setStatus(ReportFormStatus.SENT_TO_LEADER2);
 			}
 			reportFormDAO.save(reportForm);
 			return savedFeedback;
 			
 		}else if(currentUser.getPrivilege() == Privilege.LEADER2 && null != currentReceiverId){
 			feedback.setOwner(Privilege.LEADER2.toString());
 			Feedback savedFeedback = this.save(feedback);
 			if(!agree){
 				reportForm.setStatus(ReportFormStatus.DENIED);
 			}else{
 				reportForm.setStatus(ReportFormStatus.SENT_TO_OFFICE);
 			}
 					
 			reportFormDAO.save(reportForm);
 			return savedFeedback;
 		}else if(currentUser.getPrivilege() == Privilege.OFFICE){
 			feedback.setOwner(Privilege.OFFICE.toString());
			Feedback savedFeedback = this.save(feedback);
			if(!agree){
				reportForm.setStatus(ReportFormStatus.DENIED);
			}else{
				reportForm.setStatus(ReportFormStatus.PASSED);
			}
 			
 			reportFormDAO.save(reportForm);
 			return savedFeedback;
 		}
 		return null;
 	}
 
 	@Transactional
 	private boolean checkIfAllRequiredOrgUnitsSendPositiveFeedback(ReportForm reportForm) {
 		Set<OrgUnit> requiredOrgUnits = reportForm.getReportFormType().getRequiredOrgUnits();
 		List<Feedback> feedbackList = feedbackDAO.findFeedbackByReportFormId(reportForm.getId());
 		List<String> requiredOrgUnitIdList = new ArrayList<String>();
 		for(OrgUnit requiredOrgUnit : requiredOrgUnits){
 			requiredOrgUnitIdList.add(requiredOrgUnit.getId());
 		}
 		
 		for(OrgUnit requiredOrgUnit : requiredOrgUnits){
 			for(Feedback feedback : feedbackList){
 				if(!feedback.isAgree()){
 					return false;
 				}
 				if(feedback.getResponseOrgUnitId().equals(requiredOrgUnit.getId())){
 					requiredOrgUnitIdList.remove(feedback.getResponseOrgUnitId());
 				}
 			}
 		}
 		if(requiredOrgUnitIdList.size() == 0){
 			return true;
 		}
 		return false;
 	}
 	
 	@Transactional
 	public boolean checkIfOrgUnitFeedbackIsAlreadyExists(OrgUnit orgUnit){
 		if(feedbackDAO.findFeedbackByResponseOrgUnitId(orgUnit.getId()).size() > 0){
 			return true;
 		}
 		return false;
 	}
 
 }
