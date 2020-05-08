 package pt.ist.expenditureTrackingSystem.presentationTier.actions.organization;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import myorg.applicationTier.Authenticate.UserView;
 import myorg.domain.User;
 import myorg.domain.exceptions.DomainException;
 import myorg.domain.util.Money;
 import myorg.util.VariantBean;
 
 import org.apache.commons.beanutils.BeanComparator;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
 import pt.ist.expenditureTrackingSystem.domain.Role;
 import pt.ist.expenditureTrackingSystem.domain.RoleType;
 import pt.ist.expenditureTrackingSystem.domain.SyncProjectsAux.MgpProject;
 import pt.ist.expenditureTrackingSystem.domain.SyncProjectsAux.ProjectReader;
 import pt.ist.expenditureTrackingSystem.domain.SyncSuppliersAux.GiafSupplier;
 import pt.ist.expenditureTrackingSystem.domain.SyncSuppliersAux.SupplierMap;
 import pt.ist.expenditureTrackingSystem.domain.SyncSuppliersAux.SupplierReader;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionRequest;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.CPVReference;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcessYear;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.AcquisitionAfterTheFact;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.AuthorizationLog;
 import pt.ist.expenditureTrackingSystem.domain.authorizations.DelegatedAuthorization;
 import pt.ist.expenditureTrackingSystem.domain.dto.AccountingUnitBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.AuthorizationBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreatePersonBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateSupplierBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.CreateUnitBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.SupplierBean;
 import pt.ist.expenditureTrackingSystem.domain.dto.UnitBean;
 import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.domain.organization.Project;
 import pt.ist.expenditureTrackingSystem.domain.organization.SearchUsers;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
 import pt.ist.expenditureTrackingSystem.domain.organization.UserAcquisitionProcessStatistics;
 import pt.ist.expenditureTrackingSystem.presentationTier.actions.BaseAction;
 import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 import pt.utl.ist.fenix.tools.util.excel.Spreadsheet;
 import pt.utl.ist.fenix.tools.util.excel.Spreadsheet.Row;
 
 @Mapping(path = "/expenditureTrackingOrganization")
 public class OrganizationAction extends BaseAction {
 
     public final ActionForward viewOrganization(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	UnitBean unitBean = getRenderedObject("unitBean");
 	if (unitBean == null) {
 	    unitBean = new UnitBean();
 	}
 	final Unit unit = unitBean.getUnit() == null ? (Unit) getDomainObject(request, "unitOid") : unitBean.getUnit();
 	request.setAttribute("unitBean", unitBean);
 	return viewOrganization(mapping, request, unit);
     }
 
     public final ActionForward viewOrganization(final ActionMapping mapping, final HttpServletRequest request, final Unit unit) {
 	final Set<Unit> units;
 	if (unit == null) {
 	    final ExpenditureTrackingSystem expenditureTrackingSystem = ExpenditureTrackingSystem.getInstance();
 	    units = expenditureTrackingSystem.getTopLevelUnitsSet();
 	    request.setAttribute("accountingUnits", expenditureTrackingSystem.getAccountingUnitsSet());
 	} else {
 	    request.setAttribute("unit", unit);
 	    if (unit.getAccountingUnit() != null) {
 		request.setAttribute("accountingUnits", Collections.singleton(unit.getAccountingUnit()));
 	    }
 	    units = unit.getSubUnitsSet();
 	}
 	request.setAttribute("units", units);
 	return forward(request, "/expenditureTrackingOrganization/viewOrganization.jsp");
     }
 
     public final ActionForward prepareCreateUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Unit unit = getDomainObject(request, "unitOid");
 	request.setAttribute("bean", new CreateUnitBean(unit));
 	return forward(request, "/expenditureTrackingOrganization/createUnit.jsp");
     }
 
     public final ActionForward createNewUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	CreateUnitBean createUnitBean = getRenderedObject();
 	final Unit newUnit = Unit.createNewUnit(createUnitBean);
 	return viewOrganization(mapping, request, newUnit);
     }
 
     public final ActionForward prepareCreateAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	request.setAttribute("accountingUnitBean", new AccountingUnitBean());
 	return forward(request, "/expenditureTrackingOrganization/createAccountingUnit.jsp");
     }
 
     public final ActionForward createNewAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AccountingUnitBean accountingUnitBean = getRenderedObject();
 	AccountingUnit.createNewAccountingUnit(accountingUnitBean);
 	request.setAttribute("unitBean", new UnitBean());
 	return viewOrganization(mapping, request, null);
     }
 
     private ActionForward editUnit(ActionMapping mapping, HttpServletRequest request, Unit unit) {
 	request.setAttribute("unit", unit);
 	return forward(request, "/expenditureTrackingOrganization/editUnit.jsp");
     }
 
     public final ActionForward editUnit(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Unit unit = getDomainObject(request, "unitOid");
 	return editUnit(mapping, request, unit);
     }
 
     public final ActionForward deleteUnit(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Unit unit = getDomainObject(request, "unitOid");
 	final Unit parentUnit = unit.getParentUnit();
 	unit.delete();
 	return viewOrganization(mapping, request, parentUnit);
     }
 
     public final ActionForward searchUsers(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	ExpenditureTrackingSystem.getInstance();
 	SearchUsers searchUsers = getRenderedObject("searchUsers");
 	if (searchUsers == null) {
 	    searchUsers = new SearchUsers();
 	}
 	request.setAttribute("searchUsers", searchUsers);
 	return forward(request, "/expenditureTrackingOrganization/searchUsers.jsp");
     }
 
     public final ActionForward prepareCreatePerson(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final CreatePersonBean createPersonBean = new CreatePersonBean();
 	request.setAttribute("bean", createPersonBean);
 	return forward(request, "/expenditureTrackingOrganization/createPerson.jsp");
     }
 
     public final ActionForward createPerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final CreatePersonBean createPersonBean = getRenderedObject();
 	Person person = Person.createPerson(createPersonBean);
 	return viewPerson(mapping, request, person);
     }
 
     public final ActionForward editPerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	return editPerson(mapping, request, person);
     }
 
     public final ActionForward editPerson(final ActionMapping mapping, final HttpServletRequest request, final Person person) {
 	request.setAttribute("person", person);
 	return forward(request, "/expenditureTrackingOrganization/editPerson.jsp");
     }
 
     public final ActionForward deletePerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	person.delete();
 	return searchUsers(mapping, form, request, response);
     }
 
     public final ActionForward addRole(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	final Role role = getRole(request);
 	person.addRoles(role);
 	return viewPerson(mapping, request, person);
     }
 
     public final ActionForward removeRole(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	final Role role = getRole(request);
 	person.removeRoles(role);
 	return viewPerson(mapping, request, person);
     }
 
     private Role getRole(HttpServletRequest request) {
 	String role = request.getParameter("role");
 	return role == null ? null : Role.getRole(RoleType.valueOf(role));
     }
 
     public final ActionForward viewPerson(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	return viewPerson(mapping, request, person);
     }
 
     public final ActionForward viewPerson(final ActionMapping mapping, final HttpServletRequest request, final Person person) {
 	request.setAttribute("person", person);
 	request.setAttribute("availableRoles", RoleType.values());
 	return forward(request, "/expenditureTrackingOrganization/viewPerson.jsp");
     }
 
     public final ActionForward viewLoggedPerson(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final User user = UserView.getCurrentUser();
 	final Person person = user.getExpenditurePerson();
 	return viewPerson(mapping, request, person);
     }
 
     public final ActionForward editAuthorization(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Authorization authorization = getDomainObject(request, "authorizationOid");
 	request.setAttribute("authorization", authorization);
 	return forward(request, "/expenditureTrackingOrganization/editAuthorization.jsp");
     }
 
     public final ActionForward viewAuthorization(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Authorization authorization = getDomainObject(request, "authorizationOid");
 	request.setAttribute("authorization", authorization);
 	return forward(request, "/expenditureTrackingOrganization/viewAuthorization.jsp");
     }
 
     public final ActionForward attributeAuthorization(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	request.setAttribute("person", person);
 	RenderUtils.invalidateViewState();
 	final UnitBean unitBean = new UnitBean();
 	request.setAttribute("unitBean", unitBean);
 	return expandAuthorizationUnit(mapping, request, person, null);
     }
 
     public final ActionForward prepareAddResponsibleAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	request.setAttribute("person", person);
 	final Set<AccountingUnit> accountingUnits = ExpenditureTrackingSystem.getInstance().getAccountingUnitsSet();
 	request.setAttribute("accountingUnits", accountingUnits);
 	return forward(request, "/expenditureTrackingOrganization/selectResponsibleAccountingUnitToAddMember.jsp");
     }
 
     public final ActionForward addResponsibleAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	final AccountingUnit accountingUnit = getDomainObject(request, "accountingUnitOid");
 	accountingUnit.addResponsiblePeople(person);
 	return viewPerson(mapping, request, person);
     }
 
     public final ActionForward prepareAddToAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	request.setAttribute("person", person);
 	final Set<AccountingUnit> accountingUnits = ExpenditureTrackingSystem.getInstance().getAccountingUnitsSet();
 	request.setAttribute("accountingUnits", accountingUnits);
 	return forward(request, "/expenditureTrackingOrganization/selectAccountingUnitToAddMember.jsp");
     }
 
     public final ActionForward addToAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	final AccountingUnit accountingUnit = getDomainObject(request, "accountingUnitOid");
 	accountingUnit.addPeople(person);
 	return viewPerson(mapping, request, person);
     }
 
     public final ActionForward prepareAddResponsibleProjectAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	request.setAttribute("person", person);
 	final Set<AccountingUnit> accountingUnits = ExpenditureTrackingSystem.getInstance().getAccountingUnitsSet();
 	request.setAttribute("accountingUnits", accountingUnits);
 	return forward(request, "/expenditureTrackingOrganization/selectResponsibleProjectAccountingUnitToAddMember.jsp");
     }
 
     public final ActionForward addResponsibleProjectAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	final AccountingUnit accountingUnit = getDomainObject(request, "accountingUnitOid");
 	accountingUnit.addResponsibleProjectAccountants(person);
 	return viewPerson(mapping, request, person);
     }
 
     public final ActionForward prepareAddToProjectAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	request.setAttribute("person", person);
 	final Set<AccountingUnit> accountingUnits = ExpenditureTrackingSystem.getInstance().getAccountingUnitsSet();
 	request.setAttribute("accountingUnits", accountingUnits);
 	return forward(request, "/expenditureTrackingOrganization/selectProjectAccountingUnitToAddMember.jsp");
     }
 
     public final ActionForward addToProjectAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	final AccountingUnit accountingUnit = getDomainObject(request, "accountingUnitOid");
 	accountingUnit.addProjectAccountants(person);
 	return viewPerson(mapping, request, person);
     }
 
     public final ActionForward prepareAddToTreasuryAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	request.setAttribute("person", person);
 	final Set<AccountingUnit> accountingUnits = ExpenditureTrackingSystem.getInstance().getAccountingUnitsSet();
 	request.setAttribute("accountingUnits", accountingUnits);
 	return forward(request, "/expenditureTrackingOrganization/selectTreasuryAccountingUnitToAddMember.jsp");
     }
 
     public final ActionForward addToTreasuryAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	final AccountingUnit accountingUnit = getDomainObject(request, "accountingUnitOid");
 	accountingUnit.addTreasuryMembers(person);
 	return viewPerson(mapping, request, person);
     }
 
     public final ActionForward expandAuthorizationUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final UnitBean unitBean = getRenderedObject();
 	final Person person = getDomainObject(request, "personOid");
 	if (unitBean == null || unitBean.getUnit() == null) {
 	    final Unit unit = getDomainObject(request, "unitOid");
 	    return expandAuthorizationUnit(mapping, request, person, unit);
 	} else {
 	    return expandAuthorizationUnit(mapping, request, person, unitBean.getUnit());
 	}
     }
 
     public final ActionForward expandAuthorizationUnit(final ActionMapping mapping, final HttpServletRequest request,
 	    final Person person, final Unit unit) {
 	request.setAttribute("person", person);
 	request.setAttribute("unit", unit);
 	final Set<Unit> units = unit == null ? ExpenditureTrackingSystem.getInstance().getTopLevelUnitsSet() : unit
 		.getSubUnitsSet();
 	request.setAttribute("units", units);
 	final UnitBean unitBean = new UnitBean();
 	request.setAttribute("unitBean", unitBean);
 	return forward(request, "/expenditureTrackingOrganization/changeAuthorizationUnit.jsp");
     }
 
 /*    public final ActionForward changeAuthorizationUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	final Unit unit = getDomainObject(request, "unitOid");
 	person.createAuthorization(unit);
 	return viewPerson(mapping, request, person);
     }
 */
     public final ActionForward prepareCreateAuthorizationUnitWithoutPerson(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Unit unit = getDomainObject(request, "unitOid");
 	return prepareCreateAuthorizationUnit(request, null, unit, true);
     }
 
     public final ActionForward prepareCreateAuthorizationUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Person person = getDomainObject(request, "personOid");
 	final Unit unit = getDomainObject(request, "unitOid");
 	return prepareCreateAuthorizationUnit(request, person, unit, false);
     }
 
     public final ActionForward prepareCreateAuthorizationUnit(final HttpServletRequest request, 
 	    final Person person, final Unit unit, final boolean returnToUnitInterface) {
 	final AuthorizationBean authorizationBean = new AuthorizationBean(person, unit, returnToUnitInterface);
 	request.setAttribute("authorizationBean", authorizationBean);
 	request.setAttribute("person", person);
 	request.setAttribute("unit", unit);
 	return forward(request, "/expenditureTrackingOrganization/createAuthorizationUnit.jsp");
     }
 
     public final ActionForward createAuthorizationUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AuthorizationBean authorizationBean = getRenderedObject();
 	authorizationBean.getPerson().createAuthorization(authorizationBean, authorizationBean.getJustification());
 	if (authorizationBean.isReturnToUnitInterface()) {
 	    final UnitBean unitBean = new UnitBean(authorizationBean.getUnit());;
 	    request.setAttribute("unitBean", unitBean);
 	    return viewOrganization(mapping, request, authorizationBean.getUnit());
 	}
 	return viewPerson(mapping, request, authorizationBean.getPerson());
     }
 
     public final ActionForward deleteAuthorization(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Authorization authorization = getDomainObject(request, "authorizationOid");
 	final Person person = authorization.getPerson();
 	authorization.delete();
 	return viewPerson(mapping, request, person);
     }
 
     public final ActionForward manageSuppliers(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	SupplierBean supplierBean = getRenderedObject("supplierBean");
 	if (supplierBean == null) {
 	    supplierBean = new SupplierBean();
 	    Supplier supplier = getDomainObject(request, "supplierOid");
 	    supplierBean.setSupplier(supplier);
 	}
 
 	request.setAttribute("supplierBean", supplierBean);
 	return forward(request, "/expenditureTrackingOrganization/manageSuppliers.jsp");
     }
 
     public final ActionForward manageSuppliers(final ActionMapping mapping, final HttpServletRequest request,
 	    final SupplierBean supplierBean) {
 	request.setAttribute("supplierBean", supplierBean);
 	return forward(request, "/expenditureTrackingOrganization/manageSuppliers.jsp");
     }
 
     public final ActionForward viewSupplier(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Supplier supplier = getDomainObject(request, "supplierOid");
 
 	return viewSupplier(mapping, form, request, response, supplier);
     }
 
     private final ActionForward viewSupplier(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response, final Supplier supplier) {
 	request.setAttribute("supplier", supplier);
 	return forward(request, "/expenditureTrackingOrganization/viewSupplier.jsp");
     }
 
     public final ActionForward prepareCreateSupplier(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final CreateSupplierBean createSupplierBean = new CreateSupplierBean();
 
 	request.setAttribute("bean", createSupplierBean);
 	return forward(request, "/expenditureTrackingOrganization/createSupplier.jsp");
     }
 
     public final ActionForward createSupplier(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final CreateSupplierBean createSupplierBean = getRenderedObject();
 	final Supplier supplier = Supplier.createNewSupplier(createSupplierBean);
 	final SupplierBean supplierBean = new SupplierBean(supplier);
 	return manageSuppliers(mapping, request, supplierBean);
     }
 
     public final ActionForward prepareEditSupplier(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Supplier supplier = getDomainObject(request, "supplierOid");
 
 	request.setAttribute("supplier", supplier);
 	return forward(request, "/expenditureTrackingOrganization/editSupplier.jsp");
     }
 
     public final ActionForward editSupplier(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final Supplier supplier = getRenderedObject();
 	final SupplierBean supplierBean = new SupplierBean(supplier);
 	return manageSuppliers(mapping, request, supplierBean);
     }
 
     public final ActionForward deleteSupplier(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Supplier supplier = getDomainObject(request, "supplierOid");
 	supplier.delete();
 	return manageSuppliers(mapping, form, request, response);
     }
 
     public ActionForward revokeAuthorization(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final Authorization authorization = getDomainObject(request, "authorizationOid");
 
 	try {
 	    authorization.revoke();
 	} catch (DomainException e) {
 	    addMessage(request, e.getMessage());
 	}
 
 	return viewAuthorization(mapping, form, request, response);
     }
 
     public ActionForward delegateAuthorization(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	Authorization authorization = getDomainObject(request, "authorizationOid");
 	Unit unit = getDomainObject(request, "unitOid");
 	request.setAttribute("authorization", authorization);
 	Person person = getLoggedPerson();
 
 	if (authorization.getPerson() == person && authorization.getCanDelegate()) {
 	    AuthorizationBean bean = new AuthorizationBean(authorization);
 	    bean.setUnit(unit);
 	    request.setAttribute("bean", bean);
 	} else {
 	    addMessage(request, "label.unable.to.delegate.that.action");
 	}
 
 	return forward(request, "/expenditureTrackingOrganization/delegateAuthorization.jsp");
     }
 
     public ActionForward chooseDelegationUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Authorization authorization = getDomainObject(request, "authorizationOid");
 	request.setAttribute("authorization", authorization);
 	Unit unit = getDomainObject(request, "unitOid");
 	if (unit == null) {
 	    unit = authorization.getUnit();
 	}
 	request.setAttribute("unit", unit);
 	request.setAttribute("units", unit.getSubUnitsSet());
 	return forward(request, "/expenditureTrackingOrganization/delegateChooseUnit.jsp");
     }
 
     public ActionForward createDelegation(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	final AuthorizationBean bean = getRenderedObject("bean");
 	try {
 	    DelegatedAuthorization.delegate(bean.getAuthorization(), bean.getPerson(), bean.getUnit(), bean.getCanDelegate(),
 		    bean.getEndDate(), bean.getMaxAmount());
 	} catch (DomainException e) {
 	    addMessage(request, e.getMessage());
 	    request.setAttribute("bean", bean);
 	    return forward(request, "/expenditureTrackingOrganization/delegateAuthorization.jsp");
 	}
 	RenderUtils.invalidateViewState();
 	request.setAttribute("authorization", bean.getAuthorization());
 	return forward(request, "/expenditureTrackingOrganization/viewAuthorization.jsp");
     }
 
     public ActionForward removeResponsibleFromAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AccountingUnit accountingUnit = getDomainObject(request, "accountingUnitOid");
 	final Person person = getDomainObject(request, "personOid");
 	accountingUnit.removeResponsiblePeople(person);
 	return viewPerson(mapping, request, person);
     }
 
     public ActionForward removePersonFromAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AccountingUnit accountingUnit = getDomainObject(request, "accountingUnitOid");
 	final Person person = getDomainObject(request, "personOid");
 	accountingUnit.removePeople(person);
 	return viewPerson(mapping, request, person);
     }
 
     public ActionForward removePersonResponsibleForProjectAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AccountingUnit accountingUnit = getDomainObject(request, "accountingUnitOid");
 	final Person person = getDomainObject(request, "personOid");
 	accountingUnit.removeResponsibleProjectAccountants(person);
 	return viewPerson(mapping, request, person);
     }
 
     public ActionForward removePersonFromProjectAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AccountingUnit accountingUnit = getDomainObject(request, "accountingUnitOid");
 	final Person person = getDomainObject(request, "personOid");
 	accountingUnit.removeProjectAccountants(person);
 	return viewPerson(mapping, request, person);
     }
 
     public ActionForward removePersonFromTreasuryAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AccountingUnit accountingUnit = getDomainObject(request, "accountingUnitOid");
 	final Person person = getDomainObject(request, "personOid");
 	accountingUnit.removeTreasuryMembers(person);
 	return viewPerson(mapping, request, person);
     }
 
     public ActionForward viewAccountingUnit(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 	final AccountingUnit accountingUnit = getDomainObject(request, "accountingUnitOid");
 	request.setAttribute("accountingUnit", accountingUnit);
 	return forward(request, "/expenditureTrackingOrganization/viewAccountingUnit.jsp");
     }
 
     public ActionForward prepareAddUnitToAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AccountingUnit accountingUnit = getDomainObject(request, "accountingUnitOid");
 	request.setAttribute("accountingUnit", accountingUnit);
 
 	UnitBean unitBean = getRenderedObject();
 	if (unitBean == null || unitBean.getUnit() == null) {
 	    final Unit unit = getDomainObject(request, "unitOid");
 	    if (unit != null) {
 		unitBean = new UnitBean(unit);
 	    }
 	}
 	if (unitBean == null) {
 	    unitBean = new UnitBean();
 	}
 	request.setAttribute("unitBean", unitBean);
 
 	final Set<Unit> units = unitBean.getUnit() == null ? ExpenditureTrackingSystem.getInstance().getTopLevelUnitsSet()
 		: unitBean.getUnit().getSubUnitsSet();
 	request.setAttribute("units", units);
 
 	RenderUtils.invalidateViewState();
 
 	return forward(request, "/expenditureTrackingOrganization/addUnitToAccountingUnit.jsp");
     }
 
     public ActionForward addUnitToAccountingUnit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final AccountingUnit accountingUnit = getDomainObject(request, "accountingUnitOid");
 	request.setAttribute("accountingUnit", accountingUnit);
 
 	final Unit unit;
 	final UnitBean unitBean = getRenderedObject();
 	if (unitBean == null) {
 	    unit = getDomainObject(request, "unitOid");
 	} else {
 	    RenderUtils.invalidateViewState();
 	    unit = unitBean.getUnit();
 	}
 
 	accountingUnit.addUnits(unit);
 
 	return forward(request, "/expenditureTrackingOrganization/viewAccountingUnit.jsp");
     }
 
     public ActionForward listSuppliers(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) throws IOException {
 
 	Spreadsheet suppliersSheet = getSuppliersSheet();
 	response.setContentType("application/xls ");
 	response.setHeader("Content-disposition", "attachment; filename=fornecedores.xls");
 
 	ServletOutputStream outputStream = response.getOutputStream();
 
 	suppliersSheet.exportToXLSSheet(outputStream);
 	outputStream.flush();
 	outputStream.close();
 
 	return null;
     }
 
     private Spreadsheet getSuppliersSheet() {
 	Spreadsheet spreadsheet = new Spreadsheet("Fornecedores");
 	spreadsheet.setHeader("Fornecedor");
 	spreadsheet.setHeader("NIF");
 	spreadsheet.setHeader("Total alocado");
 	spreadsheet.setHeader("Ajuste Directo");
 	spreadsheet.setHeader("Fundos de Maneio");
 	spreadsheet.setHeader("Reembolsos");
 	spreadsheet.setHeader("Por outras Vias");
 
 	for (Supplier supplier : ExpenditureTrackingSystem.getInstance().getSuppliers()) {
 	    Row row = spreadsheet.addRow();
 	    row.setCell(supplier.getName());
 	    row.setCell(supplier.getFiscalIdentificationCode());
 	    row.setCell(supplier.getTotalAllocated().getValue());
 	    row.setCell(supplier.getTotalAllocatedByAcquisitionProcesses().getValue());
 	    row.setCell(supplier.getTotalAllocatedByWorkingCapitals().getValue());
 	    row.setCell(supplier.getTotalAllocatedByRefunds().getValue());
 	    row.setCell(supplier.getTotalAllocatedByPurchases().getValue());
 	}
 
 	return spreadsheet;
     }
 
     public ActionForward listGiafSuppliers(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) throws IOException, SQLException {
 
 	Spreadsheet suppliersSheet = getGiafSuppliersSheet();
 	response.setContentType("application/xls ");
 	response.setHeader("Content-disposition", "attachment; filename=fornecedores.xls");
 
 	ServletOutputStream outputStream = response.getOutputStream();
 
 	suppliersSheet.exportToXLSSheet(outputStream);
 	outputStream.flush();
 	outputStream.close();
 
 	return null;
     }
 
     private Spreadsheet getGiafSuppliersSheet() throws SQLException {
 	Spreadsheet spreadsheet = new Spreadsheet("Fornecedores");
 	spreadsheet.setHeader("forn_cod_ent");
 	spreadsheet.setHeader("num_fis");
 	spreadsheet.setHeader("nom_ent");
 	spreadsheet.setHeader("nom_ent_abv");
 	spreadsheet.setHeader("canceled");
 	spreadsheet.setHeader("rua_ent");
 	spreadsheet.setHeader("loc_ent");
 	spreadsheet.setHeader("cod_pos");
 	spreadsheet.setHeader("cod_pai");
 	spreadsheet.setHeader("tel_ent");
 	spreadsheet.setHeader("fax_ent");
 	spreadsheet.setHeader("email");
 
 	final SupplierMap supplierMap = new SupplierMap();
 	final SupplierReader supplierReader = new SupplierReader(supplierMap);
 	supplierReader.execute();
 
 	for (final GiafSupplier giafSupplier : supplierMap.getGiafSuppliers()) {
 	    Row row = spreadsheet.addRow();
 	    row.setCell(giafSupplier.codEnt == null ? "" : giafSupplier.codEnt);
 	    row.setCell(giafSupplier.numFis == null ? "" : giafSupplier.numFis);
 	    row.setCell(giafSupplier.nom_ent == null ? "" : giafSupplier.nom_ent);
 	    row.setCell(giafSupplier.nom_ent_abv == null ? "" : giafSupplier.nom_ent_abv);
 	    row.setCell(Boolean.toString(giafSupplier.canceled));
 	    row.setCell(giafSupplier.ruaEnt == null ? "" : giafSupplier.ruaEnt);
 	    row.setCell(giafSupplier.locEnt == null ? "" : giafSupplier.locEnt);
 	    row.setCell(giafSupplier.codPos == null ? "" : giafSupplier.codPos);
 	    row.setCell(giafSupplier.codPai == null ? "" : giafSupplier.codPai);
 	    row.setCell(giafSupplier.telEnt == null ? "" : giafSupplier.telEnt);
 	    row.setCell(giafSupplier.faxEnt == null ? "" : giafSupplier.faxEnt);
 	    row.setCell(giafSupplier.email == null ? "" : giafSupplier.email);
 	}
 
 	return spreadsheet;
     }
 
     public final ActionForward editSupplierLimit(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Supplier supplier = getDomainObject(request, "supplierOid");
 	request.setAttribute("supplier", supplier);
 	return forward(request, "/expenditureTrackingOrganization/editSupplierLimit.jsp");
     }
 
     public final ActionForward prepareMergeSupplier(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Supplier supplier = getDomainObject(request, "supplierToTransferOID");
 	request.setAttribute("supplierToTransfer", supplier);
 
 	SupplierBean supplierBean = getRenderedObject("supplierBean");
 	if (supplierBean == null) {
 	    supplierBean = new SupplierBean();
 	}
 	request.setAttribute("supplierBean", supplierBean);
 
 	return forward(request, "/expenditureTrackingOrganization/mergeSupplier.jsp");
     }
 
     public final ActionForward mergeSupplier(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Supplier supplierToTransfer = getDomainObject(request, "supplierToTransferOID");
 	final Supplier supplierDestination = getDomainObject(request, "supplierDestinationOID");
 
 	supplierDestination.merge(supplierToTransfer);
 
 	final SupplierBean supplierBean = new SupplierBean(supplierDestination);
 
 	return manageSuppliers(mapping, request, supplierBean);
     }
 
     public final ActionForward downloadMGPProjects(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws IOException, SQLException {
 
 	Spreadsheet suppliersSheet = getMgpProjectsSheet();
 	response.setContentType("application/xls ");
 	response.setHeader("Content-disposition", "attachment; filename=projectosMgp.xls");
 
 	ServletOutputStream outputStream = response.getOutputStream();
 
 	suppliersSheet.exportToXLSSheet(outputStream);
 	outputStream.flush();
 	outputStream.close();
 
 	return null;
     }
 
     private Spreadsheet getMgpProjectsSheet() throws SQLException {
 	Spreadsheet spreadsheet = new Spreadsheet("ProjectosMGP");
 	spreadsheet.setHeader("projectCode");
 	spreadsheet.setHeader("unidExploracao");
 	spreadsheet.setHeader("title");
 	spreadsheet.setHeader("idCoord");
 	spreadsheet.setHeader("costCenter");
 	spreadsheet.setHeader("inicio");
 	spreadsheet.setHeader("duracao");
 	spreadsheet.setHeader("status");
 	spreadsheet.setHeader("tipo");
 	spreadsheet.setHeader("regime");
 
 	final ProjectReader projectReader = new ProjectReader();
 	projectReader.execute();
 	final Set<MgpProject> mgpProjects = projectReader.getMgpProjects();
 
 	for (final MgpProject mgpProject : mgpProjects) {
 	    final Row row = spreadsheet.addRow();
 	    row.setCell(mgpProject.getProjectCode());
 	    row.setCell(mgpProject.getUnidExploracao());
 	    row.setCell(mgpProject.getTitle());
 	    final StringBuilder stringBuilder = new StringBuilder();
 	    for (final String idCoord : mgpProject.getIdCoord()) {
 		if (stringBuilder.length() > 0) {
 		    stringBuilder.append(", ");
 		}
 		stringBuilder.append(idCoord);
 	    }
 	    row.setCell(stringBuilder.toString());
 	    row.setCell(mgpProject.getCostCenter());
 	    row.setCell(mgpProject.getInicio());
 	    row.setCell(mgpProject.getDuracao());
 	    row.setCell(mgpProject.getStatus());
 	    row.setCell(mgpProject.getType());
 	    row.setCell(determineProjectRegime(mgpProject.getProjectCode()));
 	}
 
 	return spreadsheet;
     }
 
     private String determineProjectRegime(final String projectCode) {
 	final Project project = Project.findProjectByCode(projectCode);
 	return project == null ? "" : (project.getDefaultRegeimIsCCP() != null && project.getDefaultRegeimIsCCP().booleanValue()
 		? "CCP" : "C&T");
     }
 
     public final ActionForward downloadSupplierAcquisitionInformation(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws IOException {
 
 	final Supplier supplier = getDomainObject(request, "supplierOid");
 	final Spreadsheet suppliersSheet = getSupplierAcquisitionInformationSheet(supplier);
 	response.setContentType("application/xls ");
 	response.setHeader("Content-disposition", "attachment; filename=fornecedor.xls");
 
 	final ServletOutputStream outputStream = response.getOutputStream();
 
 	suppliersSheet.exportToXLSSheet(outputStream);
 	outputStream.flush();
 	outputStream.close();
 
 	return null;
     }
 
     private Spreadsheet getSupplierAcquisitionInformationSheet(final Supplier supplier) {
 	Spreadsheet spreadsheet = new Spreadsheet(supplier.getPresentationName());
 	spreadsheet.setHeader("Identificação Processo");
 	spreadsheet.setHeader("Descrição");
 	spreadsheet.setHeader("Valor alocado ao fornecedor");
 	spreadsheet.setHeader("IVA");
 	spreadsheet.setHeader("Total");
 
 	Money totalForSupplierLimit = Money.ZERO;
 	Money totalForSupplier = Money.ZERO;
 
 	for (final AcquisitionAfterTheFact acquisitionAfterTheFact : supplier.getAcquisitionsAfterTheFactSet()) {
 	    if (!acquisitionAfterTheFact.getDeletedState().booleanValue()) {
 		final Money value = acquisitionAfterTheFact.getValue();
 		totalForSupplierLimit = totalForSupplierLimit.add(value);
 		totalForSupplier = totalForSupplier.add(value);
 
 		final Row row = spreadsheet.addRow();
 		row.setCell(acquisitionAfterTheFact.getAcquisitionProcessId());
 		row.setCell(acquisitionAfterTheFact.getDescription());
 		row.setCell(value.toFormatString());
 		row.setCell(acquisitionAfterTheFact.getVatValue());
 		row.setCell(value.toFormatString());
 	    }
 	}
 
 	for (final AcquisitionRequest acquisitionRequest : supplier.getAcquisitionRequestsSet()) {
 	    final AcquisitionProcess acquisitionProcess = acquisitionRequest.getAcquisitionProcess();
 	    if (acquisitionProcess.isActive() && acquisitionProcess.isAllocatedToSupplier()) {
 		final Money forSupplierLimit = acquisitionRequest.getCurrentSupplierAllocationValue();
 		final Money currentValue = acquisitionRequest.getCurrentTotalValue();
 		totalForSupplierLimit = totalForSupplierLimit.add(forSupplierLimit);
 		totalForSupplier = totalForSupplier.add(currentValue);
 
 		final Row row = spreadsheet.addRow();
 		row.setCell(acquisitionRequest.getAcquisitionProcessId());
 		row.setCell(acquisitionRequest.getClass().getSimpleName());
 		row.setCell(forSupplierLimit.toFormatString());
 		row.setCell(acquisitionRequest.getCurrentTotalVatValue().toFormatString());
 		row.setCell(currentValue.toFormatString());
 	    }
 	}
 
 	spreadsheet.addRow();
 	final Row row = spreadsheet.addRow();
 	row.setCell("Total");
 	row.setCell("");
 	row.setCell(totalForSupplierLimit.toFormatString());
 	row.setCell("");
 	row.setCell(totalForSupplier.toFormatString());
 
 	return spreadsheet;
     }
 
     public final ActionForward managePriorityCPVs(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	List<CPVReference> priorityCPVReferences = new ArrayList<CPVReference>(ExpenditureTrackingSystem.getInstance()
 		.getPriorityCPVReferences());
 	Collections.sort(priorityCPVReferences, new BeanComparator("code"));
 	request.setAttribute("cpvs", priorityCPVReferences);
 	request.setAttribute("bean", new VariantBean());
 
 	return forward(request, "/expenditureTrackingOrganization/priorityCVPs.jsp");
     }
 
     public final ActionForward addPriorityCPV(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	CPVReference reference = getRenderedObject("cpvToAdd");
 	reference.markAsPriority();
 	RenderUtils.invalidateViewState();
 
 	return managePriorityCPVs(mapping, form, request, response);
     }
 
     public final ActionForward removePriorityCPV(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	CPVReference reference = getDomainObject(request, "cpvId");
 	reference.unmarkAsPriority();
 	RenderUtils.invalidateViewState();
 
 	return managePriorityCPVs(mapping, form, request, response);
     }
 
     public final ActionForward viewAuthorizationLogs(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final Person person = getDomainObject(request, "personOid");
 	request.setAttribute("person", person);
 
 	final Unit unit = getDomainObject(request, "unitOid");
 	request.setAttribute("unit", unit);
 
 	final Set<AuthorizationLog> authorizationLogs;
 	if (person == null && unit == null) {
 	    authorizationLogs = null;
 	} else if (person == null) {
 	    authorizationLogs = unit.getSortedAuthorizationLogsSet();
 	} else if (unit == null) {
 	    authorizationLogs = person.getSortedAuthorizationLogsSet();
 	} else {
 	    authorizationLogs = new TreeSet<AuthorizationLog>(AuthorizationLog.COMPARATOR_BY_WHEN);
 	    for (final AuthorizationLog authorizationLog : unit.getAuthorizationLogsSet()) {
 		if (authorizationLog.getPerson() == person) {
 		    authorizationLogs.add(authorizationLog);
 		}
 	    }
 	}
 	request.setAttribute("authorizationLogs", authorizationLogs);
 
 	return forward(request, "/expenditureTrackingOrganization/viewAuthorizationLogs.jsp");
     }
 
     public final ActionForward manageObservers(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final Unit unit = getDomainObject(request, "unitOid");
 	request.setAttribute("unit", unit);
 
	if (!unit.isResponsible(Person.getLoggedPerson())) {
 	    return viewLoggedPerson(mapping, form, request, response);
 	}
 
 	request.setAttribute("bean", new SearchUsers());
 	return forward(request, "/expenditureTrackingOrganization/manageUnitObservers.jsp");
     }
 
     public final ActionForward addObserver(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) {
 
 	final Unit unit = getDomainObject(request, "unitOid");
 	SearchUsers bean = getRenderedObject("bean");
 
 	unit.addObservers(bean.getPerson());
 	RenderUtils.invalidateViewState("bean");
 
 	return manageObservers(mapping, form, request, response);
 
     }
 
     public final ActionForward removeObserver(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 
 	final Unit unit = getDomainObject(request, "unitOid");
 	final Person observer = getDomainObject(request, "observerOid");
 	unit.removeObservers(observer);
 	return manageObservers(mapping, form, request, response);
     }
 
     public final ActionForward changeDefaultRegeimIsCCP(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Unit unit = getDomainObject(request, "unitOid");
 	unit.toggleDefaultRegeim();
 	final UnitBean unitBean = new UnitBean(unit);
 	request.setAttribute("unitBean", unitBean);
 	return viewOrganization(mapping, request, unit);
     }
 
     public final ActionForward viewAcquisitionProcessStatistics(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	UserAcquisitionProcessStatistics userAcquisitionProcessStatistics = getRenderedObject();
 	if (userAcquisitionProcessStatistics == null) {
 	    final User user = getDomainObject(request, "userOid");
 	    final PaymentProcessYear paymentProcessYear = PaymentProcessYear.getPaymentProcessYearByYear(Integer.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
 	    userAcquisitionProcessStatistics = new UserAcquisitionProcessStatistics(user, paymentProcessYear);
 	} else {
 	    RenderUtils.invalidateViewState();
 	}
 	request.setAttribute("userAcquisitionProcessStatistics", userAcquisitionProcessStatistics);
 	return forward(request, "/expenditureTrackingOrganization/viewAcquisitionProcessStatistics.jsp");
     }
 
     public ActionForward listCPVReferences(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) {
 	final Set<CPVReference> cvpReferences = ExpenditureTrackingSystem.getInstance().getCPVReferencesSet();
 	final SortedSet<CPVReference> sortedCPVReferences = new TreeSet<CPVReference>(CPVReference.COMPARATOR_BY_DESCRIPTION);
 	sortedCPVReferences.addAll(cvpReferences);
 	request.setAttribute("cvpReferences", sortedCPVReferences);
 	return forward(request, "/expenditureTrackingOrganization/listCPVReferences.jsp");
     }
 
 }
