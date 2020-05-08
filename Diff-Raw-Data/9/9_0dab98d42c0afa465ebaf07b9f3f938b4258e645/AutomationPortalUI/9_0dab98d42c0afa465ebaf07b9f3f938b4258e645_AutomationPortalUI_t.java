 package com.redhat.automationportalui.client;
 
 
 import com.google.gwt.activity.shared.ActivityManager;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.place.shared.PlaceHistoryHandler;
 import com.redhat.automationportalui.client.pav.BugzillaReportGeneratorPlace;
 import com.redhat.automationportalui.client.resources.APUI_Errors;
 import com.redhat.automationportalui.client.resources.CommonUIStrings;
 import com.redhat.automationportalui.client.template.AutomationPortalUITemplate;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class AutomationPortalUI implements EntryPoint
 {
 	/**
 	 * Sets up the GWT Activities and Places, which provide bookmarkable URLs.
 	 * This code is mostly from
 	 * https://developers.google.com/web-toolkit/doc/latest
 	 * /DevGuideMvpActivitiesAndPlaces
 	 */
 	@Override
 	public void onModuleLoad()
 	{	
 		/* Get the translates log messages */
 		final APUI_Errors apuiErrors = (APUI_Errors) GWT.create(APUI_Errors.class);
 		
 		/* Get the translated CommonUI Strings */
 		final CommonUIStrings commonUiStrings = (CommonUIStrings) GWT.create(CommonUIStrings.class);
 		
 		/* Build the template */
 		final AutomationPortalUITemplate template = new AutomationPortalUITemplate(commonUiStrings);
 
 		/* Get the client factory */
 		final AutomationPortalUIClientFactory topikaClientFactory = GWT.create(AutomationPortalUIClientFactory.class);
 		topikaClientFactory.initialise(template, commonUiStrings, apuiErrors);
 
 		final EventBus eventBus = topikaClientFactory.getEventBus();
 		final PlaceController placeController = topikaClientFactory.getPlaceController();
 
 		/* Start ActivityManager for the main widget with our ActivityMapper */
 		final AutomationPortalUIAppActivityMapper activityMapper = new AutomationPortalUIAppActivityMapper(topikaClientFactory);
 		final ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
 		activityManager.setDisplay(template.getContentPanel());
 		
 		/* Start PlaceHistoryHandler with our PlaceHistoryMapper */
 		final AutomationPortalUIAppPlaceHistoryMapper historyMapper = GWT.create(AutomationPortalUIAppPlaceHistoryMapper.class);
 		final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);
 		historyHandler.register(placeController, eventBus, new BugzillaReportGeneratorPlace());
 
 		/* Goes to the place represented on URL else default place */
 		historyHandler.handleCurrentHistory();
 		
 		
 	}
 }
