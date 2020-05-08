 package is.idega.idegaweb.pheidippides.presentation;
 
 import is.idega.idegaweb.pheidippides.PheidippidesConstants;
 import is.idega.idegaweb.pheidippides.bean.PheidippidesCompanyBean;
 import is.idega.idegaweb.pheidippides.business.PheidippidesService;
 import is.idega.idegaweb.pheidippides.business.RegistrationStatus;
 import is.idega.idegaweb.pheidippides.dao.PheidippidesDao;
 import is.idega.idegaweb.pheidippides.data.Company;
 import is.idega.idegaweb.pheidippides.data.Event;
 import is.idega.idegaweb.pheidippides.data.Participant;
 import is.idega.idegaweb.pheidippides.data.Registration;
 import is.idega.idegaweb.pheidippides.output.ParticipantsWriter;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import javax.faces.context.FacesContext;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.idega.block.web2.business.JQuery;
 import com.idega.block.web2.business.JQueryPlugin;
 import com.idega.block.web2.business.JQueryUIType;
 import com.idega.block.web2.business.Web2Business;
 import com.idega.builder.bean.AdvancedProperty;
 import com.idega.builder.business.BuilderLogicWrapper;
 import com.idega.event.IWPageEventListener;
 import com.idega.facelets.ui.FaceletComponent;
 import com.idega.idegaweb.IWBundle;
 import com.idega.idegaweb.IWException;
 import com.idega.idegaweb.IWMainApplication;
 import com.idega.presentation.IWBaseComponent;
 import com.idega.presentation.IWContext;
 import com.idega.presentation.ui.handlers.IWDatePickerHandler;
 import com.idega.util.CoreConstants;
 import com.idega.util.IWTimestamp;
 import com.idega.util.PresentationUtil;
 import com.idega.util.expression.ELUtil;
 
 public class CompanyParticipantsList extends IWBaseComponent implements IWPageEventListener {
 
 	private static final String PARAMETER_ACTION = "prm_action";
 	private static final int ACTION_VIEW = 1;
 	private static final int ACTION_EDIT = 2;
 	private static final int ACTION_DELETE = 3;
 	
 	private static final String PARAMETER_RACE_PK = "prm_race_pk";
 	private static final String PARAMETER_REGISTRATION_PK = "prm_registration_pk";
 	private static final String PARAMETER_SHIRT_SIZE_PK = "prm_shirt_size";
 	private static final String PARAMETER_NATIONALITY = "prm_nationality";
 	private static final String PARAMETER_NAME = "prm_name";
 	private static final String PARAMETER_DATE_OF_BIRTH = "prm_date_of_birth";
 	private static final String PARAMETER_ADDRESS = "prm_address";
 	private static final String PARAMETER_POSTAL_CODE = "prm_postal_code";
 	private static final String PARAMETER_CITY = "prm_city";
 	private static final String PARAMETER_COUNTRY_PK = "prm_country";
 	private static final String PARAMETER_GENDER = "prm_gender";
 	private static final String PARAMETER_EMAIL = "prm_email";
 	private static final String PARAMETER_PHONE = "prm_phone";
 	private static final String PARAMETER_MOBILE = "prm_mobile";
 
 	@Autowired
 	private PheidippidesService service;
 	
 	@Autowired
 	private PheidippidesDao dao;
 	
 	@Autowired
 	private BuilderLogicWrapper builderLogicWrapper;
 	
 	@Autowired
 	private Web2Business web2Business;
 	
 	@Autowired
 	private JQuery jQuery;
 	
 	private IWBundle iwb;
 
 	@Override
 	protected void initializeComponent(FacesContext context) {
 		IWContext iwc = IWContext.getIWContext(context);
 		iwb = getBundle(context, getBundleIdentifier());
 
 		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getJQuery().getBundleURIToJQueryLib());
 		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, getWeb2Business().getBundleURIsToFancyBoxScriptFiles());
 		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getJQuery().getBundleURIToJQueryPlugin(JQueryPlugin.TABLE_SORTER));
 		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, getJQuery().getBundleURISToValidation());
 
 		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, CoreConstants.DWR_ENGINE_SCRIPT);
 		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, CoreConstants.DWR_UTIL_SCRIPT);
 		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, "/dwr/interface/PheidippidesService.js");
 
 		List<String> scripts = new ArrayList<String>();
 		JQuery jQuery = getJQuery();
 		scripts.add(jQuery.getBundleURIToJQueryLib());
 		scripts.add(jQuery.getBundleURIToJQueryUILib(JQueryUIType.UI_CORE));
 		scripts.add(jQuery.getBundleURIToJQueryUILib(JQueryUIType.UI_DATEPICKER));
 		if (!iwc.getCurrentLocale().equals(Locale.ENGLISH)) {
 			scripts.add(jQuery.getBundleURIToJQueryUILib("1.8.17/i18n", "ui.datepicker-" + iwc.getCurrentLocale().getLanguage() + ".js"));
 		}
 		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, scripts);
 
 		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, iwb.getVirtualPathWithFileNameString("javascript/companyParticipantsList.js"));
 	
 		PresentationUtil.addStyleSheetToHeader(iwc, getWeb2Business().getBundleURIToFancyBoxStyleFile());
 		PresentationUtil.addStyleSheetToHeader(iwc, jQuery.getBundleURIToJQueryUILib("1.8.17/themes/base", "ui.core.css"));
 		PresentationUtil.addStyleSheetToHeader(iwc, jQuery.getBundleURIToJQueryUILib("1.8.17/themes/base", "ui.theme.css"));
 		PresentationUtil.addStyleSheetToHeader(iwc, jQuery.getBundleURIToJQueryUILib("1.8.17/themes/base", "ui.datepicker.css"));
 		PresentationUtil.addStyleSheetToHeader(iwc, iwb.getVirtualPathWithFileNameString("style/pheidippides.css"));
 
 		List<AdvancedProperty> years = new ArrayList<AdvancedProperty>();
 		int year = new IWTimestamp().getYear();
 		while (year >= 2005) {
 			years.add(new AdvancedProperty(String.valueOf(year), String.valueOf(year--)));
 		}
 		
 		PheidippidesCompanyBean bean = getBeanInstance("pheidippidesCompanyBean");
 		bean.setResponseURL(getBuilderLogicWrapper().getBuilderService(iwc).getUriToObject(this.getClass(), new ArrayList<AdvancedProperty>()));
 		bean.setEventHandler(IWMainApplication.getEncryptedClassName(this.getClass()));
 		bean.setDownloadWriter(ParticipantsWriter.class);
 		bean.setLocale(iwc.getCurrentLocale());
 
 		/* Events */
 		Company company = getDao().getCompanyByUserUUID(iwc.getCurrentUser().getUniqueId()); 
 		Event event = company.getEvent();
 		bean.setEvent(event);
 		bean.setCompany(company);
 		
 		/* Years */
 		bean.setProperty(new AdvancedProperty(String.valueOf(IWTimestamp.RightNow().getYear()), String.valueOf(IWTimestamp.RightNow().getYear())));
 
 		/* Races */
 		if (bean.getEvent() != null && bean.getProperty() != null) {
 			bean.setRaces(getDao().getRaces(bean.getEvent(), Integer.parseInt(bean.getProperty().getValue())));
 		}
 		bean.setRace(iwc.isParameterSet(PARAMETER_RACE_PK) ? getDao().getRace(Long.parseLong(iwc.getParameter(PARAMETER_RACE_PK))) : null);
 
 		FaceletComponent facelet = (FaceletComponent) iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
 		switch (parseAction(iwc)) {
 			case ACTION_VIEW:
 				facelet.setFaceletURI(iwb.getFaceletURI("companyParticipantsList/view.xhtml"));
 				showView(iwc, bean);
 				break;
 
 			case ACTION_EDIT:
 				facelet.setFaceletURI(iwb.getFaceletURI("companyParticipantsList/edit.xhtml"));
 				showEdit(iwc, bean);
 				break;
 				
 			case ACTION_DELETE:
 				facelet.setFaceletURI(iwb.getFaceletURI("companyParticipantsList/view.xhtml"));
 				handleDelete(iwc, bean);
 				break;
 		}
 
 		add(facelet);
 	}
 	
 	protected RegistrationStatus getStatus() {
 		return RegistrationStatus.OK;
 	}
 	
 	private int parseAction(IWContext iwc) {
 		int action = iwc.isParameterSet(PARAMETER_ACTION) ? Integer.parseInt(iwc.getParameter(PARAMETER_ACTION)) : ACTION_VIEW;
 		return action;
 	}
 
 	private String getBundleIdentifier() {
 		return PheidippidesConstants.IW_BUNDLE_IDENTIFIER;
 	}
 	
 	private void showView(IWContext iwc, PheidippidesCompanyBean bean) {
 		if (bean.getRace() != null) {
 			bean.setRegistrations(getDao().getRegistrations(bean.getCompany(), bean.getRace(), getStatus()));
 			bean.setParticipantsMap(getService().getParticantMap(bean.getRegistrations()));
 		}
		else if (iwc.isParameterSet(PARAMETER_ACTION)) {
			bean.setRegistrations(getDao().getRegistrations(bean.getCompany(), bean.getEvent(), new Integer(bean.getProperty().getValue()), getStatus()));
			bean.setParticipantsMap(getService().getParticantMap(bean.getRegistrations()));
		}
 	}
 	
 	private void showEdit(IWContext iwc, PheidippidesCompanyBean bean) {
 		Registration registration = getDao().getRegistration(Long.parseLong(iwc.getParameter(PARAMETER_REGISTRATION_PK)));
 		Participant participant = getService().getParticipant(registration);
 		
 		bean.setRaces(getService().getOpenRaces(bean.getEvent().getId(), IWTimestamp.RightNow().getYear()));
 		bean.setProperties(getService().getCountries());
 		bean.setProperty(new AdvancedProperty(iwc.getApplicationSettings().getProperty("default.ic_country", "104"), iwc.getApplicationSettings().getProperty("default.ic_country", "104")));
 
 		bean.setRegistration(registration);
 		bean.setParticipant(participant);
 		bean.setRaceShirtSizes(getDao().getRaceShirtSizes(registration.getRace()));
 	}
 	
 	private void handleDelete(IWContext iwc, PheidippidesCompanyBean bean) {
 		Registration registration = getDao().getRegistration(Long.parseLong(iwc.getParameter(PARAMETER_REGISTRATION_PK)));
 		getService().cancelRegistration(registration);
 		
 		showView(iwc, bean);
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
 
 	private BuilderLogicWrapper getBuilderLogicWrapper() {
 		if (builderLogicWrapper == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 		
 		return builderLogicWrapper;
 	}
 
 	private Web2Business getWeb2Business() {
 		if (web2Business == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 		
 		return web2Business;
 	}
 
 	private JQuery getJQuery() {
 		if (jQuery == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 		
 		return jQuery;
 	}
 	
 	public boolean actionPerformed(IWContext iwc) throws IWException {
 		Registration registration = getDao().getRegistration(Long.parseLong(iwc.getParameter(PARAMETER_REGISTRATION_PK)));
 		Long racePK = Long.parseLong(iwc.getParameter(PARAMETER_RACE_PK));
 		Long shirtSizePK = Long.parseLong(iwc.getParameter(PARAMETER_SHIRT_SIZE_PK));
 		String nationalityPK = iwc.getParameter(PARAMETER_NATIONALITY);
 		
 		getDao().updateRegistration(registration.getId(), racePK, shirtSizePK, nationalityPK);
 		
 		String fullName = iwc.getParameter(PARAMETER_NAME);
 		@SuppressWarnings("deprecation")
 		Date dateOfBirth = iwc.isParameterSet(PARAMETER_DATE_OF_BIRTH) ? new IWTimestamp(IWDatePickerHandler.getParsedDate(iwc.getParameter(PARAMETER_DATE_OF_BIRTH))).getSQLDate() : null;
 		String address = iwc.getParameter(PARAMETER_ADDRESS);
 		String postalCode = iwc.getParameter(PARAMETER_POSTAL_CODE);
 		String city = iwc.getParameter(PARAMETER_CITY);
 		Integer countryPK = iwc.isParameterSet(PARAMETER_COUNTRY_PK) ? Integer.parseInt(iwc.getParameter(PARAMETER_COUNTRY_PK)) : null;
 		String gender = iwc.getParameter(PARAMETER_GENDER);
 		String email = iwc.getParameter(PARAMETER_EMAIL);
 		String phone = iwc.getParameter(PARAMETER_PHONE);
 		String mobile = iwc.getParameter(PARAMETER_MOBILE);
 		
 		getService().updateUser(registration.getUserUUID(), fullName, dateOfBirth, address, postalCode, city, countryPK, gender, email, phone, mobile, null);
 		
 		return true;
 	}
 }
