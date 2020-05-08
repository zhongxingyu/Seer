 package org.mindinformatics.gwt.domeo.client.ui.sets;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.mindinformatics.gwt.domeo.client.Domeo;
 import org.mindinformatics.gwt.domeo.client.IDomeo;
 import org.mindinformatics.gwt.domeo.client.ui.east.sets.AnnotationSetsSidePanel;
 import org.mindinformatics.gwt.domeo.model.MAnnotation;
 import org.mindinformatics.gwt.domeo.model.MAnnotationSet;
 import org.mindinformatics.gwt.domeo.plugins.annotation.curation.model.MCurationToken;
 import org.mindinformatics.gwt.framework.component.ui.lenses.ILensComponent;
 import org.mindinformatics.gwt.framework.model.agents.ISoftware;
 import org.mindinformatics.gwt.framework.model.users.IUserGroup;
 import org.mindinformatics.gwt.framework.widget.EditableLabel;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
  */
 public class AnnotationSetLens extends Composite implements ILensRefresh, ILensComponent {
 
 	public static final String DESCRIPTION_CAPTION = "<type description here>";
 	
 	interface Binder extends UiBinder<Widget, AnnotationSetLens> { }
 	private static final Binder binder = GWT.create(Binder.class);
 	
 	private IDomeo _domeo;
 	private MAnnotationSet _set;
 	private AnnotationSetLens _this;
 	private AnnotationSetsSidePanel _parent;
 	
 	@UiField VerticalPanel body;
 	@UiField EditableLabel nameField;
 	@UiField EditableLabel descriptionField;
 	@UiField Label createdOnField;
 	@UiField Label createdByField;
 	@UiField Label savedOnField;
 	@UiField Label versionField;
 	@UiField HorizontalPanel sharedWithField; 
 	@UiField SetsLensStyle style;
 	@UiField HorizontalPanel footerLeftSide;
 	@UiField HorizontalPanel footerRightSide;
 	@UiField SimplePanel items;
 	@UiField VerticalPanel access;
 	@UiField VerticalPanel display;
 	@UiField VerticalPanel filter;
 	
 	CheckBox checkHighlight;
     //CheckBox showAllFilter = new CheckBox("All");
     CheckBox showCuratedFilter = new CheckBox("All Curated");
     CheckBox showNotCuratedFilter = new CheckBox("Not Curated");
     CheckBox showAddedFilter = new CheckBox("Added");
     CheckBox showAcceptedFilter = new CheckBox("Accepted");
     CheckBox showBroadFilter = new CheckBox("Broad");
     CheckBox showRejectedFilter = new CheckBox("Rejected");
 	
 	public interface SetsLensStyle extends CssResource {
         String spacedLabel();
         String activeIcon();
     }
 	
 	public AnnotationSetLens(IDomeo domeo, final AnnotationSetsSidePanel parent) {
 		_domeo = domeo;
 		_parent = parent;
 		
 		initWidget(binder.createAndBindUi(this));
 	}
 	
 	public void initialize(MAnnotationSet set) {
 		_this = this;
 		_set = set;
 		
 		nameField.addValueChangeHandler(new ValueChangeHandler<String>() {
 			@Override
 			public void onValueChange(ValueChangeEvent<String> event) {
 				_set.setLabel(event.getValue());
 				_set.setHasChanged(true);
 				_domeo.refreshAnnotationComponents();
 			}
 		});
 		descriptionField.addValueChangeHandler(new ValueChangeHandler<String>() {
 			@Override
 			public void onValueChange(ValueChangeEvent<String> event) {
 				if(!event.getValue().trim().equals(DESCRIPTION_CAPTION)) 
 					_set.setDescription(event.getValue());
 				_set.setHasChanged(true);
 				_domeo.refreshAnnotationComponents();
 			}
 		});
 		
 	      try {
 	            if(_domeo.isHostedMode() || _set.getImportedFrom()!=null) {
 	                // Check 'First' by default.
 	                //showAllFilter.setValue(true);
 	                showCuratedFilter.setValue(true);
 	                showNotCuratedFilter.setValue(true);
 	                showAddedFilter.setValue(true);
 	                showAcceptedFilter.setValue(true);
 	                showBroadFilter.setValue(true);
 	                showRejectedFilter.setValue(true);
 	                
 	                ClickHandler filtering = new ClickHandler() {
 	                    @Override
 	                    public void onClick(ClickEvent event) {            
 	                           filterShow(event);
 	                    }
 	                }; 
 	                
 	                //showAllFilter.addClickHandler(filtering);
 	                showCuratedFilter.addClickHandler(filtering);
 	                showNotCuratedFilter.addClickHandler(filtering);
 	                showAddedFilter.addClickHandler(filtering);
 	                showRejectedFilter.addClickHandler(filtering);
 	                showAcceptedFilter.addClickHandler(filtering);
 	                showBroadFilter.addClickHandler(filtering);               
 	                
 	                HorizontalPanel hp = new HorizontalPanel();
 	                //hp.add(showAllFilter);
 	                hp.add(showCuratedFilter);
 	                hp.add(showAcceptedFilter);
 	                hp.add(showBroadFilter);
 	                hp.add(showRejectedFilter);
 	                hp.add(showNotCuratedFilter);
 	                hp.add(showAddedFilter);
 	                //display.add(hp);
 	                
 	                HorizontalPanel hp1 = new HorizontalPanel();
 
 	                filter.add(new HTML("<b>Show</b> (uncheck to hide)"));
 	                filter.add(hp);
 	                
 	            }
 	        } catch(Exception e) {
 	            filter.add(new Label("Exception (display)"));
 	        }
 		
 		refreshLens();
 	}
 	
 	public void refresh() {
 		_parent.refresh();
 	}
 	
 	public void refreshLens() {
 		if(_set == null) {
 			// Exception
 		} 
 		
 		nameField.setValue(_set.getLabel());
 		if(_set.getDescription().trim().length()>0) 
 			descriptionField.setValue(_set.getDescription());
 		else
 			descriptionField.setValue(DESCRIPTION_CAPTION);
 		
 		createdOnField.setText(_set.getFormattedCreationDate());
 		createdByField.setText(_set.getCreatedBy().getName());
 		if(_set.getFormattedLastSavedOn()!=null)
 			savedOnField.setText(_set.getFormattedLastSavedOn()+(_set.getHasChanged()?"*":""));
 		else savedOnField.setText("<not saved>");
 		if(_set.getVersionNumber()!=null) 
 			versionField.setText(_set.getVersionNumber());
 		else versionField.setText("<unversioned>");
 		
 		footerLeftSide.clear();
 		footerRightSide.clear();
 		
 
 		HorizontalPanel hp0 = new HorizontalPanel();
 		checkHighlight = new CheckBox();
 		checkHighlight.setValue(_set.isEmphasized());
 		checkHighlight.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 			    // TODO reset 'show filters' 
 			    //showAllFilter.setValue(true);
 			    showCuratedFilter.setValue(true);
 			    showNotCuratedFilter.setValue(true);
 			    showAddedFilter.setValue(true);
 			    showRejectedFilter.setValue(true);
 			    showAcceptedFilter.setValue(true);
 			    showBroadFilter.setValue(true);
 
 				_domeo.getContentPanel().getAnnotationFrameWrapper()
 					.manageSetEmphasis(_set, checkHighlight.getValue());
 				_parent.refresh();
 			}
 		});
 		hp0.add(checkHighlight);
 		Label highlightLabel = new Label("Emphasize");
 		highlightLabel.setStyleName(style.spacedLabel());
 		hp0.add(highlightLabel);
 		footerLeftSide.add(hp0);
 		
 		if(_set.getIsVisible()) {
 			HorizontalPanel hp4= new HorizontalPanel();
 			final Image imgVisibility = new Image(Domeo.resources.visibleLittleIcon());
 			imgVisibility.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					AnnotationVisibilityPopup popup = new AnnotationVisibilityPopup(_domeo, _this, _set);
 					popup.show(imgVisibility.getAbsoluteLeft(), imgVisibility.getAbsoluteTop());
 				}
 			});
 			hp4.add(imgVisibility);
 			Label visibleLabel = new Label("Visible");
 			visibleLabel.setStyleName(style.spacedLabel());
 			hp4.add(visibleLabel);
 			hp4.setStyleName(style.activeIcon());
 			footerLeftSide.add(hp4);
 		} else {
 			HorizontalPanel hp4= new HorizontalPanel();
 			final Image imgVisibility = new Image(Domeo.resources.invisibleLittleIcon());
 			imgVisibility.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					AnnotationVisibilityPopup popup = new AnnotationVisibilityPopup(_domeo, _this, _set);
 					popup.show(imgVisibility.getAbsoluteLeft(), imgVisibility.getAbsoluteTop());
 				}
 			});
 			hp4.add(imgVisibility);
 			Label visibleLabel = new Label("Hidden");
 			visibleLabel.setStyleName(style.spacedLabel());
 			hp4.add(visibleLabel);
 			hp4.setStyleName(style.activeIcon());
 			footerLeftSide.add(hp4);
 		}
 		
 		try {
 			if(_domeo.getAnnotationAccessManager().isAnnotationSetPublic(_set)) {
 				HorizontalPanel hp1 = new HorizontalPanel();
 				final Image imgPublic = new Image(Domeo.resources.publicLittleIcon());
 				imgPublic.addClickHandler(new ClickHandler() {
 					@Override
 					public void onClick(ClickEvent event) {
 						AnnotationAccessPopup popup = new AnnotationAccessPopup(_domeo, _this, _set);
 						popup.show(imgPublic.getAbsoluteLeft(), imgPublic.getAbsoluteTop());
 					}
 				});
 				hp1.add(imgPublic);
 				Label publicLabel = new Label("Public");
 				publicLabel.setStyleName(style.spacedLabel());
 				hp1.add(publicLabel);
 				hp1.setStyleName(style.activeIcon());
 				footerLeftSide.add(hp1);
 			} else if(_domeo.getAnnotationAccessManager().isAnnotationSetGroups(_set)) {
 				HorizontalPanel hp1 = new HorizontalPanel();
 				final Image imgPublic = new Image(Domeo.resources.friendsLittleIcon());
 				imgPublic.addClickHandler(new ClickHandler() {
 					@Override
 					public void onClick(ClickEvent event) {
 						AnnotationAccessPopup popup = new AnnotationAccessPopup(_domeo, _this, _set);
 						popup.show(imgPublic.getAbsoluteLeft(), imgPublic.getAbsoluteTop());
 					}
 				});
 				hp1.add(imgPublic);
 				Label publicLabel = new Label("Groups");
 				publicLabel.setStyleName(style.spacedLabel());
 				hp1.add(publicLabel);
 				hp1.setStyleName(style.activeIcon());
 				footerLeftSide.add(hp1);
 				
 				try {
 					// List groups
 					access.add(new HTML("<b>Accessible to</b>"));
 					
 					if(_domeo.getAnnotationAccessManager().getAnnotationSetGroups(_set)!=null) {
 						for(IUserGroup group: _domeo.getAnnotationAccessManager().getAnnotationSetGroups(_set)) {
 							access.add(new HTML(group.getName() + " - " + group.getDescription()));
 						}
 					} else {
 						access.add(new Label("Groups missing!"));
 					}
 				} catch(Exception e) {
 					access.add(new Label("Exception " + e.getMessage()));
 				}
 				
 			} else if(_domeo.getAnnotationAccessManager().getAnnotationSetAccess(_set).equals(_domeo.getAgentManager().getUserPerson().getUri())) {
 				HorizontalPanel hp1 = new HorizontalPanel();
 				final Image imgPublic = new Image(Domeo.resources.privateLittleIcon());
 				imgPublic.addClickHandler(new ClickHandler() {
 					@Override
 					public void onClick(ClickEvent event) {
 						AnnotationAccessPopup popup = new AnnotationAccessPopup(_domeo, _this, _set);
 						popup.show(imgPublic.getAbsoluteLeft(), imgPublic.getAbsoluteTop());
 					}
 				});
 				hp1.add(imgPublic);
 				Label publicLabel = new Label("Private");
 				publicLabel.setStyleName(style.spacedLabel());
 				hp1.add(publicLabel);
 				hp1.setStyleName(style.activeIcon());
 				footerLeftSide.add(hp1);
 			}
 		} catch(Exception e) {
 			footerLeftSide.add(new Label("Exception (access)"));
 		}
 		
 		try {
 		    if(_set.getImportedFrom()!=null) {
 		        display.add(new HTML("Imported from: " + _set.getImportedFrom().getName()));
 		        //add all the checkboxes
 		    }
 		} catch(Exception e) {
 		    display.add(new Label("Exception (display)"));
         }
 				
 		if(_set.getIsLocked()) {
 			HorizontalPanel hp3 = new HorizontalPanel();
 			final Image imgRights = new Image(Domeo.resources.readOnlyLittleIcon());
 			imgRights.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					AnnotationRightsPopup popup = new AnnotationRightsPopup(_domeo, _this, _set);
 					popup.show(imgRights.getAbsoluteLeft(), imgRights.getAbsoluteTop());
 				}
 			});
 			hp3.add(imgRights);
 			Label readOnlyLabel = new Label("Locked");
 			readOnlyLabel.setStyleName(style.spacedLabel());
 			hp3.add(readOnlyLabel);
 			hp3.setStyleName(style.activeIcon());
 			footerLeftSide.add(hp3);
 		} else {
 			HorizontalPanel hp3 = new HorizontalPanel();
 			final Image imgRights = new Image(Domeo.resources.readWriteLittleIcon());
 			imgRights.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					AnnotationRightsPopup popup = new AnnotationRightsPopup(_domeo, _this, _set);
 					popup.show(imgRights.getAbsoluteLeft(), imgRights.getAbsoluteTop());
 				}
 			});
 			hp3.add(imgRights);
 			Label readOnlyLabel = new Label("Unlocked");
 			readOnlyLabel.setStyleName(style.spacedLabel());
 			hp3.add(readOnlyLabel);
 			hp3.setStyleName(style.activeIcon());
 			footerLeftSide.add(hp3);
 		}
 		
 		HorizontalPanel hp5= new HorizontalPanel();
 		final Image deleteIcon = new Image(Domeo.resources.deleteLittleIcon());
 		deleteIcon.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				_domeo.getAnnotationPersistenceManager().removeAnnotationSet(_set);
 			}
 		});
 		
 		hp5.add(deleteIcon);
 		Label deleteLabel = new Label("Delete");
 		deleteLabel.setStyleName(style.spacedLabel());
 		hp5.add(deleteLabel);
 		hp5.setStyleName(style.activeIcon());
 		footerRightSide.add(hp5);
 		
 		if(_set.getAnnotations().size()>0) {
 			Label itemsNumberText = new Label("Explore the set content of " + _set.getAnnotations().size() +(_set.getAnnotations().size()==1?" item":" items"));
 			itemsNumberText.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					_domeo.displayAnnotationOfSet(_set);
 					// event.stopPropagation();
 				}
 			});
 			items.clear();
 			items.add(itemsNumberText);
 		} else {
 			items.clear();
 			items.add(new HTML("No items"));
 		}
 	}
 	
 	private void filterShow(ClickEvent event) {
 
 	    if(event.getSource()==showCuratedFilter) {
 	        if(showCuratedFilter.getValue()) {
 	            showRejectedFilter.setValue(true);
 	            showAcceptedFilter.setValue(true);
 	            showBroadFilter.setValue(true);
 	        } else {
 	            showRejectedFilter.setValue(false);
                 showAcceptedFilter.setValue(false);
                 showBroadFilter.setValue(false);
 	        }
 	    } else {
     	    if(!showRejectedFilter.getValue() || !showAcceptedFilter.getValue() || !showBroadFilter.getValue()) {
     	        showCuratedFilter.setValue(false);
     	    } else {
     	        showCuratedFilter.setValue(true);
     	    }
 	    }
 	    
 	    Map<Long, Boolean> showMap = new HashMap<Long, Boolean>();
 	    for(MAnnotation ann: _set.getAnnotations()) {
 	        boolean show = false;
 	        if(ann.getCreator() instanceof ISoftware) {
 	            boolean found = false;
     	        for(MAnnotation curation: ann.getAnnotatedBy()) {
     	            if(curation instanceof MCurationToken && curation.getCreator().getUri().equals(_domeo.getAgentManager().getUserPerson().getUri())) {
     	                if(((MCurationToken)curation).getStatus().equals(MCurationToken.CORRECT)) {
     	                    found = true;
     	                    if(showAcceptedFilter.getValue()) {
     	                        show = true;
     	                    }
     	                } else if(((MCurationToken)curation).getStatus().equals(MCurationToken.CORRECT_BROAD)) {
     	                    found = true;
     	                    if(showBroadFilter.getValue()) {
                                 show = true;
                             }
     	                }  else if(((MCurationToken)curation).getStatus().equals(MCurationToken.INCORRECT)) {
     	                    found = true;
                             if(showRejectedFilter.getValue()) {
                                 show = true;
                             }
                         }              
     	            }
     	        }
     	        if(!found && showNotCuratedFilter.getValue()) {
     	            show = true;
     	        }
 	        } else {
 	            if(showAddedFilter.getValue()) show=true;
 	        }
 	        showMap.put(ann.getLocalId(), show);
 	    }
 
 	    if(checkHighlight.getValue()) {
     	    checkHighlight.setValue(false);
             _domeo.getContentPanel().getAnnotationFrameWrapper()
                 .manageSetEmphasis(_set, checkHighlight.getValue());
             _parent.refresh();
 	    }
 	    
 	    _domeo.getContentPanel().getAnnotationFrameWrapper().modifyDisplayAnnotation(_set, showMap);
 	}
 }
