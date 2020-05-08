 package org.iucn.sis.client.panels.assessments;
 
 import java.util.List;
 
 import org.iucn.sis.client.api.caches.AssessmentCache;
 import org.iucn.sis.client.api.caches.FetchMode;
 import org.iucn.sis.client.api.caches.RecentlyAccessedCache;
 import org.iucn.sis.client.api.caches.TaxonomyCache;
 import org.iucn.sis.client.api.container.StateManager;
 import org.iucn.sis.client.panels.utils.RefreshPortlet;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.AssessmentType;
 import org.iucn.sis.shared.api.models.RecentlyAccessed;
 import org.iucn.sis.shared.api.models.Taxon;
 
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HTMLTable.Cell;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.util.extjs.client.WindowUtils;
 import com.solertium.util.gwt.ui.StyledHTML;
 
 public class RecentAssessmentsPanel extends RefreshPortlet {
 
 	private List<RecentlyAccessedCache.RecentAssessment> recentAssessments;
 	private boolean loaded;
 
 //	private ArrayList assessments = null;
 
 	public RecentAssessmentsPanel() {
 		super("x-panel");
 		setHeading("Recent Assessments");
 		setLayout(new FitLayout());
 		setLayoutOnChange(true);
 		
 		loaded = false;
 		refresh();
 	}
 
 	private void fetchAndDrawPanel() {
 		removeAll();
 		recentAssessments = RecentlyAccessedCache.impl.list(RecentlyAccessed.ASSESSMENT);
 		
 		if (recentAssessments == null || recentAssessments.isEmpty()) {
 			add(new HTML("You have no recent assessments."));
 		} else {
 			int row = 0;
 			
 			final Grid grid = new Grid(recentAssessments.size() + 1, 4);
 			grid.getColumnFormatter().addStyleName(1, "right");
 			grid.getColumnFormatter().addStyleName(2, "right");
 			grid.setWidget(row, 0, new HTML("Species"));
 			grid.setWidget(row, 1, new HTML("Type"));
 			grid.setWidget(row, 2, new HTML("Region"));
 			grid.getRowFormatter().addStyleName(row, "hasBoldHTML");
 			grid.getRowFormatter().addStyleName(row, "hasUnderlinedHTML");
 
 			row++;
 			
 			for (final RecentlyAccessedCache.RecentAssessment curInfo : recentAssessments) {
 				grid.setText(row, 0, curInfo.name);
 				
 				final String text;
 				if (AssessmentType.DRAFT_ASSESSMENT_TYPE.equals(curInfo.type))
 					text = "Draft";
 				else if (AssessmentType.PUBLISHED_ASSESSMENT_TYPE.equals(curInfo.type))
 					text = "Published";
 				else
 					text = "Mine";
 				grid.setText(row, 1, text);
 
 				grid.setText(row, 2, curInfo.region);
 				
 				final HTML delete = new StyledHTML("[X]", "SIS_HyperlinkLookAlike");
 				delete.addClickHandler(new ClickHandler() {
 					public void onClick(ClickEvent event) {
 						RecentlyAccessedCache.impl.delete(curInfo, new GenericCallback<Object>() {
 							public void onSuccess(Object result) {
 								fetchAndDrawPanel();
 							}
 							public void onFailure(Throwable caught) {
 								Info.display("Failure", "Failed to remove this item.");
 							}
 						});
 					}
 				});
 				
 				grid.setWidget(row, 3, delete);
 				grid.getCellFormatter().setAlignment(row, 3, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);
 				
 				grid.getRowFormatter().addStyleName(row, "pointerCursor");
 				
 				row++;
 			}
 
 			grid.addClickHandler(new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					Cell cell = grid.getCellForEvent(event);
 					if (cell != null && cell.getRowIndex() != 0 && cell.getCellIndex() < 3) {
 						RecentlyAccessedCache.RecentAssessment clicked = recentAssessments.get(cell.getRowIndex() - 1);
 						setAsCurrentAssessment(clicked.id, clicked.type);
 					}
 				}
 			});
 			grid.setWidth("100%");
 			
 			add(grid);
 		}
 	}
 
 	@Override
 	public void refresh() {
 		if (loaded) {
 			fetchAndDrawPanel();
 		} else {
 			update();
 		}
 	}
 
 	public void setAsCurrentAssessment(final Integer id, final String status) {
 		WindowUtils.showLoadingAlert("Loading...");
 		AssessmentCache.impl.fetchAssessment(id, FetchMode.FULL, new GenericCallback<Assessment>() {
 			public void onFailure(Throwable caught) {
 				WindowUtils.hideLoadingAlert();
 			}
 			public void onSuccess(final Assessment assessment) {
 				TaxonomyCache.impl.fetchTaxon(assessment.getSpeciesID(), new GenericCallback<Taxon>() {
 					@Override
 					public void onFailure(Throwable caught) {
 						WindowUtils.hideLoadingAlert();
 						WindowUtils.errorAlert("Could not load assessment, please try again later.");
 					}
 					public void onSuccess(Taxon result) {
 						WindowUtils.hideLoadingAlert();
						StateManager.impl.setState(null, result, assessment);
 					}
 				});
 				
 			}
 		});
 	}
 
 	public void update() {
 		loaded = true;
 
 		RecentlyAccessedCache.impl.load(RecentlyAccessed.ASSESSMENT, new GenericCallback<Object>() {
 			public void onFailure(Throwable caught) {
 				removeAll();
 				add(new HTML("Unable to load recent assessments, please check your internet connection."));
 			}
 
 			public void onSuccess(Object arg0) {
 				loaded = true;
 				
 				fetchAndDrawPanel();
 			}
 		});
 	}
 
 }
