 package is.idega.idegaweb.pheidippides.presentation;
 
 import is.idega.idegaweb.pheidippides.PheidippidesConstants;
 import is.idega.idegaweb.pheidippides.bean.GiftCardBean;
 import is.idega.idegaweb.pheidippides.business.GiftCardSession;
 import is.idega.idegaweb.pheidippides.business.PheidippidesService;
 import is.idega.idegaweb.pheidippides.dao.PheidippidesDao;
 import is.idega.idegaweb.pheidippides.data.Participant;
 
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.context.FacesContext;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.idega.block.web2.business.JQuery;
 import com.idega.builder.bean.AdvancedProperty;
 import com.idega.core.localisation.business.LocaleSwitcher;
 import com.idega.facelets.ui.FaceletComponent;
 import com.idega.idegaweb.IWBundle;
 import com.idega.idegaweb.IWResourceBundle;
 import com.idega.presentation.IWBaseComponent;
 import com.idega.presentation.IWContext;
import com.idega.util.CoreConstants;
 import com.idega.util.PresentationUtil;
 import com.idega.util.expression.ELUtil;
 
 public class GiftCardCreator extends IWBaseComponent {
 
 	private static final String PARAMETER_ACTION = "prm_action";
 	private static final String PARAMETER_PERSONAL_ID = "prm_personal_id";
 	private static final String PARAMETER_EMAIL = "prm_email";
 	private static final String PARAMETER_AMOUNT = "prm_amount";
 	private static final String PARAMETER_COUNT = "prm_count";
 
 	private static final int ACTION_PERSON_SELECT = 1;
 	private static final int ACTION_GIFT_CARDS = 2;
 	private static final int ACTION_OVERVIEW = 3;
 	private static final int ACTION_SAVE = 4;
 
 	@Autowired
 	private PheidippidesService service;
 
 	@Autowired
 	private GiftCardSession session;
 
 	@Autowired
 	private PheidippidesDao dao;
 
 	@Autowired
 	private JQuery jQuery;
 	
 	private IWBundle iwb;
 	private IWResourceBundle iwrb;
 	
 	@Override
 	protected void initializeComponent(FacesContext context) {
 		IWContext iwc = IWContext.getIWContext(context);
 		iwb = getBundle(context, getBundleIdentifier());
 		iwrb = iwb.getResourceBundle(iwc.getCurrentLocale());
 		
 		getSession().setCurrency(null);
 		
 		if (getSession().getLocale() == null) {
 			getSession().setLocale(iwc.getCurrentLocale());
 		}
 		if (iwc.isParameterSet(LocaleSwitcher.languageParameterString) && !iwc.getCurrentLocale().equals(getSession().getLocale())) {
 			getSession().empty();
 			getSession().setLocale(iwc.getCurrentLocale());
 		}
 
 		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, getJQuery().getBundleURIToJQueryLib());
 		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, getJQuery().getBundleURISToValidation(iwc.getCurrentLocale().getLanguage()));
 
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, CoreConstants.DWR_ENGINE_SCRIPT);
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, CoreConstants.DWR_UTIL_SCRIPT);
		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, "/dwr/interface/PheidippidesService.js");
		
 		PresentationUtil.addJavaScriptSourceLineToHeader(iwc, iwb.getVirtualPathWithFileNameString("javascript/giftCardCreator.js"));
 		PresentationUtil.addStyleSheetToHeader(iwc, iwb.getVirtualPathWithFileNameString("style/pheidippides.css"));
 
 		GiftCardBean bean = getBeanInstance("giftCardBean");
 		bean.setLocale(iwc.getCurrentLocale());
 
 		switch (parseAction(iwc)) {
 			case ACTION_PERSON_SELECT:
 				showPersonSelect(iwc, bean);
 				break;
 				
 			case ACTION_GIFT_CARDS:
 				if (iwc.isParameterSet(PARAMETER_PERSONAL_ID)) {
 					Participant participant = getService().getParticipant(iwc.getParameter(PARAMETER_PERSONAL_ID));
 					getSession().setCreatorUUID(participant.getUuid());
 					getSession().setEmail(iwc.getParameter(PARAMETER_EMAIL));
 				}
 				showGiftCards(iwc, bean);
 				break;
 				
 			case ACTION_OVERVIEW:
 				if (iwc.isParameterSet(PARAMETER_AMOUNT)) {
 					int amount = Integer.parseInt(iwc.getParameter(PARAMETER_AMOUNT));
 					String amountText = iwrb.getLocalizedString("gift_card_amount." + String.valueOf(amount), String.valueOf(amount));
 					int count = Integer.parseInt(iwc.getParameter(PARAMETER_COUNT));
 					
 					getSession().addGiftCard(amount, amountText, count);
 				}
 				showOverview(iwc, bean);
 				break;
 				
 			case ACTION_SAVE:
 				save(iwc, bean);
 				break;
 		}
 	}
 	
 	private void showPersonSelect(IWContext iwc, GiftCardBean bean) {
 		FaceletComponent facelet = (FaceletComponent) iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
 		facelet.setFaceletURI(iwb.getFaceletURI("giftCard/personSelect.xhtml"));
 		add(facelet);
 	}
 	
 	private void showGiftCards(IWContext iwc, GiftCardBean bean) {
 		NumberFormat formatter = NumberFormat.getCurrencyInstance(iwc.getCurrentLocale());
 		formatter.setParseIntegerOnly(true);
 		
 		List<AdvancedProperty> amounts = new ArrayList<AdvancedProperty>();
 		amounts.add(new AdvancedProperty("1000", formatter.format(1000)));
 		amounts.add(new AdvancedProperty("2000", formatter.format(2000)));
 		amounts.add(new AdvancedProperty("3000", formatter.format(3000)));
 		amounts.add(new AdvancedProperty("4000", formatter.format(4000)));
 		amounts.add(new AdvancedProperty("5000", formatter.format(5000)));
 		amounts.add(new AdvancedProperty("6000", formatter.format(6000)));
 		amounts.add(new AdvancedProperty("7000", formatter.format(7000)));
 		amounts.add(new AdvancedProperty("8000", formatter.format(8000)));
 		amounts.add(new AdvancedProperty("9000", formatter.format(9000)));
 		amounts.add(new AdvancedProperty("10000", formatter.format(10000)));
 		amounts.add(new AdvancedProperty("15000", formatter.format(15000)));
 		amounts.add(new AdvancedProperty("20000", formatter.format(20000)));
 		amounts.add(new AdvancedProperty("25000", formatter.format(25000)));
 		bean.setAmounts(amounts);
 		
 		List<AdvancedProperty> counts = new ArrayList<AdvancedProperty>();
 		for (int i = 1; i <= 20; i++) {
 			counts.add(new AdvancedProperty(String.valueOf(i)));
 		}
 		bean.setCounts(counts);
 		
 		FaceletComponent facelet = (FaceletComponent) iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
		facelet.setFaceletURI(iwb.getFaceletURI("giftCard/giftCards.xhtml"));
 		add(facelet);
 	}
 
 	private void showOverview(IWContext iwc, GiftCardBean bean) {
 		FaceletComponent facelet = (FaceletComponent) iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
 		facelet.setFaceletURI(iwb.getFaceletURI("giftCard/overview.xhtml"));
 		add(facelet);
 	}
 	
 	private void save(IWContext iwc, GiftCardBean bean) {
 		FaceletComponent facelet = (FaceletComponent) iwc.getApplication().createComponent(FaceletComponent.COMPONENT_TYPE);
 		facelet.setFaceletURI(iwb.getFaceletURI("giftCard/save.xhtml"));
 		add(facelet);
 	}
 
 	private int parseAction(IWContext iwc) {
 		int action = iwc.isParameterSet(PARAMETER_ACTION) ? Integer.parseInt(iwc.getParameter(PARAMETER_ACTION)) : ACTION_PERSON_SELECT;
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
 
 	private GiftCardSession getSession() {
 		if (session == null) {
 			ELUtil.getInstance().autowire(this);
 		}
 		
 		return session;
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
