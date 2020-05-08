 package pt.ist.expenditureTrackingSystem.presentationTier.actions.acquisitions;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import pt.ist.expenditureTrackingSystem.applicationTier.Authenticate.User;
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.Financer;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.refund.RefundItem;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess;
 import pt.ist.expenditureTrackingSystem.domain.dto.DomainObjectBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.FundAllocationBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.UnitItemBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.presentationTier.actions.ProcessAction;
 import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
 import pt.ist.fenixWebFramework.security.UserView;
 
 public abstract class PaymentProcessAction extends ProcessAction {
 
     public ActionForward executeGenericAddPayingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final PaymentProcess process = getProcess(request);
 	request.setAttribute("process", process);
 	request.setAttribute("domainObjectBean", new DomainObjectBean<Unit>());
 	return mapping.findForward("select.unit.to.add");
     }
 
     public ActionForward addPayingUnit(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	DomainObjectBean<Unit> bean = getRenderedObject("unitToAdd");
 	List<Unit> units = new ArrayList<Unit>();
 	units.add(bean.getDomainObject());
 	genericActivityExecution(request, "GenericAddPayingUnit", units);
 	return viewProcess(mapping, form, request, response);
     }
 
     public ActionForward executeGenericRemovePayingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final PaymentProcess process = getProcess(request);
 	request.setAttribute("process", process);
 	request.setAttribute("payingUnits", process.getPayingUnits());
 	return mapping.findForward("remove.paying.units");
     }
 
     public ActionForward removePayingUnit(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	final Unit payingUnit = getDomainObject(request, "unitOID");
 	List<Unit> units = new ArrayList<Unit>();
 	units.add(payingUnit);
 	try {
 	    genericActivityExecution(request, "GenericRemovePayingUnit", units);
 	} catch (DomainException e) {
 	    addErrorMessage(e.getMessage(), getBundle());
 	}
 	return executeGenericRemovePayingUnit(mapping, form, request, response);
     }
 
     public ActionForward executeGenericAssignPayingUnitToItem(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final RequestItem item = getRequestItem(request);
 	final PaymentProcess process = getProcess(request);
 	List<UnitItemBean> beans = new ArrayList<UnitItemBean>();
 	for (Unit unit : process.getPayingUnits()) {
 	    beans.add(new UnitItemBean(unit, item));
 	}
 	request.setAttribute("item", item);
 	request.setAttribute("process", process);
 	request.setAttribute("unitItemBeans", beans);
 
 	return mapping.findForward("assign.unit.item");
     }
 
     public ActionForward executeAssignPayingUnitToItemCreation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final PaymentProcess process = getProcess(request);
 	final RequestItem item = getRequestItem(request);
 
 	List<UnitItemBean> beans = getRenderedObject("unitItemBeans");
 	try {
 	    genericActivityExecution(process, "GenericAssignPayingUnitToItem", item, beans);
 	} catch (DomainException e) {
 	    addErrorMessage(e.getMessage(), getBundle());
 	    return executeGenericAssignPayingUnitToItem(mapping, form, request, response);
 	}
 
 	return viewProcess(mapping, form, request, response);
     }
 
     public ActionForward calculateShareValuePostBack(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	return genericCalculateShareValuePostBack(mapping, form, request, response, "assign.unit.item", false);
     }
 
     public ActionForward calculateRealShareValuePostBack(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
	final RequestItem item = getRequestItem(request);
 	request.setAttribute("item", item);
 	return genericCalculateShareValuePostBack(mapping, form, request, response, "edit.real.shares.values", true);
     }
 
     private ActionForward genericCalculateShareValuePostBack(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response, String forward, boolean realValue) {
 
 	final PaymentProcess process = getProcess(request);
	final RequestItem item = getRequestItem(request);
 
 	List<UnitItemBean> beans = getRenderedObject("unitItemBeans");
 	int assigned = 0;
 	for (UnitItemBean bean : beans) {
 	    if (bean.getAssigned()) {
 		assigned++;
 	    }
 	}
 	if (assigned != 0) {
 	    Money[] shareValues;
 	    Money totalItemValue = realValue ? item.getRealValue() : item.getValue();
 	    shareValues = totalItemValue.allocate(assigned);
 
 	    int i = 0;
 	    for (UnitItemBean bean : beans) {
 		if (bean.getAssigned()) {
 		    if (realValue) {
 			bean.setRealShareValue(shareValues[i++]);
 		    } else {
 			bean.setShareValue(shareValues[i++]);
 		    }
 		} else {
 		    if (realValue) {
 			bean.setRealShareValue(null);
 		    } else {
 			bean.setShareValue(null);
 		    }
 		}
 	    }
 	}
 	request.setAttribute("item", item);
 	request.setAttribute("process", process);
 	request.setAttribute("unitItemBeans", beans);
 
 	RenderUtils.invalidateViewState();
 	return mapping.findForward(forward);
     }
 
     public ActionForward executeProjectFundAllocation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final PaymentProcess process = getProcess(request);
 	final User user = UserView.getUser();
 	if (process.getCurrentOwner() == null || (user != null && process.getCurrentOwner() == user.getPerson())) {
 	    if (process.getCurrentOwner() == null) {
 		process.takeProcess();
 	    }
 	    request.setAttribute("process", process);
 	    List<FundAllocationBean> fundAllocationBeans = new ArrayList<FundAllocationBean>();
 	    for (Financer financer : process.getProjectFinancersWithFundsAllocated(user.getPerson())) {
 		fundAllocationBeans.add(new FundAllocationBean(financer));
 	    }
 	    request.setAttribute("fundAllocationBeans", fundAllocationBeans);
 	    return mapping.findForward("allocate.project.funds");
 	} else {
 	    return viewProcess(mapping, form, request, response);
 	}
     }
 
     public ActionForward allocateProjectFunds(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final PaymentProcess process = getProcess(request);
 	final List<FundAllocationBean> fundAllocationBeans = getRenderedObject();
 	genericActivityExecution(process, "ProjectFundAllocation", fundAllocationBeans);
 	return viewProcess(mapping, form, request, response);
     }
 
     public ActionForward executeFundAllocation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final PaymentProcess process = getProcess(request);
 	final User user = UserView.getUser();
 	if (process.getCurrentOwner() == null
 		|| (user != null && process.getCurrentOwner() == user.getPerson())) {
 	    if (process.getCurrentOwner() == null) {
 		process.takeProcess();
 	    }
 	    request.setAttribute("process", process);
 	    List<FundAllocationBean> fundAllocationBeans = new ArrayList<FundAllocationBean>();
 	    for (Financer financer : process.getFinancersWithFundsAllocated(user.getPerson())) {
 		fundAllocationBeans.add(new FundAllocationBean(financer));
 	    }
 	    request.setAttribute("fundAllocationBeans", fundAllocationBeans);
 	    return mapping.findForward("allocate.funds");
 	} else {
 	    return viewProcess(mapping, form, request, response);
 	}
     }
 
     public ActionForward allocateFunds(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final PaymentProcess process = getProcess(request);
 	final List<FundAllocationBean> fundAllocationBeans = getRenderedObject();
 	genericActivityExecution(process, "FundAllocation", fundAllocationBeans);
 	return viewProcess(mapping, form, request, response);
     }
 
     @Override
     protected String getBundle() {
 	return "ACQUISITION_RESOURCES";
     }
 
     protected <T extends RequestItem> T getRequestItem(HttpServletRequest request) {
 	return (T) getDomainObject(request, "itemOid");
     }
 }
