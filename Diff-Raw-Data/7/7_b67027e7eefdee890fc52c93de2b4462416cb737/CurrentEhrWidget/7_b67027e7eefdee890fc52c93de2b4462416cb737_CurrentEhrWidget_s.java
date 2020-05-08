 package org.eastway.echarts.client.ui;
 
 import java.util.Date;
 import java.util.List;
 
 import org.eastway.echarts.client.EchartsUser;
 import org.eastway.echarts.client.request.AssignmentProxy;
 import org.eastway.echarts.client.request.AssignmentRequest;
 import org.eastway.echarts.client.request.CodeProxy;
 import org.eastway.echarts.client.request.EHRProxy;
 import org.eastway.echarts.client.request.EchartsRequestFactory;
 import org.eastway.echarts.client.request.EhrRequest;
 import org.eastway.echarts.client.style.GlobalResources;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.SpanElement;
 import com.google.gwt.requestfactory.shared.Receiver;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class CurrentEhrWidget extends Composite {
 
 	class EHRFetcher {
 		EHRProxy fetchedEHR;
 		List<AssignmentProxy> fetchedAssignments;
 
 		void Run(final EhrRequest ehrRequest, final AssignmentRequest assignmentRequest, final String caseNumber, final Receiver<EHRFetcher> callback) {
 			ehrRequest.findEHRByCaseNumber(caseNumber)
 				.with("patient")
 				.with("patient.caseStatus")
 				.with("demographics")
 				.with("demographics.gender")
 				.fire(
 					new Receiver<EHRProxy>() {
 						@Override
 						public void onSuccess(EHRProxy response) {
 							if (response != null) {
 								fetchedEHR = response;
 								assignmentRequest.findAssignmentsByCaseNumber(caseNumber).fire(
 										new Receiver<List<AssignmentProxy>>() {
 											@Override
 											public void onSuccess(List<AssignmentProxy> response) {
 												fetchedAssignments = response;
 												if (fetchedAssignments != null)
 													callback.onSuccess(EHRFetcher.this);
 											}
 										});
 							}
 						}
 					});
 								
 		}
 	}
 
 	private static CurrentEhrWidgetUiBinder uiBinder = GWT
 			.create(CurrentEhrWidgetUiBinder.class);
 
 	interface CurrentEhrWidgetUiBinder extends
 			UiBinder<Widget, CurrentEhrWidget> {
 	}
 
 	@UiField SpanElement name;
 	@UiField SpanElement dob;
 	@UiField SpanElement age;
 	@UiField SpanElement ssn;
 	@UiField SpanElement provider;
 	@UiField SpanElement caseStatus;
 	@UiField HTMLPanel container;
 
 	private String caseNumber;
 
 	public CurrentEhrWidget() {
 		initWidget(uiBinder.createAndBindUi(this));
 		INSTANCE = this;
 	}
 
 	public void setEhr(EHRProxy ehr) {
 		if (ehr == null || ehr.getPatient() == null || ehr.getDemographics() == null) {
 			container.setVisible(false);
 			return;
 		} else {
 			caseNumber = ehr.getPatient().getCaseNumber();
 			name.setInnerText(ehr.getPatient().getName() == null ? "NO DATA" : ehr.getPatient().getName());
 			dob.setInnerText(ehr.getDemographics() == null ? "NO DATA" : GlobalResources.getDateFormat()
 					.format(ehr.getDemographics()
 							.getDob()));
 			age.setInnerText(getAge(ehr.getDemographics().getDob()));
 			ssn.setInnerText(ehr.getPatient().getSsn() == null ? "NO DATA" : ehr.getPatient().getSsn());
 			provider.setInnerText(getProvider(ehr.getAssignments()));
 			caseStatus.setInnerText(getCaseStatus(ehr.getPatient().getCaseStatus()));
 			container.setVisible(true);
 		}
 	}
 
 	private String getCaseStatus(CodeProxy caseStatus) {
 		if (caseStatus == null)
 			return "NO DATA";
 		else if (caseStatus.getCodeDescriptor() == null)
 			return "NO DATA";
 		return caseStatus.getCodeDescriptor();
 	}
 
 	public void setEhr(String caseNumber, final EchartsRequestFactory requestFactory) {
 		if (caseNumber == null) {
 			container.setVisible(false);
 			return;
 		} else {
 			if (this.caseNumber != null && this.caseNumber.equals(caseNumber))
 				return;
 			final EhrRequest ehrRequest = requestFactory.ehrRequest();
 			AssignmentRequest assignmentRequest = requestFactory.assignmentRequest();
 			new EHRFetcher().Run(ehrRequest, assignmentRequest, caseNumber, new Receiver<EHRFetcher>() {
 				@Override
 				public void onSuccess(EHRFetcher response) {
 					EHRProxy ehr = requestFactory.ehrRequest().edit(response.fetchedEHR);
 					ehr.setAssignments(response.fetchedAssignments);
 					CurrentEhrWidget.instance().setEhr(ehr);
 				}
 			});
 			container.setVisible(true);
 		}
 	}
 
 	private String getAge(Date dob) {
 		if (dob == null)
 			return "NO DATA";
 		return new Long((new Date().getTime() - dob.getTime()) / (3600*24*365) / 1000).toString();
 	}
 
 	private String getProvider(List<AssignmentProxy> assignments) {
 		if (assignments == null || assignments.isEmpty())
 			return "NO DATA";
 		for (AssignmentProxy a : assignments)
 			if (a.getStaff() != null && a.getStaff().equals(EchartsUser.staffId))
 				return a.getStaffName();
 		return assignments.get(0).getStaffName() != null ? assignments.get(0).getStaffName() : "NO DATA";
 	}
 
 	private static CurrentEhrWidget INSTANCE;
 
 	public static CurrentEhrWidget instance() {
 		if (INSTANCE == null)
 			return new CurrentEhrWidget();
 		return INSTANCE;
 	}
 }
