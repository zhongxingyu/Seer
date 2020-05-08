 package eu.europeana.uim.gui.cp.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.prefetch.RunAsyncCode;
 
 import eu.europeana.uim.gui.cp.client.europeanawidgets.CollectionManagement;
 import eu.europeana.uim.gui.cp.client.europeanawidgets.ExpandedResourceManagementWidget;
 import eu.europeana.uim.gui.cp.client.europeanawidgets.FailedRecordsWidget;
 import eu.europeana.uim.gui.cp.client.europeanawidgets.ImportControlledVocabularyWidget;
 import eu.europeana.uim.gui.cp.client.europeanawidgets.ImportResourcesWidget;
 import eu.europeana.uim.gui.cp.client.europeanawidgets.LinkCachingWidget;
 import eu.europeana.uim.gui.cp.client.europeanawidgets.LinkReportingWidget;
 import eu.europeana.uim.gui.cp.client.europeanawidgets.LinkValidationWidget;
 import eu.europeana.uim.gui.cp.client.management.IngestionTriggerWidget;
 import eu.europeana.uim.gui.cp.client.monitoring.IngestionDetailWidget;
 import eu.europeana.uim.gui.cp.client.monitoring.IngestionHistoryWidget;
 import eu.europeana.uim.gui.cp.client.services.CollectionManagementProxy;
 import eu.europeana.uim.gui.cp.client.services.CollectionManagementProxyAsync;
 import eu.europeana.uim.gui.cp.client.services.ExecutionService;
 import eu.europeana.uim.gui.cp.client.services.ExecutionServiceAsync;
 import eu.europeana.uim.gui.cp.client.services.FailedRecordService;
 import eu.europeana.uim.gui.cp.client.services.FailedRecordServiceAsync;
 import eu.europeana.uim.gui.cp.client.services.ImportVocabularyProxy;
 import eu.europeana.uim.gui.cp.client.services.ImportVocabularyProxyAsync;
 import eu.europeana.uim.gui.cp.client.services.IntegrationSeviceProxy;
 import eu.europeana.uim.gui.cp.client.services.IntegrationSeviceProxyAsync;
 import eu.europeana.uim.gui.cp.client.services.ReportingService;
 import eu.europeana.uim.gui.cp.client.services.ReportingServiceAsync;
 import eu.europeana.uim.gui.cp.client.services.RepositoryService;
 import eu.europeana.uim.gui.cp.client.services.RepositoryServiceAsync;
 import eu.europeana.uim.gui.cp.client.services.ResourceService;
 import eu.europeana.uim.gui.cp.client.services.ResourceServiceAsync;
 import eu.europeana.uim.gui.cp.client.services.RetrievalService;
 import eu.europeana.uim.gui.cp.client.services.RetrievalServiceAsync;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  * 
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @author Georgios Markakis (gwarkx@hotmail.com)
  * @author Yorgos Mamakis (yorgos.mamakis@ kb.nl)
  * @since Apr 26, 2011
  */
 public class EuropeanaIngestionControlPanel extends
 		AbstractIngestionControlPanel implements EntryPoint {
 	@Override
 	public void onModuleLoad() {
 		initialize();
 	}
 
 	@Override
 	protected IngestionCustomization getDynamics() {
 		return new EuropeanaCustomization();
 	}
 
 	@Override
 	protected void addMenuEntries(SidebarMenu treeModel) {
 
 		// Initialize services here
 		final RepositoryServiceAsync repositoryService = (RepositoryServiceAsync) GWT
 				.create(RepositoryService.class);
 		final ResourceServiceAsync resourceService = (ResourceServiceAsync) GWT
 				.create(ResourceService.class);
 		final ExecutionServiceAsync executionService = (ExecutionServiceAsync) GWT
 				.create(ExecutionService.class);
 		final IntegrationSeviceProxyAsync integrationService = (IntegrationSeviceProxyAsync) GWT
 				.create(IntegrationSeviceProxy.class);
 		final ImportVocabularyProxyAsync importVocabulary = (ImportVocabularyProxyAsync) GWT
 				.create(ImportVocabularyProxy.class);
 		final CollectionManagementProxyAsync collectionManagement = (CollectionManagementProxyAsync) GWT
 				.create(CollectionManagementProxy.class);
 		final RetrievalServiceAsync retrievalService = (RetrievalServiceAsync) GWT
 				.create(RetrievalService.class);
 
 		final ReportingServiceAsync reportService = (ReportingServiceAsync) GWT
 				.create(ReportingService.class);
 		final FailedRecordServiceAsync failedRecordService = (FailedRecordServiceAsync) GWT
 				.create(FailedRecordService.class);
 		// Initialize Panel Components here
 		treeModel.addMenuEntry("Monitoring", new IngestionDetailWidget(
 				executionService), RunAsyncCode
 				.runAsyncCode(IngestionDetailWidget.class));
		treeModel.addMenuEntry("Monitoring", new IngestionHistoryWidget(
				executionService), RunAsyncCode
				.runAsyncCode(IngestionHistoryWidget.class));

 		treeModel.addMenuEntry("Managing", new IngestionTriggerWidget(
 				repositoryService, resourceService, executionService),
 				RunAsyncCode.runAsyncCode(IngestionTriggerWidget.class));
 		treeModel.addMenuEntry("Managing",
 				new ExpandedResourceManagementWidget(repositoryService,
 						resourceService, integrationService), RunAsyncCode
 						.runAsyncCode(ExpandedResourceManagementWidget.class));
 		treeModel.addMenuEntry("Validation", new LinkValidationWidget(
 				repositoryService, retrievalService), RunAsyncCode
 				.runAsyncCode(LinkValidationWidget.class));
 		treeModel.addMenuEntry("Validation", new FailedRecordsWidget(
 				"Failed Records Report", "This page allows you to preview the duplicate records that have not been ingested",
 				repositoryService, failedRecordService), RunAsyncCode
 				.runAsyncCode(FailedRecordsWidget.class));
 		
 		treeModel
 				.addMenuEntry("Link Checker/ Thumbler",
 						new LinkReportingWidget(reportService,
 								"Link Validation",
 								new String[] { "LinkCheckWorkflow" },
 								"linkcheck_overview.rptdesign",
 								new String[] { "pdf" }), RunAsyncCode
 								.runAsyncCode(LinkReportingWidget.class));
 
 		treeModel.addMenuEntry("Link Checker/ Thumbler", new LinkCachingWidget(
 				reportService, "Link Caching",
 				new String[] { "ImageCacheWorkflow" },
 				"thumbler_overview.rptdesign", new String[] { "pdf" }),
 				RunAsyncCode.runAsyncCode(LinkCachingWidget.class));
 
 		treeModel.addMenuEntry("Importing", new ImportResourcesWidget(
 				repositoryService, resourceService, integrationService),
 				RunAsyncCode.runAsyncCode(ImportResourcesWidget.class));
 
 		treeModel.addMenuEntry("Importing",
 				new ImportControlledVocabularyWidget(repositoryService,
 						resourceService, importVocabulary), RunAsyncCode
 						.runAsyncCode(ImportControlledVocabularyWidget.class));
 
 		treeModel.addMenuEntry("Importing", new CollectionManagement(
 				repositoryService, resourceService, collectionManagement),
 				RunAsyncCode.runAsyncCode(CollectionManagement.class));
 	}
 }
