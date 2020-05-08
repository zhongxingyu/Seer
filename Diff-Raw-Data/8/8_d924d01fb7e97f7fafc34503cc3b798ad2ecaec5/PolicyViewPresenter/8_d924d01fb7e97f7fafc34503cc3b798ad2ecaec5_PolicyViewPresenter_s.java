 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.monitoring.client.presenter.policy;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.ebayopensource.turmeric.monitoring.client.ConsoleUtil;
 import org.ebayopensource.turmeric.monitoring.client.Display;
 import org.ebayopensource.turmeric.monitoring.client.SupportedService;
 import org.ebayopensource.turmeric.monitoring.client.model.ConsoleService;
 import org.ebayopensource.turmeric.monitoring.client.model.HistoryToken;
 import org.ebayopensource.turmeric.monitoring.client.model.UserAction;
 import org.ebayopensource.turmeric.monitoring.client.model.policy.Condition;
 import org.ebayopensource.turmeric.monitoring.client.model.policy.Expression;
 import org.ebayopensource.turmeric.monitoring.client.model.policy.ExtraField;
 import org.ebayopensource.turmeric.monitoring.client.model.policy.PolicyKey;
 import org.ebayopensource.turmeric.monitoring.client.model.policy.PolicyQueryService;
 import org.ebayopensource.turmeric.monitoring.client.model.policy.PolicyQueryService.GetPoliciesResponse;
 import org.ebayopensource.turmeric.monitoring.client.model.policy.GenericPolicy;
 import org.ebayopensource.turmeric.monitoring.client.model.policy.PolicySubjectAssignment;
 import org.ebayopensource.turmeric.monitoring.client.model.policy.QueryCondition;
 import org.ebayopensource.turmeric.monitoring.client.model.policy.Resource;
 import org.ebayopensource.turmeric.monitoring.client.model.policy.ResourceImpl;
 import org.ebayopensource.turmeric.monitoring.client.model.policy.Rule;
 import org.ebayopensource.turmeric.monitoring.client.model.policy.RuleAttribute;
 import org.ebayopensource.turmeric.monitoring.client.model.policy.Subject;
 import org.ebayopensource.turmeric.monitoring.client.model.policy.SubjectGroup;
 import org.ebayopensource.turmeric.monitoring.client.presenter.AbstractGenericPresenter;
 import org.ebayopensource.turmeric.monitoring.client.view.common.PolicyTemplateDisplay.PolicyPageTemplateDisplay;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.HasWidgets;
 
 public class PolicyViewPresenter extends AbstractGenericPresenter {
 
 	public final static String PRESENTER_ID = "PolicyView";
 
 	
 	public final String getId() {
 		return PRESENTER_ID;
 	}
 
 	protected HandlerManager eventBus;
 	protected PolicyViewDisplay view;
 	protected Map<SupportedService, ConsoleService> serviceMap;
 	protected List<PolicySubjectAssignment> subjectAssignments;
 	protected List<Resource> resourceAssignments;
 
 	protected ResourceImpl editResourceAssignment;
 
 	protected PolicySubjectAssignment editSubjectAssignment;
 	protected PolicyQueryService service;
 
 	public PolicyViewPresenter(HandlerManager eventBus, PolicyViewDisplay view,
 			Map<SupportedService, ConsoleService> serviceMap) {
 		this.eventBus = eventBus;
 		this.view = view;
 		this.view.setAssociatedId(getId());
 		this.serviceMap = serviceMap;
 		bind();
 	}
 
 	/*
 	 * Interface definitions
 	 */
 	public interface PolicyViewDisplay extends PolicyPageTemplateDisplay {
 		Button getCancelButton();
 
 		ResourcesContentDisplay getResourceContentView();
 
 		SubjectContentDisplay getSubjectContentView();
 
 		void setPolicyDesc(String policyDesc);
 
 		void setPolicyName(String policyName);
 
 		void setPolicyType(String policyType);
 		
 		void setPolicyStatus(boolean enabled);
 
 		void clear();
 
 		void error(String msg);
 
 		UserAction getSelectedAction();
 
 		void setExtraFieldList(List<ExtraField> extraFieldList);
 
 		void setExtraFieldAvailable(boolean available);
 
 	}
 
 	public interface ResourcesContentDisplay extends Display {
 		void setAssignments(List<Resource> assignments);
 
 		void error(String msg);
 
 	}
 
 	public interface SubjectContentDisplay extends Display {
 		void setAssignments(List<PolicySubjectAssignment> assignments);
 	}
 
 	@Override
 	protected PolicyViewDisplay getView() {
 		return view;
 	}
 
 	public void bind() {
 		// fired on non saved policy
 		this.view.getCancelButton().addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				view.clear();
 
 				HistoryToken token = makeToken(PolicyController.PRESENTER_ID,
 						PolicySummaryPresenter.PRESENTER_ID, null);
 				History.newItem(token.toString(), true);
 			}
 		});
 
 	}
 
 	@Override
 	public void go(HasWidgets container, final HistoryToken token) {
 		container.clear();
 		this.view.clear();
 		this.view.activate();
 		container.add(this.view.asWidget());
 
 		service = (PolicyQueryService) serviceMap
 				.get(SupportedService.POLICY_QUERY_SERVICE);
 
 		loadPolicy(token);
 	}
 
 	private void loadPolicy(final HistoryToken token) {
 		String policyId = token.getValue(HistoryToken.SELECTED_POLICY_TOKEN_ID);
 		String policyType = token
 				.getValue(HistoryToken.SELECTED_POLICY_TOKEN_TYPE);
 		PolicyKey pKey = new PolicyKey();
 		pKey.setId(Long.valueOf(policyId));
 		pKey.setType(policyType);
 		ArrayList<PolicyKey> poKeys = new ArrayList<PolicyKey>();
 		poKeys.add(pKey);
 		QueryCondition condition = new QueryCondition();
 		condition.addQuery(new QueryCondition.Query(
 				QueryCondition.ActivePoliciesOnlyValue.FALSE));
 
 		service.findPolicies(null, poKeys, null, null, null, null, null,
 				condition,
 				new AsyncCallback<PolicyQueryService.GetPoliciesResponse>() {
 
 					@Override
 					public void onFailure(Throwable caught) {
 						PolicyViewPresenter.this.view
 								.error(ConsoleUtil.messages.serverError(caught
 										.getLocalizedMessage()));
 						GWT.log("findPolicies:Fail");
 					}
 
 					@Override
 					public void onSuccess(GetPoliciesResponse result) {
 						GWT.log("findPolicies:Success");
 
 						Collection<GenericPolicy> policies = result
 								.getPolicies();
 
 						for (GenericPolicy policy : policies) {
 							view.getResourceContentView().setAssignments(
 									policy.getResources());
 
 							view.getSubjectContentView().setAssignments(
 									fetchSubjectAndSGAssignment(policy));
 							view.setPolicyName(policy.getName());
 							view.setPolicyDesc(policy.getDescription());
 							view.setPolicyType(policy.getType());
 							view.setPolicyStatus(policy.getEnabled());
 							if ("RL".equals(policy.getType())) {
 								view.setExtraFieldAvailable(true);
 								view.setExtraFieldList(getExtraFieldView(result
 										.getPolicies()));
 
 							} else {
 								view.setExtraFieldAvailable(false);
 								view.setExtraFieldList(new ArrayList<ExtraField>());
 							}
 							break;
 						}
 
 					}
 
 					// TODO make it abstract and move its content to an specific
 					// RL policy
 					// View Presenter extends from PolicyViewPresenter
 					protected List<ExtraField> getExtraFieldView(
 							Collection<GenericPolicy> policies) {
 						List<ExtraField> rlExtraFields = new ArrayList<ExtraField>();
 						// TODO JOSE load from xml file from an Util class
 
 						if (policies.size() > 0) {
 							ArrayList<GenericPolicy> policyList = new ArrayList<GenericPolicy>(
 									policies);
 							GenericPolicy policy = policyList.get(0);
 
 							if (policy.getRules() != null
 									&& policy.getRules().size() > 0) {
 								Rule rule = policy.getRules().get(0);
 
 								if(rule.getAttributeList()!= null && rule.getAttributeList().size()>0){
 
 									for (RuleAttribute attribute : rule.getAttributeList()) {
 									
 										if(RuleAttribute.NotifyKeys.NotifyEmails.name().equals(attribute.getKey())){
 											// Policy Based Email Address
 											ExtraField field_1 = new ExtraField();
 											field_1.setFieldType("Label");
 											// TODO JOSE I18N the extra fields
 											field_1.setLabelName("Policy Based Email Address:");
 											// TODO retrieve the email
 											field_1.setValue(attribute.getValue());
 											field_1.setOrder(1);
 											rlExtraFields.add(field_1);
 											
 										}
 										if(RuleAttribute.NotifyKeys.NotifyActive.name().equals(attribute.getKey())){
 											/// Subject Based Email Address:
 											ExtraField field_2 = new ExtraField();
 											field_2.setFieldType("CheckBox");
 											// TODO JOSE I18N the extra fields
 											field_2.setLabelName("Subject Based Email Address:");
 											// TODO retrieve the value of checkbox
 											field_2.setValue(String.valueOf(attribute.getValue()));
 
 											field_2.setOrder(2);
 											rlExtraFields.add(field_2);
 											
 										}
 									}
 								}
 						
 
 								// Effect Duration
 								ExtraField field_3 = new ExtraField();
 								field_3.setFieldType("Label");
 								// TODO JOSE I18N the extra fields
 								field_3.setValue(String.valueOf(rule
 										.getEffectDuration()));
 								field_3.setLabelName("Effect Duration: (secs)");
 								field_3.setOrder(3);
 								rlExtraFields.add(field_3);
 
 								// Rollover period
 								ExtraField field_4 = new ExtraField();
 								field_4.setFieldType("Label");
 								// TODO JOSE I18N the extra fields
 								field_4.setValue(String.valueOf(rule
 										.getRolloverPeriod()));
 								field_4.setLabelName("Rollover Period:");
 								field_4.setOrder(4);
 								rlExtraFields.add(field_4);
 
 								// priority
 								ExtraField field_5 = new ExtraField();
 								field_5.setFieldType("Label");
 								// TODO JOSE I18N the extra fields
 								field_5.setValue(String.valueOf(rule
 										.getPriority()));
 								field_5.setLabelName("Priority:");
 								field_5.setOrder(5);
 								rlExtraFields.add(field_5);
 
 								// Effect
 								ExtraField field_6 = new ExtraField();
 								field_6.setFieldType("Label");
 								// TODO JOSE I18N the extra fields
 								field_6.setValue(String.valueOf(rule
 										.getEffect()));
 								field_6.setLabelName("Effect:");
 								field_6.setOrder(6);
 								rlExtraFields.add(field_6);
 
 								// Condition
 								ExtraField field_7 = new ExtraField();
 								field_7.setFieldType("Label");
 								// TODO JOSE I18N the extra fields
 								StringBuilder conditions = new StringBuilder();
 
 								Condition condition = rule.getCondition();
 
 								if (condition != null) {
 
 									Expression expression = condition
 											.getExpression();
 									if (expression != null) {
 										conditions
 												.append(expression
 														.getPrimitiveValue()
 														.getValue());
 										conditions.append('\n');
 									}
 								}
 								field_7.setValue(String.valueOf(conditions
 										.toString()));
 								field_7.setLabelName("Condition:");
 								field_7.setOrder(7);
 								rlExtraFields.add(field_7);
 							}
 						}
 						return rlExtraFields;
 					}
 				});
 	}
 
 	private List<PolicySubjectAssignment> fetchSubjectAndSGAssignment(
 			GenericPolicy policy) {
 		HashMap<String, List<Subject>> sAssignMap = new HashMap<String, List<Subject>>();
 
 		for (Subject subject : policy.getSubjects()) {
 			String type = subject.getType();
 
 			if (!sAssignMap.containsKey(type)) {
 				List list = new ArrayList();
 				list.add(subject);
 				sAssignMap.put(type, list);
 			} else {
 				List list = (List) sAssignMap.get(type);
 				list.add(subject);
 			}
 		}
 
 		HashMap<String, List<SubjectGroup>> sgAssignMap = new HashMap<String, List<SubjectGroup>>();
 
 		for (SubjectGroup subjectGroup : policy.getSubjectGroups()) {
 			String type = subjectGroup.getType();
 
 			if (!sgAssignMap.containsKey(type)) {
 				List list = new ArrayList();
 				list.add(subjectGroup);
 				sgAssignMap.put(type, list);
 			} else {
 				List list = (List) sgAssignMap.get(type);
 				list.add(subjectGroup);
 			}
 		}
 
 		// Generates the PolicySubjectAssignment objects
 		List<PolicySubjectAssignment> polSubAssignmentList = new ArrayList<PolicySubjectAssignment>();
 		PolicySubjectAssignment polSubAssignment = null;
 
		Iterator it = sAssignMap.keySet().iterator();
 		while (it.hasNext()) {
 			polSubAssignment = new PolicySubjectAssignment();
 
 			String subjectType = (String) it.next();
 			List<Subject> sList = (List<Subject>) sAssignMap.get(subjectType);
 
 			polSubAssignment.setSubjectType(subjectType);
 			polSubAssignment.setSubjects(sList);
 
 			List<SubjectGroup> sgList = (List<SubjectGroup>) sgAssignMap
 					.get(subjectType);
 
 			if (null != sgList) {
 				polSubAssignment.setSubjectGroups(sgList);
 				sgAssignMap.remove(subjectType);
 			}
 
 			polSubAssignmentList.add(polSubAssignment);
 		}
 
 		// load remaining SG
 
 		if (sgAssignMap.size() > 0) {
			Iterator itSg = sgAssignMap.keySet().iterator();
 			while (itSg.hasNext()) {
 				polSubAssignment = new PolicySubjectAssignment();
 
				String subjectGroupType = (String) it.next();
 				List<SubjectGroup> sgList = (List<SubjectGroup>) sgAssignMap
 						.get(subjectGroupType);
 
 				polSubAssignment.setSubjectType(subjectGroupType);
 				polSubAssignment.setSubjects(null);
 				polSubAssignment.setSubjectGroups(sgList);
 
 				polSubAssignmentList.add(polSubAssignment);
 
 			}
 		}
 
 		return polSubAssignmentList;
 
 	}
 
 }
