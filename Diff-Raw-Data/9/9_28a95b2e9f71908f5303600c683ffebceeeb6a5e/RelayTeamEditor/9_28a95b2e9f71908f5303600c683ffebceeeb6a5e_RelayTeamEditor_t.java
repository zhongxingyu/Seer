 package is.idega.idegaweb.pheidippides.presentation;
 
 import is.idega.idegaweb.pheidippides.PheidippidesConstants;
 import is.idega.idegaweb.pheidippides.bean.PheidippidesBean;
 import is.idega.idegaweb.pheidippides.business.PheidippidesService;
 import is.idega.idegaweb.pheidippides.business.RegistrationStatus;
 import is.idega.idegaweb.pheidippides.dao.PheidippidesDao;
 import is.idega.idegaweb.pheidippides.data.Participant;
 import is.idega.idegaweb.pheidippides.data.Registration;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.faces.context.FacesContext;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.idega.block.web2.business.JQuery;
 import com.idega.block.web2.business.JQueryPlugin;
 import com.idega.builder.bean.AdvancedProperty;
 import com.idega.facelets.ui.FaceletComponent;
 import com.idega.idegaweb.IWBundle;
 import com.idega.presentation.IWBaseComponent;
 import com.idega.presentation.IWContext;
 import com.idega.user.data.User;
 import com.idega.util.CoreConstants;
 import com.idega.util.LocaleUtil;
 import com.idega.util.PresentationUtil;
 import com.idega.util.expression.ELUtil;
import com.idega.util.text.SocialSecurityNumber;
 
 public class RelayTeamEditor extends IWBaseComponent {
 
 	private static final String PARAMETER_ACTION = "prm_action";
 
 	private static final String PARAMETER_REGISTRATION = "prm_registration_pk";
 	private static final String PARAMETER_TEAM_NAME = "prm_team_name";
 	private static final String PARAMETER_PERSONAL_ID_RELAY = "prm_personalId";
 	private static final String PARAMETER_RELAY_LEG = "prm_relay_leg";
 	private static final String PARAMETER_RELAY_LEG_FIRST = "prm_relay_leg_first";
 	private static final String PARAMETER_EMAIL = "prm_email";
 	private static final String PARAMETER_SHIRT_SIZE = "prm_shirt_size";
 	private static final String PARAMETER_NAME = "prm_name";
 	private static final String PARAMETER_DATE_OF_BIRTH = "prm_date_of_birth";
 	
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
 
 			List<RegistrationStatus> statuses = new ArrayList<RegistrationStatus>();
 			statuses.add(RegistrationStatus.OK);
 
 			Long registrationPK = iwc.isParameterSet(PARAMETER_REGISTRATION) ? Long.parseLong(iwc.getParameter(PARAMETER_REGISTRATION)) : null;
 			List<Registration> registrations = getDao().getRegistrations(user.getUniqueId(), statuses);
 			Registration registration = null;
 			for (Registration reg : registrations) {
 				if (registrationPK != null && reg.getId().equals(registrationPK)) {
 					registration = reg;
 				}
 			}
 			
 			if (iwc.isParameterSet(PARAMETER_ACTION)) {
 				String relayLeg = iwc.getParameter(PARAMETER_RELAY_LEG_FIRST);
 				String teamName = iwc.getParameter(PARAMETER_TEAM_NAME);
 				
 				List<Participant> relayPartners = new ArrayList<Participant>();
 				String[] personalIDs = iwc.getParameterValues(PARAMETER_PERSONAL_ID_RELAY);
 				String[] datesOfBirth = iwc.getParameterValues(PARAMETER_DATE_OF_BIRTH);
 				String[] names = iwc.getParameterValues(PARAMETER_NAME);
 				String[] shirtSizes = iwc.getParameterValues(PARAMETER_SHIRT_SIZE);
 				String[] relayLegs = iwc.getParameterValues(PARAMETER_RELAY_LEG);
 				String[] emails = iwc.getParameterValues(PARAMETER_EMAIL);
 				
 				DateFormat format = new SimpleDateFormat("dd.MM.yyyy");
 				
 				for (int i = 0; i < names.length; i++) {
 					if (names[i] != null && names[i].length() > 0) {
 						Participant participant = new Participant();
 						participant.setFullName(names[i]);
 						if (iwc.getCurrentLocale().equals(LocaleUtil.getIcelandicLocale())) {
 							participant.setPersonalId(personalIDs[i]);
							participant.setDateOfBirth(SocialSecurityNumber.getDateFromSocialSecurityNumber(participant.getPersonalId()));
 						}
 						else {
 							try {
 								participant.setDateOfBirth(format.parse(datesOfBirth[i]));
 							} catch (ParseException e) {
 								e.printStackTrace();
 							}
 						}
 						participant.setShirtSize(shirtSizes[i] != null && shirtSizes[i].length() > 0 ? getDao().getShirtSize(Long.parseLong(shirtSizes[i])) : null);
 						participant.setRelayLeg(relayLegs[i]);
 						participant.setEmail(emails[i]);
 						relayPartners.add(participant);
 					}
 				}
 				
 				getService().updateRelayTeam(registration, relayLeg, teamName, relayPartners);
 			}
 
 			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getJQuery().getBundleURIToJQueryLib());
 			PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, getJQuery().getBundleURISToValidation(iwc.getCurrentLocale().getLanguage()));
 			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getJQuery().getBundleURIToJQueryPlugin(JQueryPlugin.MASKED_INPUT));
 
 			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, CoreConstants.DWR_ENGINE_SCRIPT);
 			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, CoreConstants.DWR_UTIL_SCRIPT);
 			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, "/dwr/interface/PheidippidesService.js");
 			PresentationUtil.addJavaScriptSourceLineToHeader(iwc, iwb.getVirtualPathWithFileNameString("javascript/relayTeamEditor.js"));
 
 			PresentationUtil.addStyleSheetToHeader(iwc, iwb.getVirtualPathWithFileNameString("style/pheidippides.css"));
 
 			PheidippidesBean bean = getBeanInstance("pheidippidesBean");
 			bean.setLocale(iwc.getCurrentLocale());
 			bean.setRegistration(registration);
 
 			if (registration != null) {
 				bean.setParticipant(getService().getParticipant(registration));
 
 				List<AdvancedProperty> properties = new ArrayList<AdvancedProperty>();
 				for (int i = 0; i < registration.getRace().getNumberOfRelayLegs() - 1; i++) {
 					properties.add(new AdvancedProperty(String.valueOf(i + 2), String.valueOf(i + 2)));
 				}
 				bean.setProperties(properties);
 			
 				Map<String, Registration> partnerMap = new HashMap<String, Registration>();
 				Map<Registration, Participant> participantsMap = new HashMap<Registration, Participant>();
 				List<Registration> relayPartners = getService().getRelayPartners(registration);
 				if (relayPartners != null) {
 					int index = 2;
 					for (Registration partnerRegistration : relayPartners) {
 						partnerMap.put(String.valueOf(index++), partnerRegistration);
 						participantsMap.put(partnerRegistration, getService().getParticipant(partnerRegistration));
 					}
 				}
 				bean.setRegistrationMap(partnerMap);
 				bean.setParticipantsMap(participantsMap);
 
 				bean.setRaceShirtSizes(getDao().getRaceShirtSizes(registration.getRace()));
 			}
 			
 			FaceletComponent facelet = (FaceletComponent) iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
 			facelet.setFaceletURI(iwb.getFaceletURI("relayTeamEditor/view.xhtml"));
 			add(facelet);
 		}
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
