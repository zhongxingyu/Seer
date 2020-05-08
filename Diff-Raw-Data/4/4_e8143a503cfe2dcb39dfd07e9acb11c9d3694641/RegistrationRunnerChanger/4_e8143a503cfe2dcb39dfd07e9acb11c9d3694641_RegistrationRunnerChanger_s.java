 package is.idega.idegaweb.pheidippides.presentation;
 
 import is.idega.idegaweb.pheidippides.PheidippidesConstants;
 import is.idega.idegaweb.pheidippides.bean.PheidippidesBean;
 import is.idega.idegaweb.pheidippides.business.PheidippidesService;
 import is.idega.idegaweb.pheidippides.dao.PheidippidesDao;
 import is.idega.idegaweb.pheidippides.data.Registration;
 import is.idega.idegaweb.pheidippides.output.ReceiptWriter;
 
 import javax.faces.context.FacesContext;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.idega.block.web2.business.JQuery;
 import com.idega.facelets.ui.FaceletComponent;
 import com.idega.idegaweb.IWBundle;
 import com.idega.presentation.IWBaseComponent;
 import com.idega.presentation.IWContext;
 import com.idega.user.data.User;
 import com.idega.util.CoreConstants;
 import com.idega.util.PresentationUtil;
 import com.idega.util.expression.ELUtil;
 
 public class RegistrationRunnerChanger extends IWBaseComponent {
 
 	private static final String PARAMETER_REGISTRATION = "prm_registration_pk";
 	private static final String PARAMETER_SSN = "prm_ssn";
 	private static final String PARAMETER_EMAIL = "prm_email";
 	private static final String PARAMETER_PHONE = "prm_phone";
 
 	private static final String PARAMETER_ACTION = "prm_action";
 	private static final int ACTION_PHASE_ONE = 1;
 	private static final int ACTION_SAVE = 2;
 
 	@Autowired
 	private PheidippidesService service;
 
 	@Autowired
 	private PheidippidesDao dao;
 
 	@Autowired
 	private JQuery jQuery;
 
 	private IWBundle iwb;
 
 	@Override
 	protected void initializeComponent(FacesContext context) {
 		IWContext iwc = IWContext.getIWContext(context);
 		if (iwc.isLoggedOn()) {
 			User user = iwc.getCurrentUser();
 			iwb = getBundle(context, getBundleIdentifier());
 
 			Long registrationPK = iwc.isParameterSet(PARAMETER_REGISTRATION) ? Long
 					.parseLong(iwc.getParameter(PARAMETER_REGISTRATION)) : null;
			Registration registration = dao.getRegistration(registrationPK);
 			
 			//Display error here when trying to change registration for another user
 			if (!registration.getUserUUID().equals(user.getUniqueId())) {
 				return;
 			}
 
 			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getJQuery()
 					.getBundleURIToJQueryLib());
 			PresentationUtil.addJavaScriptSourcesLinesToHeader(
 					iwc,
 					getJQuery().getBundleURISToValidation(
 							iwc.getCurrentLocale().getLanguage()));
 
 			PresentationUtil.addJavaScriptSourceLineToHeader(iwc,
 					CoreConstants.DWR_ENGINE_SCRIPT);
 			PresentationUtil.addJavaScriptSourceLineToHeader(iwc,
 					CoreConstants.DWR_UTIL_SCRIPT);
 			PresentationUtil.addJavaScriptSourceLineToHeader(iwc,
 					"/dwr/interface/PheidippidesService.js");
 /*			PresentationUtil
 					.addJavaScriptSourceLineToHeader(
 							iwc,
 							iwb.getVirtualPathWithFileNameString("javascript/participantDistanceChanger.js"));*/
 
 			PresentationUtil
 					.addStyleSheetToHeader(
 							iwc,
 							iwb.getVirtualPathWithFileNameString("style/pheidippides.css"));
 
 			PheidippidesBean bean = getBeanInstance("pheidippidesBean");
 			bean.setLocale(iwc.getCurrentLocale());
 			bean.setRegistration(registration);
 			bean.setEvent(registration.getRace().getEvent());
			bean.setDownloadWriter(ReceiptWriter.class);
 
 			FaceletComponent facelet = (FaceletComponent) iwc.getApplication()
 					.createComponent(FaceletComponent.COMPONENT_TYPE);
 
 			switch (parseAction(iwc, registration)) {
 			case ACTION_PHASE_ONE:
 				facelet.setFaceletURI(iwb
 						.getFaceletURI("registrationRunnerChanger/phaseOne.xhtml"));
 				break;
 
 			case ACTION_SAVE:
 				String ssn = iwc.getParameter(PARAMETER_SSN);
 				String email = iwc.getParameter(PARAMETER_EMAIL);
 				String phone = iwc.getParameter(PARAMETER_PHONE);
 
 
 				boolean couldChangeRunner = getService()
 						.changeRegistrationRunner(registration,
 								ssn, email, phone);
 
 				if (couldChangeRunner) {
 					facelet.setFaceletURI(iwb
 							.getFaceletURI("registrationRunnerChanger/save.xhtml"));
 				} else {
 					facelet.setFaceletURI(iwb
 							.getFaceletURI("registrationRunnerChanger/error.xhtml"));
 				}
 				break;
 
 			}
 			add(facelet);
 		}
 	}
 
 	private int parseAction(IWContext iwc, Registration registration) {
 		int action = iwc.isParameterSet(PARAMETER_ACTION) ? Integer
 				.parseInt(iwc.getParameter(PARAMETER_ACTION))
 				: ACTION_PHASE_ONE;
 		return action;
 	}
 
 	private String getBundleIdentifier() {
 		return PheidippidesConstants.IW_BUNDLE_IDENTIFIER;
 	}
 
 	private PheidippidesService getService() {
 		if (service == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 
 		return service;
 	}
 
 	private PheidippidesDao getDao() {
 		if (dao == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 
 		return dao;
 	}
 
 	private JQuery getJQuery() {
 		if (jQuery == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 
 		return jQuery;
 	}
 }
