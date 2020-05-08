 package org.mindinformatics.gwt.domeo.client.ui.toolbar;
 
 import org.mindinformatics.gwt.domeo.client.Domeo;
 import org.mindinformatics.gwt.domeo.client.IDomeo;
 import org.mindinformatics.gwt.domeo.client.Resources;
 import org.mindinformatics.gwt.domeo.client.ui.plugins.PluginsViewerPanel;
 import org.mindinformatics.gwt.domeo.client.ui.preferences.PreferencesViewerPanel;
 import org.mindinformatics.gwt.domeo.client.ui.toolbar.addressbar.AddressBarPanel;
 import org.mindinformatics.gwt.domeo.component.sharing.ui.SharingOptionsViewer;
 import org.mindinformatics.gwt.domeo.component.textmining.ui.TextMiningServicePicker;
 import org.mindinformatics.gwt.framework.component.IInitializableComponent;
 import org.mindinformatics.gwt.framework.component.preferences.src.BooleanPreference;
 import org.mindinformatics.gwt.framework.component.ui.glass.EnhancedGlassPanel;
 import org.mindinformatics.gwt.framework.component.ui.toolbar.ToolbarHorizontalTogglePanel;
 import org.mindinformatics.gwt.framework.component.ui.toolbar.ToolbarItemsGroup;
 import org.mindinformatics.gwt.framework.component.ui.toolbar.ToolbarPanel;
 import org.mindinformatics.gwt.framework.component.ui.toolbar.ToolbarPopup;
 import org.mindinformatics.gwt.framework.component.ui.toolbar.ToolbarSimplePanel;
 import org.mindinformatics.gwt.framework.component.users.ui.UserAccountViewerPanel;
 import org.mindinformatics.gwt.framework.src.Application;
 import org.mindinformatics.gwt.framework.src.ApplicationResources;
 import org.mindinformatics.gwt.framework.src.ApplicationUtils;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.IFrameElement;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
 
 /**
  * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
  */
 public class DomeoToolbarPanel extends Composite implements IInitializableComponent {
 
 	public static final ToolbarResources localResources = 
 		GWT.create(ToolbarResources.class);
 	
 	public static final String DOCUMENT_COMMANDS_GROUP = "Document commands";
 	private static final String POPUP_WIDTH = "170";
 	
 	// By contract 
 	private IDomeo _domeo;
 	
 	private ToolbarPanel toolbar;
 	private ToolbarItemsGroup commandsGroup;
 	
 	private AddressBarPanel addressBarPanel;
 	private ToolbarHorizontalTogglePanel annotateButtonPanel;
 	private ToolbarHorizontalTogglePanel annotateMultipleButtonPanel;
 	private ToolbarHorizontalTogglePanel highlightButtonPanel;
 	
 	private ToolbarHorizontalTogglePanel analyzeButtonPanel;
 	private ToolbarSimplePanel shareButton;
 	
 	public DomeoToolbarPanel(IDomeo application) {
 		_domeo = application;
 		
 		_domeo.getLogger().debug(this.getClass().getName(), 
 			"Creating the Toolbar...");
 		
 		Resources _resources = Domeo.resources;
 		final ApplicationResources _applicationResources = Application.applicationResources;
 		localResources.toolbarCss().ensureInjected();
 		
 		toolbar = new ToolbarPanel(_domeo);
 		
 		ToolbarSimplePanel homepageButton = new ToolbarSimplePanel(
 				_domeo, new ClickHandler() {
 					@Override
 					public void onClick(ClickEvent event) {
 						Window.Location.assign(ApplicationUtils.getUrlBase(Window.Location.getHref()));
 						toolbar.disableToolbarItems();
 					}
 				}, _applicationResources.homeLittleIcon().getSafeUri().asString(), "Homepage");
 		
 		addressBarPanel = new AddressBarPanel(_domeo);
 		
 		addressBarPanel.initializeHandlers(
 			new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					if(addressBarPanel.getAddress().length()>0) 
 						_domeo.attemptContentLoading(addressBarPanel.getAddress());
 				}
 			}, 
 			new KeyPressHandler() {
 				@Override
 				public void onKeyPress(KeyPressEvent event) {
 					int charCode = event.getUnicodeCharCode();
 					if (charCode == 0) {
 						// it's probably Firefox
 					    int keyCode = event.getNativeEvent().getKeyCode();
 					    if (keyCode == KeyCodes.KEY_ENTER) {
 					    	if(addressBarPanel.getAddress().length()>0) 
 					    		_domeo.attemptContentLoading(addressBarPanel.getAddress());
 					    }
 					} else if (charCode == KeyCodes.KEY_ENTER) {
 						if(addressBarPanel.getAddress().length()>0) 
 							_domeo.attemptContentLoading(addressBarPanel.getAddress());
 					}
 				}
 			},
 			new SelectionHandler<Suggestion>() {
 				@Override
 				public void onSelection(SelectionEvent<Suggestion> event) {
 					_domeo.attemptContentLoading(event.getSelectedItem().getReplacementString());
 				}
 			});
 		
 		highlightButtonPanel = new ToolbarHorizontalTogglePanel(
 				_domeo, new ClickHandler() {
 					@Override
 					public void onClick(ClickEvent event) {
 						//_domeo.updateHighlightMode();
 						
 						if(isManualHighlightSelected()) 
 							_domeo.getLogger().command(this.getClass().getName(), "Enabling manual highlight");
 						else _domeo.getLogger().command(this.getClass().getName(), "Disabling manual highlight");
 						if(isManualAnnotationSelected()) {
 							_domeo.getLogger().debug(this, "Disabling manual annotation");
 							deselectManualAnnotation();
 						}
 						if(isManualMultipleAnnotationSelected()) {
 							if(_domeo.getClipboardManager().getBufferedAnnotation().size()>0) {
 								_domeo.getLogger().debug(this, "Performing manual multiple highlight");
 								_domeo.getContentPanel().getAnnotationFrameWrapper().performMultipleTargetsHighlight(_domeo.getClipboardManager().getBufferedAnnotation());
 							}
 							deselectManualMultipleAnnotation();
 						}
 						if(_domeo.getContentPanel().getAnnotationFrameWrapper().anchorNode!=null) _domeo.getContentPanel().getAnnotationFrameWrapper().annotate();
 					}
 				}, _resources.highlightLittleIcon(), 
 				_resources.highlightLittleColorIcon(), "Highlight", "Highlight");
 		
 		annotateButtonPanel = new ToolbarHorizontalTogglePanel(
 			_domeo, new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					//_domeo.updateAnnotationMode();
 					
 					if(isManualAnnotationSelected()) 
 						_domeo.getLogger().command(this.getClass().getName(), "Enabling manual annotation");
 					else _domeo.getLogger().command(this, "Disabling manual annotation");
 					if(isManualHighlightSelected()) {
 						_domeo.getLogger().debug(this, "Disabling manual highlight");
 						deselectManualHighlight();
 					}
 					if(isManualMultipleAnnotationSelected()) {
 						if(_domeo.getClipboardManager().getBufferedAnnotation().size()>0) {
 							_domeo.getLogger().debug(this, "Performing manual multiple annotation");
 							_domeo.getContentPanel().getAnnotationFrameWrapper().performMultipleTargetsAnnotation(
 									new ClickHandler() {
 										@Override
 										public void onClick(ClickEvent event) {
 											deselectManualAnnotation();
 											selectManualMultipleAnnotation();
 										}
 									}
 							);
 						}
 						deselectManualMultipleAnnotation();
 					}
					if(_domeo.getContentPanel().getAnnotationFrameWrapper().anchorNode!=null) _domeo.getContentPanel().getAnnotationFrameWrapper().annotate();
 				}
 			}, _resources.domeoAnnotateIcon(), 
 			_resources.domeoAnnotateColorIcon(), "Annotate", "Annotate");
 		
 		if(((BooleanPreference)_domeo.getPreferences().
 				getPreferenceItem(Application.class.getName(), Domeo.PREF_ANN_MULTIPLE_TARGETS))!=null &&
 				((BooleanPreference)_domeo.getPreferences().getPreferenceItem(Application.class.getName(), Domeo.PREF_ANN_MULTIPLE_TARGETS)).getValue()) {
 			annotateMultipleButtonPanel = new ToolbarHorizontalTogglePanel(
 					_domeo, new ClickHandler() {
 						@Override
 						public void onClick(ClickEvent event) {
 							//_domeo.updateAnnotationMode();
 							
 							if(isManualMultipleAnnotationSelected()) 
 								_domeo.getLogger().command(this.getClass().getName(), "Enabling multiple manual annotation");
 							else {
 								_domeo.getContentPanel().getAnnotationFrameWrapper().clearTemporaryAnnotations();
 								_domeo.getLogger().command(this, "Disabling multiple manual annotation");
 							}
 							if(isManualHighlightSelected()) {
 								_domeo.getLogger().debug(this, "Disabling multiple manual highlight");
 								deselectManualHighlight();
 							}
 							if(isManualAnnotationSelected()) {
 								_domeo.getLogger().debug(this, "Disabling manual annotation");
 								deselectManualAnnotation();
 							}
 						}
 					}, _resources.domeoClipIcon(), 
 					_resources.domeoClipColorIcon(), "Clip", "Clip");
 		}
 		
 
 //		ToolbarHorizontalPanel analyzeButtonPanel = new ToolbarHorizontalPanel(
 //			_domeo, new ClickHandler() {
 //				@Override
 //				public void onClick(ClickEvent event) {
 //					Window.alert("Click on Analyze");
 //				}
 //			}, _applicationResources.runLittleIcon().getSafeUri().asString(), "Analyze", "Analyze");
 		
 		analyzeButtonPanel = new ToolbarHorizontalTogglePanel(
 				_domeo, new ClickHandler() {
 					@Override
 					public void onClick(ClickEvent event) {
 						_domeo.getLogger().debug(this, "Beginning textminning...");
 						_domeo.getProgressPanelContainer().setProgressMessage("Textmining selection...");
 						
 						// TODO Hidious!!!!!
 						IFrameElement iframe = IFrameElement.as(_domeo.getContentPanel().getAnnotationFrameWrapper().getFrame().getElement());
 						final Document frameDocument = iframe.getContentDocument();
 						_domeo.getContentPanel().getAnnotationFrameWrapper().getSelectionText(_domeo.getContentPanel().getAnnotationFrameWrapper(), frameDocument);
 						
 						if(_domeo.getContentPanel().getAnnotationFrameWrapper().matchText!=null && _domeo.getContentPanel().getAnnotationFrameWrapper().matchText.length()>2) { 
 							TextMiningServicePicker tmsp = new TextMiningServicePicker(_domeo);
 							new EnhancedGlassPanel(_domeo, tmsp, tmsp.getTitle(), 800, false, false, false);
 						} else {
 							_domeo.getLogger().debug(this, "No text to textmine...");
 							_domeo.getContentPanel().getAnnotationFrameWrapper().clearSelection();
 							_domeo.getToolbarPanel().deselectAnalyze();
 							_domeo.getProgressPanelContainer().setWarningMessage("No text has been selected for textmining!");
 						}
 					}
 				}, _applicationResources.runLittleIcon(),
 				_applicationResources.spinningIcon2(), "Analyze", "Analyze");
 		
 		shareButton = new ToolbarSimplePanel(
 			_domeo, new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					ToolbarPopup popup = new ToolbarPopup(_domeo, "Share", Domeo.resources.shareIcon().getSafeUri().asString());
 					popup.setWidth(POPUP_WIDTH + "px");
 					popup.setPopupPosition(Window.getClientWidth()-(Integer.parseInt(POPUP_WIDTH)+48), -6); //25
 					popup.setAnimationEnabled(false);
 					popup.addButtonPanel(_applicationResources.allLinkIcon().getSafeUri().asString(), "Current Workspace", new ClickHandler() {
 						@Override
 						public void onClick(ClickEvent event) {
 							if(!_domeo.isLocalResources() && !_domeo.isHostedMode() && _domeo.getPersistenceManager().isResourceLoaded()) {
 								SharingOptionsViewer lwp = new SharingOptionsViewer(_domeo);
 								new EnhancedGlassPanel(_domeo, lwp, lwp.getTitle(), 440, false, false, false);
 							}
 						}
 					});
 					popup.show();
 				}
 			}, _applicationResources.shareIcon().getSafeUri().asString(), "Sharing");		
 		
 		ToolbarSimplePanel settingsButton = new ToolbarSimplePanel(
 			_domeo, new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					ToolbarPopup popup = new ToolbarPopup(_domeo, "Settings", Domeo.resources.settingsLittleIcon().getSafeUri().asString());
 					popup.setWidth(POPUP_WIDTH + "px");
 					popup.setPopupPosition(Window.getClientWidth()-(Integer.parseInt(POPUP_WIDTH)+27), -6); //25
 					popup.setAnimationEnabled(false);
 					popup.addButtonPanel(_applicationResources.userLittleIcon().getSafeUri().asString(), "Account", new ClickHandler() {
 						@Override
 						public void onClick(ClickEvent event) {
 							UserAccountViewerPanel lwp = new UserAccountViewerPanel(_domeo);
 							new EnhancedGlassPanel(_domeo, lwp, _domeo.getUserManager().getUser().getScreenName(), false, false, false);
 						}
 					});
 					popup.addButtonPanel(_applicationResources.preferencesLittleIcon().getSafeUri().asString(), "Preferences", new ClickHandler() {
 						@Override
 						public void onClick(ClickEvent event) {
 							PreferencesViewerPanel lwp = new PreferencesViewerPanel(_domeo);
 							new EnhancedGlassPanel(_domeo, lwp, lwp.getTitle(), false, false, false);
 						}
 					});
 					popup.addButtonPanel(_applicationResources.pluginsLittleIcon().getSafeUri().asString(), "Add-ons and Profiles", new ClickHandler() {
 						@Override
 						public void onClick(ClickEvent event) {
 							PluginsViewerPanel lwp = new PluginsViewerPanel(_domeo);
 							new EnhancedGlassPanel(_domeo, lwp, lwp.getTitle(), 850, false, false, false);
 						}
 					});
 					popup.show();
 				}
 			}, _applicationResources.settingsLittleIcon().getSafeUri().asString(), "Preferences");
 	
 		ToolbarSimplePanel helpButton = new ToolbarSimplePanel(
 			_domeo, new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					ToolbarPopup popup = new ToolbarPopup(_domeo, "Help", Domeo.resources.helpLittleIcon().getSafeUri().asString());
 					popup.setWidth(POPUP_WIDTH + "px");
 					popup.setPopupPosition(Window.getClientWidth()-(Integer.parseInt(POPUP_WIDTH)+12), -6); //25
 					popup.setAnimationEnabled(false);
 					popup.addButtonPanel("", "Report an issue", new ClickHandler() {
 						@Override
 						public void onClick(ClickEvent event) {
 							Window.alert("Report an issue");
 						}
 					});
 					popup.addButtonPanel("", "Domeo help", new ClickHandler() {
 						@Override
 						public void onClick(ClickEvent event) {
 							Window.alert("Display online resources");
 						}
 					});
 					popup.addButtonPanel("", "About Domeo", new ClickHandler() {
 						@Override
 						public void onClick(ClickEvent event) {
 							Window.alert("About " + Domeo.APP_NAME + " - " + Domeo.APP_VERSION_LABEL);
 						}
 					});
 					popup.show();
 				}
 			}, _applicationResources.helpLittleIcon().getSafeUri().asString(), "Help");
 		
 		ToolbarSimplePanel saveButton = new ToolbarSimplePanel(
 				_domeo, new ClickHandler() {
 					@Override
 					public void onClick(ClickEvent event) {
 						_domeo.getLogger().command(this, "Saving annotation...");
 						_domeo.getAnnotationPersistenceManager().saveAnnotation();
 						if(_domeo.isHostedMode()) 
 							_domeo.getAnnotationPersistenceManager().mockupSavingOfTheAnnotation();
 						//toolbar.disableToolbarItems();
 					}
 				}, _resources.saveMediumIcon().getSafeUri().asString(), "Save");
 		
 		toolbar.addToLeftPanel(homepageButton, "22");
 		toolbar.addToLeftPanel(addressBarPanel);
 		//toolbar.addToLeftPanel(annotateButtonPanel);
 		//toolbar.addToLeftPanel(analyzeButtonPanel);
 		
 		
 		commandsGroup = new ToolbarItemsGroup(DOCUMENT_COMMANDS_GROUP);
 		commandsGroup.addItem(highlightButtonPanel);
 		if(((BooleanPreference)_domeo.getPreferences().
 				getPreferenceItem(Application.class.getName(), Domeo.PREF_ANN_MULTIPLE_TARGETS))!=null &&
 				((BooleanPreference)_domeo.getPreferences().getPreferenceItem(Application.class.getName(), Domeo.PREF_ANN_MULTIPLE_TARGETS)).getValue()) {
 			commandsGroup.addItem(annotateMultipleButtonPanel);
 		}
 		commandsGroup.addItem(annotateButtonPanel);
 		
 		//commandsGroup.addItem(analyzeButtonPanel);
 		//commandsGroup.addItem(analyzeButtonPanel2);
 		commandsGroup.addItem(analyzeButtonPanel);
 		commandsGroup.addItem(saveButton);
 		
 		toolbar.registerGroup(commandsGroup);
 		
 		SimplePanel sp = new SimplePanel();
 		sp.setWidth("100px");
 		
 		toolbar.addToRightPanel(sp);
 		toolbar.addToRightPanel(shareButton);
 		toolbar.addToRightPanel(settingsButton);
 		toolbar.addToRightPanel(helpButton);
 		
 		initWidget(toolbar);
 	}
 	
 	public AddressBarPanel getAddressBarPanel() {
 		return addressBarPanel;
 	}
 	
 	public void deselectManualAnnotation() {
 		annotateButtonPanel.deselect();
 	}
 	
 	public void selectManualMultipleAnnotation() {
 		annotateMultipleButtonPanel.select();
 	}
 	
 	public void deselectManualMultipleAnnotation() {
 		annotateMultipleButtonPanel.deselect();
 	}
 	
 	public void deselectManualHighlight() {
 		highlightButtonPanel.deselect();
 	}
 	
 	public boolean isManualAnnotationSelected() {
 		return annotateButtonPanel.isSelected();
 	}
 	
 	public boolean isManualMultipleAnnotationSelected() {
 		return annotateMultipleButtonPanel.isSelected();
 	}
 	
 	public boolean isManualHighlightSelected() {
 		return highlightButtonPanel.isSelected();
 	}
 	
 	public void deselectAnalyze() {
 		analyzeButtonPanel.deselect();
 	}
 	
 	public void attachGroup(String groupName) {
 		toolbar.attachGroup(groupName);
 	}
 	
 	public void detachGroup(String groupName) {
 		toolbar.detachGroup(groupName);
 	}
 	
 	public void hideCommands() {
 		toolbar.hideGroup(DomeoToolbarPanel.DOCUMENT_COMMANDS_GROUP);
 	}
 	
 	public void disable() {
 		toolbar.disableToolbarItems();
 	}
 
 	@Override
 	public void init() {
 		toolbar.init();
 		toolbar.detachGroup(DomeoToolbarPanel.DOCUMENT_COMMANDS_GROUP);
 	}
 }
