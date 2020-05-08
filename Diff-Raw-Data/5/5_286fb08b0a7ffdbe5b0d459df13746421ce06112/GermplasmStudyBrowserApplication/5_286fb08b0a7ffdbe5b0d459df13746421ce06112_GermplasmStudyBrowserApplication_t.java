 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 
 package org.generationcp.browser.application;
 
 import org.dellroad.stuff.vaadin.SpringContextApplication;
 import org.generationcp.browser.germplasm.GermplasmBrowserMain;
 import org.generationcp.browser.germplasm.GidByPhenotypicQueries;
 import org.generationcp.browser.germplasm.SearchGermplasmByPhenotypicTab;
 import org.generationcp.browser.germplasm.TraitDataIndexContainer;
 import org.generationcp.browser.germplasm.listeners.GermplasmSelectedTabChangeListener;
 import org.generationcp.browser.study.StudyBrowserMain;
 import org.generationcp.browser.util.DatasourceConfig;
 import org.generationcp.browser.i18n.ui.I18NVerticalLayout;
 import org.generationcp.middleware.exceptions.ConfigException;
 import org.generationcp.middleware.exceptions.QueryException;
 import org.generationcp.middleware.manager.ManagerFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.web.context.ConfigurableWebApplicationContext;
 
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.Window;
 
 import com.github.peholmst.i18n4vaadin.I18N;
 import com.github.peholmst.i18n4vaadin.ResourceBundleI18N;
 import com.github.peholmst.i18n4vaadin.support.I18NWindow;
 import java.util.Locale;
 
 
 @Configurable
 public class GermplasmStudyBrowserApplication extends SpringContextApplication {
 
     private final static Logger LOG = LoggerFactory.getLogger(GermplasmStudyBrowserApplication.class);
 
     private static final long serialVersionUID = -2630998412856758023L;
 
 	private I18NWindow window;
 	private ManagerFactory factory;
 
 	private I18NVerticalLayout rootLayoutForGermplasmBrowser;
 	private I18NVerticalLayout rootLayoutForStudyBrowser;
 	private I18NVerticalLayout rootLayoutForGermplasmByPhenoTab;
 	
 	private I18N i18n;
  
     private DatasourceConfig datasourceConfig;
 
     @Override
     public void initSpringApplication(ConfigurableWebApplicationContext arg0) {
 		// set internationalization parameters
 		i18n = new ResourceBundleI18N("I18NMessages", Locale.ENGLISH, Locale.GERMAN); // add more Locale as needed
 		i18n.setCurrentLocale(Locale.ENGLISH);
 //		i18n.setCurrentLocale(Locale.GERMAN);
 
 		// create blank root layouts for the other tabs, the content will be
 		// added as the tabs are selected
 		// or as the buttons on the WelcomeTab are clicked
 		rootLayoutForGermplasmBrowser = new I18NVerticalLayout(i18n);
 		rootLayoutForStudyBrowser = new I18NVerticalLayout(i18n);
 		rootLayoutForGermplasmByPhenoTab = new I18NVerticalLayout(i18n);
 
 		// initialize Middleware ManagerFactory
 		try {
 			initDataSource();
 		} catch (Exception e1) {
 			System.out.println(e1);
 			e1.printStackTrace();
 			return;
 		}
 
 		window = new I18NWindow(i18n.getMessage("retrieveGerplasmByPheno.title"), i18n); // "Retrieve Germplasms By Phenotypic Data"
 		setMainWindow(window);
 		window.setSizeUndefined();
 
 		TabSheet tabSheet = new TabSheet();
 		// add listener triggered by selecting tabs, this listener will create
 		// the content for the tabs dynamically as needed
 
 		tabSheet.addListener(new GermplasmSelectedTabChangeListener(this));
 
 		// this will be passed to WelcomeTab so that it will have a reference to
 		// the root layout of the other tabs
 		I18NVerticalLayout layouts[] = new I18NVerticalLayout[3];
 		layouts[0] = rootLayoutForGermplasmBrowser;
 		layouts[1] = rootLayoutForStudyBrowser;
 		layouts[2] = rootLayoutForGermplasmByPhenoTab;
 
 		WelcomeTab welcomeTab = new WelcomeTab(tabSheet, this.factory, layouts, i18n);
 
 		tabSheet.addTab(welcomeTab, i18n.getMessage("welcome.title")); //"Welcome"
 		tabSheet.addTab(rootLayoutForGermplasmBrowser, i18n.getMessage("germplasmBrowser.title")); // "Germplasm Browser"
 		tabSheet.addTab(rootLayoutForStudyBrowser, i18n.getMessage("studyBrowser.title")); // "Study Browser"
 		tabSheet.addTab(rootLayoutForGermplasmByPhenoTab, i18n.getMessage("germplasmByPheno.title")); // "Search for Germplasms By Phenotypic Data"
 
         window.addComponent(tabSheet);
     }
     
     @Autowired
     public void setDataSourceConfig(DatasourceConfig datasourceConfig) {
         this.datasourceConfig = datasourceConfig;
     }	
 
     @Override
     public Window getWindow(String name) {
         // dynamically create other application-level windows which is
         // associated with specific URLs
         // these windows are the jumping on points to parts of the application
         if (super.getWindow(name) == null) {
             if (name.equals("germplasm-by-pheno")) {
                 GidByPhenotypicQueries gidByPhenoQueries = null;
                 try {
                     gidByPhenoQueries = new GidByPhenotypicQueries(this.factory, this.factory.getStudyDataManager());
                 } catch (ConfigException e) {
                     // Log into log file
                     LOG.warn(e.toString() + "\n" + e.getStackTrace());
                     e.printStackTrace();
                 }
                 TraitDataIndexContainer traitDataCon = new TraitDataIndexContainer(this.factory, this.factory.getTraitDataManager());
 
 				I18NWindow germplasmByPhenoWindow = new I18NWindow(i18n.getMessage("germplasmByPheno.title"), i18n);   // "Search for Germplasms By Phenotypic Data"
 				germplasmByPhenoWindow.setName("germplasm-by-pheno");
 				germplasmByPhenoWindow.setSizeUndefined();
 				try {
 					germplasmByPhenoWindow.addComponent(new SearchGermplasmByPhenotypicTab(gidByPhenoQueries, traitDataCon));
 				} catch (QueryException e) {
 					// Log into log file
 					LOG.error(e.toString() + "\n" + e.getStackTrace());
 					e.printStackTrace();
 				}
 				this.addWindow(germplasmByPhenoWindow);
 
                 return germplasmByPhenoWindow;
             }
 
             else if (name.equals("study")) {
 
 				I18NWindow studyBrowserWindow = new I18NWindow(i18n.getMessage("germplasmBrowser.title"), i18n);  // Study Browser
				studyBrowserWindow.setName("study");
 				studyBrowserWindow.setSizeUndefined();
 				studyBrowserWindow.addComponent(new StudyBrowserMain(factory, i18n));
 				this.addWindow(studyBrowserWindow);
 				return studyBrowserWindow;
 			}
 
             else if (name.equals("germplasm")) {
 
 				I18NWindow germplasmBrowserWindow = new I18NWindow(i18n.getMessage("germplasmBrowser.title"), i18n);  // "Germplasm Browser"
				germplasmBrowserWindow.setName("germplasm");
 				germplasmBrowserWindow.setSizeUndefined();
 				germplasmBrowserWindow.addComponent(new GermplasmBrowserMain(factory));
 				this.addWindow(germplasmBrowserWindow);
 				return germplasmBrowserWindow;
 			}
 
         }
 
         return super.getWindow(name);
     }
 
     private void initDataSource() throws Exception {
         this.factory = this.datasourceConfig.getManagerFactory();
     }
 
     public void tabSheetSelectedTabChangeAction(TabSheet source) {
 
         GidByPhenotypicQueries gidByPhenoQueries = null;
         try {
             gidByPhenoQueries = new GidByPhenotypicQueries(factory, factory.getStudyDataManager());
         } catch (ConfigException e) {
             // Log into log file
             LOG.warn(e.toString() + "\n" + e.getStackTrace());
             e.printStackTrace();
         }
         final TraitDataIndexContainer traitDataCon = new TraitDataIndexContainer(factory, factory.getTraitDataManager());
         if (source.getSelectedTab() == rootLayoutForGermplasmByPhenoTab) {
             if (rootLayoutForGermplasmByPhenoTab.getComponentCount() == 0) {
                 try {
                     rootLayoutForGermplasmByPhenoTab.addComponent(new SearchGermplasmByPhenotypicTab(gidByPhenoQueries, traitDataCon));
                 } catch (QueryException e) {
                     // Log into log file
                     LOG.error(e.toString() + "\n" + e.getStackTrace());
                     e.printStackTrace();
                 }
             }
 
         } else if (source.getSelectedTab() == rootLayoutForGermplasmBrowser) {
             if (rootLayoutForGermplasmBrowser.getComponentCount() == 0) {
                 rootLayoutForGermplasmBrowser.addComponent(new GermplasmBrowserMain(factory));
             }
         } else if (source.getSelectedTab() == rootLayoutForStudyBrowser) {
             if (rootLayoutForStudyBrowser.getComponentCount() == 0) {
                 rootLayoutForStudyBrowser.addComponent(new StudyBrowserMain(factory, i18n));
             }
         }
     }
 
 }
