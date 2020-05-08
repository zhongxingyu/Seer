 package org.iucn.sis.client.panels;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.FieldWidgetCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.caches.WorkingSetCache;
 import org.iucn.sis.client.api.container.StateManager;
 import org.iucn.sis.client.panels.dem.DEMPanel;
 import org.iucn.sis.client.panels.references.ReferenceViewTabPanel;
 import org.iucn.sis.client.tabs.FeaturedItemContainer;
 import org.iucn.sis.client.tabs.HomePageTab;
 import org.iucn.sis.client.tabs.TaxonHomePageTab;
 import org.iucn.sis.client.tabs.WorkingSetPage;
 import org.iucn.sis.shared.api.assessments.AssessmentFetchRequest;
 import org.iucn.sis.shared.api.citations.Referenceable;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.WorkingSet;
 
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.layout.FillLayout;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.DrawsLazily;
 
 public class BodyContainer extends LayoutContainer {
 
 	private LayoutContainer homePage;
 	private FeaturedItemContainer<WorkingSet> workingSetPage;
 	private FeaturedItemContainer<Taxon> taxonHomePage;
 	private FeaturedItemContainer<Integer> assessmentPage;
 	
 	private LayoutContainer current;
 	
 	private ReferenceViewTabPanel refViewPanel;
 
 	public BodyContainer() {
 		super(new FillLayout());
 		setLayoutOnChange(true);
 		addStyleName("gwt-background");
 		addStyleName("sis_bodyContainer");
 
 		homePage = new HomePageTab();
 		workingSetPage = new WorkingSetPage();
 		taxonHomePage = new TaxonHomePageTab();
 		assessmentPage = new DEMPanel();
 		
 		refViewPanel = new ReferenceViewTabPanel();
 	}
 	
 	public void openWorkingSet(final String url, final boolean updateNavigation) {
 		workingSetPage.setItems(new ArrayList<WorkingSet>(WorkingSetCache.impl.getWorkingSets().values()));
 		workingSetPage.setSelectedItem(StateManager.impl.getWorkingSet());
 		workingSetPage.draw(new DrawsLazily.DoneDrawingCallback() {
 			public void isDrawn() {
 				removeAll();
 				add(workingSetPage);
 				
 				current = workingSetPage;
 				
 				if (updateNavigation)
 					updateNavigation();
 			}
 		});
 	}
 	
 	public void openTaxon(final String url, final boolean updateNavigation) {
 		final GenericCallback<List<Taxon>> callback = new GenericCallback<List<Taxon>>() {
 			public void onFailure(Throwable caught) {
 				// TODO Auto-generated method stub
 				
 			}
 			public void onSuccess(List<Taxon> result) {
 				taxonHomePage.setItems(result);
 				taxonHomePage.setSelectedItem(StateManager.impl.getTaxon());
 				taxonHomePage.draw(new DrawsLazily.DoneDrawingCallback() {
 					public void isDrawn() {
 						removeAll();
 						add(taxonHomePage);
 						
 						current = taxonHomePage;
 						
 						if (updateNavigation)
 							updateNavigation();
 					}
 				});	
 			}
 		};
 		
 		WorkingSet ws = StateManager.impl.getWorkingSet();
 		if (ws == null)
 			callback.onSuccess(new ArrayList<Taxon>(TaxonomyCache.impl.getRecentlyAccessed()));
 		else {
 			WorkingSetCache.impl.fetchTaxaForWorkingSet(ws, callback);
 		}
 		
 	}
 	
 	public void openAssessment(final String url, final boolean updateNavigation) {
 		FieldWidgetCache.impl.resetWidgetContents();
 		
 		if (StateManager.impl.getWorkingSet() == null) {
 			AssessmentFetchRequest request = new AssessmentFetchRequest(null, StateManager.impl.getTaxon().getId());
 			AssessmentCache.impl.fetchAssessments(request, new GenericCallback<String>() {
 				public void onSuccess(String result) {
 					List<Integer> assessments = new ArrayList<Integer>();
 					for (Assessment assessment : AssessmentCache.impl.getDraftAssessmentsForTaxon(StateManager.impl.getTaxon().getId()))
 						assessments.add(assessment.getId());
 					for (Assessment assessment : AssessmentCache.impl.getPublishedAssessmentsForTaxon(StateManager.impl.getTaxon().getId()))
 						assessments.add(assessment.getId());
 					
 					assessmentPage.setItems(assessments);
 					assessmentPage.setSelectedItem(StateManager.impl.getAssessment().getId());
 					
 					assessmentPage.draw(new DrawsLazily.DoneDrawingCallback() {
 						public void isDrawn() {
 							removeAll();
 							add(assessmentPage);
 							
 							current = assessmentPage;
 							
 							if (updateNavigation)
 								updateNavigation();
 						}
 					});
 				}			
 				public void onFailure(Throwable caught) {
 					WindowUtils.errorAlert("Could not load assessment.");
 				}
 			});
 		}
 		else {
 			WorkingSetCache.impl.listAssessmentsForWorkingSet(
 					StateManager.impl.getWorkingSet(), null, 
 					new GenericCallback<List<Integer>>() {
 				public void onSuccess(List<Integer> result) {
 					assessmentPage.setItems(result);
 					assessmentPage.setSelectedItem(StateManager.impl.getAssessment().getId());
 					
 					assessmentPage.draw(new DrawsLazily.DoneDrawingCallback() {
 						public void isDrawn() {
 							removeAll();
 							add(assessmentPage);
 							
 							current = assessmentPage;
 							
 							if (updateNavigation)
 								updateNavigation();
 						}
 					});
 				}
 				public void onFailure(Throwable caught) {
 					WindowUtils.errorAlert("Failed to loading assessments for this working set.");
 				}
 			});
 		}
 	}
 	
 	public void openHomePage(boolean updateNavigation) {
 		removeAll();
 		
 		add(homePage);
 		
 		if (updateNavigation)
 			updateNavigation();
 	}
 	
 	private void updateNavigation() {
 		ClientUIContainer.headerContainer.update();
 	}
 	
 	public void refreshTaxonPage() {
 		if (taxonHomePage == current)
 			refreshBody();
 	}
 	
 	public void refreshBody() {
 		if (workingSetPage == current)
 			workingSetPage.draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
 		else if (taxonHomePage == current)
 			taxonHomePage.draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
 		else if (assessmentPage == current)
 			assessmentPage.draw(new DrawsLazily.DoneDrawingWithNothingToDoCallback());
 	}
 	
 	public boolean isAssessmentEditor() {
 		return assessmentPage == current;
 	}
 	
 	public void openReferenceManager() {
 		openReferenceManager(null);
 	}
 	
 	public void openReferenceManager(Referenceable referenceable) {
 		openReferenceManager(referenceable, "Manage References");
 	}
 	
 	public void openReferenceManager(Referenceable referenceable, String windowTitle) {
 		openReferenceManager(referenceable, windowTitle, null, null);
 	}
 	
 	public void openReferenceManager(Referenceable referenceable, String windowTitle, GenericCallback<Object> addCallback, GenericCallback<Object> removeCallback) {
 		refViewPanel.setReferences(referenceable, addCallback, removeCallback);
 		
 		final Window s = WindowUtils.getWindow(true, true, windowTitle);
 		s.setIconStyle("icon-book");
 		s.setLayout(new FitLayout());
 		s.add(refViewPanel);
 		s.setSize(850, 550);
 		s.show();
 	}
 
 }
