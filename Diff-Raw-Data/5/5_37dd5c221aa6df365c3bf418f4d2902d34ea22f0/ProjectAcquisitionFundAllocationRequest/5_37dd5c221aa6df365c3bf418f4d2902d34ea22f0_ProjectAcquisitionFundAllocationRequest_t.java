 package pt.ist.expenditureTrackingSystem.domain.acquisitions;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import module.finance.domain.Supplier;
 import module.workflow.activities.ActivityInformation;
 import module.workflow.activities.WorkflowActivity;
 import module.workflow.domain.WorkflowProcess;
 import myorg.applicationTier.Authenticate;
 import myorg.domain.User;
 import myorg.domain.VirtualHost;
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.AbstractFundAllocationActivityInformation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.AllocateProjectFundsPermanently;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons.ProjectFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.dto.FundAllocationBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Project;
 import pt.ist.expenditureTrackingSystem.domain.organization.SubProject;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.fenixWebFramework.security.UserView;
 
 public class ProjectAcquisitionFundAllocationRequest extends ProjectAcquisitionFundAllocationRequest_Base {
 
     public ProjectAcquisitionFundAllocationRequest(final UnitItem unitItem, final String processId, final String processUrl,
 	    final Integer payingUnitNumber, final String payingAccountingUnit, final Money totalValue,
 	    final Boolean finalFundAllocation) {
 	super();
 	setUnitItem(unitItem);
 	setProcessId(processId);
 	setProcessUrl(processUrl);
 	setPayingUnitNumber(payingUnitNumber);
 	setPayingAccountingUnit(payingAccountingUnit);
 	setTotalValue(totalValue);
 	setFinalFundAllocation(finalFundAllocation);
     }
 
     @Override
     public void registerFundAllocation(final String fundAllocationNumber, final String operatorUsername) {
 	if (hasCancelFundAllocationRequest()) {
 	    throw new DomainException("error.cannot.allocate.funds.because.request.has.been.canceled");
 	}
 
 	super.registerFundAllocation(fundAllocationNumber, operatorUsername);
 
 	final UnitItem unitItem = getUnitItem();
 	final ProjectFinancer financer = (ProjectFinancer) unitItem.getFinancer();
 
 	final PaymentProcess process = financer.getProcess();
 
 	final WorkflowActivity activity;
 	final ActivityInformation information;
 
 	final List<FundAllocationBean> args;
 	final FundAllocationBean fundAllocationBean = new FundAllocationBean(financer);
 	if (isFinalFundAllocation()) {
 	    activity = getActivity(process, AllocateProjectFundsPermanently.class.getSimpleName());
 	    information = ((AllocateProjectFundsPermanently) activity).getActivityInformation(process, false);
 	    fundAllocationBean.setEffectiveFundAllocationId(fundAllocationNumber);
 	} else {
 	    activity = getActivity(process, ProjectFundAllocation.class.getSimpleName());
 	    information = ((ProjectFundAllocation) activity).getActivityInformation(process, false);
 	    fundAllocationBean.setFundAllocationId(fundAllocationNumber);
 	}
 
 	args = Collections.singletonList(fundAllocationBean);
 
 	((AbstractFundAllocationActivityInformation) information).setBeans(args);
 
 	final myorg.applicationTier.Authenticate.UserView currentUserView = UserView.getUser();
 	final User user = User.findByUsername(operatorUsername);
 	if (user == null) {
 	    throw new NullPointerException("No user found for: " + operatorUsername);
 	}
 	final myorg.applicationTier.Authenticate.UserView userView = Authenticate.authenticate(user);
 	try {
 	    UserView.setUser(userView);
 	    activity.execute(information);
 	} finally {
 	    UserView.setUser(currentUserView);
 	}
     }
 
     private <T extends WorkflowProcess> WorkflowActivity<T, ActivityInformation<T>> getActivity(final WorkflowProcess process,
 	    final String activityName) {
 	return process.getActivity(activityName);
     }
 
     @Override
     public String getQueryString() {
 	if (getExternalRegistrationDate() == null) {
 	    final UnitItem unitItem = getUnitItem();
 	    final Unit unit = unitItem.getUnit();
 	    final AccountingUnit accountingUnit = unitItem.getAccountingUnit();
 	    final RequestItem item = unitItem.getItem();
 	    final CPVReference cpvReference = item.getCPVReference();
 	    final RequestWithPayment request = item.getRequest();
 	    final Supplier supplier = getSupplier(request);
 
 	    final Money shareValue = unitItem.getShareValue();
 	    final Money shareValueWithVat = unitItem.getShareValueWithVat();
 	    final Money shareVat = shareValueWithVat.subtract(shareValue);
 
 	    Object[] insertArgs = new Object[] {
 		    "INTERACT_ID", Long.valueOf(getInteractionId()),
 		    "PROCESS_ID", getProcessId(),
 		    "ITEM_ID", unitItem.getExternalId(),
 		    "PROJ_ID", getProjectId(unit),
 		    "PROJ_MEMBER", getSubProjectId(unit),
 //		    "!!!FALTA A UNIDADE DE EXPLORAÇÃO", accountingUnit.getName().substring(0, 2),
 		    "SUPPLIER_ID", supplier == null ? null : supplier.getGiafKey(),
 		    "SUPPLIER_DOC_TYPE", supplier == null ? null : "Proposta",
 		    "SUPPLIER_DOC_ID", supplier == null ? null : getProposalNumber(request),
 		    "CPV_ID", cpvReference.getCode(),
 		    "CPV_DESCRIPTION", cpvReference.getDescription(),
 		    "MOV_DESCRIPTION", limitStringSize(Integer.toString(item.getUnitItemsCount()) + " - " + item.getDescription(), 4000),
 		    "MOV_PCT_IVA", item.getVatValue(),
 		    "MOV_VALUE", shareValue,
 		    "MOV_VALUE_IVA", shareVat,
 //,
 //		    "CALLBACK_URL", getCallbackUrl()
 	    };
 	    if (isFinalFundAllocation()) {
 		final int l = insertArgs.length;
 		insertArgs = Arrays.copyOf(insertArgs, l + 2);
 		insertArgs[l] = "NTERACT_PARENT_ID";
 		insertArgs[l + 1] = getInitialInteractionId(unitItem);
 	    }
 
 	    final String q = insertQuery(insertArgs);
 	    System.out.println(q);
 	    return q;
 	}
 	final String q = isFinalFundAllocation() ?
 		selectQuery("INTERACT_ID", Long.valueOf(getInteractionId()),
			"MGP_DIST_ID", "MGP_DIST_TYPE", "MPG_DIST_DATE", "MGP_DESP_OPERATOR") :
 		selectQuery("INTERACT_ID", Long.valueOf(getInteractionId()),
			"MGP_DESP_ID", "MGP_DESP_TYPE", "MPG_DESP_DATE", "MGP_DESP_OPERATOR");
 	System.out.println(q);
 	return q;
     }
 
     private Object getInitialInteractionId(final UnitItem unitItem) {
 	for (final ProjectAcquisitionFundAllocationRequest request : unitItem.getProjectAcquisitionFundAllocationRequestSet()) {
 	    if (!request.isCanceled() && !request.isFinalFundAllocation()) {
 		return request.getInteractionId();
 	    }
 	}
 	return null;
     }
 
     @Override
     public void processResultSet(final ResultSet resultSet) throws SQLException {
 	if (getExternalRegistrationDate() == null) {
 	    registerOnExternalSystem();
 	} else {
 	    if (resultSet.next()) {
 		final String fundAllocationNumber = resultSet.getString(1);
 		final String operatorUsername = resultSet.getString(4);
 		if (fundAllocationNumber != null && operatorUsername != null) {
 		    System.out.println("fundAllocationNumber: " + fundAllocationNumber);
 		    System.out.println("operatorUsername: " + operatorUsername);
 		    registerFundAllocation(fundAllocationNumber, operatorUsername);
 		}
 	    }
 	}
     }
 
     public String getProjectId(final Unit unit) {
 	if (unit instanceof Project) {
 	    final Project project = (Project) unit;
 	    return project.getProjectCode();
 	} else if (unit instanceof SubProject) {
 	    final SubProject subProject = (SubProject) unit;
 	    final Project project = (Project) subProject.getParentUnit();
 	    return project.getProjectCode();
 	}
 	return null;
     }
 
     public String getSubProjectId(final Unit unit) {
 	if (unit instanceof SubProject) {
 	    final SubProject subProject = (SubProject) unit;
 	    final Project project = (Project) subProject.getParentUnit();
 	    final String projectName = project.getName();
 	    final String description = subProject.getName().substring(projectName.length() + 3);
 	    final int i = description.indexOf(" - ");
 	    return description.substring(0, i);
 	}
 	return null;
     }
 
     private Supplier getSupplier(final RequestWithPayment request) {
 	if (request instanceof AcquisitionRequest) {
 	    final AcquisitionRequest acquisitionRequest = (AcquisitionRequest) request;
 	    return acquisitionRequest.getSupplier();
 	}
 	return null;
     }
 
     private String getProposalNumber(final RequestWithPayment request) {
 	if (request instanceof AcquisitionRequest) {
 	    final AcquisitionRequest acquisitionRequest = (AcquisitionRequest) request;
 	    final AcquisitionProcess process = acquisitionRequest.getProcess();
 	    final AcquisitionProposalDocument acquisitionProposalDocument = process.getAcquisitionProposalDocument();
 	    return acquisitionProposalDocument.getProposalId();
 	}
 	return null;
     }
 
     @Override
     protected String getTableName() {
 	return isFinalFundAllocation() ? "JUSTIFICACOES" : "CABIMENTOS";
     }
 
     private String getCallbackUrl() {
 	final StringBuilder result = new StringBuilder();
 	result.append("http://");
 	result.append(VirtualHost.getVirtualHostForThread().getHostname());
 	result.append("/webservice/fundAllocationResultService/registerResult/");
 	result.append(getInteractionId());
 	result.append("/");
 	return result.toString();
     }
 
 }
