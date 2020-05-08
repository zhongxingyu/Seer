 package pt.ist.expenditureTrackingSystem.domain;
 
 import javax.servlet.http.HttpServletRequest;
 
 import module.dashBoard.WidgetRegister;
 import module.dashBoard.WidgetRegister.WidgetAditionPredicate;
 import module.dashBoard.domain.DashBoardPanel;
 import module.dashBoard.widgets.WidgetController;
 import module.organization.presentationTier.actions.OrganizationModelAction;
 import module.workflow.widgets.ProcessListWidget;
 import myorg.domain.ModuleInitializer;
 import myorg.domain.MyOrg;
 import myorg.domain.User;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.expenditureTrackingSystem.presentationTier.actions.organization.OrganizationModelPlugin.ExpendituresView;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.ActivateEmailNotificationWidget;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.MyProcessesWidget;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.MySearchesWidget;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.PendingRefundWidget;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.PendingSimplifiedWidget;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.PrioritiesWidget;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.TakenProcessesWidget;
 import pt.ist.expenditureTrackingSystem.presentationTier.widgets.UnreadCommentsWidget;
 import pt.ist.expenditureTrackingSystem.util.AquisitionsPendingProcessCounter;
 import pt.ist.expenditureTrackingSystem.util.RefundPendingProcessCounter;
 import pt.ist.fenixWebFramework.servlets.filters.contentRewrite.RequestChecksumFilter;
 import pt.ist.fenixWebFramework.servlets.filters.contentRewrite.RequestChecksumFilter.ChecksumPredicate;
 
 public class ExpenditureTrackingSystem extends ExpenditureTrackingSystem_Base implements ModuleInitializer {
 
     static {
 	ProcessListWidget.register(new AquisitionsPendingProcessCounter());
 	ProcessListWidget.register(new RefundPendingProcessCounter());
     }
    
     public static WidgetAditionPredicate EXPENDITURE_TRACKING_PANEL_PREDICATE = new WidgetAditionPredicate() {
 	@Override
 	public boolean canBeAdded(DashBoardPanel panel, User userAdding) {
 	    return (ExpenditureUserDashBoardPanel.class.isAssignableFrom(panel.getClass()));
 	}
     };
 
     public static WidgetAditionPredicate EXPENDITURE_SERVICES_ONLY_PREDICATE = new WidgetAditionPredicate() {
 
 	@Override
 	public boolean canBeAdded(DashBoardPanel panel, User userAdding) {
 	    return EXPENDITURE_TRACKING_PANEL_PREDICATE.canBeAdded(panel, userAdding)
 		    && (userAdding.getExpenditurePerson().hasRoleType(RoleType.ACQUISITION_CENTRAL)
 			    || !userAdding.getExpenditurePerson().getAccountingUnits().isEmpty() || !userAdding
 			    .getExpenditurePerson().getProjectAccountingUnits().isEmpty());
 	}
     };
 
     private static boolean isInitialized = false;
 
     private static ThreadLocal<ExpenditureTrackingSystem> init = null;
 
     public static ExpenditureTrackingSystem getInstance() {
 	if (init != null) {
 	    return init.get();
 	}
 
 	if (!isInitialized) {
 	    initialize();
 	}
 	final MyOrg myOrg = MyOrg.getInstance();
 	return myOrg.getExpenditureTrackingSystem();
     }
 
     public static void initialize() {
 	if (!isInitialized) {
 	    try {
 		final MyOrg myOrg = MyOrg.getInstance();
 		final ExpenditureTrackingSystem expendituretrackingSystem = myOrg.getExpenditureTrackingSystem();
 		if (expendituretrackingSystem == null) {
 		    new ExpenditureTrackingSystem();
 		}
 		init = new ThreadLocal<ExpenditureTrackingSystem>();
 		init.set(myOrg.getExpenditureTrackingSystem());
 
 		initRoles();
 		initSystemSearches();
 		registerChecksumFilterException();
 		OrganizationModelAction.partyViewHookManager.register(new ExpendituresView());
 		isInitialized = true;
 	    } finally {
 		init = null;
 	    }
 	}
     }
 
     private static void registerChecksumFilterException() {
 	RequestChecksumFilter.registerFilterRule(new ChecksumPredicate() {
 
 	    @Override
 	    public boolean shouldFilter(HttpServletRequest request) {
 		return !(request.getQueryString() != null && request.getQueryString().contains(
 			"method=calculateShareValuesViaAjax"));
 	    }
 
 	});
 
 	RequestChecksumFilter.registerFilterRule(new ChecksumPredicate() {
 	    public boolean shouldFilter(HttpServletRequest httpServletRequest) {
 		return !(httpServletRequest.getRequestURI().endsWith("/acquisitionSimplifiedProcedureProcess.do")
 			&& httpServletRequest.getQueryString() != null && httpServletRequest.getQueryString().contains(
 			"method=checkSupplierLimit"));
 	    }
 	});
 
 	RequestChecksumFilter.registerFilterRule(new ChecksumPredicate() {
 	    public boolean shouldFilter(HttpServletRequest httpServletRequest) {
 		return !(httpServletRequest.getRequestURI().endsWith("/viewRCISTAnnouncements.do"));
 	    }
 	});
 
 	RequestChecksumFilter.registerFilterRule(new ChecksumPredicate() {
 	    public boolean shouldFilter(HttpServletRequest httpServletRequest) {
		return !(httpServletRequest.getRequestURI().endsWith("/expenditureProcesses.do"))
			&& httpServletRequest.getQueryString() != null
			&& httpServletRequest.getQueryString().contains("method=viewTypeDescription");
 	    }
 	});
 
     }
 
     private static void initRoles() {
 	for (final RoleType roleType : RoleType.values()) {
 	    Role.getRole(roleType);
 	}
     }
 
     protected static void initSystemSearches() {
 	final ExpenditureTrackingSystem expendituretrackingSystem = getInstance();
 	if (expendituretrackingSystem.getSystemSearches().isEmpty()) {
 	    new MyOwnProcessesSearch();
 	    final SavedSearch savedSearch = new PendingProcessesSearch();
 	    for (final Person person : expendituretrackingSystem.getPeopleSet()) {
 		person.setDefaultSearch(savedSearch);
 	    }
 	}
     }
 
     private ExpenditureTrackingSystem() {
 	super();
 	setMyOrg(MyOrg.getInstance());
 	setAcquisitionRequestDocumentCounter(0);
     }
 
     public String nextAcquisitionRequestDocumentID() {
 	return "D" + getAndUpdateNextAcquisitionRequestDocumentCountNumber();
     }
 
     public Integer nextAcquisitionRequestDocumentCountNumber() {
 	return getAndUpdateNextAcquisitionRequestDocumentCountNumber();
     }
 
     private Integer getAndUpdateNextAcquisitionRequestDocumentCountNumber() {
 	setAcquisitionRequestDocumentCounter(getAcquisitionRequestDocumentCounter().intValue() + 1);
 	return getAcquisitionRequestDocumentCounter();
     }
 
     @Override
     public void init(final MyOrg root) {
 	initialize();
 	registerWidget(MySearchesWidget.class);
 	registerWidget(UnreadCommentsWidget.class);
 	registerWidget(TakenProcessesWidget.class);
 	registerWidget(MyProcessesWidget.class);
 	registerWidget(PendingRefundWidget.class);
 	registerWidget(PendingSimplifiedWidget.class);
 	registerWidget(ActivateEmailNotificationWidget.class);
 	WidgetRegister.registerWidget(PrioritiesWidget.class, EXPENDITURE_SERVICES_ONLY_PREDICATE);
     }
 
     private static void registerWidget(Class<? extends WidgetController> widgetClass) {
 	WidgetRegister.registerWidget(widgetClass, EXPENDITURE_TRACKING_PANEL_PREDICATE);
     }
 
 }
